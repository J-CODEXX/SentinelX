package com.cybersentinel.model;

import java.awt.Color;

public enum SlaStatus {
    WITHIN_SLA("Within SLA", new Color(46, 160, 67), new Color(46, 160, 67, 40)),
    APPROACHING_SLA("Approaching SLA", new Color(210, 153, 34), new Color(210, 153, 34, 40)),
    BREACHED_SLA("SLA Breached", new Color(248, 81, 73), new Color(248, 81, 73, 50));

    private final String displayName;
    private final Color foregroundColor;
    private final Color backgroundColor;

    SlaStatus(String displayName, Color foregroundColor, Color backgroundColor) {
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
}
