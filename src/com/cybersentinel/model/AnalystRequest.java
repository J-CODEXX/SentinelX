package com.cybersentinel.model;

import java.sql.Timestamp;

public class AnalystRequest {
    private long id;
    private String username;
    private String email;
    private String passwordHash;
    private String passwordSalt;
    private String status; // PENDING, APPROVED, REJECTED
    private String rejectionReason;
    private Long reviewedBy;
    private String reviewerUsername;
    private Timestamp createdAt;
    private Timestamp reviewedAt;

    public AnalystRequest() {}

    public AnalystRequest(long id, String username, String email, String passwordHash, String passwordSalt, String status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.status = status;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPasswordSalt() { return passwordSalt; }
    public void setPasswordSalt(String passwordSalt) { this.passwordSalt = passwordSalt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getReviewerUsername() { return reviewerUsername; }
    public void setReviewerUsername(String reviewerUsername) { this.reviewerUsername = reviewerUsername; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Timestamp reviewedAt) { this.reviewedAt = reviewedAt; }
}
