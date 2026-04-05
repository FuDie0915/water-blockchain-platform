import NProgress from 'nprogress'; // progress bar
import 'nprogress/nprogress.css'; // progress bar style

import store from '@/store';
import router from '@/router';

NProgress.configure({ showSpinner: false });

const whiteListRouters = store.getters['permission/whiteListRouters'];

router.beforeEach(async (to, from, next) => {
  NProgress.start();
  
  // 从 fullPath 解析 satoken
  let urlToken = null;
  console.log(to.fullPath);
  if (to.fullPath.includes('/dashboard/base?satoken=')) {
    urlToken = to.fullPath.split('satoken=')[1].split('&')[0];
    console.log(urlToken, "token");
    store.commit('user/setToken', urlToken);
  }
  // 获取 token，避免重复获取
  const token = store.getters['user/token'];

  if (token) {
    if (to.path === '/login') {
      setTimeout(() => {
        store.dispatch('user/logout');
        store.dispatch('permission/restore');
      });
      next();
      return;
    }

    const roles = store.getters['user/roles'];
    if (roles && roles.length > 0) {
      next();
    } else {
      try {
        console.log("获取用户信息");
        await store.dispatch('user/getUserInfo');
        await store.dispatch('permission/initRoutes', store.getters['user/roles']);
        next({ ...to });
      } catch (error) {
        await store.commit('user/removeToken');
        next('/403');
        NProgress.done();
      }
    }
  } else {
    /* white list router */
    if (whiteListRouters.indexOf(to.path) !== -1) {
      next();
    } else {
      next('/403');
    }
    NProgress.done();
  }
});

router.afterEach(() => {
  NProgress.done();
});
