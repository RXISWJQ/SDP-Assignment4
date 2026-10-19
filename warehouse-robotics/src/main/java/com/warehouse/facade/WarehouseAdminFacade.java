package com.warehouse.facade;

import com.warehouse.db.DatabaseManager;
import com.warehouse.subsystemA.*;
import com.warehouse.subsystemB.*;
import com.warehouse.subsystemC.*;

public class WarehouseAdminFacade {
    private final InventoryPersistenceGateway gatewayA;
    private final ProcessingPersistenceGateway gatewayB;
    private final FinalizationPersistenceGateway gatewayC;
    private final WarehouseWorkflowFacade workflowFacade;

    public WarehouseAdminFacade(DatabaseManager dbManager, WarehouseWorkflowFacade workflowFacade) {
        this.gatewayA = new InventoryPersistenceGateway(dbManager);
        this.gatewayB = new ProcessingPersistenceGateway(dbManager);
        this.gatewayC = new FinalizationPersistenceGateway(dbManager);
        this.workflowFacade = workflowFacade;
    }

    public String inspectOrderState(String orderId) {
        InventoryReservation resA = gatewayA.findByOrderId(orderId);
        RobotTask taskB = gatewayB.findByOrderId(orderId);
        DispatchRecord dispC = gatewayC.findByOrderId(orderId);
        ChainStatus progress = workflowFacade.getChainProgress(orderId);

        StringBuilder sb = new StringBuilder();
        sb.append("Chain Progress: ").append(progress).append("\n");
        sb.append("Subsystem A Status: ").append(resA != null ? resA.getStatus() : "N/A").append("\n");
        sb.append("Subsystem B Status: ").append(taskB != null ? taskB.getStatus() : "N/A").append("\n");
        sb.append("Subsystem C Status: ").append(dispC != null ? dispC.getStatus() : "N/A");
        return sb.toString();
    }

    public void triggerManualCompensation(String orderId) {
        workflowFacade.compensateChain(orderId);
    }
}