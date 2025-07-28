package it.uniroma3.cyberlab.repository;

import it.uniroma3.cyberlab.entity.Report;
import it.uniroma3.cyberlab.entity.Report.ReportStatus;
import it.uniroma3.cyberlab.entity.Report.ReportReason;
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
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    // Status-based queries
    List<Report> findByStatus(ReportStatus status);
    List<Report> findByStatusOrderByCreatedDateDesc(ReportStatus status);
    List<Report> findByStatusNot(ReportStatus status);
    
    // AGGIUNTO - Ordinamento ASC
    List<Report> findByStatusOrderByCreatedDateAsc(ReportStatus status);
    
    @Query("SELECT r FROM Report r WHERE r.status = 'PENDING' ORDER BY r.createdDate ASC")
    List<Report> findPendingReportsOldestFirst();
    
    // User-based queries
    List<Report> findByReportedBy(User reportedBy);
    List<Report> findByReviewedBy(User reviewedBy);
    
    @Query("SELECT r FROM Report r WHERE r.reportedBy = :user ORDER BY r.createdDate DESC")
    List<Report> findByReportedByOrderByCreatedDateDesc(@Param("user") User user);
    
    // AGGIUNTO - Segnalazioni contro un utente
    List<Report> findByReportedUserOrderByCreatedDateDesc(User reportedUser);
    
    // Comment-based queries
    List<Report> findByComment(Comment comment);
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.comment = :comment")
    long countByComment(@Param("comment") Comment comment);
    
    // Reason-based queries
    List<Report> findByReason(ReportReason reason);
    
    @Query("SELECT r.reason, COUNT(r) FROM Report r GROUP BY r.reason ORDER BY COUNT(r) DESC")
    List<Object[]> findReportCountByReason();
    
    // Date-based queries
    List<Report> findByCreatedDateAfter(LocalDateTime date);
    List<Report> findByCreatedDateBetween(LocalDateTime start, LocalDateTime end);
    List<Report> findByReviewDateAfter(LocalDateTime date);
    
    // AGGIUNTO - Data con ordinamento
    List<Report> findByCreatedDateAfterOrderByCreatedDateDesc(LocalDateTime date);
    
    // Statistics - AGGIUNTI METODI MANCANTI
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = 'PENDING'")
    long countPendingReports();
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = 'RESOLVED'")
    long countResolvedReports();
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.createdDate >= :date")
    long countReportsCreatedAfter(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.reviewedBy = :admin")
    long countReportsReviewedBy(@Param("admin") User admin);
    
    // AGGIUNTO - Conta per stato
    long countByStatus(ReportStatus status);
    
    // AGGIUNTO - Conta per data
    long countByCreatedDateAfter(LocalDateTime date);
    
    // AGGIUNTO - Verifica esistenza segnalazioni
    boolean existsByPostAndReportedBy(Post post, User reportedBy);
    boolean existsByCommentAndReportedBy(Comment comment, User reportedBy);
    boolean existsByReportedUserAndReportedBy(User reportedUser, User reportedBy);
    
    // Admin dashboard queries
    @Query("SELECT r FROM Report r WHERE r.status IN ('PENDING', 'UNDER_REVIEW') ORDER BY r.createdDate ASC")
    List<Report> findActiveReports();
    
    @Query("SELECT r FROM Report r WHERE r.reviewedBy = :admin ORDER BY r.reviewDate DESC")
    List<Report> findReportsReviewedBy(@Param("admin") User admin);
}
