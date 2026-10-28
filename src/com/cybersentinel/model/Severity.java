package com.cybersentinel.model;

import java.awt.Color;

public enum Severity {
    LOW(15, 480, new Color(46, 160, 67), new Color(46, 160, 67, 40)),
    MEDIUM(35, 120, new Color(210, 153, 34), new Color(210, 153, 34, 40)),
    HIGH(65, 30, new Color(248, 81, 73), new Color(248, 81, 73, 40)),
    CRITICAL(90, 15, new Color(255, 68, 68), new Color(255, 68, 68, 50));

    private final int defaultRiskWeight;
    private final int defaultSlaMinutes;
    private final Color foregroundColor;
    private final Color backgroundColor;

    Severity(int defaultRiskWeight, int defaultSlaMinutes, Color foregroundColor, Color backgroundColor) {
        this.defaultRiskWeight = defaultRiskWeight;
        this.defaultSlaMinutes = defaultSlaMinutes;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
    }

    public int getDefaultRiskWeight() {
        return defaultRiskWeight;
    }

    public int getDefaultSlaMinutes() {
        return defaultSlaMinutes;
    }

    public Color getForegroundColor() {
        return foregroundColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public static Severity fromString(String val) {
        if (val == null) return MEDIUM;
        for (Severity s : values()) {
            if (s.name().equalsIgnoreCase(val.trim())) return s;
        }
        return MEDIUM;
    }
}
