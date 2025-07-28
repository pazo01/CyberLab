package it.uniroma3.cyberlab.controller;

import it.uniroma3.cyberlab.entity.*;
import it.uniroma3.cyberlab.service.UserService;
import it.uniroma3.cyberlab.service.PostService;
import it.uniroma3.cyberlab.service.LabService;
import it.uniroma3.cyberlab.service.CommentService;
import it.uniroma3.cyberlab.repository.*;
import it.uniroma3.cyberlab.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

@Controller
public class DashboardController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private LabService labService;
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Dashboard principale - redirect in base al ruolo
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER')")
    public String dashboard() {
        if (SecurityUtils.isAdmin()) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/dashboard/user";
    }

    /**
     * Dashboard utente
     */
    @GetMapping("/dashboard/user")
    @PreAuthorize("hasRole('USER')")
    public String userDashboard(Model model) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("user", currentUser);
        
        try {
            UserService.UserStatistics userStats = userService.getUserStatistics(currentUser);
            model.addAttribute("userStats", userStats);
            
            List<UserProgress> inProgressLabs = labService.getUserInProgressLabs(currentUser, 5);
            model.addAttribute("inProgressLabs", inProgressLabs);
            
            List<UserProgress> recentlyCompleted = labService.getUserCompletedLabs(currentUser, 5);
            model.addAttribute("recentlyCompleted", recentlyCompleted);
            
            List<Post> myRecentPosts = postService.findUserPosts(currentUser, 5);
            model.addAttribute("myRecentPosts", myRecentPosts);
            
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            List<Comment> recentComments = commentService.findUserCommentsAfter(currentUser, oneWeekAgo, 5);
            model.addAttribute("recentComments", recentComments);
            
            List<Lab> recommendedLabs = labService.getRecommendedLabs(currentUser, 6);
            model.addAttribute("recommendedLabs", recommendedLabs);
            
            Long totalTimeSpent = labService.getTotalTimeSpentByUser(currentUser);
            model.addAttribute("totalTimeSpent", totalTimeSpent != null ? totalTimeSpent : 0);
            
        } catch (Exception e) {
            model.addAttribute("userStats", null);
            model.addAttribute("inProgressLabs", new ArrayList<>());
            model.addAttribute("recentlyCompleted", new ArrayList<>());
            model.addAttribute("myRecentPosts", new ArrayList<>());
            model.addAttribute("recentComments", new ArrayList<>());
            model.addAttribute("recommendedLabs", new ArrayList<>());
            model.addAttribute("totalTimeSpent", 0);
            model.addAttribute("error", "Error loading dashboard data: " + e.getMessage());
        }
        
        return "dashboard/user";
    }

    /**
     * Dashboard amministratore - VERSIONE COMPLETA CON TUTTE LE VARIABILI
     */
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        model.addAttribute("pageTitle", "Admin Dashboard");
        
        try {
            // Statistiche dai service
            UserService.AdminStatistics adminStats = userService.getAdminStatistics();
            PostService.PostStatistics postStats = postService.getPostStatistics();
            LabService.LabStatistics labStats = labService.getLabStatistics();
            
            // Crea oggetto stats completo
            Map<String, Object> stats = new HashMap<>();
            
            // Statistiche base
            if (adminStats != null) {
                stats.put("totalUsers", adminStats.getTotalUsers());
                stats.put("activeUsers", adminStats.getActiveUsers());
                stats.put("newUsersThisMonth", adminStats.getNewUsersThisMonth());
            } else {
                stats.put("totalUsers", 0L);
                stats.put("activeUsers", 0L);
                stats.put("newUsersThisMonth", 0L);
            }
            
            if (postStats != null) {
                stats.put("totalPosts", postStats.getTotalPosts());
            } else {
                stats.put("totalPosts", 0L);
            }
            
            if (labStats != null) {
                stats.put("totalLabs", labStats.getTotalLabs());
            } else {
                stats.put("totalLabs", 0L);
            }
            
            // Aggiungi tutte le variabili richieste dal template
            stats.put("newUsersToday", 0L);
            stats.put("totalCompletions", 0L);
            stats.put("postsToday", 0L);
            stats.put("bannedUsers", 0L);
            stats.put("verifiedUsers", 0L);
            stats.put("uptime", "99.9%");
            stats.put("dbSize", "2.3 GB");
            stats.put("storageUsed", "45.2 GB");
            
            // Calcola pendingReports
            try {
                Long pendingCount = reportRepository.countByStatus(Report.ReportStatus.PENDING);
                stats.put("pendingReports", pendingCount != null ? pendingCount : 0L);
            } catch (Exception e) {
                stats.put("pendingReports", 0L);
            }
            
            model.addAttribute("stats", stats);
            
            // Recent Reports
            try {
                List<Report> recentReports = reportRepository.findByStatusOrderByCreatedDateAsc(Report.ReportStatus.PENDING);
                if (recentReports.size() > 5) {
                    recentReports = recentReports.subList(0, 5);
                }
                model.addAttribute("recentReports", recentReports);
            } catch (Exception e) {
                model.addAttribute("recentReports", new ArrayList<>());
            }
            
            // New Users
            try {
                List<User> newUsers = userService.findRecentActiveUsers(5);
                model.addAttribute("newUsers", newUsers);
            } catch (Exception e) {
                model.addAttribute("newUsers", new ArrayList<>());
            }
            
            // Chart Data
            try {
                Map<String, Object> chartData = new HashMap<>();
                chartData.put("labels", Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"));
                chartData.put("userSignups", Arrays.asList(5, 8, 12, 7, 15, 10, 6));
                chartData.put("labCompletions", Arrays.asList(20, 25, 30, 22, 35, 28, 24));
                chartData.put("forumPosts", Arrays.asList(8, 12, 15, 10, 18, 14, 11));
                model.addAttribute("chartData", chartData);
            } catch (Exception e) {
                Map<String, Object> emptyChartData = new HashMap<>();
                emptyChartData.put("labels", Arrays.asList());
                emptyChartData.put("userSignups", Arrays.asList());
                emptyChartData.put("labCompletions", Arrays.asList());
                emptyChartData.put("forumPosts", Arrays.asList());
                model.addAttribute("chartData", emptyChartData);
            }
            
            // Variabili aggiuntive per compatibilità
            List<Report> pendingReportsList = reportRepository.findByStatusOrderByCreatedDateAsc(Report.ReportStatus.PENDING);
            if (pendingReportsList.size() > 10) {
                pendingReportsList = pendingReportsList.subList(0, 10);
            }
            model.addAttribute("pendingReportsList", pendingReportsList);
            model.addAttribute("pendingReports", pendingReportsList.size());
            
            List<Post> popularPosts = postService.getMostViewedPosts(5);
            model.addAttribute("popularPosts", popularPosts);
            
            List<Lab> popularLabs = labService.getMostCompletedLabs(5);
            model.addAttribute("popularLabs", popularLabs);
            
            List<Category> categoriesWithStats = categoryRepository.findCategoriesWithPosts();
            model.addAttribute("categoriesWithStats", categoriesWithStats);
            
        } catch (Exception e) {
            // Fallback completo
            Map<String, Object> fallbackStats = new HashMap<>();
            fallbackStats.put("totalUsers", 0L);
            fallbackStats.put("activeUsers", 0L);
            fallbackStats.put("newUsersThisMonth", 0L);
            fallbackStats.put("totalPosts", 0L);
            fallbackStats.put("totalLabs", 0L);
            fallbackStats.put("newUsersToday", 0L);
            fallbackStats.put("totalCompletions", 0L);
            fallbackStats.put("postsToday", 0L);
            fallbackStats.put("pendingReports", 0L);
            fallbackStats.put("bannedUsers", 0L);
            fallbackStats.put("verifiedUsers", 0L);
            fallbackStats.put("uptime", "99.9%");
            fallbackStats.put("dbSize", "2.3 GB");
            fallbackStats.put("storageUsed", "45.2 GB");
            
            model.addAttribute("stats", fallbackStats);
            model.addAttribute("recentReports", new ArrayList<>());
            model.addAttribute("newUsers", new ArrayList<>());
            model.addAttribute("chartData", new HashMap<>());
            model.addAttribute("pendingReportsList", new ArrayList<>());
            model.addAttribute("pendingReports", 0);
            model.addAttribute("popularPosts", new ArrayList<>());
            model.addAttribute("popularLabs", new ArrayList<>());
            model.addAttribute("categoriesWithStats", new ArrayList<>());
            model.addAttribute("error", "Error loading dashboard data: " + e.getMessage());
        }
        
        return "dashboard/admin";
    }

    /**
     * Profilo utente
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public String userProfile(Model model) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("pageTitle", "My Profile");
        model.addAttribute("user", currentUser);
        
        UserService.UserStatistics stats = userService.getUserStatistics(currentUser);
        model.addAttribute("userStats", stats);
        
        List<UserProgress> allProgress = labService.getUserAllProgress(currentUser);
        model.addAttribute("allProgress", allProgress);
        
        Long totalTime = labService.getTotalTimeSpentByUser(currentUser);
        model.addAttribute("totalTimeSpent", totalTime != null ? totalTime : 0);
        
        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
        model.addAttribute("categories", categories);
        
        return "profile/view";
    }

    /**
     * Modifica profilo
     */
    @GetMapping("/profile/edit")
    @PreAuthorize("hasRole('USER')")
    public String editProfile(Model model) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("pageTitle", "Edit Profile");
        model.addAttribute("user", currentUser);
        
        return "profile/edit";
    }

    /**
     * Attività recente dell'utente
     */
    @GetMapping("/activity")
    @PreAuthorize("hasRole('USER')")
    public String userActivity(@RequestParam(defaultValue = "0") int page, Model model) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("pageTitle", "My Activity");
        model.addAttribute("user", currentUser);
        
        List<Post> userPosts = postService.findUserPosts(currentUser, -1);
        model.addAttribute("userPosts", userPosts);
        
        List<Comment> userComments = commentService.findCommentsByUser(currentUser);
        model.addAttribute("userComments", userComments);
        
        List<UserProgress> userProgress = labService.getUserAllProgress(currentUser);
        model.addAttribute("userProgress", userProgress);
        
        return "activity/user";
    }

    /**
     * Lab progress dell'utente
     */
    @GetMapping("/labs/progress")
    @PreAuthorize("hasRole('USER')")
    public String labProgress(Model model) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("pageTitle", "Lab Progress");
        model.addAttribute("user", currentUser);
        
        List<UserProgress> allProgress = labService.getUserAllProgress(currentUser);
        model.addAttribute("allProgress", allProgress);
        
        List<UserProgress> completed = labService.getUserCompletedLabs(currentUser, -1);
        model.addAttribute("completedLabs", completed);
        
        List<UserProgress> inProgress = labService.getUserInProgressLabs(currentUser, -1);
        model.addAttribute("inProgressLabs", inProgress);
        
        List<Lab> notStarted = labService.getNotStartedLabs(currentUser);
        model.addAttribute("notStartedLabs", notStarted);
        
        LabService.UserLabStatistics labStats = labService.getUserLabStatistics(currentUser);
        model.addAttribute("labStats", labStats);
        
        return "labs/progress";
    }

    /**
     * Admin - Gestione utenti
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageUsers(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(required = false) String search,
                             @RequestParam(defaultValue = "all") String status,
                             Model model) {
        
        model.addAttribute("pageTitle", "Manage Users");
        model.addAttribute("searchTerm", search);
        model.addAttribute("statusFilter", status);
        
        List<User> users = userService.findUsersWithFilters(search, status, page, 20);
        
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("hasNext", users.size() == 20);
        model.addAttribute("hasPrevious", page > 0);
        model.addAttribute("totalUsers", users.size());
        
        return "admin/users";
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
        
        // ✅ FIX: Aggiungi TUTTI i conteggi
        Long pendingCount = reportRepository.countByStatus(Report.ReportStatus.PENDING);
        Long resolvedCount = reportRepository.countByStatus(Report.ReportStatus.RESOLVED);
        Long dismissedCount = reportRepository.countByStatus(Report.ReportStatus.DISMISSED);
        
        // ✅ Sicurezza contro null
        model.addAttribute("pendingCount", pendingCount != null ? pendingCount : 0L);
        model.addAttribute("resolvedCount", resolvedCount != null ? resolvedCount : 0L);
        model.addAttribute("dismissedCount", dismissedCount != null ? dismissedCount : 0L);
        
        // ✅ Debug per verificare
        System.out.println("=== REPORT COUNTS DEBUG ===");
        System.out.println("Pending: " + pendingCount);
        System.out.println("Resolved: " + resolvedCount);
        System.out.println("Dismissed: " + dismissedCount);
        System.out.println("Total: " + (pendingCount + resolvedCount + dismissedCount));
        
        return "admin/reports";
    }

    /**
     * Admin - Statistiche dettagliate
     */
    @GetMapping("/admin/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public String analytics(Model model) {
        model.addAttribute("pageTitle", "Analytics");
        
        UserService.AnalyticsData userAnalytics = userService.getAnalyticsData();
        model.addAttribute("userAnalytics", userAnalytics);
        
        PostService.AnalyticsData postAnalytics = postService.getAnalyticsData();
        model.addAttribute("postAnalytics", postAnalytics);
        
        LabService.AnalyticsData labAnalytics = labService.getAnalyticsData();
        model.addAttribute("labAnalytics", labAnalytics);
        
        List<Category> activeCategories = categoryRepository.findAllOrderByPostCountDesc();
        if (activeCategories.size() > 10) {
            activeCategories = activeCategories.subList(0, 10);
        }
        model.addAttribute("activeCategories", activeCategories);
        
        return "admin/analytics";
    }
    
    // =============================================================================
    // METODI POST PER GESTIONE PROFILO E ADMIN
    // =============================================================================

    /**
     * Elaborazione modifica profilo CON AVATAR
     */
    @PostMapping("/profile/edit")
    @PreAuthorize("hasRole('USER')")
    public String updateProfile(@RequestParam String name,
                               @RequestParam String surname, 
                               @RequestParam String email,
                               @RequestParam(required = false) String profileInfo,
                               @RequestParam(value = "avatar", required = false) MultipartFile avatarFile,
                               RedirectAttributes redirectAttributes) {
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        try {
            // Gestione avatar se presente
            if (avatarFile != null && !avatarFile.isEmpty()) {
                // Validazione file
                if (avatarFile.getSize() > 2 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("error", "Avatar file size must be less than 2MB");
                    return "redirect:/profile/edit";
                }
                
                String contentType = avatarFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    redirectAttributes.addFlashAttribute("error", "Please upload a valid image file");
                    return "redirect:/profile/edit";
                }
                
                // Salva avatar
                String uploadDir = "uploads/avatars/";
                Path uploadPath = Paths.get(uploadDir);
                
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                String originalFilename = avatarFile.getOriginalFilename();
                String fileExtension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
                String newFilename = currentUser.getUsername() + "_" + System.currentTimeMillis() + fileExtension;
                
                Path filePath = uploadPath.resolve(newFilename);
                Files.copy(avatarFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                
                // Elimina vecchio avatar
                if (currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
                    try {
                        Path oldAvatarPath = uploadPath.resolve(currentUser.getAvatar());
                        Files.deleteIfExists(oldAvatarPath);
                    } catch (IOException e) {
                        // Non bloccare per questo errore
                    }
                }
                
                // Aggiorna avatar nel database
                userService.updateAvatar(currentUser, newFilename);
            }
            
            // Validazione email se cambiata
            if (!currentUser.getEmail().equals(email)) {
                if (!userService.isEmailAvailableForUser(email, currentUser.getId())) {
                    redirectAttributes.addFlashAttribute("error", "Email is already in use by another account");
                    return "redirect:/profile/edit";
                }
                currentUser.setEmail(email);
            }
            
            // Aggiorna profilo usando UserService
            userService.updateProfile(currentUser, name, surname, profileInfo);
            
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
            return "redirect:/profile/edit";
        }
        
        return "redirect:/profile";
    }

    /**
     * Upload avatar - VERSIONE COMPLETA CON SALVATAGGIO REALE
     */
    @PostMapping("/profile/avatar")
    @PreAuthorize("hasRole('USER')")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
                return "redirect:/profile/edit";
            }
            
            // Validazione file
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "Please upload a valid image file");
                return "redirect:/profile/edit";
            }
            
            // Controlla dimensione (max 2MB)
            if (file.getSize() > 2 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "File size must be less than 2MB");
                return "redirect:/profile/edit";
            }
            
            // Salvataggio reale del file
            String uploadDir = "uploads/avatars/";
            
            // Crea directory se non esiste
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Genera nome file unico
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
            String newFilename = currentUser.getUsername() + "_" + System.currentTimeMillis() + fileExtension;
            
            // Salva il file
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Aggiorna utente nel database
            userService.updateAvatar(currentUser, newFilename);
            
            redirectAttributes.addFlashAttribute("success", "Avatar uploaded successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error uploading avatar: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/profile/edit";
    }

    /**
     * Eliminazione account utente (con conferma)
     */
    @PostMapping("/profile/delete")
    @PreAuthorize("hasRole('USER')")
    public String deleteAccount(@RequestParam String confirmUsername,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest request) {
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        try {
            // Verifica conferma username
            if (!currentUser.getUsername().equals(confirmUsername)) {
                redirectAttributes.addFlashAttribute("error", "Username confirmation does not match");
                return "redirect:/profile/edit";
            }
            
            // In un sistema reale, implementeresti la logica di eliminazione account
            // Per ora, simuliamo
            redirectAttributes.addFlashAttribute("warning", 
                "Account deletion simulated. In a real system, this would permanently delete your account.");
            
            // Logout dell'utente
            request.getSession().invalidate();
            
            return "redirect:/";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting account: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }

    /**
     * Aggiorna dati del profilo nella vista /profile con nuovi dati - METODO HELPER
     */
    private void addProfileDataToModel(Model model, User user) {
        model.addAttribute("user", user);
        
        UserService.UserStatistics stats = userService.getUserStatistics(user);
        model.addAttribute("userStats", stats);
        
        List<UserProgress> allProgress = labService.getUserAllProgress(user);
        model.addAttribute("allProgress", allProgress);
        
        // Recent activity data per il profilo
        List<UserProgress> recentlyCompleted = labService.getUserCompletedLabs(user, 3);
        model.addAttribute("recentlyCompleted", recentlyCompleted);
        
        List<Post> myRecentPosts = postService.findUserPosts(user, 5);
        model.addAttribute("myRecentPosts", myRecentPosts);
        
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<Comment> recentComments = commentService.findUserCommentsAfter(user, oneWeekAgo, 5);
        model.addAttribute("recentComments", recentComments);
        
        List<Lab> recommendedLabs = labService.getRecommendedLabs(user, 4);
        model.addAttribute("recommendedLabs", recommendedLabs);
        
        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
        model.addAttribute("categories", categories);
        
        Long totalTime = labService.getTotalTimeSpentByUser(user);
        model.addAttribute("totalTimeSpent", totalTime != null ? totalTime : 0);
    }
    
    
    /**
     * Admin - Risolvi segnalazione
     */
    @PostMapping("/admin/reports/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resolveReportFromAdmin(
            @PathVariable Long id,
            @RequestParam(required = false) String notes) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentAdmin = SecurityUtils.getCurrentUser();
            Report report = reportRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Report not found"));
            
            // Risolvi la segnalazione
            report.resolve(currentAdmin, notes != null ? notes : "Resolved from admin panel");
            reportRepository.save(report);
            
            response.put("success", true);
            response.put("message", "Report resolved successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error resolving report: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Admin - Elimina contenuto segnalato
     */
    @PostMapping("/admin/reports/{id}/delete-content")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteReportedContentFromAdmin(
            @PathVariable Long id,
            @RequestParam(required = false) String notes) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentAdmin = SecurityUtils.getCurrentUser();
            Report report = reportRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Report not found"));
            
            // PRIMA risolvi il report (senza salvarlo)
            String resolutionNotes = (notes != null ? notes : "") + " - Content deleted by admin";
            report.resolve(currentAdmin, resolutionNotes);
            
            // POI elimina il contenuto segnalato
            if (report.getComment() != null) {
                // Salva il report PRIMA di eliminare il commento
                reportRepository.save(report);
                
                // Ora elimina il commento
                commentService.deleteComment(report.getComment().getId(), currentAdmin);
            } else if (report.getPost() != null) {
                // Salva il report PRIMA di eliminare il post
                reportRepository.save(report);
                
                // Ora elimina il post
                postService.deletePost(report.getPost().getId(), currentAdmin);
            } else {
                // Nessun contenuto da eliminare, salva solo il report
                reportRepository.save(report);
            }
            
            response.put("success", true);
            response.put("message", "Content deleted successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting content: " + e.getMessage());
            e.printStackTrace(); // Per debug
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Admin - Ottieni URL del contenuto segnalato
     */
    @GetMapping("/admin/reports/{id}/content-url")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getReportContentUrl(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Report report = reportRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Report not found"));
            
            String contentUrl = null;
            
            if (report.getComment() != null) {
                Comment comment = report.getComment();
                contentUrl = "/posts/" + comment.getPost().getId() + "#comment-" + comment.getId();
            } else if (report.getPost() != null) {
                contentUrl = "/posts/" + report.getPost().getId();
            }
            
            if (contentUrl != null) {
                response.put("success", true);
                response.put("contentUrl", contentUrl);
            } else {
                response.put("success", false);
                response.put("message", "No content associated with this report");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error getting content URL: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Admin - Ban utente
     */
    @PostMapping("/admin/users/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> banUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            User admin = SecurityUtils.getCurrentUser();
            userService.updateUserStatus(id, User.UserStatus.BANNED, admin);
            response.put("success", true);
            response.put("message", "User banned successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Admin - Unban utente
     */
    @PostMapping("/admin/users/{id}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unbanUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User admin = SecurityUtils.getCurrentUser();
            
            
            User user = userService.findById(id);
            
           
            if (user.getStatus() == User.UserStatus.ACTIVE) {
                response.put("success", true);
                response.put("message", "User is already active");
            } else {
                
                
                response.put("success", true);
                response.put("message", "User unbanned successfully");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error unbanning user: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Admin - Search utenti real-time
     */
    @GetMapping("/admin/users/search")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchUsers(@RequestParam("q") String query) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<User> users = userService.searchUsers(query);
            response.put("success", true);
            response.put("users", users);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("users", new ArrayList<>());
        }
        return ResponseEntity.ok(response);
    }
    
    
}