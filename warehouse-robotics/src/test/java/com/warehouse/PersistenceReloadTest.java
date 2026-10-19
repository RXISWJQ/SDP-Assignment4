package com.warehouse;

import com.warehouse.db.DatabaseManager;
import com.warehouse.facade.ChainStatus;
import com.warehouse.facade.WarehouseWorkflowFacade;
import com.warehouse.subsystemA.InventoryPersistenceGateway;
import com.warehouse.subsystemA.InventoryReservation;
import com.warehouse.subsystemA.InventoryReservationStatus;
import com.warehouse.subsystemA.ShelfInventory;
import com.warehouse.subsystemB.ProcessingPersistenceGateway;
import com.warehouse.subsystemB.RobotTask;
import com.warehouse.subsystemB.RobotTaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersistenceReloadTest {
    private DatabaseManager dbManager;
    private ShelfInventory inventory;

    @BeforeEach
    public void setUp() {
        dbManager = new DatabaseManager("warehouse_reload_test.db");
        dbManager.clearAllTables();
        inventory = new ShelfInventory(100);
    }

    @Test
    public void testPersistenceReloadMidChain() {
        WarehouseWorkflowFacade workflowFacade1 = new WarehouseWorkflowFacade(dbManager, inventory);
        workflowFacade1.saveChainProgress("ORD-RELOAD-1", ChainStatus.PENDING_C);
        
        InventoryPersistenceGateway gatewayA1 = new InventoryPersistenceGateway(dbManager);
        gatewayA1.save(new InventoryReservation("ORD-RELOAD-1", "ITEM-A", 10, InventoryReservationStatus.RESERVED));

        ProcessingPersistenceGateway gatewayB1 = new ProcessingPersistenceGateway(dbManager);
        gatewayB1.save(new RobotTask("TASK-RELOAD-1", "ORD-RELOAD-1", "ROBOT-1", RobotTaskStatus.DISPATCHED));

        DatabaseManager dbManagerRestarted = new DatabaseManager("warehouse_reload_test.db");
        WarehouseWorkflowFacade workflowFacadeRestarted = new WarehouseWorkflowFacade(dbManagerRestarted, inventory);

        workflowFacadeRestarted.compensateChain("ORD-RELOAD-1");

        InventoryPersistenceGateway gatewayA2 = new InventoryPersistenceGateway(dbManagerRestarted);
        ProcessingPersistenceGateway gatewayB2 = new ProcessingPersistenceGateway(dbManagerRestarted);

        assertEquals(ChainStatus.COMPENSATED, workflowFacadeRestarted.getChainProgress("ORD-RELOAD-1"));
        assertEquals(InventoryReservationStatus.CANCELLED, gatewayA2.findByOrderId("ORD-RELOAD-1").getStatus());
        assertEquals(RobotTaskStatus.FAILED, gatewayB2.findByOrderId("ORD-RELOAD-1").getStatus());
    }
}