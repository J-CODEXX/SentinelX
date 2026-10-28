package com.cybersentinel.ui.common;

import com.cybersentinel.model.*;
import com.cybersentinel.security.RbacService;
import com.cybersentinel.service.EvidenceService;
import com.cybersentinel.service.IncidentService;
import com.cybersentinel.service.IndicatorService;
import com.cybersentinel.service.InvestigationService;
import com.cybersentinel.service.ReportService;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

public class IncidentDetailDialog extends JDialog {
    private final Incident incident;
    private final UserSession session;
    private final InvestigationService investigationService = new InvestigationService();
    private final EvidenceService evidenceService = new EvidenceService();
    private final IndicatorService indicatorService = new IndicatorService();
    private final ReportService reportService = new ReportService();
    private final IncidentService incidentService = new IncidentService();
    private final Runnable onDeleteCallback;

    public IncidentDetailDialog(Window parent, Incident incident, UserSession session) {
        this(parent, incident, session, null);
    }

    public IncidentDetailDialog(Window parent, Incident incident, UserSession session, Runnable onDeleteCallback) {
        super(parent, "Incident Dossier — " + incident.getIncidentCode(), ModalityType.APPLICATION_MODAL);
        this.incident = incident;
        this.session = session;
        this.onDeleteCallback = onDeleteCallback;

        setSize(880, 680);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.BG_CANVAS);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Theme.BG_CANVAS);
        root.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleGrp = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleGrp.setOpaque(false);

        JLabel codeLbl = new JLabel(incident.getIncidentCode());
        codeLbl.setFont(Theme.FONT_TITLE);
        codeLbl.setForeground(Theme.COLOR_PRIMARY);

        titleGrp.add(codeLbl);
        titleGrp.add(Theme.createBadge(incident.getSeverity().name(), incident.getSeverity().getForegroundColor(), incident.getSeverity().getBackgroundColor()));
        titleGrp.add(Theme.createBadge(incident.getStatus().getDisplayName(), incident.getStatus().getForegroundColor(), incident.getStatus().getBackgroundColor()));
        titleGrp.add(Theme.createBadge(incident.getPriority().name(), incident.getPriority().getColor(), new Color(incident.getPriority().getColor().getRed(), incident.getPriority().getColor().getGreen(), incident.getPriority().getColor().getBlue(), 40)));

        if (incident.isSimulated()) {
            titleGrp.add(Theme.createBadge("🔵 SIMULATED", Theme.COLOR_SIMULATED, new Color(88, 166, 255, 40)));
        }

        JLabel titleText = new JLabel(incident.getTitle());
        titleText.setFont(Theme.FONT_HEADER);
        titleText.setForeground(Theme.TEXT_PRIMARY);

        JPanel headBox = new JPanel();
        headBox.setLayout(new BoxLayout(headBox, BoxLayout.Y_AXIS));
        headBox.setOpaque(false);
        headBox.add(titleGrp);
        headBox.add(Box.createVerticalStrut(4));
        headBox.add(titleText);

        header.add(headBox, BorderLayout.WEST);

        JButton exportBtn = Theme.createSecondaryButton("Export Report");
        exportBtn.addActionListener(e -> handleExportReport());
        header.add(exportBtn, BorderLayout.EAST);

        // Center Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Theme.BG_CANVAS);
        tabs.setForeground(Theme.TEXT_PRIMARY);

        tabs.addTab("Overview", buildOverviewPanel());
        tabs.addTab("Visual Timeline", buildTimelinePanel());
        tabs.addTab("Evidence Artifacts", buildEvidencePanel());
        tabs.addTab("IOC Indicators", buildIocPanel());

        // Internal Notes ONLY visible to Security Analysts and Supermods!
        if (RbacService.canViewInternalNotes(session)) {
            tabs.addTab("Internal Notes (SOC)", buildInternalNotesPanel());
        }

        // Bottom Actions
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftActions.setOpaque(false);
        if (session != null && (session.getRole() == Role.SUPERMOD || session.getRole() == Role.ANALYST)) {
            JButton deleteBtn = Theme.createDangerButton("🗑 Delete Incident");
            deleteBtn.addActionListener(e -> handleDeleteIncident());
            leftActions.add(deleteBtn);
        }

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightActions.setOpaque(false);
        JButton closeBtn = Theme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dispose());
        rightActions.add(closeBtn);

        bottom.add(leftActions, BorderLayout.WEST);
        bottom.add(rightActions, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        add(root);
    }

    private void handleDeleteIncident() {
        int res = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete incident " + incident.getIncidentCode() + "?\n\n" +
            "Title: " + incident.getTitle() + "\n" +
            "This will delete all investigation notes, indicators, evidence links, and timeline history.\n" +
            "This action cannot be undone.",
            "Confirm Incident Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (res == JOptionPane.YES_OPTION) {
            try {
                incidentService.deleteIncident(incident.getId(), session);
                dispose();
                JOptionPane.showMessageDialog(null, "Incident " + incident.getIncidentCode() + " deleted successfully.", "Incident Deleted", JOptionPane.INFORMATION_MESSAGE);
                if (onDeleteCallback != null) {
                    onDeleteCallback.run();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete incident: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel buildOverviewPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BG_CARD);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Metadata Grid
        JPanel meta = new JPanel(new GridLayout(2, 3, 12, 10));
        meta.setOpaque(false);
        meta.add(createInfoBlock("Reported By", incident.getReportedByName()));
        meta.add(createInfoBlock("Assigned Analyst", incident.getAssignedToName()));
        meta.add(createInfoBlock("Affected System", incident.getAffectedSystem()));
        meta.add(createInfoBlock("Risk Score", incident.getRiskScore() + " / 100 (" + incident.getPriority().getLabel() + ")"));
        meta.add(createInfoBlock("Classification", incident.getType()));
        meta.add(createInfoBlock("Created At", String.valueOf(incident.getCreatedAt())));

        meta.setAlignmentX(Component.LEFT_ALIGNMENT);
        meta.setMaximumSize(new Dimension(Integer.MAX_VALUE, meta.getPreferredSize().height + 20));

        panel.add(meta);
        panel.add(Box.createVerticalStrut(14));

        // Description
        JLabel descHeader = createSectionHeader("Incident Description");
        descHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descHeader);
        panel.add(Box.createVerticalStrut(4));
        JTextArea descArea = Theme.createTextArea(4, 50);
        descArea.setText(incident.getDescription());
        descArea.setEditable(false);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descScroll);
        panel.add(Box.createVerticalStrut(14));

        // Resolution Summary
        JLabel resHeader = createSectionHeader("Resolution & User Recommendations");
        resHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(resHeader);
        panel.add(Box.createVerticalStrut(4));

        String resText = "Status: " + incident.getStatus().getDisplayName() + "\n\n";
        if (incident.getResolutionSummary() != null && !incident.getResolutionSummary().isBlank()) {
            resText += "Resolution Summary:\n" + incident.getResolutionSummary() + "\n\n";
        }
        if (incident.getRecommendations() != null && !incident.getRecommendations().isBlank()) {
            resText += "Safety Recommendations:\n" + incident.getRecommendations();
        }
        if (incident.getResolutionSummary() == null && incident.getRecommendations() == null) {
            resText += "This incident is currently undergoing triage and active investigation by the SOC response team.";
        }

        JTextArea resArea = Theme.createTextArea(5, 50);
        resArea.setText(resText);
        resArea.setEditable(false);
        JScrollPane resScroll = new JScrollPane(resArea);
        resScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(resScroll);

        return panel;
    }

    private JPanel buildTimelinePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG_CARD);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        DefaultListModel<String> model = new DefaultListModel<>();
        try {
            List<IncidentHistory> list = investigationService.getHistory(incident.getId(), session);
            for (IncidentHistory h : list) {
                model.addElement(String.format("[%s]  (%s)  %s:  %s", 
                    h.getCreatedAt().toString().substring(0, 19), h.getActorName(), h.getEvent(), h.getDescription()));
            }
        } catch (Exception e) {
            model.addElement("Failed to load timeline: " + e.getMessage());
        }

        JList<String> jList = new JList<>(model);
        jList.setBackground(Theme.BG_INPUT);
        jList.setForeground(Theme.TEXT_PRIMARY);
        jList.setFont(Theme.FONT_MONO);
        panel.add(new JScrollPane(jList), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildEvidencePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG_CARD);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        String[] cols = {"File Name", "Size", "SHA-256 Hash", "Uploaded By", "Date"};
        DefaultListModel<String> model = new DefaultListModel<>();
        try {
            List<Evidence> list = evidenceService.getEvidenceForIncident(incident.getId(), session);
            for (Evidence ev : list) {
                model.addElement(String.format("%s (%s) | SHA-256: %s | Uploaded by: %s at %s",
                    ev.getOriginalFilename(), ev.getFormattedSize(), ev.getSha256(), ev.getUploadedByName(), ev.getCreatedAt()));
            }
        } catch (Exception e) {
            model.addElement("Failed to load evidence: " + e.getMessage());
        }

        JList<String> jList = new JList<>(model);
        jList.setBackground(Theme.BG_INPUT);
        jList.setForeground(Theme.TEXT_PRIMARY);
        jList.setFont(Theme.FONT_MONO);
        panel.add(new JScrollPane(jList), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildIocPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG_CARD);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        DefaultListModel<String> model = new DefaultListModel<>();
        try {
            List<Indicator> list = indicatorService.getIndicators(incident.getId());
            for (Indicator i : list) {
                String matchStr = i.isThreatIntelMatch() ? " ⚠ [THREAT INTEL MATCH: " + i.getClassification() + " / " + i.getThreatLevel() + "]" : "";
                model.addElement(String.format("[%s] %s%s - %s", i.getType(), i.getValue(), matchStr, i.getDescription()));
            }
        } catch (Exception e) {
            model.addElement("Failed to load IOCs: " + e.getMessage());
        }

        JList<String> jList = new JList<>(model);
        jList.setBackground(Theme.BG_INPUT);
        jList.setForeground(Theme.TEXT_PRIMARY);
        jList.setFont(Theme.FONT_MONO);
        panel.add(new JScrollPane(jList), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildInternalNotesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG_CARD);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        DefaultListModel<String> model = new DefaultListModel<>();
        try {
            List<InvestigationNote> list = investigationService.getNotes(incident.getId(), session);
            for (InvestigationNote n : list) {
                model.addElement(String.format("[%s] %s: %s", n.getCreatedAt().toString().substring(0, 19), n.getAnalystName(), n.getNote()));
            }
        } catch (Exception e) {
            model.addElement("Failed to load notes: " + e.getMessage());
        }

        JList<String> jList = new JList<>(model);
        jList.setBackground(Theme.BG_INPUT);
        jList.setForeground(Theme.TEXT_PRIMARY);
        jList.setFont(Theme.FONT_MONO);
        panel.add(new JScrollPane(jList), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInfoBlock(String title, String value) {
        JPanel p = new JPanel(new BorderLayout(2, 2));
        p.setOpaque(false);
        JLabel t = new JLabel(title.toUpperCase());
        t.setFont(new Font("Segoe UI", Font.BOLD, 10));
        t.setForeground(Theme.TEXT_MUTED);
        JLabel v = new JLabel(value != null ? value : "—");
        v.setFont(Theme.FONT_REGULAR);
        v.setForeground(Theme.TEXT_PRIMARY);
        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private JLabel createSectionHeader(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BOLD);
        lbl.setForeground(Theme.COLOR_PRIMARY);
        return lbl;
    }

    private void handleExportReport() {
        try {
            String dossier = reportService.generateIncidentDossier(incident.getId());
            File file = reportService.exportToFile(dossier, "Dossier_" + incident.getIncidentCode(), "txt");
            JOptionPane.showMessageDialog(this,
                "Incident Dossier successfully exported to:\n" + file.getAbsolutePath(),
                "Report Exported", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
