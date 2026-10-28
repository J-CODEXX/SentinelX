package com.cybersentinel.model;

import java.sql.Timestamp;

public class Notification {
    private long id;
    private Long recipientId; // null or specific user
    private Long incidentId;
    private String message;
    private String type; // INFO, WARNING, CRITICAL, SLA_ALERT
    private boolean read;
    private Timestamp createdAt;

    public Notification() {}

    public Notification(Long recipientId, Long incidentId, String message, String type) {
        this.recipientId = recipientId;
        this.incidentId = incidentId;
        this.message = message;
        this.type = type != null ? type : "INFO";
        this.read = false;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public Long getIncidentId() { return incidentId; }
    public void setIncidentId(Long incidentId) { this.incidentId = incidentId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
