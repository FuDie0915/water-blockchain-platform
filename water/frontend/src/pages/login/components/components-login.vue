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
      <t-input v-model="formData.userAccount" size="large" placeholder="请输入平台账号">
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
        placeholder="请输入登录密码"
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

    <div class="login-help-row">支持平台账号登录；养殖户与监管角色可在水质模块中进入各自工作台。</div>

    <t-form-item class="btn-container">
      <t-button block size="large" theme="primary" type="submit" :loading="submitting">进入平台</t-button>
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

export default Vue.extend({
  name: 'Login',
  components: {
    UserIcon,
    LockOnIcon,
    BrowseOffIcon,
    BrowseIcon,
  },
  data() {
    return {
      FORM_RULES,
      formData: { ...INITIAL_DATA },
      showPsw: false,
      submitting: false,
    };
  },
  mounted() {
    sessionStorage.removeItem('hasRefreshed');
  },
  methods: {
    async onSubmit({ validateResult }) {
      if (validateResult !== true) return;

      this.submitting = true;
      try {
        await this.$store.dispatch('user/login', this.formData);
        this.$message.success('登录成功');
        const redirect = this.$route.query.redirect || '/dashboard/base';
        this.$router.replace(redirect).catch(() => '');
      } finally {
        this.submitting = false;
      }
    },
  },
});
</script>

<style scoped lang="less">
.login-help-row {
  margin: 8px 0 18px;
  font-size: 13px;
  line-height: 1.7;
  color: #5e738a;
}
</style>
