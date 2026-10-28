package com.cybersentinel.engine;

import java.util.ArrayList;
import java.util.List;

public final class RecommendationEngine {

    private RecommendationEngine() {}

    public static List<String> getRecommendations(String incidentType) {
        List<String> list = new ArrayList<>();
        String type = incidentType != null ? incidentType.toLowerCase() : "";

        if (type.contains("phishing")) {
            list.add("Inspect email headers for spoofing, SPF, DKIM, and DMARC alignment");
            list.add("Extract and sandbox any embedded URLs or domain links");
            list.add("Check whether user clicked link or submitted credentials");
            list.add("Force password reset and invalidate active SSO/OAuth tokens");
            list.add("Search mail server for identical message hashes across all inboxes");
            list.add("Block malicious sender address and domain at the email gateway");
            list.add("Enforce hardware-backed MFA on the victim's account");
        } else if (type.contains("malware")) {
            list.add("Isolate the affected workstation/server from the network immediately");
            list.add("Preserve volatile RAM image and forensic triage collection");
            list.add("Extract file hashes (MD5, SHA-256) and query threat intelligence");
            list.add("Check persistence mechanisms: Scheduled Tasks, Registry Run keys, Services");
            list.add("Review EDR telemetry for child processes and injection attempts");
            list.add("Quarantine binary artifacts and submit sample for reverse engineering");
            list.add("Perform AV/EDR full remediation sweep and patch vulnerability");
        } else if (type.contains("ransomware")) {
            list.add("CRITICAL: Disconnect infected host and isolate subnet to prevent lateral spread");
            list.add("Identify encryption extension, ransom note filename, and malware strain");
            list.add("Determine initial compromise vector (e.g., RDP, phishing, unpatched edge device)");
            list.add("Check domain controller security logs for lateral Kerberoasting or PsExec");
            list.add("Verify offline backup integrity and isolate backup storage systems");
            list.add("Engage Incident Response Commander and Legal Counsel for extortion review");
            list.add("Initiate clean rebuild or bare-metal recovery from validated clean backups");
        } else if (type.contains("account compromise") || type.contains("unauthorized access")) {
            list.add("Immediately revoke active web sessions, refresh tokens, and app passwords");
            list.add("Reset user account password using out-of-band verification");
            list.add("Inspect recent authentication logs for anomalous geolocation or VPN IPs");
            list.add("Audit Azure AD / Active Directory for newly added MFA devices or forwarding rules");
            list.add("Check for privilege escalation or unauthorized group membership changes");
            list.add("Verify recent mailbox delegates and file access audit trails");
        } else if (type.contains("data breach")) {
            list.add("Identify specific databases, file shares, or S3 buckets accessed");
            list.add("Determine exact scope of PII, financial, or proprietary records exfiltrated");
            list.add("Block and revoke exfiltration channels and compromised API keys");
            list.add("Preserve server access logs and database audit logs for legal discovery");
            list.add("Notify General Counsel and Data Privacy Officer regarding disclosure timelines");
            list.add("Prepare customer notification and regulatory breach notification dossier");
        } else if (type.contains("network attack")) {
            list.add("Analyze packet capture (PCAP) to determine attack vector (SYN flood, DNS amp, etc.)");
            list.add("Apply upstream firewall drop rules and rate limiting on source IP blocks");
            list.add("Engage DDoS mitigation / CDN scrubbing provider for traffic redirection");
            list.add("Inspect targeted services for buffer overflows or resource exhaustion");
            list.add("Audit ingress router ACLs and disable unnecessary exposed ports");
        } else {
            list.add("Collect endpoint diagnostic logs, system event logs, and network netflow");
            list.add("Interview the reporting user regarding recent observed anomalies");
            list.add("Cross-reference observed timestamps with central SIEM alerts");
            list.add("Document forensic evidence chain-of-custody in the investigation notes");
            list.add("Classify true positive vs false positive and apply security mitigations");
        }

        return list;
    }
}
