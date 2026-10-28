package com.cybersentinel.model;

import java.sql.Timestamp;

public class Evidence {
    private long id;
    private long incidentId;
    private String originalFilename;
    private String storedPath;
    private String description;
    private String sha256;
    private long fileSize;
    private long uploadedBy;
    private String uploadedByName = "User";
    private Timestamp createdAt;

    public Evidence() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getIncidentId() { return incidentId; }
    public void setIncidentId(long incidentId) { this.incidentId = incidentId; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getStoredPath() { return storedPath; }
    public void setStoredPath(String storedPath) { this.storedPath = storedPath; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public long getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(long uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getUploadedByName() { return uploadedByName; }
    public void setUploadedByName(String uploadedByName) { this.uploadedByName = uploadedByName != null ? uploadedByName : ""; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getFormattedSize() {
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
    }
}
