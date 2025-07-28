package it.uniroma3.cyberlab.controller;

import it.uniroma3.cyberlab.entity.Category;
import it.uniroma3.cyberlab.entity.User;
import it.uniroma3.cyberlab.security.CustomUserPrincipal;
import it.uniroma3.cyberlab.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller per la gestione delle categorie da parte degli admin
 */
@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Pagina di gestione categorie
     */
    @GetMapping
    public String categoriesPage(Model model) {
        try {
            List<Category> categories = categoryService.findAllCategories();
            model.addAttribute("categories", categories);
            
            // Statistiche per il template
            model.addAttribute("totalCategories", categories.size());
            model.addAttribute("categoriesWithContent", 
                categories.stream().filter(c -> c.getPostCount() > 0 || c.getLabCount() > 0).count());
            
            return "admin/categories";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading categories: " + e.getMessage());
            model.addAttribute("categories", List.of());
            return "admin/categories";
        }
    }

    /**
     * Crea nuova categoria
     */
    @PostMapping
    public String createCategory(@RequestParam String name,
                               @RequestParam(required = false) String description,
                               @RequestParam(defaultValue = "#6c757d") String color,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        try {
            // Ottieni l'utente dal CustomUserPrincipal
            CustomUserPrincipal userPrincipal = (CustomUserPrincipal) auth.getPrincipal();
            User admin = userPrincipal.getUser(); // Assumendo che CustomUserPrincipal abbia un metodo getUser()
            
            // Validazione base
            if (name == null || name.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Category name is required");
                return "redirect:/admin/categories";
            }
            
            if (name.trim().length() > 50) {
                redirectAttributes.addFlashAttribute("errorMessage", "Category name must not exceed 50 characters");
                return "redirect:/admin/categories";
            }
            
            // Crea categoria
            Category newCategory = categoryService.createCategory(
                name.trim(), 
                description != null ? description.trim() : null, 
                color != null ? color.trim() : "#6c757d", 
                admin
            );
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Category '" + newCategory.getName() + "' created successfully");
                
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "An error occurred while creating the category");
        }
        
        return "redirect:/admin/categories";
    }

    /**
     * Aggiorna categoria esistente
     */
    @PutMapping("/{id}")
    public String updateCategory(@PathVariable Long id,
                               @RequestParam String name,
                               @RequestParam(required = false) String description,
                               @RequestParam(defaultValue = "#6c757d") String color,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        try {
            // Ottieni l'utente dal CustomUserPrincipal
            CustomUserPrincipal userPrincipal = (CustomUserPrincipal) auth.getPrincipal();
            User admin = userPrincipal.getUser();
            
            // Validazione base
            if (name == null || name.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Category name is required");
                return "redirect:/admin/categories";
            }
            
            if (name.trim().length() > 50) {
                redirectAttributes.addFlashAttribute("errorMessage", "Category name must not exceed 50 characters");
                return "redirect:/admin/categories";
            }
            
            // Aggiorna categoria
            Category updatedCategory = categoryService.updateCategory(
                id,
                name.trim(), 
                description != null ? description.trim() : null, 
                color != null ? color.trim() : "#6c757d", 
                admin
            );
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Category '" + updatedCategory.getName() + "' updated successfully");
                
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "An error occurred while updating the category");
        }
        
        return "redirect:/admin/categories";
    }

    /**
     * Elimina categoria
     */
    @DeleteMapping("/{id}")
    public String deleteCategory(@PathVariable Long id,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        try {
            // Ottieni l'utente dal CustomUserPrincipal
            CustomUserPrincipal userPrincipal = (CustomUserPrincipal) auth.getPrincipal();
            User admin = userPrincipal.getUser();
            
            // Trova categoria per ottenere il nome prima di eliminarla
            Category category = categoryService.findById(id);
            String categoryName = category.getName();
            
            // Verifica se può essere eliminata
            if (!categoryService.canDeleteCategory(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Cannot delete category '" + categoryName + "' because it has associated posts or labs");
                return "redirect:/admin/categories";
            }
            
            // Elimina categoria
            categoryService.deleteCategory(id, admin);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Category '" + categoryName + "' deleted successfully");
                
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "An error occurred while deleting the category");
        }
        
        return "redirect:/admin/categories";
    }

    /**
     * API endpoint per verificare disponibilità nome categoria
     */
    @GetMapping("/check-name")
    @ResponseBody
    public boolean checkCategoryNameAvailability(@RequestParam String name,
                                                @RequestParam(required = false) Long excludeId) {
        try {
            if (excludeId != null) {
                return categoryService.isCategoryNameAvailable(name, excludeId);
            } else {
                return categoryService.isCategoryNameAvailable(name);
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * API endpoint per ottenere statistiche categoria
     */
    @GetMapping("/{id}/stats")
    @ResponseBody
    public CategoryService.CategoryStatistics getCategoryStats(@PathVariable Long id) {
        try {
            return categoryService.getCategoryStatistics(id);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Pagina per spostare contenuti tra categorie (opzionale)
     */
    @GetMapping("/{fromId}/move-to/{toId}")
    public String moveCategoryContent(@PathVariable Long fromId,
                                    @PathVariable Long toId,
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Ottieni l'utente dal CustomUserPrincipal
            CustomUserPrincipal userPrincipal = (CustomUserPrincipal) auth.getPrincipal();
            User admin = userPrincipal.getUser();
            
            Category fromCategory = categoryService.findById(fromId);
            Category toCategory = categoryService.findById(toId);
            
            categoryService.moveCategoryContent(fromId, toId, admin);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Content moved successfully from '" + fromCategory.getName() + 
                "' to '" + toCategory.getName() + "'");
                
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "An error occurred while moving content");
        }
        
        return "redirect:/admin/categories";
    }

    /**
     * API endpoint per ricerca categorie (opzionale)
     */
    @GetMapping("/search")
    @ResponseBody
    public List<Category> searchCategories(@RequestParam String q) {
        try {
            return categoryService.searchCategories(q);
        } catch (Exception e) {
            return List.of();
        }
    }
}