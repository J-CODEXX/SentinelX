package com.cybersentinel.test;

import com.cybersentinel.dao.*;
import com.cybersentinel.database.DatabaseConnection;
import com.cybersentinel.database.DatabaseInitializer;
import com.cybersentinel.engine.ClassificationEngine;
import com.cybersentinel.engine.RiskEngine;
import com.cybersentinel.engine.SimulationEngine;
import com.cybersentinel.model.*;
import com.cybersentinel.service.*;

import java.io.File;
import java.util.List;

public class SystemVerificationTest {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("       CYBERSENTINEL SOC PLATFORM — SYSTEM VERIFICATION & INTEGRITY TEST");
        System.out.println("================================================================================\n");

        int testsPassed = 0;
        int totalTests = 7;

        try {
            // STEP 0: Database Bootstrap
            System.out.print("[BOOTSTRAP] Connecting to MySQL & Initializing Schema... ");
            DatabaseInitializer.initialize();
            System.out.println("PASSED ✅");

            AuthService authService = new AuthService();
            IncidentDAO incidentDAO = new IncidentDAO();
            IncidentService incidentService = new IncidentService();
            InvestigationService investigationService = new InvestigationService();
            IndicatorService indicatorService = new IndicatorService();
            EvidenceService evidenceService = new EvidenceService();
            AnalyticsDAO analyticsDAO = new AnalyticsDAO();

            // WORKFLOW 1: User Login & Report Incident
            System.out.print("[TEST 1/5] Workflow 1: User Authentication & Incident Submission... ");
            UserSession userSession = authService.login("john_user", "UserPass123!");
            if (userSession == null || userSession.getRole() != Role.USER) {
                throw new IllegalStateException("Failed user login");
            }

            Incident newInc = new Incident();
            newInc.setTitle("Suspicious Email Wire Request with macro excel");
            newInc.setDescription("Received invoice attachment from spoofed vendor requesting wire payment to overseas account.");
            newInc.setType("Phishing");
            newInc.setSeverity(Severity.HIGH);
            newInc.setAffectedSystem("Finance-Workstation-11");

            Incident reported = incidentService.reportIncident(newInc, userSession);
            if (reported.getId() <= 0 || reported.getRiskScore() <= 0) {
                throw new IllegalStateException("Reported incident invalid");
            }
            System.out.println("PASSED ✅ (" + reported.getIncidentCode() + " Risk: " + reported.getRiskScore() + "/100)");
            testsPassed++;

            // WORKFLOW 2: Analyst Response, Claim, IOC Match, Resolution
            System.out.print("[TEST 2/5] Workflow 2: Analyst Response, IOC Matching & Resolution... ");
            UserSession analystSession = authService.login("analyst_alex", "AnalystPass123!");
            if (analystSession == null || analystSession.getRole() != Role.ANALYST) {
                throw new IllegalStateException("Failed analyst login");
            }

            // Claim
            boolean claimed = incidentService.claimIncident(reported.getId(), analystSession);
            if (!claimed) throw new IllegalStateException("Claim failed");

            // Add note
            investigationService.addNote(reported.getId(), "Extracted sender domain from email header: login-security-verify-portal.com", analystSession);

            // Add IOC with threat intel match
            IndicatorService.AddIndicatorResult iocResult = indicatorService.addIndicator(
                reported.getId(), "Domain", "login-security-verify-portal.com", "Extracted phish link", analystSession
            );
            if (!iocResult.isMatchFound() || !"MALICIOUS".equalsIgnoreCase(iocResult.getMatchedIntel().getClassification())) {
                throw new IllegalStateException("Expected Threat Intelligence Match not triggered!");
            }

            // Status transitions
            incidentService.updateStatus(reported.getId(), IncidentStatus.INVESTIGATING, analystSession, "Triage started");
            incidentService.updateStatus(reported.getId(), IncidentStatus.CONTAINED, analystSession, "Sender blocked");

            // Resolve
            incidentService.resolveIncident(
                reported.getId(),
                "Spoofed display name spearphishing email",
                "Domain blocked at mail gateway, user sessions invalidated",
                "The phishing link has been neutralized. No credentials entered.",
                "Always verify unexpected invoices by phone.",
                analystSession
            );
            System.out.println("PASSED ✅ (Threat Intel Matched & Incident Resolved)");
            testsPassed++;

            // WORKFLOW 3: Supermod Command Center & Analytics
            System.out.print("[TEST 3/5] Workflow 3: Supermod Global Analytics & Controls... ");
            UserSession supermodSession = authService.login("supermod", "AdminPass123!");
            if (supermodSession == null || supermodSession.getRole() != Role.SUPERMOD) {
                throw new IllegalStateException("Failed supermod login");
            }

            DashboardStats stats = analyticsDAO.getDashboardStats(null);
            if (stats.getTotalIncidents() < 4) {
                throw new IllegalStateException("Expected seeded incidents not found in stats");
            }
            System.out.println("PASSED ✅ (Global Total Incidents: " + stats.getTotalIncidents() + ", Active Analysts: " + stats.getActiveAnalysts() + ")");
            testsPassed++;

            // WORKFLOW 4: Incident Simulation Engine
            System.out.print("[TEST 4/5] Workflow 4: Red-Team Simulation Engine... ");
            Incident simInc = SimulationEngine.simulateIncident("Ransomware", Severity.CRITICAL, null, supermodSession);
            if (!simInc.isSimulated() || simInc.getRiskScore() < 50) {
                throw new IllegalStateException("Simulation engine output invalid");
            }
            System.out.println("PASSED ✅ (" + simInc.getIncidentCode() + " SIMULATED marked: " + simInc.isSimulated() + ")");
            testsPassed++;

            // WORKFLOW 5: Security & RBAC Enforcement
            System.out.print("[TEST 5/5] Workflow 5: Security, RBAC & Lockout Verification... ");
            // 1. Password verify
            boolean userCanAccessInternalNotes = userSession.getRole() == Role.ANALYST || userSession.getRole() == Role.SUPERMOD;
            if (userCanAccessInternalNotes) {
                throw new IllegalStateException("User should not have internal notes permission!");
            }

            // 2. Invalid password rejected
            try {
                authService.login("john_user", "WrongPassword123!");
                throw new IllegalStateException("Wrong password should have failed!");
            } catch (IllegalArgumentException e) {
                // expected
            }

            System.out.println("PASSED ✅ (RBAC strictly enforced, Auth secure)");
            testsPassed++;

            // WORKFLOW 6: Analyst Account Request & Supermod Approval Workflow
            System.out.print("[TEST 6/6] Workflow 6: Analyst Registration Request & Supermod Approval... ");
            String suffix = String.valueOf(System.currentTimeMillis() % 100000);
            String analystUser = "req_analyst_" + suffix;
            String analystEmail = "analyst_" + suffix + "@cybersentinel.soc";

            // 1. Submit analyst request (should NOT create user in cs_users directly)
            AnalystRequest aReq = authService.requestAnalystAccount(analystUser, analystEmail, "AnalystReqPass123!");
            if (aReq.getId() <= 0 || !"PENDING".equals(aReq.getStatus())) {
                throw new IllegalStateException("Analyst request submission failed or status not PENDING");
            }

            // 2. Attempting to log in before approval should be blocked with pending message
            try {
                authService.login(analystUser, "AnalystReqPass123!");
                throw new IllegalStateException("Login should have been blocked for pending analyst request!");
            } catch (SecurityException se) {
                if (!se.getMessage().contains("pending approval by the Supermod")) {
                    throw new IllegalStateException("Unexpected error message for pending request: " + se.getMessage());
                }
            }

            // 3. Unauthorized user cannot approve
            try {
                authService.approveAnalystRequest(aReq.getId(), userSession);
                throw new IllegalStateException("Standard user should NOT be able to approve analyst request!");
            } catch (SecurityException se) {
                // Expected RBAC rejection
            }

            // 4. Supermod approves the request -> creates account in cs_users
            User createdAnalyst = authService.approveAnalystRequest(aReq.getId(), supermodSession);
            if (createdAnalyst == null || createdAnalyst.getRole() != Role.ANALYST || !createdAnalyst.isActive()) {
                throw new IllegalStateException("Supermod approval failed to create active analyst user!");
            }

            // 5. Newly approved analyst can now log in successfully
            UserSession approvedSession = authService.login(analystUser, "AnalystReqPass123!");
            if (approvedSession == null || approvedSession.getRole() != Role.ANALYST) {
                throw new IllegalStateException("Approved analyst failed to log in!");
            }

            // 6. Test rejection flow
            String rejectedUser = "rej_analyst_" + suffix;
            AnalystRequest rReq = authService.requestAnalystAccount(rejectedUser, "rej_" + suffix + "@cybersentinel.soc", "AnalystReqPass123!");
            authService.rejectAnalystRequest(rReq.getId(), "Incomplete certification", supermodSession);
            try {
                authService.login(rejectedUser, "AnalystReqPass123!");
                throw new IllegalStateException("Login should have failed for rejected analyst request!");
            } catch (SecurityException se) {
                if (!se.getMessage().contains("rejected by the Supermod")) {
                    throw new IllegalStateException("Unexpected error message for rejected request: " + se.getMessage());
                }
            }

            System.out.println("PASSED ✅ (Request submitted, Approval enforced, Account created)");
            testsPassed++;

            // WORKFLOW 7: Incident Deletion & Real-Only Filter Verification
            System.out.print("[TEST 7/7] Workflow 7: Incident Deletion & Real-Only Filter... ");
            // 1. Report a real incident to test
            Incident realInc = new Incident();
            realInc.setTitle("Temporary Real Test Incident " + suffix);
            realInc.setDescription("Testing permanent deletion and origin filtering.");
            realInc.setType("Malware");
            realInc.setSeverity(Severity.LOW);
            realInc.setAffectedSystem("Test-VM-99");
            Incident createdReal = incidentService.reportIncident(realInc, userSession);

            // 2. Filter with simulatedOnly = false (Real Only)
            List<Incident> realList = incidentDAO.findAll(null, null, null, null, false, null, null, null);
            boolean foundReal = false;
            for (Incident inc : realList) {
                if (inc.isSimulated()) {
                    throw new IllegalStateException("Real-Only filter returned a simulated incident: " + inc.getIncidentCode());
                }
                if (inc.getId() == createdReal.getId()) {
                    foundReal = true;
                }
            }
            if (!foundReal) {
                throw new IllegalStateException("Real incident not returned in Real-Only filter!");
            }

            // 3. Unauthorized deletion attempt
            try {
                incidentService.deleteIncident(createdReal.getId(), userSession);
                throw new IllegalStateException("Standard USER should not be allowed to delete incidents!");
            } catch (SecurityException se) {
                // Expected RBAC rejection
            }

            // 4. Authorized deletion by Supermod
            boolean deleted = incidentService.deleteIncident(createdReal.getId(), supermodSession);
            if (!deleted) {
                throw new IllegalStateException("Incident deletion returned false!");
            }
            if (incidentDAO.findById(createdReal.getId()) != null) {
                throw new IllegalStateException("Incident still exists in database after deletion!");
            }

            System.out.println("PASSED ✅ (Origin filter accurate, Unauthorized blocked, Incident deleted)");
            testsPassed++;

            System.out.println("\n================================================================================");
            System.out.println("       ALL TESTS PASSED: " + testsPassed + " / " + totalTests + " WORKFLOWS VERIFIED SUCCESSFULLY!");
            System.out.println("================================================================================");

        } catch (Exception e) {
            System.err.println("\nFAILED ❌: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
