<template>
  <div class="water-monitor">
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
                    <p>(1) 点击右侧【区块链可视化引擎】，进入合约管理/合约IDE中部署编写好的智能合约；部署时填入下方养殖户及监管局区块链账户地址</p>
                    <div class="account-info">
                      <p><strong>养殖户账号信息：</strong></p>
                      <p>登录账号：<span class="account-value">{{ enterpriseAccount }}</span></p>
                      <p>登录密码：<span class="account-value">{{ enterprisePassword }}</span></p>
                      <p>区块链账户地址：<span class="account-value">{{ enterpriseBlockchainAddress }}</span></p>

                      <p><strong>监管局账号信息：</strong></p>
                      <p>登录账号：<span class="account-value">{{ monitorAccount }}</span></p>
                      <p>登录密码：<span class="account-value">{{ monitorPassword }}</span></p>
                      <p>区块链账户地址：<span class="account-value">{{ monitorBlockchainAddress }}</span></p>
                    </div>
                    <p>(2) 点击右侧【养殖户】身份进行登录，填写上方自动生成的密码, 进入养殖户界面。</p>
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
        <div class="title">海水养殖场可信水质数据监管平台</div>
        <div class="content-area">
          <!-- 未登录时显示卡片和登录组件 -->
          <div v-if="!isLoggedIn">
            <!-- 显示卡片选择 -->
            <div class="card-container" v-if="!currentLoginType">
              <div class="card sea-card-1" @click="handleCardClick('blockchain')">
                <div class="card-icon">🔗</div>
                <div class="card-title">区块链可视化引擎</div>
                <div class="card-desc">智能合约部署平台</div>
              </div>
              <div class="card sea-card-2" @click="handleCardClick('enterprise')">
                <div class="card-icon">🏭</div>
                <div class="card-title">养殖户登录</div>
                <div class="card-desc">养殖养殖户管理入口</div>
              </div>
              <div class="card sea-card-3" @click="handleCardClick('monitor')">
                <div class="card-icon">🔍</div>
                <div class="card-title">监管局登录</div>
                <div class="card-desc">监管部门管理入口</div>
              </div>
            </div>

            <!-- 显示登录组件 -->
            <div v-show="currentLoginType">
              <t-button size="small" @click="backToCards" class="back-button">返回</t-button>
              <!-- <EnterpriseLogin v-if="currentLoginType === 'enterprise'" @login-success="handleLoginSuccess" /> -->

              <div :key="18" class="enterprise-login">
                  <div class="login-container" v-show="currentLoginType === 'enterprise'">
                    <div class="login-title">
                      <h2>养殖户登录</h2>
                      <div class="login-subtitle">欢迎使用海水养殖场可信水质数据监管系统</div>
                    </div>
                    <t-form ref="form" :data="formData" :rules="rules" @submit="handleLoginSuccess">
                      <t-form-item name="username">
                        <t-input
                          v-model="formData.username"
                          placeholder="请输入用户名"
                          size="large"
                        >
                          <template #prefix-icon>
                            <t-icon name="user" />
                          </template>
                        </t-input>
                      </t-form-item>
                      <t-form-item name="password">
                        <t-input
                          v-model="formData.password"
                          type="password"
                          placeholder="请输入密码"
                          size="large"
                        >
                          <template #prefix-icon>
                            <t-icon name="lock-on" />
                          </template>
                        </t-input>
                      </t-form-item>
                      <div class="login-options">
                        <t-checkbox>记住我</t-checkbox>
                        <t-link theme="primary" hover="color">忘记密码？</t-link>
                      </div>
                      <t-form-item>
                        <t-button theme="primary" type="submit" block size="large">登录</t-button>
                      </t-form-item>
                    </t-form>
                  </div>
                </div>

                <div class="monitor-login" style="" v-show="currentLoginType === 'monitor'">
                  <div :key="15" class="login-container">
                    <div class="login-title">
                      <h2>监管局登录</h2>
                      <div class="login-subtitle">海水养殖场可信水质数据监管系统</div>
                    </div>
                    <t-form ref="form" :data="formData" :rules="rules" @submit="handleLoginSuccess">
                      <t-form-item name="username">
                        <t-input
                          v-model="formData.username"
                          placeholder="请输入用户名"
                          size="large"
                        >
                          <template #prefix-icon>
                            <t-icon name="user" />
                          </template>
                        </t-input>
                      </t-form-item>
                      <t-form-item name="password">
                        <t-input
                          v-model="formData.password"
                          type="password"
                          placeholder="请输入密码"
                          size="large"
                        >
                          <template #prefix-icon>
                            <t-icon name="lock-on" />
                          </template>
                        </t-input>
                      </t-form-item>
                      <div class="login-options">
                        <t-checkbox>记住我</t-checkbox>
                      </div>
                      <t-form-item>
                        <t-button theme="primary" type="submit" block size="large">登录</t-button>
                      </t-form-item>
                      <div class="login-tips">
                        <t-icon name="info-circle" />
                        <span>如需账号，请联系系统管理员</span>
                      </div>
                    </t-form>
                  </div>
                </div>
              <!-- <MonitorLogin v-if="currentLoginType === 'monitor'" @login-success="handleLoginSuccess" /> -->
              <t-loading attach="#alice" size="small" :loading="loading" text="登录中..."></t-loading>
            </div>
          </div>

          <!-- 登录后显示不同的表格 -->
          <div v-else class="table-container">
            <div class="user-info" style="display: flex; justify-content: space-between;" v-if="userType === 'enterprise'">
              当前用户：{{ currentUser }}
              <t-button v-if="userType === 'enterprise'" @click="logout" variant="outline" class="logout-button">退出登录</t-button>
            </div>
            <div class="controls">
              <!-- 养殖户用户显示申请许可按钮 -->
              <t-button
                v-if="userType === 'enterprise'"
                :disabled="hasActivePermit"
                @click="showApplyDialog"
                theme="primary"
                class="add-button"
              >
                申请许可
              </t-button>
            </div>

            <!-- 养殖户用户表格 -->
            <template v-if="userType === 'enterprise'">
              <div class="table-wrapper">
                <t-table
                  row-key="index"
                  :data="enterpriseTableData"
                  :columns="enterpriseColumns"
                  bordered
                  max-height="500"
                  stripe
                />
              </div>
            </template>

            <!-- 监管局用户表格 -->
            <template v-if="userType === 'monitor'">
              <!-- 顶部按钮组 -->
              <div class="monitor-header">
                <div class="user-info">
                  <span>当前用户：{{ currentUser }}</span>
                  <t-button @click="logout" variant="outline">退出登录</t-button>
                </div>
                <div class="view-switch">
                  <t-button-group>
                    <t-button
                      :variant="currentView === 'approval' ? 'base' : 'outline'"
                      @click="currentView = 'approval'"
                    >
                      养殖许可审批
                    </t-button>
                    <t-button
                      :variant="currentView === 'waterData' ? 'base' : 'outline'"
                      @click="currentView = 'waterData'"
                    >
                      水质数据
                    </t-button>
                  </t-button-group>
                  <t-button theme="primary" variant="outline" @click="showContractDialog" style="margin-left: 16px;">
                    智能合约绑定
                  </t-button>
                </div>
              </div>

              <!-- 养殖许可审批表格 -->
              <div class="table-wrapper" v-if="currentView === 'approval'">
                <t-table
                  row-key="index"
                  :data="monitorTableData"
                  :columns="monitorColumns"
                  bordered
                  max-height="500"
                  stripe
                />
              </div>

              <!-- 水数据表格区域 -->
              <div class="table-container">
                <div v-if="currentView === 'waterData'" class="table-header">
                  <div class="water-data-tabs">
                    <t-tabs v-model="waterDataType">
                      <t-tab-panel value="turbidity" label="水浑浊数据" />
                      <t-tab-panel value="tds" label="水TDS数据" />
                    </t-tabs>
                  </div>
                  <t-button v-if="waterDataType === 'turbidity'" theme="primary" variant="outline" @click="handleBatchUpload" :disabled="selectedTurbidityRowKeys.length === 0">
                    批量上链
                  </t-button>
                  <t-button v-if="waterDataType === 'tds'" theme="primary" variant="outline" @click="handleBatchUpload" :disabled="selectedTdsRowKeys.length === 0">
                    批量上链
                  </t-button>
                </div>

                <!-- 水浑浊数据表格 -->
                <div v-if="currentView === 'waterData'" class="water-data-section">
                  <div class="table-wrapper" v-if="waterDataType === 'turbidity'">
                    <t-table
                      row-key="id"
                      :data="turbidityTableData"
                      :columns="turbidityColumns"
                      :selected-row-keys="selectedTurbidityRowKeys"
                      :select-on-row-click="true"
                      :disabled-row="isRowDisabled"
                      @select-change="onTurbiditySelectChange"
                      bordered
                      max-height="400"
                      stripe
                    />
                  </div>

                  <!-- 添加分页组件 -->
                  <div class="pagination-container" style="margin-top: 5px;" v-if="waterDataType === 'turbidity'">
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

                <!-- 水TDS数据表格 -->
                <div v-if="currentView === 'waterData'" class="water-data-section">
                  <div class="table-wrapper" v-if="waterDataType === 'tds'">
                    <t-table
                      row-key="id"
                      :data="tdsTableData"
                      :columns="tdsColumns"
                      :selected-row-keys="selectedTdsRowKeys"
                      :select-on-row-click="true"
                      :disabled-row="isRowDisabled"
                      @select-change="onTdsSelectChange"
                      bordered
                      max-height="400"
                      stripe
                    />
                  </div>

                  <!-- 添加分页组件 -->
                  <div class="pagination-container" style="margin-top: 5px;"v-if="waterDataType === 'tds'">
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
                      <t-input v-model="contractForm.name" placeholder="请输入合约名称" />
                    </t-form-item>
                    <t-form-item label="合约地址">
                      <t-input v-model="contractForm.address" placeholder="请输入合约地址" />
                    </t-form-item>
                    <!-- <t-form-item label="合约ABI">
                      <t-textarea v-model="contractForm.abi" placeholder="请输入合约ABI" />
                    </t-form-item>
                    <t-form-item label="合约BIN">
                      <t-textarea v-model="contractForm.bin" placeholder="请输入合约BIN" />
                    </t-form-item> -->
                  </t-form>
                </t-dialog>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>

     <!-- 申请许可弹窗 -->
  <t-dialog
  :visible.sync="dialogVisible"
    header="申请养殖许可"
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
            <t-button theme="primary">
              点击上传
            </t-button>
          </template>
          <!-- 添加预览图片 -->
          <template #fileList>
            <div v-if="applyForm.permitImage" class="upload-preview">
              <t-image
                :src="applyForm.imgUrl"
                fit="cover"
                style="width: 100px; height: 100px; border-radius: 4px;"
              />
            </div>
          </template>
        </t-upload>
      </t-form-item>
    </t-form>
  </t-dialog>

    <!-- 添加比对成功弹窗 -->
    <t-dialog
    :visible.sync="compareDialogVisible"
    header="链上比对结果"
    :footer="false"
    width="400px"
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
import { BrowseIcon } from 'tdesign-icons-vue';
import {bindContract} from '@/api/common/common.js'
// 先导入组件
import EnterpriseLogin from './enterprise.vue';
import MonitorLogin from './monitor.vue'

// 在 <script> 标签下添加导入
import {
  collectWaterGen,
  upChain,
  Logout,
  commitPermission,
  getStepOneData,
  queryPermission,
  chainCompare,
  queryPermissionByManager,
  waterInfoQuery,
  approvePermissionByManager,
  Login
} from '@/api/water/water.js'
import { uploadCommonFile } from '@/api/common/common.js'
import uploadXuke from '@/assets/images2/liucheng2.png'
import bangding from '@/assets/images2/liucheng3.png'
import bushu from '@/assets/images2/bushu.jpg'
// 导入所需组件
import {
  // ... 其他导入
  ImageViewer as TImageViewer,
  Image as TImage,
  Button as TButton,

} from 'tdesign-vue'
import setting from '../../store/modules/setting';

export default {
  name: 'SeaMonitor',
  components: {
    EnterpriseLogin,
    MonitorLogin,
    BrowseIcon,
    TImage,
    TImageViewer,
    TButton,


  },
  data() {
    return {
      formData: {
        username: '',
        password: ''
      },
      rules: {
        username: [{ required: true, message: '请输入用户名', type: 'error' }],
        password: [{ required: true, message: '请输入密码', type: 'error' }]
      },
      uploadCommonFileUrl: '/api/common/upload',
      visible: false,
      currentStep: 0,
      steps: [
        {
          image: bushu,
          visible: false,
          title: '步骤一：智能合约部署',
          content: '' // 步骤一的内容将通过模板直接渲染
        },
        {
          image: uploadXuke,
          visible: false,
          title: '步骤二：养殖户进行养殖许可申请',
          content: `
            <p>(1)登录养殖户区块链账户后，选择【养殖户】身份登录，账号密码参考【步骤一】，进行养殖许可申请</p>
            <p>(2) 点击申请许可，上传养殖许可证；</p>
            <p>(3) 上传成功后查看养殖许可证状态</p>
          `
        },
        {
          image: bangding,
          visible: false,
          title: '步骤三：监管局审核数据',
          content: `
            <p>(1) 监管局登录系统, 账号密码参考【步骤一】；</p>
            <p>(2) 绑定智能合约信息；</p>
            <p>(3) 查看养殖户上传的养殖许可证；</p>
            <p>(4) 进行许可证审批上链以及链上比对操作。</p>
          `
        },
        {
          visible: false,
          title: '步骤四：养殖户上传水质数据',
          content: `
            <p>(1) 操作物联网设备，获取养殖场水质数据；</p>
            <p>(2) 通过物联网设备与后端讲数据传输到平台。</p>
          `
        },
        {
          title: '步骤五：监管局审核水质数据并上链',
          content: `
            <p>(1) 监管局登录系统, 账号密码参考【步骤一】；</p>
            <p>(2) 查询养殖户上传的养殖场水质数据；</p>
            <p>(3) 进行数据审核上链操作。</p>
          `
        }
      ],
      currentLoginType: '', // 当前登录类型：'' | 'enterprise' | 'monitor'
      isLoggedIn: false, // 登录状态
      currentUser: '', // 当前用户
      userType: '', // 用户类型：'enterprise' 或 'monitor'

      // 养殖户用户表格数据
      enterpriseTableData: [
      ],

      // 监管局用户表格数据
      monitorTableData: [

        // ... 其他数据
      ],
      loading: false, // 表格加载状态
      // 养殖户用户表格列配置
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
          title: '养殖许可证',
          width: 120,
          cell: (h, { row }) => {
            const imageUrl = row.permitImage;

            return h(TImageViewer, {
              props: {
                images: [imageUrl],
                closeOnEscKeydown: true
              },
              scopedSlots: {
                trigger: ({ open }) => h('img', {
                  style: {
                    width: '80px',
                    height: '80px',
                    objectFit: 'cover',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  },
                  attrs: {
                    src: imageUrl
                  },
                  on: {
                    click: open  // 点击打开预览
                  }
                })
              }
            });
          }
        },
        {
          colKey: 'status',
          title: '状态',
          cell: (h, { row }) => {
            // 状态映射
            const statusMap = {
              0: { theme: 'warning', text: '待审批' },
              1: { theme: 'success', text: '通过并上链' },
              2: { theme: 'danger', text: '不通过' }
            };

            const status = statusMap[row.status] || { theme: 'default', text: '未知状态' };

            return h('t-tag', {
              props: {
                theme: status.theme,
                variant: 'light'
              }
            }, status.text);
          }
        },
        {
          colKey: 'createTime',
          title: '创建时间',
        },
      ],

      // 监管局用户表格列配置
      monitorColumns: [
        {
          colKey: 'id',
          title: '序号',
          width: 80,
        },
        {
          colKey: 'companyName',
          title: '养殖户名称',
          width: 200,
        },
        {
          colKey: 'permit',
          title: '养殖许可证',
          width: 120,
          cell: (h, { row }) => {
            const imageUrl = row.permitImage;

            return h(TImageViewer, {
              props: {
                images: [imageUrl],
                closeOnEscKeydown: true
              },
              scopedSlots: {
                trigger: ({ open }) => h('img', {
                  style: {
                    width: '80px',
                    height: '80px',
                    objectFit: 'cover',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  },
                  attrs: {
                    src: imageUrl
                  },
                  on: {
                    click: open  // 点击打开预览
                  }
                })
              }
            });
          }
        },
        {
          colKey: 'createTime',
          title: '提交时间',
          width: 200
        },
        {
          colKey: 'status',
          title: '状态',
          cell: (h, { row }) => {
            // 状态映射
            const statusMap = {
              0: { theme: 'warning', text: '待审批' },
              1: { theme: 'success', text: '通过并上链' },
              2: { theme: 'danger', text: '不通过' }
            };

            const status = statusMap[row.status] || { theme: 'default', text: '未知状态' };

            return h('t-tag', {
              props: {
                theme: status.theme,
                variant: 'light'
              }
            }, status.text);
          }
        },
        {
          colKey: 'operation',
          title: '操作',
          width: 300,
          cell: (h, { row }) => {
            const isPending = row.status === 0; // 待审批状态

            return h('div', {
              style: {
                display: 'flex',
                gap: '8px',
                justifyContent: 'flex-start'
              }
            }, [
              // 只有待审批状态才显示这两个按钮
              ...(isPending ? [
                h('t-popconfirm', {
                  props: {
                    content: '确认通过并上链吗？',
                    theme: 'primary'
                  },
                  on: {
                    confirm: () => this.handleApprove(row)
                  }
                }, [
                  h('t-button', {
                    props: {
                      theme: 'primary',
                      variant: 'text',
                      size: 'small'
                    }
                  }, '通过上链')
                ]),
                h('t-popconfirm', {
                  props: {
                    content: '确定驳回请求吗？',
                    theme: 'danger'
                  },
                  on: {
                    confirm: () => this.handleReject(row)
                  }
                }, [
                  h('t-button', {
                    props: {
                      theme: 'danger',
                      variant: 'text',
                      size: 'small'
                    }
                  }, '驳回')
                ])
              ] : []),
              // 链上比对按钮始终显示
              h('t-button', {
                props: {
                  theme: 'primary',
                  variant: 'text',
                  size: 'small'
                },
                on: {
                  click: () => this.handleCompare(row)
                }
              }, '链上比对')
            ]);
          }
        }
      ],
      dialogVisible: false, // 控制弹窗显示
      uploadFile: null, // 上传的文件
      myFileList: [],
      applyForm: {
        imgUrl: '',
        permitImage: [] // 这里将存储 base64 字符串
      },
      componentsLoaded: false,
      currentView: 'approval', // 当前视图：'approval' | 'waterData'
      waterDataType: 'turbidity', // 水数据类型：'turbidity' | 'tds'

      // 水浑浊数据表格数据
      turbidityTableData: [

      ],

      // 水TDS数据表格数据
      tdsTableData: [

      ],

      // 水浑浊数据表格列配置
      turbidityColumns: [
        {
          colKey: 'row-select',
          type: 'multiple',
          width: 50,
          fixed: 'left',
        },
        {
          colKey: 'id',
          title: '序号',
          width: 80,
        },
        {
          colKey: 'userId',
          title: '养殖户id',
          width: 200,
        },
        {
          colKey: 'data',
          title: '浑浊度',
        },
        {
          colKey: 'time',
          title: '采集时间',
          width: 200,
        },
        {
          colKey: 'status',
          title: '状态',
          cell: (h, { row }) => {
            return h('t-tag', {
              props: {
                theme: row.status === '正常' ? 'success' : 'danger',
                variant: 'light'
              }
            }, row.status);
          }
        },
        {
          colKey: 'onChain',
          title: '是否上链',
          cell: (h, { row }) => {
            return h('t-tag', {
              props: {
                theme: row.onChain === true ? 'success' : 'danger',
                variant: 'light'
              }
            }, row.onChain === true ? '已上链' : '未上链');
          }
        },
      ],

      // 水TDS数据表格列配置
      tdsColumns: [
        {
          colKey: 'row-select',
          type: 'multiple',
          width: 50,
          fixed: 'left',
        },
        {
          colKey: 'id',
          title: '序号',
          width: 80,
        },
        {
          colKey: 'userId',
          title: '养殖户Id',
          width: 200,
        },
        {
          colKey: 'data',
          title: 'TDS值',
        },
        {
          colKey: 'time',
          title: '采集时间',
          width: 200,
        },
        {
          colKey: 'status',
          title: '状态',
          cell: (h, { row }) => {
            return h('t-tag', {
              props: {
                theme: row.status === '正常' ? 'success' : 'danger',
                variant: 'light'
              }
            }, row.status);
          }
        },
        {
          colKey: 'onChain',
          title: '是否上链',
          cell: (h, { row }) => {
            return h('t-tag', {
              props: {
                theme: row.onChain === true ? 'success' : 'danger',
                variant: 'light'
              }
            }, row.onChain === true ? '已上链' : '未上链');
          }
        }
      ],
      selectedRowKeys: [], // 选中的行keys
      selectedTdsRowKeys: [], // TDS数据选中的行keys
      selectedTurbidityRowKeys: [], // 浑浊度数据选中的行keys
      contractDialogVisible: false,
      contractForm: {
        name: '',
        address: '',
        abi: '',
        bin: '',
      },
      // 添加账号信息相关数据
      enterpriseAccount: '',
      enterprisePassword: '',
      enterpriseBlockchainAddress: '',
      monitorAccount: '',
      monitorPassword: '',
      monitorBlockchainAddress: '',

      // 分开定义两个分页对象
      turbidityPagination: {
        current: 1,
        pageSize: 10,
        total: 0
      },
      tdsPagination: {
        current: 1,
        pageSize: 10,
        total: 0
      },
      compareDialogVisible: false,
      currentCompareImage: ''
    }
  },
  // 在mounted中预加  载组件
  mounted() {
  },

  created() {
    // 在组件创建时获取账号信息
    this.getAccountInfo()
  },

  methods: {
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
    handleCardClick(type) {
      if (type === 'blockchain') {
        window.open('http://192.168.139.129:5002/WeBASE-Front', '_blank')
        return
      }
      this.formData = {
        username: '',
        password: ''
      }
      this.currentLoginType = type
    },
    // handleLoginSuccess(userType) {
    //   this.isLoggedIn = true;
    //   this.userType = userType; // 设置用户类型
    //   this.currentUser = userType === 'enterprise' ? '养殖户用户' : '监管局用户';
    // },
    logout() {
      if (this.currentLoginType === 'enterprise') {
        Logout(localStorage.getItem('farmertoken'))
        localStorage.removeItem('farmertoken')
      } else {
        Logout(localStorage.getItem('managertoken'))
        localStorage.removeItem('managertoken')
      }
      this.isLoggedIn = false;
      this.currentLoginType = '';

    },
    addNewItem() {
      // 新增数据逻辑
      const newItem = { id: this.tableData.length + 1, company: '新养殖户', permitImage: 'new-image-url', status: '待审核' };
      this.tableData.push(newItem);
    },
    backToCards() {
      this.currentLoginType = ''
    },

    uploadSuccess(response) {
      console.log(response)
      this.applyForm.permitImage[0] = response.response.fileName
      this.applyForm.imgUrl = this.$API_BASE_URL + response.response.fileName
      this.$message.success('图片上传成功');
    },

    // 处理文件上传
    async handleFileChange(file) {
      console.log(file, 'file');
      if (file.length === 0) {
        return
      }
    },
    // 修改上传组件
    showApplyDialog() {
      this.dialogVisible = true;
      this.applyForm = {
      company: '',
      permitImage: [] // 重置为空数组
    };
    },
    // 处理登录
    async handleLoginSuccess({validateResult}) {
      // 表单验证
      if (validateResult === true) {
      const userType = this.currentLoginType
      this.loading = true;
      try {
        const loginData = {
          userAccount: this.formData.username,
          userPassword: this.formData.password,
          loginType: userType === 'enterprise' ? 0 : 1
        }
        const response = await Login(loginData)
        if (response.code === 0) {
          if (userType === 'enterprise') {
            localStorage.setItem('farmertoken', response.data.token)
          } else {
            localStorage.setItem('managertoken', response.data.token)
          }
          this.currentLoginType = userType
          this.userType = userType
          await this.fetchPermissionList()
          if (this.userType === 'monitor') {
            this.waterDataType = 'turbidity'
            await this.fetchWaterData()
          }
          this.currentUser = userType === 'enterprise' ? '养殖户用户' : '监管局用户'
          this.$message.success('登录成功')
          this.loading = false;
          this.isLoggedIn = true
        } else {
          this.loading = false;
          this.$message.error(response.data.message)
        }

      } catch (error) {
        this.loading = false;
        console.log(error, 'error');
        this.$message.error('登录失败')
      }
     }else {
       this.$message.error('请输入用户名和密码')
       return
     }
    },


    // 处理许可证申请提交
    async handleApplySubmit() {
      try {
        if (this.applyForm.permitImage.length === 0) {
          this.$message.error('请上传许可证图片')
          return
        }
        const params = {
          imageUrl: this.applyForm.permitImage[0]
        }
        const response = await commitPermission(params)
        console.log(response, 'response');
        if (response.code === 0) {
          this.dialogVisible = false
          this.$message.success('申请提交成功')
          // 刷新养殖户许可证列表
          this.fetchPermissionList()
        } else {
          this.$message.error(response.data.message)
        }
      } catch (error) {

      }
    },
    handleApplyCancel() {
      this.dialogVisible = false
      this.applyForm = {
        company: '',
        permitImage: [] // 重置为空数组
      }
    },

    // 获取许可证列表
    async fetchPermissionList() {
      try {
        let response
        if (this.userType === 'enterprise') {
          response = await queryPermission()

        } else {
          response = await queryPermissionByManager()

        }

        if (response.code === 0) {
          if (this.userType === 'enterprise') {
            response.data.forEach(item => {
              item.permitImage = this.$API_BASE_URL + item.imageUrl
            })
            this.enterpriseTableData = response.data
          } else {
            response.data.forEach(item => {
              item.permitImage = this.$API_BASE_URL + item.imageUrl
            })
            this.monitorTableData = response.data
          }
        }
      } catch (error) {
        this.$message.error('获取许可证列表失败')
      }
    },

    // 处理审批
    async handleApprove(row) {
      try {
        const response = await approvePermissionByManager({
          id: row.id,
          status: 1
        })

        if (response.code === 0) {
          this.$message.success('审批通过成功')
          this.fetchPermissionList()
        }
      } catch (error) {

      }
    },

    // 处理驳回
    async handleReject(row) {
      try {
        const response = await approvePermissionByManager({
          id: row.id,
          status: 2 // 驳回
        })
        if (response.code === 0) {
          this.$message.success('驳回成功')
          this.fetchPermissionList()
        }
      } catch (error) {

      }
    },

    // 处理链上比对
    async handleCompare(row) {
      try {
        const response = await chainCompare(row.id);
        if (response.code === 0) {
          // 设置当前比对的图片
          this.currentCompareImage = this.$API_BASE_URL + response.data.imageUrl;
          // 显示比对成功弹窗
          this.compareDialogVisible = true;
          this.$message.success('链上比对成功')
        }
      } catch (error) {

      }
    },

    // 处理批量上链
    async handleBatchUpload() {
      if (this.waterDataType === 'turbidity') {
        if (this.selectedTurbidityRowKeys.length === 0) {
          this.$message.warning('请先选择要上链的数据')
          return
        }
        try {
          const response = await upChain(this.selectedTurbidityRowKeys)
        if (response.code === 0) {
          this.$message.success(`${this.selectedTurbidityRowKeys.length}条数据上链成功`)
          this.selectedTurbidityRowKeys = []
          this.turbidityPagination.current = 1
          this.fetchWaterData()
        }
        } catch (error) {
          // this.$message.error('数据上链失败')
        }
      } else if (this.waterDataType === 'tds') {
        if (this.selectedTdsRowKeys.length === 0) {
          this.$message.warning('请先选择要上链的数据')
          return
        }
        try {
          const response = await upChain(this.selectedTdsRowKeys)
        if (response.code === 0) {
          this.$message.success(`${this.selectedTdsRowKeys.length}条数据上链成功`)
          this.selectedTdsRowKeys = []
          this.tdsPagination.current = 1
          this.fetchWaterData()
        }
        } catch (error) {
          // this.$message.error('数据上链失败')
        }
      }
    },

    // 获取水质数据
    async fetchWaterData() {
      console.log("fetchWaterData")
      try {
        const params = {
          DataType: this.waterDataType === 'turbidity' ? 1 : 0,
          pageNum: this.pagination.current,
          pageSize: this.pagination.pageSize
        }
        const response = await waterInfoQuery(params)
        // 模拟请求返回数据

        if (response.code === 0) {
          if (this.waterDataType === 'turbidity') {
            this.turbidityTableData = response.data
          } else {
            this.tdsTableData = response.data
          }
          this.pagination.total = response.total
        }
      } catch (error) {
        this.$message.error('获取水质数据失败')
      }
    },

    // 显示智能合约绑定弹窗
    showContractDialog() {
      this.contractDialogVisible = true;
    },

    // 处理智能合约绑定
    async handleContractBind() {
      if (!this.validateContractForm()) {
        return;
      }

      try {
        const contractData = {
          accountAddress: "",
          contractAbi: "",
          contractAddress: this.contractForm.address,
          contractBin: "",
          contractName: this.contractForm.name,
          contractType: "海水养殖" //
        };

        // 调用合约绑定接口
        const response = await bindContract(contractData)
        if (response.code === 0) {
          this.$message.success('合约绑定成功');
          this.contractDialogVisible = false;
          this.resetContractForm();
        }
      } catch (error) {
        console.error('合约绑定失败:', error);

      }
    },

    // 验证合约表单
    validateContractForm() {
      if (!this.contractForm.name) {
        this.$message.warning('请输入合约名称');
        return false;
      }
      if (!this.contractForm.address) {
        this.$message.warning('请输入合约地址');
        return false;
      }
      // if (!this.contractForm.abi) {
      //   this.$message.warning('请输入合约ABI');
      //   return false;
      // }
      // if (!this.contractForm.bin) {
      //   this.$message.warning('请输入合约BIN');
      //   return false;
      // }
      return true;
    },

    // 重置合约表单
    resetContractForm() {
      this.contractForm = {
        name: '',
        address: '',
        abi: '',
        bin: '',
        accountAddress: ''
      };
    },


    // 浑浊度数据选择事件
    onTurbiditySelectChange(selectedRowKeys, selectedRows) {
      console.log(selectedRows)
      this.selectedTurbidityRowKeys = selectedRows.selectedRowData
        .filter(item => item && !item.onChain)  // 过滤掉已上链的数据
        .map(item => item.id);
      console.log(this.selectedTurbidityRowKeys, 'selectedTurbidityRowKeys');
    },

    // TDS数据选择事件
    onTdsSelectChange(selectedRowKeys, selectedRows) {

      this.selectedTdsRowKeys = selectedRows.selectedRowData
        .filter(item => item && !item.onChain)  // 过滤掉已上链的数据
        .map(item => item.id);
    },
    // TDS数据选择事件



    // 获取账号信息
    async getAccountInfo() {
      try {
        // const response = await this.$axios.get('/api/account/info')
        // 直接设置账号信息，不需要更新 steps[0].content
        const { data } = await getStepOneData()
        this.enterpriseAccount = data.companyAccount
        this.enterprisePassword = data.companyPassword
        this.enterpriseBlockchainAddress = data.companyAddress
        this.monitorAccount = data.managerAccount
        this.monitorPassword = data.managerPassword
        this.monitorBlockchainAddress = data.managerAddress
      } catch (error) {
        console.error('获取账号信息失败:', error)
        this.$message.error('获取账号信息失败')
      }
    },

    // 浑浊度数据分页方法
    onTurbidityPageChange(current) {
      this.turbidityPagination.current = current;
      this.fetchTurbidityData();
    },

    onTurbidityPageSizeChange(pageSize) {
      this.turbidityPagination.pageSize = pageSize;
      this.turbidityPagination.current = 1; // 重置到第一页
      this.fetchTurbidityData();
    },

    // TDS数据分页方法
    onTdsPageChange(current) {
      this.tdsPagination.current = current;
      this.fetchTdsData();
    },

    onTdsPageSizeChange(pageSize) {
      this.tdsPagination.pageSize = pageSize;
      this.tdsPagination.current = 1; // 重置到第一页
      this.fetchTdsData();
    },

    // 分开获取两种数据的方法
    async fetchTurbidityData() {
      try {
        const params = {
          DataType: 1,
          pageNum: this.turbidityPagination.current,
          pageSize: this.turbidityPagination.pageSize
        }
        const response = await waterInfoQuery(params)

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
        const params = {
          DataType: 0,
          pageNum: this.tdsPagination.current,
          pageSize: this.tdsPagination.pageSize
        }
        const response = await waterInfoQuery(params)

        if (response.code === 0) {
          this.tdsTableData = response.data;
          this.tdsPagination.total = Number(response.total);
        }
      } catch (error) {
        this.$message.error('获取TDS数据失败');
      }
    },

    // 根据类型切换调用对应的获取数据方法
    async fetchWaterData() {
      if (this.waterDataType === 'turbidity') {
        await this.fetchTurbidityData();
      } else {
        await this.fetchTdsData();
      }
    },

    // 判断行是否禁用
    isRowDisabled(row) {
      return row.onChain === true;  // 如果已上链则禁用选择
    }
  },
  watch: {
    waterDataType: {
      handler(newType) {
        this.fetchWaterData();
      }
    }
  },
  computed: {
    // 判断是否有待审批或已通过的许可
    hasActivePermit() {
      if (this.enterpriseTableData.length == 0) {
        return false;
      }
      return this.enterpriseTableData.some(item =>
        item.status === 0 || item.status === 1  // 状态为待审批(0)或通过并上链(1)
      );
    }
  }
}
</script>

<style scoped>
.water-monitor {
  height: 93vh;
  display: flex;
  flex-direction: column;
  background-image: url('../../assets/images2/beijing.png');

  .content {
    flex: 1;
    display: flex;
    overflow: hidden;
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

      .step-content {
        padding: 8px 0;

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
      position: relative;
      flex: 1;
      padding: 20px;
      display: flex;
      flex-direction: column;
      align-items: center;
      background-image: url('../../assets/images2/beijing.png');
      background-size: cover;
      background-position: center;
      overflow: hidden;
      .title {
        font-size: 32px;
        font-weight: 900;
        margin-bottom: 40px;
        color: #ffffff;
        text-shadow: 2px 2px 4px rgba(0, 188, 212, 0.3);
        letter-spacing: 2px;
      }

      .monitor-login {
  padding: 20px;

  .login-container {
    width: 50%;
    max-width: 880px;
    top: 15%;
    left: 25%;
    min-height: 430px;
    margin: 0 auto;
    padding: 40px;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 20px;
    box-shadow: 0 12px 40px rgba(0, 96, 100, 0.3);
    backdrop-filter: blur(10px);
    border: 2px solid rgba(0, 188, 212, 0.3);

    .login-title {
      text-align: center;
      margin-bottom: 40px;

      h2 {
        font-size: 28px;
        color: #00838f;
        margin-bottom: 8px;
        font-weight: 700;
      }

      .login-subtitle {
        color: #00695c;
        font-size: 14px;
      }
    }

    .login-options {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin: 16px 0;
    }

    .login-tips {
      margin-top: 24px;
      text-align: center;
      color: #888;
      font-size: 14px;

      .t-icon {
        margin-right: 8px;
        vertical-align: middle;
      }
    }
  }
}

      .content-area {
        width: 100%;
        max-width: 1200px;

        .card-container {
          margin-top: 80px;
          display: flex;
          justify-content: center;
          gap: 35px;

          .card {
            width: 280px;
            height: 240px;
            border-radius: 20px;
            box-shadow: 0 8px 30px rgba(0, 96, 100, 0.25);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            position: relative;
            overflow: hidden;
            border: 2px solid rgba(255, 255, 255, 0.5);

            &:hover {
              transform: translateY(-10px) scale(1.05);
              box-shadow: 0 15px 45px rgba(0, 150, 136, 0.4);
            }

            &.sea-card-1 {
              background: linear-gradient(135deg, #26c6da 0%, #00acc1 100%);
            }

            &.sea-card-2 {
              background: linear-gradient(135deg, #00897b 0%, #00695c 100%);
            }

            &.sea-card-3 {
              background: linear-gradient(135deg, #0097a7 0%, #00838f 100%);
            }

            .card-icon {
              font-size: 52px;
              margin-bottom: 20px;
              filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
            }

            .card-title {
              color: #ffffff;
              font-weight: 700;
              font-size: 22px;
              margin-bottom: 12px;
              text-align: center;
              text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
            }

            .card-desc {
              color: rgba(255, 255, 255, 0.9);
              font-size: 14px;
              text-align: center;
              font-weight: 500;
            }
          }

        }

        .user-info {
          font-size: 18px;
          margin-bottom: 20px;
          color: #333;
        }

        .add-button {
          margin-bottom: 20px;
        }

        .logout-button {
          margin-bottom: 20px;
          margin-right: 20px;
        }

        .table-container {
          width: 100%;
          padding: 25px;
          background: rgba(255, 255, 255, 0.95);
          border-radius: 16px;
          box-shadow: 0 8px 30px rgba(0, 96, 100, 0.2);
          border: 2px solid rgba(0, 188, 212, 0.2);

          .controls {
            display: flex;
            margin-bottom: 20px;
            gap: 10px;
          }

          .user-info {
            margin-bottom: 20px;
            font-size: 16px;
            color: #00695c;
            font-weight: 600;
          }

          .t-table {
            background: #fff;
            border-radius: 12px;
            overflow: hidden;
          }
        }
      }
    }
  }
}

.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px 28px;
  background: linear-gradient(135deg, #ffffff 0%, #e0f7fa 100%);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 96, 100, 0.15);
  border: 2px solid rgba(0, 188, 212, 0.2);

  .user-info {
    display: flex;
    align-items: center;
    gap: 16px;

    span {
      font-size: 14px;
      color: #333;
    }
  }

  .view-switch {
    .t-button-group {
      .t-button {
        min-width: 90px;
      }
    }
  }
}

.table-container {
  background-color: #fff;
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 96, 100, 0.15);
  border: 2px solid rgba(0, 188, 212, 0.2);

  .water-data-tabs {
    margin-bottom: 20px;

    :deep(.t-tabs__nav) {
      border-bottom: none;
    }

    :deep(.t-tabs__nav-item) {
      padding: 10px 20px;
      margin-right: 16px;
      border-radius: 8px;
      font-weight: 600;

      &.t-is-active {
        background: linear-gradient(135deg, #00acc1 0%, #0097a7 100%);
        color: #ffffff;
        box-shadow: 0 4px 12px rgba(0, 150, 136, 0.3);
      }
    }
  }
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.batch-upload {
  margin-left: 16px;
}

.account-info {
  padding: 20px;
  border-radius: 12px;
  margin: 16px 0;

  p {
    margin: 10px 0;
    line-height: 2;
    color: #004d40;

    strong {
      color: #00695c;
      font-weight: 700;
      font-size: 16px;
    }
  }

  .account-value {
    color: #00695c;
    background: rgba(0, 150, 136, 0.15);
    padding: 4px 10px;
    border-radius: 6px;
    margin-left: 8px;
    font-family: monospace;
    font-weight: 600;
    border: 1px solid rgba(0, 150, 136, 0.2);
  }
}


.t-image {
  transition: all 0.3s ease;

  &:hover {
    transform: scale(1.05);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}


:deep(.t-image-viewer__modal) {
  z-index: 3000;
}

.image-container {
  display: inline-block;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.image-container:hover {
  transform: scale(1.05);
}

.table-preview-image {
  width: 40px !important;
  height: 40px !important;
  border-radius: 4px !important;
  object-fit: cover !important;
  border: 1px solid #eee;
}

/* 确保图片容器大小固定 */
:deep(.t-image__wrapper) {
  width: 40px !important;
  height: 40px !important;
}

.add-button {
  margin-bottom: 16px;
}

/* 禁用状态的按钮样式 */
.add-button[disabled] {
  cursor: not-allowed;
  opacity: 0.6;
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

/* 调整弹窗样式 */
:deep(.t-dialog__body) {
  padding: 0;
}

:deep(.t-dialog__header) {
  padding: 16px 24px;
  border-bottom: 1px solid #e5e5e5;
}

.tdesign-demo-image-viewer__ui-image--icons .tdesign-demo-icon {
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

.tdesign-demo-image-viewer__ui-image--title {
  flex: 1;
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

.tdesign-demo-image-viewer__base {
  width: 160px;
  height: 160px;


}

/* 可选：为已上链的行添加特殊样式 */
:deep(.t-table) {
  .t-table__row--disabled {
    opacity: 0.6;

    .t-checkbox {
      cursor: not-allowed;
    }
  }
}

.enterprise-login {
  padding: 20px;

  .login-container {
    left: 25%;
    top: 15%;
    min-height: 430px;
    width: 50%;
    max-width: 680px;
    margin: 0 auto;
    padding: 40px;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 20px;
    box-shadow: 0 12px 40px rgba(0, 96, 100, 0.3);
    backdrop-filter: blur(10px);
    border: 2px solid rgba(0, 188, 212, 0.3);

    .login-title {
      text-align: center;
      margin-bottom: 40px;

      h2 {
        font-size: 28px;
        color: #00838f;
        margin-bottom: 8px;
        font-weight: 700;
      }

      .login-subtitle {
        color: #00695c;
        font-size: 14px;
        font-weight: 500;
      }
    }

    .login-options {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin: 16px 0;
    }
  }
}

/* 海洋主题按钮样式覆盖 */
:deep(.t-button--theme-primary) {
  background: linear-gradient(135deg, #00acc1 0%, #0097a7 100%) !important;
  border-color: #00acc1 !important;
  box-shadow: 0 4px 12px rgba(0, 150, 136, 0.3);

  &:hover {
    background: linear-gradient(135deg, #00bcd4 0%, #00acc1 100%) !important;
    box-shadow: 0 6px 16px rgba(0, 150, 136, 0.4);
  }
}

:deep(.t-button--variant-outline) {
  border-color: #00acc1 !important;
  color: #00838f !important;

  &:hover {
    background-color: rgba(0, 188, 212, 0.1) !important;
  }
}

/* 步骤组件主题色 */
:deep(.t-steps-item__icon) {
  background-color: rgba(255, 255, 255, 0.2) !important;
  border-color: rgba(255, 255, 255, 0.5) !important;
}

:deep(.t-steps-item--finish .t-steps-item__icon) {
  background-color: #00acc1 !important;
  border-color: #00acc1 !important;
}

:deep(.t-steps-item--process .t-steps-item__icon) {
  background-color: #26c6da !important;
  border-color: #26c6da !important;
}

/* 表格wrapper添加滚动功能 */
.table-wrapper {
  max-height: 550px;
  overflow-y: auto;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 150, 136, 0.15);

  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-track {
    background: rgba(0, 188, 212, 0.1);
    border-radius: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: linear-gradient(135deg, #00acc1 0%, #0097a7 100%);
    border-radius: 4px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: linear-gradient(135deg, #00bcd4 0%, #00acc1 100%);
  }
}

/* 表格主题色调整 - 海洋特色 */
:deep(.t-table) {
  background: #ffffff;

  th {
    background: linear-gradient(135deg, #00838f 0%, #00695c 100%) !important;
    color: #ffffff !important;
    font-weight: 700;
    font-size: 15px;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
    border-color: rgba(0, 150, 136, 0.3) !important;
  }

  td {
    border-color: rgba(0, 188, 212, 0.15) !important;
    transition: all 0.3s ease;
  }

  /* 斑马纹样式 */
  .t-table__row--striped {
    background: rgba(224, 247, 250, 0.3) !important;
  }

  /* hover效果 */
  tbody tr:hover {
    background: rgba(178, 235, 242, 0.4) !important;
    transform: scale(1.002);
    box-shadow: 0 2px 8px rgba(0, 150, 136, 0.15);
  }

  /* 选中行样式 */
  .t-table__row--selected {
    background: rgba(0, 188, 212, 0.15) !important;
  }

  /* 表格边框圆角 */
  .t-table__content {
    border-radius: 12px;
    overflow: hidden;
  }
}

/* 标签主题色 */
:deep(.t-tag--theme-success) {
  background-color: rgba(0, 150, 136, 0.1) !important;
  color: #00695c !important;
  border-color: #00897b !important;
}

:deep(.t-tag--theme-warning) {
  background-color: rgba(255, 193, 7, 0.1) !important;
  color: #f57c00 !important;
}


</style>

