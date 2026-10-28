package com.cybersentinel.ui.supermod;

import com.cybersentinel.config.AppConfig;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.security.SessionManager;
import com.cybersentinel.ui.common.LoginFrame;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SupermodMainFrame extends JFrame {
    private final UserSession session;
    private CardLayout cardLayout;
    private JPanel contentArea;

    private SupermodDashboardPanel dashboardPanel;
    private GlobalIncidentsPanel incidentsPanel;
    private UserManagementPanel userPanel;
    private AnalystManagementPanel analystPanel;
    private ThreatIntelManagementPanel threatIntelPanel;
    private SlaMonitoringPanel slaPanel;
    private AuditLogsPanel auditPanel;
    private SimulationCenterPanel simPanel;
    private ReportsPanel reportsPanel;
    private SystemSettingsPanel settingsPanel;

    private String currentCard = "DASHBOARD";

    // Nav buttons for active-state tracking
    private JButton[] navBtns;
    private static final String[] NAV_CARDS = {"DASHBOARD","INCIDENTS","USERS","ANALYSTS","INTEL","SLA","AUDIT","SIMULATOR","REPORTS","SETTINGS"};

    public SupermodMainFrame(UserSession session) {
        this.session = session;
        setTitle(AppConfig.APP_NAME + " — SOC Command Center & Enterprise Administration (" + session.getUsername() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 860);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_CANVAS);

        initUI();
    }

    private void rebuildUI() {
        getContentPane().removeAll();
        initUI();
        revalidate();
        repaint();
        showSection(currentCard);
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
            new EmptyBorder(8, 16, 8, 16)
        ));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brand.setOpaque(false);
        JLabel logo = new JLabel("🛡️ " + AppConfig.APP_NAME);
        logo.setFont(Theme.FONT_TITLE);
        logo.setForeground(Theme.COLOR_PRIMARY);

        JLabel portalLbl = new JLabel("•  Command Center");
        portalLbl.setFont(Theme.FONT_REGULAR);
        portalLbl.setForeground(Theme.TEXT_MUTED);

        brand.add(logo);
        brand.add(portalLbl);

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        navButtons.setOpaque(false);

        JButton btnDash = Theme.createSecondaryButton("Command Center");
        JButton btnInc = Theme.createSecondaryButton("All Incidents");
        JButton btnUsers = Theme.createSecondaryButton("Users");
        JButton btnAnalysts = Theme.createSecondaryButton("Analysts");
        JButton btnIntel = Theme.createSecondaryButton("Threat Intel");
        JButton btnSla = Theme.createSecondaryButton("SLA Monitor");
        JButton btnAudit = Theme.createSecondaryButton("Audit Logs");
        JButton btnSim = Theme.createSuccessButton("⚡ Simulator");
        JButton btnReports = Theme.createSecondaryButton("Reports");
        JButton btnSettings = Theme.createSecondaryButton("⚙ Settings");
        JButton btnTheme = Theme.createThemeToggleButton(this::rebuildUI);

        // Store secondary nav buttons for active-state management
        navBtns = new JButton[]{btnDash, btnInc, btnUsers, btnAnalysts, btnIntel, btnSla, btnAudit, null, btnReports, btnSettings};
        // Restore active state after rebuild
        for (int i = 0; i < NAV_CARDS.length; i++) {
            if (navBtns[i] != null && NAV_CARDS[i].equals(currentCard)) {
                Theme.setNavButtonActive(navBtns[i], true);
            }
        }

        JLabel adminPill = Theme.createBadge(session.getUsername() + " (SUPERMOD)", Theme.COLOR_DANGER,
            Theme.isDarkMode() ? new Color(248, 81, 73, 30) : new Color(220, 38, 38, 30));
        JButton btnLogout = Theme.createDangerButton("Logout");

        navButtons.add(btnDash);
        navButtons.add(btnInc);
        navButtons.add(btnUsers);
        navButtons.add(btnAnalysts);
        navButtons.add(btnIntel);
        navButtons.add(btnSla);
        navButtons.add(btnAudit);
        navButtons.add(btnSim);
        navButtons.add(btnReports);
        navButtons.add(btnSettings);
        navButtons.add(btnTheme);
        navButtons.add(Box.createHorizontalStrut(6));
        navButtons.add(adminPill);
        navButtons.add(btnLogout);

        nav.add(brand, BorderLayout.WEST);
        nav.add(navButtons, BorderLayout.EAST);

        // Panels
        dashboardPanel = new SupermodDashboardPanel(session, this::showSection);
        incidentsPanel = new GlobalIncidentsPanel(session);
        userPanel = new UserManagementPanel(session);
        analystPanel = new AnalystManagementPanel(session);
        threatIntelPanel = new ThreatIntelManagementPanel(session);
        slaPanel = new SlaMonitoringPanel(session);
        auditPanel = new AuditLogsPanel(session);
        simPanel = new SimulationCenterPanel(session);
        reportsPanel = new ReportsPanel(session);
        settingsPanel = new SystemSettingsPanel(session);

        contentArea.add(dashboardPanel, "DASHBOARD");
        contentArea.add(incidentsPanel, "INCIDENTS");
        contentArea.add(userPanel, "USERS");
        contentArea.add(analystPanel, "ANALYSTS");
        contentArea.add(threatIntelPanel, "INTEL");
        contentArea.add(slaPanel, "SLA");
        contentArea.add(auditPanel, "AUDIT");
        contentArea.add(simPanel, "SIMULATOR");
        contentArea.add(reportsPanel, "REPORTS");
        contentArea.add(settingsPanel, "SETTINGS");

        root.add(nav, BorderLayout.NORTH);
        root.add(contentArea, BorderLayout.CENTER);

        add(root);

        // Listeners
        btnDash.addActionListener(e -> showSection("DASHBOARD"));
        btnInc.addActionListener(e -> showSection("INCIDENTS"));
        btnUsers.addActionListener(e -> showSection("USERS"));
        btnAnalysts.addActionListener(e -> showSection("ANALYSTS"));
        btnIntel.addActionListener(e -> showSection("INTEL"));
        btnSla.addActionListener(e -> showSection("SLA"));
        btnAudit.addActionListener(e -> showSection("AUDIT"));
        btnSim.addActionListener(e -> showSection("SIMULATOR"));
        btnReports.addActionListener(e -> showSection("REPORTS"));
        btnSettings.addActionListener(e -> showSection("SETTINGS"));

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Sign out of Supermod Command Center?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                SessionManager.logout();
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
    }

    private void showSection(String name) {
        currentCard = name;
        // Update active nav button state
        if (navBtns != null) {
            for (int i = 0; i < NAV_CARDS.length; i++) {
                if (navBtns[i] != null) {
                    Theme.setNavButtonActive(navBtns[i], NAV_CARDS[i].equalsIgnoreCase(name));
                }
            }
        }
        if ("DASHBOARD".equalsIgnoreCase(name)) {
            dashboardPanel.refreshData();
        } else if ("INCIDENTS".equalsIgnoreCase(name)) {
            incidentsPanel.refreshIncidents();
        } else if ("USERS".equalsIgnoreCase(name)) {
            userPanel.refreshUsers();
        } else if ("ANALYSTS".equalsIgnoreCase(name)) {
            analystPanel.refreshAnalysts();
        } else if ("INTEL".equalsIgnoreCase(name)) {
            threatIntelPanel.refreshThreatIntel();
        } else if ("SLA".equalsIgnoreCase(name)) {
            slaPanel.refreshSlaView();
        } else if ("AUDIT".equalsIgnoreCase(name)) {
            auditPanel.refreshAuditLogs();
        } else if ("SIMULATOR".equalsIgnoreCase(name)) {
            simPanel.refreshCounters();
        } else if ("REPORTS".equalsIgnoreCase(name)) {
            reportsPanel.refreshIncidentsDropdown();
        } else if ("SETTINGS".equalsIgnoreCase(name)) {
            settingsPanel.loadSettings();
        }
        cardLayout.show(contentArea, name);
    }
}
