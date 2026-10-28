package com.cybersentinel.ui.charts;

import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LineTrendChartPanel extends JPanel {
    private final String chartTitle;
    private final Map<String, Integer> data = new LinkedHashMap<>();

    public LineTrendChartPanel(String chartTitle) {
        this.chartTitle = chartTitle;
        setOpaque(false);
        setPreferredSize(new Dimension(340, 240));
    }

    public void setData(Map<String, Integer> newData) {
        data.clear();
        if (newData != null) {
            data.putAll(newData);
        }
        repaint();
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

        if (data.isEmpty()) {
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

        int count = data.size();
        if (count < 2) {
            g2.dispose();
            return;
        }

        double stepX = plotW / (double) (count - 1);

        // Draw horizontal grid lines
        g2.setColor(Theme.isDarkMode() ? new Color(35, 45, 63, 100) : new Color(210, 220, 235));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(marginX, startY + plotH, marginX + plotW, startY + plotH);
        g2.drawLine(marginX, startY + plotH / 2, marginX + plotW, startY + plotH / 2);

        // Calculate points
        List<Point> points = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int px = (int) (marginX + i * stepX);
            int py = (int) (startY + plotH - ((entry.getValue() / (double) maxVal) * (plotH - 20)));
            points.add(new Point(px, py));
            labels.add(entry.getKey());
            i++;
        }

        // Draw Area Fill Under Curve
        GeneralPath area = new GeneralPath();
        area.moveTo(points.get(0).x, startY + plotH);
        for (Point p : points) {
            area.lineTo(p.x, p.y);
        }
        area.lineTo(points.get(points.size() - 1).x, startY + plotH);
        area.closePath();

        GradientPaint fillGrad = new GradientPaint(
            0, startY, new Color(56, 139, 253, 90),
            0, startY + plotH, new Color(56, 139, 253, 5)
        );
        g2.setPaint(fillGrad);
        g2.fill(area);

        // Draw Stroke Line
        g2.setColor(Theme.COLOR_PRIMARY);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GeneralPath line = new GeneralPath();
        line.moveTo(points.get(0).x, points.get(0).y);
        for (int j = 1; j < points.size(); j++) {
            line.lineTo(points.get(j).x, points.get(j).y);
        }
        g2.draw(line);

        // Draw Points and Values
        i = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            Point p = points.get(i);

            // Glow outer circle
            g2.setColor(new Color(56, 139, 253, 80));
            g2.fillOval(p.x - 6, p.y - 6, 12, 12);

            // Solid inner dot
            g2.setColor(Theme.TEXT_LIGHT);
            g2.fillOval(p.x - 3, p.y - 3, 6, 6);

            // Value text — clamp so it doesn't overlap the chart title
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(Theme.TEXT_LIGHT);
            String valStr = String.valueOf(entry.getValue());
            int vsw = g2.getFontMetrics().stringWidth(valStr);
            int valY = Math.max(startY + 12, p.y - 8);
            g2.drawString(valStr, p.x - vsw / 2, valY);

            // X-axis date label
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(Theme.TEXT_MUTED);
            String lbl = labels.get(i);
            int lsw = g2.getFontMetrics().stringWidth(lbl);
            g2.drawString(lbl, p.x - lsw / 2, startY + plotH + 16);

            i++;
        }

        g2.dispose();
    }
}
