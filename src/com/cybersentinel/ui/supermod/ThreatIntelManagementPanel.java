package com.cybersentinel.ui.supermod;

import com.cybersentinel.dao.ThreatIntelDAO;
import com.cybersentinel.model.ThreatIntelligence;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.service.AuditService;
import com.cybersentinel.ui.common.StatusBadgeRenderer;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ThreatIntelManagementPanel extends JPanel {
    private final UserSession session;
    private final ThreatIntelDAO threatIntelDAO = new ThreatIntelDAO();
    private final AuditService auditService = new AuditService();

    private JTextField searchField;
    private JComboBox<String> classFilter;
    private JComboBox<String> threatFilter;
    private JTable table;
    private DefaultTableModel model;

    public ThreatIntelManagementPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BG_CANVAS);
        setBorder(new EmptyBorder(14, 18, 14, 18));

        initUI();
        refreshThreatIntel();
    }

    private void initUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Threat Intelligence Master Repository (IOC Feed)");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_LIGHT);
        topBar.add(title, BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnBar.setOpaque(false);

        JButton addBtn = Theme.createPrimaryButton("+ Add Threat IOC");
        addBtn.addActionListener(e -> handleAddIoc());

        JButton editBtn = Theme.createSecondaryButton("Edit IOC");
        editBtn.addActionListener(e -> handleEditIoc());

        JButton deleteBtn = Theme.createDangerButton("Delete IOC");
        deleteBtn.addActionListener(e -> handleDeleteIoc());

        btnBar.add(addBtn);
        btnBar.add(editBtn);
        btnBar.add(deleteBtn);

        topBar.add(btnBar, BorderLayout.EAST);

        // Filter Card
        JPanel filterCard = Theme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        searchField = Theme.createTextField();
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setToolTipText("Search IOC value, source, description");

        classFilter = Theme.createComboBox(new String[]{"ALL", "MALICIOUS", "SUSPICIOUS", "BENIGN"});
        classFilter.setPreferredSize(new Dimension(130, 30));

        threatFilter = Theme.createComboBox(new String[]{"ALL", "CRITICAL", "HIGH", "MEDIUM", "LOW"});
        threatFilter.setPreferredSize(new Dimension(110, 30));

        JButton applyBtn = Theme.createPrimaryButton("Filter");
        applyBtn.addActionListener(e -> refreshThreatIntel());

        JButton resetBtn = Theme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            classFilter.setSelectedIndex(0);
            threatFilter.setSelectedIndex(0);
            refreshThreatIntel();
        });

        filterCard.add(Theme.createFilterLabel("Search:"));
        filterCard.add(searchField);
        filterCard.add(Theme.createFilterLabel("Classification:"));
        filterCard.add(classFilter);
        filterCard.add(Theme.createFilterLabel("Threat Level:"));
        filterCard.add(threatFilter);
        filterCard.add(applyBtn);
        filterCard.add(resetBtn);

        // Table Card
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"ID", "Type", "Indicator Value", "Classification", "Threat Level", "Source Feed", "Description", "Created"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(filterCard, BorderLayout.NORTH);
        center.add(tableCard, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    public void refreshThreatIntel() {
        model.setRowCount(0);

        String q = searchField.getText().trim();
        String c = (String) classFilter.getSelectedItem();
        String t = (String) threatFilter.getSelectedItem();

        try {
            List<ThreatIntelligence> list = threatIntelDAO.findAll(q, c, t);
            for (ThreatIntelligence item : list) {
                model.addRow(new Object[]{
                    item.getId(),
                    item.getType(),
                    item.getValue(),
                    item.getClassification(),
                    item.getThreatLevel(),
                    item.getSource(),
                    item.getDescription(),
                    item.getCreatedAt().toString().substring(0, 10)
                });
            }
        } catch (SQLException e) {
            System.err.println("[THREAT_INTEL] Error: " + e.getMessage());
        }
    }

    private void handleAddIoc() {
        JComboBox<String> typeCb = Theme.createComboBox(new String[]{"IP Address", "Domain", "URL", "File Hash", "Email Address", "Username", "Hostname"});
        JTextField valField = Theme.createTextField();
        JComboBox<String> classCb = Theme.createComboBox(new String[]{"MALICIOUS", "SUSPICIOUS", "BENIGN"});
        JComboBox<String> threatCb = Theme.createComboBox(new String[]{"CRITICAL", "HIGH", "MEDIUM", "LOW"});
        JTextField srcField = Theme.createTextField();
        srcField.setText("Internal Threat Feed");
        JTextField descField = Theme.createTextField();

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.add(Theme.createFilterLabel("IOC Type:")); form.add(typeCb);
        form.add(Theme.createFilterLabel("IOC Value:")); form.add(valField);
        form.add(Theme.createFilterLabel("Classification:")); form.add(classCb);
        form.add(Theme.createFilterLabel("Threat Level:")); form.add(threatCb);
        form.add(Theme.createFilterLabel("Source Feed:")); form.add(srcField);
        form.add(Theme.createFilterLabel("Description:")); form.add(descField);

        int res = JOptionPane.showConfirmDialog(this, form, "Add Threat Intelligence Indicator", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION && !valField.getText().trim().isBlank()) {
            ThreatIntelligence item = new ThreatIntelligence(
                (String) typeCb.getSelectedItem(),
                valField.getText().trim(),
                (String) classCb.getSelectedItem(),
                (String) threatCb.getSelectedItem(),
                srcField.getText().trim(),
                descField.getText().trim()
            );
            try {
                threatIntelDAO.create(item);
                auditService.log(session, "ADD_THREAT_INTEL", "THREAT_INTEL", item.getValue(), "Added IOC to Threat Intel DB: " + item.getValue());
                refreshThreatIntel();
                JOptionPane.showMessageDialog(this, "IOC added to master threat intelligence library.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to add IOC: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditIoc() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an IOC from the table to edit.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        long id = (Long) model.getValueAt(row, 0);
        String val = (String) model.getValueAt(row, 2);
        String desc = (String) model.getValueAt(row, 6);

        JTextField descField = Theme.createTextField();
        descField.setText(desc);
        JComboBox<String> classCb = Theme.createComboBox(new String[]{"MALICIOUS", "SUSPICIOUS", "BENIGN"});
        classCb.setSelectedItem(model.getValueAt(row, 3));
        JComboBox<String> threatCb = Theme.createComboBox(new String[]{"CRITICAL", "HIGH", "MEDIUM", "LOW"});
        threatCb.setSelectedItem(model.getValueAt(row, 4));

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(Theme.createFilterLabel("Classification:")); form.add(classCb);
        form.add(Theme.createFilterLabel("Threat Level:")); form.add(threatCb);
        form.add(Theme.createFilterLabel("Description:")); form.add(descField);

        int res = JOptionPane.showConfirmDialog(this, form, "Edit IOC — " + val, JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                ThreatIntelligence item = new ThreatIntelligence(
                    (String) model.getValueAt(row, 1),
                    val,
                    (String) classCb.getSelectedItem(),
                    (String) threatCb.getSelectedItem(),
                    (String) model.getValueAt(row, 5),
                    descField.getText().trim()
                );
                item.setId(id);
                threatIntelDAO.update(item);
                auditService.log(session, "EDIT_THREAT_INTEL", "THREAT_INTEL", val, "Updated Threat Intel IOC: " + val);
                refreshThreatIntel();
                JOptionPane.showMessageDialog(this, "IOC updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to update: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteIoc() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an IOC to delete.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        long id = (Long) model.getValueAt(row, 0);
        String val = (String) model.getValueAt(row, 2);

        int conf = JOptionPane.showConfirmDialog(this, "Permanently delete IOC " + val + " from Threat Intelligence database?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            try {
                threatIntelDAO.delete(id);
                auditService.log(session, "DELETE_THREAT_INTEL", "THREAT_INTEL", val, "Deleted IOC: " + val);
                refreshThreatIntel();
                JOptionPane.showMessageDialog(this, "IOC deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
