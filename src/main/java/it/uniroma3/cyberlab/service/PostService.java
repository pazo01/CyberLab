package it.uniroma3.cyberlab.service;

import it.uniroma3.cyberlab.entity.Post;
import it.uniroma3.cyberlab.entity.User;
import it.uniroma3.cyberlab.entity.Category;
import it.uniroma3.cyberlab.entity.Comment;
import it.uniroma3.cyberlab.repository.PostRepository;
import it.uniroma3.cyberlab.repository.CommentRepository;
import it.uniroma3.cyberlab.repository.CategoryRepository;
import it.uniroma3.cyberlab.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class PostService {

    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Crea nuovo post
     */
    public Post createPost(String title, String content, Post.PostType postType, 
                          String tags, Long categoryId, User author) {
        
        System.out.println("=== PostService.createPost DEBUG ===");
        System.out.println("Title: " + title);
        System.out.println("Content length: " + (content != null ? content.length() : 0));
        System.out.println("PostType: " + postType);
        System.out.println("CategoryId: " + categoryId);
        System.out.println("Author: " + (author != null ? author.getUsername() : "null"));
        
        // Validazione
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        
        if (content == null || content.trim().length() < 10) {
            throw new IllegalArgumentException("Content must be at least 10 characters");
        }
        
        if (categoryId == null) {
            throw new IllegalArgumentException("Category is required");
        }
        
        if (author == null) {
            throw new IllegalArgumentException("Author is required");
        }
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
        
        System.out.println("Category found: " + category.getName());
        
        // Crea post
        Post post = new Post();
        post.setTitle(title.trim());
        post.setContent(content.trim());
        post.setPostType(postType != null ? postType : Post.PostType.GENERAL);
        post.setTags(tags != null ? tags.trim() : null);
        post.setCategory(category);
        post.setAuthor(author);
        post.setCreatedDate(LocalDateTime.now());
        post.setLastModified(LocalDateTime.now());
        post.setViewCount(0L);
        post.setLikeCount(0L);
        
        Post savedPost = postRepository.save(post);
        System.out.println("Post saved successfully with ID: " + savedPost.getId());
        
        return savedPost;
    }

    /**
     * Aggiorna post esistente
     */
    public Post updatePost(Long postId, String title, String content, 
                          Post.PostType postType, String tags, Long categoryId, User user) {
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        // Verifica permessi
        if (!canEditPost(post, user)) {
            throw new SecurityException("You don't have permission to edit this post");
        }
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        // Aggiorna campi
        post.setTitle(title.trim());
        post.setContent(content.trim());
        post.setPostType(postType != null ? postType : post.getPostType());
        post.setTags(tags != null ? tags.trim() : null);
        post.setCategory(category);
        post.setLastModified(LocalDateTime.now());
        
        return postRepository.save(post);
    }

    /**
     * Elimina post e restituisce titolo
     */
    public String deletePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        if (!canDeletePost(post, user)) {
            throw new SecurityException("You don't have permission to delete this post");
        }
        
        String postTitle = post.getTitle();
        postRepository.delete(post);
        return postTitle;
    }

    /**
     * Trova post per ID
     */
    @Transactional(readOnly = true)
    public Post findById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    /**
     * Trova post per ID con view increment
     */
    @Transactional
    public Post findByIdAndIncrementViews(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        post.incrementViewCount();
        postRepository.save(post);
        return post;
    }

    /**
     * Trova post con filtri
     */
    @Transactional(readOnly = true)
    public List<Post> findPostsWithFilters(Category category, Post.PostType type, int page, int size) {
        List<Post> posts = new ArrayList<>();
        
        try {
            if (category != null && type != null) {
                posts = postRepository.findByCategoryAndPostType(category, type);
            } else if (category != null) {
                posts = postRepository.findByCategoryOrderByCreatedDateDesc(category);
            } else if (type != null) {
                posts = postRepository.findByPostType(type);
            } else {
                // Se non ci sono filtri, prendi tutti i post recenti
                posts = postRepository.findAll();
                posts.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
            }
            
            System.out.println("PostService.findPostsWithFilters - Found " + posts.size() + " posts");
            
            // Paginazione manuale
            int start = page * size;
            int end = Math.min(start + size, posts.size());
            
            if (start < posts.size()) {
                return posts.subList(start, end);
            } else {
                return new ArrayList<>();
            }
            
        } catch (Exception e) {
            System.out.println("Error in findPostsWithFilters: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Trova post correlati
     */
    @Transactional(readOnly = true)
    public List<Post> findRelatedPosts(Post post, int limit) {
        try {
            List<Post> relatedPosts = postRepository.findByCategoryOrderByCreatedDateDesc(post.getCategory());
            return relatedPosts.stream()
                    .filter(p -> !p.getId().equals(post.getId()))
                    .limit(limit)
                    .toList();
        } catch (Exception e) {
            System.out.println("Error finding related posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Trova post per categoria con paginazione
     */
    @Transactional(readOnly = true)
    public List<Post> findPostsByCategory(Category category, int page, int size) {
        try {
            List<Post> posts = postRepository.findByCategoryOrderByCreatedDateDesc(category);
            
            int start = page * size;
            int end = Math.min(start + size, posts.size());
            
            if (start < posts.size()) {
                return posts.subList(start, end);
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println("Error finding posts by category: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Conta post per categoria
     */
    @Transactional(readOnly = true)
    public long countPostsByCategory(Category category) {
        try {
            return postRepository.countByCategory(category);
        } catch (Exception e) {
            System.out.println("Error counting posts by category: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Ricerca post con categoria opzionale
     */
    @Transactional(readOnly = true)
    public List<Post> searchPosts(String searchTerm, Category category) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            if (category != null) {
                return postRepository.searchPostsInCategory(searchTerm.trim(), category);
            } else {
                return postRepository.searchPosts(searchTerm.trim());
            }
        } catch (Exception e) {
            System.out.println("Error searching posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Post più visti
     */
    @Transactional(readOnly = true)
    public List<Post> getMostViewedPosts(int limit) {
        try {
            PageRequest pageRequest = PageRequest.of(0, limit);
            return postRepository.findMostViewedPosts(pageRequest);
        } catch (Exception e) {
            System.out.println("Error getting most viewed posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Post recenti
     */
    @Transactional(readOnly = true)
    public List<Post> getRecentPosts(int limit) {
        try {
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            List<Post> posts = postRepository.findRecentPosts(oneMonthAgo);
            return posts.stream().limit(limit).toList();
        } catch (Exception e) {
            System.out.println("Error getting recent posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Post dell'utente
     */
    @Transactional(readOnly = true)
    public List<Post> findUserPosts(User user, int limit) {
        try {
            List<Post> posts = postRepository.findByAuthorOrderByCreatedDateDesc(user);
            if (limit > 0) {
                return posts.stream().limit(limit).toList();
            }
            return posts;
        } catch (Exception e) {
            System.out.println("Error finding user posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Conteggio totale post
     */
    @Transactional(readOnly = true)
    public long getTotalPostsCount() {
        try {
            return postRepository.count();
        } catch (Exception e) {
            System.out.println("Error getting total posts count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Trova post per categoria con paginazione Spring
     */
    @Transactional(readOnly = true)
    public List<Post> findByCategory(Category category, int page, int size) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size);
            return postRepository.findByCategoryOrderByCreatedDateDesc(category, pageRequest).getContent();
        } catch (Exception e) {
            System.out.println("Error finding posts by category with pagination: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Trova post recenti
     */
    @Transactional(readOnly = true)
    public List<Post> findRecentPosts(int days, int limit) {
        try {
            LocalDateTime dateFrom = LocalDateTime.now().minusDays(days);
            List<Post> posts = postRepository.findRecentPosts(dateFrom);
            return posts.stream().limit(limit).toList();
        } catch (Exception e) {
            System.out.println("Error finding recent posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Trova post più popolari
     */
    @Transactional(readOnly = true)
    public List<Post> findMostPopularPosts(int limit) {
        try {
            PageRequest pageRequest = PageRequest.of(0, limit);
            return postRepository.findMostViewedPosts(pageRequest);
        } catch (Exception e) {
            System.out.println("Error finding most popular posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Trova post featured/pinned
     */
    @Transactional(readOnly = true)
    public List<Post> findFeaturedPosts(int limit) {
        try {
            List<Post> featured = postRepository.findFeaturedAndPinnedPosts();
            return featured.stream().limit(limit).toList();
        } catch (Exception e) {
            System.out.println("Error finding featured posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Ricerca post in categoria specifica
     */
    @Transactional(readOnly = true)
    public List<Post> searchPostsInCategory(String searchTerm, Long categoryId) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category == null) {
                return new ArrayList<>();
            }
            
            return postRepository.searchPostsInCategory(searchTerm.trim(), category);
        } catch (Exception e) {
            System.out.println("Error searching posts in category: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Toggle like su post
     */
    public PostLikeResult toggleLike(Long postId, User user) {
        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("Post not found"));
            
            // Implementazione semplificata - in un sistema reale useresti una tabella separata
            post.incrementLikeCount();
            postRepository.save(post);
            
            return new PostLikeResult(true, post.getLikeCount());
        } catch (Exception e) {
            System.out.println("Error toggling like: " + e.getMessage());
            return new PostLikeResult(false, 0);
        }
    }

    /**
     * Pin/Unpin post (solo admin)
     */
    public Post togglePin(Long postId, User admin) {
        if (!admin.isAdmin()) {
            throw new SecurityException("Only admins can pin posts");
        }
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        post.setIsPinned(!post.getIsPinned());
        return postRepository.save(post);
    }

    /**
     * Feature/Unfeature post (solo admin)
     */
    public Post toggleFeature(Long postId, User admin) {
        if (!admin.isAdmin()) {
            throw new SecurityException("Only admins can feature posts");
        }
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        post.setIsFeatured(!post.getIsFeatured());
        return postRepository.save(post);
    }

    /**
     * Ottieni statistiche post
     */
    @Transactional(readOnly = true)
    public PostDetailStatistics getPostStatistics(Post post) {
        try {
            PostDetailStatistics stats = new PostDetailStatistics();
            
            stats.setViewCount(post.getViewCount());
            stats.setLikeCount(post.getLikeCount());
            stats.setCommentCount(commentRepository.countByPost(post));
            
            // Commenti recenti (ultima settimana)
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            List<Comment> recentComments = commentRepository.findByPostOrderByCreatedDateDesc(post)
                    .stream()
                    .filter(c -> c.getCreatedDate().isAfter(oneWeekAgo))
                    .toList();
            stats.setRecentCommentCount(recentComments.size());
            
            return stats;
        } catch (Exception e) {
            System.out.println("Error getting post statistics: " + e.getMessage());
            return new PostDetailStatistics();
        }
    }

    /**
     * Statistiche post generali
     */
    @Transactional(readOnly = true)
    public PostStatistics getPostStatistics() {
        try {
            long totalPosts = postRepository.count();
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            long newPostsThisMonth = postRepository.countPostsCreatedAfter(oneMonthAgo);
            
            return new PostStatistics(totalPosts, newPostsThisMonth);
        } catch (Exception e) {
            System.out.println("Error getting post statistics: " + e.getMessage());
            return new PostStatistics(0, 0);
        }
    }

    /**
     * Dati analytics
     */
    @Transactional(readOnly = true)
    public AnalyticsData getAnalyticsData() {
        try {
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
            
            long postsThisWeek = postRepository.countPostsCreatedAfter(oneWeekAgo);
            long postsThisMonth = postRepository.countPostsCreatedAfter(oneMonthAgo);
            long postsThreeMonths = postRepository.countPostsCreatedAfter(threeMonthsAgo);
            
            return new AnalyticsData(postsThisWeek, postsThisMonth, postsThreeMonths);
        } catch (Exception e) {
            System.out.println("Error getting analytics data: " + e.getMessage());
            return new AnalyticsData(0, 0, 0);
        }
    }

    /**
     * Ottieni statistiche globali sui post
     */
    @Transactional(readOnly = true)
    public GlobalPostStatistics getGlobalStatistics() {
        try {
            GlobalPostStatistics stats = new GlobalPostStatistics();
            
            stats.setTotalPosts(postRepository.count());
            
            // Post per tipo
            for (Post.PostType type : Post.PostType.values()) {
                long count = postRepository.findByPostType(type).size();
                stats.getPostsByType().put(type, count);
            }
            
            // Post dell'ultimo mese
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            stats.setPostsThisMonth(postRepository.countPostsCreatedAfter(oneMonthAgo));
            
            return stats;
        } catch (Exception e) {
            System.out.println("Error getting global statistics: " + e.getMessage());
            return new GlobalPostStatistics();
        }
    }

    /**
     * Verifica se l'utente può modificare il post
     */
    private boolean canEditPost(Post post, User user) {
        return user != null && (user.isAdmin() || post.isOwnedBy(user));
    }

    /**
     * Verifica se l'utente può eliminare il post
     */
    private boolean canDeletePost(Post post, User user) {
        return user != null && (user.isAdmin() || post.isOwnedBy(user));
    }

    // =============================================================================
    // DTO CLASSES
    // =============================================================================

    /**
     * DTO per risultato like
     */
    public static class PostLikeResult {
        private boolean liked;
        private long totalLikes;

        public PostLikeResult(boolean liked, long totalLikes) {
            this.liked = liked;
            this.totalLikes = totalLikes;
        }

        public boolean isLiked() { return liked; }
        public long getTotalLikes() { return totalLikes; }
    }

    /**
     * DTO per statistiche post singolo
     */
    public static class PostDetailStatistics {
        private long viewCount;
        private long likeCount;
        private long commentCount;
        private long recentCommentCount;

        public long getViewCount() { return viewCount; }
        public void setViewCount(long viewCount) { this.viewCount = viewCount; }
        
        public long getLikeCount() { return likeCount; }
        public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
        
        public long getCommentCount() { return commentCount; }
        public void setCommentCount(long commentCount) { this.commentCount = commentCount; }
        
        public long getRecentCommentCount() { return recentCommentCount; }
        public void setRecentCommentCount(long recentCommentCount) { this.recentCommentCount = recentCommentCount; }
    }

    /**
     * DTO per statistiche post generali
     */
    public static class PostStatistics {
        private final long totalPosts;
        private final long newPostsThisMonth;
        
        public PostStatistics(long totalPosts, long newPostsThisMonth) {
            this.totalPosts = totalPosts;
            this.newPostsThisMonth = newPostsThisMonth;
        }
        
        public long getTotalPosts() { return totalPosts; }
        public long getNewPostsThisMonth() { return newPostsThisMonth; }
    }

    /**
     * DTO per analytics
     */
    public static class AnalyticsData {
        private final long postsThisWeek;
        private final long postsThisMonth;
        private final long postsThreeMonths;
        
        public AnalyticsData(long postsThisWeek, long postsThisMonth, long postsThreeMonths) {
            this.postsThisWeek = postsThisWeek;
            this.postsThisMonth = postsThisMonth;
            this.postsThreeMonths = postsThreeMonths;
        }
        
        public long getPostsThisWeek() { return postsThisWeek; }
        public long getPostsThisMonth() { return postsThisMonth; }
        public long getPostsThreeMonths() { return postsThreeMonths; }
    }

    /**
     * DTO per statistiche globali
     */
    public static class GlobalPostStatistics {
        private long totalPosts;
        private long postsThisMonth;
        private Map<Post.PostType, Long> postsByType = new HashMap<>();

        public long getTotalPosts() { return totalPosts; }
        public void setTotalPosts(long totalPosts) { this.totalPosts = totalPosts; }
        
        public long getPostsThisMonth() { return postsThisMonth; }
        public void setPostsThisMonth(long postsThisMonth) { this.postsThisMonth = postsThisMonth; }
        
        public Map<Post.PostType, Long> getPostsByType() { return postsByType; }
        public void setPostsByType(Map<Post.PostType, Long> postsByType) { this.postsByType = postsByType; }
    }
}