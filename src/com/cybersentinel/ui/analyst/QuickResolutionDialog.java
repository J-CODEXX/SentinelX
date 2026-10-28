package com.cybersentinel.ui.analyst;

import com.cybersentinel.model.Incident;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.service.IncidentService;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class QuickResolutionDialog extends JDialog {
    private final Incident incident;
    private final UserSession session;
    private final IncidentService incidentService = new IncidentService();
    private final Runnable onResolved;

    private JTextArea rootCauseArea;
    private JTextArea actionsArea;
    private JTextArea userSummaryArea;
    private JTextArea recsArea;
    private JLabel errorLabel;

    public QuickResolutionDialog(Window parent, Incident incident, UserSession session, Runnable onResolved) {
        super(parent, "Resolve Security Incident — " + incident.getIncidentCode(), ModalityType.APPLICATION_MODAL);
        this.incident = incident;
        this.session = session;
        this.onResolved = onResolved;

        setSize(640, 620);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(Theme.BG_CANVAS);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Theme.BG_CANVAS);
        root.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("✅ Mark Incident as RESOLVED");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.COLOR_SUCCESS);

        JPanel form = Theme.createCardPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        form.add(createLabel("1. Root Cause Analysis *"));
        rootCauseArea = Theme.createTextArea(3, 40);
        form.add(new JScrollPane(rootCauseArea));
        form.add(Box.createVerticalStrut(8));

        form.add(createLabel("2. Actions Taken (Technical Containment & Remediation) *"));
        actionsArea = Theme.createTextArea(3, 40);
        form.add(new JScrollPane(actionsArea));
        form.add(Box.createVerticalStrut(8));

        form.add(createLabel("3. User-Facing Resolution Summary (Visible to reporter) *"));
        userSummaryArea = Theme.createTextArea(3, 40);
        userSummaryArea.setText("The security threat on your system has been successfully contained and remediated. No further malicious activity detected.");
        form.add(new JScrollPane(userSummaryArea));
        form.add(Box.createVerticalStrut(8));

        form.add(createLabel("4. Preventive Recommendations for User / IT"));
        recsArea = Theme.createTextArea(2, 40);
        recsArea.setText("Do not open unexpected attachments or links from external senders.");
        form.add(new JScrollPane(recsArea));
        form.add(Box.createVerticalStrut(6));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.COLOR_DANGER);
        form.add(errorLabel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton confirmBtn = Theme.createSuccessButton("Confirm Resolution");
        confirmBtn.addActionListener(e -> handleConfirm());

        btnPanel.add(cancelBtn);
        btnPanel.add(confirmBtn);

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

    private void handleConfirm() {
        String rootCause = rootCauseArea.getText().trim();
        String actions = actionsArea.getText().trim();
        String userSumm = userSummaryArea.getText().trim();
        String recs = recsArea.getText().trim();

        if (rootCause.isBlank() || actions.isBlank() || userSumm.isBlank()) {
            errorLabel.setText("Root Cause, Actions Taken, and User Resolution Summary are required.");
            return;
        }

        try {
            incidentService.resolveIncident(incident.getId(), rootCause, actions, userSumm, recs, session);
            dispose();
            JOptionPane.showMessageDialog(null, "Incident " + incident.getIncidentCode() + " has been marked as RESOLVED.\nReporter has been notified.", "Incident Resolved", JOptionPane.INFORMATION_MESSAGE);
            if (onResolved != null) onResolved.run();
        } catch (Exception ex) {
            errorLabel.setText("<html>" + ex.getMessage() + "</html>");
        }
    }
}
