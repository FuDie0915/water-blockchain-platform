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
        component: () => import('@/pages/water/portal.vue'),
        meta: { title: '水质监管工作台' },
      },
      {
        path: 'enterprise-login',
        name: 'enterprise-login',
        component: () => import('@/pages/water/workbench.vue'),
        meta: { title: '养殖户工作台' },
      },
      {
        path: 'monitor-login',
        name: 'monitor-login',
        component: () => import('@/pages/water/workbench.vue'),
        meta: { title: '监管工作台' },
      },
    ],
  },
];
