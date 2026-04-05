import { TOKEN_NAME } from '@/config/global';
import { login, logout as requestLogout, getUserInfo } from '@/store/api/user'

const InitUserInfo = {
  roles: [],
};

// 定义的state初始值
const state = {
  token: localStorage.getItem(TOKEN_NAME), // 默认token不走权限
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
    localStorage.removeItem('companytoken');
    localStorage.removeItem('managertoken');
    state.token = '';
  },
  setUserInfo(state, userInfo) {
    state.userInfo = userInfo?.userResp || userInfo || InitUserInfo;
  },
};

const getters = {
  token: (state) => state.token,
  roles: (state) => state.userInfo?.userRole,
};

const actions = {
  async login({ commit }, userInfo) {
    try {
      const { data } = await login(userInfo);
      console.log('data', data);
      commit('setToken', data.token);
      localStorage.setItem('platformUserAccount', userInfo.userAccount || 'admin');
      localStorage.setItem('platformUserPassword', '123456');
      return data;
    } catch (error) {
      throw error;
    }
  },
  async getUserInfo({ commit, state }) {
    try {
      const token = localStorage.getItem(TOKEN_NAME);
      const { data } = await getUserInfo(token);
      commit('setUserInfo', data);
    } catch (error) {
      throw error;
    }
  },
  async logout({ commit, state }) {
    const token = state.token || localStorage.getItem(TOKEN_NAME);
    const companyToken = localStorage.getItem('companytoken');
    const managerToken = localStorage.getItem('managertoken');

    commit('removeToken');
    commit('setUserInfo', InitUserInfo);

    const logoutTasks = [token, companyToken, managerToken]
      .filter(Boolean)
      .map((item) => requestLogout(item).catch(() => null));

    Promise.allSettled(logoutTasks);
  },
};

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters,
};
