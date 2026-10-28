package com.cybersentinel.service;

import com.cybersentinel.dao.*;
import com.cybersentinel.engine.ClassificationEngine;
import com.cybersentinel.engine.RecommendationEngine;
import com.cybersentinel.engine.RiskEngine;
import com.cybersentinel.model.*;
import com.cybersentinel.security.RbacService;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class IncidentService {
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final InvestigationDAO investigationDAO = new InvestigationDAO();
    private final IndicatorDAO indicatorDAO = new IndicatorDAO();
    private final EvidenceDAO evidenceDAO = new EvidenceDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final AuditDAO auditDAO = new AuditDAO();

    public Incident reportIncident(Incident incident, UserSession session) throws Exception {
        RbacService.checkAuthenticated(session);

        if (incident.getTitle() == null || incident.getTitle().trim().isBlank()) {
            throw new IllegalArgumentException("Incident Title is required.");
        }
        if (incident.getDescription() == null || incident.getDescription().trim().isBlank()) {
            throw new IllegalArgumentException("Incident Description is required.");
        }
        if (incident.getType() == null || incident.getType().isBlank()) {
            incident.setType("Suspicious Activity");
        }

        // 1. Generate unique Incident Code (e.g., INC-20261024-4821)
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        String code = "INC-" + datePart + "-" + rand;
        incident.setIncidentCode(code);

        // 2. Automated Classification Analysis
        ClassificationEngine.ClassificationResult classResult = 
            ClassificationEngine.classify(incident.getTitle(), incident.getDescription());
        
        // If user submitted generic/other, adopt classification suggestion
        if ("Other".equalsIgnoreCase(incident.getType()) || "Suspicious Activity".equalsIgnoreCase(incident.getType())) {
            incident.setType(classResult.getSuggestedCategory());
        }

        // 3. Risk Engine Scoring
        RiskEngine.RiskAssessment risk = RiskEngine.calculateRisk(
            incident.getSeverity(),
            incident.getType(),
            incident.getAffectedSystem(),
            null,
            0
        );
        incident.setRiskScore(risk.getScore());
        incident.setPriority(risk.getCalculatedPriority());

        // Set status and reporter
        incident.setStatus(IncidentStatus.OPEN);
        incident.setReportedBy(session.getUserId());
        if (incident.getObservedAt() == null) {
            incident.setObservedAt(new Timestamp(System.currentTimeMillis()));
        }

        // 4. Save to Database
        long id = incidentDAO.createIncident(incident);
        incident.setId(id);

        // 5. Generate Response Recommendations Checklist
        List<String> recs = RecommendationEngine.getRecommendations(incident.getType());
        int order = 0;
        for (String rec : recs) {
            investigationDAO.addAction(new IncidentAction(id, rec, order++));
        }

        // 6. Record Initial Timeline History
        investigationDAO.addHistory(new IncidentHistory(
            id, session.getUserId(), session.getUsername(), "REPORTED",
            "Incident reported by " + session.getUsername() + ". Auto-classified as " + 
            classResult.getSuggestedCategory() + " with Risk Score " + risk.getScore() + "/100."
        ));

        // 7. Staff Notification & Audit Log
        notificationDAO.broadcastToRole(Role.ANALYST.getId(), id, 
            "New " + incident.getSeverity().name() + " incident reported: " + incident.getIncidentCode() + " - " + incident.getTitle(),
            incident.getSeverity() == Severity.CRITICAL ? "CRITICAL" : "INFO"
        );

        auditDAO.log(new AuditLog(
            session.getUserId(), session.getUsername(), "CREATE_INCIDENT", "INCIDENT", code,
            "Incident created: " + incident.getTitle() + " [Risk: " + incident.getRiskScore() + "]"
        ));

        return incident;
    }

    public boolean claimIncident(long incidentId, UserSession session) throws Exception {
        RbacService.checkAuthenticated(session);
        if (!RbacService.canClaimIncident(session)) {
            throw new SecurityException("Only SOC Analysts and Supermods can claim incidents.");
        }

        Incident inc = incidentDAO.findById(incidentId);
        if (inc == null) throw new IllegalArgumentException("Incident not found.");

        boolean claimed = incidentDAO.claimIncident(incidentId, session.getUserId());
        if (claimed) {
            investigationDAO.addHistory(new IncidentHistory(
                incidentId, session.getUserId(), session.getUsername(), "CLAIMED",
                "Incident claimed and assigned to " + session.getUsername() + "."
            ));

            notificationDAO.createNotification(new Notification(
                inc.getReportedBy(), incidentId,
                "Your incident " + inc.getIncidentCode() + " has been assigned to SOC Analyst " + session.getUsername() + ".",
                "INFO"
            ));

            auditDAO.log(new AuditLog(
                session.getUserId(), session.getUsername(), "CLAIM_INCIDENT", "INCIDENT", inc.getIncidentCode(),
                "Incident assigned to analyst " + session.getUsername()
            ));
        }
        return claimed;
    }

    public void updateStatus(long incidentId, IncidentStatus targetStatus, UserSession session, String reason) throws Exception {
        RbacService.checkAuthenticated(session);
        Incident inc = incidentDAO.findById(incidentId);
        if (inc == null) throw new IllegalArgumentException("Incident not found.");

        if (!inc.getStatus().canTransitionTo(targetStatus, session.getRole())) {
            throw new IllegalStateException("Invalid status transition from " + inc.getStatus().getDisplayName() + 
                " to " + targetStatus.getDisplayName() + " for role " + session.getRole());
        }

        incidentDAO.updateStatus(incidentId, targetStatus);

        String note = "Status updated to " + targetStatus.getDisplayName() + 
                      (reason != null && !reason.isBlank() ? " (" + reason.trim() + ")" : "");
        investigationDAO.addHistory(new IncidentHistory(
            incidentId, session.getUserId(), session.getUsername(), "STATUS_CHANGE", note
        ));

        notificationDAO.createNotification(new Notification(
            inc.getReportedBy(), incidentId,
            "Incident " + inc.getIncidentCode() + " status updated to " + targetStatus.getDisplayName() + ".",
            "STATUS_UPDATE"
        ));

        auditDAO.log(new AuditLog(
            session.getUserId(), session.getUsername(), "UPDATE_STATUS", "INCIDENT", inc.getIncidentCode(),
            "Status changed from " + inc.getStatus().name() + " to " + targetStatus.name()
        ));
    }

    public void assignIncident(long incidentId, Long analystId, UserSession session) throws Exception {
        RbacService.requireRole(session, Role.SUPERMOD, Role.ANALYST);
        Incident inc = incidentDAO.findById(incidentId);
        if (inc == null) throw new IllegalArgumentException("Incident not found.");

        incidentDAO.assignIncident(incidentId, analystId);

        String desc = (analystId != null && analystId > 0) ? "Assigned to analyst ID " + analystId : "Unassigned";
        investigationDAO.addHistory(new IncidentHistory(
            incidentId, session.getUserId(), session.getUsername(), "ASSIGNMENT", desc
        ));

        auditDAO.log(new AuditLog(
            session.getUserId(), session.getUsername(), "ASSIGN_INCIDENT", "INCIDENT", inc.getIncidentCode(),
            "Assignment updated: " + desc
        ));
    }

    public void resolveIncident(long incidentId, String rootCause, String actionsTaken, 
                                String resolutionSummary, String recommendations, UserSession session) throws Exception {
        RbacService.checkAuthenticated(session);
        Incident inc = incidentDAO.findById(incidentId);
        if (inc == null) throw new IllegalArgumentException("Incident not found.");

        if (resolutionSummary == null || resolutionSummary.trim().isBlank()) {
            throw new IllegalArgumentException("User-facing Resolution Summary is required.");
        }

        incidentDAO.resolveIncident(incidentId, rootCause, actionsTaken, resolutionSummary, recommendations);

        investigationDAO.addHistory(new IncidentHistory(
            incidentId, session.getUserId(), session.getUsername(), "RESOLVED",
            "Incident resolved by " + session.getUsername() + ". Summary: " + resolutionSummary
        ));

        // Notify reporter
        notificationDAO.createNotification(new Notification(
            inc.getReportedBy(), incidentId,
            "Your reported incident " + inc.getIncidentCode() + " has been RESOLVED by the SOC team.",
            "RESOLVED"
        ));

        // Notify Supermods
        notificationDAO.broadcastToRole(Role.SUPERMOD.getId(), incidentId,
            "Incident " + inc.getIncidentCode() + " was resolved by " + session.getUsername(), "INFO");

        auditDAO.log(new AuditLog(
            session.getUserId(), session.getUsername(), "RESOLVE_INCIDENT", "INCIDENT", inc.getIncidentCode(),
            "Incident resolved: " + rootCause
        ));
    }

    public void escalateIncident(long incidentId, String reason, UserSession session) throws Exception {
        RbacService.checkAuthenticated(session);
        Incident inc = incidentDAO.findById(incidentId);
        if (inc == null) throw new IllegalArgumentException("Incident not found.");

        if (reason == null || reason.trim().isBlank()) {
            throw new IllegalArgumentException("Escalation Reason is required.");
        }

        incidentDAO.updateStatus(incidentId, IncidentStatus.ESCALATED);

        investigationDAO.addHistory(new IncidentHistory(
            incidentId, session.getUserId(), session.getUsername(), "ESCALATED",
            "Escalated to SOC Command Center. Reason: " + reason.trim()
        ));

        // Generate high-priority notification for Supermod
        notificationDAO.broadcastToRole(Role.SUPERMOD.getId(), incidentId,
            "🚨 ESCALATION ALERT: " + inc.getIncidentCode() + " - " + reason.trim(), "CRITICAL"
        );

        auditDAO.log(new AuditLog(
            session.getUserId(), session.getUsername(), "ESCALATE_INCIDENT", "INCIDENT", inc.getIncidentCode(),
            "Escalated by " + session.getUsername() + ": " + reason.trim()
        ));
    }

    public void updateSeverity(long incidentId, Severity newSeverity, UserSession session) throws Exception {
        RbacService.requireRole(session, Role.ANALYST, Role.SUPERMOD);
        Incident inc = incidentDAO.findById(incidentId);
        if (inc == null) throw new IllegalArgumentException("Incident not found.");

        List<Indicator> iocs = indicatorDAO.getByIncident(incidentId);
        int evCount = evidenceDAO.getByIncident(incidentId).size();

        RiskEngine.RiskAssessment risk = RiskEngine.calculateRisk(
            newSeverity, inc.getType(), inc.getAffectedSystem(), iocs, evCount
        );

        incidentDAO.updateSeverityAndRisk(incidentId, newSeverity, risk.getScore(), risk.getCalculatedPriority());

        investigationDAO.addHistory(new IncidentHistory(
            incidentId, session.getUserId(), session.getUsername(), "SEVERITY_OVERRIDE",
            "Severity updated to " + newSeverity.name() + ". Risk score recalculated to " + risk.getScore() + "/100."
        ));

        auditDAO.log(new AuditLog(
            session.getUserId(), session.getUsername(), "UPDATE_SEVERITY", "INCIDENT", inc.getIncidentCode(),
            "Severity set to " + newSeverity.name() + " [Risk: " + risk.getScore() + "]"
        ));
    }

    public void recalculateRisk(long incidentId, UserSession session) throws Exception {
        RbacService.checkAuthenticated(session);
        Incident inc = incidentDAO.findById(incidentId);
        if (inc == null) throw new IllegalArgumentException("Incident not found.");

        List<Indicator> iocs = indicatorDAO.getByIncident(incidentId);
        int evCount = evidenceDAO.getByIncident(incidentId).size();

        RiskEngine.RiskAssessment risk = RiskEngine.calculateRisk(
            inc.getSeverity(), inc.getType(), inc.getAffectedSystem(), iocs, evCount
        );

        incidentDAO.updateSeverityAndRisk(incidentId, inc.getSeverity(), risk.getScore(), risk.getCalculatedPriority());

        investigationDAO.addHistory(new IncidentHistory(
            incidentId, session.getUserId(), session.getUsername(), "RISK_RECALCULATION",
            "Risk Engine recalculated score to " + risk.getScore() + "/100 (" + risk.getCalculatedPriority().getLabel() + ")."
        ));
    }

    public Incident getIncident(long id) throws SQLException {
        return incidentDAO.findById(id);
    }

    public List<Incident> getIncidents(String query, String status, String severity, String type, 
                                       Boolean simulated, Long assignedTo, Long reportedBy, String sortOrder) throws SQLException {
        return incidentDAO.findAll(query, status, severity, type, simulated, assignedTo, reportedBy, sortOrder);
    }

    public boolean deleteIncident(long incidentId, UserSession session) throws Exception {
        RbacService.checkAuthenticated(session);
        if (session.getRole() != Role.SUPERMOD && session.getRole() != Role.ANALYST) {
            throw new SecurityException("Access Denied: Only SOC staff can delete incidents.");
        }

        Incident inc = incidentDAO.findById(incidentId);
        if (inc == null) {
            throw new IllegalArgumentException("Incident #" + incidentId + " not found.");
        }

        boolean ok = incidentDAO.delete(incidentId);
        if (ok) {
            auditDAO.log(new AuditLog(
                session.getUserId(), session.getUsername(), "DELETE_INCIDENT", "INCIDENT", inc.getIncidentCode(),
                "Permanently deleted incident " + inc.getIncidentCode() + ": " + inc.getTitle()
            ));
        }
        return ok;
    }
}
