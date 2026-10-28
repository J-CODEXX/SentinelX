package com.cybersentinel.dao;

import com.cybersentinel.database.DatabaseConnection;
import com.cybersentinel.model.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditDAO {

    public void log(AuditLog log) {
        String sql = "INSERT INTO cs_audit_logs (actor_id, action, entity, entity_id, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getActorId() != null && log.getActorId() > 0) {
                ps.setLong(1, log.getActorId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, log.getAction());
            ps.setString(3, log.getEntity());
            ps.setString(4, log.getEntityId());
            ps.setString(5, log.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    log.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("[WARN] Failed to write audit log: " + e.getMessage());
        }
    }

    public List<AuditLog> findAll(String actorQuery, String actionQuery, String entityQuery, int limit) throws SQLException {
        List<AuditLog> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT a.*, u.username AS actor_name FROM cs_audit_logs a " +
            "LEFT JOIN cs_users u ON a.actor_id = u.id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (actorQuery != null && !actorQuery.isBlank()) {
            sql.append("AND LOWER(u.username) LIKE ? ");
            params.add("%" + actorQuery.trim().toLowerCase() + "%");
        }
        if (actionQuery != null && !actionQuery.isBlank() && !actionQuery.equalsIgnoreCase("ALL")) {
            sql.append("AND a.action = ? ");
            params.add(actionQuery.trim());
        }
        if (entityQuery != null && !entityQuery.isBlank() && !entityQuery.equalsIgnoreCase("ALL")) {
            sql.append("AND a.entity = ? ");
            params.add(entityQuery.trim());
        }

        sql.append("ORDER BY a.created_at DESC LIMIT ?");
        params.add(Math.max(10, Math.min(500, limit)));

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog a = new AuditLog();
                    a.setId(rs.getLong("id"));
                    long actorId = rs.getLong("actor_id");
                    if (!rs.wasNull()) a.setActorId(actorId);
                    String uName = rs.getString("actor_name");
                    a.setActorUsername(uName != null ? uName : "SYSTEM");
                    a.setAction(rs.getString("action"));
                    a.setEntity(rs.getString("entity"));
                    a.setEntityId(rs.getString("entity_id"));
                    a.setDescription(rs.getString("description"));
                    a.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(a);
                }
            }
        }
        return list;
    }
}
