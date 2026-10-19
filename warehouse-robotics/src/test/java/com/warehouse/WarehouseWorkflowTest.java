package com.warehouse;

import com.warehouse.db.DatabaseManager;
import com.warehouse.facade.ChainStatus;
import com.warehouse.facade.WarehouseWorkflowFacade;
import com.warehouse.subsystemA.*;
import com.warehouse.subsystemB.*;
import com.warehouse.subsystemC.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WarehouseWorkflowTest {
    private DatabaseManager dbManager;
    private ShelfInventory inventory;
    private WarehouseWorkflowFacade workflowFacade;
    private InventoryPersistenceGateway gatewayA;
    private ProcessingPersistenceGateway gatewayB;
    private FinalizationPersistenceGateway gatewayC;

    @BeforeEach
    public void setUp() {
        dbManager = new DatabaseManager("warehouse_test.db");
        dbManager.clearAllTables();
        inventory = new ShelfInventory(100);
        workflowFacade = new WarehouseWorkflowFacade(dbManager, inventory);
        gatewayA = new InventoryPersistenceGateway(dbManager);
        gatewayB = new ProcessingPersistenceGateway(dbManager);
        gatewayC = new FinalizationPersistenceGateway(dbManager);
    }

    private void assertOrderState(String orderId, ChainStatus expectedChain, InventoryReservationStatus expectedA, RobotTaskStatus expectedB, DispatchRecordStatus expectedC) {
        assertEquals(expectedChain, workflowFacade.getChainProgress(orderId));
        InventoryReservation a = gatewayA.findByOrderId(orderId);
        RobotTask b = gatewayB.findByOrderId(orderId);
        DispatchRecord c = gatewayC.findByOrderId(orderId);

        if (expectedA != null) assertEquals(expectedA, a.getStatus());
        if (expectedB != null) assertEquals(expectedB, b.getStatus());
        if (expectedC != null) assertEquals(expectedC, c.getStatus());
    }

    @Test
    public void testHappyPath() {
        boolean success = workflowFacade.executeFulfillmentChain("ORD-101", "ITEM-A", 5, "ROBOT-1", "STATION-1", false, false);
        assertTrue(success);
        assertOrderState("ORD-101", ChainStatus.COMPLETED, InventoryReservationStatus.RESERVED, RobotTaskStatus.COMPLETED, DispatchRecordStatus.CONFIRMED);
        assertEquals(95, inventory.getAvailableStock());
    }

    @Test
    public void testBFailsAfterACommitted() {
        boolean success = workflowFacade.executeFulfillmentChain("ORD-102", "ITEM-A", 5, "ROBOT-1", "STATION-1", true, false);
        assertFalse(success);
        assertOrderState("ORD-102", ChainStatus.COMPENSATED, InventoryReservationStatus.CANCELLED, null, null);
        assertEquals(100, inventory.getAvailableStock());
    }

    @Test
    public void testCFailsAfterAAndBCommitted() {
        boolean success = workflowFacade.executeFulfillmentChain("ORD-103", "ITEM-A", 5, "ROBOT-1", "STATION-1", false, true);
        assertFalse(success);
        assertOrderState("ORD-103", ChainStatus.COMPENSATED, InventoryReservationStatus.CANCELLED, RobotTaskStatus.FAILED, DispatchRecordStatus.ABORTED);
        assertEquals(100, inventory.getAvailableStock());
    }

    @Test
    public void testPersistenceReloadMidChain() {
        workflowFacade.saveChainProgress("ORD-104", ChainStatus.PENDING_C);
        InventoryReservation resA = new InventoryReservation("ORD-104", "ITEM-A", 10, InventoryReservationStatus.RESERVED);
        gatewayA.save(resA);
        RobotTask taskB = new RobotTask("TASK-104", "ORD-104", "ROBOT-1", RobotTaskStatus.DISPATCHED);
        gatewayB.save(taskB);

        DatabaseManager dbManager2 = new DatabaseManager("warehouse_test.db");
        WarehouseWorkflowFacade workflowFacade2 = new WarehouseWorkflowFacade(dbManager2, inventory);

        workflowFacade2.compensateChain("ORD-104");

        InventoryPersistenceGateway gatewayA2 = new InventoryPersistenceGateway(dbManager2);
        ProcessingPersistenceGateway gatewayB2 = new ProcessingPersistenceGateway(dbManager2);

        assertEquals(ChainStatus.COMPENSATED, workflowFacade2.getChainProgress("ORD-104"));
        assertEquals(InventoryReservationStatus.CANCELLED, gatewayA2.findByOrderId("ORD-104").getStatus());
        assertEquals(RobotTaskStatus.FAILED, gatewayB2.findByOrderId("ORD-104").getStatus());
    }
}