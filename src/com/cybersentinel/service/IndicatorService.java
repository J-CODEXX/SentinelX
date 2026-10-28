package com.cybersentinel.service;

import com.cybersentinel.dao.*;
import com.cybersentinel.engine.RiskEngine;
import com.cybersentinel.model.*;
import com.cybersentinel.security.RbacService;

import java.sql.SQLException;
import java.util.List;

public class IndicatorService {
    private final IndicatorDAO indicatorDAO = new IndicatorDAO();
    private final ThreatIntelDAO threatIntelDAO = new ThreatIntelDAO();
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final InvestigationDAO investigationDAO = new InvestigationDAO();
    private final EvidenceDAO evidenceDAO = new EvidenceDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final AuditDAO auditDAO = new AuditDAO();

    public static class AddIndicatorResult {
        private final Indicator indicator;
        private final ThreatIntelligence matchedIntel; // null if no match

        public AddIndicatorResult(Indicator indicator, ThreatIntelligence matchedIntel) {
            this.indicator = indicator;
            this.matchedIntel = matchedIntel;
        }

        public Indicator getIndicator() { return indicator; }
        public ThreatIntelligence getMatchedIntel() { return matchedIntel; }
        public boolean isMatchFound() { return matchedIntel != null; }
    }

    public AddIndicatorResult addIndicator(long incidentId, String type, String value, 
                                          String description, UserSession session) throws Exception {
        RbacService.requireRole(session, Role.ANALYST, Role.SUPERMOD);

        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException("IOC Value is required.");
        }
        if (type == null || type.isBlank()) {
            type = "IP Address";
        }

        Incident inc = incidentDAO.findById(incidentId);
        if (inc == null) throw new IllegalArgumentException("Incident not found.");

        String cleanValue = value.trim();

        // 1. Query Threat Intelligence Master DB
        ThreatIntelligence match = threatIntelDAO.findMatch(cleanValue);

        Indicator ind = new Indicator();
        ind.setIncidentId(incidentId);
        ind.setType(type);
        ind.setValue(cleanValue);
        ind.setDescription(description != null ? description.trim() : "");
        ind.setCreatedBy(session.getUserId());
        ind.setCreatedByName(session.getUsername());

        if (match != null) {
            ind.setThreatIntelMatch(true);
            ind.setClassification(match.getClassification());
            ind.setThreatLevel(match.getThreatLevel());
            if (ind.getDescription().isBlank()) {
                ind.setDescription("Matched Threat Intel: " + match.getDescription());
            }
        } else {
            ind.setThreatIntelMatch(false);
            ind.setClassification("UNKNOWN");
            ind.setThreatLevel("UNKNOWN");
        }

        long id = indicatorDAO.addIndicator(ind);
        ind.setId(id);

        // 2. Recalculate Risk Score dynamically
        List<Indicator> iocs = indicatorDAO.getByIncident(incidentId);
        int evCount = evidenceDAO.getByIncident(incidentId).size();
        RiskEngine.RiskAssessment risk = RiskEngine.calculateRisk(
            inc.getSeverity(), inc.getType(), inc.getAffectedSystem(), iocs, evCount
        );
        incidentDAO.updateSeverityAndRisk(incidentId, inc.getSeverity(), risk.getScore(), risk.getCalculatedPriority());

        // 3. Timeline event & Audit log
        String histMsg = "Added IOC [" + type + "]: " + cleanValue;
        if (match != null) {
            histMsg += " ⚠ THREAT INTEL MATCH: " + match.getClassification() + " (" + match.getThreatLevel() + ") from " + match.getSource();
            
            // Broadcast high-priority alert
            notificationDAO.broadcastToRole(Role.ANALYST.getId(), incidentId,
                "🚨 THREAT INTEL MATCH in " + inc.getIncidentCode() + ": IOC " + cleanValue + " (" + match.getClassification() + ")",
                "CRITICAL"
            );
        }

        investigationDAO.addHistory(new IncidentHistory(
            incidentId, session.getUserId(), session.getUsername(), "ADD_IOC", histMsg
        ));

        auditDAO.log(new AuditLog(
            session.getUserId(), session.getUsername(), "ADD_IOC", "INDICATOR", cleanValue,
            "Added IOC to " + inc.getIncidentCode() + (match != null ? " [MATCHED INTEL]" : "")
        ));

        return new AddIndicatorResult(ind, match);
    }

    public List<Indicator> getIndicators(long incidentId) throws SQLException {
        return indicatorDAO.getByIncident(incidentId);
    }

    public void removeIndicator(long indicatorId, long incidentId, UserSession session) throws Exception {
        RbacService.requireRole(session, Role.ANALYST, Role.SUPERMOD);
        indicatorDAO.delete(indicatorId);
        auditDAO.log(new AuditLog(
            session.getUserId(), session.getUsername(), "DELETE_IOC", "INDICATOR", String.valueOf(indicatorId),
            "Removed IOC from incident ID " + incidentId
        ));
    }
}
