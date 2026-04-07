<template>
  <t-form
    ref="form"
    class="item-container login-password"
    :data="formData"
    :rules="FORM_RULES"
    label-width="0"
    @submit="onSubmit"
  >
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
  admin: '管理员端',
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
      default: 'company',
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
      if (this.role === 'admin') return '请输入管理员账号';
      if (this.role === 'manager') return '请输入监管账号';
      return '请输入养殖户账号';
    },
    passwordPlaceholder() {
      return '请输入登录密码';
    },
    submitText() {
      return this.role === 'admin' ? '登录管理员端' : `登录${this.roleText}`;
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
        const normalizedRoles = Array.isArray(role) ? role : [role];
        const fallbackRoute = data?.defaultRoute || ROLE_ROUTE_MAP[normalizedRoles[0]] || '/dashboard/base';
        const redirect = this.$route.query.redirect && this.$route.query.redirect !== '/login'
          ? this.$route.query.redirect
          : fallbackRoute;

        await this.$store.dispatch('permission/initRoutes', normalizedRoles);
        this.$message.success(`${ROLE_TEXT_MAP[normalizedRoles[0]] || '当前端口'}登录成功`);
        await this.$nextTick();
        await this.$router.replace(redirect).catch(() => '');
      } finally {
        this.submitting = false;
      }
    },
  },
});
</script>
