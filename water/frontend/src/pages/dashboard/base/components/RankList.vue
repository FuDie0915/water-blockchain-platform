<template>
  <div class="rank-list-panel">
    <!-- 左侧光伏板故障上链数据 -->
    <t-card class="left-panel" :bordered="false">
      <template #header>
        <div class="header-with-tabs">
          <h1 style="font-size: 17px;margin-top:9px;font-weight: 1000;">光伏板链上数据</h1>
          <t-tabs style="margin-left: 360px;" v-model="solarDataType">
            <t-tab-panel value="fault" label="故障数据" />
            <t-tab-panel value="consumable" label="耗材数据" />
          </t-tabs>
        </div>
      </template>

      <!-- 光伏板故障数据表格 -->
      <t-table
        v-if="solarDataType === 'fault'"
        :data="solarPanelData"
        :columns="solarPanelColumns"
        :hover="true"
        :loading="loading"
        :max-height="400"
        :stripe="false"
        row-key="id"
      />

      <!-- 光伏板耗材数据表格 -->
      <t-table
        v-if="solarDataType === 'consumable'"
        :data="consumableData"
        :columns="consumableColumns"
        :hover="true"
        :loading="loading"
        :max-height="400"
        :stripe="false"
        row-key="id"
      />
    </t-card>

    <!-- 右侧水质检测上链数据 -->
    <t-card class="right-panel" :bordered="false">
      <template #header>
        <div class="header-with-tabs">
          <h1 style="font-size: 17px;font-weight: 1000;">海水质检测上链数据</h1>
          <t-tabs style="margin-left: 400px;" v-model="waterDataType">
            <t-tab-panel value="turbidity" label="水浑浊数据" />
            <t-tab-panel value="tds" label="水TDS数据" />
          </t-tabs>
        </div>
      </template>

      <!-- 水浑浊数据表格 -->
      <t-table
        v-if="waterDataType === 'turbidity'"
        :data="turbidityData"
        :columns="turbidityColumns"
        :hover="true"
        :loading="loading"
        :max-height="400"
        :stripe="false"
        row-key="id"
      />

      <!-- 水TDS数据表格 -->
      <t-table
        v-if="waterDataType === 'tds'"
        :data="tdsData"
        :columns="tdsColumns"
        :hover="true"

        :loading="loading"
        :max-height="400"
        :stripe="false"
        row-key="id"
      />
    </t-card>
  </div>
</template>

<script>
import { waterInfoQuery } from '@/api/water/water.js'
import { getUavOnChainList } from '@/api/common/common.js'
import { ImageViewer as TImageViewer } from 'tdesign-vue'
export default {
  name: 'RankList',
  components: {
    TImageViewer
  },
  data() {
    return {
      loading: false,
      waterDataType: 'turbidity',
      solarDataType: 'fault',

      // 光伏板故障数据
      solarPanelData: [],

      // 水浑浊数据
      turbidityData: [],

      // 水TDS数据
      tdsData: [],

      // 光伏板耗材数据
      consumableData: [],

      // 光伏板故障表格列配置
      solarPanelColumns: [
        {
          colKey: 'id',
          title: 'ID',
          width: '60',
        },
        {
          colKey: 'description',
          title: '问题详情',
          width: '120',
        },
        {
          colKey: 'location',
          title: '光伏板位置',
          width: '105',
        },
        {
          colKey: 'faultLocation',
          title: '故障位置',
          width: '89',
        },
        {
          colKey: 'createTime',
          title: '采集时间',
          width: '170',
        },
        {
          colKey: 'image',
          title: '故障图片',
          width: '110',
          cell: (h, { row }) => {
            const imageUrl = row.image;

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
        // {
        //   colKey: 'status',
        //   title: '链上状态',
        //   width: '100',
        //   cell: (h, { row }) => {
        //     return h('t-tag', {
        //       props: {
        //         theme: row.onChain ? 'success' : 'danger',
        //         variant: 'light'
        //       }
        //     }, row.onChain ? '已上链' : '未上链');
        //   }
        // }
      ],

      // 水浑浊数据表格列配置
      turbidityColumns: [
        {
          colKey: 'id',
          title: '序号',
          width: '70',
        },
        {
          colKey: 'userId',
          title: '企业ID',
          width: '190',
        },
        {
          colKey: 'data',
          title: '浑浊度',
          width: '120',
        },
        {
          colKey: 'time',
          title: '采集时间',
          width: '200',
        },
        {
          colKey: 'onChain',
          title: '链上状态',
          width: '103',
          cell: (h, { row }) => {
            return h('t-tag', {
              props: {
                theme: row.status === '正常' ? 'success' : 'danger',
                variant: 'light'
              }
            }, row.onChain ? '已上链' : '未上链');
          }
        },
        {
          colKey: 'status',
          title: '状态',
          width: '100',
          cell: (h, { row }) => {
            return h('t-tag', {
              props: {
                theme: row.status === '正常' ? 'success' : 'danger',
                variant: 'light'
              }
            }, row.status);
          }
        }
      ],

      // 水TDS数据表格列配置
      tdsColumns: [
        {
          colKey: 'id',
          title: '序号',
          width: '80',
        },
        {
          colKey: 'userId',
          title: '企业ID',
          width: '200',
        },
        {
          colKey: 'data',
          title: 'TDS值',
          width: '120',
        },
        {
          colKey: 'time',
          title: '采集时间',
          width: '200',
        },
        {
          colKey: 'onChain',
          title: '是否上链',
          width: '100',
          cell: (h, { row }) => {
            return h('t-tag', {
              props: {
                theme: row.status === '正常' ? 'success' : 'danger',
                variant: 'light'
              }
            }, row.onChain ? '已上链' : '未上链');
          }
        },
        {
          colKey: 'status',
          title: '状态',
          width: '100',
          cell: (h, { row }) => {
            return h('t-tag', {
              props: {
                theme: row.status === '正常' ? 'success' : 'danger',
                variant: 'light'
              }
            }, row.status);
          }
        }
      ],

      // 光伏板耗材表格列配置
      consumableColumns: [
        {
          colKey: 'id',
          title: 'ID',
          width: '60',
        },
        {
          colKey: 'allMaterial',
          title: '总耗材',
          width: '120',
        },
        {
          colKey: 'location',
          title: '光伏板位置',
          width: '105',
        },
        {
          colKey: 'allTime',
          title: '总工时',
          width: '89',
        },
        {
          colKey: 'createTime',
          title: '采集时间',
          width: '170',
        }
      ],
    };
  },
  watch: {
    waterDataType: {
      handler(newType) {
        this.fetchWaterData();
      }
    }
  },
  methods: {
    async fetchWaterData() {
      try {
        const params = {
          DataType: this.waterDataType === 'turbidity' ? 1 : 0,
          pageNum: 1,
          pageSize: 1000
        }
        const response = await waterInfoQuery(params)
        // 模拟请求返回数据

        if (response.code === 0) {
          if (this.waterDataType === 'turbidity') {
            this.turbidityData = response.data
          } else {
            this.tdsData = response.data
          }
        }
      } catch (error) {
        this.$message.error('获取水质数据失败')
      }
    },
    async fetchData() {
      this.loading = true;
      try {
        const response = await getUavOnChainList();
        console.log(response);
        if (response.code === 0) {
          response.data.forEach(item => {
            item.image = this.$API_BASE_URL + item.image
          })
          this.solarPanelData = response.data;
          this.consumableData = response.data.map(item => {
            if (item.id % 2 === 0) {
              item.allMaterial = "螺旋桨、起落架";
              item.allTime = `${Math.floor(Math.random() * 5) + 1}h`; // 随机1-5小时
            } else if (item.id % 2 === 1) {
              item.allMaterial = "护翼、内部密封件";
              item.allTime = `${Math.floor(Math.random() * 5) + 1}h`; // 随机1-5小时
            }
            return item;
          }
          );
        }
      } catch (error) {
        console.error('获取数据失败:', error);
      } finally {
        this.loading = false;
      }
    }
  },
  mounted() {
    this.fetchWaterData();
    this.fetchData();
  }
};
</script>

<style lang="less" scoped>
.rank-list-panel {
  display: flex;
  gap: 16px;

  .left-panel,
  .right-panel {
    flex: 1;
    margin: 0;
    background: #fff;

    :deep(.t-card__body) {
      padding: 0;
    }

    :deep(.t-table) {
      .t-table__header {
        background-color: #fff;
      }

      .t-table__body {
        tr {
          background-color: #fff;

          &:hover {
            background-color: #f5f5f5;
          }
        }
      }
    }
  }

  .header-with-tabs {
    display: flex;
    align-items: center;
    justify-content: space-between;

    :deep(.t-tabs) {
      margin-bottom: -16px;
    }

    :deep(.t-tabs__nav) {
      border-bottom: none;
    }
  }
}
</style>
