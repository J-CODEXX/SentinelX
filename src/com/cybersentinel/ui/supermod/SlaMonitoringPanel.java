package com.cybersentinel.ui.supermod;

import com.cybersentinel.dao.IncidentDAO;
import com.cybersentinel.dao.SettingDAO;
import com.cybersentinel.model.Incident;
import com.cybersentinel.model.Severity;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.service.SettingService;
import com.cybersentinel.service.SlaService;
import com.cybersentinel.ui.common.StatusBadgeRenderer;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class SlaMonitoringPanel extends JPanel {
    private final UserSession session;
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final SlaService slaService = new SlaService();
    private final SettingService settingService = new SettingService();
    private final SettingDAO settingDAO = new SettingDAO();

    private JTextField critField;
    private JTextField highField;
    private JTextField medField;
    private JTextField lowField;

    private JTable slaTable;
    private DefaultTableModel slaModel;

    public SlaMonitoringPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BG_CANVAS);
        setBorder(new EmptyBorder(14, 18, 14, 18));

        initUI();
        refreshSlaView();
    }

    private void initUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Service Level Agreement (SLA) Real-Time Monitoring & Policy");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_LIGHT);
        topBar.add(title, BorderLayout.WEST);

        JButton refreshBtn = Theme.createSecondaryButton("🔄 Refresh Status");
        refreshBtn.addActionListener(e -> refreshSlaView());
        topBar.add(refreshBtn, BorderLayout.EAST);

        // Threshold Configuration Card
        JPanel configCard = Theme.createCardPanel();
        configCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        JLabel cTitle = new JLabel("Configured SLA Targets (Minutes):");
        cTitle.setFont(Theme.FONT_BOLD);
        cTitle.setForeground(Theme.TEXT_PRIMARY);

        critField = Theme.createTextField(); critField.setPreferredSize(new Dimension(50, 28));
        highField = Theme.createTextField(); highField.setPreferredSize(new Dimension(50, 28));
        medField = Theme.createTextField(); medField.setPreferredSize(new Dimension(50, 28));
        lowField = Theme.createTextField(); lowField.setPreferredSize(new Dimension(50, 28));

        JButton saveTargetsBtn = Theme.createPrimaryButton("Save Targets");
        saveTargetsBtn.addActionListener(e -> handleSaveTargets());

        configCard.add(cTitle);
        configCard.add(Theme.createFilterLabel("Critical:")); configCard.add(critField);
        configCard.add(Theme.createFilterLabel("High:")); configCard.add(highField);
        configCard.add(Theme.createFilterLabel("Medium:")); configCard.add(medField);
        configCard.add(Theme.createFilterLabel("Low:")); configCard.add(lowField);
        configCard.add(saveTargetsBtn);

        // SLA Table Card
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"Incident ID", "Title", "Severity", "Current Status", "Analyst", "SLA Status", "Target Time", "Elapsed Time", "Time Remaining"};
        slaModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        slaTable = new JTable(slaModel);
        Theme.styleTable(slaTable);
        slaTable.getColumnModel().getColumn(2).setCellRenderer(new StatusBadgeRenderer());
        slaTable.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeRenderer());
        slaTable.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        tableCard.add(new JScrollPane(slaTable), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(configCard, BorderLayout.NORTH);
        center.add(tableCard, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    public void refreshSlaView() {
        // Load targets
        critField.setText(settingDAO.getSetting("sla_critical_minutes", "15"));
        highField.setText(settingDAO.getSetting("sla_high_minutes", "30"));
        medField.setText(settingDAO.getSetting("sla_medium_minutes", "120"));
        lowField.setText(settingDAO.getSetting("sla_low_minutes", "480"));

        slaModel.setRowCount(0);
        try {
            List<Incident> list = incidentDAO.findAll(null, null, null, null, null, null, null, null);
            for (Incident inc : list) {
                SlaService.SlaInfo sla = slaService.calculateSla(inc);
                slaModel.addRow(new Object[]{
                    inc.getIncidentCode(),
                    inc.getTitle(),
                    inc.getSeverity().name(),
                    inc.getStatus().getDisplayName(),
                    inc.getAssignedToName(),
                    sla.getStatus().name(),
                    sla.getTargetMinutes() + " mins",
                    sla.getElapsedMinutes() + " mins",
                    sla.getFormattedRemaining()
                });
            }
        } catch (SQLException e) {
            System.err.println("[SLA_MON] Error: " + e.getMessage());
        }
    }

    private void handleSaveTargets() {
        try {
            settingService.updateSetting("sla_critical_minutes", critField.getText().trim(), session);
            settingService.updateSetting("sla_high_minutes", highField.getText().trim(), session);
            settingService.updateSetting("sla_medium_minutes", medField.getText().trim(), session);
            settingService.updateSetting("sla_low_minutes", lowField.getText().trim(), session);

            refreshSlaView();
            JOptionPane.showMessageDialog(this, "SLA policy thresholds updated successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save settings: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
