package com.cybersentinel.ui.user;

import com.cybersentinel.config.AppConfig;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.security.SessionManager;
import com.cybersentinel.ui.common.LoginFrame;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UserMainFrame extends JFrame {
    private final UserSession session;
    private CardLayout cardLayout;
    private JPanel contentArea;

    private UserDashboardPanel dashboardPanel;
    private UserIncidentTrackingPanel trackingPanel;
    private String currentCard = "DASHBOARD";

    // Nav button references for active-state highlighting
    private JButton activeNavBtn = null;

    public UserMainFrame(UserSession session) {
        this.session = session;
        setTitle(AppConfig.APP_NAME + " — Employee Security Portal (" + session.getUsername() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_CANVAS);

        initUI();
    }

    private void rebuildUI() {
        getContentPane().removeAll();
        initUI();
        revalidate();
        repaint();
        if ("TRACKING".equals(currentCard)) {
            trackingPanel.loadIncidents();
            cardLayout.show(contentArea, "TRACKING");
        } else {
            dashboardPanel.refreshData();
            cardLayout.show(contentArea, "DASHBOARD");
        }
    }

    private void initUI() {
        getContentPane().setBackground(Theme.BG_CANVAS);
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_CANVAS);

        // Top Navigation Bar
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Theme.NAV_BG);
        nav.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR),
            new EmptyBorder(12, 20, 12, 20)
        ));

        // Brand
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brand.setOpaque(false);
        JLabel logo = new JLabel("🛡️ " + AppConfig.APP_NAME);
        logo.setFont(Theme.FONT_TITLE);
        logo.setForeground(Theme.COLOR_PRIMARY);

        JLabel portalLbl = new JLabel("•  Employee Incident Portal");
        portalLbl.setFont(Theme.FONT_REGULAR);
        portalLbl.setForeground(Theme.TEXT_MUTED);

        brand.add(logo);
        brand.add(portalLbl);

        // Nav Buttons
        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navButtons.setOpaque(false);

        JButton btnDash = Theme.createSecondaryButton("Dashboard");
        JButton btnReport = Theme.createPrimaryButton("+ Report Incident");
        JButton btnMyInc = Theme.createSecondaryButton("My Incidents");
        JButton btnTheme = Theme.createThemeToggleButton(this::rebuildUI);

        JLabel userPill = Theme.createBadge(session.getUsername() + " (USER)", Theme.COLOR_SIMULATED,
            Theme.isDarkMode() ? new Color(88, 166, 255, 30) : new Color(2, 132, 199, 30));

        JButton btnLogout = Theme.createDangerButton("Logout");

        navButtons.add(btnDash);
        navButtons.add(btnReport);
        navButtons.add(btnMyInc);
        navButtons.add(btnTheme);
        navButtons.add(Box.createHorizontalStrut(10));
        navButtons.add(userPill);
        navButtons.add(btnLogout);

        // Restore active button state after rebuild
        if ("DASHBOARD".equals(currentCard)) Theme.setNavButtonActive(btnDash, true);
        else if ("TRACKING".equals(currentCard)) Theme.setNavButtonActive(btnMyInc, true);

        nav.add(brand, BorderLayout.WEST);
        nav.add(navButtons, BorderLayout.EAST);

        // Content Area Panels
        dashboardPanel = new UserDashboardPanel(session, this::openReportDialog);
        trackingPanel = new UserIncidentTrackingPanel(session);

        contentArea.add(dashboardPanel, "DASHBOARD");
        contentArea.add(trackingPanel, "TRACKING");

        root.add(nav, BorderLayout.NORTH);
        root.add(contentArea, BorderLayout.CENTER);

        add(root);

        // Button Listeners
        btnDash.addActionListener(e -> {
            currentCard = "DASHBOARD";
            setActiveNavBtn(btnDash, new JButton[]{btnDash, btnMyInc});
            dashboardPanel.refreshData();
            cardLayout.show(contentArea, "DASHBOARD");
        });

        btnReport.addActionListener(e -> openReportDialog());

        btnMyInc.addActionListener(e -> {
            currentCard = "TRACKING";
            setActiveNavBtn(btnMyInc, new JButton[]{btnDash, btnMyInc});
            trackingPanel.loadIncidents();
            cardLayout.show(contentArea, "TRACKING");
        });

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out?", "Confirm Sign Out", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                SessionManager.logout();
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
    }

    private void openReportDialog() {
        ReportIncidentDialog dlg = new ReportIncidentDialog(this, session, (newInc) -> {
            dashboardPanel.refreshData();
            trackingPanel.loadIncidents();
            currentCard = "TRACKING";
            cardLayout.show(contentArea, "TRACKING");
        });
        dlg.setVisible(true);
    }

    private void setActiveNavBtn(JButton active, JButton[] all) {
        activeNavBtn = active;
        for (JButton b : all) {
            Theme.setNavButtonActive(b, b == active);
        }
    }
}
