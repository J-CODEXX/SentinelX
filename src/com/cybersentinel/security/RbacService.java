package com.cybersentinel.security;

import com.cybersentinel.model.Incident;
import com.cybersentinel.model.Role;
import com.cybersentinel.model.UserSession;

public final class RbacService {

    private RbacService() {}

    public static void checkAuthenticated(UserSession session) {
        if (session == null) {
            throw new SecurityException("Authentication required. Please log in.");
        }
    }

    public static void requireRole(UserSession session, Role... allowedRoles) {
        checkAuthenticated(session);
        for (Role allowed : allowedRoles) {
            if (session.getRole() == allowed) {
                return;
            }
        }
        throw new SecurityException("Access Denied: Role " + session.getRole() + " is not authorized for this operation.");
    }

    public static boolean canViewIncident(UserSession session, Incident incident) {
        if (session == null || incident == null) return false;
        if (session.getRole() == Role.SUPERMOD || session.getRole() == Role.ANALYST) {
            return true;
        }
        // Normal USER can ONLY view incidents they reported
        return incident.getReportedBy() == session.getUserId();
    }

    public static boolean canViewInternalNotes(UserSession session) {
        if (session == null) return false;
        return session.getRole() == Role.ANALYST || session.getRole() == Role.SUPERMOD;
    }

    public static boolean canClaimIncident(UserSession session) {
        if (session == null) return false;
        return session.getRole() == Role.ANALYST || session.getRole() == Role.SUPERMOD;
    }

    public static boolean canManageUsers(UserSession session) {
        if (session == null) return false;
        return session.getRole() == Role.SUPERMOD;
    }

    public static boolean canManageThreatIntel(UserSession session) {
        if (session == null) return false;
        return session.getRole() == Role.SUPERMOD;
    }

    public static boolean canSimulate(UserSession session) {
        if (session == null) return false;
        return session.getRole() == Role.ANALYST || session.getRole() == Role.SUPERMOD;
    }

    public static boolean canCloseOrReopen(UserSession session) {
        if (session == null) return false;
        return session.getRole() == Role.SUPERMOD;
    }
}
