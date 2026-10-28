package com.cybersentinel.service;

import com.cybersentinel.dao.AuditDAO;
import com.cybersentinel.dao.SettingDAO;
import com.cybersentinel.model.AuditLog;
import com.cybersentinel.model.Role;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.security.RbacService;

import java.sql.SQLException;
import java.util.Map;

public class SettingService {
    private final SettingDAO settingDAO = new SettingDAO();
    private final AuditDAO auditDAO = new AuditDAO();

    public Map<String, String> getAllSettings(UserSession session) throws Exception {
        RbacService.requireRole(session, Role.SUPERMOD);
        return settingDAO.getAllSettings();
    }

    public void updateSetting(String key, String value, UserSession session) throws Exception {
        RbacService.requireRole(session, Role.SUPERMOD);
        if (key == null || key.isBlank() || value == null) {
            throw new IllegalArgumentException("Key and value are required.");
        }
        settingDAO.setSetting(key.trim(), value.trim());

        auditDAO.log(new AuditLog(
            session.getUserId(), session.getUsername(), "UPDATE_SETTING", "SYSTEM_SETTING", key.trim(),
            "Setting '" + key.trim() + "' updated to '" + value.trim() + "'"
        ));
    }
}
