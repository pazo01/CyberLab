package it.uniroma3.cyberlab.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @Column(unique = true, nullable = false, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    private String password; // BCrypt encrypted
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.USER;
    
    @Column(length = 100)
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
    
    @Column(length = 100)
    @Size(max = 100, message = "Surname must not exceed 100 characters")
    private String surname;
    
    @Column(name = "join_date", nullable = false)
    private LocalDateTime joinDate = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserStatus status = UserStatus.ACTIVE;
    
    @Column(name = "profile_info", columnDefinition = "TEXT")
    private String profileInfo;
    
    
    @Column(name = "avatar_url", length = 255)
    private String avatar;
    
    // Relationships
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();
    
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();
    
    @OneToMany(mappedBy = "reportedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Report> reports = new ArrayList<>();
    
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Lab> createdLabs = new ArrayList<>();
    
    // Enums
    public enum Role {
        USER, ADMIN
    }
    
    public enum UserStatus {
        ACTIVE, BANNED, SUSPENDED
    }
    
    // Constructors
    public User() {}
    
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSurname() {
        return surname;
    }
    
    public void setSurname(String surname) {
        this.surname = surname;
    }
    
    public LocalDateTime getJoinDate() {
        return joinDate;
    }
    
    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    
    public String getProfileInfo() {
        return profileInfo;
    }
    
    public void setProfileInfo(String profileInfo) {
        this.profileInfo = profileInfo;
    }
    
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public List<Post> getPosts() {
        return posts;
    }
    
    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
    
    public List<Comment> getComments() {
        return comments;
    }
    
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    
    public List<Report> getReports() {
        return reports;
    }
    
    public void setReports(List<Report> reports) {
        this.reports = reports;
    }
    
    public List<Lab> getCreatedLabs() {
        return createdLabs;
    }
    
    public void setCreatedLabs(List<Lab> createdLabs) {
        this.createdLabs = createdLabs;
    }
    
    // Helper methods
    public String getFullName() {
        if (name != null && surname != null) {
            return name + " " + surname;
        } else if (name != null) {
            return name;
        } else if (surname != null) {
            return surname;
        }
        return username;
    }
    
    

    
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
    
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
    
    // Override methods
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", status=" + status +
                '}';
    }
}