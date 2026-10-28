package com.cybersentinel.model;

import java.sql.Timestamp;

public class Indicator {
    private long id;
    private long incidentId;
    private String type; // IP Address, Domain, URL, File Hash, Email Address, Username, Hostname
    private String value;
    private String classification = "UNKNOWN"; // BENIGN, SUSPICIOUS, MALICIOUS, UNKNOWN
    private String threatLevel = "UNKNOWN"; // LOW, MEDIUM, HIGH, CRITICAL, UNKNOWN
    private String description;
    private boolean threatIntelMatch = false;
    private long createdBy;
    private String createdByName = "";
    private Timestamp createdAt;

    public Indicator() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getIncidentId() { return incidentId; }
    public void setIncidentId(long incidentId) { this.incidentId = incidentId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public String getThreatLevel() { return threatLevel; }
    public void setThreatLevel(String threatLevel) { this.threatLevel = threatLevel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isThreatIntelMatch() { return threatIntelMatch; }
    public void setThreatIntelMatch(boolean threatIntelMatch) { this.threatIntelMatch = threatIntelMatch; }

    public long getCreatedBy() { return createdBy; }
    public void setCreatedBy(long createdBy) { this.createdBy = createdBy; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName != null ? createdByName : ""; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
