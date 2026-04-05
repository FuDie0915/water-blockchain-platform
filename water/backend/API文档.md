# 水质区块链监测平台 - API 接口文档

## 基本信息

- **服务地址：** `http://localhost:8080`
- **认证方式：** Sa-Token（Header 传递）
- **数据格式：** JSON
- **字符编码：** UTF-8
- **Swagger 文档：** `http://localhost:8080/swagger-ui/index.html`

---

## 通用说明

### 统一响应结构

所有接口返回统一的 `BaseResponse` 格式：

```json
{
  "code": 0,
  "data": {},
  "message": "成功"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码，0 表示成功 |
| data | T | 响应数据 |
| message | String | 响应消息 |

### 分页响应结构

分页接口返回 `PageResponse` 格式：

```json
{
  "code": 0,
  "data": {
    "records": [],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "message": "成功"
}
```

### 错误码定义

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 40000 | 参数错误 |
| 40100 | 未登录 |
| 40101 | 无权限 |
| 50000 | 系统错误 |
| 70003 | 区块链调用失败 |

### 认证机制

系统使用 **Sa-Token** 进行身份认证，不同角色使用不同的 Header 传递 Token：

| 角色      | Token Header | 说明 |
|---------|-------------|------|
| 平台管理员   | `satoken` | 平台用户登录后获取 |
| 养殖户企业用户 | `companytoken` | 企业登录后获取 |
| 监管方     | `managertoken` | 监管方登录后获取 |

> Token 有效期为 24 小时，支持活跃续期。

---

## 一、用户管理模块（平台）

### 1.1 平台登录/注册

**接口地址：** `POST /user/loginOrRegister`

**是否需要认证：** 否

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userAccount | String | 是 | 用户账号 |
| userName | String | 是 | 用户昵称 |

**请求示例：**

```json
{
  "userAccount": "admin001",
  "userName": "管理员"
}
```

**响应数据（UserLoginResp）：**

```json
{
  "code": 0,
  "data": {
    "userResp": {
      "userId": 1,
      "userAccount": "admin001",
      "userName": "管理员",
      "userRole": "admin",
      "accountAddress": "0x1234..."
    },
    "token": "uuid-token-string"
  },
  "message": "成功"
}
```

**响应字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| userResp.userId | Long | 用户 ID |
| userResp.userAccount | String | 用户账号 |
| userResp.userName | String | 用户昵称 |
| userResp.userRole | String | 角色：admin / company / manager |
| userResp.accountAddress | String | 区块链账户地址 |
| token | String | 认证 Token |

---

### 1.2 根据 Token 获取登录信息

**接口地址：** `GET /user/getLoginInfoByToken/{token}`

**是否需要认证：** 否

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | String | 是 | 认证 Token |

**响应数据：** 同 1.1 响应格式（`UserLoginResp`）

---

### 1.3 退出登录

**接口地址：** `GET /user/logout/{token}`

**是否需要认证：** 否

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | String | 是 | 认证 Token |

**响应示例：**

```json
{
  "code": 0,
  "data": true,
  "message": "成功"
}
```

---

## 二、水质业务接口

### 2.1 养殖户企业/监管方登录

**接口地址：** `POST /water/user/login`

**是否需要认证：** 否

**请求参数：**

| 参数 | 类型 | 必填 | 说明                 |
|------|------|------|--------------------|
| userAccount | String | 是 | 用户账号               |
| userPassword | String | 是 | 用户密码               |
| loginType | Integer | 是 | 登录类型：0=养殖户企业，1=监管方 |

**请求示例：**

```json
{
  "userAccount": "company001",
  "userPassword": "123456",
  "loginType": 0
}
```

**响应数据：** 同 1.1 响应格式（`UserLoginResp`）

> **注意：** 登录类型决定返回的 Token 应放在哪个 Header 中：
> - `loginType=0`（养殖户企业）→ Header: `companytoken`
> - `loginType=1`（监管方）→ Header: `managertoken`

---

### 2.2 获取养殖户与监管局信息

**接口地址：** `POST /water/step1Info`

**是否需要认证：** 否

**请求参数：** 无

**响应数据（WaterStep1InfoResp）：**

```json
{
  "code": 0,
  "data": {
    "companyAccount": "company001",
    "companyPassword": "123456",
    "companyAddress": "0xabc...",
    "managerAccount": "manager001",
    "managerPassword": "123456",
    "managerAddress": "0xdef..."
  },
  "message": "成功"
}
```

| 字段 | 类型 | 说明         |
|------|------|------------|
| companyAccount | String | 养殖户企业账号    |
| companyPassword | String | 养殖户企业密码    |
| companyAddress | String | 养殖户企业区块链地址 |
| managerAccount | String | 监管方账号      |
| managerPassword | String | 监管方密码      |
| managerAddress | String | 监管方区块链地址   |

---

### 2.3 养殖户企业提交许可证

**接口地址：** `POST /water/permission/commit`

**是否需要认证：** 是（Header: `companytoken`，角色：`company`）

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| imageUrl | String | 是 | 许可证图片 URL 路径 |

**请求示例：**

```json
{
  "imageUrl": "/profile/upload/20260401/permit.jpg"
}
```

**响应示例：**

```json
{
  "code": 0,
  "data": true,
  "message": "成功"
}
```

---

### 2.4 养殖户企业查询许可证列表

**接口地址：** `GET /water/permission/company/query`

**是否需要认证：** 是（Header: `companytoken`，角色：`company`）

**请求参数：** 无

**响应数据（List\<CompanyCertResp\>）：**

```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "userId": 2,
      "companyName": "XX水务公司",
      "imageUrl": "/profile/upload/20260401/permit.jpg",
      "status": 1,
      "createTime": "2026-04-01T10:30:00"
    }
  ],
  "message": "成功"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 许可证 ID |
| userId | Long | 用户 ID |
| companyName | String | 企业名称 |
| imageUrl | String | 许可证图片路径 |
| status | Integer | 状态：0=待审核，1=已通过并上链，2=已驳回 |
| createTime | Date | 创建时间 |

---

### 2.5 监管方查询许可证列表

**接口地址：** `GET /water/permission/manager/query`

**是否需要认证：** 是（Header: `managertoken`，角色：`manager`）

**请求参数：** 无

**响应数据：** 同 2.4（`List<CompanyCertResp>`）

---

### 2.6 监管方审核许可证

**接口地址：** `POST /water/permission/verify`

**是否需要认证：** 是（Header: `managertoken`，角色：`manager`）

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 许可证 ID |
| status | Integer | 是 | 审核结果：1=通过并上链，2=驳回 |

**请求示例：**

```json
{
  "id": 1,
  "status": 1
}
```

**响应示例：**

```json
{
  "code": 0,
  "data": true,
  "message": "成功"
}
```

> **说明：** 当 `status=1` 时，系统将自动调用区块链智能合约，将许可证信息上链存证。

---

### 2.7 验证许可证是否已上链

**接口地址：** `POST /water/permission/isOnChain/{permissionId}`

**是否需要认证：** 否

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| permissionId | Long | 是 | 许可证 ID |

**响应数据（CompanyCertResp）：**

```json
{
  "code": 0,
  "data": {
    "id": 1,
    "userId": 2,
    "companyName": "XX水务公司",
    "imageUrl": "/profile/upload/20260401/permit.jpg",
    "status": 1,
    "createTime": "2026-04-01T10:30:00"
  },
  "message": "成功"
}
```

> **说明：** 该接口会查询区块链上是否存在对应的许可证记录，并返回最新的许可证信息。

---

### 2.8 生成水质数据

**接口地址：** `POST /water/data/gen`

**是否需要认证：** 否

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dataType | Integer | 是 | 数据类型：0=TDS（溶解性总固体），1=浊度 |

**请求示例：**

```json
{
  "dataType": 0
}
```

**响应示例：**

```json
{
  "code": 0,
  "data": true,
  "message": "成功"
}
```

---

### 2.9 设备采集水质数据

**接口地址：** `POST /water/data/collect`

**是否需要认证：** 否

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户 ID |
| dataType | Integer | 是 | 数据类型：0=TDS，1=浊度 |
| status | String | 是 | 数据状态：Normal（正常）/ Abnormal（异常） |
| data | String | 是 | 水质数据值 |

**请求示例：**

```json
{
  "userId": 2,
  "dataType": 0,
  "status": "Normal",
  "data": "325.6"
}
```

**响应示例：**

```json
{
  "code": 0,
  "data": true,
  "message": "成功"
}
```

---

### 2.10 分页查询水质数据

**接口地址：** `GET /water/data/page`

**是否需要认证：** 否

**请求参数（Query）：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dataType | Integer | 否 | 数据类型：0=TDS，1=浊度 |
| pageNum | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 10 |

**请求示例：**

```
GET /water/data/page?dataType=0&pageNum=1&pageSize=10
```

**响应数据（PageResponse\<WaterDataResp\>）：**

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 2,
        "dataType": 0,
        "data": "325.6",
        "status": "Normal",
        "hash": "0xabc123...",
        "time": "2026-04-01T10:30:00",
        "isOnChain": true
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "message": "成功"
}
```

**WaterDataResp 字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 数据 ID |
| userId | Long | 用户 ID |
| dataType | Integer | 数据类型：0=TDS，1=浊度 |
| data | String | 水质数据值 |
| status | String | 状态：Normal / Abnormal |
| hash | String | 数据哈希值 |
| time | Date | 采集时间 |
| isOnChain | boolean | 是否已上链 |

---

### 2.11 批量上链水质数据

**接口地址：** `POST /water/data/upChain`

**是否需要认证：** 是（Header: `managertoken`，角色：`manager`）

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | List\<Long\> | 是 | 需要上链的水质数据 ID 列表 |

**请求示例：**

```json
{
  "id": [1, 2, 3, 4, 5]
}
```

**响应示例：**

```json
{
  "code": 0,
  "data": true,
  "message": "成功"
}
```

> **说明：** 该接口将选中的水质数据批量写入区块链智能合约，确保数据不可篡改。上链成功后，对应数据的 `isOnChain` 字段将更新为 `true`。

---

## 三、用户合约接口

### 3.1 绑定/创建用户合约

**接口地址：** `POST /user_contract/create`

**是否需要认证：** 是

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| contractType | String | 是 | 合约类型 |
| contractName | String | 是 | 合约名称 |
| contractAddress | String | 是 | 智能合约地址 |
| contractAbi | String | 是 | 合约 ABI |
| contractBin | String | 是 | 合约字节码 |

**请求示例：**

```json
{
  "contractType": "Water",
  "contractName": "WaterContract",
  "contractAddress": "0x9ec7a3b41a530ffe2d9757e7fc0068d2b9a352ae",
  "contractAbi": "[{\"type\":\"function\",...}]",
  "contractBin": "6080604052..."
}
```

**响应示例：**

```json
{
  "code": 0,
  "data": {},
  "message": "成功"
}
```

---

### 3.2 查询用户合约

**接口地址：** `GET /user_contract/query`

**是否需要认证：** 是

**请求参数（Query）：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| contractType | String | 是 | 合约类型 |

**请求示例：**

```
GET /user_contract/query?contractType=Water
```

**响应数据（UserContract）：**

```json
{
  "code": 0,
  "data": {
    "userId": 1,
    "contractType": "Water",
    "contractName": "WaterContract",
    "contractAddress": "0x9ec7a3b41a530ffe2d9757e7fc0068d2b9a352ae",
    "contractAbi": "[{\"type\":\"function\",...}]",
    "contractBin": "6080604052..."
  },
  "message": "成功"
}
```

---

## 四、公共模块

### 4.1 获取首页看板数据

**接口地址：** `GET /common/indexData`

**是否需要认证：** 否

**请求参数：** 无

**响应数据（IndexDataResp）：**

```json
{
  "code": 0,
  "data": {
    "blockCount": 1523,
    "nodeCount": 4,
    "tranCount": 892,
    "tranCount2": 5,
    "nodeStatusList": [
      {
        "nodeId": "node1",
        "blockNumber": 1523,
        "pbftView": 42,
        "status": 1,
        "latestStatusUpdateTime": "2026-04-01T10:30:00"
      }
    ]
  },
  "message": "成功"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| blockCount | Long | 区块高度 |
| nodeCount | Long | 节点数量 |
| tranCount | Long | 交易总数 |
| tranCount2 | Long | 待处理交易数 |
| nodeStatusList | List | 节点状态列表 |
| nodeStatusList[].nodeId | String | 节点 ID |
| nodeStatusList[].blockNumber | Long | 节点区块高度 |
| nodeStatusList[].pbftView | Long | PBFT 视图 |
| nodeStatusList[].status | Integer | 节点状态 |
| nodeStatusList[].latestStatusUpdateTime | Date | 最近更新时间 |

---

### 4.2 文件上传

**接口地址：** `POST /common/upload`

**是否需要认证：** 否

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | 是 | 上传的文件（最大 100MB） |

**请求示例：** `multipart/form-data` 格式，字段名 `file`

**响应数据：**

```json
{
  "url": "/profile/upload/20260401/abc123.jpg",
  "fileName": "abc123.jpg",
  "newFileName": "20260401_abc123.jpg",
  "originalFilename": "permit.jpg"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| url | String | 文件访问 URL |
| fileName | String | 存储文件名 |
| newFileName | String | 新文件名 |
| originalFilename | String | 原始文件名 |

---

## 五、AI 聊天接口

### 5.1 AI 对话（流式）

**接口地址：** `GET /ai/chat`

**是否需要认证：** 否

**请求参数（Query）：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| message | String | 是 | 用户消息内容 |

**响应格式：** `text/event-stream`（Server-Sent Events 流式响应）

**响应示例：**

```
data: {"content": "您好"}

data: {"content": "，我是"}

data: {"content": "水质监测"}
```

> **说明：** 该接口使用 GLM-4 Flash 模型，通过 SSE（Server-Sent Events）实现流式输出，适合前端实时展示 AI 回复内容。

---

## 附录

### A. 数据库表结构

#### 用户表（user）

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | BIGINT | 用户 ID（主键） |
| userAccount | VARCHAR | 用户账号 |
| userPassword | VARCHAR | 密码（加密） |
| userName | VARCHAR | 用户昵称 |
| userRole | VARCHAR | 角色：admin / company / manager |
| accountAddress | VARCHAR | 区块链账户地址 |
| privateKey | VARCHAR | 区块链私钥 |
| isDelete | INT | 逻辑删除：0=未删除，1=已删除 |

#### 养殖户企业许可证表（company_cert）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键（自增） |
| userId | BIGINT | 用户 ID |
| imageUrl | VARCHAR | 许可证图片路径 |
| status | INT | 状态：0=待审核，1=已通过，2=已驳回 |
| createTime | DATETIME | 创建时间 |

#### 用户合约表（user_contract）

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | BIGINT | 用户 ID |
| contractType | VARCHAR | 合约类型 |
| contractName | VARCHAR | 合约名称 |
| contractAddress | VARCHAR | 智能合约地址 |
| contractAbi | TEXT | 合约 ABI |
| contractBin | TEXT | 合约字节码 |

#### 水质数据表（water_data）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键（自增） |
| userId | BIGINT | 用户 ID |
| dataType | INT | 数据类型：0=TDS，1=浊度 |
| data | VARCHAR | 水质数据 |
| status | VARCHAR | 状态：Normal / Abnormal |
| hash | VARCHAR | 数据哈希 |
| time | DATETIME | 采集时间 |
| isOnChain | BOOLEAN | 是否已上链 |

### B. 智能合约方法

合约名称：`Water`，部署地址：`0x9ec7a3b41a530ffe2d9757e7fc0068d2b9a352ae`

| 方法 | 类型 | 说明 |
|------|------|------|
| manager() | 调用 | 获取监管方地址 |
| company() | 调用 | 获取企业地址 |
| companyCertByUserId(userId) | 调用 | 根据用户 ID 查询许可证 |
| uploadCompanyCert(userId, imageUrl) | 交易 | 上传许可证到链上 |
| waterDataByUserId(userId) | 调用 | 根据用户 ID 查询水质数据 |
| insertWaterData(userId, dataType, data, status, hash) | 交易 | 写入水质数据到链上 |
| getWaterDataByUserIdAndType(userId, dataType) | 调用 | 按用户和类型查询水质数据 |

### C. 业务流程说明

#### 许可证审核流程

```
企业提交许可证 → 管理员审核 → 通过（自动上链）/ 驳回
                         ↓
                  可在区块链验证
```

#### 水质数据上链流程

```
设备采集数据 → 数据入库 → 管理员批量选择 → 上链存证
                ↓
           可分页查询    ←→ 区块链验证
```
