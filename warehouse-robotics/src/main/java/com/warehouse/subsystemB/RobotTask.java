package com.warehouse.subsystemB;

public class RobotTask {
    private final String taskId;
    private final String orderId;
    private final String robotId;
    private RobotTaskStatus status;

    public RobotTask(String taskId, String orderId, String robotId, RobotTaskStatus status) {
        this.taskId = taskId;
        this.orderId = orderId;
        this.robotId = robotId;
        this.status = status;
    }

    public String getTaskId() { return taskId; }
    public String getOrderId() { return orderId; }
    public String getRobotId() { return robotId; }
    public RobotTaskStatus getStatus() { return status; }

    public void transitionTo(RobotTaskStatus newStatus) {
        if (this.status == RobotTaskStatus.FAILED && newStatus == RobotTaskStatus.COMPLETED) {
            throw new IllegalStateException("Cannot complete a failed task");
        }
        this.status = newStatus;
    }
}