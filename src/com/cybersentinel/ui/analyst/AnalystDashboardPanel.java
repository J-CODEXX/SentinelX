package com.cybersentinel.ui.analyst;

import com.cybersentinel.dao.AnalyticsDAO;
import com.cybersentinel.model.DashboardStats;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.ui.charts.BarChartPanel;
import com.cybersentinel.ui.charts.DonutChartPanel;
import com.cybersentinel.ui.charts.LineTrendChartPanel;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public class AnalystDashboardPanel extends JPanel {
    private final UserSession session;
    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();
    private final Consumer<String> onFilterQueueRequested;

    private JPanel kpiRow;
    private DonutChartPanel severityChart;
    private LineTrendChartPanel trendChart;
    private BarChartPanel categoryChart;
    private BarChartPanel workloadChart;
    private DonutChartPanel slaChart;

    public AnalystDashboardPanel(UserSession session, Consumer<String> onFilterQueueRequested) {
        this.session = session;
        this.onFilterQueueRequested = onFilterQueueRequested;

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_CANVAS);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initUI();
        refreshData();
    }

    private void initUI() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // Header Title
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("Security Operations Center (SOC) — Analyst Command Dashboard");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_LIGHT);

        JLabel sub = new JLabel("Live Incident Telemetry & Triage Workload");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_MUTED);

        JPanel headBox = new JPanel(new GridLayout(2, 1, 0, 2));
        headBox.setOpaque(false);
        headBox.add(title);
        headBox.add(sub);
        head.add(headBox, BorderLayout.WEST);

        JButton refreshBtn = Theme.createSecondaryButton("🔄 Refresh SOC Data");
        refreshBtn.addActionListener(e -> refreshData());
        head.add(refreshBtn, BorderLayout.EAST);

        // 1. KPI Cards Row
        kpiRow = new JPanel(new GridLayout(1, 5, 10, 0));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        // 2. Charts Grid (Row 1: Severity, Trends, Categories)
        JPanel chartsRow1 = new JPanel(new GridLayout(1, 3, 10, 0));
        chartsRow1.setOpaque(false);
        chartsRow1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        severityChart = new DonutChartPanel("Severity Distribution");
        severityChart.addFilterListener(sev -> {
            if (onFilterQueueRequested != null) onFilterQueueRequested.accept("SEV:" + sev);
        });

        trendChart = new LineTrendChartPanel("Incident Velocity Trend (7 Days)");

        categoryChart = new BarChartPanel("Top Threat Categories");
        categoryChart.setBarColor(Theme.COLOR_PURPLE);
        categoryChart.addFilterListener(cat -> {
            if (onFilterQueueRequested != null) onFilterQueueRequested.accept("CAT:" + cat);
        });

        JPanel p1 = Theme.createCardPanel(); p1.setLayout(new BorderLayout()); p1.add(severityChart);
        JPanel p2 = Theme.createCardPanel(); p2.setLayout(new BorderLayout()); p2.add(trendChart);
        JPanel p3 = Theme.createCardPanel(); p3.setLayout(new BorderLayout()); p3.add(categoryChart);

        chartsRow1.add(p1);
        chartsRow1.add(p2);
        chartsRow1.add(p3);

        // 3. Charts Grid (Row 2: Workload, SLA Status)
        JPanel chartsRow2 = new JPanel(new GridLayout(1, 2, 10, 0));
        chartsRow2.setOpaque(false);
        chartsRow2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));

        workloadChart = new BarChartPanel("Active Analyst Workload");
        workloadChart.setBarColor(Theme.COLOR_SIMULATED);

        slaChart = new DonutChartPanel("SLA Response Compliance");

        JPanel p4 = Theme.createCardPanel(); p4.setLayout(new BorderLayout()); p4.add(workloadChart);
        JPanel p5 = Theme.createCardPanel(); p5.setLayout(new BorderLayout()); p5.add(slaChart);

        chartsRow2.add(p4);
        chartsRow2.add(p5);

        center.add(head);
        center.add(Box.createVerticalStrut(12));
        center.add(kpiRow);
        center.add(Box.createVerticalStrut(12));
        center.add(chartsRow1);
        center.add(Box.createVerticalStrut(12));
        center.add(chartsRow2);

        JScrollPane scroll = new JScrollPane(center);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);
    }

    public void refreshData() {
        DashboardStats stats = analyticsDAO.getDashboardStats(session);

        kpiRow.removeAll();
        kpiRow.add(Theme.createKpiCard("Assigned to Me", String.valueOf(stats.getMyAssignedIncidents()), "My active cases", Theme.COLOR_PRIMARY));
        kpiRow.add(Theme.createKpiCard("Open Queue", String.valueOf(stats.getOpenIncidents()), "Awaiting assignment", Theme.COLOR_SIMULATED));
        kpiRow.add(Theme.createKpiCard("Investigating", String.valueOf(stats.getInvestigatingIncidents()), "In active response", Theme.COLOR_WARNING));
        kpiRow.add(Theme.createKpiCard("Critical Threats", String.valueOf(stats.getCriticalIncidents()), "P1 emergencies", Theme.COLOR_DANGER));
        kpiRow.add(Theme.createKpiCard("Resolved", String.valueOf(stats.getResolvedIncidents()), "Closed successfully", Theme.COLOR_SUCCESS));
        kpiRow.revalidate();
        kpiRow.repaint();

        severityChart.setData(stats.getSeverityDistribution(), null);
        trendChart.setData(stats.getTrendDaily());
        categoryChart.setData(stats.getCategoryDistribution());
        workloadChart.setData(stats.getAnalystWorkload());

        // SLA chart data
        java.util.Map<String, Integer> slaMap = new java.util.LinkedHashMap<>();
        slaMap.put("Within SLA", stats.getWithinSlaCount());
        slaMap.put("Approaching SLA", stats.getApproachingSlaCount());
        slaMap.put("Breached SLA", stats.getBreachedSlaCount());

        java.util.Map<String, Color> slaColors = new java.util.LinkedHashMap<>();
        slaColors.put("Within SLA", Theme.COLOR_SUCCESS);
        slaColors.put("Approaching SLA", Theme.COLOR_WARNING);
        slaColors.put("Breached SLA", Theme.COLOR_DANGER);

        slaChart.setData(slaMap, slaColors);
    }
}
