package com.cybersentinel.dao;

import com.cybersentinel.database.DatabaseConnection;
import com.cybersentinel.model.DashboardStats;
import com.cybersentinel.model.Role;
import com.cybersentinel.model.UserSession;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnalyticsDAO {

    public DashboardStats getDashboardStats(UserSession session) {
        DashboardStats stats = new DashboardStats();
        Long filterUserId = (session != null && session.getRole() == Role.USER) ? session.getUserId() : null;

        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Overall counts
            String sql = "SELECT " +
                    "COUNT(*) AS total, " +
                    "SUM(CASE WHEN status = 'OPEN' THEN 1 ELSE 0 END) AS open_cnt, " +
                    "SUM(CASE WHEN status = 'INVESTIGATING' THEN 1 ELSE 0 END) AS inv_cnt, " +
                    "SUM(CASE WHEN severity = 'CRITICAL' THEN 1 ELSE 0 END) AS crit_cnt, " +
                    "SUM(CASE WHEN severity = 'HIGH' THEN 1 ELSE 0 END) AS high_cnt, " +
                    "SUM(CASE WHEN status IN ('RESOLVED', 'CLOSED') THEN 1 ELSE 0 END) AS res_cnt, " +
                    "SUM(CASE WHEN (assigned_to IS NULL OR assigned_to = 0) AND status NOT IN ('RESOLVED', 'CLOSED') THEN 1 ELSE 0 END) AS unassigned_cnt, " +
                    "SUM(CASE WHEN status = 'ESCALATED' THEN 1 ELSE 0 END) AS esc_cnt, " +
                    "SUM(CASE WHEN is_simulated = TRUE THEN 1 ELSE 0 END) AS sim_cnt, " +
                    "SUM(CASE WHEN is_simulated = FALSE THEN 1 ELSE 0 END) AS real_cnt " +
                    "FROM cs_incidents " +
                    (filterUserId != null ? "WHERE reported_by = ?" : "");

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (filterUserId != null) ps.setLong(1, filterUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats.setTotalIncidents(rs.getInt("total"));
                        stats.setOpenIncidents(rs.getInt("open_cnt"));
                        stats.setInvestigatingIncidents(rs.getInt("inv_cnt"));
                        stats.setCriticalIncidents(rs.getInt("crit_cnt"));
                        stats.setHighIncidents(rs.getInt("high_cnt"));
                        stats.setResolvedIncidents(rs.getInt("res_cnt"));
                        stats.setUnassignedIncidents(rs.getInt("unassigned_cnt"));
                        stats.setEscalatedIncidents(rs.getInt("esc_cnt"));
                        stats.setSimulatedIncidents(rs.getInt("sim_cnt"));
                        stats.setRealIncidents(rs.getInt("real_cnt"));
                    }
                }
            }

            // 2. Active analysts count
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM cs_users WHERE role_id IN (2, 3) AND active = TRUE")) {
                if (rs.next()) {
                    stats.setActiveAnalysts(rs.getInt(1));
                }
            }

            // 3. My assigned incidents (for analyst)
            if (session != null && session.getRole() == Role.ANALYST) {
                String mySql = "SELECT COUNT(*) FROM cs_incidents WHERE assigned_to = ? AND status NOT IN ('RESOLVED', 'CLOSED')";
                try (PreparedStatement ps = conn.prepareStatement(mySql)) {
                    ps.setLong(1, session.getUserId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) stats.setMyAssignedIncidents(rs.getInt(1));
                    }
                }
            }

            // 4. Severity Distribution
            String sevSql = "SELECT severity, COUNT(*) as cnt FROM cs_incidents " +
                    (filterUserId != null ? "WHERE reported_by = ? " : "") +
                    "GROUP BY severity";
            try (PreparedStatement ps = conn.prepareStatement(sevSql)) {
                if (filterUserId != null) ps.setLong(1, filterUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    Map<String, Integer> map = new LinkedHashMap<>();
                    map.put("CRITICAL", 0);
                    map.put("HIGH", 0);
                    map.put("MEDIUM", 0);
                    map.put("LOW", 0);
                    while (rs.next()) {
                        map.put(rs.getString("severity").toUpperCase(), rs.getInt("cnt"));
                    }
                    stats.setSeverityDistribution(map);
                }
            }

            // 5. Status Distribution
            String statusSql = "SELECT status, COUNT(*) as cnt FROM cs_incidents " +
                    (filterUserId != null ? "WHERE reported_by = ? " : "") +
                    "GROUP BY status";
            try (PreparedStatement ps = conn.prepareStatement(statusSql)) {
                if (filterUserId != null) ps.setLong(1, filterUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    Map<String, Integer> map = new LinkedHashMap<>();
                    while (rs.next()) {
                        map.put(rs.getString("status"), rs.getInt("cnt"));
                    }
                    stats.setStatusDistribution(map);
                }
            }

            // 6. Category Distribution
            String catSql = "SELECT type, COUNT(*) as cnt FROM cs_incidents " +
                    (filterUserId != null ? "WHERE reported_by = ? " : "") +
                    "GROUP BY type ORDER BY cnt DESC LIMIT 8";
            try (PreparedStatement ps = conn.prepareStatement(catSql)) {
                if (filterUserId != null) ps.setLong(1, filterUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    Map<String, Integer> map = new LinkedHashMap<>();
                    while (rs.next()) {
                        map.put(rs.getString("type"), rs.getInt("cnt"));
                    }
                    stats.setCategoryDistribution(map);
                }
            }

            // 7. Analyst Workload
            String workSql = "SELECT COALESCE(u.username, 'Unassigned') AS analyst, COUNT(i.id) AS cnt " +
                    "FROM cs_incidents i " +
                    "LEFT JOIN cs_users u ON i.assigned_to = u.id " +
                    "WHERE i.status NOT IN ('RESOLVED', 'CLOSED') " +
                    "GROUP BY analyst ORDER BY cnt DESC";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(workSql)) {
                Map<String, Integer> map = new LinkedHashMap<>();
                while (rs.next()) {
                    map.put(rs.getString("analyst"), rs.getInt("cnt"));
                }
                stats.setAnalystWorkload(map);
            }

            // 8. Trends (Daily past 7 days)
            LocalDate today = LocalDate.now();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
            Map<String, Integer> trendMap = new LinkedHashMap<>();
            for (int i = 6; i >= 0; i--) {
                LocalDate day = today.minusDays(i);
                trendMap.put(day.format(fmt), 0);
            }

            String trendSql = "SELECT DATE_FORMAT(created_at, '%m-%d') AS day_str, COUNT(*) AS cnt " +
                    "FROM cs_incidents " +
                    "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                    (filterUserId != null ? "AND reported_by = ? " : "") +
                    "GROUP BY day_str";
            try (PreparedStatement ps = conn.prepareStatement(trendSql)) {
                if (filterUserId != null) ps.setLong(1, filterUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String dayStr = rs.getString("day_str");
                        trendMap.put(dayStr, rs.getInt("cnt"));
                    }
                }
            }
            stats.setTrendDaily(trendMap);

            // 9. SLA Performance calculation
            calculateSlaStats(conn, stats, filterUserId);

            // 10. Threat Intel IOC Breakdown
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT classification, COUNT(*) as cnt FROM cs_threat_intelligence GROUP BY classification")) {
                Map<String, Integer> map = new LinkedHashMap<>();
                while (rs.next()) {
                    map.put(rs.getString("classification"), rs.getInt("cnt"));
                }
                stats.setThreatIntelDistribution(map);
            }

        } catch (SQLException e) {
            System.err.println("[WARN] Error loading dashboard statistics: " + e.getMessage());
        }

        return stats;
    }

    private void calculateSlaStats(Connection conn, DashboardStats stats, Long filterUserId) throws SQLException {
        // Fetch SLA thresholds in minutes
        int slaCritical = 15;
        int slaHigh = 30;
        int slaMedium = 120;
        int slaLow = 480;

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT setting_key, setting_value FROM cs_system_settings WHERE setting_key LIKE 'sla_%'")) {
            while (rs.next()) {
                String k = rs.getString("setting_key");
                int val = Integer.parseInt(rs.getString("setting_value"));
                if ("sla_critical_minutes".equals(k)) slaCritical = val;
                else if ("sla_high_minutes".equals(k)) slaHigh = val;
                else if ("sla_medium_minutes".equals(k)) slaMedium = val;
                else if ("sla_low_minutes".equals(k)) slaLow = val;
            }
        } catch (Exception ignored) {}

        String sql = "SELECT severity, status, created_at, resolved_at FROM cs_incidents " +
                     (filterUserId != null ? "WHERE reported_by = ?" : "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (filterUserId != null) ps.setLong(1, filterUserId);
            try (ResultSet rs = ps.executeQuery()) {
                int within = 0;
                int approaching = 0;
                int breached = 0;
                long now = System.currentTimeMillis();

                while (rs.next()) {
                    String sev = rs.getString("severity");
                    String stat = rs.getString("status");
                    Timestamp created = rs.getTimestamp("created_at");
                    Timestamp resolved = rs.getTimestamp("resolved_at");

                    int targetMinutes;
                    if ("CRITICAL".equalsIgnoreCase(sev)) targetMinutes = slaCritical;
                    else if ("HIGH".equalsIgnoreCase(sev)) targetMinutes = slaHigh;
                    else if ("MEDIUM".equalsIgnoreCase(sev)) targetMinutes = slaMedium;
                    else targetMinutes = slaLow;

                    long elapsedMinutes;
                    if (resolved != null) {
                        elapsedMinutes = (resolved.getTime() - created.getTime()) / (1000 * 60);
                    } else {
                        elapsedMinutes = (now - created.getTime()) / (1000 * 60);
                    }

                    if (elapsedMinutes > targetMinutes) {
                        breached++;
                    } else if (elapsedMinutes >= (targetMinutes * 0.70)) {
                        approaching++;
                    } else {
                        within++;
                    }
                }

                stats.setWithinSlaCount(within);
                stats.setApproachingSlaCount(approaching);
                stats.setBreachedSlaCount(breached);
            }
        }
    }
}
