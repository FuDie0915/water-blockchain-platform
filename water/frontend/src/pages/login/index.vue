<template>
  <div class="login-wrapper marine-login">
    <div class="login-shell">
      <section class="brand-panel ocean-card">
        <div class="brand-badge">海洋环保 · 区块链可信监管</div>
        <h1 class="brand-title">海链卫蓝</h1>
        <p class="brand-subtitle">海水养殖可信水质监管平台</p>
        <p class="brand-note">登录后将按角色自动进入对应工作台，流程更直接、更清晰。</p>

        <div class="brand-points">
          <span class="brand-point">可信留痕</span>
          <span class="brand-point">实时预警</span>
          <span class="brand-point">多角色协同</span>
        </div>

        <div class="quick-guide">
          <div class="quick-guide__item">
            <span class="quick-guide__index">01</span>
            <div>
              <strong>选择身份</strong>
              <p>支持管理员、养殖户、监管端独立登录。</p>
            </div>
          </div>
          <div class="quick-guide__item">
            <span class="quick-guide__index">02</span>
            <div>
              <strong>输入账号</strong>
              <p>使用对应角色账号完成登录或开户注册。</p>
            </div>
          </div>
          <div class="quick-guide__item">
            <span class="quick-guide__index">03</span>
            <div>
              <strong>进入工作台</strong>
              <p>系统自动跳转到各自的业务控制页面。</p>
            </div>
          </div>
        </div>
      </section>

      <section class="form-panel">
        <div class="panel-header">
          <div>
            <div class="panel-tag">{{ currentPanelTag }}</div>
            <h2>{{ type === 'register' ? '创建角色账号' : '欢迎登录' }}</h2>
            <p>{{ panelDescription }}</p>
          </div>
        </div>

        <div class="switch-tabs">
          <button :class="['switch-tab', { active: type === 'login' }]" @click="switchType('login')">账号登录</button>
          <button :class="['switch-tab', { active: type === 'register' }]" @click="switchType('register')">开户注册</button>
        </div>

        <div class="role-switch">
          <button
            :class="['role-option', { active: activeRole === 'farmers' }]"
            @click="type === 'register' ? selectRegisterRole('farmers') : selectLoginRole('farmers')"
          >
            {{ type === 'register' ? '养殖户注册' : '养殖户登录' }}
          </button>
          <button
            :class="['role-option', { active: activeRole === 'manager' }]"
            @click="type === 'register' ? selectRegisterRole('manager') : selectLoginRole('manager')"
          >
            {{ type === 'register' ? '监管端注册' : '监管端登录' }}
          </button>
          <button
            v-if="type === 'login'"
            :class="['role-option', 'role-option--admin', { active: activeRole === 'admin' }]"
            @click="selectLoginRole('admin')"
          >
            管理员登录
          </button>
        </div>
        <div v-if="type === 'register'" class="role-switch__hint">管理员账号由平台统一创建，此处仅开放养殖户与监管端注册。</div>

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
      loginRole: 'farmers',
      registerRole: 'farmers',
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

      if (this.loginRole === 'admin') return '管理员登录入口';
      if (this.loginRole === 'manager') return '监管端登录入口';
      return '养殖户登录入口';
    },
    panelDescription() {
      if (this.type === 'register') {
        return '当前仅开放养殖户与监管端开户注册，管理员账号由平台统一分配。';
      }

      if (this.loginRole === 'admin') {
        return '管理员登录后将直接进入平台总览、权限配置与系统管理中心。';
      }

      return '请选择角色后继续登录，系统会自动进入对应的业务控制台。';
    },
  },
  methods: {
    switchType(val) {
      this.type = val;
      if (val === 'register') {
        this.registerRole = this.registerRole || 'farmers';
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
