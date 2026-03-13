import axios from 'axios';
import { message } from 'antd';

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080',
  timeout: 30000,
});

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  config.headers['X-Client-Type'] = 'web';
  return config;
});

request.interceptors.response.use(
  (res) => {
    const data = res.data;
    // 统一错误码：0 = 成功，40100 = 未登录
    if (data.code === 0) return data.data;
    if (data.code === 40100) {
      localStorage.removeItem('admin_token');
      window.location.hash = '#/login';
    }
    message.error(data.message || '请求失败');
    return Promise.reject(new Error(data.message));
  },
  (err) => {
    message.error(err.response?.data?.message || err.message || '网络错误');
    return Promise.reject(err);
  }
);

export default request;
