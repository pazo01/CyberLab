package it.uniroma3.cyberlab.repository;

import it.uniroma3.cyberlab.entity.UserProgress;
import it.uniroma3.cyberlab.entity.UserProgress.ProgressStatus;
import it.uniroma3.cyberlab.entity.User;
import it.uniroma3.cyberlab.entity.Lab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    
    // Basic queries
    Optional<UserProgress> findByUserAndLab(User user, Lab lab);
    List<UserProgress> findByUser(User user);
    List<UserProgress> findByLab(Lab lab);
    List<UserProgress> findByStatus(ProgressStatus status);
    
    List<UserProgress> findByUserOrderByLastAccessedDesc(User user);
    List<UserProgress> findByLabOrderByCompletionPercentageDesc(Lab lab);
    
    // Status-based queries
    List<UserProgress> findByUserAndStatus(User user, ProgressStatus status);
    List<UserProgress> findByLabAndStatus(Lab lab, ProgressStatus status);
    
    @Query("SELECT up FROM UserProgress up WHERE up.user = :user AND up.status = 'COMPLETED' ORDER BY up.completedDate DESC")
    List<UserProgress> findCompletedByUser(@Param("user") User user);
    
    @Query("SELECT up FROM UserProgress up WHERE up.user = :user AND up.status = 'IN_PROGRESS' ORDER BY up.lastAccessed DESC")
    List<UserProgress> findInProgressByUser(@Param("user") User user);
    
    // Completion statistics
    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.user = :user AND up.status = 'COMPLETED'")
    long countCompletedByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.lab = :lab AND up.status = 'COMPLETED'")
    long countCompletedByLab(@Param("lab") Lab lab);
    
    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.user = :user AND up.status = 'IN_PROGRESS'")
    long countInProgressByUser(@Param("user") User user);
    
    /**
     * Conta UserProgress per stato
     */
    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.status = :status")
    long countByStatus(@Param("status") UserProgress.ProgressStatus status);
    
    /**
     * Conta per stato senza query custom (metodo alternativo)
     */
    default long countByStatusSimple(UserProgress.ProgressStatus status) {
        return findByStatus(status).size();
    }
    
    // Time-based queries
    List<UserProgress> findByStartedDateAfter(LocalDateTime date);
    List<UserProgress> findByCompletedDateAfter(LocalDateTime date);
    List<UserProgress> findByLastAccessedAfter(LocalDateTime date);
    
    @Query("SELECT up FROM UserProgress up WHERE up.user = :user AND up.lastAccessed >= :date ORDER BY up.lastAccessed DESC")
    List<UserProgress> findRecentActivityByUser(@Param("user") User user, @Param("date") LocalDateTime date);
    
    // Progress percentage queries
    @Query("SELECT up FROM UserProgress up WHERE up.user = :user AND up.completionPercentage >= :percentage")
    List<UserProgress> findByUserAndMinCompletion(@Param("user") User user, @Param("percentage") int percentage);
    
    @Query("SELECT AVG(up.completionPercentage) FROM UserProgress up WHERE up.lab = :lab")
    Double findAverageCompletionByLab(@Param("lab") Lab lab);
    
    @Query("SELECT AVG(up.timeSpent) FROM UserProgress up WHERE up.lab = :lab AND up.status = 'COMPLETED'")
    Double findAverageTimeSpentByLab(@Param("lab") Lab lab);
    
    // User statistics
    @Query("SELECT SUM(up.timeSpent) FROM UserProgress up WHERE up.user = :user")
    Long findTotalTimeSpentByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(DISTINCT up.lab) FROM UserProgress up WHERE up.user = :user")
    long countDistinctLabsByUser(@Param("user") User user);
    
    // Lab statistics
    @Query("SELECT COUNT(DISTINCT up.user) FROM UserProgress up WHERE up.lab = :lab")
    long countDistinctUsersByLab(@Param("lab") Lab lab);
    
    
    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.status = 'COMPLETED'")
    long countCompletedLabs();
    
    @Query("SELECT up FROM UserProgress up WHERE up.lab = :lab ORDER BY up.timeSpent ASC")
    List<UserProgress> findFastestCompletionsByLab(@Param("lab") Lab lab);
    
    // Leaderboard queries
    @Query("SELECT up.user, COUNT(up) as completedCount FROM UserProgress up WHERE up.status = 'COMPLETED' GROUP BY up.user ORDER BY completedCount DESC")
    List<Object[]> findUsersByCompletedLabsCount();
    
    @Query("SELECT up.user, SUM(up.timeSpent) as totalTime FROM UserProgress up WHERE up.status = 'COMPLETED' GROUP BY up.user ORDER BY totalTime DESC")
    List<Object[]> findUsersByTotalTimeSpent();
    
    // Existence checks
    boolean existsByUserAndLab(User user, Lab lab);
    
    @Query("SELECT CASE WHEN COUNT(up) > 0 THEN true ELSE false END FROM UserProgress up WHERE up.user = :user AND up.lab = :lab AND up.status = 'COMPLETED'")
    boolean hasUserCompletedLab(@Param("user") User user, @Param("lab") Lab lab);
}