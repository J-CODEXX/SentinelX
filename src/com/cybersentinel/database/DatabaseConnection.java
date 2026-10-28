package com.cybersentinel.database;

import com.cybersentinel.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages database connections using JDBC.
 */
public final class DatabaseConnection {

    static {
        try {
            // Load MySQL Connector/J driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                // Fallback driver name for older drivers
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ignored) {
                System.err.println("[WARN] MySQL JDBC Driver class not found in standard lookup: " + e.getMessage());
            }
        }
    }

    private DatabaseConnection() {}

    /**
     * Obtains a new JDBC Connection to the configured MySQL database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                AppConfig.getDbUrl(),
                AppConfig.getDbUser(),
                AppConfig.getDbPassword()
        );
    }

    /**
     * Tests whether MySQL server and cyber_projects database can be reached.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
