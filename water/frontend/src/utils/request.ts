import axios from 'axios';
import proxy from '../config/host';
import { MessagePlugin } from 'tdesign-vue';
import router from '@/router';

const env = import.meta.env.MODE || 'development';
const API_HOST = env === 'mock' ? '/' : proxy[env]?.API || '';
const SUCCESS_CODE = 0;
const AUTH_ERROR_CODES = [40100, 401, 403];

const instance = axios.create({
  baseURL: API_HOST,
  timeout: 10000,
  withCredentials: true,
});

function clearTokens() {
  ['satoken', 'farmertoken', 'managertoken'].forEach((key) => {
    localStorage.removeItem(key);
  });
}

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('satoken');
  const farmerToken = localStorage.getItem('farmertoken');
  const managerToken = localStorage.getItem('managertoken');

  config.headers = config.headers || {};

  if (token) {
    config.headers.satoken = token;
  }
  if (farmerToken) {
    config.headers.farmertoken = farmerToken;
  }
  if (managerToken) {
    config.headers.managertoken = managerToken;
  }

  return config;
});

instance.interceptors.response.use(
  (response) => {
    const { data, config } = response;

    if (config.url?.includes('/common/upload')) {
      if (data?.fileName) {
        return {
          code: SUCCESS_CODE,
          data,
          message: '上传成功',
        };
      }
      MessagePlugin.error('上传失败');
      return Promise.reject(new Error('上传失败'));
    }

    if (typeof data?.code === 'undefined') {
      return data;
    }

    if (data.code !== SUCCESS_CODE) {
      if (AUTH_ERROR_CODES.includes(data.code)) {
        clearTokens();
        MessagePlugin.error('登录已过期，请重新登录');
        if (router.currentRoute.path !== '/login') {
          router.push('/login');
        }
      } else if (data.message) {
        MessagePlugin.error(data.message);
      }

      return Promise.reject(new Error(data.message || '请求失败'));
    }

    return data;
  },
  (error) => {
    const message = error?.response?.data?.message || error?.message || '网络请求失败';
    MessagePlugin.error(message);
    return Promise.reject(error);
  },
);

export default instance;
