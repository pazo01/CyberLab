package it.uniroma3.cyberlab.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lab")
public class Lab {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Lab title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @Column(columnDefinition = "TEXT")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Theory content is required")
    private String theory; // Theory explanation
    
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Exercise content is required")
    private String exercise; // Practical exercise instructions
    
    @Column(columnDefinition = "TEXT")
    private String solution; // Solution/hints for the exercise
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty = Difficulty.BEGINNER;
    
    @Column(name = "estimated_time")
    private Integer estimatedTime; // Minutes to complete
    
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();
    
    @Column(name = "last_modified")
    private LocalDateTime lastModified = LocalDateTime.now();
    
    @Column(name = "is_published")
    private Boolean isPublished = false;
    
    @Column(name = "view_count")
    private Long viewCount = 0L;
    
    @Column(name = "completion_count")
    private Long completionCount = 0L;
    
    @Column(length = 500)
    private String prerequisites; // What users should know before
    
    @Column(length = 500)
    private String tools; // Tools needed for the lab
    
    @Column(name = "lab_url")
    private String labUrl; // URL to interactive lab environment
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy; // Admin who created the lab
    
    @OneToMany(mappedBy = "lab", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserProgress> userProgresses = new ArrayList<>();
    
    // Enums
    public enum Difficulty {
        BEGINNER("Beginner", "#28a745"),
        INTERMEDIATE("Intermediate", "#ffc107"),
        ADVANCED("Advanced", "#dc3545"),
        EXPERT("Expert", "#6f42c1");
        
        private final String displayName;
        private final String color;
        
        Difficulty(String displayName, String color) {
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
    public Lab() {}
    
    public Lab(String title, String theory, String exercise, Category category, User createdBy) {
        this.title = title;
        this.theory = theory;
        this.exercise = exercise;
        this.category = category;
        this.createdBy = createdBy;
    }
    
    public Lab(String title, String description, String theory, String exercise, 
               Difficulty difficulty, Category category, User createdBy) {
        this.title = title;
        this.description = description;
        this.theory = theory;
        this.exercise = exercise;
        this.difficulty = difficulty;
        this.category = category;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getTheory() {
        return theory;
    }
    
    public void setTheory(String theory) {
        this.theory = theory;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getExercise() {
        return exercise;
    }
    
    public void setExercise(String exercise) {
        this.exercise = exercise;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getSolution() {
        return solution;
    }
    
    public void setSolution(String solution) {
        this.solution = solution;
    }
    
    public Difficulty getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
    
    public Integer getEstimatedTime() {
        return estimatedTime;
    }
    
    public void setEstimatedTime(Integer estimatedTime) {
        this.estimatedTime = estimatedTime;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
    
    public Boolean getIsPublished() {
        return isPublished;
    }
    
    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }
    
    public Long getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
    
    public Long getCompletionCount() {
        return completionCount;
    }
    
    public void setCompletionCount(Long completionCount) {
        this.completionCount = completionCount;
    }
    
    public String getPrerequisites() {
        return prerequisites;
    }
    
    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }
    
    public String getTools() {
        return tools;
    }
    
    public void setTools(String tools) {
        this.tools = tools;
    }
    
    public String getLabUrl() {
        return labUrl;
    }
    
    public void setLabUrl(String labUrl) {
        this.labUrl = labUrl;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    public List<UserProgress> getUserProgresses() {
        return userProgresses;
    }
    
    public void setUserProgresses(List<UserProgress> userProgresses) {
        this.userProgresses = userProgresses;
    }
    
    // Helper methods
    public void incrementViewCount() {
        this.viewCount++;
    }
    
    public void incrementCompletionCount() {
        this.completionCount++;
    }
    
    public boolean isCreatedBy(User user) {
        return createdBy != null && createdBy.getId().equals(user.getId());
    }
    
    public String getEstimatedTimeFormatted() {
        if (estimatedTime == null) return "N/A";
        if (estimatedTime < 60) {
            return estimatedTime + " min";
        } else {
            int hours = estimatedTime / 60;
            int minutes = estimatedTime % 60;
            if (minutes == 0) {
                return hours + "h";
            } else {
                return hours + "h " + minutes + "min";
            }
        }
    }
    
    public List<String> getPrerequisiteList() {
        if (prerequisites == null || prerequisites.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<String> prereqList = new ArrayList<>();
        for (String prereq : prerequisites.split(",")) {
            String trimmedPrereq = prereq.trim();
            if (!trimmedPrereq.isEmpty()) {
                prereqList.add(trimmedPrereq);
            }
        }
        return prereqList;
    }
    
    public List<String> getToolList() {
        if (tools == null || tools.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<String> toolList = new ArrayList<>();
        for (String tool : tools.split(",")) {
            String trimmedTool = tool.trim();
            if (!trimmedTool.isEmpty()) {
                toolList.add(trimmedTool);
            }
        }
        return toolList;
    }
    
    public double getCompletionRate() {
        if (viewCount == 0) return 0.0;
        return (double) completionCount / viewCount * 100;
    }
    
    // Override methods
    @Override
    public String toString() {
        return "Lab{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", difficulty=" + difficulty +
                ", category=" + (category != null ? category.getName() : "null") +
                ", createdBy=" + (createdBy != null ? createdBy.getUsername() : "null") +
                ", isPublished=" + isPublished +
                '}';
    }
}