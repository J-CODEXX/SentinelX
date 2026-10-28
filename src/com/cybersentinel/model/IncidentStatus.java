package com.cybersentinel.model;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Set;

public enum IncidentStatus {
    OPEN("Open", new Color(88, 166, 255), new Color(88, 166, 255, 35)),
    TRIAGED("Triaged", new Color(121, 192, 255), new Color(121, 192, 255, 35)),
    ASSIGNED("Assigned", new Color(163, 113, 247), new Color(163, 113, 247, 35)),
    INVESTIGATING("Investigating", new Color(210, 153, 34), new Color(210, 153, 34, 35)),
    CONTAINED("Contained", new Color(219, 109, 40), new Color(219, 109, 40, 35)),
    REMEDIATED("Remediated", new Color(56, 139, 253), new Color(56, 139, 253, 35)),
    RESOLVED("Resolved", new Color(46, 160, 67), new Color(46, 160, 67, 40)),
    CLOSED("Closed", new Color(139, 148, 158), new Color(139, 148, 158, 30)),
    ESCALATED("Escalated", new Color(248, 81, 73), new Color(248, 81, 73, 45));

    private final String displayName;
    private final Color foregroundColor;
    private final Color backgroundColor;

    IncidentStatus(String displayName, Color foregroundColor, Color backgroundColor) {
        this.displayName = displayName;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getForegroundColor() {
        return foregroundColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isClosedOrResolved() {
        return this == RESOLVED || this == CLOSED;
    }

    public boolean isActive() {
        return !isClosedOrResolved();
    }

    /**
     * Checks if transitioning from this status to target status is valid.
     */
    public boolean canTransitionTo(IncidentStatus target, Role role) {
        if (target == null || target == this) return true;
        // Supermod can override and transition to any state or reopen
        if (role == Role.SUPERMOD) return true;

        switch (this) {
            case OPEN:
                return EnumSet.of(TRIAGED, ASSIGNED, INVESTIGATING, ESCALATED, CLOSED).contains(target);
            case TRIAGED:
                return EnumSet.of(ASSIGNED, INVESTIGATING, ESCALATED, CLOSED).contains(target);
            case ASSIGNED:
                return EnumSet.of(INVESTIGATING, ESCALATED, CLOSED).contains(target);
            case INVESTIGATING:
                return EnumSet.of(CONTAINED, REMEDIATED, RESOLVED, ESCALATED, CLOSED).contains(target);
            case CONTAINED:
                return EnumSet.of(REMEDIATED, RESOLVED, INVESTIGATING, ESCALATED).contains(target);
            case REMEDIATED:
                return EnumSet.of(RESOLVED, INVESTIGATING, ESCALATED).contains(target);
            case ESCALATED:
                return EnumSet.of(INVESTIGATING, ASSIGNED, CONTAINED, REMEDIATED, RESOLVED, CLOSED).contains(target);
            case RESOLVED:
                return EnumSet.of(CLOSED, INVESTIGATING).contains(target);
            case CLOSED:
                return false; // Only Supermod can reopen closed
            default:
                return false;
        }
    }

    public static IncidentStatus fromString(String val) {
        if (val == null) return OPEN;
        for (IncidentStatus s : values()) {
            if (s.name().equalsIgnoreCase(val.trim())) return s;
        }
        return OPEN;
    }
}
