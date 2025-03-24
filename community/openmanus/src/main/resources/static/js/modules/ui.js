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
        // 处理任务进度信息的特殊格式化
        if (content.includes('Progress:') && content.includes('Steps:')) {
            const progressMatch = content.match(/Progress: (\d+)\/(\d+) steps completed \(([0-9.]+)%\)/);
            const statusMatch = content.match(/Status: (\d+) completed, (\d+) in progress, (\d+) blocked, (\d+) not started/);
            
            if (progressMatch && statusMatch) {
                // 提取进度信息
                const completed = progressMatch[1];
                const total = progressMatch[2];
                const percentage = progressMatch[3];
                
                // 创建美化的进度显示
                let formattedContent = content.replace(/(Progress:.+?)(\n|$)/, 
                    `<div class="message-progress">
                        <div class="progress-text">进度: ${completed}/${total} 步骤完成 (${percentage}%)</div>
                        <div class="inline-progress-bar">
                            <div class="inline-progress" style="width: ${percentage}%"></div>
                        </div>
                    </div>\n`
                );
                
                return formattedContent.replace(/\n/g, '<br>');
            }
        }
        
        // 基本格式化，支持换行
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
