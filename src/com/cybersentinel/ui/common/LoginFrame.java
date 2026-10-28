package com.cybersentinel.ui.common;

import com.cybersentinel.config.AppConfig;
import com.cybersentinel.database.DatabaseInitializer;
import com.cybersentinel.model.Role;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.service.AuthService;
import com.cybersentinel.ui.analyst.AnalystMainFrame;
import com.cybersentinel.ui.supermod.SupermodMainFrame;
import com.cybersentinel.ui.theme.Theme;
import com.cybersentinel.ui.user.UserMainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginFrame extends JFrame {
    private final AuthService authService = new AuthService();
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JButton loginButton;

    public LoginFrame() {
        setTitle(AppConfig.APP_NAME + " — SOC Authentication");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Theme.BG_CANVAS);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_CANVAS);
        root.setBorder(new EmptyBorder(28, 36, 28, 36));

        // Header Panel with SOC branding
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel logo = new JLabel("🛡️ " + AppConfig.APP_NAME.toUpperCase());
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Theme.COLOR_PRIMARY);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(AppConfig.APP_TAGLINE);
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(logo);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(24));

        // Form Card
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel userLbl = new JLabel("Username");
        userLbl.setFont(Theme.FONT_BOLD);
        userLbl.setForeground(Theme.TEXT_PRIMARY);

        usernameField = Theme.createTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(Theme.FONT_BOLD);
        passLbl.setForeground(Theme.TEXT_PRIMARY);

        passwordField = Theme.createPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.COLOR_DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginButton = Theme.createPrimaryButton("Sign In to SOC");
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton registerButton = Theme.createSecondaryButton("Register New Account");
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        registerButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(userLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(14));
        card.add(passLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(8));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(10));
        card.add(registerButton);

        // Quick Demo Fill Panel
        JPanel demoPanel = new JPanel(new GridLayout(1, 3, 8, 0));
        demoPanel.setOpaque(false);
        demoPanel.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton btnUser = Theme.createSecondaryButton("Demo User");
        btnUser.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnUser.addActionListener(e -> {
            usernameField.setText("john_user");
            passwordField.setText("UserPass123!");
            statusLabel.setText(" ");
        });

        JButton btnAnalyst = Theme.createSecondaryButton("Demo Analyst");
        btnAnalyst.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnAnalyst.addActionListener(e -> {
            usernameField.setText("analyst_alex");
            passwordField.setText("AnalystPass123!");
            statusLabel.setText(" ");
        });

        JButton btnAdmin = Theme.createSecondaryButton("Demo Supermod");
        btnAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnAdmin.addActionListener(e -> {
            usernameField.setText("supermod");
            passwordField.setText("AdminPass123!");
            statusLabel.setText(" ");
        });

        demoPanel.add(btnUser);
        demoPanel.add(btnAnalyst);
        demoPanel.add(btnAdmin);

        root.add(header, BorderLayout.NORTH);
        root.add(card, BorderLayout.CENTER);
        root.add(demoPanel, BorderLayout.SOUTH);

        add(root);

        // Events
        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> openRegisterDialog());

        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        };
        usernameField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isBlank() || password.isBlank()) {
            statusLabel.setText("Please enter both username and password.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Authenticating...");
        statusLabel.setText(" ");

        SwingWorker<UserSession, Void> worker = new SwingWorker<>() {
            @Override
            protected UserSession doInBackground() throws Exception {
                // Ensure tables and demo accounts exist
                DatabaseInitializer.initialize();
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("Sign In to SOC");
                try {
                    UserSession session = get();
                    launchRoleDashboard(session);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statusLabel.setText("<html>" + cause.getMessage() + "</html>");
                }
            }
        };
        worker.execute();
    }

    private void launchRoleDashboard(UserSession session) {
        dispose();
        SwingUtilities.invokeLater(() -> {
            if (session.getRole() == Role.SUPERMOD) {
                new SupermodMainFrame(session).setVisible(true);
            } else if (session.getRole() == Role.ANALYST) {
                new AnalystMainFrame(session).setVisible(true);
            } else {
                new UserMainFrame(session).setVisible(true);
            }
        });
    }

    private void openRegisterDialog() {
        RegisterDialog dialog = new RegisterDialog(this, (registeredUser) -> {
            usernameField.setText(registeredUser.getUsername());
            passwordField.setText("");
            statusLabel.setForeground(Theme.COLOR_SUCCESS);
            statusLabel.setText("Account created! Enter your password to sign in.");
            passwordField.requestFocusInWindow();
        });
        dialog.setVisible(true);
    }
}
