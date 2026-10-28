package com.cybersentinel.dao;

import com.cybersentinel.database.DatabaseConnection;
import com.cybersentinel.model.Incident;
import com.cybersentinel.model.IncidentStatus;
import com.cybersentinel.model.Priority;
import com.cybersentinel.model.Severity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IncidentDAO {

    public long createIncident(Incident inc) throws SQLException {
        String sql = "INSERT INTO cs_incidents (incident_code, title, type, description, severity, risk_score, " +
                     "priority, status, reported_by, assigned_to, affected_system, is_simulated, observed_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, inc.getIncidentCode());
            ps.setString(2, inc.getTitle());
            ps.setString(3, inc.getType());
            ps.setString(4, inc.getDescription());
            ps.setString(5, inc.getSeverity().name());
            ps.setInt(6, inc.getRiskScore());
            ps.setString(7, inc.getPriority().name());
            ps.setString(8, inc.getStatus().name());
            ps.setLong(9, inc.getReportedBy());
            if (inc.getAssignedTo() != null && inc.getAssignedTo() > 0) {
                ps.setLong(10, inc.getAssignedTo());
            } else {
                ps.setNull(10, Types.BIGINT);
            }
            ps.setString(11, inc.getAffectedSystem());
            ps.setBoolean(12, inc.isSimulated());
            if (inc.getObservedAt() != null) {
                ps.setTimestamp(13, inc.getObservedAt());
            } else {
                ps.setTimestamp(13, new Timestamp(System.currentTimeMillis()));
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    inc.setId(rs.getLong(1));
                    return inc.getId();
                }
            }
        }
        return -1;
    }

    public Incident findById(long id) throws SQLException {
        String sql = "SELECT i.*, u1.username AS reporter_name, u2.username AS analyst_name " +
                     "FROM cs_incidents i " +
                     "LEFT JOIN cs_users u1 ON i.reported_by = u1.id " +
                     "LEFT JOIN cs_users u2 ON i.assigned_to = u2.id " +
                     "WHERE i.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Incident findByCode(String code) throws SQLException {
        String sql = "SELECT i.*, u1.username AS reporter_name, u2.username AS analyst_name " +
                     "FROM cs_incidents i " +
                     "LEFT JOIN cs_users u1 ON i.reported_by = u1.id " +
                     "LEFT JOIN cs_users u2 ON i.assigned_to = u2.id " +
                     "WHERE i.incident_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Incident> findAll(String query, String status, String severity, String type,
                                  Boolean simulatedOnly, Long assignedTo, Long reportedBy,
                                  String sortOrder) throws SQLException {
        List<Incident> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT i.*, u1.username AS reporter_name, u2.username AS analyst_name " +
            "FROM cs_incidents i " +
            "LEFT JOIN cs_users u1 ON i.reported_by = u1.id " +
            "LEFT JOIN cs_users u2 ON i.assigned_to = u2.id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (reportedBy != null) {
            sql.append("AND i.reported_by = ? ");
            params.add(reportedBy);
        }
        if (assignedTo != null) {
            if (assignedTo == -1L) {
                sql.append("AND (i.assigned_to IS NULL OR i.assigned_to = 0) ");
            } else {
                sql.append("AND i.assigned_to = ? ");
                params.add(assignedTo);
            }
        }
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND i.status = ? ");
            params.add(status.trim().toUpperCase());
        }
        if (severity != null && !severity.isBlank() && !severity.equalsIgnoreCase("ALL")) {
            sql.append("AND i.severity = ? ");
            params.add(severity.trim().toUpperCase());
        }
        if (type != null && !type.isBlank() && !type.equalsIgnoreCase("ALL")) {
            sql.append("AND i.type = ? ");
            params.add(type.trim());
        }
        if (simulatedOnly != null) {
            sql.append("AND i.is_simulated = ? ");
            params.add(simulatedOnly);
        }
        if (query != null && !query.isBlank()) {
            String q = "%" + query.trim().toLowerCase() + "%";
            sql.append("AND (LOWER(i.incident_code) LIKE ? OR LOWER(i.title) LIKE ? OR LOWER(i.description) LIKE ? OR LOWER(i.affected_system) LIKE ? OR LOWER(i.type) LIKE ?) ");
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
        }

        // Dynamic sort order
        if ("OLDEST".equalsIgnoreCase(sortOrder)) {
            sql.append("ORDER BY i.created_at ASC");
        } else if ("RISK_ASC".equalsIgnoreCase(sortOrder)) {
            sql.append("ORDER BY i.risk_score ASC, i.created_at DESC");
        } else if ("RISK_DESC".equalsIgnoreCase(sortOrder)) {
            sql.append("ORDER BY i.risk_score DESC, i.created_at DESC");
        } else {
            // Default: NEWEST first
            sql.append("ORDER BY i.created_at DESC, i.risk_score DESC");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }


    public boolean claimIncident(long incidentId, long analystId) throws SQLException {
        String sql = "UPDATE cs_incidents SET assigned_to = ?, status = 'ASSIGNED' " +
                     "WHERE id = ? AND (assigned_to IS NULL OR assigned_to = 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, analystId);
            ps.setLong(2, incidentId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    public void updateStatus(long incidentId, IncidentStatus newStatus) throws SQLException {
        String sql = "UPDATE cs_incidents SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setLong(2, incidentId);
            ps.executeUpdate();
        }
    }

    public void assignIncident(long incidentId, Long analystId) throws SQLException {
        String sql = "UPDATE cs_incidents SET assigned_to = ?, status = CASE WHEN status = 'OPEN' THEN 'ASSIGNED' ELSE status END WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (analystId != null && analystId > 0) {
                ps.setLong(1, analystId);
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setLong(2, incidentId);
            ps.executeUpdate();
        }
    }

    public void updateSeverityAndRisk(long incidentId, Severity severity, int riskScore, Priority priority) throws SQLException {
        String sql = "UPDATE cs_incidents SET severity = ?, risk_score = ?, priority = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, severity.name());
            ps.setInt(2, riskScore);
            ps.setString(3, priority.name());
            ps.setLong(4, incidentId);
            ps.executeUpdate();
        }
    }

    public void resolveIncident(long incidentId, String rootCause, String actionsTaken, 
                                String resolutionSummary, String recommendations) throws SQLException {
        String sql = "UPDATE cs_incidents SET status = 'RESOLVED', root_cause = ?, actions_taken = ?, " +
                     "resolution_summary = ?, recommendations = ?, resolved_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rootCause);
            ps.setString(2, actionsTaken);
            ps.setString(3, resolutionSummary);
            ps.setString(4, recommendations);
            ps.setLong(5, incidentId);
            ps.executeUpdate();
        }
    }

    public boolean delete(long incidentId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete child records to ensure foreign key integrity
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cs_incident_actions WHERE incident_id = ?")) {
                    ps.setLong(1, incidentId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cs_indicators WHERE incident_id = ?")) {
                    ps.setLong(1, incidentId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cs_evidence WHERE incident_id = ?")) {
                    ps.setLong(1, incidentId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cs_investigation_notes WHERE incident_id = ?")) {
                    ps.setLong(1, incidentId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cs_incident_history WHERE incident_id = ?")) {
                    ps.setLong(1, incidentId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cs_notifications WHERE incident_id = ?")) {
                    ps.setLong(1, incidentId);
                    ps.executeUpdate();
                }
                int deleted;
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cs_incidents WHERE id = ?")) {
                    ps.setLong(1, incidentId);
                    deleted = ps.executeUpdate();
                }
                conn.commit();
                return deleted > 0;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private Incident mapRow(ResultSet rs) throws SQLException {
        Incident inc = new Incident();
        inc.setId(rs.getLong("id"));
        inc.setIncidentCode(rs.getString("incident_code"));
        inc.setTitle(rs.getString("title"));
        inc.setType(rs.getString("type"));
        inc.setDescription(rs.getString("description"));
        inc.setSeverity(Severity.fromString(rs.getString("severity")));
        inc.setRiskScore(rs.getInt("risk_score"));
        inc.setPriority(Priority.fromString(rs.getString("priority")));
        inc.setStatus(IncidentStatus.fromString(rs.getString("status")));
        inc.setReportedBy(rs.getLong("reported_by"));
        inc.setReportedByName(rs.getString("reporter_name"));
        
        long assigned = rs.getLong("assigned_to");
        if (!rs.wasNull()) {
            inc.setAssignedTo(assigned);
            inc.setAssignedToName(rs.getString("analyst_name"));
        } else {
            inc.setAssignedTo(null);
            inc.setAssignedToName("Unassigned");
        }

        inc.setAffectedSystem(rs.getString("affected_system"));
        inc.setResolutionSummary(rs.getString("resolution_summary"));
        inc.setRootCause(rs.getString("root_cause"));
        inc.setActionsTaken(rs.getString("actions_taken"));
        inc.setRecommendations(rs.getString("recommendations"));
        inc.setSimulated(rs.getBoolean("is_simulated"));
        inc.setObservedAt(rs.getTimestamp("observed_at"));
        inc.setCreatedAt(rs.getTimestamp("created_at"));
        inc.setUpdatedAt(rs.getTimestamp("updated_at"));
        inc.setResolvedAt(rs.getTimestamp("resolved_at"));
        return inc;
    }
}
