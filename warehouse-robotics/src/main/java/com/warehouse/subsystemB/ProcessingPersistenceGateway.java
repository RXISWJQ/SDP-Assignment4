package com.warehouse.subsystemB;

import com.warehouse.db.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProcessingPersistenceGateway {
    private final DatabaseManager dbManager;

    public ProcessingPersistenceGateway(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void save(RobotTask task) {
        String query = "INSERT INTO robot_tasks (task_id, order_id, robot_id, status) " +
                       "VALUES (?, ?, ?, ?) ON CONFLICT(task_id) DO UPDATE SET status=excluded.status";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, task.getTaskId());
            stmt.setString(2, task.getOrderId());
            stmt.setString(3, task.getRobotId());
            stmt.setString(4, task.getStatus().name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save robot task", e);
        }
    }

    public RobotTask findByOrderId(String orderId) {
        String query = "SELECT task_id, order_id, robot_id, status FROM robot_tasks WHERE order_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new RobotTask(
                            rs.getString("task_id"),
                            rs.getString("order_id"),
                            rs.getString("robot_id"),
                            RobotTaskStatus.valueOf(rs.getString("status"))
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find robot task", e);
        }
        return null;
    }
}