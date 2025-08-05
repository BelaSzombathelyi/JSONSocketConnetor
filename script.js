class JSONSocketConnector {
    constructor() {
        this.socket = null;
        this.isConnected = false;
        this.targetHost = 'localhost';
        this.targetPort = 60200;
        
        this.initializeElements();
        this.bindEvents();
        this.validateInput();
        
        // Auto-connect on startup
        setTimeout(() => this.connect(), 1000);
    }

    initializeElements() {
        // Connection controls
        this.hostnameInput = document.getElementById('hostname');
        this.portInput = document.getElementById('port');
        this.reconnectBtn = document.getElementById('reconnect-btn');
        this.connectionStatus = document.getElementById('connection-status');
        this.statusIndicator = document.getElementById('status-indicator');
        this.statusText = document.getElementById('status-text');

        // Message input
        this.messageInput = document.getElementById('message-input');
        this.sendBtn = document.getElementById('send-btn');
        this.clearInputBtn = document.getElementById('clear-input-btn');
        this.validationStatus = document.getElementById('validation-status');

        // Message display
        this.messagesDisplay = document.getElementById('messages-display');
        this.clearMessagesBtn = document.getElementById('clear-messages-btn');
        this.autoScrollCheckbox = document.getElementById('auto-scroll-checkbox');
    }

    bindEvents() {
        // Connection controls
        this.reconnectBtn.addEventListener('click', () => this.reconnect());
        this.hostnameInput.addEventListener('input', () => this.updateConnectionParams());
        this.portInput.addEventListener('input', () => this.updateConnectionParams());

        // Message input
        this.messageInput.addEventListener('input', () => this.validateInput());
        this.sendBtn.addEventListener('click', () => this.sendMessage());
        this.clearInputBtn.addEventListener('click', () => this.clearInput());

        // Message display
        this.clearMessagesBtn.addEventListener('click', () => this.clearMessages());

        // Keyboard shortcuts
        this.messageInput.addEventListener('keydown', (e) => {
            if (e.ctrlKey && e.key === 'Enter') {
                this.sendMessage();
            }
        });
    }

    updateConnectionParams() {
        this.targetHost = this.hostnameInput.value.trim() || 'localhost';
        this.targetPort = parseInt(this.portInput.value) || 60200;
    }

    validateInput() {
        const text = this.messageInput.value.trim();
        this.validationStatus.textContent = '';
        this.validationStatus.className = 'validation-status';

        if (!text) {
            this.sendBtn.disabled = !this.isConnected;
            return;
        }

        try {
            JSON.parse(text);
            this.validationStatus.textContent = '✓ Valid JSON';
            this.validationStatus.classList.add('valid');
            this.sendBtn.disabled = !this.isConnected;
        } catch (error) {
            this.validationStatus.textContent = `✗ Invalid JSON: ${error.message}`;
            this.validationStatus.classList.add('invalid');
            this.sendBtn.disabled = true;
        }
    }

    connect() {
        if (this.socket) {
            this.socket.disconnect();
        }

        this.updateConnectionStatus('connecting', 'Connecting...');
        
        try {
            this.socket = io();
            this.setupSocketEvents();
            
            // Request connection to the target TCP server
            this.socket.emit('connect-tcp', {
                host: this.targetHost,
                port: this.targetPort
            });
        } catch (error) {
            this.updateConnectionStatus('disconnected', `Connection failed: ${error.message}`);
            this.addMessage('error', 'Connection Error', error.message);
        }
    }

    setupSocketEvents() {
        this.socket.on('connect', () => {
            console.log('Connected to WebSocket server');
        });

        this.socket.on('tcp-connected', (data) => {
            this.isConnected = true;
            this.updateConnectionStatus('connected', `Connected to ${data.host}:${data.port}`);
            this.addMessage('received', 'System', `Connected to TCP server ${data.host}:${data.port}`);
            this.validateInput();
        });

        this.socket.on('tcp-disconnected', (data) => {
            this.isConnected = false;
            this.updateConnectionStatus('disconnected', 'Disconnected');
            this.addMessage('error', 'System', `Disconnected from TCP server: ${data.reason || 'Unknown reason'}`);
            this.validateInput();
        });

        this.socket.on('tcp-error', (error) => {
            this.isConnected = false;
            this.updateConnectionStatus('disconnected', `Error: ${error.message}`);
            this.addMessage('error', 'TCP Error', error.message);
            this.validateInput();
        });

        this.socket.on('tcp-message', (data) => {
            this.addMessage('received', 'Received', data.message);
        });

        this.socket.on('disconnect', () => {
            this.isConnected = false;
            this.updateConnectionStatus('disconnected', 'WebSocket disconnected');
            this.validateInput();
        });

        this.socket.on('connect_error', (error) => {
            this.updateConnectionStatus('disconnected', `WebSocket error: ${error.message}`);
            this.addMessage('error', 'WebSocket Error', error.message);
        });
    }

    reconnect() {
        this.updateConnectionParams();
        this.connect();
    }

    sendMessage() {
        const text = this.messageInput.value.trim();
        if (!text || !this.isConnected) return;

        try {
            const jsonData = JSON.parse(text);
            
            this.socket.emit('send-tcp-message', { message: text });
            this.addMessage('sent', 'Sent', text);
            
            // Clear input after successful send
            this.clearInput();
            
        } catch (error) {
            this.addMessage('error', 'Send Error', `Invalid JSON: ${error.message}`);
        }
    }

    clearInput() {
        this.messageInput.value = '';
        this.validateInput();
        this.messageInput.focus();
    }

    clearMessages() {
        this.messagesDisplay.innerHTML = '';
    }

    updateConnectionStatus(status, text) {
        this.statusText.textContent = text;
        this.statusIndicator.className = `status-indicator ${status}`;
        
        // Update reconnect button
        this.reconnectBtn.disabled = status === 'connecting';
        this.reconnectBtn.textContent = status === 'connecting' ? 'Connecting...' : 'Reconnect';
    }

    addMessage(type, category, content) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${type}`;
        
        const timestamp = new Date().toLocaleTimeString();
        
        messageDiv.innerHTML = `
            <div class="message-header">
                <span class="message-type">${category}</span>
                <span class="message-time">${timestamp}</span>
            </div>
            <div class="message-content">${this.escapeHtml(content)}</div>
        `;
        
        this.messagesDisplay.appendChild(messageDiv);
        
        // Auto-scroll if enabled
        if (this.autoScrollCheckbox.checked) {
            this.messagesDisplay.scrollTop = this.messagesDisplay.scrollHeight;
        }
        
        // Limit message history to prevent memory issues
        const messages = this.messagesDisplay.children;
        if (messages.length > 1000) {
            messages[0].remove();
        }
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Initialize the application when the DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new JSONSocketConnector();
});

// Handle page visibility changes
document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
        // Optionally reconnect when page becomes visible
        console.log('Page is visible again');
    }
});