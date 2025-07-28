package it.uniroma3.cyberlab.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Set;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        
        String redirectUrl = "/dashboard"; // Default redirect
        
        // Redirect based on role
        if (roles.contains("ROLE_ADMIN")) {
            redirectUrl = "/admin/dashboard";
        } else if (roles.contains("ROLE_USER")) {
            redirectUrl = "/dashboard";
        }
        
        // Check if there was a saved request (e.g., user tried to access protected page)
        String targetUrl = request.getParameter("redirect");
        if (targetUrl != null && !targetUrl.isEmpty()) {
            redirectUrl = targetUrl;
        }
        
        response.sendRedirect(redirectUrl);
    }
}