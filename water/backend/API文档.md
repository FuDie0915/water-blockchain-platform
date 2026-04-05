# 当前后端 API 清单

## 基本信息

- 服务地址：`http://localhost:8080`
- Swagger：`http://localhost:8080/swagger-ui/index.html`
- 代码目录：`water/backend/src/main/java/com/water/platform/api`
- 当前有效接口数：`18`

## 认证说明

以下认证规则是按当前代码配置整理，不是按接口名称推断。

### 1. 全局登录拦截

`SaTokenConfigure` 对 `/**` 开启了全局登录校验，仅排除了以下路径：

- `POST /user/loginOrRegister`
- `POST /water/data/collect`
- `GET /common/indexData`
- `/profile/upload/**`
- `/static/**`
- `/v3/api-docs`
- `/swagger-ui/**`
- `/error/**`
- `/doc.html`
- `/aircraft/issues/collect`

这意味着：除上述排除路径外，其余接口按当前配置都需要已有登录态，默认依赖 `satoken`。

### 2. 角色附加校验

带 `@AuthCheck` 的接口还会额外校验自定义请求头：

- 企业角色：`companytoken`
- 监管方角色：`managertoken`
- 管理员角色：`satoken`

因此，部分接口除了全局登录态外，还要求额外的角色 Token 头。

## 接口列表

### 1. 用户模块 `UserApi`

| 方法 | 路径 | 登录要求 | 请求参数 | 说明 |
| --- | --- | --- | --- | --- |
| POST | `/user/loginOrRegister` | 无 | JSON: `userAccount`, `userName` | 平台登录或注册 |
| GET | `/user/getLoginInfoByToken/{token}` | 需要 `satoken` | Path: `token` | 根据 token 获取登录用户信息 |
| GET | `/user/logout/{token}` | 需要 `satoken` | Path: `token` | 退出登录 |

### 2. 水质业务模块 `WaterApi`

| 方法 | 路径 | 登录要求 | 请求参数 | 说明 |
| --- | --- | --- | --- | --- |
| POST | `/water/user/login` | 需要 `satoken` | JSON: `userAccount`, `userPassword`, `loginType` | 企业或监管局登录 |
| POST | `/water/step1Info` | 需要 `satoken` | 无 | 水质步骤 1 信息 |
| POST | `/water/permission/commit` | 需要 `satoken` + `companytoken` | JSON: `imageUrl` | 企业提交许可证 |
| GET | `/water/permission/company/query` | 需要 `satoken` + `companytoken` | 无 | 企业查询许可证 |
| GET | `/water/permission/manager/query` | 需要 `satoken` + `managertoken` | 无 | 监管局查询许可证 |
| POST | `/water/permission/verify` | 需要 `satoken` + `managertoken` | JSON: `id`, `status` | 监管局审批许可证 |
| POST | `/water/permission/isOnChain/{permissionId}` | 需要 `satoken` | Path: `permissionId` | 许可证链上比对 |
| POST | `/water/data/gen` | 需要 `satoken` | JSON: `dataType` | 水数据生成 |
| POST | `/water/data/collect` | 无 | JSON: `userId`, `dataType`, `status`, `data` | 水数据采集 |
| GET | `/water/data/page` | 需要 `satoken` | Query: `DataType`, `pageNum`, `pageSize` | 水数据分页查询 |
| POST | `/water/data/upChain` | 需要 `satoken` + `managertoken` | JSON: `id` | 水数据上链 |

#### `WaterApi` 参数补充

- `loginType`：`0` 表示企业，`1` 表示监管局
- `status`：许可证审批接口中，`1` 表示通过并上链，`2` 表示不通过
- `dataType`：`0` 表示 TDS，`1` 表示浑浊
- `/water/data/page` 的查询参数名在代码中写的是 `DataType`，首字母大写
- `/water/data/upChain` 的 `id` 类型为 `List<Long>`

### 3. 用户合约模块 `UserContractApi`

| 方法 | 路径 | 登录要求 | 请求参数 | 说明 |
| --- | --- | --- | --- | --- |
| POST | `/user_contract/create` | 需要 `satoken` | JSON: `contractType`, `contractName`, `contractAddress`, `contractAbi`, `contractBin` | 绑定用户合约 |
| GET | `/user_contract/query` | 需要 `satoken` | Query: `contractType` | 查询用户合约 |

### 4. 公共模块 `CommonApi`

| 方法 | 路径 | 登录要求 | 请求参数 | 说明 |
| --- | --- | --- | --- | --- |
| GET | `/common/indexData` | 无 | 无 | 区块链看板 |
| POST | `/common/upload` | 需要 `satoken` | `multipart/form-data`: `file` | 单文件上传 |

### 5. AI 模块 `AiApi`

| 方法 | 路径 | 登录要求 | 请求参数 | 说明 |
| --- | --- | --- | --- | --- |
| GET | `/ai/chat` | 需要 `satoken` | Query: `message` | AI 聊天，返回 SSE 流 |

## 请求体字段汇总

### `POST /user/loginOrRegister`

```json
{
  "userAccount": "string",
  "userName": "string"
}
```

### `POST /water/user/login`

```json
{
  "userAccount": "string",
  "userPassword": "string",
  "loginType": 0
}
```

### `POST /water/permission/commit`

```json
{
  "imageUrl": "string"
}
```

### `POST /water/permission/verify`

```json
{
  "id": 1,
  "status": 1
}
```

### `POST /water/data/gen`

```json
{
  "dataType": 0
}
```

### `POST /water/data/collect`

```json
{
  "userId": 1,
  "dataType": 0,
  "status": "正常",
  "data": "325.6"
}
```

### `POST /water/data/upChain`

```json
{
  "id": [1, 2, 3]
}
```

### `POST /user_contract/create`

```json
{
  "contractType": "string",
  "contractName": "string",
  "contractAddress": "string",
  "contractAbi": "string",
  "contractBin": "string"
}
```

## 未纳入统计的接口

以下映射在代码中已被注释，不属于当前有效接口：

- `/user/page`
- `/common/download`
- `/common/uploads`
- `/common/download/resource`

## 来源文件

- `water/backend/src/main/java/com/water/platform/api/UserApi.java`
- `water/backend/src/main/java/com/water/platform/api/WaterApi.java`
- `water/backend/src/main/java/com/water/platform/api/UserContractApi.java`
- `water/backend/src/main/java/com/water/platform/api/CommonApi.java`
- `water/backend/src/main/java/com/water/platform/api/AiApi.java`
- `water/backend/src/main/java/com/water/platform/base/config/SaTokenConfigure.java`
- `water/backend/src/main/java/com/water/platform/aop/AuthInterceptor.java`
