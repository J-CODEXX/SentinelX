package com.cybersentinel.ui.user;

import com.cybersentinel.dao.IncidentDAO;
import com.cybersentinel.model.Incident;
import com.cybersentinel.model.UserSession;
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

public class UserIncidentTrackingPanel extends JPanel {
    private final UserSession session;
    private final IncidentDAO incidentDAO = new IncidentDAO();

    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> severityFilter;
    private JComboBox<String> originFilter;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> sortFilter;
    private JTable table;
    private DefaultTableModel model;

    public UserIncidentTrackingPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_CANVAS);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initUI();
        loadIncidents();
    }

    private void initUI() {
        // Filter Card
        JPanel filterCard = Theme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));
filterCard.setPreferredSize(new Dimension(0, 180));

        JLabel sLbl = Theme.createFilterLabel("Search:");

        searchField = Theme.createTextField();
        searchField.setPreferredSize(new Dimension(180, 30));
        searchField.setToolTipText("Search by code, title, description, system, or type");

        JLabel statLbl = Theme.createFilterLabel("Status:");

        statusFilter = Theme.createComboBox(new String[]{"ALL", "OPEN", "TRIAGED", "ASSIGNED", "INVESTIGATING", "CONTAINED", "RESOLVED", "CLOSED"});
        statusFilter.setPreferredSize(new Dimension(130, 30));

        JLabel sevLbl = Theme.createFilterLabel("Severity:");

        severityFilter = Theme.createComboBox(new String[]{"ALL", "CRITICAL", "HIGH", "MEDIUM", "LOW"});
        severityFilter.setPreferredSize(new Dimension(110, 30));

        JLabel origLbl = Theme.createFilterLabel("Origin:");
        originFilter = Theme.createComboBox(new String[]{"ALL (Real & Sim)", "Real Only", "Simulated Only"});
        originFilter.setPreferredSize(new Dimension(140, 30));

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
        applyBtn.setPreferredSize(new Dimension(130, 30));
        applyBtn.addActionListener(e -> loadIncidents());

        JButton resetBtn = Theme.createSecondaryButton("Reset");
        resetBtn.setPreferredSize(new Dimension(120, 30));
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            severityFilter.setSelectedIndex(0);
            originFilter.setSelectedIndex(0);
            categoryFilter.setSelectedIndex(0);
            sortFilter.setSelectedIndex(0);
            loadIncidents();
        });

        filterCard.add(sLbl);
        filterCard.add(searchField);
        filterCard.add(statLbl);
        filterCard.add(statusFilter);
        filterCard.add(sevLbl);
        filterCard.add(severityFilter);
        filterCard.add(origLbl);
        filterCard.add(originFilter);
        filterCard.add(Theme.createFilterLabel("Category:"));
        filterCard.add(categoryFilter);
        filterCard.add(Theme.createFilterLabel("Sort:"));
        filterCard.add(sortFilter);
        filterCard.add(applyBtn);
        filterCard.add(resetBtn);

        // Table Card
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("My Incident Submissions");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_PRIMARY);

        JButton viewBtn = Theme.createPrimaryButton("View Full Dossier");
        viewBtn.addActionListener(e -> openSelectedIncident());

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(viewBtn, BorderLayout.EAST);

        String[] cols = {"Incident Code", "Title", "Category", "Severity", "Risk Score", "Status", "Assigned Analyst", "Reported Date"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedIncident();
                }
            }
        });

        tableCard.add(headerPanel, BorderLayout.NORTH);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        add(filterCard, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    public void loadIncidents() {
        model.setRowCount(0);
        String q = searchField.getText().trim();
        String st = (String) statusFilter.getSelectedItem();
        String sev = (String) severityFilter.getSelectedItem();
        String orig = (String) originFilter.getSelectedItem();
        String cat = (String) categoryFilter.getSelectedItem();
        String sortSel = (String) sortFilter.getSelectedItem();

        String sortOrder;
        if ("Oldest First".equals(sortSel)) sortOrder = "OLDEST";
        else if ("Risk: High \u2192 Low".equals(sortSel)) sortOrder = "RISK_DESC";
        else if ("Risk: Low \u2192 High".equals(sortSel)) sortOrder = "RISK_ASC";
        else sortOrder = "NEWEST";

        Boolean simOnly = null;
        if ("Real Only".equalsIgnoreCase(orig)) simOnly = false;
        else if ("Simulated Only".equalsIgnoreCase(orig)) simOnly = true;

        try {
            List<Incident> list = incidentDAO.findAll(q, st, sev, "ALL".equals(cat) ? null : cat, simOnly, null, session.getUserId(), sortOrder);
            for (Incident inc : list) {
                model.addRow(new Object[]{
                    inc.getIncidentCode(),
                    inc.getTitle(),
                    inc.getType(),
                    inc.getSeverity().name(),
                    inc.getRiskScore() + "/100",
                    inc.getStatus().getDisplayName(),
                    inc.getAssignedToName(),
                    inc.getCreatedAt().toString().substring(0, 16)
                });
            }
        } catch (SQLException e) {
            System.err.println("[USER_TRACK] Error loading incidents: " + e.getMessage());
        }
    }

    private void openSelectedIncident() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an incident from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String code = (String) model.getValueAt(row, 0);
        try {
            Incident inc = incidentDAO.findByCode(code);
            if (inc != null) {
                new IncidentDetailDialog(SwingUtilities.getWindowAncestor(this), inc, session, this::loadIncidents).setVisible(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
