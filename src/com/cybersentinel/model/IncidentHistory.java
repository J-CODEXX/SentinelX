package com.cybersentinel.model;

import java.sql.Timestamp;

public class IncidentHistory {
    private long id;
    private long incidentId;
    private long actorId;
    private String actorName = "System";
    private String event;
    private String description;
    private Timestamp createdAt;

    public IncidentHistory() {}

    public IncidentHistory(long incidentId, long actorId, String actorName, String event, String description) {
        this.incidentId = incidentId;
        this.actorId = actorId;
        this.actorName = actorName;
        this.event = event;
        this.description = description;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getIncidentId() { return incidentId; }
    public void setIncidentId(long incidentId) { this.incidentId = incidentId; }

    public long getActorId() { return actorId; }
    public void setActorId(long actorId) { this.actorId = actorId; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
