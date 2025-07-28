package it.uniroma3.cyberlab.controller;

import it.uniroma3.cyberlab.entity.*;
import it.uniroma3.cyberlab.repository.CommentRepository;
import it.uniroma3.cyberlab.repository.ReportRepository;
import it.uniroma3.cyberlab.service.CommentService;
import it.uniroma3.cyberlab.service.PostService;
import it.uniroma3.cyberlab.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;
    
    @Autowired
    private PostService postService;

    // --- DIPENDENZE AGGIUNTE ---
    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private CommentRepository commentRepository;
    // -------------------------

    /**
     * Crea nuovo commento (AJAX) - VERSIONE CORRETTA
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createComment(
            @RequestParam Long postId,
            @RequestParam String content,
            @RequestParam(required = false) Long parentCommentId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.ok(response);
            }
            
            Comment savedComment = commentService.createComment(
                postId, 
                content, 
                currentUser, 
                parentCommentId
            );
            
            response.put("success", true);
            response.put("message", "Comment added successfully");
            response.put("commentId", savedComment.getId());
            
            String commentHtml = generateCommentHtml(savedComment);
            response.put("commentHtml", commentHtml);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error creating comment: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Form modifica commento
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('USER')")
    public String editCommentForm(@PathVariable Long id, Model model) {
        
        Comment comment = commentService.findById(id);
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || (!SecurityUtils.isAdmin() && !comment.isOwnedBy(currentUser))) {
            return "redirect:/posts/" + comment.getPost().getId() + "?error=access_denied";
        }
        
        model.addAttribute("comment", comment);
        model.addAttribute("pageTitle", "Edit Comment");
        
        return "comments/edit";
    }

    /**
     * Aggiorna commento
     */
    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('USER')")
    public String updateComment(@PathVariable Long id,
                               @RequestParam String content,
                               RedirectAttributes redirectAttributes) {
        
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            Comment updatedComment = commentService.updateComment(id, content, currentUser);
            
            redirectAttributes.addFlashAttribute("success", "Comment updated successfully!");
            return "redirect:/posts/" + updatedComment.getPost().getId() + "#comment-" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/comments/" + id + "/edit";
        }
    }

    /**
     * Cancella commento
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String deleteComment(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            Long postId = commentService.deleteComment(id, currentUser);
            
            redirectAttributes.addFlashAttribute("success", "Comment deleted successfully!");
            return "redirect:/posts/" + postId;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/posts";
        }
    }

    /**
     * Like/Unlike commento (AJAX)
     */
    @PostMapping("/{id}/like")
    @PreAuthorize("hasRole('USER')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long id) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            Comment comment = commentService.toggleLike(id, currentUser);
            
            response.put("success", true);
            response.put("likeCount", comment.getLikeCount());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error toggling like: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Carica replies di un commento (AJAX)
     */
    @GetMapping("/{id}/replies")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loadReplies(@PathVariable Long id) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Comment> replies = commentService.findRepliesByParentId(id);
            
            StringBuilder repliesHtml = new StringBuilder();
            for (Comment reply : replies) {
                repliesHtml.append(generateCommentHtml(reply));
            }
            
            response.put("success", true);
            response.put("repliesHtml", repliesHtml.toString());
            response.put("replyCount", replies.size());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error loading replies: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Segnala commento inappropriato - VERSIONE CORRETTA E COMPLETA
     */
    @PostMapping("/{id}/report")
    @PreAuthorize("hasRole('USER')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reportComment(
            @PathVariable Long id,
            @RequestParam Report.ReportReason reason,
            @RequestParam(required = false) String details) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            Comment commentToReport = commentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Comment with ID " + id + " not found."));

            // L'utente non può segnalare i propri commenti
            if (commentToReport.isOwnedBy(currentUser)) {
                response.put("success", false);
                response.put("message", "You cannot report your own comment.");
                return ResponseEntity.ok(response);
            }

            // Controlla se l'utente ha già segnalato questo commento
            boolean alreadyReported = reportRepository.existsByCommentAndReportedBy(commentToReport, currentUser);
            if (alreadyReported) {
                response.put("success", false);
                response.put("message", "You have already reported this comment.");
                return ResponseEntity.ok(response);
            }

            // Crea e salva la nuova segnalazione
            Report newReport = new Report();
            newReport.setComment(commentToReport);
            newReport.setReportedBy(currentUser);
            newReport.setReason(reason);
            if (details != null && !details.trim().isEmpty()) {
                newReport.setAdditionalDetails(details.trim());
            }
            reportRepository.save(newReport);

            // Aggiorna lo stato del commento
            if(commentToReport.getReportCount() != null){
                commentToReport.incrementReportCount();
            }
            commentToReport.setIsReported(true);
            commentRepository.save(commentToReport);
            
            response.put("success", true);
            response.put("message", "Comment reported successfully. Thank you for keeping our community safe.");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error reporting comment: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Lista commenti dell'utente
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public String myComments(Model model) {
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        List<Comment> userComments = commentService.findCommentsByUser(currentUser);
        
        model.addAttribute("comments", userComments);
        model.addAttribute("pageTitle", "My Comments");
        model.addAttribute("totalComments", userComments.size());
        
        return "comments/my-comments";
    }

    /**
     * Admin - Lista commenti segnalati
     */
    @GetMapping("/reported")
    @PreAuthorize("hasRole('ADMIN')")
    public String reportedComments(Model model) {
        
        List<Comment> reportedComments = commentService.findReportedComments();
        
        model.addAttribute("comments", reportedComments);
        model.addAttribute("pageTitle", "Reported Comments");
        model.addAttribute("totalReported", reportedComments.size());
        
        return "admin/reported-comments";
    }

    /**
     * Helper method per generare HTML del commento - VERSIONE MIGLIORATA
     */
    private String generateCommentHtml(Comment comment) {
        StringBuilder html = new StringBuilder();
        
        html.append("<div class='comment-item' id='comment-").append(comment.getId()).append("'>");
        html.append("<div class='comment-content'>");
        
        html.append("<div class='comment-header'>");
        html.append("<span class='comment-author'>");
        html.append("<a href='/users/").append(comment.getAuthor().getUsername()).append("'>");
        html.append(comment.getAuthor().getUsername());
        html.append("</a>");
        html.append("</span>");
        html.append("<span class='comment-time'>");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        html.append(comment.getCreatedDate().format(formatter));
        html.append("</span>");
        html.append("</div>");
        
        html.append("<div class='comment-body'>");
        html.append(escapeHtml(comment.getContent()));
        html.append("</div>");
        
        html.append("<div class='comment-actions'>");
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            html.append("<button class='comment-action' onclick='likeComment(").append(comment.getId()).append(")'>");
            html.append("<i class='fas fa-heart'></i> Like (").append(comment.getLikeCount()).append(")");
            html.append("</button>");
            
            if (!comment.isOwnedBy(currentUser)) {
                html.append("<button class='comment-action' onclick='showReportModal(").append(comment.getId()).append(")'>");
                html.append("<i class='fas fa-flag'></i> Report");
                html.append("</button>");
            }
            
            if (comment.isOwnedBy(currentUser)) {
                html.append("<a class='comment-action' href='/comments/").append(comment.getId()).append("/edit'>");
                html.append("<i class='fas fa-edit'></i> Edit");
                html.append("</a>");
                
                html.append("<button class='comment-action danger' onclick='deleteComment(").append(comment.getId()).append(")'>");
                html.append("<i class='fas fa-trash'></i> Delete");
                html.append("</button>");
            }
            else if (SecurityUtils.isAdmin()) {
                html.append("<button class='comment-action danger' onclick='deleteComment(").append(comment.getId()).append(")'>");
                html.append("<i class='fas fa-trash'></i> Delete");
                html.append("</button>");
            }
        }
        
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");
        
        return html.toString();
    }

    /**
     * Helper method per escape HTML
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#039;");
    }
}