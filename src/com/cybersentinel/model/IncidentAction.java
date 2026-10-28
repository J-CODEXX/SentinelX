package com.cybersentinel.model;

import java.sql.Timestamp;

public class IncidentAction {
    private long id;
    private long incidentId;
    private String actionText;
    private boolean completed;
    private Long completedBy;
    private String completedByName;
    private Timestamp completedAt;
    private int sortOrder;

    public IncidentAction() {}

    public IncidentAction(long incidentId, String actionText, int sortOrder) {
        this.incidentId = incidentId;
        this.actionText = actionText;
        this.sortOrder = sortOrder;
        this.completed = false;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getIncidentId() { return incidentId; }
    public void setIncidentId(long incidentId) { this.incidentId = incidentId; }

    public String getActionText() { return actionText; }
    public void setActionText(String actionText) { this.actionText = actionText; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Long getCompletedBy() { return completedBy; }
    public void setCompletedBy(Long completedBy) { this.completedBy = completedBy; }

    public String getCompletedByName() { return completedByName; }
    public void setCompletedByName(String completedByName) { this.completedByName = completedByName; }

    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
