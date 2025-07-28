/**
 * LAB VIEW CONTROLLER - CyberLab Platform
 * Gestisce la visualizzazione completa del laboratorio con teoria e pratica
 */

class LabViewController {
    constructor(labData) {
        this.labId = labData.id;
        this.labTitle = labData.title;
        this.currentHint = 0;
        this.startTime = Date.now();
        this.isCompleted = false;
        this.currentTab = 'challenge';
        this.isFullscreen = false;
        
        this.init();
    }

    init() {
        this.bindEvents();
        this.startProgressTracking();
        this.setupCommunication();
        this.initializeUI();
        this.logActivity('Lab view initialized', 'info');
    }

    bindEvents() {
        // Tab switching events
        document.querySelectorAll('.practice-tab').forEach(tab => {
            tab.addEventListener('click', (e) => {
                const tabName = e.target.dataset.tab || e.target.closest('[data-tab]').dataset.tab;
                this.switchTab(tabName);
            });
        });

        // Floating controls
        this.bindFloatingControls();

        // Keyboard shortcuts
        document.addEventListener('keydown', (e) => {
            this.handleKeyboardShortcuts(e);
        });

        // Window resize for responsive handling
        window.addEventListener('resize', () => {
            this.handleResize();
        });
    }

    bindFloatingControls() {
        // Restart lab
        document.addEventListener('click', (e) => {
            if (e.target.closest('[onclick*="restartLab"]')) {
                e.preventDefault();
                this.restartLab();
            }
        });

        // Show hint
        document.addEventListener('click', (e) => {
            if (e.target.closest('[onclick*="showHint"]')) {
                e.preventDefault();
                this.showHint();
            }
        });

        // Check solution
        document.addEventListener('click', (e) => {
            if (e.target.closest('[onclick*="checkSolution"]')) {
                e.preventDefault();
                this.checkSolution();
            }
        });

        // Toggle fullscreen
        document.addEventListener('click', (e) => {
            if (e.target.closest('[onclick*="toggleFullscreen"]')) {
                e.preventDefault();
                this.toggleFullscreen();
            }
        });
    }

    switchTab(tabName) {
        // Update tabs
        document.querySelectorAll('.practice-tab').forEach(tab => {
            tab.classList.remove('active');
        });
        
        const activeTab = document.querySelector(`[data-tab="${tabName}"]`);
        if (activeTab) {
            activeTab.classList.add('active');
        }

        // Update content
        document.querySelectorAll('.practice-content').forEach(content => {
            content.classList.remove('active');
        });
        
        const activeContent = document.getElementById(`${tabName}-content`);
        if (activeContent) {
            activeContent.classList.add('active');
        }

        this.currentTab = tabName;
        this.logActivity(`Switched to ${tabName} tab`, 'info');

        // Send tab change to iframe if needed
        this.sendMessageToActiveIframe({ type: 'tab-changed', tab: tabName });
    }

    setupCommunication() {
        // Listen for messages from practice iframes
        window.addEventListener('message', (event) => {
            if (event.data && typeof event.data === 'object') {
                this.handleIframeMessage(event.data);
            }
        });
    }

    handleIframeMessage(data) {
        switch (data.type) {
            case 'lab-completed':
                this.handleCompletion(data);
                break;
            case 'progress-update':
                this.updateProgress(data.progress);
                break;
            case 'hint-request':
                this.showHint();
                break;
            case 'challenge-started':
                this.logActivity('Challenge started', 'success');
                break;
            case 'challenge-failed':
                this.logActivity('Challenge attempt failed', 'warning');
                break;
            case 'solution-found':
                this.logActivity('Solution found!', 'success');
                break;
            default:
                console.log('Unknown message type:', data.type);
        }
    }

    sendMessageToActiveIframe(message) {
        const activeIframe = document.querySelector('.practice-content.active iframe');
        if (activeIframe) {
            try {
                activeIframe.contentWindow.postMessage(message, '*');
            } catch (error) {
                console.warn('Failed to send message to iframe:', error);
            }
        }
    }

    startProgressTracking() {
        // Update progress every 30 seconds
        this.progressInterval = setInterval(() => {
            this.updateProgress();
        }, 30000);

        // Update time spent every minute
        this.timeInterval = setInterval(() => {
            this.updateTimeSpent();
        }, 60000);
    }

    updateProgress(progressData = null) {
        const timeSpent = Math.floor((Date.now() - this.startTime) / 1000 / 60);
        
        const data = progressData || {
            timeSpent: timeSpent,
            lastActivity: new Date().toISOString(),
            currentTab: this.currentTab
        };

        // Send to backend
        fetch(`/api/labs/${this.labId}/progress`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify(data)
        }).catch(error => {
            console.error('Progress update failed:', error);
        });

        // Update UI
        this.updateProgressUI(data);
    }

    updateProgressUI(data) {
        // Update time spent display
        const timeElement = document.querySelector('.time-spent');
        if (timeElement) {
            timeElement.textContent = this.formatTime(data.timeSpent || 0);
        }

        // Update progress percentage if provided
        if (data.progressPercentage !== undefined) {
            const progressBars = document.querySelectorAll('.progress-fill');
            const progressTexts = document.querySelectorAll('.progress-text');
            
            progressBars.forEach(bar => {
                bar.style.width = `${data.progressPercentage}%`;
            });
            
            progressTexts.forEach(text => {
                text.textContent = `${data.progressPercentage}%`;
            });
        }
    }

    handleCompletion(data) {
        if (!this.isCompleted) {
            this.isCompleted = true;
            this.showCompletionModal(data);
            this.saveCompletion(data);
            this.logActivity('Lab completed successfully!', 'success');
        }
    }

    showCompletionModal(data) {
        const modal = document.getElementById('completionModal');
        if (modal) {
            // Update modal content
            const pointsEl = document.getElementById('earnedPoints');
            const timeEl = document.getElementById('finalTime');
            
            if (pointsEl) pointsEl.textContent = data.points || 150;
            if (timeEl) timeEl.textContent = this.getTimeSpent();
            
            // Show modal with animation
            modal.style.display = 'flex';
            modal.classList.add('show');
            
            // Celebration animation
            this.triggerCelebration();
        }
    }

    saveCompletion(data) {
        fetch(`/api/labs/${this.labId}/complete`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify({
                completedAt: new Date().toISOString(),
                timeSpent: Math.floor((Date.now() - this.startTime) / 1000 / 60),
                pointsEarned: data.points || 150,
                solutions: data.solutions || [],
                currentTab: this.currentTab
            })
        }).catch(error => {
            console.error('Completion save failed:', error);
        });
    }

    // Floating control functions
    restartLab() {
        if (confirm('Are you sure you want to restart the lab? All progress will be lost.')) {
            // Clear progress
            this.currentHint = 0;
            this.startTime = Date.now();
            this.isCompleted = false;
            
            // Refresh iframes
            document.querySelectorAll('.practice-content iframe').forEach(iframe => {
                iframe.src = iframe.src;
            });
            
            // Reset UI
            this.resetProgressUI();
            
            this.logActivity('Lab restarted', 'info');
        }
    }

    showHint() {
        const hints = document.querySelectorAll('.hint');
        
        if (this.currentHint < hints.length) {
            const hint = hints[this.currentHint];
            
            // Highlight the current hint
            hint.style.animation = 'pulse 1s ease-in-out 3';
            hint.style.border = '2px solid var(--primary-green)';
            hint.style.backgroundColor = 'rgba(0, 255, 0, 0.1)';
            
            // Scroll to hint
            hint.scrollIntoView({ behavior: 'smooth', block: 'center' });
            
            this.currentHint++;
            this.logActivity(`Hint ${this.currentHint} shown`, 'info');
            
            // Send hint to iframe
            this.sendMessageToActiveIframe({ 
                type: 'hint-shown', 
                hintIndex: this.currentHint - 1,
                hintText: hint.textContent 
            });
        } else {
            alert('No more hints available! You\'ve got this!');
            this.logActivity('All hints exhausted', 'warning');
        }
    }

    checkSolution() {
        // Send solution check request to active iframe
        this.sendMessageToActiveIframe({ type: 'check-solution' });
        this.logActivity('Solution check requested', 'info');
    }

    toggleFullscreen() {
        const practicePanel = document.querySelector('.practice-panel');
        const button = document.querySelector('[onclick*="toggleFullscreen"] i');
        
        this.isFullscreen = !this.isFullscreen;
        
        if (this.isFullscreen) {
            practicePanel.classList.add('fullscreen');
            if (button) button.className = 'fas fa-compress';
            this.logActivity('Entered fullscreen mode', 'info');
        } else {
            practicePanel.classList.remove('fullscreen');
            if (button) button.className = 'fas fa-expand';
            this.logActivity('Exited fullscreen mode', 'info');
        }
    }

    handleKeyboardShortcuts(e) {
        // F11 - Toggle fullscreen
        if (e.key === 'F11') {
            e.preventDefault();
            this.toggleFullscreen();
        }
        
        // Ctrl+H - Show hint
        if (e.ctrlKey && e.key === 'h') {
            e.preventDefault();
            this.showHint();
        }
        
        // Ctrl+R - Restart lab (with confirmation)
        if (e.ctrlKey && e.key === 'r') {
            e.preventDefault();
            this.restartLab();
        }
        
        // Ctrl+Enter - Check solution
        if (e.ctrlKey && e.key === 'Enter') {
            e.preventDefault();
            this.checkSolution();
        }
        
        // Tab keys for switching between practice tabs
        if (e.altKey && e.key >= '1' && e.key <= '3') {
            e.preventDefault();
            const tabs = ['challenge', 'sandbox', 'validation'];
            const tabIndex = parseInt(e.key) - 1;
            if (tabs[tabIndex]) {
                this.switchTab(tabs[tabIndex]);
            }
        }
    }

    // Utility functions
    initializeUI() {
        // Add loading states
        this.addLoadingStates();
        
        // Initialize tooltips
        this.initTooltips();
        
        // Set up auto-save for any form inputs
        this.setupAutoSave();
    }

    addLoadingStates() {
        document.querySelectorAll('.practice-content iframe').forEach(iframe => {
            iframe.addEventListener('load', () => {
                iframe.style.opacity = '1';
                iframe.style.filter = 'none';
            });
            
            // Add loading overlay
            const overlay = document.createElement('div');
            overlay.className = 'iframe-loading';
            overlay.innerHTML = '<div class="loading-spinner"></div><p>Loading lab environment...</p>';
            iframe.parentNode.appendChild(overlay);
            
            iframe.addEventListener('load', () => {
                overlay.remove();
            });
        });
    }

    initTooltips() {
        // Add tooltips to floating controls
        const tooltips = {
            'restartLab': 'Restart Lab (Ctrl+R)',
            'showHint': 'Show Hint (Ctrl+H)',
            'checkSolution': 'Check Solution (Ctrl+Enter)',
            'toggleFullscreen': 'Toggle Fullscreen (F11)'
        };
        
        Object.entries(tooltips).forEach(([action, tooltip]) => {
            const element = document.querySelector(`[onclick*="${action}"]`);
            if (element) {
                element.title = tooltip;
                element.setAttribute('data-tooltip', tooltip);
            }
        });
    }

    setupAutoSave() {
        // Auto-save any notes or form inputs
        document.querySelectorAll('textarea, input[type="text"]').forEach(input => {
            let timeout;
            input.addEventListener('input', () => {
                clearTimeout(timeout);
                timeout = setTimeout(() => {
                    this.autoSave(input.name || input.id, input.value);
                }, 1000);
            });
        });
    }

    autoSave(key, value) {
        const data = { [key]: value };
        fetch(`/api/labs/${this.labId}/autosave`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify(data)
        }).catch(error => {
            console.warn('Auto-save failed:', error);
        });
    }

    handleResize() {
        // Handle responsive layout changes
        const practicePanel = document.querySelector('.practice-panel');
        if (window.innerWidth <= 768 && this.isFullscreen) {
            // On mobile, fullscreen should be different
            practicePanel.style.height = '100vh';
            practicePanel.style.width = '100vw';
        }
    }

    resetProgressUI() {
        // Reset all progress indicators
        document.querySelectorAll('.progress-fill').forEach(bar => {
            bar.style.width = '0%';
        });
        
        document.querySelectorAll('.progress-text').forEach(text => {
            text.textContent = '0%';
        });
        
        // Reset hints
        document.querySelectorAll('.hint').forEach(hint => {
            hint.style.animation = '';
            hint.style.border = '';
            hint.style.backgroundColor = '';
        });
    }

    triggerCelebration() {
        // Create celebration particles
        const celebration = document.createElement('div');
        celebration.className = 'celebration-overlay';
        celebration.innerHTML = `
            <div class="celebration-particles">
                ${Array.from({length: 50}, () => 
                    '<div class="particle" style="left: ' + Math.random() * 100 + '%; animation-delay: ' + Math.random() * 2 + 's;"></div>'
                ).join('')}
            </div>
        `;
        
        document.body.appendChild(celebration);
        
        setTimeout(() => {
            celebration.remove();
        }, 3000);
    }

    getTimeSpent() {
        const elapsed = Date.now() - this.startTime;
        const minutes = Math.floor(elapsed / 60000);
        const seconds = Math.floor((elapsed % 60000) / 1000);
        return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }

    formatTime(minutes) {
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`;
    }

    updateTimeSpent() {
        const timeElement = document.querySelector('.time-spent');
        if (timeElement) {
            timeElement.textContent = this.getTimeSpent();
        }
    }

    logActivity(message, type = 'info') {
        const timestamp = new Date().toLocaleTimeString();
        console.log(`[${timestamp}] [LAB-${this.labId}] ${message}`);
        
        // Optional: Send to analytics
        if (window.analytics) {
            window.analytics.track('lab_activity', {
                labId: this.labId,
                labTitle: this.labTitle,
                activity: message,
                type: type,
                timestamp: timestamp
            });
        }
    }

    // Cleanup
    destroy() {
        if (this.progressInterval) {
            clearInterval(this.progressInterval);
        }
        if (this.timeInterval) {
            clearInterval(this.timeInterval);
        }
        
        // Remove event listeners
        document.removeEventListener('keydown', this.handleKeyboardShortcuts.bind(this));
        window.removeEventListener('resize', this.handleResize.bind(this));
        
        this.logActivity('Lab view controller destroyed', 'info');
    }
}

// Global functions for backward compatibility
window.restartLab = function() {
    if (window.labController) {
        window.labController.restartLab();
    }
};

window.showHint = function() {
    if (window.labController) {
        window.labController.showHint();
    }
};

window.checkSolution = function() {
    if (window.labController) {
        window.labController.checkSolution();
    }
};

window.toggleFullscreen = function() {
    if (window.labController) {
        window.labController.toggleFullscreen();
    }
};

window.nextLab = function() {
    window.location.href = '/labs';
};

window.closeCompletion = function() {
    const modal = document.getElementById('completionModal');
    if (modal) {
        modal.style.display = 'none';
        modal.classList.remove('show');
    }
};

// Auto-initialize if lab data is available
document.addEventListener('DOMContentLoaded', function() {
    if (window.labData) {
        window.labController = new LabViewController(window.labData);
    }
});

// Export for module use
if (typeof module !== 'undefined' && module.exports) {
    module.exports = LabViewController;
}