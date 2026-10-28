package com.cybersentinel.ui.supermod;

import com.cybersentinel.dao.AnalyticsDAO;
import com.cybersentinel.dao.IncidentDAO;
import com.cybersentinel.dao.UserDAO;
import com.cybersentinel.engine.SimulationEngine;
import com.cybersentinel.model.*;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class SimulationCenterPanel extends JPanel {
    private final UserSession session;
    private final UserDAO userDAO = new UserDAO();
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();

    private JComboBox<String> typeCb;
    private JComboBox<String> sevCb;
    private JComboBox<Object> analystCb;

    private JSpinner intervalSpinner;
    private JButton startLiveBtn;
    private JButton stopLiveBtn;
    private JLabel liveStatusLabel;

    private JLabel genCounter;
    private JLabel activeCounter;
    private JLabel resolvedCounter;
    private JLabel critCounter;

    public SimulationCenterPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG_CANVAS);
        setBorder(new EmptyBorder(14, 18, 14, 18));

        initUI();
        refreshCounters();

        SimulationEngine.addCountListener(count -> SwingUtilities.invokeLater(this::refreshCounters));
    }

    private void initUI() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // Header
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("Cybersecurity Red-Team Attack & Incident Simulation Engine");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_LIGHT);

        JLabel sub = new JLabel("Simulate real-world attacks to train SOC analysts and validate response automation.");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_MUTED);

        JPanel headBox = new JPanel(new GridLayout(2, 1, 0, 2));
        headBox.setOpaque(false);
        headBox.add(title);
        headBox.add(sub);
        head.add(headBox, BorderLayout.WEST);

        // Live Metric Counters Card
        JPanel statsCard = Theme.createCardPanel();
        statsCard.setLayout(new GridLayout(1, 4, 12, 0));
        statsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        genCounter = new JLabel("0", SwingConstants.CENTER);
        genCounter.setFont(new Font("Segoe UI", Font.BOLD, 24));
        genCounter.setForeground(Theme.COLOR_SIMULATED);

        activeCounter = new JLabel("0", SwingConstants.CENTER);
        activeCounter.setFont(new Font("Segoe UI", Font.BOLD, 24));
        activeCounter.setForeground(Theme.COLOR_WARNING);

        resolvedCounter = new JLabel("0", SwingConstants.CENTER);
        resolvedCounter.setFont(new Font("Segoe UI", Font.BOLD, 24));
        resolvedCounter.setForeground(Theme.COLOR_SUCCESS);

        critCounter = new JLabel("0", SwingConstants.CENTER);
        critCounter.setFont(new Font("Segoe UI", Font.BOLD, 24));
        critCounter.setForeground(Theme.COLOR_DANGER);

        statsCard.add(createCounterBlock("Generated Simulations", genCounter));
        statsCard.add(createCounterBlock("Active Simulated Cases", activeCounter));
        statsCard.add(createCounterBlock("Resolved by SOC", resolvedCounter));
        statsCard.add(createCounterBlock("Critical Simulations", critCounter));

        // Two Split Columns: Manual Generator (Left) + Live Generator (Right)
        JPanel split = new JPanel(new GridLayout(1, 2, 12, 0));
        split.setOpaque(false);

        // 1. Manual Generator Card
        JPanel manualCard = Theme.createCardPanel();
        manualCard.setLayout(new BoxLayout(manualCard, BoxLayout.Y_AXIS));

        JLabel mTitle = new JLabel("⚡ TARGETED INCIDENT GENERATOR");
        mTitle.setFont(Theme.FONT_HEADER);
        mTitle.setForeground(Theme.COLOR_PRIMARY);

        JLabel mSub = new JLabel("Generate an immediate realistic incident through the full SOC pipeline.");
        mSub.setFont(Theme.FONT_SMALL);
        mSub.setForeground(Theme.TEXT_MUTED);

        mTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        typeCb = Theme.createComboBox(new String[]{"RANDOM", "Phishing", "Ransomware", "Malware", "Account Compromise", "Data Breach", "Network Attack", "Unauthorized Access"});
        typeCb.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeCb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        sevCb = Theme.createComboBox(new String[]{"RANDOM", "CRITICAL", "HIGH", "MEDIUM", "LOW"});
        sevCb.setAlignmentX(Component.LEFT_ALIGNMENT);
        sevCb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        analystCb = new JComboBox<>();
        analystCb.setBackground(Theme.BG_INPUT);
        analystCb.setForeground(Theme.TEXT_PRIMARY);
        analystCb.setAlignmentX(Component.LEFT_ALIGNMENT);
        analystCb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        analystCb.addItem("Automatic / Unassigned");
        try {
            for (User u : userDAO.findAllAnalysts()) {
                analystCb.addItem(u);
            }
        } catch (Exception ignored) {}

        JButton triggerBtn = Theme.createPrimaryButton("⚡ GENERATE SINGLE INCIDENT");
        triggerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        triggerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        triggerBtn.addActionListener(e -> handleManualTrigger());

        JLabel mTypeLbl = new JLabel("Incident Category:");
        mTypeLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        mTypeLbl.setForeground(Theme.TEXT_MUTED);
        mTypeLbl.setFont(Theme.FONT_SMALL);

        JLabel mSevLbl = new JLabel("Target Severity:");
        mSevLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        mSevLbl.setForeground(Theme.TEXT_MUTED);
        mSevLbl.setFont(Theme.FONT_SMALL);

        JLabel mAnalystLbl = new JLabel("Analyst Assignment:");
        mAnalystLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        mAnalystLbl.setForeground(Theme.TEXT_MUTED);
        mAnalystLbl.setFont(Theme.FONT_SMALL);

        manualCard.add(mTitle);
        manualCard.add(Box.createVerticalStrut(2));
        manualCard.add(mSub);
        manualCard.add(Box.createVerticalStrut(14));

        manualCard.add(mTypeLbl);
        manualCard.add(typeCb);
        manualCard.add(Box.createVerticalStrut(8));

        manualCard.add(mSevLbl);
        manualCard.add(sevCb);
        manualCard.add(Box.createVerticalStrut(8));

        manualCard.add(mAnalystLbl);
        manualCard.add(analystCb);
        manualCard.add(Box.createVerticalStrut(16));

        manualCard.add(triggerBtn);

        // 2. Continuous Live Simulation Card
        JPanel liveCard = Theme.createCardPanel();
        liveCard.setLayout(new BoxLayout(liveCard, BoxLayout.Y_AXIS));

        JLabel lTitle = new JLabel("🔄 LIVE BACKGROUND INCIDENT STREAM");
        lTitle.setFont(Theme.FONT_HEADER);
        lTitle.setForeground(Theme.COLOR_SIMULATED);

        JLabel lSub = new JLabel("Stream simulated threat scenarios periodically in background worker thread.");
        lSub.setFont(Theme.FONT_SMALL);
        lSub.setForeground(Theme.TEXT_MUTED);

        JPanel intRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        intRow.setOpaque(false);
        intRow.add(Theme.createFilterLabel("Generation Interval (Seconds):"));
        intervalSpinner = new JSpinner(new SpinnerNumberModel(15, 5, 120, 5));
        intervalSpinner.setPreferredSize(new Dimension(70, 30));
        intRow.add(intervalSpinner);

        liveStatusLabel = new JLabel("Status: STOPPED (Idle)");
        liveStatusLabel.setFont(Theme.FONT_BOLD);
        liveStatusLabel.setForeground(Theme.TEXT_MUTED);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        startLiveBtn = Theme.createSuccessButton("▶ START LIVE SIMULATION");
        stopLiveBtn = Theme.createDangerButton("⏹ STOP LIVE SIMULATION");
        stopLiveBtn.setEnabled(false);

        startLiveBtn.addActionListener(e -> handleStartLive());
        stopLiveBtn.addActionListener(e -> handleStopLive());

        btnRow.add(startLiveBtn);
        btnRow.add(stopLiveBtn);

        lTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        lSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        intRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        liveStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        liveCard.add(lTitle);
        liveCard.add(Box.createVerticalStrut(2));
        liveCard.add(lSub);
        liveCard.add(Box.createVerticalStrut(14));
        liveCard.add(intRow);
        liveCard.add(Box.createVerticalStrut(12));
        liveCard.add(liveStatusLabel);
        liveCard.add(Box.createVerticalStrut(16));
        liveCard.add(btnRow);

        split.add(manualCard);
        split.add(liveCard);

        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        split.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(head);
        center.add(Box.createVerticalStrut(10));
        center.add(statsCard);
        center.add(Box.createVerticalStrut(12));
        center.add(split);

        add(center, BorderLayout.CENTER);
    }

    private JPanel createCounterBlock(String title, JLabel valLabel) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel t = new JLabel(title.toUpperCase(), SwingConstants.CENTER);
        t.setFont(new Font("Segoe UI", Font.BOLD, 10));
        t.setForeground(Theme.TEXT_MUTED);
        p.add(t, BorderLayout.NORTH);
        p.add(valLabel, BorderLayout.CENTER);
        return p;
    }

    public void refreshCounters() {
        genCounter.setText(String.valueOf(SimulationEngine.getGeneratedCount()));

        try {
            List<Incident> list = incidentDAO.findAll(null, null, null, null, true, null, null, null);
            int active = 0;
            int resolved = 0;
            int crit = 0;

            for (Incident i : list) {
                if (i.getStatus().isActive()) active++;
                else resolved++;
                if (i.getSeverity() == Severity.CRITICAL) crit++;
            }

            activeCounter.setText(String.valueOf(active));
            resolvedCounter.setText(String.valueOf(resolved));
            critCounter.setText(String.valueOf(crit));
        } catch (Exception ignored) {}
    }

    private void handleManualTrigger() {
        String type = (String) typeCb.getSelectedItem();
        String sevStr = (String) sevCb.getSelectedItem();
        Severity sev = !"RANDOM".equalsIgnoreCase(sevStr) ? Severity.fromString(sevStr) : null;

        Long analystId = null;
        Object sel = analystCb.getSelectedItem();
        if (sel instanceof User u) {
            analystId = u.getId();
        }

        try {
            Incident sim = SimulationEngine.simulateIncident(type, sev, analystId, session);
            refreshCounters();
            JOptionPane.showMessageDialog(this,
                "<html><b>⚡ Simulated Incident Generated!</b><br><br>" +
                "Incident ID: <font color='#388BFD'><b>" + sim.getIncidentCode() + "</b></font><br>" +
                "Title: <b>" + sim.getTitle() + "</b><br>" +
                "Category: <b>" + sim.getType() + "</b><br>" +
                "Severity: <b>" + sim.getSeverity().name() + "</b><br>" +
                "Risk Score: <b>" + sim.getRiskScore() + " / 100</b><br><br>" +
                "The incident is now active in the SOC queue.</html>",
                "Simulated Scenario Created", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Generation failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleStartLive() {
        int interval = (Integer) intervalSpinner.getValue();
        SimulationEngine.startLiveSimulation(interval, session);
        startLiveBtn.setEnabled(false);
        stopLiveBtn.setEnabled(true);
        intervalSpinner.setEnabled(false);
        liveStatusLabel.setText("Status: RUNNING (Every " + interval + "s)");
        liveStatusLabel.setForeground(Theme.COLOR_SUCCESS);
    }

    private void handleStopLive() {
        SimulationEngine.stopLiveSimulation();
        startLiveBtn.setEnabled(true);
        stopLiveBtn.setEnabled(false);
        intervalSpinner.setEnabled(true);
        liveStatusLabel.setText("Status: STOPPED (Idle)");
        liveStatusLabel.setForeground(Theme.TEXT_MUTED);
    }
}
