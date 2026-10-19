package com.warehouse.subsystemC;

public class DispatchRecord {
    private final String dispatchId;
    private final String orderId;
    private final String stationId;
    private DispatchRecordStatus status;

    public DispatchRecord(String dispatchId, String orderId, String stationId, DispatchRecordStatus status) {
        this.dispatchId = dispatchId;
        this.orderId = orderId;
        this.stationId = stationId;
        this.status = status;
    }

    public String getDispatchId() { return dispatchId; }
    public String getOrderId() { return orderId; }
    public String getStationId() { return stationId; }
    public DispatchRecordStatus getStatus() { return status; }

    public void transitionTo(DispatchRecordStatus newStatus) {
        if (this.status == DispatchRecordStatus.ABORTED && newStatus == DispatchRecordStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot confirm an aborted dispatch");
        }
        this.status = newStatus;
    }
}