package com.warehouse.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final String dbPath;

    public DatabaseManager(String dbPath) {
        this.dbPath = dbPath;
        initDatabase();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    public void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory_reservations (" +
                    "order_id TEXT PRIMARY KEY, " +
                    "item_sku TEXT, " +
                    "quantity INTEGER, " +
                    "status TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS robot_tasks (" +
                    "task_id TEXT PRIMARY KEY, " +
                    "order_id TEXT, " +
                    "robot_id TEXT, " +
                    "status TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS dispatch_records (" +
                    "dispatch_id TEXT PRIMARY KEY, " +
                    "order_id TEXT, " +
                    "station_id TEXT, " +
                    "status TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS chain_tracker (" +
                    "order_id TEXT PRIMARY KEY, " +
                    "step_status TEXT)");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public void clearAllTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM inventory_reservations");
            stmt.execute("DELETE FROM robot_tasks");
            stmt.execute("DELETE FROM dispatch_records");
            stmt.execute("DELETE FROM chain_tracker");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear database", e);
        }
    }
}