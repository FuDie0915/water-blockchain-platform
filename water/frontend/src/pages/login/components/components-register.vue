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
      <t-input v-model="formData.userAccount" size="large" :placeholder="accountPlaceholder">
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

    <t-form-item>
      <t-button block size="large" type="submit" theme="primary" :loading="submitting">立即注册{{ roleText }}</t-button>
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
  confirmPassword: [{ required: true, message: '请再次输入密码', type: 'error' }],
};

const ROLE_TEXT_MAP = {
  company: '养殖户端',
  manager: '监管端',
};

export default Vue.extend({
  name: 'Register',
  components: {
    UserIcon,
    BrowseIcon,
    BrowseOffIcon,
    LockOnIcon,
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
      showConfirmPsw: false,
      submitting: false,
    };
  },
  computed: {
    roleText() {
      return ROLE_TEXT_MAP[this.role] || '当前角色';
    },
    accountPlaceholder() {
      return this.role === 'manager' ? '请输入监管端注册账号' : '请输入养殖户注册账号';
    },
    registerTip() {
      return this.role === 'manager'
        ? '监管端账号创建成功后，可直接进入审批、核验与预警处理工作台。'
        : '养殖户账号创建成功后，可直接进入水质监测、台账记录与预警页面。';
    },
  },
  methods: {
    async onSubmit({ validateResult }: { validateResult: boolean }) {
      if (validateResult !== true) return;

      if (this.formData.password !== this.formData.confirmPassword) {
        this.$message.error('两次输入的密码不一致');
        return;
      }

      this.submitting = true;
      try {
        await this.$store.dispatch('user/register', {
          userAccount: this.formData.userAccount,
          userPassword: this.formData.password,
          roleType: this.role,
        });
        localStorage.setItem('platformUserAccount', this.formData.userAccount);
        localStorage.setItem('platformUserPassword', this.formData.password);
        this.$message.success(`${this.roleText}注册成功，请登录`);
        this.$emit('register-success', {
          role: this.role,
          userAccount: this.formData.userAccount,
          userPassword: this.formData.password,
        });
      } catch (error) {
        this.$message.error(error?.message || '注册失败，请稍后重试');
      } finally {
        this.submitting = false;
      }
    },
  },
});
</script>

<style scoped lang="less">
</style>
