-- ============================================================================
-- 海水养殖区块链水质监管平台 - 数据库初始化脚本
-- Database: water_platform
-- MySQL 8.0+
-- Charset: utf8mb4 / Collation: utf8mb4_general_ci
-- Generated: 2026-04-19
-- Total tables: 14
-- ============================================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET collation_connection = 'utf8mb4_general_ci';

-- ============================================================================
-- 1. user - 用户表
-- 存储系统所有用户：管理员(admin)、养殖户(farmers)、监管局(manager)
-- ============================================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `userId`          BIGINT        NOT NULL                                  COMMENT '用户ID（雪花算法）',
    `userAccount`     VARCHAR(64)   NOT NULL                                  COMMENT '登录账号（唯一）',
    `userPassword`    VARCHAR(255)  NOT NULL                                  COMMENT '密码（MD5+salt格式: salt$hash）',
    `userName`        VARCHAR(64)   NULL                                      COMMENT '用户名称（企业名称/监管局名称）',
    `userRole`        VARCHAR(32)   NOT NULL                                  COMMENT '角色类型: admin/farmers/manager',
    `accountAddress`  VARCHAR(128)  NULL                                      COMMENT '区块链账户地址',
    `privateKey`      VARCHAR(255)  NULL                                      COMMENT '区块链账户私钥',
    `userStatus`      INT           NOT NULL DEFAULT 1                        COMMENT '用户状态: 0=禁用, 1=启用',
    `extInfo`         TEXT          NULL                                      COMMENT '扩展信息（JSON格式）',
    PRIMARY KEY (`userId`),
    UNIQUE KEY `uk_user_account` (`userAccount`),
    KEY `idx_user_role` (`userRole`),
    KEY `idx_user_status` (`userStatus`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ============================================================================
-- 2. water_data - 水质数据表
-- 存储IoT自动采集数据(TDS/浑浊度)和人工录入数据(水温/盐度/pH/溶解氧/氨氮/亚硝酸盐)
-- ============================================================================
DROP TABLE IF EXISTS `water_data`;
CREATE TABLE `water_data` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT                    COMMENT '主键ID',
    `userId`       BIGINT        NOT NULL                                  COMMENT '用户ID（养殖户）',
    `dataType`     INT           NOT NULL                                  COMMENT '数据类型: 0=TDS, 1=浑浊度(IoT); 2=水温, 3=盐度, 4=pH, 5=溶解氧, 6=氨氮, 7=亚硝酸盐(人工)',
    `data`         VARCHAR(255)  NOT NULL                                  COMMENT '数据值',
    `status`       VARCHAR(32)   NULL                                      COMMENT '数据状态: 正常/异常',
    `hash`         VARCHAR(255)  NULL                                      COMMENT '区块链交易哈希',
    `time`         DATETIME      NULL                                      COMMENT '采集时间',
    `isOnChain`    TINYINT(1)    NOT NULL DEFAULT 0                        COMMENT '是否已上链: 0=未上链, 1=已上链',
    `auditStatus`  INT           NOT NULL DEFAULT 0                        COMMENT '审核状态: 0=待审核, 1=已通过, 2=已拒绝',
    PRIMARY KEY (`id`),
    KEY `idx_water_user_type` (`userId`, `dataType`),
    KEY `idx_water_time` (`time`),
    KEY `idx_water_audit` (`auditStatus`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='水质数据表';

-- ============================================================================
-- 3. company_cert - 企业许可证/资质证书表
-- 存储养殖户企业资质图片及审核、上链状态
-- ============================================================================
DROP TABLE IF EXISTS `company_cert`;
CREATE TABLE `company_cert` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT                    COMMENT '主键ID',
    `userId`       BIGINT        NOT NULL                                  COMMENT '用户ID（养殖户）',
    `imageUrl`     VARCHAR(512)  NULL                                      COMMENT '许可证图像路径',
    `status`       INT           NOT NULL DEFAULT 0                        COMMENT '审核状态: 0=待审批, 1=通过并上链, 2=不通过',
    `createTime`   DATETIME      NULL                                      COMMENT '创建时间',
    `chainTxHash`  VARCHAR(255)  NULL                                      COMMENT '区块链交易哈希',
    PRIMARY KEY (`id`),
    KEY `idx_cert_user` (`userId`),
    KEY `idx_cert_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='企业许可证/资质证书表';

-- ============================================================================
-- 4. pond - 养殖池塘表
-- 存储池塘基本信息、实时水质指标、审核及上链状态
-- ============================================================================
DROP TABLE IF EXISTS `pond`;
CREATE TABLE `pond` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT                    COMMENT '主键ID',
    `userId`      BIGINT        NOT NULL                                  COMMENT '用户ID（养殖户）',
    `pondName`    VARCHAR(128)  NOT NULL                                  COMMENT '塘名',
    `breedType`   VARCHAR(128)  NULL                                      COMMENT '养殖品种',
    `area`        DOUBLE        NULL                                      COMMENT '养殖面积（亩）',
    `depth`       DOUBLE        NULL                                      COMMENT '水深（米）',
    `startDate`   DATETIME      NULL                                      COMMENT '养殖周期开始时间',
    `endDate`     DATETIME      NULL                                      COMMENT '养殖周期结束时间',
    `status`      INT           NOT NULL DEFAULT 0                        COMMENT '池塘状态: 0=空闲, 1=养殖中, 2=休整',
    `waterTemp`   DOUBLE        NULL                                      COMMENT '水温测量值（℃）',
    `salinity`    DOUBLE        NULL                                      COMMENT '盐度测量值',
    `ph`          DOUBLE        NULL                                      COMMENT 'pH测量值',
    `doValue`     DOUBLE        NULL                                      COMMENT '溶解氧测量值（mg/L）',
    `nh3n`        DOUBLE        NULL                                      COMMENT '氨氮测量值（mg/L）',
    `no2`         DOUBLE        NULL                                      COMMENT '亚硝酸盐测量值（mg/L）',
    `createTime`  DATETIME      NULL                                      COMMENT '创建时间',
    `updateTime`  DATETIME      NULL                                      COMMENT '更新时间',
    `auditStatus` INT           NOT NULL DEFAULT 0                        COMMENT '审核状态: 0=待审核, 1=已通过, 2=已拒绝',
    `isDelete`    INT           NOT NULL DEFAULT 0                        COMMENT '逻辑删除: 0=未删除, 1=已删除',
    `chainTxHash` VARCHAR(255)  NULL                                      COMMENT '区块链交易哈希',
    PRIMARY KEY (`id`),
    KEY `idx_pond_user` (`userId`),
    KEY `idx_pond_status` (`status`),
    KEY `idx_pond_audit` (`auditStatus`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='养殖池塘表';

-- ============================================================================
-- 5. seed_record - 苗种投放记录表
-- ============================================================================
DROP TABLE IF EXISTS `seed_record`;
CREATE TABLE `seed_record` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT                    COMMENT '主键ID',
    `pondId`      BIGINT        NOT NULL                                  COMMENT '池塘ID',
    `userId`      BIGINT        NOT NULL                                  COMMENT '用户ID（养殖户）',
    `recordDate`  DATETIME      NULL                                      COMMENT '投放日期',
    `manager`     VARCHAR(64)   NULL                                      COMMENT '责任人',
    `seedType`    VARCHAR(128)  NULL                                      COMMENT '苗种品种',
    `seedSpec`    VARCHAR(128)  NULL                                      COMMENT '苗种规格',
    `weight`      DOUBLE        NULL                                      COMMENT '投放重量（kg）',
    `remark`      VARCHAR(512)  NULL                                      COMMENT '备注',
    `createTime`  DATETIME      NULL                                      COMMENT '创建时间',
    `auditStatus` INT           NOT NULL DEFAULT 0                        COMMENT '审核状态: 0=待审核, 1=已通过, 2=已拒绝',
    `isDelete`    INT           NOT NULL DEFAULT 0                        COMMENT '逻辑删除: 0=未删除, 1=已删除',
    `chainTxHash` VARCHAR(255)  NULL                                      COMMENT '区块链交易哈希',
    PRIMARY KEY (`id`),
    KEY `idx_seed_pond` (`pondId`),
    KEY `idx_seed_user` (`userId`),
    KEY `idx_seed_audit` (`auditStatus`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='苗种投放记录表';

-- ============================================================================
-- 6. feed_record - 投喂记录表
-- ============================================================================
DROP TABLE IF EXISTS `feed_record`;
CREATE TABLE `feed_record` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT                    COMMENT '主键ID',
    `pondId`      BIGINT        NOT NULL                                  COMMENT '池塘ID',
    `userId`      BIGINT        NOT NULL                                  COMMENT '用户ID（养殖户）',
    `feedDate`    DATETIME      NULL                                      COMMENT '投喂日期',
    `feedBrand`   VARCHAR(128)  NULL                                      COMMENT '饲料品牌',
    `feedAmount`  DOUBLE        NULL                                      COMMENT '投喂用量（kg）',
    `manager`     VARCHAR(64)   NULL                                      COMMENT '责任人',
    `remark`      VARCHAR(512)  NULL                                      COMMENT '备注',
    `createTime`  DATETIME      NULL                                      COMMENT '创建时间',
    `auditStatus` INT           NOT NULL DEFAULT 0                        COMMENT '审核状态: 0=待审核, 1=已通过, 2=已拒绝',
    `isDelete`    INT           NOT NULL DEFAULT 0                        COMMENT '逻辑删除: 0=未删除, 1=已删除',
    `chainTxHash` VARCHAR(255)  NULL                                      COMMENT '区块链交易哈希',
    PRIMARY KEY (`id`),
    KEY `idx_feed_pond` (`pondId`),
    KEY `idx_feed_user` (`userId`),
    KEY `idx_feed_audit` (`auditStatus`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='投喂记录表';

-- ============================================================================
-- 7. medicine_record - 用药记录表
-- ============================================================================
DROP TABLE IF EXISTS `medicine_record`;
CREATE TABLE `medicine_record` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT                COMMENT '主键ID',
    `pondId`          BIGINT        NOT NULL                               COMMENT '池塘ID',
    `userId`          BIGINT        NOT NULL                               COMMENT '用户ID（养殖户）',
    `medicineDate`    DATETIME      NULL                                   COMMENT '用药日期',
    `manager`         VARCHAR(64)   NULL                                   COMMENT '责任人',
    `medicineName`    VARCHAR(128)  NULL                                   COMMENT '药品通用名',
    `purpose`         VARCHAR(64)   NULL                                   COMMENT '用药目的: 预防/治疗/消毒',
    `dosage`          VARCHAR(128)  NULL                                   COMMENT '用药剂量',
    `withdrawalStart` DATETIME      NULL                                   COMMENT '休药期开始时间',
    `withdrawalEnd`   DATETIME      NULL                                   COMMENT '休药期结束时间',
    `remark`          VARCHAR(512)  NULL                                   COMMENT '备注',
    `createTime`      DATETIME      NULL                                   COMMENT '创建时间',
    `auditStatus`     INT           NOT NULL DEFAULT 0                     COMMENT '审核状态: 0=待审核, 1=已通过, 2=已拒绝',
    `isDelete`        INT           NOT NULL DEFAULT 0                     COMMENT '逻辑删除: 0=未删除, 1=已删除',
    `chainTxHash`     VARCHAR(255)  NULL                                   COMMENT '区块链交易哈希',
    PRIMARY KEY (`id`),
    KEY `idx_medicine_pond` (`pondId`),
    KEY `idx_medicine_user` (`userId`),
    KEY `idx_medicine_audit` (`auditStatus`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用药记录表';

-- ============================================================================
-- 8. harvest_record - 收获记录表
-- ============================================================================
DROP TABLE IF EXISTS `harvest_record`;
CREATE TABLE `harvest_record` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT                  COMMENT '主键ID',
    `pondId`        BIGINT        NOT NULL                                 COMMENT '池塘ID',
    `userId`        BIGINT        NOT NULL                                 COMMENT '用户ID（养殖户）',
    `harvestDate`   DATETIME      NULL                                     COMMENT '收获日期',
    `manager`       VARCHAR(64)   NULL                                     COMMENT '责任人',
    `batchNo`       VARCHAR(64)   NULL                                     COMMENT '收获批次号',
    `spec`          VARCHAR(128)  NULL                                     COMMENT '品种规格',
    `totalWeight`   DOUBLE        NULL                                     COMMENT '总收获重量（kg）',
    `survivalRate`  DOUBLE        NULL                                     COMMENT '成活率（%）',
    `buyerInfo`     VARCHAR(256)  NULL                                     COMMENT '收购方信息',
    `qualityResult` VARCHAR(256)  NULL                                     COMMENT '质量检测结果',
    `remark`        VARCHAR(512)  NULL                                     COMMENT '备注',
    `createTime`    DATETIME      NULL                                     COMMENT '创建时间',
    `auditStatus`   INT           NOT NULL DEFAULT 0                       COMMENT '审核状态: 0=待审核, 1=已通过, 2=已拒绝',
    `isDelete`      INT           NOT NULL DEFAULT 0                       COMMENT '逻辑删除: 0=未删除, 1=已删除',
    `chainTxHash`   VARCHAR(255)  NULL                                     COMMENT '区块链交易哈希',
    PRIMARY KEY (`id`),
    KEY `idx_harvest_pond` (`pondId`),
    KEY `idx_harvest_user` (`userId`),
    KEY `idx_harvest_audit` (`auditStatus`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收获记录表';

-- ============================================================================
-- 9. manager_farmer - 监管局-养殖户绑定关系表
-- ============================================================================
DROP TABLE IF EXISTS `manager_farmer`;
CREATE TABLE `manager_farmer` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT                    COMMENT '主键ID',
    `managerId`   BIGINT        NOT NULL                                  COMMENT '监管局用户ID',
    `farmerId`    BIGINT        NOT NULL                                  COMMENT '养殖户用户ID',
    `status`      INT           NOT NULL DEFAULT 0                        COMMENT '绑定状态: 0=待审核, 1=已通过, 2=已拒绝',
    `createTime`  DATETIME      NULL                                      COMMENT '创建时间',
    `updateTime`  DATETIME      NULL                                      COMMENT '更新时间',
    `isDelete`    INT           NOT NULL DEFAULT 0                        COMMENT '逻辑删除: 0=未删除, 1=已删除',
    `chainTxHash` VARCHAR(255)  NULL                                      COMMENT '区块链交易哈希',
    PRIMARY KEY (`id`),
    KEY `idx_mf_manager` (`managerId`),
    KEY `idx_mf_farmer` (`farmerId`),
    KEY `idx_mf_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='监管局-养殖户绑定关系表';

-- ============================================================================
-- 10. manager_audit - 监管局审核申请表
-- 存储监管局用户提交的资质认证申请及管理员审核结果
-- ============================================================================
DROP TABLE IF EXISTS `manager_audit`;
CREATE TABLE `manager_audit` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT                COMMENT '主键ID',
    `userId`          BIGINT        NOT NULL                               COMMENT '监管局用户ID',
    `institutionName` VARCHAR(128)  NULL                                   COMMENT '机构名称',
    `jurisdiction`    VARCHAR(128)  NULL                                   COMMENT '管辖区域',
    `phone`           VARCHAR(32)   NULL                                   COMMENT '联系电话',
    `remark`          VARCHAR(512)  NULL                                   COMMENT '申请备注',
    `status`          INT           NOT NULL DEFAULT 0                     COMMENT '审核状态: 0=待审核, 1=已通过, 2=已拒绝',
    `auditRemark`     VARCHAR(512)  NULL                                   COMMENT '管理员审核备注',
    `auditUserId`     BIGINT        NULL                                   COMMENT '审核管理员ID',
    `createTime`      DATETIME      NULL                                   COMMENT '申请时间',
    `updateTime`      DATETIME      NULL                                   COMMENT '审核时间',
    `chainTxHash`     VARCHAR(255)  NULL                                   COMMENT '区块链交易哈希',
    PRIMARY KEY (`id`),
    KEY `idx_ma_user` (`userId`),
    KEY `idx_ma_status` (`status`),
    KEY `idx_ma_audit_user` (`auditUserId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='监管局审核申请表';

-- ============================================================================
-- 11. warning_threshold - 预警阈值配置表
-- 每个监管局用户可配置其管辖范围内水质指标的预警阈值范围
-- ============================================================================
DROP TABLE IF EXISTS `warning_threshold`;
CREATE TABLE `warning_threshold` (
    `id`           BIGINT  NOT NULL AUTO_INCREMENT                        COMMENT '主键ID',
    `managerId`    BIGINT  NOT NULL                                        COMMENT '监管局用户ID',
    `waterTempMin` DOUBLE  NULL                                            COMMENT '水温下限（℃）',
    `waterTempMax` DOUBLE  NULL                                            COMMENT '水温上限（℃）',
    `salinityMin`  DOUBLE  NULL                                            COMMENT '盐度下限',
    `salinityMax`  DOUBLE  NULL                                            COMMENT '盐度上限',
    `phMin`        DOUBLE  NULL                                            COMMENT 'pH下限',
    `phMax`        DOUBLE  NULL                                            COMMENT 'pH上限',
    `doMin`        DOUBLE  NULL                                            COMMENT '溶解氧下限（mg/L）',
    `doMax`        DOUBLE  NULL                                            COMMENT '溶解氧上限（mg/L）',
    `nh3nMin`      DOUBLE  NULL                                            COMMENT '氨氮下限（mg/L）',
    `nh3nMax`      DOUBLE  NULL                                            COMMENT '氨氮上限（mg/L）',
    `no2Min`       DOUBLE  NULL                                            COMMENT '亚硝酸盐下限（mg/L）',
    `no2Max`       DOUBLE  NULL                                            COMMENT '亚硝酸盐上限（mg/L）',
    `createTime`   DATETIME NULL                                            COMMENT '创建时间',
    `updateTime`   DATETIME NULL                                            COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_wt_manager` (`managerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预警阈值配置表';

-- ============================================================================
-- 12. announcement - 公告表
-- ============================================================================
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT                    COMMENT '主键ID',
    `title`       VARCHAR(255)  NOT NULL                                  COMMENT '公告标题',
    `content`     TEXT          NULL                                      COMMENT '公告内容',
    `targetRole`  VARCHAR(32)   NULL                                      COMMENT '目标角色: farmers/manager/all',
    `publisherId` BIGINT        NULL                                      COMMENT '发布人ID',
    `createTime`  DATETIME      NULL                                      COMMENT '发布时间',
    `chainTxHash` VARCHAR(255)  NULL                                      COMMENT '区块链交易哈希',
    PRIMARY KEY (`id`),
    KEY `idx_ann_role` (`targetRole`),
    KEY `idx_ann_publisher` (`publisherId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告表';

-- ============================================================================
-- 13. user_contract - 用户合约表
-- 存储用户部署/绑定的智能合约信息（复合主键: userId + contractType）
-- ============================================================================
DROP TABLE IF EXISTS `user_contract`;
CREATE TABLE `user_contract` (
    `userId`          BIGINT        NOT NULL                              COMMENT '用户ID',
    `contractType`    VARCHAR(64)   NOT NULL                              COMMENT '合约类型',
    `contractName`    VARCHAR(128)  NULL                                  COMMENT '合约名称',
    `contractAddress` VARCHAR(255)  NULL                                  COMMENT '合约部署地址',
    `contractAbi`     TEXT          NULL                                  COMMENT '合约ABI（JSON）',
    `contractBin`     TEXT          NULL                                  COMMENT '合约字节码（Binary）',
    PRIMARY KEY (`userId`, `contractType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户合约表';

-- ============================================================================
-- 14. notification - 站内信通知表
-- ============================================================================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id`         BIGINT        NOT NULL AUTO_INCREMENT                    COMMENT '主键ID',
    `userId`     BIGINT        NOT NULL                                  COMMENT '接收者用户ID',
    `title`      VARCHAR(255)  NULL                                      COMMENT '通知标题',
    `content`    TEXT          NULL                                      COMMENT '通知内容',
    `type`       VARCHAR(32)   NOT NULL                                  COMMENT '通知类型: AUDIT/WARNING/ANNOUNCEMENT/BINDING',
    `relatedId`  BIGINT        NULL                                      COMMENT '关联业务ID',
    `isRead`     INT           NOT NULL DEFAULT 0                        COMMENT '是否已读: 0=未读, 1=已读',
    `createTime` DATETIME      NULL                                      COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_noti_user` (`userId`),
    KEY `idx_noti_type` (`type`),
    KEY `idx_noti_read` (`isRead`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='站内信通知表';

-- ============================================================================
-- 初始数据: 管理员账户
-- 密码: 123456, MD5+salt格式
-- salt: admin2024, MD5(admin2024123456) = a66abb56816c4577889a98ac5efab959
-- 格式: salt$hash
-- ============================================================================
INSERT INTO `user` (`userId`, `userAccount`, `userPassword`, `userName`, `userRole`, `accountAddress`, `privateKey`, `userStatus`, `extInfo`)
VALUES (1, 'admin', 'admin2024$a66abb56816c4577889a98ac5efab959', '系统管理员', 'admin', NULL, NULL, 1, NULL);
