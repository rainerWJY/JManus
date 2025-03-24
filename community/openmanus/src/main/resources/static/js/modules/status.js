import { BASE_URL, STEP_STATUS_ICONS, STEP_STATUS_CLASSES } from './config.js';

export function initStatusModule(ui) {
    let currentPlanId = null;
    let statusCheckInterval = null;
    let logs = [];
    
    function startLoading() {
        // 显示状态区域
        ui.statusContainer.className = 'status-visible';
        
        // 清空之前的日志
        ui.executionLogs.innerHTML = '';
        ui.stepsContainer.innerHTML = '';
        logs = [];
        
        // 开始定时检查状态
        statusCheckInterval = setInterval(checkPlanStatus, 1000);
    }
    
    function stopLoading() {
        // 停止定时检查
        if (statusCheckInterval) {
            clearInterval(statusCheckInterval);
            statusCheckInterval = null;
        }
        
        // 隐藏状态区域（延迟几秒）
        setTimeout(() => {
            ui.statusContainer.className = 'status-hidden';
        }, 10000); // 保持10秒以便用户查看完整信息
    }
    
    function checkPlanStatus() {
        fetch(`${BASE_URL}/manus/status`)
            .then(response => response.json())
            .then(status => {
                if (!status) return;
                
                // 更新计划ID
                ui.planIdElement.textContent = status.planId || '-';
                currentPlanId = status.planId;
                
                // 计算并显示进度
                const progress = status.progressPercentage || 0;
                ui.planProgressElement.textContent = Math.round(progress);
                ui.progressBar.style.width = `${progress}%`;
                
                // 更新步骤摘要
                if (status.steps) {
                    updateStepsSummary(status.steps);
                }
                
                // 更新日志
                if (status.logs && status.logs.length > 0) {
                    updateLogs(status.logs);
                }
                
                // 如果状态是完成，停止轮询
                if (status.state === 'COMPLETED') {
                    stopLoading();
                }
            })
            .catch(error => {
                console.error('Error checking status:', error);
                addLog(`错误: 检查状态失败 - ${error.message}`, 'error');
            });
    }
    
    function updateStepsSummary(steps) {
        // 更新步骤摘要文本
        const completed = steps.filter(s => s.status === 'completed').length;
        const inProgress = steps.filter(s => s.status === 'in progress').length;
        const blocked = steps.filter(s => s.status === 'blocked').length;
        const notStarted = steps.filter(s => s.status === 'not started').length;
        
        ui.stepsSummary.textContent = `(${completed} 已完成, ${inProgress} 进行中, ${blocked} 阻塞, ${notStarted} 未开始)`;
        
        // 更新步骤列表
        ui.stepsContainer.innerHTML = '';
        steps.forEach((step, index) => {
            const stepElement = document.createElement('div');
            stepElement.className = 'step-item';
            
            const statusSpan = document.createElement('span');
            statusSpan.className = `step-status ${STEP_STATUS_CLASSES[step.status] || ''}`;
            statusSpan.textContent = STEP_STATUS_ICONS[step.status] || '-';
            
            const textSpan = document.createElement('span');
            textSpan.textContent = `${index}. ${step.description}`;
            
            stepElement.appendChild(statusSpan);
            stepElement.appendChild(textSpan);
            ui.stepsContainer.appendChild(stepElement);
        });
    }
    
    function updateLogs(newLogs) {
        // 添加新日志
        newLogs.forEach(log => {
            if (!logs.includes(log.message)) {
                logs.push(log.message);
                addLog(log.message, log.level);
            }
        });
    }
    
    function addLog(message, level = 'info') {
        const logEntry = document.createElement('div');
        logEntry.className = `log-entry log-${level}`;
        logEntry.textContent = message;
        ui.executionLogs.appendChild(logEntry);
        ui.executionLogs.scrollTop = ui.executionLogs.scrollHeight;
    }
    
    return {
        startLoading,
        stopLoading,
        checkPlanStatus,
        addLog
    };
}
