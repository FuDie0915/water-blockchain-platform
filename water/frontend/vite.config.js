import { loadEnv } from 'vite';
import { viteMockServe } from 'vite-plugin-mock';
import { createVuePlugin } from 'vite-plugin-vue2';
import { createSvgPlugin } from 'vite-plugin-vue2-svg';

import path from 'path';

const CWD = process.cwd();

export default ({ mode }) => {
  const { VITE_BASE_URL } = loadEnv(mode, CWD);
  const API_BASE_URL = 'http://localhost:8080';
  return {
    base: VITE_BASE_URL,
    resolve: {
      alias: {
        '~': path.resolve(__dirname, './'),
        '@': path.resolve(__dirname, './src'),
      },
    },

    css: {
      preprocessorOptions: {
        less: {
          modifyVars: {},
        },
      },
    },

    plugins: [
      createVuePlugin({
        jsx: true,
      }),
      viteMockServe({
        mockPath: 'mock',
        localEnabled: true,
      }),
      createSvgPlugin(),
    ],

    build: {
      cssCodeSplit: false,
    },

    

    server: {
      host: '0.0.0.0',
      port: 3001,
      proxy: {
        '/gk_api': {
          // 用于开发环境下的转发请求
          // 更多请参考：https://vitejs.dev/config/#server-proxy
          target: API_BASE_URL,
          rewrite: (path) => path.replace(/^\/gk_api/, ''), // 删除路径中的 /api 前缀
          changeOrigin: true,
        },
        '/api': {
          // 用于开发环境下的转发请求
          // 更多请参考：https://vitejs.dev/config/#server-proxy
          target: API_BASE_URL,
          rewrite: (path) => path.replace(/^\/api/, ''), // 删除路径中的 /api 前缀
          changeOrigin: true,
        },
      },
    },
  };
};
