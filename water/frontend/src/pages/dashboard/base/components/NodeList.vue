<template>
  <t-card class="node-list-panel" :bordered="false">
    <template #header>节点ID列表</template>
    <t-table
      :data="nodeData"
      :columns="columns"
      :hover="true"
      table-layout="auto"
      :stripe="true"
      :row-key="'id'"
      :loading="loading"

      @page-change="onPageChange"
    >
      <template #status="{ row }">
        <t-tag :theme="row.status === 1 ? 'success' : 'danger'" variant="light">
          {{ row.status === 1 ? "正常": "异常" }}
        </t-tag>
      </template>
    </t-table>
  </t-card>
</template>

<script>
import { getBlockchainBoard } from '@/api/common/common';
export default {
  name: 'NodeList',
  data() {
    return {
      loading: false,
      nodeData: [
        {
          id: '64bec7d1758aa997972aa0efd7c05174209abeb6416601ecb08bd12f14d37ec01fb9895fcaffe796bfb264faca33e5595e018ede8bb',
          height: '2319',
          pbftView: '3025441',
          status: '运行'
        },
        {
          id: 'a475b514c990995f4337f99ff452a765b51f99c7206be70f2fd9ccade039d6b62bce7818a728241080f56af10715745527cdfa1844e',
          height: '2319',
          pbftView: '3025442',
          status: '运行'
        },
        {
          id: 'c3c38561ccd97c20d97c87e1e4bd2637c5fad0d01c48a502d0660e113674c4e047da826bfa29454900a05743904fa727a72035d0d',
          height: '2319',
          pbftView: '3025445',
          status: '运行'
        },
        {
          id: 'f69aa24b81c05903e309b991389c0565a414d1d97a06238a76671b54685225653ea7386685b5e7912c7352144b0a82bcbd10e89',
          height: '2319',
          pbftView: '3025444',
          status: '运行'
        }
      ],
      columns: [
        {
          colKey: 'nodeId',
          title: '节点ID',
          width: 'auto',
          ellipsis: true
        },
        {
          colKey: 'blockNumber',
          title: '块高',
          width: '100',
        },
        {
          colKey: 'pbftView',
          title: 'pbftView',
          width: '120',
        },
        {
          colKey: 'status',
          title: '状态',
          width: '100',
          cell: 'status'
        }
      ],
      pagination: {
        total: 0,
        pageSize: 5,
        current: 1,
      },
    };
  },
  methods: {
    async onPageChange(pageInfo) {
      this.pagination.current = pageInfo.current;
      // 这里可以添加分页请求逻辑
    },

    async fetchBlockchainBoard() {
      const response = await getBlockchainBoard();
      if (response.code === 0) {
        console.log(response.data);
        this.nodeData = response.data.nodeStatusList;
      }
    },
    async fetchNodeData() {
      this.loading = true;
      try {
        // 这里可以添加获取节点数据的API请求
        // const { data } = await this.$axios.get('/api/nodes');
        // this.nodeData = data.list;
        // this.pagination.total = data.total;
      } catch (error) {
        console.error('获取节点数据失败:', error);
      } finally {
        this.loading = false;
      }
    }
  },
  mounted() {
    this.fetchBlockchainBoard();
  }
};
</script>

<style lang="less" scoped>
.node-list-panel {
  margin: 0;
  :deep(.t-card__body) {
    padding: 0;
  }
}
</style> 