package com.cybersentinel.model;

import java.sql.Timestamp;

public class Incident {
    private long id;
    private String incidentCode;
    private String title;
    private String type;
    private String description;
    private Severity severity = Severity.MEDIUM;
    private int riskScore = 30;
    private Priority priority = Priority.P3;
    private IncidentStatus status = IncidentStatus.OPEN;
    private long reportedBy;
    private String reportedByName = "";
    private Long assignedTo;
    private String assignedToName = "";
    private String affectedSystem = "General Infrastructure";
    private String resolutionSummary;
    private String rootCause;
    private String actionsTaken;
    private String recommendations;
    private boolean isSimulated = false;
    private Timestamp observedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp resolvedAt;

    public Incident() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getIncidentCode() { return incidentCode; }
    public void setIncidentCode(String incidentCode) { this.incidentCode = incidentCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity != null ? severity : Severity.MEDIUM; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { 
        this.riskScore = Math.max(0, Math.min(100, riskScore));
        this.priority = Priority.fromRiskScore(this.riskScore);
    }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority != null ? priority : Priority.P3; }

    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status != null ? status : IncidentStatus.OPEN; }

    public long getReportedBy() { return reportedBy; }
    public void setReportedBy(long reportedBy) { this.reportedBy = reportedBy; }

    public String getReportedByName() { return reportedByName; }
    public void setReportedByName(String reportedByName) { this.reportedByName = reportedByName != null ? reportedByName : ""; }

    public Long getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Long assignedTo) { this.assignedTo = assignedTo; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName != null ? assignedToName : ""; }

    public String getAffectedSystem() { return affectedSystem; }
    public void setAffectedSystem(String affectedSystem) { this.affectedSystem = affectedSystem; }

    public String getResolutionSummary() { return resolutionSummary; }
    public void setResolutionSummary(String resolutionSummary) { this.resolutionSummary = resolutionSummary; }

    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }

    public String getActionsTaken() { return actionsTaken; }
    public void setActionsTaken(String actionsTaken) { this.actionsTaken = actionsTaken; }

    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }

    public boolean isSimulated() { return isSimulated; }
    public void setSimulated(boolean simulated) { isSimulated = simulated; }

    public Timestamp getObservedAt() { return observedAt; }
    public void setObservedAt(Timestamp observedAt) { this.observedAt = observedAt; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }

    public boolean isAssigned() {
        return assignedTo != null && assignedTo > 0;
    }

    public long getElapsedMinutes() {
        if (createdAt == null) return 0;
        long end = (resolvedAt != null) ? resolvedAt.getTime() : System.currentTimeMillis();
        return Math.max(0, (end - createdAt.getTime()) / (1000 * 60));
    }
}
