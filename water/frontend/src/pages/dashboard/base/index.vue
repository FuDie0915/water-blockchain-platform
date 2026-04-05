<template>
  <div>
    <t-back-top
      container=".tdesign-starter-layout"
      :visible-height="0"
      style="position: absolute"
      :offset="['24px', '80px']"
    >
      <t-icon name="backtop" size="20px" />
    </t-back-top>
    <!-- 顶部 card  -->
    <top-panel class="row-container" />
    <!-- 节点ID列表 -->
    <node-list class="row-container" />
    <!-- 列表排名 -->
    <rank-list class="row-container" />
  </div>
</template>
<script>
import TopPanel from './components/TopPanel.vue';
import NodeList from './components/NodeList.vue';
import RankList from './components/RankList.vue';

export default {
  name: 'DashboardBase',
  components: {
    TopPanel,
    NodeList,
    RankList,
  },
  created() {
    // 先检查localStorage中是否已有token
    const localToken = localStorage.getItem('satoken');
    
    if (localToken) {
      // 如果localStorage中已有token,直接返回
      return;
    }

    // localStorage中没有token,则检查URL参数
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('satoken');
    console.log(token);
    
    if (!token) {
      // 如果URL中也没有token参数,跳转到403页面
      this.$router.push('/result/403');
    } else {
      // URL中有token,存储到localStorage
      this.$store.dispatch('user/setToken', token);
    }
  },
};
</script>
<style scoped lang="less">
.row-container {
  margin-top: 16px;
  margin-bottom: 16px;
}
/deep/ .t-card__body {
  padding-top: 0;
}
</style>
