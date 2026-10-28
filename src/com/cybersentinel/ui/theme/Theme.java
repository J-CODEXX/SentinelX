package com.cybersentinel.ui.theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public final class Theme {

    private static boolean darkMode = true;
    private static final List<Runnable> listeners = new ArrayList<>();

    // Dynamic Color Palette
    public static Color BG_CANVAS;
    public static Color BG_CARD;
    public static Color BG_CARD_HOVER;
    public static Color BG_INPUT;
    public static Color BORDER_COLOR;
    public static Color BORDER_INPUT;
    public static Color BORDER_ACTIVE;

    public static Color TEXT_PRIMARY;
    public static Color TEXT_MUTED;
    public static Color TEXT_LIGHT;
    public static Color NAV_BG;
    public static Color TABLE_HEADER_BG;

    // Accent Status Colors
    public static Color COLOR_PRIMARY;
    public static Color COLOR_SUCCESS;
    public static Color COLOR_WARNING;
    public static Color COLOR_DANGER;
    public static Color COLOR_PURPLE;
    public static Color COLOR_SIMULATED;

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    static {
        setDarkMode(true);
    }

    private Theme() {}

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void toggleTheme() {
        setDarkMode(!darkMode);
    }

    public static void setDarkMode(boolean dark) {
        darkMode = dark;
        if (darkMode) {
            // Modern SOC Cyber Dark - High Contrast & Crisp Visibility
            BG_CANVAS = new Color(11, 15, 25);         // Deep SOC canvas (#0B0F19)
            BG_CARD = new Color(22, 29, 44);           // Elevated SOC card (#161D2C)
            BG_CARD_HOVER = new Color(32, 42, 64);
            BG_INPUT = new Color(13, 17, 26);          // Inset high-contrast input (#0D111A)
            BORDER_COLOR = new Color(55, 70, 95);      // Visible card border (#37465F)
            BORDER_INPUT = new Color(85, 108, 145);    // Prominent visible textbox border (#556C91)
            BORDER_ACTIVE = new Color(56, 139, 253);   // Glowing focus cyan-blue (#388BFD)

            TEXT_PRIMARY = new Color(113, 127, 134);   // Bright crystal-clear white-silver (#F8FAFD)
            TEXT_MUTED = new Color(165, 180, 204);     // High legibility secondary text (#A5B4CC)
            TEXT_LIGHT = new Color(255, 255, 255);     // 100% white
            NAV_BG = new Color(14, 18, 30);            // Top nav bar (#0E121E)
            TABLE_HEADER_BG = new Color(16, 22, 35);

            COLOR_PRIMARY = new Color(56, 139, 253);   // Cyber Blue
            COLOR_SUCCESS = new Color(46, 160, 67);    // Resolved Green
            COLOR_WARNING = new Color(210, 153, 34);   // Amber
            COLOR_DANGER = new Color(248, 81, 73);     // Critical Red
            COLOR_PURPLE = new Color(163, 113, 247);   // Purple
            COLOR_SIMULATED = new Color(88, 166, 255); // Simulated Blue
        } else {
            // Executive Enterprise Light - Clean, High Contrast & Modern
            BG_CANVAS = new Color(243, 246, 251);      // Soft executive light gray/blue (#F3F6FB)
            BG_CARD = new Color(255, 255, 255);        // Pure white card (#FFFFFF)
            BG_CARD_HOVER = new Color(245, 248, 253);
            BG_INPUT = new Color(255, 255, 255);       // Pure white textbox
            BORDER_COLOR = new Color(218, 226, 237);   // Clean card border (#DAE2ED)
            BORDER_INPUT = new Color(175, 190, 212);   // Distinct visible textbox border (#AFBED4)
            BORDER_ACTIVE = new Color(14, 116, 224);   // Vibrant royal blue (#0E74E0)

            TEXT_PRIMARY = new Color(18, 24, 38);      // Deep rich charcoal (#121826) - max contrast
            TEXT_MUTED = new Color(85, 100, 120);      // Readable slate gray (#556478)
            TEXT_LIGHT = new Color(10, 14, 24);        // Dark headline text
            NAV_BG = new Color(255, 255, 255);         // Clean white nav
            TABLE_HEADER_BG = new Color(240, 244, 250);

            COLOR_PRIMARY = new Color(14, 116, 224);
            COLOR_SUCCESS = new Color(22, 142, 60);
            COLOR_WARNING = new Color(202, 108, 0);
            COLOR_DANGER = new Color(220, 38, 38);
            COLOR_PURPLE = new Color(124, 58, 237);
            COLOR_SIMULATED = new Color(2, 132, 199);
        }

        applyUIManagerDefaults();

        for (Runnable l : listeners) {
            try {
                l.run();
            } catch (Exception ignored) {}
        }
    }

    public static void addThemeChangeListener(Runnable listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void applyUIManagerDefaults() {
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("Label.font", FONT_REGULAR);
        UIManager.put("CheckBox.foreground", TEXT_PRIMARY);
        UIManager.put("CheckBox.font", FONT_REGULAR);
        UIManager.put("RadioButton.foreground", TEXT_PRIMARY);
        UIManager.put("Panel.background", BG_CANVAS);
        UIManager.put("OptionPane.background", BG_CARD);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("TextField.background", BG_INPUT);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", COLOR_PRIMARY);
        UIManager.put("TextArea.background", BG_INPUT);
        UIManager.put("TextArea.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.background", BG_INPUT);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("Table.background", BG_CARD);
        UIManager.put("Table.foreground", TEXT_PRIMARY);
        UIManager.put("Table.gridColor", BORDER_COLOR);
        UIManager.put("TableHeader.background", TABLE_HEADER_BG);
        UIManager.put("TableHeader.foreground", TEXT_MUTED);
        UIManager.put("Viewport.background", BG_CARD);
        UIManager.put("ScrollPane.background", BG_CARD);
    }

    public static JButton createThemeToggleButton(Runnable onToggle) {
        String label = isDarkMode() ? "☀️ Light Mode" : "🌙 Dark Mode";
        JButton btn = createSecondaryButton(label);
        btn.setToolTipText(isDarkMode() ? "Switch to Clean Light Mode" : "Switch to SOC Dark Mode");
        btn.addActionListener(e -> {
            toggleTheme();
            if (onToggle != null) {
                onToggle.run();
            }
        });
        return btn;
    }

    public static JLabel createFilterLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(BORDER_COLOR);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));
        return panel;
    }

    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, COLOR_PRIMARY, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, COLOR_SUCCESS, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createStyledButton(text, COLOR_DANGER, Color.WHITE);
    }

    public static JButton createWarningButton(String text) {
        return createStyledButton(text, COLOR_WARNING, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = Boolean.TRUE.equals(getClientProperty("nav.active"));
                if (active) {
                    // Active nav: filled with primary accent color
                    Color bg = isDarkMode()
                        ? new Color(56, 139, 253, 55)
                        : new Color(14, 116, 224, 30);
                    g2.setColor(bg);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                    g2.setColor(COLOR_PRIMARY);
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                    // Bottom accent bar
                    g2.setColor(COLOR_PRIMARY);
                    g2.fillRoundRect(4, getHeight() - 3, getWidth() - 8, 3, 3, 3);
                } else {
                    Color base = isDarkMode()
                        ? (getModel().isRollover() ? BG_CARD_HOVER : BG_CARD)
                        : (getModel().isRollover() ? new Color(238, 242, 248) : new Color(248, 250, 253));
                    g2.setColor(base);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                    g2.setColor(BORDER_COLOR);
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(TEXT_PRIMARY);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Sets the active/selected state on a nav button created by createSecondaryButton.
     * Active buttons get a highlighted border + accent underline bar.
     */
    public static void setNavButtonActive(JButton btn, boolean active) {
        btn.putClientProperty("nav.active", active);
        btn.setForeground(active ? COLOR_PRIMARY : TEXT_PRIMARY);
        btn.repaint();
    }

    private static JButton createStyledButton(String text, Color baseColor, Color textColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(baseColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(baseColor.brighter());
                } else {
                    g2.setColor(baseColor);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(textColor);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setOpaque(true);
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(COLOR_PRIMARY);
        tf.setFont(FONT_REGULAR);
        tf.setSelectedTextColor(Color.WHITE);
        tf.setSelectionColor(new Color(56, 139, 253, 160));
        tf.setDisabledTextColor(TEXT_MUTED);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        // Focus indicator for high visibility
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                tf.setBackground(BG_INPUT);
                tf.setForeground(TEXT_PRIMARY);
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_ACTIVE, 2, true),
                    BorderFactory.createEmptyBorder(5, 9, 5, 9)
                ));
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                tf.setBackground(BG_INPUT);
                tf.setForeground(TEXT_PRIMARY);
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_INPUT, 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
            }
        });
        return tf;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pf.setOpaque(false);
        pf.setBackground(BG_INPUT);
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(COLOR_PRIMARY);
        pf.setFont(FONT_REGULAR);
        pf.setSelectedTextColor(Color.WHITE);
        pf.setSelectionColor(new Color(56, 139, 253, 160));
        pf.setDisabledTextColor(TEXT_MUTED);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        pf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                pf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_ACTIVE, 2, true),
                    BorderFactory.createEmptyBorder(5, 9, 5, 9)
                ));
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                pf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_INPUT, 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
            }
        });
        return pf;
    }

    public static JTextArea createTextArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setBackground(BG_INPUT);
        ta.setForeground(TEXT_PRIMARY);
        ta.setCaretColor(COLOR_PRIMARY);
        ta.setFont(FONT_REGULAR);
        ta.setSelectedTextColor(Color.WHITE);
        ta.setSelectionColor(new Color(56, 139, 253, 160));
        ta.setDisabledTextColor(TEXT_PRIMARY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return ta;
    }

   public static <T> JComboBox<T> createComboBox(T[] items) {

    JComboBox<T> cb = new JComboBox<>(items);

    cb.setBackground(BG_INPUT);
    cb.setForeground(TEXT_PRIMARY);
    cb.setFont(FONT_REGULAR);
    cb.setFocusable(false);

    cb.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER_INPUT, 1, true),
        BorderFactory.createEmptyBorder(2, 6, 2, 6)
    ));

    cb.setRenderer(new DefaultListCellRenderer() {

        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            super.getListCellRendererComponent(
                    list,
                    value,
                    index,
                    isSelected,
                    cellHasFocus
            );

            setFont(FONT_REGULAR);
            setBorder(
                new EmptyBorder(5, 8, 5, 8)
            );

            if (isSelected) {

                setBackground(COLOR_PRIMARY);
                setForeground(Color.WHITE);

            } else {

                setBackground(BG_INPUT);
                setForeground(TEXT_PRIMARY);
            }

            return this;
        }
    });

    return cb;
}

    public static JLabel createBadge(String text, Color fgColor, Color bgColor) {
        JLabel badge = new JLabel(" " + text + " ", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(FONT_BOLD);
        badge.setForeground(fgColor);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        return badge;
    }

    public static JPanel createKpiCard(String title, String value, String subtitle, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(BORDER_COLOR);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 14, 10, 14));
        card.setLayout(new BorderLayout(2, 2));

        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(TEXT_MUTED);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valLbl.setForeground(accentColor != null ? accentColor : TEXT_PRIMARY);

        JLabel subLbl = new JLabel(subtitle != null ? subtitle : "");
        subLbl.setFont(FONT_SMALL);
        subLbl.setForeground(TEXT_MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);
        return card;
    }

    public static void styleTable(JTable table) {
        table.setOpaque(true);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(isDarkMode() ? new Color(56, 139, 253, 70) : new Color(14, 116, 224, 45));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TEXT_MUTED);
        header.setFont(FONT_BOLD);
        header.setPreferredSize(new Dimension(0, 34));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, isSelected, hasFocus, r, c);
                setOpaque(true);
                if (isSelected) {
                    comp.setBackground(isDarkMode() ? new Color(56, 139, 253, 70) : new Color(14, 116, 224, 45));
                    comp.setForeground(TEXT_PRIMARY);
                } else {
                    Color rowBg = isDarkMode()
                        ? (r % 2 == 0 ? BG_CARD : BG_CARD_HOVER)
                        : (r % 2 == 0 ? BG_CARD : new Color(248, 250, 253));
                    comp.setBackground(rowBg);
                    comp.setForeground(TEXT_PRIMARY);
                }
                if (comp instanceof JLabel) {
                    ((JLabel) comp).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                }
                return comp;
            }
        };

        table.setDefaultRenderer(Object.class, defaultRenderer);
        table.setDefaultRenderer(String.class, defaultRenderer);
        table.setDefaultRenderer(Number.class, defaultRenderer);
        table.setDefaultRenderer(Integer.class, defaultRenderer);
        table.setDefaultRenderer(Long.class, defaultRenderer);
        table.setDefaultRenderer(Double.class, defaultRenderer);
        table.setDefaultRenderer(Boolean.class, defaultRenderer);
        table.setDefaultRenderer(java.util.Date.class, defaultRenderer);

        SwingUtilities.invokeLater(() -> {
            Container p = table.getParent();
            if (p instanceof JViewport) {
                p.setBackground(BG_CARD);
                Container gp = p.getParent();
                if (gp instanceof JScrollPane) {
                    gp.setBackground(BG_CARD);
                    ((JScrollPane) gp).setBorder(BorderFactory.createEmptyBorder());
                    ((JScrollPane) gp).getViewport().setBackground(BG_CARD);
                }
            }
        });
    }
}
