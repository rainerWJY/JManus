// 从application.yml获取的端口配置
export const PORT = 18080;
export const BASE_URL = `http://${window.location.hostname}:${PORT}`;

// 步骤状态对应的图标
export const STEP_STATUS_ICONS = {
    completed: '✓',
    'in progress': '→',
    blocked: '✕',
    'not started': '○'
};

// 步骤状态对应的CSS类
export const STEP_STATUS_CLASSES = {
    completed: 'step-completed',
    'in progress': 'step-in-progress',
    blocked: 'step-blocked',
    'not started': 'step-not-started'
};
