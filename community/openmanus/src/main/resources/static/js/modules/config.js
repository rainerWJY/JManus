// 从application.yml获取的端口配置
export const PORT = 18080; // 确保这与application.yml中的端口匹配
export const BASE_URL = `${window.location.protocol}//${window.location.hostname}:${PORT}`;

// 如果在同一个服务器上，使用当前域名
export const API_BASE_URL = window.location.origin;

// 调试信息
console.log(`BASE_URL: ${BASE_URL}`);
console.log(`API_BASE_URL: ${API_BASE_URL}`);
console.log(`Current origin: ${window.location.origin}`);

// 步骤状态对应的图标
export const STEP_STATUS_ICONS = {
    'completed': '✓',
    'in_progress': '→',
    'blocked': '✕',
    'not_started': '○'
};

// 步骤状态对应的CSS类
export const STEP_STATUS_CLASSES = {
    'completed': 'step-completed',
    'in_progress': 'step-in-progress',
    'blocked': 'step-blocked',
    'not_started': 'step-not-started'
};
