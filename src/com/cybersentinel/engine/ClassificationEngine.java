package com.cybersentinel.engine;

import com.cybersentinel.model.Severity;

import java.util.*;

public final class ClassificationEngine {

    private ClassificationEngine() {}

    public static class ClassificationResult {
        private final String suggestedCategory;
        private final Severity suggestedSeverity;
        private final String reason;
        private final int confidence; // 0 to 100

        public ClassificationResult(String suggestedCategory, Severity suggestedSeverity, String reason, int confidence) {
            this.suggestedCategory = suggestedCategory;
            this.suggestedSeverity = suggestedSeverity;
            this.reason = reason;
            this.confidence = Math.max(10, Math.min(100, confidence));
        }

        public String getSuggestedCategory() { return suggestedCategory; }
        public Severity getSuggestedSeverity() { return suggestedSeverity; }
        public String getReason() { return reason; }
        public int getConfidence() { return confidence; }
    }

    private static final Map<String, List<String>> KEYWORDS = new LinkedHashMap<>();

    static {
        KEYWORDS.put("Ransomware", List.of(
            "ransom", "encrypt", "bitcoin", "files locked", "decrypt", "shadow copy", 
            "readme.txt", "vssadmin", "extortion", "locked documents", ".locked"
        ));
        KEYWORDS.put("Data Breach", List.of(
            "exfiltration", "leak", "database dump", "pii", "credit card", "ssn", 
            "customer data", "stolen records", "pastebin", "unauthorized export"
        ));
        KEYWORDS.put("Account Compromise", List.of(
            "password", "credential", "login", "account", "mfa bypass", "unauthorized login", 
            "brute force", "hijacked", "session token", "failed logins", "reset link"
        ));
        KEYWORDS.put("Phishing", List.of(
            "email", "verify", "login link", "click", "attachment", "invoice", "spoof", 
            "wire transfer", "ceo fraud", "urgent action", "suspicious sender"
        ));
        KEYWORDS.put("Malware", List.of(
            "virus", "trojan", "executable", ".exe", "payload", "backdoor", "miner", 
            "c2", "command and control", "dropper", "persistence", "dll injection"
        ));
        KEYWORDS.put("Network Attack", List.of(
            "ddos", "syn flood", "port scan", "lateral movement", "packet flood", 
            "beaconing", "firewall alert", "arp poison", "dns tunnel"
        ));
        KEYWORDS.put("Unauthorized Access", List.of(
            "privilege escalation", "sudo abuse", "unauthorized entry", "forbidden directory", 
            "root access", "admin rights", "bypassed acl"
        ));
        KEYWORDS.put("Insider Threat", List.of(
            "disgruntled", "usb copy", "after hours download", "mass copy", 
            "confidential folder", "employee exfil"
        ));
    }

    public static ClassificationResult classify(String title, String description) {
        String combined = ((title != null ? title : "") + " " + (description != null ? description : "")).toLowerCase();
        
        String bestCategory = "Suspicious Activity";
        int maxMatches = 0;
        List<String> matchedTokens = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : KEYWORDS.entrySet()) {
            int count = 0;
            List<String> tokens = new ArrayList<>();
            for (String kw : entry.getValue()) {
                if (combined.contains(kw)) {
                    count++;
                    tokens.add(kw);
                }
            }
            if (count > maxMatches) {
                maxMatches = count;
                bestCategory = entry.getKey();
                matchedTokens = tokens;
            }
        }

        Severity severity;
        int confidence;

        if (maxMatches >= 3) {
            confidence = Math.min(95, 70 + (maxMatches * 8));
        } else if (maxMatches >= 1) {
            confidence = 50 + (maxMatches * 15);
        } else {
            confidence = 35;
            bestCategory = "Suspicious Activity";
            matchedTokens.add("general anomaly");
        }

        switch (bestCategory) {
            case "Ransomware" -> severity = Severity.CRITICAL;
            case "Data Breach" -> severity = Severity.CRITICAL;
            case "Malware" -> severity = maxMatches >= 2 ? Severity.HIGH : Severity.MEDIUM;
            case "Account Compromise" -> severity = Severity.HIGH;
            case "Network Attack" -> severity = Severity.HIGH;
            case "Unauthorized Access" -> severity = Severity.HIGH;
            case "Phishing" -> severity = Severity.MEDIUM;
            default -> severity = Severity.LOW;
        }

        String reason = "Matched security signals: " + String.join(", ", matchedTokens);
        return new ClassificationResult(bestCategory, severity, reason, confidence);
    }
}
