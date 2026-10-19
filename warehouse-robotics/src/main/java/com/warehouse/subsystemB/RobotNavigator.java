package com.warehouse.subsystemB;

public class RobotNavigator {
    public boolean planPath(String robotId, boolean simulateCollision) {
        if (simulateCollision) {
            throw new RuntimeException("Robot navigation failure: Path blocked or collision risk detected");
        }
        return true;
    }
}