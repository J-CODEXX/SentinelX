package com.cybersentinel.ui.supermod;

import com.cybersentinel.model.AnalystRequest;
import com.cybersentinel.model.User;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.service.AuthService;
import com.cybersentinel.ui.common.StatusBadgeRenderer;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AnalystRequestsDialog extends JDialog {
    private final UserSession session;
    private final Runnable onUpdated;
    private final AuthService authService = new AuthService();

    private JComboBox<String> statusFilterCombo;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton approveBtn;
    private JButton rejectBtn;
    private List<AnalystRequest> currentRequests = new ArrayList<>();

    public AnalystRequestsDialog(Window parent, UserSession session, Runnable onUpdated) {
        super(parent, "SOC Security Analyst Account Requests", ModalityType.APPLICATION_MODAL);
        this.session = session;
        this.onUpdated = onUpdated;

        setSize(860, 560);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.BG_CANVAS);

        initUI();
        loadRequests();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Theme.BG_CANVAS);
        root.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("🛡️ SOC Analyst Account Registration Requests");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_LIGHT);

        JLabel subtitle = new JLabel("Review pending registration requests. Approving a request creates the analyst account in the directory.");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_MUTED);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(subtitle);
        header.add(titleBox, BorderLayout.WEST);

        // Filter Bar
        JPanel filterCard = Theme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        statusFilterCombo = Theme.createComboBox(new String[]{"PENDING Only", "ALL Requests", "APPROVED", "REJECTED"});
        statusFilterCombo.setPreferredSize(new Dimension(160, 30));
        statusFilterCombo.addActionListener(e -> loadRequests());

        JButton refreshBtn = Theme.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadRequests());

        filterCard.add(Theme.createFilterLabel("Filter Status:"));
        filterCard.add(statusFilterCombo);
        filterCard.add(refreshBtn);

        // Center Table Card
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"Req ID", "Username", "Work Email", "Status", "Requested At", "Reviewed By", "Rejection Reason"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        Theme.styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeRenderer());

        table.getSelectionModel().addListSelectionListener(e -> updateActionButtonsState());

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        // Action Buttons at bottom
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);

        approveBtn = Theme.createSuccessButton("✔ Accept & Create Account");
        approveBtn.setEnabled(false);
        approveBtn.addActionListener(e -> handleApprove());

        rejectBtn = Theme.createDangerButton("✖ Reject Request");
        rejectBtn.setEnabled(false);
        rejectBtn.addActionListener(e -> handleReject());

        leftActions.add(approveBtn);
        leftActions.add(rejectBtn);

        JButton closeBtn = Theme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dispose());

        bottomBar.add(leftActions, BorderLayout.WEST);
        bottomBar.add(closeBtn, BorderLayout.EAST);

        // Assemble root
        JPanel northBox = new JPanel(new BorderLayout(0, 10));
        northBox.setOpaque(false);
        northBox.add(header, BorderLayout.NORTH);
        northBox.add(filterCard, BorderLayout.CENTER);

        root.add(northBox, BorderLayout.NORTH);
        root.add(tableCard, BorderLayout.CENTER);
        root.add(bottomBar, BorderLayout.SOUTH);

        add(root);
    }

    private void loadRequests() {
        tableModel.setRowCount(0);
        String filter = (String) statusFilterCombo.getSelectedItem();
        String statusArg = null;
        if ("PENDING Only".equalsIgnoreCase(filter)) statusArg = "PENDING";
        else if ("APPROVED".equalsIgnoreCase(filter)) statusArg = "APPROVED";
        else if ("REJECTED".equalsIgnoreCase(filter)) statusArg = "REJECTED";

        try {
            currentRequests = authService.getAnalystRequests(statusArg);
            for (AnalystRequest req : currentRequests) {
                String reviewedByStr = req.getReviewerUsername() != null ? req.getReviewerUsername() : "-";
                String reasonStr = req.getRejectionReason() != null ? req.getRejectionReason() : "-";
                String dateStr = req.getCreatedAt() != null ? req.getCreatedAt().toString().substring(0, 16) : "-";

                tableModel.addRow(new Object[]{
                    req.getId(),
                    req.getUsername(),
                    req.getEmail(),
                    req.getStatus(),
                    dateStr,
                    reviewedByStr,
                    reasonStr
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load analyst requests: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        updateActionButtonsState();
    }

    private void updateActionButtonsState() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentRequests.size()) {
            approveBtn.setEnabled(false);
            rejectBtn.setEnabled(false);
            return;
        }

        AnalystRequest req = currentRequests.get(row);
        boolean isPending = "PENDING".equalsIgnoreCase(req.getStatus());
        approveBtn.setEnabled(isPending);
        rejectBtn.setEnabled(isPending);
    }

    private void handleApprove() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentRequests.size()) return;

        AnalystRequest req = currentRequests.get(row);
        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only pending requests can be accepted.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Approve and create SOC Analyst account for '" + req.getUsername() + "' (" + req.getEmail() + ")?\n" +
            "This will create their account and allow them to log in immediately.",
            "Confirm Account Creation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            User createdUser = authService.approveAnalystRequest(req.getId(), session);
            JOptionPane.showMessageDialog(
                this,
                "Analyst account for '" + createdUser.getUsername() + "' has been approved and created successfully!\n" +
                "The user can now log in with their requested credentials.",
                "Account Created",
                JOptionPane.INFORMATION_MESSAGE
            );
            loadRequests();
            if (onUpdated != null) {
                onUpdated.run();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Approval failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleReject() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentRequests.size()) return;

        AnalystRequest req = currentRequests.get(row);
        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only pending requests can be rejected.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reason = JOptionPane.showInputDialog(
            this,
            "Enter rejection reason for analyst request from '" + req.getUsername() + "' (optional):",
            "Reject Analyst Request",
            JOptionPane.PLAIN_MESSAGE
        );

        if (reason == null) return; // User cancelled

        try {
            authService.rejectAnalystRequest(req.getId(), reason, session);
            JOptionPane.showMessageDialog(
                this,
                "Analyst account request for '" + req.getUsername() + "' has been rejected.",
                "Request Rejected",
                JOptionPane.INFORMATION_MESSAGE
            );
            loadRequests();
            if (onUpdated != null) {
                onUpdated.run();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Rejection failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
