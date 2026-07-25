/* ============================================
   个人博客 — 公共 JS 工具库
   ============================================ */

// ---------- API 请求封装 ----------
const api = {
  baseURL: '',

  async request(url, options = {}) {
    const token = localStorage.getItem('token');
    const headers = {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': 'Bearer ' + token } : {}),
      ...options.headers,
    };

    try {
      const res = await fetch(this.baseURL + url, { ...options, headers });
      const data = await res.json();

      if (data.code === 401 || res.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
        throw new Error(data.msg || '登录已过期');
      }
      return data;
    } catch (err) {
      if (err.message !== '登录已过期') {
        throw err;
      }
      throw err;
    }
  },

  get(url) {
    return this.request(url, { method: 'GET' });
  },

  post(url, body) {
    return this.request(url, { method: 'POST', body: JSON.stringify(body) });
  },

  put(url, body) {
    return this.request(url, { method: 'PUT', body: JSON.stringify(body) });
  },

  delete(url) {
    return this.request(url, { method: 'DELETE' });
  },
};

// ---------- Toast 通知 ----------
function showToast(message, type = 'info', duration = 3000) {
  let container = document.querySelector('.toast-container');
  if (!container) {
    container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const icons = { success: '✅', error: '❌', warning: '⚠️', info: 'ℹ️' };
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `
    <span>${icons[type] || ''}</span>
    <span>${message}</span>
    <button class="toast-close" onclick="this.parentElement.remove()">&times;</button>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.classList.add('toast-removing');
    setTimeout(() => toast.remove(), 300);
  }, duration);
}

// ---------- 用户状态管理 ----------
const userState = {
  getToken() {
    return localStorage.getItem('token');
  },

  isLoggedIn() {
    return !!this.getToken();
  },

  getUserInfo() {
    const cached = localStorage.getItem('userInfo');
    if (cached) return JSON.parse(cached);
    // 从 JWT token 解析
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return { username: payload.sub, userId: payload.userId };
    } catch {
      return null;
    }
  },

  async fetchAndCacheUserInfo() {
    if (!this.isLoggedIn()) return null;
    try {
      const res = await api.get('/api/user/me');
      if (res.code === 200 && res.data) {
        localStorage.setItem('userInfo', JSON.stringify(res.data));
        return res.data;
      }
    } catch {}
    return null;
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    window.location.href = '/login';
  },
};

// ---------- 格式化工具 ----------
function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  const now = new Date();
  const diff = now - d;
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return Math.floor(diff / 60000) + ' 分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + ' 小时前';
  if (diff < 604800000) return Math.floor(diff / 86400000) + ' 天前';
  return d.toISOString().substring(0, 10);
}

function escapeHtml(text) {
  if (!text) return '';
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

// ---------- 防抖 ----------
function debounce(fn, delay = 300) {
  let timer;
  return function(...args) {
    clearTimeout(timer);
    timer = setTimeout(() => fn.apply(this, args), delay);
  };
}

// ---------- 页面初始化检查 ----------
document.addEventListener('DOMContentLoaded', () => {
  const currentPath = window.location.pathname;
  const needsAuth = currentPath.includes('/publish') || currentPath.includes('/edit') || currentPath.includes('/profile');

  // 需要登录的页面，未登录则跳转
  if (needsAuth && !userState.isLoggedIn()) {
    showToast('请先登录', 'warning');
    setTimeout(() => { window.location.href = '/login'; }, 300);
  }
});

// 导出到全局
window.api = api;
window.showToast = showToast;
window.userState = userState;
window.formatDate = formatDate;
window.escapeHtml = escapeHtml;
window.debounce = debounce;
