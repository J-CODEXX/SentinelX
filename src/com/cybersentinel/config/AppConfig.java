package com.cybersentinel.config;

/**
 * Global configuration settings for CyberSentinel SOC Platform.
 */
public final class AppConfig {
    public static final String APP_NAME = "CyberSentinel";
    public static final String APP_TAGLINE = "SOC Incident Management & Response Platform";
    public static final String APP_VERSION = "2.4.0-PRIME";

    // Database Configuration
    public static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/cyber_projects?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=5000&socketTimeout=10000";
    public static final String DEFAULT_DB_USER = "root";
    public static final String DEFAULT_DB_PASS = "";

    // Evidence Directory
    public static final String EVIDENCE_DIR = "data/evidence";
    public static final String REPORTS_DIR = "data/reports";

    // Security Lockout Policy Defaults
    public static final int MAX_FAILED_LOGINS = 5;
    public static final int LOCKOUT_MINUTES = 15;

    // Evidence max upload size (default 20MB)
    public static final long DEFAULT_MAX_EVIDENCE_BYTES = 20 * 1024 * 1024;

    private AppConfig() {}

    public static String getDbUrl() {
        String env = System.getenv("CYBERSENTINEL_DB_URL");
        return (env != null && !env.isBlank()) ? env : DEFAULT_DB_URL;
    }

    public static String getDbUser() {
        String env = System.getenv("CYBERSENTINEL_DB_USER");
        return (env != null && !env.isBlank()) ? env : DEFAULT_DB_USER;
    }

    public static String getDbPassword() {
        String env = System.getenv("CYBERSENTINEL_DB_PASSWORD");
        return (env != null && !env.isBlank()) ? env : DEFAULT_DB_PASS;
    }
}
