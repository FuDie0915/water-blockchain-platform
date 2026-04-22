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

    <t-form-item name="captchaCode">
      <div class="captcha-row">
        <t-input
          v-model="formData.captchaCode"
          size="large"
          placeholder="请输入验证码"
          class="captcha-input"
        >
          <template #prefix-icon>
            <lock-on-icon />
          </template>
        </t-input>
        <img
          v-if="captchaImage"
          :src="captchaImage"
          class="captcha-image"
          @click="refreshCaptcha"
          title="点击刷新验证码"
        />
        <t-button v-else size="large" @click="refreshCaptcha" class="captcha-btn">获取验证码</t-button>
      </div>
    </t-form-item>

    <t-form-item>
      <t-button block size="large" type="submit" theme="primary" :loading="submitting">立即注册{{ roleText }}</t-button>
    </t-form-item>
  </t-form>
</template>

<script lang="ts">
import Vue from 'vue';
import { UserIcon, BrowseIcon, BrowseOffIcon, LockOnIcon } from 'tdesign-icons-vue';
import { getCaptcha } from '@/api/water/user';

const INITIAL_DATA = {
  userAccount: '',
  password: '',
  confirmPassword: '',
  captchaKey: '',
  captchaCode: '',
};

const FORM_RULES = {
  userAccount: [{ required: true, message: '请输入账号', type: 'error' }],
  password: [{ required: true, message: '请输入密码', type: 'error' }],
  confirmPassword: [{ required: true, message: '请再次输入密码', type: 'error' }],
  captchaCode: [{ required: true, message: '请输入验证码', type: 'error' }],
};

const ROLE_TEXT_MAP = {
  farmers: '养殖户端',
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
      default: 'farmers',
    },
  },
  data() {
    return {
      FORM_RULES,
      formData: { ...INITIAL_DATA },
      showPsw: false,
      showConfirmPsw: false,
      submitting: false,
      captchaImage: '',
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
  mounted() {
    this.refreshCaptcha();
  },
  methods: {
    async refreshCaptcha() {
      try {
        const { data } = await getCaptcha();
        this.captchaImage = data?.imageBase64 || '';
        this.formData.captchaKey = data?.captchaKey || '';
      } catch (error) {
        console.error('获取验证码失败', error);
      }
    },
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
          captchaKey: this.formData.captchaKey,
          captchaCode: this.formData.captchaCode,
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
        this.refreshCaptcha();
      } finally {
        this.submitting = false;
      }
    },
  },
});
</script>

<style scoped lang="less">
.captcha-row {
  display: flex;
  gap: 12px;
  width: 100%;
}
.captcha-input {
  flex: 1;
}
.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  object-fit: contain;
  background: #f5f5f5;
}
.captcha-btn {
  width: 120px;
}
</style>
