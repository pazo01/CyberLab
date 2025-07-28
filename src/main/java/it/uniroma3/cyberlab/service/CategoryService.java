package it.uniroma3.cyberlab.service;

import it.uniroma3.cyberlab.entity.Category;
import it.uniroma3.cyberlab.entity.Post;
import it.uniroma3.cyberlab.entity.Lab;
import it.uniroma3.cyberlab.entity.User;
import it.uniroma3.cyberlab.repository.CategoryRepository;
import it.uniroma3.cyberlab.repository.PostRepository;
import it.uniroma3.cyberlab.repository.LabRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private LabRepository labRepository;

    /**
     * Crea nuova categoria (solo admin)
     */
    public Category createCategory(String name, String description, String color, User admin) {
        if (!admin.isAdmin()) {
            throw new SecurityException("Only admins can create categories");
        }
        
        // Controlla se nome già esiste
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("Category name already exists");
        }
        
        Category category = new Category();
        category.setName(name.trim());
        category.setDescription(description != null ? description.trim() : null);
        category.setColor(color != null ? color.trim() : "#6c757d");
        
        return categoryRepository.save(category);
    }

    /**
     * Aggiorna categoria esistente (solo admin)
     */
    public Category updateCategory(Long categoryId, String name, String description, String color, User admin) {
        if (!admin.isAdmin()) {
            throw new SecurityException("Only admins can update categories");
        }
        
        Category category = findById(categoryId);
        
        // Controlla se nuovo nome già esiste (escludendo categoria corrente)
        if (!category.getName().equalsIgnoreCase(name) && 
            categoryRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("Category name already exists");
        }
        
        category.setName(name.trim());
        category.setDescription(description != null ? description.trim() : null);
        category.setColor(color != null ? color.trim() : "#6c757d");
        
        return categoryRepository.save(category);
    }

    /**
     * Trova categoria per ID
     */
    @Transactional(readOnly = true)
    public Category findById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    /**
     * Trova categoria per nome
     */
    @Transactional(readOnly = true)
    public Category findByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    /**
     * Trova tutte le categorie
     */
    @Transactional(readOnly = true)
    public List<Category> findAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    /**
     * Trova categorie con post
     */
    @Transactional(readOnly = true)
    public List<Category> findCategoriesWithPosts() {
        return categoryRepository.findCategoriesWithPosts();
    }

    /**
     * Trova categorie con lab
     */
    @Transactional(readOnly = true)
    public List<Category> findCategoriesWithLabs() {
        return categoryRepository.findCategoriesWithLabs();
    }

    /**
     * Trova categorie attive (con contenuti)
     */
    @Transactional(readOnly = true)
    public List<Category> findActiveCategories() {
        return categoryRepository.findActiveCategoriesWithContent();
    }

    /**
     * Elimina categoria (solo admin)
     */
    public void deleteCategory(Long categoryId, User admin) {
        if (!admin.isAdmin()) {
            throw new SecurityException("Only admins can delete categories");
        }
        
        Category category = findById(categoryId);
        
        // Controlla se categoria ha post o lab associati
        long postsCount = postRepository.countByCategory(category);
        long labsCount = labRepository.countByCategory(category);
        
        if (postsCount > 0 || labsCount > 0) {
            throw new RuntimeException("Cannot delete category with associated posts or labs. " +
                    "Found " + postsCount + " posts and " + labsCount + " labs.");
        }
        
        categoryRepository.delete(category);
    }

    /**
     * Sposta contenuti in un'altra categoria prima di eliminare
     */
    public void moveCategoryContent(Long fromCategoryId, Long toCategoryId, User admin) {
        if (!admin.isAdmin()) {
            throw new SecurityException("Only admins can move category content");
        }
        
        Category fromCategory = findById(fromCategoryId);
        Category toCategory = findById(toCategoryId);
        
        // Sposta post
        List<Post> posts = postRepository.findByCategory(fromCategory);
        for (Post post : posts) {
            post.setCategory(toCategory);
        }
        postRepository.saveAll(posts);
        
        // Sposta lab
        List<Lab> labs = labRepository.findByCategory(fromCategory);
        for (Lab lab : labs) {
            lab.setCategory(toCategory);
        }
        labRepository.saveAll(labs);
    }

    /**
     * Controlla se nome categoria è disponibile
     */
    @Transactional(readOnly = true)
    public boolean isCategoryNameAvailable(String name) {
        return !categoryRepository.existsByNameIgnoreCase(name);
    }

    /**
     * Controlla se nome categoria è disponibile (escludendo categoria corrente)
     */
    @Transactional(readOnly = true)
    public boolean isCategoryNameAvailable(String name, Long excludeCategoryId) {
        Category currentCategory = findById(excludeCategoryId);
        if (currentCategory.getName().equalsIgnoreCase(name)) {
            return true; // Stesso nome della categoria corrente
        }
        return !categoryRepository.existsByNameIgnoreCase(name);
    }

    /**
     * Statistiche categoria
     */
    @Transactional(readOnly = true)
    public CategoryStatistics getCategoryStatistics(Long categoryId) {
        Category category = findById(categoryId);
        
        long postsCount = postRepository.countByCategory(category);
        long labsCount = labRepository.countByCategory(category);
        long totalViews = postRepository.sumViewsByCategory(category) + 
                         labRepository.sumViewsByCategory(category);
        
        // Post più popolare nella categoria
        List<Post> popularPosts = postRepository.findByCategoryOrderByViewCountDesc(category);
        Post mostViewedPost = popularPosts.isEmpty() ? null : popularPosts.get(0);
        
        // Lab più popolare nella categoria
        List<Lab> popularLabs = labRepository.findByCategoryOrderByViewCountDesc(category);
        Lab mostViewedLab = popularLabs.isEmpty() ? null : popularLabs.get(0);
        
        return new CategoryStatistics(category, postsCount, labsCount, totalViews, 
                                    mostViewedPost, mostViewedLab);
    }

    /**
     * Ottieni categorie più popolari
     */
    @Transactional(readOnly = true)
    public List<Category> getMostPopularCategories(int limit) {
        return categoryRepository.findAllOrderByPostCountDesc()
                .stream().limit(limit).toList();
    }

    /**
     * Ricerca categorie
     */
    @Transactional(readOnly = true)
    public List<Category> searchCategories(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }
        return categoryRepository.searchByNameOrDescription(searchTerm.trim());
    }

    /**
     * Verifica se categoria può essere eliminata
     */
    @Transactional(readOnly = true)
    public boolean canDeleteCategory(Long categoryId) {
        Category category = findById(categoryId);
        long postsCount = postRepository.countByCategory(category);
        long labsCount = labRepository.countByCategory(category);
        return postsCount == 0 && labsCount == 0;
    }

    /**
     * Ottieni statistiche globali categorie
     */
    @Transactional(readOnly = true)
    public GlobalCategoryStatistics getGlobalStatistics() {
        long totalCategories = categoryRepository.count();
        long categoriesWithPosts = categoryRepository.findCategoriesWithPosts().size();
        long categoriesWithLabs = categoryRepository.findCategoriesWithLabs().size();
        long emptycategories = totalCategories - Math.max(categoriesWithPosts, categoriesWithLabs);
        
        return new GlobalCategoryStatistics(totalCategories, categoriesWithPosts, 
                                           categoriesWithLabs, emptycategories);
    }

    // =============================================================================
    // DTO CLASSES
    // =============================================================================

    /**
     * DTO per statistiche categoria
     */
    public static class CategoryStatistics {
        private final Category category;
        private final long postsCount;
        private final long labsCount;
        private final long totalViews;
        private final Post mostViewedPost;
        private final Lab mostViewedLab;
        
        public CategoryStatistics(Category category, long postsCount, long labsCount, 
                                long totalViews, Post mostViewedPost, Lab mostViewedLab) {
            this.category = category;
            this.postsCount = postsCount;
            this.labsCount = labsCount;
            this.totalViews = totalViews;
            this.mostViewedPost = mostViewedPost;
            this.mostViewedLab = mostViewedLab;
        }
        
        // Getters
        public Category getCategory() { return category; }
        public long getPostsCount() { return postsCount; }
        public long getLabsCount() { return labsCount; }
        public long getTotalViews() { return totalViews; }
        public Post getMostViewedPost() { return mostViewedPost; }
        public Lab getMostViewedLab() { return mostViewedLab; }
        public long getTotalContent() { return postsCount + labsCount; }
        
        public boolean isEmpty() { return getTotalContent() == 0; }
        public boolean hasContent() { return getTotalContent() > 0; }
    }

    /**
     * DTO per statistiche globali categorie
     */
    public static class GlobalCategoryStatistics {
        private final long totalCategories;
        private final long categoriesWithPosts;
        private final long categoriesWithLabs;
        private final long emptyCategories;
        
        public GlobalCategoryStatistics(long totalCategories, long categoriesWithPosts, 
                                      long categoriesWithLabs, long emptyCategories) {
            this.totalCategories = totalCategories;
            this.categoriesWithPosts = categoriesWithPosts;
            this.categoriesWithLabs = categoriesWithLabs;
            this.emptyCategories = emptyCategories;
        }
        
        // Getters
        public long getTotalCategories() { return totalCategories; }
        public long getCategoriesWithPosts() { return categoriesWithPosts; }
        public long getCategoriesWithLabs() { return categoriesWithLabs; }
        public long getEmptyCategories() { return emptyCategories; }
        
        public double getUtilizationRate() {
            return totalCategories > 0 ? 
                (double) (totalCategories - emptyCategories) / totalCategories * 100 : 0;
        }
    }
}