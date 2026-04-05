import { ViewModuleIcon } from 'tdesign-icons-vue';
import Layout from '@/layouts/index.vue';

export default [
  {
    path: '/water',
    component: Layout,
    name: 'water',
    meta: {
      title: '海水环境可信水质数据监管',
      icon: ViewModuleIcon,
    },
    children: [
      {
        path: 'index',
        name: 'water',
        component: () => import('@/pages/water/index.vue'),
        meta: { title: '水质实训' },
      },
      {
        path: 'enterprise-login',
        name: 'enterprise-login',
        component: () => import('@/pages/water/enterprise.vue')
      },
      {
        path: 'monitor-login',
        name: 'monitor-login',
        component: () => import('@/pages/water/monitor.vue')
      }
    ],
  },
];
