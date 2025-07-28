package it.uniroma3.cyberlab.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "lab_id"}))
public class UserProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgressStatus status = ProgressStatus.NOT_STARTED;
    
    @Column(name = "started_date")
    private LocalDateTime startedDate;
    
    @Column(name = "completed_date")
    private LocalDateTime completedDate;
    
    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;
    
    @Column(name = "time_spent")
    private Integer timeSpent = 0; // Minutes spent on the lab
    
    @Column(name = "completion_percentage")
    private Integer completionPercentage = 0;
    
    @Column(columnDefinition = "TEXT")
    private String notes; // User's personal notes
    
    @Column(name = "attempts_count")
    private Integer attemptsCount = 0;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;
    
    // Enums
    public enum ProgressStatus {
        NOT_STARTED("Not Started", "#6c757d"),
        IN_PROGRESS("In Progress", "#ffc107"),
        COMPLETED("Completed", "#28a745"),
        ABANDONED("Abandoned", "#dc3545");
        
        private final String displayName;
        private final String color;
        
        ProgressStatus(String displayName, String color) {
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
    public UserProgress() {}
    
    public UserProgress(User user, Lab lab) {
        this.user = user;
        this.lab = lab;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public ProgressStatus getStatus() {
        return status;
    }
    
    public void setStatus(ProgressStatus status) {
        this.status = status;
        
        // Automatically set dates based on status
        if (status == ProgressStatus.IN_PROGRESS && startedDate == null) {
            this.startedDate = LocalDateTime.now();
        }
        if (status == ProgressStatus.COMPLETED && completedDate == null) {
            this.completedDate = LocalDateTime.now();
            this.completionPercentage = 100;
        }
        this.lastAccessed = LocalDateTime.now();
    }
    
    public LocalDateTime getStartedDate() {
        return startedDate;
    }
    
    public void setStartedDate(LocalDateTime startedDate) {
        this.startedDate = startedDate;
    }
    
    public LocalDateTime getCompletedDate() {
        return completedDate;
    }
    
    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }
    
    public LocalDateTime getLastAccessed() {
        return lastAccessed;
    }
    
    public void setLastAccessed(LocalDateTime lastAccessed) {
        this.lastAccessed = lastAccessed;
    }
    
    public Integer getTimeSpent() {
        return timeSpent;
    }
    
    public void setTimeSpent(Integer timeSpent) {
        this.timeSpent = timeSpent;
    }
    
    public Integer getCompletionPercentage() {
        return completionPercentage;
    }
    
    public void setCompletionPercentage(Integer completionPercentage) {
        this.completionPercentage = Math.max(0, Math.min(100, completionPercentage));
        
        // Update status based on completion percentage
        if (completionPercentage == 100) {
            this.status = ProgressStatus.COMPLETED;
            if (this.completedDate == null) {
                this.completedDate = LocalDateTime.now();
            }
        } else if (completionPercentage > 0) {
            this.status = ProgressStatus.IN_PROGRESS;
        }
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Integer getAttemptsCount() {
        return attemptsCount;
    }
    
    public void setAttemptsCount(Integer attemptsCount) {
        this.attemptsCount = attemptsCount;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Lab getLab() {
        return lab;
    }
    
    public void setLab(Lab lab) {
        this.lab = lab;
    }
    
    // Helper methods
    public void startLab() {
        if (status == ProgressStatus.NOT_STARTED) {
            this.status = ProgressStatus.IN_PROGRESS;
            this.startedDate = LocalDateTime.now();
        }
        this.lastAccessed = LocalDateTime.now();
    }
    
    public void completeLab() {
        this.status = ProgressStatus.COMPLETED;
        this.completionPercentage = 100;
        if (this.completedDate == null) {
            this.completedDate = LocalDateTime.now();
        }
        this.lastAccessed = LocalDateTime.now();
    }
    
    public void abandonLab() {
        this.status = ProgressStatus.ABANDONED;
        this.lastAccessed = LocalDateTime.now();
    }
    
    public void incrementAttempts() {
        this.attemptsCount++;
    }
    
    public void addTimeSpent(int minutes) {
        this.timeSpent += minutes;
    }
    
    public boolean isCompleted() {
        return status == ProgressStatus.COMPLETED;
    }
    
    public boolean isInProgress() {
        return status == ProgressStatus.IN_PROGRESS;
    }
    
    public boolean isNotStarted() {
        return status == ProgressStatus.NOT_STARTED;
    }
    
    public boolean isAbandoned() {
        return status == ProgressStatus.ABANDONED;
    }
    
    public String getTimeSpentFormatted() {
        if (timeSpent == null || timeSpent == 0) return "0 min";
        if (timeSpent < 60) {
            return timeSpent + " min";
        } else {
            int hours = timeSpent / 60;
            int minutes = timeSpent % 60;
            if (minutes == 0) {
                return hours + "h";
            } else {
                return hours + "h " + minutes + "min";
            }
        }
    }
    
    public String getProgressDescription() {
        if (isCompleted()) {
            return "Completed in " + getTimeSpentFormatted();
        } else if (isInProgress()) {
            return completionPercentage + "% complete";
        } else if (isAbandoned()) {
            return "Abandoned at " + completionPercentage + "%";
        } else {
            return "Not started";
        }
    }
    
    // Override methods
    @Override
    public String toString() {
        return "UserProgress{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", lab=" + (lab != null ? lab.getTitle() : "null") +
                ", status=" + status +
                ", completionPercentage=" + completionPercentage +
                ", timeSpent=" + timeSpent +
                '}';
    }
}