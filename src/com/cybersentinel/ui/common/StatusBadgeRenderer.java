package com.cybersentinel.ui.common;

import com.cybersentinel.model.IncidentStatus;
import com.cybersentinel.model.Priority;
import com.cybersentinel.model.Role;
import com.cybersentinel.model.Severity;
import com.cybersentinel.model.SlaStatus;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class StatusBadgeRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        if (value == null) {
            Component c = super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
            applyRowBackground(c, isSelected, row);
            return c;
        }

        String text = value.toString().trim();

        // 1. Severity Badge
        for (Severity s : Severity.values()) {
            if (s.name().equalsIgnoreCase(text)) {
                Color fg = getSeverityFg(s);
                Color bg = getSeverityBg(s);
                return createBadgeLabel(text, fg, bg, isSelected, row);
            }
        }

        // 2. Incident Status Badge
        for (IncidentStatus st : IncidentStatus.values()) {
            if (st.name().equalsIgnoreCase(text) || st.getDisplayName().equalsIgnoreCase(text)) {
                Color fg = getStatusFg(st);
                Color bg = getStatusBg(st);
                return createBadgeLabel(st.getDisplayName(), fg, bg, isSelected, row);
            }
        }

        // 3. Priority Badge
        for (Priority p : Priority.values()) {
            if (p.name().equalsIgnoreCase(text) || p.getLabel().startsWith(text)) {
                Color c = p.getColor();
                Color fg = Theme.isDarkMode() ? c : c.darker();
                Color bg = Theme.isDarkMode()
                    ? new Color(c.getRed(), c.getGreen(), c.getBlue(), 40)
                    : new Color(c.getRed(), c.getGreen(), c.getBlue(), 35);
                return createBadgeLabel(p.name(), fg, bg, isSelected, row);
            }
        }

        // 4. SLA Status Badge
        for (SlaStatus sla : SlaStatus.values()) {
            if (sla.name().equalsIgnoreCase(text) || sla.getDisplayName().equalsIgnoreCase(text)) {
                Color fg = Theme.isDarkMode() ? sla.getForegroundColor() : sla.getForegroundColor().darker();
                Color bg = Theme.isDarkMode() ? sla.getBackgroundColor() : new Color(sla.getForegroundColor().getRed(), sla.getForegroundColor().getGreen(), sla.getForegroundColor().getBlue(), 35);
                return createBadgeLabel(sla.getDisplayName(), fg, bg, isSelected, row);
            }
        }

        // 5. User Roles
        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(text)) {
                Color base = r == Role.SUPERMOD ? Theme.COLOR_DANGER : (r == Role.ANALYST ? Theme.COLOR_PURPLE : Theme.COLOR_SIMULATED);
                Color fg = Theme.isDarkMode() ? base : base.darker();
                Color bg = Theme.isDarkMode() ? new Color(base.getRed(), base.getGreen(), base.getBlue(), 40) : new Color(base.getRed(), base.getGreen(), base.getBlue(), 30);
                return createBadgeLabel(r.name(), fg, bg, isSelected, row);
            }
        }

        // 6. Active / Locked / Approval Statuses
        if ("ACTIVE".equalsIgnoreCase(text) || "APPROVED".equalsIgnoreCase(text)) {
            Color fg = Theme.isDarkMode() ? Theme.COLOR_SUCCESS : new Color(22, 140, 50);
            Color bg = Theme.isDarkMode() ? new Color(46, 160, 67, 40) : new Color(46, 160, 67, 30);
            return createBadgeLabel(text.toUpperCase(), fg, bg, isSelected, row);
        }
        if ("PENDING".equalsIgnoreCase(text)) {
            Color fg = Theme.isDarkMode() ? Theme.COLOR_WARNING : new Color(185, 105, 0);
            Color bg = Theme.isDarkMode() ? new Color(210, 153, 34, 40) : new Color(210, 153, 34, 30);
            return createBadgeLabel("PENDING", fg, bg, isSelected, row);
        }
        if ("REJECTED".equalsIgnoreCase(text)) {
            Color fg = Theme.COLOR_DANGER;
            Color bg = Theme.isDarkMode() ? new Color(248, 81, 73, 40) : new Color(220, 38, 38, 30);
            return createBadgeLabel("REJECTED", fg, bg, isSelected, row);
        }
        if ("INACTIVE".equalsIgnoreCase(text)) {
            Color fg = Theme.TEXT_MUTED;
            Color bg = Theme.isDarkMode() ? new Color(139, 148, 158, 30) : new Color(139, 148, 158, 25);
            return createBadgeLabel("INACTIVE", fg, bg, isSelected, row);
        }
        if ("LOCKED".equalsIgnoreCase(text)) {
            Color fg = Theme.COLOR_DANGER;
            Color bg = Theme.isDarkMode() ? new Color(248, 81, 73, 40) : new Color(220, 38, 38, 30);
            return createBadgeLabel("LOCKED", fg, bg, isSelected, row);
        }
        if ("UNLOCKED".equalsIgnoreCase(text)) {
            Color fg = Theme.TEXT_MUTED;
            Color bg = Theme.isDarkMode() ? new Color(139, 148, 158, 30) : new Color(139, 148, 158, 25);
            return createBadgeLabel("UNLOCKED", fg, bg, isSelected, row);
        }

        // 7. Simulated vs Real Badges
        if ("SIMULATED".equalsIgnoreCase(text) || "YES".equalsIgnoreCase(text) || "🔵 SIMULATED".equalsIgnoreCase(text)) {
            Color fg = Theme.isDarkMode() ? Theme.COLOR_SIMULATED : new Color(2, 115, 175);
            Color bg = Theme.isDarkMode() ? new Color(88, 166, 255, 45) : new Color(88, 166, 255, 35);
            return createBadgeLabel("🔵 SIMULATED", fg, bg, isSelected, row);
        }
        if ("REAL".equalsIgnoreCase(text) || "NO".equalsIgnoreCase(text)) {
            Color fg = Theme.TEXT_MUTED;
            Color bg = Theme.isDarkMode() ? new Color(139, 148, 158, 30) : new Color(139, 148, 158, 25);
            return createBadgeLabel("REAL", fg, bg, isSelected, row);
        }

        // Fallback standard text cell
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setFont(Theme.FONT_REGULAR);
        label.setForeground(isSelected ? Theme.TEXT_LIGHT : Theme.TEXT_PRIMARY);
        applyRowBackground(label, isSelected, row);
        label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        return label;
    }

    private void applyRowBackground(Component comp, boolean isSelected, int row) {
        if (comp == null) return;
        if (isSelected) {
            comp.setBackground(Theme.isDarkMode() ? new Color(56, 139, 253, 70) : new Color(14, 116, 224, 45));
        } else {
            Color rowBg = Theme.isDarkMode()
                ? (row % 2 == 0 ? Theme.BG_CARD : Theme.BG_CARD_HOVER)
                : (row % 2 == 0 ? Theme.BG_CARD : new Color(248, 250, 253));
            comp.setBackground(rowBg);
        }
    }

    private JLabel createBadgeLabel(String text, Color fg, Color bg, boolean isSelected, int row) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 1. ALWAYS paint the full cell background first so NO black uninitialized surface shows!
                Color cellBg = isSelected
                    ? (Theme.isDarkMode() ? new Color(56, 139, 253, 70) : new Color(14, 116, 224, 45))
                    : (Theme.isDarkMode()
                        ? (row % 2 == 0 ? Theme.BG_CARD : Theme.BG_CARD_HOVER)
                        : (row % 2 == 0 ? Theme.BG_CARD : new Color(248, 250, 253)));
                g2.setColor(cellBg);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // 2. Draw the rounded pill badge in the center
                g2.setColor(bg);
                int h = 22;
                int w = Math.min(getWidth() - 12, g2.getFontMetrics().stringWidth(text) + 18);
                int x = (getWidth() - w) / 2;
                int y = (getHeight() - h) / 2;
                g2.fill(new RoundRectangle2D.Float(x, y, w, h, 10, 10));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(fg);
        badge.setOpaque(true);
        applyRowBackground(badge, isSelected, row);
        return badge;
    }

    private Color getSeverityFg(Severity s) {
        if (Theme.isDarkMode()) return s.getForegroundColor();
        switch (s) {
            case LOW: return new Color(22, 135, 45);
            case MEDIUM: return new Color(185, 105, 0);
            case HIGH: return new Color(210, 30, 30);
            case CRITICAL: return new Color(180, 10, 10);
            default: return s.getForegroundColor().darker();
        }
    }

    private Color getSeverityBg(Severity s) {
        Color base = s.getForegroundColor();
        return Theme.isDarkMode()
            ? s.getBackgroundColor()
            : new Color(base.getRed(), base.getGreen(), base.getBlue(), 35);
    }

    private Color getStatusFg(IncidentStatus st) {
        if (Theme.isDarkMode()) return st.getForegroundColor();
        return st.getForegroundColor().darker();
    }

    private Color getStatusBg(IncidentStatus st) {
        Color base = st.getForegroundColor();
        return Theme.isDarkMode()
            ? st.getBackgroundColor()
            : new Color(base.getRed(), base.getGreen(), base.getBlue(), 35);
    }
}
