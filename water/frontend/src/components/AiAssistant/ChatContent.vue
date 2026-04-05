<template>
  <div class="ai-assistant-chat-content">
    <!-- 快捷指令 Chips -->
    <!-- 计划书 3.2：首次打开显示 3 个 Chip 标签 -->
    <div v-if="messages.length === 0" class="ai-assistant-quick-actions">
      <div
        v-for="(action, index) in quickActions"
        :key="index"
        class="ai-assistant-chip"
        @click="handleQuickAction(action)"
      >
        {{ action }}
      </div>
    </div>

    <!-- 消息列表 -->
    <div ref="messagesContainer" class="ai-assistant-messages">
      <!-- 欢迎/空状态消息 -->
      <div
        v-if="messages.length === 0"
        class="ai-assistant-message ai-assistant-message-ai"
      >
        <div class="ai-assistant-message-avatar ai-assistant-avatar-ai">
          <svg class="ai-assistant-avatar-icon" viewBox="0 0 24 24">
            <path d="M12 2c-5.33 5.33-8 8.67-8 12a8 8 0 1 0 16 0c0-3.33-2.67-6.67-8-12z"/>
          </svg>
        </div>
        <div class="ai-assistant-message-bubble ai-assistant-bubble-ai">
          你好！我是水环境监管 AI 助手，可以帮你分析水质数据、提供处置建议。请问有什么可以帮助你？
        </div>
      </div>

      <!-- 历史消息 -->
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="[
          'ai-assistant-message',
          msg.role === 'user' ? 'ai-assistant-message-user' : 'ai-assistant-message-ai'
        ]"
      >
        <div
          :class="[
            'ai-assistant-message-avatar',
            msg.role === 'user' ? 'ai-assistant-avatar-user' : 'ai-assistant-avatar-ai'
          ]"
        >
          <svg v-if="msg.role === 'user'" class="ai-assistant-avatar-icon" viewBox="0 0 24 24">
            <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
          </svg>
          <svg v-else class="ai-assistant-avatar-icon" viewBox="0 0 24 24">
            <path d="M12 2c-5.33 5.33-8 8.67-8 12a8 8 0 1 0 16 0c0-3.33-2.67-6.67-8-12z"/>
          </svg>
        </div>
        <div
          :class="[
            'ai-assistant-message-bubble',
            msg.role === 'user' ? 'ai-assistant-bubble-user' : 'ai-assistant-bubble-ai'
          ]"
        >
          {{ msg.content }}
        </div>
      </div>

      <!-- 正在输入的 AI 消息（打字机效果） -->
      <!-- 计划书 3.2：SSE 流式渲染，逐字打字机效果 -->
      <div
        v-if="streamingMessage"
        class="ai-assistant-message ai-assistant-message-ai"
      >
        <div class="ai-assistant-message-avatar ai-assistant-avatar-ai">
          <svg class="ai-assistant-avatar-icon" viewBox="0 0 24 24">
            <path d="M12 2c-5.33 5.33-8 8.67-8 12a8 8 0 1 0 16 0c0-3.33-2.67-6.67-8-12z"/>
          </svg>
        </div>
        <div class="ai-assistant-message-bubble ai-assistant-bubble-ai">
          {{ displayedMessage }}
          <span v-if="!isStreamingComplete" class="ai-assistant-typing-cursor"></span>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="isLoading" class="ai-assistant-message ai-assistant-message-ai">
        <div class="ai-assistant-message-avatar ai-assistant-avatar-ai">
          <svg class="ai-assistant-avatar-icon" viewBox="0 0 24 24">
            <path d="M12 2c-5.33 5.33-8 8.67-8 12a8 8 0 1 0 16 0c0-3.33-2.67-6.67-8-12z"/>
          </svg>
        </div>
        <div class="ai-assistant-loading">
          <div class="ai-assistant-loading-dot"></div>
          <div class="ai-assistant-loading-dot"></div>
          <div class="ai-assistant-loading-dot"></div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="ai-assistant-input-area">
      <div class="ai-assistant-input-wrapper">
        <textarea
          ref="inputTextarea"
          v-model="inputText"
          class="ai-assistant-input"
          placeholder="输入问题..."
          rows="1"
          @input="autoResize"
          @keydown.enter.exact.prevent="handleSend"
          @keydown.enter.shift.exact="inputText += '\n'"
        ></textarea>
        <button
          class="ai-assistant-send-btn"
          :disabled="!inputText.trim() || isLoading || isStreaming"
          @click="handleSend"
        >
          <svg class="ai-assistant-send-icon" viewBox="0 0 24 24">
            <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { SSEConnection, getConversationHistory, clearConversationHistory } from './useSSE';

export default {
  name: 'AiAssistantChatContent',

  props: {
    // 新建对话回调
    onNewChat: {
      type: Function,
      default: null
    },
    // 警报状态回调
    onAlertStateChange: {
      type: Function,
      default: null
    }
  },

  data() {
    return {
      // 快捷指令（计划书 3.2）
      quickActions: [
        '提供水质处置建议',
        '分析氨氮突变原因',
        '评估断面水质风险'
      ],

      // 对话历史
      messages: [],
      conversation: null,

      // 输入
      inputText: '',

      // 流式状态
      isLoading: false,
      isStreaming: false,
      isStreamingComplete: true,
      streamingMessage: '',
      displayedMessage: '',

      // 打字机动画
      typewriterTimer: null,

      // 智能滚动（计划书 3.2）
      isUserScrolling: false,
      userScrollTimeout: null,

      // SSE 连接
      sseConnection: null
    };
  },

  mounted() {
    // 加载对话历史
    this.conversation = getConversationHistory();
    this.messages = [...this.conversation.messages];

    // 监听滚动事件，实现智能滚动
    this.$nextTick(() => {
      if (this.$refs.messagesContainer) {
        this.$refs.messagesContainer.addEventListener('scroll', this.handleScroll);
      }
    });

    // 初始化 SSE 连接
    this.sseConnection = new SSEConnection({
      onMessage: this.handleSSEMessage,
      onError: this.handleSSEError,
      onComplete: this.handleSSEComplete
    });
  },

  beforeDestroy() {
    // 清理定时器
    if (this.typewriterTimer) {
      cancelAnimationFrame(this.typewriterTimer);
    }

    // 清理滚动监听
    if (this.$refs.messagesContainer) {
      this.$refs.messagesContainer.removeEventListener('scroll', this.handleScroll);
    }

    // 关闭 SSE 连接（计划书 3.3：务必关闭）
    if (this.sseConnection) {
      this.sseConnection.close();
    }
  },

  methods: {
    /**
     * 发送消息
     */
    handleSend() {
      const text = this.inputText.trim();
      if (!text || this.isLoading || this.isStreaming) {
        return;
      }

      // 添加用户消息到列表
      this.messages.push({
        role: 'user',
        content: text,
        timestamp: new Date().toISOString()
      });

      // 清空输入
      this.inputText = '';
      this.$nextTick(() => {
        this.autoResize();
      });

      // 开始流式接收
      this.isLoading = true;
      this.isStreaming = true;
      this.isStreamingComplete = false;
      this.streamingMessage = '';
      this.displayedMessage = '';

      // 发送到 SSE
      this.sseConnection.send(text, this.conversation);

      // 滚动到底部
      this.$nextTick(() => {
        this.scrollToBottom();
      });
    },

    /**
     * 快捷指令点击
     */
    handleQuickAction(action) {
      this.inputText = action;
      this.$nextTick(() => {
        this.handleSend();
      });
    },

    /**
     * SSE 消息处理（打字机效果）
     * 计划书 3.2：SSE 数据到达时，逐字打字机效果显示
     * 计划书 3.2：利用 requestAnimationFrame 平滑渲染
     */
    handleSSEMessage(data) {
      this.isLoading = false;
      this.streamingMessage = data.content;

      // 打字机动画：逐步显示内容
      this.animateTypewriter(data.content);
    },

    /**
     * 打字机动画
     */
    animateTypewriter(targetContent) {
      if (this.typewriterTimer) {
        cancelAnimationFrame(this.typewriterTimer);
      }

      let currentIndex = this.displayedMessage.length;

      const animate = () => {
        if (currentIndex < targetContent.length) {
          // 每次增加 1-3 个字符，模拟自然打字速度
          const increment = Math.floor(Math.random() * 3) + 1;
          currentIndex = Math.min(currentIndex + increment, targetContent.length);
          this.displayedMessage = targetContent.substring(0, currentIndex);

          // 智能滚动（计划书 3.2：用户上滚时暂停自动滚动）
          if (!this.isUserScrolling) {
            this.scrollToBottom();
          }

          // 继续下一帧
          this.typewriterTimer = requestAnimationFrame(animate);
        }
      };

      this.typewriterTimer = requestAnimationFrame(animate);
    },

    /**
     * SSE 完成处理
     */
    handleSSEComplete(data) {
      this.isLoading = false;
      this.isStreaming = false;
      this.isStreamingComplete = true;
      this.displayedMessage = data.content;

      // 添加到消息列表
      this.messages.push({
        role: 'assistant',
        content: data.content,
        timestamp: new Date().toISOString()
      });

      this.streamingMessage = '';

      // 警报检测（计划书 2.3：关键词检测）
      this.checkForAlert(data.content);

      // 滚动到底部
      this.$nextTick(() => {
        this.scrollToBottom();
      });
    },

    /**
     * SSE 错误处理
     */
    handleSSEError() {
      this.isLoading = false;
      this.isStreaming = false;
      this.streamingMessage = '';
    },

    /**
     * 警报检测
     * 计划书 2.3：当 AI 返回"报警/超标"信息时，触发警报
     */
    checkForAlert(content) {
      const alertKeywords = ['超标', '报警', '异常', '危险', '污染'];
      const hasAlert = alertKeywords.some(keyword => content.includes(keyword));

      if (this.onAlertStateChange) {
        this.onAlertStateChange(hasAlert);
      }
    },

    /**
     * 新建对话
     */
    handleNewChat() {
      this.conversation = clearConversationHistory();
      this.messages = [];
      this.inputText = '';
      this.streamingMessage = '';
      this.displayedMessage = '';

      // 关闭警报状态
      if (this.onAlertStateChange) {
        this.onAlertStateChange(false);
      }
    },

    /**
     * 自动调整输入框高度
     */
    autoResize() {
      this.$nextTick(() => {
        const textarea = this.$refs.inputTextarea;
        if (textarea) {
          textarea.style.height = 'auto';
          textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px';
        }
      });
    },

    /**
     * 滚动到底部
     */
    scrollToBottom() {
      const container = this.$refs.messagesContainer;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    },

    /**
     * 滚动事件处理（智能滚动）
     * 计划书 3.2：用户上滚查看历史时暂停自动滚动
     */
    handleScroll() {
      const container = this.$refs.messagesContainer;
      if (!container) return;

      // 判断是否在底部附近（50px 内）
      const isNearBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 50;

      if (!isNearBottom) {
        // 用户向上滚动，暂停自动滚动
        this.isUserScrolling = true;

        // 清除之前的定时器
        if (this.userScrollTimeout) {
          clearTimeout(this.userScrollTimeout);
        }

        // 3 秒后恢复自动滚动
        this.userScrollTimeout = setTimeout(() => {
          this.isUserScrolling = false;
        }, 3000);
      } else {
        // 用户滚动到底部，恢复自动滚动
        this.isUserScrolling = false;
      }
    },

    /**
     * 外部调用：聚焦输入框
     */
    focusInput() {
      this.$nextTick(() => {
        if (this.$refs.inputTextarea) {
          this.$refs.inputTextarea.focus();
        }
      });
    }
  }
};
</script>

<style scoped>
/* 样式已通过 index.js 全局导入，这里不需要重复导入 */
</style>
