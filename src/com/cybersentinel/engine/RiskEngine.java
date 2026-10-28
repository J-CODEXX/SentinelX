package com.cybersentinel.engine;

import com.cybersentinel.model.Indicator;
import com.cybersentinel.model.Priority;
import com.cybersentinel.model.Severity;

import java.util.List;

public final class RiskEngine {

    private RiskEngine() {}

    public static class RiskAssessment {
        private final int score;
        private final Severity calculatedSeverity;
        private final Priority calculatedPriority;
        private final String levelLabel; // LOW, MEDIUM, HIGH, CRITICAL

        public RiskAssessment(int score) {
            this.score = Math.max(0, Math.min(100, score));
            if (this.score >= 75) {
                this.calculatedSeverity = Severity.CRITICAL;
                this.calculatedPriority = Priority.P1;
                this.levelLabel = "CRITICAL";
            } else if (this.score >= 50) {
                this.calculatedSeverity = Severity.HIGH;
                this.calculatedPriority = Priority.P2;
                this.levelLabel = "HIGH";
            } else if (this.score >= 25) {
                this.calculatedSeverity = Severity.MEDIUM;
                this.calculatedPriority = Priority.P3;
                this.levelLabel = "MEDIUM";
            } else {
                this.calculatedSeverity = Severity.LOW;
                this.calculatedPriority = Priority.P4;
                this.levelLabel = "LOW";
            }
        }

        public int getScore() { return score; }
        public Severity getCalculatedSeverity() { return calculatedSeverity; }
        public Priority getCalculatedPriority() { return calculatedPriority; }
        public String getLevelLabel() { return levelLabel; }
    }

    /**
     * Calculates comprehensive risk score based on multiple SOC parameters.
     */
    public static RiskAssessment calculateRisk(
            Severity severity,
            String incidentType,
            String affectedSystem,
            List<Indicator> indicators,
            int evidenceCount
    ) {
        double score = 0.0;

        // 1. Base Severity Weight (40% weight)
        switch (severity != null ? severity : Severity.MEDIUM) {
            case CRITICAL -> score += 40.0;
            case HIGH -> score += 28.0;
            case MEDIUM -> score += 18.0;
            case LOW -> score += 8.0;
        }

        // 2. Incident Category Baseline Risk (25% weight)
        String typeLower = incidentType != null ? incidentType.toLowerCase() : "";
        if (typeLower.contains("ransomware") || typeLower.contains("data breach")) {
            score += 25.0;
        } else if (typeLower.contains("malware") || typeLower.contains("account compromise") || typeLower.contains("unauthorized access")) {
            score += 20.0;
        } else if (typeLower.contains("network attack") || typeLower.contains("phishing")) {
            score += 15.0;
        } else if (typeLower.contains("insider threat")) {
            score += 18.0;
        } else {
            score += 10.0;
        }

        // 3. Asset Criticality from System Name (20% weight)
        String sysLower = affectedSystem != null ? affectedSystem.toLowerCase() : "";
        if (sysLower.contains("prod") || sysLower.contains("dc") || sysLower.contains("domain controller") || 
            sysLower.contains("database") || sysLower.contains("core") || sysLower.contains("cloud") || sysLower.contains("server")) {
            score += 20.0;
        } else if (sysLower.contains("vpn") || sysLower.contains("gateway") || sysLower.contains("firewall") || sysLower.contains("router")) {
            score += 15.0;
        } else if (sysLower.contains("workstation") || sysLower.contains("laptop") || sysLower.contains("pc")) {
            score += 10.0;
        } else {
            score += 8.0;
        }

        // 4. Threat Indicators & Matches (10% weight)
        if (indicators != null && !indicators.isEmpty()) {
            score += Math.min(10.0, indicators.size() * 3.0);
            boolean hasMalicious = indicators.stream().anyMatch(i -> 
                "MALICIOUS".equalsIgnoreCase(i.getClassification()) || "CRITICAL".equalsIgnoreCase(i.getThreatLevel())
            );
            if (hasMalicious) {
                score += 12.0; // Bonus threat penalty
            }
        }

        // 5. Evidence Artifacts Attached (5% weight)
        if (evidenceCount > 0) {
            score += Math.min(5.0, evidenceCount * 2.0);
        }

        return new RiskAssessment((int) Math.round(score));
    }
}
