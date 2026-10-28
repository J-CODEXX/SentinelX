package com.cybersentinel.dao;

import com.cybersentinel.database.DatabaseConnection;
import com.cybersentinel.model.AnalystRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnalystRequestDAO {

    public AnalystRequestDAO() {
        ensureTableExists();
    }

    public void ensureTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS cs_analyst_requests (" +
                     "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                     "username VARCHAR(50) NOT NULL, " +
                     "email VARCHAR(100) NOT NULL, " +
                     "password_hash VARCHAR(128) NOT NULL, " +
                     "password_salt VARCHAR(64) NOT NULL, " +
                     "status VARCHAR(20) NOT NULL DEFAULT 'PENDING', " +
                     "rejection_reason TEXT NULL, " +
                     "reviewed_by INT NULL, " +
                     "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                     "reviewed_at TIMESTAMP NULL DEFAULT NULL, " +
                     "INDEX idx_analyst_req_status (status), " +
                     "INDEX idx_analyst_req_username (username)" +
                     ") ENGINE=InnoDB";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("[WARN] Could not verify cs_analyst_requests table: " + e.getMessage());
        }
    }

    public long createRequest(AnalystRequest req) throws SQLException {
        String sql = "INSERT INTO cs_analyst_requests (username, email, password_hash, password_salt, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, req.getUsername());
            ps.setString(2, req.getEmail());
            ps.setString(3, req.getPasswordHash());
            ps.setString(4, req.getPasswordSalt());
            ps.setString(5, req.getStatus() != null ? req.getStatus() : "PENDING");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    req.setId(rs.getLong(1));
                    return req.getId();
                }
            }
        }
        return -1;
    }

    public AnalystRequest findById(long id) throws SQLException {
        String sql = "SELECT r.*, u.username AS reviewer_username FROM cs_analyst_requests r " +
                     "LEFT JOIN cs_users u ON r.reviewed_by = u.id WHERE r.id = ?";
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

    public AnalystRequest findLatestByUsername(String username) throws SQLException {
        if (username == null) return null;
        String sql = "SELECT r.*, u.username AS reviewer_username FROM cs_analyst_requests r " +
                     "LEFT JOIN cs_users u ON r.reviewed_by = u.id WHERE r.username = ? ORDER BY r.id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean hasPendingRequest(String username) throws SQLException {
        if (username == null) return false;
        String sql = "SELECT COUNT(*) FROM cs_analyst_requests WHERE username = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public int countPending() throws SQLException {
        String sql = "SELECT COUNT(*) FROM cs_analyst_requests WHERE status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<AnalystRequest> findAll(String statusFilter) throws SQLException {
        List<AnalystRequest> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT r.*, u.username AS reviewer_username FROM cs_analyst_requests r " +
            "LEFT JOIN cs_users u ON r.reviewed_by = u.id "
        );

        if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter.trim())) {
            sql.append("WHERE r.status = ? ");
        }
        sql.append("ORDER BY r.id DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter.trim())) {
                ps.setString(1, statusFilter.trim().toUpperCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public void updateStatus(long id, String status, Long reviewedBy, String rejectionReason) throws SQLException {
        String sql = "UPDATE cs_analyst_requests SET status = ?, reviewed_by = ?, rejection_reason = ?, reviewed_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (reviewedBy != null && reviewedBy > 0) {
                ps.setLong(2, reviewedBy);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, rejectionReason);
            ps.setLong(4, id);
            ps.executeUpdate();
        }
    }

    private AnalystRequest mapRow(ResultSet rs) throws SQLException {
        AnalystRequest req = new AnalystRequest();
        req.setId(rs.getLong("id"));
        req.setUsername(rs.getString("username"));
        req.setEmail(rs.getString("email"));
        req.setPasswordHash(rs.getString("password_hash"));
        req.setPasswordSalt(rs.getString("password_salt"));
        req.setStatus(rs.getString("status"));
        req.setRejectionReason(rs.getString("rejection_reason"));
        long revBy = rs.getLong("reviewed_by");
        if (!rs.wasNull()) {
            req.setReviewedBy(revBy);
        }
        req.setReviewerUsername(rs.getString("reviewer_username"));
        req.setCreatedAt(rs.getTimestamp("created_at"));
        req.setReviewedAt(rs.getTimestamp("reviewed_at"));
        return req;
    }
}
