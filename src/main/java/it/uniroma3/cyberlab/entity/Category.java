package it.uniroma3.cyberlab.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "Category name is required")
    @Size(max = 50, message = "Category name must not exceed 50 characters")
    private String name;
    
    @Column(columnDefinition = "TEXT")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Column(length = 10)
    private String icon; // Emoji or icon class
    
    @Column(length = 7)
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color")
    private String color; // Hex color for UI
    
    // Relationships
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Lab> labs = new ArrayList<>();
    
    // Constructors
    public Category() {}
    
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public Category(String name, String description, String icon, String color) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.color = color;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public List<Post> getPosts() {
        return posts;
    }
    
    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
    
    public List<Lab> getLabs() {
        return labs;
    }
    
    public void setLabs(List<Lab> labs) {
        this.labs = labs;
    }
    
    // Helper methods
    public int getPostCount() {
        return posts != null ? posts.size() : 0;
    }
    
    public int getLabCount() {
        return labs != null ? labs.size() : 0;
    }
    
    // Override methods
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", icon='" + icon + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}