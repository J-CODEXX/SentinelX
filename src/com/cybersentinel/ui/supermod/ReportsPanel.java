package com.cybersentinel.ui.supermod;

import com.cybersentinel.dao.IncidentDAO;
import com.cybersentinel.model.Incident;
import com.cybersentinel.model.UserSession;
import com.cybersentinel.service.ReportService;
import com.cybersentinel.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ReportsPanel extends JPanel {
    private final UserSession session;
    private final ReportService reportService = new ReportService();
    private final IncidentDAO incidentDAO = new IncidentDAO();

    private JComboBox<IncidentItem> incidentSelectCb;
    private JTextArea previewArea;
    private String currentReportContent = "";
    private String currentFilenamePrefix = "Report";

    private static class IncidentItem {
        final long id;
        final String code;
        final String title;
        IncidentItem(long id, String code, String title) { this.id = id; this.code = code; this.title = title; }
        @Override public String toString() { return code + " — " + title; }
    }

    public ReportsPanel(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BG_CANVAS);
        setBorder(new EmptyBorder(14, 18, 14, 18));

        initUI();
        refreshIncidentsDropdown();
    }

    private void initUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Security Operations Analytics & Compliance Reporting");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_LIGHT);
        topBar.add(title, BorderLayout.WEST);

        JButton exportBtn = Theme.createPrimaryButton("💾 Save / Export Report to File");
        exportBtn.addActionListener(e -> handleExportToFile());
        topBar.add(exportBtn, BorderLayout.EAST);

        // Control Selector Card
        JPanel ctrlCard = Theme.createCardPanel();
        ctrlCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        JButton genSummaryBtn = Theme.createSecondaryButton("Executive Security Summary");
        genSummaryBtn.addActionListener(e -> handleGenerateSummary());

        JButton genCsvBtn = Theme.createSecondaryButton("Export All Incidents (CSV)");
        genCsvBtn.addActionListener(e -> handleExportCsv());

        incidentSelectCb = new JComboBox<>();
        incidentSelectCb.setBackground(Theme.BG_INPUT);
        incidentSelectCb.setForeground(Theme.TEXT_PRIMARY);
        incidentSelectCb.setPreferredSize(new Dimension(280, 30));

        JButton genDossierBtn = Theme.createPrimaryButton("Generate Incident Dossier");
        genDossierBtn.addActionListener(e -> handleGenerateDossier());

        ctrlCard.add(genSummaryBtn);
        ctrlCard.add(genCsvBtn);
        ctrlCard.add(Theme.createFilterLabel("Select Incident:"));
        ctrlCard.add(incidentSelectCb);
        ctrlCard.add(genDossierBtn);

        // Preview Card
        JPanel previewCard = Theme.createCardPanel();
        previewCard.setLayout(new BorderLayout(0, 6));

        JLabel pTitle = new JLabel("DOCUMENT PREVIEW / OUTPUT");
        pTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        pTitle.setForeground(Theme.TEXT_MUTED);

        previewArea = Theme.createTextArea(20, 80);
        previewArea.setFont(Theme.FONT_MONO);
        previewArea.setEditable(false);
        previewArea.setText("Select a report type above to generate analysis and export documentation.");

        previewCard.add(pTitle, BorderLayout.NORTH);
        previewCard.add(new JScrollPane(previewArea), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(ctrlCard, BorderLayout.NORTH);
        center.add(previewCard, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    public void refreshIncidentsDropdown() {
        incidentSelectCb.removeAllItems();
        try {
            List<Incident> list = incidentDAO.findAll(null, null, null, null, null, null, null, null);
            for (Incident inc : list) {
                incidentSelectCb.addItem(new IncidentItem(inc.getId(), inc.getIncidentCode(), inc.getTitle()));
            }
        } catch (Exception ignored) {}
    }

    private void handleGenerateSummary() {
        try {
            String report = reportService.generateSecuritySummary();
            currentReportContent = report;
            currentFilenamePrefix = "Executive_Security_Summary";
            previewArea.setText(report);
            previewArea.setCaretPosition(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Generation error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleGenerateDossier() {
        IncidentItem item = (IncidentItem) incidentSelectCb.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Please select an incident from the dropdown.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            String dossier = reportService.generateIncidentDossier(item.id);
            currentReportContent = dossier;
            currentFilenamePrefix = "Dossier_" + item.code;
            previewArea.setText(dossier);
            previewArea.setCaretPosition(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Dossier error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleExportCsv() {
        try {
            File f = reportService.exportIncidentsCsv();
            JOptionPane.showMessageDialog(this, "Incident Inventory exported to:\n" + f.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "CSV Export error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleExportToFile() {
        if (currentReportContent.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please generate a report first before exporting.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            File f = reportService.exportToFile(currentReportContent, currentFilenamePrefix, "txt");
            JOptionPane.showMessageDialog(this, "Report exported to file:\n" + f.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
