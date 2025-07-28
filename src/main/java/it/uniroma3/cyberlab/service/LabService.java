package it.uniroma3.cyberlab.service;

import it.uniroma3.cyberlab.entity.Lab;
import it.uniroma3.cyberlab.entity.User;
import it.uniroma3.cyberlab.entity.Category;
import it.uniroma3.cyberlab.entity.UserProgress;
import it.uniroma3.cyberlab.repository.LabRepository;
import it.uniroma3.cyberlab.repository.UserProgressRepository;
import it.uniroma3.cyberlab.repository.CategoryRepository;
import it.uniroma3.cyberlab.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class LabService {

    @Autowired
    private LabRepository labRepository;
    
    @Autowired
    private UserProgressRepository userProgressRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Trova lab per ID
     */
    @Transactional(readOnly = true)
    public Lab findById(Long labId) {
        return labRepository.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found"));
    }

    /**
     * Trova lab per ID con view increment
     */
    @Transactional
    public Lab findByIdAndIncrementViews(Long id) {
        Lab lab = labRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lab not found"));
        
        if (!lab.getIsPublished() && !SecurityUtils.isAdmin()) {
            throw new RuntimeException("Lab not available");
        }
        
        lab.incrementViewCount();
        labRepository.save(lab);
        return lab;
    }

    /**
     * Trova lab pubblicati con filtri
     */
    @Transactional(readOnly = true)
    public List<Lab> findPublishedLabs(Category category, Lab.Difficulty difficulty, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        
        if (category != null && difficulty != null) {
            return labRepository.findByCategoryAndDifficultyAndIsPublishedTrue(category, difficulty);
        } else if (category != null) {
            return labRepository.findByCategoryAndIsPublishedTrueOrderByCreatedDateDesc(category);
        } else if (difficulty != null) {
            return labRepository.findByDifficultyAndIsPublishedTrueOrderByCreatedDateDesc(difficulty);
        } else {
            return labRepository.findByIsPublishedTrue(pageRequest).getContent();
        }
    }

    /**
     * Ottiene mappa progresso utente per lista lab
     */
    @Transactional(readOnly = true)
    public Map<Long, UserProgress> getUserProgressMap(User user) {
        Map<Long, UserProgress> progressMap = new HashMap<>();
        List<UserProgress> userProgress = userProgressRepository.findByUser(user);
        for (UserProgress progress : userProgress) {
            progressMap.put(progress.getLab().getId(), progress);
        }
        return progressMap;
    }

    /**
     * Ottiene progresso utente per un lab specifico
     */
    @Transactional(readOnly = true)
    public UserProgress getUserProgress(User user, Lab lab) {
        return userProgressRepository.findByUserAndLab(user, lab).orElse(null);
    }

    /**
     * Trova lab correlati
     */
    @Transactional(readOnly = true)
    public List<Lab> findRelatedLabs(Lab lab, int limit) {
        List<Lab> relatedLabs = labRepository.findByCategoryAndIsPublishedTrueOrderByCreatedDateDesc(lab.getCategory());
        return relatedLabs.stream()
                .filter(l -> !l.getId().equals(lab.getId()))
                .limit(limit)
                .toList();
    }

    /**
     * Lab in corso dell'utente
     */
    @Transactional(readOnly = true)
    public List<UserProgress> getUserInProgressLabs(User user, int limit) {
        List<UserProgress> inProgress = userProgressRepository.findInProgressByUser(user);
        if (limit > 0 && inProgress.size() > limit) {
            return inProgress.subList(0, limit);
        }
        return inProgress;
    }

    /**
     * Lab completati dell'utente
     */
    @Transactional(readOnly = true)
    public List<UserProgress> getUserCompletedLabs(User user, int limit) {
        List<UserProgress> completed = userProgressRepository.findCompletedByUser(user);
        if (limit > 0 && completed.size() > limit) {
            return completed.subList(0, limit);
        }
        return completed;
    }

    /**
     * Tutti i progressi dell'utente
     */
    @Transactional(readOnly = true)
    public List<UserProgress> getUserAllProgress(User user) {
        return userProgressRepository.findByUserOrderByLastAccessedDesc(user);
    }

    /**
     * Lab non ancora iniziati dall'utente
     */
    @Transactional(readOnly = true)
    public List<Lab> getNotStartedLabs(User user) {
        List<Lab> allLabs = labRepository.findByIsPublishedTrue();
        return allLabs.stream()
                .filter(lab -> !userProgressRepository.existsByUserAndLab(user, lab))
                .toList();
    }

    /**
     * Tempo totale speso dall'utente sui lab
     */
    @Transactional(readOnly = true)
    public Long getTotalTimeSpentByUser(User user) {
        return userProgressRepository.findTotalTimeSpentByUser(user);
    }

    /**
     * Lab raccomandati per l'utente
     */
    @Transactional(readOnly = true)
    public List<Lab> getRecommendedLabs(User user, int limit) {
        List<Lab> allLabs = labRepository.findByIsPublishedTrue();
        return allLabs.stream()
                .filter(lab -> !userProgressRepository.existsByUserAndLab(user, lab))
                .limit(limit)
                .toList();
    }

    /**
     * Lab per principianti
     */
    @Transactional(readOnly = true)
    public List<Lab> getBeginnerLabs(int limit) {
        return labRepository.findByDifficultyAndIsPublishedTrueOrderByCreatedDateDesc(Lab.Difficulty.BEGINNER)
                .stream().limit(limit).toList();
    }

    /**
     * Lab in evidenza
     */
    @Transactional(readOnly = true)
    public List<Lab> getFeaturedLabs(int limit) {
        return labRepository.findMostViewedLabs(PageRequest.of(0, limit));
    }

    /**
     * Lab pubblicati
     */
    @Transactional(readOnly = true)
    public List<Lab> getPublishedLabs(int limit) {
        List<Lab> labs = labRepository.findByIsPublishedTrue();
        if (limit > 0 && labs.size() > limit) {
            return labs.subList(0, limit);
        }
        return labs;
    }

    /**
     * Lab più visti
     */
    @Transactional(readOnly = true)
    public List<Lab> getMostViewedLabs(int limit) {
        return labRepository.findMostViewedLabs(PageRequest.of(0, limit));
    }

    /**
     * Lab più completati
     */
    @Transactional(readOnly = true)
    public List<Lab> getMostCompletedLabs(int limit) {
        return labRepository.findMostCompletedLabs(PageRequest.of(0, limit));
    }

    /**
     * Ricerca lab - IMPLEMENTAZIONE FALLBACK se metodo non esiste nel repository
     */
    @Transactional(readOnly = true)
    public List<Lab> searchLabs(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }
        
        try {
            // Prova a usare il metodo del repository se esiste
            return labRepository.searchLabs(searchTerm.trim());
        } catch (Exception e) {
            // Fallback: ricerca manuale nei lab pubblicati
            String lowerTerm = searchTerm.trim().toLowerCase();
            return labRepository.findByIsPublishedTrue().stream()
                    .filter(lab -> 
                        lab.getTitle().toLowerCase().contains(lowerTerm) ||
                        lab.getDescription().toLowerCase().contains(lowerTerm))
                    .toList();
        }
    }

    /**
     * Trova lab con filtro stato
     */
    @Transactional(readOnly = true)
    public List<Lab> findLabsWithStatus(String status) {
        switch (status.toLowerCase()) {
            case "published":
                return labRepository.findByIsPublishedTrue();
            case "draft":
                return labRepository.findUnpublishedLabs();
            default:
                return labRepository.findAllOrderByCreatedDateDesc();
        }
    }

    /**
     * Conteggio totale lab
     */
    @Transactional(readOnly = true)
    public long getTotalLabsCount() {
        return labRepository.count();
    }

    /**
     * Conteggio totale completamenti - IMPLEMENTAZIONE FALLBACK
     */
    @Transactional(readOnly = true)
    public long getTotalCompletionsCount() {
        try {
            // Prova a usare il metodo del repository se esiste
            return userProgressRepository.countCompletedLabs();
        } catch (Exception e) {
            // Fallback: conta manualmente i lab completati
            return userProgressRepository.findByStatus(UserProgress.ProgressStatus.COMPLETED).size();
        }
    }

    /**
     * Crea lab con tutti i parametri
     */
    public Lab createLab(String title, String description, String theory, String exercise, 
                        String solution, Lab.Difficulty difficulty, Integer estimatedTime,
                        String prerequisites, String tools, String labUrl, Long categoryId,
                        Boolean isPublished, User createdBy) {
        
        if (!createdBy.isAdmin()) {
            throw new SecurityException("Only admins can create labs");
        }
        
        // Validazione
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        
        if (theory == null || theory.trim().isEmpty()) {
            throw new IllegalArgumentException("Theory content cannot be empty");
        }
        
        if (exercise == null || exercise.trim().isEmpty()) {
            throw new IllegalArgumentException("Exercise content cannot be empty");
        }
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        Lab newLab = new Lab();
        newLab.setTitle(title.trim());
        newLab.setDescription(description != null ? description.trim() : null);
        newLab.setTheory(theory.trim());
        newLab.setExercise(exercise.trim());
        newLab.setSolution(solution != null ? solution.trim() : null);
        newLab.setDifficulty(difficulty != null ? difficulty : Lab.Difficulty.BEGINNER);
        newLab.setEstimatedTime(estimatedTime);
        newLab.setPrerequisites(prerequisites != null ? prerequisites.trim() : null);
        newLab.setTools(tools != null ? tools.trim() : null);
        newLab.setLabUrl(labUrl != null ? labUrl.trim() : null);
        newLab.setCategory(category);
        newLab.setCreatedBy(createdBy);
        newLab.setIsPublished(isPublished != null ? isPublished : false);
        
        return labRepository.save(newLab);
    }

    /**
     * Aggiorna lab con tutti i parametri
     */
    public Lab updateLab(Long labId, String title, String description, String theory, 
                        String exercise, String solution, Lab.Difficulty difficulty, 
                        Integer estimatedTime, String prerequisites, String tools, 
                        String labUrl, Long categoryId, Boolean isPublished) {
        
        Lab lab = findById(labId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        lab.setTitle(title.trim());
        lab.setDescription(description != null ? description.trim() : null);
        lab.setTheory(theory.trim());
        lab.setExercise(exercise.trim());
        lab.setSolution(solution != null ? solution.trim() : null);
        lab.setDifficulty(difficulty != null ? difficulty : lab.getDifficulty());
        lab.setEstimatedTime(estimatedTime);
        lab.setPrerequisites(prerequisites != null ? prerequisites.trim() : null);
        lab.setTools(tools != null ? tools.trim() : null);
        lab.setLabUrl(labUrl != null ? labUrl.trim() : null);
        lab.setCategory(category);
        lab.setIsPublished(isPublished != null ? isPublished : lab.getIsPublished());
        lab.setLastModified(LocalDateTime.now());
        
        return labRepository.save(lab);
    }

    /**
     * Elimina lab e restituisce titolo
     */
    public String deleteLab(Long labId) {
        Lab lab = findById(labId);
        String labTitle = lab.getTitle();
        labRepository.delete(lab);
        return labTitle;
    }

    /**
     * Statistiche lab generali
     */
    @Transactional(readOnly = true)
    public LabStatistics getLabStatistics() {
        long totalLabs = labRepository.count();
        long publishedLabs = labRepository.countPublishedLabs();
        long draftLabs = totalLabs - publishedLabs;
        
        return new LabStatistics(totalLabs, publishedLabs, draftLabs);
    }

    /**
     * Statistiche lab per utente
     */
    @Transactional(readOnly = true)
    public UserLabStatistics getUserLabStatistics(User user) {
        long totalLabs = labRepository.countPublishedLabs();
        long completedLabs = userProgressRepository.countCompletedByUser(user);
        long inProgressLabs = userProgressRepository.countInProgressByUser(user);
        long notStartedLabs = totalLabs - completedLabs - inProgressLabs;
        
        Long totalTimeSpent = userProgressRepository.findTotalTimeSpentByUser(user);
        
        return new UserLabStatistics(totalLabs, completedLabs, inProgressLabs, 
                                    notStartedLabs, totalTimeSpent != null ? totalTimeSpent : 0);
    }

    /**
     * Dati analytics per admin
     */
    @Transactional(readOnly = true)
    public AnalyticsData getAnalyticsData() {
        long totalLabs = labRepository.count();
        long publishedLabs = labRepository.countPublishedLabs();
        long totalCompletions = getTotalCompletionsCount(); // Usa il metodo con fallback
        
        return new AnalyticsData(totalLabs, publishedLabs, totalCompletions);
    }

    /**
     * Inizia lab per utente
     */
    public UserProgress startLab(Long labId, User user) {
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new IllegalArgumentException("Lab not found"));
        
        if (!lab.getIsPublished()) {
            throw new IllegalArgumentException("Lab is not published");
        }
        
        // Controlla se già esiste progresso
        Optional<UserProgress> existingProgress = userProgressRepository.findByUserAndLab(user, lab);
        if (existingProgress.isPresent()) {
            UserProgress progress = existingProgress.get();
            progress.setLastAccessed(LocalDateTime.now());
            if (progress.isNotStarted()) {
                progress.startLab();
            }
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
    public UserProgress updateProgress(Long labId, User user, int percentage, 
                                     String notes, int timeSpent) {
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new IllegalArgumentException("Lab not found"));
        
        UserProgress progress = userProgressRepository.findByUserAndLab(user, lab)
                .orElseThrow(() -> new IllegalArgumentException("Progress not found"));
        
        progress.setCompletionPercentage(percentage);
        progress.addTimeSpent(timeSpent);
        progress.setLastAccessed(LocalDateTime.now());
        
        if (notes != null && !notes.trim().isEmpty()) {
            progress.setNotes(notes.trim());
        }
        
        // Se completato al 100%, aggiorna statistiche lab
        if (percentage >= 100 && !progress.isCompleted()) {
            progress.completeLab();
            lab.incrementCompletionCount();
            labRepository.save(lab);
        }
        
        return userProgressRepository.save(progress);
    }

    /**
     * Verifica se lab può essere modificato
     */
    @Transactional(readOnly = true)
    public boolean canEditLab(Lab lab, User user) {
        return user != null && user.isAdmin();
    }

    /**
     * Verifica se lab può essere eliminato
     */
    @Transactional(readOnly = true)
    public boolean canDeleteLab(Lab lab, User user) {
        return user != null && user.isAdmin();
    }

    // =============================================================================
    // DTO CLASSES
    // =============================================================================

    /**
     * DTO per statistiche lab generali
     */
    public static class LabStatistics {
        private final long totalLabs;
        private final long publishedLabs;
        private final long draftLabs;
        
        public LabStatistics(long totalLabs, long publishedLabs, long draftLabs) {
            this.totalLabs = totalLabs;
            this.publishedLabs = publishedLabs;
            this.draftLabs = draftLabs;
        }
        
        // Getters
        public long getTotalLabs() { return totalLabs; }
        public long getPublishedLabs() { return publishedLabs; }
        public long getDraftLabs() { return draftLabs; }
    }

    /**
     * DTO per statistiche lab per utente
     */
    public static class UserLabStatistics {
        private final long totalLabs;
        private final long completedLabs;
        private final long inProgressLabs;
        private final long notStartedLabs;
        private final long totalTimeSpent;
        
        public UserLabStatistics(long totalLabs, long completedLabs, long inProgressLabs, 
                               long notStartedLabs, long totalTimeSpent) {
            this.totalLabs = totalLabs;
            this.completedLabs = completedLabs;
            this.inProgressLabs = inProgressLabs;
            this.notStartedLabs = notStartedLabs;
            this.totalTimeSpent = totalTimeSpent;
        }
        
        // Getters
        public long getTotalLabs() { return totalLabs; }
        public long getCompletedLabs() { return completedLabs; }
        public long getInProgressLabs() { return inProgressLabs; }
        public long getNotStartedLabs() { return notStartedLabs; }
        public long getTotalTimeSpent() { return totalTimeSpent; }
        
        public double getCompletionPercentage() {
            return totalLabs > 0 ? (double) completedLabs / totalLabs * 100 : 0;
        }
    }

    /**
     * DTO per analytics
     */
    public static class AnalyticsData {
        private final long totalLabs;
        private final long publishedLabs;
        private final long totalCompletions;
        
        public AnalyticsData(long totalLabs, long publishedLabs, long totalCompletions) {
            this.totalLabs = totalLabs;
            this.publishedLabs = publishedLabs;
            this.totalCompletions = totalCompletions;
        }
        
        // Getters
        public long getTotalLabs() { return totalLabs; }
        public long getPublishedLabs() { return publishedLabs; }
        public long getTotalCompletions() { return totalCompletions; }
        
        public double getAverageCompletionsPerLab() {
            return publishedLabs > 0 ? (double) totalCompletions / publishedLabs : 0;
        }
    }
}