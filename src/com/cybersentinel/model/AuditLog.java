package com.cybersentinel.model;

import java.sql.Timestamp;

public class AuditLog {
    private long id;
    private Long actorId;
    private String actorUsername = "SYSTEM";
    private String action;
    private String entity;
    private String entityId;
    private String description;
    private Timestamp createdAt;

    public AuditLog() {}

    public AuditLog(Long actorId, String actorUsername, String action, String entity, String entityId, String description) {
        this.actorId = actorId;
        this.actorUsername = actorUsername != null ? actorUsername : "SYSTEM";
        this.action = action;
        this.entity = entity;
        this.entityId = entityId;
        this.description = description;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Long getActorId() { return actorId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }

    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
