package com.cybersentinel.service;

import com.cybersentinel.config.AppConfig;
import com.cybersentinel.dao.AnalystRequestDAO;
import com.cybersentinel.dao.AuditDAO;
import com.cybersentinel.dao.NotificationDAO;
import com.cybersentinel.dao.UserDAO;
import com.cybersentinel.model.AnalystRequest;
import com.cybersentinel.model.AuditLog;
import com.cybersentinel.model.Role;
import com.cybersentinel.model.User;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.security.PasswordUtil;
import com.cybersentinel.security.RbacService;
import com.cybersentinel.security.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();
    private final AuditDAO auditDAO = new AuditDAO();
    private final AnalystRequestDAO analystRequestDAO = new AnalystRequestDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public UserSession login(String username, String password) throws Exception {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            AnalystRequest req = analystRequestDAO.findLatestByUsername(username.trim());
            if (req != null) {
                if ("PENDING".equalsIgnoreCase(req.getStatus())) {
                    throw new SecurityException("Your analyst account request is pending approval by the Supermod.");
                } else if ("REJECTED".equalsIgnoreCase(req.getStatus())) {
                    String reasonMsg = req.getRejectionReason() != null && !req.getRejectionReason().isBlank()
                            ? " Reason: " + req.getRejectionReason() : "";
                    throw new SecurityException("Your analyst registration request was rejected by the Supermod." + reasonMsg);
                }
            }
            userDAO.recordLoginAttempt(username, false);
            auditDAO.log(new AuditLog(null, username, "FAILED_LOGIN", "USER", null, "Login attempt with non-existent username"));
            throw new IllegalArgumentException("Invalid username or password.");
        }

        if (!user.isActive()) {
            userDAO.recordLoginAttempt(username, false);
            auditDAO.log(new AuditLog(user.getId(), username, "INACTIVE_LOGIN_BLOCKED", "USER", String.valueOf(user.getId()), "Attempted login to deactivated account"));
            throw new SecurityException("This account has been deactivated. Please contact your SOC administrator.");
        }

        if (user.isLocked()) {
            userDAO.recordLoginAttempt(username, false);
            auditDAO.log(new AuditLog(user.getId(), username, "LOCKED_LOGIN_BLOCKED", "USER", String.valueOf(user.getId()), "Login attempted while account locked"));
            throw new SecurityException("Account is temporarily locked due to repeated failed login attempts. Try again later.");
        }

        boolean match = PasswordUtil.verifyPassword(password, user.getPasswordHash(), user.getPasswordSalt());
        if (!match) {
            userDAO.recordLoginAttempt(username, false);
            userDAO.incrementFailedAttempts(username);
            int newCount = user.getFailedAttempts() + 1;

            if (newCount >= AppConfig.MAX_FAILED_LOGINS) {
                userDAO.lockUser(username, AppConfig.LOCKOUT_MINUTES);
                auditDAO.log(new AuditLog(user.getId(), username, "ACCOUNT_LOCKED", "USER", String.valueOf(user.getId()), 
                    "Account locked for " + AppConfig.LOCKOUT_MINUTES + " minutes after " + newCount + " failed attempts"));
                throw new SecurityException("Too many failed attempts. Account has been locked for " + AppConfig.LOCKOUT_MINUTES + " minutes.");
            }

            auditDAO.log(new AuditLog(user.getId(), username, "FAILED_LOGIN", "USER", String.valueOf(user.getId()), 
                "Incorrect password. Failed attempt count: " + newCount));
            throw new IllegalArgumentException("Invalid username or password.");
        }

        // Login Succeeded
        userDAO.recordLoginAttempt(username, true);
        userDAO.resetFailedAttempts(username);

        UserSession session = new UserSession(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
        SessionManager.setCurrentSession(session);

        auditDAO.log(new AuditLog(user.getId(), user.getUsername(), "USER_LOGIN", "SESSION", String.valueOf(session.getUserId()), 
            "Successful login as " + user.getRole().name()));

        return session;
    }

    public User registerUser(String username, String email, String password, Role role) throws Exception {
        if (username == null || username.trim().length() < 3 || username.trim().length() > 40) {
            throw new IllegalArgumentException("Username must be between 3 and 40 characters.");
        }
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }
        if (!PasswordUtil.isValidPolicy(password)) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain both letters and digits.");
        }

        if (userDAO.findByUsername(username.trim()) != null) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(hash);
        user.setPasswordSalt(salt);
        user.setRole(role != null ? role : Role.USER);
        user.setActive(true);

        long id = userDAO.createUser(user);
        user.setId(id);

        auditDAO.log(new AuditLog(id, username, "USER_REGISTERED", "USER", String.valueOf(id), 
            "New account created with role: " + user.getRole().name()));

        return user;
    }

    public void changePassword(long userId, String currentPassword, String newPassword) throws Exception {
        User user = userDAO.findById(userId);
        if (user == null) throw new IllegalArgumentException("User not found.");

        if (!PasswordUtil.verifyPassword(currentPassword, user.getPasswordHash(), user.getPasswordSalt())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (!PasswordUtil.isValidPolicy(newPassword)) {
            throw new IllegalArgumentException("New password must be at least 8 characters and contain letters and numbers.");
        }

        String newSalt = PasswordUtil.generateSalt();
        String newHash = PasswordUtil.hashPassword(newPassword, newSalt);
        userDAO.updatePassword(userId, newHash, newSalt);

        auditDAO.log(new AuditLog(userId, user.getUsername(), "PASSWORD_CHANGED", "USER", String.valueOf(userId), "User changed password successfully"));
    }

    public void adminResetPassword(long adminId, String adminUsername, long targetUserId, String newPassword) throws Exception {
        User target = userDAO.findById(targetUserId);
        if (target == null) throw new IllegalArgumentException("Target user not found.");
        if (!PasswordUtil.isValidPolicy(newPassword)) {
            throw new IllegalArgumentException("Password does not meet complexity requirements.");
        }
        String newSalt = PasswordUtil.generateSalt();
        String newHash = PasswordUtil.hashPassword(newPassword, newSalt);
        userDAO.updatePassword(targetUserId, newHash, newSalt);

        auditDAO.log(new AuditLog(adminId, adminUsername, "ADMIN_PASSWORD_RESET", "USER", String.valueOf(targetUserId), "Administrator reset password for user: " + target.getUsername()));
    }

    public void setUserActive(long adminId, String adminUsername, long targetUserId, boolean active) throws Exception {
        User target = userDAO.findById(targetUserId);
        if (target == null) throw new IllegalArgumentException("Target user not found.");
        userDAO.setActiveStatus(targetUserId, active);

        auditDAO.log(new AuditLog(adminId, adminUsername, active ? "USER_ACTIVATED" : "USER_DEACTIVATED", "USER", String.valueOf(targetUserId), "Account status changed to: " + (active ? "ACTIVE" : "DEACTIVATED")));
    }

    public AnalystRequest requestAnalystAccount(String username, String email, String password) throws Exception {
        if (username == null || username.trim().length() < 3 || username.trim().length() > 40) {
            throw new IllegalArgumentException("Username must be between 3 and 40 characters.");
        }
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }
        if (!PasswordUtil.isValidPolicy(password)) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain both letters and digits.");
        }

        if (userDAO.findByUsername(username.trim()) != null) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }

        if (analystRequestDAO.hasPendingRequest(username.trim())) {
            throw new IllegalArgumentException("An analyst registration request for '" + username + "' is already pending Supermod review.");
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        AnalystRequest req = new AnalystRequest();
        req.setUsername(username.trim());
        req.setEmail(email.trim().toLowerCase());
        req.setPasswordHash(hash);
        req.setPasswordSalt(salt);
        req.setStatus("PENDING");

        long id = analystRequestDAO.createRequest(req);
        req.setId(id);

        auditDAO.log(new AuditLog(null, username.trim(), "ANALYST_REQUEST_SUBMITTED", "USER_REQUEST", String.valueOf(id),
            "New analyst registration request submitted for: " + username.trim()));

        // Broadcast alert notification to all active Supermods
        notificationDAO.broadcastToRole(3, null, "New analyst account registration request submitted for '" + username.trim() + "'. Awaiting Supermod approval.", "INFO");

        return req;
    }

    public User approveAnalystRequest(long requestId, UserSession session) throws Exception {
        RbacService.requireRole(session, Role.SUPERMOD);

        AnalystRequest req = analystRequestDAO.findById(requestId);
        if (req == null) {
            throw new IllegalArgumentException("Request #" + requestId + " not found.");
        }
        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new IllegalStateException("Request #" + requestId + " is not pending approval (status: " + req.getStatus() + ").");
        }

        if (userDAO.findByUsername(req.getUsername()) != null) {
            throw new IllegalArgumentException("Username '" + req.getUsername() + "' is already taken by an existing account.");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPasswordHash(req.getPasswordHash());
        user.setPasswordSalt(req.getPasswordSalt());
        user.setRole(Role.ANALYST);
        user.setActive(true);

        long id = userDAO.createUser(user);
        user.setId(id);

        analystRequestDAO.updateStatus(requestId, "APPROVED", session.getUserId(), null);

        auditDAO.log(new AuditLog(session.getUserId(), session.getUsername(), "ANALYST_REQUEST_APPROVED", "USER", String.valueOf(id),
            "Supermod approved analyst account for " + user.getUsername()));

        return user;
    }

    public void rejectAnalystRequest(long requestId, String reason, UserSession session) throws Exception {
        RbacService.requireRole(session, Role.SUPERMOD);

        AnalystRequest req = analystRequestDAO.findById(requestId);
        if (req == null) {
            throw new IllegalArgumentException("Request #" + requestId + " not found.");
        }
        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new IllegalStateException("Request #" + requestId + " is not pending approval.");
        }

        String actualReason = (reason != null && !reason.isBlank()) ? reason.trim() : "Declined by SOC Administrator";
        analystRequestDAO.updateStatus(requestId, "REJECTED", session.getUserId(), actualReason);

        auditDAO.log(new AuditLog(session.getUserId(), session.getUsername(), "ANALYST_REQUEST_REJECTED", "USER_REQUEST", String.valueOf(requestId),
            "Supermod rejected analyst registration request for " + req.getUsername() + ". Reason: " + actualReason));
    }

    public int getPendingAnalystRequestsCount() {
        try {
            return analystRequestDAO.countPending();
        } catch (SQLException e) {
            System.err.println("[WARN] Error counting pending analyst requests: " + e.getMessage());
            return 0;
        }
    }

    public List<AnalystRequest> getAnalystRequests(String statusFilter) throws SQLException {
        return analystRequestDAO.findAll(statusFilter);
    }
}
