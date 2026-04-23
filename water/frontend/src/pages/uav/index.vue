<template>
  <div class="drone-patrol">


    <!-- 主要内容区 -->
    <div class="content">
      <!-- 左侧步骤指引 -->
      <div class="steps-panel">
        <div class="step-card">
          <t-steps layout="vertical" :current="currentStep">
            <t-step-item 
              v-for="(step, index) in steps" 
              :key="index"
              :title="step.title"
              :status="index < currentStep ? 'finish' : ''"
            >
            
              <template #content>
                <div class="step-content">
                  <!-- 步骤一的特殊处理 -->
                  <template v-if="index === 0">
                    <p>(1) 点击右侧【区块链可视化引擎】，进入合约管理/合约IDE中部署编写好的智能合约；部署时填入下方管理员区块链账户地址</p>
                    <div class="account-info">
                      <p>管理员区块链账户地址：<span class="account-value">{{ droneBlockchainAddress }}</span></p>
                    </div>
                    <p>(2) 点击【我的无人机】，进入我的无人机页面；</p>
                    <p>(3) 点击【创建】按钮，初始化测试无人机</p>
                    <p>(4) 成功创建后可以查看到测试无人机的信息。</p>
                  </template>
                  <!-- 其他步骤使用普通的 v-html -->
                  <template v-else>
                    <div v-html="step.content"></div>
                  </template>
                  
                  <div class="tdesign-demo-image-viewer__base" v-if="step.image">
                    <t-image-viewer v-model="step.visible" :images="[step.image]" :closeOnEscKeydown="false">
                      <template #trigger="{ open }">
                        <div class="tdesign-demo-image-viewer__ui-image" @click="open">
                          <img alt="test" :src="step.image" class="tdesign-demo-image-viewer__ui-image--img" />
                          <div class="tdesign-demo-image-viewer__ui-image--hover">
                            <span><browse-icon size="1.4em" /> 预览</span>
                          </div>
                        </div>
                      </template>
                    </t-image-viewer>
                  </div>
                  <div class="step-controls" v-if="currentStep === index">
                    <t-button 
                      size="small" 
                      variant="text" 
                      @click="prevStep"
                      :disabled="currentStep === 0"
                    >
                      上一步
                    </t-button>
                    <t-button 
                      size="small" 
                      variant="base" 
                      @click="nextStep"
                      :disabled="currentStep === steps.length - 1"
                    >
                      下一步
                    </t-button>
                  </div>
                </div>
              </template>
            </t-step-item>
          </t-steps>
        </div>
      </div>

      <!-- 右侧主显示区 -->
      <div class="main-view">
        <!-- 添加顶部标题栏 -->
        <div class="patrol-header">
          <div class="time">{{ currentTime }}</div>
          <div class="title">巡查区域</div>
          <div class="header-buttons">
            <t-button class="header-btn" @click="openVisualizationEngine">
              区块链可视化引擎
            </t-button>
            <t-button class="header-btn" @click="openCollectDialog">
              <template #icon>
                <t-icon name="folder" />
              </template>
              采集数据
            </t-button>
            <t-button class="header-btn" @click="openDroneDialog">
              <template #icon>
                <t-icon name="user" />
              </template>
              我的无人机
            </t-button>
            <t-button class="header-btn" @click="openResourceDialog">
              <template #icon>
                <t-icon name="table" />
              </template>
              资源列表
            </t-button>
          </div>
        </div>

        <!-- 地图容器 -->
        <div class="map-container">
          <!-- 地图标记点 -->
          <div 
            v-for="(point, index) in mapPoints" 
            :key="index"
            class="map-point"
            :style="{ left: point.x, top: point.y }"
            @click="selectPoint(index)"
          >
            <img 
              :src="selectedPoint === index ? require('@/assets/images/marker-red.png') : require('@/assets/images/marker-blue.png')"
              class="point-marker"
              alt="marker"
            />
          </div>

          <!-- 底部数据面板 -->
          <div class="data-panel" v-if="selectedPoint !== null">
            <div class="area-name">{{ mapPoints[selectedPoint].name }}</div>
            <div class="patrol-time">
              <span>人工巡检：{{ mapPoints[selectedPoint].manualTime }}</span>
              <span>机器巡检：{{ mapPoints[selectedPoint].machineTime }}</span>
            </div>
            <div class="metrics">
              <div class="metric-item">
                <img src="data:image/png;base64,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" alt="湿度图标" class="metric-icon" />
                <div class="metric-value">{{ mapPoints[selectedPoint].humidity }}%</div>
                <div class="metric-label">湿度</div>
              </div>
              <div class="metric-item">
                <div class="metric-value">{{ mapPoints[selectedPoint].temperature }}°C</div>
                <div class="metric-label">温度</div>
              </div>
              <div class="metric-item">
                <div class="metric-value">{{ mapPoints[selectedPoint].windLevel }}级</div>
                <div class="metric-label">风力</div>
              </div>
              <div class="metric-item">
                <div class="metric-value">{{ mapPoints[selectedPoint].wind }}W</div>
                <div class="metric-label">风向</div>
              </div>
              <div class="metric-item">
                <div class="metric-value">{{ mapPoints[selectedPoint].uv }}</div>
                <div class="metric-label">紫外线</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 我的无人机弹窗 -->
    <t-dialog
      :visible.sync="droneDialogVisible"
      header="我的无人机"
      :footer="false"
      width="700px"
    >
      <template v-if="droneInfo">
        <!-- 展示无人机信息 -->
        <div class="drone-info">
          <div class="drone-item"  style="display: flex;flex-direction: column;justify-content: space-between;">
            <img :src="require('@/assets/images/wurenji.png')" style="width: 100px;height: 100px;" alt="无人机" class="drone-image"/>
            <div class="drone-details">
              <div class="drone-name">{{ droneInfo.name }}</div>
              <div class="drone-battery">
                <t-progress :percentage="droneInfo.battery" :color="{ from: '#00A870', to: '#00A870' }" />
              </div>
            </div>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="no-drone-tip">暂无无人机信息</div>
      </template>
      
      <!-- 创建无人机对话框 -->
      <t-dialog
      :visible.sync="createDroneVisible"
        header="创建无人机"
        :confirm-btn="{ content: '确认', theme: 'primary' }"
        :cancel-btn="{ content: '取消' }"
        @confirm="handleCreateDrone"
        @cancel="createDroneVisible = false"
      >
        <t-input
          v-model="newDroneName"
          placeholder="请输入无人机名称"
          :maxlength="20"
        />
      </t-dialog>
      
      <!-- 底部创建按钮 -->
      <div class="dialog-footer" v-if="!droneInfo">
        <t-button @click="handleCreateDrone" theme="primary">创建无人机</t-button>
      </div>
    </t-dialog>

    <!-- 采集数据弹窗 -->
    <t-dialog
      :visible.sync="collectDialogVisible"
      header="采集数据"
      :footer="false"
      width="900px"
    >
      <div class="collect-container">
        <div class="collect-images" v-if="collectedData.length">
          <div v-for="(item, index) in collectedData" :key="index" class="image-item">
            <img :src="item.image" @click="showDetail(item)" alt="采集图片" />
            <div class="image-time">{{ item.time }}</div>
            <div class="analysis-tag">已分析</div>
          </div>
        </div>
        <div class="empty-tip" v-else>
          暂无采集数据
        </div>
        <div class="collect-controls">
          <t-button :loading="isCollecting" :disabled="isCollecting" @click="startCollect" theme="primary">
            {{ isCollecting ? '采集中...' : '开始采集' }}
          </t-button>
          <t-button v-if="isCollecting" @click="endCollect">
            结束采集
          </t-button>
        </div>
      </div>
    </t-dialog>

    <!-- 故障详情弹窗 -->
    <t-dialog
      :visible.sync="detailDialogVisible"
      header="故障分析详情"
      :footer="false"
      width="800px"
    >
      <div class="detail-container" v-if="currentDetail">
        <div class="detail-header">
          <div class="detail-icon">
            <img :src="currentDetail[0].image" alt="光伏板" />
          </div>
          <div class="detail-info">
            <div class="detail-title">{{ currentDetail[0].location }}</div>
            <div class="detail-time">
             <div>巡查时间: {{ currentDetail[0].createTime }}</div>
              <!-- <div>巡检时间: {{ currentDetail.time }}</div>  -->
            </div>
          </div>
        </div>
        <div class="detail-content">
          <div class="filter-bar">
            <t-checkbox-group style="margin-right: 0px; width: 100px;" v-model="filterOptions" @change="handleSelectAll()">
              <t-checkbox value="all">全选</t-checkbox>
            </t-checkbox-group>
            <t-select v-model="filterType" placeholder="所有">
              <t-option value="all" label="全部" />
            </t-select>
            <t-button theme="primary" variant="outline" @click="handleBatchUpload(currentDetail[0].location)">批量上链</t-button>
            <t-button theme="primary" variant="outline" @click="showContractDialog">智能合约绑定</t-button>
          </div>
          <div class="detail-list">
            <div v-for="(item, index) in currentDetail" :key="index" class="detail-item">
              <t-checkbox 
                :disabled="item.isOnChain"
                v-model="item.selected"
                @change="handleSingleSelect(item)"
              ></t-checkbox>
              <img :src="item.image" alt="故障图片" class="item-image" />
              <div class="item-info">
                <div class="item-title error">
                  {{ item.title }}
                </div>
                <div class="item-desc">问题详情：{{ item.description }}</div>
                <div class="item-location">
                  <div>光伏板位置：{{ item.location || '未知' }}</div>
                  <div>故障位置：{{ item.faultLocation }}</div>
                </div>
              </div>
              <t-button theme="primary" v-if="!item.isOnChain" @click="handleSingleUpload(item)" :disabled="item.isOnChain" variant="outline" class="upload-btn">上链</t-button>
              <t-button theme="success" v-else variant="outline" class="delete-btn">已上链</t-button>

            </div>
          </div>
        </div>
      </div>
    </t-dialog>

    <!-- 智能合约绑定弹窗 -->
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
          <t-input v-model="contractForm.contractName" placeholder="请输入合约名称" />
        </t-form-item>
        <t-form-item label="合约地址">
          <t-input v-model="contractForm.contractAddress" placeholder="请输入合约地址" />
        </t-form-item>
        <!-- <t-form-item label="合约ABI">
          <t-textarea v-model="contractForm.abi" placeholder="请输入合约ABI" />
        </t-form-item>
        <t-form-item label="合约BIN">
          <t-textarea v-model="contractForm.bin" placeholder="请输入合约BIN" />
        </t-form-item>
        <t-form-item label="区块链账户地址">
          <t-input v-model="contractForm.accountAddress" placeholder="请输入区块链账户地址" />
        </t-form-item> -->
      </t-form>
    </t-dialog>

    <!-- 资源列表弹窗 -->
    <t-dialog
      :visible.sync="resourceDialogVisible"
      header="资源列表"
      :footer="false"
      width="600px"
    >
      <div class="resource-container">
        <div class="resource-list">
          <div v-for="(resource, index) in resources" :key="index" class="resource-item">
            <div class="resource-info">
              <t-icon name="file-pdf" size="24px" style="color: #e34d59; margin-right: 12px;" />
              <div class="resource-name">{{ resource.name }}</div>
            </div>
            <t-button theme="primary" variant="outline" @click="downloadResource(resource)">
              <template #icon>
                <t-icon name="download" />
              </template>
              下载
            </t-button>
          </div>
        </div>
      </div>
    </t-dialog>
  </div>
</template>

<script>
import { 
  createUav, 
  getUavInfo, 
  getStepOneInfo, 
  createSolarPanelProblem, 
  getSolarPanelProblem, 
  upChainProblem ,
  getSolarPanelInspectionArea,
  
} from '@/api/uav/uav'
import {bindContract} from '@/api/common/common.js'
import { BrowseIcon } from 'tdesign-icons-vue';
import frontImage from '@/assets/images/front2.jpg'
import shujuImage from '@/assets/images/shuju3.jpg'
import shanglian from '@/assets/images/shanglian2.jpg'
import guangfuban1 from '@/assets/images/guangfuban1.jpg'
import guangfuban2 from '@/assets/images/guangfuban2.jpg'
import guangfuban3 from '@/assets/images/guangfuban3.jpg'
import { settings } from 'nprogress';
export default {
  components: {
      BrowseIcon,
    },
  name: 'DronePatrol',
  data() {
    
    return {
      
      currentStep: 0,
      currentTime: '',
      timer: null,
      isPatrolling: false,
      steps: [
        {
          image: frontImage,
          visible: false,
          title: '步骤1：创建我的无人机',
          content: '' // 步骤一的内容现在直接在模板中定义
        },
        {
          title: '步骤2：巡查区域选择',
          visible: false,
          content: `
            <p>(1) 查看巡检区域，了解每个巡检区域的温度，湿度、风力、风向、紫外线。
            <p>(2) 在巡检区域中找到对应巡检图标，点击查看巡检区域的信息</p>
          `
        },
        {
          image: shujuImage,
          visible: false,
          title: '步骤3：同步无人机采集的图片并进行分析',
          content: `
          <p>(1) 点击无人机模拟采集光伏板场景中【采集数据】按钮；</p>
          <p>(2) 点击【开始采集】并操作无人机采集数据；</p>
          <p>(3) 无人机结束采集后，点击【结束采集】；</p>
          <p>(4) 查看采集的数据；</p>
          <p>(5) 点击采集到的图片进入查看故障数据</p>
          `
        },
        {
          image: shanglian,
          visible: false,
          title: '步骤4：编写智能合约并绑定智能合约将数据上链',
          content: `
           <p>(1) 进入“区块链可视化引擎”界面；以自己学号为命名创建文件夹并编写智能合约, 可将已写好的合约上传至"区块链可视化引擎并编译部署"</p>
           <p>(2) 点击【智能合约绑定】按钮，将已部署好的合约信息及区块链账户地址填入，点击【确认】按钮完成智能合约绑定；</p>
           <p>(3) 查看每一条采集的数据是否符合上链的要求，若符合点击【上链】将数据存储至区块链上, 同时也可以采用批量上链的方式。</p>
           <p>(4) 上链后观察“区块链可视化引擎”块高和交易数量的变化</p>

          `
        },
      ],
      selectedPoint: null,
      mapPoints: [
        {
          x: '20%',
          y: '30%',
          name: '光伏4区',
          manualTime: '22分钟',
          machineTime: '2分钟',
          humidity: 44,
          temperature: 31,
          windLevel: 5,
          wind: 3,
          uv: '强'
        },
        {
          x: '40%',
          y: '20%',
          name: '光伏5区',
          manualTime: '25分钟',
          machineTime: '3分钟',
          humidity: 42,
          temperature: 32,
          windLevel: 4,
          wind: 2,
          uv: '中'
        },
        {
          x: '60%',
          y: '40%',
          name: '光伏6区',
          manualTime: '15分钟',
          machineTime: '6分钟',
          humidity: 42,
          temperature: 32,
          windLevel: 4,
          wind: 2,
          uv: '中'
        },
        {
          x: '80%',
          y: '60%',
          name: '光伏7区',
          manualTime: '55分钟',
          machineTime: '10分钟',
          humidity: 22,
          temperature: 12,
          windLevel: 6,
          wind: 9,
          uv: '中'
        },
        {
          x: '10%',
          y: '50%',
          name: '光伏2区',
          manualTime: '34分钟',
          machineTime: '10分钟',
          humidity: 12,
          temperature: 12,
          windLevel: 6,
          wind: 9,
          uv: '中'
        },
        {
          x: '70%',
          y: '8%',
          name: '光伏1区',
          manualTime: '25分钟',
          machineTime: '2分钟',
          humidity: 25,
          temperature: 36,
          windLevel: 5,
          wind: 3,
          uv: '强'
        },
        {
          x: '86%',
          y: '36%',
          name: '光伏8区',
          manualTime: '25分钟',
          machineTime: '2分钟',
          humidity: 36,
          temperature: 21,
          windLevel: 5,
          wind: 3,
          uv: '强'
        },
        {
          x: '8%',
          y: '16%',
          name: '光伏3区',
          manualTime: '25分钟',
          machineTime: '2分钟',
          humidity: 21,
          temperature: 11,
          windLevel: 4,
          wind: 3,
          uv: '强'
        },
        // ... 可以继续添加更多点位
      ],
      // 我的无人机
      droneDialogVisible: false,
      createDroneVisible: false,
      newDroneName: '',
      droneInfo: null,
      // 采集数据
      collectDialogVisible: false,
      detailDialogVisible: false,
      isCollecting: false,
      collectedData: [],
      currentDetail: null,
      filterOptions: [],
      filterType: 'all',

      // 智能合约弹窗
      contractDialogVisible: false,
      contractForm: {
        contractName: '',
        contractAddress: '',
        contractAbi: '',
        contractBin: '',
        accountAddress: '',
      },
      selectedIds: [], // 存储选中的ID
      // 添加账户信息

      droneBlockchainAddress: '',
      // 账户信息
      droneAccount: '',
      dronePassword: '',
      
      // 资源列表
      resourceDialogVisible: false,
      resources: [
        {
          name: 'OpenCV与机器视觉基础培训资源.pdf',
          url: '/profile/upload/OpenCV与机器视觉基础培训资源.pdf'
        },
        {
          name: '光伏产业概述与无人机巡检流程介绍.pdf',
          url: '/profile/upload/光伏产业概述与无人机巡检流程介绍.pdf'
        },
        {
          name: '开源联盟链平台与智能合约开发培训资源.pdf',
          url: '/profile/upload/开源联盟链平台与智能合约开发培训资源.pdf'
        }
      ]
    }
  },
  mounted() {
    this.updateTime()
    this.timer = setInterval(this.updateTime, 1000)
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  created() {

    this.fetchStepOneInfo();
  },
  methods: {
    goBack() {
      this.$router.go(-1)
    },
    updateTime() {
      const now = new Date()
      this.currentTime = now.toLocaleString()
    },
    togglePatrol() {
      this.isPatrolling = !this.isPatrolling;
    },
    prevStep() {
      if (this.currentStep > 0) {
        this.currentStep--;
      }
    },
    nextStep() {
      if (this.currentStep < this.steps.length - 1) {
        this.currentStep++;
      }
    },
    selectPoint(index) {
      this.selectedPoint = index;
    },
    openVisualizationEngine() {
      window.open('http://101.200.241.222:5002/WeBASE-Front', '_blank'); // 替换为实际的 URL
    },
    // 打开我的无人机弹窗
    openDroneDialog() {
      this.fetchDroneInfo();
      this.droneDialogVisible = true;
     
    },

    // 显示创建无人机对话框
    showCreateDroneDialog() {
      this.createDroneVisible = true;
    },

    // 创建无人机
    async handleCreateDrone() {
      try {
        const response = await createUav();
        if (response.code === 0) {
          this.$message.success('创建成功');
          this.createDroneVisible = false;
          await this.fetchDroneInfo();
        }
      } catch (error) {
        console.error('创建无人机失败:', error);
        this.$message.error('创建失败');
      }
    },

    // 获取无人机信息
    async fetchDroneInfo() {
      try {
        const response = await getUavInfo();
        console.log(response.data)
        if (response.data.length === 0) {
          return;
        }
        if (response.code === 0) {
          this.droneInfo = {}
          this.droneInfo.name = response.data[0].name;
          this.droneInfo.battery = 100;
          this.droneInfo.createTime = response.data[0].createTime;
        }
      } catch (error) {
        console.error('获取无人机信息失败:', error);
        this.$message.error('获取无人机信息失败');
      }
    },

    // 获取步骤一信息
    async fetchStepOneInfo() {
      try {
        const response = await getStepOneInfo();
        if (response.code === 0) {
          // 处理步骤一信息
          this.droneBlockchainAddress = response.data.address;
        }
      } catch (error) {
        console.error('获取步骤一信息失败:', error);
      }
    },

    // // 生成光伏板问题
    async generateProblem() {
      try {
        const response = await createSolarPanelProblem({
          // 添加所需参数
        });
        if (response.code === 0) {
          this.$message.success('问题生成成功');
          await this.fetchInspectionArea();
        }
      } catch (error) {
        console.error('生成问题失败:', error);
        this.$message.error('生成问题失败');
      }
    },

    // 获取巡检过区域列表
    async fetchInspectionArea() {
      try {
        const response = await getSolarPanelInspectionArea();
        if (response.code === 0) {
          const data = []
          response.data.forEach(item => {
            const obj = {}
            obj.name = item
            obj.image = this.$API_BASE_URL + '/static/test.png'
            data.push(obj)
          })
          this.collectedData = data;
        }
      } catch (error) {
     
      }
    },

    // 获取光伏板问题详情列表
    async fetchProblems(name) {
      try {
        const response = await getSolarPanelProblem(name);
        if (response.code === 0) {
 
          // 更新问题列表
          response.data.forEach(item => {
            item.image = this.$API_BASE_URL + item.image
          })
          this.currentDetail = response.data;
        }
        
      } catch (error) {
        console.error('获取问题列表失败:', error);
        this.$message.error('获取问题列表失败');
      }
    },

    // 批量上链
    async handleBatchUpload(location) {
      if (this.selectedIds.length === 0) {
        this.$message.warning('请先选择要上链的数据');
        return;
      }

      try {
        const response = await upChainProblem(this.selectedIds);
        
        if (response.code === 0) {
          this.$message.success(`${this.selectedIds.length}条数据上链成功`);
          this.selectedIds = [];
          await this.fetchProblems(location)
        }
      } catch (error) {

      }
    },

    // 单个问题上链
    async handleSingleUpload(item) {
      console.log(item)
      try {
        const response = await upChainProblem([item.id]);
        
        if (response.code === 0) {
          this.$message.success('数据上链成功');
          await this.fetchProblems(item.location);
        }
      } catch (error) {

      }
    },

    async openCollectDialog() {
      await this.fetchInspectionArea();
      this.collectDialogVisible = true;
    },
    // 开采集数据
    startCollect() {
      this.isCollecting = true;
      setTimeout(async () => {
       await this.generateProblem();
       this.isCollecting = false;
     }, 2000);
      // 模拟采集数据
      // setTimeout(() => {
      //   this.collectedData = [
      //     { image: guangfuban1, time: '2023-04-01 10:00', 
      //     issues: [
      //       {id: 1, image: guangfuban1, title: '光伏板1号故障', description: '光伏板1号故障详情',
      //        location: '光伏板1号位置', faultLocation: '故障位置'}, 
      //       {id: 2, image: guangfuban1, title: '光伏板1号故障', description: '光伏板2号故障详情', 
      //       location: '光伏板2号位置', faultLocation: '故障位置'}] 
      //     },

      //     { image: guangfuban2, time: '2023-04-01 11:00', issues: [{id: 3, image: guangfuban2, title: '光伏板2号故障', description: '光伏板2号故障详情', location: '光伏板2号位置', faultLocation: '故障位置'}] },
      //     { image: guangfuban3, time: '2023-04-01 12:00', issues: [{id: 4, image: guangfuban3, title: '光伏板3号故障', description: '光伏板3号故障详情', location: '光伏板3号位置', faultLocation: '故障位置'}] },
      //     // ... 可以继续添加更多数据
      //   ];
      //   this.isCollecting = false;
      // }, 3000);
    },

    // 结束采集数据
    endCollect() {
      // this.fetchProblems();
      this.isCollecting = false;
    },

    // 显示故障详情
    async showDetail(item) {
      const response = await getSolarPanelProblem(item.name);
      if (response.code === 0) {
        response.data.forEach((item) => {
          item.image = this.$API_BASE_URL + item.image
        })     
        this.currentDetail = response.data;
        this.detailDialogVisible = true;

      }
      
      
    },

    // 智能合约绑定
    async handleContractBind() {
      // 参数校验
      if (!this.contractForm.contractName) {
        this.$message.warning('请输入合约名称');
        return;
      }
      if (!this.contractForm.contractAddress) {
        this.$message.warning('请输入合约地址');
        return;
      }

      try {
        this.contractForm.contractType = '无人机'
        const response = await bindContract(this.contractForm);

        if (response.code === 0) {
          this.$message.success('合约绑定成功');
          this.contractDialogVisible = false;
          // 重置表单
          this.contractForm = {
            contractName: '',
            contractAddress: '',
            contractAbi: '',
            contractBin: '',
            accountAddress: ''
          };
        }
      } catch (error) {

      }
    },

    // 修改全选部分
    handleSelectAll() {
      const isAllSelected = this.filterOptions.includes('all');
      if (this.currentDetail) {
        this.currentDetail.forEach(item => {
          item.selected = isAllSelected;
          if (isAllSelected) {
            if (!this.selectedIds.includes(item.id)) {
              this.selectedIds.push(item.id);
            }
          } else {
            this.selectedIds = this.selectedIds.filter(id => id !== item.id);
          }
        });
      }
      console.log('当前选中的ID:', this.selectedIds);
    },

    // 处理单个选择
    handleSingleSelect(item) {
      if (item.selected) {
        if (!this.selectedIds.includes(item.id)) {
          this.selectedIds.push(item.id);
        }
      } else {
        this.selectedIds = this.selectedIds.filter(id => id !== item.id);
        if (this.filterOptions.includes('all')) {
          this.filterOptions = this.filterOptions.filter(option => option !== 'all');
        }
      }
      console.log('当前选中的ID:', this.selectedIds);
    },

    // 显示智能合约绑定弹窗
    showContractDialog() {
      this.contractDialogVisible = true;
    },

    // 打开资源列表弹窗
    openResourceDialog() {
      this.resourceDialogVisible = true;
    },

    // 下载资源
    downloadResource(resource) {
      const downloadUrl = this.$API_BASE_URL + resource.url;
      window.open(downloadUrl, '_blank');
    },
  },
  computed: {
    stepControlsStyle() {
      // 根据当前步骤计算位置
      const baseTop = 100; // 基础顶部距离
      const stepHeight = 200; // 每个步骤的大致高度
      const top = baseTop + (this.currentStep * stepHeight);
      return {
        position: 'absolute',
        left: '50%',
        transform: 'translateX(-50%)',
        top: `${top}px`,
        zIndex: 10
      }
    }
  }
}
</script>

<style lang="less" scoped>

.tdesign-demo-image-viewer__ui-image {
  width: 350px;
  height: 160px;
  display: inline-flex;
  position: relative;
  justify-content: center;
  align-items: center;
  border-radius: var(--td-radius-small);
  overflow: hidden;
}

.tdesign-demo-image-viewer__ui-image--hover {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  left: 0;
  top: 0;
  opacity: 0;
  background-color: rgba(0, 0, 0, 0.6);
  color: var(--td-text-color-anti);
  line-height: 22px;
  transition: 0.2s;
}

.tdesign-demo-image-viewer__ui-image:hover .tdesign-demo-image-viewer__ui-image--hover {
  opacity: 1;
  cursor: pointer;
}

.tdesign-demo-image-viewer__ui-image--img {
  width: 150%;
  height: auto;
  max-width: 100%;
  max-height: 100%;
  cursor: pointer;
  position: absolute;
}

.tdesign-demo-image-viewer__ui-image--footer {
  padding: 0 16px;
  height: 56px;
  width: 100%;
  line-height: 56px;
  font-size: 16px;
  position: absolute;
  bottom: 0;
  color: var(--td-text-color-anti);
  background-image: linear-gradient(0deg, rgba(0, 0, 0, 0.4) 0%, rgba(0, 0, 0, 0) 100%);
  display: flex;
  box-sizing: border-box;
}

.tdesign-demo-image-viewer__ui-image--title {
  flex: 1;
}

.tdesign-demo-popup__reference {
  margin-left: 16px;
}

.tdesign-demo-image-viewer__ui-image--icons .tdesign-demo-icon {
  cursor: pointer;
}

.tdesign-demo-image-viewer__base {
  width: 160px;
  height: 160px;


}


.drone-patrol {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #001529;
  color: white;

  .header {
    height: 60px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    background-color: rgba(0, 0, 0, 0.2);
  }

  .content {
    flex: 1;
    display: flex;
    
    .steps-panel {
      width: 30%;
      padding: 20px;
      background-color: #ffffff;
      border-right: 1px solid #e0e0e0;
      box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
      z-index: 1;
      color: #333;
      font-weight: 500;
      height: 100vh;
      overflow-y: auto;
      position: sticky;
      top: 0;

      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-track {
        background: #f1f1f1;
        border-radius: 3px;
      }

      &::-webkit-scrollbar-thumb {
        background: #888;
        border-radius: 3px;
      }

      &::-webkit-scrollbar-thumb:hover {
        background: #555;
      }

      .step-card {
        background-color: #f5f5f5;
        border-radius: 8px;
        padding: 16px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
      }

      :deep(.t-steps) {
        color: #333;
        font-weight: 500;

        .t-step-item {
          &.t-step-item--finish {
            .t-step-item__icon {
              background-color: #0052d9;
              color: white;
            }
          }
        }
      }

      .step-content {
        padding: 8px 0;
        position: relative;
        
        p {
          margin: 8px 0;
          line-height: 1.5;
          color: #333;
        }

        .step-controls {
          display: flex;
          justify-content: space-between;
          padding: 8px 16px;
          margin-top: 16px;
          background: rgba(255, 255, 255, 0.9);
          border-radius: 20px;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
      }
    }

    .main-view {
      flex: 1;
      position: relative;
      display: flex;
      flex-direction: column;
      overflow: hidden;

      .patrol-header {
        height: 60px;
        background: linear-gradient(90deg, rgba(0,21,41,0.8) 0%, rgba(0,21,41,0.6) 100%);
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 20px;
        color: white;
        border-bottom: 1px solid rgba(0, 255, 255, 0.1);

        .time {
          font-size: 14px;
          color: rgba(255, 255, 255, 0.8);
        }

        .title {
          font-size: 20px;
          font-weight: bold;
        }

        .header-buttons {
          display: flex;
          gap: 10px;

          .header-btn {
            display: flex;
            align-items: center;
            gap: 5px;
            padding: 0 10px;
            border: 1px solid rgba(0, 255, 255, 0.1);
            border-radius: 5px;
            color: white;
            transition: all 0.3s ease;

            &:hover {
              transform: scale(1.05);
            }
          }
        }
      }

      .map-container {
        position: relative;
        width: 100%;
        height: 100%;
        background-image: url('@/assets/images/beijing.jpg'); // 需要添加卫星地图背景
        background-size: cover;
        background-position: center;

        .map-point {
          position: absolute;
          cursor: pointer;
          transform: translate(-50%, -50%); // 使标记点居中对齐

          .point-marker {
            width: 40px; // 根据实际图标大小调整
            height: 60px; // 根据实际图标大小调整
            transition: all 0.3s ease; // 添加过渡效果
            
            &:hover {
              transform: scale(1.1); // 鼠标悬停时略微放大
            }
          }
        }

        .data-panel {
          margin-bottom: 150px;
          position: absolute;
          bottom: 20px;
          left: 50%;
          transform: translateX(-50%);
          background: rgba(0, 21, 41, 0.8);
          border-radius: 10px;
          padding: 20px;
          color: white;
          width: 80%;

          .area-name {
            font-size: 22px;
            margin-bottom: 10px;
          }

          .patrol-time {
            display: flex;
            gap: 20px;
            margin-bottom: 20px;
            color: #8c8c8c;
          }

          .metrics {
            display: flex;
            justify-content: space-around;

            .metric-item {
              display: flex;
              align-items: center;
              margin-bottom: 10px;
              flex-direction: row;

              .metric-value {
                font-size: 24px;
                color: #00ffff;
              }

              .metric-label {
                color: #8c8c8c;
                margin-top: 5px;
              }
            }
          }
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
  width: 100%;
}

.collect-container {
  .collect-images {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .image-item {
      position: relative;
      cursor: pointer;

      img {
        width: 100%;
        height: 200px;
        object-fit: cover;
        border-radius: 8px;
      }

      .image-time {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        padding: 8px;
        background: rgba(0, 0, 0, 0.6);
        color: white;
        font-size: 14px;
        border-bottom-left-radius: 8px;
        border-bottom-right-radius: 8px;
      }

      .analysis-tag {
        position: absolute;
        top: 10px;
        right: 10px;
        padding: 4px 8px;
        background: #e34d59;
        color: white;
        border-radius: 4px;
        font-size: 12px;
      }
    }
  }

  .collect-controls {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-top: 20px;
  }
}

.detail-container {
  .detail-header {
    display: flex;
    align-items: center;
    margin-bottom: 24px;

    .detail-icon {
      width: 60px;
      height: 60px;
      margin-right: 16px;

      img {
        width: 100%;
        height: 100%;
        object-fit: contain;
      }
    }

    .detail-info {
      .detail-title {
        font-size: 20px;
        font-weight: bold;
        margin-bottom: 8px;
      }

      .detail-time {
        color: #999;
        font-size: 14px;
      }
    }
  }

  .filter-bar {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 16px;
  }

  .detail-list {
    .detail-item {
      display: flex;
      align-items: center;
      padding: 16px;
      border-bottom: 1px solid #eee;

      .item-image {
        width: 120px;
        height: 80px;
        object-fit: cover;
        margin: 0 16px;
        border-radius: 4px;
      }

      .item-info {
        flex: 1;

        .item-title {
          font-size: 16px;
          margin-bottom: 8px;

          // &.normal {
          //   color: #00a870;
          // }

          &.error {
            color: #e34d59;
          }
        }

        .item-desc {
          color: #666;
          margin-bottom: 4px;
        }

        .item-location {
          color: #999;
          font-size: 14px;
        }
      }

      .upload-btn {
        margin-left: 16px;
      }
    }
  }
}

.account-info {
  background: #f5f5f5;
  padding: 2px;
  border-radius: 8px;
  margin: 2px 0;

  p {
    margin: 8px 0;
    line-height: 1.5;
  }

  .account-value {
    color: #0052d9;
    font-family: monospace;
    background: rgba(0, 82, 217, 0.1);
    padding: 2px 6px;
    border-radius: 4px;
  }

  strong {
    color: #333;
    font-size: 16px;
  }
}

.resource-container {
  .resource-list {
    .resource-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px;
      border-bottom: 1px solid #eee;
      transition: background-color 0.3s;

      &:hover {
        background-color: #f5f5f5;
      }

      &:last-child {
        border-bottom: none;
      }

      .resource-info {
        display: flex;
        align-items: center;
        flex: 1;

        .resource-name {
          font-size: 14px;
          color: #333;
        }
      }
    }
  }
}
</style>