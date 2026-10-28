package com.cybersentinel.dao;

import com.cybersentinel.database.DatabaseConnection;
import com.cybersentinel.model.IncidentAction;
import com.cybersentinel.model.IncidentHistory;
import com.cybersentinel.model.InvestigationNote;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvestigationDAO {

    public void addNote(InvestigationNote note) throws SQLException {
        String sql = "INSERT INTO cs_investigation_notes (incident_id, analyst_id, note) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, note.getIncidentId());
            ps.setLong(2, note.getAnalystId());
            ps.setString(3, note.getNote());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    note.setId(rs.getLong(1));
                }
            }
        }
    }

    public List<InvestigationNote> getNotesByIncident(long incidentId) throws SQLException {
        List<InvestigationNote> list = new ArrayList<>();
        String sql = "SELECT n.*, u.username AS analyst_username FROM cs_investigation_notes n " +
                     "JOIN cs_users u ON n.analyst_id = u.id " +
                     "WHERE n.incident_id = ? ORDER BY n.created_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, incidentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvestigationNote note = new InvestigationNote();
                    note.setId(rs.getLong("id"));
                    note.setIncidentId(rs.getLong("incident_id"));
                    note.setAnalystId(rs.getLong("analyst_id"));
                    note.setAnalystName(rs.getString("analyst_username"));
                    note.setNote(rs.getString("note"));
                    note.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(note);
                }
            }
        }
        return list;
    }

    public void addHistory(IncidentHistory h) throws SQLException {
        String sql = "INSERT INTO cs_incident_history (incident_id, actor_id, event, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, h.getIncidentId());
            ps.setLong(2, h.getActorId());
            ps.setString(3, h.getEvent());
            ps.setString(4, h.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    h.setId(rs.getLong(1));
                }
            }
        }
    }

    public List<IncidentHistory> getHistoryByIncident(long incidentId) throws SQLException {
        List<IncidentHistory> list = new ArrayList<>();
        String sql = "SELECT h.*, u.username AS actor_username FROM cs_incident_history h " +
                     "LEFT JOIN cs_users u ON h.actor_id = u.id " +
                     "WHERE h.incident_id = ? ORDER BY h.created_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, incidentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    IncidentHistory h = new IncidentHistory();
                    h.setId(rs.getLong("id"));
                    h.setIncidentId(rs.getLong("incident_id"));
                    h.setActorId(rs.getLong("actor_id"));
                    String actorName = rs.getString("actor_username");
                    h.setActorName(actorName != null ? actorName : "System");
                    h.setEvent(rs.getString("event"));
                    h.setDescription(rs.getString("description"));
                    h.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(h);
                }
            }
        }
        return list;
    }

    public void addAction(IncidentAction action) throws SQLException {
        String sql = "INSERT INTO cs_incident_actions (incident_id, action_text, is_completed, sort_order) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, action.getIncidentId());
            ps.setString(2, action.getActionText());
            ps.setBoolean(3, action.isCompleted());
            ps.setInt(4, action.getSortOrder());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    action.setId(rs.getLong(1));
                }
            }
        }
    }

    public List<IncidentAction> getActionsByIncident(long incidentId) throws SQLException {
        List<IncidentAction> list = new ArrayList<>();
        String sql = "SELECT a.*, u.username AS completed_by_name FROM cs_incident_actions a " +
                     "LEFT JOIN cs_users u ON a.completed_by = u.id " +
                     "WHERE a.incident_id = ? ORDER BY a.sort_order ASC, a.id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, incidentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    IncidentAction a = new IncidentAction();
                    a.setId(rs.getLong("id"));
                    a.setIncidentId(rs.getLong("incident_id"));
                    a.setActionText(rs.getString("action_text"));
                    a.setCompleted(rs.getBoolean("is_completed"));
                    long compBy = rs.getLong("completed_by");
                    if (!rs.wasNull()) {
                        a.setCompletedBy(compBy);
                        a.setCompletedByName(rs.getString("completed_by_name"));
                    }
                    a.setCompletedAt(rs.getTimestamp("completed_at"));
                    a.setSortOrder(rs.getInt("sort_order"));
                    list.add(a);
                }
            }
        }
        return list;
    }

    public void toggleActionCompleted(long actionId, boolean completed, Long completedBy) throws SQLException {
        String sql = "UPDATE cs_incident_actions SET is_completed = ?, completed_by = ?, " +
                     "completed_at = CASE WHEN ? = TRUE THEN CURRENT_TIMESTAMP ELSE NULL END WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, completed);
            if (completed && completedBy != null && completedBy > 0) {
                ps.setLong(2, completedBy);
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setBoolean(3, completed);
            ps.setLong(4, actionId);
            ps.executeUpdate();
        }
    }
}
