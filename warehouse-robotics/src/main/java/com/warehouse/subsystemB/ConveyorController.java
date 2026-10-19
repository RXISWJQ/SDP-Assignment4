package com.warehouse.subsystemB;

public class ConveyorController {
    private boolean operational = true;

    public void setOperational(boolean operational) {
        this.operational = operational;
    }

    public void dispatchConveyor(String orderId) {
        if (!operational) {
            throw new IllegalStateException("Conveyor hardware failure or jam detected");
        }
    }

    public void stopConveyor(String orderId) {
    }
}