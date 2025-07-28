// homepage.js - CyberLab Homepage JavaScript

/**
 * Initialize Homepage Features
 */
document.addEventListener('DOMContentLoaded', function() {
    initMatrixBackground();
    initTerminalTyping();
    initCounterAnimation();
    initScrollAnimations();
    initParallaxEffects();
    initGlitchEffects();
    initFloatingCode();
});

/**
 * Matrix Background Animation
 */
function initMatrixBackground() {
    const matrixCells = document.querySelectorAll('.matrix-cell');
    
    // Random activation of matrix cells
    function activateRandomCells() {
        const activeCells = Math.floor(Math.random() * 5) + 1;
        
        for (let i = 0; i < activeCells; i++) {
            const randomCell = matrixCells[Math.floor(Math.random() * matrixCells.length)];
            if (randomCell && !randomCell.classList.contains('active')) {
                randomCell.style.background = '#0f0';
                randomCell.style.boxShadow = '0 0 20px #0f0';
                randomCell.classList.add('active');
                
                setTimeout(() => {
                    randomCell.style.background = '#181818';
                    randomCell.style.boxShadow = 'none';
                    randomCell.classList.remove('active');
                }, 1000 + Math.random() * 2000);
            }
        }
    }
    
    // Start matrix animation
    setInterval(activateRandomCells, 200);
    
    // Mouse interaction
    matrixCells.forEach(cell => {
        cell.addEventListener('mouseenter', function() {
            this.style.background = '#0f0';
            this.style.boxShadow = '0 0 20px #0f0';
        });
        
        cell.addEventListener('mouseleave', function() {
            if (!this.classList.contains('active')) {
                this.style.background = '#181818';
                this.style.boxShadow = 'none';
            }
        });
    });
}

/**
 * Terminal Typing Animation
 */
function initTerminalTyping() {
    const typingElement = document.getElementById('typingText');
    const outputElement = document.getElementById('terminalOutput');
    
    if (!typingElement || !outputElement) return;
    
    const commands = [
        { 
            command: 'nmap -sS target.com',
            output: 'Starting Nmap scan...\nPORT     STATE SERVICE\n22/tcp   open  ssh\n80/tcp   open  http\n443/tcp  open  https'
        },
        {
            command: 'sqlmap -u "http://target.com?id=1"',
            output: 'Testing for SQL injection...\n[INFO] Testing \'MySQL >= 5.0 boolean-based blind\'\n[INFO] Parameter \'id\' is vulnerable'
        },
        {
            command: 'python3 exploit.py',
            output: 'Launching exploit...\n[+] Target is vulnerable!\n[+] Shell acquired: www-data@target'
        },
        {
            command: 'cat /etc/passwd',
            output: 'root:x:0:0:root:/root:/bin/bash\nwww-data:x:33:33:www-data:/var/www:/usr/sbin/nologin'
        }
    ];
    
    let currentCommandIndex = 0;
    
    function typeCommand(text, callback) {
        typingElement.textContent = '';
        let i = 0;
        
        const typing = setInterval(() => {
            if (i < text.length) {
                typingElement.textContent += text[i];
                i++;
                
                // Random typing sound
                if (Math.random() < 0.3) {
                    playTypingSound();
                }
            } else {
                clearInterval(typing);
                setTimeout(callback, 500);
            }
        }, 80 + Math.random() * 40);
    }
    
    function showOutput(text, callback) {
        const lines = text.split('\n');
        let lineIndex = 0;
        
        function showNextLine() {
            if (lineIndex < lines.length) {
                const line = document.createElement('div');
                line.textContent = lines[lineIndex];
                line.style.opacity = '0';
                line.style.animation = 'fadeIn 0.3s ease forwards';
                outputElement.appendChild(line);
                lineIndex++;
                setTimeout(showNextLine, 300);
            } else {
                setTimeout(callback, 2000);
            }
        }
        
        showNextLine();
    }
    
    function runTerminalSequence() {
        const currentCommand = commands[currentCommandIndex];
        
        typeCommand(currentCommand.command, () => {
            showOutput(currentCommand.output, () => {
                // Clear and move to next command
                typingElement.textContent = '';
                outputElement.innerHTML = '';
                currentCommandIndex = (currentCommandIndex + 1) % commands.length;
                
                setTimeout(runTerminalSequence, 1000);
            });
        });
    }
    
    // Start terminal animation
    setTimeout(runTerminalSequence, 2000);
}

/**
 * Counter Animation for Statistics
 */
function initCounterAnimation() {
    const counters = document.querySelectorAll('.stat-number');
    const observerOptions = {
        threshold: 0.5,
        rootMargin: '0px'
    };
    
    const counterObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                animateCounter(entry.target);
                counterObserver.unobserve(entry.target);
            }
        });
    }, observerOptions);
    
    counters.forEach(counter => {
        counterObserver.observe(counter);
    });
}

function animateCounter(element) {
    const target = parseInt(element.textContent.replace(/[^\d]/g, ''));
    const duration = 2000;
    const start = performance.now();
    const originalText = element.textContent;
    
    function updateCounter(currentTime) {
        const elapsed = currentTime - start;
        const progress = Math.min(elapsed / duration, 1);
        
        // Easing function
        const easeOut = 1 - Math.pow(1 - progress, 3);
        const current = Math.floor(easeOut * target);
        
        // Format number with commas
        let formattedNumber = current.toLocaleString();
        
        // Handle special cases like "1,337" and "4,200"
        if (originalText.includes('k')) {
            formattedNumber = (current / 1000).toFixed(1) + 'k';
        }
        
        element.textContent = formattedNumber;
        
        if (progress < 1) {
            requestAnimationFrame(updateCounter);
        } else {
            element.textContent = originalText; // Restore original formatting
        }
    }
    
    requestAnimationFrame(updateCounter);
}

/**
 * Scroll Animations
 */
function initScrollAnimations() {
    const animatedElements = document.querySelectorAll('.feature-card, .lab-card, .post-card');
    
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '50px'
    };
    
    const scrollObserver = new IntersectionObserver((entries) => {
        entries.forEach((entry, index) => {
            if (entry.isIntersecting) {
                setTimeout(() => {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }, index * 100);
                scrollObserver.unobserve(entry.target);
            }
        });
    }, observerOptions);
    
    animatedElements.forEach(element => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(30px)';
        element.style.transition = 'all 0.6s ease';
        scrollObserver.observe(element);
    });
}

/**
 * Parallax Effects
 */
function initParallaxEffects() {
    window.addEventListener('scroll', () => {
        const scrolled = window.pageYOffset;
        const rate = scrolled * -0.5;
        
        // Parallax for floating code elements
        const floatingElements = document.querySelectorAll('.code-snippet');
        floatingElements.forEach((element, index) => {
            const speed = 0.2 + (index * 0.1);
            element.style.transform = `translateY(${scrolled * speed}px)`;
        });
        
        // Parallax for hero background
        const heroSection = document.querySelector('.hero');
        if (heroSection) {
            heroSection.style.transform = `translateY(${rate}px)`;
        }
    });
}

/**
 * Glitch Effects
 */
function initGlitchEffects() {
    const glitchElements = document.querySelectorAll('.glitch-text');
    
    glitchElements.forEach(element => {
        // Random glitch activation
        setInterval(() => {
            if (Math.random() < 0.1) {
                element.classList.add('glitch-active');
                setTimeout(() => {
                    element.classList.remove('glitch-active');
                }, 200);
            }
        }, 2000);
        
        // Mouse hover glitch
        element.addEventListener('mouseenter', function() {
            this.classList.add('glitch-active');
        });
        
        element.addEventListener('mouseleave', function() {
            this.classList.remove('glitch-active');
        });
    });
}

/**
 * Floating Code Animation
 */
function initFloatingCode() {
    const codeContainer = document.querySelector('.floating-code-bg');
    if (!codeContainer) return;
    
    const codeSnippets = [
        '<script>alert("XSS")</script>',
        'SELECT * FROM users WHERE id=1 OR 1=1',
        '../../../etc/passwd',
        '0x41414141',
        'import requests',
        'nmap -sS target.com',
        'john --wordlist=rockyou.txt hash.txt',
        'nc -lvp 4444',
        'python3 -m http.server 8000',
        'curl -X POST -d "cmd=ls" target.com/shell.php'
    ];
    
    function createFloatingCode() {
        const snippet = document.createElement('div');
        snippet.className = 'code-snippet';
        snippet.textContent = codeSnippets[Math.floor(Math.random() * codeSnippets.length)];
        
        // Random position
        snippet.style.left = Math.random() * 100 + '%';
        snippet.style.top = '100%';
        snippet.style.animationDelay = Math.random() * 2 + 's';
        snippet.style.fontSize = (0.7 + Math.random() * 0.3) + 'rem';
        
        codeContainer.appendChild(snippet);
        
        // Remove after animation
        setTimeout(() => {
            if (snippet.parentNode) {
                snippet.parentNode.removeChild(snippet);
            }
        }, 15000);
    }
    
    // Create floating code periodically
    setInterval(createFloatingCode, 3000);
}

/**
 * Typing Sound Effect
 */
function playTypingSound() {
    try {
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioContext.createOscillator();
        const gainNode = audioContext.createGain();
        
        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);
        
        oscillator.frequency.setValueAtTime(800 + Math.random() * 400, audioContext.currentTime);
        oscillator.type = 'square';
        
        gainNode.gain.setValueAtTime(0.005, audioContext.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.001, audioContext.currentTime + 0.05);
        
        oscillator.start(audioContext.currentTime);
        oscillator.stop(audioContext.currentTime + 0.05);
    } catch (e) {
        // Fallback for browsers without Web Audio API
    }
}

/**
 * Navigation Enhancements
 */
function initNavigation() {
    const navbar = document.querySelector('.navbar');
    
    // Navbar scroll effect
    let lastScrollTop = 0;
    window.addEventListener('scroll', () => {
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        
        if (scrollTop > 100) {
            navbar.classList.add('navbar-scrolled');
        } else {
            navbar.classList.remove('navbar-scrolled');
        }
        
        // Hide/show navbar on scroll
        if (scrollTop > lastScrollTop && scrollTop > 200) {
            navbar.style.transform = 'translateY(-100%)';
        } else {
            navbar.style.transform = 'translateY(0)';
        }
        
        lastScrollTop = scrollTop;
    });
    
    // Active link highlighting
    const navLinks = document.querySelectorAll('.nav-link');
    const sections = document.querySelectorAll('section[id]');
    
    window.addEventListener('scroll', () => {
        const current = window.pageYOffset;
        
        sections.forEach(section => {
            const sectionTop = section.offsetTop - 150;
            const sectionHeight = section.clientHeight;
            const sectionId = section.getAttribute('id');
            
            if (current >= sectionTop && current < sectionTop + sectionHeight) {
                navLinks.forEach(link => {
                    link.classList.remove('active');
                    if (link.getAttribute('href') === `#${sectionId}`) {
                        link.classList.add('active');
                    }
                });
            }
        });
    });
}

/**
 * Button Hover Effects
 */
function initButtonEffects() {
    const buttons = document.querySelectorAll('.btn');
    
    buttons.forEach(button => {
        button.addEventListener('mouseenter', function() {
            if (this.classList.contains('btn-primary')) {
                this.style.boxShadow = '0 0 25px rgba(0, 255, 0, 0.5)';
            }
        });
        
        button.addEventListener('mouseleave', function() {
            this.style.boxShadow = '';
        });
        
        // Click ripple effect
        button.addEventListener('click', function(e) {
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;
            
            ripple.style.cssText = `
                position: absolute;
                width: ${size}px;
                height: ${size}px;
                left: ${x}px;
                top: ${y}px;
                background: rgba(255, 255, 255, 0.3);
                border-radius: 50%;
                transform: scale(0);
                animation: ripple 0.6s ease-out;
                pointer-events: none;
            `;
            
            this.style.position = 'relative';
            this.style.overflow = 'hidden';
            this.appendChild(ripple);
            
            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });
}

/**
 * Progress Bar Animations
 */
function initProgressBars() {
    const progressBars = document.querySelectorAll('.progress-fill');
    
    const progressObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const progressBar = entry.target;
                const targetWidth = progressBar.getAttribute('data-progress') || '0%';
                
                setTimeout(() => {
                    progressBar.style.width = targetWidth;
                }, 200);
                
                progressObserver.unobserve(progressBar);
            }
        });
    }, { threshold: 0.5 });
    
    progressBars.forEach(bar => {
        progressObserver.observe(bar);
    });
}

/**
 * Card Hover Effects
 */
function initCardEffects() {
    const cards = document.querySelectorAll('.feature-card, .lab-card, .post-card');
    
    cards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            // Add glow effect
            this.style.filter = 'brightness(1.1)';
            
            // Animate child elements
            const icon = this.querySelector('.feature-icon');
            if (icon) {
                icon.style.transform = 'scale(1.1) rotate(5deg)';
            }
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.filter = '';
            
            const icon = this.querySelector('.feature-icon');
            if (icon) {
                icon.style.transform = '';
            }
        });
        
        // 3D tilt effect
        card.addEventListener('mousemove', function(e) {
            const rect = this.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            const centerX = rect.width / 2;
            const centerY = rect.height / 2;
            
            const rotateX = (y - centerY) / 10;
            const rotateY = (centerX - x) / 10;
            
            this.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateZ(10px)`;
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = '';
        });
    });
}

/**
 * Text Scramble Effect
 */
function initTextScramble() {
    const scrambleElements = document.querySelectorAll('[data-scramble]');
    
    scrambleElements.forEach(element => {
        const originalText = element.textContent;
        
        element.addEventListener('mouseenter', function() {
            scrambleText(this, originalText);
        });
    });
}

function scrambleText(element, finalText) {
    const chars = '!@#$%^&*()_+-=[]{}|;:,.<>?ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    const length = finalText.length;
    let iterations = 0;
    
    const interval = setInterval(() => {
        element.textContent = finalText
            .split('')
            .map((char, index) => {
                if (index < iterations) {
                    return finalText[index];
                }
                return chars[Math.floor(Math.random() * chars.length)];
            })
            .join('');
        
        if (iterations >= length) {
            clearInterval(interval);
        }
        
        iterations += 1 / 3;
    }, 30);
}

/**
 * Loading States
 */
function initLoadingStates() {
    const loadingButtons = document.querySelectorAll('[data-loading]');
    
    loadingButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            if (this.classList.contains('loading')) {
                e.preventDefault();
                return;
            }
            
            this.classList.add('loading');
            this.disabled = true;
            
            // Simulate loading
            setTimeout(() => {
                this.classList.remove('loading');
                this.disabled = false;
            }, 2000);
        });
    });
}

/**
 * Smooth Scrolling
 */
function initSmoothScrolling() {
    const scrollLinks = document.querySelectorAll('a[href^="#"]');
    
    scrollLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            const targetId = this.getAttribute('href');
            const targetSection = document.querySelector(targetId);
            
            if (targetSection) {
                const offsetTop = targetSection.getBoundingClientRect().top + window.pageYOffset - 80;
                
                window.scrollTo({
                    top: offsetTop,
                    behavior: 'smooth'
                });
            }
        });
    });
}

/**
 * Terminal Commands with Real Output
 */
function initAdvancedTerminal() {
    const terminal = document.querySelector('.terminal-body');
    if (!terminal) return;
    
    const commands = [
        {
            command: 'whoami',
            output: 'elite_hacker'
        },
        {
            command: 'ls -la /home/elite_hacker',
            output: 'drwxr-xr-x  2 elite_hacker users  4096 Dec 15 10:30 .\ndrwxr-xr-x  3 root         root   4096 Dec 14 09:15 ..\n-rw-r--r--  1 elite_hacker users   220 Dec 14 09:15 .bash_logout\n-rw-r--r--  1 elite_hacker users  3771 Dec 14 09:15 .bashrc\n-rwxr-xr-x  1 elite_hacker users  8192 Dec 15 10:30 exploit.py\n-rw-r--r--  1 elite_hacker users  1024 Dec 15 10:25 targets.txt'
        },
        {
            command: 'cat exploit.py | head -10',
            output: '#!/usr/bin/env python3\n# Advanced SQL Injection Exploit\n# Target: vulnerable web application\n\nimport requests\nimport sys\nfrom urllib.parse import urlencode\n\nclass SQLiExploit:\n    def __init__(self, target_url):'
        },
        {
            command: 'nmap -Pn -sS target.cyberlab.com',
            output: 'Starting Nmap 7.94 scan...\nNmap scan report for target.cyberlab.com\nHost is up (0.032s latency).\n\nPORT     STATE SERVICE\n22/tcp   open  ssh\n80/tcp   open  http\n443/tcp  open  https\n3306/tcp open  mysql\n\nNmap done: 1 IP address scanned'
        }
    ];
    
    let commandIndex = 0;
    let isTyping = false;
    
    function executeCommand() {
        if (isTyping) return;
        isTyping = true;
        
        const cmd = commands[commandIndex];
        commandIndex = (commandIndex + 1) % commands.length;
        
        // Clear terminal
        setTimeout(() => {
            terminal.innerHTML = '<div class="terminal-line"><span class="prompt">elite_hacker@cyberlab:~$</span> <span class="command" id="currentCommand"></span><span class="cursor">█</span></div>';
            
            typeCommand(cmd.command, () => {
                setTimeout(() => {
                    showCommandOutput(cmd.output, () => {
                        isTyping = false;
                        setTimeout(executeCommand, 3000);
                    });
                }, 1000);
            });
        }, 500);
    }
    
    function typeCommand(command, callback) {
        const commandElement = document.getElementById('currentCommand');
        if (!commandElement) return;
        
        let i = 0;
        const typing = setInterval(() => {
            if (i < command.length) {
                commandElement.textContent += command[i];
                i++;
                
                // Random typing sound
                if (Math.random() < 0.4) {
                    playTypingSound();
                }
            } else {
                clearInterval(typing);
                // Remove cursor
                const cursor = terminal.querySelector('.cursor');
                if (cursor) cursor.remove();
                callback();
            }
        }, 50 + Math.random() * 100);
    }
    
    function showCommandOutput(output, callback) {
        const lines = output.split('\n');
        let lineIndex = 0;
        
        function showNextLine() {
            if (lineIndex < lines.length) {
                const line = document.createElement('div');
                line.className = 'output-line';
                line.textContent = lines[lineIndex];
                line.style.color = '#00ff41';
                line.style.opacity = '0';
                line.style.animation = 'fadeIn 0.2s ease forwards';
                terminal.appendChild(line);
                
                lineIndex++;
                setTimeout(showNextLine, 100);
            } else {
                callback();
            }
        }
        
        showNextLine();
    }
    
    // Start terminal simulation
    executeCommand();
}

/**
 * Performance Monitoring
 */
function initPerformanceMonitoring() {
    // Monitor page load performance
    window.addEventListener('load', () => {
        const perfData = performance.timing;
        const pageLoadTime = perfData.loadEventEnd - perfData.navigationStart;
        
        console.log(`%c⚡ CyberLab loaded in ${pageLoadTime}ms`, 'color: #0f0; font-weight: bold;');
        
        // Track animations performance
        let frameCount = 0;
        let lastTime = performance.now();
        
        function countFrames(currentTime) {
            frameCount++;
            
            if (currentTime - lastTime >= 1000) {
                console.log(`%c🎬 FPS: ${frameCount}`, 'color: #0f0;');
                frameCount = 0;
                lastTime = currentTime;
            }
            
            requestAnimationFrame(countFrames);
        }
        
        requestAnimationFrame(countFrames);
    });
}

/**
 * Add CSS for additional animations
 */
function addAdditionalStyles() {
    const style = document.createElement('style');
    style.textContent = `
        @keyframes ripple {
            to {
                transform: scale(2);
                opacity: 0;
            }
        }
        
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
        }
        
        .navbar-scrolled {
            background: rgba(17, 17, 17, 0.98) !important;
            backdrop-filter: blur(20px);
        }
        
        .glitch-active {
            animation: glitch-shake 0.2s ease-in-out;
        }
        
        @keyframes glitch-shake {
            0%, 100% { transform: translate(0); }
            25% { transform: translate(-2px, 2px); }
            50% { transform: translate(2px, -2px); }
            75% { transform: translate(-1px, -1px); }
        }
        
        .loading {
            position: relative;
            color: transparent !important;
        }
        
        .loading::after {
            content: '';
            position: absolute;
            top: 50%;
            left: 50%;
            width: 20px;
            height: 20px;
            margin: -10px 0 0 -10px;
            border: 2px solid transparent;
            border-top: 2px solid #0f0;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }
        
        .output-line {
            font-family: 'Fira Code', monospace;
            font-size: 0.85rem;
            line-height: 1.4;
            margin: 2px 0;
        }
        
        .terminal-line .prompt {
            color: #00ff41;
            font-weight: 600;
        }
        
        .terminal-line .command {
            color: #ffffff;
            margin-left: 0.5rem;
        }
    `;
    document.head.appendChild(style);
}

/**
 * Initialize all homepage features
 */
function initHomepage() {
    addAdditionalStyles();
    initNavigation();
    initButtonEffects();
    initProgressBars();
    initCardEffects();
    initTextScramble();
    initLoadingStates();
    initSmoothScrolling();
    initAdvancedTerminal();
    initPerformanceMonitoring();
    
    console.log('%c🚀 CyberLab Homepage Initialized', 'color: #0f0; font-size: 16px; font-weight: bold;');
    console.log('%c⚡ Matrix protocols activated', 'color: #0f0;');
    console.log('%c🔒 Security systems online', 'color: #0f0;');
}

// Initialize when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initHomepage);
} else {
    initHomepage();
}