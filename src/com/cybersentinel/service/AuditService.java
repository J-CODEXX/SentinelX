package com.cybersentinel.service;

import com.cybersentinel.dao.AuditDAO;
import com.cybersentinel.model.AuditLog;
import com.cybersentinel.model.Role;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.security.RbacService;

import java.sql.SQLException;
import java.util.List;

public class AuditService {
    private final AuditDAO auditDAO = new AuditDAO();

    public void log(UserSession session, String action, String entity, String entityId, String description) {
        Long actorId = session != null ? session.getUserId() : null;
        String actorName = session != null ? session.getUsername() : "SYSTEM";
        auditDAO.log(new AuditLog(actorId, actorName, action, entity, entityId, description));
    }

    public List<AuditLog> getLogs(UserSession session, String actor, String action, String entity, int limit) throws Exception {
        RbacService.requireRole(session, Role.SUPERMOD);
        return auditDAO.findAll(actor, action, entity, limit);
    }
}
