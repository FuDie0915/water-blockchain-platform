import { LayersIcon } from 'tdesign-icons-vue';
import Layout from '@/layouts/index.vue';

export default [
  {
    path: '/sea',
    component: Layout,
    name: 'sea',
    meta: {
      title: '海水养殖场可信水质数据监管',
      icon: LayersIcon,
    },
    children: [
      {
        path: 'index',
        name: 'sea',
        component: () => import('@/pages/sea/index.vue'),
        meta: { title: '养殖实训' },
      },
      {
        path: 'enterprise-login',
        name: 'enterprise-login',
        component: () => import('@/pages/sea/enterprise.vue')
      },
      {
        path: 'monitor-login',
        name: 'monitor-login',
        component: () => import('@/pages/sea/monitor.vue')
      }
    ],
  },
];
