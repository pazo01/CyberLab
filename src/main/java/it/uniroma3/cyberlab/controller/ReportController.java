package it.uniroma3.cyberlab.controller;

import it.uniroma3.cyberlab.entity.*;
import it.uniroma3.cyberlab.repository.*;
import it.uniroma3.cyberlab.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private UserRepository userRepository;

    /*
     * Il metodo @PostMapping("/create") che si trovava qui è stato rimosso.
     * La sua presenza creava un potenziale conflitto con la creazione dei commenti.
     * La funzionalità di segnalazione è già gestita correttamente dal CommentController
     * all'endpoint /comments/{id}/report, come usato dal frontend in view.html.
     */

    /**
     * Le mie segnalazioni
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public String myReports(Model model) {
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        List<Report> userReports = reportRepository.findByReportedByOrderByCreatedDateDesc(currentUser);
        
        model.addAttribute("reports", userReports);
        model.addAttribute("pageTitle", "My Reports");
        model.addAttribute("totalReports", userReports.size());
        
        // Statistiche per status
        long pendingCount = userReports.stream().filter(Report::isPending).count();
        long resolvedCount = userReports.stream().filter(Report::isResolved).count();
        long dismissedCount = userReports.stream().filter(Report::isDismissed).count();
        
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("dismissedCount", dismissedCount);
        
        return "reports/my-reports";
    }

    /**
     * Admin - Gestione segnalazioni
     */
    @GetMapping("/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageReports(@RequestParam(defaultValue = "PENDING") String status,
                               Model model) {
        
        model.addAttribute("pageTitle", "Manage Reports");
        model.addAttribute("statusFilter", status);
        
        List<Report> reports;
        try {
            Report.ReportStatus reportStatus = Report.ReportStatus.valueOf(status);
            reports = reportRepository.findByStatusOrderByCreatedDateDesc(reportStatus);
        } catch (IllegalArgumentException e) {
            reports = reportRepository.findAll();
        }
        
        model.addAttribute("reports", reports);
        model.addAttribute("totalReports", reports.size());
        
        model.addAttribute("pendingCount", reportRepository.countByStatus(Report.ReportStatus.PENDING));
        model.addAttribute("resolvedCount", reportRepository.countByStatus(Report.ReportStatus.RESOLVED));
        
        return "admin/reports";
    }

    /**
     * Admin - Visualizza singola segnalazione
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewReport(@PathVariable Long id, Model model) {
        
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        model.addAttribute("report", report);
        model.addAttribute("pageTitle", "Report #" + id);
        
        // Altre segnalazioni per lo stesso commento
        List<Report> relatedReports = reportRepository.findByComment(report.getComment());
        relatedReports = relatedReports.stream()
                .filter(r -> !r.getId().equals(report.getId()))
                .toList();
        model.addAttribute("relatedReports", relatedReports);
        
        // Storico segnalazioni dell'utente segnalato
        User reportedUser = report.getComment().getAuthor();
        List<Comment> userComments = commentRepository.findByAuthor(reportedUser);
        long totalReportedComments = userComments.stream()
                .filter(Comment::getIsReported)
                .count();
        model.addAttribute("userTotalReportedComments", totalReportedComments);
        
        return "admin/report-detail";
    }

    /**
     * Admin - Risolvi segnalazione
     */
    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resolveReport(
            @PathVariable Long id,
            @RequestParam(required = false) String notes,
            @RequestParam(defaultValue = "false") boolean deleteComment) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentAdmin = SecurityUtils.getCurrentUser();
            Report report = reportRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Report not found"));
            
            // Risolvi la segnalazione
            report.resolve(currentAdmin, notes);
            reportRepository.save(report);
            
            // Se richiesto, cancella il commento
            if (deleteComment) {
                Comment comment = report.getComment();
                commentRepository.delete(comment);
                response.put("commentDeleted", true);
            }
            
            response.put("success", true);
            response.put("message", "Report resolved successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error resolving report: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Admin - Rigetta segnalazione
     */
    @PostMapping("/{id}/dismiss")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> dismissReport(
            @PathVariable Long id,
            @RequestParam(required = false) String notes) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentAdmin = SecurityUtils.getCurrentUser();
            Report report = reportRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Report not found"));
            
            // Rigetta la segnalazione
            report.dismiss(currentAdmin, notes);
            reportRepository.save(report);
            
            // Decrementa contatore nel commento se era l'unica segnalazione
            Comment comment = report.getComment();
            List<Report> activeReports = reportRepository.findByComment(comment)
                    .stream()
                    .filter(r -> r.getStatus() == Report.ReportStatus.PENDING || 
                               r.getStatus() == Report.ReportStatus.UNDER_REVIEW)
                    .toList();
            
            if (activeReports.isEmpty()) {
                comment.setIsReported(false);
                comment.setReportCount(0);
                commentRepository.save(comment);
            }
            
            response.put("success", true);
            response.put("message", "Report dismissed successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error dismissing report: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Admin - Escalate segnalazione
     */
    @PostMapping("/{id}/escalate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> escalateReport(
            @PathVariable Long id,
            @RequestParam(required = false) String notes) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentAdmin = SecurityUtils.getCurrentUser();
            Report report = reportRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Report not found"));
            
            // Escalate la segnalazione
            report.escalate(currentAdmin, notes);
            reportRepository.save(report);
            
            response.put("success", true);
            response.put("message", "Report escalated successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error escalating report: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Admin - Batch action su segnalazioni
     */
    @PostMapping("/batch-action")
    @PreAuthorize("hasRole('ADMIN')")
    public String batchAction(@RequestParam List<Long> reportIds,
                             @RequestParam String action,
                             @RequestParam(required = false) String notes,
                             RedirectAttributes redirectAttributes) {
        
        try {
            User currentAdmin = SecurityUtils.getCurrentUser();
            int processedCount = 0;
            
            for (Long reportId : reportIds) {
                Report report = reportRepository.findById(reportId).orElse(null);
                if (report != null) {
                    switch (action.toLowerCase()) {
                        case "resolve":
                            report.resolve(currentAdmin, notes);
                            break;
                        case "dismiss":
                            report.dismiss(currentAdmin, notes);
                            break;
                        case "escalate":
                            report.escalate(currentAdmin, notes);
                            break;
                        default:
                            continue;
                    }
                    reportRepository.save(report);
                    processedCount++;
                }
            }
            
            redirectAttributes.addFlashAttribute("success", 
                    processedCount + " reports processed successfully");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                    "Error processing reports: " + e.getMessage());
        }
        
        return "redirect:/reports/admin";
    }

    /**
     * Admin - Statistiche segnalazioni
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public String reportStats(Model model) {
        
        model.addAttribute("pageTitle", "Report Statistics");
        
        // Statistiche generali
        long totalReports = reportRepository.count();
        long pendingReports = reportRepository.countPendingReports();
        long resolvedReports = reportRepository.countResolvedReports();
        
        model.addAttribute("totalReports", totalReports);
        model.addAttribute("pendingReports", pendingReports);
        model.addAttribute("resolvedReports", resolvedReports);
        
        // Percentuali
        double pendingPercentage = totalReports > 0 ? (double) pendingReports / totalReports * 100 : 0;
        double resolvedPercentage = totalReports > 0 ? (double) resolvedReports / totalReports * 100 : 0;
        
        model.addAttribute("pendingPercentage", pendingPercentage);
        model.addAttribute("resolvedPercentage", resolvedPercentage);
        
        // Distribuzione per motivo
        List<Object[]> reasonDistribution = reportRepository.findReportCountByReason();
        model.addAttribute("reasonDistribution", reasonDistribution);
        
        // Top admin per report risolte
        User currentAdmin = SecurityUtils.getCurrentUser();
        long adminResolvedCount = reportRepository.countReportsReviewedBy(currentAdmin);
        model.addAttribute("adminResolvedCount", adminResolvedCount);
        
        // Commenti più segnalati
        List<Comment> mostReportedComments = commentRepository.findReportedCommentsOrderByReportCount();
        if (mostReportedComments.size() > 10) {
            mostReportedComments = mostReportedComments.subList(0, 10);
        }
        model.addAttribute("mostReportedComments", mostReportedComments);
        
        return "admin/report-stats";
    }
}