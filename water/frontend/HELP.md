# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于 TDesign Vue Starter 的 Vue 2 + Vite 前端项目，主要用于水质监测、海洋监测和无人机数据的可视化展示，包含区块链浏览器功能。

### 技术栈
- **框架**: Vue 2.6.14 + Vue Router + Vuex
- **构建工具**: Vite 4.1.4
- **UI 组件库**: TDesign Vue 1.9.0
- **语言**: TypeScript 5.1.6 + JavaScript
- **样式**: Less
- **图表**: ECharts 5.1.2
- **HTTP 客户端**: Axios 1.1.3

---

## 开发命令

### 基础开发
```bash
npm install              # 安装依赖
npm run dev              # 启动开发服务器（自动打开浏览器）
npm run dev:mock         # 使用 Mock 数据启动
npm run dev:linux        # Linux 环境启动（不自动打开浏览器）
```

### 构建命令
```bash
npm run build            # 生产环境构建
npm run build:test       # 测试环境构建
npm run build:site       # 站点构建
```

### 代码质量
```bash
npm run lint             # ESLint 检查
npm run lint:fix         # ESLint 自动修复
npm run stylelint        # 样式检查
npm run stylelint:fix    # 样式自动修复
```

### Git 提交
项目使用 Husky + Commitizen + Commitlint 规范提交信息：
```bash
git commit               # 会触发交互式提交界面
```

---

## 环境配置

### API 代理配置
开发环境下，API 请求会通过 Vite 代理转发到后端服务器。配置位于 [vite.config.js](vite.config.js)：

```javascript
server: {
  host: '0.0.0.0',
  port: 3001,
  proxy: {
    '/gk_api': {
      target: 'http://14.103.241.66:8080',
      rewrite: (path) => path.replace(/^\/gk_api/, ''),
      changeOrigin: true,
    },
    '/api': {
      target: 'http://14.103.241.66:8080',
      rewrite: (path) => path.replace(/^\/api/, ''),
      changeOrigin: true,
    },
  },
}
```

**注意**: 硬编码的 API 地址 `http://14.103.241.66:8080` 同时存在于：
- [src/main.jsx](src/main.jsx:21) - `Vue.prototype.$API_BASE_URL`
- [vite.config.js](vite.config.js:12)

### 环境模式
- `mock` - 使用本地 Mock 数据
- `development` - 开发环境（默认）
- `test` - 测试环境
- `release` - 生产环境
- `site` - 站点环境

---

## 认证与权限模块

### Token 管理
项目使用自定义的 `satoken` 进行身份验证，存储在 `localStorage` 中：

**相关文件**:
- [src/config/global.ts](src/config/global.ts:2) - Token 名称定义
- [src/utils/request.ts](src/utils/request.ts:44) - Token 拦截器
- [src/permission.js](src/permission.js) - 路由权限控制

### 多 Token 支持
系统支持三种 Token 类型：
1. **satoken** - 普通用户 Token
2. **companytoken** - 企业用户 Token
3. **managertoken** - 监管局 Token

Token 在请求拦截器中被添加到请求头：
```javascript
// src/utils/request.ts
if (token) {
  config.headers['satoken'] = token;
}
if (companyToken) {
  config.headers['companytoken'] = companyToken;
}
if (managerToken) {
  config.headers['managertoken'] = managerToken;
}
```

### 路由权限控制
权限系统位于 [src/store/modules/permission.ts](src/store/modules/permission.ts)：

1. **白名单路由** - 无需认证即可访问：
   - `/login` - 登录页
   - `/403` - 权限不足页

2. **角色权限过滤**：
   - `admin` 角色：拥有所有路由权限
   - 其他角色：根据 `meta.roleCode` 或路由 `name` 过滤

3. **权限初始化流程**（[src/permission.js](src/permission.js:39)）：
   
   ```
   检查 Token → 获取用户信息 → 初始化路由 → 放行
   ```

### URL Token 解析
系统支持从 URL 中提取 Token（用于外部系统跳转）：
```javascript
// src/permission.js:17
if (to.fullPath.includes('/dashboard/base?satoken=')) {
  urlToken = to.fullPath.split('satoken=')[1].split('&')[0];
  store.commit('user/setToken', urlToken);
}
```

---

## 主题与配置模块

### 动态主题系统
主题管理位于 [src/store/modules/setting.ts](src/store/modules/setting.ts)：

**支持的主题模式**：
- `light` - 浅色模式
- `dark` - 深色模式
- `auto` - 跟随系统设置

**布局模式**：
- `side` - 侧边栏布局
- `top` - 顶部导航布局
- `mix` - 混合布局

**主题切换** 通过修改 `document.documentElement` 属性实现：

```javascript
document.documentElement.setAttribute('theme-mode', 'dark');
document.documentElement.setAttribute('theme-color', brandTheme);
```

### 多标签页路由
标签页管理位于 [src/store/modules/tab-router.ts](src/store/modules/tab-router.ts)：
- 标签页列表会持久化到 `localStorage`
- 支持标签页的动态添加和缓存
- 布局组件 [src/layouts/index.vue](src/layouts/index.vue:64) 监听路由变化自动添加标签

---

## 业务模块架构

### 模块化路由
业务路由按功能模块拆分，位于 [src/router/modules/](src/router/modules/)：

| 文件 | 功能 | 路由前缀 |
|------|------|----------|
| [base.ts](src/router/modules/base.ts) | 区块链浏览器 | `/dashboard` |
| [water.ts](src/router/modules/water.ts) | 水质监测 | `/water` |
| [sea.ts](src/router/modules/sea.ts) | 海洋监测 | `/sea` |
| [uav.ts](src/router/modules/uav.ts) | 无人机监测 | `/uav` |
| [components.ts](src/router/modules/components.ts) | 组件示例 | - |
| [others.ts](src/router/modules/others.ts) | 其他页面 | - |

### 动态路由加载
路由在 [src/router/index.js](src/router/index.js:14) 中合并：
```javascript
export const asyncRouterList = [ ...baseRouters, ...waterRouters];
```

**注意**: 目前 `seaRouters` 和 `uavRouters` 虽然已导入，但未加入 `asyncRouterList`，需要手动添加才能访问。

### 海水质监测模块
水质监测模块（[src/pages/water/](src/pages/water/)）包含三个页面：
- `index.vue` - 海水质实训主页
- `enterprise.vue` - 企业登录页
- `monitor.vue` - 监管局登录页

---

## API 通信层

### Axios 配置
HTTP 客户端配置位于 [src/utils/request.ts](src/utils/request.ts)：

**基础配置**：
- 基础 URL：根据环境从 [src/config/host.ts](src/config/host.ts) 读取
- 超时时间：8000ms
- 携带凭证：`withCredentials: true`
- 重试次数：3 次

### 请求拦截器
1. **Token 注入** - 自动添加三种 Token 到请求头
2. **GET 参数处理** - 将 `params` 序列化为查询字符串并附加到 URL

### 响应拦截器
1. **文件上传特殊处理** - 对 `/common/upload` 接口返回格式进行转换
2. **统一错误处理**：
   - `40100` 错误码：清除 Token 并跳转到 `/403`
   - 其他错误：显示错误提示

### API 服务模块
API 服务按业务模块组织在 [src/service/](src/service/)：
- [service-user.ts](src/service/service-user.ts) - 用户相关
- [service-base.ts](src/service/service-base.ts) - 基础服务
- [service-detail.ts](src/service/service-detail.ts) - 详情服务
- [service-detail-deploy.ts](src/service/service-detail-deploy.ts) - 部署相关
- [service-detail-base.ts](src/service/service-detail-base.ts) - 详情基础
- [service-advance.ts](src/service/service-advance.ts) - 高级服务

---

## 状态管理

### Vuex Store 结构
主 Store 位于 [src/store/index.ts](src/store/index.ts)，包含以下模块：

| 模块 | 文件 | 功能 |
|------|------|------|
| `user` | [src/store/modules/user.ts](src/store/modules/user.ts) | 用户信息、Token 管理 |
| `setting` | [src/store/modules/setting.ts](src/store/modules/setting.ts) | 主题、布局配置 |
| `permission` | [src/store/modules/permission.ts](src/store/modules/permission.ts) | 路由权限 |
| `notification` | [src/store/modules/notification.ts](src/store/modules/notification.ts) | 通知消息 |
| `tabRouter` | [src/store/modules/tab-router.ts](src/store/modules/tab-router.ts) | 多标签页管理 |

### 用户状态管理
用户模块（[src/store/modules/user.ts](src/store/modules/user.ts)）提供：
- `login` - 登录并设置 Token
- `getUserInfo` - 获取用户信息
- `logout` - 退出登录并清除状态

API 定义在 [src/store/api/user.js](src/store/api/user.js)：
- 登录接口：`/gk_api/user/loginOrRegister`
- 获取用户信息：`/gk_api/user/getLoginInfoByToken/{token}`
- 退出登录：`/gk_api/user/logout/{token}`

---

## 组件结构

### 布局组件
主布局位于 [src/layouts/](src/layouts/)：
- [index.vue](src/layouts/index.vue) - 主布局容器，根据配置切换布局模式
- [components/LayoutHeader.vue](src/layouts/components/LayoutHeader.vue) - 顶部导航栏
- [components/LayoutSidebar.vue](src/layouts/components/LayoutSidebar.vue) - 侧边栏
- [components/LayoutContent.vue](src/layouts/components/LayoutContent.vue) - 内容区域
- [components/MenuContent.vue](src/layouts/components/MenuContent.vue) - 菜单内容
- [components/Breadcrumb.vue](src/layouts/components/Breadcrumb.vue) - 面包屑导航
- [components/Notice.vue](src/layouts/components/Notice.vue) - 通知组件
- [components/Search.vue](src/layouts/components/Search.vue) - 搜索组件
- [setting.vue](src/layouts/setting.vue) - 主题设置面板

### 通用组件
- [src/components/color/](src/components/color/) - 颜色选择组件
- [src/components/product-card/](src/components/product-card/) - 产品卡片组件
- [src/components/result/](src/components/result/) - 结果页面组件
- [src/components/thumbnail/](src/components/thumbnail/) - 缩略图组件
- [src/components/AiFloatingWindow/](src/components/AiFloatingWindow/) - AI 悬浮窗组件

### 页面组件
页面组件按功能组织在 [src/pages/](src/pages/)：
- `dashboard/` - 仪表盘（区块链浏览器）
- `water/` - 水质监测
- `sea/` - 海洋监测
- `uav/` - 无人机监测
- `login/` - 登录页
- `result/` - 各种结果页（404、500、403 等）
- `list/` - 列表页示例
- `form/` - 表单页示例
- `detail/` - 详情页示例
- `user/` - 用户页示例

---

## 重要配置文件

| 文件 | 用途 |
|------|------|
| [.eslintrc](.eslintrc) | ESLint 规则配置（基于 Airbnb，TypeScript + Vue） |
| [.prettierrc.js](.prettierrc.js) | Prettier 代码格式化配置 |
| [commitlint.config.js](commitlint.config.js) | Git 提交信息规范检查 |
| [vite.config.js](vite.config.js) | Vite 构建配置 |
| [tsconfig.json](tsconfig.json) | TypeScript 配置（如存在） |
| [src/config/style.ts](src/config/style.ts) | 默认样式配置 |
| [src/config/color.ts](src/config/color.ts) | 颜色主题配置 |

---

## 开发注意事项

### 文件扩展名
- TypeScript 文件使用 `.ts` 扩展名
- Vue 组件使用 `.vue` 扩展名
- JavaScript 文件使用 `.js` 扩展名
- 部分配置和路由文件使用 `.js` 而非 `.ts`

### 组件命名
- Vue 组件文件名使用 kebab-case（如 `layout-header.vue`）
- 组件在模板中使用 kebab-case（如 `<layout-header />`）

### 类型定义
- 全局类型定义位于 [src/interface.ts](src/interface.ts)
- 组件 props 可以使用 TypeScript 类型定义
- 使用 JSDoc 注释为 JavaScript 添加类型信息

### API 请求规范
- 使用 `src/utils/request.ts` 导出的 axios 实例
- API 服务函数统一放在 `src/service/` 目录
- 响应拦截器已处理统一错误，无需在调用处重复处理

---

## 常见问题

### 如何添加新的业务模块？
1. 在 `src/router/modules/` 创建新的路由文件
2. 在 `src/router/index.js` 中导入并添加到 `asyncRouterList`
3. 在 `src/pages/` 创建对应的页面组件
4. 如需权限控制，在路由 meta 中配置 `roleCode`

### 如何修改 API 地址？
修改以下文件中的 API 地址：
- `vite.config.js` 中的 `target`
- `src/main.jsx` 中的 `Vue.prototype.$API_BASE_URL`

### 如何添加新的主题色？
修改 `src/config/color.ts` 中的颜色配置，主题系统会自动生成渐变色。

### 如何调试权限问题？
1. 检查 `src/permission.js` 中的路由守卫逻辑
2. 查看控制台输出的 `roles` 信息
3. 确认路由配置中的 `meta.roleCode` 与用户角色匹配

---

## AI 悬浮窗组件

### 组件位置
- 主组件：[src/components/AiFloatingWindow/index.vue](src/components/AiFloatingWindow/index.vue)
- 对话窗口：[src/components/AiFloatingWindow/AiChatDialog.vue](src/components/AiFloatingWindow/AiChatDialog.vue)
- API 工具：[src/components/AiFloatingWindow/utils/api.js](src/components/AiFloatingWindow/utils/api.js)

### 功能说明
- 可拖动的悬浮球，位置自动保存到 localStorage
- 点击悬浮球展开 AI 对话窗口
- 支持快捷提问和自由输入
- 根据用户类型（监管局/企业/公众）提供不同快捷问题
- 自动携带对应 Token 调用后端接口
- 响应式设计，支持移动端全屏显示
- 键盘快捷键支持（Esc 关闭对话框）

### 使用方式
组件已在主布局（[src/layouts/index.vue](src/layouts/index.vue)）中全局注册，无需手动引入。

### API 接口
- 后端接口：`GET /ai/chat`
- 请求参数：`question` (query string), `sessionId` (可选)
- 返回值：String (AI 的回答)
- Token 自动从 localStorage 获取（satoken/companytoken/managertoken）

### 样式定制
使用项目 TDesign CSS 变量，自动适配浅色/深色主题。
