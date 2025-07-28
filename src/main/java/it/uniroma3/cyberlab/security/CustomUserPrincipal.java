package it.uniroma3.cyberlab.security;

import it.uniroma3.cyberlab.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomUserPrincipal implements UserDetails {

    private User user;

    public CustomUserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add role-based authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        
        // Add additional permissions based on role
        if (user.getRole() == User.Role.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("ADMIN_READ"));
            authorities.add(new SimpleGrantedAuthority("ADMIN_WRITE"));
            authorities.add(new SimpleGrantedAuthority("USER_MANAGE"));
            authorities.add(new SimpleGrantedAuthority("CONTENT_MODERATE"));
        }
        
        if (user.getRole() == User.Role.USER) {
            authorities.add(new SimpleGrantedAuthority("USER_READ"));
            authorities.add(new SimpleGrantedAuthority("USER_WRITE"));
            authorities.add(new SimpleGrantedAuthority("CONTENT_CREATE"));
        }
        
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != User.UserStatus.BANNED && 
               user.getStatus() != User.UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == User.UserStatus.ACTIVE;
    }

    // Additional helper methods
    public User getUser() {
        return user;
    }

 // in CustomUserPrincipal.java

    public Long getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getFullName() {
        return user.getFullName();
    }

    public boolean isAdmin() {
        return user.getRole() == User.Role.ADMIN;
    }

    public boolean isUser() {
        return user.getRole() == User.Role.USER;
    }
 }