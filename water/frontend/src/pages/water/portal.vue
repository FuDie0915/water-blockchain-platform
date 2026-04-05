<template>
  <div class="marine-page water-portal">
    <section class="marine-page__hero">
      <div class="ocean-card hero-panel">
        <div class="hero-badge">海水养殖 · 许可审批 · 数据上链</div>
        <h1>水质监管工作台</h1>
        <p>
          面向企业申报、监管审批、数据审查与智能辅助场景，提供统一清晰的业务入口与操作视图。
        </p>
      </div>

      <div class="ocean-card auth-card">
        <div class="auth-card__title">当前认证状态</div>
        <div class="auth-list">
          <div class="auth-item">
            <span>平台登录态</span>
            <t-tag :theme="authStatus.satoken ? 'success' : 'default'" variant="light-outline">
              {{ authStatus.satoken ? '已登录' : '未登录' }}
            </t-tag>
          </div>
          <div class="auth-item">
            <span>企业令牌</span>
            <t-tag :theme="authStatus.companytoken ? 'success' : 'warning'" variant="light-outline">
              {{ authStatus.companytoken ? '已就绪' : '未获取' }}
            </t-tag>
          </div>
          <div class="auth-item">
            <span>监管令牌</span>
            <t-tag :theme="authStatus.managertoken ? 'success' : 'warning'" variant="light-outline">
              {{ authStatus.managertoken ? '已就绪' : '未获取' }}
            </t-tag>
          </div>
        </div>
      </div>
    </section>

    <t-row :gutter="[16, 16]" class="role-row">
      <t-col v-for="panel in rolePanels" :key="panel.title" :xs="12" :lg="6">
        <t-card :title="panel.title" :bordered="false" class="role-card">
          <div class="role-card__body">
            <div>
              <div class="role-card__desc">{{ panel.description }}</div>
              <ul class="role-card__list">
                <li v-for="item in panel.features" :key="item">{{ item }}</li>
              </ul>
            </div>
            <div class="role-card__action">
              <t-button theme="primary" variant="outline" @click="go(panel.path)">{{ panel.buttonText }}</t-button>
            </div>
          </div>
        </t-card>
      </t-col>
    </t-row>

    <t-card title="核心业务能力" :bordered="false" class="workflow-card">
      <div class="workflow-grid">
        <div class="workflow-item" v-for="item in workflows" :key="item.title">
          <div class="workflow-item__title">{{ item.title }}</div>
          <div class="workflow-item__desc">{{ item.description }}</div>
          <div class="workflow-item__api">{{ item.api }}</div>
        </div>
      </div>
    </t-card>
  </div>
</template>

<script>
export default {
  name: 'WaterPortal',
  data() {
    return {
      authStatus: {
        satoken: false,
        companytoken: false,
        managertoken: false,
      },
      rolePanels: [
        {
          title: '企业工作台',
          description: '用于提交许可证申请、上传材料并查看链上校验结果。',
          path: '/water/enterprise-login',
          buttonText: '进入企业端',
          features: [
            '提交许可证申请',
            '查看申请进度',
            '执行链上核验',
          ],
        },
        {
          title: '监管工作台',
          description: '用于许可证审批、水数据分页查看与链上存证操作。',
          path: '/water/monitor-login',
          buttonText: '进入监管端',
          features: [
            '处理许可证审批',
            '查看水质数据',
            '执行批量上链',
          ],
        },
      ],
      workflows: [
        {
          title: '许可证审批闭环',
          description: '企业提交申请后，监管方可以直接审核并进行链上核验。',
          api: '企业申报 · 监管审批 · 结果追踪',
        },
        {
          title: '水质数据分页监管',
          description: '支持按数据类型分页查询并对异常数据执行上链操作。',
          api: '数据检索 · 异常识别 · 批量上链',
        },
        {
          title: '文件上传与材料留痕',
          description: '许可证附件可统一上传留痕，方便后续审查与复核。',
          api: '材料上传 · 留痕归档',
        },
        {
          title: '智能辅助问答',
          description: '支持智能问答，辅助解释监管流程和异常处置建议。',
          api: '智能咨询 · 辅助决策',
        },
      ],
    };
  },
  created() {
    this.refreshAuthStatus();
  },
  methods: {
    refreshAuthStatus() {
      this.authStatus = {
        satoken: !!localStorage.getItem('satoken'),
        companytoken: !!localStorage.getItem('companytoken'),
        managertoken: !!localStorage.getItem('managertoken'),
      };
    },
    go(path) {
      this.$router.push(path);
    },
  },
};
</script>

<style scoped lang="less">
.hero-panel,
.auth-card,
.role-card,
.workflow-card {
  border-radius: 20px;
  border: 1px solid var(--marine-border-strong);
}

.hero-panel {
  padding: 28px;

  h1 {
    margin: 14px 0 12px;
    font-size: 34px;
    line-height: 1.25;
    color: #12314d;
  }

  p {
    margin: 0;
    line-height: 1.8;
    color: #5e738a;
  }
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(31, 116, 216, 0.10);
  color: #1f74d8;
  font-size: 12px;
  font-weight: 600;
}

.auth-card {
  padding: 24px;

  &__title {
    font-size: 18px;
    font-weight: 600;
    color: #12314d;
    margin-bottom: 16px;
  }
}

.auth-list {
  display: grid;
  gap: 12px;
}

.auth-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fbff 0%, #f1f8ff 100%);
  border: 1px solid var(--marine-divider);
  color: #4f6780;
}

.role-row {
  margin-bottom: 16px;
}

.role-card {
  min-height: 260px;

  :deep(.t-card__header) {
    padding-bottom: 10px;
  }

  :deep(.t-card__title) {
    font-size: 24px;
    font-weight: 700;
    color: var(--marine-text);
  }

  :deep(.t-card__body) {
    height: 100%;
  }

  &__body {
    min-height: 190px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    gap: 16px;
  }

  &__desc {
    color: #5e738a;
    line-height: 1.8;
    margin-bottom: 12px;
  }

  &__list {
    margin: 0;
    padding-left: 18px;
    display: grid;
    gap: 8px;
    color: #4f6780;
    line-height: 1.7;
  }

  &__action {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    padding-top: 10px;
    border-top: 1px solid var(--marine-divider);
  }
}

.workflow-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.workflow-item {
  padding: 16px 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f8fbff 0%, #eef7ff 100%);
  border: 1px solid var(--marine-divider);

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #12314d;
  }

  &__desc {
    margin-top: 8px;
    color: #5e738a;
    line-height: 1.7;
  }

  &__api {
    margin-top: 10px;
    color: #1f74d8;
    font-size: 13px;
    font-weight: 500;
  }
}

@media (max-width: 992px) {
  .hero-panel {
    padding: 20px;

    h1 {
      font-size: 28px;
    }
  }

  .workflow-grid {
    grid-template-columns: 1fr;
  }
}
</style>