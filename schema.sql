-- CyberSentinel SOC Database Schema
-- Target: cyber_projects

CREATE DATABASE IF NOT EXISTS cyber_projects;
USE cyber_projects;

-- Roles table
CREATE TABLE IF NOT EXISTS cs_roles (
    role_name VARCHAR(20) PRIMARY KEY,
    description VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

INSERT IGNORE INTO cs_roles (role_name, description) VALUES
('USER', 'Standard end-user reporting and tracking incidents'),
('ANALYST', 'Security analyst investigating, containing and remediating incidents'),
('SUPERMOD', 'Security Operations Center Super Moderator / Administrator');

-- Users table
CREATE TABLE IF NOT EXISTS cs_users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role_name VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_name) REFERENCES cs_roles(role_name)
) ENGINE=InnoDB;

-- Login attempts for security auditing and lockouts
CREATE TABLE IF NOT EXISTS cs_login_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45) DEFAULT '127.0.0.1',
    success BOOLEAN NOT NULL,
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- System Settings
CREATE TABLE IF NOT EXISTS cs_system_settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Incidents table
CREATE TABLE IF NOT EXISTS cs_incidents (
    id INT AUTO_INCREMENT PRIMARY KEY,
    incident_code VARCHAR(30) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    risk_score INT NOT NULL DEFAULT 0,
    priority VARCHAR(10) NOT NULL DEFAULT 'P3',
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    reported_by INT NOT NULL,
    assigned_to INT NULL,
    affected_system VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL DEFAULT NULL,
    is_simulated BOOLEAN NOT NULL DEFAULT FALSE,
    escalation_reason TEXT NULL,
    root_cause TEXT NULL,
    actions_taken TEXT NULL,
    resolution_summary TEXT NULL,
    user_recommendations TEXT NULL,
    INDEX idx_incident_status (status),
    INDEX idx_incident_severity (severity),
    INDEX idx_incident_risk (risk_score),
    INDEX idx_incident_reported_by (reported_by),
    INDEX idx_incident_assigned_to (assigned_to),
    FOREIGN KEY (reported_by) REFERENCES cs_users(id),
    FOREIGN KEY (assigned_to) REFERENCES cs_users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Incident history (state transitions, timeline)
CREATE TABLE IF NOT EXISTS cs_incident_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id INT NOT NULL,
    actor_id INT NULL,
    actor_name VARCHAR(100) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    old_status VARCHAR(30) NULL,
    new_status VARCHAR(30) NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_history_incident (incident_id),
    FOREIGN KEY (incident_id) REFERENCES cs_incidents(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Investigation notes (internal SOC notes history)
CREATE TABLE IF NOT EXISTS cs_investigation_notes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    incident_id INT NOT NULL,
    analyst_id INT NOT NULL,
    analyst_name VARCHAR(100) NOT NULL,
    note TEXT NOT NULL,
    is_internal BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notes_incident (incident_id),
    FOREIGN KEY (incident_id) REFERENCES cs_incidents(id) ON DELETE CASCADE,
    FOREIGN KEY (analyst_id) REFERENCES cs_users(id)
) ENGINE=InnoDB;

-- Evidence table
CREATE TABLE IF NOT EXISTS cs_evidence (
    id INT AUTO_INCREMENT PRIMARY KEY,
    incident_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    sha256_hash VARCHAR(64) NOT NULL,
    description TEXT NULL,
    uploaded_by INT NOT NULL,
    uploaded_by_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_evidence_incident (incident_id),
    FOREIGN KEY (incident_id) REFERENCES cs_incidents(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES cs_users(id)
) ENGINE=InnoDB;

-- Threat Intelligence Database (IOC Master repository)
CREATE TABLE IF NOT EXISTS cs_threat_intelligence (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(30) NOT NULL,
    value VARCHAR(255) NOT NULL UNIQUE,
    classification VARCHAR(30) NOT NULL,
    threat_level VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    source VARCHAR(100) NOT NULL DEFAULT 'Internal Database',
    reference_count INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_threat_val (value)
) ENGINE=InnoDB;

-- Incident Indicators of Compromise (linked to specific incidents)
CREATE TABLE IF NOT EXISTS cs_indicators (
    id INT AUTO_INCREMENT PRIMARY KEY,
    incident_id INT NOT NULL,
    type VARCHAR(30) NOT NULL,
    value VARCHAR(255) NOT NULL,
    classification VARCHAR(30) NOT NULL,
    threat_level VARCHAR(20) NOT NULL,
    description TEXT NULL,
    is_threat_intel_match BOOLEAN NOT NULL DEFAULT FALSE,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_indicator_incident (incident_id),
    INDEX idx_indicator_val (value),
    FOREIGN KEY (incident_id) REFERENCES cs_incidents(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES cs_users(id)
) ENGINE=InnoDB;

-- Recommended actions checklist per incident
CREATE TABLE IF NOT EXISTS cs_incident_actions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    incident_id INT NOT NULL,
    action_description VARCHAR(255) NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_by VARCHAR(100) NULL,
    completed_at TIMESTAMP NULL DEFAULT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    INDEX idx_actions_incident (incident_id),
    FOREIGN KEY (incident_id) REFERENCES cs_incidents(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Notifications
CREATE TABLE IF NOT EXISTS cs_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL, -- NULL indicates broadcast to all relevant staff (Analysts / Supermod)
    role_target VARCHAR(20) NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'INFO',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    related_incident_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notif_user (user_id),
    INDEX idx_notif_read (is_read),
    FOREIGN KEY (user_id) REFERENCES cs_users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Audit logs (Immutable)
CREATE TABLE IF NOT EXISTS cs_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id INT NULL,
    actor_username VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NULL,
    description TEXT NOT NULL,
    ip_address VARCHAR(45) DEFAULT '127.0.0.1',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_actor (actor_username),
    INDEX idx_audit_action (action),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB;

-- Analyst Registration Requests (pending Supermod approval)
CREATE TABLE IF NOT EXISTS cs_analyst_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(128) NOT NULL,
    password_salt VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT NULL,
    reviewed_by INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL DEFAULT NULL,
    INDEX idx_analyst_req_status (status),
    INDEX idx_analyst_req_username (username),
    FOREIGN KEY (reviewed_by) REFERENCES cs_users(id) ON DELETE SET NULL
) ENGINE=InnoDB;
