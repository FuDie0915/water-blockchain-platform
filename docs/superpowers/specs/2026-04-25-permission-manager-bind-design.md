# 养殖户许可证绑定监管局审核功能设计文档

> 版本：v1.0
> 创建日期：2026-04-25
> 状态：待实现

---

## 一、需求背景

### 1.1 问题描述

当前系统中，养殖户提交排污许可证后，监管局审核流程存在以下问题：

1. `company_cert` 表缺少 `managerId` 字段，无法关联许可证应由哪个监管局审核
2. 监管局查询待审核许可证时，无法筛选只属于自己的管辖范围
3. 许可证审核流程与绑定关系未关联

### 1.2 目标

实现养殖户许可证与监管局的绑定审核流程：
- 养殖户提交许可证时，自动关联已绑定的监管局
- 监管局只能审核自己管辖范围内养殖户提交的许可证
- 确保三端（管理员、养殖户、监管局）逻辑互通

---

## 二、现有资源分析

### 2.1 已有的绑定机制

**`manager_farmer` 表：**
```sql
CREATE TABLE `manager_farmer` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `managerId`   BIGINT        NOT NULL COMMENT '监管局用户ID',
    `farmerId`    BIGINT        NOT NULL COMMENT '养殖户用户ID',
    `status`      INT           NOT NULL DEFAULT 0 COMMENT '绑定状态: 0=待审核, 1=已通过, 2=已拒绝',
    ...
)
```

**已有的服务方法：**
- `ManagerFarmerService.validateFarmerBound(farmerId)` - 校验养殖户是否已绑定监管局
- `ManagerFarmerService.getManagedFarmerIds(managerId)` - 获取监管局管辖的所有养殖户ID

### 2.2 已有的许可证表

**`company_cert` 表：**
```sql
CREATE TABLE `company_cert` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `userId`       BIGINT        NOT NULL COMMENT '用户ID（养殖户）',
    `imageUrl`     VARCHAR(512)  NULL COMMENT '许可证图像路径',
    `status`       INT           NOT NULL DEFAULT 0 COMMENT '审核状态: 0=待审批, 1=通过并上链, 2=不通过',
    `createTime`   DATETIME      NULL,
    `chainTxHash`  VARCHAR(255)  NULL,
    PRIMARY KEY (`id`)
)
```

**问题：缺少 `managerId` 字段**

---

## 三、设计方案

### 3.1 核心设计决策

| 决策点 | 选择 | 理由 |
|--------|------|------|
| 许可证关联监管局方式 | 提交时自动关联 | 符合现有绑定逻辑，用户体验最佳 |
| 数据库修改方式 | 新增字段 | 数据模型清晰，查询效率高 |

### 3.2 数据库修改

#### 3.2.1 新增字段

```sql
ALTER TABLE `company_cert`
ADD COLUMN `managerId` BIGINT NULL COMMENT '监管局用户ID（审核方）' AFTER `userId`,
ADD KEY `idx_cert_manager` (`managerId`);
```

#### 3.2.2 数据迁移脚本

```sql
-- 为已存在的许可证填充managerId（根据绑定关系）
UPDATE company_cert cc
INNER JOIN manager_farmer mf ON cc.userId = mf.farmerId AND mf.status = 1 AND mf.isDelete = 0
SET cc.managerId = mf.managerId
WHERE cc.managerId IS NULL;
```

---

## 四、后端实现

### 4.1 实体类修改

**文件：** `CompanyCert.java`

```java
@TableName(value = "company_cert")
@Data
public class CompanyCert {
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户ID（养殖户）")
    private Long userId;

    @ApiModelProperty(value = "监管局用户ID（审核方）")
    private Long managerId;  // 新增字段

    @ApiModelProperty(value = "排污许可证图像路径")
    private String imageUrl;

    @ApiModelProperty(value = "状态(0:待审批、1:通过并上链、2:不通过)")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    private String chainTxHash;
}
```

### 4.2 响应DTO修改

**文件：** `CompanyCertResp.java`

```java
@Data
public class CompanyCertResp {
    private Long id;
    private Long userId;
    private String companyName;

    @ApiModelProperty(value = "监管局用户ID")
    private Long managerId;  // 新增

    @ApiModelProperty(value = "监管局名称")
    private String managerName;  // 新增

    private String imageUrl;
    private Integer status;
    private Date createTime;
    private Boolean isOnChain;
}
```

### 4.3 Service层修改

**文件：** `WaterService.java`

#### 4.3.1 修改 commit() 方法

**原逻辑：**
```java
public BaseResponse<Boolean> commit(PermissionCommitReq permissionCommitReq) {
    Long userId = TokenUtil.getLoginUserId();
    boolean exists = companyCertMapper.exists(...);
    ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR);
    CompanyCert companyCert = new CompanyCert(null, userId, permissionCommitReq.getImageUrl(), 0, DateUtil.date(), null);
    companyCertMapper.insert(companyCert);
    return ResultUtils.success(Boolean.TRUE);
}
```

**新逻辑：**
```java
public BaseResponse<Boolean> commit(PermissionCommitReq permissionCommitReq) {
    Long userId = TokenUtil.getLoginUserId();

    // 新增：校验养殖户是否已绑定监管局
    ManagerFarmer bind = managerFarmerService.validateFarmerBound(userId);
    Long managerId = bind.getManagerId();

    // 修改：同一监管局下不能重复提交
    boolean exists = companyCertMapper.exists(new LambdaQueryWrapper<CompanyCert>()
            .eq(CompanyCert::getUserId, userId)
            .eq(CompanyCert::getManagerId, managerId)
            .and(w -> w.eq(CompanyCert::getStatus, 0).or().eq(CompanyCert::getStatus, 1)));
    ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "您已提交过许可证，请等待审核结果");

    // 修改：创建时包含managerId
    CompanyCert companyCert = new CompanyCert();
    companyCert.setUserId(userId);
    companyCert.setManagerId(managerId);  // 新增
    companyCert.setImageUrl(permissionCommitReq.getImageUrl());
    companyCert.setStatus(0);
    companyCert.setCreateTime(DateUtil.date());
    companyCertMapper.insert(companyCert);

    // 新增：通知监管局
    notificationService.send(managerId, "新的许可证待审核",
            "养殖户提交了新的排污许可证，请及时审核", "AUDIT", companyCert.getId());

    return ResultUtils.success(Boolean.TRUE);
}
```

#### 4.3.2 修改 verify() 方法

**原逻辑：**
```java
public BaseResponse<Boolean> verify(PermissionVerifyReq permissionVerifyReq) {
    CompanyCert companyCert = companyCertMapper.selectOne(new LambdaQueryWrapper<CompanyCert>()
            .eq(CompanyCert::getId, permissionVerifyReq.getId())
            .eq(CompanyCert::getStatus, 0));
    ThrowUtils.throwIf(companyCert == null, ErrorCode.VERIFY_FAILED);
    // ... 后续审核逻辑
}
```

**新逻辑：**
```java
public BaseResponse<Boolean> verify(PermissionVerifyReq permissionVerifyReq) {
    Long managerId = TokenUtil.getLoginUserId();

    // 修改：校验许可证属于当前监管局管辖范围
    CompanyCert companyCert = companyCertMapper.selectOne(new LambdaQueryWrapper<CompanyCert>()
            .eq(CompanyCert::getId, permissionVerifyReq.getId())
            .eq(CompanyCert::getManagerId, managerId)  // 新增：校验归属
            .eq(CompanyCert::getStatus, 0));
    ThrowUtils.throwIf(companyCert == null, ErrorCode.VERIFY_FAILED, "许可证不存在或无权审核");

    // 后续审核逻辑不变
    companyCert.setStatus(permissionVerifyReq.getStatus());
    companyCertMapper.updateById(companyCert);

    if (permissionVerifyReq.getStatus() == 1 && isBlockchainEnabled()) {
        WaterRawCall waterRawCall = getWaterRawCall();
        TransactionReceipt transactionReceipt = waterRawCall.uploadCompanyCert(...);
        ChainUtil.check(transactionReceipt);
    }

    String certResult = permissionVerifyReq.getStatus() == 1 ? "通过" : "未通过";
    String extra = (permissionVerifyReq.getStatus() == 1 && isBlockchainEnabled()) ? "，许可证已上链" : "";
    notificationService.send(companyCert.getUserId(), "排污许可证审核" + certResult,
            "您的排污许可证审核" + certResult + extra, "AUDIT", companyCert.getId());

    return ResultUtils.success(Boolean.TRUE);
}
```

#### 4.3.3 修改 managerAllQuery() 方法

**原逻辑：**
```java
public BaseResponse<List<CompanyCertResp>> managerAllQuery(Integer status) {
    LambdaQueryWrapper<CompanyCert> wrapper = new LambdaQueryWrapper<>();
    if (status != null) {
        wrapper.eq(CompanyCert::getStatus, status);
    }
    wrapper.orderByDesc(CompanyCert::getId);
    // ... 查询逻辑
}
```

**新逻辑：**
```java
public BaseResponse<List<CompanyCertResp>> managerAllQuery(Integer status) {
    Long managerId = TokenUtil.getLoginUserId();

    LambdaQueryWrapper<CompanyCert> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(CompanyCert::getManagerId, managerId);  // 新增：只查询自己的
    if (status != null) {
        wrapper.eq(CompanyCert::getStatus, status);
    }
    wrapper.orderByDesc(CompanyCert::getId);

    List<CompanyCert> companyCerts = companyCertMapper.selectList(wrapper);
    if (companyCerts == null || companyCerts.isEmpty()) {
        return ResultUtils.success(null);
    }

    List<CompanyCertResp> resps = BeanConvertUtils.convertListTo(companyCerts, CompanyCertResp::new);
    for (CompanyCertResp resp : resps) {
        User farmer = userMapper.selectById(resp.getUserId());
        if (farmer != null) {
            resp.setCompanyName(farmer.getUserName());
        }
        // 新增：填充监管局名称
        resp.setManagerName(null);  // 当前监管局查询自己的，不需要显示
    }
    return ResultUtils.success(resps);
}
```

#### 4.3.4 修改 companyQuery() 方法

**新逻辑：**
```java
public BaseResponse<List<CompanyCertResp>> companyQuery() {
    Long userId = TokenUtil.getLoginUserId();
    List<CompanyCert> companyCerts = companyCertMapper.selectList(new LambdaQueryWrapper<CompanyCert>()
            .eq(CompanyCert::getUserId, userId)
            .orderByDesc(CompanyCert::getId));

    if (companyCerts == null || companyCerts.isEmpty()) {
        return ResultUtils.success(null);
    }

    List<CompanyCertResp> resps = BeanConvertUtils.convertListTo(companyCerts, CompanyCertResp::new);

    // 填充养殖户名称
    User farmer = userMapper.selectById(userId);
    String farmerName = farmer != null ? farmer.getUserName() : null;

    for (CompanyCertResp resp : resps) {
        resp.setCompanyName(farmerName);

        // 新增：填充监管局名称
        if (resp.getManagerId() != null) {
            User manager = userMapper.selectById(resp.getManagerId());
            resp.setManagerName(manager != null ? manager.getUserName() : null);
        }
    }

    return ResultUtils.success(resps);
}
```

---

## 五、业务流程

### 5.1 养殖户许可证提交流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    养殖户许可证提交流程                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. 养殖户登录                                                   │
│         │                                                       │
│         ▼                                                       │
│  2. 检查绑定状态 (GET /bind/status)                              │
│         │                                                       │
│    ┌────┴────┐                                                  │
│    │         │                                                  │
│  未绑定    已绑定                                                │
│    │         │                                                  │
│    ▼         ▼                                                  │
│  提示绑定  3. 上传许可证图片 (POST /common/upload)                │
│             │                                                   │
│             ▼                                                   │
│           4. 提交许可证 (POST /water/permission/commit)          │
│              - 校验绑定关系                                      │
│              - 自动获取 managerId                                │
│              - 创建记录 status=0                                 │
│              - 通知监管局                                        │
│             │                                                   │
│             ▼                                                   │
│           等待审核                                               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 监管局许可证审核流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    监管局许可证审核流程                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. 监管局登录                                                   │
│         │                                                       │
│         ▼                                                       │
│  2. 查询待审核许可证                                              │
│     (GET /water/permission/manager/allQuery?status=0)           │
│     - 只返回 managerId = 当前监管局ID 的记录                      │
│         │                                                       │
│         ▼                                                       │
│  3. 查看许可证详情                                               │
│         │                                                       │
│         ▼                                                       │
│  4. 审核决策                                                     │
│         │                                                       │
│    ┌────┴────┐                                                  │
│    │         │                                                  │
│  通过(1)   拒绝(2)                                              │
│    │         │                                                  │
│    ▼         ▼                                                  │
│  上链存证  仅更新状态                                            │
│  通知养殖户  通知养殖户                                          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 5.3 三端交互关系

```
┌──────────────┐     绑定申请      ┌──────────────┐
│              │ ────────────────> │              │
│   养殖户      │                   │   监管局      │
│  (farmers)   │ <──────────────── │  (manager)   │
│              │    审核许可证      │              │
└──────┬───────┘                   └──────┬───────┘
       │                                  │
       │                                  │
       │         ┌──────────────┐         │
       │         │              │         │
       └────────>│   管理员      │<────────┘
                 │   (admin)    │
                 │              │
                 └──────────────┘
                   - 审批监管局注册
                   - 管理绑定关系
                   - 查看所有数据
```

---

## 六、修改文件清单

| 文件路径 | 修改类型 | 说明 |
|----------|----------|------|
| `database-schema.sql` | 修改 | 新增 managerId 字段定义 |
| `CompanyCert.java` | 修改 | 新增 managerId 属性 |
| `CompanyCertResp.java` | 修改 | 新增 managerId、managerName 属性 |
| `WaterService.java` | 修改 | 修改 commit/verify/managerAllQuery/companyQuery 方法 |
| `WaterApi.java` | 无需修改 | API接口不变 |
| 前端页面 | 无需修改 | API调用不变，后端逻辑调整 |

---

## 七、测试要点

### 7.1 单元测试

| 测试场景 | 预期结果 |
|----------|----------|
| 养殖户未绑定监管局时提交许可证 | 返回错误："您还未绑定监管局" |
| 养殖户已绑定监管局时提交许可证 | 成功创建记录，managerId正确填充 |
| 养殖户重复提交许可证（同一监管局） | 返回错误："您已提交过许可证" |
| 监管局查询许可证列表 | 只返回自己管辖范围内的许可证 |
| 监管局审核非自己管辖的许可证 | 返回错误："许可证不存在或无权审核" |
| 监管局审核自己管辖的许可证 | 成功更新状态，通知养殖户 |

### 7.2 集成测试

1. 完整流程测试：养殖户注册 → 绑定监管局 → 提交许可证 → 监管局审核
2. 数据迁移测试：验证现有许可证数据正确关联 managerId

---

## 八、风险评估

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 现有许可证数据无 managerId | 监管局无法审核历史数据 | 执行数据迁移脚本 |
| 养殖户解绑后许可证状态 | 已提交的许可证仍归原监管局审核 | 业务逻辑不变，解绑不影响已提交记录 |
| 监管局被禁用 | 其管辖的许可证无人审核 | 管理员可重新分配或启用监管局 |

---

## 九、实施计划

| 阶段 | 任务 | 预估时间 |
|------|------|----------|
| 1 | 执行数据库DDL，新增字段 | 5分钟 |
| 2 | 执行数据迁移脚本 | 5分钟 |
| 3 | 修改实体类和DTO | 10分钟 |
| 4 | 修改WaterService | 30分钟 |
| 5 | 单元测试 | 20分钟 |
| 6 | 集成测试 | 15分钟 |

**总计：约1.5小时**

---

> **文档结束**
