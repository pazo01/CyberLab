package it.uniroma3.cyberlab.service;

import it.uniroma3.cyberlab.entity.Comment;
import it.uniroma3.cyberlab.entity.Post;
import it.uniroma3.cyberlab.entity.User;
import it.uniroma3.cyberlab.entity.Report;
import it.uniroma3.cyberlab.repository.CommentRepository;
import it.uniroma3.cyberlab.repository.PostRepository;
import it.uniroma3.cyberlab.repository.ReportRepository;
import it.uniroma3.cyberlab.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private ReportRepository reportRepository;

    /**
     * Crea nuovo commento
     */
    public Comment createComment(Long postId, String content, User author, Long parentCommentId) {
        // Validazione
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }
        
        if (content.trim().length() > 2000) {
            throw new IllegalArgumentException("Comment must not exceed 2000 characters");
        }
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        Comment newComment = new Comment();
        newComment.setContent(content.trim());
        newComment.setPost(post);
        newComment.setAuthor(author);
        
        // Se è una reply
        if (parentCommentId != null) {
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElse(null);
            if (parentComment != null && parentComment.getPost().getId().equals(postId)) {
                newComment.setParentComment(parentComment);
            }
        }
        
        return commentRepository.save(newComment);
    }

    /**
     * Trova commento per ID - METODO AGGIUNTO
     */
    @Transactional(readOnly = true)
    public Comment findById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    /**
     * Trova commenti per post - METODO AGGIUNTO
     */
    @Transactional(readOnly = true)
    public List<Comment> findCommentsByPost(Post post) {
        return commentRepository.findTopLevelCommentsByPost(post);
    }

    /**
     * Trova commenti dell'utente - METODO AGGIUNTO
     */
    @Transactional(readOnly = true)
    public List<Comment> findCommentsByUser(User user) {
        return commentRepository.findByAuthorOrderByCreatedDateDesc(user);
    }

    /**
     * Trova commenti utente dopo una data - METODO AGGIUNTO
     */
    @Transactional(readOnly = true)
    public List<Comment> findUserCommentsAfter(User user, LocalDateTime date, int limit) {
        return commentRepository.findByAuthorOrderByCreatedDateDesc(user)
                .stream()
                .filter(c -> c.getCreatedDate().isAfter(date))
                .limit(limit)
                .toList();
    }

    /**
     * Trova replies per parent ID - METODO AGGIUNTO
     */
    @Transactional(readOnly = true)
    public List<Comment> findRepliesByParentId(Long parentId) {
        Comment parent = findById(parentId);
        return commentRepository.findByParentComment(parent);
    }

    /**
     * Aggiorna commento esistente
     */
    public Comment updateComment(Long commentId, String content, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        
        // Verifica permessi
        if (!canEditComment(comment, user)) {
            throw new SecurityException("You don't have permission to edit this comment");
        }
        
        // Validazione content
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }
        
        if (content.trim().length() > 2000) {
            throw new IllegalArgumentException("Comment must not exceed 2000 characters");
        }
        
        comment.setContent(content.trim());
        comment.setLastModified(LocalDateTime.now());
        comment.setIsEdited(true);
        
        return commentRepository.save(comment);
    }

    /**
     * Elimina commento e restituisce post ID - CORREZIONE SIGNATURE
     */
    public Long deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        
        if (!canDeleteComment(comment, user)) {
            throw new SecurityException("You don't have permission to delete this comment");
        }
        
        Long postId = comment.getPost().getId();
        commentRepository.delete(comment);
        return postId;
    }

    /**
     * Toggle like su commento - CORREZIONE RETURN TYPE
     */
    public Comment toggleLike(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        
        // Non può likare i propri commenti
        if (comment.isOwnedBy(user)) {
            throw new IllegalArgumentException("You cannot like your own comment");
        }
        
        // Implementazione semplificata - in un sistema reale useresti una tabella separata
        comment.incrementLikeCount();
        return commentRepository.save(comment);
    }

    /**
     * Segnala commento - METODO AGGIUNTO
     */
    public void reportComment(Long commentId, User reportedBy, Report.ReportReason reason, String details) {
        Comment comment = findById(commentId);
        
        // Controlla se già segnalato
        List<Report> existingReports = reportRepository.findByComment(comment);
        boolean alreadyReported = existingReports.stream()
                .anyMatch(report -> report.getReportedBy().getId().equals(reportedBy.getId()));
        
        if (alreadyReported) {
            throw new RuntimeException("You have already reported this comment");
        }
        
        // Crea segnalazione
        Report report = new Report();
        report.setComment(comment);
        report.setReportedBy(reportedBy);
        report.setReason(reason);
        report.setAdditionalDetails(details);
        
        reportRepository.save(report);
        
        // Incrementa contatore
        comment.incrementReportCount();
        commentRepository.save(comment);
    }

    /**
     * Trova commenti segnalati - METODO AGGIUNTO
     */
    @Transactional(readOnly = true)
    public List<Comment> findReportedComments() {
        return commentRepository.findReportedCommentsOrderByReportCount();
    }

    /**
     * Trova commenti top-level per post
     */
    @Transactional(readOnly = true)
    public List<Comment> findTopLevelCommentsByPost(Post post) {
        return commentRepository.findTopLevelCommentsByPost(post);
    }

    /**
     * Trova replies per commento
     */
    @Transactional(readOnly = true)
    public List<Comment> findRepliesByComment(Comment parentComment) {
        return commentRepository.findByParentComment(parentComment);
    }

    /**
     * Trova commenti dell'utente
     */
    @Transactional(readOnly = true)
    public List<Comment> findUserComments(User user, int limit) {
        List<Comment> comments = commentRepository.findByAuthorOrderByCreatedDateDesc(user);
        return comments.stream().limit(limit).toList();
    }

    /**
     * Trova commenti recenti
     */
    @Transactional(readOnly = true)
    public List<Comment> findRecentComments(int days, int limit) {
        LocalDateTime dateFrom = LocalDateTime.now().minusDays(days);
        List<Comment> comments = commentRepository.findRecentComments(dateFrom);
        return comments.stream().limit(limit).toList();
    }

    /**
     * Segna commento come segnalato
     */
    public void markAsReported(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        
        comment.incrementReportCount();
        commentRepository.save(comment);
    }

    /**
     * Rimuovi segnalazione da commento
     */
    public void removeReportFlag(Long commentId, User admin) {
        if (!admin.isAdmin()) {
            throw new SecurityException("Only admins can remove report flags");
        }
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        
        comment.setIsReported(false);
        comment.setReportCount(0);
        commentRepository.save(comment);
    }

    /**
     * Ottieni statistiche commento
     */
    @Transactional(readOnly = true)
    public CommentStatistics getCommentStatistics(Comment comment) {
        CommentStatistics stats = new CommentStatistics();
        
        stats.setLikeCount(comment.getLikeCount());
        stats.setReplyCount(commentRepository.findByParentComment(comment).size());
        stats.setReportCount(comment.getReportCount());
        stats.setIsReported(comment.getIsReported());
        stats.setIsEdited(comment.getIsEdited());
        
        return stats;
    }

    /**
     * Ottieni statistiche globali commenti
     */
    @Transactional(readOnly = true)
    public GlobalCommentStatistics getGlobalStatistics() {
        GlobalCommentStatistics stats = new GlobalCommentStatistics();
        
        stats.setTotalComments(commentRepository.count());
        stats.setReportedComments(commentRepository.countReportedComments());
        
        // Commenti dell'ultimo mese
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        stats.setCommentsThisMonth(commentRepository.countCommentsCreatedAfter(oneMonthAgo));
        
        return stats;
    }

    /**
     * Verifica se l'utente può modificare il commento
     */
    private boolean canEditComment(Comment comment, User user) {
        return user != null && (user.isAdmin() || comment.isOwnedBy(user));
    }

    /**
     * Verifica se l'utente può eliminare il commento
     */
    private boolean canDeleteComment(Comment comment, User user) {
        return user != null && (user.isAdmin() || comment.isOwnedBy(user));
    }

    /**
     * Verifica se l'utente può segnalare il commento
     */
    public boolean canReportComment(Comment comment, User user) {
        return user != null && !comment.isOwnedBy(user);
    }

    /**
     * Conta commenti per post
     */
    @Transactional(readOnly = true)
    public long countCommentsByPost(Post post) {
        return commentRepository.countByPost(post);
    }

    /**
     * Conta commenti per utente
     */
    @Transactional(readOnly = true)
    public long countCommentsByUser(User user) {
        return commentRepository.countByAuthor(user);
    }

    // =============================================================================
    // DTO CLASSES
    // =============================================================================

    /**
     * DTO per risultato like commento
     */
    public static class CommentLikeResult {
        private boolean liked;
        private long totalLikes;

        public CommentLikeResult(boolean liked, long totalLikes) {
            this.liked = liked;
            this.totalLikes = totalLikes;
        }

        public boolean isLiked() { return liked; }
        public long getTotalLikes() { return totalLikes; }
    }

    /**
     * DTO per statistiche commento
     */
    public static class CommentStatistics {
        private long likeCount;
        private long replyCount;
        private int reportCount;
        private boolean isReported;
        private boolean isEdited;

        // Getters and Setters
        public long getLikeCount() { return likeCount; }
        public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
        
        public long getReplyCount() { return replyCount; }
        public void setReplyCount(long replyCount) { this.replyCount = replyCount; }
        
        public int getReportCount() { return reportCount; }
        public void setReportCount(int reportCount) { this.reportCount = reportCount; }
        
        public boolean isReported() { return isReported; }
        public void setIsReported(boolean isReported) { this.isReported = isReported; }
        
        public boolean isEdited() { return isEdited; }
        public void setIsEdited(boolean isEdited) { this.isEdited = isEdited; }
    }

    /**
     * DTO per statistiche globali commenti
     */
    public static class GlobalCommentStatistics {
        private long totalComments;
        private long reportedComments;
        private long commentsThisMonth;

        // Getters and Setters
        public long getTotalComments() { return totalComments; }
        public void setTotalComments(long totalComments) { this.totalComments = totalComments; }
        
        public long getReportedComments() { return reportedComments; }
        public void setReportedComments(long reportedComments) { this.reportedComments = reportedComments; }
        
        public long getCommentsThisMonth() { return commentsThisMonth; }
        public void setCommentsThisMonth(long commentsThisMonth) { this.commentsThisMonth = commentsThisMonth; }
    }
}