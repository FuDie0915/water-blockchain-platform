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
      nodeData: [],
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