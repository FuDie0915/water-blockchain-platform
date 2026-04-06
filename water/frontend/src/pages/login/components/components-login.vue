<template>
  <t-form
    ref="form"
    class="item-container login-password"
    :data="formData"
    :rules="FORM_RULES"
    label-width="0"
    @submit="onSubmit"
  >
    <div class="login-role-tip">
      <span>当前登录端口</span>
      <strong>{{ roleText }}</strong>
    </div>

    <t-form-item name="userAccount">
      <t-input v-model="formData.userAccount" size="large" :placeholder="accountPlaceholder">
        <template #prefix-icon>
          <user-icon />
        </template>
      </t-input>
    </t-form-item>

    <t-form-item name="userPassword">
      <t-input
        v-model="formData.userPassword"
        size="large"
        :type="showPsw ? 'text' : 'password'"
        clearable
        :placeholder="passwordPlaceholder"
      >
        <template #prefix-icon>
          <lock-on-icon />
        </template>
        <template #suffix-icon>
          <browse-icon v-if="showPsw" key="browse" @click="showPsw = false" />
          <browse-off-icon v-else key="browse-off" @click="showPsw = true" />
        </template>
      </t-input>
    </t-form-item>

    <div class="login-help-row">{{ helpText }}</div>

    <t-form-item class="btn-container">
      <t-button block size="large" theme="primary" type="submit" :loading="submitting">{{ submitText }}</t-button>
    </t-form-item>
  </t-form>
</template>

<script lang="ts">
import Vue from 'vue';
import { UserIcon, LockOnIcon, BrowseOffIcon, BrowseIcon } from 'tdesign-icons-vue';

const INITIAL_DATA = {
  userAccount: '',
  userPassword: '',
};

const FORM_RULES = {
  userAccount: [{ required: true, message: '请输入账号', type: 'error' }],
  userPassword: [{ required: true, message: '请输入密码', type: 'error' }],
};

const ROLE_TEXT_MAP = {
  admin: '平台总览',
  company: '养殖户端',
  manager: '监管端',
};

const ROLE_ROUTE_MAP = {
  admin: '/dashboard/base',
  company: '/water/enterprise-login',
  manager: '/water/monitor-login',
};

export default Vue.extend({
  name: 'Login',
  components: {
    UserIcon,
    LockOnIcon,
    BrowseOffIcon,
    BrowseIcon,
  },
  props: {
    role: {
      type: String,
      default: 'admin',
    },
  },
  data() {
    return {
      FORM_RULES,
      formData: { ...INITIAL_DATA },
      showPsw: false,
      submitting: false,
    };
  },
  computed: {
    roleText() {
      return ROLE_TEXT_MAP[this.role] || '当前端口';
    },
    accountPlaceholder() {
      return this.role === 'manager' ? '请输入监管账号' : '请输入登录账号';
    },
    passwordPlaceholder() {
      return '请输入登录密码';
    },
    submitText() {
      return `登录${this.roleText}`;
    },
    helpText() {
      if (this.role === 'manager') {
        return '登录成功后将根据当前账号权限自动进入对应控制台，并限制跨端访问。';
      }
      return '登录成功后将根据当前账号权限自动进入对应控制台，并限制跨端访问。';
    },
  },
  mounted() {
    sessionStorage.removeItem('hasRefreshed');
    this.formData = {
      userAccount: localStorage.getItem('platformUserAccount') || '',
      userPassword: localStorage.getItem('platformUserPassword') || '',
    };
  },
  methods: {
    async onSubmit({ validateResult }) {
      if (validateResult !== true) return;

      this.submitting = true;
      try {
        const account = (this.formData.userAccount || '').trim();
        const data = await this.$store.dispatch('user/login', {
          ...this.formData,
          roleType: this.role,
        });
        const role = account.toLowerCase() === 'admin' ? 'admin' : (data?.userResp?.userRole || this.role);
        const fallbackRoute = ROLE_ROUTE_MAP[role] || '/dashboard/base';
        const redirect = this.$route.query.redirect && this.$route.query.redirect !== '/login'
          ? this.$route.query.redirect
          : fallbackRoute;

        this.$message.success(`${ROLE_TEXT_MAP[role] || '当前端口'}登录成功`);
        this.$router.replace(redirect).catch(() => '');
      } finally {
        this.submitting = false;
      }
    },
  },
});
</script>

<style scoped lang="less">
.login-role-tip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  padding: 10px 14px;
  border-radius: 12px;
  background: rgba(31, 116, 216, 0.08);
  color: #1d4f79;

  strong {
    font-weight: 700;
  }
}

.login-help-row {
  margin: 8px 0 18px;
  font-size: 13px;
  line-height: 1.7;
  color: #5e738a;
}
</style>
