package com.cybersentinel.ui.user;

import com.cybersentinel.dao.AnalyticsDAO;
import com.cybersentinel.dao.IncidentDAO;
import com.cybersentinel.dao.NotificationDAO;
import com.cybersentinel.model.DashboardStats;
import com.cybersentinel.model.Incident;
import com.cybersentinel.model.Notification;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.ui.charts.BarChartPanel;
import com.cybersentinel.ui.charts.DonutChartPanel;
import com.cybersentinel.ui.common.IncidentDetailDialog;
import com.cybersentinel.ui.common.StatusBadgeRenderer;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

public class UserDashboardPanel extends JPanel {
    private final UserSession session;
    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    private JPanel kpiRow;
    private DonutChartPanel statusDonut;
    private BarChartPanel severityBar;
    private JTable recentTable;
    private DefaultTableModel tableModel;
    private DefaultListModel<String> notifListModel;

    public UserDashboardPanel(UserSession session, Runnable onReportIncidentClicked) {
        this.session = session;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_CANVAS);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Banner with CTA
        JPanel topBanner = Theme.createCardPanel();
        topBanner.setLayout(new BorderLayout());

        JPanel greeting = new JPanel(new GridLayout(2, 1, 0, 4));
        greeting.setOpaque(false);
        JLabel hi = new JLabel("Welcome back, " + session.getUsername() + " 👋");
        hi.setFont(Theme.FONT_TITLE);
        hi.setForeground(Theme.TEXT_LIGHT);

        JLabel desc = new JLabel("Track your reported security incidents and cybersecurity safety status.");
        desc.setFont(Theme.FONT_REGULAR);
        desc.setForeground(Theme.TEXT_MUTED);
        greeting.add(hi);
        greeting.add(desc);

        JButton reportBtn = Theme.createPrimaryButton("+ Report New Incident");
        reportBtn.addActionListener(e -> {
            if (onReportIncidentClicked != null) onReportIncidentClicked.run();
        });

        topBanner.add(greeting, BorderLayout.WEST);
        topBanner.add(reportBtn, BorderLayout.EAST);

        // Center Content (KPIs + Charts + Table + Notifs)
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // KPI Cards Row
        kpiRow = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        // Charts Row
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 12, 0));
        chartsRow.setOpaque(false);
        chartsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        statusDonut = new DonutChartPanel("My Incident Status");
        severityBar = new BarChartPanel("Severity Distribution");
        severityBar.setBarColor(Theme.COLOR_PRIMARY);

        JPanel c1 = Theme.createCardPanel();
        c1.setLayout(new BorderLayout());
        c1.add(statusDonut);

        JPanel c2 = Theme.createCardPanel();
        c2.setLayout(new BorderLayout());
        c2.add(severityBar);

        chartsRow.add(c1);
        chartsRow.add(c2);

        // Bottom Split: Recent Incidents (Left) + Notifications (Right)
        JPanel bottomRow = new JPanel(new BorderLayout(12, 0));
        bottomRow.setOpaque(false);

        // Recent Incidents Table
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 8));

        JLabel tblTitle = new JLabel("Recent Reported Incidents (Double-click to open)");
        tblTitle.setFont(Theme.FONT_HEADER);
        tblTitle.setForeground(Theme.TEXT_PRIMARY);

        String[] cols = {"Incident ID", "Title", "Type", "Severity", "Risk", "Status", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        recentTable = new JTable(tableModel);
        Theme.styleTable(recentTable);
        recentTable.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeRenderer());
        recentTable.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        recentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedIncident();
                }
            }
        });

        tableCard.add(tblTitle, BorderLayout.NORTH);
        tableCard.add(new JScrollPane(recentTable), BorderLayout.CENTER);

        // Notifications Sidebar
        JPanel notifCard = Theme.createCardPanel();
        notifCard.setLayout(new BorderLayout(0, 8));
        notifCard.setPreferredSize(new Dimension(280, 0));

        JPanel notifHeader = new JPanel(new BorderLayout());
        notifHeader.setOpaque(false);
        JLabel notifTitle = new JLabel("Alerts & Updates");
        notifTitle.setFont(Theme.FONT_HEADER);
        notifTitle.setForeground(Theme.TEXT_PRIMARY);

        JButton markAllBtn = new JButton("Clear All");
        markAllBtn.setFont(Theme.FONT_SMALL);
        markAllBtn.setForeground(Theme.COLOR_PRIMARY);
        markAllBtn.setContentAreaFilled(false);
        markAllBtn.setBorderPainted(false);
        markAllBtn.addActionListener(e -> clearNotifications());
        notifHeader.add(notifTitle, BorderLayout.WEST);
        notifHeader.add(markAllBtn, BorderLayout.EAST);

        notifListModel = new DefaultListModel<>();
        JList<String> notifList = new JList<>(notifListModel);
        notifList.setBackground(Theme.BG_INPUT);
        notifList.setForeground(Theme.TEXT_PRIMARY);
        notifList.setFont(Theme.FONT_SMALL);

        notifCard.add(notifHeader, BorderLayout.NORTH);
        notifCard.add(new JScrollPane(notifList), BorderLayout.CENTER);

        bottomRow.add(tableCard, BorderLayout.CENTER);
        bottomRow.add(notifCard, BorderLayout.EAST);

        center.add(kpiRow);
        center.add(Box.createVerticalStrut(12));
        center.add(chartsRow);
        center.add(Box.createVerticalStrut(12));
        center.add(bottomRow);

        add(topBanner, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        // 1. Refresh KPIs and Charts
        DashboardStats stats = analyticsDAO.getDashboardStats(session);

        kpiRow.removeAll();
        kpiRow.add(Theme.createKpiCard("Total Reported", String.valueOf(stats.getTotalIncidents()), "All your submissions", Theme.COLOR_PRIMARY));
        kpiRow.add(Theme.createKpiCard("Awaiting Triage", String.valueOf(stats.getOpenIncidents()), "Open in SOC queue", Theme.COLOR_SIMULATED));
        kpiRow.add(Theme.createKpiCard("Investigating", String.valueOf(stats.getInvestigatingIncidents()), "Assigned to analysts", Theme.COLOR_WARNING));
        kpiRow.add(Theme.createKpiCard("Resolved", String.valueOf(stats.getResolvedIncidents()), "Closed & remediated", Theme.COLOR_SUCCESS));
        kpiRow.revalidate();
        kpiRow.repaint();

        statusDonut.setData(stats.getStatusDistribution(), null);
        severityBar.setData(stats.getSeverityDistribution());

        // 2. Refresh Recent Table
        tableModel.setRowCount(0);
        try {
            List<Incident> list = incidentDAO.findAll(null, null, null, null, null, null, session.getUserId(), null);
            int max = Math.min(10, list.size());
            for (int i = 0; i < max; i++) {
                Incident inc = list.get(i);
                tableModel.addRow(new Object[]{
                    inc.getIncidentCode(),
                    inc.getTitle(),
                    inc.getType(),
                    inc.getSeverity().name(),
                    inc.getRiskScore() + "/100",
                    inc.getStatus().getDisplayName(),
                    inc.getCreatedAt().toString().substring(0, 10)
                });
            }
        } catch (SQLException e) {
            System.err.println("[USER_DASH] Error loading incidents: " + e.getMessage());
        }

        // 3. Refresh Notifications
        notifListModel.clear();
        try {
            List<Notification> notifs = notificationDAO.getForUser(session.getUserId(), false);
            if (notifs.isEmpty()) {
                notifListModel.addElement("No new notifications.");
            } else {
                for (Notification n : notifs) {
                    notifListModel.addElement(n.getMessage());
                }
            }
        } catch (SQLException ignored) {}
    }

    private void clearNotifications() {
        try {
            notificationDAO.markAllRead(session.getUserId());
            refreshData();
        } catch (SQLException ignored) {}
    }

    private void openSelectedIncident() {
        int row = recentTable.getSelectedRow();
        if (row < 0) return;
        String code = (String) tableModel.getValueAt(row, 0);
        try {
            Incident inc = incidentDAO.findByCode(code);
            if (inc != null) {
                new IncidentDetailDialog(SwingUtilities.getWindowAncestor(this), inc, session).setVisible(true);
            }
        } catch (SQLException ignored) {}
    }
}
