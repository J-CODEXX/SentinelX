package com.cybersentinel.ui.user;

import com.cybersentinel.model.Incident;
import com.cybersentinel.model.Severity;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.service.EvidenceService;
import com.cybersentinel.service.IncidentService;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.sql.Timestamp;
import java.util.function.Consumer;

public class ReportIncidentDialog extends JDialog {
    private final UserSession session;
    private final IncidentService incidentService = new IncidentService();
    private final EvidenceService evidenceService = new EvidenceService();
    private final Consumer<Incident> onReported;

    private JTextField titleField;
    private JComboBox<String> typeComboBox;
    private JComboBox<Severity> severityComboBox;
    private JTextField systemField;
    private JTextArea descriptionArea;
    private JTextArea contextArea;
    private JLabel fileLabel;
    private File selectedEvidenceFile = null;
    private JLabel errorLabel;

    private static final String[] INCIDENT_TYPES = {
        "Phishing", "Malware", "Ransomware", "Account Compromise",
        "Unauthorized Access", "Data Breach", "Network Attack",
        "Insider Threat", "Suspicious Activity", "Other"
    };

    public ReportIncidentDialog(Window parent, UserSession session, Consumer<Incident> onReported) {
        super(parent, "Report Cybersecurity Incident", ModalityType.APPLICATION_MODAL);
        this.session = session;
        this.onReported = onReported;

        setSize(640, 680);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(Theme.BG_CANVAS);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Theme.BG_CANVAS);
        root.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel title = new JLabel("🛡️ Submit Incident Report");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.COLOR_PRIMARY);

        JPanel form = Theme.createCardPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        form.add(createLabel("Incident Title *"));
        titleField = Theme.createTextField();
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        form.add(titleField);
        form.add(Box.createVerticalStrut(8));

        // Two-column row for Type and Severity
        JPanel row1 = new JPanel(new GridLayout(1, 2, 12, 0));
        row1.setOpaque(false);

        JPanel pType = new JPanel(new BorderLayout(0, 4));
        pType.setOpaque(false);
        pType.add(createLabel("Incident Type"), BorderLayout.NORTH);
        typeComboBox = Theme.createComboBox(INCIDENT_TYPES);
        pType.add(typeComboBox, BorderLayout.CENTER);

        JPanel pSev = new JPanel(new BorderLayout(0, 4));
        pSev.setOpaque(false);
        pSev.add(createLabel("Perceived Severity"), BorderLayout.NORTH);
        severityComboBox = Theme.createComboBox(Severity.values());
        severityComboBox.setSelectedItem(Severity.MEDIUM);
        pSev.add(severityComboBox, BorderLayout.CENTER);

        row1.add(pType);
        row1.add(pSev);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        form.add(row1);
        form.add(Box.createVerticalStrut(8));

        form.add(createLabel("Affected System / Workstation / Service *"));
        systemField = Theme.createTextField();
        systemField.setText("My Workstation");
        systemField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        form.add(systemField);
        form.add(Box.createVerticalStrut(8));

        form.add(createLabel("Detailed Description (What happened, unusual behavior, links clicked) *"));
        descriptionArea = Theme.createTextArea(4, 40);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        form.add(descScroll);
        form.add(Box.createVerticalStrut(8));

        form.add(createLabel("Additional Context / Notes"));
        contextArea = Theme.createTextArea(2, 40);
        JScrollPane ctxScroll = new JScrollPane(contextArea);
        ctxScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        form.add(ctxScroll);
        form.add(Box.createVerticalStrut(10));

        // Evidence File Attachment
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filePanel.setOpaque(false);
        JButton attachBtn = Theme.createSecondaryButton("Attach Evidence File");
        attachBtn.addActionListener(e -> chooseFile());
        fileLabel = new JLabel("No file selected (max 20MB)");
        fileLabel.setFont(Theme.FONT_SMALL);
        fileLabel.setForeground(Theme.TEXT_MUTED);

        filePanel.add(attachBtn);
        filePanel.add(fileLabel);
        filePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(filePanel);
        form.add(Box.createVerticalStrut(8));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.COLOR_DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(errorLabel);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton submitBtn = Theme.createPrimaryButton("Submit Incident Report");
        submitBtn.addActionListener(e -> handleSubmit());

        btnPanel.add(cancelBtn);
        btnPanel.add(submitBtn);

        root.add(title, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);

        add(root);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BOLD);
        lbl.setForeground(Theme.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            selectedEvidenceFile = chooser.getSelectedFile();
            fileLabel.setText(selectedEvidenceFile.getName() + " (" + (selectedEvidenceFile.length() / 1024) + " KB)");
            fileLabel.setForeground(Theme.COLOR_SUCCESS);
        }
    }

    private void handleSubmit() {
        String title = titleField.getText().trim();
        String desc = descriptionArea.getText().trim();
        String sys = systemField.getText().trim();
        String type = (String) typeComboBox.getSelectedItem();
        Severity sev = (Severity) severityComboBox.getSelectedItem();
        String extra = contextArea.getText().trim();

        if (title.isBlank()) {
            errorLabel.setText("Please enter an incident title.");
            titleField.requestFocusInWindow();
            return;
        }
        if (desc.isBlank()) {
            errorLabel.setText("Please provide a detailed incident description.");
            descriptionArea.requestFocusInWindow();
            return;
        }
        if (sys.isBlank()) {
            sys = "General Workstation";
        }

        try {
            Incident inc = new Incident();
            inc.setTitle(title);
            inc.setType(type);
            inc.setSeverity(sev);
            inc.setAffectedSystem(sys);
            inc.setDescription(desc + (!extra.isBlank() ? "\n\nAdditional Context:\n" + extra : ""));
            inc.setObservedAt(new Timestamp(System.currentTimeMillis()));

            Incident created = incidentService.reportIncident(inc, session);

            // Upload attached evidence if selected
            if (selectedEvidenceFile != null && selectedEvidenceFile.exists()) {
                evidenceService.uploadEvidence(created.getId(), selectedEvidenceFile, "Initial user attachment", session);
            }

            dispose();

            JOptionPane.showMessageDialog(null,
                "<html><b>Incident Successfully Reported!</b><br><br>" +
                "Incident ID: <font color='#388BFD'><b>" + created.getIncidentCode() + "</b></font><br>" +
                "Classification: <b>" + created.getType() + "</b><br>" +
                "Initial Risk Score: <b>" + created.getRiskScore() + " / 100 (" + created.getPriority().name() + ")</b><br><br>" +
                "Our SOC analysts have been alerted and will investigate shortly.</html>",
                "Incident Submitted", JOptionPane.INFORMATION_MESSAGE);

            if (onReported != null) {
                onReported.accept(created);
            }
        } catch (Exception ex) {
            errorLabel.setText("<html>" + ex.getMessage() + "</html>");
        }
    }
}
