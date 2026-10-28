package com.cybersentinel.ui.analyst;

import com.cybersentinel.engine.ClassificationEngine;
import com.cybersentinel.model.*;
import com.cybersentinel.service.*;
import com.cybersentinel.ui.common.StatusBadgeRenderer;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class InvestigationWorkspacePanel extends JPanel {
    private final UserSession session;
    private Incident incident;
    private final IncidentService incidentService = new IncidentService();
    private final InvestigationService investigationService = new InvestigationService();
    private final EvidenceService evidenceService = new EvidenceService();
    private final IndicatorService indicatorService = new IndicatorService();
    private final SlaService slaService = new SlaService();

    // Top Header Components
    private JLabel codeLabel;
    private JLabel titleLabel;
    private JLabel statusBadge;
    private JLabel severityBadge;
    private JLabel priorityBadge;
    private JLabel slaBadge;
    private JLabel simBadge;

    // Left Column Components
    private JLabel riskScoreLabel;
    private JLabel riskLevelLabel;
    private JLabel classifSuggestLabel;
    private JLabel classifReasonLabel;
    private JLabel reporterLabel;
    private JLabel systemLabel;
    private JLabel createdLabel;
    private JTextArea descArea;

    // Center Column Components
    private JPanel checklistContainer;
    private DefaultTableModel iocTableModel;
    private JTable iocTable;
    private JPanel threatIntelBanner;
    private JLabel threatIntelBannerText;
    private DefaultTableModel evidenceTableModel;
    private JTable evidenceTable;

    // Right Column Components
    private DefaultListModel<String> notesModel;
    private JTextArea newNoteArea;
    private DefaultListModel<String> timelineModel;

    public InvestigationWorkspacePanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BG_CANVAS);
        setBorder(new EmptyBorder(12, 16, 12, 16));

        initUI();
    }

    private void initUI() {
        // 1. Top Header Bar
        JPanel topHeader = Theme.createCardPanel();
        topHeader.setLayout(new BorderLayout(10, 0));

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTitle.setOpaque(false);

        codeLabel = new JLabel("INC-XXXXXXXX-XXXX");
        codeLabel.setFont(Theme.FONT_TITLE);
        codeLabel.setForeground(Theme.COLOR_PRIMARY);

        titleLabel = new JLabel("Select an incident to investigate");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);

        statusBadge = Theme.createBadge("OPEN", Theme.COLOR_SIMULATED, new Color(88, 166, 255, 30));
        severityBadge = Theme.createBadge("MEDIUM", Theme.COLOR_WARNING, new Color(210, 153, 34, 30));
        priorityBadge = Theme.createBadge("P3", Theme.COLOR_WARNING, new Color(210, 153, 34, 30));
        slaBadge = Theme.createBadge("Within SLA", Theme.COLOR_SUCCESS, new Color(46, 160, 67, 30));
        simBadge = Theme.createBadge("🔵 SIMULATED", Theme.COLOR_SIMULATED, new Color(88, 166, 255, 40));
        simBadge.setVisible(false);

        leftTitle.add(codeLabel);
        leftTitle.add(titleLabel);
        leftTitle.add(statusBadge);
        leftTitle.add(severityBadge);
        leftTitle.add(priorityBadge);
        leftTitle.add(slaBadge);
        leftTitle.add(simBadge);

        // Action Buttons on Top Bar
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);

        JButton startBtn = Theme.createPrimaryButton("▶ Start Investigation");
        startBtn.addActionListener(e -> updateStatus(IncidentStatus.INVESTIGATING, "Analyst initiated active investigation"));

        JButton containBtn = Theme.createWarningButton("🛡️ Contain");
        containBtn.addActionListener(e -> updateStatus(IncidentStatus.CONTAINED, "Host/network containment applied"));

        JButton remediateBtn = Theme.createSecondaryButton("🔧 Remediate");
        remediateBtn.addActionListener(e -> updateStatus(IncidentStatus.REMEDIATED, "Malicious artifacts removed"));

        JButton resolveBtn = Theme.createSuccessButton("✅ Resolve");
        resolveBtn.addActionListener(e -> openResolutionDialog());

        JButton escalateBtn = Theme.createDangerButton("🚨 Escalate");
        escalateBtn.addActionListener(e -> openEscalateDialog());

        JButton sevBtn = Theme.createSecondaryButton("Change Severity");
        sevBtn.addActionListener(e -> openChangeSeverityDialog());

        actions.add(startBtn);
        actions.add(containBtn);
        actions.add(remediateBtn);
        actions.add(resolveBtn);
        actions.add(escalateBtn);
        actions.add(sevBtn);

        topHeader.add(leftTitle, BorderLayout.WEST);
        topHeader.add(actions, BorderLayout.EAST);

        add(topHeader, BorderLayout.NORTH);

        // 2. Main 3-Column Cockpit Split
        JPanel grid = new JPanel(new GridLayout(1, 3, 10, 0));
        grid.setOpaque(false);

        grid.add(buildLeftColumn());
        grid.add(buildCenterColumn());
        grid.add(buildRightColumn());

        add(grid, BorderLayout.CENTER);
    }

    private JPanel buildLeftColumn() {
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        // Risk Score Card
        JPanel riskCard = Theme.createCardPanel();
        riskCard.setLayout(new BorderLayout(5, 5));
        riskCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel rTitle = new JLabel("RISK SCORING ENGINE");
        rTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        rTitle.setForeground(Theme.TEXT_MUTED);

        riskScoreLabel = new JLabel("0 / 100");
        riskScoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        riskScoreLabel.setForeground(Theme.COLOR_PRIMARY);

        riskLevelLabel = new JLabel("LOW • P4");
        riskLevelLabel.setFont(Theme.FONT_BOLD);
        riskLevelLabel.setForeground(Theme.COLOR_SUCCESS);

        JButton recalcBtn = Theme.createSecondaryButton("Recalculate");
        recalcBtn.setFont(Theme.FONT_SMALL);
        recalcBtn.addActionListener(e -> handleRecalculateRisk());

        JPanel rLeft = new JPanel(new GridLayout(2, 1));
        rLeft.setOpaque(false);
        rLeft.add(riskScoreLabel);
        rLeft.add(riskLevelLabel);

        riskCard.add(rTitle, BorderLayout.NORTH);
        riskCard.add(rLeft, BorderLayout.CENTER);
        riskCard.add(recalcBtn, BorderLayout.EAST);

        // Classification Engine Card
        JPanel classCard = Theme.createCardPanel();
        classCard.setLayout(new BoxLayout(classCard, BoxLayout.Y_AXIS));
        classCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel cTitle = new JLabel("AUTOMATED CLASSIFICATION ENGINE");
        cTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        cTitle.setForeground(Theme.TEXT_MUTED);

        classifSuggestLabel = new JLabel("Suggested: —");
        classifSuggestLabel.setFont(Theme.FONT_BOLD);
        classifSuggestLabel.setForeground(Theme.COLOR_SIMULATED);

        classifReasonLabel = new JLabel("Confidence: —");
        classifReasonLabel.setFont(Theme.FONT_SMALL);
        classifReasonLabel.setForeground(Theme.TEXT_MUTED);

        classCard.add(cTitle);
        classCard.add(Box.createVerticalStrut(4));
        classCard.add(classifSuggestLabel);
        classCard.add(Box.createVerticalStrut(2));
        classCard.add(classifReasonLabel);

        // Metadata Card
        JPanel metaCard = Theme.createCardPanel();
        metaCard.setLayout(new GridLayout(4, 1, 0, 4));
        metaCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        reporterLabel = new JLabel("Reporter: —");
        systemLabel = new JLabel("Affected System: —");
        createdLabel = new JLabel("Reported Time: —");

        reporterLabel.setFont(Theme.FONT_REGULAR); reporterLabel.setForeground(Theme.TEXT_PRIMARY);
        systemLabel.setFont(Theme.FONT_REGULAR); systemLabel.setForeground(Theme.TEXT_PRIMARY);
        createdLabel.setFont(Theme.FONT_REGULAR); createdLabel.setForeground(Theme.TEXT_PRIMARY);

        metaCard.add(new JLabel("INCIDENT METADATA"));
        metaCard.add(reporterLabel);
        metaCard.add(systemLabel);
        metaCard.add(createdLabel);

        // Description Card
        JPanel descCard = Theme.createCardPanel();
        descCard.setLayout(new BorderLayout(0, 4));
        JLabel dTitle = new JLabel("DESCRIPTION & OBSERVED SYMPTOMS");
        dTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        dTitle.setForeground(Theme.TEXT_MUTED);

        descArea = Theme.createTextArea(6, 30);
        descArea.setEditable(false);
        descCard.add(dTitle, BorderLayout.NORTH);
        descCard.add(new JScrollPane(descArea), BorderLayout.CENTER);

        left.add(riskCard);
        left.add(Box.createVerticalStrut(8));
        left.add(classCard);
        left.add(Box.createVerticalStrut(8));
        left.add(metaCard);
        left.add(Box.createVerticalStrut(8));
        left.add(descCard);

        return left;
    }

    private JPanel buildCenterColumn() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // Threat Intelligence Match Banner (Conditional Alert)
        threatIntelBanner = new JPanel(new BorderLayout());
        threatIntelBanner.setBackground(new Color(248, 81, 73, 50));
        threatIntelBanner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.COLOR_DANGER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        threatIntelBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        threatIntelBanner.setVisible(false);

        threatIntelBannerText = new JLabel("⚠ THREAT INTELLIGENCE MATCH DETECTED!");
        threatIntelBannerText.setFont(Theme.FONT_BOLD);
        threatIntelBannerText.setForeground(Theme.COLOR_DANGER);
        threatIntelBanner.add(threatIntelBannerText, BorderLayout.CENTER);

        // Response Recommendations Checklist Card
        JPanel checkCard = Theme.createCardPanel();
        checkCard.setLayout(new BorderLayout(0, 6));
        checkCard.setPreferredSize(new Dimension(0, 180));

        JLabel chkTitle = new JLabel("RESPONSE ACTION CHECKLIST");
        chkTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        chkTitle.setForeground(Theme.TEXT_MUTED);

        checklistContainer = new JPanel();
        checklistContainer.setLayout(new BoxLayout(checklistContainer, BoxLayout.Y_AXIS));
        checklistContainer.setOpaque(false);

        JScrollPane chkScroll = new JScrollPane(checklistContainer);
        chkScroll.setBorder(null);
        chkScroll.setOpaque(false);
        chkScroll.getViewport().setOpaque(false);

        checkCard.add(chkTitle, BorderLayout.NORTH);
        checkCard.add(chkScroll, BorderLayout.CENTER);

        // IOC Indicators Card
        JPanel iocCard = Theme.createCardPanel();
        iocCard.setLayout(new BorderLayout(0, 6));
        iocCard.setPreferredSize(new Dimension(0, 180));

        JPanel iocHead = new JPanel(new BorderLayout());
        iocHead.setOpaque(false);
        JLabel iocTitle = new JLabel("INDICATORS OF COMPROMISE (IOC)");
        iocTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        iocTitle.setForeground(Theme.TEXT_MUTED);

        JButton addIocBtn = Theme.createSecondaryButton("+ Add IOC");
        addIocBtn.setFont(Theme.FONT_SMALL);
        addIocBtn.addActionListener(e -> openAddIocDialog());
        iocHead.add(iocTitle, BorderLayout.WEST);
        iocHead.add(addIocBtn, BorderLayout.EAST);

        String[] iocCols = {"Type", "Value", "Class", "Threat", "Match"};
        iocTableModel = new DefaultTableModel(iocCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        iocTable = new JTable(iocTableModel);
        Theme.styleTable(iocTable);
        iocTable.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());

        iocCard.add(iocHead, BorderLayout.NORTH);
        iocCard.add(new JScrollPane(iocTable), BorderLayout.CENTER);

        // Evidence Card
        JPanel evCard = Theme.createCardPanel();
        evCard.setLayout(new BorderLayout(0, 6));
        evCard.setPreferredSize(new Dimension(0, 160));

        JPanel evHead = new JPanel(new BorderLayout());
        evHead.setOpaque(false);
        JLabel evTitle = new JLabel("EVIDENCE ARTIFACTS & CHECKSUMS");
        evTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        evTitle.setForeground(Theme.TEXT_MUTED);

        JButton addEvBtn = Theme.createSecondaryButton("+ Upload Evidence");
        addEvBtn.setFont(Theme.FONT_SMALL);
        addEvBtn.addActionListener(e -> handleUploadEvidence());
        evHead.add(evTitle, BorderLayout.WEST);
        evHead.add(addEvBtn, BorderLayout.EAST);

        String[] evCols = {"File Name", "Size", "SHA-256", "By"};
        evidenceTableModel = new DefaultTableModel(evCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        evidenceTable = new JTable(evidenceTableModel);
        Theme.styleTable(evidenceTable);

        evCard.add(evHead, BorderLayout.NORTH);
        evCard.add(new JScrollPane(evidenceTable), BorderLayout.CENTER);

        center.add(threatIntelBanner);
        center.add(Box.createVerticalStrut(6));
        center.add(checkCard);
        center.add(Box.createVerticalStrut(6));
        center.add(iocCard);
        center.add(Box.createVerticalStrut(6));
        center.add(evCard);

        return center;
    }

    private JPanel buildRightColumn() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        // Investigation Notes History Card
        JPanel notesCard = Theme.createCardPanel();
        notesCard.setLayout(new BorderLayout(0, 6));
        notesCard.setPreferredSize(new Dimension(0, 260));

        JLabel nTitle = new JLabel("INVESTIGATION NOTES HISTORY");
        nTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        nTitle.setForeground(Theme.TEXT_MUTED);

        notesModel = new DefaultListModel<>();
        JList<String> notesList = new JList<>(notesModel);
        notesList.setBackground(Theme.BG_INPUT);
        notesList.setForeground(Theme.TEXT_PRIMARY);
        notesList.setFont(Theme.FONT_REGULAR);

        // New Note Input
        JPanel newNoteBox = new JPanel(new BorderLayout(6, 0));
        newNoteBox.setOpaque(false);
        newNoteArea = Theme.createTextArea(2, 20);
        JButton postNoteBtn = Theme.createPrimaryButton("Post Note");
        postNoteBtn.setFont(Theme.FONT_SMALL);
        postNoteBtn.addActionListener(e -> handlePostNote());

        newNoteBox.add(new JScrollPane(newNoteArea), BorderLayout.CENTER);
        newNoteBox.add(postNoteBtn, BorderLayout.EAST);

        notesCard.add(nTitle, BorderLayout.NORTH);
        notesCard.add(new JScrollPane(notesList), BorderLayout.CENTER);
        notesCard.add(newNoteBox, BorderLayout.SOUTH);

        // Investigation Timeline Card
        JPanel timeCard = Theme.createCardPanel();
        timeCard.setLayout(new BorderLayout(0, 6));
        timeCard.setPreferredSize(new Dimension(0, 220));

        JLabel tTitle = new JLabel("INVESTIGATION TIMELINE");
        tTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tTitle.setForeground(Theme.TEXT_MUTED);

        timelineModel = new DefaultListModel<>();
        JList<String> timeList = new JList<>(timelineModel);
        timeList.setBackground(Theme.BG_INPUT);
        timeList.setForeground(Theme.TEXT_PRIMARY);
        timeList.setFont(Theme.FONT_MONO);

        timeCard.add(tTitle, BorderLayout.NORTH);
        timeCard.add(new JScrollPane(timeList), BorderLayout.CENTER);

        right.add(notesCard);
        right.add(Box.createVerticalStrut(8));
        right.add(timeCard);

        return right;
    }

    public void loadIncident(Incident inc) {
        this.incident = inc;
        if (inc == null) return;

        // Top Header
        codeLabel.setText(inc.getIncidentCode());
        titleLabel.setText(inc.getTitle());
        statusBadge.setText(inc.getStatus().getDisplayName());
        severityBadge.setText(inc.getSeverity().name());
        priorityBadge.setText(inc.getPriority().name());

        SlaService.SlaInfo sla = slaService.calculateSla(inc);
        slaBadge.setText(sla.getStatus().getDisplayName() + " (" + sla.getFormattedRemaining() + ")");

        simBadge.setVisible(inc.isSimulated());

        // Left Column
        riskScoreLabel.setText(inc.getRiskScore() + " / 100");
        riskLevelLabel.setText(inc.getPriority().getLabel().toUpperCase());
        if (inc.getRiskScore() >= 75) {
            riskScoreLabel.setForeground(Theme.COLOR_DANGER);
            riskLevelLabel.setForeground(Theme.COLOR_DANGER);
        } else if (inc.getRiskScore() >= 50) {
            riskScoreLabel.setForeground(Theme.COLOR_WARNING);
            riskLevelLabel.setForeground(Theme.COLOR_WARNING);
        } else {
            riskScoreLabel.setForeground(Theme.COLOR_PRIMARY);
            riskLevelLabel.setForeground(Theme.COLOR_SUCCESS);
        }

        // Classification Engine Output
        ClassificationEngine.ClassificationResult cl = ClassificationEngine.classify(inc.getTitle(), inc.getDescription());
        classifSuggestLabel.setText("Suggested: " + cl.getSuggestedCategory() + " (" + cl.getConfidence() + "% Match)");
        classifReasonLabel.setText("<html>" + cl.getReason() + "</html>");

        reporterLabel.setText("Reporter: " + inc.getReportedByName());
        systemLabel.setText("System: " + inc.getAffectedSystem());
        createdLabel.setText("Reported: " + inc.getCreatedAt());
        descArea.setText(inc.getDescription());

        // Center Column: Checklist
        checklistContainer.removeAll();
        try {
            List<IncidentAction> actions = investigationService.getActions(inc.getId());
            for (IncidentAction a : actions) {
                JCheckBox cb = new JCheckBox(a.getActionText(), a.isCompleted());
                cb.setOpaque(false);
                cb.setForeground(a.isCompleted() ? Theme.COLOR_SUCCESS : Theme.TEXT_PRIMARY);
                cb.setFont(Theme.FONT_REGULAR);
                cb.addActionListener(e -> {
                    try {
                        investigationService.toggleAction(a.getId(), cb.isSelected(), session);
                        cb.setForeground(cb.isSelected() ? Theme.COLOR_SUCCESS : Theme.TEXT_PRIMARY);
                    } catch (Exception ex) {
                        cb.setSelected(!cb.isSelected());
                    }
                });
                checklistContainer.add(cb);
                checklistContainer.add(Box.createVerticalStrut(4));
            }
        } catch (Exception ignored) {}
        checklistContainer.revalidate();
        checklistContainer.repaint();

        // Center Column: IOCs
        iocTableModel.setRowCount(0);
        boolean anyThreatMatch = false;
        try {
            List<Indicator> iocs = indicatorService.getIndicators(inc.getId());
            for (Indicator i : iocs) {
                if (i.isThreatIntelMatch()) anyThreatMatch = true;
                iocTableModel.addRow(new Object[]{
                    i.getType(),
                    i.getValue(),
                    i.getClassification(),
                    i.getThreatLevel(),
                    i.isThreatIntelMatch() ? "⚠ MATCH" : "NO"
                });
            }
        } catch (Exception ignored) {}

        threatIntelBanner.setVisible(anyThreatMatch);

        // Center Column: Evidence
        evidenceTableModel.setRowCount(0);
        try {
            List<Evidence> evList = evidenceService.getEvidenceForIncident(inc.getId(), session);
            for (Evidence ev : evList) {
                evidenceTableModel.addRow(new Object[]{
                    ev.getOriginalFilename(),
                    ev.getFormattedSize(),
                    ev.getSha256().substring(0, 16) + "...",
                    ev.getUploadedByName()
                });
            }
        } catch (Exception ignored) {}

        // Right Column: Notes & Timeline
        notesModel.clear();
        try {
            List<InvestigationNote> notes = investigationService.getNotes(inc.getId(), session);
            for (InvestigationNote n : notes) {
                notesModel.addElement("[" + n.getCreatedAt().toString().substring(11, 19) + "] " + n.getAnalystName() + ": " + n.getNote());
            }
        } catch (Exception ignored) {}

        timelineModel.clear();
        try {
            List<IncidentHistory> hist = investigationService.getHistory(inc.getId(), session);
            for (IncidentHistory h : hist) {
                timelineModel.addElement(String.format("[%s] %s: %s", h.getCreatedAt().toString().substring(11, 19), h.getEvent(), h.getDescription()));
            }
        } catch (Exception ignored) {}
    }

    private void updateStatus(IncidentStatus newStatus, String note) {
        if (incident == null) return;
        try {
            incidentService.updateStatus(incident.getId(), newStatus, session, note);
            incident = incidentService.getIncident(incident.getId());
            loadIncident(incident);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Status update failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handlePostNote() {
        if (incident == null) return;
        String note = newNoteArea.getText().trim();
        if (note.isBlank()) return;

        try {
            investigationService.addNote(incident.getId(), note, session);
            newNoteArea.setText("");
            incident = incidentService.getIncident(incident.getId());
            loadIncident(incident);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to post note: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRecalculateRisk() {
        if (incident == null) return;
        try {
            incidentService.recalculateRisk(incident.getId(), session);
            incident = incidentService.getIncident(incident.getId());
            loadIncident(incident);
            JOptionPane.showMessageDialog(this, "Risk Score updated to " + incident.getRiskScore() + "/100 (" + incident.getPriority().getLabel() + ")", "Risk Recalculated", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Recalculation error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAddIocDialog() {
        if (incident == null) return;

        JTextField valField = Theme.createTextField();
        JComboBox<String> typeCb = Theme.createComboBox(new String[]{"IP Address", "Domain", "URL", "File Hash", "Email Address", "Username", "Hostname"});
        JTextField descField = Theme.createTextField();

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(Theme.createFilterLabel("IOC Type:"));
        panel.add(typeCb);
        panel.add(Theme.createFilterLabel("IOC Value:"));
        panel.add(valField);
        panel.add(Theme.createFilterLabel("Description:"));
        panel.add(descField);

        int res = JOptionPane.showConfirmDialog(this, panel, "Add Indicator of Compromise", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION && !valField.getText().trim().isBlank()) {
            try {
                IndicatorService.AddIndicatorResult r = indicatorService.addIndicator(
                    incident.getId(), (String) typeCb.getSelectedItem(), valField.getText().trim(), descField.getText().trim(), session
                );
                incident = incidentService.getIncident(incident.getId());
                loadIncident(incident);

                if (r.isMatchFound()) {
                    JOptionPane.showMessageDialog(this,
                        "<html><b>🚨 THREAT INTELLIGENCE MATCH FOUND!</b><br><br>" +
                        "Classification: <font color='#F85149'><b>" + r.getMatchedIntel().getClassification() + "</b></font><br>" +
                        "Threat Level: <b>" + r.getMatchedIntel().getThreatLevel() + "</b><br>" +
                        "Source: <b>" + r.getMatchedIntel().getSource() + "</b><br><br>" +
                        "Description: " + r.getMatchedIntel().getDescription() + "<br><br>" +
                        "Incident risk score has been dynamically elevated.</html>",
                        "Threat Intelligence Alert", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding IOC: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleUploadEvidence() {
        if (incident == null) return;
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            String desc = JOptionPane.showInputDialog(this, "Evidence Description:", "Forensic Disk/Artifact");
            try {
                evidenceService.uploadEvidence(incident.getId(), f, desc, session);
                incident = incidentService.getIncident(incident.getId());
                loadIncident(incident);
                JOptionPane.showMessageDialog(this, "Evidence uploaded and cryptographic SHA-256 computed.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Upload error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openResolutionDialog() {
        if (incident == null) return;
        new QuickResolutionDialog(SwingUtilities.getWindowAncestor(this), incident, session, () -> {
            try {
                incident = incidentService.getIncident(incident.getId());
                loadIncident(incident);
            } catch (Exception ignored) {}
        }).setVisible(true);
    }

    private void openEscalateDialog() {
        if (incident == null) return;
        String reason = JOptionPane.showInputDialog(this, "Escalation Reason for SOC Supermod:", "Escalate to Command Center", JOptionPane.WARNING_MESSAGE);
        if (reason != null && !reason.trim().isBlank()) {
            try {
                incidentService.escalateIncident(incident.getId(), reason.trim(), session);
                incident = incidentService.getIncident(incident.getId());
                loadIncident(incident);
                JOptionPane.showMessageDialog(this, "Incident escalated to Supermod Command Center.", "Escalated", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Escalation error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openChangeSeverityDialog() {
        if (incident == null) return;
        Severity[] sevs = Severity.values();
        Severity sel = (Severity) JOptionPane.showInputDialog(
            this, "Select new severity level:", "Override Severity",
            JOptionPane.PLAIN_MESSAGE, null, sevs, incident.getSeverity()
        );
        if (sel != null && sel != incident.getSeverity()) {
            try {
                incidentService.updateSeverity(incident.getId(), sel, session);
                incident = incidentService.getIncident(incident.getId());
                loadIncident(incident);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
