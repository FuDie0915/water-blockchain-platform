import NProgress from 'nprogress';
import 'nprogress/nprogress.css';

import store from '@/store';
import router from '@/router';

NProgress.configure({ showSpinner: false });

const whiteListRouters = store.getters['permission/whiteListRouters'];

function normalizeRoles(roleValue) {
  if (Array.isArray(roleValue)) return roleValue;
  return roleValue ? [roleValue] : [];
}

function getDefaultRouteByRoles(roles) {
  if (roles.includes('admin')) return '/dashboard/base';
  if (roles.includes('manager')) return '/water/monitor-login';
  if (roles.includes('farmers')) return '/water/enterprise-login';
  return '/login';
}

function clearInvalidRoleTokens(roles) {
  if (roles.includes('admin')) {
    localStorage.removeItem('farmertoken');
    localStorage.removeItem('managertoken');
    return;
  }

  if (roles.includes('farmers')) {
    localStorage.removeItem('managertoken');
  }

  if (roles.includes('manager')) {
    localStorage.removeItem('farmertoken');
  }
}

function isRouteAllowed(to, roles) {
  const allowedRoles = to.meta?.allowedRoles;
  if (!Array.isArray(allowedRoles) || allowedRoles.length === 0) {
    return true;
  }
  return roles.some((role) => allowedRoles.includes(role));
}

router.beforeEach(async (to, from, next) => {
  NProgress.start();

  const urlTokenMatch = to.fullPath.match(/[?&]satoken=([^&]+)/);
  if (urlTokenMatch?.[1]) {
    store.commit('user/setToken', urlTokenMatch[1]);
  }

  const token = store.getters['user/token'];

  if (token) {
    try {
      let roles = normalizeRoles(store.getters['user/roles']);

      if (!roles.length) {
        await store.dispatch('user/getUserInfo');
        roles = normalizeRoles(store.getters['user/roles']);
      }

      const permissionRouters = store.getters['permission/routers'] || [];
      if (roles.length && (!Array.isArray(permissionRouters) || permissionRouters.length === 0)) {
        await store.dispatch('permission/initRoutes', roles);
      }

      clearInvalidRoleTokens(roles);
      const defaultRoute = getDefaultRouteByRoles(roles);

      if (to.path === '/login') {
        next(defaultRoute);
        return;
      }

      if (!isRouteAllowed(to, roles)) {
        next(defaultRoute);
        return;
      }

      next();
    } catch (error) {
      store.commit('user/removeToken');
      next('/login');
      NProgress.done();
    }
  } else {
    if (whiteListRouters.indexOf(to.path) !== -1) {
      next();
    } else {
      next('/login');
    }
    NProgress.done();
  }
});

router.afterEach(() => {
  NProgress.done();
});
