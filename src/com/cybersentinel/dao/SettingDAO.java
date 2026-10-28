package com.cybersentinel.dao;

import com.cybersentinel.database.DatabaseConnection;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingDAO {

    public String getSetting(String key, String defaultValue) {
        String sql = "SELECT setting_value FROM cs_system_settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
        } catch (SQLException e) {
            System.err.println("[WARN] Error reading setting " + key + ": " + e.getMessage());
        }
        return defaultValue;
    }

    public void setSetting(String key, String value) throws SQLException {
        String sql = "INSERT INTO cs_system_settings (setting_key, setting_value) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), updated_at = CURRENT_TIMESTAMP";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }

    public Map<String, String> getAllSettings() throws SQLException {
        Map<String, String> map = new LinkedHashMap<>();
        String sql = "SELECT setting_key, setting_value FROM cs_system_settings ORDER BY setting_key ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }
        }
        return map;
    }
}
