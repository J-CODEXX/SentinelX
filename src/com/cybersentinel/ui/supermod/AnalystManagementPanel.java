package com.cybersentinel.ui.supermod;

import com.cybersentinel.dao.AnalyticsDAO;
import com.cybersentinel.dao.IncidentDAO;
import com.cybersentinel.dao.UserDAO;
import com.cybersentinel.model.DashboardStats;
import com.cybersentinel.model.Incident;
import com.cybersentinel.model.Role;
import com.cybersentinel.model.User;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.service.AuthService;
import com.cybersentinel.ui.charts.BarChartPanel;
import com.cybersentinel.ui.common.StatusBadgeRenderer;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class AnalystManagementPanel extends JPanel {
    private final UserSession session;
    private final UserDAO userDAO = new UserDAO();
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();
    private final AuthService authService = new AuthService();

    private JTable analystTable;
    private DefaultTableModel analystModel;
    private BarChartPanel workloadChart;
    private JButton pendingRequestsBtn;

    public AnalystManagementPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BG_CANVAS);
        setBorder(new EmptyBorder(14, 18, 14, 18));

        initUI();
        refreshAnalysts();
    }

    private void initUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("SOC Security Analyst Team Management");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_LIGHT);
        topBar.add(title, BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnBar.setOpaque(false);

        pendingRequestsBtn = Theme.createWarningButton("Review Requests (0)");
        pendingRequestsBtn.setPreferredSize(new Dimension(180, 30));
        pendingRequestsBtn.addActionListener(e -> handleOpenPendingRequests());

        JButton addBtn = Theme.createPrimaryButton("+ Add New Analyst");
        addBtn.addActionListener(e -> handleAddAnalyst());

        JButton toggleBtn = Theme.createWarningButton("Toggle Active");
        toggleBtn.addActionListener(e -> handleToggleActive());

        JButton viewCasesBtn = Theme.createSecondaryButton("View Active Caseload");
        viewCasesBtn.addActionListener(e -> handleViewAnalystCaseload());

        btnBar.add(pendingRequestsBtn);
        btnBar.add(addBtn);
        btnBar.add(toggleBtn);
        btnBar.add(viewCasesBtn);

        topBar.add(btnBar, BorderLayout.EAST);

        // Center Split: Analyst Table (Left) + Workload Bar Chart (Right)
        JPanel center = new JPanel(new GridLayout(1, 2, 12, 0));
        center.setOpaque(false);

        // Table Card
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 8));

        JLabel tTitle = new JLabel("Security Operations Team Roster");
        tTitle.setFont(Theme.FONT_HEADER);
        tTitle.setForeground(Theme.TEXT_PRIMARY);

        String[] cols = {"Analyst ID", "Username", "Email", "Status", "Created"};
        analystModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        analystTable = new JTable(analystModel);
        Theme.styleTable(analystTable);

        tableCard.add(tTitle, BorderLayout.NORTH);
        tableCard.add(new JScrollPane(analystTable), BorderLayout.CENTER);

        // Chart Card
        JPanel chartCard = Theme.createCardPanel();
        chartCard.setLayout(new BorderLayout());

        workloadChart = new BarChartPanel("Live Active Caseload per Analyst");
        workloadChart.setBarColor(Theme.COLOR_SIMULATED);
        chartCard.add(workloadChart, BorderLayout.CENTER);

        center.add(tableCard);
        center.add(chartCard);

        add(topBar, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    public void refreshAnalysts() {
        analystModel.setRowCount(0);

        try {
            List<User> list = userDAO.findAllAnalysts();
            for (User u : list) {
                analystModel.addRow(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.isActive() ? "ACTIVE" : "INACTIVE",
                    u.getCreatedAt().toString().substring(0, 10)
                });
            }

            DashboardStats stats = analyticsDAO.getDashboardStats(null);
            workloadChart.setData(stats.getAnalystWorkload());

            int pendingCount = authService.getPendingAnalystRequestsCount();
            if (pendingCount > 0) {
                pendingRequestsBtn.setText("Review Requests (" + pendingCount + " Pending)");
                pendingRequestsBtn.setBackground(Theme.COLOR_WARNING);
            } else {
                pendingRequestsBtn.setText("Review Requests (0)");
                pendingRequestsBtn.setBackground(Theme.BG_CARD_HOVER);
            }
        } catch (SQLException e) {
            System.err.println("[ANALYST_MGMT] Error: " + e.getMessage());
        }
    }

    private void handleOpenPendingRequests() {
        AnalystRequestsDialog dialog = new AnalystRequestsDialog(
            SwingUtilities.getWindowAncestor(this),
            session,
            this::refreshAnalysts
        );
        dialog.setVisible(true);
    }

    private void handleAddAnalyst() {
        JTextField uField = Theme.createTextField();
        JTextField eField = Theme.createTextField();
        JPasswordField pField = Theme.createPasswordField();

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(Theme.createFilterLabel("Analyst Username:"));
        form.add(uField);
        form.add(Theme.createFilterLabel("Work Email:"));
        form.add(eField);
        form.add(Theme.createFilterLabel("Initial Password:"));
        form.add(pField);

        int res = JOptionPane.showConfirmDialog(this, form, "Add New SOC Analyst", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String u = uField.getText().trim();
            String em = eField.getText().trim();
            String p = new String(pField.getPassword());
            try {
                authService.registerUser(u, em, p, Role.ANALYST);
                refreshAnalysts();
                JOptionPane.showMessageDialog(this, "Analyst account " + u + " created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to create analyst: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleToggleActive() {
        int row = analystTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an analyst from the roster.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        long id = (Long) analystModel.getValueAt(row, 0);
        try {
            User u = userDAO.findById(id);
            if (u != null) {
                authService.setUserActive(session.getUserId(), session.getUsername(), id, !u.isActive());
                refreshAnalysts();
                JOptionPane.showMessageDialog(this, "Status updated for " + u.getUsername(), "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleViewAnalystCaseload() {
        int row = analystTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an analyst from the roster.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        long id = (Long) analystModel.getValueAt(row, 0);
        String name = (String) analystModel.getValueAt(row, 1);

        try {
            List<Incident> list = incidentDAO.findAll(null, null, null, null, null, id, null, null);
            StringBuilder sb = new StringBuilder();
            sb.append("Caseload for ").append(name).append(" [Total Assigned: ").append(list.size()).append("]:\n\n");
            for (Incident inc : list) {
                sb.append(String.format("• %s | %s | Sev: %s | Status: %s | Risk: %d/100\n",
                    inc.getIncidentCode(), inc.getTitle(), inc.getSeverity().name(),
                    inc.getStatus().getDisplayName(), inc.getRiskScore()));
            }
            if (list.isEmpty()) {
                sb.append("No active incidents currently assigned to this analyst.");
            }

            JTextArea ta = Theme.createTextArea(14, 50);
            ta.setText(sb.toString());
            ta.setEditable(false);

            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Analyst Workload — " + name, JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
