export function initUIModule() {
    // 获取UI元素
    const messagesContainer = document.getElementById('chat-messages');
    const userInput = document.getElementById('user-input');
    const sendButton = document.getElementById('send-button');
    const statusContainer = document.getElementById('status-container');
    const planIdElement = document.getElementById('plan-id');
    const planProgressElement = document.getElementById('plan-progress');
    const progressBar = document.getElementById('progress-bar');
    const stepsSummary = document.getElementById('steps-summary');
    const stepsContainer = document.getElementById('steps-container');
    const executionLogs = document.getElementById('execution-logs');
    
    function addMessage(content, sender) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${sender}`;
        
        const messageContent = document.createElement('div');
        messageContent.className = 'message-content';
        messageContent.innerHTML = formatMessage(content);
        
        messageDiv.appendChild(messageContent);
        messagesContainer.appendChild(messageDiv);
        
        // 滚动到最新消息
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
    
    function formatMessage(content) {
        // ...existing code...
        return content.replace(/\n/g, '<br>');
    }
    
    return {
        messagesContainer,
        userInput,
        sendButton,
        statusContainer,
        planIdElement,
        planProgressElement,
        progressBar,
        stepsSummary,
        stepsContainer,
        executionLogs,
        addMessage,
        formatMessage
    };
}
