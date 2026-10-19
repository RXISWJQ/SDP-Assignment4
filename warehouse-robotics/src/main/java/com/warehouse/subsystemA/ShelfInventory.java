package com.warehouse.subsystemA;

public class ShelfInventory {
    private int availableStock;

    public ShelfInventory(int availableStock) {
        this.availableStock = availableStock;
    }

    public boolean checkAvailability(int requested) {
        return availableStock >= requested;
    }

    public void deduct(int qty) {
        if (!checkAvailability(qty)) {
            throw new IllegalStateException("Not enough stock on shelves");
        }
        availableStock -= qty;
    }

    public void restore(int qty) {
        availableStock += qty;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}