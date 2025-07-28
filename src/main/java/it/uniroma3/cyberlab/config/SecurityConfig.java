package it.uniroma3.cyberlab.config;

import it.uniroma3.cyberlab.security.CustomUserDetailsService;
import it.uniroma3.cyberlab.security.CustomAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider());

        http.authorizeHttpRequests(authz -> authz
                // Public pages - accessible to everyone
                .requestMatchers("/", "/home", "/about", "/contact").permitAll()
                .requestMatchers("/posts", "/posts/**").permitAll()
                .requestMatchers("/labs", "/labs/**").permitAll()
                
                // Authentication pages
                .requestMatchers("/login", "/register", "/forgot-password").permitAll()
                
                // Static resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                
                // Public lab theory (read-only)
                .requestMatchers("/labs/theory/**", "/labs/categories").permitAll()
                
                // Public posts (read-only)
                .requestMatchers("/posts/view/**", "/posts/category/**", "/posts/search").permitAll()
                
                // User area - requires authentication
                .requestMatchers("/profile/**", "/dashboard", "/posts/create", "/posts/edit/**").hasRole("USER")
                .requestMatchers("/comments/create", "/comments/edit/**", "/reports/create").hasRole("USER")
                .requestMatchers("/labs/practice/**", "/labs/progress/**").hasRole("USER")
                
                // Admin area - requires admin role
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/labs/create", "/labs/edit/**", "/labs/delete/**").hasRole("ADMIN")
                .requestMatchers("/users/manage/**", "/reports/manage/**").hasRole("ADMIN")
                .requestMatchers("/posts/delete/**", "/comments/delete/**").hasRole("ADMIN")
                
                // API endpoints
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/user/**").hasRole("USER")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Lab sandbox endpoints (special handling)
                .requestMatchers("/lab-sandbox/**").hasRole("USER")
                
                // All other requests require authentication
                .anyRequest().authenticated()
        );

        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/authenticate")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(customAuthenticationSuccessHandler())
                .failureUrl("/login?error")
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/home?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        );

        http.rememberMe(remember -> remember
                .key("CyberLabRememberMeKey")
                .userDetailsService(userDetailsService)
                .tokenValiditySeconds(86400 * 7) // 7 days
        );

        // CSRF protection (enabled for forms, disabled for API)
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/lab-sandbox/**")
        );

        // Session management
        http.sessionManagement(session -> session
                .maximumSessions(3)
                .maxSessionsPreventsLogin(false)
        );

        // Security headers
        http.headers(headers -> headers
                .frameOptions().sameOrigin() // Allow frames for lab sandboxes
                .contentTypeOptions().and()
                .httpStrictTransportSecurity().and()
        );

        return http.build();
    }
}