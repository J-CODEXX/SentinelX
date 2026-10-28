package com.cybersentinel.service;

import com.cybersentinel.dao.NotificationDAO;
import com.cybersentinel.model.Notification;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.security.RbacService;

import java.sql.SQLException;
import java.util.List;

public class NotificationService {
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public List<Notification> getMyNotifications(UserSession session, boolean onlyUnread) throws SQLException {
        if (session == null) return List.of();
        return notificationDAO.getForUser(session.getUserId(), onlyUnread);
    }

    public void markAsRead(long notificationId, UserSession session) throws Exception {
        RbacService.checkAuthenticated(session);
        notificationDAO.markAsRead(notificationId);
    }

    public void markAllAsRead(UserSession session) throws Exception {
        RbacService.checkAuthenticated(session);
        notificationDAO.markAllRead(session.getUserId());
    }

    public void sendNotification(Long recipientId, Long incidentId, String message, String type) {
        try {
            notificationDAO.createNotification(new Notification(recipientId, incidentId, message, type));
        } catch (SQLException e) {
            System.err.println("[WARN] Failed to send notification: " + e.getMessage());
        }
    }
}
