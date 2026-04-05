import { Edit1Icon } from 'tdesign-icons-vue';
import Layout from '@/layouts/index.vue';

export default [
  {
    path: '/uav',
    component: Layout,
    redirect: '/uav/index',
    name: 'uav',
    meta: {
      title: '光伏板无人机巡检',
      icon: Edit1Icon,
    },
    children: [
      {
        path: 'uav',
        name: 'Uav',
        component: () => import('@/pages/uav/index.vue'),
        meta: { title: '巡检实训' },
      },
    ],
  },
];
