package com.warehouse.subsystemA;

public class InventoryReservation {
    private final String orderId;
    private final String itemSku;
    private final int quantity;
    private InventoryReservationStatus status;

    public InventoryReservation(String orderId, String itemSku, int quantity, InventoryReservationStatus status) {
        this.orderId = orderId;
        this.itemSku = itemSku;
        this.quantity = quantity;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public String getItemSku() { return itemSku; }
    public int getQuantity() { return quantity; }
    public InventoryReservationStatus getStatus() { return status; }

    public void transitionTo(InventoryReservationStatus newStatus) {
        if (this.status == InventoryReservationStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change status from CANCELLED");
        }
        if (this.status == InventoryReservationStatus.RESERVED && newStatus == InventoryReservationStatus.NEW) {
            throw new IllegalStateException("Illegal transition from RESERVED to NEW");
        }
        this.status = newStatus;
    }
}