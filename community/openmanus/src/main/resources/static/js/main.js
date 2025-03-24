// 导入模块
import { initChatModule } from './modules/chat.js';
import { initStatusModule } from './modules/status.js';
import { initUIModule } from './modules/ui.js';

document.addEventListener('DOMContentLoaded', () => {
    // 初始化各个模块
    const ui = initUIModule();
    const status = initStatusModule(ui);
    const chat = initChatModule(ui, status);
    
    // 设置事件监听器
    ui.sendButton.addEventListener('click', () => chat.handleSendMessage());
    
    // 支持按Shift+Enter发送消息（Enter换行）
    ui.userInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && e.shiftKey) {
            e.preventDefault();
            chat.handleSendMessage();
        }
    });
    
    // 添加初始系统消息
    ui.addMessage('你好！我是Manus AI助手，请问有什么可以帮助你的吗？', 'system');
});
