class ChatApp {
    constructor() {
        this.messages = [];
        this.currentStreamController = null;
        this.init();
    }

    init() {
        const sendBtn = document.getElementById('sendBtn');
        const stopBtn = document.getElementById('stopBtn');
        const clearBtn = document.getElementById('clearBtn');
        const messageInput = document.getElementById('messageInput');

        sendBtn.addEventListener('click', () => this.sendMessage());
        stopBtn.addEventListener('click', () => this.stopStream());
        clearBtn.addEventListener('click', () => this.clearChat());
        
        messageInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });

        // Load config on startup
        this.loadConfig();
    }

    async loadConfig() {
        try {
            const response = await fetch('/api/chat/config');
            const config = await response.json();
            console.log('Config loaded:', config);
        } catch (error) {
            console.error('Failed to load config:', error);
        }
    }

    addMessage(role, content, isStreaming = false) {
        const messageId = Date.now();
        const message = {
            id: messageId,
            role: role,
            content: content,
            isStreaming: isStreaming
        };
        this.messages.push(message);
        this.renderMessage(message);
        return messageId;
    }

    updateMessage(messageId, content, isStreaming = false) {
        const message = this.messages.find(m => m.id === messageId);
        if (message) {
            message.content = content;
            message.isStreaming = isStreaming;
            this.renderMessage(message);
        }
    }

    renderMessage(message) {
        const messagesContainer = document.getElementById('messages');
        let messageElement = document.getElementById(`msg-${message.id}`);
        
        if (!messageElement) {
            messageElement = document.createElement('div');
            messageElement.id = `msg-${message.id}`;
            messageElement.className = `message ${message.role}`;
            messagesContainer.appendChild(messageElement);
        }

        const header = document.createElement('div');
        header.className = 'message-header';
        header.textContent = message.role === 'user' ? 'You' : 'Assistant';

        const content = document.createElement('div');
        content.className = `message-content ${message.isStreaming ? 'streaming' : ''}`;
        content.textContent = message.content;

        messageElement.innerHTML = '';
        messageElement.appendChild(header);
        messageElement.appendChild(content);

        // Scroll to bottom
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    renderAllMessages() {
        const messagesContainer = document.getElementById('messages');
        messagesContainer.innerHTML = '';
        this.messages.forEach(msg => this.renderMessage(msg));
    }

    async sendMessage() {
        const input = document.getElementById('messageInput');
        const message = input.value.trim();
        
        if (!message) {
            return;
        }

        // Disable input and send button
        input.disabled = true;
        document.getElementById('sendBtn').disabled = true;
        document.getElementById('stopBtn').disabled = false;

        // Add user message
        this.addMessage('user', message);
        input.value = '';

        // Add assistant message placeholder
        const assistantMessageId = this.addMessage('assistant', '', true);

        try {
            await this.streamChat(message, assistantMessageId);
        } catch (error) {
            console.error('Error sending message:', error);
            this.updateMessage(assistantMessageId, 'Error: ' + error.message, false);
        } finally {
            // Re-enable input and buttons
            input.disabled = false;
            document.getElementById('sendBtn').disabled = false;
            document.getElementById('stopBtn').disabled = true;
        }
    }

    async streamChat(userMessage, assistantMessageId) {
        const abortController = new AbortController();
        this.currentStreamController = abortController;

        try {
            const requestBody = {
                messages: this.messages
                    .filter(m => m.role !== 'assistant' || !m.isStreaming)
                    .map(m => ({
                        role: m.role,
                        content: m.content
                    })),
                newMessage: userMessage
            };

            const response = await fetch('/api/chat/stream', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody),
                signal: abortController.signal
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const reader = response.body.getReader();
            const decoder = new TextDecoder();
            let buffer = '';
            let fullContent = '';
            let currentEvent = null;

            while (true) {
                const { done, value } = await reader.read();
                
                if (done) {
                    break;
                }

                buffer += decoder.decode(value, { stream: true });
                const lines = buffer.split('\n');
                buffer = lines.pop() || '';

                for (const line of lines) {
                    if (line.trim() === '') {
                        // Empty line indicates end of event
                        if (currentEvent === 'done') {
                            this.updateMessage(assistantMessageId, fullContent, false);
                            return;
                        } else if (currentEvent === 'error') {
                            throw new Error(fullContent || 'Unknown error');
                        }
                        currentEvent = null;
                        continue;
                    }
                    
                    if (line.startsWith('event: ')) {
                        currentEvent = line.substring(7).trim();
                    } else if (line.startsWith('data: ')) {
                        const data = line.substring(6);
                        
                        if (currentEvent === 'token') {
                            fullContent += data;
                            this.updateMessage(assistantMessageId, fullContent, true);
                        } else if (currentEvent === 'done') {
                            this.updateMessage(assistantMessageId, fullContent, false);
                            return;
                        } else if (currentEvent === 'error') {
                            throw new Error(data || 'Unknown error');
                        }
                    }
                }
            }

            // Final update if stream ended without done event
            this.updateMessage(assistantMessageId, fullContent, false);
        } catch (error) {
            if (error.name === 'AbortError') {
                this.updateMessage(assistantMessageId, fullContent || '[Stopped]', false);
            } else {
                throw error;
            }
        } finally {
            this.currentStreamController = null;
        }
    }

    stopStream() {
        if (this.currentStreamController) {
            this.currentStreamController.abort();
            this.currentStreamController = null;
        }
    }

    clearChat() {
        if (confirm('Are you sure you want to clear the conversation?')) {
            this.messages = [];
            this.renderAllMessages();
        }
    }
}

// Initialize app when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    new ChatApp();
});

