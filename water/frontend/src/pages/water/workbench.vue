<template>
  <div class="marine-page water-workbench">
    <t-loading :loading="loading" text="正在同步水质监管工作台...">
      <div v-if="isLoggedIn" class="workbench-shell">
        <section class="ocean-card workbench-hero">
          <div class="hero-main">
            <div class="hero-badge">{{ roleBadge }}</div>
            <h1>{{ roleTitle }}</h1>
            <p>{{ roleSubtitle }}</p>

            <div class="hero-actions">
              <t-button v-if="userType === 'enterprise'" theme="primary" :disabled="hasActivePermit" @click="showApplyDialog">
                提交许可证
              </t-button>
              <template v-else>
                <t-button theme="primary" @click="currentView = 'approval'">许可审批</t-button>
                <t-button variant="outline" @click="currentView = 'waterData'">水数据审查</t-button>
                <t-button variant="outline" @click="showContractDialog">绑定智能合约</t-button>
              </template>
              <t-button variant="text" @click="goPortal">返回入口</t-button>
              <t-button variant="text" @click="logout">退出角色</t-button>
            </div>
          </div>

          <div class="hero-side">
            <div class="side-card">
              <div class="side-card__title">当前会话</div>
              <div class="side-row">
                <span>当前身份</span>
                <strong>{{ currentUser || '--' }}</strong>
              </div>
              <div class="side-row">
                <span>平台账号</span>
                <strong>{{ platformAccount }}</strong>
              </div>
              <div class="side-row">
                <span>角色令牌</span>
                <t-tag :theme="currentRoleToken ? 'success' : 'warning'" variant="light-outline">
                  {{ currentRoleToken ? '已就绪' : '未获取' }}
                </t-tag>
              </div>
            </div>

            <div class="side-card chain-box">
              <div class="side-card__title">链上账户地址</div>
              <div class="chain-address">{{ currentChainAddress || '--' }}</div>
            </div>
          </div>
        </section>

        <t-row :gutter="[16, 16]" class="summary-row">
          <t-col v-for="item in summaryCards" :key="item.title" :xs="12" :sm="6" :xl="3">
            <t-card :bordered="false" class="summary-card">
              <div class="summary-card__label">{{ item.title }}</div>
              <div class="summary-card__value">{{ item.value }}</div>
              <div class="summary-card__desc">{{ item.description }}</div>
            </t-card>
          </t-col>
        </t-row>

        <template v-if="userType === 'enterprise'">
          <t-row :gutter="[16, 16]" class="content-row">
            <t-col :xs="12" :lg="4">
              <t-card title="企业操作说明" :bordered="false" class="panel-card info-panel">
                <ul class="guide-list">
                  <li>使用当前平台账号自动进入企业工作台，无需再次输入账号密码。</li>
                  <li>提交排污许可证后，监管端可直接审核并执行链上核验。</li>
                  <li>若当前存在待审批或已通过许可证，将暂时禁止重复提交。</li>
                </ul>

                <div class="account-box">
                  <div class="account-item">
                    <label>企业登录账号</label>
                    <span>{{ enterpriseAccount || platformAccount }}</span>
                  </div>
                  <div class="account-item">
                    <label>企业默认密码</label>
                    <span>{{ enterprisePassword || '123456' }}</span>
                  </div>
                  <div class="account-item">
                    <label>区块链地址</label>
                    <span>{{ enterpriseBlockchainAddress || '--' }}</span>
                  </div>
                </div>
              </t-card>
            </t-col>

            <t-col :xs="12" :lg="8">
              <t-card title="许可证记录" :bordered="false" class="panel-card">
                <div class="panel-tip">展示企业已提交的许可证状态与链上比对能力。</div>
                <t-table
                  row-key="id"
                  :data="enterpriseTableData"
                  :columns="enterpriseColumns"
                  bordered
                  max-height="520"
                  :empty="'暂无许可证记录'"
                />
              </t-card>
            </t-col>
          </t-row>
        </template>

        <template v-else>
          <t-row :gutter="[16, 16]" class="content-row">
            <t-col :xs="12" :lg="8">
              <t-card title="监管工作台" :bordered="false" class="panel-card">
                <div class="toolbar-row">
                  <t-button-group>
                    <t-button :variant="currentView === 'approval' ? 'base' : 'outline'" @click="currentView = 'approval'">
                      排污审批
                    </t-button>
                    <t-button :variant="currentView === 'waterData' ? 'base' : 'outline'" @click="currentView = 'waterData'">
                      水数据
                    </t-button>
                  </t-button-group>

                  <div class="toolbar-actions">
                    <span class="toolbar-meta">待审批 {{ pendingPermitCount }} 件</span>
                    <t-button size="small" variant="outline" @click="showContractDialog">智能合约绑定</t-button>
                  </div>
                </div>

                <div v-if="currentView === 'approval'">
                  <t-table
                    row-key="id"
                    :data="monitorTableData"
                    :columns="monitorColumns"
                    bordered
                    max-height="520"
                    :empty="'暂无审批数据'"
                  />
                </div>

                <div v-else>
                  <div class="toolbar-row secondary">
                    <t-tabs v-model="waterDataType">
                      <t-tab-panel value="turbidity" label="水浑浊数据" />
                      <t-tab-panel value="tds" label="水 TDS 数据" />
                    </t-tabs>

                    <t-button theme="primary" variant="outline" :disabled="batchDisabled" @click="handleBatchUpload">
                      批量上链
                    </t-button>
                  </div>

                  <div v-if="waterDataType === 'turbidity'">
                    <t-table
                      row-key="id"
                      :data="turbidityTableData"
                      :columns="turbidityColumns"
                      :selected-row-keys="selectedTurbidityRowKeys"
                      :select-on-row-click="true"
                      :disabled-row="isRowDisabled"
                      bordered
                      max-height="420"
                      @select-change="onTurbiditySelectChange"
                    />
                    <div class="pagination-row">
                      <t-pagination
                        :current="turbidityPagination.current"
                        :total="turbidityPagination.total"
                        :page-size="turbidityPagination.pageSize"
                        :page-size-options="[10, 20, 50]"
                        show-total
                        show-page-size-options
                        @current-change="onTurbidityPageChange"
                        @page-size-change="onTurbidityPageSizeChange"
                      />
                    </div>
                  </div>

                  <div v-else>
                    <t-table
                      row-key="id"
                      :data="tdsTableData"
                      :columns="tdsColumns"
                      :selected-row-keys="selectedTdsRowKeys"
                      :select-on-row-click="true"
                      :disabled-row="isRowDisabled"
                      bordered
                      max-height="420"
                      @select-change="onTdsSelectChange"
                    />
                    <div class="pagination-row">
                      <t-pagination
                        :current="tdsPagination.current"
                        :total="tdsPagination.total"
                        :page-size="tdsPagination.pageSize"
                        :page-size-options="[10, 20, 50]"
                        show-total
                        show-page-size-options
                        @current-change="onTdsPageChange"
                        @page-size-change="onTdsPageSizeChange"
                      />
                    </div>
                  </div>
                </div>
              </t-card>
            </t-col>

            <t-col :xs="12" :lg="4">
              <t-card title="监管账户信息" :bordered="false" class="panel-card info-panel">
                <div class="account-box">
                  <div class="account-item">
                    <label>监管登录账号</label>
                    <span>{{ monitorAccount || platformAccount }}</span>
                  </div>
                  <div class="account-item">
                    <label>监管默认密码</label>
                    <span>{{ monitorPassword || '123456' }}</span>
                  </div>
                  <div class="account-item">
                    <label>区块链地址</label>
                    <span>{{ monitorBlockchainAddress || '--' }}</span>
                  </div>
                </div>

                <div class="guide-panel">
                  <div class="guide-panel__title">工作说明</div>
                  <ul class="guide-list compact">
                    <li>可集中处理企业许可证审批与结果复核。</li>
                    <li>支持查看水质数据并对异常记录执行批量上链。</li>
                    <li>可在当前页面完成合约信息绑定与管理。</li>
                  </ul>
                </div>
              </t-card>
            </t-col>
          </t-row>
        </template>
      </div>

      <div v-else class="ocean-card empty-state">
        <div class="empty-state__title">正在进入{{ routeRoleText }}...</div>
        <div class="empty-state__desc">系统会自动复用当前平台登录态，同步对应角色的许可证和水质数据。</div>
      </div>
    </t-loading>

    <t-dialog
      :visible.sync="dialogVisible"
      header="提交排污许可证"
      :confirm-btn="{ content: '提交', theme: 'primary' }"
      :cancel-btn="{ content: '取消', theme: 'default' }"
      @confirm="handleApplySubmit"
      @cancel="handleApplyCancel"
    >
      <t-form :data="applyForm" ref="applyForm">
        <t-form-item label="许可证图片" name="permitImage">
          <t-upload
            :action="uploadCommonFileUrl"
            v-model="myFileList"
            :onSuccess="uploadSuccess"
            theme="image"
            accept="image/*"
            :multiple="false"
            :auto-upload="true"
            @change="handleFileChange"
          >
            <template #default>
              <t-button theme="primary">点击上传</t-button>
            </template>
            <template #fileList>
              <div v-if="applyForm.permitImage && applyForm.permitImage.length" class="upload-preview">
                <t-image :src="applyForm.imgUrl" fit="cover" style="width: 100px; height: 100px; border-radius: 8px;" />
              </div>
            </template>
          </t-upload>
        </t-form-item>
      </t-form>
    </t-dialog>

    <t-dialog
      :visible.sync="contractDialogVisible"
      header="智能合约绑定"
      :confirm-btn="{ content: '确认', theme: 'primary' }"
      :cancel-btn="{ content: '取消' }"
      @confirm="handleContractBind"
      @cancel="contractDialogVisible = false"
    >
      <t-form>
        <t-form-item label="合约名称">
          <t-input v-model="contractForm.name" placeholder="请输入合约名称" />
        </t-form-item>
        <t-form-item label="合约地址">
          <t-input v-model="contractForm.address" placeholder="请输入合约地址" />
        </t-form-item>
      </t-form>
    </t-dialog>

    <t-dialog
      :visible.sync="compareDialogVisible"
      header="链上比对结果"
      :footer="false"
      width="420px"
    >
      <div class="compare-success">
        <t-icon name="check-circle-filled" class="success-icon" />
        <img :src="currentCompareImage" class="compare-image" />
        <div class="success-text">链上比对成功</div>
      </div>
    </t-dialog>
  </div>
</template>

<script>
import { bindContract } from '@/api/common/common.js';
import {
  upChain,
  Logout,
  commitPermission,
  getStepOneData,
  queryPermission,
  chainCompare,
  queryPermissionByManager,
  waterInfoQuery,
  approvePermissionByManager,
  Login,
} from '@/api/water/water.js';
import { ImageViewer as TImageViewer, Image as TImage } from 'tdesign-vue';

export default {
  name: 'WaterWorkbench',
  components: {
    TImage,
    TImageViewer,
  },
  data() {
    return {
      loading: false,
      isLoggedIn: false,
      currentUser: '',
      userType: '',
      currentLoginType: '',
      currentView: 'approval',
      waterDataType: 'turbidity',
      uploadCommonFileUrl: '/api/common/upload',
      dialogVisible: false,
      contractDialogVisible: false,
      compareDialogVisible: false,
      currentCompareImage: '',
      myFileList: [],
      applyForm: {
        imgUrl: '',
        permitImage: [],
      },
      contractForm: {
        name: '',
        address: '',
        abi: '',
        bin: '',
      },
      enterpriseAccount: '',
      enterprisePassword: '',
      enterpriseBlockchainAddress: '',
      monitorAccount: '',
      monitorPassword: '',
      monitorBlockchainAddress: '',
      enterpriseTableData: [],
      monitorTableData: [],
      turbidityTableData: [],
      tdsTableData: [],
      selectedTdsRowKeys: [],
      selectedTurbidityRowKeys: [],
      turbidityPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
      },
      tdsPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
      },
      enterpriseColumns: [
        {
          colKey: 'id',
          title: '序号',
          width: 80,
        },
        {
          colKey: 'companyName',
          title: '企业名称',
        },
        {
          colKey: 'permit',
          title: '许可证',
          width: 110,
          cell: (h, { row }) => {
            const imageUrl = row.permitImage;
            return h(TImageViewer, {
              props: {
                images: [imageUrl],
                closeOnEscKeydown: true,
              },
              scopedSlots: {
                trigger: ({ open }) =>
                  h('img', {
                    style: {
                      width: '64px',
                      height: '64px',
                      objectFit: 'cover',
                      borderRadius: '8px',
                      cursor: 'pointer',
                    },
                    attrs: { src: imageUrl },
                    on: { click: open },
                  }),
              },
            });
          },
        },
        {
          colKey: 'status',
          title: '状态',
          cell: (h, { row }) => {
            const statusMap = {
              0: { theme: 'warning', text: '待审批' },
              1: { theme: 'success', text: '已上链' },
              2: { theme: 'danger', text: '不通过' },
            };
            const status = statusMap[row.status] || { theme: 'default', text: '未知状态' };
            return h('t-tag', { props: { theme: status.theme, variant: 'light' } }, status.text);
          },
        },
        {
          colKey: 'createTime',
          title: '提交时间',
          width: 200,
        },
        {
          colKey: 'operation',
          title: '操作',
          width: 120,
          cell: (h, { row }) => {
            const canCompare = Number(row.status) === 1;
            return h(
              't-button',
              {
                props: {
                  theme: canCompare ? 'primary' : 'default',
                  variant: 'text',
                  size: 'small',
                  disabled: !canCompare,
                },
                on: canCompare
                  ? {
                      click: () => this.handleCompare(row),
                    }
                  : {},
              },
              canCompare ? '链上比对' : '未上链',
            );
          },
        },
      ],
      monitorColumns: [
        {
          colKey: 'id',
          title: '序号',
          width: 80,
        },
        {
          colKey: 'companyName',
          title: '企业名称',
          width: 180,
        },
        {
          colKey: 'permit',
          title: '排污许可证',
          width: 110,
          cell: (h, { row }) => {
            const imageUrl = row.permitImage;
            return h(TImageViewer, {
              props: {
                images: [imageUrl],
                closeOnEscKeydown: true,
              },
              scopedSlots: {
                trigger: ({ open }) =>
                  h('img', {
                    style: {
                      width: '64px',
                      height: '64px',
                      objectFit: 'cover',
                      borderRadius: '8px',
                      cursor: 'pointer',
                    },
                    attrs: { src: imageUrl },
                    on: { click: open },
                  }),
              },
            });
          },
        },
        {
          colKey: 'createTime',
          title: '提交时间',
          width: 180,
        },
        {
          colKey: 'status',
          title: '状态',
          cell: (h, { row }) => {
            const statusMap = {
              0: { theme: 'warning', text: '待审批' },
              1: { theme: 'success', text: '已上链' },
              2: { theme: 'danger', text: '不通过' },
            };
            const status = statusMap[row.status] || { theme: 'default', text: '未知状态' };
            return h('t-tag', { props: { theme: status.theme, variant: 'light' } }, status.text);
          },
        },
        {
          colKey: 'operation',
          title: '操作',
          width: 260,
          cell: (h, { row }) => {
            const isPending = row.status === 0;
            return h(
              'div',
              {
                style: {
                  display: 'flex',
                  gap: '8px',
                  flexWrap: 'wrap',
                },
              },
              [
                ...(isPending
                  ? [
                      h(
                        't-popconfirm',
                        {
                          props: { content: '确认通过并上链吗？', theme: 'primary' },
                          on: { confirm: () => this.handleApprove(row) },
                        },
                        [
                          h(
                            't-button',
                            { props: { theme: 'primary', variant: 'text', size: 'small' } },
                            '通过上链',
                          ),
                        ],
                      ),
                      h(
                        't-popconfirm',
                        {
                          props: { content: '确定驳回请求吗？', theme: 'danger' },
                          on: { confirm: () => this.handleReject(row) },
                        },
                        [
                          h(
                            't-button',
                            { props: { theme: 'danger', variant: 'text', size: 'small' } },
                            '驳回',
                          ),
                        ],
                      ),
                    ]
                  : []),
                h(
                  't-button',
                  {
                    props: {
                      theme: Number(row.status) === 1 ? 'primary' : 'default',
                      variant: 'text',
                      size: 'small',
                      disabled: Number(row.status) !== 1,
                    },
                    on: Number(row.status) === 1 ? { click: () => this.handleCompare(row) } : {},
                  },
                  Number(row.status) === 1 ? '链上比对' : '未上链',
                ),
              ],
            );
          },
        },
      ],
      turbidityColumns: [
        { colKey: 'row-select', type: 'multiple', width: 50, fixed: 'left' },
        { colKey: 'id', title: '序号', width: 80 },
        { colKey: 'userId', title: '企业 ID', width: 150 },
        { colKey: 'data', title: '浑浊度' },
        { colKey: 'time', title: '采集时间', width: 180 },
        {
          colKey: 'status',
          title: '状态',
          cell: (h, { row }) => h('t-tag', { props: { theme: row.status === '正常' ? 'success' : 'danger', variant: 'light' } }, row.status),
        },
        {
          colKey: 'onChain',
          title: '是否上链',
          cell: (h, { row }) => h('t-tag', { props: { theme: row.onChain ? 'success' : 'warning', variant: 'light' } }, row.onChain ? '已上链' : '未上链'),
        },
      ],
      tdsColumns: [
        { colKey: 'row-select', type: 'multiple', width: 50, fixed: 'left' },
        { colKey: 'id', title: '序号', width: 80 },
        { colKey: 'userId', title: '企业 ID', width: 150 },
        { colKey: 'data', title: 'TDS 值' },
        { colKey: 'time', title: '采集时间', width: 180 },
        {
          colKey: 'status',
          title: '状态',
          cell: (h, { row }) => h('t-tag', { props: { theme: row.status === '正常' ? 'success' : 'danger', variant: 'light' } }, row.status),
        },
        {
          colKey: 'onChain',
          title: '是否上链',
          cell: (h, { row }) => h('t-tag', { props: { theme: row.onChain ? 'success' : 'warning', variant: 'light' } }, row.onChain ? '已上链' : '未上链'),
        },
      ],
    };
  },
  computed: {
    routeRoleText() {
      return this.$route?.name === 'monitor-login' ? '监管端' : '企业端';
    },
    roleBadge() {
      return this.userType === 'monitor' ? '监管端 · 许可证审批 · 水数据上链' : '企业端 · 许可证申请 · 链上核验';
    },
    roleTitle() {
      return this.userType === 'monitor' ? '监管水质审查工作台' : '企业水质合规工作台';
    },
    roleSubtitle() {
      return this.userType === 'monitor'
        ? '集中查看企业许可证、审批请求与水质分页数据，并直接执行合约绑定和批量上链。'
        : '基于当前平台登录态快速进入企业工作台，完成许可证提交、状态跟踪与链上比对。';
    },
    platformAccount() {
      return localStorage.getItem('platformUserAccount') || this.$store.state.user?.userInfo?.userAccount || '--';
    },
    currentRoleToken() {
      return this.userType === 'monitor' ? localStorage.getItem('managertoken') : localStorage.getItem('companytoken');
    },
    currentChainAddress() {
      return this.userType === 'monitor' ? this.monitorBlockchainAddress : this.enterpriseBlockchainAddress;
    },
    permitSource() {
      return this.userType === 'monitor' ? this.monitorTableData : this.enterpriseTableData;
    },
    pendingPermitCount() {
      return this.monitorTableData.filter((item) => item.status === 0).length;
    },
    approvedPermitCount() {
      return this.permitSource.filter((item) => item.status === 1).length;
    },
    rejectedPermitCount() {
      return this.permitSource.filter((item) => item.status === 2).length;
    },
    abnormalWaterCount() {
      return [...this.turbidityTableData, ...this.tdsTableData].filter((item) => item.status && item.status !== '正常').length;
    },
    summaryCards() {
      if (this.userType === 'monitor') {
        return [
          {
            title: '待审批许可证',
            value: this.pendingPermitCount,
            description: '待监管侧审核的企业申请数',
          },
          {
            title: '已上链许可证',
            value: this.approvedPermitCount,
            description: '已审批并完成链上存证',
          },
          {
            title: '水数据异常条数',
            value: this.abnormalWaterCount,
            description: '用于重点审查异常水质记录',
          },
          {
            title: '待上链数据',
            value: this.waterDataType === 'turbidity' ? this.selectedTurbidityRowKeys.length : this.selectedTdsRowKeys.length,
            description: '当前勾选的批量上链数量',
          },
        ];
      }

      return [
        {
          title: '许可证总数',
          value: this.enterpriseTableData.length,
          description: '企业已提交的许可证记录',
        },
        {
          title: '待审批',
          value: this.enterpriseTableData.filter((item) => item.status === 0).length,
          description: '等待监管部门审核',
        },
        {
          title: '已上链',
          value: this.approvedPermitCount,
          description: '通过审批并完成链上校验',
        },
        {
          title: '不通过',
          value: this.rejectedPermitCount,
          description: '需补充材料后重新提交',
        },
      ];
    },
    batchDisabled() {
      return this.waterDataType === 'turbidity' ? this.selectedTurbidityRowKeys.length === 0 : this.selectedTdsRowKeys.length === 0;
    },
    hasActivePermit() {
      if (this.enterpriseTableData.length === 0) {
        return false;
      }
      return this.enterpriseTableData.some((item) => item.status === 0 || item.status === 1);
    },
  },
  watch: {
    waterDataType() {
      if (this.userType === 'monitor') {
        this.fetchWaterData();
      }
    },
    '$route.name'() {
      this.autoEnterByRoute();
    },
  },
  created() {
    this.getAccountInfo();
    this.autoEnterByRoute();
  },
  methods: {
    async initRoleWorkbench(userType) {
      this.currentLoginType = userType;
      this.userType = userType;
      this.currentUser = userType === 'enterprise' ? '养殖户用户' : '监管局用户';
      this.currentView = 'approval';
      await this.fetchPermissionList();
      if (userType === 'monitor') {
        await this.fetchWaterData();
      }
      this.isLoggedIn = true;
    },
    async loginByCurrentSession(userType) {
      let userAccount = localStorage.getItem('platformUserAccount');
      if (!userAccount) {
        try {
          await this.$store.dispatch('user/getUserInfo');
          userAccount = this.$store.state.user?.userInfo?.userAccount || '';
          if (userAccount) {
            localStorage.setItem('platformUserAccount', userAccount);
          }
        } catch (error) {
          console.log(error, 'error');
        }
      }

      if (!userAccount) {
        this.$message.error('未找到当前平台账号，请先重新登录');
        this.$router.replace('/login');
        return false;
      }

      const passwordCandidates = [localStorage.getItem('platformUserPassword'), '123456'].filter(Boolean);

      this.loading = true;
      try {
        for (const password of passwordCandidates) {
          const response = await Login({
            userAccount,
            userPassword: password,
            loginType: userType === 'enterprise' ? 0 : 1,
          });
          if (response.code === 0) {
            if (userType === 'enterprise') {
              localStorage.setItem('companytoken', response.data.token);
            } else {
              localStorage.setItem('managertoken', response.data.token);
            }
            await this.initRoleWorkbench(userType);
            this.$message.success(`已使用当前平台账号进入${userType === 'enterprise' ? '企业端' : '监管端'}`);
            return true;
          }
        }
      } catch (error) {
        console.log(error, 'error');
      } finally {
        this.loading = false;
      }

      this.$message.error('无法复用当前登录态进入该端口，请先重新登录平台账号');
      this.$router.replace('/water/index');
      return false;
    },
    async autoEnterByRoute() {
      const routeName = this.$route?.name;
      if (!['enterprise-login', 'monitor-login'].includes(routeName)) {
        return;
      }

      const userType = routeName === 'enterprise-login' ? 'enterprise' : 'monitor';
      const roleTokenKey = userType === 'enterprise' ? 'companytoken' : 'managertoken';

      if (!localStorage.getItem('satoken')) {
        this.$message.error('请先登录平台账号');
        this.$router.replace('/login');
        return;
      }

      if (localStorage.getItem(roleTokenKey)) {
        this.loading = true;
        try {
          await this.initRoleWorkbench(userType);
        } finally {
          this.loading = false;
        }
        return;
      }

      await this.loginByCurrentSession(userType);
    },
    goPortal() {
      this.$router.push('/water/index');
    },
    logout() {
      if (this.userType === 'enterprise') {
        Logout(localStorage.getItem('companytoken'));
        localStorage.removeItem('companytoken');
      } else {
        Logout(localStorage.getItem('managertoken'));
        localStorage.removeItem('managertoken');
      }
      this.isLoggedIn = false;
      this.userType = '';
      this.currentUser = '';
      this.$router.push('/water/index');
    },
    uploadSuccess(response) {
      const fileName = response?.response?.fileName || response?.file?.response?.fileName;
      if (!fileName) {
        this.$message.error('图片上传失败');
        return;
      }
      this.applyForm.permitImage = [fileName];
      this.applyForm.imgUrl = this.$API_BASE_URL + fileName;
      this.$message.success('图片上传成功');
    },
    async handleFileChange(file) {
      if (!file || file.length === 0) {
        return;
      }
    },
    showApplyDialog() {
      this.dialogVisible = true;
      this.applyForm = {
        imgUrl: '',
        permitImage: [],
      };
      this.myFileList = [];
    },
    async handleApplySubmit() {
      try {
        if (this.applyForm.permitImage.length === 0) {
          this.$message.error('请上传许可证图片');
          return;
        }
        const response = await commitPermission({ imageUrl: this.applyForm.permitImage[0] });
        if (response.code === 0) {
          this.dialogVisible = false;
          this.$message.success('申请提交成功');
          await this.fetchPermissionList();
        }
      } catch (error) {
        this.$message.error('申请提交失败');
      }
    },
    handleApplyCancel() {
      this.dialogVisible = false;
      this.applyForm = {
        imgUrl: '',
        permitImage: [],
      };
    },
    async fetchPermissionList() {
      try {
        let response;
        if (this.userType === 'enterprise') {
          response = await queryPermission();
        } else {
          response = await queryPermissionByManager();
        }

        if (response.code === 0) {
          const list = (response.data || []).map((item) => ({
            ...item,
            permitImage: this.$API_BASE_URL + item.imageUrl,
          }));
          if (this.userType === 'enterprise') {
            this.enterpriseTableData = list;
          } else {
            this.monitorTableData = list;
          }
        }
      } catch (error) {
        this.$message.error('获取许可证列表失败');
      }
    },
    async handleApprove(row) {
      try {
        const response = await approvePermissionByManager({ id: row.id, status: 1 });
        if (response.code === 0) {
          this.$message.success('审批通过成功');
          await this.fetchPermissionList();
        }
      } catch (error) {
        this.$message.error('审批失败');
      }
    },
    async handleReject(row) {
      try {
        const response = await approvePermissionByManager({ id: row.id, status: 2 });
        if (response.code === 0) {
          this.$message.success('驳回成功');
          await this.fetchPermissionList();
        }
      } catch (error) {
        this.$message.error('驳回失败');
      }
    },
    async handleCompare(row) {
      if (Number(row?.status) !== 1) {
        this.$message.warning('仅已上链的许可证支持链上比对');
        return;
      }

      try {
        const response = await chainCompare(row.id);
        if (response.code === 0) {
          const imageUrl = response?.data?.imageUrl || row.imageUrl || '';
          this.currentCompareImage = imageUrl.startsWith('http') ? imageUrl : `${this.$API_BASE_URL}${imageUrl}`;
          this.compareDialogVisible = true;
          this.$message.success('链上比对成功');
        }
      } catch (error) {
        console.error('chain compare failed', error);
      }
    },
    async handleBatchUpload() {
      const ids = this.waterDataType === 'turbidity' ? this.selectedTurbidityRowKeys : this.selectedTdsRowKeys;
      if (ids.length === 0) {
        this.$message.warning('请先选择要上链的数据');
        return;
      }
      try {
        const response = await upChain(ids);
        if (response.code === 0) {
          this.$message.success(`${ids.length} 条数据上链成功`);
          this.selectedTurbidityRowKeys = [];
          this.selectedTdsRowKeys = [];
          await this.fetchWaterData();
        }
      } catch (error) {
        this.$message.error('数据上链失败');
      }
    },
    async fetchWaterData() {
      if (this.waterDataType === 'turbidity') {
        await this.fetchTurbidityData();
      } else {
        await this.fetchTdsData();
      }
    },
    showContractDialog() {
      this.contractDialogVisible = true;
    },
    async handleContractBind() {
      if (!this.validateContractForm()) {
        return;
      }

      try {
        const response = await bindContract({
          accountAddress: '',
          contractAbi: '',
          contractAddress: this.contractForm.address,
          contractBin: '',
          contractName: this.contractForm.name,
          contractType: '水质',
        });
        if (response.code === 0) {
          this.$message.success('合约绑定成功');
          this.contractDialogVisible = false;
          this.resetContractForm();
        }
      } catch (error) {
        this.$message.error('合约绑定失败');
      }
    },
    validateContractForm() {
      if (!this.contractForm.name) {
        this.$message.warning('请输入合约名称');
        return false;
      }
      if (!this.contractForm.address) {
        this.$message.warning('请输入合约地址');
        return false;
      }
      return true;
    },
    resetContractForm() {
      this.contractForm = {
        name: '',
        address: '',
        abi: '',
        bin: '',
      };
    },
    onTurbiditySelectChange(selectedRowKeys, selectedRows) {
      this.selectedTurbidityRowKeys = selectedRows.selectedRowData.filter((item) => item && !item.onChain).map((item) => item.id);
    },
    onTdsSelectChange(selectedRowKeys, selectedRows) {
      this.selectedTdsRowKeys = selectedRows.selectedRowData.filter((item) => item && !item.onChain).map((item) => item.id);
    },
    async getAccountInfo() {
      try {
        const { data } = await getStepOneData();
        this.enterpriseAccount = data.companyAccount;
        this.enterprisePassword = data.companyPassword;
        this.enterpriseBlockchainAddress = data.companyAddress;
        this.monitorAccount = data.managerAccount;
        this.monitorPassword = data.managerPassword;
        this.monitorBlockchainAddress = data.managerAddress;
      } catch (error) {
        console.error('获取账号信息失败:', error);
      }
    },
    onTurbidityPageChange(current) {
      this.turbidityPagination.current = current;
      this.fetchTurbidityData();
    },
    onTurbidityPageSizeChange(pageSize) {
      this.turbidityPagination.pageSize = pageSize;
      this.turbidityPagination.current = 1;
      this.fetchTurbidityData();
    },
    onTdsPageChange(current) {
      this.tdsPagination.current = current;
      this.fetchTdsData();
    },
    onTdsPageSizeChange(pageSize) {
      this.tdsPagination.pageSize = pageSize;
      this.tdsPagination.current = 1;
      this.fetchTdsData();
    },
    async fetchTurbidityData() {
      try {
        const response = await waterInfoQuery({
          DataType: 1,
          pageNum: this.turbidityPagination.current,
          pageSize: this.turbidityPagination.pageSize,
        });
        if (response.code === 0) {
          this.turbidityTableData = response.data;
          this.turbidityPagination.total = Number(response.total);
        }
      } catch (error) {
        this.$message.error('获取浑浊度数据失败');
      }
    },
    async fetchTdsData() {
      try {
        const response = await waterInfoQuery({
          DataType: 0,
          pageNum: this.tdsPagination.current,
          pageSize: this.tdsPagination.pageSize,
        });
        if (response.code === 0) {
          this.tdsTableData = response.data;
          this.tdsPagination.total = Number(response.total);
        }
      } catch (error) {
        this.$message.error('获取 TDS 数据失败');
      }
    },
    isRowDisabled(row) {
      return row.onChain === true;
    },
  },
};
</script>

<style scoped lang="less">
.water-workbench {
  .workbench-shell {
    display: grid;
    gap: 16px;
  }
}

.workbench-hero,
.summary-card,
.panel-card,
.empty-state {
  border-radius: 20px;
  border: 1px solid var(--marine-border-strong);
}

.workbench-hero {
  display: grid;
  grid-template-columns: 1.35fr 0.9fr;
  gap: 16px;
  padding: 24px;
}

.hero-main {
  h1 {
    margin: 14px 0 12px;
    font-size: 34px;
    line-height: 1.25;
    color: #12314d;
  }

  p {
    margin: 0;
    color: #5e738a;
    line-height: 1.8;
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

.hero-side {
  display: grid;
  gap: 12px;
}

.side-card {
  padding: 16px 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f8fbff 0%, #eff7ff 100%);
  border: 1px solid var(--marine-divider);

  &__title {
    margin-bottom: 12px;
    font-size: 15px;
    font-weight: 600;
    color: #12314d;
  }
}

.side-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 0;
  color: #5e738a;

  strong {
    color: #12314d;
  }
}

.chain-address {
  font-size: 13px;
  line-height: 1.7;
  word-break: break-all;
  color: #1d4f79;
}

.summary-row,
.content-row {
  margin-bottom: 0;
}

.summary-card {
  min-height: 150px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(238, 247, 255, 0.92) 100%);

  &__label {
    font-size: 14px;
    color: #5e738a;
  }

  &__value {
    margin: 12px 0 10px;
    font-size: 30px;
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
  min-height: 100%;

  :deep(.t-card__title) {
    font-size: 18px;
    font-weight: 600;
  }
}

.panel-tip {
  margin-bottom: 12px;
  color: #5e738a;
  font-size: 13px;
}

.info-panel {
  background: linear-gradient(180deg, rgba(248, 251, 255, 0.95) 0%, rgba(255, 255, 255, 0.98) 100%);
}

.guide-list {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 10px;
  line-height: 1.75;
  color: #5e738a;

  &.compact {
    gap: 8px;
    font-size: 13px;
  }
}

.account-box {
  display: grid;
  gap: 10px;
  margin-top: 18px;
}

.account-item {
  display: grid;
  gap: 6px;
  padding: 12px 14px;
  border-radius: 14px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  border: 1px solid var(--marine-divider);

  label {
    font-size: 12px;
    color: #6a7f95;
  }

  span {
    font-size: 13px;
    line-height: 1.7;
    color: #12314d;
    word-break: break-all;
  }
}

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;

  &.secondary {
    align-items: flex-end;
  }
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar-meta {
  font-size: 13px;
  color: #5e738a;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.guide-panel {
  margin-top: 18px;
  padding: 14px 16px;
  border-radius: 14px;
  background: linear-gradient(180deg, #ffffff 0%, #f5faff 100%);
  border: 1px solid var(--marine-divider);

  &__title {
    margin-bottom: 10px;
    font-size: 14px;
    font-weight: 600;
    color: #12314d;
  }
}

.empty-state {
  padding: 28px;
  text-align: center;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(237, 245, 255, 0.95) 100%);

  &__title {
    font-size: 20px;
    font-weight: 600;
    color: #12314d;
  }

  &__desc {
    margin-top: 10px;
    color: #5e738a;
    line-height: 1.7;
  }
}

.compare-success {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;

  .success-icon {
    font-size: 48px;
    color: #00a870;
    margin-bottom: 16px;
  }

  .compare-image {
    width: 200px;
    height: 200px;
    object-fit: contain;
    border-radius: 8px;
    margin: 16px 0;
    border: 1px solid #e5e5e5;
  }

  .success-text {
    font-size: 18px;
    color: #00a870;
    font-weight: 500;
    margin-top: 16px;
  }
}

:deep(.t-dialog__body) {
  padding-top: 12px;
}

@media (max-width: 1100px) {
  .workbench-hero {
    grid-template-columns: 1fr;
    padding: 20px;
  }

  .hero-main h1 {
    font-size: 28px;
  }
}
</style>