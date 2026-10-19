package com.warehouse.subsystemC;

import com.warehouse.db.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FinalizationPersistenceGateway {
    private final DatabaseManager dbManager;

    public FinalizationPersistenceGateway(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void save(DispatchRecord record) {
        String query = "INSERT INTO dispatch_records (dispatch_id, order_id, station_id, status) " +
                       "VALUES (?, ?, ?, ?) ON CONFLICT(dispatch_id) DO UPDATE SET status=excluded.status";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, record.getDispatchId());
            stmt.setString(2, record.getOrderId());
            stmt.setString(3, record.getStationId());
            stmt.setString(4, record.getStatus().name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save dispatch record", e);
        }
    }

    public DispatchRecord findByOrderId(String orderId) {
        String query = "SELECT dispatch_id, order_id, station_id, status FROM dispatch_records WHERE order_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new DispatchRecord(
                            rs.getString("dispatch_id"),
                            rs.getString("order_id"),
                            rs.getString("station_id"),
                            DispatchRecordStatus.valueOf(rs.getString("status"))
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find dispatch record", e);
        }
        return null;
    }
}