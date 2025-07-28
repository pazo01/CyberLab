package it.uniroma3.cyberlab.repository;

import it.uniroma3.cyberlab.entity.Post;
import it.uniroma3.cyberlab.entity.Post.PostType;
import it.uniroma3.cyberlab.entity.Category;
import it.uniroma3.cyberlab.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // Basic queries
    List<Post> findByAuthor(User author);
    List<Post> findByCategory(Category category);
    List<Post> findByPostType(PostType postType);
    
    Page<Post> findByAuthor(User author, Pageable pageable);
    Page<Post> findByCategory(Category category, Pageable pageable);
    Page<Post> findByPostType(PostType postType, Pageable pageable);
    
    // Author ordered queries
    List<Post> findByAuthorOrderByCreatedDateDesc(User author);
    List<Post> findByAuthorOrderByCreatedDateAsc(User author);
    
    // Category ordered queries
    List<Post> findByCategoryOrderByCreatedDateDesc(Category category);
    List<Post> findByCategoryOrderByCreatedDateAsc(Category category);
    Page<Post> findByCategoryOrderByCreatedDateDesc(Category category, Pageable pageable);
    
    // Featured and pinned posts
    List<Post> findByIsFeaturedTrueOrderByCreatedDateDesc();
    List<Post> findByIsPinnedTrueOrderByCreatedDateDesc();
    
    @Query("SELECT p FROM Post p WHERE p.isFeatured = true OR p.isPinned = true ORDER BY p.isPinned DESC, p.createdDate DESC")
    List<Post> findFeaturedAndPinnedPosts();
    
    // Date-based queries
    List<Post> findByCreatedDateAfter(LocalDateTime date);
    List<Post> findByCreatedDateBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT p FROM Post p WHERE p.createdDate >= :date ORDER BY p.createdDate DESC")
    List<Post> findRecentPosts(@Param("date") LocalDateTime date);
    
    // Popular posts
    @Query("SELECT p FROM Post p ORDER BY p.viewCount DESC")
    List<Post> findMostViewedPosts(Pageable pageable);
    
    @Query("SELECT p FROM Post p ORDER BY p.likeCount DESC")
    List<Post> findMostLikedPosts(Pageable pageable);
    
    @Query("SELECT p FROM Post p ORDER BY SIZE(p.comments) DESC")
    List<Post> findMostCommentedPosts(Pageable pageable);
    
    // Search functionality
    @Query("SELECT p FROM Post p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.tags) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Post> searchPosts(@Param("search") String search);
    
    @Query("SELECT p FROM Post p WHERE " +
           "p.category = :category AND (" +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Post> searchPostsInCategory(@Param("search") String search, @Param("category") Category category);
    
    // Category and type combinations
    List<Post> findByCategoryAndPostType(Category category, PostType postType);
    
    // Statistics
    @Query("SELECT COUNT(p) FROM Post p WHERE p.author = :author")
    long countByAuthor(@Param("author") User author);
    
    @Query("SELECT COUNT(p) FROM Post p WHERE p.category = :category")
    long countByCategory(@Param("category") Category category);
    
    @Query("SELECT COUNT(p) FROM Post p WHERE p.createdDate >= :date")
    long countPostsCreatedAfter(@Param("date") LocalDateTime date);
    
    // Author's posts by category
    @Query("SELECT p FROM Post p WHERE p.author = :author AND p.category = :category")
    List<Post> findByAuthorAndCategory(@Param("author") User author, @Param("category") Category category);
    
    List<Post> findByCategoryOrderByViewCountDesc(Category category);
    @Query("SELECT COALESCE(SUM(p.viewCount), 0) FROM Post p WHERE p.category = :category")
    Long sumViewsByCategory(@Param("category") Category category);
    
}
