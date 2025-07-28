package it.uniroma3.cyberlab.service;

import it.uniroma3.cyberlab.entity.*;
import it.uniroma3.cyberlab.repository.ReportRepository;
import it.uniroma3.cyberlab.repository.PostRepository;
import it.uniroma3.cyberlab.repository.CommentRepository;
import it.uniroma3.cyberlab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Crea segnalazione per post
     */
    public Report reportPost(Long postId, User reportedBy, Report.ReportReason reason, String details) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        // Controlla se già segnalato da questo utente
        boolean alreadyReported = reportRepository.existsByPostAndReportedBy(post, reportedBy);
        if (alreadyReported) {
            throw new RuntimeException("You have already reported this post");
        }
        
        Report report = new Report();
        report.setPost(post);
        report.setReportedBy(reportedBy);
        report.setReason(reason);
        report.setAdditionalDetails(details);
        report.setStatus(Report.ReportStatus.PENDING);
        
        Report savedReport = reportRepository.save(report);
        
        // Incrementa contatore segnalazioni nel post
        post.incrementReportCount();
        postRepository.save(post);
        
        return savedReport;
    }

    /**
     * Crea segnalazione per commento
     */
    public Report reportComment(Long commentId, User reportedBy, Report.ReportReason reason, String details) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        // Controlla se già segnalato da questo utente
        boolean alreadyReported = reportRepository.existsByCommentAndReportedBy(comment, reportedBy);
        if (alreadyReported) {
            throw new RuntimeException("You have already reported this comment");
        }
        
        Report report = new Report();
        report.setComment(comment);
        report.setReportedBy(reportedBy);
        report.setReason(reason);
        report.setAdditionalDetails(details);
        report.setStatus(Report.ReportStatus.PENDING);
        
        Report savedReport = reportRepository.save(report);
        
        // Incrementa contatore segnalazioni nel commento
        comment.incrementReportCount();
        commentRepository.save(comment);
        
        return savedReport;
    }

    /**
     * Crea segnalazione per utente
     */
    public Report reportUser(Long userId, User reportedBy, Report.ReportReason reason, String details) {
        User reportedUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Non può segnalare se stesso
        if (reportedUser.getId().equals(reportedBy.getId())) {
            throw new RuntimeException("You cannot report yourself");
        }
        
        // Controlla se già segnalato da questo utente
        boolean alreadyReported = reportRepository.existsByReportedUserAndReportedBy(reportedUser, reportedBy);
        if (alreadyReported) {
            throw new RuntimeException("You have already reported this user");
        }
        
        Report report = new Report();
        report.setReportedUser(reportedUser);
        report.setReportedBy(reportedBy);
        report.setReason(reason);
        report.setAdditionalDetails(details);
        report.setStatus(Report.ReportStatus.PENDING);
        
        return reportRepository.save(report);
    }

    /**
     * Trova segnalazione per ID
     */
    @Transactional(readOnly = true)
    public Report findById(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
    }

    /**
     * Risolvi segnalazione
     */
    public Report resolveReport(Long reportId, User admin, String resolution) {
        if (!admin.isAdmin()) {
            throw new SecurityException("Only admins can resolve reports");
        }
        
        Report report = findById(reportId);
        
        report.setStatus(Report.ReportStatus.RESOLVED);
        report.setResolvedBy(admin);
        report.setResolution(resolution);
        report.setResolvedDate(LocalDateTime.now());
        
        return reportRepository.save(report);
    }

    /**
     * Rifiuta segnalazione
     */
    public Report dismissReport(Long reportId, User admin, String reason) {
        if (!admin.isAdmin()) {
            throw new SecurityException("Only admins can dismiss reports");
        }
        
        Report report = findById(reportId);
        
        report.setStatus(Report.ReportStatus.DISMISSED);
        report.setResolvedBy(admin);
        report.setResolution(reason);
        report.setResolvedDate(LocalDateTime.now());
        
        return reportRepository.save(report);
    }

    /**
     * Trova segnalazioni in sospeso
     */
    @Transactional(readOnly = true)
    public List<Report> findPendingReports() {
        return reportRepository.findByStatusOrderByCreatedDateAsc(Report.ReportStatus.PENDING);
    }

    /**
     * Trova segnalazioni per stato
     */
    @Transactional(readOnly = true)
    public List<Report> findReportsByStatus(Report.ReportStatus status) {
        return reportRepository.findByStatusOrderByCreatedDateDesc(status);
    }

    /**
     * Trova segnalazioni per utente (che ha fatto le segnalazioni)
     */
    @Transactional(readOnly = true)
    public List<Report> findReportsByUser(User user) {
        return reportRepository.findByReportedByOrderByCreatedDateDesc(user);
    }

    /**
     * Trova segnalazioni contro un utente
     */
    @Transactional(readOnly = true)
    public List<Report> findReportsAgainstUser(User user) {
        return reportRepository.findByReportedUserOrderByCreatedDateDesc(user);
    }

    /**
     * Elimina segnalazione (solo admin)
     */
    public void deleteReport(Long reportId, User admin) {
        if (!admin.isAdmin()) {
            throw new SecurityException("Only admins can delete reports");
        }
        
        Report report = findById(reportId);
        reportRepository.delete(report);
    }

    /**
     * Statistiche segnalazioni
     */
    @Transactional(readOnly = true)
    public ReportStatistics getReportStatistics() {
        long totalReports = reportRepository.count();
        long pendingReports = reportRepository.countByStatus(Report.ReportStatus.PENDING);
        long resolvedReports = reportRepository.countByStatus(Report.ReportStatus.RESOLVED);
        long dismissedReports = reportRepository.countByStatus(Report.ReportStatus.DISMISSED);
        
        return new ReportStatistics(totalReports, pendingReports, resolvedReports, dismissedReports);
    }

    /**
     * Statistiche segnalazioni per periodo
     */
    @Transactional(readOnly = true)
    public ReportPeriodStatistics getPeriodStatistics() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        
        long reportsThisWeek = reportRepository.countByCreatedDateAfter(oneWeekAgo);
        long reportsThisMonth = reportRepository.countByCreatedDateAfter(oneMonthAgo);
        
        return new ReportPeriodStatistics(reportsThisWeek, reportsThisMonth);
    }

    /**
     * Trova segnalazioni recenti
     */
    @Transactional(readOnly = true)
    public List<Report> findRecentReports(int days, int limit) {
        LocalDateTime dateFrom = LocalDateTime.now().minusDays(days);
        List<Report> reports = reportRepository.findByCreatedDateAfterOrderByCreatedDateDesc(dateFrom);
        return reports.stream().limit(limit).toList();
    }

    /**
     * Verifica se utente può segnalare un contenuto
     */
    public boolean canReport(User reporter, Object content) {
        if (reporter == null) return false;
        
        if (content instanceof Post post) {
            return !post.isOwnedBy(reporter);
        } else if (content instanceof Comment comment) {
            return !comment.isOwnedBy(reporter);
        } else if (content instanceof User user) {
            return !user.getId().equals(reporter.getId());
        }
        
        return false;
    }

    // =============================================================================
    // DTO CLASSES
    // =============================================================================

    /**
     * DTO per statistiche segnalazioni
     */
    public static class ReportStatistics {
        private final long totalReports;
        private final long pendingReports;
        private final long resolvedReports;
        private final long dismissedReports;
        
        public ReportStatistics(long totalReports, long pendingReports, long resolvedReports, long dismissedReports) {
            this.totalReports = totalReports;
            this.pendingReports = pendingReports;
            this.resolvedReports = resolvedReports;
            this.dismissedReports = dismissedReports;
        }
        
        // Getters
        public long getTotalReports() { return totalReports; }
        public long getPendingReports() { return pendingReports; }
        public long getResolvedReports() { return resolvedReports; }
        public long getDismissedReports() { return dismissedReports; }
        
        public double getResolutionRate() { 
            return totalReports > 0 ? (double) resolvedReports / totalReports * 100 : 0; 
        }
        
        public double getPendingRate() {
            return totalReports > 0 ? (double) pendingReports / totalReports * 100 : 0;
        }
    }

    /**
     * DTO per statistiche periodo
     */
    public static class ReportPeriodStatistics {
        private final long reportsThisWeek;
        private final long reportsThisMonth;
        
        public ReportPeriodStatistics(long reportsThisWeek, long reportsThisMonth) {
            this.reportsThisWeek = reportsThisWeek;
            this.reportsThisMonth = reportsThisMonth;
        }
        
        // Getters
        public long getReportsThisWeek() { return reportsThisWeek; }
        public long getReportsThisMonth() { return reportsThisMonth; }
    }
}