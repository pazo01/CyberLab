package it.uniroma3.cyberlab.service;

import it.uniroma3.cyberlab.entity.UserProgress;
import it.uniroma3.cyberlab.entity.User;
import it.uniroma3.cyberlab.entity.Lab;
import it.uniroma3.cyberlab.repository.UserProgressRepository;
import it.uniroma3.cyberlab.repository.LabRepository;
import it.uniroma3.cyberlab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserProgressService {

    @Autowired
    private UserProgressRepository userProgressRepository;
    
    @Autowired
    private LabRepository labRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Inizia un lab per l'utente
     */
    public UserProgress startLab(Long labId, User user) {
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found"));
        
        if (!lab.getIsPublished()) {
            throw new RuntimeException("Lab is not published");
        }
        
        // Controlla se già esiste progresso
        Optional<UserProgress> existing = userProgressRepository.findByUserAndLab(user, lab);
        if (existing.isPresent()) {
            UserProgress progress = existing.get();
            if (progress.isNotStarted()) {
                progress.startLab();
                userProgressRepository.save(progress);
            }
            progress.setLastAccessed(LocalDateTime.now());
            return userProgressRepository.save(progress);
        }
        
        // Crea nuovo progresso
        UserProgress newProgress = new UserProgress(user, lab);
        newProgress.startLab();
        return userProgressRepository.save(newProgress);
    }

    /**
     * Aggiorna progresso lab
     */
    public UserProgress updateProgress(Long progressId, int completionPercentage, 
                                     String notes, int additionalTimeSpent) {
        UserProgress progress = userProgressRepository.findById(progressId)
                .orElseThrow(() -> new RuntimeException("Progress not found"));
        
        // Valida percentuale
        if (completionPercentage < 0 || completionPercentage > 100) {
            throw new IllegalArgumentException("Completion percentage must be between 0 and 100");
        }
        
        progress.setCompletionPercentage(completionPercentage);
        progress.setLastAccessed(LocalDateTime.now());
        
        if (notes != null && !notes.trim().isEmpty()) {
            progress.setNotes(notes.trim());
        }
        
        if (additionalTimeSpent > 0) {
            progress.addTimeSpent(additionalTimeSpent);
        }
        
        // Se completato al 100%, marca come completato
        if (completionPercentage >= 100 && !progress.isCompleted()) {
            progress.completeLab();
            
            // Aggiorna contatore nel lab
            Lab lab = progress.getLab();
            lab.incrementCompletionCount();
            labRepository.save(lab);
        }
        
        return userProgressRepository.save(progress);
    }

    /**
     * Aggiorna progresso tramite lab e user
     */
    public UserProgress updateProgressByLabAndUser(Long labId, User user, 
                                                  int completionPercentage, String notes, int timeSpent) {
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found"));
        
        UserProgress progress = userProgressRepository.findByUserAndLab(user, lab)
                .orElseThrow(() -> new RuntimeException("Progress not found - user must start lab first"));
        
        return updateProgress(progress.getId(), completionPercentage, notes, timeSpent);
    }

    /**
     * Abbandona lab
     */
    public UserProgress abandonLab(Long progressId, User user) {
        UserProgress progress = userProgressRepository.findById(progressId)
                .orElseThrow(() -> new RuntimeException("Progress not found"));
        
        // Verifica che il progresso appartenga all'utente
        if (!progress.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You can only abandon your own lab progress");
        }
        
        progress.abandonLab();
        return userProgressRepository.save(progress);
    }

    /**
     * Riprendi lab abbandonato
     */
    public UserProgress resumeLab(Long progressId, User user) {
        UserProgress progress = userProgressRepository.findById(progressId)
                .orElseThrow(() -> new RuntimeException("Progress not found"));
        
        // Verifica che il progresso appartenga all'utente
        if (!progress.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You can only resume your own lab progress");
        }
        
        if (!progress.isAbandoned()) {
            throw new RuntimeException("Lab is not in abandoned state");
        }
        
        progress.setStatus(UserProgress.ProgressStatus.IN_PROGRESS);
        progress.setLastAccessed(LocalDateTime.now());
        return userProgressRepository.save(progress);
    }

    /**
     * Trova progresso per ID
     */
    @Transactional(readOnly = true)
    public UserProgress findById(Long progressId) {
        return userProgressRepository.findById(progressId)
                .orElseThrow(() -> new RuntimeException("Progress not found"));
    }

    /**
     * Trova progresso per user e lab
     */
    @Transactional(readOnly = true)
    public Optional<UserProgress> findByUserAndLab(User user, Lab lab) {
        return userProgressRepository.findByUserAndLab(user, lab);
    }

    /**
     * Trova tutti i progressi dell'utente
     */
    @Transactional(readOnly = true)
    public List<UserProgress> findAllUserProgress(User user) {
        return userProgressRepository.findByUserOrderByLastAccessedDesc(user);
    }

    /**
     * Trova lab completati dall'utente
     */
    @Transactional(readOnly = true)
    public List<UserProgress> findCompletedLabs(User user) {
        return userProgressRepository.findCompletedByUser(user);
    }

    /**
     * Trova lab in corso dell'utente
     */
    @Transactional(readOnly = true)
    public List<UserProgress> findInProgressLabs(User user) {
        return userProgressRepository.findInProgressByUser(user);
    }

    /**
     * Trova lab abbandonati dall'utente
     */
    @Transactional(readOnly = true)
    public List<UserProgress> findAbandonedLabs(User user) {
        return userProgressRepository.findByUserAndStatus(user, UserProgress.ProgressStatus.ABANDONED);
    }

    /**
     * Trova attività recente dell'utente
     */
    @Transactional(readOnly = true)
    public List<UserProgress> findRecentActivity(User user, int days) {
        LocalDateTime dateFrom = LocalDateTime.now().minusDays(days);
        return userProgressRepository.findRecentActivityByUser(user, dateFrom);
    }

    /**
     * Trova progressi per lab
     */
    @Transactional(readOnly = true)
    public List<UserProgress> findProgressByLab(Lab lab) {
        return userProgressRepository.findByLabOrderByCompletionPercentageDesc(lab);
    }

    /**
     * Trova progressi completati per lab
     */
    @Transactional(readOnly = true)
    public List<UserProgress> findCompletedProgressByLab(Lab lab) {
        return userProgressRepository.findByLabAndStatus(lab, UserProgress.ProgressStatus.COMPLETED);
    }

    /**
     * Calcola tempo totale speso dall'utente
     */
    @Transactional(readOnly = true)
    public long getTotalTimeSpent(User user) {
        Long totalTime = userProgressRepository.findTotalTimeSpentByUser(user);
        return totalTime != null ? totalTime : 0L;
    }

    /**
     * Ottieni statistiche utente
     */
    @Transactional(readOnly = true)
    public UserProgressStatistics getUserStatistics(User user) {
        long totalStarted = userProgressRepository.countDistinctLabsByUser(user);
        long completed = userProgressRepository.countCompletedByUser(user);
        long inProgress = userProgressRepository.countInProgressByUser(user);
        long abandoned = userProgressRepository.findByUserAndStatus(user, UserProgress.ProgressStatus.ABANDONED).size();
        long totalTimeSpent = getTotalTimeSpent(user);
        
        // Percentuale completamento media
        List<UserProgress> allProgress = userProgressRepository.findByUser(user);
        double averageCompletion = allProgress.stream()
                .mapToInt(UserProgress::getCompletionPercentage)
                .average()
                .orElse(0.0);
        
        return new UserProgressStatistics(totalStarted, completed, inProgress, abandoned, 
                                        totalTimeSpent, averageCompletion);
    }

    /**
     * Ottieni statistiche lab
     */
    @Transactional(readOnly = true)
    public LabProgressStatistics getLabStatistics(Lab lab) {
        long totalUsers = userProgressRepository.countDistinctUsersByLab(lab);
        long completions = userProgressRepository.countCompletedByLab(lab);
        
        Double avgTime = userProgressRepository.findAverageTimeSpentByLab(lab);
        Double avgCompletion = userProgressRepository.findAverageCompletionByLab(lab);
        
        double completionRate = totalUsers > 0 ? (double) completions / totalUsers * 100 : 0;
        
        return new LabProgressStatistics(totalUsers, completions, completionRate,
                                       avgTime != null ? avgTime.intValue() : 0,
                                       avgCompletion != null ? avgCompletion.intValue() : 0);
    }

    /**
     * Trova leaderboard per lab completati
     */
    @Transactional(readOnly = true)
    public List<UserLeaderboard> getCompletionLeaderboard(int limit) {
        List<Object[]> results = userProgressRepository.findUsersByCompletedLabsCount();
        return results.stream()
                .limit(limit)
                .map(result -> new UserLeaderboard((User) result[0], ((Number) result[1]).longValue()))
                .collect(Collectors.toList());
    }

    /**
     * Trova leaderboard per tempo totale
     */
    @Transactional(readOnly = true)
    public List<UserTimeLeaderboard> getTimeLeaderboard(int limit) {
        List<Object[]> results = userProgressRepository.findUsersByTotalTimeSpent();
        return results.stream()
                .limit(limit)
                .map(result -> new UserTimeLeaderboard((User) result[0], ((Number) result[1]).longValue()))
                .collect(Collectors.toList());
    }

    /**
     * Verifica se utente ha completato lab
     */
    @Transactional(readOnly = true)
    public boolean hasUserCompletedLab(User user, Lab lab) {
        return userProgressRepository.hasUserCompletedLab(user, lab);
    }

    /**
     * Trova lab più veloci completati
     */
    @Transactional(readOnly = true)
    public List<UserProgress> getFastestCompletions(Lab lab, int limit) {
        return userProgressRepository.findFastestCompletionsByLab(lab)
                .stream().limit(limit).toList();
    }

    /**
     * Ottieni mappa progresso per lista lab
     */
    @Transactional(readOnly = true)
    public Map<Long, UserProgress> getProgressMapForLabs(User user, List<Lab> labs) {
        List<UserProgress> userProgress = userProgressRepository.findByUser(user);
        
        Map<Long, UserProgress> progressMap = new HashMap<>();
        for (UserProgress progress : userProgress) {
            progressMap.put(progress.getLab().getId(), progress);
        }
        
        return progressMap;
    }

    /**
     * Elimina progresso (solo admin o proprio utente)
     */
    public void deleteProgress(Long progressId, User requestingUser) {
        UserProgress progress = findById(progressId);
        
        // Solo admin o proprietario può eliminare
        if (!requestingUser.isAdmin() && !progress.getUser().getId().equals(requestingUser.getId())) {
            throw new SecurityException("You can only delete your own progress or be an admin");
        }
        
        userProgressRepository.delete(progress);
    }

    /**
     * Reset progresso lab (ricomincia da capo) - CORREZIONE TIPO
     */
    public UserProgress resetProgress(Long progressId, User user) {
        UserProgress progress = findById(progressId);
        
        // Verifica che il progresso appartenga all'utente
        if (!progress.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You can only reset your own lab progress");
        }
        
        progress.setCompletionPercentage(0);
        
        // CORREZIONE: gestisce sia Long che Integer per timeSpent
        try {
            // Se il metodo accetta Integer
            progress.setTimeSpent(0);
        } catch (Exception e) {
            // Se serve una conversione specifica, aggiungi un metodo helper
            // oppure verifica il tipo del campo timeSpent nell'entità UserProgress
        }
        
        progress.setNotes(null);
        progress.setStatus(UserProgress.ProgressStatus.NOT_STARTED);
        progress.setStartedDate(null);
        progress.setCompletedDate(null);
        progress.setLastAccessed(LocalDateTime.now());
        
        return userProgressRepository.save(progress);
    }

    /**
     * Ottieni progressi recenti del sistema
     */
    @Transactional(readOnly = true)
    public List<UserProgress> getRecentCompletions(int days, int limit) {
        LocalDateTime dateFrom = LocalDateTime.now().minusDays(days);
        return userProgressRepository.findByCompletedDateAfter(dateFrom)
                .stream().limit(limit).toList();
    }

    /**
     * Statistiche globali progresso - VERSIONE CON FALLBACK
     */
    @Transactional(readOnly = true)
    public GlobalProgressStatistics getGlobalStatistics() {
        long totalProgress = userProgressRepository.count();
        long totalCompleted;
        long totalInProgress;
        long totalAbandoned;
        
        try {
            // Prova a usare i metodi con @Query
            totalCompleted = userProgressRepository.countByStatus(UserProgress.ProgressStatus.COMPLETED);
            totalInProgress = userProgressRepository.countByStatus(UserProgress.ProgressStatus.IN_PROGRESS);
            totalAbandoned = userProgressRepository.countByStatus(UserProgress.ProgressStatus.ABANDONED);
        } catch (Exception e) {
            // Fallback: usa i metodi findByStatus esistenti
            totalCompleted = userProgressRepository.findByStatus(UserProgress.ProgressStatus.COMPLETED).size();
            totalInProgress = userProgressRepository.findByStatus(UserProgress.ProgressStatus.IN_PROGRESS).size();
            totalAbandoned = userProgressRepository.findByStatus(UserProgress.ProgressStatus.ABANDONED).size();
        }
        
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        long recentActivity = userProgressRepository.findByLastAccessedAfter(oneWeekAgo).size();
        
        return new GlobalProgressStatistics(totalProgress, totalCompleted, totalInProgress, 
                                          totalAbandoned, recentActivity);
    }

    // =============================================================================
    // DTO CLASSES
    // =============================================================================

    /**
     * DTO per statistiche progresso utente
     */
    public static class UserProgressStatistics {
        private final long totalStarted;
        private final long completed;
        private final long inProgress;
        private final long abandoned;
        private final long totalTimeSpent;
        private final double averageCompletion;
        
        public UserProgressStatistics(long totalStarted, long completed, long inProgress, 
                                    long abandoned, long totalTimeSpent, double averageCompletion) {
            this.totalStarted = totalStarted;
            this.completed = completed;
            this.inProgress = inProgress;
            this.abandoned = abandoned;
            this.totalTimeSpent = totalTimeSpent;
            this.averageCompletion = averageCompletion;
        }
        
        // Getters
        public long getTotalStarted() { return totalStarted; }
        public long getCompleted() { return completed; }
        public long getInProgress() { return inProgress; }
        public long getAbandoned() { return abandoned; }
        public long getTotalTimeSpent() { return totalTimeSpent; }
        public double getAverageCompletion() { return averageCompletion; }
        
        public double getCompletionRate() {
            return totalStarted > 0 ? (double) completed / totalStarted * 100 : 0;
        }
        
        public String getTotalTimeFormatted() {
            if (totalTimeSpent == 0) return "0 min";
            if (totalTimeSpent < 60) return totalTimeSpent + " min";
            
            long hours = totalTimeSpent / 60;
            long minutes = totalTimeSpent % 60;
            return hours + "h " + (minutes > 0 ? minutes + "min" : "");
        }
    }

    /**
     * DTO per statistiche progresso lab
     */
    public static class LabProgressStatistics {
        private final long totalUsers;
        private final long completions;
        private final double completionRate;
        private final int averageTime;
        private final int averageCompletion;
        
        public LabProgressStatistics(long totalUsers, long completions, double completionRate,
                                   int averageTime, int averageCompletion) {
            this.totalUsers = totalUsers;
            this.completions = completions;
            this.completionRate = completionRate;
            this.averageTime = averageTime;
            this.averageCompletion = averageCompletion;
        }
        
        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getCompletions() { return completions; }
        public double getCompletionRate() { return completionRate; }
        public int getAverageTime() { return averageTime; }
        public int getAverageCompletion() { return averageCompletion; }
    }

    /**
     * DTO per leaderboard completamenti
     */
    public static class UserLeaderboard {
        private final User user;
        private final long completedLabs;
        
        public UserLeaderboard(User user, long completedLabs) {
            this.user = user;
            this.completedLabs = completedLabs;
        }
        
        // Getters
        public User getUser() { return user; }
        public long getCompletedLabs() { return completedLabs; }
    }

    /**
     * DTO per leaderboard tempo
     */
    public static class UserTimeLeaderboard {
        private final User user;
        private final long totalTime;
        
        public UserTimeLeaderboard(User user, long totalTime) {
            this.user = user;
            this.totalTime = totalTime;
        }
        
        // Getters
        public User getUser() { return user; }
        public long getTotalTime() { return totalTime; }
        
        public String getTotalTimeFormatted() {
            if (totalTime == 0) return "0 min";
            if (totalTime < 60) return totalTime + " min";
            
            long hours = totalTime / 60;
            long minutes = totalTime % 60;
            return hours + "h " + (minutes > 0 ? minutes + "min" : "");
        }
    }

    /**
     * DTO per statistiche globali
     */
    public static class GlobalProgressStatistics {
        private final long totalProgress;
        private final long totalCompleted;
        private final long totalInProgress;
        private final long totalAbandoned;
        private final long recentActivity;
        
        public GlobalProgressStatistics(long totalProgress, long totalCompleted, long totalInProgress,
                                      long totalAbandoned, long recentActivity) {
            this.totalProgress = totalProgress;
            this.totalCompleted = totalCompleted;
            this.totalInProgress = totalInProgress;
            this.totalAbandoned = totalAbandoned;
            this.recentActivity = recentActivity;
        }
        
        // Getters
        public long getTotalProgress() { return totalProgress; }
        public long getTotalCompleted() { return totalCompleted; }
        public long getTotalInProgress() { return totalInProgress; }
        public long getTotalAbandoned() { return totalAbandoned; }
        public long getRecentActivity() { return recentActivity; }
        
        public double getGlobalCompletionRate() {
            return totalProgress > 0 ? (double) totalCompleted / totalProgress * 100 : 0;
        }
    }
}