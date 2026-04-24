<template>
  <div class="user-profile-page">
    <t-loading :loading="loading" text="加载用户信息中...">
      <t-row :gutter="[24, 24]">
        <t-col :span="12">
          <t-card title="基本信息" :bordered="false" class="profile-card">
            <template #actions>
              <t-button v-if="!editingBasic" theme="primary" variant="outline" @click="startEditBasic">
                <edit-icon slot="icon" />编辑信息
              </t-button>
              <template v-else>
                <t-button theme="default" variant="outline" @click="cancelEditBasic">取消</t-button>
                <t-button theme="primary" :loading="savingBasic" @click="saveBasic">保存</t-button>
              </template>
            </template>

            <div class="profile-section">
              <div class="profile-avatar">
                <t-avatar size="80px">{{ avatarText }}</t-avatar>
                <div class="profile-name">{{ userInfo.userName || '未设置名称' }}</div>
                <t-tag :theme="roleTheme" variant="light">{{ roleText }}</t-tag>
              </div>

              <t-form :data="basicForm" :disabled="!editingBasic" layout="inline" class="profile-form">
                <t-form-item label="用户账号">
                  <t-input v-model="userInfo.userAccount" disabled />
                </t-form-item>
                <t-form-item label="用户名称">
                  <t-input v-model="basicForm.userName" placeholder="请输入用户名称" />
                </t-form-item>
                <t-form-item label="角色类型">
                  <t-input :value="roleText" disabled />
                </t-form-item>
                <t-form-item label="账号状态">
                  <t-tag :theme="userInfo.userStatus === 1 ? 'success' : 'warning'" variant="light-outline">
                    {{ userInfo.userStatus === 1 ? '正常' : '禁用' }}
                  </t-tag>
                </t-form-item>
                <t-form-item label="区块链地址">
                  <div class="chain-address">{{ userInfo.accountAddress || '--' }}</div>
                </t-form-item>
              </t-form>
            </div>
          </t-card>
        </t-col>

        <t-col :span="12">
          <t-card title="安全设置" :bordered="false" class="profile-card">
            <div class="security-section">
              <div class="security-item">
                <div class="security-item__left">
                  <div class="security-item__title">登录密码</div>
                  <div class="security-item__desc">定期修改密码可以提高账号安全性</div>
                </div>
                <div class="security-item__right">
                  <t-button theme="default" variant="outline" @click="showPasswordDialog">修改密码</t-button>
                </div>
              </div>
            </div>
          </t-card>

          <t-card v-if="userRole === 'farmers'" title="养殖户信息" :bordered="false" class="profile-card" style="margin-top: 16px;">
            <template #actions>
              <t-button v-if="!editingExt" theme="primary" variant="outline" @click="startEditExt">
                <edit-icon slot="icon" />编辑信息
              </t-button>
              <template v-else>
                <t-button theme="default" variant="outline" @click="cancelEditExt">取消</t-button>
                <t-button theme="primary" :loading="savingExt" @click="saveExt">保存</t-button>
              </template>
            </template>

            <t-form :data="farmerExtForm" :disabled="!editingExt" layout="inline" class="profile-form">
              <t-form-item label="联系电话">
                <t-input v-model="farmerExtForm.phone" placeholder="请输入联系电话" />
              </t-form-item>
              <t-form-item label="养殖场名称">
                <t-input v-model="farmerExtForm.farmName" placeholder="请输入养殖场名称" />
              </t-form-item>
              <t-form-item label="养殖场地址">
                <t-input v-model="farmerExtForm.farmAddress" placeholder="请输入养殖场地址" />
              </t-form-item>
            </t-form>
          </t-card>

          <t-card v-if="userRole === 'manager'" title="监管端信息" :bordered="false" class="profile-card" style="margin-top: 16px;">
            <template #actions>
              <t-button v-if="!editingExt" theme="primary" variant="outline" @click="startEditExt">
                <edit-icon slot="icon" />编辑信息
              </t-button>
              <template v-else>
                <t-button theme="default" variant="outline" @click="cancelEditExt">取消</t-button>
                <t-button theme="primary" :loading="savingExt" @click="saveExt">保存</t-button>
              </template>
            </template>

            <t-form :data="managerExtForm" :disabled="!editingExt" layout="inline" class="profile-form">
              <t-form-item label="联系电话">
                <t-input v-model="managerExtForm.phone" placeholder="请输入联系电话" />
              </t-form-item>
              <t-form-item label="机构名称">
                <t-input v-model="managerExtForm.institutionName" placeholder="请输入机构名称" />
              </t-form-item>
              <t-form-item label="管辖区域">
                <t-input v-model="managerExtForm.jurisdiction" placeholder="请输入管辖区域" />
              </t-form-item>
            </t-form>
          </t-card>
        </t-col>
      </t-row>
    </t-loading>

    <t-dialog
      :visible.sync="passwordDialogVisible"
      header="修改密码"
      width="450px"
      :confirm-btn="{ content: '确认修改', theme: 'primary', loading: savingPassword }"
      @confirm="handlePasswordSubmit"
      @cancel="resetPasswordForm"
    >
      <t-form :data="passwordForm" layout="vertical">
        <t-form-item label="当前密码">
          <t-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" />
        </t-form-item>
        <t-form-item label="新密码">
          <t-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
        </t-form-item>
        <t-form-item label="确认新密码">
          <t-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
        </t-form-item>
      </t-form>
    </t-dialog>
  </div>
</template>

<script>
import { EditIcon } from 'tdesign-icons-vue';
import { updateProfile } from '@/api/water/user';

export default {
  name: 'UserProfile',
  components: {
    EditIcon,
  },
  data() {
    return {
      loading: false,
      editingBasic: false,
      savingBasic: false,
      editingExt: false,
      savingExt: false,
      passwordDialogVisible: false,
      savingPassword: false,
      userInfo: {
        userId: '',
        userAccount: '',
        userName: '',
        userRole: '',
        userStatus: 1,
        accountAddress: '',
        extInfo: '',
      },
      basicForm: {
        userName: '',
      },
      farmerExtForm: {
        phone: '',
        farmName: '',
        farmAddress: '',
      },
      managerExtForm: {
        phone: '',
        institutionName: '',
        jurisdiction: '',
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
      },
    };
  },
  computed: {
    userRole() {
      return this.userInfo.userRole || localStorage.getItem('platformUserRole') || '';
    },
    roleText() {
      const map = {
        admin: '系统管理员',
        farmers: '养殖户',
        manager: '监管端',
      };
      return map[this.userRole] || '未知角色';
    },
    roleTheme() {
      const map = {
        admin: 'warning',
        farmers: 'success',
        manager: 'primary',
      };
      return map[this.userRole] || 'default';
    },
    avatarText() {
      const name = this.userInfo.userName || this.userInfo.userAccount || 'U';
      return name.charAt(0).toUpperCase();
    },
  },
  created() {
    this.fetchUserInfo();
  },
  methods: {
    async fetchUserInfo() {
      this.loading = true;
      try {
        const data = await this.$store.dispatch('user/getUserInfo');
        const userResp = data?.userResp || data || {};
        this.userInfo = {
          userId: userResp.userId || '',
          userAccount: userResp.userAccount || '',
          userName: userResp.userName || '',
          userRole: userResp.userRole || '',
          userStatus: userResp.userStatus ?? 1,
          accountAddress: userResp.accountAddress || '',
          extInfo: userResp.extInfo || '',
        };
        this.basicForm.userName = this.userInfo.userName;
        this.parseExtInfo();
      } catch (error) {
        console.error('获取用户信息失败:', error);
        this.$message.error('获取用户信息失败');
      } finally {
        this.loading = false;
      }
    },
    parseExtInfo() {
      if (!this.userInfo.extInfo) return;
      try {
        const ext = JSON.parse(this.userInfo.extInfo);
        if (this.userRole === 'farmers') {
          this.farmerExtForm = {
            phone: ext.phone || '',
            farmName: ext.farmName || '',
            farmAddress: ext.farmAddress || '',
          };
        } else if (this.userRole === 'manager') {
          this.managerExtForm = {
            phone: ext.phone || '',
            institutionName: ext.institutionName || '',
            jurisdiction: ext.jurisdiction || '',
          };
        }
      } catch (e) {
        console.warn('解析扩展信息失败:', e);
      }
    },
    startEditBasic() {
      this.editingBasic = true;
      this.basicForm.userName = this.userInfo.userName;
    },
    cancelEditBasic() {
      this.editingBasic = false;
      this.basicForm.userName = this.userInfo.userName;
    },
    async saveBasic() {
      if (!this.basicForm.userName) {
        this.$message.warning('请输入用户名称');
        return;
      }
      this.savingBasic = true;
      try {
        const res = await updateProfile({ userName: this.basicForm.userName });
        if (res.code === 0) {
          this.userInfo.userName = this.basicForm.userName;
          this.editingBasic = false;
          this.$message.success('修改成功');
          this.$store.commit('user/setUserInfo', { userResp: this.userInfo });
        } else {
          this.$message.error(res.message || '修改失败');
        }
      } catch (error) {
        console.error('修改基本信息失败:', error);
        this.$message.error('修改失败');
      } finally {
        this.savingBasic = false;
      }
    },
    startEditExt() {
      this.editingExt = true;
      this.parseExtInfo();
    },
    cancelEditExt() {
      this.editingExt = false;
      this.parseExtInfo();
    },
    async saveExt() {
      this.savingExt = true;
      try {
        let extInfo = '';
        if (this.userRole === 'farmers') {
          extInfo = JSON.stringify(this.farmerExtForm);
        } else if (this.userRole === 'manager') {
          extInfo = JSON.stringify(this.managerExtForm);
        }
        const res = await updateProfile({ extInfo });
        if (res.code === 0) {
          this.userInfo.extInfo = extInfo;
          this.editingExt = false;
          this.$message.success('修改成功');
        } else {
          this.$message.error(res.message || '修改失败');
        }
      } catch (error) {
        console.error('修改扩展信息失败:', error);
        this.$message.error('修改失败');
      } finally {
        this.savingExt = false;
      }
    },
    showPasswordDialog() {
      this.passwordDialogVisible = true;
      this.resetPasswordForm();
    },
    resetPasswordForm() {
      this.passwordForm = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
      };
    },
    async handlePasswordSubmit() {
      const { oldPassword, newPassword, confirmPassword } = this.passwordForm;
      if (!oldPassword || !newPassword || !confirmPassword) {
        this.$message.warning('请填写完整信息');
        return;
      }
      if (newPassword !== confirmPassword) {
        this.$message.warning('两次输入的新密码不一致');
        return;
      }
      if (newPassword.length < 6) {
        this.$message.warning('密码长度不能少于6位');
        return;
      }
      this.savingPassword = true;
      try {
        const res = await updateProfile({
          oldPassword,
          userPassword: newPassword,
        });
        if (res.code === 0) {
          this.$message.success('密码修改成功，请重新登录');
          this.passwordDialogVisible = false;
          this.resetPasswordForm();
          setTimeout(() => {
            this.$store.dispatch('user/logout');
            this.$router.push('/login');
          }, 1500);
        } else {
          this.$message.error(res.message || '修改失败');
        }
      } catch (error) {
        console.error('修改密码失败:', error);
        this.$message.error('修改失败');
      } finally {
        this.savingPassword = false;
      }
    },
  },
};
</script>

<style scoped lang="less">
.user-profile-page {
  padding: 0;
}

.profile-card {
  border-radius: 16px;
  border: 1px solid var(--td-border-level-1-color);

  :deep(.t-card__title) {
    font-size: 18px;
    font-weight: 600;
  }
}

.profile-section {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.profile-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  min-width: 120px;
}

.profile-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--td-text-color-primary);
}

.profile-form {
  flex: 1;

  :deep(.t-form-item) {
    width: 100%;
    margin-bottom: 20px;
  }

  :deep(.t-form-item__label) {
    width: 100px;
    text-align: right;
    padding-right: 12px;
  }

  :deep(.t-form-item__content) {
    flex: 1;
  }

  :deep(.t-input) {
    width: 100%;
    max-width: 320px;
  }
}

.chain-address {
  font-family: monospace;
  font-size: 13px;
  color: var(--td-text-color-secondary);
  word-break: break-all;
  max-width: 320px;
}

.security-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-radius: 12px;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
  border: 1px solid var(--td-border-level-1-color);

  &__title {
    font-size: 15px;
    font-weight: 600;
    color: var(--td-text-color-primary);
  }

  &__desc {
    margin-top: 4px;
    font-size: 13px;
    color: var(--td-text-color-secondary);
  }
}

@media (max-width: 992px) {
  .profile-section {
    flex-direction: column;
    align-items: center;
  }

  .profile-form {
    width: 100%;
  }
}
</style>
