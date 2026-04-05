/**
 * AI Assistant 入口文件
 * 计划书 4：使用 new Vue(Component).$mount() 创建实例
 * 将生成的 $el 追加到 document.body
 * 目的：彻底脱离 #app，避免受全局 overflow 影响
 */

import Vue from 'vue';
import FloatingBall from './FloatingBall.vue';
import ChatDrawer from './ChatDrawer.vue';

// 导入样式（确保在挂载前加载）
import './styles.less';

// 创建一个根组件来管理悬浮球和抽屉
const AiAssistantRoot = Vue.extend({
  name: 'AiAssistantRoot',
  components: {
    FloatingBall,
    ChatDrawer
  },
  data() {
    return {
      // 悬浮球始终挂载显示，抽屉默认关闭
      drawerVisible: false,
      isAlert: false
    };
  },
  methods: {
    /**
     * 切换抽屉显示
     */
    toggleDrawer() {
      this.drawerVisible = !this.drawerVisible;
      // 点击悬浮球时关闭警报状态
      if (this.drawerVisible && this.isAlert) {
        this.isAlert = false;
      }
    },

    /**
     * 关闭抽屉
     */
    closeDrawer() {
      this.drawerVisible = false;
    },

    /**
     * 警报状态变化
     */
    handleAlertStateChange(isAlert) {
      this.isAlert = isAlert;
    }
  },
  render(h) {
    return h('div', { class: 'ai-assistant-root' }, [
      // 使用组件构造函数而不是字符串标签名
      h(FloatingBall, {
        props: {
          drawerVisible: this.drawerVisible,
          alertState: this.isAlert
        },
        on: {
          'toggle-drawer': this.toggleDrawer
        }
      }),
      h(ChatDrawer, {
        props: {
          visible: this.drawerVisible
        },
        on: {
          'close': this.closeDrawer,
          'alert-state-change': this.handleAlertStateChange
        }
      })
    ]);
  }
});

// 全局实例
let aiAssistantInstance = null;

/**
 * 挂载 AI 助手
 * 计划书 4：动态挂载到 document.body
 */
function mountAiAssistant() {
  // 如果已经挂载，直接返回
  if (aiAssistantInstance) {
    return aiAssistantInstance;
  }

  // 创建 Vue 实例
  aiAssistantInstance = new AiAssistantRoot();

  // 挂载到 document.body
  // 计划书 4：彻底脱离 #app，避免受全局 overflow 影响
  const mountNode = aiAssistantInstance.$mount();

  // 将生成的 DOM 元素追加到 document.body
  document.body.appendChild(mountNode.$el);

  return aiAssistantInstance;
}

/**
 * 卸载 AI 助手
 */
function unmountAiAssistant() {
  if (aiAssistantInstance) {
    // 移除 DOM 元素
    if (aiAssistantInstance.$el && aiAssistantInstance.$el.parentNode) {
      aiAssistantInstance.$el.parentNode.removeChild(aiAssistantInstance.$el);
    }

    // 销毁 Vue 实例
    aiAssistantInstance.$destroy();
    aiAssistantInstance = null;
  }
}

// 导出挂载和卸载函数
export {
  mountAiAssistant,
  unmountAiAssistant
};

// 默认导出挂载函数（方便直接调用）
export default mountAiAssistant;
