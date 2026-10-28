package com.cybersentinel.ui.analyst;

import com.cybersentinel.dao.IncidentDAO;
import com.cybersentinel.engine.SimulationEngine;
import com.cybersentinel.model.Incident;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.service.IncidentService;
import com.cybersentinel.service.SlaService;
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
import java.util.function.Consumer;

public class IncidentQueuePanel extends JPanel {
    private final UserSession session;
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final IncidentService incidentService = new IncidentService();
    private final SlaService slaService = new SlaService();
    private final Consumer<Incident> onOpenWorkspace;

    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> severityFilter;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> sortFilter;
    private JCheckBox unassignedOnlyCb;
    private JCheckBox myCasesOnlyCb;
    private JCheckBox simulatedOnlyCb;
    private JCheckBox realOnlyCb;

    private JTable table;
    private DefaultTableModel model;

    public IncidentQueuePanel(UserSession session, Consumer<Incident> onOpenWorkspace) {
        this.session = session;
        this.onOpenWorkspace = onOpenWorkspace;

        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG_CANVAS);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initUI();
        refreshQueue();
    }

    private void initUI() {
        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTitle.setOpaque(false);
        JLabel title = new JLabel("SOC Triage & Incident Response Queue");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_LIGHT);
        leftTitle.add(title);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton claimBtn = Theme.createPrimaryButton("🎯 Claim Incident");
        claimBtn.addActionListener(e -> handleClaimSelected());

        JButton openWorkspaceBtn = Theme.createWarningButton("🔍 Open Workspace");
        openWorkspaceBtn.addActionListener(e -> handleOpenSelected());

        JButton simBtn = Theme.createSuccessButton("⚡ SIMULATE INCIDENT");
        simBtn.addActionListener(e -> handleQuickSimulate());

        JButton deleteBtn = Theme.createDangerButton("🗑 Delete Incident");
        deleteBtn.addActionListener(e -> handleDeleteSelected());

        rightActions.add(claimBtn);
        rightActions.add(openWorkspaceBtn);
        rightActions.add(simBtn);
        rightActions.add(deleteBtn);

        topBar.add(leftTitle, BorderLayout.WEST);
        topBar.add(rightActions, BorderLayout.EAST);

        // Filter Bar Card
        JPanel filterCard = Theme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        searchField = Theme.createTextField();
        searchField.setPreferredSize(new Dimension(160, 30));
        searchField.setToolTipText("Search ID, Title, System, Description, or Type");

        statusFilter = Theme.createComboBox(new String[]{"ALL", "OPEN", "TRIAGED", "ASSIGNED", "INVESTIGATING", "CONTAINED", "REMEDIATED", "RESOLVED", "ESCALATED"});
        statusFilter.setPreferredSize(new Dimension(130, 30));

        severityFilter = Theme.createComboBox(new String[]{"ALL", "CRITICAL", "HIGH", "MEDIUM", "LOW"});
        severityFilter.setPreferredSize(new Dimension(110, 30));

        categoryFilter = Theme.createComboBox(new String[]{
            "ALL", "Ransomware", "Data Breach", "Account Compromise", "Phishing",
            "Malware", "Network Attack", "Unauthorized Access", "Insider Threat", "Suspicious Activity"
        });
        categoryFilter.setPreferredSize(new Dimension(155, 30));

        sortFilter = Theme.createComboBox(new String[]{
            "Newest First", "Oldest First", "Risk: High → Low", "Risk: Low → High"
        });
        sortFilter.setPreferredSize(new Dimension(145, 30));

        unassignedOnlyCb = new JCheckBox("Unassigned Only");
        unassignedOnlyCb.setOpaque(false);
        unassignedOnlyCb.setFont(Theme.FONT_BOLD);
        unassignedOnlyCb.setForeground(Theme.TEXT_PRIMARY);

        myCasesOnlyCb = new JCheckBox("Assigned to Me");
        myCasesOnlyCb.setOpaque(false);
        myCasesOnlyCb.setFont(Theme.FONT_BOLD);
        myCasesOnlyCb.setForeground(Theme.TEXT_PRIMARY);

        realOnlyCb = new JCheckBox("Real Only");
        realOnlyCb.setOpaque(false);
        realOnlyCb.setFont(Theme.FONT_BOLD);
        realOnlyCb.setForeground(Theme.COLOR_SUCCESS);
        realOnlyCb.addActionListener(e -> {
            if (realOnlyCb.isSelected()) simulatedOnlyCb.setSelected(false);
            refreshQueue();
        });

        simulatedOnlyCb = new JCheckBox("Simulated Only");
        simulatedOnlyCb.setOpaque(false);
        simulatedOnlyCb.setFont(Theme.FONT_BOLD);
        simulatedOnlyCb.setForeground(Theme.COLOR_SIMULATED);
        simulatedOnlyCb.addActionListener(e -> {
            if (simulatedOnlyCb.isSelected()) realOnlyCb.setSelected(false);
            refreshQueue();
        });

        JButton filterBtn = Theme.createSecondaryButton("Filter");
        filterBtn.addActionListener(e -> refreshQueue());

        JButton resetBtn = Theme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            severityFilter.setSelectedIndex(0);
            categoryFilter.setSelectedIndex(0);
            sortFilter.setSelectedIndex(0);
            unassignedOnlyCb.setSelected(false);
            myCasesOnlyCb.setSelected(false);
            realOnlyCb.setSelected(false);
            simulatedOnlyCb.setSelected(false);
            refreshQueue();
        });

        filterCard.add(Theme.createFilterLabel("Search:"));
        filterCard.add(searchField);
        filterCard.add(Theme.createFilterLabel("Status:"));
        filterCard.add(statusFilter);
        filterCard.add(Theme.createFilterLabel("Severity:"));
        filterCard.add(severityFilter);
        filterCard.add(Theme.createFilterLabel("Category:"));
        filterCard.add(categoryFilter);
        filterCard.add(Theme.createFilterLabel("Sort:"));
        filterCard.add(sortFilter);
        filterCard.add(unassignedOnlyCb);
        filterCard.add(myCasesOnlyCb);
        filterCard.add(realOnlyCb);
        filterCard.add(simulatedOnlyCb);
        filterCard.add(filterBtn);
        filterCard.add(resetBtn);

        // Table Card
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"Incident ID", "Title", "Category", "Severity", "Risk Score", "Status", "SLA Status", "Assigned Analyst", "Type", "Created"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        Theme.styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusBadgeRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleOpenSelected();
                }
            }
        });

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(filterCard, BorderLayout.NORTH);
        centerPanel.add(tableCard, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void applyExternalFilter(String filterSpec) {
        if (filterSpec.startsWith("SEV:")) {
            severityFilter.setSelectedItem(filterSpec.substring(4).toUpperCase());
        } else if (filterSpec.startsWith("CAT:")) {
            searchField.setText(filterSpec.substring(4));
        }
        refreshQueue();
    }

    public void refreshQueue() {
        model.setRowCount(0);

        String query = searchField.getText().trim();
        String st = (String) statusFilter.getSelectedItem();
        String sev = (String) severityFilter.getSelectedItem();
        String cat = (String) categoryFilter.getSelectedItem();
        String sortSel = (String) sortFilter.getSelectedItem();

        String sortOrder;
        if ("Oldest First".equals(sortSel)) sortOrder = "OLDEST";
        else if ("Risk: High \u2192 Low".equals(sortSel)) sortOrder = "RISK_DESC";
        else if ("Risk: Low \u2192 High".equals(sortSel)) sortOrder = "RISK_ASC";
        else sortOrder = "NEWEST";

        Long assignedTo = null;
        if (unassignedOnlyCb.isSelected()) {
            assignedTo = -1L; // unassigned flag
        } else if (myCasesOnlyCb.isSelected()) {
            assignedTo = session.getUserId();
        }

        Boolean simOnly = null;
        if (realOnlyCb.isSelected()) {
            simOnly = false;
        } else if (simulatedOnlyCb.isSelected()) {
            simOnly = true;
        }

        try {
            List<Incident> list = incidentDAO.findAll(query, st, sev, "ALL".equals(cat) ? null : cat, simOnly, assignedTo, null, sortOrder);
            for (Incident inc : list) {
                SlaService.SlaInfo sla = slaService.calculateSla(inc);
                model.addRow(new Object[]{
                    inc.getIncidentCode(),
                    inc.getTitle(),
                    inc.getType(),
                    inc.getSeverity().name(),
                    inc.getRiskScore() + "/100 (" + inc.getPriority().name() + ")",
                    inc.getStatus().getDisplayName(),
                    sla.getStatus().name(),
                    inc.getAssignedToName(),
                    inc.isSimulated() ? "🔵 SIMULATED" : "REAL",
                    inc.getCreatedAt().toString().substring(0, 16)
                });
            }
        } catch (SQLException e) {
            System.err.println("[QUEUE] Error loading queue: " + e.getMessage());
        }
    }

    private void handleClaimSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an unassigned incident from the queue to claim.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String code = (String) model.getValueAt(row, 0);
        try {
            Incident inc = incidentDAO.findByCode(code);
            if (inc == null) return;

            if (inc.isAssigned() && !inc.getAssignedTo().equals(session.getUserId())) {
                JOptionPane.showMessageDialog(this, "This incident is already assigned to: " + inc.getAssignedToName(), "Cannot Claim", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean claimed = incidentService.claimIncident(inc.getId(), session);
            if (claimed) {
                JOptionPane.showMessageDialog(this, "You have claimed " + inc.getIncidentCode() + "!\nStatus changed to ASSIGNED.", "Claimed", JOptionPane.INFORMATION_MESSAGE);
                refreshQueue();
            } else {
                JOptionPane.showMessageDialog(this, "Could not claim incident (may have just been claimed by another analyst).", "Race Condition", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error claiming incident: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleOpenSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an incident to open in the Investigation Workspace.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String code = (String) model.getValueAt(row, 0);
        try {
            Incident inc = incidentDAO.findByCode(code);
            if (inc != null && onOpenWorkspace != null) {
                onOpenWorkspace.accept(inc);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleQuickSimulate() {
        try {
            Incident sim = SimulationEngine.simulateIncident("RANDOM", null, null, session);
            refreshQueue();
            JOptionPane.showMessageDialog(this,
                "<html><b>⚡ Simulated Incident Generated!</b><br><br>" +
                "Incident ID: <font color='#388BFD'><b>" + sim.getIncidentCode() + "</b></font><br>" +
                "Title: <b>" + sim.getTitle() + "</b><br>" +
                "Classification: <b>" + sim.getType() + "</b><br>" +
                "Severity: <b>" + sim.getSeverity().name() + "</b><br>" +
                "Risk Score: <b>" + sim.getRiskScore() + "/100</b><br><br>" +
                "The incident has entered the normal SOC pipeline and is now in your queue.</html>",
                "Simulated Incident", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Simulation error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an incident from the queue to delete.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String code = (String) model.getValueAt(row, 0);
        try {
            Incident inc = incidentDAO.findByCode(code);
            if (inc == null) return;

            int res = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently delete incident " + inc.getIncidentCode() + "?\n\n" +
                "Title: " + inc.getTitle() + "\n" +
                "Category: " + inc.getType() + " | Severity: " + inc.getSeverity().name() + "\n\n" +
                "This action cannot be undone.",
                "Confirm Incident Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (res == JOptionPane.YES_OPTION) {
                incidentService.deleteIncident(inc.getId(), session);
                refreshQueue();
                JOptionPane.showMessageDialog(this, "Incident " + inc.getIncidentCode() + " deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to delete incident: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
