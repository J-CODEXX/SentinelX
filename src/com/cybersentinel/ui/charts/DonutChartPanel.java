package com.cybersentinel.ui.charts;

import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DonutChartPanel extends JPanel {
    private final String chartTitle;
    private final Map<String, Integer> data = new LinkedHashMap<>();
    private final Map<String, Color> colorMap = new LinkedHashMap<>();
    private final List<Consumer<String>> filterListeners = new ArrayList<>();
    private String hoveredKey = null;

    private static final Color[] DEFAULT_PALETTE = {
        new Color(248, 81, 73),   // Red
        new Color(210, 153, 34),  // Amber
        new Color(56, 139, 253),  // Blue
        new Color(46, 160, 67),   // Green
        new Color(163, 113, 247), // Purple
        new Color(121, 192, 255), // Cyan
        new Color(219, 109, 40)   // Orange
    };

    public DonutChartPanel(String chartTitle) {
        this.chartTitle = chartTitle;
        setOpaque(false);
        setPreferredSize(new Dimension(280, 240));

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // Check if hovering over legend
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (hoveredKey != null) {
                    for (Consumer<String> l : filterListeners) {
                        l.accept(hoveredKey);
                    }
                }
            }
        });
    }

    public void setData(Map<String, Integer> newData, Map<String, Color> customColors) {
        data.clear();
        colorMap.clear();
        if (newData != null) {
            data.putAll(newData);
        }
        if (customColors != null) {
            colorMap.putAll(customColors);
        } else {
            int idx = 0;
            for (String k : data.keySet()) {
                colorMap.put(k, DEFAULT_PALETTE[idx % DEFAULT_PALETTE.length]);
                idx++;
            }
        }
        repaint();
    }

    public void addFilterListener(Consumer<String> listener) {
        if (listener != null) filterListeners.add(listener);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Clip to component bounds to prevent any drawing overflow
        g.setClip(0, 0, getWidth(), getHeight());
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Title
        g2.setFont(Theme.FONT_HEADER);
        g2.setColor(Theme.TEXT_PRIMARY);
        g2.drawString(chartTitle, 16, 22);

        int total = data.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0 || data.isEmpty()) {
            g2.setFont(Theme.FONT_REGULAR);
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString("No incident data available.", getWidth() / 2 - 80, getHeight() / 2);
            g2.dispose();
            return;
        }

        // Reserve 50px at bottom for legend rows (up to 3 lines × ~15px)
        int size = Math.min(getWidth() - 32, getHeight() - 90 - 50);
        if (size <= 20) { g2.dispose(); return; }
        int x = (getWidth() - size) / 2;
        int y = 35;

        double currentAngle = 90.0;
        Shape outer = new Ellipse2D.Double(x, y, size, size);
        int cutout = (int) (size * 0.58);
        int cutX = x + (size - cutout) / 2;
        int cutY = y + (size - cutout) / 2;
        Shape inner = new Ellipse2D.Double(cutX, cutY, cutout, cutout);

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            if (entry.getValue() <= 0) continue;
            double sweep = (entry.getValue() / (double) total) * 360.0;
            Color c = colorMap.getOrDefault(entry.getKey(), Theme.COLOR_PRIMARY);

            Arc2D.Double arc = new Arc2D.Double(x, y, size, size, currentAngle, -sweep, Arc2D.PIE);
            Area sliceArea = new Area(arc);
            sliceArea.subtract(new Area(inner));

            g2.setColor(c);
            g2.fill(sliceArea);
            g2.setColor(Theme.BG_CARD);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(sliceArea);

            currentAngle -= sweep;
        }

        // Inner circle background
        g2.setColor(Theme.BG_CARD);
        g2.fill(inner);

        // Center Total text
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2.setColor(Theme.TEXT_LIGHT);
        String totStr = String.valueOf(total);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(totStr, cutX + (cutout - fm.stringWidth(totStr)) / 2, cutY + cutout / 2 + 2);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(Theme.TEXT_MUTED);
        String sub = "TOTAL";
        FontMetrics fms = g2.getFontMetrics();
        g2.drawString(sub, cutX + (cutout - fms.stringWidth(sub)) / 2, cutY + cutout / 2 + 16);

        // Draw Interactive Legend below donut
        int legY = y + size + 16;
        int legX = 16;
        g2.setFont(Theme.FONT_SMALL);

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            Color c = colorMap.getOrDefault(entry.getKey(), Theme.COLOR_PRIMARY);
            g2.setColor(c);
            g2.fillRoundRect(legX, legY, 9, 9, 3, 3);

            g2.setColor(Theme.TEXT_MUTED);
            String itemText = entry.getKey() + " (" + entry.getValue() + ")";
            g2.drawString(itemText, legX + 13, legY + 8);

            legX += g2.getFontMetrics().stringWidth(itemText) + 24;
            if (legX > getWidth() - 90) {
                legX = 16;
                legY += 15;
            }
        }

        g2.dispose();
    }
}
