package com.cybersentinel.ui.supermod;

import com.cybersentinel.dao.AuditDAO;
import com.cybersentinel.model.AuditLog;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.service.ReportService;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class AuditLogsPanel extends JPanel {
    private final UserSession session;
    private final AuditDAO auditDAO = new AuditDAO();
    private final ReportService reportService = new ReportService();

    private JTextField actorField;
    private JComboBox<String> actionFilter;
    private JComboBox<String> entityFilter;
    private JTable table;
    private DefaultTableModel model;

    public AuditLogsPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BG_CANVAS);
        setBorder(new EmptyBorder(14, 18, 14, 18));

        initUI();
        refreshAuditLogs();
    }

    private void initUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Immutable Security & Administrative Audit Trail");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_LIGHT);
        topBar.add(title, BorderLayout.WEST);

        JButton exportBtn = Theme.createPrimaryButton("Export Audit CSV");
        exportBtn.addActionListener(e -> handleExportCsv());
        topBar.add(exportBtn, BorderLayout.EAST);

        // Filter Card
        JPanel filterCard = Theme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        actorField = Theme.createTextField();
        actorField.setPreferredSize(new Dimension(150, 30));
        actorField.setToolTipText("Filter by Actor Username");

        actionFilter = Theme.createComboBox(new String[]{"ALL", "CREATE_INCIDENT", "CLAIM_INCIDENT", "UPDATE_STATUS", "RESOLVE_INCIDENT", "ESCALATE_INCIDENT", "ADD_IOC", "UPLOAD_EVIDENCE", "USER_LOGIN", "FAILED_LOGIN", "ACCOUNT_LOCKED", "UPDATE_SETTING"});
        actionFilter.setPreferredSize(new Dimension(160, 30));

        entityFilter = Theme.createComboBox(new String[]{"ALL", "INCIDENT", "USER", "EVIDENCE", "INDICATOR", "SYSTEM_SETTING", "THREAT_INTEL", "SESSION"});
        entityFilter.setPreferredSize(new Dimension(140, 30));

        JButton applyBtn = Theme.createPrimaryButton("Filter Logs");
        applyBtn.addActionListener(e -> refreshAuditLogs());

        JButton resetBtn = Theme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            actorField.setText("");
            actionFilter.setSelectedIndex(0);
            entityFilter.setSelectedIndex(0);
            refreshAuditLogs();
        });

        filterCard.add(Theme.createFilterLabel("Actor:"));
        filterCard.add(actorField);
        filterCard.add(Theme.createFilterLabel("Action:"));
        filterCard.add(actionFilter);
        filterCard.add(Theme.createFilterLabel("Entity:"));
        filterCard.add(entityFilter);
        filterCard.add(applyBtn);
        filterCard.add(resetBtn);

        // Table Card
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"Log ID", "Timestamp", "Actor", "Action", "Entity", "Entity ID", "Description"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(filterCard, BorderLayout.NORTH);
        center.add(tableCard, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    public void refreshAuditLogs() {
        model.setRowCount(0);

        String act = actorField.getText().trim();
        String action = (String) actionFilter.getSelectedItem();
        String ent = (String) entityFilter.getSelectedItem();

        try {
            List<AuditLog> list = auditDAO.findAll(act, action, ent, 200);
            for (AuditLog l : list) {
                model.addRow(new Object[]{
                    l.getId(),
                    l.getCreatedAt().toString().substring(0, 19),
                    l.getActorUsername(),
                    l.getAction(),
                    l.getEntity(),
                    l.getEntityId() != null ? l.getEntityId() : "—",
                    l.getDescription()
                });
            }
        } catch (SQLException e) {
            System.err.println("[AUDIT] Error loading logs: " + e.getMessage());
        }
    }

    private void handleExportCsv() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("LogID,Timestamp,Actor,Action,Entity,EntityID,Description\n");
            for (int r = 0; r < model.getRowCount(); r++) {
                sb.append(model.getValueAt(r, 0)).append(",")
                  .append("\"").append(model.getValueAt(r, 1)).append("\",")
                  .append("\"").append(model.getValueAt(r, 2)).append("\",")
                  .append("\"").append(model.getValueAt(r, 3)).append("\",")
                  .append("\"").append(model.getValueAt(r, 4)).append("\",")
                  .append("\"").append(model.getValueAt(r, 5)).append("\",")
                  .append("\"").append(String.valueOf(model.getValueAt(r, 6)).replace("\"", "\"\"")).append("\"\n");
            }
            File file = reportService.exportToFile(sb.toString(), "Audit_Trail", "csv");
            JOptionPane.showMessageDialog(this, "Audit logs exported successfully to:\n" + file.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
