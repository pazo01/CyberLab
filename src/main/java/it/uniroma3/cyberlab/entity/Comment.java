package it.uniroma3.cyberlab.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment")
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Comment content is required")
    @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters")
    private String content;
    
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();
    
    @Column(name = "last_modified")
    private LocalDateTime lastModified = LocalDateTime.now();
    
    @Column(name = "is_reported")
    private Boolean isReported = false;
    
    @Column(name = "report_count")
    private Integer reportCount = 0;
    
    @Column(name = "like_count")
    private Long likeCount = 0L;
    
    @Column(name = "is_edited")
    private Boolean isEdited = false;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment; // For nested comments/replies
    
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> replies = new ArrayList<>();
    
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Report> reports = new ArrayList<>();
    
    // Constructors
    public Comment() {}
    
    public Comment(String content, Post post, User author) {
        this.content = content;
        this.post = post;
        this.author = author;
    }
    
    public Comment(String content, Post post, User author, Comment parentComment) {
        this.content = content;
        this.post = post;
        this.author = author;
        this.parentComment = parentComment;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
        this.lastModified = LocalDateTime.now();
        this.isEdited = true;
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
    
    public Boolean getIsReported() {
        return isReported;
    }
    
    public void setIsReported(Boolean isReported) {
        this.isReported = isReported;
    }
    
    public Integer getReportCount() {
        return reportCount;
    }
    
    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }
    
    public Long getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
    
    public Boolean getIsEdited() {
        return isEdited;
    }
    
    public void setIsEdited(Boolean isEdited) {
        this.isEdited = isEdited;
    }
    
    public Post getPost() {
        return post;
    }
    
    public void setPost(Post post) {
        this.post = post;
    }
    
    public User getAuthor() {
        return author;
    }
    
    public void setAuthor(User author) {
        this.author = author;
    }
    
    public Comment getParentComment() {
        return parentComment;
    }
    
    public void setParentComment(Comment parentComment) {
        this.parentComment = parentComment;
    }
    
    public List<Comment> getReplies() {
        return replies;
    }
    
    public void setReplies(List<Comment> replies) {
        this.replies = replies;
    }
    
    public List<Report> getReports() {
        return reports;
    }
    
    public void setReports(List<Report> reports) {
        this.reports = reports;
    }
    
    // Helper methods
    public void incrementReportCount() {
        this.reportCount++;
        if (this.reportCount > 0) {
            this.isReported = true;
        }
    }
    
    public void decrementReportCount() {
        if (this.reportCount > 0) {
            this.reportCount--;
        }
        if (this.reportCount == 0) {
            this.isReported = false;
        }
    }
    
    public void incrementLikeCount() {
        this.likeCount++;
    }
    
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
    
    public boolean isReply() {
        return parentComment != null;
    }
    
    public boolean hasReplies() {
        return replies != null && !replies.isEmpty();
    }
    
    public int getReplyCount() {
        return replies != null ? replies.size() : 0;
    }
    
    public boolean isOwnedBy(User user) {
        return author != null && author.getId().equals(user.getId());
    }
    
    public String getShortContent(int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }
    
    // Override methods
    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + (content != null ? content.substring(0, Math.min(50, content.length())) + "..." : "null") + '\'' +
                ", author=" + (author != null ? author.getUsername() : "null") +
                ", post=" + (post != null ? post.getId() : "null") +
                ", isReported=" + isReported +
                ", reportCount=" + reportCount +
                '}';
    }
}