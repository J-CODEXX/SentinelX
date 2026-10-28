package com.cybersentinel.dao;

import com.cybersentinel.database.DatabaseConnection;
import com.cybersentinel.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public long createNotification(Notification n) throws SQLException {
        String sql = "INSERT INTO cs_notifications (recipient_id, incident_id, message, type, is_read) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (n.getRecipientId() != null && n.getRecipientId() > 0) {
                ps.setLong(1, n.getRecipientId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            if (n.getIncidentId() != null && n.getIncidentId() > 0) {
                ps.setLong(2, n.getIncidentId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setString(3, n.getMessage());
            ps.setString(4, n.getType());
            ps.setBoolean(5, n.isRead());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    n.setId(rs.getLong(1));
                    return n.getId();
                }
            }
        }
        return -1;
    }

    public void broadcastToRole(int roleId, Long incidentId, String message, String type) throws SQLException {
        String sql = "INSERT INTO cs_notifications (recipient_id, incident_id, message, type, is_read) " +
                     "SELECT id, ?, ?, ?, FALSE FROM cs_users WHERE (role_id = ? OR role_id = 3) AND active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (incidentId != null && incidentId > 0) {
                ps.setLong(1, incidentId);
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, message);
            ps.setString(3, type);
            ps.setInt(4, roleId);
            ps.executeUpdate();
        }
    }

    public List<Notification> getForUser(long userId, boolean onlyUnread) throws SQLException {
        List<Notification> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM cs_notifications WHERE recipient_id = ? ");
        if (onlyUnread) {
            sql.append("AND is_read = FALSE ");
        }
        sql.append("ORDER BY created_at DESC LIMIT 50");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getLong("id"));
                    n.setRecipientId(rs.getLong("recipient_id"));
                    long incId = rs.getLong("incident_id");
                    if (!rs.wasNull()) n.setIncidentId(incId);
                    n.setMessage(rs.getString("message"));
                    n.setType(rs.getString("type"));
                    n.setRead(rs.getBoolean("is_read"));
                    n.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(n);
                }
            }
        }
        return list;
    }

    public void markAsRead(long notificationId) throws SQLException {
        String sql = "UPDATE cs_notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, notificationId);
            ps.executeUpdate();
        }
    }

    public void markAllRead(long userId) throws SQLException {
        String sql = "UPDATE cs_notifications SET is_read = TRUE WHERE recipient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }
}
