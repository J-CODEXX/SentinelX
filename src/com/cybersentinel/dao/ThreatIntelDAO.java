package com.cybersentinel.dao;

import com.cybersentinel.database.DatabaseConnection;
import com.cybersentinel.model.ThreatIntelligence;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ThreatIntelDAO {

    public ThreatIntelligence findMatch(String value) throws SQLException {
        if (value == null || value.isBlank()) return null;
        String sql = "SELECT * FROM cs_threat_intelligence WHERE LOWER(value) = LOWER(?) LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<ThreatIntelligence> findAll(String query, String classification, String threatLevel) throws SQLException {
        List<ThreatIntelligence> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM cs_threat_intelligence WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (classification != null && !classification.isBlank() && !classification.equalsIgnoreCase("ALL")) {
            sql.append("AND classification = ? ");
            params.add(classification.trim());
        }
        if (threatLevel != null && !threatLevel.isBlank() && !threatLevel.equalsIgnoreCase("ALL")) {
            sql.append("AND threat_level = ? ");
            params.add(threatLevel.trim());
        }
        if (query != null && !query.isBlank()) {
            String q = "%" + query.trim().toLowerCase() + "%";
            sql.append("AND (LOWER(value) LIKE ? OR LOWER(description) LIKE ? OR LOWER(source) LIKE ?) ");
            params.add(q);
            params.add(q);
            params.add(q);
        }
        sql.append("ORDER BY id DESC");

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

    public long create(ThreatIntelligence item) throws SQLException {
        String sql = "INSERT INTO cs_threat_intelligence (type, value, classification, threat_level, source, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getType());
            ps.setString(2, item.getValue());
            ps.setString(3, item.getClassification());
            ps.setString(4, item.getThreatLevel());
            ps.setString(5, item.getSource());
            ps.setString(6, item.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setId(rs.getLong(1));
                    return item.getId();
                }
            }
        }
        return -1;
    }

    public void update(ThreatIntelligence item) throws SQLException {
        String sql = "UPDATE cs_threat_intelligence SET type = ?, value = ?, classification = ?, " +
                     "threat_level = ?, source = ?, description = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getType());
            ps.setString(2, item.getValue());
            ps.setString(3, item.getClassification());
            ps.setString(4, item.getThreatLevel());
            ps.setString(5, item.getSource());
            ps.setString(6, item.getDescription());
            ps.setLong(7, item.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM cs_threat_intelligence WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public Map<String, Integer> getDistributionByClassification() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT classification, COUNT(*) as cnt FROM cs_threat_intelligence GROUP BY classification";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("classification"), rs.getInt("cnt"));
            }
        }
        return map;
    }

    private ThreatIntelligence mapRow(ResultSet rs) throws SQLException {
        ThreatIntelligence item = new ThreatIntelligence();
        item.setId(rs.getLong("id"));
        item.setType(rs.getString("type"));
        item.setValue(rs.getString("value"));
        item.setClassification(rs.getString("classification"));
        item.setThreatLevel(rs.getString("threat_level"));
        item.setSource(rs.getString("source"));
        item.setDescription(rs.getString("description"));
        item.setCreatedAt(rs.getTimestamp("created_at"));
        return item;
    }
}
