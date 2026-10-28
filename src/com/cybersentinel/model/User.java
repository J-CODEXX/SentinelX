package com.cybersentinel.model;

import java.sql.Timestamp;

public class User {
    private long id;
    private String username;
    private String email;
    private String passwordHash;
    private String passwordSalt;
    private int roleId;
    private Role role;
    private boolean active;
    private int failedAttempts;
    private Timestamp lockedUntil;
    private Timestamp createdAt;

    public User() {}

    public User(long id, String username, String email, Role role, boolean active) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.roleId = role != null ? role.getId() : 1;
        this.active = active;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPasswordSalt() { return passwordSalt; }
    public void setPasswordSalt(String passwordSalt) { this.passwordSalt = passwordSalt; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { 
        this.roleId = roleId; 
        this.role = Role.fromId(roleId);
    }

    public Role getRole() { return role; }
    public void setRole(Role role) { 
        this.role = role;
        if (role != null) this.roleId = role.getId();
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public Timestamp getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Timestamp lockedUntil) { this.lockedUntil = lockedUntil; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isLocked() {
        if (lockedUntil == null) return false;
        return lockedUntil.after(new Timestamp(System.currentTimeMillis()));
    }

    @Override
    public String toString() {
        return username + " (" + (role != null ? role.name() : "USER") + ")";
    }
}
