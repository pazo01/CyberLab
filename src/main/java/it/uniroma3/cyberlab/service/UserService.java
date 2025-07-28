package it.uniroma3.cyberlab.service;

import it.uniroma3.cyberlab.entity.User;
import it.uniroma3.cyberlab.entity.UserProgress;
import it.uniroma3.cyberlab.repository.UserRepository;
import it.uniroma3.cyberlab.repository.UserProgressRepository;
import it.uniroma3.cyberlab.repository.PostRepository;
import it.uniroma3.cyberlab.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserProgressRepository userProgressRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Crea nuovo utente con validazione
     */
    public User createUser(String username, String email, String password, String name, String surname) {
        // Validazione duplicati
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Crea utente
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setSurname(surname);
        user.setRole(User.Role.USER);
        user.setStatus(User.UserStatus.ACTIVE);
        
        return userRepository.save(user);
    }

    /**
     * Trova utente per username o email
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
    }

    /**
     * Verifica disponibilità username
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsernameIgnoreCase(username);
    }

    /**
     * Verifica disponibilità email
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmailIgnoreCase(email);
    }

    /**
     * Aggiorna profilo utente
     */
    public User updateProfile(User user, String name, String surname, String profileInfo) {
        user.setName(name);
        user.setSurname(surname);
        user.setProfileInfo(profileInfo);
        return userRepository.save(user);
    }

    /**
     * Cambia password utente
     */
    public void changePassword(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Bannare/sbannare utente (solo admin)
     */
    public User updateUserStatus(Long userId, User.UserStatus status, User admin) {
        if (!admin.isAdmin()) {
            throw new SecurityException("Only admins can change user status");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setStatus(status);
        return userRepository.save(user);
    }

    /**
     * Promovi utente ad admin
     */
    public User promoteToAdmin(Long userId, User admin) {
        if (!admin.isAdmin()) {
            throw new SecurityException("Only admins can promote users");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setRole(User.Role.ADMIN);
        return userRepository.save(user);
    }

    /**
     * Statistiche utente
     */
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics(User user) {
        UserStatistics stats = new UserStatistics();
        
        // Conteggi base
        stats.setTotalPosts(postRepository.countByAuthor(user));
        stats.setTotalComments(commentRepository.countByAuthor(user));
        stats.setCompletedLabs(userProgressRepository.countCompletedByUser(user));
        stats.setInProgressLabs(userProgressRepository.countInProgressByUser(user));
        
        // Tempo totale sui lab
        Long totalTime = userProgressRepository.findTotalTimeSpentByUser(user);
        stats.setTotalTimeSpent(totalTime != null ? totalTime : 0L);
        
        // Progresso lab
        List<UserProgress> allProgress = userProgressRepository.findByUser(user);
        stats.setTotalLabsStarted(allProgress.size());
        
        // Calcola percentuale completamento generale
        if (!allProgress.isEmpty()) {
            double avgCompletion = allProgress.stream()
                    .mapToInt(UserProgress::getCompletionPercentage)
                    .average()
                    .orElse(0.0);
            stats.setOverallProgress((int) avgCompletion);
        }
        
        return stats;
    }

    /**
     * Statistiche amministratore - METODO AGGIUNTO
     */
    @Transactional(readOnly = true)
    public AdminStatistics getAdminStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countActiveUsers();
        long bannedUsers = userRepository.countBannedUsers();
        
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        long newUsersThisMonth = userRepository.countUsersJoinedAfter(oneMonthAgo);
        
        return new AdminStatistics(totalUsers, activeUsers, bannedUsers, newUsersThisMonth);
    }

    /**
     * Cerca utenti con filtri - METODO AGGIUNTO
     */
    @Transactional(readOnly = true)
    public List<User> findUsersWithFilters(String search, String status, int page, int size) {
        List<User> users;
        
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.searchUsers(search.trim());
        } else if (!"all".equals(status)) {
            try {
                User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
                users = userRepository.findByStatus(userStatus);
            } catch (IllegalArgumentException e) {
                users = userRepository.findAll();
            }
        } else {
            users = userRepository.findAll();
        }
        
        // Simulazione paginazione
        int start = page * size;
        int end = Math.min(start + size, users.size());
        
        if (start < users.size()) {
            return users.subList(start, end);
        } else {
            return List.of();
        }
    }

    /**
     * Ricerca utenti per nome/username - METODO AGGIUNTO
     */
    @Transactional(readOnly = true)
    public List<User> searchUsers(String searchTerm) {
        return userRepository.searchUsers(searchTerm);
    }

    /**
     * Conteggio totale utenti - METODO AGGIUNTO
     */
    @Transactional(readOnly = true)
    public long getTotalUsersCount() {
        return userRepository.count();
    }

    /**
     * Dati analytics per admin - METODO AGGIUNTO
     */
    @Transactional(readOnly = true)
    public AnalyticsData getAnalyticsData() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        
        long usersThisWeek = userRepository.countUsersJoinedAfter(oneWeekAgo);
        long usersThisMonth = userRepository.countUsersJoinedAfter(oneMonthAgo);
        long usersThreeMonths = userRepository.countUsersJoinedAfter(threeMonthsAgo);
        
        return new AnalyticsData(usersThisWeek, usersThisMonth, usersThreeMonths);
    }

    /**
     * Trova utenti attivi di recente
     */
    @Transactional(readOnly = true)
    public List<User> findRecentActiveUsers(int limit) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<User> users = userRepository.findNewActiveUsers(oneWeekAgo);
        return users.stream().limit(limit).toList();
    }

    /**
     * Conta utenti per status
     */
    @Transactional(readOnly = true)
    public long countUsersByStatus(User.UserStatus status) {
        return userRepository.findByStatus(status).size();
    }

    /**
     * Statistiche admin - RINOMINATO per compatibilità
     */
    @Transactional(readOnly = true)
    public AdminUserStatistics getAdminUserStatistics() {
        AdminUserStatistics stats = new AdminUserStatistics();
        
        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.countActiveUsers());
        stats.setBannedUsers(userRepository.countBannedUsers());
        stats.setAdminUsers(userRepository.countByRole(User.Role.ADMIN));
        
        // Nuovi utenti ultimo mese
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        stats.setNewUsersThisMonth(userRepository.countUsersJoinedAfter(oneMonthAgo));
        
        return stats;
    }

    // =============================================================================
    // DTO CLASSES
    // =============================================================================

    /**
     * DTO per statistiche utente
     */
    public static class UserStatistics {
        private long totalPosts;
        private long totalComments;
        private long completedLabs;
        private long inProgressLabs;
        private long totalLabsStarted;
        private long totalTimeSpent;
        private int overallProgress;

        // Getters and Setters
        public long getTotalPosts() { return totalPosts; }
        public void setTotalPosts(long totalPosts) { this.totalPosts = totalPosts; }
        
        public long getTotalComments() { return totalComments; }
        public void setTotalComments(long totalComments) { this.totalComments = totalComments; }
        
        public long getCompletedLabs() { return completedLabs; }
        public void setCompletedLabs(long completedLabs) { this.completedLabs = completedLabs; }
        
        public long getInProgressLabs() { return inProgressLabs; }
        public void setInProgressLabs(long inProgressLabs) { this.inProgressLabs = inProgressLabs; }
        
        public long getTotalLabsStarted() { return totalLabsStarted; }
        public void setTotalLabsStarted(long totalLabsStarted) { this.totalLabsStarted = totalLabsStarted; }
        
        public long getTotalTimeSpent() { return totalTimeSpent; }
        public void setTotalTimeSpent(long totalTimeSpent) { this.totalTimeSpent = totalTimeSpent; }
        
        public int getOverallProgress() { return overallProgress; }
        public void setOverallProgress(int overallProgress) { this.overallProgress = overallProgress; }

        public String getTotalTimeSpentFormatted() {
            if (totalTimeSpent == 0) return "0 min";
            if (totalTimeSpent < 60) {
                return totalTimeSpent + " min";
            } else {
                long hours = totalTimeSpent / 60;
                long minutes = totalTimeSpent % 60;
                if (minutes == 0) {
                    return hours + "h";
                } else {
                    return hours + "h " + minutes + "min";
                }
            }
        }
    }

    /**
     * DTO per statistiche admin - AGGIUNTO
     */
    public static class AdminStatistics {
        private final long totalUsers;
        private final long activeUsers;
        private final long bannedUsers;
        private final long newUsersThisMonth;
        
        public AdminStatistics(long totalUsers, long activeUsers, long bannedUsers, long newUsersThisMonth) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.bannedUsers = bannedUsers;
            this.newUsersThisMonth = newUsersThisMonth;
        }
        
        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getActiveUsers() { return activeUsers; }
        public long getBannedUsers() { return bannedUsers; }
        public long getNewUsersThisMonth() { return newUsersThisMonth; }
    }

    /**
     * DTO per analytics - AGGIUNTO
     */
    public static class AnalyticsData {
        private final long usersThisWeek;
        private final long usersThisMonth;
        private final long usersThreeMonths;
        
        public AnalyticsData(long usersThisWeek, long usersThisMonth, long usersThreeMonths) {
            this.usersThisWeek = usersThisWeek;
            this.usersThisMonth = usersThisMonth;
            this.usersThreeMonths = usersThreeMonths;
        }
        
        // Getters
        public long getUsersThisWeek() { return usersThisWeek; }
        public long getUsersThisMonth() { return usersThisMonth; }
        public long getUsersThreeMonths() { return usersThreeMonths; }
    }

    /**
     * DTO per statistiche admin dettagliate
     */
    public static class AdminUserStatistics {
        private long totalUsers;
        private long activeUsers;
        private long bannedUsers;
        private long adminUsers;
        private long newUsersThisMonth;

        // Getters and Setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        
        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
        
        public long getBannedUsers() { return bannedUsers; }
        public void setBannedUsers(long bannedUsers) { this.bannedUsers = bannedUsers; }
        
        public long getAdminUsers() { return adminUsers; }
        public void setAdminUsers(long adminUsers) { this.adminUsers = adminUsers; }
        
        public long getNewUsersThisMonth() { return newUsersThisMonth; }
        public void setNewUsersThisMonth(long newUsersThisMonth) { this.newUsersThisMonth = newUsersThisMonth; }
    }
    
    
    
    
 // =============================================================================
 // METODI DA AGGIUNGERE AL UserService.java
 // =============================================================================

 /**
  * Aggiorna profilo utente con email
  */
 public User updateProfileWithEmail(User user, String name, String surname, String email, String profileInfo) {
     // Verifica che la nuova email non sia già in uso (se diversa da quella attuale)
     if (!user.getEmail().equals(email) && !isEmailAvailableForUser(email, user.getId())) {
         throw new IllegalArgumentException("Email is already in use by another account");
     }
     
     user.setName(name);
     user.setSurname(surname);
     user.setEmail(email);
     user.setProfileInfo(profileInfo);
     return userRepository.save(user);
 }

 /**
  * Verifica email disponibile per utente specifico
  */
 @Transactional(readOnly = true)
 public boolean isEmailAvailableForUser(String email, Long userId) {
     // Usa il metodo esistente existsByEmailIgnoreCase
     if (!userRepository.existsByEmailIgnoreCase(email)) {
         return true; // Email disponibile
     }
     
     // Se email esiste, verifica se appartiene allo stesso utente
     Optional<User> existingUser = userRepository.findByUsernameOrEmail(email, email);
     return existingUser.isPresent() && existingUser.get().getId().equals(userId);
 }

 /**
  * Aggiorna solo email utente
  */
 public User updateUserEmail(User user, String newEmail) {
     if (!isEmailAvailable(newEmail)) {
         throw new IllegalArgumentException("Email is already in use");
     }
     
     user.setEmail(newEmail);
     return userRepository.save(user);
 }

 /**
  * Aggiorna avatar utente (placeholder per upload)
  */
 public User updateUserAvatar(User user, String avatarFileName) {
     // In un sistema reale, salveresti il file e aggiorneresti il campo avatar
     // Per ora, salviamo solo il nome del file nel profileInfo
     String currentInfo = user.getProfileInfo();
     String updatedInfo = (currentInfo != null ? currentInfo + "\n" : "") + 
                         "[Avatar] " + avatarFileName;
     user.setProfileInfo(updatedInfo);
     return userRepository.save(user);
 }

 /**
  * Salva preferenze utente (versione semplificata)
  */
 public User updateUserPreferences(User user, Map<String, Object> preferences) {
     // In un sistema reale, useresti una tabella UserPreferences separata
     // Per ora, salviamo nel campo profileInfo
     
     StringBuilder prefString = new StringBuilder("[Preferences] ");
     preferences.forEach((key, value) -> 
         prefString.append(key).append(": ").append(value).append("; "));
     
     String currentInfo = user.getProfileInfo();
     // Rimuovi vecchie preferenze se esistono
     if (currentInfo != null && currentInfo.contains("[Preferences]")) {
         currentInfo = currentInfo.replaceAll("\\[Preferences\\][^\\n]*", "").trim();
     }
     
     String updatedInfo = (currentInfo != null && !currentInfo.isEmpty() ? currentInfo + "\n" : "") + 
                         prefString.toString();
     
     user.setProfileInfo(updatedInfo);
     return userRepository.save(user);
 }

 /**
  * Elimina account utente (soft delete)
  */
 public void deleteUserAccount(User user) {
     // In un sistema reale, implementeresti una soft delete o hard delete
     // con cleanup di tutti i dati correlati (posts, comments, progress, etc.)
     
     // Per ora, cambiamo solo lo status
     user.setStatus(User.UserStatus.SUSPENDED);
     user.setEmail("deleted_" + user.getId() + "@cyberlab.deleted");
     user.setUsername("deleted_user_" + user.getId());
     userRepository.save(user);
     
     // TODO: In un sistema reale dovresti anche:
     // 1. Eliminare o anonimizzare posts e comments
     // 2. Eliminare progress dei lab
     // 3. Eliminare reports e segnalazioni
     // 4. Notificare admin della cancellazione
 }

 /**
  * Verifica forza password
  */
 public PasswordStrength checkPasswordStrength(String password) {
     int score = 0;
     String feedback = "";
     
     if (password.length() >= 8) score++;
     else feedback += "Password should be at least 8 characters. ";
     
     if (password.matches(".*[a-z].*")) score++;
     else feedback += "Add lowercase letters. ";
     
     if (password.matches(".*[A-Z].*")) score++;
     else feedback += "Add uppercase letters. ";
     
     if (password.matches(".*[0-9].*")) score++;
     else feedback += "Add numbers. ";
     
     if (password.matches(".*[^A-Za-z0-9].*")) score++;
     else feedback += "Add special characters. ";
     
     PasswordStrength.Strength strength;
     switch (score) {
         case 0, 1 -> strength = PasswordStrength.Strength.VERY_WEAK;
         case 2 -> strength = PasswordStrength.Strength.WEAK;
         case 3 -> strength = PasswordStrength.Strength.FAIR;
         case 4 -> strength = PasswordStrength.Strength.GOOD;
         case 5 -> strength = PasswordStrength.Strength.STRONG;
         default -> strength = PasswordStrength.Strength.VERY_WEAK;
     }
     
     return new PasswordStrength(strength, score, feedback.trim());
 }

 /**
  * Ottieni preferenze utente dal profileInfo
  */
 @Transactional(readOnly = true)
 public Map<String, String> getUserPreferences(User user) {
     Map<String, String> preferences = new HashMap<>();
     
     if (user.getProfileInfo() != null && user.getProfileInfo().contains("[Preferences]")) {
         String prefLine = user.getProfileInfo();
         int start = prefLine.indexOf("[Preferences]");
         if (start != -1) {
             String prefString = prefLine.substring(start + "[Preferences]".length());
             int end = prefString.indexOf("\n");
             if (end != -1) {
                 prefString = prefString.substring(0, end);
             }
             
             // Parse preferences string
             String[] pairs = prefString.split(";");
             for (String pair : pairs) {
                 String[] keyValue = pair.split(":");
                 if (keyValue.length == 2) {
                     preferences.put(keyValue[0].trim(), keyValue[1].trim());
                 }
             }
         }
     }
     
     // Default values se non trovate
     preferences.putIfAbsent("Email Notifications", "enabled");
     preferences.putIfAbsent("Lab Completion Notifications", "enabled");
     preferences.putIfAbsent("Weekly Digest", "disabled");
     preferences.putIfAbsent("Theme", "dark");
     preferences.putIfAbsent("Language", "en");
     preferences.putIfAbsent("Timezone", "Europe/Rome");
     
     return preferences;
 }

 /**
  * Trova utente per ID con eccezione
  */
 @Transactional(readOnly = true)
 public User findById(Long userId) {
     return userRepository.findById(userId)
             .orElseThrow(() -> new IllegalArgumentException("User not found"));
 }

 // =============================================================================
 // DTO CLASSES DA AGGIUNGERE
 // =============================================================================

 /**
  * DTO per forza password
  */
 public static class PasswordStrength {
     public enum Strength {
         VERY_WEAK, WEAK, FAIR, GOOD, STRONG
     }
     
     private final Strength strength;
     private final int score;
     private final String feedback;
     
     public PasswordStrength(Strength strength, int score, String feedback) {
         this.strength = strength;
         this.score = score;
         this.feedback = feedback;
     }
     
     // Getters
     public Strength getStrength() { return strength; }
     public int getScore() { return score; }
     public String getFeedback() { return feedback; }
     
     public String getStrengthText() {
         return switch (strength) {
             case VERY_WEAK -> "Very Weak";
             case WEAK -> "Weak";
             case FAIR -> "Fair";
             case GOOD -> "Good";
             case STRONG -> "Strong";
         };
     }
     
     public String getStrengthColor() {
         return switch (strength) {
             case VERY_WEAK -> "#ff4444";
             case WEAK -> "#ff8c00";
             case FAIR -> "#ffa500";
             case GOOD -> "#32cd32";
             case STRONG -> "#00ff41";
         };
     }
 }

 /**
  * DTO per aggiornamento profilo
  */
 public static class ProfileUpdateRequest {
     private String name;
     private String surname;
     private String email;
     private String profileInfo;
     
     // Constructors
     public ProfileUpdateRequest() {}
     
     public ProfileUpdateRequest(String name, String surname, String email, String profileInfo) {
         this.name = name;
         this.surname = surname;
         this.email = email;
         this.profileInfo = profileInfo;
     }
     
     // Getters and Setters
     public String getName() { return name; }
     public void setName(String name) { this.name = name; }
     
     public String getSurname() { return surname; }
     public void setSurname(String surname) { this.surname = surname; }
     
     public String getEmail() { return email; }
     public void setEmail(String email) { this.email = email; }
     
     public String getProfileInfo() { return profileInfo; }
     public void setProfileInfo(String profileInfo) { this.profileInfo = profileInfo; }
 }
    
    
 public void updateAvatar(User user, String avatarFilename) {
	    user.setAvatar(avatarFilename);
	    userRepository.save(user);
	}
    
    
}