package com.cybersentinel.dao;

import com.cybersentinel.database.DatabaseConnection;
import com.cybersentinel.model.Indicator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IndicatorDAO {

    public long addIndicator(Indicator ind) throws SQLException {
        String sql = "INSERT INTO cs_indicators (incident_id, type, value, classification, threat_level, " +
                     "description, created_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, ind.getIncidentId());
            ps.setString(2, ind.getType());
            ps.setString(3, ind.getValue());
            ps.setString(4, ind.getClassification());
            ps.setString(5, ind.getThreatLevel());
            ps.setString(6, ind.getDescription());
            ps.setLong(7, ind.getCreatedBy());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    ind.setId(rs.getLong(1));
                    return ind.getId();
                }
            }
        }
        return -1;
    }

    public List<Indicator> getByIncident(long incidentId) throws SQLException {
        List<Indicator> list = new ArrayList<>();
        String sql = "SELECT i.*, u.username AS creator_name FROM cs_indicators i " +
                     "LEFT JOIN cs_users u ON i.created_by = u.id " +
                     "WHERE i.incident_id = ? ORDER BY i.created_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, incidentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Indicator ind = new Indicator();
                    ind.setId(rs.getLong("id"));
                    ind.setIncidentId(rs.getLong("incident_id"));
                    ind.setType(rs.getString("type"));
                    ind.setValue(rs.getString("value"));
                    ind.setClassification(rs.getString("classification"));
                    ind.setThreatLevel(rs.getString("threat_level"));
                    ind.setDescription(rs.getString("description"));
                    ind.setCreatedBy(rs.getLong("created_by"));
                    String cName = rs.getString("creator_name");
                    ind.setCreatedByName(cName != null ? cName : "Analyst");
                    ind.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(ind);
                }
            }
        }
        return list;
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM cs_indicators WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public int countByIncident(long incidentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cs_indicators WHERE incident_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, incidentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }
}
