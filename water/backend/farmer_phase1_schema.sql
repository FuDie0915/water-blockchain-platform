SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 养殖户端一期扩展表设计（在现有 blockchain.sql 基础上新增）
-- 覆盖：阈值规则、养殖池、预警事件、统一存证台账
-- ============================================

DROP TABLE IF EXISTS `water_metric_rule`;
CREATE TABLE `water_metric_rule`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `metricCode` varchar(32) NOT NULL,
  `metricName` varchar(64) NOT NULL,
  `unit` varchar(16) DEFAULT NULL,
  `safeMin` decimal(10,2) DEFAULT NULL,
  `safeMax` decimal(10,2) DEFAULT NULL,
  `warnMin` decimal(10,2) DEFAULT NULL,
  `warnMax` decimal(10,2) DEFAULT NULL,
  `dangerMin` decimal(10,2) DEFAULT NULL,
  `dangerMax` decimal(10,2) DEFAULT NULL,
  `standardSource` varchar(255) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_metric_code` (`metricCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `water_metric_rule` (`metricCode`, `metricName`, `unit`, `safeMin`, `safeMax`, `warnMin`, `warnMax`, `dangerMin`, `dangerMax`, `standardSource`, `remark`) VALUES
('WATER_TEMP', '水温', '℃', 22.00, 30.00, 18.00, 32.00, 18.00, 32.00, 'IoT水质检测阈值参考', '低于18或高于32视为危险'),
('SALINITY', '盐度', '‰', 18.00, 32.00, 15.00, 35.00, 15.00, 35.00, 'IoT水质检测阈值参考', '适用于海水养殖一期通用区间'),
('PH', 'pH', '', 7.80, 8.60, 7.50, 9.00, 7.50, 9.00, 'IoT水质检测阈值参考', '偏酸或偏碱均需预警'),
('DO', '溶解氧', 'mg/L', 5.00, 99.00, 3.00, 5.00, 0.00, 3.00, 'IoT水质检测阈值参考', '低于3需立即处理'),
('AMMONIA_N', '氨氮', 'mg/L', 0.00, 0.20, 0.20, 0.50, 0.50, 99.00, 'IoT水质检测阈值参考', '高氨氮易引发中毒风险'),
('NITRITE', '亚硝酸盐', 'mg/L', 0.00, 0.10, 0.10, 0.20, 0.20, 99.00, 'IoT水质检测阈值参考', '高于0.2需重点处置'),
('TURBIDITY', '浑浊度', 'NTU', 0.00, 20.00, 20.00, 50.00, 50.00, 999.00, '当前项目兼容指标', '兼容旧演示模型'),
('TDS', '总溶解固体', 'mg/L', 30000.00, 38000.00, 25000.00, 42000.00, 0.00, 99999.00, '当前项目兼容指标', '兼容旧演示模型');

DROP TABLE IF EXISTS `pond`;
CREATE TABLE `pond`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) NOT NULL,
  `pondName` varchar(100) NOT NULL,
  `pondCode` varchar(64) DEFAULT NULL,
  `areaSize` decimal(10,2) DEFAULT NULL,
  `depth` decimal(10,2) DEFAULT NULL,
  `breedType` varchar(100) DEFAULT NULL,
  `status` varchar(32) DEFAULT 'running',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `warning_event`;
CREATE TABLE `warning_event`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) NOT NULL,
  `pondId` bigint(20) DEFAULT NULL,
  `metricCode` varchar(32) DEFAULT NULL,
  `warningType` varchar(32) NOT NULL,
  `warningLevel` varchar(16) NOT NULL,
  `currentValue` varchar(64) DEFAULT NULL,
  `detail` varchar(500) DEFAULT NULL,
  `handleStatus` varchar(32) DEFAULT 'pending',
  `handleRemark` varchar(500) DEFAULT NULL,
  `isOnChain` tinyint(1) NOT NULL DEFAULT 0,
  `hash` varchar(255) DEFAULT NULL,
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `blockchain_evidence`;
CREATE TABLE `blockchain_evidence`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) NOT NULL,
  `pondId` bigint(20) DEFAULT NULL,
  `businessType` varchar(64) NOT NULL,
  `businessId` bigint(20) DEFAULT NULL,
  `evidenceNo` varchar(64) NOT NULL,
  `hash` varchar(255) DEFAULT NULL,
  `onChainStatus` varchar(32) NOT NULL DEFAULT 'pending',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_evidence_no` (`evidenceNo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
