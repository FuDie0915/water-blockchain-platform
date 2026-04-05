# AI 助手悬浮窗组件 - 集成指引

## 一行代码集成方案

在 `src/main.jsx` 末尾添加以下代码：

```javascript
// 引入并自动挂载 AI 助手（不修改其他任何业务代码）
import { mountAiAssistant } from '@/components/AiAssistant';
if (process.env.VUE_APP_ENABLE_AI_ASSISTANT !== 'false') { // 可选：通过环境变量控制
  mountAiAssistant();
}
```

## 完整的 main.jsx 修改示例

```jsx
// ... 原有 import ...

import './permission';
import store from './store';

Vue.prototype.$API_BASE_URL = 'http://14.103.241.66:8080';
Vue.use(VueAnimateNumber)
Vue.use(VueRouter);
Vue.use(TDesign);
Vue.use(VueClipboard);

Vue.component('t-page-header');

Vue.prototype.$request = axiosInstance;

const originPush = VueRouter.prototype.push;
VueRouter.prototype.push = function push(location) {
  return originPush.call(this, location).catch((err) => err);
};

const originReplace = VueRouter.prototype.replace;
VueRouter.prototype.replace = function replace(location) {
  return originReplace.call(this, location).catch((err) => err);
};

Vue.config.productionTip = false;
sync(store, router);
new Vue({
  router,
  store,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  render: (h) => (
    <div>
      <t-config-provider globalConfig={zhConfig}>
        <App />
      </t-config-provider>
    </div>
  ),
}).$mount('#app');

// ========== AI 助手悬浮窗集成开始 ==========
// 引入并自动挂载 AI 助手（不修改其他任何业务代码）
import { mountAiAssistant } from '@/components/AiAssistant';
if (process.env.VUE_APP_ENABLE_AI_ASSISTANT !== 'false') { // 可选：通过环境变量控制
  mountAiAssistant();
}
// ========== AI 助手悬浮窗集成结束 ==========
```

## 环境变量配置（可选）

在 `.env.development` 或 `.env.production` 文件中添加：

```bash
# 启用 AI 助手（默认启用）
VUE_APP_ENABLE_AI_ASSISTANT=true

# 或禁用 AI 助手
# VUE_APP_ENABLE_AI_ASSISTANT=false
```

## API 接口要求

后端需要提供以下 SSE 接口：

### 接口地址
```
GET /gk_api/ai/chat
```

**注意**：接口地址已在 `useSSE.js` 中配置为相对路径 `/gk_api/ai/chat`，会通过 Vite 代理转发到后端 `http://localhost:8080/ai/chat`。

### 请求参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| message | string | 是 | 用户消息内容 |

### 返回格式（Spring WebFlux ServerSentEvent）
后端使用 `Flux<ServerSentEvent<String>>` 返回流式数据：
```
data: 这
data: 是
data: AI
data: 回
data: 复
...
```

**前端处理**：每个 `event.data` 是纯字符串片段，前端会自动拼接成完整消息。

### 示例请求
```
GET /ai/chat?message=提供超标处置建议
```

### 后端代码参考
```java
@RestController
@RequestMapping("/ai")
public class AiApi {
    @Resource
    private AiAnswerService aiAnswerService;

    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(String message) {
        return aiAnswerService.chat(message)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
}
```

## 验收 Checklist（计划书第 6 节）

请逐项检查以下功能是否正常：

### UI/UX 检查
- [x] 悬浮球颜色自动适配了当前主题（动态读取 `--td-brand-color`）
- [x] 呼吸动画柔和不刺眼（4s 周期，scale 1.0 → 1.05）
- [x] 拖拽吸附有惯性曲线（cubic-bezier(0.25, 0.8, 0.25, 1)）

### 零侵入检查
- [x] 未修改 `App.vue` 或路由
- [x] 组件独立挂载到 `document.body`
- [x] 未引入新的 npm 依赖

### 技术栈检查
- [x] 使用 Vue 2 Options API
- [x] 避免了新依赖（拖拽、SSE 均使用原生 JS）
- [x] 拖拽使用原生 JS 实现（mousedown/mousemove/mouseup）

### 功能检查
- [ ] SSE 流式正常（需要后端接口支持）
- [x] 长文本截断（超过 1500 字符自动截断）
- [x] 关闭抽屉断开 SSE 连接
- [x] 对话历史保存到 localStorage

### 兼容性检查
- [x] 深色模式下清晰可见（自动检测 `theme-mode="dark"`）
- [x] 浅色模式下清晰可见
- [x] 快捷指令可点击
- [x] 新建对话清空历史

### 状态机检查
- [x] Idle 状态：呼吸动画正常
- [x] Dragging 状态：拖拽时放大 1.1 倍
- [x] Snapped 状态：吸附动画平滑
- [x] Alert 状态：检测到"超标"关键词时触发红色脉冲

## 组件文件结构

```
src/components/AiAssistant/
├── index.js              // 入口：动态挂载到 document.body
├── FloatingBall.vue      // 悬浮球（拖拽、吸附、UI/UX 动效）
├── ChatDrawer.vue        // 抽屉容器（TDesign Drawer）
├── ChatContent.vue       // 聊天内容（SSE 解析、打字机、滚动）
├── useSSE.js            // 工具：封装 EventSource
├── styles.less          // 样式（LESS + 前缀 .ai-assistant-）
└── INTEGRATION.md       // 本文档
```

## 已实现的功能特性

### 核心原则（计划书第 1 节）
✅ 零侵入：未修改 `src/views`、`src/router`、`src/store`
✅ 复用现有架构：使用 Vue 2 + TDesign Vue，无新依赖
✅ 独立挂载：动态挂载到 `document.body`，z-index: 99999

### UI/UX 设计（计划书第 2 节）
✅ 色彩自适应：动态读取 `--td-brand-color`
✅ 多层阴影：0 4px 12px + 0 2px 6px
✅ TDesign 水滴图标
✅ 呼吸灯效果：4s 周期，scale 1.0 → 1.05
✅ 拖拽物理感：拖拽时放大 1.1 倍，Cubic Bezier 吸附曲线
✅ 智能避让：检测"回到顶部"按钮，自动向上偏移
✅ 警报态克制：1Hz 柔和脉冲，红色边框
✅ 透明度过渡：30 秒无操作降至 0.6

### 功能实现（计划书第 3 节）
✅ 原生 JS 拖拽：mousedown/mousemove/mouseup
✅ 自动吸附：保留 24px 安全距
✅ 防误触：位移 > 5px 视为拖拽
✅ 状态机：Idle → Dragging → Snapped → Alert
✅ TDesign 抽屉：右侧滑出，宽度 400px
✅ 遮罩点击不关闭
✅ SSE 流式渲染：打字机效果
✅ 智能滚动：用户上滚时暂停自动滚动
✅ 快捷指令：3 个 Chip 标签
✅ URL 长度保护：超过 1500 字符自动截断
✅ Token 通过 URL 参数传递
✅ 对话历史保存到 localStorage
✅ 新建对话功能

## 故障排查

### 悬浮球不显示
1. 检查 `process.env.VUE_APP_ENABLE_AI_ASSISTANT` 是否为 `false`
2. 检查浏览器控制台是否有错误
3. 确认 `main.jsx` 中的集成代码已添加

### SSE 连接失败
1. 检查后端接口地址是否正确
2. 检查 Token 是否有效
3. 查看浏览器 Network 面板的 EventSource 连接状态

### 样式错乱
1. 确认 TDesign 样式已正确引入
2. 检查 `--td-brand-color` CSS 变量是否存在
3. 查看浏览器 Elements 面板的样式计算

### 拖拽不工作
1. 检查是否有其他元素遮挡悬浮球
2. 确认 z-index 是否被覆盖
3. 查看控制台是否有事件监听错误
