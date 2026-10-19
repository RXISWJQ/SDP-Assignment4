package com.warehouse.subsystemC;

public class ChargingStationManager {
    public boolean allocateStation(String stationId, boolean simulatePowerOverload) {
        if (simulatePowerOverload) {
            throw new RuntimeException("Charging station error: Power grid overload detected");
        }
        return true;
    }
}