package com.cybersentinel.ui.supermod;

import com.cybersentinel.dao.IncidentDAO;
import com.cybersentinel.dao.UserDAO;
import com.cybersentinel.model.*;
import com.cybersentinel.service.IncidentService;
import com.cybersentinel.service.SlaService;
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

public class GlobalIncidentsPanel extends JPanel {
    private final UserSession session;
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final UserDAO userDAO = new UserDAO();
    private final IncidentService incidentService = new IncidentService();
    private final SlaService slaService = new SlaService();

    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> severityFilter;
    private JComboBox<String> simFilter;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> sortFilter;
    private JTable table;
    private DefaultTableModel model;

    public GlobalIncidentsPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BG_CANVAS);
        setBorder(new EmptyBorder(14, 18, 14, 18));

        initUI();
        refreshIncidents();
    }

    private void initUI() {
        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Global Incident Management & Security Operations Queue");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_LIGHT);
        topBar.add(title, BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnBar.setOpaque(false);

        JButton assignBtn = Theme.createPrimaryButton("Assign Analyst");
        assignBtn.addActionListener(e -> handleAssign());

        JButton reopenBtn = Theme.createWarningButton("Reopen / Close");
        reopenBtn.addActionListener(e -> handleToggleClosed());

        JButton overrideSevBtn = Theme.createSecondaryButton("Override Severity");
        overrideSevBtn.addActionListener(e -> handleOverrideSeverity());

        JButton dossierBtn = Theme.createSecondaryButton("View Full Dossier");
        dossierBtn.addActionListener(e -> handleViewDossier());

        JButton deleteBtn = Theme.createDangerButton("🗑 Delete Incident");
        deleteBtn.addActionListener(e -> handleDeleteIncident());

        btnBar.add(assignBtn);
        btnBar.add(reopenBtn);
        btnBar.add(overrideSevBtn);
        btnBar.add(dossierBtn);
        btnBar.add(deleteBtn);

        topBar.add(btnBar, BorderLayout.EAST);

        // Filter Card
        JPanel filterCard = Theme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        searchField = Theme.createTextField();
        searchField.setPreferredSize(new Dimension(180, 30));
        searchField.setToolTipText("Search Code, Title, Description, System, or Type");

        statusFilter = Theme.createComboBox(new String[]{"ALL", "OPEN", "TRIAGED", "ASSIGNED", "INVESTIGATING", "CONTAINED", "REMEDIATED", "RESOLVED", "CLOSED", "ESCALATED"});
        statusFilter.setPreferredSize(new Dimension(130, 30));

        severityFilter = Theme.createComboBox(new String[]{"ALL", "CRITICAL", "HIGH", "MEDIUM", "LOW"});
        severityFilter.setPreferredSize(new Dimension(110, 30));

        simFilter = Theme.createComboBox(new String[]{"ALL (Real & Sim)", "Real Only", "Simulated Only"});
        simFilter.setPreferredSize(new Dimension(140, 30));

        categoryFilter = Theme.createComboBox(new String[]{
            "ALL", "Ransomware", "Data Breach", "Account Compromise", "Phishing",
            "Malware", "Network Attack", "Unauthorized Access", "Insider Threat", "Suspicious Activity"
        });
        categoryFilter.setPreferredSize(new Dimension(155, 30));

        sortFilter = Theme.createComboBox(new String[]{
            "Newest First", "Oldest First", "Risk: High → Low", "Risk: Low → High"
        });
        sortFilter.setPreferredSize(new Dimension(145, 30));

        JButton applyBtn = Theme.createPrimaryButton("Apply Filters");
        applyBtn.addActionListener(e -> refreshIncidents());

        JButton resetBtn = Theme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            severityFilter.setSelectedIndex(0);
            simFilter.setSelectedIndex(0);
            categoryFilter.setSelectedIndex(0);
            sortFilter.setSelectedIndex(0);
            refreshIncidents();
        });

        filterCard.add(Theme.createFilterLabel("Search:"));
        filterCard.add(searchField);
        filterCard.add(Theme.createFilterLabel("Status:"));
        filterCard.add(statusFilter);
        filterCard.add(Theme.createFilterLabel("Severity:"));
        filterCard.add(severityFilter);
        filterCard.add(Theme.createFilterLabel("Origin:"));
        filterCard.add(simFilter);
        filterCard.add(Theme.createFilterLabel("Category:"));
        filterCard.add(categoryFilter);
        filterCard.add(Theme.createFilterLabel("Sort:"));
        filterCard.add(sortFilter);
        filterCard.add(applyBtn);
        filterCard.add(resetBtn);

        // Table Card
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"Incident ID", "Title", "Category", "Severity", "Risk", "Priority", "Status", "SLA Status", "Analyst", "Reporter", "Type", "Created"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        Theme.styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(10).setCellRenderer(new StatusBadgeRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleViewDossier();
                }
            }
        });

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(filterCard, BorderLayout.NORTH);
        centerPanel.add(tableCard, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshIncidents() {
        model.setRowCount(0);

        String q = searchField.getText().trim();
        String st = (String) statusFilter.getSelectedItem();
        String sev = (String) severityFilter.getSelectedItem();
        String sim = (String) simFilter.getSelectedItem();
        String cat = (String) categoryFilter.getSelectedItem();
        String sortSel = (String) sortFilter.getSelectedItem();

        String sortOrder;
        if ("Oldest First".equals(sortSel)) sortOrder = "OLDEST";
        else if ("Risk: High \u2192 Low".equals(sortSel)) sortOrder = "RISK_DESC";
        else if ("Risk: Low \u2192 High".equals(sortSel)) sortOrder = "RISK_ASC";
        else sortOrder = "NEWEST";

        Boolean simOnly = null;
        if ("Simulated Only".equalsIgnoreCase(sim)) simOnly = true;
        else if ("Real Only".equalsIgnoreCase(sim)) simOnly = false;

        try {
            List<Incident> list = incidentDAO.findAll(q, st, sev, "ALL".equals(cat) ? null : cat, simOnly, null, null, sortOrder);
            for (Incident inc : list) {
                SlaService.SlaInfo sla = slaService.calculateSla(inc);
                model.addRow(new Object[]{
                    inc.getIncidentCode(),
                    inc.getTitle(),
                    inc.getType(),
                    inc.getSeverity().name(),
                    inc.getRiskScore() + "/100",
                    inc.getPriority().name(),
                    inc.getStatus().getDisplayName(),
                    sla.getStatus().name(),
                    inc.getAssignedToName(),
                    inc.getReportedByName(),
                    inc.isSimulated() ? "🔵 SIMULATED" : "REAL",
                    inc.getCreatedAt().toString().substring(0, 16)
                });
            }
        } catch (SQLException e) {
            System.err.println("[GLOBAL_INC] Error: " + e.getMessage());
        }
    }


    private void handleAssign() {
        Incident inc = getSelectedIncident();
        if (inc == null) return;

        try {
            List<User> analysts = userDAO.findAllAnalysts();
            if (analysts.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No active analysts found.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Object[] options = new Object[analysts.size() + 1];
            options[0] = "— Unassign —";
            for (int i = 0; i < analysts.size(); i++) {
                options[i + 1] = analysts.get(i);
            }

            Object sel = JOptionPane.showInputDialog(
                this, "Select SOC Analyst for " + inc.getIncidentCode() + ":",
                "Assign / Reassign Incident", JOptionPane.PLAIN_MESSAGE, null, options, options[0]
            );

            if (sel != null) {
                Long analystId = null;
                if (sel instanceof User u) {
                    analystId = u.getId();
                }
                incidentService.assignIncident(inc.getId(), analystId, session);
                refreshIncidents();
                JOptionPane.showMessageDialog(this, "Incident assignment updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Assignment failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleToggleClosed() {
        Incident inc = getSelectedIncident();
        if (inc == null) return;

        try {
            if (inc.getStatus() == IncidentStatus.CLOSED) {
                int res = JOptionPane.showConfirmDialog(this, "Reopen incident " + inc.getIncidentCode() + " and move to INVESTIGATING?", "Reopen Incident", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    incidentService.updateStatus(inc.getId(), IncidentStatus.INVESTIGATING, session, "Reopened by Supermod command");
                    refreshIncidents();
                }
            } else {
                int res = JOptionPane.showConfirmDialog(this, "Close incident " + inc.getIncidentCode() + " permanently?", "Close Incident", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    incidentService.updateStatus(inc.getId(), IncidentStatus.CLOSED, session, "Closed by Supermod command");
                    refreshIncidents();
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Status update failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleOverrideSeverity() {
        Incident inc = getSelectedIncident();
        if (inc == null) return;

        Severity[] sevs = Severity.values();
        Severity sel = (Severity) JOptionPane.showInputDialog(
            this, "Override severity for " + inc.getIncidentCode() + ":", "Override Severity",
            JOptionPane.PLAIN_MESSAGE, null, sevs, inc.getSeverity()
        );

        if (sel != null && sel != inc.getSeverity()) {
            try {
                incidentService.updateSeverity(inc.getId(), sel, session);
                refreshIncidents();
                JOptionPane.showMessageDialog(this, "Severity updated and risk recalculated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleViewDossier() {
        Incident inc = getSelectedIncident();
        if (inc == null) return;
        new IncidentDetailDialog(SwingUtilities.getWindowAncestor(this), inc, session, this::refreshIncidents).setVisible(true);
    }

    private void handleDeleteIncident() {
        Incident inc = getSelectedIncident();
        if (inc == null) return;

        int res = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete incident " + inc.getIncidentCode() + "?\n\n" +
            "Title: " + inc.getTitle() + "\n" +
            "Category: " + inc.getType() + " | Severity: " + inc.getSeverity().name() + "\n\n" +
            "This will delete all investigation notes, indicators, evidence links, and timeline history.\n" +
            "This action cannot be undone.",
            "Confirm Incident Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (res == JOptionPane.YES_OPTION) {
            try {
                incidentService.deleteIncident(inc.getId(), session);
                refreshIncidents();
                JOptionPane.showMessageDialog(this, "Incident " + inc.getIncidentCode() + " deleted successfully.", "Incident Deleted", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete incident: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Incident getSelectedIncident() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an incident from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        String code = (String) model.getValueAt(row, 0);
        try {
            return incidentDAO.findByCode(code);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
