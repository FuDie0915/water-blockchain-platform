<template>
  <div class="marine-page dashboard-page">
    <t-loading :loading="loading" text="加载区块链看板中...">
      <section class="marine-page__hero">
        <div class="ocean-card hero-panel">
          <div class="hero-badge">可信监管 · 海洋环保 · 实时链上数据</div>
          <h1>海洋环保区块链总览</h1>
          <p>
            实时展示区块高度、节点运行状态和链上交易活跃度，
            为海水养殖可信监管提供统一看板入口。
          </p>
          <div class="hero-actions">
            <t-button theme="primary" @click="goTo('/water/index')">进入水质监管</t-button>
            <t-button variant="outline" @click="showAiTip">打开 AI 助手</t-button>
          </div>
        </div>

        <div class="ocean-card status-panel">
          <div class="status-panel__label">节点健康度</div>
          <div class="status-panel__value">{{ healthyNodeCount }}/{{ boardData.nodeCount || 0 }}</div>
          <div class="status-panel__text">
            当前在线节点可支撑许可证审批、水数据查询、上链核验与智能辅助问答。
          </div>
        </div>
      </section>

      <t-row :gutter="[16, 16]" class="stats-row">
        <t-col v-for="item in stats" :key="item.title" :xs="12" :sm="6" :xl="3">
          <t-card :bordered="false" class="stat-card">
            <div class="stat-card__label">{{ item.title }}</div>
            <div class="stat-card__value">{{ item.value }}</div>
            <div class="stat-card__desc">{{ item.description }}</div>
          </t-card>
        </t-col>
      </t-row>

      <t-row :gutter="[16, 16]" class="content-row">
        <t-col :xs="12" :lg="8">
          <t-card title="核心业务入口" :bordered="false" class="quick-card">
            <div class="quick-list">
              <div class="quick-item" v-for="item in quickEntries" :key="item.title">
                <div class="quick-item__content">
                  <div class="quick-item__title">{{ item.title }}</div>
                  <div class="quick-item__desc">{{ item.description }}</div>
                </div>
                <t-button size="small" variant="outline" @click="goTo(item.path)">进入</t-button>
              </div>
            </div>
          </t-card>
        </t-col>
        <t-col :xs="12" :lg="4" class="ability-col">
          <t-card title="平台能力" :bordered="false" class="quick-card ability-card">
            <div class="ability-list">
              <div class="quick-item quick-item--plain" v-for="item in platformCapabilities" :key="item.title">
                <div class="quick-item__content">
                  <div class="quick-item__title">{{ item.title }}</div>
                  <div class="quick-item__desc">{{ item.description }}</div>
                </div>
              </div>
            </div>
          </t-card>
        </t-col>
      </t-row>

      <t-card title="区块链节点状态" :bordered="false" class="table-card">
        <t-table
          row-key="nodeId"
          :data="boardData.nodeStatusList || []"
          :columns="columns"
          :hover="true"
          table-layout="auto"
          :pagination="tablePagination"
        >
          <template #status="{ row }">
            <t-tag :theme="row.status === 1 ? 'success' : 'warning'" variant="light-outline">
              {{ row.status === 1 ? '正常' : '异常' }}
            </t-tag>
          </template>
          <template #latestStatusUpdateTime="{ row }">
            {{ formatTime(row.latestStatusUpdateTime) }}
          </template>
        </t-table>
      </t-card>
    </t-loading>
  </div>
</template>

<script>
import { getBlockchainBoard } from '@/api/common/common';

export default {
  name: 'DashboardBase',
  data() {
    return {
      loading: false,
      boardData: {
        blockCount: 0,
        nodeCount: 0,
        tranCount: 0,
        tranCount2: 0,
        nodeStatusList: [],
      },
      columns: [
        {
          colKey: 'nodeId',
          title: '节点 ID',
          ellipsis: true,
        },
        {
          colKey: 'blockNumber',
          title: '块高',
          width: 120,
        },
        {
          colKey: 'pbftView',
          title: 'PBFT 视图',
          width: 120,
        },
        {
          colKey: 'status',
          title: '状态',
          width: 100,
          cell: 'status',
        },
        {
          colKey: 'latestStatusUpdateTime',
          title: '最后更新时间',
          width: 180,
          cell: 'latestStatusUpdateTime',
        },
      ],
      quickEntries: [
        {
          title: '水质监管工作台',
          description: '进入企业申请、监管审批与链上校验流程。',
          path: '/water/index',
        },
        {
          title: '企业登录入口',
          description: '快速进入许可证提交与状态查询页面。',
          path: '/water/enterprise-login',
        },
        {
          title: '监管局入口',
          description: '进行审批、水数据查看与上链操作。',
          path: '/water/monitor-login',
        },
      ],
      platformCapabilities: [
        {
          title: '区块链运行总览',
          description: '展示节点状态、区块高度与交易活跃度。',
        },
        {
          title: '许可证审批与数据监管',
          description: '支持企业申报、监管审批、结果追踪与水数据处置。',
        },
        {
          title: '智能辅助问答',
          description: '结合右下角 AI 助手快速获取流程说明。',
        },
      ],
      tablePagination: {
        current: 1,
        pageSize: 5,
        total: 0,
      },
    };
  },
  computed: {
    stats() {
      return [
        {
          title: '当前区块高度',
          value: this.boardData.blockCount || 0,
          description: '反映链上账本同步进度',
        },
        {
          title: '节点总数',
          value: this.boardData.nodeCount || 0,
          description: '支撑可信监管的网络规模',
        },
        {
          title: '累计交易数',
          value: this.boardData.tranCount || 0,
          description: '许可证与水数据相关链上记录',
        },
        {
          title: '异常交易数',
          value: this.boardData.tranCount2 || 0,
          description: '根据链上失败交易实时统计',
        },
      ];
    },
    healthyNodeCount() {
      return (this.boardData.nodeStatusList || []).filter((item) => item.status === 1).length;
    },
  },
  created() {
    this.fetchBoard();
  },
  methods: {
    async fetchBoard() {
      this.loading = true;
      try {
        const response = await getBlockchainBoard();
        if (response?.code === 0 && response.data) {
          this.boardData = {
            ...this.boardData,
            ...response.data,
            nodeStatusList: response.data.nodeStatusList || [],
          };
          this.tablePagination = {
            ...this.tablePagination,
            total: this.boardData.nodeCount || this.boardData.nodeStatusList.length || 0,
          };
        }
      } finally {
        this.loading = false;
      }
    },
    goTo(path) {
      this.$router.push(path);
    },
    showAiTip() {
      this.$message.info('右下角悬浮球已接入 AI 助手，可直接打开智能问答。');
    },
    formatTime(value) {
      if (!value) return '--';
      return new Date(value).toLocaleString();
    },
  },
};
</script>

<style scoped lang="less">
.hero-panel,
.status-panel,
.stat-card,
.quick-card,
.table-card {
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
    max-width: 760px;
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

.hero-actions {
  display: flex;
  gap: 12px;
  margin-top: 22px;
  flex-wrap: wrap;
}

.status-panel {
  padding: 24px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: linear-gradient(135deg, #1f74d8 0%, #0da8c5 100%);
  color: #fff;

  &__label {
    font-size: 14px;
    opacity: 0.85;
  }

  &__value {
    margin: 12px 0;
    font-size: 42px;
    font-weight: 700;
  }

  &__text {
    line-height: 1.8;
    opacity: 0.92;
  }
}

.stats-row,
.content-row {
  margin-bottom: 16px;
}

.content-row {
  align-items: stretch;
}

.content-row > .t-col {
  display: flex;
}

.stat-card {
  min-height: 160px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.99) 0%, rgba(238, 247, 255, 0.96) 100%);

  &__label {
    font-size: 14px;
    color: #5e738a;
  }

  &__value {
    margin: 12px 0 10px;
    font-size: 32px;
    font-weight: 700;
    color: #12314d;
  }

  &__desc {
    font-size: 13px;
    line-height: 1.7;
    color: #6a7f95;
  }
}

.quick-list,
.ability-list {
  display: grid;
  grid-template-rows: repeat(3, minmax(86px, 1fr));
  gap: 12px;
  flex: 1;
}

.quick-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 86px;
  padding: 14px 16px;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fbff 0%, #f1f8ff 100%);
  border: 1px solid var(--marine-divider);

  &__content {
    min-width: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
    line-height: 1.5;
    color: #12314d;
  }

  &__desc {
    margin-top: 6px;
    font-size: 13px;
    line-height: 1.7;
    color: #6a7f95;
  }

  &--plain {
    justify-content: flex-start;
  }
}

.quick-card {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;

  :deep(.t-card__body) {
    height: 100%;
    display: flex;
    flex-direction: column;
  }
}

.ability-card {
  :deep(.t-card__body) {
    padding-top: 10px;
  }
}


.table-card {
  :deep(.t-card__body) {
    padding-top: 8px;
  }
}

@media (max-width: 992px) {
  .hero-panel {
    padding: 20px;

    h1 {
      font-size: 28px;
    }
  }

  .status-panel {
    &__value {
      font-size: 34px;
    }
  }

  .quick-item {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
