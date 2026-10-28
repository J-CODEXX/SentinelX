package com.cybersentinel.model;

import java.sql.Timestamp;

public class UserSession {
    private final long userId;
    private final String username;
    private final String email;
    private final Role role;
    private final Timestamp loginTime;

    public UserSession(long userId, String username, String email, Role role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role != null ? role : Role.USER;
        this.loginTime = new Timestamp(System.currentTimeMillis());
    }

    public long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public Timestamp getLoginTime() { return loginTime; }

    public boolean isUser() { return role == Role.USER; }
    public boolean isAnalyst() { return role == Role.ANALYST; }
    public boolean isSupermod() { return role == Role.SUPERMOD; }
    public boolean isStaff() { return role == Role.ANALYST || role == Role.SUPERMOD; }
}
