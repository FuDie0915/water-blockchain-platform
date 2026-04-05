import axios from 'axios';
import proxy from '../config/host';
import { MessagePlugin } from 'tdesign-vue';
import router from '@/router';  // 引入路由实例

const env = import.meta.env.MODE || 'development';

const API_HOST = env === 'mock' ? '/' : proxy[env].API; // 如果是mock模式 就不配置host 会走本地Mock拦截

const CODE = {
  LOGIN_TIMEOUT: 1000,
  REQUEST_SUCCESS: 0,
  REQUEST_FOBID: 1001,
};

const instance = axios.create({
  baseURL: API_HOST,
  timeout: 8000,
  withCredentials: true,
});

// eslint-disable-next-line
// @ts-ignore
// axios的retry ts类型有问题
instance.interceptors.retry = 3;

const debounce = (func, delay) => {
  let timeout;
  return function(...args) {
    const context = this;
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(context, args), delay);
  };
};

const debouncedRequest = debounce((config) => {
  return instance(config);
}, 200); // 设置防抖延迟为300毫秒

instance.interceptors.request.use((config) => {

  
  // 从localStorage获取token
  const token = localStorage.getItem('satoken');
  const companyToken = localStorage.getItem('companytoken')
  const managerToken = localStorage.getItem('managertoken')
  // 如果token存在，添加到请求头
  if (token) {
    config.headers['satoken'] = token;
  }
  // 企业 监管局
  if (companyToken) {
    config.headers['companytoken'] = companyToken;
  }
  if (managerToken) {
    config.headers['managertoken'] = managerToken;
  }
  
  // 处理 GET 请求的参数
  if (config.method?.toLowerCase() === 'get' && config.params) {
    // 构建查询字符串
    const queryString = Object.keys(config.params)
      .filter(key => config.params[key] !== undefined && config.params[key] !== null)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(config.params[key])}`)
      .join('&');
    
    // 将查询参数附加到 URL
    config.url = `${config.url}${queryString ? '?' + queryString : ''}`;
    
    // 清空 params，防止 axios 重复添加
    config.params = {};
  }
  return config;
});

instance.interceptors.response.use(
  (response) => {
    if (response.status === 200) {
      const { data } = response;
      
      // 处理文件上传接口的特殊返回格式
      if (response.config.url?.includes('/common/upload')) {
        if (data.fileName) {
          return {
            code: 0,
            data: data.fileName,
            message: '上传成功'
          };
        } else {
          return {
            code: -1,
            data: null,
            message: '上传失败'
          };
        }
      }

      // 其他接口的处理保持不变
      if (data.code !== CODE.REQUEST_SUCCESS) {
        // token过期或未登录
        if (data.code === 40100) {
          localStorage.removeItem('satoken');
          MessagePlugin.error('登录已过期，请重新登录');
          router.push('/403');
        } else {
          MessagePlugin.error(data.message || '请求失败');
        }
        throw new Error(data.message || '请求失败');
      }
      return data;
    }
  },
  (error) => {
    console.log(error, 'error');
    const message = error.response?.data?.message || error.message || '请求失败';
    MessagePlugin.error(message);
    throw new Error(message);
  }
);

export default instance;
