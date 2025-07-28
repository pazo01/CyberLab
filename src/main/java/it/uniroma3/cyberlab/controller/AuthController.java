package it.uniroma3.cyberlab.controller;

import it.uniroma3.cyberlab.entity.User;
import it.uniroma3.cyberlab.service.UserService;
import it.uniroma3.cyberlab.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * Pagina di login - URL: /login
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           @RequestParam(value = "redirect", required = false) String redirect,
                           Model model) {
        
        // Se utente già autenticato, redirect alla dashboard
        if (SecurityUtils.isAuthenticated()) {
            return SecurityUtils.isAdmin() ? "redirect:/admin/dashboard" : "redirect:/dashboard";
        }
        
        model.addAttribute("pageTitle", "Login");
        
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        
        if (redirect != null) {
            model.addAttribute("redirect", redirect);
        }
        
        return "auth/login";
    }

    /**
     * Pagina di registrazione - URL: /register
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        
        // Se utente già autenticato, redirect alla dashboard
        if (SecurityUtils.isAuthenticated()) {
            return SecurityUtils.isAdmin() ? "redirect:/admin/dashboard" : "redirect:/dashboard";
        }
        
        model.addAttribute("pageTitle", "Register");
        model.addAttribute("user", new RegisterRequest());
        
        return "auth/register";
    }

    /**
     * Elaborazione registrazione - URL: /register
     */
    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("user") RegisterRequest request,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        
        model.addAttribute("pageTitle", "Register");
        
        // Validazione custom usando UserService
        if (!userService.isUsernameAvailable(request.getUsername())) {
            bindingResult.rejectValue("username", "username.exists", "Username already exists!");
        }
        
        if (!userService.isEmailAvailable(request.getEmail())) {
            bindingResult.rejectValue("email", "email.exists", "Email already exists!");
        }
        
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match!");
        }
        
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        
        try {
            // Crea nuovo utente tramite service
            User newUser = userService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getName(),
                request.getSurname()
            );
            
            redirectAttributes.addFlashAttribute("success", 
                "Registration successful! You can now login with your credentials.");
            
            return "redirect:/login";
            
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed. Please try again.");
            return "auth/register";
        }
    }

    /**
     * Pagina forgot password - URL: /forgot-password
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        model.addAttribute("pageTitle", "Forgot Password");
        return "auth/forgot-password";
    }

    /**
     * Elaborazione forgot password
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email,
                                       RedirectAttributes redirectAttributes) {
        
        // Usa UserService per verificare email
        if (!userService.isEmailAvailable(email)) {
            // Email esiste - in un sistema reale invieresti email di reset
            redirectAttributes.addFlashAttribute("message", 
                "If the email exists, you will receive password reset instructions.");
        } else {
            // Email non esiste - stessa risposta per sicurezza
            redirectAttributes.addFlashAttribute("message", 
                "If the email exists, you will receive password reset instructions.");
        }
        
        return "redirect:/forgot-password";
    }

    /**
     * Logout personalizzato (se necessario) - URL: /logout
     */
    @GetMapping("/logout")
    public String logout() {
        // Spring Security gestisce automaticamente il logout
        return "redirect:/login?logout";
    }

    /**
     * Verifica disponibilità username (AJAX) - URL: /check-username
     */
    @GetMapping("/check-username")
    @ResponseBody
    public boolean checkUsername(@RequestParam String username) {
        return userService.isUsernameAvailable(username);
    }

    /**
     * Verifica disponibilità email (AJAX) - URL: /check-email
     */
    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return userService.isEmailAvailable(email);
    }

    /**
     * DTO per registrazione
     */
    public static class RegisterRequest {
        
        private String username;
        private String email;
        private String password;
        private String confirmPassword;
        private String name;
        private String surname;
        
        // Constructors
        public RegisterRequest() {}
        
        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getSurname() { return surname; }
        public void setSurname(String surname) { this.surname = surname; }
        
        @Override
        public String toString() {
            return "RegisterRequest{" +
                    "username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    ", name='" + name + '\'' +
                    ", surname='" + surname + '\'' +
                    '}';
        }
    }
}