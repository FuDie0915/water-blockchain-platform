<template>
  <div class="marine-page dashboard-page">
    <t-loading :loading="loading" text="加载系统管理控制台中...">
      <section class="marine-page__hero">
        <div class="ocean-card hero-panel">
          <div class="hero-badge">系统管理员端 · 权限治理 · 节点巡检</div>
          <h1>海水养殖系统管理员控制台</h1>
          <p>
            面向平台管理员统一处理账号开通、阈值配置、节点巡检、日志备份和公告发布。
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
            <template #actions>
              <t-button size="small" theme="primary" @click="openCreateUserDialog">新增用户</t-button>
            </template>
            <div class="section-tip">面向管理员分别管理养殖户与监管账号，可查看主体、片区、状态和最近活跃情况。</div>
            <div class="account-split-grid">
              <div class="account-split-panel">
                <div class="account-split-panel__title">养殖户管理</div>
                <t-table
                  row-key="userId"
                  :data="farmerAccounts"
                  :columns="accountColumns"
                  bordered
                  size="small"
                  max-height="260"
                  :pagination="false"
                  :loading="loadingUsers"
                />
              </div>
              <div class="account-split-panel">
                <div class="account-split-panel__title">监管端管理</div>
                <t-table
                  row-key="userId"
                  :data="managerAccounts"
                  :columns="accountColumns"
                  bordered
                  size="small"
                  max-height="260"
                  :pagination="false"
                  :loading="loadingUsers"
                />
              </div>
            </div>
          </t-card>
        </t-col>

        <t-col :xs="12" :lg="4">
          <t-card title="预警阈值配置" :bordered="false" class="panel-card">
            <template #actions>
              <t-button size="small" theme="primary" variant="outline" @click="openThresholdDialog">配置阈值</t-button>
            </template>
            <t-loading v-if="loadingThreshold" text="加载中..." />
            <div v-else class="threshold-list">
              <div v-for="item in thresholdRules" :key="item.metric" class="threshold-item">
                <div class="threshold-item__top">
                  <span>{{ item.metric }}</span>
                  <t-tag theme="primary" variant="light-outline">{{ item.unit }}</t-tag>
                </div>
                <div class="threshold-item__range">正常：{{ item.normal }}</div>
                <div class="threshold-item__range">预警：{{ item.warning }}</div>
                <div class="threshold-item__range danger">危险：{{ item.danger }}</div>
              </div>
              <div v-if="thresholdRules.length === 0" class="empty-tip">暂无阈值配置</div>
            </div>
          </t-card>
        </t-col>
      </t-row>

      <t-row :gutter="[16, 16]" class="content-row">
        <t-col :xs="12" :lg="12">
          <t-card title="监管局资质审核" :bordered="false" class="panel-card table-card">
            <div class="section-tip">审核监管局用户提交的资质认证申请，通过后监管局用户可正常使用系统功能。</div>
            <t-table
              row-key="id"
              :data="managerAuditList"
              :columns="managerAuditColumns"
              :hover="true"
              table-layout="auto"
              :loading="loadingManagerAudit"
              :empty="'暂无待审核申请'"
            />
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
            <div class="section-tip">支持向全部养殖户或指定角色推送平台通知。</div>
            <div class="notice-form-grid">
              <t-input v-model="noticeForm.title" placeholder="请输入公告标题，如：大风天气巡检提醒" />
              <t-select v-model="noticeForm.targetRole" :options="noticeAudienceOptions" placeholder="请选择推送对象" />
              <t-textarea v-model="noticeForm.content" :maxlength="500" placeholder="请输入公告内容" />
            </div>
            <div class="form-actions">
              <t-button theme="primary" :loading="publishing" @click="publishAnnouncement">发布公告</t-button>
              <t-button variant="outline" @click="resetNoticeForm">清空内容</t-button>
            </div>
          </t-card>
        </t-col>

        <t-col :xs="12" :lg="4">
          <t-card title="最近公告" :bordered="false" class="panel-card">
            <t-loading v-if="loadingAnnouncements" text="加载中..." />
            <div v-else class="announcement-list">
              <div v-for="item in announcements" :key="item.id" class="announcement-item">
                <div class="announcement-item__top">
                  <span>{{ item.title }}</span>
                  <t-tag theme="primary" variant="light-outline">{{ formatTargetRole(item.targetRole) }}</t-tag>
                </div>
                <div class="announcement-item__desc">{{ item.content }}</div>
                <div class="announcement-item__time">{{ item.createTime }}</div>
              </div>
              <div v-if="announcements.length === 0" class="empty-tip">暂无公告</div>
            </div>
          </t-card>
        </t-col>
      </t-row>
    </t-loading>

    <!-- 新增用户对话框 -->
    <t-dialog
      :visible.sync="createUserDialogVisible"
      header="新增用户"
      width="500px"
      :confirm-btn="{ content: '创建', theme: 'primary', loading: creatingUser }"
      @confirm="handleCreateUser"
      @cancel="resetCreateUserForm"
    >
      <t-form :data="createUserForm" layout="vertical">
        <t-form-item label="用户账号">
          <t-input v-model="createUserForm.userAccount" placeholder="请输入用户账号" />
        </t-form-item>
        <t-form-item label="用户名称">
          <t-input v-model="createUserForm.userName" placeholder="请输入用户名称" />
        </t-form-item>
        <t-form-item label="用户角色">
          <t-select v-model="createUserForm.userRole" :options="roleOptions" placeholder="请选择用户角色" />
        </t-form-item>
        <t-form-item label="联系电话">
          <t-input v-model="createUserForm.phone" placeholder="请输入联系电话" />
        </t-form-item>
        <t-form-item label="初始密码">
          <t-input v-model="createUserForm.userPassword" type="password" placeholder="请输入初始密码" />
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- 阈值配置对话框 -->
    <t-dialog
      :visible.sync="thresholdDialogVisible"
      header="预警阈值配置"
      width="600px"
      :confirm-btn="{ content: '保存', theme: 'primary', loading: savingThreshold }"
      @confirm="handleSaveThreshold"
      @cancel="thresholdDialogVisible = false"
    >
      <t-form :data="thresholdForm" layout="vertical">
        <t-row :gutter="[12, 12]">
          <t-col :span="6">
            <t-form-item label="溶解氧正常值(mg/L)">
              <t-input v-model="thresholdForm.doNormal" placeholder="≥ 5" />
            </t-form-item>
          </t-col>
          <t-col :span="6">
            <t-form-item label="溶解氧预警值">
              <t-input v-model="thresholdForm.doWarning" placeholder="3 ~ 5" />
            </t-form-item>
          </t-col>
          <t-col :span="6">
            <t-form-item label="pH正常范围">
              <t-input v-model="thresholdForm.phNormal" placeholder="7.8 ~ 8.6" />
            </t-form-item>
          </t-col>
          <t-col :span="6">
            <t-form-item label="pH预警范围">
              <t-input v-model="thresholdForm.phWarning" placeholder="7.5 ~ 7.8 / 8.6 ~ 9.0" />
            </t-form-item>
          </t-col>
          <t-col :span="6">
            <t-form-item label="氨氮正常值(mg/L)">
              <t-input v-model="thresholdForm.nh3Normal" placeholder="≤ 0.2" />
            </t-form-item>
          </t-col>
          <t-col :span="6">
            <t-form-item label="氨氮预警值">
              <t-input v-model="thresholdForm.nh3Warning" placeholder="0.2 ~ 0.5" />
            </t-form-item>
          </t-col>
        </t-row>
      </t-form>
    </t-dialog>
  </div>
</template>

<script>
import { getBlockchainBoard } from '@/api/common/common';
import { getAdminUserList, adminCreateUser, updateUserStatus } from '@/api/water/user';
import { getAdminAnnouncementList, adminPublishAnnouncement } from '@/api/water/announcement';
import { getAdminDashboard } from '@/api/water/dashboard';
import { getThreshold, createThreshold } from '@/api/water/water';
import { getAdminManagerAuditList, adminApproveManagerAudit, adminRejectManagerAudit } from '@/api/water/managerAudit';

export default {
  name: 'DashboardBase',
  data() {
    return {
      loading: false,
      loadingUsers: false,
      loadingAnnouncements: false,
      loadingThreshold: false,
      publishing: false,
      creatingUser: false,
      savingThreshold: false,
      createUserDialogVisible: false,
      thresholdDialogVisible: false,
      boardData: {
        blockCount: 0,
        nodeCount: 0,
        tranCount: 0,
        tranCount2: 0,
        nodeStatusList: [],
      },
      dashboardData: {},
      userList: [],
      announcements: [],
      thresholdData: null,
      // 监管局审核相关
      managerAuditList: [],
      loadingManagerAudit: false,
      managerAuditColumns: [
        { colKey: 'id', title: '申请ID', width: 80 },
        { colKey: 'institutionName', title: '机构名称', minWidth: 160 },
        { colKey: 'jurisdiction', title: '管辖区域', width: 140 },
        { colKey: 'phone', title: '联系电话', width: 120 },
        { colKey: 'createTime', title: '申请时间', width: 160 },
        {
          colKey: 'status',
          title: '审核状态',
          width: 100,
          cell: (h, { row }) => {
            const statusMap = {
              0: { theme: 'warning', text: '待审核' },
              1: { theme: 'success', text: '已通过' },
              2: { theme: 'danger', text: '已拒绝' },
            };
            const status = statusMap[row.status] || { theme: 'default', text: '未知' };
            return h('t-tag', { props: { theme: status.theme, variant: 'light' } }, status.text);
          },
        },
        {
          colKey: 'operation',
          title: '操作',
          width: 180,
          cell: (h, { row }) => {
            if (row.status !== 0) return h('span', { style: { color: '#6a7f95' } }, '--');
            return h('div', { style: { display: 'flex', gap: '8px' } }, [
              h('t-button', {
                props: { theme: 'success', variant: 'text', size: 'small' },
                on: { click: () => this.handleApproveManagerAudit(row) },
              }, '通过'),
              h('t-button', {
                props: { theme: 'danger', variant: 'text', size: 'small' },
                on: { click: () => this.handleRejectManagerAudit(row) },
              }, '拒绝'),
            ]);
          },
        },
      ],
      columns: [
        { colKey: 'nodeId', title: '节点 ID', ellipsis: true },
        { colKey: 'blockNumber', title: '块高', width: 120 },
        { colKey: 'pbftView', title: 'PBFT 视图', width: 120 },
        { colKey: 'status', title: '状态', width: 100, cell: 'status' },
        { colKey: 'latestStatusUpdateTime', title: '最后更新时间', width: 180, cell: 'latestStatusUpdateTime' },
      ],
      accountColumns: [
        { colKey: 'userAccount', title: '账号名', minWidth: 130 },
        { colKey: 'userName', title: '用户名称', minWidth: 120 },
        {
          colKey: 'userRole',
          title: '角色类型',
          width: 100,
          cell: (h, { row }) => h('t-tag', {
            props: { theme: row.userRole === 'manager' ? 'primary' : 'success', variant: 'light' }
          }, this.formatRole(row.userRole)),
        },
        {
          colKey: 'userStatus',
          title: '账号状态',
          width: 100,
          cell: (h, { row }) => h('t-tag', {
            props: { theme: row.userStatus === 1 ? 'success' : 'warning', variant: 'light-outline' }
          }, row.userStatus === 1 ? '启用' : '停用'),
        },
        {
          colKey: 'operation',
          title: '操作',
          width: 120,
          cell: (h, { row }) => h('t-button', {
            props: {
              size: 'small',
              variant: 'text',
              theme: row.userStatus === 1 ? 'warning' : 'success',
            },
            on: { click: () => this.toggleAccountStatus(row) },
          }, row.userStatus === 1 ? '停用' : '启用'),
        },
      ],
      noticeAudienceOptions: [
        { label: '全部养殖户', value: 'farmers' },
        { label: '全部监管端', value: 'manager' },
        { label: '全平台', value: 'all' },
      ],
      roleOptions: [
        { label: '养殖户', value: 'farmers' },
        { label: '监管端', value: 'manager' },
        { label: '管理员', value: 'admin' },
      ],
      noticeForm: {
        title: '',
        targetRole: 'farmers',
        content: '',
      },
      createUserForm: {
        userAccount: '',
        userName: '',
        userRole: 'farmers',
        phone: '',
        userPassword: '123456',
      },
      thresholdForm: {
        doNormal: '≥ 5',
        doWarning: '3 ~ 5',
        phNormal: '7.8 ~ 8.6',
        phWarning: '7.5 ~ 7.8 / 8.6 ~ 9.0',
        nh3Normal: '≤ 0.2',
        nh3Warning: '0.2 ~ 0.5',
      },
      backupJobs: [
        { id: 1, title: 'MySQL 全量备份', time: '今日 03:00', size: '128 MB', status: '成功', theme: 'success' },
        { id: 2, title: '链上回执归档', time: '今日 03:20', size: '42 MB', status: '成功', theme: 'success' },
      ],
      systemLogs: [
        { id: 1, level: 'INFO', theme: 'success', module: '账号管理', message: '系统运行正常', time: '今日 10:06' },
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
      return this.userList.filter((item) => item.userRole === 'farmers');
    },
    managerAccounts() {
      return this.userList.filter((item) => item.userRole === 'manager');
    },
    pendingManagerAuditCount() {
      return this.managerAuditList.filter((item) => item.status === 0).length;
    },
    stats() {
      return [
        { title: '养殖户账号', value: this.farmerAccounts.length, description: '当前已开通并纳入平台管理的养殖主体' },
        { title: '监管账号', value: this.managerAccounts.length, description: '可执行审批、复核与抽检的监管账户' },
        { title: '待审核监管局', value: this.pendingManagerAuditCount, description: '待审批的监管局资质申请数量' },
        { title: '公告已发布', value: this.announcements.length, description: '最近面向养殖户或监管端的通知条数' },
      ];
    },
    businessOverviewCards() {
      const data = this.dashboardData || {};
      return [
        { title: '养殖池总数', value: `${data.pondCount || 0}个`, description: '当前纳入平台监管的养殖池规模' },
        { title: '活跃预警', value: `${data.warningCount || 0}条`, description: '待管理员关注或派发的异常事件' },
        { title: '待审批许可', value: `${data.pendingPermitCount || 0}项`, description: '监管端待处理的许可申请数量' },
        { title: '上链完整率', value: `${data.chainRate || '98'}%`, description: '最近业务台账链上固化完成度' },
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
    thresholdRules() {
      if (!this.thresholdData) {
        return [
          { metric: '溶解氧 DO', unit: 'mg/L', normal: '≥ 5', warning: '3 ~ 5', danger: '< 3' },
          { metric: 'pH', unit: '', normal: '7.8 ~ 8.6', warning: '7.5 ~ 7.8 / 8.6 ~ 9.0', danger: '< 7.5 / > 9.0' },
          { metric: '氨氮', unit: 'mg/L', normal: '≤ 0.2', warning: '0.2 ~ 0.5', danger: '> 0.5' },
        ];
      }
      return [
        { metric: '溶解氧 DO', unit: 'mg/L', normal: this.thresholdData.doNormal || '≥ 5', warning: this.thresholdData.doWarning || '3 ~ 5', danger: '< 3' },
        { metric: 'pH', unit: '', normal: this.thresholdData.phNormal || '7.8 ~ 8.6', warning: this.thresholdData.phWarning || '7.5 ~ 7.8 / 8.6 ~ 9.0', danger: '< 7.5 / > 9.0' },
        { metric: '氨氮', unit: 'mg/L', normal: this.thresholdData.nh3Normal || '≤ 0.2', warning: this.thresholdData.nh3Warning || '0.2 ~ 0.5', danger: '> 0.5' },
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
    this.fetchAllData();
  },
  methods: {
    async fetchAllData() {
      this.loading = true;
      await Promise.all([
        this.fetchBoard(),
        this.fetchUsers(),
        this.fetchAnnouncements(),
        this.fetchDashboard(),
        this.fetchThreshold(),
        this.fetchManagerAuditList(),
      ]);
      this.loading = false;
    },
    async fetchBoard() {
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
        console.error('加载区块链看板失败:', error);
        this.applyMockBoard();
      } finally {
        this.tablePagination = {
          ...this.tablePagination,
          total: this.boardData.nodeCount || this.boardData.nodeStatusList.length || 0,
        };
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
          { nodeId: 'node1', blockNumber: 28416, pbftView: 7821, status: 1, latestStatusUpdateTime: Date.now() - 30000 },
          { nodeId: 'node2', blockNumber: 28415, pbftView: 7820, status: 1, latestStatusUpdateTime: Date.now() - 45000 },
          { nodeId: 'node3', blockNumber: 28412, pbftView: 7818, status: 0, latestStatusUpdateTime: Date.now() - 180000 },
        ],
      };
    },
    async fetchUsers() {
      this.loadingUsers = true;
      try {
        const res = await getAdminUserList();
        if (res.code === 0 && res.data) {
          // 后端返回 PageResponse，data 字段包含用户列表
          const pageData = res.data;
          this.userList = Array.isArray(pageData.data) ? pageData.data : pageData.records || [];
        }
      } catch (error) {
        console.error('加载用户列表失败:', error);
      } finally {
        this.loadingUsers = false;
      }
    },
    async fetchAnnouncements() {
      this.loadingAnnouncements = true;
      try {
        const res = await getAdminAnnouncementList();
        if (res.code === 0 && res.data) {
          // 公告接口返回 List<Announcement>，直接使用
          this.announcements = Array.isArray(res.data) ? res.data : [];
        }
      } catch (error) {
        console.error('加载公告列表失败:', error);
      } finally {
        this.loadingAnnouncements = false;
      }
    },
    async fetchDashboard() {
      try {
        const res = await getAdminDashboard();
        if (res.code === 0 && res.data) {
          this.dashboardData = res.data;
        }
      } catch (error) {
        console.error('加载看板数据失败:', error);
      }
    },
    async fetchThreshold() {
      this.loadingThreshold = true;
      try {
        const res = await getThreshold();
        if (res.code === 0 && res.data) {
          this.thresholdData = res.data;
        }
      } catch (error) {
        console.error('加载阈值配置失败:', error);
      } finally {
        this.loadingThreshold = false;
      }
    },
    formatTime(value) {
      if (!value) return '--';
      return new Date(value).toLocaleString();
    },
    formatRole(role) {
      const map = { farmers: '养殖户', manager: '监管端', admin: '管理员' };
      return map[role] || role;
    },
    formatTargetRole(role) {
      const map = { farmers: '全部养殖户', manager: '全部监管端', all: '全平台' };
      return map[role] || role;
    },
    async toggleAccountStatus(row) {
      try {
        const newStatus = row.userStatus === 1 ? 0 : 1;
        const res = await updateUserStatus({ userId: row.userId, userStatus: newStatus });
        if (res.code === 0) {
          row.userStatus = newStatus;
          this.$message.success(`${row.userName} 账号状态已切换为${newStatus === 1 ? '启用' : '停用'}`);
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        console.error('切换用户状态失败:', error);
        this.$message.error('操作失败');
      }
    },
    openCreateUserDialog() {
      this.createUserDialogVisible = true;
    },
    resetCreateUserForm() {
      this.createUserForm = {
        userAccount: '',
        userName: '',
        userRole: 'farmers',
        phone: '',
        userPassword: '123456',
      };
    },
    async handleCreateUser() {
      const { userAccount, userName, userRole, userPassword } = this.createUserForm;
      if (!userAccount || !userName || !userRole || !userPassword) {
        this.$message.warning('请填写完整信息');
        return;
      }
      this.creatingUser = true;
      try {
        const res = await adminCreateUser(this.createUserForm);
        if (res.code === 0) {
          this.$message.success('用户创建成功');
          this.createUserDialogVisible = false;
          this.resetCreateUserForm();
          this.fetchUsers();
        } else {
          this.$message.error(res.message || '创建失败');
        }
      } catch (error) {
        console.error('创建用户失败:', error);
        this.$message.error('创建用户失败');
      } finally {
        this.creatingUser = false;
      }
    },
    openThresholdDialog() {
      if (this.thresholdData) {
        this.thresholdForm = {
          doNormal: this.thresholdData.doNormal || '≥ 5',
          doWarning: this.thresholdData.doWarning || '3 ~ 5',
          phNormal: this.thresholdData.phNormal || '7.8 ~ 8.6',
          phWarning: this.thresholdData.phWarning || '7.5 ~ 7.8 / 8.6 ~ 9.0',
          nh3Normal: this.thresholdData.nh3Normal || '≤ 0.2',
          nh3Warning: this.thresholdData.nh3Warning || '0.2 ~ 0.5',
        };
      }
      this.thresholdDialogVisible = true;
    },
    async handleSaveThreshold() {
      this.savingThreshold = true;
      try {
        const res = await createThreshold(this.thresholdForm);
        if (res.code === 0) {
          this.$message.success('阈值配置保存成功');
          this.thresholdDialogVisible = false;
          this.fetchThreshold();
        } else {
          this.$message.error(res.message || '保存失败');
        }
      } catch (error) {
        console.error('保存阈值配置失败:', error);
        this.$message.error('保存失败');
      } finally {
        this.savingThreshold = false;
      }
    },
    async publishAnnouncement() {
      if (!this.noticeForm.title || !this.noticeForm.content) {
        this.$message.warning('请先填写公告标题和内容');
        return;
      }
      this.publishing = true;
      try {
        const res = await adminPublishAnnouncement(this.noticeForm);
        if (res.code === 0) {
          this.$message.success('公告发布成功');
          this.resetNoticeForm();
          this.fetchAnnouncements();
        } else {
          this.$message.error(res.message || '发布失败');
        }
      } catch (error) {
        console.error('发布公告失败:', error);
        this.$message.error('发布公告失败');
      } finally {
        this.publishing = false;
      }
    },
    resetNoticeForm() {
      this.noticeForm = {
        title: '',
        targetRole: 'farmers',
        content: '',
      };
    },
    async fetchManagerAuditList() {
      this.loadingManagerAudit = true;
      try {
        const res = await getAdminManagerAuditList({ pageNum: 1, pageSize: 100 });
        if (res.code === 0 && res.data) {
          // 返回 PageResponse，data 字段包含列表
          const pageData = res.data;
          this.managerAuditList = Array.isArray(pageData.data) ? pageData.data : (Array.isArray(pageData) ? pageData : []);
        }
      } catch (error) {
        console.error('获取监管局审核列表失败:', error);
      } finally {
        this.loadingManagerAudit = false;
      }
    },
    async handleApproveManagerAudit(row) {
      try {
        const res = await adminApproveManagerAudit({ id: row.id, auditRemark: '审核通过' });
        if (res.code === 0) {
          this.$message.success(`已通过 ${row.institutionName} 的审核申请`);
          this.fetchManagerAuditList();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        console.error('审批失败:', error);
        this.$message.error('审批失败');
      }
    },
    async handleRejectManagerAudit(row) {
      try {
        const res = await adminRejectManagerAudit({ id: row.id, auditRemark: '审核未通过' });
        if (res.code === 0) {
          this.$message.warning(`已拒绝 ${row.institutionName} 的审核申请`);
          this.fetchManagerAuditList();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        console.error('审批失败:', error);
        this.$message.error('审批失败');
      }
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

  &__label { font-size: 14px; opacity: 0.85; }
  &__value { margin: 12px 0; font-size: 42px; font-weight: 700; }
  &__text { line-height: 1.8; opacity: 0.92; }
}

.stats-row,
.content-row { margin-bottom: 16px; }

.content-row { align-items: stretch; }
.content-row > .t-col { display: flex; }

.stat-card {
  min-height: 160px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.99) 0%, rgba(238, 247, 255, 0.96) 100%);

  &__label { font-size: 14px; color: #5e738a; }
  &__value { margin: 12px 0 10px; font-size: 32px; font-weight: 700; color: #12314d; }
  &__desc { font-size: 13px; line-height: 1.7; color: #6a7f95; }
}

.panel-card {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;

  :deep(.t-card__body) { height: 100%; display: flex; flex-direction: column; }
  :deep(.t-card__title) { font-size: 18px; font-weight: 600; }
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

  &__label { font-size: 13px; color: #6a7f95; }
  &__value { margin: 8px 0 6px; font-size: 28px; font-weight: 700; color: #12314d; }
  &__desc { font-size: 12px; line-height: 1.7; color: #6a7f95; }
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
  &__title { font-size: 14px; font-weight: 600; color: #12314d; }
  &__desc { margin-top: 4px; font-size: 12px; color: #6a7f95; }
  &__value { font-size: 18px; font-weight: 700; color: #1f74d8; white-space: nowrap; }
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
  &__title { font-size: 14px; font-weight: 600; color: #12314d; }
  &__desc { margin-top: 6px; font-size: 12px; line-height: 1.7; color: #6a7f95; }
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

  &__title { margin-bottom: 10px; font-size: 15px; font-weight: 600; color: #12314d; }
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
  &__top { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 8px; color: #12314d; font-weight: 600; }
  &__range { margin-top: 6px; font-size: 13px; color: #5e738a; &.danger { color: #c64751; } }
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

.empty-tip {
  text-align: center;
  padding: 20px;
  color: #6a7f95;
  font-size: 13px;
}

.table-card :deep(.t-card__body) { padding-top: 8px; }

@media (max-width: 992px) {
  .hero-panel {
    padding: 20px;
    h1 { font-size: 28px; }
  }
  .status-panel &__value { font-size: 34px; }
  .overview-grid, .account-split-grid { grid-template-columns: 1fr; }
  .overview-table__row { align-items: flex-start; flex-direction: column; }
}
</style>
