<template>
  <t-form
    ref="form"
    class="item-container register-simple"
    :data="formData"
    :rules="FORM_RULES"
    label-width="0"
    @submit="onSubmit"
  >
    <t-form-item name="userAccount">
      <t-input v-model="formData.userAccount" size="large" placeholder="请输入注册账号">
        <template #prefix-icon>
          <user-icon />
        </template>
      </t-input>
    </t-form-item>

    <t-form-item name="password">
      <t-input
        v-model="formData.password"
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

    <t-form-item name="confirmPassword">
      <t-input
        v-model="formData.confirmPassword"
        size="large"
        :type="showConfirmPsw ? 'text' : 'password'"
        clearable
        placeholder="请再次输入密码"
      >
        <template #prefix-icon>
          <lock-on-icon />
        </template>
        <template #suffix-icon>
          <browse-icon v-if="showConfirmPsw" key="confirm-browse" @click="showConfirmPsw = false" />
          <browse-off-icon v-else key="confirm-browse-off" @click="showConfirmPsw = true" />
        </template>
      </t-input>
    </t-form-item>

    <div class="register-tip">注册后可直接使用该账号进入平台，默认密码以你当前填写为准。</div>

    <t-form-item>
      <t-button block size="large" type="submit" theme="primary" :loading="submitting">注册</t-button>
    </t-form-item>
  </t-form>
</template>

<script lang="ts">
import Vue from 'vue';
import { UserIcon, BrowseIcon, BrowseOffIcon, LockOnIcon } from 'tdesign-icons-vue';

const INITIAL_DATA = {
  userAccount: '',
  password: '',
  confirmPassword: '',
};

const FORM_RULES = {
  userAccount: [{ required: true, message: '请输入账号', type: 'error' }],
  password: [{ required: true, message: '请输入密码', type: 'error' }],
  confirmPassword: [
    { required: true, message: '请再次输入密码', type: 'error' },
    {
      validator: (val, rules, formData) => val === formData.password,
      message: '两次输入的密码不一致',
      type: 'error',
    },
  ],
};

export default Vue.extend({
  name: 'Register',
  components: {
    UserIcon,
    BrowseIcon,
    BrowseOffIcon,
    LockOnIcon,
  },
  data() {
    return {
      FORM_RULES,
      formData: { ...INITIAL_DATA },
      showPsw: false,
      showConfirmPsw: false,
      submitting: false,
    };
  },
  methods: {
    async onSubmit({ validateResult }: { validateResult: boolean }) {
      if (validateResult !== true) return;

      this.submitting = true;
      try {
        await this.$store.dispatch('user/login', {
          userAccount: this.formData.userAccount,
        });
        localStorage.setItem('platformUserPassword', this.formData.password);
        this.$message.success('注册成功，请使用新账号登录');
        this.$emit('register-success');
      } finally {
        this.submitting = false;
      }
    },
  },
});
</script>

<style scoped lang="less">
.register-tip {
  margin: 6px 0 16px;
  font-size: 13px;
  line-height: 1.7;
  color: #5e738a;
}
</style>
