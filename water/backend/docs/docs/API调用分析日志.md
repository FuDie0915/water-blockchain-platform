# 海水养殖区块链水质监管平台 — 前后端API调用分析日志

> 分析日期：2026-04-25
> 分析范围：前端API调用代码 vs 后端API接口文档

---

## 一、分析概述

### 1.1 后端API统计

| 模块 | 接口数量 | 控制器 |
|------|---------|--------|
| 用户管理 | 17 | UserApi |
| 监管局审核管理 | 6 | ManagerAuditApi |
| 绑定管理 | 10 | BindApi |
| 养殖池管理 | 10 | PondApi |
| 苗种投放 | 9 | SeedApi |
| 投喂记录 | 9 | FeedApi |
| 用药记录 | 9 | MedicineApi |
| 收获记录 | 9 | HarvestApi |
| 水质数据监管 | 22 | WaterApi |
| 公告管理 | 5 | AnnouncementApi |
| 通知管理 | 5 | NotificationApi |
| 数据看板 | 3 | DashboardApi |
| 用户合约 | 3 | UserContractApi |
| AI聊天 | 1 | AiApi |
| 公共模块 | 2 | CommonApi |
| **合计** | **119** | -- |

### 1.2 前端API文件分布

| 文件路径 | 功能模块 |
|----------|----------|
| `src/api/water/user.js` | 用户管理 |
| `src/api/water/bind.js` | 绑定管理 |
| `src/api/water/pond.js` | 养殖池管理 |
| `src/api/water/seed.js` | 苗种投放 |
| `src/api/water/feed.js` | 投喂记录 |
| `src/api/water/medicine.js` | 用药记录 |
| `src/api/water/harvest.js` | 收获记录 |
| `src/api/water/water.js` | 水质数据监管 |
| `src/api/water/announcement.js` | 公告管理 |
| `src/api/water/notification.js` | 通知管理 |
| `src/api/water/dashboard.js` | 数据看板 |
| `src/api/water/managerAudit.js` | 监管局审核 |
| `src/api/common/common.js` | 公共模块/用户合约 |

---

## 二、API调用对照分析

### 2.1 用户管理模块 (UserApi)

| 后端接口 | 前端调用 | 状态 | 备注 |
|----------|----------|------|------|
| `GET /user/captcha` | `getCaptcha()` | ✅ 已调用 | |
| `POST /user/register/farmers` | `registerFarmer()` | ✅ 已调用 | |
| `POST /user/register/manager` | `registerManager()` | ✅ 已调用 | |
| `POST /user/login/admin` | `loginAdmin()` | ✅ 已调用 | |
| `POST /user/login/farmers` | `loginFarmer()` | ✅ 已调用 | |
| `POST /user/login/manager` | `loginManager()` | ✅ 已调用 | |
| `POST /user/updateProfile` | `updateProfile()` | ✅ 已调用 | |
| `GET /user/admin/list` | `getAdminUserList()` | ✅ 已调用 | |
| `POST /user/admin/updateStatus` | `updateUserStatus()` | ✅ 已调用 | |
| `POST /user/admin/updateUser` | `adminUpdateUser()` | ✅ 已调用 | |
| `POST /user/admin/createUser` | `adminCreateUser()` | ✅ 已调用 | |
| `POST /user/admin/deleteUser` | `adminDeleteUser()` | ✅ 已调用 | ⚠️ 参数通过params传递，正确 |
| `POST /user/admin/batchUpdateStatus` | `batchUpdateStatus()` | ✅ 已调用 | |
| `POST /user/admin/batchDelete` | `batchDeleteUsers()` | ✅ 已调用 | |
| `GET /user/getLoginInfoByToken/{token}` | `getLoginInfoByToken()` | ✅ 已调用 | |
| `GET /user/logout/{token}` | `logout()` | ✅ 已调用 | |
| `GET /user/admin/export` | `exportUserList()` | ✅ 已调用 | |

**结论：用户管理模块API调用完整，无缺失。**

---

### 2.2 绑定管理模块 (BindApi)

| 后端接口 | 前端调用 | 状态 | 备注 |
|----------|----------|------|------|
| `GET /bind/managers` | `getManagerList()` | ✅ 已调用 | |
| `POST /bind/apply` | `applyBind()` | ✅ 已调用 | |
| `GET /bind/status` | `getBindStatus()` | ✅ 已调用 | |
| `GET /bind/manager/list` | `getManagerBindList()` | ✅ 已调用 | |
| `POST /bind/manager/approve` | `managerApproveBind()` | ✅ 已调用 | ⚠️ 参数通过params传递，正确 |
| `POST /bind/manager/reject` | `managerRejectBind()` | ✅ 已调用 | ⚠️ 参数通过params传递，正确 |
| `GET /bind/admin/list` | `getAdminBindList()` | ✅ 已调用 | |
| `POST /bind/unbind` | `unbind()` | ✅ 已调用 | |
| `POST /bind/manager/unbind` | `managerUnbind()` | ✅ 已调用 | ⚠️ 参数通过params传递，正确 |
| `POST /bind/manager/upChain` | `managerUpChainBind()` | ✅ 已调用 | ⚠️ 参数通过params传递，正确 |

**结论：绑定管理模块API调用完整，无缺失。**

---

### 2.3 养殖池管理模块 (PondApi)

| 后端接口 | 前端调用 | 状态 | 备注 |
|----------|----------|------|------|
| `POST /pond/create` | `createPond()` | ✅ 已调用 | |
| `GET /pond/list` | `getPondList()` | ✅ 已调用 | |
| `POST /pond/update` | `updatePond()` | ✅ 已调用 | |
| `POST /pond/update/status` | `updatePondStatus()` | ✅ 已调用 | |
| `POST /pond/delete` | `deletePond()` | ✅ 已调用 | ⚠️ 参数通过params传递，正确 |
| `GET /pond/manager/list` | `getManagerPondList()` | ✅ 已调用 | |
| `POST /pond/manager/audit` | `managerAuditPond()` | ✅ 已调用 | |
| `POST /pond/manager/batchAudit` | `managerBatchAuditPond()` | ✅ 已调用 | |
| `GET /pond/manager/export` | `exportPondList()` | ✅ 已调用 | |
| `POST /pond/manager/upChain` | `managerUpChainPond()` | ✅ 已调用 | |

**结论：养殖池管理模块API调用完整，无缺失。**

---

### 2.4 苗种投放模块 (SeedApi)

| 后端接口 | 前端调用 | 状态 | 备注 |
|----------|----------|------|------|
| `POST /seed/create` | `createSeed()` | ✅ 已调用 | |
| `GET /seed/list` | `getSeedList()` | ✅ 已调用 | |
| `POST /seed/update` | `updateSeed()` | ✅ 已调用 | |
| `POST /seed/delete` | `deleteSeed()` | ✅ 已调用 | ⚠️ 参数通过params传递，正确 |
| `GET /seed/manager/list` | `getManagerSeedList()` | ✅ 已调用 | |
| `POST /seed/manager/audit` | `managerAuditSeed()` | ✅ 已调用 | |
| `POST /seed/manager/batchAudit` | `managerBatchAuditSeed()` | ✅ 已调用 | |
| `GET /seed/manager/export` | `exportSeedList()` | ✅ 已调用 | |
| `POST /seed/manager/upChain` | `managerUpChainSeed()` | ✅ 已调用 | |

**结论：苗种投放模块API调用完整，无缺失。**

---

### 2.5 投喂记录模块 (FeedApi)

| 后端接口 | 前端调用 | 状态 | 备注 |
|----------|----------|------|------|
| `POST /feed/create` | `createFeed()` | ✅ 已调用 | |
| `GET /feed/list` | `getFeedList()` | ✅ 已调用 | |
| `POST /feed/update` | `updateFeed()` | ✅ 已调用 | |
| `POST /feed/delete` | `deleteFeed()` | ✅ 已调用 | ⚠️ 参数通过params传递，正确 |
| `GET /feed/manager/list` | `getManagerFeedList()` | ✅ 已调用 | |
| `POST /feed/manager/audit` | `managerAuditFeed()` | ✅ 已调用 | |
| `POST /feed/manager/batchAudit` | `managerBatchAuditFeed()` | ✅ 已调用 | |
| `GET /feed/manager/export` | `exportFeedList()` | ✅ 已调用 | |
| `POST /feed/manager/upChain` | `managerUpChainFeed()` | ✅ 已调用 | |

**结论：投喂记录模块API调用完整，无缺失。**

---

### 2.6 用药记录模块 (MedicineApi)

| 后端接口 | 前端调用 | 状态 | 备注 |
|----------|----------|------|------|
| `POST /medicine/create` | `createMedicine()` | ✅ 已调用 | |
| `GET /medicine/list` | `getMedicineList()` | ✅ 已调用 | |
| `POST /medicine/update` | `updateMedicine()` | ✅ 已调用 | |
| `POST /medicine/delete` | `deleteMedicine()` | ✅ 已调用 | ⚠️ 参数通过params传递，正确 |
| `GET /medicine/manager/list` | `getManagerMedicineList()` | ✅ 已调用 | |
| `POST /medicine/manager/audit` | `managerAuditMedicine()` | ✅ 已调用 | |
| `POST /medicine/manager/batchAudit` | `managerBatchAuditMedicine()` | ✅ 已调用 | |
| `GET /medicine/manager/export` | `exportMedicineList()` | ✅ 已调用 | |
| `POST /medicine/manager/upChain` | `managerUpChainMedicine()` | ✅ 已调用 | |

**结论：用药记录模块API调用完整，无缺失。**

---

### 2.7 收获记录模块 (HarvestApi)

| 后端接口 | 前端调用 | 状态 | 备注 |
|----------|----------|------|------|
| `POST /harvest/create` | `createHarvest()` | ✅ 已调用 | |
| `GET /harvest/list` | `getHarvestList()` | ✅ 已调用 | |
| `POST /harvest/update` | `updateHarvest()` | ✅ 已调用 | |
| `POST /harvest/delete` | `deleteHarvest()` | ✅ 已调用 | ⚠️ 参数通过params传递，正确 |
| `GET /harvest/manager/list` | `getManagerHarvestList()` | ✅ 已调用 | |
| `POST /harvest/manager/audit` | `managerAuditHarvest()` | ✅ 已调用 | |
| `POST /harvest/manager/batchAudit` | `managerBatchAuditHarvest()` | ✅ 已调用 | |
| `GET /harvest/manager/export` | `exportHarvestList()` | ✅ 已调用 | |
| `POST /harvest/manager/upChain` | `managerUpChainHarvest()` | ✅ 已调用 | |

**结论：收获记录模块API调用完整，无缺失。**

---

### 2.8 水质数据监管模块 (WaterApi)

| 后端接口 | 前端调用 | 状态 | 备注 |
|----------|----------|------|------|
| `POST /water/step1Info` | `getStepOneData()` | ✅ 已调用 | |
| `POST /water/permission/commit` | `commitPermission()` | ✅ 已调用 | |
| `GET /water/permission/company/query` | `queryPermission()` | ✅ 已调用 | |
| `GET /water/permission/manager/query` | `queryPermissionByManager()` | ✅ 已调用 | |
| `GET /water/permission/manager/allQuery` | `queryAllPermissionByManager()` | ✅ 已调用 | |
| `POST /water/permission/delete/{certId}` | `deletePermission()` | ✅ 已调用 | |
| `POST /water/data/delete/{dataId}` | `deleteWaterData()` | ✅ 已调用 | |
| `POST /water/permission/verify` | `approvePermissionByManager()` | ✅ 已调用 | |
| `POST /water/permission/isOnChain/{id}` | `chainCompare()` | ✅ 已调用 | |
| `POST /water/data/gen` | `collectWaterGen()` | ✅ 已调用 | |
| `POST /water/data/collect` | `collectWaterData()` | ✅ 已调用 | |
| `POST /water/data/manual` | `manualReportWaterData()` | ✅ 已调用 | |
| `GET /water/data/page` | `waterInfoQuery()` | ✅ 已调用 | ⚠️ 参数名DataType首字母大写 |
| `POST /water/data/upChain` | `upChain()` | ✅ 已调用 | |
| `POST /water/threshold/create` | `createThreshold()` | ✅ 已调用 | |
| `POST /water/threshold/update` | `updateThreshold()` | ✅ 已调用 | |
| `GET /water/threshold/get` | `getThreshold()` | ✅ 已调用 | |
| `GET /water/data/manager/list` | `getManagerWaterDataList()` | ✅ 已调用 | |
| `POST /water/data/manager/audit` | `managerAuditWaterData()` | ✅ 已调用 | |
| `POST /water/data/manager/batchAudit` | `managerBatchAuditWaterData()` | ✅ 已调用 | |
| `GET /water/data/manager/export` | `exportWaterDataList()` | ✅ 已调用 | |

**结论：水质数据监管模块API调用完整，无缺失。**

---

### 2.9 监管局审核管理模块 (ManagerAuditApi)

| 后端接口 | 前端调用 | 状态 | 备注 |
|----------|----------|------|------|
| `POST /manager-audit/submit` | `submitManagerAudit()` | ✅ 已调用 | |
| `GET /manager-audit/status` | `getManagerAuditStatus()` | ✅ 已调用 | |
| `GET /manager-audit/admin/list` | `getAdminManagerAuditList()` | ✅ 已调用 | |
| `POST /manager-audit/admin/approve` | `adminApproveManagerAudit()` | ✅ 已调用 | |
| `POST /manager-audit/admin/reject` | `adminRejectManagerAudit()` | ✅ 已调用 | |
| `POST /manager-audit/admin/upChain` | `adminUpChainManagerAudit()` | ✅ 已调用 | ⚠️ 参数通过params传递，正确 |

**结论：监管局审核管理模块API调用完整，无缺失。**

---

### 2.10 公告管理模块 (AnnouncementApi)

| 后端接口 | 前端调用 | 状态 | 备注 |
|----------|----------|------|------|
| `POST /announcement/admin/publish` | `adminPublishAnnouncement()` | ✅ 已调用 | |
| `GET /announcement/admin/list` | `getAdminAnnouncementList()` | ✅ 已调用 | |
| `POST /announcement/admin/delete/{id}` | `adminDeleteAnnouncement()` | ✅ 已调用 | |
| `GET /announcement/list` | `getAnnouncementList()` | ✅ 已调用 | |
| `POST /announcement/admin/upChain` | `adminUpChainAnnouncement()` | ✅ 已调用 | ⚠️ 参数通过params传递，正确 |

**结论：公告管理模块API调用完整，无缺失。**

---

### 2.11 通知管理模块 (NotificationApi)

| 后端接口 | 前端调用 | 状态 | 备注 |
|----------|----------|------|------|
| `GET /notification/list` | `getNotificationList()` | ✅ 已调用 | |
| `GET /notification/unreadCount` | `getUnreadCount()` | ✅ 已调用 | |
| `POST /notification/read/{id}` | `markNotificationRead()` | ✅ 已调用 | |
| `POST /notification/readAll` | `markAllRead()` | ✅ 已调用 | |
| `POST /notification/delete/{id}` | `deleteNotification()` | ✅ 已调用 | |

**结论：通知管理模块API调用完整，无缺失。**

---

### 2.12 数据看板模块 (DashboardApi)

| 后端接口 | 前端调用 | 状态 | 备注 |
|----------|----------|------|------|
| `GET /dashboard/admin` | `getAdminDashboard()` | ✅ 已调用 | |
| `GET /dashboard/manager` | `getManagerDashboard()` | ✅ 已调用 | |
| `GET /dashboard/farmer` | `getFarmerDashboard()` | ✅ 已调用 | |

**结论：数据看板模块API调用完整，无缺失。**

---

### 2.13 用户合约模块 (UserContractApi)

| 后端接口 | 前端调用 | 状态 | 备注 |
|----------|----------|------|------|
| `POST /user_contract/create` | `bindContract()` | ✅ 已调用 | |
| `GET /user_contract/query` | `queryContract()` | ✅ 已调用 | |
| `POST /user_contract/delete` | `deleteContract()` | ✅ 已调用 | ⚠️ 参数通过params传递，正确 |

**结论：用户合约模块API调用完整，无缺失。**

---

### 2.14 AI聊天模块 (AiApi)

| 后端接口 | 前端调用 | 状态 | 备注 |
|----------|----------|------|------|
| `GET /ai/chat` | ❌ 未找到 | ⚠️ 未调用 | 前端未封装AI聊天API |

**问题：AI聊天模块API未在前端API文件中封装。**

---

### 2.15 公共模块 (CommonApi)

| 后端接口 | 前端调用 | 状态 | 备注 |
|----------|----------|------|------|
| `GET /common/indexData` | `getBlockchainBoard()` | ✅ 已调用 | |
| `POST /common/upload` | `uploadCommonFile()` | ✅ 已调用 | |

**结论：公共模块API调用完整，无缺失。**

---

## 三、发现的问题汇总

### 3.1 未调用的API

| 模块 | 接口 | 问题描述 |
|------|------|----------|
| AI聊天 | `GET /ai/chat` | 前端未封装对应的API函数 |

### 3.2 潜在问题

#### 3.2.1 验证码图片字段不一致

**问题描述：**
- 后端返回字段：`captchaImage`（Base64编码图片）
- 前端使用字段：`imageBase64`

**代码位置：** `src/pages/login/components/components-register.vue:152`

```javascript
// 前端代码
this.captchaImage = data?.imageBase64 || '';

// 后端返回
{
  "captchaKey": "xxx",
  "captchaImage": "data:image/png;base64,..."
}
```

**影响：** 验证码图片可能无法正确显示。

**建议修复：**
```javascript
this.captchaImage = data?.captchaImage || data?.imageBase64 || '';
```

---

#### 3.2.2 水质数据分页查询参数大小写

**问题描述：**
- 后端接口参数名：`DataType`（首字母大写D）
- 前端已正确使用：`DataType`

**代码位置：** `src/api/water/water.js:42`

```javascript
// 前端代码（正确）
params: {
  DataType: data.DataType,  // 首字母大写
  pageNum: data.pageNum,
  pageSize: data.pageSize,
}
```

**状态：** 已正确处理，无需修改。

---

#### 3.2.3 POST请求参数传递方式

**问题描述：** 部分POST接口参数通过query string传递，而非RequestBody。

**涉及接口：**
| 接口 | 前端处理 | 状态 |
|------|----------|------|
| `POST /user/admin/deleteUser` | `params: { userId }` | ✅ 正确 |
| `POST /bind/manager/approve` | `params: { id }` | ✅ 正确 |
| `POST /bind/manager/reject` | `params: { id }` | ✅ 正确 |
| `POST /bind/manager/unbind` | `params: { id }` | ✅ 正确 |
| `POST /bind/manager/upChain` | `params: { id }` | ✅ 正确 |
| `POST /pond/delete` | `params: { id }` | ✅ 正确 |
| `POST /seed/delete` | `params: { id }` | ✅ 正确 |
| `POST /feed/delete` | `params: { id }` | ✅ 正确 |
| `POST /medicine/delete` | `params: { id }` | ✅ 正确 |
| `POST /harvest/delete` | `params: { id }` | ✅ 正确 |
| `POST /announcement/admin/upChain` | `params: { id }` | ✅ 正确 |
| `POST /user_contract/delete` | `params: { contractType }` | ✅ 正确 |
| `POST /manager-audit/admin/upChain` | `params: { id }` | ✅ 正确 |

**结论：** 所有需要通过params传递的POST接口均已正确处理。

---

## 四、响应格式处理分析

### 4.1 特殊响应格式接口

| 接口 | 后端响应格式 | 前端处理 | 状态 |
|------|-------------|----------|------|
| `/seed/list` | 直接返回PageResponse | 已在request.ts中处理 | ✅ |
| `/feed/list` | 直接返回PageResponse | 已在request.ts中处理 | ✅ |
| `/medicine/list` | 直接返回PageResponse | 已在request.ts中处理 | ✅ |
| `/harvest/list` | 直接返回PageResponse | 已在request.ts中处理 | ✅ |
| `/water/data/page` | 直接返回PageResponse | 已在request.ts中处理 | ✅ |
| `/common/upload` | 返回裸Map | 已在request.ts中特殊处理 | ✅ |
| Excel导出接口 | 返回文件流 | 已设置responseType: 'blob' | ✅ |

### 4.2 request.ts响应处理逻辑

```javascript
// 文件上传特殊处理
if (config.url?.includes('/common/upload')) {
  if (data?.fileName) {
    return {
      code: SUCCESS_CODE,
      data,
      message: '上传成功',
    };
  }
}

// 无code字段直接返回
if (typeof data?.code === 'undefined') {
  return data;
}
```

**结论：** 响应格式处理完善，已覆盖所有特殊情况。

---

## 五、Token认证机制分析

### 5.1 Token存储

```javascript
// 存储的Token类型
localStorage.setItem('satoken', token);        // 主Token
localStorage.setItem('farmertoken', token);    // 养殖户Token
localStorage.setItem('managertoken', token);   // 监管局Token
```

### 5.2 Token传递

```javascript
// 请求拦截器
instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('satoken');
  const farmerToken = localStorage.getItem('farmertoken');
  const managerToken = localStorage.getItem('managertoken');

  if (token) config.headers.satoken = token;
  if (farmerToken) config.headers.farmertoken = farmerToken;
  if (managerToken) config.headers.managertoken = managerToken;

  return config;
});
```

**结论：** Token认证机制完整，符合后端要求。

---

## 六、总结与建议

### 6.1 API覆盖率统计

| 模块 | 后端接口数 | 前端已调用 | 覆盖率 |
|------|-----------|-----------|--------|
| 用户管理 | 17 | 17 | 100% |
| 绑定管理 | 10 | 10 | 100% |
| 养殖池管理 | 10 | 10 | 100% |
| 苗种投放 | 9 | 9 | 100% |
| 投喂记录 | 9 | 9 | 100% |
| 用药记录 | 9 | 9 | 100% |
| 收获记录 | 9 | 9 | 100% |
| 水质数据监管 | 21 | 21 | 100% |
| 监管局审核 | 6 | 6 | 100% |
| 公告管理 | 5 | 5 | 100% |
| 通知管理 | 5 | 5 | 100% |
| 数据看板 | 3 | 3 | 100% |
| 用户合约 | 3 | 3 | 100% |
| AI聊天 | 1 | 0 | 0% |
| 公共模块 | 2 | 2 | 100% |
| **合计** | **119** | **118** | **99.2%** |

### 6.2 待修复问题清单

| 优先级 | 问题描述 | 影响范围 | 建议操作 |
|--------|----------|----------|----------|
| 高 | AI聊天API未封装 | AI助手功能不可用 | 新增`aiChat()`函数 |
| 中 | 验证码字段名不一致 | 验证码可能不显示 | 修改字段名兼容处理 |

### 6.3 建议新增的API封装

```javascript
// src/api/water/ai.js（建议新增）

import request from '@/utils/request'

// AI聊天（SSE流式响应）
export function aiChat(message) {
  const token = localStorage.getItem('satoken');
  const url = `/gk_api/ai/chat?message=${encodeURIComponent(message)}`;
  
  return new EventSource(url, {
    headers: {
      'satoken': token
    }
  });
}
```

---

## 七、附录

### 附录A：前端API文件完整清单

```
water/frontend/src/
├── api/
│   ├── water/
│   │   ├── user.js          # 用户管理 (15个函数)
│   │   ├── bind.js          # 绑定管理 (9个函数)
│   │   ├── pond.js          # 养殖池管理 (9个函数)
│   │   ├── seed.js          # 苗种投放 (8个函数)
│   │   ├── feed.js          # 投喂记录 (8个函数)
│   │   ├── medicine.js      # 用药记录 (8个函数)
│   │   ├── harvest.js       # 收获记录 (8个函数)
│   │   ├── water.js         # 水质数据监管 (20个函数)
│   │   ├── announcement.js  # 公告管理 (5个函数)
│   │   ├── notification.js  # 通知管理 (5个函数)
│   │   ├── dashboard.js     # 数据看板 (3个函数)
│   │   ├── managerAudit.js  # 监管局审核 (6个函数)
│   │   └── farming-process.js # 养殖过程汇总
│   └── common/
│       └── common.js        # 公共模块 (5个函数)
└── store/
    └── api/
        └── user.js          # 用户认证 (4个函数)
```

### 附录B：后端API路径前缀

所有后端API路径前缀为 `/gk_api`，前端请求时会自动添加。

---

> **文档结束**
>
> 分析完成时间：2026-04-25
> 分析工具：Claude Code
