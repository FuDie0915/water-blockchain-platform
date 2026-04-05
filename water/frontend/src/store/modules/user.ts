import { TOKEN_NAME } from '@/config/global';
import { login, logout, getUserInfo } from '@/store/api/user'

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
    state.token = '';
  },
  setUserInfo(state, userInfo) {
    state.userInfo = userInfo.userResp;
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
      // commit('setUserInfo', data.user);
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
  async logout({ commit }) {
    commit('removeToken');
    commit('setUserInfo', InitUserInfo);
  },
};

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters,
};
