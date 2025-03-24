import { BASE_URL, API_BASE_URL, STEP_STATUS_ICONS, STEP_STATUS_CLASSES } from './config.js';

export function initStatusModule(ui) {
    let currentPlanId = null;
    let statusCheckInterval = null;
    let retryCount = 0;
    const MAX_RETRIES = 3;
    const seenLogs = new Set();
    let lastError = null;
    
    function startLoading() {
        // 显示状态区域
        ui.statusContainer.className = 'status-visible';
        
        // 清空之前的日志
        ui.executionLogs.innerHTML = '';
        ui.stepsContainer.innerHTML = '';
        
        // 添加初始加载日志
        addLog("正在启动计划...", "info");
        
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
        // 尝试多种可能的URL格式，确保能找到正确的端点
        const urls = [
            `${BASE_URL}/manus/status`,
            `${API_BASE_URL}/manus/status`,
            `/manus/status`,                          // 相对路径
            `http://${window.location.hostname}:18080/manus/status` // 直接使用配置端口
        ];
        
        // 使用第一个URL
        fetchWithFallback(urls)
            .then(response => {
                retryCount = 0; // 成功后重置重试计数
                return response;
            })
            .then(data => {
                if (!data) {
                    throw new Error('未找到计划数据');
                }
                
                // 处理空计划情况
                if (data.message && data.message.includes("未找到")) {
                    addLog(`${data.message}，请先发送一个查询`, 'info');
                    return;
                }
                
                // 如果是错误响应
                if (data.error) {
                    addLog(`服务器错误: ${data.error}`, 'error');
                    return;
                }
                
                // 更新状态界面
                updateStatusUI(data);
            })
            .catch(error => {
                handleStatusError(error);
            });
    }
    
    function fetchWithFallback(urls, index = 0) {
        if (index >= urls.length) {
            return Promise.reject(new Error('所有URL尝试均失败'));
        }
        
        return fetch(urls[index], { 
                cache: 'no-cache',  // 禁用缓存
                headers: {
                    'Accept': 'application/json',
                    'Cache-Control': 'no-cache' 
                }
            })
            .then(response => {
                if (!response.ok) {
                    const error = new Error(`HTTP错误: ${response.status}`);
                    error.status = response.status;
                    throw error;
                }
                
                // 捕获空响应
                if (response.headers.get('content-length') === '0') {
                    return {};
                }
                
                // 安全地解析JSON，处理空响应或格式错误
                return response.text().then(text => {
                    if (!text || text.trim() === '') {
                        return {};
                    }
                    try {
                        return JSON.parse(text);
                    } catch (e) {
                        console.warn('解析JSON错误:', e, 'Raw text:', text);
                        throw new Error('无效的JSON响应');
                    }
                });
            })
            .catch(error => {
                console.warn(`URL ${urls[index]} 失败:`, error);
                // 尝试下一个URL
                return fetchWithFallback(urls, index + 1);
            });
    }
    
    function updateStatusUI(status) {
        // 更新计划ID
        if (status.planId) {
            ui.planIdElement.textContent = status.planId;
            currentPlanId = status.planId;
        }
        
        // 计算并显示进度
        const progress = status.progressPercentage || 0;
        ui.planProgressElement.textContent = Math.round(progress);
        ui.progressBar.style.width = `${progress}%`;
        
        // 更新步骤列表
        if (status.steps && Array.isArray(status.steps)) {
            updateStepsUI(status.steps);
        }
        
        // 更新状态摘要
        if (status.statusCounts) {
            updateStatusSummary(status.statusCounts);
        }
        
        // 如果计划完成，停止轮询
        if (status.state === 'COMPLETED') {
            addLog('计划完成！', 'info');
            stopLoading();
        }
    }
    
    function updateStepsUI(steps) {
        ui.stepsContainer.innerHTML = '';
        
        if (steps.length === 0) {
            const emptyEl = document.createElement('div');
            emptyEl.className = 'step-item empty';
            emptyEl.textContent = '等待计划初始化...';
            ui.stepsContainer.appendChild(emptyEl);
            return;
        }
        
        steps.forEach((step, index) => {
            const stepElement = document.createElement('div');
            stepElement.className = 'step-item';
            
            const statusClass = STEP_STATUS_CLASSES[step.status] || 'step-not-started';
            stepElement.classList.add(statusClass);
            
            const statusIcon = STEP_STATUS_ICONS[step.status] || '○';
            
            stepElement.innerHTML = `
                <div class="step-status">${statusIcon}</div>
                <div class="step-content">
                    <div class="step-description">${index}. ${step.description || '未命名步骤'}</div>
                    ${step.notes ? `<div class="step-notes">${step.notes}</div>` : ''}
                </div>
            `;
            
            ui.stepsContainer.appendChild(stepElement);
        });
    }
    
    function updateStatusSummary(counts) {
        const completed = counts.completed || 0;
        const inProgress = counts.in_progress || 0;
        const blocked = counts.blocked || 0;
        const notStarted = counts.not_started || 0;
        
        ui.stepsSummary.textContent = `(${completed} 已完成, ${inProgress} 进行中, ${blocked} 阻塞, ${notStarted} 未开始)`;
    }
    
    function handleStatusError(error) {
        console.error('状态检查错误:', error);
        
        // 避免重复显示相同错误
        const errorMessage = `${error.message || '状态更新失败'}`;
        if (!lastError || lastError !== errorMessage) {
            addLog(errorMessage, 'error');
            lastError = errorMessage;
        }
        
        // 如果是404错误，可能是服务未就绪，或还没有发送请求创建计划
        if (error.status === 404) {
            addLog("提示: 服务尚未就绪或未创建计划。请发送一个查询开始交互。", 'info');
        }
        
        // 如果重试次数过多，暂停检查并减少频率
        retryCount++;
        if (retryCount > MAX_RETRIES) {
            addLog(`已达到最大重试次数(${MAX_RETRIES})，降低状态检查频率`, 'warn');
            
            if (statusCheckInterval) {
                clearInterval(statusCheckInterval);
                statusCheckInterval = null;
            }
            
            // 10秒后以较低频率重试
            setTimeout(() => {
                retryCount = 0;
                addLog('以较低频率继续状态检查', 'info');
                statusCheckInterval = setInterval(checkPlanStatus, 3000); // 降低到3秒一次
            }, 5000);
        }
    }
    
    function addLog(message, level = 'info') {
        // 避免重复日志
        if (seenLogs.has(message)) return;
        seenLogs.add(message);
        
        const logEntry = document.createElement('div');
        logEntry.className = `log-entry log-${level}`;
        logEntry.textContent = `[${new Date().toLocaleTimeString()}] ${message}`;
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
