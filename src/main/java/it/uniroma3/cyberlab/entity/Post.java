package it.uniroma3.cyberlab.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Post title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Post content is required")
    @Size(min = 10, message = "Content must be at least 10 characters")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 20)
    private PostType postType = PostType.GENERAL;
    
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();
    
    @Column(name = "last_modified")
    private LocalDateTime lastModified = LocalDateTime.now();
    
    @Column(name = "view_count")
    private Long viewCount = 0L;
    
    @Column(name = "like_count")
    private Long likeCount = 0L;
    
    @Column(length = 500)
    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags; // Comma-separated tags
    
    @Column(name = "is_pinned")
    private Boolean isPinned = false;
    
    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    // ========== CAMPI AGGIUNTI PER SEGNALAZIONI ==========
    @Column(name = "report_count")
    private Integer reportCount = 0;
    
    @Column(name = "is_reported")
    private Boolean isReported = false;
    
    @Column(name = "is_locked")
    private Boolean isLocked = false;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();
    
    // ========== AGGIUNTA RELAZIONE CON REPORT ==========
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Report> reports = new ArrayList<>();
    
    // Enums
    public enum PostType {
        SCRIPT("Attack Script"),
        TOOL("Security Tool"),
        WRITEUP("CTF Writeup"),
        TUTORIAL("Tutorial"),
        NEWS("Security News"),
        GENERAL("General Discussion");
        
        private final String displayName;
        
        PostType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public Post() {}
    
    public Post(String title, String content, User author, Category category) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.category = category;
    }
    
    public Post(String title, String content, PostType postType, User author, Category category) {
        this.title = title;
        this.content = content;
        this.postType = postType;
        this.author = author;
        this.category = category;
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
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
        this.lastModified = LocalDateTime.now();
    }
    
    public PostType getPostType() {
        return postType;
    }
    
    public void setPostType(PostType postType) {
        this.postType = postType;
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
    
    public Long getViewCount() {
        return viewCount != null ? viewCount : 0L;
    }
    
    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
    
    public Long getLikeCount() {
        return likeCount != null ? likeCount : 0L;
    }
    
    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public Boolean getIsPinned() {
        return isPinned;
    }
    
    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }
    
    public Boolean getIsFeatured() {
        return isFeatured;
    }
    
    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }
    
    // ========== GETTER/SETTER CAMPI SEGNALAZIONI ==========
    public Integer getReportCount() {
        return reportCount;
    }
    
    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }
    
    public Boolean getIsReported() {
        return isReported;
    }
    
    public void setIsReported(Boolean isReported) {
        this.isReported = isReported;
    }
    
    public Boolean getIsLocked() {
        return isLocked;
    }
    
    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
    }
    
    public User getAuthor() {
        return author;
    }
    
    public void setAuthor(User author) {
        this.author = author;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public List<Comment> getComments() {
        return comments;
    }
    
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    
    // ========== GETTER/SETTER REPORTS ==========
    public List<Report> getReports() {
        return reports;
    }
    
    public void setReports(List<Report> reports) {
        this.reports = reports;
    }
    
    // Helper methods
    public void incrementViewCount() {
        if (this.viewCount == null) {
            this.viewCount = 0L;
        }
        this.viewCount++;
    }
    
    public void incrementLikeCount() {
        if (this.likeCount == null) {
            this.likeCount = 0L;
        }
        this.likeCount++;
    }
    
    public void decrementLikeCount() {
        if (this.likeCount == null) {
            this.likeCount = 0L;
        }
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
    
    // ========== METODI AGGIUNTI PER SEGNALAZIONI ==========
    public void incrementReportCount() {
        this.reportCount = (this.reportCount == null ? 0 : this.reportCount) + 1;
        this.isReported = true;
    }
    
    public void decrementReportCount() {
        if (this.reportCount != null && this.reportCount > 0) {
            this.reportCount--;
            if (this.reportCount == 0) {
                this.isReported = false;
            }
        }
    }
    
    public boolean hasReports() {
        return this.reportCount != null && this.reportCount > 0;
    }
    
    public int getCommentCount() {
        return comments != null ? comments.size() : 0;
    }
    
    public String getShortContent(int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }
    
    public List<String> getTagList() {
        if (tags == null || tags.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<String> tagList = new ArrayList<>();
        for (String tag : tags.split(",")) {
            String trimmedTag = tag.trim();
            if (!trimmedTag.isEmpty()) {
                tagList.add(trimmedTag);
            }
        }
        return tagList;
    }
    
    public boolean isOwnedBy(User user) {
        return author != null && user != null && author.getId().equals(user.getId());
    }
    
    // ========== METODI HELPER AGGIUNTIVI ==========
    public boolean canBeReportedBy(User user) {
        return user != null && !isOwnedBy(user);
    }
    
    public boolean isPinned() {
        return isPinned != null && isPinned;
    }
    
    public boolean isFeatured() {
        return isFeatured != null && isFeatured;
    }
    
    public boolean isReported() {
        return isReported != null && isReported;
    }
    
    public boolean isLocked() {
        return isLocked != null && isLocked;
    }
    
    // Override methods
    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", postType=" + postType +
                ", author=" + (author != null ? author.getUsername() : "null") +
                ", category=" + (category != null ? category.getName() : "null") +
                ", createdDate=" + createdDate +
                ", reportCount=" + reportCount +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Post post = (Post) obj;
        return id != null && id.equals(post.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}