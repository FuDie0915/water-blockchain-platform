<template>
  <div class="login-wrapper marine-login">
    <div class="login-shell">
      <section class="brand-panel ocean-card">
        <div class="brand-badge">海洋环保 · 区块链可信监管</div>
        <h1 class="brand-title">海链卫蓝</h1>
        <p class="brand-subtitle">基于区块链的海水养殖可信水质监管平台</p>

        <div class="brand-intro">
          聚合首页看板、水质许可证审批、链上存证和 AI 助手，为企业与监管部门提供统一入口。
        </div>

        <div class="feature-list">
          <div class="feature-item">
            <strong>可信上链</strong>
            <span>集中展示链上运行态、关键统计指标与业务留痕结果。</span>
          </div>
          <div class="feature-item">
            <strong>水质监管</strong>
            <span>覆盖企业申报、监管审批、数据审查与结果追踪闭环。</span>
          </div>
          <div class="feature-item">
            <strong>智能辅助</strong>
            <span>右下角提供智能助手入口，支持快速问答和辅助分析。</span>
          </div>
        </div>
      </section>

      <section class="form-panel">
        <div class="panel-header">
          <div>
            <div class="panel-tag">统一登录门户</div>
            <h2>{{ type === 'register' ? '创建平台账号' : '欢迎登录' }}</h2>
            <p>登录后可进入区块链看板、水质监管工作台和 AI 助手。</p>
          </div>
        </div>

        <div class="switch-tabs">
          <button :class="['switch-tab', { active: type === 'login' }]" @click="switchType('login')">账号登录</button>
          <button :class="['switch-tab', { active: type === 'register' }]" @click="switchType('register')">开户注册</button>
        </div>

        <login v-if="type === 'login'" />
        <register v-else @register-success="switchType('login')" />
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
    };
  },
  methods: {
    switchType(val) {
      this.type = val;
    },
  },
};
</script>

<style lang="less">
@import url('./index.less');
</style>
