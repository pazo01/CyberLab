// app.js - CyberLab Platform Main JavaScript

document.addEventListener('DOMContentLoaded', function() {
    console.log('CyberLab Platform - App loaded');
    
    // Initialize tooltips
    initializeTooltips();
    
    // Initialize modals
    initializeModals();
    
    // Initialize notifications
    initializeNotifications();
    
    // Initialize search functionality
    initializeSearch();
    
    // Initialize theme handling
    initializeTheme();
});

// Tooltip functionality
function initializeTooltips() {
    const tooltips = document.querySelectorAll('.tooltip');
    tooltips.forEach(tooltip => {
        tooltip.addEventListener('mouseenter', function() {
            const tooltipText = this.querySelector('.tooltip-text');
            if (tooltipText) {
                tooltipText.style.visibility = 'visible';
                tooltipText.style.opacity = '1';
            }
        });
        
        tooltip.addEventListener('mouseleave', function() {
            const tooltipText = this.querySelector('.tooltip-text');
            if (tooltipText) {
                tooltipText.style.visibility = 'hidden';
                tooltipText.style.opacity = '0';
            }
        });
    });
}

// Modal functionality
function initializeModals() {
    const modalTriggers = document.querySelectorAll('[data-modal]');
    const modals = document.querySelectorAll('.modal');
    
    modalTriggers.forEach(trigger => {
        trigger.addEventListener('click', function(e) {
            e.preventDefault();
            const modalId = this.getAttribute('data-modal');
            const modal = document.getElementById(modalId);
            if (modal) {
                showModal(modal);
            }
        });
    });
    
    modals.forEach(modal => {
        const closeButtons = modal.querySelectorAll('.modal-close, .btn-secondary');
        closeButtons.forEach(btn => {
            btn.addEventListener('click', function() {
                hideModal(modal);
            });
        });
        
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                hideModal(modal);
            }
        });
    });
}

function showModal(modal) {
    modal.style.display = 'flex';
    modal.classList.add('show');
    document.body.style.overflow = 'hidden';
}

function hideModal(modal) {
    modal.classList.remove('show');
    setTimeout(() => {
        modal.style.display = 'none';
        document.body.style.overflow = '';
    }, 300);
}

// Notification functionality
function initializeNotifications() {
    window.showNotification = function(title, message, type = 'info') {
        const notification = createNotification(title, message, type);
        document.body.appendChild(notification);
        
        setTimeout(() => notification.classList.add('show'), 100);
        
        setTimeout(() => {
            hideNotification(notification);
        }, 5000);
    };
}

function createNotification(title, message, type) {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <div class="notification-header">
            <span class="notification-title">${title}</span>
            <button class="notification-close" onclick="hideNotification(this.parentElement.parentElement)">&times;</button>
        </div>
        <div class="notification-body">${message}</div>
    `;
    return notification;
}

function hideNotification(notification) {
    notification.classList.remove('show');
    setTimeout(() => {
        if (notification.parentElement) {
            notification.parentElement.removeChild(notification);
        }
    }, 300);
}

// Search functionality
function initializeSearch() {
    const searchInputs = document.querySelectorAll('.search-input');
    const searchButtons = document.querySelectorAll('.search-button');
    
    searchInputs.forEach(input => {
        input.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performSearch(this.value);
            }
        });
    });
    
    searchButtons.forEach(button => {
        button.addEventListener('click', function() {
            const searchInput = this.parentElement.querySelector('.search-input');
            if (searchInput) {
                performSearch(searchInput.value);
            }
        });
    });
}

function performSearch(query) {
    if (query.trim()) {
        console.log('Performing search for:', query);
        // Add your search logic here
        showNotification('Ricerca', `Cercando: "${query}"`, 'info');
    }
}

// Theme functionality
function initializeTheme() {
    const themeToggle = document.getElementById('theme-toggle');
    if (themeToggle) {
        themeToggle.addEventListener('click', function() {
            toggleTheme();
        });
    }
    
    // Load saved theme
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme) {
        document.body.setAttribute('data-theme', savedTheme);
    }
}

function toggleTheme() {
    const currentTheme = document.body.getAttribute('data-theme');
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';
    
    document.body.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    
    showNotification('Tema', `Tema cambiato in: ${newTheme}`, 'success');
}

// Utility functions
function toggleDropdown(button) {
    const dropdown = button.nextElementSibling;
    const isOpen = dropdown.classList.contains('show');
    
    // Close all dropdowns
    document.querySelectorAll('.dropdown-menu.show').forEach(menu => {
        menu.classList.remove('show');
    });
    
    // Toggle current dropdown
    if (!isOpen) {
        dropdown.classList.add('show');
    }
}

function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(function() {
        showNotification('Copiato', 'Testo copiato negli appunti', 'success');
    }).catch(function() {
        showNotification('Errore', 'Impossibile copiare il testo', 'error');
    });
}

// Close dropdowns when clicking outside
document.addEventListener('click', function(e) {
    if (!e.target.closest('.dropdown')) {
        document.querySelectorAll('.dropdown-menu.show').forEach(menu => {
            menu.classList.remove('show');
        });
    }
});

// Loading states
function showLoading(element) {
    element.classList.add('loading');
    element.disabled = true;
}

function hideLoading(element) {
    element.classList.remove('loading');
    element.disabled = false;
}

// Form validation
function validateForm(form) {
    const requiredFields = form.querySelectorAll('[required]');
    let isValid = true;
    
    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            field.classList.add('error');
            isValid = false;
        } else {
            field.classList.remove('error');
        }
    });
    
    return isValid;
}

// Export functions for global use
window.toggleDropdown = toggleDropdown;
window.copyToClipboard = copyToClipboard;
window.showLoading = showLoading;
window.hideLoading = hideLoading;
window.validateForm = validateForm;
window.hideNotification = hideNotification;