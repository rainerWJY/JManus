import { BASE_URL, API_BASE_URL } from './config.js';

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
        
        // 添加消息节流
        const loadingMsgId = addLoadingMessage();
        const messageContainer = ui.messagesContainer;
        
        // 使用 RequestAnimationFrame 优化滚动
        function smoothScroll() {
            if (messageContainer.scrollTop < messageContainer.scrollHeight - messageContainer.clientHeight) {
                messageContainer.scrollTop += Math.ceil((messageContainer.scrollHeight - messageContainer.scrollTop - messageContainer.clientHeight) / 8);
                requestAnimationFrame(smoothScroll);
            }
        }
        
        // 尝试不同的URL格式
        const urls = [
            `${BASE_URL}/manus/chat?query=${encodeURIComponent(query)}`,
            `${API_BASE_URL}/manus/chat?query=${encodeURIComponent(query)}`
        ];
        
        fetchWithFallback(urls)
            .then(data => {
                // 移除加载消息
                removeLoadingMessage(loadingMsgId);
                
                // 显示系统回复
                const responseText = data.result || data.error || "抱歉，服务器未返回期望的响应";
                ui.addMessage(responseText, 'system');
                
                // 请求完成
                isWaitingForResponse = false;
                ui.sendButton.disabled = false;
                
                // 如果成功，10秒后隐藏状态
                if (!data.error) {
                    setTimeout(() => {
                        status.stopLoading();
                    }, 10000);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                removeLoadingMessage(loadingMsgId);
                ui.addMessage(`请求失败: ${error.message || '网络错误'}。请稍后再试。`, 'system error');
                isWaitingForResponse = false;
                ui.sendButton.disabled = false;
                status.addLog(`请求失败: ${error.message || '网络错误'}`, 'error');
            });
    }
    
    function fetchWithFallback(urls, index = 0) {
        if (index >= urls.length) {
            return Promise.reject(new Error('所有URL尝试均失败'));
        }
        
        return fetch(urls[index], { timeout: 60000 }) // 60秒超时
            .then(response => {
                if (!response.ok) {
                    throw new Error(`服务器错误: ${response.status}`);
                }
                
                return response.json();
            })
            .catch(error => {
                console.warn(`URL ${urls[index]} 失败:`, error);
                // 尝试下一个URL
                return fetchWithFallback(urls, index + 1);
            });
    }
    
    // 添加加载消息
    function addLoadingMessage() {
        const msgId = 'loading-' + Date.now();
        const loadingDiv = document.createElement('div');
        loadingDiv.id = msgId;
        loadingDiv.className = 'message system';
        
        const content = document.createElement('div');
        content.className = 'message-content loading';
        content.innerHTML = '正在思考...';
        
        loadingDiv.appendChild(content);
        ui.messagesContainer.appendChild(loadingDiv);
        ui.messagesContainer.scrollTop = ui.messagesContainer.scrollHeight;
        
        return msgId;
    }
    
    // 移除加载消息
    function removeLoadingMessage(msgId) {
        const loadingMsg = document.getElementById(msgId);
        if (loadingMsg) {
            loadingMsg.parentNode.removeChild(loadingMsg);
        }
    }
    
    return {
        handleSendMessage,
        sendQueryToBackend
    };
}
