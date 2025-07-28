package it.uniroma3.cyberlab.security;

import it.uniroma3.cyberlab.entity.User;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /**
     * Get the currently authenticated user
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || 
            !authentication.isAuthenticated() || 
            authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) principal).getUser();
        }
        
        return null;
    }

    /**
     * Get the current user's ID
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Get the current username
     */
    public static String getCurrentUsername() {
        User user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * Check if current user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !(authentication instanceof AnonymousAuthenticationToken);
    }

    /**
     * Check if current user is admin
     */
    public static boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && user.getRole() == User.Role.ADMIN;
    }

    /**
     * Check if current user is regular user
     */
    public static boolean isUser() {
        User user = getCurrentUser();
        return user != null && user.getRole() == User.Role.USER;
    }

    /**
     * Check if current user owns the given entity
     */
    public static boolean isOwner(User owner) {
        User currentUser = getCurrentUser();
        return currentUser != null && owner != null && 
               currentUser.getId().equals(owner.getId());
    }

    /**
     * Check if current user can edit content (is owner or admin)
     */
    public static boolean canEdit(User owner) {
        return isAdmin() || isOwner(owner);
    }

    /**
     * Check if current user can delete content (is owner or admin)
     */
    public static boolean canDelete(User owner) {
        return isAdmin() || isOwner(owner);
    }
}