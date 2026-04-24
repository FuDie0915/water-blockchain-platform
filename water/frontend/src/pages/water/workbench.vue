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
              <template v-if="userType === 'enterprise'">
                <t-button theme="primary" :disabled="hasActivePermit" @click="showApplyDialog">提交许可证</t-button>
                <t-button :theme="farmerView === 'monitor' ? 'primary' : 'default'" :variant="farmerView === 'monitor' ? 'base' : 'outline'" @click="switchFarmerView('monitor')">水质监测</t-button>
                <t-button :theme="farmerView === 'warning' ? 'warning' : 'default'" :variant="farmerView === 'warning' ? 'base' : 'outline'" @click="switchFarmerView('warning')">预警中心</t-button>
                <t-button variant="outline" :loading="farmingProcessLoading" @click="openFarmingProcess">养殖过程管理</t-button>
                <t-button variant="outline" @click="showBindDialog">绑定监管局</t-button>
              </template>
              <template v-else>
                <!-- 监管局审核状态提示 -->
                <t-tag v-if="managerAuditStatus === null" theme="warning" variant="light-outline" class="audit-status-tag">
                  未提交审核
                </t-tag>
                <t-tag v-else-if="managerAuditStatus === 0" theme="primary" variant="light-outline" class="audit-status-tag">
                  待审批
                </t-tag>
                <t-tag v-else-if="managerAuditStatus === 1" theme="success" variant="light-outline" class="audit-status-tag">
                  已通过
                </t-tag>
                <t-tag v-else-if="managerAuditStatus === 2" theme="danger" variant="light-outline" class="audit-status-tag">
                  已拒绝
                </t-tag>
                <t-button v-if="managerAuditStatus === null || managerAuditStatus === 2" theme="warning" variant="outline" @click="showManagerAuditDialog">提交资质审核</t-button>
                <t-button :theme="currentView === 'dashboard' ? 'primary' : 'default'" :variant="currentView === 'dashboard' ? 'base' : 'outline'" @click="switchRegulatorView('dashboard')">监管总览</t-button>
                <t-button :theme="currentView === 'approval' ? 'primary' : 'default'" :variant="currentView === 'approval' ? 'base' : 'outline'" @click="switchRegulatorView('approval')">许可审批</t-button>
                <t-button :theme="currentView === 'bindApproval' ? 'primary' : 'default'" :variant="currentView === 'bindApproval' ? 'base' : 'outline'" @click="switchRegulatorView('bindApproval')">绑定审批</t-button>
                <t-button :theme="currentView === 'waterData' ? 'primary' : 'default'" :variant="currentView === 'waterData' ? 'base' : 'outline'" @click="switchRegulatorView('waterData')">水数据审查</t-button>
                <t-button variant="outline" :loading="farmingProcessLoading" @click="openFarmingProcess">养殖过程监管</t-button>
                <t-button variant="outline" @click="showContractDialog">绑定智能合约</t-button>
              </template>
              <t-button variant="text" @click="logout">退出登录</t-button>
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

            <div v-if="userType === 'enterprise'" class="side-card bind-status-box">
              <div class="side-card__title">监管局绑定</div>
              <div class="side-row">
                <span>绑定状态</span>
                <t-tag :theme="bindStatusTheme" variant="light-outline">{{ bindStatusText }}</t-tag>
              </div>
              <div v-if="bindStatus && bindStatus.managerName" class="side-row">
                <span>监管局</span>
                <strong>{{ bindStatus.managerName }}</strong>
              </div>
              <div v-if="bindStatus && bindStatus.applyTime" class="side-row">
                <span>申请时间</span>
                <span>{{ bindStatus.applyTime }}</span>
              </div>
              <div v-if="bindStatus && bindStatus.status === 1" class="side-row bind-action-row">
                <t-button size="small" theme="warning" variant="outline" @click="handleUnbind">解除绑定</t-button>
              </div>
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
            <t-col :xs="12" :lg="8">
              <t-card title="养殖户驾驶舱" :bordered="false" class="panel-card">
                <div class="panel-tip">围绕参考阈值展示当前关键水质、预警待办、全过程台账与区块链留痕进度。</div>
                <div class="pond-switcher">
                  <span class="pond-switcher__label">当前养殖池：</span>
                  <t-button
                    v-for="pond in pondOptions"
                    :key="pond.value"
                    size="small"
                    :theme="activePondId === pond.value ? 'primary' : 'default'"
                    :variant="activePondId === pond.value ? 'base' : 'outline'"
                    @click="activePondId = pond.value"
                  >
                    {{ pond.label }}
                  </t-button>
                  <button class="pond-add-button" type="button" @click="showPondAddTip">
                    <span class="pond-add-button__icon">+</span>
                    <span>新增养殖池</span>
                  </button>
                </div>
                <div class="metric-grid">
                  <div v-for="item in farmerMetricCards" :key="item.metricCode" class="metric-card-mini">
                    <div class="metric-card-mini__top">
                      <span>{{ item.label }}</span>
                      <t-tag :theme="item.theme" variant="light">{{ item.levelText }}</t-tag>
                    </div>
                    <div class="metric-card-mini__value">
                      {{ item.value }}
                      <small>{{ item.unit }}</small>
                    </div>
                    <div class="metric-card-mini__meta">{{ activePondLabel }} · 正常范围：{{ item.normalText }}{{ item.unit }}</div>
                    <div class="metric-card-mini__desc">{{ item.tip }}</div>
                  </div>
                </div>

                <div class="farmer-section-grid">
                  <div class="farmer-subpanel">
                    <div class="farmer-subpanel__title">预警待办</div>
                    <div v-for="item in filteredWarningList" :key="item.id" class="warning-item">
                      <div class="warning-item__head">
                        <span>{{ item.title }}</span>
                        <t-tag :theme="item.theme" variant="light-outline">{{ item.levelText }}</t-tag>
                      </div>
                      <div class="warning-item__time">{{ formatWarningType(item.type) }} · {{ item.time }}</div>
                      <div class="warning-item__desc">{{ item.detail }}</div>
                    </div>
                  </div>

                  <div class="farmer-subpanel">
                    <div class="farmer-subpanel__title">全过程台账</div>
                    <div v-for="item in filteredLedgerItems" :key="item.id" class="ledger-item">
                      <div class="ledger-item__title">{{ item.title }}</div>
                      <div class="warning-item__time">{{ item.pondName || activePondLabel }} · {{ item.time }} · {{ item.status }}</div>
                      <div class="warning-item__desc">{{ item.detail }}</div>
                    </div>
                  </div>
                </div>
              </t-card>
            </t-col>

            <t-col :xs="12" :lg="4">
              <t-card title="阈值标准对照" :bordered="false" class="panel-card info-panel">
                <div class="threshold-list">
                  <div v-for="item in primaryThresholdRules" :key="item.metricCode" class="threshold-item">
                    <div class="threshold-item__title">{{ item.label }}</div>
                    <div class="threshold-item__range">正常：{{ item.normalText }}{{ item.unit }}</div>
                    <div class="threshold-item__range">预警：{{ item.warningText }}{{ item.unit }}</div>
                    <div class="threshold-item__range danger">危险：{{ item.dangerText }}{{ item.unit }}</div>
                  </div>
                </div>
              </t-card>
            </t-col>
          </t-row>

          <t-row :gutter="[16, 16]" class="content-row">
            <t-col :xs="12" :lg="4">
              <t-card title="养殖户操作说明" :bordered="false" class="panel-card info-panel">
                <ul class="guide-list">
                  <li>使用当前平台账号自动进入养殖户工作台，无需再次输入账号密码。</li>
                  <li>实时查看水温、盐度、pH、溶解氧、氨氮、亚硝酸盐状态。</li>
                  <li>提交许可证、记录养殖全过程后，可在当前页面持续查看链上留痕状态。</li>
                </ul>

                <div class="account-box">
                  <div class="account-item">
                    <label>养殖户登录账号</label>
                    <span>{{ enterpriseAccount || platformAccount }}</span>
                  </div>
                  <div class="account-item">
                    <label>养殖户默认密码</label>
                    <span>{{ enterprisePassword || '--' }}</span>
                  </div>
                  <div class="account-item">
                    <label>区块链地址</label>
                    <span>{{ enterpriseBlockchainAddress || '--' }}</span>
                  </div>
                </div>
              </t-card>
            </t-col>

            <t-col :xs="12" :lg="8">
              <t-card title="许可证与链上存证记录" :bordered="false" class="panel-card">
                <div class="panel-tip">展示许可证审批进度，并同步汇总最近业务留痕与链上核验状态。</div>
                <div class="trace-list">
                  <div v-for="item in filteredTraceabilityTimeline" :key="item.id" class="trace-item">
                    <div class="trace-item__content">
                      <div class="ledger-item__title">{{ item.title }}</div>
                      <div class="warning-item__time">{{ item.pondName || activePondLabel }} · {{ item.time }} · {{ item.evidenceNo }}</div>
                    </div>
                    <div class="trace-item__aside">
                      <t-tag :theme="item.isOnChain ? 'success' : 'warning'" variant="light-outline">
                        {{ item.isOnChain ? '已上链' : '待上链' }}
                      </t-tag>
                    </div>
                  </div>
                </div>
                <t-table
                  row-key="id"
                  :data="enterpriseTableData"
                  :columns="enterpriseColumns"
                  bordered
                  max-height="420"
                  :empty="'暂无许可证记录'"
                />
              </t-card>
            </t-col>
          </t-row>

          <t-row :gutter="[16, 16]" class="content-row">
            <t-col :xs="12" :lg="12">
              <t-card :bordered="false" class="panel-card recent-chain-panel">
                <div class="chain-panel-header">
                  <div>
                    <div class="ledger-item__title">最近操作上链记录</div>
                    <div class="panel-tip compact">按当前养殖池汇总最近业务留痕、上链状态与区块哈希摘要。</div>
                  </div>
                  <t-button theme="primary" variant="text" @click="openFarmerTraceability">查看全部</t-button>
                </div>
                <t-table
                  row-key="id"
                  :data="recentChainRecords"
                  :columns="farmerChainColumns"
                  bordered
                  size="small"
                  max-height="320"
                  :empty="'暂无最近上链记录'"
                />
              </t-card>
            </t-col>
          </t-row>

          <t-row :gutter="[16, 16]" class="content-row">
            <t-col :xs="12" :lg="12">
              <t-card ref="farmerCenterCard" title="水质监测 / 预警中心 / 存证查询" :bordered="false" class="panel-card">
                <div class="toolbar-row monitor-toolbar">
                  <t-tabs :value="farmerView" @change="handleFarmerTabChange">
                    <t-tab-panel value="monitor" label="水质监测" />
                    <t-tab-panel value="warning" label="预警中心" />
                    <t-tab-panel value="traceability" label="存证查询" />
                  </t-tabs>

                  <div class="toolbar-actions" v-if="farmerView === 'monitor'">
                    <t-button size="small" :variant="trendRange === '24h' ? 'base' : 'outline'" @click="trendRange = '24h'">24小时</t-button>
                    <t-button size="small" :variant="trendRange === '7d' ? 'base' : 'outline'" @click="trendRange = '7d'">7天</t-button>
                    <t-button size="small" :variant="trendRange === '30d' ? 'base' : 'outline'" @click="trendRange = '30d'">30天</t-button>
                    <t-button theme="primary" variant="outline" @click="showManualEntryDialog">手动补录</t-button>
                  </div>
                </div>

                <template v-if="farmerView === 'monitor'">
                  <div class="monitor-chart-panel">
                    <div class="monitor-chart-panel__header">
                      <div>
                        <div class="ledger-item__title">水质趋势折线图</div>
                        <div class="warning-item__time">展示溶解氧与 pH 在 {{ monitorPeriodSummary.label }} 内的动态变化</div>
                      </div>
                      <t-tag theme="primary" variant="light-outline">动态趋势</t-tag>
                    </div>
                    <div class="monitor-chart-legend-tip">当前展示：溶解氧与 pH 双轴对比趋势</div>
                    <div ref="waterTrendChartRef" class="water-trend-chart"></div>
                  </div>

                  <div class="trend-summary-grid">
                    <div class="trend-summary-item">
                      <div class="summary-card__label">统计范围</div>
                      <div class="summary-card__value small">{{ monitorPeriodSummary.label }}</div>
                      <div class="summary-card__desc">{{ monitorPeriodSummary.tip }}</div>
                    </div>
                    <div class="trend-summary-item">
                      <div class="summary-card__label">采样次数</div>
                      <div class="summary-card__value small">{{ monitorPeriodSummary.samples }}</div>
                      <div class="summary-card__desc">自动采集 + 手动补录</div>
                    </div>
                    <div class="trend-summary-item">
                      <div class="summary-card__label">异常记录</div>
                      <div class="summary-card__value small">{{ monitorPeriodSummary.abnormal }}</div>
                      <div class="summary-card__desc">超过阈值范围的重点数据</div>
                    </div>
                    <div class="trend-summary-item">
                      <div class="summary-card__label">已同步存证</div>
                      <div class="summary-card__value small">{{ monitorPeriodSummary.onChain }}</div>
                      <div class="summary-card__desc">支持后续链上核验</div>
                    </div>
                  </div>

                  <t-table
                    row-key="id"
                    :data="displayMonitorRecords"
                    :columns="farmerMonitorColumns"
                    bordered
                    max-height="420"
                    :empty="'暂无监测记录'"
                  />
                </template>

                <template v-else-if="farmerView === 'warning'">
                  <div class="warning-center-grid">
                    <div v-for="item in filteredWarningList" :key="`center-${item.id}`" class="warning-center-card">
                      <div class="warning-item__head">
                        <span>{{ item.title }}</span>
                        <t-tag :theme="item.theme" variant="light">{{ item.levelText }}</t-tag>
                      </div>
                      <div class="warning-item__time">{{ formatWarningType(item.type) }} · {{ item.time }}</div>
                      <div class="warning-item__desc">{{ item.detail }}</div>
                    </div>
                  </div>
                </template>

                <template v-else>
                  <div class="traceability-layout">
                    <div class="traceability-records-panel">
                      <div class="farmer-subpanel__title">存证记录</div>
                      <div class="warning-item__time">点击左侧记录，可查看对应的区块链存证证书详情。</div>
                      <div class="traceability-record-list">
                        <button
                          v-for="item in filteredTraceabilityTimeline"
                          :key="`detail-${item.id}`"
                          type="button"
                          :class="['traceability-record-card', { active: currentTraceabilityCertificate && currentTraceabilityCertificate.id === item.id }]"
                          @click="selectedTraceabilityId = item.id"
                        >
                          <div class="traceability-record-card__head">
                            <span>{{ item.title }}</span>
                            <t-tag :theme="item.isOnChain ? 'success' : 'warning'" variant="light-outline">
                              {{ item.isOnChain ? '已上链' : '待上链' }}
                            </t-tag>
                          </div>
                          <div class="warning-item__time">{{ item.pondName || activePondLabel }} · {{ item.time }}</div>
                          <div class="traceability-record-card__meta">{{ item.evidenceNo }}</div>
                          <div class="traceability-record-card__hash">{{ getChainHashSummary(item) }}</div>
                        </button>
                      </div>
                    </div>

                    <div v-if="currentTraceabilityCertificate" class="traceability-certificate-panel">
                      <div class="certificate-head">
                        <div class="certificate-emblem">✦</div>
                        <div class="certificate-title">区块链数据存证证书</div>
                        <div class="certificate-subtitle">{{ currentTraceabilityCertificate.evidenceNo }}</div>
                      </div>

                      <div class="certificate-sheet">
                        <div class="certificate-row">
                          <div class="certificate-label">养殖户</div>
                          <div class="certificate-value">{{ currentTraceabilityCertificate.ownerName || '--' }}</div>
                        </div>
                        <div class="certificate-row">
                          <div class="certificate-label">存证内容</div>
                          <div class="certificate-value">{{ currentTraceabilityCertificate.contentTitle || currentTraceabilityCertificate.title }}</div>
                        </div>
                        <div class="certificate-row">
                          <div class="certificate-label">上链时间</div>
                          <div class="certificate-value">{{ currentTraceabilityCertificate.chainTime || currentTraceabilityCertificate.time }}</div>
                        </div>
                        <div class="certificate-row">
                          <div class="certificate-label">区块哈希</div>
                          <div class="certificate-value hash">{{ currentTraceabilityCertificate.hash || getChainHashSummary(currentTraceabilityCertificate) }}</div>
                        </div>
                        <div class="certificate-row">
                          <div class="certificate-label">区块高度</div>
                          <div class="certificate-value">{{ currentTraceabilityCertificate.blockHeight || '--' }}</div>
                        </div>
                        <div class="certificate-row">
                          <div class="certificate-label">说明</div>
                          <div class="certificate-value">{{ currentTraceabilityCertificate.description }}</div>
                        </div>
                      </div>


                      <div :class="['certificate-verify-bar', { pending: !currentTraceabilityCertificate.isOnChain }]">
                        <span class="certificate-verify-bar__icon">{{ currentTraceabilityCertificate.isOnChain ? '✔' : '…' }}</span>
                        <span>{{ currentTraceabilityCertificate.verifyText }}</span>
                      </div>
                    </div>
                  </div>
                </template>
              </t-card>
            </t-col>
          </t-row>
        </template>

        <template v-else>
          <t-row :gutter="[16, 16]" class="content-row">
            <t-col :xs="12" :lg="8">
              <t-card title="全域监管态势" :bordered="false" class="panel-card">
                <div class="panel-tip">按片区查看养殖主体、水质风险、预警流转和日常监管重点，当前为前端假数据展示版。</div>

                <div class="toolbar-row secondary">
                  <div class="pond-switcher">
                    <span class="pond-switcher__label">监管片区：</span>
                    <t-button
                      v-for="item in regulatorRegionOptions"
                      :key="item.value"
                      size="small"
                      :theme="activeRegulatorRegion === item.value ? 'primary' : 'default'"
                      :variant="activeRegulatorRegion === item.value ? 'base' : 'outline'"
                      @click="activeRegulatorRegion = item.value"
                    >
                      {{ item.label }}
                    </t-button>
                  </div>

                  <div class="toolbar-actions">
                    <t-tag theme="primary" variant="light-outline">联网养殖池 {{ displayedRegulatorPonds.length }}</t-tag>
                    <t-tag theme="warning" variant="light-outline">活跃预警 {{ activeRegulatorWarningCount }}</t-tag>
                  </div>
                </div>

                <div class="regulator-insight-grid">
                  <div v-for="item in regulatorQuickPanels" :key="item.title" class="regulator-overview-card">
                    <div class="summary-card__label">{{ item.title }}</div>
                    <div class="summary-card__value small">{{ item.value }}</div>
                    <div class="summary-card__desc">{{ item.description }}</div>
                  </div>
                </div>

                <div class="farmer-section-grid regulator-section-grid">
                  <div class="farmer-subpanel">
                    <div class="farmer-subpanel__title">重点预警清单</div>
                    <div v-for="item in displayedRegulatorWarnings.slice(0, 4)" :key="item.id" class="warning-item">
                      <div class="warning-item__head">
                        <span>{{ item.title }}</span>
                        <t-tag :theme="item.theme" variant="light-outline">{{ item.levelText }}</t-tag>
                      </div>
                      <div class="warning-item__time">{{ item.region }} · {{ item.companyName }} · {{ item.time }}</div>
                      <div class="warning-item__desc">{{ item.detail }}</div>
                    </div>
                  </div>

                  <div class="farmer-subpanel">
                    <div class="farmer-subpanel__title">养殖主体概况</div>
                    <div v-for="item in displayedFarmerProfiles.slice(0, 4)" :key="item.id" class="ledger-item">
                      <div class="ledger-item__title">{{ item.companyName }}</div>
                      <div class="warning-item__time">{{ item.region }} · 养殖池 {{ item.pondCount }} 个 · 最近上传 {{ item.latestUpload }}</div>
                      <div class="warning-item__desc">许可证 {{ item.licenseStatusText }}，链上同步率 {{ item.chainRate }}，当前 {{ item.riskText }}。</div>
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
                    <span>{{ monitorPassword || '--' }}</span>
                  </div>
                  <div class="account-item">
                    <label>区块链地址</label>
                    <span>{{ monitorBlockchainAddress || '--' }}</span>
                  </div>
                </div>

                <div class="guide-panel">
                  <div class="guide-panel__title">监管重点</div>
                  <ul class="guide-list compact">
                    <li>优先处理高风险预警池和待审批许可证。</li>
                    <li>对异常水质记录进行抽检、复核与批量上链。</li>
                    <li>统一查看链上回执与监管留痕闭环状态。</li>
                  </ul>
                </div>

                <div class="guide-panel regulator-mini-panel">
                  <div class="guide-panel__title">今日监管动态</div>
                  <div v-for="item in regulatorProcessTimeline.slice(0, 3)" :key="item.id" class="trace-item regulator-mini-trace">
                    <div>
                      <div class="ledger-item__title">{{ item.title }}</div>
                      <div class="warning-item__time">{{ item.time }}</div>
                    </div>
                    <t-tag :theme="item.theme" variant="light-outline">{{ item.statusText }}</t-tag>
                  </div>
                </div>
              </t-card>
            </t-col>
          </t-row>

          <t-row :gutter="[16, 16]" class="content-row">
            <t-col :xs="12" :lg="12">
              <t-card title="监管专项工作区" :bordered="false" class="panel-card">
                <div class="toolbar-row">
                  <t-button-group>
                    <t-button :variant="currentView === 'dashboard' ? 'base' : 'outline'" @click="switchRegulatorView('dashboard')">区域监测</t-button>
                    <t-button :variant="currentView === 'farmers' ? 'base' : 'outline'" @click="switchRegulatorView('farmers')">养殖主体</t-button>
                    <t-button :variant="currentView === 'approval' ? 'base' : 'outline'" @click="switchRegulatorView('approval')">许可审批</t-button>
                    <t-button :variant="currentView === 'bindApproval' ? 'base' : 'outline'" @click="switchRegulatorView('bindApproval')">绑定审批</t-button>
                    <t-button :variant="currentView === 'waterData' ? 'base' : 'outline'" @click="switchRegulatorView('waterData')">水数据</t-button>
                    <t-button :variant="currentView === 'chain' ? 'base' : 'outline'" @click="switchRegulatorView('chain')">链上监管</t-button>
                  </t-button-group>

                  <div class="toolbar-actions">
                    <span class="toolbar-meta">当前片区：{{ activeRegulatorRegionLabel }}</span>
                    <t-button size="small" variant="outline" @click="showContractDialog">智能合约绑定</t-button>
                  </div>
                </div>

                <template v-if="currentView === 'dashboard'">
                  <div class="monitor-chart-panel">
                    <div class="monitor-chart-panel__header">
                      <div>
                        <div class="ledger-item__title">区域风险对比趋势</div>
                        <div class="warning-item__time">对比综合评分与预警数量，辅助监管端安排抽检与巡查顺序</div>
                      </div>
                      <t-tag theme="primary" variant="light-outline">区域对比</t-tag>
                    </div>
                    <div class="monitor-chart-legend-tip">默认展示片区综合得分与预警数量；选择片区后切换为池塘明细。</div>
                    <div ref="regulatorRegionChartRef" class="water-trend-chart regulator-trend-chart"></div>
                  </div>

                  <div class="regulator-pond-grid">
                    <div v-for="item in displayedRegulatorPonds" :key="item.id" class="regulator-pond-card">
                      <div class="warning-item__head">
                        <span>{{ item.pondName }}</span>
                        <t-tag :theme="item.theme" variant="light">{{ item.statusText }}</t-tag>
                      </div>
                      <div class="warning-item__time">{{ item.region }} · {{ item.companyName }} · 最近上传 {{ item.lastUpload }}</div>
                      <div class="regulator-pond-card__metrics">
                        <span>DO {{ item.doValue }} mg/L</span>
                        <span>pH {{ item.phValue }}</span>
                        <span>盐度 {{ item.salinity }} ‰</span>
                      </div>
                      <div class="warning-item__desc">{{ item.note }}</div>
                    </div>
                  </div>
                </template>

                <template v-else-if="currentView === 'farmers'">
                  <div class="panel-tip">汇总片区内养殖主体的许可证状态、链上同步率与当前风险等级，便于开展分级监管。</div>
                  <t-table
                    row-key="id"
                    :data="displayedFarmerProfiles"
                    :columns="regulatorFarmerColumns"
                    bordered
                    max-height="520"
                    :empty="'暂无主体数据'"
                  />
                </template>

                <template v-else-if="currentView === 'approval'">
                  <div class="panel-tip">集中处理养殖户排污许可证申请，并可直接执行通过上链、驳回与链上比对。</div>
                  <t-table
                    row-key="id"
                    :data="monitorTableData"
                    :columns="monitorColumns"
                    bordered
                    max-height="520"
                    :empty="'暂无审批数据'"
                  />
                </template>

                <template v-else-if="currentView === 'waterData'">
                  <div class="warning-center-grid regulator-warning-grid">
                    <div v-for="item in displayedRegulatorWarnings.slice(0, 4)" :key="`reg-${item.id}`" class="warning-center-card">
                      <div class="warning-item__head">
                        <span>{{ item.title }}</span>
                        <t-tag :theme="item.theme" variant="light">{{ item.levelText }}</t-tag>
                      </div>
                      <div class="warning-item__time">{{ item.region }} · {{ item.pondName }} · {{ item.progress }}</div>
                      <div class="warning-item__desc">{{ item.detail }}</div>
                    </div>
                  </div>

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
                      :data="displayedTurbidityTableData"
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
                      :data="displayedTdsTableData"
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
                </template>

                <template v-else-if="currentView === 'bindApproval'">
                  <div class="panel-tip">审批养殖户的绑定申请，通过后养殖户可正常提交水质数据和许可证申请。</div>
                  <t-table
                    row-key="id"
                    :data="bindApprovalList"
                    :columns="bindApprovalColumns"
                    bordered
                    max-height="520"
                    :empty="'暂无绑定申请'"
                  />
                </template>

                <template v-else>
                  <div class="panel-tip">查看监管端已生成的链上回执、哈希摘要与核验状态，方便后续抽检留痕与追责。</div>
                  <t-table
                    row-key="id"
                    :data="regulatorChainRecords"
                    :columns="regulatorChainColumns"
                    bordered
                    max-height="360"
                    :empty="'暂无链上监管记录'"
                  />

                  <div class="trace-list big regulator-trace-list">
                    <div v-for="item in regulatorProcessTimeline" :key="`timeline-${item.id}`" class="trace-item regulator-trace-item">
                      <div>
                        <div class="ledger-item__title">{{ item.title }}</div>
                        <div class="warning-item__time">{{ item.time }}</div>
                        <div class="warning-item__desc">{{ item.detail }}</div>
                      </div>
                      <t-tag :theme="item.theme" variant="light-outline">{{ item.statusText }}</t-tag>
                    </div>
                  </div>
                </template>
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
      :visible.sync="manualEntryDialogVisible"
      header="手动补录水质数据"
      :confirm-btn="{ content: '保存并判定', theme: 'primary' }"
      :cancel-btn="{ content: '取消' }"
      @confirm="handleManualEntrySubmit"
      @cancel="resetManualEntryForm"
    >
      <t-form>
        <t-form-item label="指标类型">
          <t-select v-model="manualEntryForm.metricCode" :options="manualEntryMetricOptions" placeholder="请选择指标" />
        </t-form-item>
        <t-form-item label="检测值">
          <t-input v-model="manualEntryForm.value" placeholder="请输入检测值" />
        </t-form-item>
        <t-form-item label="备注说明">
          <t-input v-model="manualEntryForm.remark" placeholder="如：人工复测 / 巡检抽样" />
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

    <!-- 监管局审核申请对话框 -->
    <t-dialog
      :visible.sync="managerAuditDialogVisible"
      header="监管局资质审核申请"
      width="500px"
      :confirm-btn="{ content: '提交申请', theme: 'primary', loading: managerAuditSubmitting }"
      @confirm="handleManagerAuditSubmit"
      @cancel="resetManagerAuditForm"
    >
      <t-form :data="managerAuditForm" layout="vertical">
        <t-form-item label="机构名称" required>
          <t-input v-model="managerAuditForm.institutionName" placeholder="请输入机构名称，如：XX市水务监管局" />
        </t-form-item>
        <t-form-item label="管辖区域" required>
          <t-input v-model="managerAuditForm.jurisdiction" placeholder="请输入管辖区域，如：XX市XX区" />
        </t-form-item>
        <t-form-item label="联系电话" required>
          <t-input v-model="managerAuditForm.phone" placeholder="请输入联系电话" />
        </t-form-item>
        <t-form-item label="申请备注">
          <t-textarea v-model="managerAuditForm.remark" :maxlength="200" placeholder="选填，可说明申请原因或补充信息" />
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- 养殖户绑定监管局对话框 -->
    <t-dialog
      :visible.sync="bindDialogVisible"
      header="绑定监管局"
      width="500px"
      :confirm-btn="{ content: '申请绑定', theme: 'primary', loading: bindSubmitting }"
      @confirm="handleApplyBind"
      @cancel="bindDialogVisible = false"
    >
      <div class="bind-dialog-tip">选择要绑定的监管局，提交申请后等待监管局审批通过。</div>
      <t-form layout="vertical">
        <t-form-item label="选择监管局" required>
          <t-select v-model="selectedManagerId" :options="managerOptions" placeholder="请选择监管局" />
        </t-form-item>
      </t-form>
      <div v-if="bindStatus && bindStatus.status !== undefined" class="bind-current-status">
        <span>当前状态：</span>
        <t-tag :theme="bindStatusTheme" variant="light-outline">{{ bindStatusText }}</t-tag>
      </div>
    </t-dialog>

    <t-dialog
      :visible.sync="pondAddDialogVisible"
      header="新增养殖池"
      width="520px"
      :confirm-btn="{ content: '创建', theme: 'primary', loading: pondAddSubmitting }"
      :cancel-btn="{ content: '取消' }"
      @confirm="handleCreatePond"
      @cancel="resetPondAddForm"
    >
      <t-form :data="pondAddForm" layout="vertical">
        <t-form-item label="塘名" required>
          <t-input v-model="pondAddForm.pondName" placeholder="请输入塘名，如：一号塘" />
        </t-form-item>
        <t-form-item label="养殖品种" required>
          <t-select v-model="pondAddForm.breedType" :options="breedTypeOptions" placeholder="请选择养殖品种" />
        </t-form-item>
        <t-row :gutter="16">
          <t-col :span="6">
            <t-form-item label="面积(亩)" required>
              <t-input-number v-model="pondAddForm.area" :min="0" placeholder="请输入" />
            </t-form-item>
          </t-col>
          <t-col :span="6">
            <t-form-item label="水深(米)" required>
              <t-input-number v-model="pondAddForm.depth" :min="0" :decimal-places="2" placeholder="请输入" />
            </t-form-item>
          </t-col>
        </t-row>
        <t-row :gutter="16">
          <t-col :span="6">
            <t-form-item label="养殖周期开始" required>
              <t-date-picker v-model="pondAddForm.startDate" clearable />
            </t-form-item>
          </t-col>
          <t-col :span="6">
            <t-form-item label="养殖周期结束" required>
              <t-date-picker v-model="pondAddForm.endDate" clearable />
            </t-form-item>
          </t-col>
        </t-row>
        <t-form-item label="池塘状态" required>
          <t-select v-model="pondAddForm.status" :options="pondStatusOptions" />
        </t-form-item>
      </t-form>
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
  getThreshold,
} from '@/api/water/water.js';
import { getPondList, getManagerPondList, createPond } from '@/api/water/pond.js';
import { getSeedList, getFeedList, getMedicineList, getHarvestList } from '@/api/water/farming-process.js';
import { getManagerSeedList, getManagerFeedList, getManagerMedicineList, getManagerHarvestList } from '@/api/water/farming-process.js';
import { getBindStatus, getManagerBindList, getManagerList, applyBind, unbind, managerApproveBind, managerRejectBind } from '@/api/water/bind.js';
import { getFarmerDashboard, getManagerDashboard } from '@/api/water/dashboard.js';
import { getManagerAuditStatus, submitManagerAudit } from '@/api/water/managerAudit.js';
import { TooltipComponent, LegendComponent, GridComponent } from 'echarts/components';
import { LineChart } from 'echarts/charts';
import { CanvasRenderer } from 'echarts/renderers';
import * as echarts from 'echarts/core';
import { ImageViewer as TImageViewer, Image as TImage } from 'tdesign-vue';
import { WATER_DATA_TYPES, METRIC_LEVEL_THEME, getWaterMetricLevel } from '@/constants/water-thresholds.js';

echarts.use([TooltipComponent, LegendComponent, GridComponent, LineChart, CanvasRenderer]);

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
      currentView: 'dashboard',
      waterDataType: 'turbidity',
      farmingProcessLoading: false,
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
      pondOptions: [],
      activePondId: '',
      selectedTraceabilityId: null,
      pondMetricsMap: {},
      warningTodoList: [],
      farmingLedgerItems: [],
      traceabilityTimeline: [],
      farmerView: 'monitor',
      trendRange: '24h',
      manualEntryDialogVisible: false,
      // 监管局审核申请相关
      managerAuditStatus: null, // null=未查询, 0=待审核, 1=已通过, 2=已拒绝
      managerAuditInfo: null,
      managerAuditDialogVisible: false,
      managerAuditSubmitting: false,
      managerAuditForm: {
        institutionName: '',
        jurisdiction: '',
        phone: '',
        remark: '',
      },
      // 养殖户绑定监管局相关
      bindDialogVisible: false,
      bindSubmitting: false,
      bindStatus: null,
      managerList: [],
      selectedManagerId: '',
      // 新增养殖池相关
      pondAddDialogVisible: false,
      pondAddSubmitting: false,
      pondAddForm: {
        pondName: '',
        breedType: '',
        area: null,
        depth: null,
        startDate: '',
        endDate: '',
        status: 1,
      },
      breedTypeOptions: [
        { label: '南美白对虾', value: '南美白对虾' },
        { label: '斑节对虾', value: '斑节对虾' },
        { label: '石斑鱼', value: '石斑鱼' },
        { label: '鲈鱼', value: '鲈鱼' },
        { label: '大黄鱼', value: '大黄鱼' },
        { label: '其他', value: '其他' },
      ],
      pondStatusOptions: [
        { label: '空闲', value: 0 },
        { label: '养殖中', value: 1 },
        { label: '休整', value: 2 },
      ],
      bindApprovalList: [],
      bindApprovalColumns: [
        { colKey: 'id', title: '申请ID', width: 80 },
        { colKey: 'farmerName', title: '养殖户', minWidth: 140 },
        { colKey: 'farmerAccount', title: '养殖户账号', minWidth: 130 },
        { colKey: 'applyTime', title: '申请时间', width: 160 },
        {
          colKey: 'status',
          title: '状态',
          width: 100,
          cell: (h, { row }) => {
            const statusMap = {
              0: { theme: 'warning', text: '待审批' },
              1: { theme: 'success', text: '已通过' },
              2: { theme: 'danger', text: '已拒绝' },
            };
            const status = statusMap[row.status] || { theme: 'default', text: '未知' };
            return h('t-tag', { props: { theme: status.theme, variant: 'light-outline' } }, status.text);
          },
        },
        {
          colKey: 'operation',
          title: '操作',
          width: 180,
          cell: (h, { row }) => {
            if (row.status !== 0) {
              return h('span', { style: { color: '#6a7f95' } }, '--');
            }
            return h('div', { style: { display: 'flex', gap: '8px' } }, [
              h('t-button', {
                props: { theme: 'success', variant: 'text', size: 'small' },
                on: { click: () => this.handleApproveBind(row) },
              }, '通过'),
              h('t-button', {
                props: { theme: 'danger', variant: 'text', size: 'small' },
                on: { click: () => this.handleRejectBind(row) },
              }, '拒绝'),
            ]);
          },
        },
      ],
      waterTrendChart: null,
      regulatorRegionChart: null,
      chartRefreshTimer: null,
      chartRenderRetryTimer: null,
      regulatorChartRetryTimer: null,
      chartRefreshTick: 0, // deprecated, kept for compatibility
      activeRegulatorRegion: 'all',
      regulatorRegionOptions: [
        { value: 'all', label: '全部片区' },
      ],
      manualEntryForm: {
        metricCode: 'DO',
        value: '',
        remark: '',
      },
      monitorRecords: [],
      regulatorPondSnapshots: [],
      regulatorWarningItems: [],
      regulatorFarmerProfiles: [],
      regulatorChainRecords: [],
      regulatorProcessTimeline: [],
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
          title: '养殖户名称',
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
          title: '养殖户名称',
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
                          props: { content: '确认通过并上链吗？' },
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
                          props: { content: '确定驳回请求吗？' },
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
        { colKey: 'companyName', title: '养殖户名称', minWidth: 180 },
        { colKey: 'region', title: '所属片区', width: 140 },
        { colKey: 'pondName', title: '养殖池', width: 140 },
        { colKey: 'data', title: '浑浊度', width: 120 },
        { colKey: 'time', title: '采集时间', width: 180 },
        {
          colKey: 'status',
          title: '状态',
          width: 100,
          cell: (h, { row }) => h('t-tag', { props: { theme: row.status === '正常' ? 'success' : 'danger', variant: 'light' } }, row.status),
        },
        {
          colKey: 'isOnChain',
          title: '是否上链',
          width: 110,
          cell: (h, { row }) => h('t-tag', { props: { theme: row.isOnChain ? 'success' : 'warning', variant: 'light' } }, row.isOnChain ? '已上链' : '未上链'),
        },
      ],
      tdsColumns: [
        { colKey: 'row-select', type: 'multiple', width: 50, fixed: 'left' },
        { colKey: 'id', title: '序号', width: 80 },
        { colKey: 'companyName', title: '养殖户名称', minWidth: 180 },
        { colKey: 'region', title: '所属片区', width: 140 },
        { colKey: 'pondName', title: '养殖池', width: 140 },
        { colKey: 'data', title: 'TDS 值', width: 130 },
        { colKey: 'time', title: '采集时间', width: 180 },
        {
          colKey: 'status',
          title: '状态',
          width: 100,
          cell: (h, { row }) => h('t-tag', { props: { theme: row.status === '正常' ? 'success' : 'danger', variant: 'light' } }, row.status),
        },
        {
          colKey: 'isOnChain',
          title: '是否上链',
          width: 110,
          cell: (h, { row }) => h('t-tag', { props: { theme: row.isOnChain ? 'success' : 'warning', variant: 'light' } }, row.isOnChain ? '已上链' : '未上链'),
        },
      ],
      farmerMonitorColumns: [
        { colKey: 'time', title: '采集时间', width: 180 },
        { colKey: 'metricLabel', title: '指标类型', width: 140 },
        { colKey: 'valueText', title: '检测值', width: 140 },
        { colKey: 'sourceText', title: '来源', width: 120 },
        {
          colKey: 'levelText',
          title: '判定结果',
          width: 110,
          cell: (h, { row }) => h('t-tag', { props: { theme: row.theme || 'primary', variant: 'light' } }, row.levelText),
        },
        { colKey: 'suggestion', title: '处置建议' },
      ],
      farmerChainColumns: [
        { colKey: 'time', title: '时间', width: 140 },
        {
          colKey: 'actionType',
          title: '操作类型',
          width: 120,
          cell: (h, { row }) => h('t-tag', { props: { theme: row.actionTheme || 'primary', variant: 'light' } }, row.actionType),
        },
        { colKey: 'detail', title: '详情', minWidth: 260 },
        {
          colKey: 'statusText',
          title: '上链状态',
          width: 110,
          cell: (h, { row }) => h('t-tag', { props: { theme: row.statusTheme || 'success', variant: 'light-outline' } }, row.statusText),
        },
        {
          colKey: 'hashShort',
          title: '区块哈希',
          minWidth: 150,
          cell: (h, { row }) => h('span', { class: 'chain-hash-text' }, row.hashShort),
        },
      ],
      regulatorFarmerColumns: [
        { colKey: 'companyName', title: '养殖主体', minWidth: 180 },
        { colKey: 'accountId', title: '主体编号', width: 120 },
        { colKey: 'region', title: '所属片区', width: 150 },
        { colKey: 'pondCount', title: '养殖池数', width: 110 },
        {
          colKey: 'licenseStatusText',
          title: '许可证状态',
          width: 130,
          cell: (h, { row }) => h('t-tag', { props: { theme: row.licenseTheme || 'primary', variant: 'light' } }, row.licenseStatusText),
        },
        { colKey: 'chainRate', title: '链上同步率', width: 120 },
        { colKey: 'latestUpload', title: '最近上传', width: 140 },
        {
          colKey: 'riskText',
          title: '风险等级',
          width: 120,
          cell: (h, { row }) => h('t-tag', { props: { theme: row.riskTheme || 'primary', variant: 'light-outline' } }, row.riskText),
        },
      ],
      regulatorChainColumns: [
        { colKey: 'businessType', title: '业务类型', width: 120 },
        { colKey: 'evidenceNo', title: '存证编号', minWidth: 190 },
        { colKey: 'companyName', title: '养殖主体', minWidth: 160 },
        { colKey: 'uploadTime', title: '提交时间', width: 180 },
        {
          colKey: 'verifyText',
          title: '核验状态',
          width: 120,
          cell: (h, { row }) => h('t-tag', { props: { theme: row.theme || 'primary', variant: 'light' } }, row.verifyText),
        },
        { colKey: 'hash', title: '哈希摘要', minWidth: 150 },
      ],
    };
  },
  computed: {
    routeRoleText() {
      return this.$route?.name === 'monitor-login' ? '监管端' : '养殖户端';
    },
    roleBadge() {
      return this.userType === 'monitor' ? '监管端 · 全域巡查 · 许可审批' : '养殖户端 · 水质仪表盘 · 全程留痕';
    },
    roleTitle() {
      return this.userType === 'monitor' ? '海水养殖监管驾驶舱' : '养殖户可信养殖工作台';
    },
    roleSubtitle() {
      return this.userType === 'monitor'
        ? '集中查看片区风险、养殖主体合规状态、水质异常与链上存证进度，支持监管端开展分级巡查。'
        : '基于当前平台登录态快速进入养殖户工作台，集中查看阈值化水质指标、预警待办、存证进度与许可证状态。';
    },
    platformAccount() {
      return localStorage.getItem('platformUserAccount') || this.$store.state.user?.userInfo?.userAccount || '--';
    },
    currentRoleToken() {
      return this.userType === 'monitor' ? localStorage.getItem('managertoken') : localStorage.getItem('farmertoken');
    },
    currentChainAddress() {
      const storedAddress = localStorage.getItem('platformAccountAddress') || '';
      if (this.userType === 'monitor') {
        return this.monitorBlockchainAddress || storedAddress || '--';
      }
      return this.enterpriseBlockchainAddress || storedAddress || '--';
    },
    bindStatusText() {
      if (!this.bindStatus) return '未绑定';
      const status = this.bindStatus.status;
      if (status === 0) return '待审批';
      if (status === 1) return '已绑定';
      if (status === 2) return '已拒绝';
      return '未绑定';
    },
    bindStatusTheme() {
      if (!this.bindStatus) return 'default';
      const status = this.bindStatus.status;
      if (status === 0) return 'warning';
      if (status === 1) return 'success';
      if (status === 2) return 'danger';
      return 'default';
    },
    managerOptions() {
      return this.managerList.map((item) => ({
        label: item.userName || item.userAccount,
        value: item.userId,
      }));
    },
    permitSource() {
      return this.userType === 'monitor' ? this.monitorTableData : this.enterpriseTableData;
    },
    pendingPermitCount() {
      return this.monitorTableData.filter((item) => Number(item.status) === 0).length;
    },
    approvedPermitCount() {
      return this.permitSource.filter((item) => Number(item.status) === 1).length;
    },
    rejectedPermitCount() {
      return this.permitSource.filter((item) => Number(item.status) === 2).length;
    },
    abnormalWaterCount() {
      return [...this.turbidityTableData, ...this.tdsTableData].filter((item) => item.status && item.status !== '正常').length;
    },
    primaryThresholdRules() {
      return WATER_DATA_TYPES.filter((item) => !['TDS', 'TURBIDITY'].includes(item.metricCode));
    },
    activePondLabel() {
      return this.pondOptions.find((item) => item.value === this.activePondId)?.label || '当前养殖池';
    },
    activeRegulatorRegionLabel() {
      return this.regulatorRegionOptions.find((item) => item.value === this.activeRegulatorRegion)?.label || '全部片区';
    },
    farmerMetrics() {
      return this.pondMetricsMap[this.activePondId] || [];
    },
    farmerMetricCards() {
      return this.farmerMetrics.map((item) => {
        const rule = WATER_DATA_TYPES.find((ruleItem) => ruleItem.metricCode === item.metricCode) || {};
        const level = getWaterMetricLevel(item.metricCode, item.value);
        return {
          ...item,
          ...rule,
          level,
          theme: METRIC_LEVEL_THEME[level] || 'primary',
          levelText: this.formatMetricLevel(level),
        };
      });
    },
    filteredWarningList() {
      return this.warningTodoList.filter((item) => item.pondId === this.activePondId);
    },
    filteredLedgerItems() {
      return this.farmingLedgerItems.filter((item) => item.pondId === this.activePondId);
    },
    filteredTraceabilityTimeline() {
      return this.traceabilityTimeline.filter((item) => item.pondId === this.activePondId);
    },
    currentTraceabilityCertificate() {
      return this.filteredTraceabilityTimeline.find((item) => item.id === this.selectedTraceabilityId)
        || this.filteredTraceabilityTimeline[0]
        || null;
    },
    recentChainRecords() {
      return this.filteredTraceabilityTimeline
        .map((item, index) => ({
          id: item.id,
          time: item.time,
          actionType: this.getChainActionType(item),
          actionTheme: item.isOnChain ? 'primary' : 'warning',
          detail: `${item.title} · ${item.evidenceNo}`,
          statusText: item.isOnChain ? '已上链' : '待上链',
          statusTheme: item.isOnChain ? 'success' : 'warning',
          hashShort: this.getChainHashSummary(item, index),
        }))
        .slice(0, 5);
    },
    farmerWarningCount() {
      return this.filteredWarningList.filter((item) => item.level !== 'normal').length;
    },
    todayChainCount() {
      return this.filteredTraceabilityTimeline.filter((item) => item.isOnChain).length;
    },
    manualEntryMetricOptions() {
      return this.primaryThresholdRules.map((item) => ({
        label: `${item.label}${item.unit ? `（${item.unit}）` : ''}`,
        value: item.metricCode,
      }));
    },
    monitorPeriodSummary() {
      const configMap = {
        '24h': { label: '近24小时', samples: this.monitorRecords.length, abnormal: this.monitorRecords.filter(r => r.levelText === '预警' || r.levelText === '危险').length, onChain: this.monitorRecords.filter(r => r.isOnChain).length, tip: '更适合查看实时波动与短时风险。' },
        '7d': { label: '近7天', samples: this.monitorRecords.length, abnormal: this.monitorRecords.filter(r => r.levelText === '预警' || r.levelText === '危险').length, onChain: this.monitorRecords.filter(r => r.isOnChain).length, tip: '适合观察本周水质变化趋势。' },
        '30d': { label: '近30天', samples: this.monitorRecords.length, abnormal: this.monitorRecords.filter(r => r.levelText === '预警' || r.levelText === '危险').length, onChain: this.monitorRecords.filter(r => r.isOnChain).length, tip: '适合复盘阶段性养殖环境变化。' },
      };
      return configMap[this.trendRange] || configMap['24h'];
    },
    displayMonitorRecords() {
      const countMap = { '24h': 5, '7d': 8, '30d': 12 };
      return this.monitorRecords.filter((item) => item.pondId === this.activePondId).slice(0, countMap[this.trendRange] || 5);
    },
    displayedRegulatorPonds() {
      if (this.activeRegulatorRegion === 'all') {
        return this.regulatorPondSnapshots;
      }
      return this.regulatorPondSnapshots.filter((item) => item.regionCode === this.activeRegulatorRegion);
    },
    displayedRegulatorWarnings() {
      if (this.activeRegulatorRegion === 'all') {
        return this.regulatorWarningItems;
      }
      return this.regulatorWarningItems.filter((item) => item.regionCode === this.activeRegulatorRegion);
    },
    linkedRegulatorFarmers() {
      const permitNames = this.monitorTableData.map((item) => item.companyName).filter(Boolean);
      const baseNames = this.regulatorFarmerProfiles.map((item) => item.companyName).filter(Boolean);
      const mergedNames = [...new Set([...permitNames, ...baseNames])];

      return mergedNames.map((companyName, index) => {
        const matchedProfile = this.regulatorFarmerProfiles.find((item) => item.companyName === companyName) || this.regulatorFarmerProfiles[index % this.regulatorFarmerProfiles.length];
        const permitRows = this.monitorTableData.filter((item) => item.companyName === companyName);
        const latestPermit = permitRows[0] || {};
        const statusMap = {
          0: { text: '待审批', theme: 'warning' },
          1: { text: '许可证有效', theme: 'success' },
          2: { text: '审批未通过', theme: 'danger' },
        };
        const fallbackState = matchedProfile.licenseStatusText
          ? { text: matchedProfile.licenseStatusText, theme: matchedProfile.licenseTheme || 'primary' }
          : statusMap[0];
        const permitState = statusMap[Number(latestPermit.status)] || fallbackState;

        return {
          ...matchedProfile,
          id: latestPermit.id || matchedProfile.id || index + 1,
          companyName,
          accountId: matchedProfile.accountId || '',
          latestUpload: latestPermit.createTime || matchedProfile.latestUpload || '--',
          licenseStatusText: permitState.text,
          licenseTheme: permitState.theme,
        };
      });
    },
    displayedFarmerProfiles() {
      if (this.activeRegulatorRegion === 'all') {
        return this.linkedRegulatorFarmers;
      }
      return this.linkedRegulatorFarmers.filter((item) => item.regionCode === this.activeRegulatorRegion);
    },
    displayedTurbidityTableData() {
      if (this.activeRegulatorRegion === 'all') {
        return this.turbidityTableData;
      }
      return this.turbidityTableData.filter((item) => item.regionCode === this.activeRegulatorRegion);
    },
    displayedTdsTableData() {
      if (this.activeRegulatorRegion === 'all') {
        return this.tdsTableData;
      }
      return this.tdsTableData.filter((item) => item.regionCode === this.activeRegulatorRegion);
    },
    activeRegulatorWarningCount() {
      return this.displayedRegulatorWarnings.filter((item) => item.progress !== '已闭环').length;
    },
    highRiskPondCount() {
      return this.displayedRegulatorPonds.filter((item) => item.theme === 'danger').length;
    },
    regulatorQuickPanels() {
      const totalRegions = this.regulatorRegionOptions.filter((item) => item.value !== 'all').length;
      const coveredRegions = new Set(this.displayedRegulatorPonds.map((item) => item.regionCode)).size;
      const permitRate = Math.round((this.approvedPermitCount / Math.max(this.permitSource.length, 1)) * 100);
      const closedWarningCount = this.displayedRegulatorWarnings.filter((item) => item.progress === '已闭环').length;
      const closureRate = Math.round((closedWarningCount / Math.max(this.displayedRegulatorWarnings.length, 1)) * 100);

      return [
        {
          title: '片区接入进度',
          value: `${coveredRegions}/${totalRegions}`,
          description: '当前片区均已纳入监管台账。',
        },
        {
          title: '许可证合规率',
          value: `${permitRate}%`,
          description: '已审批许可证 / 当前申请总量。',
        },
        {
          title: '重点监管主体',
          value: this.displayedFarmerProfiles.length,
          description: '支持分级查看主体合规与同步情况。',
        },
        {
          title: '预警闭环率',
          value: `${closureRate}%`,
          description: '展示今日已闭环的异常预警处理比例。',
        },
      ];
    },
    regulatorRegionChartData() {
      if (this.activeRegulatorRegion === 'all') {
        const regionList = this.regulatorRegionOptions.filter((item) => item.value !== 'all');
        return {
          xAxis: regionList.map((item) => item.label),
          scoreValues: regionList.map((item) => {
            const regionPonds = this.regulatorPondSnapshots.filter((pond) => pond.regionCode === item.value);
            const score = regionPonds.reduce((sum, pond) => sum + pond.score, 0) / Math.max(regionPonds.length, 1);
            return Number(score.toFixed(1));
          }),
          warningValues: regionList.map((item) => this.regulatorWarningItems.filter((warning) => warning.regionCode === item.value && warning.progress !== '已闭环').length),
        };
      }

      return {
        xAxis: this.displayedRegulatorPonds.map((item) => item.pondName),
        scoreValues: this.displayedRegulatorPonds.map((item) => item.score),
        warningValues: this.displayedRegulatorPonds.map((item) => item.warningCount),
      };
    },
    monitorTrendData() {
      // 如果没有监测记录数据，返回空趋势
      if (!this.monitorRecords || this.monitorRecords.length === 0) {
        return { xAxis: [], doValues: [], phValues: [] };
      }

      // 基于实际监测记录生成趋势数据
      const records = this.monitorRecords.filter(r => r.pondId === this.activePondId);
      const xAxis = records.map(r => r.time || '');
      const doValues = records.map(r => {
        const val = Number(String(r.valueText || r.value || '').replace(/[^\d.\-]/g, ''));
        return isNaN(val) ? 0 : val;
      });
      const phValues = records.map(r => {
        const val = Number(String(r.valueText || r.value || '').replace(/[^\d.\-]/g, ''));
        return isNaN(val) ? 0 : val;
      });

      return { xAxis, doValues, phValues };
    },
    summaryCards() {
      if (this.userType === 'monitor') {
        return [
          {
            title: '联网养殖池',
            value: this.displayedRegulatorPonds.length,
            description: `${this.activeRegulatorRegionLabel}内当前纳入监管的池塘数量`,
          },
          {
            title: '高风险养殖池',
            value: this.highRiskPondCount,
            description: '建议优先安排现场抽检与复核',
          },
          {
            title: '待审批许可证',
            value: this.pendingPermitCount,
            description: '待监管侧审核并反馈处理结果',
          },
          {
            title: '活跃预警事件',
            value: this.activeRegulatorWarningCount,
            description: '含水质超标、设备异常等重点事项',
          },
        ];
      }

      return [
        {
          title: '核心监测指标',
          value: this.farmerMetricCards.length,
          description: '已纳入养殖户首页展示的关键水质项',
        },
        {
          title: '当前预警',
          value: this.farmerWarningCount,
          description: '需尽快跟进处理的预警与风险提示',
        },
        {
          title: '今日已上链',
          value: this.todayChainCount,
          description: '已形成链上留痕的业务记录条数',
        },
        {
          title: '许可证通过',
          value: this.approvedPermitCount,
          description: '已通过审批并可执行链上核验',
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
      return this.enterpriseTableData.some((item) => Number(item.status) === 0 || Number(item.status) === 1);
    },
  },
  watch: {
    waterDataType() {
      if (this.userType === 'monitor') {
        this.fetchWaterData();
      }
    },
    trendRange() {
      this.$nextTick(() => {
        this.queueRenderWaterTrendChart();
      });
    },
    farmerView(value) {
      if (value === 'monitor') {
        this.$nextTick(() => {
          this.queueRenderWaterTrendChart();
          this.startChartAutoRefresh();
        });
      } else {
        this.stopChartAutoRefresh();
      }
    },
    currentView(value) {
      if (this.userType === 'monitor' && value === 'dashboard') {
        this.$nextTick(() => {
          this.queueRenderRegulatorChart();
        });
      }
    },
    activeRegulatorRegion() {
      if (this.userType === 'monitor' && this.currentView === 'dashboard') {
        this.$nextTick(() => {
          this.queueRenderRegulatorChart();
        });
      }
    },
    '$route.name'() {
      this.autoEnterByRoute();
    },
  },
  created() {
    this.setDefaultAccountInfo();
    this.autoEnterByRoute();
  },
  mounted() {
    window.addEventListener('resize', this.handleWaterChartResize, false);
    setTimeout(() => {
      this.prefetchFarmingProcessPage();
    }, 180);
  },
  beforeDestroy() {
    this.stopChartAutoRefresh();
    if (this.chartRenderRetryTimer) {
      clearTimeout(this.chartRenderRetryTimer);
      this.chartRenderRetryTimer = null;
    }
    if (this.regulatorChartRetryTimer) {
      clearTimeout(this.regulatorChartRetryTimer);
      this.regulatorChartRetryTimer = null;
    }
    window.removeEventListener('resize', this.handleWaterChartResize, false);
    if (this.waterTrendChart) {
      this.waterTrendChart.dispose();
      this.waterTrendChart = null;
    }
    if (this.regulatorRegionChart) {
      this.regulatorRegionChart.dispose();
      this.regulatorRegionChart = null;
    }
  },
  methods: {
    async initRoleWorkbench(userType) {
      this.currentLoginType = userType;
      this.userType = userType;
      this.currentUser = userType === 'enterprise' ? '养殖户用户' : '监管局用户';
      this.currentView = userType === 'monitor' ? 'dashboard' : 'approval';

      // 监管局登录时检查审核状态
      if (userType === 'monitor') {
        await this.checkManagerAuditStatus();
      }

      // 养殖户登录时检查绑定状态
      if (userType === 'enterprise') {
        await this.fetchBindStatus();
      }

      // 加载基础数据
      await Promise.all([
        this.fetchPermissionList(),
        this.fetchPondList(),
        this.fetchDashboardData(),
        this.getAccountInfo(),
      ]);

      if (userType === 'monitor') {
        await this.fetchWaterData();
      }

      // 加载养殖过程数据
      await this.fetchFarmingProcessData();

      this.isLoggedIn = true;
      if (userType === 'enterprise') {
        this.$nextTick(() => {
          this.queueRenderWaterTrendChart();
          this.startChartAutoRefresh();
        });
      } else {
        this.$nextTick(() => {
          this.queueRenderRegulatorChart();
        });
      }
    },
    async checkManagerAuditStatus() {
      try {
        const res = await getManagerAuditStatus();
        if (res.code === 0 && res.data) {
          this.managerAuditInfo = res.data;
          this.managerAuditStatus = res.data.status;
          // 如果审核被拒绝，允许重新提交
          if (this.managerAuditStatus === 2) {
            this.$message.warning('您的监管局资质审核未通过，请重新提交申请');
          }
        } else {
          // 未提交审核申请
          this.managerAuditStatus = null;
          this.managerAuditInfo = null;
        }
      } catch (error) {
        console.error('获取审核状态失败:', error);
        this.managerAuditStatus = null;
      }
    },
    showManagerAuditDialog() {
      this.managerAuditDialogVisible = true;
      if (this.managerAuditInfo) {
        // 如果有之前的申请信息，填充表单
        this.managerAuditForm = {
          institutionName: this.managerAuditInfo.institutionName || '',
          jurisdiction: this.managerAuditInfo.jurisdiction || '',
          phone: this.managerAuditInfo.phone || '',
          remark: this.managerAuditInfo.remark || '',
        };
      }
    },
    async handleManagerAuditSubmit() {
      const { institutionName, jurisdiction, phone } = this.managerAuditForm;
      if (!institutionName || !jurisdiction || !phone) {
        this.$message.warning('请填写完整的申请信息');
        return;
      }
      this.managerAuditSubmitting = true;
      try {
        const res = await submitManagerAudit(this.managerAuditForm);
        if (res.code === 0) {
          this.$message.success('审核申请已提交，请等待管理员审批');
          this.managerAuditDialogVisible = false;
          await this.checkManagerAuditStatus();
        } else {
          this.$message.error(res.message || '提交失败');
        }
      } catch (error) {
        console.error('提交审核申请失败:', error);
        this.$message.error('提交失败，请稍后重试');
      } finally {
        this.managerAuditSubmitting = false;
      }
    },
    resetManagerAuditForm() {
      this.managerAuditForm = {
        institutionName: '',
        jurisdiction: '',
        phone: '',
        remark: '',
      };
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

      const passwordCandidates = [localStorage.getItem('platformUserPassword')].filter(Boolean);

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
              localStorage.setItem('farmertoken', response.data.token);
            } else {
              localStorage.setItem('managertoken', response.data.token);
            }
            await this.initRoleWorkbench(userType);
            this.$message.success(`已使用当前平台账号进入${userType === 'enterprise' ? '养殖户端' : '监管端'}`);
            return true;
          }
        }
      } catch (error) {
        console.log(error, 'error');
      } finally {
        this.loading = false;
      }

      await this.initRoleWorkbench(userType);
      this.$message.warning(`${userType === 'enterprise' ? '养殖户端' : '监管端'}接口暂不可用，已进入前端演示模式`);
      return true;
    },
    async autoEnterByRoute() {
      const routeName = this.$route?.name;
      if (!['enterprise-login', 'monitor-login'].includes(routeName)) {
        return;
      }

      const userType = routeName === 'enterprise-login' ? 'enterprise' : 'monitor';
      const roleTokenKey = userType === 'enterprise' ? 'farmertoken' : 'managertoken';

      if (!localStorage.getItem('satoken')) {
        this.$message.error('请先登录平台账号');
        this.$router.replace('/login');
        return;
      }

      let currentRole = this.$store.getters['user/roles'] || localStorage.getItem('platformUserRole') || '';
      if (!currentRole) {
        try {
          const data = await this.$store.dispatch('user/getUserInfo');
          currentRole = data?.userResp?.userRole || data?.userRole || '';
        } catch (error) {
          console.log(error, 'error');
        }
      }

      if (currentRole === 'admin') {
        this.$message.warning('当前账号将自动进入平台总览页');
        this.$router.replace('/dashboard/base');
        return;
      }

      if (currentRole === 'farmers' && userType !== 'enterprise') {
        this.$message.warning('当前为养殖户登录态，已返回养殖户控制台');
        this.$router.replace('/water/enterprise-login');
        return;
      }

      if (currentRole === 'manager' && userType !== 'monitor') {
        this.$message.warning('当前为监管端登录态，已返回监管控制台');
        this.$router.replace('/water/monitor-login');
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
    async logout() {
      try {
        await this.$store.dispatch('user/logout');
        await this.$store.dispatch('permission/restore');
        this.$message.success('已退出登录');
      } finally {
        this.isLoggedIn = false;
        this.userType = '';
        this.currentUser = '';
        this.currentLoginType = '';
        this.$router.replace('/login').catch(() => '');
      }
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
    formatMetricLevel(level) {
      const levelMap = {
        normal: '正常',
        warning: '预警',
        danger: '危险',
      };
      return levelMap[level] || '关注';
    },
    formatWarningType(type) {
      const typeMap = {
        water_quality: '水质超标',
        environment: '环境异常',
        device_offline: '设备离线',
        farming_noncompliance: '养殖规范异常',
      };
      return typeMap[type] || '风险提示';
    },
    async prefetchFarmingProcessPage() {
      if (this._farmingProcessPrefetched) {
        return;
      }
      try {
        await import('@/pages/water/farming-process.vue');
        this._farmingProcessPrefetched = true;
      } catch (error) {
        console.warn('预加载养殖过程页面失败:', error?.message || error);
      }
    },
    async openFarmingProcess() {
      if (this.farmingProcessLoading) {
        return;
      }
      this.farmingProcessLoading = true;
      try {
        await this.prefetchFarmingProcessPage();
        await this.$router.push('/water/breeding').catch(() => '');
      } finally {
        this.farmingProcessLoading = false;
      }
    },
    openFarmerTraceability() {
      const firstRecord = this.filteredTraceabilityTimeline[0];
      if (firstRecord) {
        this.selectedTraceabilityId = firstRecord.id;
      }
      this.switchFarmerView('traceability');
    },
    getChainActionType(item) {
      const evidenceNo = item?.evidenceNo || '';
      const title = item?.title || '';

      if (evidenceNo.includes('BC-WATER') || title.includes('水质')) return '水质数据';
      if (evidenceNo.includes('BC-FEED') || title.includes('投喂')) return '投喂记录';
      if (evidenceNo.includes('BC-PERMIT') || title.includes('许可证')) return '许可证';
      if (evidenceNo.includes('BC-SEED') || title.includes('苗种')) return '苗种投放';
      if (evidenceNo.includes('BC-MED') || title.includes('用药')) return '用药记录';
      if (evidenceNo.includes('BC-ALERT')) return '预警处置';
      if (evidenceNo.includes('BC-MANUAL')) return '手动补录';
      return '养殖留痕';
    },
    getChainHashSummary(item, index = 0) {
      if (item?.hash) {
        const hash = String(item.hash);
        if (hash.length <= 22) return hash;
        return `${hash.slice(0, 12)}...${hash.slice(-8)}`;
      }

      const source = `${item?.evidenceNo || item?.title || 'CHAIN'}-${index}`;
      let hash = 0;
      for (let i = 0; i < source.length; i += 1) {
        hash = (hash * 33 + source.charCodeAt(i)) >>> 0;
      }
      return `0x${hash.toString(16).padStart(8, '0')}...`;
    },
    switchFarmerView(view) {
      this.farmerView = view;
      this.$nextTick(() => {
        const target = this.$refs.farmerCenterCard?.$el || this.$refs.farmerCenterCard;
        if (target?.scrollIntoView) {
          target.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
        if (view === 'monitor') {
          this.queueRenderWaterTrendChart();
          this.startChartAutoRefresh();
        } else {
          this.stopChartAutoRefresh();
        }
      });
    },
    handleFarmerTabChange(value) {
      this.farmerView = value;
      if (value === 'monitor') {
        this.$nextTick(() => {
          this.queueRenderWaterTrendChart();
          this.startChartAutoRefresh();
        });
      } else {
        this.stopChartAutoRefresh();
      }
    },
    queueRenderWaterTrendChart(delay = 80) {
      if (this.chartRenderRetryTimer) {
        clearTimeout(this.chartRenderRetryTimer);
      }
      this.chartRenderRetryTimer = setTimeout(() => {
        this.renderWaterTrendChart();
      }, delay);
    },
    queueRenderRegulatorChart(delay = 80) {
      if (this.regulatorChartRetryTimer) {
        clearTimeout(this.regulatorChartRetryTimer);
      }
      this.regulatorChartRetryTimer = setTimeout(() => {
        this.renderRegulatorChart();
      }, delay);
    },
    handleWaterChartResize() {
      if (this.waterTrendChart) {
        this.waterTrendChart.resize();
      }
      if (this.regulatorRegionChart) {
        this.regulatorRegionChart.resize();
      }
    },
    switchRegulatorView(view) {
      this.currentView = view;
      if (view === 'dashboard') {
        this.$nextTick(() => {
          this.queueRenderRegulatorChart();
        });
      }
      if (view === 'bindApproval') {
        this.fetchBindApprovalList();
      }
    },
    startChartAutoRefresh() {
      this.stopChartAutoRefresh();
      if (this.userType !== 'enterprise' || this.farmerView !== 'monitor' || this.trendRange !== '24h') {
        return;
      }
      this.chartRefreshTimer = setInterval(() => {
        this.chartRefreshTick += 1;
        this.renderWaterTrendChart();
      }, 3500);
    },
    stopChartAutoRefresh() {
      if (this.chartRefreshTimer) {
        clearInterval(this.chartRefreshTimer);
        this.chartRefreshTimer = null;
      }
    },
    renderWaterTrendChart() {
      if (this.userType !== 'enterprise' || this.farmerView !== 'monitor') {
        return;
      }
      const container = this.$refs.waterTrendChartRef;
      if (!container) {
        return;
      }
      if (!container.clientWidth || !container.clientHeight) {
        this.queueRenderWaterTrendChart(160);
        return;
      }

      this.waterTrendChart = echarts.getInstanceByDom(container) || echarts.init(container);

      const trendData = this.monitorTrendData;
      this.waterTrendChart.setOption({
        animationDuration: 900,
        animationEasing: 'cubicOut',
        color: ['#1f74d8', '#00a870'],
        tooltip: { trigger: 'axis' },
        legend: {
          right: 14,
          top: 4,
          itemWidth: 18,
          itemGap: 18,
          textStyle: { color: '#4f6780', padding: [0, 4, 0, 4] },
        },
        grid: { left: 48, right: 60, top: 56, bottom: 28 },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: trendData.xAxis,
          axisLine: { lineStyle: { color: '#d6e4f0' } },
          axisLabel: { color: '#6a7f95' },
        },
        yAxis: [
          {
            type: 'value',
            name: '溶解氧',
            min: 3.5,
            max: 6.2,
            axisLine: { show: false },
            splitLine: { lineStyle: { color: '#edf3f9' } },
            axisLabel: { color: '#6a7f95', formatter: '{value}' },
          },
          {
            type: 'value',
            name: 'pH',
            position: 'right',
            min: 7.8,
            max: 8.3,
            splitLine: { show: false },
            axisLabel: { color: '#6a7f95', formatter: '{value}' },
          },
        ],
        series: [
          {
            name: '溶解氧',
            type: 'line',
            smooth: 0.16,
            yAxisIndex: 0,
            symbol: 'circle',
            symbolSize: 7,
            lineStyle: { width: 3 },
            data: trendData.doValues,
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(31, 116, 216, 0.28)' },
                { offset: 1, color: 'rgba(31, 116, 216, 0.03)' },
              ]),
            },
            markLine: {
              symbol: 'none',
              label: { color: '#1f74d8' },
              lineStyle: { type: 'dashed', color: '#1f74d8' },
              data: [{ yAxis: 5, name: 'DO警戒线' }],
            },
          },
          {
            name: 'pH',
            type: 'line',
            smooth: 0.12,
            yAxisIndex: 1,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: { width: 3 },
            data: trendData.phValues,
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(0, 168, 112, 0.20)' },
                { offset: 1, color: 'rgba(0, 168, 112, 0.03)' },
              ]),
            },
          },
        ],
      }, true);
      this.waterTrendChart.resize();
    },
    renderRegulatorChart() {
      if (this.userType !== 'monitor' || this.currentView !== 'dashboard') {
        return;
      }
      const container = this.$refs.regulatorRegionChartRef;
      if (!container) {
        return;
      }
      if (!container.clientWidth || !container.clientHeight) {
        this.queueRenderRegulatorChart(160);
        return;
      }

      this.regulatorRegionChart = echarts.getInstanceByDom(container) || echarts.init(container);
      const chartData = this.regulatorRegionChartData;

      this.regulatorRegionChart.setOption({
        animationDuration: 900,
        animationEasing: 'cubicOut',
        color: ['#1f74d8', '#ff9f43'],
        tooltip: { trigger: 'axis' },
        legend: {
          right: 14,
          top: 4,
          itemGap: 18,
          textStyle: { color: '#4f6780' },
        },
        grid: { left: 44, right: 50, top: 52, bottom: 28 },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: chartData.xAxis,
          axisLine: { lineStyle: { color: '#d6e4f0' } },
          axisLabel: { color: '#6a7f95' },
        },
        yAxis: [
          {
            type: 'value',
            name: '综合评分',
            min: 60,
            max: 100,
            splitLine: { lineStyle: { color: '#edf3f9' } },
            axisLabel: { color: '#6a7f95' },
          },
          {
            type: 'value',
            name: '预警数',
            position: 'right',
            minInterval: 1,
            splitLine: { show: false },
            axisLabel: { color: '#6a7f95' },
          },
        ],
        series: [
          {
            name: '综合评分',
            type: 'line',
            smooth: 0.2,
            yAxisIndex: 0,
            symbol: 'circle',
            symbolSize: 7,
            lineStyle: { width: 3 },
            data: chartData.scoreValues,
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(31, 116, 216, 0.20)' },
                { offset: 1, color: 'rgba(31, 116, 216, 0.03)' },
              ]),
            },
          },
          {
            name: '预警数量',
            type: 'line',
            smooth: 0.1,
            yAxisIndex: 1,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: { width: 3, type: 'dashed' },
            data: chartData.warningValues,
          },
        ],
      }, true);
      this.regulatorRegionChart.resize();
    },
    formatNow() {
      return new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-');
    },
    normalizeImageUrl(url) {
      if (!url) {
        return '';
      }
      if (String(url).startsWith('http') || String(url).startsWith('data:')) {
        return url;
      }
      return `${this.$API_BASE_URL}${url}`;
    },
    enrichWaterRows(rows, type = 'turbidity') {
    },
    enrichWaterRows(rows, type = 'turbidity') {
      return (rows || []).map((row, index) => {
        const fallbackProfile = this.regulatorFarmerProfiles[index % this.regulatorFarmerProfiles.length] || {};
        const profile = this.regulatorFarmerProfiles.find((item) => item.accountId === row.userId || item.companyName === row.companyName) || fallbackProfile;
        const snapshot = this.regulatorPondSnapshots.find((item) => item.companyName === profile.companyName) || {};
        const valueText = String(row.data ?? row.value ?? '--');
        const numericValue = Number(String(valueText).replace(/[^\d.\-]/g, ''));
        const derivedStatus = type === 'turbidity'
          ? (numericValue <= 20 ? '正常' : '异常')
          : (numericValue >= 30000 && numericValue <= 38000 ? '正常' : '异常');

        return {
          ...row,
          userId: row.userId || '',
          companyName: row.companyName || '',
          regionCode: row.regionCode || '',
          region: row.region || '',
          pondName: row.pondName || '',
          data: valueText,
          time: row.time || row.createTime || '',
          status: row.status || derivedStatus,
          isOnChain: Boolean(row.isOnChain),
        };
      });
    },
    getMetricSuggestion(metricCode, level) {
      if (level === 'danger') {
        return '已进入危险区间，建议立即排查并采取处置措施。';
      }
      if (level === 'warning') {
        if (metricCode === 'DO') return '建议及时开启增氧设备，并在夜间加强巡检。';
        if (metricCode === 'NITRITE') return '建议局部换水并复测，防止继续上升。';
        if (metricCode === 'AMMONIA_N') return '建议控制投喂量并检查底质。';
        return '建议尽快复测，并结合现场情况处理。';
      }
      return '指标处于正常范围，可继续保持当前养殖管理。';
    },
    showPondAddTip() {
      this.pondAddDialogVisible = true;
      this.resetPondAddForm();
    },
    resetPondAddForm() {
      this.pondAddForm = {
        pondName: '',
        breedType: '',
        area: null,
        depth: null,
        startDate: '',
        endDate: '',
        status: 1,
      };
    },
    async handleCreatePond() {
      const { pondName, breedType, area, depth, startDate, endDate, status } = this.pondAddForm;
      if (!pondName || !breedType || area == null || depth == null || !startDate || !endDate) {
        this.$message.warning('请填写完整信息');
        return;
      }
      this.pondAddSubmitting = true;
      try {
        const formatDate = (date) => {
          if (!date) return null;
          const d = date instanceof Date ? date : new Date(date);
          const year = d.getFullYear();
          const month = String(d.getMonth() + 1).padStart(2, '0');
          const day = String(d.getDate()).padStart(2, '0');
          return `${year}-${month}-${day}`;
        };
        const res = await createPond({
          pondName,
          breedType,
          area,
          depth,
          startDate: formatDate(startDate),
          endDate: formatDate(endDate),
          status,
        });
        if (res.code === 0) {
          this.$message.success('养殖池创建成功');
          this.pondAddDialogVisible = false;
          await this.fetchPondList();
        } else {
          this.$message.error(res.message || '创建失败');
        }
      } catch (error) {
        console.error('创建养殖池失败:', error);
        this.$message.error('创建失败');
      } finally {
        this.pondAddSubmitting = false;
      }
    },
    showManualEntryDialog() {
      this.manualEntryDialogVisible = true;
    },
    resetManualEntryForm() {
      this.manualEntryDialogVisible = false;
      this.manualEntryForm = {
        metricCode: 'DO',
        value: '',
        remark: '',
      };
    },
    handleManualEntrySubmit() {
      const { metricCode, value, remark } = this.manualEntryForm;
      const rule = WATER_DATA_TYPES.find((item) => item.metricCode === metricCode);
      const numericValue = Number(value);

      if (!rule) {
        this.$message.warning('请选择指标类型');
        return;
      }
      if (!Number.isFinite(numericValue)) {
        this.$message.warning('请输入有效的检测值');
        return;
      }

      const level = getWaterMetricLevel(metricCode, numericValue);
      const levelText = this.formatMetricLevel(level);
      const suggestion = remark || this.getMetricSuggestion(metricCode, level);

      this.monitorRecords.unshift({
        id: Date.now(),
        pondId: this.activePondId,
        pondName: this.activePondLabel,
        time: this.formatNow(),
        metricCode,
        metricLabel: rule.label,
        valueText: `${numericValue} ${rule.unit}`.trim(),
        sourceText: '人工补录',
        levelText,
        theme: METRIC_LEVEL_THEME[level] || 'primary',
        suggestion,
      });

      const targetMetric = this.farmerMetrics.find((item) => item.metricCode === metricCode);
      if (targetMetric) {
        targetMetric.value = numericValue;
        targetMetric.tip = suggestion;
      }

      if (level !== 'normal') {
        this.warningTodoList.unshift({
          id: Date.now() + 1,
          pondId: this.activePondId,
          pondName: this.activePondLabel,
          title: `${rule.label}人工补录触发${levelText}`,
          type: 'water_quality',
          level,
          levelText,
          theme: METRIC_LEVEL_THEME[level] || 'warning',
          time: '刚刚',
          detail: `${rule.label}检测值为 ${numericValue}${rule.unit}，${suggestion}`,
        });
      }

      this.traceabilityTimeline.unshift({
        id: Date.now() + 2,
        pondId: this.activePondId,
        pondName: this.activePondLabel,
        title: `${rule.label}补录记录已生成存证任务`,
        time: '刚刚',
        evidenceNo: `BC-MANUAL-${Date.now()}`,
        onChain: level === 'normal',
      });

      this.$message.success(`已补录 ${rule.label} 数据，并完成阈值判定`);
      this.resetManualEntryForm();
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

        if (response.code === 0 && Array.isArray(response.data) && response.data.length) {
          const list = response.data.map((item, index) => ({
            ...item,
            id: item.id || index + 1,
            companyName: item.companyName || '',
            status: Number(item.status ?? 0),
            permitImage: this.normalizeImageUrl(item.imageUrl || item.permitImage),
          }));
          if (this.userType === 'enterprise') {
            this.enterpriseTableData = list;
          } else {
            this.monitorTableData = list;
          }
          return;
        }

        this.enterpriseTableData = [];
        this.monitorTableData = [];
      } catch (error) {
        console.error('获取许可证列表失败:', error);
        this.enterpriseTableData = [];
        this.monitorTableData = [];
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
        console.error('approve failed', error);
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
        console.error('reject failed', error);
      }
    },
    showBindDialog() {
      this.bindDialogVisible = true;
      this.fetchManagerList();
    },
    async fetchManagerList() {
      try {
        const res = await getManagerList();
        if (res.code === 0 && res.data) {
          this.managerList = res.data;
        }
      } catch (error) {
        console.error('获取监管局列表失败:', error);
        this.$message.error('获取监管局列表失败');
      }
    },
    async fetchBindStatus() {
      try {
        const res = await getBindStatus();
        if (res.code === 0 && res.data) {
          this.bindStatus = res.data;
        } else {
          this.bindStatus = null;
        }
      } catch (error) {
        console.error('获取绑定状态失败:', error);
        this.bindStatus = null;
      }
    },
    async handleApplyBind() {
      if (!this.selectedManagerId) {
        this.$message.warning('请选择要绑定的监管局');
        return;
      }
      this.bindSubmitting = true;
      try {
        const res = await applyBind({ managerId: this.selectedManagerId });
        if (res.code === 0) {
          this.$message.success('申请已提交，请等待监管局审批');
          this.bindDialogVisible = false;
          this.selectedManagerId = '';
          await this.fetchBindStatus();
        } else {
          this.$message.error(res.message || '申请失败');
        }
      } catch (error) {
        console.error('申请绑定失败:', error);
        this.$message.error('申请失败');
      } finally {
        this.bindSubmitting = false;
      }
    },
    async handleUnbind() {
      try {
        const res = await unbind();
        if (res.code === 0) {
          this.$message.success('已解除绑定');
          this.bindStatus = null;
        } else {
          this.$message.error(res.message || '解绑失败');
        }
      } catch (error) {
        console.error('解绑失败:', error);
        this.$message.error('解绑失败');
      }
    },
    async fetchBindApprovalList() {
      try {
        const res = await getManagerBindList();
        if (res.code === 0 && res.data) {
          this.bindApprovalList = res.data.map((item) => ({
            ...item,
            farmerName: item.farmerName || item.farmerAccount || '--',
            farmerAccount: item.farmerAccount || '--',
            applyTime: item.applyTime || item.createTime || '--',
          }));
        } else {
          this.bindApprovalList = [];
        }
      } catch (error) {
        console.error('获取绑定申请列表失败:', error);
        this.bindApprovalList = [];
      }
    },
    async handleApproveBind(row) {
      try {
        const res = await managerApproveBind(row.id);
        if (res.code === 0) {
          this.$message.success('已通过绑定申请');
          await this.fetchBindApprovalList();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        console.error('通过绑定失败:', error);
        this.$message.error('操作失败');
      }
    },
    async handleRejectBind(row) {
      try {
        const res = await managerRejectBind(row.id);
        if (res.code === 0) {
          this.$message.warning('已拒绝绑定申请');
          await this.fetchBindApprovalList();
        } else {
          this.$message.error(res.message || '操作失败');
        }
      } catch (error) {
        console.error('拒绝绑定失败:', error);
        this.$message.error('操作失败');
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
      this.selectedTurbidityRowKeys = selectedRows.selectedRowData.filter((item) => item && !item.isOnChain).map((item) => item.id);
    },
    onTdsSelectChange(selectedRowKeys, selectedRows) {
      this.selectedTdsRowKeys = selectedRows.selectedRowData.filter((item) => item && !item.isOnChain).map((item) => item.id);
    },
    setDefaultAccountInfo() {
      const platformRole = localStorage.getItem('platformUserRole') || '';
      const platformAccount = localStorage.getItem('platformUserAccount') || '';
      const platformPassword = localStorage.getItem('platformUserPassword') || '';

      this.enterpriseAccount = this.enterpriseAccount || (platformRole === 'farmers' ? platformAccount : '');
      this.enterprisePassword = this.enterprisePassword || platformPassword;
      this.enterpriseBlockchainAddress = this.enterpriseBlockchainAddress || '';

      this.monitorAccount = this.monitorAccount || (platformRole === 'manager' ? platformAccount : '');
      this.monitorPassword = this.monitorPassword || platformPassword;
      this.monitorBlockchainAddress = this.monitorBlockchainAddress || '';
    },
    async getAccountInfo() {
      try {
        const userInfo = this.$store.state.user?.userInfo || {};
        const accountAddress = userInfo.accountAddress || localStorage.getItem('platformAccountAddress') || '';
        if (accountAddress) {
          if (this.userType === 'enterprise') {
            this.enterpriseBlockchainAddress = accountAddress;
          } else {
            this.monitorBlockchainAddress = accountAddress;
          }
        }
      } catch (error) {
        console.warn('获取账号信息失败:', error?.message || error);
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
        if (response.code === 0 && Array.isArray(response.data) && response.data.length) {
          this.turbidityTableData = this.enrichWaterRows(response.data, 'turbidity');
          this.turbidityPagination.total = Number(response.total || response.data.length);
          return;
        }
        this.turbidityTableData = [];
        this.turbidityPagination.total = 0;
      } catch (error) {
        console.error('获取浑浊度数据失败:', error);
        this.turbidityTableData = [];
        this.turbidityPagination.total = 0;
      }
    },
    async fetchTdsData() {
      try {
        const response = await waterInfoQuery({
          DataType: 0,
          pageNum: this.tdsPagination.current,
          pageSize: this.tdsPagination.pageSize,
        });
        if (response.code === 0 && Array.isArray(response.data) && response.data.length) {
          this.tdsTableData = this.enrichWaterRows(response.data, 'tds');
          this.tdsPagination.total = Number(response.total || response.data.length);
          return;
        }
        this.tdsTableData = [];
        this.tdsPagination.total = 0;
      } catch (error) {
        console.error('获取TDS数据失败:', error);
        this.tdsTableData = [];
        this.tdsPagination.total = 0;
      }
    },
    isRowDisabled(row) {
      return row.isOnChain === true;
    },
    async fetchPondList() {
      try {
        const res = this.userType === 'monitor' ? await getManagerPondList({ pageNum: 1, pageSize: 100 }) : await getPondList();
        if (res.code === 0 && res.data) {
          // 养殖户接口返回 List<Pond>，监管局接口返回 PageResponse<Pond>
          let list;
          if (this.userType === 'monitor') {
            // 监管局：PageResponse，data 字段包含列表
            const pageData = res.data;
            list = Array.isArray(pageData.data) ? pageData.data : (Array.isArray(pageData) ? pageData : []);
          } else {
            // 养殖户：直接返回列表
            list = Array.isArray(res.data) ? res.data : [];
          }
          if (list.length > 0) {
            this.pondOptions = list.map(item => ({
              value: String(item.id),
              label: item.pondName || item.name || `养殖池${item.id}`,
              species: item.breedType || item.species || item.fishType || '',
              status: item.auditStatus !== undefined ? (item.auditStatus === 1 ? 'APPROVED' : (item.auditStatus === 2 ? 'REJECTED' : 'PENDING')) : (item.status || 'PENDING'),
              pondStatus: item.status,
              farmerId: item.farmerId ? String(item.farmerId) : null,
              farmerName: item.farmerName || item.userName || '',
              farmName: item.farmName || '',
            }));
            this.activePondId = this.pondOptions[0]?.value || 'pond-1';
            // 为每个养殖池初始化默认水质指标数据（占位显示）
            list.forEach(item => {
              const pondId = String(item.id);
              if (!this.pondMetricsMap[pondId]) {
                this.pondMetricsMap[pondId] = [
                  { metricCode: 'WATER_TEMP', value: item.waterTemp || '--', tip: '暂无数据' },
                  { metricCode: 'SALINITY', value: item.salinity || '--', tip: '暂无数据' },
                  { metricCode: 'PH', value: item.ph || '--', tip: '暂无数据' },
                  { metricCode: 'DO', value: item.doValue || '--', tip: '暂无数据' },
                  { metricCode: 'NH3N', value: item.nh3n || '--', tip: '暂无数据' },
                  { metricCode: 'NO2', value: item.no2 || '--', tip: '暂无数据' },
                ];
              }
            });
          }
        }
      } catch (error) {
        console.warn('加载养殖池列表失败，使用默认数据:', error);
      }
    },
    async fetchDashboardData() {
      try {
        const res = this.userType === 'monitor' ? await getManagerDashboard() : await getFarmerDashboard();
        if (res.code === 0 && res.data) {
          const data = res.data;
          // 更新看板统计数据
          if (this.userType === 'monitor') {
            // 监管端数据
            if (data.pondList) {
              this.regulatorPondSnapshots = data.pondList.map((item, index) => ({
                id: item.id || index + 1,
                regionCode: item.regionCode || '',
                region: item.region || '',
                companyName: item.farmerName || item.companyName || '',
                pondName: item.pondName || item.name || '',
                doValue: item.doValue || 0,
                phValue: item.phValue || 0,
                salinity: item.salinity || 0,
                score: item.score || 0,
                warningCount: item.warningCount || 0,
                statusText: item.status === 'APPROVED' ? '运行稳定' : '待审核',
                theme: item.status === 'APPROVED' ? 'success' : 'warning',
                lastUpload: item.lastUpload || '--',
                note: item.note || '',
              }));
            }
            if (data.farmerList) {
              this.regulatorFarmerProfiles = data.farmerList.map((item, index) => ({
                id: item.id || index + 1,
                accountId: item.userAccount || '',
                regionCode: item.regionCode || '',
                region: item.region || '',
                companyName: item.userName || item.companyName || '',
                pondCount: item.pondCount || 0,
                latestUpload: item.latestUpload || '--',
                licenseStatusText: item.licenseStatus === 'APPROVED' ? '许可证有效' : '待审批',
                licenseTheme: item.licenseStatus === 'APPROVED' ? 'success' : 'warning',
                chainRate: item.chainRate ? `${item.chainRate}%` : '--',
                riskText: item.riskLevel || '',
                riskTheme: item.riskLevel === '高风险' ? 'danger' : (item.riskLevel === '中风险' ? 'warning' : 'success'),
              }));
            }
            if (data.warningList) {
              this.regulatorWarningItems = data.warningList.map((item, index) => ({
                id: item.id || index + 1,
                regionCode: item.regionCode || '',
                region: item.region || '',
                companyName: item.farmerName || '',
                pondName: item.pondName || '',
                title: item.title || '',
                levelText: item.level || '',
                theme: item.level === '高风险' ? 'danger' : 'warning',
                time: item.time || '--',
                progress: item.progress || '',
                detail: item.detail || '',
              }));
            }
          } else {
            // 养殖户数据
            if (data.metrics) {
              // 更新水质指标
              Object.keys(data.metrics).forEach(pondId => {
                if (this.pondMetricsMap[pondId]) {
                  const metrics = data.metrics[pondId];
                  this.pondMetricsMap[pondId] = metrics.map(m => ({
                    metricCode: m.metricCode || m.code,
                    value: m.value,
                    tip: m.tip || '',
                  }));
                }
              });
            }
            if (data.warningList) {
              this.warningTodoList = data.warningList.map((item, index) => ({
                id: item.id || index + 1,
                pondId: String(item.pondId) || 'pond-1',
                pondName: item.pondName || '',
                title: item.title || '',
                type: item.type || 'water_quality',
                level: item.level || 'warning',
                levelText: item.levelText || '',
                theme: item.level === 'danger' ? 'danger' : 'warning',
                time: item.time || '--',
                detail: item.detail || '',
              }));
            }
          }
        }
      } catch (error) {
        console.warn('加载看板数据失败:', error);
      }
    },
    async fetchFarmingProcessData() {
      try {
        const pondId = this.activePondId !== 'all' ? { pondId: this.activePondId, pageNum: 1, pageSize: 100 } : { pageNum: 1, pageSize: 100 };

        // 获取养殖过程数据用于存证时间线
        const [seedRes, feedRes, medicineRes, harvestRes] = await Promise.all([
          this.userType === 'monitor' ? getManagerSeedList(pondId) : getSeedList(pondId),
          this.userType === 'monitor' ? getManagerFeedList(pondId) : getFeedList(pondId),
          this.userType === 'monitor' ? getManagerMedicineList(pondId) : getMedicineList(pondId),
          this.userType === 'monitor' ? getManagerHarvestList(pondId) : getHarvestList(pondId),
        ]);

        // 构建存证时间线
        const timeline = [];

        // 解析列表数据的辅助函数
        const parseListData = (res, isMonitor) => {
          if (!res || res.code !== 0 || !res.data) return [];
          // 养殖户接口直接返回 PageResponse，监管局接口返回 BaseResponse<PageResponse>
          const pageData = isMonitor ? res.data : res;
          if (Array.isArray(pageData.data)) {
            return pageData.data;
          }
          return Array.isArray(pageData) ? pageData : [];
        };

        const seedList = parseListData(seedRes, this.userType === 'monitor');
        seedList.forEach(item => {
          timeline.push({
            id: `seed-${item.id}`,
            pondId: String(item.pondId || ''),
            pondName: item.pondName || this.pondOptions.find(p => p.value === String(item.pondId))?.label || '',
            title: `苗种投放: ${item.seedType || ''}`,
            time: item.recordDate || item.createTime || '',
            evidenceNo: `BC-SEED-${item.id}`,
            isOnChain: item.auditStatus === 1,
            ownerName: item.farmerName || item.manager || '',
            contentTitle: '苗种投放记录',
            chainTime: item.chainTime || item.updateTime || '',
            hash: item.txHash || item.chainTxHash || '',
            blockHeight: item.blockNumber || '--',
            description: `投放${item.weight || ''}kg`,
            verifyText: item.auditStatus === 1 ? '区块链存证验证通过' : '待上链确认',
          });
        });

        const feedList = parseListData(feedRes, this.userType === 'monitor');
        feedList.forEach(item => {
          timeline.push({
            id: `feed-${item.id}`,
            pondId: String(item.pondId || ''),
            pondName: item.pondName || '',
            title: `投喂记录: ${item.feedBrand || ''}`,
            time: item.feedDate || item.createTime || '',
            evidenceNo: `BC-FEED-${item.id}`,
            isOnChain: item.auditStatus === 1,
            ownerName: item.farmerName || item.manager || '',
            contentTitle: '投喂记录',
            chainTime: item.chainTime || item.updateTime || '',
            hash: item.txHash || item.chainTxHash || '',
            blockHeight: item.blockNumber || '--',
            description: `投喂${item.feedAmount || ''}kg`,
            verifyText: item.auditStatus === 1 ? '区块链存证验证通过' : '待上链确认',
          });
        });

        const medicineList = parseListData(medicineRes, this.userType === 'monitor');
        medicineList.forEach(item => {
          timeline.push({
            id: `medicine-${item.id}`,
            pondId: String(item.pondId || ''),
            pondName: item.pondName || '',
            title: `用药记录: ${item.medicineName || ''}`,
            time: item.medicineDate || item.createTime || '',
            evidenceNo: `BC-MED-${item.id}`,
            isOnChain: item.auditStatus === 1,
            ownerName: item.farmerName || item.manager || '',
            contentTitle: '用药记录',
            chainTime: item.chainTime || item.updateTime || '',
            hash: item.txHash || item.chainTxHash || '',
            blockHeight: item.blockNumber || '--',
            description: `${item.medicineName || ''} ${item.dosage || ''}`,
            verifyText: item.auditStatus === 1 ? '区块链存证验证通过' : '待上链确认',
          });
        });

        const harvestList = parseListData(harvestRes, this.userType === 'monitor');
        harvestList.forEach(item => {
          timeline.push({
            id: `harvest-${item.id}`,
            pondId: String(item.pondId || ''),
            pondName: item.pondName || '',
            title: `收获记录: ${item.batchNo || ''}`,
            time: item.harvestDate || item.createTime || '',
            evidenceNo: `BC-HARVEST-${item.id}`,
            isOnChain: item.auditStatus === 1,
            ownerName: item.farmerName || item.manager || '',
            contentTitle: '收获记录',
            chainTime: item.chainTime || item.updateTime || '',
            hash: item.txHash || item.chainTxHash || '',
            blockHeight: item.blockNumber || '--',
            description: `收获${item.totalWeight || ''}kg，去向${item.buyerInfo || ''}`,
            verifyText: item.auditStatus === 1 ? '区块链存证验证通过' : '待上链确认',
          });
        });

        // 按时间排序
        timeline.sort((a, b) => String(b.time).localeCompare(String(a.time)));

        if (timeline.length > 0) {
          this.traceabilityTimeline = timeline;
        }
      } catch (error) {
        console.warn('加载养殖过程数据失败:', error);
      }
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
  align-items: stretch;
}

.summary-row > .t-col,
.content-row > .t-col {
  display: flex;
}

.summary-card {
  width: 100%;
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

.pond-switcher {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;

  &__label {
    font-size: 13px;
    color: #5e738a;
    font-weight: 600;
  }
}

.pond-add-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 12px;
  height: 32px;
  border-radius: 999px;
  border: 1px dashed #7fb3ff;
  background: rgba(31, 116, 216, 0.06);
  color: #1f74d8;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(31, 116, 216, 0.12);
    border-color: #4b95ff;
  }

  &__icon {
    font-size: 16px;
    font-weight: 700;
    line-height: 1;
  }
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.metric-card-mini {
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid var(--marine-divider);
  background: linear-gradient(180deg, #f8fbff 0%, #eff7ff 100%);

  &__top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    color: #4f6780;
    font-size: 13px;
  }

  &__value {
    margin: 10px 0 8px;
    font-size: 28px;
    font-weight: 700;
    color: #12314d;

    small {
      margin-left: 4px;
      font-size: 13px;
      font-weight: 500;
      color: #5e738a;
    }
  }

  &__meta {
    font-size: 12px;
    color: #5e738a;
  }

  &__desc {
    margin-top: 8px;
    font-size: 12px;
    line-height: 1.7;
    color: #6a7f95;
  }
}

.farmer-section-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.farmer-subpanel {
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid var(--marine-divider);
  background: #fbfdff;

  &__title {
    margin-bottom: 10px;
    font-size: 15px;
    font-weight: 600;
    color: #12314d;
  }
}

.warning-item,
.ledger-item,
.trace-item,
.threshold-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--marine-divider);

  &:last-child {
    border-bottom: 0;
    padding-bottom: 0;
  }
}

.warning-item__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  color: #12314d;
  font-weight: 600;
}

.warning-item__time {
  margin-top: 4px;
  font-size: 12px;
  color: #6a7f95;
}

.warning-item__desc {
  margin-top: 6px;
  line-height: 1.7;
  color: #4f6780;
}

.ledger-item__title,
.threshold-item__title {
  font-size: 14px;
  font-weight: 600;
  color: #12314d;
}

.threshold-list {
  display: grid;
}

.threshold-item__range {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.6;
  color: #5e738a;

  &.danger {
    color: #c64751;
  }
}

.trace-list {
  margin-bottom: 12px;
  padding: 0 2px;
}

.trace-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.trace-item__content {
  flex: 1;
  min-width: 0;
}

.trace-item__aside {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
}


.traceability-layout {
  display: grid;
  grid-template-columns: minmax(260px, 0.9fr) minmax(0, 1.3fr);
  gap: 16px;
}

.traceability-records-panel {
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid var(--marine-divider);
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
}

.traceability-record-list {
  display: grid;
  gap: 10px;
  margin-top: 12px;
}

.traceability-record-card {
  width: 100%;
  text-align: left;
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid var(--marine-divider);
  background: #fff;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: rgba(31, 116, 216, 0.28);
    box-shadow: 0 10px 24px rgba(31, 116, 216, 0.08);
  }

  &.active {
    border-color: #6ba3f5;
    background: linear-gradient(180deg, rgba(239, 247, 255, 0.96) 0%, rgba(255, 255, 255, 1) 100%);
    box-shadow: 0 12px 28px rgba(31, 116, 216, 0.10);
  }

  &__head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 8px;
    color: #12314d;
    font-weight: 600;
  }

  &__meta,
  &__hash {
    margin-top: 6px;
    font-size: 12px;
    color: #6a7f95;
  }

  &__hash {
    font-family: 'Consolas', 'Courier New', monospace;
    color: #1d4f79;
  }

}

.traceability-certificate-panel {
  padding: 18px;
  border-radius: 18px;
  border: 1px solid rgba(31, 116, 216, 0.24);
  background: linear-gradient(180deg, rgba(248, 251, 255, 0.98) 0%, rgba(255, 255, 255, 1) 100%);
  box-shadow: 0 14px 32px rgba(31, 116, 216, 0.08);
}

.certificate-head {
  text-align: center;
  margin-bottom: 16px;
}

.certificate-emblem {
  width: 64px;
  height: 64px;
  margin: 0 auto 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: 2px solid #82aef0;
  color: #6b9be6;
  font-size: 28px;
  background: rgba(31, 116, 216, 0.08);
}

.certificate-title {
  font-size: 28px;
  font-weight: 700;
  color: #6b9be6;
}

.certificate-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #7c90a6;
}

.certificate-sheet {
  overflow: hidden;
  border-radius: 16px;
  border: 1px solid var(--marine-divider);
  background: rgba(255, 255, 255, 0.96);
}

.certificate-row {
  display: grid;
  grid-template-columns: 150px 1fr;
  border-bottom: 1px solid var(--marine-divider);

  &:last-child {
    border-bottom: 0;
  }
}

.certificate-label {
  padding: 14px 16px;
  background: rgba(238, 247, 255, 0.9);
  color: #425b77;
  font-weight: 600;
}

.certificate-value {
  padding: 14px 16px;
  color: #12314d;
  line-height: 1.75;

  &.hash {
    font-family: 'Consolas', 'Courier New', monospace;
    color: #4f6780;
    word-break: break-all;
  }
}


.certificate-verify-bar {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 54px;
  border-radius: 14px;
  background: rgba(0, 168, 112, 0.12);
  color: #158a58;
  font-size: 18px;
  font-weight: 600;

  &.pending {
    background: rgba(237, 166, 28, 0.14);
    color: #b47416;
  }

  &__icon {
    font-size: 20px;
    font-weight: 700;
  }
}

.chain-panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.chain-hash-text {
  font-family: 'Consolas', 'Courier New', monospace;
  color: #1d4f79;
}

.monitor-toolbar {
  align-items: center;
}

.monitor-chart-panel {
  margin-bottom: 16px;
  padding: 16px 18px 12px;
  border-radius: 16px;
  border: 1px solid var(--marine-divider);
  background: linear-gradient(180deg, rgba(248, 251, 255, 0.98) 0%, rgba(255, 255, 255, 1) 100%);

  &__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 6px;
  }
}

.monitor-chart-legend-tip {
  margin-bottom: 12px;
  font-size: 12px;
  color: #6a7f95;
}

.water-trend-chart {
  width: 100%;
  height: 320px;
}

.trend-summary-grid,
.warning-center-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.trend-summary-item,
.warning-center-card {
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid var(--marine-divider);
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
}

.regulator-insight-grid,
.regulator-pond-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.regulator-insight-grid {
  margin-bottom: 16px;
}

.regulator-overview-card,
.regulator-pond-card {
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid var(--marine-divider);
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
}

.regulator-pond-card__metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;

  span {
    padding: 4px 8px;
    border-radius: 999px;
    background: rgba(31, 116, 216, 0.08);
    color: #1d4f79;
    font-size: 12px;
  }
}

.regulator-warning-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.regulator-trace-list {
  margin-top: 12px;
}

.regulator-trace-item,
.regulator-mini-trace {
  padding: 12px 0;
  border-bottom: 1px solid var(--marine-divider);

  &:last-child {
    border-bottom: 0;
    padding-bottom: 0;
  }
}

.regulator-mini-panel {
  margin-top: 14px;
}

.regulator-trend-chart {
  height: 300px;
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

.panel-tip {
  margin-bottom: 12px;
  color: #5e738a;
  font-size: 13px;

  &.compact {
    margin: 4px 0 0;
  }
}

.info-panel {
  background: linear-gradient(180deg, rgba(248, 251, 255, 0.95) 0%, rgba(255, 255, 255, 0.98) 100%);

  :deep(.t-card__body) {
    justify-content: space-between;
  }
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
  align-content: start;
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
  margin-top: auto;
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

.bind-dialog-tip {
  margin-bottom: 16px;
  color: #5e738a;
  font-size: 14px;
  line-height: 1.6;
}

.bind-current-status {
  margin-top: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #4f6780;
}

.bind-status-box {
  margin-top: 12px;
}

.bind-action-row {
  justify-content: flex-end;
  padding-top: 8px;
}

@media (max-width: 1100px) {
  .workbench-hero {
    grid-template-columns: 1fr;
    padding: 20px;
  }

  .hero-main h1 {
    font-size: 28px;
  }

  .metric-grid,
  .farmer-section-grid,
  .trend-summary-grid,
  .warning-center-grid,
  .regulator-insight-grid,
  .regulator-pond-grid,
  .regulator-warning-grid,
  .traceability-layout {
    grid-template-columns: 1fr;
  }

  .monitor-chart-panel__header,
  .trace-item,
  .chain-panel-header,
  .traceability-record-card__head {
    flex-direction: column;
    align-items: flex-start;
  }

  .certificate-row {
    grid-template-columns: 1fr;
  }

  .water-trend-chart {
    height: 260px;
  }

  .summary-row > .t-col,
  .content-row > .t-col {
    display: block;
  }
}
</style>