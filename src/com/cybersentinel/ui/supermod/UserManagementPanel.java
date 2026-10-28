package com.cybersentinel.ui.supermod;

import com.cybersentinel.dao.IncidentDAO;
import com.cybersentinel.dao.UserDAO;
import com.cybersentinel.model.Incident;
import com.cybersentinel.model.Role;
import com.cybersentinel.model.User;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.service.AuthService;
import com.cybersentinel.ui.common.StatusBadgeRenderer;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class UserManagementPanel extends JPanel {
    private final UserSession session;
    private final UserDAO userDAO = new UserDAO();
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final AuthService authService = new AuthService();

    private JTextField searchField;
    private JTable userTable;
    private DefaultTableModel userModel;

    public UserManagementPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BG_CANVAS);
        setBorder(new EmptyBorder(14, 18, 14, 18));

        initUI();
        refreshUsers();
    }

    private void initUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Enterprise User & Identity Directory");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_LIGHT);
        topBar.add(title, BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnBar.setOpaque(false);

        JButton toggleActiveBtn = Theme.createWarningButton("Toggle Active / Deactivate");
        toggleActiveBtn.addActionListener(e -> handleToggleActive());

        JButton resetPassBtn = Theme.createSecondaryButton("Reset Password");
        resetPassBtn.addActionListener(e -> handleResetPassword());

        JButton viewHistoryBtn = Theme.createPrimaryButton("View Reported History");
        viewHistoryBtn.addActionListener(e -> handleViewUserHistory());

        btnBar.add(toggleActiveBtn);
        btnBar.add(resetPassBtn);
        btnBar.add(viewHistoryBtn);

        topBar.add(btnBar, BorderLayout.EAST);

        // Filter Card
        JPanel filterCard = Theme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        searchField = Theme.createTextField();
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setToolTipText("Search Username or Email");

        JButton searchBtn = Theme.createPrimaryButton("Search");
        searchBtn.addActionListener(e -> refreshUsers());

        JButton refreshBtn = Theme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            refreshUsers();
        });

        filterCard.add(Theme.createFilterLabel("Search Users:"));
        filterCard.add(searchField);
        filterCard.add(searchBtn);
        filterCard.add(refreshBtn);

        // Table Card
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"User ID", "Username", "Email", "Role", "Active Status", "Failed Attempts", "Account Lock", "Created At"};
        userModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = new JTable(userModel);
        Theme.styleTable(userTable);
        userTable.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeRenderer());

        tableCard.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(filterCard, BorderLayout.NORTH);
        center.add(tableCard, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    public void refreshUsers() {
        userModel.setRowCount(0);
        String q = searchField.getText().trim().toLowerCase();

        try {
            List<User> list = userDAO.findAll();
            for (User u : list) {
                if (!q.isBlank() && !u.getUsername().toLowerCase().contains(q) && !u.getEmail().toLowerCase().contains(q)) {
                    continue;
                }
                userModel.addRow(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getRole().name(),
                    u.isActive() ? "ACTIVE" : "DEACTIVATED",
                    u.getFailedAttempts(),
                    u.isLocked() ? "LOCKED" : "NORMAL",
                    u.getCreatedAt().toString().substring(0, 10)
                });
            }
        } catch (SQLException e) {
            System.err.println("[USER_MGMT] Error loading users: " + e.getMessage());
        }
    }

    private void handleToggleActive() {
        User u = getSelectedUser();
        if (u == null) return;

        if (u.getId() == session.getUserId()) {
            JOptionPane.showMessageDialog(this, "You cannot deactivate your own administrative account.", "Forbidden", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            authService.setUserActive(session.getUserId(), session.getUsername(), u.getId(), !u.isActive());
            refreshUsers();
            JOptionPane.showMessageDialog(this, "Account status updated for " + u.getUsername(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleResetPassword() {
        User u = getSelectedUser();
        if (u == null) return;

        String newPass = JOptionPane.showInputDialog(this, "Enter new temporary password for " + u.getUsername() + "\n(min 8 chars, letters & digits):", "Reset User Password", JOptionPane.PLAIN_MESSAGE);
        if (newPass != null && !newPass.isBlank()) {
            try {
                authService.adminResetPassword(session.getUserId(), session.getUsername(), u.getId(), newPass);
                refreshUsers();
                JOptionPane.showMessageDialog(this, "Password reset successfully for " + u.getUsername(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Reset failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleViewUserHistory() {
        User u = getSelectedUser();
        if (u == null) return;

        try {
            List<Incident> list = incidentDAO.findAll(null, null, null, null, null, null, u.getId(), null);
            StringBuilder sb = new StringBuilder();
            sb.append("Incident History for ").append(u.getUsername()).append(" [Total: ").append(list.size()).append("]:\n\n");
            for (Incident inc : list) {
                sb.append(String.format("• %s | %s | Sev: %s | Status: %s | Risk: %d/100 | %s\n",
                    inc.getIncidentCode(), inc.getTitle(), inc.getSeverity().name(),
                    inc.getStatus().getDisplayName(), inc.getRiskScore(), inc.getCreatedAt().toString().substring(0, 10)));
            }
            if (list.isEmpty()) {
                sb.append("This user has not submitted any security incidents yet.");
            }

            JTextArea ta = Theme.createTextArea(14, 50);
            ta.setText(sb.toString());
            ta.setEditable(false);

            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Incident History — " + u.getUsername(), JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private User getSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        long id = (Long) userModel.getValueAt(row, 0);
        try {
            return userDAO.findById(id);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
