package com.cybersentinel.service;

import com.cybersentinel.dao.AuditDAO;
import com.cybersentinel.dao.IncidentDAO;
import com.cybersentinel.dao.InvestigationDAO;
import com.cybersentinel.model.*;
import com.cybersentinel.security.RbacService;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class InvestigationService {
    private final InvestigationDAO investigationDAO = new InvestigationDAO();
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final AuditDAO auditDAO = new AuditDAO();

    public void addNote(long incidentId, String noteText, UserSession session) throws Exception {
        RbacService.checkAuthenticated(session);
        if (!RbacService.canViewInternalNotes(session)) {
            throw new SecurityException("Only Security Analysts and Supermods can create internal investigation notes.");
        }
        if (noteText == null || noteText.trim().isBlank()) {
            throw new IllegalArgumentException("Note content cannot be empty.");
        }

        InvestigationNote note = new InvestigationNote(incidentId, session.getUserId(), session.getUsername(), noteText.trim());
        investigationDAO.addNote(note);

        Incident inc = incidentDAO.findById(incidentId);
        String incCode = inc != null ? inc.getIncidentCode() : String.valueOf(incidentId);

        auditDAO.log(new AuditLog(
            session.getUserId(), session.getUsername(), "ADD_INVESTIGATION_NOTE", "INCIDENT", incCode,
            "Added internal investigation note."
        ));
    }

    public List<InvestigationNote> getNotes(long incidentId, UserSession session) throws Exception {
        RbacService.checkAuthenticated(session);
        // Sensitive internal notes are hidden from normal end users!
        if (!RbacService.canViewInternalNotes(session)) {
            return Collections.emptyList();
        }
        return investigationDAO.getNotesByIncident(incidentId);
    }

    public List<IncidentHistory> getHistory(long incidentId, UserSession session) throws Exception {
        RbacService.checkAuthenticated(session);
        Incident inc = incidentDAO.findById(incidentId);
        if (inc == null) throw new IllegalArgumentException("Incident not found.");
        if (!RbacService.canViewIncident(session, inc)) {
            throw new SecurityException("Unauthorized access to incident timeline.");
        }
        return investigationDAO.getHistoryByIncident(incidentId);
    }

    public List<IncidentAction> getActions(long incidentId) throws SQLException {
        return investigationDAO.getActionsByIncident(incidentId);
    }

    public void toggleAction(long actionId, boolean completed, UserSession session) throws Exception {
        RbacService.requireRole(session, Role.ANALYST, Role.SUPERMOD);
        investigationDAO.toggleActionCompleted(actionId, completed, session.getUserId());
    }
}
