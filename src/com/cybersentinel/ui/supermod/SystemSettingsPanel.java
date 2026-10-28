package com.cybersentinel.ui.supermod;

import com.cybersentinel.dao.SettingDAO;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.service.SettingService;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class SystemSettingsPanel extends JPanel {
    private final UserSession session;
    private final SettingService settingService = new SettingService();
    private final SettingDAO settingDAO = new SettingDAO();

    private JTextField slaCrit;
    private JTextField slaHigh;
    private JTextField slaMed;
    private JTextField slaLow;
    private JTextField maxEvidenceMb;
    private JTextField lockoutThreshold;
    private JTextField lockoutMinutes;
    private JTextField passwordMinLength;

    public SystemSettingsPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BG_CANVAS);
        setBorder(new EmptyBorder(14, 18, 14, 18));

        initUI();
        loadSettings();
    }

    private void initUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Enterprise System Configuration & Security Policies");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_LIGHT);
        topBar.add(title, BorderLayout.WEST);

        JButton saveBtn = Theme.createPrimaryButton("Save All Policy Settings");
        saveBtn.addActionListener(e -> handleSaveAll());
        topBar.add(saveBtn, BorderLayout.EAST);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // 1. SLA Policy Card
        JPanel slaCard = Theme.createCardPanel();
        slaCard.setLayout(new GridLayout(4, 2, 10, 8));
        slaCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        slaCrit = Theme.createTextField();
        slaHigh = Theme.createTextField();
        slaMed = Theme.createTextField();
        slaLow = Theme.createTextField();

        slaCard.add(Theme.createFilterLabel("CRITICAL Incident Response Target (Minutes):")); slaCard.add(slaCrit);
        slaCard.add(Theme.createFilterLabel("HIGH Severity Response Target (Minutes):")); slaCard.add(slaHigh);
        slaCard.add(Theme.createFilterLabel("MEDIUM Severity Response Target (Minutes):")); slaCard.add(slaMed);
        slaCard.add(Theme.createFilterLabel("LOW Severity Response Target (Minutes):")); slaCard.add(slaLow);

        // 2. Security & Lockout Policy Card
        JPanel secCard = Theme.createCardPanel();
        secCard.setLayout(new GridLayout(4, 2, 10, 8));
        secCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        maxEvidenceMb = Theme.createTextField();
        lockoutThreshold = Theme.createTextField();
        lockoutMinutes = Theme.createTextField();
        passwordMinLength = Theme.createTextField();

        secCard.add(Theme.createFilterLabel("Maximum Forensic Evidence Upload Size (MB):")); secCard.add(maxEvidenceMb);
        secCard.add(Theme.createFilterLabel("Failed Login Attempts Before Account Lock:")); secCard.add(lockoutThreshold);
        secCard.add(Theme.createFilterLabel("Temporary Lockout Duration (Minutes):")); secCard.add(lockoutMinutes);
        secCard.add(Theme.createFilterLabel("Enforced Minimum Password Length:")); secCard.add(passwordMinLength);

        slaCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        secCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel slaHeader = createSectionHeader("SLA THRESHOLDS & ESCALATION RULES");
        slaHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel secHeader = createSectionHeader("AUTHENTICATION, EVIDENCE & LOCKOUT CONTROLS");
        secHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(slaHeader);
        center.add(Box.createVerticalStrut(4));
        center.add(slaCard);
        center.add(Box.createVerticalStrut(14));
        center.add(secHeader);
        center.add(Box.createVerticalStrut(4));
        center.add(secCard);

        add(topBar, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private JLabel createSectionHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_HEADER);
        l.setForeground(Theme.COLOR_PRIMARY);
        return l;
    }

    public void loadSettings() {
        slaCrit.setText(settingDAO.getSetting("sla_critical_minutes", "15"));
        slaHigh.setText(settingDAO.getSetting("sla_high_minutes", "30"));
        slaMed.setText(settingDAO.getSetting("sla_medium_minutes", "120"));
        slaLow.setText(settingDAO.getSetting("sla_low_minutes", "480"));

        long bytes = Long.parseLong(settingDAO.getSetting("max_evidence_bytes", "20971520"));
        maxEvidenceMb.setText(String.valueOf(bytes / (1024 * 1024)));

        lockoutThreshold.setText(settingDAO.getSetting("lockout_threshold", "5"));
        lockoutMinutes.setText(settingDAO.getSetting("lockout_duration_minutes", "15"));
        passwordMinLength.setText(settingDAO.getSetting("min_password_length", "8"));
    }

    private void handleSaveAll() {
        try {
            settingService.updateSetting("sla_critical_minutes", slaCrit.getText().trim(), session);
            settingService.updateSetting("sla_high_minutes", slaHigh.getText().trim(), session);
            settingService.updateSetting("sla_medium_minutes", slaMed.getText().trim(), session);
            settingService.updateSetting("sla_low_minutes", slaLow.getText().trim(), session);

            long mb = Long.parseLong(maxEvidenceMb.getText().trim());
            settingService.updateSetting("max_evidence_bytes", String.valueOf(mb * 1024 * 1024), session);

            settingService.updateSetting("lockout_threshold", lockoutThreshold.getText().trim(), session);
            settingService.updateSetting("lockout_duration_minutes", lockoutMinutes.getText().trim(), session);
            settingService.updateSetting("min_password_length", passwordMinLength.getText().trim(), session);

            JOptionPane.showMessageDialog(this, "System configuration settings saved successfully.", "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
