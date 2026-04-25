<template>
  <div class="enterprise-login">
    <div class="login-container">
      <div class="login-title">
        <h2>养殖户登录</h2>
        <div class="login-subtitle">欢迎使用水环境可信水质数据监管系统</div>
      </div>
      <t-form ref="form" :data="formData" :rules="rules" @submit="onSubmit">
        <t-form-item name="username">
          <t-input
            v-model="formData.username"
            placeholder="请输入用户名"
            size="large"
          >
            <template #prefix-icon>
              <t-icon name="user" />
            </template>
          </t-input>
        </t-form-item>
        <t-form-item name="password">
          <t-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            size="large"
          >
            <template #prefix-icon>
              <t-icon name="lock-on" />
            </template>
          </t-input>
        </t-form-item>
        <div class="login-options">
          <t-checkbox>记住我</t-checkbox>
          <t-link theme="primary" hover="color">忘记密码？</t-link>
        </div>
        <t-form-item>
          <t-button theme="primary" type="submit" block size="large">登录</t-button>
        </t-form-item>
      </t-form>
    </div>
  </div>
</template>

<script>
import { Login } from '@/api/water/water';

export default {
  name: 'EnterpriseLogin',
  data() {
    return {
      loading: false,
      formData: {
        username: '',
        password: ''
      },
      rules: {
        username: [{ required: true, message: '请输入用户名', type: 'error' }],
        password: [{ required: true, message: '请输入密码', type: 'error' }]
      }
    }
  },
  methods: {
    async onSubmit({ validateResult }) {
      if (validateResult !== true) {
        this.$message.error('请输入用户名和密码');
        return;
      }

      this.loading = true;
      try {
        const loginData = {
          userAccount: this.formData.username,
          userPassword: this.formData.password,
          loginType: 0
        };
        const response = await Login(loginData);
        if (response.code === 0) {
          localStorage.setItem('farmertoken', response.data.token);
          this.$message.success('登录成功');
          this.$router.replace('/water/enterprise-login');
        } else {
          this.$message.error(response.message || '登录失败');
        }
      } catch (error) {
        console.error('登录失败:', error);
        this.$message.error('登录失败');
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style lang="less" scoped>
.enterprise-login {
  padding: 20px;

  .login-container {
    max-width: 480px;
    margin: 0 auto;
    padding: 40px;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(10px);

    .login-title {
      text-align: center;
      margin-bottom: 40px;

      h2 {
        font-size: 28px;
        color: #0052d9;
        margin-bottom: 8px;
      }

      .login-subtitle {
        color: #888;
        font-size: 14px;
      }
    }

    .login-options {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin: 16px 0;
    }
  }
}
</style>
