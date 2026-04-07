import { ViewModuleIcon } from 'tdesign-icons-vue';
import Layout from '@/layouts/index.vue';
import FarmingProcess from '@/pages/water/farming-process.vue';

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
        name: 'water-index',
        component: () => import('@/pages/water/portal.vue'),
        meta: { title: '平台总览', roleCode: 'admin', allowedRoles: ['admin'] },
      },
      {
        path: 'enterprise-login',
        name: 'enterprise-login',
        component: () => import('@/pages/water/workbench.vue'),
        meta: { title: '养殖户工作台', roleCode: 'company', allowedRoles: ['company'] },
      },
      {
        path: 'breeding',
        name: 'farmer-breeding',
        component: FarmingProcess,
        meta: { title: '养殖过程管理', roleCode: 'company', allowedRoles: ['company', 'manager'] },
      },
      {
        path: 'monitor-login',
        name: 'monitor-login',
        component: () => import('@/pages/water/workbench.vue'),
        meta: { title: '监管工作台', roleCode: 'manager', allowedRoles: ['manager'] },
      },
    ],
  },
];
