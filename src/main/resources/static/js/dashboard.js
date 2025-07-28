// dashboard.js - CyberLab Dashboard JavaScript

/**
 * Initialize User Dashboard
 */
function initDashboard(progressData, activityData) {
    console.log('%c🚀 Initializing User Dashboard', 'color: #0f0; font-weight: bold;');
    
    // Animate stats on load
    animateStats();
    
    // Initialize progress chart
    if (progressData) {
        initProgressChart(progressData);
    }
    
    // Initialize progress bars
    animateProgressBars();
    
    // Initialize tooltips
    initTooltips();
    
    // Add hover effects
    initHoverEffects();
    
    // Initialize activity timeline
    if (activityData) {
        updateActivityTimeline(activityData);
    }
    
    // Auto-refresh stats every 30 seconds
    setInterval(refreshDashboardStats, 30000);
}

/**
 * Initialize Admin Dashboard
 */
function initAdminDashboard(adminStats, chartData) {
    console.log('%c🛡️ Initializing Admin Dashboard', 'color: #ff0; font-weight: bold;');
    
    // Animate admin stats
    animateStats();
    
    // Initialize activity chart
    if (chartData) {
        initActivityChart(chartData);
    }
    
    // Initialize system health monitors
    initSystemHealthMonitors();
    
    // Initialize report handlers
    initReportHandlers();
    
    // Initialize admin action buttons
    initAdminActions();
    
    // Real-time updates
    initRealTimeUpdates();
    
    // Chart range selector
    initChartRangeSelector();
}

/**
 * Animate Statistics Numbers
 */
function animateStats() {
    const statNumbers = document.querySelectorAll('.stat-number');
    
    statNumbers.forEach(stat => {
        const finalValue = stat.textContent;
        const isTime = finalValue.includes('h') || finalValue.includes('min');
        const isPercentage = finalValue.includes('%');
        
        if (!isTime && !isPercentage) {
            // Extract number for animation
            const numericValue = parseInt(finalValue.replace(/[^0-9]/g, ''));
            animateCounter(stat, numericValue);
        }
    });
}

/**
 * Animate Counter
 */
function animateCounter(element, target) {
    const duration = 2000;
    const start = 0;
    const increment = target / (duration / 16);
    let current = start;
    
    const timer = setInterval(() => {
        current += increment;
        
        if (current >= target) {
            current = target;
            clearInterval(timer);
        }
        
        element.textContent = Math.floor(current).toLocaleString();
    }, 16);
}

/**
 * Initialize Progress Chart (User Dashboard)
 */
function initProgressChart(data) {
    const ctx = document.getElementById('progressChart');
    if (!ctx) return;
    
    const chartCtx = ctx.getContext('2d');
    
    // Create gradient
    const gradient = chartCtx.createLinearGradient(0, 0, 0, 250);
    gradient.addColorStop(0, 'rgba(0, 255, 0, 0.8)');
    gradient.addColorStop(1, 'rgba(0, 255, 0, 0.1)');
    
    new Chart(chartCtx, {
        type: 'line',
        data: {
            labels: data.labels || ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
            datasets: [{
                label: 'Progress',
                data: data.values || [65, 70, 75, 72, 78, 85, 90],
                backgroundColor: gradient,
                borderColor: '#0f0',
                borderWidth: 2,
                fill: true,
                tension: 0.4,
                pointBackgroundColor: '#0f0',
                pointBorderColor: '#000',
                pointBorderWidth: 2,
                pointRadius: 5,
                pointHoverRadius: 7
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    titleColor: '#0f0',
                    bodyColor: '#fff',
                    borderColor: '#0f0',
                    borderWidth: 1,
                    callbacks: {
                        label: function(context) {
                            return 'Progress: ' + context.parsed.y + '%';
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    max: 100,
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)',
                        borderColor: '#333'
                    },
                    ticks: {
                        color: '#aaa',
                        callback: function(value) {
                            return value + '%';
                        }
                    }
                },
                x: {
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)',
                        borderColor: '#333'
                    },
                    ticks: {
                        color: '#aaa'
                    }
                }
            }
        }
    });
}

/**
 * Initialize Activity Chart (Admin Dashboard)
 */
function initActivityChart(data) {
    const ctx = document.getElementById('activityChart');
    if (!ctx) return;
    
    const chartCtx = ctx.getContext('2d');
    
    new Chart(chartCtx, {
        type: 'bar',
        data: {
            labels: data.labels || ['Users', 'Labs', 'Posts', 'Comments'],
            datasets: [{
                label: 'This Week',
                data: data.thisWeek || [45, 32, 28, 51],
                backgroundColor: 'rgba(0, 255, 0, 0.6)',
                borderColor: '#0f0',
                borderWidth: 1
            }, {
                label: 'Last Week',
                data: data.lastWeek || [38, 28, 25, 45],
                backgroundColor: 'rgba(0, 255, 0, 0.3)',
                borderColor: '#0f0',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: {
                        color: '#aaa'
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    titleColor: '#0f0',
                    bodyColor: '#fff',
                    borderColor: '#0f0',
                    borderWidth: 1
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)',
                        borderColor: '#333'
                    },
                    ticks: {
                        color: '#aaa'
                    }
                },
                x: {
                    grid: {
                        display: false,
                        borderColor: '#333'
                    },
                    ticks: {
                        color: '#aaa'
                    }
                }
            }
        }
    });
}

/**
 * Animate Progress Bars
 */
function animateProgressBars() {
    const progressBars = document.querySelectorAll('.progress-fill');
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const progressBar = entry.target;
                const targetWidth = progressBar.getAttribute('data-progress') || progressBar.style.width;
                
                // Reset and animate
                progressBar.style.width = '0%';
                setTimeout(() => {
                    progressBar.style.width = targetWidth;
                }, 100);
                
                observer.unobserve(progressBar);
            }
        });
    });
    
    progressBars.forEach(bar => observer.observe(bar));
}

/**
 * Initialize Tooltips
 */
function initTooltips() {
    const tooltipElements = document.querySelectorAll('[data-tooltip]');
    
    tooltipElements.forEach(element => {
        element.addEventListener('mouseenter', function() {
            const tooltip = document.createElement('div');
            tooltip.className = 'custom-tooltip';
            tooltip.textContent = this.getAttribute('data-tooltip');
            tooltip.style.cssText = `
                position: absolute;
                background: rgba(0, 0, 0, 0.9);
                color: #0f0;
                padding: 0.5rem;
                border-radius: 4px;
                font-size: 0.8rem;
                z-index: 1000;
                pointer-events: none;
                border: 1px solid #0f0;
            `;
            
            document.body.appendChild(tooltip);
            
            const rect = this.getBoundingClientRect();
            tooltip.style.left = rect.left + (rect.width / 2) - (tooltip.offsetWidth / 2) + 'px';
            tooltip.style.top = rect.top - tooltip.offsetHeight - 10 + 'px';
            
            this.addEventListener('mouseleave', function() {
                tooltip.remove();
            }, { once: true });
        });
    });
}

/**
 * Initialize Hover Effects
 */
function initHoverEffects() {
    // Stat cards
    const statCards = document.querySelectorAll('.stat-card');
    statCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px) scale(1.02)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = '';
        });
    });
    
    // Achievement cards
    const achievementCards = document.querySelectorAll('.achievement-card');
    achievementCards.forEach(card => {
        card.addEventListener('click', function() {
            if (this.classList.contains('locked')) {
                showAchievementDetails(this);
            }
        });
    });
}

/**
 * Update Activity Timeline
 */
function updateActivityTimeline(activities) {
    const timeline = document.querySelector('.activity-timeline');
    if (!timeline || !activities) return;
    
    activities.forEach((activity, index) => {
        setTimeout(() => {
            const item = timeline.children[index];
            if (item) {
                item.style.opacity = '0';
                item.style.transform = 'translateX(-20px)';
                
                setTimeout(() => {
                    item.style.transition = 'all 0.5s ease';
                    item.style.opacity = '1';
                    item.style.transform = 'translateX(0)';
                }, 100);
            }
        }, index * 100);
    });
}

/**
 * Refresh Dashboard Stats
 */
async function refreshDashboardStats() {
    try {
        const response = await fetch('/api/dashboard/stats');
        const data = await response.json();
        
        // Update stats with animation
        Object.keys(data).forEach(key => {
            const element = document.querySelector(`[data-stat="${key}"]`);
            if (element) {
                const oldValue = parseInt(element.textContent);
                const newValue = data[key];
                
                if (oldValue !== newValue) {
                    animateValueChange(element, oldValue, newValue);
                }
            }
        });
    } catch (error) {
        console.error('Failed to refresh stats:', error);
    }
}

/**
 * Animate Value Change
 */
function animateValueChange(element, from, to) {
    const duration = 1000;
    const start = performance.now();
    
    function update(currentTime) {
        const elapsed = currentTime - start;
        const progress = Math.min(elapsed / duration, 1);
        
        const current = from + (to - from) * easeOutCubic(progress);
        element.textContent = Math.floor(current);
        
        if (progress < 1) {
            requestAnimationFrame(update);
        } else {
            element.textContent = to;
            
            // Add flash effect
            element.style.color = '#0f0';
            element.style.textShadow = '0 0 20px #0f0';
            setTimeout(() => {
                element.style.color = '';
                element.style.textShadow = '';
            }, 500);
        }
    }
    
    requestAnimationFrame(update);
}

/**
 * Easing function
 */
function easeOutCubic(t) {
    return 1 - Math.pow(1 - t, 3);
}

/**
 * Initialize System Health Monitors (Admin)
 */
function initSystemHealthMonitors() {
    // Simulate real-time updates
    setInterval(() => {
        updateSystemMetric('server-load', Math.random() * 100);
        updateSystemMetric('memory-usage', Math.random() * 100);
        updateSystemMetric('database', Math.random() * 100);
    }, 5000);
}

/**
 * Update System Metric
 */
function updateSystemMetric(metric, value) {
    const progressBar = document.querySelector(`[data-metric="${metric}"] .progress-fill`);
    const valueElement = document.querySelector(`[data-metric="${metric}"] .metric-value`);
    
    if (progressBar && valueElement) {
        progressBar.style.width = value + '%';
        valueElement.textContent = Math.floor(value) + '%';
        
        // Change color based on value
        if (value > 80) {
            progressBar.style.background = 'linear-gradient(90deg, #ff4444, #ff6666)';
        } else if (value > 60) {
            progressBar.style.background = 'linear-gradient(90deg, #ffc107, #ffdb4d)';
        } else {
            progressBar.style.background = 'linear-gradient(90deg, #0f0, #00ff41)';
        }
    }
}

/**
 * Initialize Report Handlers
 */
function initReportHandlers() {
    // Review report
    window.reviewReport = function(reportId) {
        console.log('Reviewing report:', reportId);
        // Open modal or redirect to report details
        window.location.href = `/admin/reports/${reportId}`;
    };
    
    // Dismiss report
    window.dismissReport = async function(reportId) {
        if (confirm('Are you sure you want to dismiss this report?')) {
            try {
                const response = await fetch(`/api/admin/reports/${reportId}/dismiss`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-Token': getCsrfToken()
                    }
                });
                
                if (response.ok) {
                    // Remove report from UI
                    const reportElement = document.querySelector(`[data-report-id="${reportId}"]`);
                    if (reportElement) {
                        reportElement.style.opacity = '0';
                        setTimeout(() => reportElement.remove(), 300);
                    }
                    
                    // Update count
                    updateReportCount(-1);
                }
            } catch (error) {
                console.error('Failed to dismiss report:', error);
                alert('Failed to dismiss report');
            }
        }
    };
}

/**
 * Update Report Count
 */
function updateReportCount(change) {
    const badge = document.querySelector('.notification-badge');
    if (badge) {
        const currentCount = parseInt(badge.textContent);
        const newCount = currentCount + change;
        
        if (newCount > 0) {
            badge.textContent = newCount;
        } else {
            badge.remove();
        }
    }
}

/**
 * Initialize Admin Actions
 */
function initAdminActions() {
    // Create Lab Modal
    window.showCreateLabModal = function() {
        console.log('Opening create lab modal');
        // Implementation would open a modal
    };
    
    // Create Category Modal
    window.showCreateCategoryModal = function() {
        console.log('Opening create category modal');
        // Implementation would open a modal
    };
    
    // Broadcast Modal
    window.showBroadcastModal = function() {
        console.log('Opening broadcast modal');
        // Implementation would open a modal
    };
    
    // Export Data
    window.exportData = async function() {
        if (confirm('Export all platform data?')) {
            window.location.href = '/admin/export';
        }
    };
    
    // Clear Cache
    window.clearCache = async function() {
        if (confirm('Clear all cached data?')) {
            try {
                const response = await fetch('/api/admin/cache/clear', {
                    method: 'POST',
                    headers: {
                        'X-CSRF-Token': getCsrfToken()
                    }
                });
                
                if (response.ok) {
                    showNotification('Cache cleared successfully', 'success');
                }
            } catch (error) {
                console.error('Failed to clear cache:', error);
                showNotification('Failed to clear cache', 'error');
            }
        }
    };
    
    // Maintenance Mode
    window.showMaintenanceModal = function() {
        if (confirm('Enable maintenance mode? This will prevent users from accessing the platform.')) {
            console.log('Enabling maintenance mode');
            // Implementation would toggle maintenance mode
        }
    };
}

/**
 * Initialize Real-Time Updates
 */
function initRealTimeUpdates() {
    // Simulate WebSocket connection for real-time updates
    console.log('Connecting to real-time updates...');
    
    // In production, this would be a WebSocket connection
    setInterval(() => {
        // Simulate random events
        if (Math.random() < 0.3) {
            addActivityLogEntry({
                time: new Date().toLocaleTimeString(),
                admin: 'admin',
                action: 'Updated system settings'
            });
        }
    }, 10000);
}

/**
 * Add Activity Log Entry
 */
function addActivityLogEntry(entry) {
    const logEntries = document.querySelector('.log-entries');
    if (!logEntries) return;
    
    const logEntry = document.createElement('div');
    logEntry.className = 'log-entry';
    logEntry.style.opacity = '0';
    logEntry.innerHTML = `
        <span class="log-time">${entry.time}</span>
        <span class="log-user">${entry.admin}</span>
        <span class="log-action">${entry.action}</span>
    `;
    
    logEntries.insertBefore(logEntry, logEntries.firstChild);
    
    // Animate entry
    setTimeout(() => {
        logEntry.style.transition = 'opacity 0.3s ease';
        logEntry.style.opacity = '1';
    }, 10);
    
    // Remove old entries if too many
    while (logEntries.children.length > 10) {
        logEntries.removeChild(logEntries.lastChild);
    }
}

/**
 * Initialize Chart Range Selector
 */
function initChartRangeSelector() {
    const rangeSelector = document.getElementById('chartRange');
    if (!rangeSelector) return;
    
    rangeSelector.addEventListener('change', async function() {
        const range = this.value;
        
        try {
            const response = await fetch(`/api/admin/chart-data?range=${range}`);
            const data = await response.json();
            
            // Update chart with new data
            updateActivityChart(data);
        } catch (error) {
            console.error('Failed to fetch chart data:', error);
        }
    });
}

/**
 * Show Achievement Details
 */
function showAchievementDetails(card) {
    const name = card.querySelector('h4').textContent;
    const description = card.querySelector('p').textContent;
    const progress = card.querySelector('.progress-fill')?.style.width || '0%';
    
    alert(`${name}\n\n${description}\n\nProgress: ${progress}`);
}

/**
 * Show Notification
 */
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: var(--bg-card);
        border: 1px solid ${type === 'success' ? '#0f0' : type === 'error' ? '#ff4444' : '#17a2b8'};
        color: ${type === 'success' ? '#0f0' : type === 'error' ? '#ff4444' : '#17a2b8'};
        padding: 1rem 1.5rem;
        border-radius: 4px;
        z-index: 10000;
        animation: slideInRight 0.3s ease;
    `;
    notification.textContent = message;
    
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.style.animation = 'slideOutRight 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

/**
 * Get CSRF Token
 */
function getCsrfToken() {
    return document.querySelector('meta[name="csrf-token"]')?.content || '';
}

/**
 * User Management Functions
 */
window.approveUser = async function(userId) {
    console.log('Approving user:', userId);
    // Implementation would approve user
};

window.viewUser = function(userId) {
    window.location.href = `/admin/users/${userId}`;
};

// Add CSS animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            opacity: 0;
            transform: translateX(100%);
        }
        to {
            opacity: 1;
            transform: translateX(0);
        }
    }
    
    @keyframes slideOutRight {
        from {
            opacity: 1;
            transform: translateX(0);
        }
        to {
            opacity: 0;
            transform: translateX(100%);
        }
    }
`;
document.head.appendChild(style);

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', function() {
    console.log('%c🎮 CyberLab Dashboard Ready', 'color: #0f0; font-size: 16px; font-weight: bold;');
});