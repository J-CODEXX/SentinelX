package com.cybersentinel.model;

import java.awt.Color;

public enum Priority {
    P1("P1 - Critical", new Color(255, 68, 68)),
    P2("P2 - High", new Color(248, 81, 73)),
    P3("P3 - Medium", new Color(210, 153, 34)),
    P4("P4 - Low", new Color(46, 160, 67));

    private final String label;
    private final Color color;

    Priority(String label, Color color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public Color getColor() {
        return color;
    }

    public static Priority fromRiskScore(int riskScore) {
        if (riskScore >= 75) return P1;
        if (riskScore >= 50) return P2;
        if (riskScore >= 25) return P3;
        return P4;
    }

    public static Priority fromString(String val) {
        if (val == null) return P3;
        for (Priority p : values()) {
            if (p.name().equalsIgnoreCase(val.trim())) return p;
        }
        return P3;
    }
}
