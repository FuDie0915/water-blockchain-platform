import { resetRouter, asyncRouterList } from '@/router';

function cloneRoute(route) {
  return {
    ...route,
    meta: route.meta ? { ...route.meta } : route.meta,
    children: Array.isArray(route.children) ? route.children.map((child) => ({
      ...child,
      meta: child.meta ? { ...child.meta } : child.meta,
    })) : route.children,
  };
}

function filterPermissionsRouters(routes, roles) {
  const roleList = Array.isArray(roles) ? roles.filter(Boolean) : [roles].filter(Boolean);
  return (routes || []).reduce((res, route) => {
    const currentRoute = cloneRoute(route);
    const children = (currentRoute.children || []).filter((childRouter) => {
      const allowedRoles = childRouter.meta?.allowedRoles;
      const roleCode = childRouter.meta?.roleCode || childRouter.name;

      if (Array.isArray(allowedRoles) && allowedRoles.length > 0) {
        return allowedRoles.some((role) => roleList.includes(role));
      }

      return roleList.includes(roleCode);
    });

    if (children.length > 0) {
      currentRoute.children = children;
      res.push(currentRoute);
    }

    return res;
  }, []);
}

const state = {
  whiteListRouters: ['/login', '/403'],
  routers: [],
};

const mutations = {
  setRouters: (state, routers) => {
    state.routers = routers;
  },
};

const getters = {
  routers: (state) => state.routers,
  whiteListRouters: (state) => state.whiteListRouters,
};

const actions = {
  async initRoutes({ commit }, roles) {
    const normalizedRoles = Array.isArray(roles) ? roles.filter(Boolean) : [roles].filter(Boolean);
    const accessedRouters = filterPermissionsRouters(asyncRouterList, normalizedRoles);

    commit('setRouters', accessedRouters);

    // register routers
    // router.addRoutes(state.routers);
  },
  async restore({ commit }) {
    // remove routers
    resetRouter();
    commit('setRouters', []);
  },
};

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters,
};
