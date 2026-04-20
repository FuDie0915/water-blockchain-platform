# 海水养殖区块链水质监管平台 — API 接口文档

> 版本：v1.0  
> 更新日期：2026-04-19  
> 接口总数：**119**（15 个 Controller 模块）

---

## 一、概述

本文档描述"海水养殖区块链水质监管平台"后端全部 REST API 接口，涵盖 15 个 Controller 共 119 个端点。平台采用 Sa-Token 做会话认证，自定义 `@AuthCheck` 注解做角色鉴权，通过 FISCO BCOS 区块链实现关键业务数据上链存证。

### 系统角色

| 角色标识 | 中文名 | 说明 |
|---------|--------|------|
| `admin` | 管理员 | 系统最高权限，管理用户、审核监管局资质、发布公告 |
| `farmers` | 养殖户 | 提交养殖数据（养殖池、苗种、投喂、用药、收获等），需绑定监管局 |
| `manager` | 监管局 | 审核养殖户提交的数据，管理预警阈值，审批许可证，需通过管理员审核 |

### 审核状态说明

| 值 | 含义 | 说明 |
|----|------|------|
| 0 | 待审核 | 刚提交，等待监管局审核 |
| 1 | 已通过 | 审核通过 |
| 2 | 已拒绝 | 审核拒绝 |

---

## 二、认证方式

### 请求头

所有需要登录的接口须在请求头中携带 Token：

```
satoken: <token_value>
```

Token 在登录成功后由服务端返回，可通过 `GET /user/getLoginInfoByToken/{token}` 验证有效性。

### 鉴权注解

- **无注解**：公开接口，无需登录
- **`@AuthCheck(roleType = "admin")`**：仅管理员可访问
- **`@AuthCheck(roleType = "farmers")`**：仅养殖户可访问
- **`@AuthCheck(roleType = "manager")`**：仅监管局可访问
- **无 `@AuthCheck` 但有 Sa-Token 登录校验**：需登录，不限制角色

---

## 三、通用响应格式

### 3.1 BaseResponse（单条/非分页）

```json
{
  "code": 0,
  "data": "<T>",
  "message": "ok"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 业务状态码，0 表示成功 |
| data | T | 返回数据，泛型 |
| message | String | 提示信息 |

### 3.2 PageResponse（分页）

```json
{
  "code": 0,
  "data": ["<item>", "..."],
  "message": "ok",
  "total": 100,
  "size": 10,
  "current": 1
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 业务状态码，0 表示成功 |
| data | List\<T\> | 当前页数据列表 |
| message | String | 提示信息 |
| total | long | 总记录数 |
| size | long | 每页数据量 |
| current | long | 当前第几页 |

### 3.3 SSE 流式响应（AI 聊天）

`Content-Type: text/event-stream`

```
data: <chunk_text>

data: <chunk_text>
```

### 3.4 文件流响应（Excel 导出）

`Content-Type: application/octet-stream`  
`Content-Disposition: attachment; filename=xxx.xlsx`

---

## 四、错误码表

| 错误码 | 枚举名 | 含义 |
|--------|--------|------|
| 0 | SUCCESS | 成功 |
| 40000 | PARAMS_ERROR | 请求参数错误 |
| 40001 | HEADER_PARAMS_ERROR | 请求头参数错误（缺少 satoken） |
| 40100 | NOT_LOGIN_ERROR | 未登录 |
| 40101 | NO_AUTH_ERROR | 无权限 |
| 40102 | INVALID_USER | 用户不存在 |
| 40103 | USER_LOCKED | 用户未激活（被禁用） |
| 40104 | USER_EXISTS | 账户已存在 |
| 40400 | NOT_FOUND_ERROR | 请求数据不存在 |
| 40300 | FORBIDDEN_ERROR | 禁止访问 |
| 50000 | SYSTEM_ERROR | 系统内部异常 |
| 50001 | OPERATION_ERROR | 操作失败 |
| 60001 | FILE_SIZE_ERROR | 文件上传大小超出限制（最大 50MB） |
| 60002 | FILE_NAME_ERROR | 文件名称长度超出限制 |
| 60003 | FILE_UPLOAD_ERROR | 上传文件类型异常 |
| 70003 | CHAIN_CALL_FAILED | 区块链调用失败 |
| 20001 | BIND_FAILED | 合约绑定失败 |
| 20002 | VERIFY_FAILED | 无可审核许可证内容 |
| 99999 | — | 用户合约绑定/更新失败（自定义错误） |

---

## 五、接口详情

---

### 5.1 用户管理模块 UserApi

基础路径：`/user`  
接口数量：17

---

#### 5.1.1 获取图形验证码

- **Method**: `GET`
- **Path**: `/user/captcha`
- **权限**: 公开（无需登录）
- **请求参数**: 无
- **响应类型**: `BaseResponse<ImageCaptchaUtil.CaptchaResult>`
- **响应 data 字段**:

| 字段 | 类型 | 说明 |
|------|------|------|
| captchaKey | String | 验证码唯一标识，注册时需回传 |
| captchaImage | String | Base64 编码的验证码图片 |

- **备注**: 每次调用生成新的验证码，key 需在注册时回传

---

#### 5.1.2 养殖户注册

- **Method**: `POST`
- **Path**: `/user/register/farmers`
- **权限**: 公开（无需登录）
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userAccount | String | 是 | 登录账号 |
| userPassword | String | 是 | 登录密码 |
| captchaKey | String | 是 | 验证码标识（由 captcha 接口返回） |
| captchaCode | String | 是 | 用户输入的验证码 |
| extInfo | String | 否 | 扩展信息（JSON 格式） |

- **响应类型**: `BaseResponse<UserLoginResp>`

---

#### 5.1.3 监管局注册

- **Method**: `POST`
- **Path**: `/user/register/manager`
- **权限**: 公开（无需登录）
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userAccount | String | 是 | 登录账号 |
| userPassword | String | 是 | 登录密码 |
| captchaKey | String | 是 | 验证码标识 |
| captchaCode | String | 是 | 用户输入的验证码 |
| extInfo | String | 否 | 扩展信息（JSON 格式） |

- **响应类型**: `BaseResponse<UserLoginResp>`

---

#### 5.1.4 管理员登录

- **Method**: `POST`
- **Path**: `/user/login/admin`
- **权限**: 公开（无需登录）
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userAccount | String | 是 | 登录账号 |
| userPassword | String | 是 | 登录密码 |

- **响应类型**: `BaseResponse<UserLoginResp>`

---

#### 5.1.5 养殖户登录

- **Method**: `POST`
- **Path**: `/user/login/farmers`
- **权限**: 公开（无需登录）
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userAccount | String | 是 | 登录账号 |
| userPassword | String | 是 | 登录密码 |

- **响应类型**: `BaseResponse<UserLoginResp>`

---

#### 5.1.6 监管局登录

- **Method**: `POST`
- **Path**: `/user/login/manager`
- **权限**: 公开（无需登录）
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userAccount | String | 是 | 登录账号 |
| userPassword | String | 是 | 登录密码 |

- **响应类型**: `BaseResponse<UserLoginResp>`

---

#### 5.1.7 修改个人信息

- **Method**: `POST`
- **Path**: `/user/updateProfile`
- **权限**: 需登录（不限制角色）
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userName | String | 否 | 用户姓名 |
| oldPassword | String | 否 | 旧密码（修改密码时必填） |
| userPassword | String | 否 | 新密码 |
| extInfo | String | 否 | 扩展信息（JSON 格式） |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.1.8 管理员查看用户列表

- **Method**: `GET`
- **Path**: `/user/admin/list`
- **权限**: ADMIN
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | long | 否 | 页码，默认 1 |
| pageSize | long | 否 | 每页条数，默认 10 |

- **响应类型**: `BaseResponse<PageResponse<UserResp>>`

---

#### 5.1.9 管理员修改用户状态

- **Method**: `POST`
- **Path**: `/user/admin/updateStatus`
- **权限**: ADMIN
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户 ID |
| userStatus | Integer | 是 | 用户状态：0=禁用，1=启用 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.1.10 管理员修改用户信息

- **Method**: `POST`
- **Path**: `/user/admin/updateUser`
- **权限**: ADMIN
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户 ID |
| userName | String | 否 | 用户姓名 |
| userAccount | String | 否 | 登录账号 |
| extInfo | String | 否 | 扩展信息（JSON 格式） |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.1.11 管理员创建用户

- **Method**: `POST`
- **Path**: `/user/admin/createUser`
- **权限**: ADMIN
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userAccount | String | 是 | 登录账号 |
| userPassword | String | 是 | 登录密码 |
| userName | String | 是 | 用户姓名 |
| userRole | String | 是 | 角色类型：admin / farmers / manager |
| extInfo | String | 否 | 扩展信息（JSON 格式） |

- **响应类型**: `BaseResponse<UserResp>`

---

#### 5.1.12 管理员删除用户

- **Method**: `POST`
- **Path**: `/user/admin/deleteUser`
- **权限**: ADMIN
- **请求参数（RequestParam，查询字符串）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户 ID |

- **请求示例**: `POST /user/admin/deleteUser?userId=123`
- **响应类型**: `BaseResponse<Boolean>`
- **注意**: 参数通过 query string 传递，不是 RequestBody

---

#### 5.1.13 管理员批量修改用户状态

- **Method**: `POST`
- **Path**: `/user/admin/batchUpdateStatus`
- **权限**: ADMIN
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userIds | List\<Long\> | 是 | 用户 ID 列表（最多 100 条） |
| userStatus | Integer | 是 | 用户状态：0=禁用，1=启用 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.1.14 管理员批量删除用户

- **Method**: `POST`
- **Path**: `/user/admin/batchDelete`
- **权限**: ADMIN
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userIds | List\<Long\> | 是 | 用户 ID 列表（最多 100 条） |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.1.15 根据 Token 获取登录用户信息

- **Method**: `GET`
- **Path**: `/user/getLoginInfoByToken/{token}`
- **权限**: 公开（通过路径中的 token 验证）
- **请求参数（PathVariable）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | String | 是 | 登录 Token |

- **响应类型**: `BaseResponse<UserLoginResp>`

---

#### 5.1.16 退出登录

- **Method**: `GET`
- **Path**: `/user/logout/{token}`
- **权限**: 公开（通过路径中的 token 验证）
- **请求参数（PathVariable）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | String | 是 | 登录 Token |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.1.17 管理员导出用户列表 Excel

- **Method**: `GET`
- **Path**: `/user/admin/export`
- **权限**: ADMIN
- **请求参数**: 无
- **响应类型**: Excel 文件流（`application/octet-stream`）
- **备注**: 直接返回文件流，浏览器触发下载

---

### 5.2 监管局审核管理模块 ManagerAuditApi

基础路径：`/manager-audit`  
接口数量：6

---

#### 5.2.1 监管局提交审核申请

- **Method**: `POST`
- **Path**: `/manager-audit/submit`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| institutionName | String | 是 | 机构名称 |
| jurisdiction | String | 是 | 管辖区域 |
| phone | String | 是 | 联系电话 |
| remark | String | 否 | 申请备注 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.2.2 监管局查看自己审核状态

- **Method**: `GET`
- **Path**: `/manager-audit/status`
- **权限**: MANAGER
- **请求参数**: 无
- **响应类型**: `BaseResponse<ManagerAudit>`
- **备注**: 返回当前登录监管局的审核申请记录，若未提交过则 data 为 null

---

#### 5.2.3 管理员查看审核申请列表

- **Method**: `GET`
- **Path**: `/manager-audit/admin/list`
- **权限**: ADMIN
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 否 | 审核状态筛选 |
| pageNum | Long | 是 | 页码 |
| pageSize | Long | 是 | 每页条数 |

- **响应类型**: `BaseResponse<PageResponse<ManagerAudit>>`

---

#### 5.2.4 管理员批准审核

- **Method**: `POST`
- **Path**: `/manager-audit/admin/approve`
- **权限**: ADMIN
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 审核申请 ID |
| auditRemark | String | 否 | 审核备注 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.2.5 管理员拒绝审核

- **Method**: `POST`
- **Path**: `/manager-audit/admin/reject`
- **权限**: ADMIN
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 审核申请 ID |
| auditRemark | String | 否 | 审核备注 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.2.6 管理员上链监管局审批记录

- **Method**: `POST`
- **Path**: `/manager-audit/admin/upChain`
- **权限**: ADMIN
- **请求参数（RequestParam，查询字符串）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 审核申请记录 ID |

- **请求示例**: `POST /manager-audit/admin/upChain?id=1`
- **响应类型**: `BaseResponse<Boolean>`
- **注意**: 参数通过 query string 传递，不是 RequestBody

---

### 5.3 监管局-养殖户绑定管理模块 BindApi

基础路径：`/bind`  
接口数量：10

---

#### 5.3.1 获取所有已注册监管局列表

- **Method**: `GET`
- **Path**: `/bind/managers`
- **权限**: FARMERS
- **请求参数**: 无
- **响应类型**: `BaseResponse<List<User>>`
- **备注**: 养殖户查看所有已注册的监管局，用于选择绑定目标

---

#### 5.3.2 养殖户申请绑定监管局

- **Method**: `POST`
- **Path**: `/bind/apply`
- **权限**: FARMERS
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| managerId | Long | 是 | 监管局用户 ID |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.3.3 养殖户查看自己的绑定状态

- **Method**: `GET`
- **Path**: `/bind/status`
- **权限**: FARMERS
- **请求参数**: 无
- **响应类型**: `BaseResponse<BindStatusResp>`
- **备注**: 返回当前养殖户与监管局的绑定关系状态

---

#### 5.3.4 监管局查看所有绑定申请

- **Method**: `GET`
- **Path**: `/bind/manager/list`
- **权限**: MANAGER
- **请求参数**: 无
- **响应类型**: `BaseResponse<List<ManagerFarmer>>`
- **备注**: 监管局查看向其发起绑定的所有养殖户列表

---

#### 5.3.5 监管局同意绑定

- **Method**: `POST`
- **Path**: `/bind/manager/approve`
- **权限**: MANAGER
- **请求参数（RequestParam，查询字符串）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 绑定记录 ID |

- **请求示例**: `POST /bind/manager/approve?id=1`
- **响应类型**: `BaseResponse<Boolean>`
- **注意**: 参数通过 query string 传递，不是 RequestBody

---

#### 5.3.6 监管局拒绝绑定

- **Method**: `POST`
- **Path**: `/bind/manager/reject`
- **权限**: MANAGER
- **请求参数（RequestParam，查询字符串）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 绑定记录 ID |

- **请求示例**: `POST /bind/manager/reject?id=1`
- **响应类型**: `BaseResponse<Boolean>`
- **注意**: 参数通过 query string 传递，不是 RequestBody

---

#### 5.3.7 管理员查看所有绑定关系

- **Method**: `GET`
- **Path**: `/bind/admin/list`
- **权限**: ADMIN
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| managerId | Long | 否 | 监管局用户 ID 筛选 |
| farmerId | Long | 否 | 养殖户用户 ID 筛选 |
| status | Integer | 否 | 绑定状态筛选 |
| pageNum | Long | 是 | 页码 |
| pageSize | Long | 是 | 每页条数 |

- **响应类型**: `BaseResponse<PageResponse<BindingResp>>`

---

#### 5.3.8 养殖户主动解绑监管局

- **Method**: `POST`
- **Path**: `/bind/unbind`
- **权限**: FARMERS
- **请求参数**: 无
- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.3.9 监管局主动解除绑定关系

- **Method**: `POST`
- **Path**: `/bind/manager/unbind`
- **权限**: MANAGER
- **请求参数（RequestParam，查询字符串）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 绑定记录 ID |

- **请求示例**: `POST /bind/manager/unbind?id=1`
- **响应类型**: `BaseResponse<Boolean>`
- **注意**: 参数通过 query string 传递，不是 RequestBody

---

#### 5.3.10 监管局上链绑定记录

- **Method**: `POST`
- **Path**: `/bind/manager/upChain`
- **权限**: MANAGER
- **请求参数（RequestParam，查询字符串）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 绑定记录 ID |

- **请求示例**: `POST /bind/manager/upChain?id=1`
- **响应类型**: `BaseResponse<Boolean>`
- **注意**: 参数通过 query string 传递，不是 RequestBody；将绑定记录存证到区块链

---

### 5.4 养殖池管理模块 PondApi

基础路径：`/pond`  
接口数量：10

---

#### 5.4.1 新增养殖池

- **Method**: `POST`
- **Path**: `/pond/create`
- **权限**: FARMERS
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pondName | String | 是 | 塘名 |
| breedType | String | 是 | 养殖品种 |
| area | Double | 是 | 养殖面积（亩） |
| depth | Double | 是 | 水深（米） |
| startDate | Date(epoch ms) | 是 | 养殖周期开始时间 |
| endDate | Date(epoch ms) | 是 | 养殖周期结束时间 |
| status | Integer | 是 | 池塘状态：0=空闲，1=养殖中，2=休整 |
| waterTemp | Double | 否 | 水温测量值 |
| salinity | Double | 否 | 盐度测量值 |
| ph | Double | 否 | pH 测量值 |
| doValue | Double | 否 | 溶解氧测量值 |
| nh3n | Double | 否 | 氨氮测量值 |
| no2 | Double | 否 | 亚硝酸盐测量值 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.4.2 获取当前用户养殖池列表

- **Method**: `GET`
- **Path**: `/pond/list`
- **权限**: FARMERS
- **请求参数**: 无
- **响应类型**: `BaseResponse<List<Pond>>`
- **备注**: 返回当前登录养殖户的全部养殖池，不分页

---

#### 5.4.3 更新养殖池信息

- **Method**: `POST`
- **Path**: `/pond/update`
- **权限**: FARMERS
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 养殖池 ID |
| pondName | String | 否 | 塘名 |
| breedType | String | 否 | 养殖品种 |
| area | Double | 否 | 养殖面积（亩） |
| depth | Double | 否 | 水深（米） |
| startDate | Date(epoch ms) | 否 | 养殖周期开始时间 |
| endDate | Date(epoch ms) | 否 | 养殖周期结束时间 |
| waterTemp | Double | 否 | 水温测量值 |
| salinity | Double | 否 | 盐度测量值 |
| ph | Double | 否 | pH 测量值 |
| doValue | Double | 否 | 溶解氧测量值 |
| nh3n | Double | 否 | 氨氮测量值 |
| no2 | Double | 否 | 亚硝酸盐测量值 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.4.4 更新养殖池状态

- **Method**: `POST`
- **Path**: `/pond/update/status`
- **权限**: FARMERS
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 养殖池 ID |
| status | Integer | 是 | 池塘状态：0=空闲，1=养殖中，2=休整 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.4.5 删除养殖池

- **Method**: `POST`
- **Path**: `/pond/delete`
- **权限**: FARMERS
- **请求参数（RequestParam，查询字符串）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 养殖池 ID |

- **请求示例**: `POST /pond/delete?id=1`
- **响应类型**: `BaseResponse<Boolean>`
- **注意**: 参数通过 query string 传递，不是 RequestBody

---

#### 5.4.6 监管局查看管辖养殖户的养殖池

- **Method**: `GET`
- **Path**: `/pond/manager/list`
- **权限**: MANAGER
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| farmerId | Long | 否 | 养殖户用户 ID 筛选 |
| auditStatus | Integer | 否 | 审核状态筛选 |
| pageNum | Long | 是 | 页码 |
| pageSize | Long | 是 | 每页条数 |

- **响应类型**: `BaseResponse<PageResponse<Pond>>`

---

#### 5.4.7 监管局审核养殖池

- **Method**: `POST`
- **Path**: `/pond/manager/audit`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 养殖池记录 ID |
| auditStatus | Integer | 是 | 审核状态：1=通过，2=拒绝 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.4.8 监管局批量审核养殖池

- **Method**: `POST`
- **Path**: `/pond/manager/batchAudit`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 养殖池记录 ID 列表 |
| auditStatus | Integer | 是 | 审核状态：1=通过，2=拒绝 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.4.9 监管局导出养殖池 Excel

- **Method**: `GET`
- **Path**: `/pond/manager/export`
- **权限**: MANAGER
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| farmerId | Long | 否 | 养殖户用户 ID 筛选 |

- **响应类型**: Excel 文件流（`application/octet-stream`）

---

#### 5.4.10 监管局上链养殖池

- **Method**: `POST`
- **Path**: `/pond/manager/upChain`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 养殖池记录 ID 列表 |
| auditStatus | Integer | 否 | 伴随提交的审核状态字段（实际仅使用 ids） |

- **响应类型**: `BaseResponse<Boolean>`
- **备注**: 将指定的养殖池记录存证到区块链

---

### 5.5 苗种投放管理模块 SeedApi

基础路径：`/seed`  
接口数量：9

---

#### 5.5.1 新增苗种投放

- **Method**: `POST`
- **Path**: `/seed/create`
- **权限**: FARMERS
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pondId | Long | 是 | 塘号（养殖池 ID） |
| recordDate | Date(epoch ms) | 是 | 投放日期 |
| manager | String | 否 | 责任人 |
| seedType | String | 否 | 苗种品种 |
| seedSpec | String | 否 | 苗种规格 |
| weight | Double | 否 | 投放重量（kg） |
| remark | String | 否 | 备注 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.5.2 苗种投放记录列表

- **Method**: `GET`
- **Path**: `/seed/list`
- **权限**: FARMERS
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pondId | Long | 否 | 养殖池 ID 筛选 |
| pageNum | Long | 是 | 页码 |
| pageSize | Long | 是 | 每页条数 |

- **响应类型**: `PageResponse<SeedRecord>`
- **注意**: 该接口直接返回 PageResponse，不包裹在 BaseResponse 中

---

#### 5.5.3 修改苗种投放记录

- **Method**: `POST`
- **Path**: `/seed/update`
- **权限**: FARMERS
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录 ID |
| pondId | Long | 否 | 塘号 |
| recordDate | Date(epoch ms) | 否 | 投放日期 |
| manager | String | 否 | 责任人 |
| seedType | String | 否 | 苗种品种 |
| seedSpec | String | 否 | 苗种规格 |
| weight | Double | 否 | 投放重量（kg） |
| remark | String | 否 | 备注 |

- **响应类型**: `BaseResponse<Boolean>`
- **备注**: 仅审核状态为 0（待审核）的记录可修改

---

#### 5.5.4 删除苗种投放记录

- **Method**: `POST`
- **Path**: `/seed/delete`
- **权限**: FARMERS
- **请求参数（RequestParam，查询字符串）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录 ID |

- **请求示例**: `POST /seed/delete?id=1`
- **响应类型**: `BaseResponse<Boolean>`
- **注意**: 参数通过 query string 传递；仅待审核状态可删除

---

#### 5.5.5 监管局查看管辖养殖户的苗种投放记录

- **Method**: `GET`
- **Path**: `/seed/manager/list`
- **权限**: MANAGER
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| farmerId | Long | 否 | 养殖户用户 ID 筛选 |
| auditStatus | Integer | 否 | 审核状态筛选 |
| pageNum | Long | 是 | 页码 |
| pageSize | Long | 是 | 每页条数 |

- **响应类型**: `BaseResponse<PageResponse<SeedRecord>>`

---

#### 5.5.6 监管局审核苗种投放记录

- **Method**: `POST`
- **Path**: `/seed/manager/audit`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录 ID |
| auditStatus | Integer | 是 | 审核状态：1=通过，2=拒绝 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.5.7 监管局批量审核苗种投放记录

- **Method**: `POST`
- **Path**: `/seed/manager/batchAudit`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 记录 ID 列表 |
| auditStatus | Integer | 是 | 审核状态：1=通过，2=拒绝 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.5.8 监管局导出苗种投放记录 Excel

- **Method**: `GET`
- **Path**: `/seed/manager/export`
- **权限**: MANAGER
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| farmerId | Long | 否 | 养殖户用户 ID 筛选 |

- **响应类型**: Excel 文件流（`application/octet-stream`）

---

#### 5.5.9 监管局上链苗种记录

- **Method**: `POST`
- **Path**: `/seed/manager/upChain`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 记录 ID 列表 |
| auditStatus | Integer | 否 | 伴随提交的审核状态字段（实际仅使用 ids） |

- **响应类型**: `BaseResponse<Boolean>`
- **备注**: 将指定的苗种投放记录存证到区块链

---

### 5.6 投喂记录管理模块 FeedApi

基础路径：`/feed`  
接口数量：9

---

#### 5.6.1 新增投喂记录

- **Method**: `POST`
- **Path**: `/feed/create`
- **权限**: FARMERS
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pondId | Long | 是 | 塘号（养殖池 ID） |
| feedDate | Date(epoch ms) | 是 | 投喂日期 |
| feedBrand | String | 否 | 饲料品牌 |
| feedAmount | Double | 否 | 投喂用量（kg） |
| manager | String | 否 | 责任人 |
| remark | String | 否 | 备注 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.6.2 投喂记录列表

- **Method**: `GET`
- **Path**: `/feed/list`
- **权限**: FARMERS
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pondId | Long | 否 | 养殖池 ID 筛选 |
| pageNum | Long | 是 | 页码 |
| pageSize | Long | 是 | 每页条数 |

- **响应类型**: `PageResponse<FeedRecord>`
- **注意**: 该接口直接返回 PageResponse，不包裹在 BaseResponse 中

---

#### 5.6.3 修改投喂记录

- **Method**: `POST`
- **Path**: `/feed/update`
- **权限**: FARMERS
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录 ID |
| pondId | Long | 否 | 塘号 |
| feedDate | Date(epoch ms) | 否 | 投喂日期 |
| feedBrand | String | 否 | 饲料品牌 |
| feedAmount | Double | 否 | 投喂用量（kg） |
| manager | String | 否 | 责任人 |
| remark | String | 否 | 备注 |

- **响应类型**: `BaseResponse<Boolean>`
- **备注**: 仅审核状态为 0（待审核）的记录可修改

---

#### 5.6.4 删除投喂记录

- **Method**: `POST`
- **Path**: `/feed/delete`
- **权限**: FARMERS
- **请求参数（RequestParam，查询字符串）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录 ID |

- **请求示例**: `POST /feed/delete?id=1`
- **响应类型**: `BaseResponse<Boolean>`
- **注意**: 参数通过 query string 传递；仅待审核状态可删除

---

#### 5.6.5 监管局查看管辖养殖户的投喂记录

- **Method**: `GET`
- **Path**: `/feed/manager/list`
- **权限**: MANAGER
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| farmerId | Long | 否 | 养殖户用户 ID 筛选 |
| auditStatus | Integer | 否 | 审核状态筛选 |
| pageNum | Long | 是 | 页码 |
| pageSize | Long | 是 | 每页条数 |

- **响应类型**: `BaseResponse<PageResponse<FeedRecord>>`

---

#### 5.6.6 监管局审核投喂记录

- **Method**: `POST`
- **Path**: `/feed/manager/audit`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录 ID |
| auditStatus | Integer | 是 | 审核状态：1=通过，2=拒绝 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.6.7 监管局批量审核投喂记录

- **Method**: `POST`
- **Path**: `/feed/manager/batchAudit`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 记录 ID 列表 |
| auditStatus | Integer | 是 | 审核状态：1=通过，2=拒绝 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.6.8 监管局导出投喂记录 Excel

- **Method**: `GET`
- **Path**: `/feed/manager/export`
- **权限**: MANAGER
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| farmerId | Long | 否 | 养殖户用户 ID 筛选 |

- **响应类型**: Excel 文件流（`application/octet-stream`）

---

#### 5.6.9 监管局上链投喂记录

- **Method**: `POST`
- **Path**: `/feed/manager/upChain`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 记录 ID 列表 |
| auditStatus | Integer | 否 | 伴随提交的审核状态字段（实际仅使用 ids） |

- **响应类型**: `BaseResponse<Boolean>`
- **备注**: 将指定的投喂记录存证到区块链

---

### 5.7 用药记录管理模块 MedicineApi

基础路径：`/medicine`  
接口数量：9

---

#### 5.7.1 新增用药记录

- **Method**: `POST`
- **Path**: `/medicine/create`
- **权限**: FARMERS
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pondId | Long | 是 | 塘号（养殖池 ID） |
| medicineDate | Date(epoch ms) | 是 | 用药日期 |
| manager | String | 否 | 责任人 |
| medicineName | String | 否 | 药品通用名 |
| purpose | String | 否 | 用药目的（预防/治疗/消毒） |
| dosage | String | 否 | 用药剂量 |
| withdrawalStart | Date(epoch ms) | 否 | 休药期开始日期 |
| withdrawalEnd | Date(epoch ms) | 否 | 休药期结束日期 |
| remark | String | 否 | 备注 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.7.2 用药记录列表

- **Method**: `GET`
- **Path**: `/medicine/list`
- **权限**: FARMERS
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pondId | Long | 否 | 养殖池 ID 筛选 |
| pageNum | Long | 是 | 页码 |
| pageSize | Long | 是 | 每页条数 |

- **响应类型**: `PageResponse<MedicineRecord>`
- **注意**: 该接口直接返回 PageResponse，不包裹在 BaseResponse 中

---

#### 5.7.3 修改用药记录

- **Method**: `POST`
- **Path**: `/medicine/update`
- **权限**: FARMERS
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录 ID |
| pondId | Long | 否 | 塘号 |
| medicineDate | Date(epoch ms) | 否 | 用药日期 |
| manager | String | 否 | 责任人 |
| medicineName | String | 否 | 药品通用名 |
| purpose | String | 否 | 用药目的（预防/治疗/消毒） |
| dosage | String | 否 | 用药剂量 |
| withdrawalStart | Date(epoch ms) | 否 | 休药期开始日期 |
| withdrawalEnd | Date(epoch ms) | 否 | 休药期结束日期 |
| remark | String | 否 | 备注 |

- **响应类型**: `BaseResponse<Boolean>`
- **备注**: 仅审核状态为 0（待审核）的记录可修改

---

#### 5.7.4 删除用药记录

- **Method**: `POST`
- **Path**: `/medicine/delete`
- **权限**: FARMERS
- **请求参数（RequestParam，查询字符串）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录 ID |

- **请求示例**: `POST /medicine/delete?id=1`
- **响应类型**: `BaseResponse<Boolean>`
- **注意**: 参数通过 query string 传递；仅待审核状态可删除

---

#### 5.7.5 监管局查看管辖养殖户的用药记录

- **Method**: `GET`
- **Path**: `/medicine/manager/list`
- **权限**: MANAGER
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| farmerId | Long | 否 | 养殖户用户 ID 筛选 |
| auditStatus | Integer | 否 | 审核状态筛选 |
| pageNum | Long | 是 | 页码 |
| pageSize | Long | 是 | 每页条数 |

- **响应类型**: `BaseResponse<PageResponse<MedicineRecord>>`

---

#### 5.7.6 监管局审核用药记录

- **Method**: `POST`
- **Path**: `/medicine/manager/audit`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录 ID |
| auditStatus | Integer | 是 | 审核状态：1=通过，2=拒绝 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.7.7 监管局批量审核用药记录

- **Method**: `POST`
- **Path**: `/medicine/manager/batchAudit`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 记录 ID 列表 |
| auditStatus | Integer | 是 | 审核状态：1=通过，2=拒绝 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.7.8 监管局导出用药记录 Excel

- **Method**: `GET`
- **Path**: `/medicine/manager/export`
- **权限**: MANAGER
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| farmerId | Long | 否 | 养殖户用户 ID 筛选 |

- **响应类型**: Excel 文件流（`application/octet-stream`）

---

#### 5.7.9 监管局上链用药记录

- **Method**: `POST`
- **Path**: `/medicine/manager/upChain`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 记录 ID 列表 |
| auditStatus | Integer | 否 | 伴随提交的审核状态字段（实际仅使用 ids） |

- **响应类型**: `BaseResponse<Boolean>`
- **备注**: 将指定的用药记录存证到区块链

---

### 5.8 收获记录管理模块 HarvestApi

基础路径：`/harvest`  
接口数量：9

---

#### 5.8.1 新增收获记录

- **Method**: `POST`
- **Path**: `/harvest/create`
- **权限**: FARMERS
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pondId | Long | 是 | 塘号（养殖池 ID） |
| harvestDate | Date(epoch ms) | 是 | 收获日期 |
| manager | String | 否 | 责任人 |
| batchNo | String | 否 | 收获批次 |
| spec | String | 否 | 品种规格 |
| totalWeight | Double | 否 | 总收获重量（kg） |
| survivalRate | Double | 否 | 成活率（%） |
| buyerInfo | String | 否 | 收购方信息 |
| qualityResult | String | 否 | 质量检测结果 |
| remark | String | 否 | 备注 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.8.2 收获记录列表

- **Method**: `GET`
- **Path**: `/harvest/list`
- **权限**: FARMERS
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pondId | Long | 否 | 养殖池 ID 筛选 |
| pageNum | Long | 是 | 页码 |
| pageSize | Long | 是 | 每页条数 |

- **响应类型**: `PageResponse<HarvestRecord>`
- **注意**: 该接口直接返回 PageResponse，不包裹在 BaseResponse 中

---

#### 5.8.3 修改收获记录

- **Method**: `POST`
- **Path**: `/harvest/update`
- **权限**: FARMERS
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录 ID |
| pondId | Long | 否 | 塘号 |
| harvestDate | Date(epoch ms) | 否 | 收获日期 |
| manager | String | 否 | 责任人 |
| batchNo | String | 否 | 收获批次 |
| spec | String | 否 | 品种规格 |
| totalWeight | Double | 否 | 总收获重量（kg） |
| survivalRate | Double | 否 | 成活率（%） |
| buyerInfo | String | 否 | 收购方信息 |
| qualityResult | String | 否 | 质量检测结果 |
| remark | String | 否 | 备注 |

- **响应类型**: `BaseResponse<Boolean>`
- **备注**: 仅审核状态为 0（待审核）的记录可修改

---

#### 5.8.4 删除收获记录

- **Method**: `POST`
- **Path**: `/harvest/delete`
- **权限**: FARMERS
- **请求参数（RequestParam，查询字符串）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录 ID |

- **请求示例**: `POST /harvest/delete?id=1`
- **响应类型**: `BaseResponse<Boolean>`
- **注意**: 参数通过 query string 传递；仅待审核状态可删除

---

#### 5.8.5 监管局查看管辖养殖户的收获记录

- **Method**: `GET`
- **Path**: `/harvest/manager/list`
- **权限**: MANAGER
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| farmerId | Long | 否 | 养殖户用户 ID 筛选 |
| auditStatus | Integer | 否 | 审核状态筛选 |
| pageNum | Long | 是 | 页码 |
| pageSize | Long | 是 | 每页条数 |

- **响应类型**: `BaseResponse<PageResponse<HarvestRecord>>`

---

#### 5.8.6 监管局审核收获记录

- **Method**: `POST`
- **Path**: `/harvest/manager/audit`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录 ID |
| auditStatus | Integer | 是 | 审核状态：1=通过，2=拒绝 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.8.7 监管局批量审核收获记录

- **Method**: `POST`
- **Path**: `/harvest/manager/batchAudit`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 记录 ID 列表 |
| auditStatus | Integer | 是 | 审核状态：1=通过，2=拒绝 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.8.8 监管局导出收获记录 Excel

- **Method**: `GET`
- **Path**: `/harvest/manager/export`
- **权限**: MANAGER
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| farmerId | Long | 否 | 养殖户用户 ID 筛选 |

- **响应类型**: Excel 文件流（`application/octet-stream`）

---

#### 5.8.9 监管局上链收获记录

- **Method**: `POST`
- **Path**: `/harvest/manager/upChain`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 记录 ID 列表 |
| auditStatus | Integer | 否 | 伴随提交的审核状态字段（实际仅使用 ids） |

- **响应类型**: `BaseResponse<Boolean>`
- **备注**: 将指定的收获记录存证到区块链

---

### 5.9 水质数据监管模块 WaterApi

基础路径：`/water`  
接口数量：22

---

#### 5.9.1 获取区块链账户信息（步骤1）

- **Method**: `POST`
- **Path**: `/water/step1Info`
- **权限**: 需登录（不限制角色）
- **请求参数**: 无
- **响应类型**: `BaseResponse<WaterStep1InfoResp>`
- **备注**: 获取当前用户的区块链账户相关信息

---

#### 5.9.2 养殖户提交许可证

- **Method**: `POST`
- **Path**: `/water/permission/commit`
- **权限**: FARMERS
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| imageUrl | String | 是 | 许可证图像路径（通过 /common/upload 上传后获得的 URL） |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.9.3 养殖户查询自己的许可证

- **Method**: `GET`
- **Path**: `/water/permission/company/query`
- **权限**: FARMERS
- **请求参数**: 无
- **响应类型**: `BaseResponse<List<CompanyCertResp>>`

---

#### 5.9.4 监管局查待审核许可证

- **Method**: `GET`
- **Path**: `/water/permission/manager/query`
- **权限**: MANAGER
- **请求参数**: 无
- **响应类型**: `BaseResponse<List<CompanyCertResp>>`
- **备注**: 查询提交给当前监管局的待审核许可证

---

#### 5.9.5 监管局查询所有许可证

- **Method**: `GET`
- **Path**: `/water/permission/manager/allQuery`
- **权限**: MANAGER
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 否 | 许可证状态筛选 |

- **响应类型**: `BaseResponse<List<CompanyCertResp>>`

---

#### 5.9.6 养殖户删除许可证

- **Method**: `POST`
- **Path**: `/water/permission/delete/{certId}`
- **权限**: FARMERS
- **请求参数（PathVariable）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| certId | Long | 是 | 许可证 ID |

- **响应类型**: `BaseResponse<Boolean>`
- **备注**: 仅可删除被拒绝的许可证

---

#### 5.9.7 删除水质数据

- **Method**: `POST`
- **Path**: `/water/data/delete/{dataId}`
- **权限**: 需登录（不限制角色）
- **请求参数（PathVariable）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dataId | Long | 是 | 水质数据 ID |

- **响应类型**: `BaseResponse<Boolean>`
- **备注**: 仅可删除未上链的水质数据

---

#### 5.9.8 监管局审批许可证

- **Method**: `POST`
- **Path**: `/water/permission/verify`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 许可证记录 ID |
| status | Integer | 是 | 审批状态：1=通过（自动上链），2=拒绝 |

- **响应类型**: `BaseResponse<Boolean>`
- **备注**: 审批通过（status=1）时会自动将许可证信息上链存证

---

#### 5.9.9 许可证链上比对

- **Method**: `POST`
- **Path**: `/water/permission/isOnChain/{permissionId}`
- **权限**: 需登录（不限制角色）
- **请求参数（PathVariable）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| permissionId | Long | 是 | 许可证 ID |

- **响应类型**: `BaseResponse<CompanyCertResp>`
- **备注**: 从区块链上读取许可证信息与链下数据比对

---

#### 5.9.10 水数据生成

- **Method**: `POST`
- **Path**: `/water/data/gen`
- **权限**: 需登录（不限制角色）
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dataType | Integer | 是 | 数据类型：0=TDS，1=浑浊度 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.9.11 水数据采集（设备端）

- **Method**: `POST`
- **Path**: `/water/data/collect`
- **权限**: 公开（无需认证）
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户 ID |
| dataType | Integer | 是 | 数据类型：0=TDS，1=浑浊度 |
| data | String | 是 | 水数据值 |
| status | String | 是 | 水质状态（正常/异常） |

- **响应类型**: `BaseResponse<Boolean>`
- **备注**: 此接口为 IoT 设备数据上报专用，无需登录认证

---

#### 5.9.12 养殖户手动上报水质数据

- **Method**: `POST`
- **Path**: `/water/data/manual`
- **权限**: FARMERS
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pondId | Long | 是 | 塘号（养殖池 ID） |
| dataType | Integer | 是 | 数据类型：2=水温，3=盐度，4=pH，5=溶解氧，6=氨氮，7=亚硝酸盐 |
| data | String | 是 | 数据值 |

- **响应类型**: `BaseResponse<Boolean>`
- **备注**: 手动上报的水质数据需经监管局审核

---

#### 5.9.13 水数据分页查询

- **Method**: `GET`
- **Path**: `/water/data/page`
- **权限**: 需登录（不限制角色）
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| DataType | Integer | 是 | 数据类型（注意大写 D） |
| pageNum | Long | 是 | 页码 |
| pageSize | Long | 是 | 每页条数 |

- **响应类型**: `PageResponse<WaterDataResp>`
- **注意**: 参数名 `DataType` 首字母大写；该接口直接返回 PageResponse，不包裹在 BaseResponse 中

---

#### 5.9.14 水数据上链

- **Method**: `POST`
- **Path**: `/water/data/upChain`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | List\<Long\> | 是 | 水质数据 ID 列表 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.9.15 监管局新建预警阈值配置

- **Method**: `POST`
- **Path**: `/water/threshold/create`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody，全部必填）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| waterTempMin | Double | 是 | 水温下限（℃） |
| waterTempMax | Double | 是 | 水温上限（℃） |
| salinityMin | Double | 是 | 盐度下限 |
| salinityMax | Double | 是 | 盐度上限 |
| phMin | Double | 是 | pH 下限 |
| phMax | Double | 是 | pH 上限 |
| doMin | Double | 是 | 溶解氧下限（mg/L） |
| doMax | Double | 是 | 溶解氧上限（mg/L） |
| nh3nMin | Double | 是 | 氨氮下限（mg/L） |
| nh3nMax | Double | 是 | 氨氮上限（mg/L） |
| no2Min | Double | 是 | 亚硝酸盐下限（mg/L） |
| no2Max | Double | 是 | 亚硝酸盐上限（mg/L） |

- **响应类型**: `BaseResponse<Boolean>`
- **备注**: 只能创建一次，重复创建会报错

---

#### 5.9.16 监管局修改预警阈值配置

- **Method**: `POST`
- **Path**: `/water/threshold/update`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody，只传需要修改的字段）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| waterTempMin | Double | 否 | 水温下限（℃） |
| waterTempMax | Double | 否 | 水温上限（℃） |
| salinityMin | Double | 否 | 盐度下限 |
| salinityMax | Double | 否 | 盐度上限 |
| phMin | Double | 否 | pH 下限 |
| phMax | Double | 否 | pH 上限 |
| doMin | Double | 否 | 溶解氧下限（mg/L） |
| doMax | Double | 否 | 溶解氧上限（mg/L） |
| nh3nMin | Double | 否 | 氨氮下限（mg/L） |
| nh3nMax | Double | 否 | 氨氮上限（mg/L） |
| no2Min | Double | 否 | 亚硝酸盐下限（mg/L） |
| no2Max | Double | 否 | 亚硝酸盐上限（mg/L） |

- **响应类型**: `BaseResponse<Boolean>`
- **备注**: 部分更新，只传需要修改的字段

---

#### 5.9.17 监管局获取预警阈值配置

- **Method**: `GET`
- **Path**: `/water/threshold/get`
- **权限**: MANAGER
- **请求参数**: 无
- **响应类型**: `BaseResponse<WarningThreshold>`

---

#### 5.9.18 监管局查看管辖养殖户的水质数据

- **Method**: `GET`
- **Path**: `/water/data/manager/list`
- **权限**: MANAGER
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| farmerId | Long | 否 | 养殖户用户 ID 筛选 |
| auditStatus | Integer | 否 | 审核状态筛选 |
| pageNum | Long | 是 | 页码 |
| pageSize | Long | 是 | 每页条数 |

- **响应类型**: `BaseResponse<PageResponse<WaterDataResp>>`

---

#### 5.9.19 监管局审核水质数据

- **Method**: `POST`
- **Path**: `/water/data/manager/audit`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 水质数据记录 ID |
| auditStatus | Integer | 是 | 审核状态：1=通过，2=拒绝 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.9.20 监管局批量审核水质数据

- **Method**: `POST`
- **Path**: `/water/data/manager/batchAudit`
- **权限**: MANAGER
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List\<Long\> | 是 | 水质数据记录 ID 列表 |
| auditStatus | Integer | 是 | 审核状态：1=通过，2=拒绝 |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.9.21 监管局导出水质数据 Excel

- **Method**: `GET`
- **Path**: `/water/data/manager/export`
- **权限**: MANAGER
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| farmerId | Long | 否 | 养殖户用户 ID 筛选 |
| dataType | Integer | 否 | 数据类型筛选 |

- **响应类型**: Excel 文件流（`application/octet-stream`）

---

### 5.10 公告管理模块 AnnouncementApi

基础路径：`/announcement`  
接口数量：5

---

#### 5.10.1 管理员发布公告

- **Method**: `POST`
- **Path**: `/announcement/admin/publish`
- **权限**: ADMIN
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 是 | 公告标题 |
| content | String | 是 | 公告内容 |
| targetRole | String | 是 | 目标角色：farmers / manager / all |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.10.2 管理员查看所有公告

- **Method**: `GET`
- **Path**: `/announcement/admin/list`
- **权限**: ADMIN
- **请求参数**: 无
- **响应类型**: `BaseResponse<List<Announcement>>`

---

#### 5.10.3 管理员删除公告

- **Method**: `POST`
- **Path**: `/announcement/admin/delete/{id}`
- **权限**: ADMIN
- **请求参数（PathVariable）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 公告 ID |

- **响应类型**: `BaseResponse<Boolean>`
- **备注**: 已上链的公告不可删除（由业务层校验）

---

#### 5.10.4 用户根据角色查看公告

- **Method**: `GET`
- **Path**: `/announcement/list`
- **权限**: 需登录（不限制角色）
- **请求参数**: 无
- **响应类型**: `BaseResponse<List<Announcement>>`
- **备注**: 系统自动根据当前登录用户的角色筛选可见公告

---

#### 5.10.5 管理员上链系统公告

- **Method**: `POST`
- **Path**: `/announcement/admin/upChain`
- **权限**: ADMIN
- **请求参数（RequestParam，查询字符串）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 公告 ID |

- **请求示例**: `POST /announcement/admin/upChain?id=1`
- **响应类型**: `BaseResponse<Boolean>`
- **注意**: 参数通过 query string 传递，不是 RequestBody

---

### 5.11 消息通知模块 NotificationApi

基础路径：`/notification`  
接口数量：5

---

#### 5.11.1 分页查询我的通知列表

- **Method**: `GET`
- **Path**: `/notification/list`
- **权限**: 需登录（不限制角色）
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| isRead | Integer | 否 | 已读状态筛选（0=未读，1=已读），不传则返回全部 |
| pageNum | long | 否 | 页码，默认 1 |
| pageSize | long | 否 | 每页条数，默认 10 |

- **响应类型**: `BaseResponse<PageResponse<Notification>>`

---

#### 5.11.2 获取未读通知数量

- **Method**: `GET`
- **Path**: `/notification/unreadCount`
- **权限**: 需登录（不限制角色）
- **请求参数**: 无
- **响应类型**: `BaseResponse<Long>`

---

#### 5.11.3 标记单条通知已读

- **Method**: `POST`
- **Path**: `/notification/read/{id}`
- **权限**: 需登录（不限制角色）
- **请求参数（PathVariable）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 通知 ID |

- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.11.4 全部标记已读

- **Method**: `POST`
- **Path**: `/notification/readAll`
- **权限**: 需登录（不限制角色）
- **请求参数**: 无
- **响应类型**: `BaseResponse<Boolean>`

---

#### 5.11.5 删除通知

- **Method**: `POST`
- **Path**: `/notification/delete/{id}`
- **权限**: 需登录（不限制角色）
- **请求参数（PathVariable）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 通知 ID |

- **响应类型**: `BaseResponse<Boolean>`

---

### 5.12 数据统计看板模块 DashboardApi

基础路径：`/dashboard`  
接口数量：3

---

#### 5.12.1 管理员看板

- **Method**: `GET`
- **Path**: `/dashboard/admin`
- **权限**: ADMIN
- **请求参数**: 无
- **响应类型**: `BaseResponse<AdminDashboardResp>`

---

#### 5.12.2 监管局看板

- **Method**: `GET`
- **Path**: `/dashboard/manager`
- **权限**: MANAGER
- **请求参数**: 无
- **响应类型**: `BaseResponse<ManagerDashboardResp>`

---

#### 5.12.3 养殖户看板

- **Method**: `GET`
- **Path**: `/dashboard/farmer`
- **权限**: FARMERS
- **请求参数**: 无
- **响应类型**: `BaseResponse<FarmerDashboardResp>`

---

### 5.13 用户合约模块 UserContractApi

基础路径：`/user_contract`  
接口数量：3

---

#### 5.13.1 绑定合约

- **Method**: `POST`
- **Path**: `/user_contract/create`
- **权限**: 需登录（不限制角色）
- **Content-Type**: `application/json`
- **请求参数（RequestBody）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| contractType | String | 是 | 合约类型 |
| contractName | String | 是 | 合约名称 |
| contractAddress | String | 是 | 合约地址 |
| contractAbi | String | 是 | 合约 ABI |
| contractBin | String | 是 | 合约 BIN |

- **响应类型**: `BaseResponse<Object>`
- **备注**: 如果同一用户同一合约类型已存在，则执行更新操作

---

#### 5.13.2 查询合约

- **Method**: `GET`
- **Path**: `/user_contract/query`
- **权限**: 需登录（不限制角色）
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| contractType | String | 是 | 合约类型 |

- **响应类型**: `BaseResponse<UserContract>`

---

#### 5.13.3 解绑合约

- **Method**: `POST`
- **Path**: `/user_contract/delete`
- **权限**: 需登录（不限制角色）
- **请求参数（RequestParam，查询字符串）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| contractType | String | 是 | 合约类型 |

- **请求示例**: `POST /user_contract/delete?contractType=WaterData`
- **响应类型**: `BaseResponse<Boolean>`
- **注意**: 参数通过 query string 传递，不是 RequestBody

---

### 5.14 AI 聊天模块 AiApi

基础路径：`/ai`  
接口数量：1

---

#### 5.14.1 AI 聊天（SSE 流式响应）

- **Method**: `GET`
- **Path**: `/ai/chat`
- **权限**: 需登录（不限制角色，通过请求头 satoken 校验）
- **请求参数（RequestParam）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| message | String | 是 | 用户提问内容 |

- **响应类型**: `Flux<ServerSentEvent<String>>`（SSE 流）
- **响应格式**: `Content-Type: text/event-stream`
- **请求示例**: `GET /ai/chat?message=水质pH偏高怎么处理`
- **备注**: 返回 Server-Sent Events 流式响应，前端需使用 `EventSource` 或类似方式接收

---

### 5.15 公共模块 CommonApi

基础路径：`/common`  
接口数量：2

---

#### 5.15.1 区块链看板数据

- **Method**: `GET`
- **Path**: `/common/indexData`
- **权限**: 公开（无需登录）
- **请求参数**: 无
- **响应类型**: `BaseResponse<IndexDataResp>`
- **备注**: 返回区块链平台概况数据（如区块高度、交易数等）

---

#### 5.15.2 通用文件上传

- **Method**: `POST`
- **Path**: `/common/upload`
- **权限**: 需登录（不限制角色）
- **Content-Type**: `multipart/form-data`
- **请求参数（FormData）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | 是 | 上传的文件（最大 50MB） |

- **响应类型**: `Map<String, Object>`（非标准 BaseResponse）
- **响应字段**:

| 字段 | 类型 | 说明 |
|------|------|------|
| url | String | 文件完整访问 URL |
| fileName | String | 服务端存储路径 |
| newFileName | String | 新文件名 |
| originalFilename | String | 原始文件名 |

- **备注**: 此接口返回裸 `Map<String, Object>` 而非 `BaseResponse`

---

## 六、附录

### 附录 A：@RequestParam POST 端点速查表

以下 POST 端点的参数通过 **query string** 传递（而非 RequestBody JSON），前端调用时需特别注意：

| 端点 | 参数 | 类型 |
|------|------|------|
| `POST /user/admin/deleteUser` | userId | Long |
| `POST /manager-audit/admin/upChain` | id | Long |
| `POST /bind/manager/approve` | id | Long |
| `POST /bind/manager/reject` | id | Long |
| `POST /bind/manager/unbind` | id | Long |
| `POST /bind/manager/upChain` | id | Long |
| `POST /pond/delete` | id | Long |
| `POST /seed/delete` | id | Long |
| `POST /feed/delete` | id | Long |
| `POST /medicine/delete` | id | Long |
| `POST /harvest/delete` | id | Long |
| `POST /announcement/admin/upChain` | id | Long |
| `POST /user_contract/delete` | contractType | String |

**调用示例**:
```javascript
// 正确写法
axios.post('/pond/delete?id=123');

// 错误写法（不会生效）
axios.post('/pond/delete', { id: 123 });
```

---

### 附录 B：业务前置条件

| 操作 | 前置条件 |
|------|----------|
| 养殖户提交数据（养殖池/苗种/投喂/用药/收获） | 养殖户已注册并登录 |
| 养殖户提交养殖数据 | 养殖户已绑定监管局（绑定状态为已通过） |
| 监管局审核养殖户数据 | 监管局已通过管理员审核（审核状态为已通过） |
| 监管局提交审核申请 | 监管局已注册并登录 |
| 管理员审核监管局 | 当前用户角色为 admin |
| 上链操作 | 对应记录审核状态为已通过（部分接口不做此校验） |
| 修改/删除记录 | 记录审核状态为待审核（auditStatus=0） |
| 新建预警阈值 | 系统中不存在已有阈值配置（只能创建一次） |
| 删除许可证 | 许可证状态为已拒绝 |
| 删除水质数据 | 水质数据未上链 |
| AI 聊天 | 请求头必须包含有效的 satoken |

---

### 附录 C：字段类型映射

| Java 类型 | JSON 类型 | 说明 |
|-----------|----------|------|
| Long | number | 64 位整数，JS 中注意精度（建议以字符串处理超过 2^53 的值） |
| Integer | number | 32 位整数 |
| Double | number | 双精度浮点数 |
| String | string | 字符串 |
| Date | number/long | 时间戳（epoch 毫秒），JSON 序列化后为数字 |
| List\<Long\> | number[] | Long 数组 |
| Boolean | boolean | 布尔值 |
| MultipartFile | file | `multipart/form-data` 上传 |

---

### 附录 D：前端对接注意事项

1. **Token 管理**：登录成功后将 token 存储在本地，后续所有请求通过 `satoken` 请求头携带；token 可通过 `GET /user/getLoginInfoByToken/{token}` 验证有效性。

2. **分页参数**：后端使用 `pageNum`（从 1 开始）和 `pageSize`，部分接口有默认值（pageNum=1, pageSize=10），建议前端始终显式传递。

3. **响应包裹不一致**：大部分接口返回 `BaseResponse<T>`，但以下接口例外：
   - `/seed/list`、`/feed/list`、`/medicine/list`、`/harvest/list`、`/water/data/page` 直接返回 `PageResponse<T>`
   - `/common/upload` 返回裸 `Map<String, Object>`
   - 所有 Excel 导出接口直接返回文件流

4. **参数大小写**：`GET /water/data/page` 的参数名为 `DataType`（首字母大写 D），与其他接口的驼峰命名（dataType）不同。

5. **SSE 流式响应**：`GET /ai/chat` 使用 Server-Sent Events，前端需使用 `EventSource` API 或 `fetch` + `ReadableStream` 处理流式数据。

6. **文件上传限制**：文件大小最大 50MB，文件名长度有上限，仅允许特定文件类型。

7. **批量操作上限**：批量修改状态、批量删除用户接口，单次最多操作 100 条记录。

8. **Excel 导出**：所有导出接口（共 7 个）直接返回 `application/octet-stream` 文件流，前端需通过 Blob 处理下载。导出接口列表：
   - `GET /user/admin/export`
   - `GET /pond/manager/export`
   - `GET /seed/manager/export`
   - `GET /feed/manager/export`
   - `GET /medicine/manager/export`
   - `GET /harvest/manager/export`
   - `GET /water/data/manager/export`

---

> **文档结束** -- 全部 119 个接口，15 个 Controller 模块
