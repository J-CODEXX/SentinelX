package com.cybersentinel.ui.charts;

import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class HeatmapPanel extends JPanel {
    private final String chartTitle;
    private final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private final String[] categories = {"Phishing", "Malware", "Ransomware", "Acct Comp", "Net Attack"};
    private final int[][] grid = {
        {1, 3, 0, 2, 4},
        {2, 1, 1, 3, 2},
        {4, 5, 2, 1, 3},
        {3, 2, 0, 4, 1},
        {5, 4, 3, 2, 6},
        {1, 0, 1, 0, 2},
        {0, 1, 0, 1, 1}
    };

    public HeatmapPanel(String chartTitle) {
        this.chartTitle = chartTitle;
        setOpaque(false);
        setPreferredSize(new Dimension(320, 240));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Title
        g2.setFont(Theme.FONT_HEADER);
        g2.setColor(Theme.TEXT_PRIMARY);
        g2.drawString(chartTitle, 16, 22);

        int startX = 55;
        int startY = 48;
        int cols = categories.length;
        int rows = days.length;

        int cellW = (getWidth() - startX - 20) / cols;
        int cellH = (getHeight() - startY - 25) / rows;

        // Draw Category Headers
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(Theme.TEXT_MUTED);
        for (int c = 0; c < cols; c++) {
            int cx = startX + c * cellW;
            String cat = categories[c];
            if (cat.length() > 8) cat = cat.substring(0, 7) + "…";
            g2.drawString(cat, cx + 2, startY - 8);
        }

        // Draw Matrix
        for (int r = 0; r < rows; r++) {
            // Day label
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString(days[r], 16, startY + r * cellH + cellH / 2 + 4);

            for (int c = 0; c < cols; c++) {
                int val = grid[r][c];
                Color cellColor = getIntensityColor(val);
                int cx = startX + c * cellW;
                int cy = startY + r * cellH;

                g2.setColor(cellColor);
                g2.fill(new RoundRectangle2D.Float(cx + 2, cy + 2, cellW - 4, cellH - 4, 4, 4));

                if (val > 0) {
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    g2.setColor(val > 2 ? Color.BLACK : Theme.TEXT_LIGHT);
                    String valStr = String.valueOf(val);
                    int strW = g2.getFontMetrics().stringWidth(valStr);
                    g2.drawString(valStr, cx + (cellW - strW) / 2, cy + cellH / 2 + 4);
                }
            }
        }

        g2.dispose();
    }

    private Color getIntensityColor(int val) {
        if (val <= 0) return Theme.isDarkMode() ? new Color(21, 27, 40) : new Color(230, 236, 245);
        if (val == 1) return new Color(56, 139, 253, 90);
        if (val == 2) return new Color(56, 139, 253, 170);
        if (val == 3) return new Color(56, 139, 253, 230);
        if (val == 4) return new Color(210, 153, 34);
        return new Color(248, 81, 73);
    }
}
