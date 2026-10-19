package com.warehouse.subsystemA;

public class OrderBatcher {
    public boolean validateBatch(String orderId, int quantity) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid Order ID");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        return true;
    }
}