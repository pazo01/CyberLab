package it.uniroma3.cyberlab.repository;

import it.uniroma3.cyberlab.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Basic queries
    Optional<Category> findByName(String name);
    Optional<Category> findByNameIgnoreCase(String name);
    boolean existsByName(String name);
    boolean existsByNameIgnoreCase(String name);
    
    // Ordered queries
    List<Category> findAllByOrderByNameAsc();
    
    @Query("SELECT c FROM Category c ORDER BY SIZE(c.posts) DESC")
    List<Category> findAllOrderByPostCountDesc();
    
    @Query("SELECT c FROM Category c ORDER BY SIZE(c.labs) DESC")
    List<Category> findAllOrderByLabCountDesc();
    
    // Search - AGGIUNTO
    @Query("SELECT c FROM Category c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Category> searchByNameOrDescription(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Category> searchCategories(@Param("search") String search);
    
    // Categories with content
    @Query("SELECT DISTINCT c FROM Category c WHERE SIZE(c.posts) > 0")
    List<Category> findCategoriesWithPosts();
    
    @Query("SELECT DISTINCT c FROM Category c WHERE SIZE(c.labs) > 0")
    List<Category> findCategoriesWithLabs();
    
    // AGGIUNTO - Categorie attive con contenuto
    @Query("SELECT DISTINCT c FROM Category c WHERE " +
           "EXISTS (SELECT 1 FROM Post p WHERE p.category = c) OR " +
           "EXISTS (SELECT 1 FROM Lab l WHERE l.category = c)")
    List<Category> findActiveCategoriesWithContent();
}