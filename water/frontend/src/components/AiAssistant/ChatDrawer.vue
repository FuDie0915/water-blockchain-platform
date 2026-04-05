<template>
  <!-- 计划书 3.2：TDesign t-drawer，右侧滑出，宽度 400px -->
  <!-- 遮罩层点击不关闭（防误触） -->
  <t-drawer
    :visible="visible"
    size="400px"
    placement="right"
    :header="false"
    :footer="false"
    :close-on-overlay-click="false"
    :destroy-on-close="false"
    @close="handleClose"
  >
    <!-- 使用 template 包裹所有内容 -->
    <template v-slot:default>
      <div class="ai-assistant-drawer-container">
        <!-- 抽屉头部 -->
        <div class="ai-assistant-drawer-header">
          <div class="ai-assistant-drawer-title">AI 智能助手</div>
          <div style="display: flex; gap: 8px; align-items: center;">
            <!-- 新建对话按钮 -->
            <button class="ai-assistant-new-chat-btn" @click="handleNewChat">
              <svg class="ai-assistant-new-icon" viewBox="0 0 24 24">
                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
              </svg>
              新建对话
            </button>
            <!-- 关闭按钮 -->
            <button class="ai-assistant-close-btn" @click="handleClose">
              <svg class="ai-assistant-close-icon" viewBox="0 0 24 24">
                <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- 聊天内容区 -->
        <div class="ai-assistant-drawer-content">
          <chat-content
            ref="chatContent"
            :on-new-chat="handleNewChat"
            :on-alert-state-change="handleAlertStateChange"
          />
        </div>
      </div>
    </template>
  </t-drawer>
</template>

<script>
import { Drawer as TDrawer } from 'tdesign-vue';
import ChatContent from './ChatContent.vue';

export default {
  name: 'AiAssistantChatDrawer',

  components: {
    TDrawer,
    ChatContent
  },

  props: {
    // 抽屉可见性
    visible: {
      type: Boolean,
      default: false
    }
  },

  methods: {
    /**
     * 关闭抽屉
     */
    handleClose() {
      this.$emit('close');
    },

    /**
     * 新建对话
     */
    handleNewChat() {
      if (this.$refs.chatContent) {
        this.$refs.chatContent.handleNewChat();
      }
    },

    /**
     * 警报状态变化
     */
    handleAlertStateChange(isAlert) {
      this.$emit('alert-state-change', isAlert);
    },

    /**
     * 聚焦输入框
     */
    focusInput() {
      if (this.$refs.chatContent) {
        this.$refs.chatContent.focusInput();
      }
    }
  },

  watch: {
    visible(newVal) {
      // 抽屉打开时聚焦输入框
      if (newVal) {
        this.$nextTick(() => {
          this.focusInput();
        });
      }
    }
  }
};
</script>

<style scoped>
/* 样式已通过 index.js 全局导入，这里不需要重复导入 */
</style>

<style>
/* 关闭按钮样式 */
.ai-assistant-close-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 4px;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.ai-assistant-close-btn:hover {
  background: rgba(0, 0, 0, 0.06);
}

.ai-assistant-close-icon {
  width: 20px;
  height: 20px;
  fill: var(--td-text-color-secondary, #666666);
}

/* 深色模式适配 */
[theme-mode="dark"] .ai-assistant-close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

[theme-mode="dark"] .ai-assistant-close-icon {
  fill: var(--td-text-color-secondary, #999999);
}
</style>
