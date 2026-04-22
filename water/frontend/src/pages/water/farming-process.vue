<template>
  <div class="marine-page farming-process-page">
    <section class="ocean-card page-hero">
      <div>
        <div class="page-badge">{{ pageBadge }}</div>
        <h1>{{ pageTitle }}</h1>
        <p>{{ pageDescription }}</p>
      </div>
      <div class="hero-actions">
        <t-select
          v-if="isMonitorView"
          v-model="selectedFarmer"
          class="role-filter"
          :options="farmerFilterOptions"
          placeholder="请选择养殖户"
        />
        <t-button v-if="!isMonitorView" theme="primary" @click="openCreateDialog">{{ currentCreateLabel }}</t-button>
        <t-button v-else theme="primary" variant="outline" @click="showAllFarmers">查看全部养殖户</t-button>
        <t-button variant="outline" @click="goBackWorkbench">返回工作台</t-button>
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

    <t-row :gutter="[16, 16]" class="content-row">
      <t-col :xs="12" :lg="3">
        <t-card :title="sidePanelTitle" :bordered="false" class="panel-card side-panel">
          <div v-if="loadingPonds" style="text-align: center; padding: 20px;">
            <t-loading text="加载中..." />
          </div>
          <template v-else>
            <button
              v-for="pond in visiblePondOptions"
              :key="pond.value"
              type="button"
              class="pond-card"
              :class="{ active: selectedPond === pond.value }"
              @click="selectedPond = pond.value"
            >
              <div>
                <div class="pond-card__name">{{ pond.label }}</div>
                <div class="pond-card__type">{{ pond.farmName }}{{ pond.farmerName ? ' · ' + pond.farmerName : '' }}{{ pond.species ? ' · ' + pond.species : '' }}</div>
              </div>
              <t-tag :theme="pond.status === '正常' || pond.status === 'APPROVED' ? 'success' : 'warning'" variant="light-outline">{{ pond.statusText || pond.status }}</t-tag>
            </button>
          </template>
        </t-card>
      </t-col>

      <t-col :xs="12" :lg="9">
        <t-card :bordered="false" class="panel-card main-panel">
          <div class="toolbar-row">
            <t-tabs :value="activeTab" @change="handleTabChange">
              <t-tab-panel v-if="isMonitorView" value="trace" label="过程溯源" />
              <t-tab-panel value="seed" label="苗种投放" />
              <t-tab-panel value="feed" label="投喂记录" />
              <t-tab-panel value="medicine" label="用药记录" />
              <t-tab-panel value="harvest" label="收货记录" />
            </t-tabs>

            <div class="toolbar-actions">
              <t-button v-if="!isMonitorView" theme="primary" @click="openCreateDialog">{{ currentCreateLabel }}</t-button>
            </div>
          </div>

          <template v-if="isMonitorView && activeTab === 'trace'">
            <div class="traceability-panel">
              <div class="traceability-hero">
                <div class="traceability-hero__icon">i</div>
                <div>
                  <div class="traceability-hero__title">养殖全过程溯源</div>
                  <div class="traceability-hero__subtitle">查看任意养殖户的完整养殖数据</div>
                </div>
              </div>

              <div class="traceability-owner">
                养殖户：<strong>{{ activeTraceabilityProfile.farmerName }}</strong>
                <span>{{ activeTraceabilityProfile.farmName }}</span>
              </div>

              <div class="traceability-grid">
                <div class="traceability-grid__item">
                  <div class="traceability-grid__label">养殖品种</div>
                  <div class="traceability-grid__value">{{ activeTraceabilityProfile.species }}</div>
                </div>
                <div class="traceability-grid__item">
                  <div class="traceability-grid__label">投放时间</div>
                  <div class="traceability-grid__value">{{ activeTraceabilityProfile.stockDate }}</div>
                </div>
                <div class="traceability-grid__item">
                  <div class="traceability-grid__label">投放数量</div>
                  <div class="traceability-grid__value">{{ activeTraceabilityProfile.quantity }}</div>
                </div>
                <div class="traceability-grid__item">
                  <div class="traceability-grid__label">生长天数</div>
                  <div class="traceability-grid__value">{{ activeTraceabilityProfile.growthDays }}</div>
                </div>
                <div class="traceability-grid__item">
                  <div class="traceability-grid__label">投喂次数</div>
                  <div class="traceability-grid__value">{{ activeTraceabilityProfile.feedCount }}</div>
                </div>
                <div class="traceability-grid__item">
                  <div class="traceability-grid__label">上链状态</div>
                  <div class="traceability-grid__value">
                    <t-tag :theme="activeTraceabilityProfile.chainTheme" variant="light-outline">{{ activeTraceabilityProfile.chainText }}</t-tag>
                  </div>
                </div>
              </div>

              <div class="traceability-stage-list">
                <div v-for="stage in monitorTraceabilityStages" :key="stage.title" class="traceability-stage">
                  <div>
                    <div class="traceability-stage__title">{{ stage.title }}</div>
                    <div class="traceability-stage__desc">{{ stage.description }}</div>
                  </div>
                  <t-tag :theme="stage.theme" variant="light-outline">{{ stage.status }}</t-tag>
                </div>
              </div>
            </div>
          </template>

          <template v-else>
            <div class="table-header">
              <div>
                <div class="table-title">{{ currentTabLabel }}</div>
                <div class="table-tip">{{ filterSummaryText }}</div>
              </div>
              <div class="pond-switcher">
                <t-button
                  v-for="option in pondFilterOptions"
                  :key="option.value"
                  size="small"
                  :theme="selectedPond === option.value ? 'primary' : 'default'"
                  :variant="selectedPond === option.value ? 'base' : 'outline'"
                  @click="selectedPond = option.value"
                >
                  {{ option.label }}
                </t-button>
              </div>
            </div>

            <t-loading v-if="loadingData" text="加载中..." />
            <t-table
              v-else
              row-key="id"
              bordered
              hover
              size="small"
              max-height="520"
              :data="filteredCurrentData"
              :columns="currentColumns"
              :empty="'暂无数据'"
            />
          </template>
        </t-card>
      </t-col>
    </t-row>

    <t-row :gutter="[16, 16]" class="content-row">
      <t-col :xs="12" :lg="12">
        <t-card title="最近存证动态" :bordered="false" class="panel-card">
          <div class="trace-list">
            <div v-for="item in latestTimeline" :key="item.id" class="trace-item">
              <div class="trace-item__content">
                <div class="trace-item__title">{{ item.title }}</div>
                <div class="trace-item__meta">{{ isMonitorView ? `${item.farmerName} · ${item.farmName} · ` : '' }}{{ item.pondName }} · {{ item.time }} · {{ item.operator }}</div>
                <div class="trace-item__desc">{{ item.remark }}</div>
              </div>
              <div class="trace-item__aside">
                <t-tag :theme="resolveStatusTheme(item.status)" variant="light-outline">{{ item.status }}</t-tag>
              </div>
            </div>
          </div>
        </t-card>
      </t-col>
    </t-row>

    <t-dialog
      v-if="!isMonitorView"
      :visible.sync="dialogVisible"
      :header="currentCreateLabel"
      :confirm-btn="{ content: '保存', theme: 'primary', loading: submitting }"
      :cancel-btn="{ content: '取消' }"
      width="560px"
      @confirm="handleCreateRecord"
      @cancel="resetForm"
    >
      <t-form :data="formModel" layout="vertical">
        <t-row :gutter="[12, 12]">
          <t-col :span="6">
            <t-form-item label="日期">
              <t-date-picker v-model="formModel.date" clearable />
            </t-form-item>
          </t-col>
          <t-col :span="6">
            <t-form-item label="时间">
              <t-time-picker v-model="formModel.time" format="HH:mm" clearable />
            </t-form-item>
          </t-col>
          <t-col :span="6">
            <t-form-item label="塘号">
              <t-select v-model="formModel.pondId" :options="pondSelectOptions" placeholder="请选择养殖池" />
            </t-form-item>
          </t-col>
          <t-col :span="6">
            <t-form-item label="操作人">
              <t-input v-model="formModel.operator" placeholder="请输入操作人" />
            </t-form-item>
          </t-col>
          <t-col :span="6">
            <t-form-item :label="formFieldLabels.primary">
              <t-input v-model="formModel.primaryValue" :placeholder="`请输入${formFieldLabels.primary}`" />
            </t-form-item>
          </t-col>
          <t-col :span="6">
            <t-form-item :label="formFieldLabels.secondary">
              <t-input v-model="formModel.secondaryValue" :placeholder="`请输入${formFieldLabels.secondary}`" />
            </t-form-item>
          </t-col>
          <t-col :span="12">
            <t-form-item label="备注">
              <t-input v-model="formModel.remark" placeholder="请输入备注说明" />
            </t-form-item>
          </t-col>
        </t-row>
      </t-form>
    </t-dialog>
  </div>
</template>

<script>
import { getPondList, getManagerPondList } from '@/api/water/pond';
import { getSeedList, createSeed, getManagerSeedList } from '@/api/water/seed';
import { getFeedList, createFeed, getManagerFeedList } from '@/api/water/feed';
import { getMedicineList, createMedicine, getManagerMedicineList } from '@/api/water/medicine';
import { getHarvestList, createHarvest, getManagerHarvestList } from '@/api/water/harvest';

const STATUS_THEME_MAP = {
  '已上链': 'success',
  '待上链': 'warning',
  '已完成': 'success',
  '待确认': 'warning',
  '正常': 'success',
  '关注': 'warning',
  'APPROVED': 'success',
  'PENDING': 'warning',
  'REJECTED': 'danger',
};

const STATUS_TEXT_MAP = {
  'APPROVED': '已上链',
  'PENDING': '待上链',
  'REJECTED': '已拒绝',
};

const MONITOR_EXTRA_COLUMNS = [
  { colKey: 'farmerName', title: '养殖户', minWidth: 120 },
  { colKey: 'farmName', title: '养殖场名称', minWidth: 170 },
];

function createStatusColumn() {
  return {
    colKey: 'status',
    title: '上链状态',
    width: 110,
    cell: (h, { row }) => {
      const statusText = STATUS_TEXT_MAP[row.status] || row.status || '--';
      return h('t-tag', {
        props: {
          theme: STATUS_THEME_MAP[row.status] || 'primary',
          variant: 'light-outline',
        },
      }, statusText);
    },
  };
}

const seedBaseColumns = [
  { colKey: 'date', title: '日期', width: 120 },
  { colKey: 'time', title: '时间', width: 90 },
  { colKey: 'batchNo', title: '批次号', minWidth: 140 },
  { colKey: 'category', title: '苗种类型', minWidth: 140 },
  { colKey: 'quantity', title: '投放数量', width: 120 },
  { colKey: 'source', title: '苗种来源', minWidth: 160 },
  { colKey: 'pondName', title: '塘号', width: 110 },
  { colKey: 'operator', title: '操作人', width: 100 },
  createStatusColumn(),
];

const feedBaseColumns = [
  { colKey: 'date', title: '日期', width: 120 },
  { colKey: 'time', title: '时间', width: 90 },
  { colKey: 'brand', title: '饲料品牌', minWidth: 160 },
  { colKey: 'amount', title: '用量', width: 100 },
  { colKey: 'pondName', title: '塘号', width: 110 },
  { colKey: 'operator', title: '操作人', width: 100 },
  { colKey: 'remark', title: '备注', minWidth: 220, ellipsis: true },
  createStatusColumn(),
];

const medicineBaseColumns = [
  { colKey: 'date', title: '日期', width: 120 },
  { colKey: 'time', title: '时间', width: 90 },
  { colKey: 'name', title: '药品名称', minWidth: 150 },
  { colKey: 'dosage', title: '剂量', width: 110 },
  { colKey: 'purpose', title: '用途', minWidth: 140 },
  { colKey: 'withdrawal', title: '休药期', width: 100 },
  { colKey: 'pondName', title: '塘号', width: 110 },
  { colKey: 'operator', title: '操作人', width: 100 },
  createStatusColumn(),
];

const harvestBaseColumns = [
  { colKey: 'date', title: '日期', width: 120 },
  { colKey: 'time', title: '时间', width: 90 },
  { colKey: 'batchNo', title: '收货批次', minWidth: 140 },
  { colKey: 'spec', title: '规格', minWidth: 130 },
  { colKey: 'weight', title: '重量', width: 100 },
  { colKey: 'destination', title: '去向', minWidth: 160 },
  { colKey: 'pondName', title: '塘号', width: 110 },
  { colKey: 'operator', title: '验收人', width: 100 },
  createStatusColumn(),
];

export default {
  name: 'FarmingProcess',
  data() {
    return {
      activeTab: 'feed',
      selectedFarmer: 'all',
      selectedPond: 'all',
      dialogVisible: false,
      submitting: false,
      loadingPonds: false,
      loadingData: false,
      pondOptions: [],
      seedRecords: [],
      feedRecords: [],
      medicineRecords: [],
      harvestRecords: [],
      seedBaseColumns,
      feedBaseColumns,
      medicineBaseColumns,
      harvestBaseColumns,
      formModel: {
        date: '',
        time: '',
        pondId: '',
        operator: '',
        primaryValue: '',
        secondaryValue: '',
        remark: '',
      },
    };
  },
  computed: {
    currentRoleCode() {
      const roleValue = this.$store?.getters?.['user/roles'] || localStorage.getItem('platformUserRole') || 'farmers';
      return Array.isArray(roleValue) ? roleValue[0] : roleValue;
    },
    isMonitorView() {
      return this.currentRoleCode === 'manager';
    },
    pageBadge() {
      return this.isMonitorView ? '监管端 · 全量总览' : '养殖户端';
    },
    pageTitle() {
      return this.isMonitorView ? '养殖过程监管' : '养殖过程管理';
    },
    pageDescription() {
      return this.isMonitorView
        ? '监管端可查看全部养殖户的苗种投放、投喂、用药与收货台账。'
        : '覆盖苗种投放、投喂记录、用药记录、收货记录。';
    },
    sidePanelTitle() {
      return this.isMonitorView ? '监管养殖池总览' : '我的养殖池';
    },
    farmerFilterOptions() {
      const uniqueFarmers = this.pondOptions.reduce((result, item) => {
        if (item.farmerId && !result.find((option) => option.value === item.farmerId)) {
          result.push({ label: `${item.farmerName || ''} · ${item.farmName || ''}`, value: item.farmerId });
        }
        return result;
      }, []);
      return [{ label: '全部养殖户', value: 'all' }, ...uniqueFarmers];
    },
    visiblePondOptions() {
      let options = [...this.pondOptions];
      if (!this.isMonitorView) {
        // 养殖户只看自己的养殖池
        options = options; // 后端已根据用户过滤
      } else if (this.selectedFarmer !== 'all') {
        options = options.filter((item) => item.farmerId === this.selectedFarmer);
      }
      return options.map(item => ({
        ...item,
        statusText: STATUS_TEXT_MAP[item.status] || item.status,
      }));
    },
    pondFilterOptions() {
      return [{ label: '全部养殖池', value: 'all' }, ...this.visiblePondOptions.map((item) => ({ label: `${item.label} · ${item.farmerName || ''}`, value: item.value }))];
    },
    pondSelectOptions() {
      return this.visiblePondOptions.map((item) => ({ label: item.label, value: item.value }));
    },
    selectedFarmerLabel() {
      if (!this.isMonitorView || this.selectedFarmer === 'all') return '全部养殖户';
      return this.farmerFilterOptions.find((item) => item.value === this.selectedFarmer)?.label || '全部养殖户';
    },
    selectedPondLabel() {
      if (this.selectedPond === 'all') return '全部养殖池';
      return this.pondOptions.find((item) => item.value === this.selectedPond)?.label || '全部养殖池';
    },
    currentTabLabel() {
      const map = {
        trace: '过程溯源',
        seed: '苗种投放',
        feed: '投喂记录',
        medicine: '用药记录',
        harvest: '收货记录',
      };
      return map[this.activeTab] || '养殖记录';
    },
    currentCreateLabel() {
      const map = {
        seed: '新增投放',
        feed: '新增投喂',
        medicine: '新增用药',
        harvest: '新增收货',
      };
      return map[this.activeTab] || '新增记录';
    },
    currentColumns() {
      const map = {
        seed: this.seedBaseColumns,
        feed: this.feedBaseColumns,
        medicine: this.medicineBaseColumns,
        harvest: this.harvestBaseColumns,
      };
      const baseColumns = map[this.activeTab] || this.feedBaseColumns;
      return this.isMonitorView ? [...MONITOR_EXTRA_COLUMNS, ...baseColumns] : baseColumns;
    },
    currentData() {
      const map = {
        seed: this.seedRecords,
        feed: this.feedRecords,
        medicine: this.medicineRecords,
        harvest: this.harvestRecords,
      };
      return map[this.activeTab] || [];
    },
    filteredCurrentData() {
      if (this.selectedPond === 'all') return this.currentData;
      return this.currentData.filter((item) => item.pondId === this.selectedPond);
    },
    scopedSeedRecords() {
      return this.filterRecordsByCurrentScope(this.seedRecords);
    },
    scopedFeedRecords() {
      return this.filterRecordsByCurrentScope(this.feedRecords);
    },
    scopedMedicineRecords() {
      return this.filterRecordsByCurrentScope(this.medicineRecords);
    },
    scopedHarvestRecords() {
      return this.filterRecordsByCurrentScope(this.harvestRecords);
    },
    activeTraceabilityProfile() {
      const pondInfo = (this.selectedPond !== 'all' && this.pondOptions.find((item) => item.value === this.selectedPond))
        || this.visiblePondOptions[0]
        || this.pondOptions[0]
        || {};
      const firstSeed = this.scopedSeedRecords[0] || {};
      const totalScopedRecords = [
        ...this.scopedSeedRecords,
        ...this.scopedFeedRecords,
        ...this.scopedMedicineRecords,
        ...this.scopedHarvestRecords,
      ];
      const allOnChain = totalScopedRecords.length > 0 && totalScopedRecords.every((item) => ['APPROVED', '已上链', '已完成'].includes(item.status));
      const seedDate = firstSeed.date || '--';
      const growthDays = firstSeed.date
        ? `${Math.max(1, Math.ceil((Date.now() - new Date(firstSeed.date).getTime()) / (1000 * 60 * 60 * 24)))}天`
        : '--';

      return {
        farmerName: pondInfo.farmerName || firstSeed.farmerName || '全部养殖户',
        farmName: pondInfo.farmName || firstSeed.farmName || '示范养殖场',
        species: pondInfo.species || firstSeed.category || '--',
        stockDate: seedDate,
        quantity: firstSeed.quantity || '--',
        growthDays,
        feedCount: `${this.scopedFeedRecords.length}次`,
        chainText: allOnChain ? '完整' : '待补充',
        chainTheme: allOnChain ? 'success' : 'warning',
      };
    },
    monitorTraceabilityStages() {
      const buildStage = (title, records, description) => ({
        title,
        description,
        status: !records.length ? '暂无记录' : (records.every((item) => ['APPROVED', '已上链', '已完成'].includes(item.status)) ? '已上链' : '待补链'),
        theme: !records.length ? 'default' : (records.every((item) => ['APPROVED', '已上链', '已完成'].includes(item.status)) ? 'success' : 'warning'),
      });

      return [
        buildStage('苗种投放', this.scopedSeedRecords, this.scopedSeedRecords[0] ? `已登记 ${this.scopedSeedRecords[0].quantity}，来源 ${this.scopedSeedRecords[0].source}` : '暂无投放数据'),
        buildStage('日常投喂', this.scopedFeedRecords, this.scopedFeedRecords[0] ? `累计 ${this.scopedFeedRecords.length} 次，最近一次 ${this.scopedFeedRecords[0].date} ${this.scopedFeedRecords[0].time}` : '暂无投喂数据'),
        buildStage('用药管控', this.scopedMedicineRecords, this.scopedMedicineRecords[0] ? `累计 ${this.scopedMedicineRecords.length} 次，最近使用 ${this.scopedMedicineRecords[0].name}` : '暂无用药数据'),
        buildStage('收货验收', this.scopedHarvestRecords, this.scopedHarvestRecords[0] ? `最近批次 ${this.scopedHarvestRecords[0].batchNo}，重量 ${this.scopedHarvestRecords[0].weight}` : '暂无收货数据'),
      ];
    },
    filterSummaryText() {
      if (this.isMonitorView) {
        return `当前筛选：${this.selectedFarmerLabel} / ${this.selectedPondLabel}，共 ${this.filteredCurrentData.length} 条记录。`;
      }
      return `当前养殖池：${this.selectedPondLabel}，共 ${this.filteredCurrentData.length} 条记录。`;
    },
    summaryCards() {
      const scopeRecords = [...this.seedRecords, ...this.feedRecords, ...this.medicineRecords, ...this.harvestRecords];
      const scopeFeedRecords = this.feedRecords;
      const totalFeed = scopeFeedRecords.reduce((sum, item) => sum + Number(String(item.amount).replace(/[^\d.]/g, '') || 0), 0);
      const onChainCount = scopeRecords.filter((item) => item.status === 'APPROVED' || item.status === '已上链').length;
      const scopePonds = this.pondOptions;
      const uniqueFarmers = new Set(scopePonds.map((item) => item.farmerId).filter(Boolean)).size;
      const warningPonds = scopePonds.filter((item) => item.status !== '正常' && item.status !== 'APPROVED').length;

      if (this.isMonitorView) {
        return [
          { title: '监管养殖户', value: `${uniqueFarmers}户`, description: '当前纳入监管的养殖主体' },
          { title: '养殖池数量', value: `${scopePonds.length}个`, description: '可查看全部养殖池过程台账' },
          { title: '已上链记录', value: `${onChainCount}条`, description: '已完成链上留痕的业务记录' },
          { title: '重点关注池', value: `${warningPonds}个`, description: '状态为关注或待核查的养殖池' },
        ];
      }

      return [
        { title: '苗种投放批次', value: `${this.seedRecords.length}批`, description: '已登记的投苗批次' },
        { title: '累计投喂量', value: `${totalFeed}kg`, description: '投喂总量统计值' },
        { title: '用药记录数', value: `${this.medicineRecords.length}次`, description: '含常规维护与应急处置' },
        { title: '已上链台账', value: `${onChainCount}条`, description: '展示区块链存证状态' },
      ];
    },
    latestTimeline() {
      const scopeRecords = [...this.seedRecords, ...this.feedRecords, ...this.medicineRecords, ...this.harvestRecords];

      return scopeRecords
        .map((item) => ({
          id: `${this.getRecordType(item)}-${item.id}`,
          title: `${this.getRecordType(item)} · ${item.pondName}`,
          time: `${item.date} ${item.time || ''}`,
          pondName: item.pondName,
          farmerName: item.farmerName,
          farmName: item.farmName,
          operator: item.operator,
          remark: item.remark || '',
          status: STATUS_TEXT_MAP[item.status] || item.status,
        }))
        .sort((a, b) => `${b.time}`.localeCompare(a.time))
        .slice(0, 6);
    },
    formFieldLabels() {
      const map = {
        seed: { primary: '苗种类型', secondary: '投放数量' },
        feed: { primary: '饲料品牌', secondary: '投喂量(kg)' },
        medicine: { primary: '药品名称', secondary: '剂量' },
        harvest: { primary: '收货批次', secondary: '重量(kg)' },
      };
      return map[this.activeTab] || { primary: '主字段', secondary: '补充字段' };
    },
  },
  watch: {
    selectedFarmer() {
      if (this.selectedPond !== 'all' && !this.visiblePondOptions.some((item) => item.value === this.selectedPond)) {
        this.selectedPond = 'all';
      }
      this.resetForm();
      this.loadAllData();
    },
    selectedPond() {
      // 切换养殖池时重新加载当前tab数据
      this.loadCurrentTabData();
    },
  },
  mounted() {
    if (this.isMonitorView) {
      this.activeTab = 'trace';
    }
    this.loadPonds();
    this.loadAllData();
  },
  methods: {
    async loadPonds() {
      this.loadingPonds = true;
      try {
        const res = this.isMonitorView ? await getManagerPondList() : await getPondList();
        if (res.code === 0 && res.data) {
          // 养殖户接口返回 List<Pond>，监管局接口返回 PageResponse<Pond>
          let list;
          if (this.isMonitorView) {
            // 监管局：PageResponse，data 字段包含列表
            const pageData = res.data;
            list = Array.isArray(pageData.data) ? pageData.data : (Array.isArray(pageData) ? pageData : []);
          } else {
            // 养殖户：直接返回列表
            list = Array.isArray(res.data) ? res.data : [];
          }
          this.pondOptions = list.map(item => ({
            value: String(item.id),
            label: item.pondName || item.name || `养殖池${item.id}`,
            species: item.species || item.fishType || '',
            status: item.status || 'PENDING',
            farmerId: item.farmerId ? String(item.farmerId) : null,
            farmerName: item.farmerName || item.userName || '',
            farmName: item.farmName || '',
          }));
          if (!this.isMonitorView && this.pondOptions.length > 0) {
            this.selectedPond = this.pondOptions[0].value;
          }
        }
      } catch (error) {
        console.error('加载养殖池失败:', error);
        this.$message.error('加载养殖池失败');
      } finally {
        this.loadingPonds = false;
      }
    },
    async loadAllData() {
      this.loadingData = true;
      try {
        const params = this.selectedPond !== 'all' ? { pondId: this.selectedPond } : {};

        const [seedRes, feedRes, medicineRes, harvestRes] = await Promise.all([
          this.isMonitorView ? getManagerSeedList(params) : getSeedList(params),
          this.isMonitorView ? getManagerFeedList(params) : getFeedList(params),
          this.isMonitorView ? getManagerMedicineList(params) : getMedicineList(params),
          this.isMonitorView ? getManagerHarvestList(params) : getHarvestList(params),
        ]);

        this.seedRecords = this.parseListResponse(seedRes);
        this.feedRecords = this.parseListResponse(feedRes);
        this.medicineRecords = this.parseListResponse(medicineRes);
        this.harvestRecords = this.parseListResponse(harvestRes);
      } catch (error) {
        console.error('加载数据失败:', error);
      } finally {
        this.loadingData = false;
      }
    },
    async loadCurrentTabData() {
      this.loadingData = true;
      const params = this.selectedPond !== 'all' ? { pondId: this.selectedPond } : {};

      try {
        let res;
        switch (this.activeTab) {
          case 'seed':
            res = await (this.isMonitorView ? getManagerSeedList(params) : getSeedList(params));
            this.seedRecords = this.parseListResponse(res);
            break;
          case 'feed':
            res = await (this.isMonitorView ? getManagerFeedList(params) : getFeedList(params));
            this.feedRecords = this.parseListResponse(res);
            break;
          case 'medicine':
            res = await (this.isMonitorView ? getManagerMedicineList(params) : getMedicineList(params));
            this.medicineRecords = this.parseListResponse(res);
            break;
          case 'harvest':
            res = await (this.isMonitorView ? getManagerHarvestList(params) : getHarvestList(params));
            this.harvestRecords = this.parseListResponse(res);
            break;
        }
      } catch (error) {
        console.error('加载数据失败:', error);
      } finally {
        this.loadingData = false;
      }
    },
    parseListResponse(res) {
      if (!res) return [];

      // 处理两种返回格式：
      // 1. 养殖户接口直接返回 PageResponse（res.code === 0 且 res.data 是数组）
      // 2. 监管局接口返回 BaseResponse<PageResponse>（res.code === 0 且 res.data.data 是数组）
      let list = [];

      if (res.code === 0) {
        if (Array.isArray(res.data)) {
          // 养殖户接口：res 是 PageResponse，res.data 是数组
          list = res.data;
        } else if (res.data && Array.isArray(res.data.data)) {
          // 监管局接口：res 是 BaseResponse，res.data 是 PageResponse，res.data.data 是数组
          list = res.data.data;
        }
      }

      return list.map(item => ({
        ...item,
        id: item.id,
        pondId: String(item.pondId || ''),
        pondName: item.pondName || '',
        farmerId: item.farmerId ? String(item.farmerId) : null,
        farmerName: item.farmerName || item.userName || '',
        farmName: item.farmName || '',
        date: item.date || item.createTime?.split(' ')[0] || '',
        time: item.time || item.createTime?.split(' ')[1]?.substring(0, 5) || '',
      }));
    },
    filterRecordsByCurrentScope(records) {
      let result = Array.isArray(records) ? [...records] : [];

      if (this.isMonitorView && this.selectedFarmer !== 'all') {
        result = result.filter((item) => item.farmerId === this.selectedFarmer);
      }

      if (this.selectedPond !== 'all') {
        result = result.filter((item) => item.pondId === this.selectedPond);
      }

      return result;
    },
    resolveStatusTheme(status) {
      return STATUS_THEME_MAP[status] || 'primary';
    },
    showAllFarmers() {
      this.selectedFarmer = 'all';
      this.selectedPond = 'all';
      this.$message.success('已切换为全部养殖户视图');
    },
    handleTabChange(value) {
      this.activeTab = value;
      this.resetForm();
      this.loadCurrentTabData();
    },
    openCreateDialog() {
      if (this.isMonitorView) {
        this.$message.info('监管端当前以查看全部养殖户过程台账为主');
        return;
      }
      this.dialogVisible = true;
      if (this.selectedPond !== 'all') {
        this.formModel.pondId = this.selectedPond;
      }
    },
    resetForm() {
      this.formModel = {
        date: new Date().toISOString().split('T')[0],
        time: '',
        pondId: this.selectedPond === 'all' ? (this.visiblePondOptions[0]?.value || '') : this.selectedPond,
        operator: '',
        primaryValue: '',
        secondaryValue: '',
        remark: '',
      };
    },
    getRecordType(item) {
      if (item.batchNo && (item.category || item.spec)) return '苗种投放';
      if (item.brand) return '投喂记录';
      if (item.withdrawal || item.name) return '用药记录';
      return '收货记录';
    },
    async handleCreateRecord() {
      if (this.isMonitorView) {
        return;
      }

      const { date, time, pondId, operator, primaryValue, secondaryValue, remark } = this.formModel;
      if (!date || !pondId || !operator || !primaryValue || !secondaryValue) {
        this.$message.warning('请先补全必要信息');
        return;
      }

      this.submitting = true;
      try {
        let res;
        if (this.activeTab === 'seed') {
          res = await createSeed({
            pondId: Number(pondId),
            recordDate: date,
            manager: operator,
            seedType: primaryValue,
            weight: Number(secondaryValue),
            remark: remark || '',
          });
        } else if (this.activeTab === 'feed') {
          res = await createFeed({
            pondId: Number(pondId),
            feedDate: date,
            manager: operator,
            feedBrand: primaryValue,
            feedAmount: Number(secondaryValue),
            remark: remark || '',
          });
        } else if (this.activeTab === 'medicine') {
          res = await createMedicine({
            pondId: Number(pondId),
            medicineDate: date,
            manager: operator,
            medicineName: primaryValue,
            dosage: secondaryValue,
            purpose: '日常维护',
            remark: remark || '',
          });
        } else {
          res = await createHarvest({
            pondId: Number(pondId),
            harvestDate: date,
            manager: operator,
            batchNo: primaryValue,
            totalWeight: Number(secondaryValue),
            remark: remark || '',
          });
        }

        if (res.code === 0) {
          this.$message.success(`${this.currentTabLabel}记录已创建`);
          this.dialogVisible = false;
          this.resetForm();
          await this.loadCurrentTabData();
        } else {
          this.$message.error(res.message || '创建失败');
        }
      } catch (error) {
        console.error('创建记录失败:', error);
        this.$message.error('创建记录失败');
      } finally {
        this.submitting = false;
      }
    },
    goBackWorkbench() {
      const target = this.isMonitorView ? '/water/monitor-login' : '/water/enterprise-login';
      this.$router.push(target).catch(() => '');
    },
  },
};
</script>

<style lang="less" scoped>
.farming-process-page {
  padding: 24px;
  background: linear-gradient(180deg, #f5f9ff 0%, #f8fbff 100%);
  min-height: 100%;
}

.page-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 24px 28px;
  margin-bottom: 16px;
  border-radius: 18px;
  background: linear-gradient(135deg, #eef6ff 0%, #ffffff 100%);
  box-shadow: 0 10px 28px rgba(31, 116, 216, 0.08);

  h1 {
    margin: 8px 0;
    font-size: 32px;
    font-weight: 700;
    color: #1f2d3d;
  }

  p {
    margin: 0;
    color: #5f6b7a;
    font-size: 14px;
  }
}

.page-badge {
  display: inline-flex;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(31, 116, 216, 0.12);
  color: #1f74d8;
  font-size: 12px;
  font-weight: 600;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.role-filter {
  min-width: 220px;
}

.summary-row,
.content-row {
  margin-top: 0 !important;
  margin-bottom: 16px;
}

.summary-card,
.panel-card {
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(15, 82, 186, 0.08);
}

.summary-card__label {
  font-size: 13px;
  color: #6b7785;
}

.summary-card__value {
  margin: 8px 0 6px;
  font-size: 30px;
  line-height: 1.2;
  font-weight: 700;
  color: #1f2d3d;
}

.summary-card__desc {
  font-size: 12px;
  color: #8a96a3;
}

.side-panel {
  margin-bottom: 16px;
}

.pond-card {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 12px;
  margin-bottom: 12px;
  border: 1px solid #d8e8ff;
  border-radius: 14px;
  background: #f8fbff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pond-card:hover,
.pond-card.active {
  border-color: #1f74d8;
  box-shadow: 0 8px 20px rgba(31, 116, 216, 0.12);
  transform: translateY(-1px);
}

.pond-card__name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2d3d;
}

.pond-card__type {
  margin-top: 4px;
  font-size: 12px;
  color: #7b8794;
}

.toolbar-row,
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-actions,
.pond-switcher {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.table-header {
  margin: 6px 0 16px;
}

.table-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
}

.table-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #7b8794;
}

.trace-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.trace-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 14px;
  background: #f8fbff;
}

.trace-item__content {
  flex: 1;
  min-width: 0;
}

.trace-item__aside {
  display: flex;
  align-items: center;
  gap: 10px;
}

.trace-item__title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
}

.trace-item__meta {
  margin-top: 4px;
  font-size: 12px;
  color: #7b8794;
}

.trace-item__desc {
  margin-top: 6px;
  font-size: 13px;
  color: #4f5d6b;
}

.traceability-panel {
  padding-top: 6px;
}

.traceability-hero {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  text-align: center;
  margin: 6px 0 18px;
}

.traceability-hero__icon {
  width: 72px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #eef4ff 0%, #dceaff 100%);
  color: #7f8ea3;
  font-size: 40px;
  font-weight: 700;
}

.traceability-hero__title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2d3d;
}

.traceability-hero__subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #6b7785;
}

.traceability-owner {
  margin: 14px 0 18px;
  font-size: 16px;
  color: #4f5d6b;
}

.traceability-owner strong {
  margin-right: 8px;
  font-size: 28px;
  color: #1f2d3d;
}

.traceability-owner span {
  color: #6b7785;
}

.traceability-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.traceability-grid__item {
  padding: 14px 16px;
  border-radius: 14px;
  background: #f8fbff;
  border: 1px solid #e3eefc;
}

.traceability-grid__label {
  font-size: 12px;
  color: #7b8794;
}

.traceability-grid__value {
  margin-top: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
}

.traceability-stage-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.traceability-stage {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 14px;
  background: #f8fbff;
}

.traceability-stage__title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
}

.traceability-stage__desc {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.6;
  color: #6b7785;
}


@media (max-width: 768px) {
  .farming-process-page {
    padding: 16px;
  }

  .page-hero {
    padding: 18px;
    align-items: flex-start;
    flex-direction: column;

    h1 {
      font-size: 26px;
    }
  }

  .traceability-grid,
  .traceability-stage-list {
    grid-template-columns: 1fr;
  }

  .traceability-owner strong {
    display: block;
    margin-bottom: 6px;
    font-size: 22px;
  }
}
</style>
