package com.cybersentinel.dao;

import com.cybersentinel.database.DatabaseConnection;
import com.cybersentinel.model.Role;
import com.cybersentinel.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User findByUsername(String username) throws SQLException {
        if (username == null) return null;
        String sql = "SELECT id, username, email, password_hash, password_salt, role_id, active, failed_attempts, locked_until, created_at " +
                     "FROM cs_users WHERE username = ?";
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

    public User findById(long id) throws SQLException {
        String sql = "SELECT id, username, email, password_hash, password_salt, role_id, active, failed_attempts, locked_until, created_at " +
                     "FROM cs_users WHERE id = ?";
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

    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, username, email, password_hash, password_salt, role_id, active, failed_attempts, locked_until, created_at " +
                     "FROM cs_users ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<User> findAllAnalysts() throws SQLException {
        List<User> list = new ArrayList<>();
        // Role ID 2 = ANALYST, Role ID 3 = SUPERMOD (both can be assigned)
        String sql = "SELECT id, username, email, password_hash, password_salt, role_id, active, failed_attempts, locked_until, created_at " +
                     "FROM cs_users WHERE role_id IN (2, 3) AND active = TRUE ORDER BY username ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public long createUser(User user) throws SQLException {
        String sql = "INSERT INTO cs_users (username, email, password_hash, password_salt, role_id, active, failed_attempts) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getPasswordSalt());
            ps.setInt(5, user.getRoleId());
            ps.setBoolean(6, user.isActive());
            ps.setInt(7, 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getLong(1));
                    return user.getId();
                }
            }
        }
        return -1;
    }

    public void updatePassword(long userId, String newHash, String newSalt) throws SQLException {
        String sql = "UPDATE cs_users SET password_hash = ?, password_salt = ?, failed_attempts = 0, locked_until = NULL WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setString(2, newSalt);
            ps.setLong(3, userId);
            ps.executeUpdate();
        }
    }

    public void setActiveStatus(long userId, boolean active) throws SQLException {
        String sql = "UPDATE cs_users SET active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    public void recordLoginAttempt(String username, boolean success) {
        String sql = "INSERT INTO cs_login_attempts (username, succeeded) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setBoolean(2, success);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public void incrementFailedAttempts(String username) throws SQLException {
        String sql = "UPDATE cs_users SET failed_attempts = failed_attempts + 1 WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }

    public void lockUser(String username, int lockoutMinutes) throws SQLException {
        Timestamp lockedUntil = new Timestamp(System.currentTimeMillis() + (lockoutMinutes * 60L * 1000L));
        String sql = "UPDATE cs_users SET locked_until = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, lockedUntil);
            ps.setString(2, username);
            ps.executeUpdate();
        }
    }

    public void resetFailedAttempts(String username) throws SQLException {
        String sql = "UPDATE cs_users SET failed_attempts = 0, locked_until = NULL WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setPasswordSalt(rs.getString("password_salt"));
        u.setRoleId(rs.getInt("role_id"));
        u.setActive(rs.getBoolean("active"));
        u.setFailedAttempts(rs.getInt("failed_attempts"));
        u.setLockedUntil(rs.getTimestamp("locked_until"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
