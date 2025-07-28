package it.uniroma3.cyberlab.repository;

import it.uniroma3.cyberlab.entity.Comment;
import it.uniroma3.cyberlab.entity.Post;
import it.uniroma3.cyberlab.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Basic queries
    List<Comment> findByPost(Post post);
    List<Comment> findByAuthor(User author);
    List<Comment> findByPostOrderByCreatedDateAsc(Post post);
    List<Comment> findByPostOrderByCreatedDateDesc(Post post);
    
    // Parent/child relationships
    List<Comment> findByParentCommentIsNull(); // Top-level comments
    List<Comment> findByParentComment(Comment parentComment);
    List<Comment> findByPostAndParentCommentIsNull(Post post); // Top-level comments for a post
    
    @Query("SELECT c FROM Comment c WHERE c.post = :post AND c.parentComment IS NULL ORDER BY c.createdDate ASC")
    List<Comment> findTopLevelCommentsByPost(@Param("post") Post post);
    
    // Reported comments
    List<Comment> findByIsReportedTrue();
    List<Comment> findByReportCountGreaterThan(int count);
    
    @Query("SELECT c FROM Comment c WHERE c.isReported = true ORDER BY c.reportCount DESC, c.createdDate DESC")
    List<Comment> findReportedCommentsOrderByReportCount();
    
    // Recent comments
    List<Comment> findByCreatedDateAfter(LocalDateTime date);
    List<Comment> findByCreatedDateBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT c FROM Comment c WHERE c.createdDate >= :date ORDER BY c.createdDate DESC")
    List<Comment> findRecentComments(@Param("date") LocalDateTime date);
    
    // User's comments
    @Query("SELECT c FROM Comment c WHERE c.author = :author ORDER BY c.createdDate DESC")
    List<Comment> findByAuthorOrderByCreatedDateDesc(@Param("author") User author);
    
    // Statistics
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post = :post")
    long countByPost(@Param("post") Post post);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.author = :author")
    long countByAuthor(@Param("author") User author);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.isReported = true")
    long countReportedComments();
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.createdDate >= :date")
    long countCommentsCreatedAfter(@Param("date") LocalDateTime date);
    
    // Most liked comments
    @Query("SELECT c FROM Comment c ORDER BY c.likeCount DESC")
    List<Comment> findMostLikedComments();
}
