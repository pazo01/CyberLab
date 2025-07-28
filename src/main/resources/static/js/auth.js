// auth.js - CyberLab Authentication JavaScript

/**
 * Initialize Matrix Background Animation
 */
function initMatrixBackground() {
    const matrixCells = document.querySelectorAll('.matrix-cell');
    
    // Random hover effect
    setInterval(() => {
        const randomCell = matrixCells[Math.floor(Math.random() * matrixCells.length)];
        if (randomCell) {
            randomCell.style.background = '#0f0';
            randomCell.style.boxShadow = '0 0 20px #0f0';
            
            setTimeout(() => {
                randomCell.style.background = '#181818';
                randomCell.style.boxShadow = 'none';
            }, 500);
        }
    }, 100);
    
    // Matrix digital rain effect
    createDigitalRain();
}

/**
 * Create Digital Rain Effect
 */
function createDigitalRain() {
    const chars = '01アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲン';
    const container = document.querySelector('.matrix-background');
    
    if (!container) return;
    
    for (let i = 0; i < 20; i++) {
        createRainColumn(container, chars);
    }
}

/**
 * Create a single rain column
 */
function createRainColumn(container, chars) {
    const column = document.createElement('div');
    column.style.cssText = `
        position: absolute;
        top: -100%;
        left: ${Math.random() * 100}%;
        width: 20px;
        height: 100%;
        font-family: 'Fira Code', monospace;
        font-size: 14px;
        color: rgba(0, 255, 0, 0.7);
        z-index: 1;
        animation: fall ${5 + Math.random() * 10}s linear infinite;
        animation-delay: ${Math.random() * 5}s;
    `;
    
    // Add characters to column
    for (let i = 0; i < 20; i++) {
        const char = document.createElement('div');
        char.textContent = chars[Math.floor(Math.random() * chars.length)];
        char.style.opacity = Math.random();
        column.appendChild(char);
    }
    
    container.appendChild(column);
    
    // Change characters periodically
    setInterval(() => {
        const charElements = column.children;
        for (let char of charElements) {
            if (Math.random() < 0.1) {
                char.textContent = chars[Math.floor(Math.random() * chars.length)];
            }
        }
    }, 100);
}

// Add CSS for falling animation
const style = document.createElement('style');
style.textContent = `
    @keyframes fall {
        to {
            transform: translateY(100vh);
        }
    }
`;
document.head.appendChild(style);

/**
 * Enhanced Form Input Animation
 */
function enhanceFormInputs() {
    const inputs = document.querySelectorAll('.input-box input');
    
    inputs.forEach(input => {
        // Focus effects
        input.addEventListener('focus', function() {
            this.parentElement.classList.add('focused');
            playTypingSound();
        });
        
        input.addEventListener('blur', function() {
            this.parentElement.classList.remove('focused');
            if (!this.value) {
                this.parentElement.classList.remove('filled');
            } else {
                this.parentElement.classList.add('filled');
            }
        });
        
        // Typing effects
        input.addEventListener('input', function() {
            if (Math.random() < 0.3) playTypingSound();
            
            // Real-time validation feedback
            validateField(this);
        });
        
        // Enter key handling
        input.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                const form = this.closest('form');
                const inputs = Array.from(form.querySelectorAll('input[required]'));
                const currentIndex = inputs.indexOf(this);
                
                if (currentIndex < inputs.length - 1) {
                    e.preventDefault();
                    inputs[currentIndex + 1].focus();
                }
            }
        });
    });
}

/**
 * Play typing sound effect
 */
function playTypingSound() {
    // Create audio context for typing sounds
    const audioContext = new (window.AudioContext || window.webkitAudioContext)();
    const oscillator = audioContext.createOscillator();
    const gainNode = audioContext.createGain();
    
    oscillator.connect(gainNode);
    gainNode.connect(audioContext.destination);
    
    oscillator.frequency.setValueAtTime(800 + Math.random() * 200, audioContext.currentTime);
    oscillator.type = 'square';
    
    gainNode.gain.setValueAtTime(0.01, audioContext.currentTime);
    gainNode.gain.exponentialRampToValueAtTime(0.001, audioContext.currentTime + 0.1);
    
    oscillator.start(audioContext.currentTime);
    oscillator.stop(audioContext.currentTime + 0.1);
}

/**
 * Password Strength Checker
 */
function initPasswordStrength() {
    const passwordInput = document.getElementById('password');
    const strengthBars = document.querySelectorAll('.strength-bar');
    const requirements = {
        length: document.getElementById('length'),
        uppercase: document.getElementById('uppercase'),
        lowercase: document.getElementById('lowercase'),
        number: document.getElementById('number')
    };
    
    if (!passwordInput) return;
    
    passwordInput.addEventListener('input', function() {
        const password = this.value;
        const strength = calculatePasswordStrength(password);
        
        // Update strength bars
        strengthBars.forEach((bar, index) => {
            bar.className = 'strength-bar';
            if (index < strength.score) {
                bar.classList.add(strength.level);
            }
        });
        
        // Update requirements
        updateRequirement(requirements.length, password.length >= 8);
        updateRequirement(requirements.uppercase, /[A-Z]/.test(password));
        updateRequirement(requirements.lowercase, /[a-z]/.test(password));
        updateRequirement(requirements.number, /\d/.test(password));
    });
}

/**
 * Calculate password strength
 */
function calculatePasswordStrength(password) {
    let score = 0;
    let level = 'weak';
    
    if (password.length >= 8) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[a-z]/.test(password)) score++;
    if (/\d/.test(password)) score++;
    if (/[^A-Za-z\d]/.test(password)) score++;
    
    if (score >= 4) level = 'strong';
    else if (score >= 2) level = 'medium';
    
    return { score, level };
}

/**
 * Update requirement status
 */
function updateRequirement(element, met) {
    if (!element) return;
    
    if (met) {
        element.classList.add('met');
    } else {
        element.classList.remove('met');
    }
}

/**
 * Real-time validation
 */
function initRealTimeValidation() {
    const usernameInput = document.querySelector('input[name="username"]');
    const emailInput = document.querySelector('input[name="email"]');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const passwordInput = document.getElementById('password');
    
    // Username validation
    if (usernameInput) {
        let usernameTimeout;
        usernameInput.addEventListener('input', function() {
            clearTimeout(usernameTimeout);
            usernameTimeout = setTimeout(() => {
                checkUsernameAvailability(this.value);
            }, 500);
        });
    }
    
    // Email validation
    if (emailInput) {
        let emailTimeout;
        emailInput.addEventListener('input', function() {
            clearTimeout(emailTimeout);
            emailTimeout = setTimeout(() => {
                checkEmailAvailability(this.value);
            }, 500);
        });
    }
    
    // Password confirmation
    if (confirmPasswordInput && passwordInput) {
        confirmPasswordInput.addEventListener('input', function() {
            const password = passwordInput.value;
            const confirmPassword = this.value;
            
            if (confirmPassword && password !== confirmPassword) {
                this.setCustomValidity('Passwords do not match');
                showFieldError(this, 'Passwords do not match');
            } else {
                this.setCustomValidity('');
                hideFieldError(this);
            }
        });
    }
}

/**
 * Check username availability
 */
async function checkUsernameAvailability(username) {
    if (username.length < 3) return;
    
    try {
        const response = await fetch(`/check-username?username=${encodeURIComponent(username)}`);
        const available = await response.json();
        
        const input = document.querySelector('input[name="username"]');
        if (!available) {
            showFieldError(input, 'Username already taken');
        } else {
            hideFieldError(input);
            showFieldSuccess(input, 'Username available');
        }
    } catch (error) {
        console.error('Username check failed:', error);
    }
}

/**
 * Check email availability
 */
async function checkEmailAvailability(email) {
    if (!email.includes('@')) return;
    
    try {
        const response = await fetch(`/check-email?email=${encodeURIComponent(email)}`);
        const available = await response.json();
        
        const input = document.querySelector('input[name="email"]');
        if (!available) {
            showFieldError(input, 'Email already registered');
        } else {
            hideFieldError(input);
            showFieldSuccess(input, 'Email available');
        }
    } catch (error) {
        console.error('Email check failed:', error);
    }
}

/**
 * Show field error
 */
function showFieldError(input, message) {
    hideFieldMessages(input);
    
    const errorDiv = document.createElement('div');
    errorDiv.className = 'field-error';
    errorDiv.innerHTML = `<span>${message}</span>`;
    
    input.parentElement.appendChild(errorDiv);
    input.classList.add('error');
}

/**
 * Show field success
 */
function showFieldSuccess(input, message) {
    hideFieldMessages(input);
    
    const successDiv = document.createElement('div');
    successDiv.className = 'field-success';
    successDiv.innerHTML = `<span>${message}</span>`;
    
    input.parentElement.appendChild(successDiv);
    input.classList.add('success');
    input.classList.remove('error');
}

/**
 * Hide field messages
 */
function hideFieldMessages(input) {
    const parent = input.parentElement;
    const existingError = parent.querySelector('.field-error');
    const existingSuccess = parent.querySelector('.field-success');
    
    if (existingError) existingError.remove();
    if (existingSuccess) existingSuccess.remove();
    
    input.classList.remove('error', 'success');
}

/**
 * Hide field error
 */
function hideFieldError(input) {
    const parent = input.parentElement;
    const existingError = parent.querySelector('.field-error');
    if (existingError) existingError.remove();
    input.classList.remove('error');
}

/**
 * Validate individual field
 */
function validateField(input) {
    const value = input.value;
    const type = input.type;
    const name = input.name;
    
    // Clear previous validation
    input.setCustomValidity('');
    
    switch (name) {
        case 'username':
            if (value.length > 0 && value.length < 3) {
                input.setCustomValidity('Username must be at least 3 characters');
            } else if (!/^[a-zA-Z0-9_]+$/.test(value) && value.length > 0) {
                input.setCustomValidity('Username can only contain letters, numbers, and underscores');
            }
            break;
            
        case 'email':
            if (value.length > 0 && !value.includes('@')) {
                input.setCustomValidity('Please enter a valid email address');
            }
            break;
            
        case 'password':
            if (value.length > 0 && value.length < 8) {
                input.setCustomValidity('Password must be at least 8 characters');
            }
            break;
    }
}

/**
 * Form submission handling
 */
function initFormSubmission() {
    const forms = document.querySelectorAll('.auth-form');
    
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const submitButton = form.querySelector('.btn-auth');
            
            // Add loading state
            submitButton.classList.add('loading');
            submitButton.disabled = true;
            
            // Validate form before submission
            if (!validateForm(form)) {
                e.preventDefault();
                submitButton.classList.remove('loading');
                submitButton.disabled = false;
                return;
            }
            
            // Show submission feedback
            showSubmissionFeedback();
        });
    });
}

/**
 * Validate entire form
 */
function validateForm(form) {
    const inputs = form.querySelectorAll('input[required]');
    let isValid = true;
    
    inputs.forEach(input => {
        if (!input.checkValidity()) {
            isValid = false;
            showFieldError(input, input.validationMessage);
        }
    });
    
    // Check password confirmation
    const password = form.querySelector('input[name="password"]');
    const confirmPassword = form.querySelector('input[name="confirmPassword"]');
    
    if (password && confirmPassword && password.value !== confirmPassword.value) {
        isValid = false;
        showFieldError(confirmPassword, 'Passwords do not match');
    }
    
    // Check terms agreement
    const termsCheckbox = form.querySelector('#terms');
    if (termsCheckbox && !termsCheckbox.checked) {
        isValid = false;
        showAlert('Please agree to the Terms of Service and Privacy Policy', 'error');
    }
    
    return isValid;
}

/**
 * Show submission feedback
 */
function showSubmissionFeedback() {
    // Create and show loading overlay
    const overlay = document.createElement('div');
    overlay.className = 'submission-overlay';
    overlay.innerHTML = `
        <div class="submission-content">
            <div class="loading-spinner"></div>
            <p>Processing your request...</p>
            <div class="security-scan">
                <div class="scan-line"></div>
                <p>Performing security validation</p>
            </div>
        </div>
    `;
    
    document.body.appendChild(overlay);
}

/**
 * Show alert message
 */
function showAlert(message, type = 'info') {
    // Remove existing alerts
    const existingAlerts = document.querySelectorAll('.dynamic-alert');
    existingAlerts.forEach(alert => alert.remove());
    
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} dynamic-alert`;
    alert.innerHTML = `
        <i class="icon-${type === 'error' ? 'alert' : type === 'success' ? 'check' : 'info'}"></i>
        <span>${message}</span>
        <button class="alert-close" onclick="this.parentElement.remove()">×</button>
    `;
    
    const form = document.querySelector('.auth-form');
    form.insertBefore(alert, form.firstChild);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (alert.parentElement) {
            alert.remove();
        }
    }, 5000);
}

/**
 * Keyboard shortcuts
 */
function initKeyboardShortcuts() {
    document.addEventListener('keydown', function(e) {
        // Escape to close alerts
        if (e.key === 'Escape') {
            const alerts = document.querySelectorAll('.dynamic-alert');
            alerts.forEach(alert => alert.remove());
        }
        
        // Ctrl/Cmd + Enter to submit form
        if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
            const submitButton = document.querySelector('.btn-auth');
            if (submitButton && !submitButton.disabled) {
                submitButton.click();
            }
        }
    });
}

/**
 * Add glitch effect to elements
 */
function addGlitchEffect() {
    const glitchElements = document.querySelectorAll('.auth-header h2');
    
    glitchElements.forEach(element => {
        element.addEventListener('mouseenter', function() {
            this.classList.add('glitch');
            setTimeout(() => {
                this.classList.remove('glitch');
            }, 500);
        });
    });
}

/**
 * Initialize particle system
 */
function initParticleSystem() {
    const canvas = document.createElement('canvas');
    canvas.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        pointer-events: none;
        z-index: 1;
        opacity: 0.3;
    `;
    
    document.body.appendChild(canvas);
    
    const ctx = canvas.getContext('2d');
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    
    const particles = [];
    
    // Create particles
    for (let i = 0; i < 50; i++) {
        particles.push({
            x: Math.random() * canvas.width,
            y: Math.random() * canvas.height,
            vx: (Math.random() - 0.5) * 0.5,
            vy: (Math.random() - 0.5) * 0.5,
            size: Math.random() * 2 + 1,
            opacity: Math.random() * 0.5 + 0.2
        });
    }
    
    function animateParticles() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        
        particles.forEach(particle => {
            // Update position
            particle.x += particle.vx;
            particle.y += particle.vy;
            
            // Wrap around edges
            if (particle.x < 0) particle.x = canvas.width;
            if (particle.x > canvas.width) particle.x = 0;
            if (particle.y < 0) particle.y = canvas.height;
            if (particle.y > canvas.height) particle.y = 0;
            
            // Draw particle
            ctx.fillStyle = `rgba(0, 255, 0, ${particle.opacity})`;
            ctx.beginPath();
            ctx.arc(particle.x, particle.y, particle.size, 0, Math.PI * 2);
            ctx.fill();
        });
        
        requestAnimationFrame(animateParticles);
    }
    
    animateParticles();
    
    // Resize handler
    window.addEventListener('resize', () => {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
    });
}

/**
 * Terminal-like input effect
 */
function initTerminalEffect() {
    const inputs = document.querySelectorAll('.input-box input');
    
    inputs.forEach(input => {
        const originalPlaceholder = input.placeholder;
        
        input.addEventListener('focus', function() {
            if (!this.value) {
                typewriterEffect(this, '█', 500);
            }
        });
        
        input.addEventListener('blur', function() {
            // Remove cursor if empty
            if (!this.value) {
                this.value = '';
            }
        });
    });
}

/**
 * Typewriter effect
 */
function typewriterEffect(element, text, duration) {
    element.value = '';
    let index = 0;
    
    const interval = setInterval(() => {
        if (index < text.length) {
            element.value += text[index];
            index++;
        } else {
            clearInterval(interval);
            setTimeout(() => {
                element.value = '';
            }, duration);
        }
    }, 100);
}

/**
 * Security scanning animation
 */
function initSecurityScan() {
    const authContainer = document.querySelector('.auth-container');
    
    if (authContainer) {
        const scanLine = document.createElement('div');
        scanLine.className = 'security-scan-line';
        scanLine.style.cssText = `
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 2px;
            background: linear-gradient(90deg, transparent, #0f0, transparent);
            animation: securityScan 3s ease-in-out infinite;
            opacity: 0.7;
        `;
        
        authContainer.appendChild(scanLine);
    }
}

// Add CSS animations for effects
const additionalStyles = document.createElement('style');
additionalStyles.textContent = `
    .glitch {
        animation: glitch 0.5s ease-in-out;
    }
    
    @keyframes glitch {
        0%, 100% { transform: translate(0); }
        20% { transform: translate(-2px, 2px); }
        40% { transform: translate(-2px, -2px); }
        60% { transform: translate(2px, 2px); }
        80% { transform: translate(2px, -2px); }
    }
    
    @keyframes securityScan {
        0% { transform: translateY(-100%); opacity: 0; }
        50% { opacity: 1; }
        100% { transform: translateY(calc(450px)); opacity: 0; }
    }
    
    .submission-overlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.9);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 10000;
        animation: fadeIn 0.3s ease;
    }
    
    .submission-content {
        text-align: center;
        color: #0f0;
    }
    
    .loading-spinner {
        width: 50px;
        height: 50px;
        border: 3px solid #333;
        border-top: 3px solid #0f0;
        border-radius: 50%;
        animation: spin 1s linear infinite;
        margin: 0 auto 20px;
    }
    
    .security-scan {
        margin-top: 30px;
        position: relative;
        padding: 20px;
        border: 1px solid #0f0;
        border-radius: 4px;
    }
    
    .scan-line {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 2px;
        background: #0f0;
        animation: scanLine 2s ease-in-out infinite;
    }
    
    @keyframes scanLine {
        0% { transform: translateX(-100%); }
        100% { transform: translateX(100%); }
    }
    
    .field-error {
        color: #ff6666;
        font-size: 0.8em;
        margin-top: 5px;
        animation: slideInDown 0.3s ease;
    }
    
    .field-success {
        color: #0f0;
        font-size: 0.8em;
        margin-top: 5px;
        animation: slideInDown 0.3s ease;
    }
    
    .input-box input.error {
        border-color: #ff4444 !important;
        box-shadow: 0 0 15px rgba(255, 68, 68, 0.3) !important;
    }
    
    .input-box input.success {
        border-color: #0f0 !important;
        box-shadow: 0 0 15px rgba(0, 255, 0, 0.3) !important;
    }
    
    .alert-close {
        background: none;
        border: none;
        color: inherit;
        font-size: 1.2em;
        cursor: pointer;
        margin-left: 10px;
        opacity: 0.7;
    }
    
    .alert-close:hover {
        opacity: 1;
    }
    
    @keyframes fadeIn {
        from { opacity: 0; }
        to { opacity: 1; }
    }
`;
document.head.appendChild(additionalStyles);

/**
 * Initialize all auth features
 */
function initAllAuthFeatures() {
    initMatrixBackground();
    enhanceFormInputs();
    initPasswordStrength();
    initRealTimeValidation();
    initFormSubmission();
    initKeyboardShortcuts();
    addGlitchEffect();
    initParticleSystem();
    initTerminalEffect();
    initSecurityScan();
}

// Initialize when DOM is loaded
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initAllAuthFeatures);
} else {
    initAllAuthFeatures();
}