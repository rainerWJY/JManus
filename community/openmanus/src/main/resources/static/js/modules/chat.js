import { BASE_URL } from './config.js';

export function initChatModule(ui, status) {
    let isWaitingForResponse = false;
    
    function handleSendMessage() {
        if (ui.userInput.value.trim() === '' || isWaitingForResponse) return;
        
        // 获取用户输入
        const userMessage = ui.userInput.value.trim();
        
        // 添加用户消息到聊天界面
        ui.addMessage(userMessage, 'user');
        
        // 清空输入框
        ui.userInput.value = '';
        
        // 发送请求到后端
        sendQueryToBackend(userMessage);
    }
    
    function sendQueryToBackend(query) {
        isWaitingForResponse = true;
        ui.sendButton.disabled = true;
        
        // 显示加载状态
        status.startLoading();
        
        // 发送请求到后端
        fetch(`${BASE_URL}/manus/chat?query=${encodeURIComponent(query)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('网络请求失败');
                }
                return response.text();
            })
            .then(responseText => {
                // 显示系统回复
                ui.addMessage(responseText, 'system');
                // 请求完成
                isWaitingForResponse = false;
                ui.sendButton.disabled = false;
                status.stopLoading();
            })
            .catch(error => {
                console.error('Error:', error);
                ui.addMessage('抱歉，服务出现了问题，请稍后再试。', 'system');
                isWaitingForResponse = false;
                ui.sendButton.disabled = false;
                status.stopLoading();
            });
    }
    
    return {
        handleSendMessage,
        sendQueryToBackend
    };
}
