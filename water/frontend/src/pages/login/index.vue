<template>
  <div class="login-wrapper marine-login">
    <div class="login-shell">
      <section class="brand-panel ocean-card">
        <div class="brand-badge">海洋环保 · 区块链可信监管</div>
        <h1 class="brand-title">海链卫蓝</h1>
        <p class="brand-subtitle">基于区块链的海水养殖可信水质监管平台</p>

        <div class="brand-intro">
          聚合首页看板、水质许可证审批、链上存证和 AI 助手，为多角色业务场景提供统一入口。
        </div>

        <div class="feature-list">
          <div class="feature-item">
            <strong>平台总览</strong>
            <span>特殊平台账号登录后会自动进入总览页，统一查看区块链全局态势。</span>
          </div>
          <div class="feature-item">
            <strong>养殖户端</strong>
            <span>用于许可证申报、材料上传、链上校验与进度追踪。</span>
          </div>
          <div class="feature-item">
            <strong>监管端</strong>
            <span>用于审批许可证、查看水质数据、执行监管核验与处置。</span>
          </div>
        </div>
      </section>

      <section class="form-panel">
        <div class="panel-header">
          <div>
            <div class="panel-tag">{{ currentPanelTag }}</div>
            <h2>{{ type === 'register' ? '创建角色账号' : '选择登录端口' }}</h2>
            <p>养殖户与监管端支持独立登录/注册，登录成功后将自动进入对应角色控制台。</p>
          </div>
        </div>

        <div class="switch-tabs">
          <button :class="['switch-tab', { active: type === 'login' }]" @click="switchType('login')">账号登录</button>
          <button :class="['switch-tab', { active: type === 'register' }]" @click="switchType('register')">开户注册</button>
        </div>

        <div class="role-switch">
          <button
            :class="['role-option', { active: activeRole === 'company' }]"
            @click="type === 'register' ? selectRegisterRole('company') : selectLoginRole('company')"
          >
            {{ type === 'register' ? '养殖户注册' : '养殖户登录' }}
          </button>
          <button
            :class="['role-option', { active: activeRole === 'manager' }]"
            @click="type === 'register' ? selectRegisterRole('manager') : selectLoginRole('manager')"
          >
            {{ type === 'register' ? '监管端注册' : '监管端登录' }}
          </button>
        </div>

        <login v-if="type === 'login'" :role="loginRole" />
        <register v-else :role="registerRole" @register-success="handleRegisterSuccess" />
      </section>
    </div>

    <footer class="copyright">Copyright © 2026 海链卫蓝 · Marine Trust Platform</footer>
  </div>
</template>

<script>
import Login from './components/components-login.vue';
import Register from './components/components-register.vue';

export default {
  name: 'LoginIndex',
  components: {
    Login,
    Register,
  },
  data() {
    return {
      type: 'login',
      loginRole: 'company',
      registerRole: 'company',
    };
  },
  computed: {
    activeRole() {
      return this.type === 'register' ? this.registerRole : this.loginRole;
    },
    currentPanelTag() {
      if (this.type === 'register') {
        return this.registerRole === 'manager' ? '监管端开户注册' : '养殖户开户注册';
      }

      if (this.loginRole === 'admin') return '平台总览入口';
      if (this.loginRole === 'manager') return '监管端登录入口';
      return '养殖户登录入口';
    },
  },
  methods: {
    switchType(val) {
      this.type = val;
      if (val === 'register') {
        this.registerRole = this.registerRole || 'company';
      }
    },
    selectLoginRole(role) {
      this.type = 'login';
      this.loginRole = role;
    },
    selectRegisterRole(role) {
      this.type = 'register';
      this.registerRole = role;
    },
    handleRegisterSuccess(payload) {
      const nextRole = typeof payload === 'string' ? payload : payload?.role;
      this.type = 'login';
      this.loginRole = nextRole || this.registerRole;
    },
  },
};
</script>

<style lang="less">
@import url('./index.less');
</style>
