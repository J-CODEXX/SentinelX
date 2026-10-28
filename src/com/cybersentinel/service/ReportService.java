package com.cybersentinel.service;

import com.cybersentinel.config.AppConfig;
import com.cybersentinel.dao.*;
import com.cybersentinel.model.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReportService {
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final InvestigationDAO investigationDAO = new InvestigationDAO();
    private final EvidenceDAO evidenceDAO = new EvidenceDAO();
    private final IndicatorDAO indicatorDAO = new IndicatorDAO();
    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();
    private final UserDAO userDAO = new UserDAO();

    public String generateIncidentDossier(long incidentId) throws Exception {
        Incident inc = incidentDAO.findById(incidentId);
        if (inc == null) throw new IllegalArgumentException("Incident not found.");

        List<InvestigationNote> notes = investigationDAO.getNotesByIncident(incidentId);
        List<IncidentHistory> history = investigationDAO.getHistoryByIncident(incidentId);
        List<Evidence> evidenceList = evidenceDAO.getByIncident(incidentId);
        List<Indicator> indicators = indicatorDAO.getByIncident(incidentId);

        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("                    CYBERSENTINEL SOC INCIDENT DOSSIER\n");
        sb.append("================================================================================\n\n");
        sb.append(String.format("Incident Code    : %s\n", inc.getIncidentCode()));
        sb.append(String.format("Title            : %s\n", inc.getTitle()));
        sb.append(String.format("Classification   : %s\n", inc.getType()));
        sb.append(String.format("Current Status   : %s\n", inc.getStatus().getDisplayName()));
        sb.append(String.format("Severity Level   : %s\n", inc.getSeverity().name()));
        sb.append(String.format("Risk Score       : %d / 100 (%s)\n", inc.getRiskScore(), inc.getPriority().getLabel()));
        sb.append(String.format("Affected System  : %s\n", inc.getAffectedSystem()));
        sb.append(String.format("Reported By      : %s\n", inc.getReportedByName()));
        sb.append(String.format("Assigned Analyst : %s\n", inc.getAssignedToName()));
        sb.append(String.format("Observed At      : %s\n", inc.getObservedAt()));
        sb.append(String.format("Created At       : %s\n", inc.getCreatedAt()));
        sb.append(String.format("Simulated Record : %s\n\n", inc.isSimulated() ? "YES (SIMULATED)" : "NO (REAL INCIDENT)"));

        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("INCIDENT DESCRIPTION\n");
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(inc.getDescription()).append("\n\n");

        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("RESOLUTION & REMEDIATION SUMMARY\n");
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("Root Cause      : ").append(inc.getRootCause() != null ? inc.getRootCause() : "Pending investigation").append("\n");
        sb.append("Actions Taken   : ").append(inc.getActionsTaken() != null ? inc.getActionsTaken() : "None recorded").append("\n");
        sb.append("User Resolution : ").append(inc.getResolutionSummary() != null ? inc.getResolutionSummary() : "Pending closure").append("\n");
        sb.append("Recommendations : ").append(inc.getRecommendations() != null ? inc.getRecommendations() : "None").append("\n\n");

        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("INDICATORS OF COMPROMISE (IOCs) [Count: ").append(indicators.size()).append("]\n");
        sb.append("--------------------------------------------------------------------------------\n");
        if (indicators.isEmpty()) {
            sb.append("No IOCs recorded for this incident.\n\n");
        } else {
            for (Indicator i : indicators) {
                sb.append(String.format("- [%s] %s | Class: %s | Threat: %s %s\n",
                    i.getType(), i.getValue(), i.getClassification(), i.getThreatLevel(),
                    i.isThreatIntelMatch() ? "(⚠ THREAT INTEL MATCH)" : ""));
            }
            sb.append("\n");
        }

        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("EVIDENCE ARTIFACTS [Count: ").append(evidenceList.size()).append("]\n");
        sb.append("--------------------------------------------------------------------------------\n");
        if (evidenceList.isEmpty()) {
            sb.append("No forensic evidence files attached.\n\n");
        } else {
            for (Evidence ev : evidenceList) {
                sb.append(String.format("- %s (%s) | SHA-256: %s | By: %s\n",
                    ev.getOriginalFilename(), ev.getFormattedSize(), ev.getSha256(), ev.getUploadedByName()));
            }
            sb.append("\n");
        }

        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("INTERNAL INVESTIGATION NOTES [Count: ").append(notes.size()).append("]\n");
        sb.append("--------------------------------------------------------------------------------\n");
        if (notes.isEmpty()) {
            sb.append("No internal investigation notes logged.\n\n");
        } else {
            for (InvestigationNote n : notes) {
                sb.append(String.format("[%s] %s:\n%s\n\n", n.getCreatedAt(), n.getAnalystName(), n.getNote()));
            }
        }

        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("INCIDENT TIMELINE EVENTS [Count: ").append(history.size()).append("]\n");
        sb.append("--------------------------------------------------------------------------------\n");
        for (IncidentHistory h : history) {
            sb.append(String.format("[%s] (%s) %s: %s\n", h.getCreatedAt(), h.getActorName(), h.getEvent(), h.getDescription()));
        }

        sb.append("\n================================================================================\n");
        sb.append("Generated by CyberSentinel SOC Platform on ").append(LocalDateTime.now()).append("\n");
        sb.append("================================================================================\n");

        return sb.toString();
    }

    public String generateSecuritySummary() throws SQLException {
        DashboardStats stats = analyticsDAO.getDashboardStats(null);

        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("                  CYBERSENTINEL EXECUTIVE SECURITY SUMMARY\n");
        sb.append("================================================================================\n\n");
        sb.append(String.format("Report Generated : %s\n\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

        sb.append("KEY PERFORMANCE INDICATORS (KPIs):\n");
        sb.append(String.format("- Total Incidents Recorded : %d\n", stats.getTotalIncidents()));
        sb.append(String.format("- Active Open Incidents    : %d\n", stats.getOpenIncidents()));
        sb.append(String.format("- Actively Investigating   : %d\n", stats.getInvestigatingIncidents()));
        sb.append(String.format("- Critical Incidents       : %d\n", stats.getCriticalIncidents()));
        sb.append(String.format("- High Severity Incidents  : %d\n", stats.getHighIncidents()));
        sb.append(String.format("- Resolved Incidents       : %d\n", stats.getResolvedIncidents()));
        sb.append(String.format("- Unassigned Incidents     : %d\n", stats.getUnassignedIncidents()));
        sb.append(String.format("- Escalated Incidents      : %d\n", stats.getEscalatedIncidents()));
        sb.append(String.format("- Active SOC Analysts      : %d\n\n", stats.getActiveAnalysts()));

        sb.append("SLA COMPLIANCE PERFORMANCE:\n");
        sb.append(String.format("- Within SLA Target        : %d\n", stats.getWithinSlaCount()));
        sb.append(String.format("- Approaching SLA Warning  : %d\n", stats.getApproachingSlaCount()));
        sb.append(String.format("- SLA Breaches             : %d\n\n", stats.getBreachedSlaCount()));

        sb.append("INCIDENTS BY CATEGORY:\n");
        for (Map.Entry<String, Integer> e : stats.getCategoryDistribution().entrySet()) {
            sb.append(String.format("  • %-25s : %d\n", e.getKey(), e.getValue()));
        }
        sb.append("\n");

        sb.append("ANALYST ACTIVE WORKLOAD:\n");
        for (Map.Entry<String, Integer> e : stats.getAnalystWorkload().entrySet()) {
            sb.append(String.format("  • %-25s : %d active cases\n", e.getKey(), e.getValue()));
        }
        sb.append("\n================================================================================\n");

        return sb.toString();
    }

    public File exportToFile(String content, String filenamePrefix, String extension) throws Exception {
        Path reportsDir = Paths.get(AppConfig.REPORTS_DIR);
        if (!Files.exists(reportsDir)) {
            Files.createDirectories(reportsDir);
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File file = reportsDir.resolve(filenamePrefix + "_" + timestamp + "." + extension).toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

    public File exportIncidentsCsv() throws Exception {
        List<Incident> list = incidentDAO.findAll(null, null, null, null, null, null, null, null);
        StringBuilder sb = new StringBuilder();
        sb.append("IncidentCode,Title,Type,Severity,RiskScore,Priority,Status,Reporter,Analyst,AffectedSystem,Simulated,CreatedAt\n");

        for (Incident inc : list) {
            sb.append(escapeCsv(inc.getIncidentCode())).append(",")
              .append(escapeCsv(inc.getTitle())).append(",")
              .append(escapeCsv(inc.getType())).append(",")
              .append(escapeCsv(inc.getSeverity().name())).append(",")
              .append(inc.getRiskScore()).append(",")
              .append(escapeCsv(inc.getPriority().name())).append(",")
              .append(escapeCsv(inc.getStatus().name())).append(",")
              .append(escapeCsv(inc.getReportedByName())).append(",")
              .append(escapeCsv(inc.getAssignedToName())).append(",")
              .append(escapeCsv(inc.getAffectedSystem())).append(",")
              .append(inc.isSimulated() ? "YES" : "NO").append(",")
              .append(inc.getCreatedAt()).append("\n");
        }

        return exportToFile(sb.toString(), "Incident_Inventory", "csv");
    }

    private String escapeCsv(String str) {
        if (str == null) return "\"\"";
        return "\"" + str.replace("\"", "\"\"") + "\"";
    }
}
