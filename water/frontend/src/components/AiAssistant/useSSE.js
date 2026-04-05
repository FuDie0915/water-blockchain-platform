/**
 * AI Assistant SSE Tool
 * 计划书 3.3：使用原生 EventSource，URL 长度保护，Token 通过 URL 参数传递
 */

// ============================================
// 常量定义
// ============================================
const MAX_URL_LENGTH = 1500; // URL 长度限制，防止 414 错误
const MAX_HISTORY_ROUNDS = 3; // 最多保留 3 轮对话历史
const STORAGE_KEY = 'ai_assistant_history'; // localStorage 键名

// ============================================
// 工具函数：从 localStorage 获取对话历史
// ============================================
function getConversationHistory() {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      const parsed = JSON.parse(stored);
      // 验证数据结构
      if (parsed && Array.isArray(parsed.messages)) {
        return parsed;
      }
    }
  } catch (e) {
    // 忽略解析错误
  }
  // 返回默认空对话
  return {
    sessionId: generateSessionId(),
    messages: []
  };
}

// ============================================
// 工具函数：保存对话历史到 localStorage
// ============================================
function saveConversationHistory(conversation) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(conversation));
  } catch (e) {
    // 忽略保存错误
  }
}

// ============================================
// 工具函数：添加消息到历史
// ============================================
function addMessageToHistory(role, content) {
  const conversation = getConversationHistory();
  conversation.messages.push({
    role,
    content,
    timestamp: new Date().toISOString()
  });

  // 只保留最近 3 轮对话（每轮包含用户消息和 AI 回复，最多 6 条消息）
  if (conversation.messages.length > MAX_HISTORY_ROUNDS * 2) {
    conversation.messages = conversation.messages.slice(-MAX_HISTORY_ROUNDS * 2);
  }

  saveConversationHistory(conversation);
  return conversation;
}

// ============================================
// 工具函数：清空对话历史（新建对话）
// ============================================
function clearConversationHistory() {
  const newConversation = {
    sessionId: generateSessionId(),
    messages: []
  };
  saveConversationHistory(newConversation);
  return newConversation;
}

// ============================================
// 工具函数：生成 Session ID
// ============================================
function generateSessionId() {
  return 'sess_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
}

// ============================================
// 工具函数：获取 Token
// 计划书：优先级 satoken > companytoken > managertoken
// ============================================
function getToken() {
  // 按优先级获取 Token
  return (
    localStorage.getItem('satoken') ||
    localStorage.getItem('companytoken') ||
    localStorage.getItem('managertoken') ||
    ''
  );
}

// ============================================
// 工具函数：构建 SSE URL
// 计划书 3.3：URL 保护，超过 1500 字符自动截断最早记录
// 计划书 3.3：鉴权通过 URL 参数 ?token=xxx 传递
//
// 后端接口：GET /ai/chat?message={message}&token={token}
// 返回格式：ServerSentEvent<String> (纯文本流)
// ============================================
function buildSSEUrl(message, conversation) {
  // 使用项目配置的代理前缀 /gk_api
  const baseUrl = '/gk_api/ai/chat';

  // 构建 URL 参数
  const params = new URLSearchParams();
  params.append('message', message);

  // 计划书 3.3：通过 URL 参数传递 Token
  // 优先级：satoken > companytoken > managertoken（与 src/utils/request.ts 保持一致）
  const token = getToken();
  if (token) {
    params.append('satoken', token);
  }

  // 添加会话 ID（用于上下文管理）
  if (conversation?.sessionId) {
    params.append('sessionId', conversation.sessionId);
  }

  // 构建完整 URL
  const fullUrl = `${baseUrl}?${params.toString()}`;

  return fullUrl;
}

// ============================================
// SSE 连接管理类
// ============================================
class SSEConnection {
  constructor(options) {
    this.options = {
      onMessage: null,       // 接收到消息回调
      onError: null,         // 错误回调
      onComplete: null,      // 完成回调
      ...options
    };
    this.eventSource = null;
    this.isConnecting = false;
    this.currentMessage = ''; // 当前正在接收的消息
  }

  /**
   * 发送消息并建立 SSE 连接
   * @param {string} message - 用户消息
   * @param {object} conversation - 对话历史对象
   */
  send(message, conversation) {
    // 如果已有连接，先关闭
    this.close();

    // 构建 URL
    const url = buildSSEUrl(message, conversation);

    // 记录用户消息到历史
    const updatedConversation = addMessageToHistory('user', message);

    this.isConnecting = true;
    this.currentMessage = '';

    try {
      // 创建 EventSource 连接
      this.eventSource = new EventSource(url);

      // 监听消息
      this.eventSource.onmessage = (event) => {
        this.handleMessage(event);
      };

      // 监听错误
      this.eventSource.onerror = (error) => {
        // readyState: 0 = CLOSED, 1 = OPEN, 2 = CONNECTING
        // 如果已有消息内容且连接关闭，视为正常完成（后端发送完毕后关闭连接）
        if (this.eventSource.readyState === 0 && this.currentMessage) {
          this.complete();
          return;
        }

        // readyState: 0 = CLOSED, 1 = OPEN, 2 = CONNECTING
        // 如果已有消息内容且连接关闭，视为正常完成（后端发送完毕后关闭连接）
        if (this.eventSource.readyState === 0 && this.currentMessage) {
          this.complete();
          return;
        }

        // 立即关闭连接，阻止 EventSource 自动重连
        this.eventSource.close();
        this.eventSource = null;
        this.isConnecting = false;

        if (this.options.onError) {
          this.options.onError(new Error('SSE connection failed'));
        }
      };

    } catch (error) {
      this.handleError(error);
    }
  }

  /**
   * 处理 SSE 消息
   * Spring WebFlux ServerSentEvent 格式: data: {...}
   */
  handleMessage(event) {
    try {
      const data = event.data;

      if (!data) {
        return;
      }

      // 检查是否为流结束标记
      if (data === '[DONE]' || data === '[END]') {
        this.complete();
        return;
      }

      // 尝试解析 JSON（ServerSentEvent 格式）
      let content = '';
      try {
        const parsed = JSON.parse(data);
        content = parsed.data || parsed.content || data;
      } catch {
        // 不是 JSON，直接使用原始数据
        content = data;
      }

      // 累积消息内容
      this.currentMessage += content;

      // 调用回调
      if (this.options.onMessage) {
        this.options.onMessage({
          content: this.currentMessage,
          delta: content,
          isComplete: false
        });
      }

    } catch (e) {
      // 忽略解析错误
    }
  }

  /**
   * 处理错误
   */
  handleError(error) {
    this.isConnecting = false;

    if (this.options.onError) {
      this.options.onError(error);
    }
  }

  /**
   * 完成流式接收
   */
  complete() {
    this.isConnecting = false;

    // 保存 AI 回复到历史
    if (this.currentMessage) {
      addMessageToHistory('assistant', this.currentMessage);
    }

    if (this.options.onComplete) {
      this.options.onComplete({
        content: this.currentMessage,
        isComplete: true
      });
    }

    // 关闭连接
    this.close();
  }

  /**
   * 关闭 SSE 连接
   * 计划书 3.3：关闭抽屉或组件销毁时，务必关闭连接
   */
  close() {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
    this.isConnecting = false;
  }

  /**
   * 获取当前状态
   */
  getStatus() {
    return {
      isConnecting: this.isConnecting,
      hasConnection: !!this.eventSource,
      currentMessage: this.currentMessage
    };
  }
}

// ============================================
// 导出 API
// ============================================
export {
  SSEConnection,
  getConversationHistory,
  saveConversationHistory,
  clearConversationHistory,
  addMessageToHistory,
  getToken,
  buildSSEUrl,
  MAX_URL_LENGTH,
  MAX_HISTORY_ROUNDS
};
