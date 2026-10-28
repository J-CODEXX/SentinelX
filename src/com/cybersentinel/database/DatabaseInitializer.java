package com.cybersentinel.database;

import com.cybersentinel.model.Role;
import com.cybersentinel.security.PasswordUtil;

import java.sql.*;

public final class DatabaseInitializer {

    private DatabaseInitializer() {}

    public static void initialize() throws SQLException {
        // 1. Verify connection
        try (Connection conn = DatabaseConnection.getConnection()) {
            ensureRoles(conn);
            ensureSettings(conn);
            ensureDefaultAccounts(conn);
            ensureThreatIntelSeed(conn);
            ensureDemoIncidents(conn);
            ensureAnalystRequestsTable(conn);
        }
    }

    private static void ensureRoles(Connection conn) throws SQLException {
        String sql = "INSERT IGNORE INTO cs_roles (id, name) VALUES " +
                     "(1, 'USER'), (2, 'ANALYST'), (3, 'SUPERMOD')";
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    private static void ensureSettings(Connection conn) throws SQLException {
        String sql = "INSERT IGNORE INTO cs_system_settings (setting_key, setting_value) VALUES " +
                     "('sla_critical_minutes', '15'), " +
                     "('sla_high_minutes', '30'), " +
                     "('sla_medium_minutes', '120'), " +
                     "('sla_low_minutes', '480'), " +
                     "('max_evidence_bytes', '20971520'), " +
                     "('lockout_threshold', '5'), " +
                     "('lockout_duration_minutes', '15')";
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    private static void ensureDefaultAccounts(Connection conn) throws SQLException {
        // Seed Supermod: supermod / AdminPass123!
        createUserIfMissing(conn, "supermod", "supermod@cybersentinel.soc", "AdminPass123!", 3);
        // Seed Analysts: analyst_alex / AnalystPass123!, analyst_sarah / AnalystPass123!
        createUserIfMissing(conn, "analyst_alex", "alex.rivera@cybersentinel.soc", "AnalystPass123!", 2);
        createUserIfMissing(conn, "analyst_sarah", "sarah.chen@cybersentinel.soc", "AnalystPass123!", 2);
        // Seed Users: john_user / UserPass123!, jane_user / UserPass123!
        createUserIfMissing(conn, "john_user", "john.doe@company.corp", "UserPass123!", 1);
        createUserIfMissing(conn, "jane_user", "jane.smith@company.corp", "UserPass123!", 1);
    }

    private static void createUserIfMissing(Connection conn, String username, String email, String password, int roleId) throws SQLException {
        String checkSql = "SELECT id FROM cs_users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return; // already exists
            }
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);
        String insertSql = "INSERT INTO cs_users (username, email, password_hash, password_salt, role_id, active, failed_attempts) " +
                          "VALUES (?, ?, ?, ?, ?, TRUE, 0)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hash);
            ps.setString(4, salt);
            ps.setInt(5, roleId);
            ps.executeUpdate();
        }
    }

    private static void ensureThreatIntelSeed(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM cs_threat_intelligence")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        String insertSql = "INSERT INTO cs_threat_intelligence (type, value, classification, threat_level, source, description) VALUES " +
            "('IP Address', '185.220.101.5', 'MALICIOUS', 'CRITICAL', 'CISA Threat Bulletin', 'Active Tor exit node associated with FIN7 ransomware campaigns'), " +
            "('Domain', 'login-security-verify-portal.com', 'MALICIOUS', 'HIGH', 'Internal Threat Feed', 'Known credential harvester spoofing Microsoft 365 login'), " +
            "('URL', 'http://update-system-patch.online/payload.exe', 'MALICIOUS', 'CRITICAL', 'VirusTotal Enterprise', 'Direct payload delivery URL for RedLine Stealer'), " +
            "('File Hash', 'a3c4f728b9911e80931238914ab282c0b65f7281099238491823901283912a3f', 'MALICIOUS', 'CRITICAL', 'Mandiant Threat Intelligence', 'Cobalt Strike 4.8 malleable C2 beacon DLL'), " +
            "('Email Address', 'finance-update-secure@outlook-cfo-alert.com', 'MALICIOUS', 'HIGH', 'Email Gateway Quarantine', 'Executive BEC spoofing campaign targeting finance staff'), " +
            "('IP Address', '91.240.118.232', 'MALICIOUS', 'CRITICAL', 'CrowdStrike Intelligence', 'LockBit 3.0 ransomware affiliate command server'), " +
            "('Domain', 'cdn-jquery-analytics.cc', 'SUSPICIOUS', 'MEDIUM', 'Emerging Threats', 'Obfuscated JavaScript web skimmer infrastructure'), " +
            "('IP Address', '8.8.8.8', 'BENIGN', 'LOW', 'Google Public DNS', 'Legitimate DNS service often probed during reachability tests')";

        try (Statement st = conn.createStatement()) {
            st.executeUpdate(insertSql);
        }
    }

    private static void ensureAnalystRequestsTable(Connection conn) throws SQLException {
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
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    private static long getUserId(Connection conn, String username) throws SQLException {
        String sql = "SELECT id FROM cs_users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        }
        return 1L;
    }

    private static void ensureDemoIncidents(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM cs_incidents")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        long johnId = getUserId(conn, "john_user");
        long janeId = getUserId(conn, "jane_user");
        long alexId = getUserId(conn, "analyst_alex");
        long supermodId = getUserId(conn, "supermod");

        // Incident 1: Phishing
        long inc1 = insertDemoIncident(conn, "INC-20261024-1001",
            "Executive Impersonation Wire Fraud Phishing",
            "Phishing",
            "Executive Assistant received an urgent email purportedly from the CEO requesting an emergency $75,000 wire transfer for an acquisition. Email contained an attachment 'Wire_Instructions.pdf.exe'.",
            "HIGH", 68, "P2", "INVESTIGATING", johnId, (int) alexId, "Finance-Exchange-Mailbox", false);

        insertDemoHistory(conn, inc1, johnId, "REPORTED", "Reported by john_user via incident portal.");
        insertDemoHistory(conn, inc1, alexId, "CLAIMED", "Analyst analyst_alex claimed the incident.");
        insertDemoNote(conn, inc1, alexId, "Email header analysis confirms SPF fail and display name spoofing. Domain outlook-cfo-alert.com was registered 48 hours ago.");
        insertDemoIoc(conn, inc1, "Email Address", "finance-update-secure@outlook-cfo-alert.com", "MALICIOUS", "HIGH", alexId);

        // Incident 2: Ransomware Outbreak
        long inc2 = insertDemoIncident(conn, "INC-20261024-1002",
            "LockBit Ransomware Encryption on File Server",
            "Ransomware",
            "Users on Subnet 192.168.10.0/24 report network drives locked with README_LOCKBIT.txt left in directories. Volume Shadow Copies deleted via vssadmin.",
            "CRITICAL", 92, "P1", "CONTAINED", janeId, (int) alexId, "Corp-FileServer-Prod", false);

        insertDemoHistory(conn, inc2, janeId, "REPORTED", "Reported by jane_user after noticing locked spreadsheets.");
        insertDemoHistory(conn, inc2, alexId, "CLAIMED", "Analyst analyst_alex assigned.");
        insertDemoHistory(conn, inc2, alexId, "CONTAINED", "Isolated Subnet 10.0/24 at core firewall switch. Ransomware spread halted.");
        insertDemoNote(conn, inc2, alexId, "Network port 445 SMB blocked. Forensic memory dump captured from FileSrv-01.");
        insertDemoIoc(conn, inc2, "IP Address", "91.240.118.232", "MALICIOUS", "CRITICAL", alexId);

        // Incident 3: Cobalt Strike Beacon
        long inc3 = insertDemoIncident(conn, "INC-20261024-1003",
            "Cobalt Strike Beacon Ingress on Engineering Laptop",
            "Malware",
            "Endpoint EDR detected anomalous injection into svchost.exe communicating with known command-and-control server at 185.220.101.5.",
            "CRITICAL", 84, "P1", "OPEN", johnId, null, "Dev-Laptop-WS-09", false);

        insertDemoHistory(conn, inc3, johnId, "REPORTED", "Automated alert triggered from Defender EDR.");
        insertDemoIoc(conn, inc3, "IP Address", "185.220.101.5", "MALICIOUS", "CRITICAL", alexId);
        insertDemoIoc(conn, inc3, "File Hash", "a3c4f728b9911e80931238914ab282c0b65f7281099238491823901283912a3f", "MALICIOUS", "CRITICAL", alexId);

        // Incident 4: Account Compromise (Resolved)
        long inc4 = insertDemoIncident(conn, "INC-20261024-1004",
            "Suspicious Off-Hours VPN Authentication",
            "Account Compromise",
            "User account authenticated via SSL-VPN from unknown IP in Bucharest at 03:20 AM with 14 failed attempts prior.",
            "MEDIUM", 42, "P3", "RESOLVED", janeId, (int) supermodId, "Fortinet-VPN-Gateway", false);

        insertDemoHistory(conn, inc4, janeId, "REPORTED", "Reported by jane_user.");
        insertDemoHistory(conn, inc4, supermodId, "CLAIMED", "Claimed by supermod.");
        insertDemoHistory(conn, inc4, supermodId, "RESOLVED", "Compromised credential revoked and MFA hardware token re-enrolled.");
        insertDemoNote(conn, inc4, supermodId, "User confirmed they did not initiate this login session. Password reset and sessions killed.");

        // Update resolution columns for inc4
        String updateRes = "UPDATE cs_incidents SET root_cause = 'Password spray attack against legacy external VPN portal', " +
            "actions_taken = 'Killed active VPN tunnel, forced credential reset, enforced FIDO2 MFA requirement', " +
            "resolution_summary = 'Your account has been secured and MFA re-enabled. No internal lateral movement observed.', " +
            "recommendations = 'Ensure you do not reuse work passwords across external personal websites.', " +
            "resolved_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateRes)) {
            ps.setLong(1, inc4);
            ps.executeUpdate();
        }
    }

    private static long insertDemoIncident(Connection conn, String code, String title, String type, String desc,
                                           String sev, int risk, String priority, String status,
                                           long reportedBy, Integer assignedTo, String system, boolean sim) throws SQLException {
        String sql = "INSERT INTO cs_incidents (incident_code, title, type, description, severity, risk_score, " +
                     "priority, status, reported_by, assigned_to, affected_system, is_simulated, observed_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setString(2, title);
            ps.setString(3, type);
            ps.setString(4, desc);
            ps.setString(5, sev);
            ps.setInt(6, risk);
            ps.setString(7, priority);
            ps.setString(8, status);
            ps.setLong(9, reportedBy);
            if (assignedTo != null && assignedTo > 0) {
                ps.setLong(10, assignedTo);
            } else {
                ps.setNull(10, Types.BIGINT);
            }
            ps.setString(11, system);
            ps.setBoolean(12, sim);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return -1;
    }

    private static void insertDemoHistory(Connection conn, long incId, long actorId, String event, String desc) throws SQLException {
        String sql = "INSERT INTO cs_incident_history (incident_id, actor_id, event, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, incId);
            ps.setLong(2, actorId);
            ps.setString(3, event);
            ps.setString(4, desc);
            ps.executeUpdate();
        }
    }

    private static void insertDemoNote(Connection conn, long incId, long analystId, String note) throws SQLException {
        String sql = "INSERT INTO cs_investigation_notes (incident_id, analyst_id, note) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, incId);
            ps.setLong(2, analystId);
            ps.setString(3, note);
            ps.executeUpdate();
        }
    }

    private static void insertDemoIoc(Connection conn, long incId, String type, String value, String classif, String threat, long createdBy) throws SQLException {
        String sql = "INSERT INTO cs_indicators (incident_id, type, value, classification, threat_level, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, incId);
            ps.setString(2, type);
            ps.setString(3, value);
            ps.setString(4, classif);
            ps.setString(5, threat);
            ps.setLong(6, createdBy);
            ps.executeUpdate();
        }
    }
}
