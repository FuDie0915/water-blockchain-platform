-- ============================================================================
-- 数据库迁移脚本：许可证表新增managerId字段
-- 版本：v1.0
-- 日期：2026-04-25
-- ============================================================================

-- 1. 新增 managerId 字段
ALTER TABLE `company_cert`
ADD COLUMN `managerId` BIGINT NULL COMMENT '监管局用户ID（审核方）' AFTER `userId`;

-- 2. 新增索引
ALTER TABLE `company_cert`
ADD KEY `idx_cert_manager` (`managerId`);

-- 3. 数据迁移：为已存在的许可证填充managerId（根据绑定关系）
UPDATE company_cert cc
INNER JOIN manager_farmer mf ON cc.userId = mf.farmerId AND mf.status = 1 AND mf.isDelete = 0
SET cc.managerId = mf.managerId
WHERE cc.managerId IS NULL;

-- ============================================================================
-- 验证脚本（可选执行）
-- ============================================================================

-- 查看迁移结果
-- SELECT cc.id, cc.userId, cc.managerId, u.userName as farmerName, mu.userName as managerName
-- FROM company_cert cc
-- LEFT JOIN user u ON cc.userId = u.userId
-- LEFT JOIN user mu ON cc.managerId = mu.userId;
