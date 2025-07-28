package it.uniroma3.cyberlab.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CyberLabPasswordGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Password che rispettano le regole di validazione
        String adminPassword = "adminP@sswOrd!";   // Admin password
        String userPassword = "userP@sswOrd!";     // User password
        
        System.out.println("=== CYBERLAB PASSWORD GENERATOR ===");
        System.out.println("Generating BCrypt hashes for CyberLab users...");
        System.out.println();
        
        // Genera hash per admin
        String adminHash = encoder.encode(adminPassword);
        System.out.println("ADMIN CREDENTIALS:");
        System.out.println("Username: admin");
        System.out.println("Password: " + adminPassword);
        System.out.println("BCrypt Hash: " + adminHash);
        System.out.println("Verification: " + encoder.matches(adminPassword, adminHash));
        System.out.println();
        
        // Genera hash per users
        String userHash = encoder.encode(userPassword);
        System.out.println("USER CREDENTIALS:");
        System.out.println("Username: alice_hacker, bob_pentester, carol_researcher");
        System.out.println("Password: " + userPassword);
        System.out.println("BCrypt Hash: " + userHash);
        System.out.println("Verification: " + encoder.matches(userPassword, userHash));
        System.out.println();
        
        // Testa l'hash del progetto precedente
        String oldHash = "$2a$10$XSxBbq9kd2XZYtDFpPKDaexY6Swa/0tLj.L5.T7AaIGngZM4LqiTC";
        System.out.println("=== TESTING OLD HASH ===");
        System.out.println("Old hash from previous project: " + oldHash);
        System.out.println("Testing with 'adminP@sswOrd!': " + encoder.matches("adminP@sswOrd!", oldHash));
        System.out.println();
        
        System.out.println("=== IMPORT.SQL UPDATE ===");
        System.out.println("Replace the password hashes in your import.sql:");
        System.out.println();
        System.out.println("-- Admin user");
        System.out.println("INSERT INTO users (id, username, password, role, name, surname, email, join_date, status)");
        System.out.println("VALUES (1, 'admin', '" + adminHash + "', 'ADMIN', 'Admin', 'CyberLab', 'admin@cyberlab.com', CURRENT_TIMESTAMP, 'ACTIVE');");
        System.out.println();
        System.out.println("-- Regular users");
        System.out.println("INSERT INTO users (id, username, password, role, name, surname, email, join_date, status)");
        System.out.println("VALUES (2, 'alice_hacker', '" + userHash + "', 'USER', 'Alice', 'Security', 'alice@example.com', CURRENT_TIMESTAMP, 'ACTIVE');");
        System.out.println();
        System.out.println("INSERT INTO users (id, username, password, role, name, surname, email, join_date, status)");
        System.out.println("VALUES (3, 'bob_pentester', '" + userHash + "', 'USER', 'Bob', 'Pentester', 'bob@example.com', CURRENT_TIMESTAMP, 'ACTIVE');");
        System.out.println();
        System.out.println("INSERT INTO users (id, username, password, role, name, surname, email, join_date, status)");
        System.out.println("VALUES (4, 'carol_researcher', '" + userHash + "', 'USER', 'Carol', 'Researcher', 'carol@example.com', CURRENT_TIMESTAMP, 'ACTIVE');");
        System.out.println();
        
        System.out.println("=== LOGIN CREDENTIALS ===");
        System.out.println("ADMIN LOGIN:");
        System.out.println("  Username: admin");
        System.out.println("  Password: adminP@sswOrd!");
        System.out.println();
        System.out.println("USER LOGIN:");
        System.out.println("  Username: alice_hacker (or any user)");
        System.out.println("  Password: userP@sswOrd!");
    }
}