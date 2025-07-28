package it.uniroma3.cyberlab.repository;

import it.uniroma3.cyberlab.entity.Lab;
import it.uniroma3.cyberlab.entity.Lab.Difficulty;
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
public interface LabRepository extends JpaRepository<Lab, Long> {
    
    // Basic queries
    List<Lab> findByCategory(Category category);
    List<Lab> findByCreatedBy(User createdBy);
    List<Lab> findByDifficulty(Difficulty difficulty);
    List<Lab> findByIsPublishedTrue();
    List<Lab> findByIsPublishedFalse();
    
    Page<Lab> findByCategory(Category category, Pageable pageable);
    Page<Lab> findByDifficulty(Difficulty difficulty, Pageable pageable);
    Page<Lab> findByIsPublishedTrue(Pageable pageable);
    
    // Published labs by category and difficulty
    List<Lab> findByCategoryAndIsPublishedTrueOrderByCreatedDateDesc(Category category);
    List<Lab> findByDifficultyAndIsPublishedTrueOrderByCreatedDateDesc(Difficulty difficulty);
    List<Lab> findByCategoryAndDifficultyAndIsPublishedTrue(Category category, Difficulty difficulty);
    
    // Popular labs
    @Query("SELECT l FROM Lab l WHERE l.isPublished = true ORDER BY l.viewCount DESC")
    List<Lab> findMostViewedLabs(Pageable pageable);
    
    @Query("SELECT l FROM Lab l WHERE l.isPublished = true ORDER BY l.completionCount DESC")
    List<Lab> findMostCompletedLabs(Pageable pageable);
    
    @Query("SELECT l FROM Lab l WHERE l.isPublished = true ORDER BY (l.completionCount * 1.0 / NULLIF(l.viewCount, 0)) DESC")
    List<Lab> findLabsByCompletionRate(Pageable pageable);
    
    // Recent labs
    List<Lab> findByCreatedDateAfter(LocalDateTime date);
    List<Lab> findByIsPublishedTrueAndCreatedDateAfterOrderByCreatedDateDesc(LocalDateTime date);
    
    // Search functionality
    @Query("SELECT l FROM Lab l WHERE l.isPublished = true AND (" +
           "LOWER(l.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.theory) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Lab> searchPublishedLabs(@Param("search") String search);
    
    @Query("SELECT l FROM Lab l WHERE " +
           "LOWER(l.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Lab> searchAllLabs(@Param("search") String search);
    
    // Statistics
    @Query("SELECT COUNT(l) FROM Lab l WHERE l.isPublished = true")
    long countPublishedLabs();
    
    @Query("SELECT COUNT(l) FROM Lab l WHERE l.createdBy = :creator")
    long countByCreatedBy(@Param("creator") User creator);
    
    @Query("SELECT COUNT(l) FROM Lab l WHERE l.category = :category AND l.isPublished = true")
    long countPublishedLabsByCategory(@Param("category") Category category);
    
    @Query("SELECT COUNT(l) FROM Lab l WHERE l.difficulty = :difficulty AND l.isPublished = true")
    long countPublishedLabsByDifficulty(@Param("difficulty") Difficulty difficulty);
    
    // Admin queries
    @Query("SELECT l FROM Lab l ORDER BY l.createdDate DESC")
    List<Lab> findAllOrderByCreatedDateDesc();
    
    @Query("SELECT l FROM Lab l WHERE l.isPublished = false ORDER BY l.createdDate DESC")
    List<Lab> findUnpublishedLabs();
    
    @Query("SELECT l FROM Lab l WHERE LOWER(l.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(l.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Lab> searchLabs(@Param("searchTerm") String searchTerm);
    
    long countByCategory(Category category);
    List<Lab> findByCategoryOrderByViewCountDesc(Category category);
    @Query("SELECT COALESCE(SUM(l.viewCount), 0) FROM Lab l WHERE l.category = :category")
    Long sumViewsByCategory(@Param("category") Category category);
    
}