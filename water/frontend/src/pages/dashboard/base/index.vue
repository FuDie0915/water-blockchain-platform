<template>
  <div class="marine-page dashboard-page">
    <t-loading :loading="loading" text="加载系统管理控制台中...">
      <section class="marine-page__hero">
        <div class="ocean-card hero-panel">
          <div class="hero-badge">系统管理员端 · 权限治理 · 节点巡检</div>
          <h1>海水养殖系统管理员控制台</h1>
          <p>
            面向平台管理员统一处理账号开通、阈值配置、节点巡检、日志备份和公告发布，
            可直接查看整体管理流程与平台运行概况。
          </p>

        </div>

        <div class="ocean-card status-panel">
          <div class="status-panel__label">节点健康度</div>
          <div class="status-panel__value">{{ healthyNodeCount }}/{{ boardData.nodeCount || 0 }}</div>
          <div class="status-panel__text">
            最近备份：{{ latestBackupTime }} · 当前可支撑账号管理、阈值下发与公告推送。
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
          <t-card title="全局数据总览" :bordered="false" class="panel-card">
            <div class="section-tip">统一查看养殖户、监管端、预警事件、上链进度与许可证处理情况，便于管理员掌握平台整体运行态势。</div>
            <div class="overview-grid">
              <div v-for="item in businessOverviewCards" :key="item.title" class="overview-item">
                <div class="overview-item__label">{{ item.title }}</div>
                <div class="overview-item__value">{{ item.value }}</div>
                <div class="overview-item__desc">{{ item.description }}</div>
              </div>
            </div>
            <div class="overview-table">
              <div v-for="item in dataOverviewRows" :key="item.title" class="overview-table__row">
                <div>
                  <div class="overview-table__title">{{ item.title }}</div>
                  <div class="overview-table__desc">{{ item.description }}</div>
                </div>
                <div class="overview-table__value">{{ item.value }}</div>
              </div>
            </div>
          </t-card>
        </t-col>

        <t-col :xs="12" :lg="4">
          <t-card title="平台治理摘要" :bordered="false" class="panel-card">
            <div class="summary-list">
              <div v-for="item in governanceFocusList" :key="item.title" class="summary-list__item">
                <div>
                  <div class="summary-list__title">{{ item.title }}</div>
                  <div class="summary-list__desc">{{ item.description }}</div>
                </div>
                <t-tag :theme="item.theme" variant="light-outline">{{ item.status }}</t-tag>
              </div>
            </div>
          </t-card>
        </t-col>
      </t-row>

      <t-row :gutter="[16, 16]" class="content-row">
        <t-col :xs="12" :lg="8">
          <t-card title="用户权限管理" :bordered="false" class="panel-card table-card">
            <div class="section-tip">面向管理员分别管理养殖户与监管账号，可查看主体、片区、状态和最近活跃情况。</div>
            <div class="account-split-grid">
              <div class="account-split-panel">
                <div class="account-split-panel__title">养殖户管理</div>
                <t-table
                  row-key="id"
                  :data="farmerAccounts"
                  :columns="accountColumns"
                  bordered
                  size="small"
                  max-height="260"
                  :pagination="false"
                />
              </div>
              <div class="account-split-panel">
                <div class="account-split-panel__title">监管端管理</div>
                <t-table
                  row-key="id"
                  :data="managerAccounts"
                  :columns="accountColumns"
                  bordered
                  size="small"
                  max-height="260"
                  :pagination="false"
                />
              </div>
            </div>
          </t-card>
        </t-col>

        <t-col :xs="12" :lg="4">
          <t-card title="预警阈值配置" :bordered="false" class="panel-card">
            <div class="threshold-list">
              <div v-for="item in thresholdRules" :key="item.metric" class="threshold-item">
                <div class="threshold-item__top">
                  <span>{{ item.metric }}</span>
                  <t-tag theme="primary" variant="light-outline">{{ item.unit }}</t-tag>
                </div>
                <div class="threshold-item__range">正常：{{ item.normal }}</div>
                <div class="threshold-item__range">预警：{{ item.warning }}</div>
                <div class="threshold-item__range danger">危险：{{ item.danger }}</div>
                <div class="threshold-item__meta">最近更新：{{ item.updateTime }} · {{ item.updatedBy }}</div>
              </div>
            </div>
          </t-card>
        </t-col>
      </t-row>

      <t-row :gutter="[16, 16]" class="content-row">
        <t-col :xs="12" :lg="8">
          <t-card title="区块链节点状态监控" :bordered="false" class="panel-card table-card">
            <div class="section-tip">监控节点块高、PBFT 视图与最后上报时间，便于管理员快速判断区块链网络稳定性。</div>
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
        </t-col>

        <t-col :xs="12" :lg="4">
          <t-card title="系统日志与数据备份" :bordered="false" class="panel-card">
            <div class="backup-list">
              <div v-for="item in backupJobs" :key="item.id" class="backup-item">
                <div class="backup-item__top">
                  <span>{{ item.title }}</span>
                  <t-tag :theme="item.theme" variant="light">{{ item.status }}</t-tag>
                </div>
                <div class="backup-item__meta">{{ item.time }} · {{ item.size }}</div>
              </div>
            </div>

            <div class="log-list">
              <div v-for="item in systemLogs" :key="item.id" class="log-item">
                <div class="log-item__top">
                  <t-tag :theme="item.theme" variant="light-outline">{{ item.level }}</t-tag>
                  <span>{{ item.module }}</span>
                </div>
                <div class="log-item__desc">{{ item.message }}</div>
                <div class="log-item__time">{{ item.time }}</div>
              </div>
            </div>
          </t-card>
        </t-col>
      </t-row>

      <t-row :gutter="[16, 16]" class="content-row">
        <t-col :xs="12" :lg="8">
          <t-card title="公告发布" :bordered="false" class="panel-card">
            <div class="section-tip">支持向全部养殖户或指定角色推送平台通知，并统一留存发布记录。</div>
            <div class="notice-form-grid">
              <t-input v-model="noticeForm.title" placeholder="请输入公告标题，如：大风天气巡检提醒" />
              <t-select v-model="noticeForm.audience" :options="noticeAudienceOptions" placeholder="请选择推送对象" />
              <t-textarea v-model="noticeForm.content" :maxlength="160" placeholder="请输入公告内容，支持面向所有养殖户推送提醒" />
            </div>
            <div class="form-actions">
              <t-button theme="primary" @click="publishAnnouncement">发布公告</t-button>
              <t-button variant="outline" @click="resetNoticeForm">清空内容</t-button>
            </div>
          </t-card>
        </t-col>

        <t-col :xs="12" :lg="4">
          <t-card title="最近公告" :bordered="false" class="panel-card">
            <div class="announcement-list">
              <div v-for="item in announcements" :key="item.id" class="announcement-item">
                <div class="announcement-item__top">
                  <span>{{ item.title }}</span>
                  <t-tag :theme="item.theme" variant="light-outline">{{ formatNoticeAudience(item.audience) }}</t-tag>
                </div>
                <div class="announcement-item__desc">{{ item.content }}</div>
                <div class="announcement-item__time">{{ item.publishTime }}</div>
              </div>
            </div>
          </t-card>
        </t-col>
      </t-row>
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
      systemAccounts: [
        { id: 1, accountName: 'company_east_01', displayName: '蓝海一号养殖场', role: '养殖户', region: '东港示范区', status: '启用', lastActive: '今日 10:18', permissions: '水质上报 / 存证查询' },
        { id: 2, accountName: 'company_west_03', displayName: '澄海生态养殖合作社', role: '养殖户', region: '西湾养殖区', status: '启用', lastActive: '今日 09:50', permissions: '许可证申请 / 水质上报' },
        { id: 3, accountName: 'manager_city_01', displayName: '海洋监管一处', role: '监管账号', region: '市级监管', status: '启用', lastActive: '今日 10:02', permissions: '审批复核 / 数据上链' },
        { id: 4, accountName: 'manager_district_02', displayName: '西湾片区监管组', role: '监管账号', region: '西湾养殖区', status: '停用', lastActive: '昨日 16:24', permissions: '异常复核 / 预警处置' },
      ],
      accountColumns: [
        { colKey: 'accountName', title: '账号名', minWidth: 150 },
        { colKey: 'displayName', title: '主体名称', minWidth: 170 },
        {
          colKey: 'role',
          title: '角色类型',
          width: 120,
          cell: (h, { row }) => h('t-tag', { props: { theme: row.role === '监管账号' ? 'primary' : 'success', variant: 'light' } }, row.role),
        },
        { colKey: 'region', title: '所属片区', width: 130 },
        {
          colKey: 'status',
          title: '账号状态',
          width: 110,
          cell: (h, { row }) => h('t-tag', { props: { theme: row.status === '启用' ? 'success' : 'warning', variant: 'light-outline' } }, row.status),
        },
        { colKey: 'permissions', title: '主要权限', minWidth: 180 },
        { colKey: 'lastActive', title: '最近活跃', width: 120 },
        {
          colKey: 'operation',
          title: '操作',
          width: 160,
          cell: (h, { row }) => h('div', { style: { display: 'flex', gap: '8px', flexWrap: 'wrap' } }, [
            h('t-button', {
              props: {
                size: 'small',
                variant: 'text',
                theme: row.status === '启用' ? 'warning' : 'success',
              },
              on: { click: () => this.toggleAccountStatus(row) },
            }, row.status === '启用' ? '停用' : '启用'),
            h('t-button', {
              props: { size: 'small', variant: 'text', theme: 'primary' },
              on: { click: () => this.showPermissionTip(row) },
            }, '详情'),
          ]),
        },
      ],
      thresholdRules: [
        { metric: '溶解氧 DO', unit: 'mg/L', normal: '≥ 5', warning: '3 ~ 5', danger: '< 3', updatedBy: '管理员A', updateTime: '今日 08:30' },
        { metric: 'pH', unit: '', normal: '7.8 ~ 8.6', warning: '7.5 ~ 7.8 / 8.6 ~ 9.0', danger: '< 7.5 / > 9.0', updatedBy: '管理员A', updateTime: '今日 08:30' },
        { metric: '氨氮', unit: 'mg/L', normal: '≤ 0.2', warning: '0.2 ~ 0.5', danger: '> 0.5', updatedBy: '管理员B', updateTime: '昨日 17:20' },
      ],
      backupJobs: [
        { id: 1, title: 'MySQL 全量备份', time: '今日 03:00', size: '128 MB', status: '成功', theme: 'success' },
        { id: 2, title: '链上回执归档', time: '今日 03:20', size: '42 MB', status: '成功', theme: 'success' },
        { id: 3, title: '预警日志增量备份', time: '昨日 23:40', size: '18 MB', status: '待核验', theme: 'warning' },
      ],
      systemLogs: [
        { id: 1, level: 'INFO', theme: 'success', module: '账号管理', message: '已为“蓝海一号养殖场”开通养殖户账号与默认权限。', time: '今日 10:06' },
        { id: 2, level: 'WARN', theme: 'warning', module: '阈值配置', message: '管理员调整了西湾片区氨氮预警阈值，待监管端同步确认。', time: '今日 09:40' },
        { id: 3, level: 'INFO', theme: 'primary', module: '公告发布', message: '“大风天气巡检提醒” 已推送给全部养殖户。', time: '昨日 18:15' },
      ],
      noticeAudienceOptions: [
        { label: '全部养殖户', value: 'all-farmers' },
        { label: '全部监管账号', value: 'all-managers' },
        { label: '全平台', value: 'all' },
      ],
      noticeForm: {
        title: '',
        audience: 'all-farmers',
        content: '',
      },
      announcements: [
        { id: 1, title: '大风天气巡检提醒', audience: 'all-farmers', content: '请各养殖户于今日 18:00 前完成增氧设备、电源与围栏巡检。', publishTime: '今日 09:00', theme: 'primary' },
        { id: 2, title: '监管端周报提交通知', audience: 'all-managers', content: '请各片区监管员在周五 17:30 前提交本周异常处置汇总。', publishTime: '昨日 16:30', theme: 'warning' },
      ],
      tablePagination: {
        current: 1,
        pageSize: 5,
        total: 0,
      },
    };
  },
  computed: {
    farmerAccounts() {
      return this.systemAccounts.filter((item) => item.role === '养殖户');
    },
    managerAccounts() {
      return this.systemAccounts.filter((item) => item.role === '监管账号');
    },
    stats() {
      const farmerCount = this.farmerAccounts.length;
      const managerCount = this.managerAccounts.length;
      const enabledCount = this.systemAccounts.filter((item) => item.status === '启用').length;
      return [
        {
          title: '养殖户账号',
          value: farmerCount,
          description: '当前已开通并纳入平台管理的养殖主体',
        },
        {
          title: '监管账号',
          value: managerCount,
          description: '可执行审批、复核与抽检的监管账户',
        },
        {
          title: '启用账号数',
          value: enabledCount,
          description: '目前处于正常使用状态的系统账户',
        },
        {
          title: '公告已发布',
          value: this.announcements.length,
          description: '最近面向养殖户或监管端的通知条数',
        },
      ];
    },
    businessOverviewCards() {
      const warningCount = 3;
      const permitPending = 1;
      const chainRate = '98%';
      const totalPonds = 12;
      return [
        { title: '养殖池总数', value: `${totalPonds}个`, description: '当前纳入平台监管的养殖池规模' },
        { title: '活跃预警', value: `${warningCount}条`, description: '待管理员关注或派发的异常事件' },
        { title: '待审批许可', value: `${permitPending}项`, description: '监管端待处理的许可申请数量' },
        { title: '上链完整率', value: chainRate, description: '最近业务台账链上固化完成度' },
      ];
    },
    dataOverviewRows() {
      return [
        { title: '养殖户纳管情况', description: '已开通账号并纳入平台统一监管的养殖主体', value: `${this.farmerAccounts.length} 户` },
        { title: '监管端值守情况', description: '当前可参与审批、巡检和异常处置的监管账号', value: `${this.managerAccounts.length} 个` },
        { title: '区块链节点健康度', description: '用于支撑存证与核验的区块链节点在线情况', value: `${this.healthyNodeCount}/${this.boardData.nodeCount || 0}` },
        { title: '最近备份状态', description: '平台日志和回执文件的最近备份结果', value: this.latestBackupTime },
      ];
    },
    governanceFocusList() {
      return [
        { title: '养殖户账号治理', description: '重点检查新开户、停用与权限收敛情况。', status: '正常', theme: 'success' },
        { title: '监管端排班巡检', description: '确保各片区监管账号在线并及时处理告警。', status: '进行中', theme: 'primary' },
        { title: '全域数据留痕', description: '持续跟踪全过程记录的上链完整率与一致性。', status: '需复核', theme: 'warning' },
      ];
    },
    healthyNodeCount() {
      return (this.boardData.nodeStatusList || []).filter((item) => item.status === 1).length;
    },
    latestBackupTime() {
      return this.backupJobs[0]?.time || '--';
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
          if (!this.boardData.nodeStatusList.length) {
            this.applyMockBoard();
          }
        } else {
          this.applyMockBoard();
        }
      } catch (error) {
        console.error('加载区块链看板失败，已切换为备用数据:', error);
        this.applyMockBoard();
      } finally {
        this.tablePagination = {
          ...this.tablePagination,
          total: this.boardData.nodeCount || this.boardData.nodeStatusList.length || 0,
        };
        this.loading = false;
      }
    },
    applyMockBoard() {
      this.boardData = {
        blockCount: 28416,
        nodeCount: 4,
        tranCount: 1268,
        tranCount2: 9,
        nodeStatusList: [
          { nodeId: 'node0', blockNumber: 28416, pbftView: 7821, status: 1, latestStatusUpdateTime: Date.now() },
          { nodeId: 'node1', blockNumber: 28416, pbftView: 7821, status: 1, latestStatusUpdateTime: Date.now() - 30 * 1000 },
          { nodeId: 'node2', blockNumber: 28415, pbftView: 7820, status: 1, latestStatusUpdateTime: Date.now() - 45 * 1000 },
          { nodeId: 'node3', blockNumber: 28412, pbftView: 7818, status: 0, latestStatusUpdateTime: Date.now() - 3 * 60 * 1000 },
        ],
      };
    },
    goTo(path) {
      this.$router.push(path);
    },
    formatTime(value) {
      if (!value) return '--';
      return new Date(value).toLocaleString();
    },
    toggleAccountStatus(row) {
      row.status = row.status === '启用' ? '停用' : '启用';
      this.$message.success(`${row.displayName} 账号状态已切换为${row.status}`);
    },
    showPermissionTip(row) {
      this.$message.info(`${row.displayName}：当前权限为 ${row.permissions}`);
    },
    formatNoticeAudience(value) {
      const map = {
        'all-farmers': '全部养殖户',
        'all-managers': '全部监管端',
        all: '全平台',
      };
      return map[value] || '自定义';
    },
    publishAnnouncement() {
      if (!this.noticeForm.title || !this.noticeForm.content) {
        this.$message.warning('请先填写公告标题和内容');
        return;
      }
      this.announcements.unshift({
        id: Date.now(),
        title: this.noticeForm.title,
        audience: this.noticeForm.audience,
        content: this.noticeForm.content,
        publishTime: '刚刚',
        theme: 'success',
      });
      this.$message.success('公告已发布并推送到目标用户');
      this.resetNoticeForm();
    },
    resetNoticeForm() {
      this.noticeForm = {
        title: '',
        audience: 'all-farmers',
        content: '',
      };
    },
  },
};
</script>

<style scoped lang="less">
.hero-panel,
.status-panel,
.stat-card,
.panel-card,
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

.panel-card {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;

  :deep(.t-card__body) {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  :deep(.t-card__title) {
    font-size: 18px;
    font-weight: 600;
  }
}

.section-tip {
  margin-bottom: 12px;
  color: #5e738a;
  font-size: 13px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.overview-item {
  padding: 14px 16px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
  border: 1px solid var(--marine-divider);

  &__label {
    font-size: 13px;
    color: #6a7f95;
  }

  &__value {
    margin: 8px 0 6px;
    font-size: 28px;
    font-weight: 700;
    color: #12314d;
  }

  &__desc {
    font-size: 12px;
    line-height: 1.7;
    color: #6a7f95;
  }
}

.overview-table {
  display: grid;
  gap: 10px;

  &__row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 12px 14px;
    border-radius: 14px;
    background: #f8fbff;
  }

  &__title {
    font-size: 14px;
    font-weight: 600;
    color: #12314d;
  }

  &__desc {
    margin-top: 4px;
    font-size: 12px;
    color: #6a7f95;
  }

  &__value {
    font-size: 18px;
    font-weight: 700;
    color: #1f74d8;
    white-space: nowrap;
  }
}

.summary-list {
  display: grid;
  gap: 12px;

  &__item {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    padding: 14px 16px;
    border-radius: 16px;
    background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
    border: 1px solid var(--marine-divider);
  }

  &__title {
    font-size: 14px;
    font-weight: 600;
    color: #12314d;
  }

  &__desc {
    margin-top: 6px;
    font-size: 12px;
    line-height: 1.7;
    color: #6a7f95;
  }
}

.account-split-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.account-split-panel {
  padding: 12px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
  border: 1px solid var(--marine-divider);

  &__title {
    margin-bottom: 10px;
    font-size: 15px;
    font-weight: 600;
    color: #12314d;
  }
}

.threshold-list,
.log-list,
.announcement-list,
.backup-list {
  display: grid;
  gap: 12px;
}

.threshold-item,
.log-item,
.announcement-item,
.backup-item {
  padding: 14px 16px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
  border: 1px solid var(--marine-divider);
}

.threshold-item {
  &__top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    margin-bottom: 8px;
    color: #12314d;
    font-weight: 600;
  }

  &__range {
    margin-top: 6px;
    font-size: 13px;
    color: #5e738a;

    &.danger {
      color: #c64751;
    }
  }

  &__meta {
    margin-top: 8px;
    font-size: 12px;
    color: #6a7f95;
  }
}

.backup-item__top,
.log-item__top,
.announcement-item__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  color: #12314d;
  font-weight: 600;
}

.backup-item__meta,
.log-item__time,
.announcement-item__time {
  margin-top: 6px;
  font-size: 12px;
  color: #6a7f95;
}

.log-item__desc,
.announcement-item__desc {
  margin-top: 8px;
  line-height: 1.7;
  color: #4f6780;
}

.notice-form-grid {
  display: grid;
  gap: 12px;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 14px;
  flex-wrap: wrap;
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

  .overview-grid,
  .account-split-grid {
    grid-template-columns: 1fr;
  }

  .overview-table__row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
