package com.cybersentinel.dao;

import com.cybersentinel.database.DatabaseConnection;
import com.cybersentinel.model.Evidence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvidenceDAO {

    public long addEvidence(Evidence ev) throws SQLException {
        String sql = "INSERT INTO cs_evidence (incident_id, original_filename, stored_path, description, " +
                     "sha256, file_size, uploaded_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, ev.getIncidentId());
            ps.setString(2, ev.getOriginalFilename());
            ps.setString(3, ev.getStoredPath());
            ps.setString(4, ev.getDescription());
            ps.setString(5, ev.getSha256());
            ps.setLong(6, ev.getFileSize());
            ps.setLong(7, ev.getUploadedBy());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    ev.setId(rs.getLong(1));
                    return ev.getId();
                }
            }
        }
        return -1;
    }

    public List<Evidence> getByIncident(long incidentId) throws SQLException {
        List<Evidence> list = new ArrayList<>();
        String sql = "SELECT e.*, u.username AS uploader_name FROM cs_evidence e " +
                     "LEFT JOIN cs_users u ON e.uploaded_by = u.id " +
                     "WHERE e.incident_id = ? ORDER BY e.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, incidentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Evidence ev = new Evidence();
                    ev.setId(rs.getLong("id"));
                    ev.setIncidentId(rs.getLong("incident_id"));
                    ev.setOriginalFilename(rs.getString("original_filename"));
                    ev.setStoredPath(rs.getString("stored_path"));
                    ev.setDescription(rs.getString("description"));
                    ev.setSha256(rs.getString("sha256"));
                    ev.setFileSize(rs.getLong("file_size"));
                    ev.setUploadedBy(rs.getLong("uploaded_by"));
                    String upName = rs.getString("uploader_name");
                    ev.setUploadedByName(upName != null ? upName : "User");
                    ev.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(ev);
                }
            }
        }
        return list;
    }

    public Evidence findById(long id) throws SQLException {
        String sql = "SELECT e.*, u.username AS uploader_name FROM cs_evidence e " +
                     "LEFT JOIN cs_users u ON e.uploaded_by = u.id WHERE e.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Evidence ev = new Evidence();
                    ev.setId(rs.getLong("id"));
                    ev.setIncidentId(rs.getLong("incident_id"));
                    ev.setOriginalFilename(rs.getString("original_filename"));
                    ev.setStoredPath(rs.getString("stored_path"));
                    ev.setDescription(rs.getString("description"));
                    ev.setSha256(rs.getString("sha256"));
                    ev.setFileSize(rs.getLong("file_size"));
                    ev.setUploadedBy(rs.getLong("uploaded_by"));
                    ev.setUploadedByName(rs.getString("uploader_name"));
                    ev.setCreatedAt(rs.getTimestamp("created_at"));
                    return ev;
                }
            }
        }
        return null;
    }
}
