package com.cybersentinel.service;

import com.cybersentinel.config.AppConfig;
import com.cybersentinel.dao.AuditDAO;
import com.cybersentinel.dao.EvidenceDAO;
import com.cybersentinel.dao.IncidentDAO;
import com.cybersentinel.dao.InvestigationDAO;
import com.cybersentinel.model.AuditLog;
import com.cybersentinel.model.Evidence;
import com.cybersentinel.model.Incident;
import com.cybersentinel.model.IncidentHistory;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.security.RbacService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

public class EvidenceService {
    private final EvidenceDAO evidenceDAO = new EvidenceDAO();
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final InvestigationDAO investigationDAO = new InvestigationDAO();
    private final AuditDAO auditDAO = new AuditDAO();

    public Evidence uploadEvidence(long incidentId, File sourceFile, String description, UserSession session) throws Exception {
        RbacService.checkAuthenticated(session);
        Incident inc = incidentDAO.findById(incidentId);
        if (inc == null) throw new IllegalArgumentException("Incident not found.");
        if (!RbacService.canViewIncident(session, inc)) {
            throw new SecurityException("Unauthorized access to incident evidence.");
        }

        if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
            throw new IllegalArgumentException("Invalid source file selected.");
        }

        long fileSize = sourceFile.length();
        if (fileSize > AppConfig.DEFAULT_MAX_EVIDENCE_BYTES) {
            throw new IllegalArgumentException("File exceeds maximum allowed size of 20MB.");
        }

        // 1. Calculate SHA-256 Hash
        String sha256 = calculateSha256(sourceFile);

        // 2. Copy to safe Evidence Storage Directory
        Path evidenceDir = Paths.get(AppConfig.EVIDENCE_DIR);
        if (!Files.exists(evidenceDir)) {
            Files.createDirectories(evidenceDir);
        }

        String ext = "";
        int dot = sourceFile.getName().lastIndexOf('.');
        if (dot > 0) ext = sourceFile.getName().substring(dot);
        String storedFilename = "EV_" + incidentId + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
        Path targetPath = evidenceDir.resolve(storedFilename);

        Files.copy(sourceFile.toPath(), targetPath);

        // 3. Save Evidence Record
        Evidence ev = new Evidence();
        ev.setIncidentId(incidentId);
        ev.setOriginalFilename(sourceFile.getName());
        ev.setStoredPath(targetPath.toAbsolutePath().toString());
        ev.setDescription(description != null && !description.isBlank() ? description.trim() : "Forensic file attachment");
        ev.setSha256(sha256);
        ev.setFileSize(fileSize);
        ev.setUploadedBy(session.getUserId());
        ev.setUploadedByName(session.getUsername());

        long id = evidenceDAO.addEvidence(ev);
        ev.setId(id);

        // 4. Record in Timeline and Audit Log
        investigationDAO.addHistory(new IncidentHistory(
            incidentId, session.getUserId(), session.getUsername(), "EVIDENCE_UPLOAD",
            "Evidence uploaded: " + ev.getOriginalFilename() + " [SHA-256: " + sha256.substring(0, 16) + "...]"
        ));

        auditDAO.log(new AuditLog(
            session.getUserId(), session.getUsername(), "UPLOAD_EVIDENCE", "EVIDENCE", String.valueOf(id),
            "Uploaded " + ev.getOriginalFilename() + " for incident " + inc.getIncidentCode()
        ));

        return ev;
    }

    public List<Evidence> getEvidenceForIncident(long incidentId, UserSession session) throws Exception {
        RbacService.checkAuthenticated(session);
        Incident inc = incidentDAO.findById(incidentId);
        if (inc == null) throw new IllegalArgumentException("Incident not found.");
        if (!RbacService.canViewIncident(session, inc)) {
            throw new SecurityException("Unauthorized access to incident evidence.");
        }
        return evidenceDAO.getByIncident(incidentId);
    }

    public static String calculateSha256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
