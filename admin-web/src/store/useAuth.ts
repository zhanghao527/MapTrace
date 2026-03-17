import { create } from 'zustand';

/**
 * 使用 sessionStorage 替代 localStorage 存储 token，
 * 关闭浏览器标签页后 token 自动清除，降低 XSS 窃取风险。
 * 如需"记住我"功能，可在 setAuth 中根据 rememberMe 参数选择存储方式。
 */
const storage = sessionStorage;

interface AuthState {
  token: string | null;
  role: string;
  nickname: string;
  adminId: string | null;
  mustChangePassword: boolean;
  setAuth: (data: { token: string; role: string; nickname: string; adminId: string; mustChangePassword: boolean; rememberMe?: boolean }) => void;
  logout: () => void;
  isLoggedIn: () => boolean;
}

export const useAuth = create<AuthState>((set, get) => ({
  token: storage.getItem('admin_token') || localStorage.getItem('admin_token'),
  role: storage.getItem('admin_role') || localStorage.getItem('admin_role') || '',
  nickname: storage.getItem('admin_nickname') || localStorage.getItem('admin_nickname') || '',
  adminId: storage.getItem('admin_id') || localStorage.getItem('admin_id'),
  mustChangePassword: false,
  setAuth: (data) => {
    const store = data.rememberMe ? localStorage : storage;
    // 清除另一个存储中的旧数据
    const otherStore = data.rememberMe ? storage : localStorage;
    otherStore.removeItem('admin_token');
    otherStore.removeItem('admin_role');
    otherStore.removeItem('admin_nickname');
    otherStore.removeItem('admin_id');

    store.setItem('admin_token', data.token);
    store.setItem('admin_role', data.role);
    store.setItem('admin_nickname', data.nickname);
    store.setItem('admin_id', String(data.adminId));
    set({ token: data.token, role: data.role, nickname: data.nickname, adminId: data.adminId, mustChangePassword: data.mustChangePassword });
  },
  logout: () => {
    // 清除两种存储
    [localStorage, sessionStorage].forEach((s) => {
      s.removeItem('admin_token');
      s.removeItem('admin_role');
      s.removeItem('admin_nickname');
      s.removeItem('admin_id');
    });
    set({ token: null, role: '', nickname: '', adminId: null, mustChangePassword: false });
  },
  isLoggedIn: () => !!get().token,
}));
