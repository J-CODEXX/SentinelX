package com.cybersentinel.security;

import com.cybersentinel.model.UserSession;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class SessionManager {
    private static UserSession currentSession = null;
    private static final List<Consumer<UserSession>> sessionListeners = new ArrayList<>();

    private SessionManager() {}

    public static synchronized UserSession getCurrentSession() {
        return currentSession;
    }

    public static synchronized void setCurrentSession(UserSession session) {
        currentSession = session;
        for (Consumer<UserSession> listener : new ArrayList<>(sessionListeners)) {
            try {
                listener.accept(session);
            } catch (Exception ignored) {}
        }
    }

    public static synchronized boolean isLoggedIn() {
        return currentSession != null;
    }

    public static synchronized void logout() {
        setCurrentSession(null);
    }

    public static synchronized void addSessionListener(Consumer<UserSession> listener) {
        if (listener != null) {
            sessionListeners.add(listener);
        }
    }

    public static synchronized void removeSessionListener(Consumer<UserSession> listener) {
        sessionListeners.remove(listener);
    }
}
