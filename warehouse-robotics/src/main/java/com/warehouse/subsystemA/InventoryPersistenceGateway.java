package com.warehouse.subsystemA;

import com.warehouse.db.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryPersistenceGateway {
    private final DatabaseManager dbManager;

    public InventoryPersistenceGateway(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void save(InventoryReservation reservation) {
        String query = "INSERT INTO inventory_reservations (order_id, item_sku, quantity, status) " +
                       "VALUES (?, ?, ?, ?) ON CONFLICT(order_id) DO UPDATE SET status=excluded.status";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, reservation.getOrderId());
            stmt.setString(2, reservation.getItemSku());
            stmt.setInt(3, reservation.getQuantity());
            stmt.setString(4, reservation.getStatus().name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save inventory reservation", e);
        }
    }

    public InventoryReservation findByOrderId(String orderId) {
        String query = "SELECT order_id, item_sku, quantity, status FROM inventory_reservations WHERE order_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new InventoryReservation(
                            rs.getString("order_id"),
                            rs.getString("item_sku"),
                            rs.getInt("quantity"),
                            InventoryReservationStatus.valueOf(rs.getString("status"))
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find inventory reservation", e);
        }
        return null;
    }
}