package it.uniroma3.cyberlab.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report")
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportReason reason;
    
    @Column(name = "additional_details", columnDefinition = "TEXT")
    @Size(max = 500, message = "Additional details must not exceed 500 characters")
    private String additionalDetails;
    
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;
    
    @Column(name = "review_date")
    private LocalDateTime reviewDate;
    
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;
    
    // ========== CAMPI AGGIUNTI PER COMPLETEZZA ==========
    @Column(columnDefinition = "TEXT")
    private String resolution;
    
    @Column(name = "resolved_date")
    private LocalDateTime resolvedDate;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;
    
    // ========== AGGIUNTA RELAZIONE CON POST ==========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;
    
    // ========== AGGIUNTA RELAZIONE CON UTENTE SEGNALATO ==========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy; // Admin who reviewed the report
    
    // ========== AGGIUNTA RELAZIONE CON CHI HA RISOLTO ==========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy; // Admin who resolved the report
    
    // Enums
    public enum ReportReason {
        SPAM("Spam or Irrelevant Content"),
        HARASSMENT("Harassment or Abuse"),
        INAPPROPRIATE_CONTENT("Inappropriate Content"),
        MALICIOUS_CODE("Malicious Code"),
        COPYRIGHT_VIOLATION("Copyright Violation"),
        MISINFORMATION("Misinformation"),
        HATE_SPEECH("Hate Speech"),
        ILLEGAL_CONTENT("Illegal Content"),
        OTHER("Other");
        
        private final String displayName;
        
        ReportReason(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ReportStatus {
        PENDING("Pending Review", "#ffc107"),
        UNDER_REVIEW("Under Review", "#17a2b8"),
        RESOLVED("Resolved", "#28a745"),
        DISMISSED("Dismissed", "#6c757d"),
        ESCALATED("Escalated", "#dc3545");
        
        private final String displayName;
        private final String color;
        
        ReportStatus(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    // Constructors
    public Report() {}
    
    public Report(Comment comment, User reportedBy, ReportReason reason) {
        this.comment = comment;
        this.reportedBy = reportedBy;
        this.reason = reason;
    }
    
    public Report(Comment comment, User reportedBy, ReportReason reason, String additionalDetails) {
        this.comment = comment;
        this.reportedBy = reportedBy;
        this.reason = reason;
        this.additionalDetails = additionalDetails;
    }
    
    // ========== COSTRUTTORI AGGIUNTI ==========
    public Report(Post post, User reportedBy, ReportReason reason) {
        this.post = post;
        this.reportedBy = reportedBy;
        this.reason = reason;
    }
    
    public Report(User reportedUser, User reportedBy, ReportReason reason) {
        this.reportedUser = reportedUser;
        this.reportedBy = reportedBy;
        this.reason = reason;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public ReportReason getReason() {
        return reason;
    }
    
    public void setReason(ReportReason reason) {
        this.reason = reason;
    }
    
    public String getAdditionalDetails() {
        return additionalDetails;
    }
    
    public void setAdditionalDetails(String additionalDetails) {
        this.additionalDetails = additionalDetails;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public ReportStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReportStatus status) {
        this.status = status;
        if (status != ReportStatus.PENDING && this.reviewDate == null) {
            this.reviewDate = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getReviewDate() {
        return reviewDate;
    }
    
    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }
    
    public String getAdminNotes() {
        return adminNotes;
    }
    
    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
    
    // ========== GETTER/SETTER CAMPI AGGIUNTI ==========
    public String getResolution() {
        return resolution;
    }
    
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
    
    public LocalDateTime getResolvedDate() {
        return resolvedDate;
    }
    
    public void setResolvedDate(LocalDateTime resolvedDate) {
        this.resolvedDate = resolvedDate;
    }
    
    public Comment getComment() {
        return comment;
    }
    
    public void setComment(Comment comment) {
        this.comment = comment;
    }
    
    // ========== GETTER/SETTER POST ==========
    public Post getPost() {
        return post;
    }
    
    public void setPost(Post post) {
        this.post = post;
    }
    
    // ========== GETTER/SETTER REPORTED USER ==========
    public User getReportedUser() {
        return reportedUser;
    }
    
    public void setReportedUser(User reportedUser) {
        this.reportedUser = reportedUser;
    }
    
    public User getReportedBy() {
        return reportedBy;
    }
    
    public void setReportedBy(User reportedBy) {
        this.reportedBy = reportedBy;
    }
    
    public User getReviewedBy() {
        return reviewedBy;
    }
    
    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
        if (reviewedBy != null && this.reviewDate == null) {
            this.reviewDate = LocalDateTime.now();
        }
    }
    
    // ========== GETTER/SETTER RESOLVED BY ==========
    public User getResolvedBy() {
        return resolvedBy;
    }
    
    public void setResolvedBy(User resolvedBy) {
        this.resolvedBy = resolvedBy;
        if (resolvedBy != null && this.resolvedDate == null) {
            this.resolvedDate = LocalDateTime.now();
        }
    }
    
    // Helper methods
    public boolean isPending() {
        return status == ReportStatus.PENDING;
    }
    
    public boolean isResolved() {
        return status == ReportStatus.RESOLVED;
    }
    
    public boolean isDismissed() {
        return status == ReportStatus.DISMISSED;
    }
    
    public boolean isUnderReview() {
        return status == ReportStatus.UNDER_REVIEW;
    }
    
    public boolean isEscalated() {
        return status == ReportStatus.ESCALATED;
    }
    
    public boolean isReviewed() {
        return reviewDate != null && reviewedBy != null;
    }
    
    // ========== METODI HELPER AGGIUNTI ==========
    public boolean isCompleted() {
        return isResolved() || isDismissed();
    }
    
    public String getReportedContent() {
        if (post != null) return "Post: " + post.getTitle();
        if (comment != null) return "Comment on post: " + comment.getPost().getTitle();
        if (reportedUser != null) return "User: " + reportedUser.getUsername();
        return "Unknown content";
    }
    
    public String getReportType() {
        if (post != null) return "POST";
        if (comment != null) return "COMMENT";
        if (reportedUser != null) return "USER";
        return "UNKNOWN";
    }
    
    public void markAsReviewed(User admin, ReportStatus newStatus, String notes) {
        this.reviewedBy = admin;
        this.status = newStatus;
        this.reviewDate = LocalDateTime.now();
        this.adminNotes = notes;
    }
    
    public void resolve(User admin, String resolution) {
        this.resolvedBy = admin;
        this.status = ReportStatus.RESOLVED;
        this.resolvedDate = LocalDateTime.now();
        this.resolution = resolution;
        if (this.reviewDate == null) {
            this.reviewDate = LocalDateTime.now();
            this.reviewedBy = admin;
        }
    }
    
    public void dismiss(User admin, String reason) {
        this.resolvedBy = admin;
        this.status = ReportStatus.DISMISSED;
        this.resolvedDate = LocalDateTime.now();
        this.resolution = reason;
        if (this.reviewDate == null) {
            this.reviewDate = LocalDateTime.now();
            this.reviewedBy = admin;
        }
    }
    
    public void escalate(User admin, String notes) {
        markAsReviewed(admin, ReportStatus.ESCALATED, notes);
    }
    
    public String getTimeSinceCreated() {
        // Simple time formatting - you could use a proper time library
        LocalDateTime now = LocalDateTime.now();
        long hours = java.time.Duration.between(createdDate, now).toHours();
        if (hours < 1) {
            long minutes = java.time.Duration.between(createdDate, now).toMinutes();
            return minutes + " minutes ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else {
            long days = java.time.Duration.between(createdDate, now).toDays();
            return days + " days ago";
        }
    }
    
    // Override methods
    @Override
    public String toString() {
        return "Report{" +
                "id=" + id +
                ", reason=" + reason +
                ", status=" + status +
                ", reportedBy=" + (reportedBy != null ? reportedBy.getUsername() : "null") +
                ", reportType=" + getReportType() +
                ", createdDate=" + createdDate +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Report report = (Report) obj;
        return id != null && id.equals(report.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}