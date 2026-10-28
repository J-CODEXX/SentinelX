package com.cybersentinel.model;

public enum Role {
    USER(1, "Standard User"),
    ANALYST(2, "SOC Security Analyst"),
    SUPERMOD(3, "SOC Super Moderator");

    private final int id;
    private final String displayName;

    Role(int id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromId(int id) {
        for (Role r : values()) {
            if (r.id == id) return r;
        }
        return USER;
    }

    public static Role fromString(String name) {
        if (name == null) return USER;
        for (Role r : values()) {
            if (r.name().equalsIgnoreCase(name.trim())) return r;
        }
        return USER;
    }
}
