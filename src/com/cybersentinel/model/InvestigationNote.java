package com.cybersentinel.model;

import java.sql.Timestamp;

public class InvestigationNote {
    private long id;
    private long incidentId;
    private long analystId;
    private String analystName = "Analyst";
    private String note;
    private Timestamp createdAt;

    public InvestigationNote() {}

    public InvestigationNote(long incidentId, long analystId, String analystName, String note) {
        this.incidentId = incidentId;
        this.analystId = analystId;
        this.analystName = analystName;
        this.note = note;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getIncidentId() { return incidentId; }
    public void setIncidentId(long incidentId) { this.incidentId = incidentId; }

    public long getAnalystId() { return analystId; }
    public void setAnalystId(long analystId) { this.analystId = analystId; }

    public String getAnalystName() { return analystName; }
    public void setAnalystName(String analystName) { this.analystName = analystName; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
