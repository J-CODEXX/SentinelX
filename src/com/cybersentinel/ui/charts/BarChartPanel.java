package com.cybersentinel.ui.charts;

import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BarChartPanel extends JPanel {
    private final String chartTitle;
    private final Map<String, Integer> data = new LinkedHashMap<>();
    private final List<Consumer<String>> filterListeners = new ArrayList<>();
    private Color barColor = Theme.COLOR_PRIMARY;

    public BarChartPanel(String chartTitle) {
        this.chartTitle = chartTitle;
        setOpaque(false);
        setPreferredSize(new Dimension(320, 240));
    }

    public void setBarColor(Color color) {
        this.barColor = color;
    }

    public void setData(Map<String, Integer> newData) {
        data.clear();
        if (newData != null) {
            data.putAll(newData);
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

        if (data.isEmpty() || data.values().stream().allMatch(v -> v == 0)) {
            g2.setFont(Theme.FONT_REGULAR);
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString("No incident data available.", getWidth() / 2 - 80, getHeight() / 2);
            g2.dispose();
            return;
        }

        int maxVal = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        if (maxVal == 0) maxVal = 1;

        int marginX = 24;
        int startY = 45;
        int plotH = getHeight() - startY - 45;
        int plotW = getWidth() - marginX * 2;

        int barCount = data.size();
        int slotW = plotW / Math.max(1, barCount);
        int barW = Math.min(36, Math.max(14, slotW - 14));

        // Draw horizontal grid lines
        g2.setColor(new Color(35, 45, 63, 100));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(marginX, startY + plotH, marginX + plotW, startY + plotH);
        g2.drawLine(marginX, startY + plotH / 2, marginX + plotW, startY + plotH / 2);

        int i = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int val = entry.getValue();
            int barH = (int) ((val / (double) maxVal) * (plotH - 20));
            int bx = marginX + i * slotW + (slotW - barW) / 2;
            int by = startY + plotH - barH;

            // Bar Gradient
            GradientPaint gp = new GradientPaint(
                bx, by, barColor.brighter(),
                bx, by + barH, barColor
            );
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Float(bx, by, barW, barH, 6, 6));

            // Value text — clamp so it never overlaps the chart title area
            g2.setFont(Theme.FONT_SMALL);
            g2.setColor(Theme.TEXT_PRIMARY);
            String valStr = String.valueOf(val);
            int vsw = g2.getFontMetrics().stringWidth(valStr);
            int valY = Math.max(startY + 12, by - 4);
            g2.drawString(valStr, bx + (barW - vsw) / 2, valY);

            // Label text below bar
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(Theme.TEXT_MUTED);
            String label = entry.getKey();
            if (label.length() > 9) label = label.substring(0, 8) + "…";
            int lsw = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, bx + (barW - lsw) / 2, startY + plotH + 16);

            i++;
        }

        g2.dispose();
    }
}
