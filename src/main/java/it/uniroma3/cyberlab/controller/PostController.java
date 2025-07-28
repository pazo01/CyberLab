package it.uniroma3.cyberlab.controller;

import it.uniroma3.cyberlab.entity.*;
import it.uniroma3.cyberlab.service.PostService;
import it.uniroma3.cyberlab.service.CommentService;
import it.uniroma3.cyberlab.repository.CategoryRepository;
import it.uniroma3.cyberlab.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Lista tutti i post (pubblica)
     */
    @GetMapping
    public String listPosts(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(required = false) Long categoryId,
                           @RequestParam(required = false) String type,
                           Model model) {
        
        model.addAttribute("pageTitle", "All Posts");
        
        // Filtri
        Category selectedCategory = null;
        if (categoryId != null) {
            selectedCategory = categoryRepository.findById(categoryId).orElse(null);
        }
        
        Post.PostType selectedType = null;
        if (type != null && !type.isEmpty()) {
            try {
                selectedType = Post.PostType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid type
            }
        }
        
        // Usa PostService per ottenere post filtrati
        List<Post> posts = postService.findPostsWithFilters(selectedCategory, selectedType, page, 15);
        
        // DEBUG: Log posts trovati
        System.out.println("=== LIST POSTS DEBUG ===");
        System.out.println("Posts found: " + posts.size());
        posts.forEach(p -> System.out.println("- " + p.getTitle() + " by " + p.getAuthor().getUsername()));
        
        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", page);
        model.addAttribute("hasNext", posts.size() == 15);
        model.addAttribute("hasPrevious", page > 0);
        
        // Dati per filtri
        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
        model.addAttribute("categories", categories);
        model.addAttribute("postTypes", Post.PostType.values());
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("selectedType", selectedType);
        
        // Statistiche per sidebar
        model.addAttribute("totalPosts", postService.getTotalPostsCount());
        
        return "posts/list";
    }

    /**
     * Visualizza singolo post - FIXED
     */
    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        
        try {
            // Usa PostService per trovare e incrementare views
            Post post = postService.findByIdAndIncrementViews(id);
            
            model.addAttribute("post", post);
            model.addAttribute("pageTitle", post.getTitle());
            
            // Commenti del post usando CommentService - FIXED
            List<Comment> comments = commentService.findTopLevelCommentsByPost(post);
            model.addAttribute("comments", comments);
            
            // Nuovo commento (se utente autenticato)
            if (SecurityUtils.isAuthenticated()) {
                model.addAttribute("newComment", new Comment());
            }
            
            // Post correlati usando PostService
            List<Post> relatedPosts = postService.findRelatedPosts(post, 5);
            model.addAttribute("relatedPosts", relatedPosts);
            
            // Check permessi per edit/delete
            User currentUser = SecurityUtils.getCurrentUser();
            model.addAttribute("canEdit", currentUser != null && 
                    (SecurityUtils.isAdmin() || post.isOwnedBy(currentUser)));
            
            return "posts/view";
            
        } catch (Exception e) {
            System.out.println("ERROR viewing post " + id + ": " + e.getMessage());
            return "redirect:/posts?error=post_not_found";
        }
    }

    /**
     * Form creazione nuovo post
     */
    @GetMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public String createPostForm(Model model) {
        
        // DEBUG: Verifica categorie disponibili
        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
        System.out.println("=== CREATE FORM DEBUG ===");
        System.out.println("Available categories: " + categories.size());
        categories.forEach(cat -> System.out.println("- " + cat.getName() + " (ID: " + cat.getId() + ")"));
        
        model.addAttribute("pageTitle", "Create New Post");
        model.addAttribute("post", new PostCreateRequest());
        model.addAttribute("categories", categories);
        model.addAttribute("postTypes", Post.PostType.values());
        
        return "posts/create";
    }

    /**
     * Elaborazione creazione post - FIXED
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public String createPost(@Valid @ModelAttribute("post") PostCreateRequest request,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        
        // DEBUG: Log dei dati ricevuti
        System.out.println("=== POST CREATE DEBUG ===");
        System.out.println("Title: " + request.getTitle());
        System.out.println("Content: " + (request.getContent() != null ? request.getContent().substring(0, Math.min(50, request.getContent().length())) + "..." : "null"));
        System.out.println("PostType: " + request.getPostType());
        System.out.println("CategoryId: " + request.getCategoryId());
        System.out.println("Tags: " + request.getTags());
        System.out.println("BindingResult errors: " + bindingResult.hasErrors());
        
        if (bindingResult.hasErrors()) {
            System.out.println("=== VALIDATION ERRORS ===");
            bindingResult.getAllErrors().forEach(error -> 
                System.out.println("Error: " + error.getDefaultMessage())
            );
            
            model.addAttribute("pageTitle", "Create New Post");
            List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
            model.addAttribute("categories", categories);
            model.addAttribute("postTypes", Post.PostType.values());
            return "posts/create";
        }
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            System.out.println("ERROR: No current user found!");
            return "redirect:/login";
        }
        
        System.out.println("Current user: " + currentUser.getUsername());
        
        try {
            // Usa PostService per creare il post
            Post savedPost = postService.createPost(
                request.getTitle(),
                request.getContent(),
                request.getPostType(),
                request.getTags(),
                request.getCategoryId(),
                currentUser
            );
            
            System.out.println("SUCCESS: Post created with ID: " + savedPost.getId());
            redirectAttributes.addFlashAttribute("success", "Post created successfully!");
            return "redirect:/posts/" + savedPost.getId();
            
        } catch (Exception e) {
            System.out.println("ERROR creating post: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("error", "Error creating post: " + e.getMessage());
            model.addAttribute("pageTitle", "Create New Post");
            List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
            model.addAttribute("categories", categories);
            model.addAttribute("postTypes", Post.PostType.values());
            return "posts/create";
        }
    }

    /**
     * Form modifica post
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('USER')")
    public String editPostForm(@PathVariable Long id, Model model) {
        
        try {
            Post post = postService.findById(id);
            
            User currentUser = SecurityUtils.getCurrentUser();
            if (currentUser == null || (!SecurityUtils.isAdmin() && !post.isOwnedBy(currentUser))) {
                return "redirect:/posts/" + id + "?error=access_denied";
            }
            
            model.addAttribute("pageTitle", "Edit Post");
            
            PostCreateRequest editRequest = new PostCreateRequest();
            editRequest.setTitle(post.getTitle());
            editRequest.setContent(post.getContent());
            editRequest.setPostType(post.getPostType());
            editRequest.setTags(post.getTags());
            editRequest.setCategoryId(post.getCategory().getId());
            
            model.addAttribute("post", editRequest);
            model.addAttribute("postId", id);
            
            List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
            model.addAttribute("categories", categories);
            model.addAttribute("postTypes", Post.PostType.values());
            
            return "posts/edit";
            
        } catch (Exception e) {
            System.out.println("ERROR editing post " + id + ": " + e.getMessage());
            return "redirect:/posts?error=post_not_found";
        }
    }

    /**
     * Elaborazione modifica post
     */
    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('USER')")
    public String editPost(@PathVariable Long id,
                          @Valid @ModelAttribute("post") PostCreateRequest request,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Post");
            model.addAttribute("postId", id);
            List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
            model.addAttribute("categories", categories);
            model.addAttribute("postTypes", Post.PostType.values());
            return "posts/edit";
        }
        
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            
            // Usa PostService per aggiornare il post
            Post updatedPost = postService.updatePost(
                id,
                request.getTitle(),
                request.getContent(),
                request.getPostType(),
                request.getTags(),
                request.getCategoryId(),
                currentUser
            );
            
            redirectAttributes.addFlashAttribute("success", "Post updated successfully!");
            return "redirect:/posts/" + id;
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pageTitle", "Edit Post");
            model.addAttribute("postId", id);
            List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
            model.addAttribute("categories", categories);
            model.addAttribute("postTypes", Post.PostType.values());
            return "posts/edit";
        }
    }

    /**
     * Cancellazione post
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('USER')")
    public String deletePost(@PathVariable Long id, 
                            RedirectAttributes redirectAttributes) {
        
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            String postTitle = postService.deletePost(id, currentUser);
            
            redirectAttributes.addFlashAttribute("success", 
                    "Post '" + postTitle + "' deleted successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/posts";
    }

    /**
     * Ricerca post
     */
    @GetMapping("/search")
    public String searchPosts(@RequestParam(required = false) String q,
                             @RequestParam(required = false) Long categoryId,
                             Model model) {
        
        model.addAttribute("pageTitle", "Search Posts");
        model.addAttribute("query", q);
        
        List<Post> searchResults = List.of();
        
        if (q != null && !q.trim().isEmpty()) {
            String searchTerm = q.trim();
            Category category = null;
            
            if (categoryId != null) {
                category = categoryRepository.findById(categoryId).orElse(null);
                model.addAttribute("selectedCategory", category);
            }
            
            // Usa PostService per la ricerca
            searchResults = postService.searchPosts(searchTerm, category);
        }
        
        model.addAttribute("posts", searchResults);
        model.addAttribute("resultCount", searchResults.size());
        
        // Categorie per filtro
        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
        model.addAttribute("categories", categories);
        
        return "posts/search";
    }

    /**
     * Post per categoria
     */
    @GetMapping("/category/{categoryId}")
    public String postsByCategory(@PathVariable Long categoryId,
                                  @RequestParam(defaultValue = "0") int page,
                                  Model model) {
        
        try {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            
            model.addAttribute("category", category);
            model.addAttribute("pageTitle", "Posts in " + category.getName());
            
            // Usa PostService per ottenere post per categoria
            List<Post> posts = postService.findPostsByCategory(category, page, 15);
            
            model.addAttribute("posts", posts);
            model.addAttribute("currentPage", page);
            model.addAttribute("hasNext", posts.size() == 15);
            model.addAttribute("hasPrevious", page > 0);
            
            // Statistiche categoria usando PostService
            model.addAttribute("totalPosts", postService.countPostsByCategory(category));
            
            return "posts/category";
            
        } catch (Exception e) {
            return "redirect:/posts?error=category_not_found";
        }
    }

    /**
     * DTO per creazione/modifica post - FIXED WITH VALIDATION
     */
    public static class PostCreateRequest {
        
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        private String title;
        
        @NotBlank(message = "Content is required")
        @Size(min = 10, message = "Content must be at least 10 characters")
        private String content;
        
        private Post.PostType postType = Post.PostType.GENERAL;
        
        @Size(max = 500, message = "Tags must not exceed 500 characters")
        private String tags;
        
        @NotNull(message = "Category is required")
        private Long categoryId;
        
        // Constructors
        public PostCreateRequest() {}
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public Post.PostType getPostType() { return postType; }
        public void setPostType(Post.PostType postType) { this.postType = postType; }
        
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
        
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        
        @Override
        public String toString() {
            return "PostCreateRequest{" +
                    "title='" + title + '\'' +
                    ", postType=" + postType +
                    ", categoryId=" + categoryId +
                    '}';
        }
    }
}