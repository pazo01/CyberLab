package it.uniroma3.cyberlab.controller;

import it.uniroma3.cyberlab.entity.*;
import it.uniroma3.cyberlab.service.LabService;
import it.uniroma3.cyberlab.repository.CategoryRepository;
import it.uniroma3.cyberlab.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/labs")
public class LabController {

    @Autowired
    private LabService labService;
    
    @Autowired
    private CategoryRepository categoryRepository; 

    /**
     * Lista tutti i lab (pubblici)
     */
    @GetMapping
    public String listLabs(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) String difficulty,
                          Model model) {
        
        model.addAttribute("pageTitle", "Virtual Labs");
        
        Category selectedCategory = null;
        if (categoryId != null) {
            selectedCategory = categoryRepository.findById(categoryId).orElse(null);
        }
        
        Lab.Difficulty selectedDifficulty = null;
        if (difficulty != null && !difficulty.isEmpty()) {
            try {
                selectedDifficulty = Lab.Difficulty.valueOf(difficulty.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid difficulty
            }
        }
        
        List<Lab> labs = labService.findPublishedLabs(selectedCategory, selectedDifficulty, page, 12);
        
        model.addAttribute("labs", labs);
        model.addAttribute("currentPage", page);
        model.addAttribute("hasNext", labs.size() == 12);
        model.addAttribute("hasPrevious", page > 0);
        
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("difficulties", Lab.Difficulty.values());
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("selectedDifficulty", selectedDifficulty);
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            Map<Long, UserProgress> progressMap = labService.getUserProgressMap(currentUser);
            model.addAttribute("userProgress", progressMap);
            
            // Calculate and add user-specific stats only if the user is logged in
            LabService.UserLabStatistics userStats = labService.getUserLabStatistics(currentUser);
            model.addAttribute("userLabStats", userStats);
        }
        
        return "labs/list";
    }

    /**
     * Lab practice page by type - DEVE VENIRE PRIMA delle route con {id}
     */
    @GetMapping("/practice/{labType}")
    public String practiceLabByType(@PathVariable String labType) {
        switch (labType.toLowerCase()) {
            case "xss":
                return "labs/practice/xss";
            case "sqli":
                return "labs/practice/sqli"; 
            case "traversal":
                return "labs/practice/traversal";
            default:
                return "redirect:/labs";
        }
    }

    /**
     * Route specifiche per lab types (ALTERNATIVA)
     */
    @GetMapping("/xss")
    public String xssLab() {
        return "labs/practice/xss";
    }

    @GetMapping("/sqli")
    public String sqliLab() {
        return "labs/practice/sqli";
    }

    @GetMapping("/traversal") 
    public String traversalLab() {
        return "labs/practice/traversal";
    }

    /**
     * Visualizza lab - teoria (USA REGEX PER SOLO NUMERI)
     */
    @GetMapping("/{id:[0-9]+}")
    public String viewLab(@PathVariable Long id, Model model) {
        
        Lab lab = labService.findByIdAndIncrementViews(id);
        
        model.addAttribute("lab", lab);
        model.addAttribute("pageTitle", lab.getTitle());
        
        User currentUser = SecurityUtils.getCurrentUser();
        UserProgress userProgress = null;
        if (currentUser != null) {
            userProgress = labService.getUserProgress(currentUser, lab);
        }
        model.addAttribute("userProgress", userProgress);
        
        List<Lab> relatedLabs = labService.findRelatedLabs(lab, 4);
        model.addAttribute("relatedLabs", relatedLabs);
        
        return "labs/view";
    }

    /**
     * Lab pratico - richiede autenticazione
     */
    @GetMapping("/{id:[0-9]+}/practice")
    @PreAuthorize("hasRole('USER')")
    public String practiceLab(@PathVariable Long id, Model model) {
        
        User currentUser = SecurityUtils.getCurrentUser();
        
        UserProgress userProgress = labService.startLab(id, currentUser);
        Lab lab = userProgress.getLab();
        
        model.addAttribute("lab", lab);
        model.addAttribute("userProgress", userProgress);
        model.addAttribute("pageTitle", lab.getTitle() + " - Practice");
        
        String labType = lab.getCategory().getName().toLowerCase();
        model.addAttribute("labType", labType);
        
        return "labs/practice/" + getSandboxTemplate(labType);
    }

    /**
     * Aggiorna progresso lab (AJAX)
     */
    @PostMapping("/{id:[0-9]+}/progress")
    @PreAuthorize("hasRole('USER')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProgress(
            @PathVariable Long id,
            @RequestParam int percentage,
            @RequestParam(required = false) String notes,
            @RequestParam(defaultValue = "0") int timeSpent) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            
            UserProgress progress = labService.updateProgress(id, currentUser, percentage, notes, timeSpent);
            
            response.put("success", true);
            response.put("message", "Progress updated successfully");
            response.put("status", progress.getStatus().name());
            response.put("completionPercentage", progress.getCompletionPercentage());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating progress: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    private String getSandboxTemplate(String labType) {
        return switch (labType.toLowerCase()) {
            case "xss" -> "xss-sandbox";
            case "sql injection" -> "sqli-sandbox";
            case "path traversal" -> "path-traversal-sandbox";
            case "cryptography" -> "crypto-sandbox";
            case "binary exploitation" -> "binary-sandbox";
            default -> "general-sandbox";
        };
    }
}