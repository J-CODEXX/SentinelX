package com.cybersentinel.ui.common;

import com.cybersentinel.model.Role;
import com.cybersentinel.model.User;
import com.cybersentinel.service.AuthService;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public class RegisterDialog extends JDialog {
    private final AuthService authService = new AuthService();
    private final Consumer<User> onSuccess;

    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<Role> roleComboBox;
    private JLabel errorLabel;

    public RegisterDialog(Frame parent, Consumer<User> onSuccess) {
        super(parent, "Register New Account", true);
        this.onSuccess = onSuccess;

        setSize(420, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(Theme.BG_CANVAS);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_CANVAS);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Create CyberSentinel Account");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.COLOR_PRIMARY);

        JPanel form = Theme.createCardPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        form.add(createFieldLabel("Username"));
        usernameField = Theme.createTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(10));

        form.add(createFieldLabel("Work Email"));
        emailField = Theme.createTextField();
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        form.add(emailField);
        form.add(Box.createVerticalStrut(10));

        form.add(createFieldLabel("Requested Role"));
        roleComboBox = Theme.createComboBox(new Role[]{Role.USER, Role.ANALYST});
        roleComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        form.add(roleComboBox);
        form.add(Box.createVerticalStrut(4));

        JLabel roleHintLabel = new JLabel("Standard user accounts are created instantly.");
        roleHintLabel.setFont(Theme.FONT_SMALL);
        roleHintLabel.setForeground(Theme.TEXT_MUTED);
        roleHintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(roleHintLabel);
        form.add(Box.createVerticalStrut(10));

        form.add(createFieldLabel("Password (min 8 chars, letters & digits)"));
        passwordField = Theme.createPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(10));

        form.add(createFieldLabel("Confirm Password"));
        confirmPasswordField = Theme.createPasswordField();
        confirmPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        form.add(confirmPasswordField);
        form.add(Box.createVerticalStrut(8));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.COLOR_DANGER);
        form.add(errorLabel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);

        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton submitBtn = Theme.createPrimaryButton("Create Account");
        submitBtn.addActionListener(e -> handleSubmit());

        roleComboBox.addActionListener(e -> {
            Role sel = (Role) roleComboBox.getSelectedItem();
            if (sel == Role.ANALYST) {
                submitBtn.setText("Request Analyst Account");
                roleHintLabel.setText("<html><i>Analyst accounts require Supermod approval before creation.</i></html>");
                roleHintLabel.setForeground(Theme.COLOR_WARNING);
            } else {
                submitBtn.setText("Create Account");
                roleHintLabel.setText("Standard user accounts are created instantly.");
                roleHintLabel.setForeground(Theme.TEXT_MUTED);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(submitBtn);

        root.add(title, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);

        add(root);
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BOLD);
        lbl.setForeground(Theme.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void handleSubmit() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());
        Role role = (Role) roleComboBox.getSelectedItem();

        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            errorLabel.setText("All fields are required.");
            return;
        }
        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        if (role == Role.ANALYST) {
            try {
                authService.requestAnalystAccount(username, email, password);
                dispose();
                JOptionPane.showMessageDialog(
                    getParent(),
                    "<html><b>Analyst Registration Request Submitted!</b><br><br>" +
                    "Your request for SOC Security Analyst access has been sent to the Super Moderator.<br>" +
                    "Only after the Super Moderator reviews and accepts your request<br>" +
                    "will your analyst account be created.<br><br>" +
                    "<i>You will be able to log in once your request is accepted.</i></html>",
                    "Analyst Request Pending Approval",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                errorLabel.setText("<html>" + ex.getMessage() + "</html>");
            }
            return;
        }

        try {
            User user = authService.registerUser(username, email, password, role);
            dispose();
            if (onSuccess != null) {
                onSuccess.accept(user);
            }
        } catch (Exception ex) {
            errorLabel.setText("<html>" + ex.getMessage() + "</html>");
        }
    }
}
