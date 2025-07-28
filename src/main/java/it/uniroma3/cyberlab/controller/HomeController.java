package it.uniroma3.cyberlab.controller;

import it.uniroma3.cyberlab.entity.Category;
import it.uniroma3.cyberlab.entity.Post;
import it.uniroma3.cyberlab.entity.Lab;
import it.uniroma3.cyberlab.repository.CategoryRepository;
import it.uniroma3.cyberlab.repository.PostRepository;
import it.uniroma3.cyberlab.repository.LabRepository;
import it.uniroma3.cyberlab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private LabRepository labRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Homepage - Landing page del sito
     */
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        // Statistiche per la homepage
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalPosts", postRepository.count());
        model.addAttribute("totalLabs", labRepository.countPublishedLabs());
        model.addAttribute("totalCategories", categoryRepository.count());
        
        // Post in evidenza (featured/pinned)
        List<Post> featuredPosts = postRepository.findFeaturedAndPinnedPosts();
        if (featuredPosts.size() > 6) {
            featuredPosts = featuredPosts.subList(0, 6); // Massimo 6 post
        }
        model.addAttribute("featuredPosts", featuredPosts);
        
        // Lab più popolari
        List<Lab> popularLabs = labRepository.findMostViewedLabs(PageRequest.of(0, 4));
        model.addAttribute("popularLabs", popularLabs);
        
        // Categorie principali
        List<Category> categories = categoryRepository.findAllOrderByPostCountDesc();
        if (categories.size() > 8) {
            categories = categories.subList(0, 8); // Massimo 8 categorie
        }
        model.addAttribute("categories", categories);
        
        // Post recenti (ultima settimana)
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<Post> recentPosts = postRepository.findRecentPosts(oneWeekAgo);
        if (recentPosts.size() > 5) {
            recentPosts = recentPosts.subList(0, 5);
        }
        model.addAttribute("recentPosts", recentPosts);
        
        return "layout/homepage";
    }

    /**
     * Pagina About - Informazioni sul progetto
     */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About CyberLab");
        
        // Statistiche generali
        model.addAttribute("totalUsers", userRepository.countActiveUsers());
        model.addAttribute("totalPosts", postRepository.count());
        model.addAttribute("totalLabs", labRepository.countPublishedLabs());
        
        // Conteggio per categoria
        List<Category> categoriesWithStats = categoryRepository.findCategoriesWithPosts();
        model.addAttribute("categoriesWithStats", categoriesWithStats);
        
        return "about";
    }

    /**
     * Pagina Contact - Contatti e informazioni
     */
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "Contact Us");
        return "contact";
    }

    /**
     * Lista categorie pubbliche
     */
    @GetMapping("/categories")
    public String categories(Model model) {
        List<Category> allCategories = categoryRepository.findAllByOrderByNameAsc();
        model.addAttribute("categories", allCategories);
        model.addAttribute("pageTitle", "All Categories");
        
        return "categories";
    }

    /**
     * Vista categoria specifica con post e lab
     */
    @GetMapping("/category/{id}")
    public String categoryView(@PathVariable Long id, 
                               @RequestParam(defaultValue = "0") int page,
                               Model model) {
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        model.addAttribute("category", category);
        model.addAttribute("pageTitle", category.getName());
        
        // Post della categoria (paginati)
        PageRequest pageRequest = PageRequest.of(page, 10);
        var postPage = postRepository.findByCategoryOrderByCreatedDateDesc(category, pageRequest);
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("hasNext", postPage.hasNext());
        model.addAttribute("hasPrevious", postPage.hasPrevious());
        
        // Lab della categoria
        List<Lab> categoryLabs = labRepository.findByCategoryAndIsPublishedTrueOrderByCreatedDateDesc(category);
        model.addAttribute("labs", categoryLabs);
        
        // Statistiche categoria
        model.addAttribute("postCount", postRepository.countByCategory(category));
        model.addAttribute("labCount", labRepository.countPublishedLabsByCategory(category));
        
        return "category-view";
    }

    /**
     * Ricerca globale
     */
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q,
                        @RequestParam(defaultValue = "all") String type,
                        Model model) {
        
        model.addAttribute("query", q);
        model.addAttribute("searchType", type);
        model.addAttribute("pageTitle", "Search Results");
        
        if (q != null && !q.trim().isEmpty()) {
            String searchTerm = q.trim();
            
            if ("all".equals(type) || "posts".equals(type)) {
                List<Post> foundPosts = postRepository.searchPosts(searchTerm);
                model.addAttribute("posts", foundPosts);
                model.addAttribute("postCount", foundPosts.size());
            }
            
            if ("all".equals(type) || "labs".equals(type)) {
                List<Lab> foundLabs = labRepository.searchPublishedLabs(searchTerm);
                model.addAttribute("labs", foundLabs);
                model.addAttribute("labCount", foundLabs.size());
            }
            
            if ("all".equals(type) || "categories".equals(type)) {
                List<Category> foundCategories = categoryRepository.searchCategories(searchTerm);
                model.addAttribute("categories", foundCategories);
                model.addAttribute("categoryCount", foundCategories.size());
            }
        }
        
        return "search-results";
    }

    /**
     * Feed pubblico dei post recenti
     */
    @GetMapping("/feed")
    public String publicFeed(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) Long categoryId,
                            Model model) {
        
        model.addAttribute("pageTitle", "Latest Posts");
        
        PageRequest pageRequest = PageRequest.of(page, 20);
        
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null) {
                var postPage = postRepository.findByCategoryOrderByCreatedDateDesc(category, pageRequest);
                model.addAttribute("posts", postPage.getContent());
                model.addAttribute("selectedCategory", category);
                model.addAttribute("pageTitle", "Posts in " + category.getName());
                
                // Pagination info
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", postPage.getTotalPages());
                model.addAttribute("hasNext", postPage.hasNext());
                model.addAttribute("hasPrevious", postPage.hasPrevious());
            }
        } else {
            // Tutti i post recenti
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            List<Post> recentPosts = postRepository.findRecentPosts(oneMonthAgo);
            
            // Simuliamo paginazione manuale
            int start = page * 20;
            int end = Math.min(start + 20, recentPosts.size());
            
            if (start < recentPosts.size()) {
                model.addAttribute("posts", recentPosts.subList(start, end));
            } else {
                model.addAttribute("posts", List.of());
            }
            
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", (recentPosts.size() + 19) / 20);
            model.addAttribute("hasNext", end < recentPosts.size());
            model.addAttribute("hasPrevious", page > 0);
        }
        
        // Tutte le categorie per il filtro
        List<Category> allCategories = categoryRepository.findAllByOrderByNameAsc();
        model.addAttribute("categories", allCategories);
        
        return "public-feed";
    }

    /**
     * Statistiche pubbliche della piattaforma
     */
    @GetMapping("/stats")
    public String platformStats(Model model) {
        model.addAttribute("pageTitle", "Platform Statistics");
        
        // Statistiche generali
        model.addAttribute("totalUsers", userRepository.countActiveUsers());
        model.addAttribute("totalPosts", postRepository.count());
        model.addAttribute("totalComments", "N/A"); // Implementare se serve
        model.addAttribute("totalLabs", labRepository.countPublishedLabs());
        
        // Utenti registrati nell'ultimo mese
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        model.addAttribute("newUsersThisMonth", userRepository.countUsersJoinedAfter(oneMonthAgo));
        
        // Post dell'ultimo mese
        model.addAttribute("newPostsThisMonth", postRepository.countPostsCreatedAfter(oneMonthAgo));
        
        // Top categorie per numero di post
        List<Category> topCategories = categoryRepository.findAllOrderByPostCountDesc();
        if (topCategories.size() > 10) {
            topCategories = topCategories.subList(0, 10);
        }
        model.addAttribute("topCategories", topCategories);
        
        // Lab più completati
        List<Lab> topLabs = labRepository.findMostCompletedLabs(PageRequest.of(0, 10));
        model.addAttribute("topLabs", topLabs);
        
        return "stats";
    }

    /**
     * Error handler per 404
     */
    @GetMapping("/404")
    public String notFound(Model model) {
        model.addAttribute("pageTitle", "Page Not Found");
        return "error/404";
    }

    /**
     * Error handler per 403
     */
    @GetMapping("/403")
    public String accessDenied(Model model) {
        model.addAttribute("pageTitle", "Access Denied");
        return "error/403";
    }

    /**
     * Error handler per 500
     */
    @GetMapping("/500")
    public String serverError(Model model) {
        model.addAttribute("pageTitle", "Server Error");
        return "error/500";
    }
}