import { TOKEN_NAME } from '@/config/global';
import { login, register as requestRegister, logout as requestLogout, getUserInfo } from '@/store/api/user';

const InitUserInfo = {
  roles: [],
  userRole: '',
};

const getDefaultRouteByRole = (role) => {
  if (role === 'admin') return '/dashboard/base';
  if (role === 'manager') return '/water/monitor-login';
  return '/water/enterprise-login';
};

const resolveRole = (userInfo = {}, fallbackRole = '', fallbackAccount = '') => {
  const account = (userInfo?.userResp?.userAccount || userInfo?.userAccount || fallbackAccount || '').toLowerCase();
  if (account === 'admin') {
    return 'admin';
  }

  return userInfo?.userResp?.userRole || userInfo?.userRole || fallbackRole || '';
};

// 定义的state初始值
const state = {
  token: localStorage.getItem(TOKEN_NAME),
  userInfo: InitUserInfo,
};

const mutations = {
  setToken(state, token) {
    localStorage.setItem(TOKEN_NAME, token);
    state.token = token;
  },
  removeToken(state) {
    localStorage.removeItem(TOKEN_NAME);
    localStorage.removeItem('platformUserAccount');
    localStorage.removeItem('platformUserPassword');
    localStorage.removeItem('platformUserRole');
    localStorage.removeItem('farmertoken');
    localStorage.removeItem('managertoken');
    state.token = '';
  },
  setUserInfo(state, userInfo) {
    const resolvedUserInfo = userInfo?.userResp || userInfo || InitUserInfo;
    const normalizedRole = resolveRole(userInfo, resolvedUserInfo?.userRole, resolvedUserInfo?.userAccount || localStorage.getItem('platformUserAccount'));
    state.userInfo = {
      ...resolvedUserInfo,
      userRole: normalizedRole,
    };
    if (normalizedRole) {
      localStorage.setItem('platformUserRole', normalizedRole);
    }
  },
};

const getters = {
  token: (state) => state.token,
  roles: (state) => state.userInfo?.userRole,
  defaultRoute: (state) => getDefaultRouteByRole(state.userInfo?.userRole),
};

const actions = {
  async login({ commit }, userInfo) {
    try {
      const { data } = await login(userInfo);
      const role = resolveRole(data, userInfo?.roleType, userInfo?.userAccount);
      const normalizedData = {
        ...data,
        userResp: {
          ...(data?.userResp || {}),
          userAccount: data?.userResp?.userAccount || userInfo.userAccount || '',
          userRole: role,
        },
      };

      localStorage.removeItem('farmertoken');
      localStorage.removeItem('managertoken');

      commit('setToken', normalizedData.token);
      commit('setUserInfo', normalizedData);

      localStorage.setItem('platformUserAccount', userInfo.userAccount || '');
      localStorage.setItem('platformUserPassword', userInfo.userPassword || '');
      localStorage.setItem('platformUserRole', role);

      if (role === 'farmers') {
        localStorage.setItem('farmertoken', normalizedData.token);
      } else if (role === 'manager') {
        localStorage.setItem('managertoken', normalizedData.token);
      }

      return {
        ...normalizedData,
        defaultRoute: getDefaultRouteByRole(role),
      };
    } catch (error) {
      throw error;
    }
  },
  async register(_, formData) {
    const { data } = await requestRegister(formData);
    return data;
  },
  async getUserInfo({ commit }) {
    try {
      const token = localStorage.getItem(TOKEN_NAME);
      const { data } = await getUserInfo(token);
      const role = resolveRole(data, localStorage.getItem('platformUserRole'), localStorage.getItem('platformUserAccount'));
      const normalizedData = {
        ...data,
        userResp: {
          ...(data?.userResp || {}),
          userRole: role,
        },
      };
      commit('setUserInfo', normalizedData);
      return normalizedData;
    } catch (error) {
      throw error;
    }
  },
  async logout({ commit, state }) {
    const token = state.token || localStorage.getItem(TOKEN_NAME);
    const farmerToken = localStorage.getItem('farmertoken');
    const managerToken = localStorage.getItem('managertoken');

    commit('removeToken');
    commit('setUserInfo', InitUserInfo);

    const logoutTasks = [token, farmerToken, managerToken]
      .filter(Boolean)
      .map((item) => requestLogout(item).catch(() => null));

    await Promise.allSettled(logoutTasks);
  },
};

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters,
};
