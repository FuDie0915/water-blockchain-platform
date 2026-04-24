<template>
  <div class="marine-page water-portal">
    <section class="marine-page__hero">
      <div class="ocean-card hero-panel">
        <div class="hero-badge">海水养殖 · 许可审批 · 数据上链</div>
        <h1>平台总览</h1>
        <p>
          聚合展示区块链运行态、业务概览与可信留痕信息，作为平台总览入口使用。
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
            <span>当前角色</span>
            <t-tag :theme="authStatus.role ? 'primary' : 'default'" variant="light-outline">
              {{ formatRole(authStatus.role) }}
            </t-tag>
          </div>
          <div class="auth-item">
            <span>养殖户令牌</span>
            <t-tag :theme="authStatus.farmertoken ? 'success' : 'warning'" variant="light-outline">
              {{ authStatus.farmertoken ? '已就绪' : '未获取' }}
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
      <t-col v-for="panel in rolePanels" :key="panel.title" :xs="12" :lg="12">
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
        role: '',
        farmertoken: false,
        managertoken: false,
      },
      rolePanels: [
        {
          title: '系统管理员端',
          description: '用于管理养殖户/监管账号、阈值规则、节点状态、日志备份与全平台公告。',
          path: '/dashboard/base',
          buttonText: '进入系统管理端',
          features: ['权限管理', '阈值配置', '节点与备份监控'],
        },
      ],
      workflows: [
        {
          title: '养殖户驾驶舱',
          description: '首页汇总水温、盐度、pH、溶解氧、氨氮、亚硝酸盐和预警状态。',
          api: '仪表盘 · 指标卡片 · 消息提醒',
        },
        {
          title: '阈值化水质监测',
          description: '按参考阈值自动区分正常、预警、危险，并保留后续上链能力。',
          api: '趋势曲线 · 阈值判定 · 报告导出',
        },
        {
          title: '全过程记录与溯源',
          description: '覆盖苗种投放、投喂、用药、收获等全过程记录与存证台账。',
          api: '台账记录 · 存证编号 · 链上核验',
        },
        {
          title: '智能预警联动',
          description: '结合设备、水质和养殖规范，形成待处理预警和监管联动闭环。',
          api: '实时告警 · 处理留痕 · 风险复核',
        },
        {
          title: '系统管理员配置中心',
          description: '统一维护账号开通、预警阈值、节点巡检、系统日志与公告发布。',
          api: '权限开通 · 节点监控 · 公告推送',
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
        role: localStorage.getItem('platformUserRole') || '',
        farmertoken: !!localStorage.getItem('farmertoken'),
        managertoken: !!localStorage.getItem('managertoken'),
      };
    },
    formatRole(role) {
      if (role === 'admin') return '平台总览';
      if (role === 'manager') return '监管端';
      if (role === 'farmers') return '养殖户端';
      return '未识别';
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