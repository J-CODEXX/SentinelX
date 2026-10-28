package com.cybersentinel.model;

import java.sql.Timestamp;

public class ThreatIntelligence {
    private long id;
    private String type; // IP Address, Domain, URL, File Hash, Email Address, Username, Hostname
    private String value;
    private String classification; // BENIGN, SUSPICIOUS, MALICIOUS
    private String threatLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private String source = "Internal Database";
    private String description;
    private Timestamp createdAt;

    public ThreatIntelligence() {}

    public ThreatIntelligence(String type, String value, String classification, String threatLevel, String source, String description) {
        this.type = type;
        this.value = value;
        this.classification = classification;
        this.threatLevel = threatLevel;
        this.source = source != null ? source : "Internal Database";
        this.description = description;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public String getThreatLevel() { return threatLevel; }
    public void setThreatLevel(String threatLevel) { this.threatLevel = threatLevel; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
