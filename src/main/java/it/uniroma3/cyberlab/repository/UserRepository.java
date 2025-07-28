// UserRepository.java
package it.uniroma3.cyberlab.repository;

import it.uniroma3.cyberlab.entity.User;
import it.uniroma3.cyberlab.entity.User.Role;
import it.uniroma3.cyberlab.entity.User.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Authentication queries
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    // Existence checks
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    
    // Role-based queries
    List<User> findByRole(Role role);
    List<User> findByRoleAndStatus(Role role, UserStatus status);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);
    
    // Status-based queries
    List<User> findByStatus(UserStatus status);
    List<User> findByStatusNot(UserStatus status);
    
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' ORDER BY u.joinDate DESC")
    List<User> findActiveUsersOrderByJoinDate();
    
    // Date-based queries
    List<User> findByJoinDateAfter(LocalDateTime date);
    List<User> findByJoinDateBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT u FROM User u WHERE u.joinDate >= :date AND u.status = 'ACTIVE'")
    List<User> findNewActiveUsers(@Param("date") LocalDateTime date);
    
    // Search queries
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.surname) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchUsers(@Param("search") String search);
    
    // Admin statistics
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'ACTIVE'")
    long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'BANNED'")
    long countBannedUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.joinDate >= :date")
    long countUsersJoinedAfter(@Param("date") LocalDateTime date);
}
