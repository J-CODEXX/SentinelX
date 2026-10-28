package com.cybersentinel.ui.analyst;

import com.cybersentinel.config.AppConfig;
import com.cybersentinel.engine.SimulationEngine;
import com.cybersentinel.model.Incident;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.security.SessionManager;
import com.cybersentinel.ui.common.LoginFrame;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AnalystMainFrame extends JFrame {
    private final UserSession session;
    private CardLayout cardLayout;
    private JPanel contentArea;

    private AnalystDashboardPanel dashboardPanel;
    private IncidentQueuePanel queuePanel;
    private InvestigationWorkspacePanel workspacePanel;
    private String currentCard = "DASHBOARD";
    private Incident activeIncident = null;

    // Nav button references for active-state highlighting
    private JButton activeNavBtn = null;

    public AnalystMainFrame(UserSession session) {
        this.session = session;
        setTitle(AppConfig.APP_NAME + " — Security Operations Center (Analyst: " + session.getUsername() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_CANVAS);

        initUI();
    }

    private void rebuildUI() {
        getContentPane().removeAll();
        initUI();
        if (activeIncident != null) {
            workspacePanel.loadIncident(activeIncident);
        }
        revalidate();
        repaint();
        if ("QUEUE".equals(currentCard)) {
            queuePanel.refreshQueue();
            cardLayout.show(contentArea, "QUEUE");
        } else if ("WORKSPACE".equals(currentCard)) {
            cardLayout.show(contentArea, "WORKSPACE");
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
            new EmptyBorder(10, 20, 10, 20)
        ));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brand.setOpaque(false);
        JLabel logo = new JLabel("🛡️ " + AppConfig.APP_NAME);
        logo.setFont(Theme.FONT_TITLE);
        logo.setForeground(Theme.COLOR_PRIMARY);

        JLabel portalLbl = new JLabel("•  SOC Analyst Cockpit");
        portalLbl.setFont(Theme.FONT_REGULAR);
        portalLbl.setForeground(Theme.TEXT_MUTED);

        brand.add(logo);
        brand.add(portalLbl);

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        navButtons.setOpaque(false);

        JButton btnDash = Theme.createSecondaryButton("SOC Dashboard");
        JButton btnQueue = Theme.createSecondaryButton("Incident Queue");
        JButton btnWork = Theme.createSecondaryButton("Investigation Workspace");
        JButton btnSim = Theme.createSuccessButton("⚡ SIMULATE INCIDENT");
        JButton btnTheme = Theme.createThemeToggleButton(this::rebuildUI);

        JLabel analystPill = Theme.createBadge(session.getUsername() + " (ANALYST)", Theme.COLOR_PURPLE,
            Theme.isDarkMode() ? new Color(163, 113, 247, 30) : new Color(124, 58, 237, 30));
        JButton btnLogout = Theme.createDangerButton("Logout");

        navButtons.add(btnDash);
        navButtons.add(btnQueue);
        navButtons.add(btnWork);
        navButtons.add(btnSim);
        navButtons.add(btnTheme);
        navButtons.add(Box.createHorizontalStrut(8));
        navButtons.add(analystPill);
        navButtons.add(btnLogout);

        // Restore active button state after rebuild
        if ("DASHBOARD".equals(currentCard)) Theme.setNavButtonActive(btnDash, true);
        else if ("QUEUE".equals(currentCard)) Theme.setNavButtonActive(btnQueue, true);
        else if ("WORKSPACE".equals(currentCard)) Theme.setNavButtonActive(btnWork, true);

        nav.add(brand, BorderLayout.WEST);
        nav.add(navButtons, BorderLayout.EAST);

        // Content Area Panels
        dashboardPanel = new AnalystDashboardPanel(session, (filter) -> {
            queuePanel.applyExternalFilter(filter);
            currentCard = "QUEUE";
            cardLayout.show(contentArea, "QUEUE");
        });

        queuePanel = new IncidentQueuePanel(session, (incident) -> {
            activeIncident = incident;
            workspacePanel.loadIncident(incident);
            currentCard = "WORKSPACE";
            cardLayout.show(contentArea, "WORKSPACE");
        });

        workspacePanel = new InvestigationWorkspacePanel(session);

        contentArea.add(dashboardPanel, "DASHBOARD");
        contentArea.add(queuePanel, "QUEUE");
        contentArea.add(workspacePanel, "WORKSPACE");

        root.add(nav, BorderLayout.NORTH);
        root.add(contentArea, BorderLayout.CENTER);

        add(root);

        // Action Listeners
        btnDash.addActionListener(e -> {
            currentCard = "DASHBOARD";
            setActiveNavBtn(btnDash, new JButton[]{btnDash, btnQueue, btnWork});
            dashboardPanel.refreshData();
            cardLayout.show(contentArea, "DASHBOARD");
        });

        btnQueue.addActionListener(e -> {
            currentCard = "QUEUE";
            setActiveNavBtn(btnQueue, new JButton[]{btnDash, btnQueue, btnWork});
            queuePanel.refreshQueue();
            cardLayout.show(contentArea, "QUEUE");
        });

        btnWork.addActionListener(e -> {
            currentCard = "WORKSPACE";
            setActiveNavBtn(btnWork, new JButton[]{btnDash, btnQueue, btnWork});
            cardLayout.show(contentArea, "WORKSPACE");
        });

        btnSim.addActionListener(e -> {
            try {
                Incident sim = SimulationEngine.simulateIncident("RANDOM", null, null, session);
                activeIncident = sim;
                queuePanel.refreshQueue();
                dashboardPanel.refreshData();
                workspacePanel.loadIncident(sim);
                currentCard = "WORKSPACE";
                cardLayout.show(contentArea, "WORKSPACE");
                JOptionPane.showMessageDialog(this,
                    "Simulated Incident " + sim.getIncidentCode() + " created and loaded into workspace!",
                    "Simulated", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Simulation error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Sign out of SOC Analyst Cockpit?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                SessionManager.logout();
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
    }

    private void setActiveNavBtn(JButton active, JButton[] all) {
        activeNavBtn = active;
        for (JButton b : all) {
            Theme.setNavButtonActive(b, b == active);
        }
    }
}
