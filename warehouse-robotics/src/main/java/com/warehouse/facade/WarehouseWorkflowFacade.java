package com.warehouse.facade;

import com.warehouse.db.DatabaseManager;
import com.warehouse.subsystemA.*;
import com.warehouse.subsystemB.*;
import com.warehouse.subsystemC.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WarehouseWorkflowFacade {
    private final DatabaseManager dbManager;
    private final OrderBatcher orderBatcher;
    private final ShelfInventory shelfInventory;
    private final InventoryPersistenceGateway gatewayA;

    private final RobotNavigator robotNavigator;
    private final ConveyorController conveyorController;
    private final ProcessingPersistenceGateway gatewayB;

    private final ChargingStationManager stationManager;
    private final DispatchLogger dispatchLogger;
    private final FinalizationPersistenceGateway gatewayC;

    public WarehouseWorkflowFacade(DatabaseManager dbManager,
                                   ShelfInventory shelfInventory) {
        this.dbManager = dbManager;
        this.orderBatcher = new OrderBatcher();
        this.shelfInventory = shelfInventory;
        this.gatewayA = new InventoryPersistenceGateway(dbManager);

        this.robotNavigator = new RobotNavigator();
        this.conveyorController = new ConveyorController();
        this.gatewayB = new ProcessingPersistenceGateway(dbManager);

        this.stationManager = new ChargingStationManager();
        this.dispatchLogger = new DispatchLogger();
        this.gatewayC = new FinalizationPersistenceGateway(dbManager);
    }

    public boolean executeFulfillmentChain(String orderId, String itemSku, int quantity,
                                           String robotId, String stationId,
                                           boolean failAtB, boolean failAtC) {
        orderBatcher.validateBatch(orderId, quantity);
        saveChainProgress(orderId, ChainStatus.PENDING_A);

        if (!shelfInventory.checkAvailability(quantity)) {
            throw new IllegalStateException("Subsystem A Failure: Insufficient stock");
        }
        shelfInventory.deduct(quantity);
        InventoryReservation reservation = new InventoryReservation(orderId, itemSku, quantity, InventoryReservationStatus.NEW);
        reservation.transitionTo(InventoryReservationStatus.RESERVED);
        gatewayA.save(reservation);
        saveChainProgress(orderId, ChainStatus.PENDING_B);

        try {
            robotNavigator.planPath(robotId, failAtB);
            conveyorController.dispatchConveyor(orderId);
            RobotTask task = new RobotTask("TASK-" + orderId, orderId, robotId, RobotTaskStatus.PENDING);
            task.transitionTo(RobotTaskStatus.DISPATCHED);
            gatewayB.save(task);
            saveChainProgress(orderId, ChainStatus.PENDING_C);
        } catch (Exception e) {
            compensateChain(orderId);
            return false;
        }

        try {
            stationManager.allocateStation(stationId, failAtC);
            DispatchRecord record = new DispatchRecord("DISP-" + orderId, orderId, stationId, DispatchRecordStatus.PENDING);
            record.transitionTo(DispatchRecordStatus.CONFIRMED);
            gatewayC.save(record);
            dispatchLogger.logDispatch("DISP-" + orderId, orderId);

            RobotTask task = gatewayB.findByOrderId(orderId);
            task.transitionTo(RobotTaskStatus.COMPLETED);
            gatewayB.save(task);

            saveChainProgress(orderId, ChainStatus.COMPLETED);
            return true;
        } catch (Exception e) {
            compensateChain(orderId);
            return false;
        }
    }

    public void compensateChain(String orderId) {
        ChainStatus status = getChainProgress(orderId);
        if (status == ChainStatus.PENDING_C) {
            DispatchRecord recordC = gatewayC.findByOrderId(orderId);
            if (recordC != null) {
                recordC.transitionTo(DispatchRecordStatus.ABORTED);
                gatewayC.save(recordC);
                dispatchLogger.logAborted(recordC.getDispatchId(), orderId);
            }

            RobotTask taskB = gatewayB.findByOrderId(orderId);
            if (taskB != null) {
                taskB.transitionTo(RobotTaskStatus.FAILED);
                gatewayB.save(taskB);
                conveyorController.stopConveyor(orderId);
            }

            InventoryReservation resA = gatewayA.findByOrderId(orderId);
            if (resA != null) {
                resA.transitionTo(InventoryReservationStatus.CANCELLED);
                gatewayA.save(resA);
                shelfInventory.restore(resA.getQuantity());
            }
            saveChainProgress(orderId, ChainStatus.COMPENSATED);
        } else if (status == ChainStatus.PENDING_B) {
            InventoryReservation resA = gatewayA.findByOrderId(orderId);
            if (resA != null) {
                resA.transitionTo(InventoryReservationStatus.CANCELLED);
                gatewayA.save(resA);
                shelfInventory.restore(resA.getQuantity());
            }
            saveChainProgress(orderId, ChainStatus.COMPENSATED);
        }
    }

    public void saveChainProgress(String orderId, ChainStatus status) {
        String query = "INSERT INTO chain_tracker (order_id, step_status) " +
                       "VALUES (?, ?) ON CONFLICT(order_id) DO UPDATE SET step_status=excluded.step_status";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, orderId);
            stmt.setString(2, status.name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save chain progress", e);
        }
    }

    public ChainStatus getChainProgress(String orderId) {
        String query = "SELECT step_status FROM chain_tracker WHERE order_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return ChainStatus.valueOf(rs.getString("step_status"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get chain progress", e);
        }
        return null;
    }
}