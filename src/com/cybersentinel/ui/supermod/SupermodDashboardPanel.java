package com.cybersentinel.ui.supermod;

import com.cybersentinel.dao.AnalyticsDAO;
import com.cybersentinel.dao.IncidentDAO;
import com.cybersentinel.model.DashboardStats;
import com.cybersentinel.model.Incident;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.ui.charts.BarChartPanel;
import com.cybersentinel.ui.charts.DonutChartPanel;
import com.cybersentinel.ui.charts.HeatmapPanel;
import com.cybersentinel.ui.charts.LineTrendChartPanel;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SupermodDashboardPanel extends JPanel {
    private final UserSession session;
    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final Consumer<String> onNavigate;

    private JPanel alertCenterBanner;
    private JLabel alertText;
    private JPanel kpiRow1;
    private JPanel kpiRow2;

    private LineTrendChartPanel trendChart;
    private DonutChartPanel severityChart;
    private BarChartPanel categoryChart;
    private DonutChartPanel statusChart;
    private BarChartPanel workloadChart;
    private DonutChartPanel slaChart;
    private DonutChartPanel threatIntelChart;
    private DonutChartPanel simVsRealChart;
    private HeatmapPanel heatmapPanel;

    public SupermodDashboardPanel(UserSession session, Consumer<String> onNavigate) {
        this.session = session;
        this.onNavigate = onNavigate;

        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BG_CANVAS);
        setBorder(new EmptyBorder(14, 18, 14, 18));

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
        JLabel title = new JLabel("Global Security Command Center — Super Moderator Console");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_LIGHT);

        JLabel sub = new JLabel("Enterprise Incident Oversight, Threat Intelligence & SLA Telemetry");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_MUTED);

        JPanel headBox = new JPanel(new GridLayout(2, 1, 0, 2));
        headBox.setOpaque(false);
        headBox.add(title);
        headBox.add(sub);
        head.add(headBox, BorderLayout.WEST);

        JButton refreshBtn = Theme.createSecondaryButton("🔄 Refresh Command Center");
        refreshBtn.addActionListener(e -> refreshData());
        head.add(refreshBtn, BorderLayout.EAST);

        // Security Alert Center Banner
        alertCenterBanner = new JPanel(new BorderLayout(10, 0));
        alertCenterBanner.setBackground(new Color(248, 81, 73, 40));
        alertCenterBanner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.COLOR_DANGER, 1),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        alertCenterBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        alertText = new JLabel("🚨 SECURITY ALERT CENTER: Loading threat conditions...");
        alertText.setFont(Theme.FONT_BOLD);
        alertText.setForeground(Theme.COLOR_DANGER);

        JButton alertActionBtn = Theme.createDangerButton("Inspect Alerts");
        alertActionBtn.setFont(Theme.FONT_SMALL);
        alertActionBtn.addActionListener(e -> {
            if (onNavigate != null) onNavigate.accept("INCIDENTS");
        });

        alertCenterBanner.add(alertText, BorderLayout.CENTER);
        alertCenterBanner.add(alertActionBtn, BorderLayout.EAST);

        // KPI Rows
        kpiRow1 = new JPanel(new GridLayout(1, 4, 10, 0));
        kpiRow1.setOpaque(false);
        kpiRow1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        kpiRow2 = new JPanel(new GridLayout(1, 4, 10, 0));
        kpiRow2.setOpaque(false);
        kpiRow2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Charts Grid 1: Trends, Severity, Category
        JPanel grid1 = new JPanel(new GridLayout(1, 3, 10, 0));
        grid1.setOpaque(false);
        grid1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));

        trendChart = new LineTrendChartPanel("Incident Velocity Trend");
        severityChart = new DonutChartPanel("Severity Distribution");
        categoryChart = new BarChartPanel("Threat Categories");
        categoryChart.setBarColor(Theme.COLOR_PURPLE);

        JPanel p1 = Theme.createCardPanel(); p1.setLayout(new BorderLayout()); p1.add(trendChart);
        JPanel p2 = Theme.createCardPanel(); p2.setLayout(new BorderLayout()); p2.add(severityChart);
        JPanel p3 = Theme.createCardPanel(); p3.setLayout(new BorderLayout()); p3.add(categoryChart);
        grid1.add(p1); grid1.add(p2); grid1.add(p3);

        // Charts Grid 2: Global Status, Workload, SLA
        JPanel grid2 = new JPanel(new GridLayout(1, 3, 10, 0));
        grid2.setOpaque(false);
        grid2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));

        statusChart = new DonutChartPanel("Incident Lifecycle Status");
        workloadChart = new BarChartPanel("Analyst Workload Distribution");
        workloadChart.setBarColor(Theme.COLOR_SIMULATED);
        slaChart = new DonutChartPanel("SLA Target Performance");

        JPanel p4 = Theme.createCardPanel(); p4.setLayout(new BorderLayout()); p4.add(statusChart);
        JPanel p5 = Theme.createCardPanel(); p5.setLayout(new BorderLayout()); p5.add(workloadChart);
        JPanel p6 = Theme.createCardPanel(); p6.setLayout(new BorderLayout()); p6.add(slaChart);
        grid2.add(p4); grid2.add(p5); grid2.add(p6);

        // Charts Grid 3: Threat Intel, Real vs Sim, Heatmap
        JPanel grid3 = new JPanel(new GridLayout(1, 3, 10, 0));
        grid3.setOpaque(false);
        grid3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));

        threatIntelChart = new DonutChartPanel("Threat Intelligence IOCs");
        simVsRealChart = new DonutChartPanel("Real vs Simulated Ratio");
        heatmapPanel = new HeatmapPanel("Security Incident Activity Heatmap");

        JPanel p7 = Theme.createCardPanel(); p7.setLayout(new BorderLayout()); p7.add(threatIntelChart);
        JPanel p8 = Theme.createCardPanel(); p8.setLayout(new BorderLayout()); p8.add(simVsRealChart);
        JPanel p9 = Theme.createCardPanel(); p9.setLayout(new BorderLayout()); p9.add(heatmapPanel);
        grid3.add(p7); grid3.add(p8); grid3.add(p9);

        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        alertCenterBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiRow1.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiRow2.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid1.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid2.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid3.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(head);
        center.add(Box.createVerticalStrut(8));
        center.add(alertCenterBanner);
        center.add(Box.createVerticalStrut(10));
        center.add(kpiRow1);
        center.add(Box.createVerticalStrut(8));
        center.add(kpiRow2);
        center.add(Box.createVerticalStrut(10));
        center.add(grid1);
        center.add(Box.createVerticalStrut(10));
        center.add(grid2);
        center.add(Box.createVerticalStrut(10));
        center.add(grid3);

        JScrollPane scroll = new JScrollPane(center);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);
    }

    public void refreshData() {
        DashboardStats stats = analyticsDAO.getDashboardStats(null);

        // Alert Center Logic
        if (stats.getCriticalIncidents() > 0) {
            alertText.setText("🚨 CRITICAL ALERT: " + stats.getCriticalIncidents() + " critical incident(s) require immediate SOC attention!");
            alertCenterBanner.setVisible(true);
        } else if (stats.getBreachedSlaCount() > 0) {
            alertText.setText("⚠ SLA WARNING: " + stats.getBreachedSlaCount() + " incident(s) have breached configured SLA thresholds.");
            alertCenterBanner.setVisible(true);
        } else if (stats.getEscalatedIncidents() > 0) {
            alertText.setText("🚨 ESCALATION: " + stats.getEscalatedIncidents() + " incident(s) escalated to Command Center.");
            alertCenterBanner.setVisible(true);
        } else {
            alertText.setText("✅ SOC STATUS GREEN: All systems nominal. SLA compliance within targets.");
            alertCenterBanner.setBackground(new Color(46, 160, 67, 30));
            alertText.setForeground(Theme.COLOR_SUCCESS);
            alertCenterBanner.setVisible(true);
        }

        // Row 1 KPIs
        kpiRow1.removeAll();
        kpiRow1.add(Theme.createKpiCard("Total Incidents", String.valueOf(stats.getTotalIncidents()), "All recorded events", Theme.COLOR_PRIMARY));
        kpiRow1.add(Theme.createKpiCard("Critical Threats", String.valueOf(stats.getCriticalIncidents()), "P1 emergencies", Theme.COLOR_DANGER));
        kpiRow1.add(Theme.createKpiCard("Active Cases", String.valueOf(stats.getOpenIncidents() + stats.getInvestigatingIncidents()), "Open or investigating", Theme.COLOR_WARNING));
        kpiRow1.add(Theme.createKpiCard("Unassigned Queue", String.valueOf(stats.getUnassignedIncidents()), "Awaiting assignment", Theme.COLOR_SIMULATED));
        kpiRow1.revalidate();
        kpiRow1.repaint();

        // Row 2 KPIs
        kpiRow2.removeAll();
        kpiRow2.add(Theme.createKpiCard("Resolved Cases", String.valueOf(stats.getResolvedIncidents()), "Remediated & closed", Theme.COLOR_SUCCESS));
        kpiRow2.add(Theme.createKpiCard("Escalated Incidents", String.valueOf(stats.getEscalatedIncidents()), "Command escalations", Theme.COLOR_DANGER));
        kpiRow2.add(Theme.createKpiCard("Active Analysts", String.valueOf(stats.getActiveAnalysts()), "SOC staff on duty", Theme.COLOR_PURPLE));
        kpiRow2.add(Theme.createKpiCard("SLA Breaches", String.valueOf(stats.getBreachedSlaCount()), "Exceeded targets", stats.getBreachedSlaCount() > 0 ? Theme.COLOR_DANGER : Theme.COLOR_SUCCESS));
        kpiRow2.revalidate();
        kpiRow2.repaint();

        // Populate Charts
        trendChart.setData(stats.getTrendDaily());
        severityChart.setData(stats.getSeverityDistribution(), null);
        categoryChart.setData(stats.getCategoryDistribution());
        statusChart.setData(stats.getStatusDistribution(), null);
        workloadChart.setData(stats.getAnalystWorkload());

        // SLA Performance Chart
        Map<String, Integer> slaMap = new LinkedHashMap<>();
        slaMap.put("Within SLA", stats.getWithinSlaCount());
        slaMap.put("Approaching SLA", stats.getApproachingSlaCount());
        slaMap.put("Breached SLA", stats.getBreachedSlaCount());

        Map<String, Color> slaColors = new LinkedHashMap<>();
        slaColors.put("Within SLA", Theme.COLOR_SUCCESS);
        slaColors.put("Approaching SLA", Theme.COLOR_WARNING);
        slaColors.put("Breached SLA", Theme.COLOR_DANGER);
        slaChart.setData(slaMap, slaColors);

        // Threat Intel Chart
        threatIntelChart.setData(stats.getThreatIntelDistribution(), null);

        // Real vs Simulated Chart
        Map<String, Integer> simMap = new LinkedHashMap<>();
        simMap.put("Real Threats", stats.getRealIncidents());
        simMap.put("Simulated", stats.getSimulatedIncidents());
        Map<String, Color> simColors = new LinkedHashMap<>();
        simColors.put("Real Threats", Theme.COLOR_PRIMARY);
        simColors.put("Simulated", Theme.COLOR_SIMULATED);
        simVsRealChart.setData(simMap, simColors);
    }
}
