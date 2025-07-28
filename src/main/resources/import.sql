-- CyberLab Database Import Script
-- Basato sulla struttura che funzionava nel progetto precedente

-- Inserisce le categorie, UN COMANDO PER OGNI CATEGORIA
INSERT INTO category (id, name, description, icon) VALUES (1, 'XSS', 'Cross-Site Scripting vulnerabilities and techniques', '🕷️');
INSERT INTO category (id, name, description, icon) VALUES (2, 'SQL Injection', 'SQL Injection attacks and prevention', '💉');
INSERT INTO category (id, name, description, icon) VALUES (3, 'Path Traversal', 'Directory traversal and file inclusion attacks', '📁');
INSERT INTO category (id, name, description, icon) VALUES (4, 'Cryptography', 'Encryption, hashing and cryptographic techniques', '🔐');
INSERT INTO category (id, name, description, icon) VALUES (5, 'Binary Exploitation', 'Buffer overflows and binary analysis', '⚡');
INSERT INTO category (id, name, description, icon) VALUES (6, 'OSINT', 'Open Source Intelligence gathering tools', '🔍');
INSERT INTO category (id, name, description, icon) VALUES (7, 'DDoS Tools', 'Distributed Denial of Service techniques', '⚡');
INSERT INTO category (id, name, description, icon) VALUES (8, 'Data Leaks', 'Data breach analysis and prevention', '🔓');
INSERT INTO category (id, name, description, icon) VALUES (9, 'AI Security', 'Artificial Intelligence security tools', '🤖');
INSERT INTO category (id, name, description, icon) VALUES (10, 'CTF Writeups', 'Capture The Flag competition solutions', '🏆');

-- Inserisce l'utente admin nella tabella 'users'
-- Password: adminP@sswOrd! (hash BCrypt generato)
INSERT INTO users (id, username, password, role, name, surname, email, join_date, status) VALUES (1, 'admin', '$2a$10$hY2Sg/kFZYl.zU1jnerFzOKzQ4dvSRp3LXzerEM7.vjAB1hcKO4M.', 'ADMIN', 'Admin', 'CyberLab', 'admin@cyberlab.com', CURRENT_TIMESTAMP, 'ACTIVE');

-- Inserisce gli utenti normali, UN COMANDO PER OGNI UTENTE  
-- Password: userP@sswOrd! (hash BCrypt generato)
INSERT INTO users (id, username, password, role, name, surname, email, join_date, status) VALUES (2, 'alice_hacker', '$2a$10$1byHsAaEEwNXtIxqOE9aw.IgdyrRVwT8hMi9o1R1Vi0lO7LyNTs9S', 'USER', 'Alice', 'Security', 'alice@example.com', CURRENT_TIMESTAMP, 'ACTIVE');

-- Inserisce i laboratori, UN COMANDO PER OGNI LAB
INSERT INTO lab (id, title, description, category_id, theory, exercise, difficulty, created_by, created_date) VALUES (1, 'XSS Basics', 'Introduction to Cross-Site Scripting attacks', 1, 'XSS allows attackers to inject malicious scripts into web applications...', 'Try to execute alert() in the vulnerable form below', 'BEGINNER', 1, CURRENT_TIMESTAMP);
INSERT INTO lab (id, title, description, category_id, theory, exercise, difficulty, created_by, created_date) VALUES (2, 'SQL Injection Fundamentals', 'Basic SQL injection techniques', 2, 'SQL injection occurs when user input is not properly sanitized...', 'Bypass the login form using SQL injection', 'BEGINNER', 1, CURRENT_TIMESTAMP);
INSERT INTO lab (id, title, description, category_id, theory, exercise, difficulty, created_by, created_date) VALUES (3, 'Path Traversal Attack', 'Directory traversal vulnerabilities', 3, 'Path traversal attacks allow access to files outside the web root...', 'Access /etc/passwd using directory traversal', 'INTERMEDIATE', 1, CURRENT_TIMESTAMP);

-- Inserisce i post del forum, UN COMANDO PER OGNI POST
INSERT INTO post (id, title, content, category_id, author_id, created_date, last_modified, post_type) VALUES (1, 'Advanced XSS Payload Collection', 'Here are some advanced XSS payloads I have collected from various CTFs and real-world scenarios...', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SCRIPT');
INSERT INTO post (id, title, content, category_id, author_id, created_date, last_modified, post_type) VALUES (2, 'OSINT Tool: Sherlock', 'Great tool for username reconnaissance across social networks. Very useful for information gathering phase...', 6, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'TOOL');
INSERT INTO post (id, title, content, category_id, author_id, created_date, last_modified, post_type) VALUES (3, 'PicoCTF 2023 Binary Exploitation Writeup', 'Step-by-step solution for the buffer overflow challenge from PicoCTF 2023...', 5, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'WRITEUP');

-- Inserisce i commenti, UN COMANDO PER OGNI COMMENTO
INSERT INTO comment (id, content, post_id, author_id, created_date, is_reported, report_count) VALUES (1, 'Great collection! The DOM-based payloads are particularly useful for modern web applications.', 1, 3, CURRENT_TIMESTAMP, false, 0);
INSERT INTO comment (id, content, post_id, author_id, created_date, is_reported, report_count) VALUES (2, 'Thanks for sharing this tool, works perfectly! Already found several accounts linked to my target.', 2, 2, CURRENT_TIMESTAMP, false, 0);
INSERT INTO comment (id, content, post_id, author_id, created_date, is_reported, report_count) VALUES (3, 'Excellent writeup, very detailed explanation of the exploitation process.', 3, 2, CURRENT_TIMESTAMP, false, 0);

-- =========================================================================
-- ***** AGGIUNGI QUESTO BLOCCO ALLA FINE DELLO SCRIPT *****
-- Sincronizza i contatori degli ID per PostgreSQL dopo gli inserimenti manuali.
-- Questo comando dice al DB: "guarda l'ID più alto e fai partire il prossimo da lì".
SELECT setval(pg_get_serial_sequence('category', 'id'), COALESCE(MAX(id), 1)) FROM category;
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE(MAX(id), 1)) FROM users;
SELECT setval(pg_get_serial_sequence('lab', 'id'), COALESCE(MAX(id), 1)) FROM lab;
SELECT setval(pg_get_serial_sequence('post', 'id'), COALESCE(MAX(id), 1)) FROM post;
SELECT setval(pg_get_serial_sequence('comment', 'id'), COALESCE(MAX(id), 1)) FROM comment;
-- =========================================================================