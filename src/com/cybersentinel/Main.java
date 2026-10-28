package com.cybersentinel;

import com.cybersentinel.database.DatabaseConnection;
import com.cybersentinel.database.DatabaseInitializer;
import com.cybersentinel.ui.common.LoginFrame;

import javax.swing.*;
import java.awt.*;

public final class Main {

    public static void main(String[] args) {
        // Set system look and feel for native menus and window frames
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        com.cybersentinel.ui.theme.Theme.applyUIManagerDefaults();

        // Enable high-DPI text antialiasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Verify MySQL connectivity and bootstrap database schema + demo data
        SwingUtilities.invokeLater(() -> {
            try {
                if (!DatabaseConnection.testConnection()) {
                    System.err.println("[CYBERSENTINEL] MySQL connection check: attempting initial connection...");
                }
                DatabaseInitializer.initialize();
                System.out.println("[CYBERSENTINEL] Database verified and bootstrap complete.");
            } catch (Exception e) {
                System.err.println("[WARN] Initial bootstrap warning: " + e.getMessage());
                // Non-fatal, login screen will handle retry gracefully
            }

            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
