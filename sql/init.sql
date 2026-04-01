-- ============================================================
-- 装备通用质量特性六性分析系统 - 数据库初始化脚本
-- 标准: GJB 450A / GJB 368B / GJB 900
-- ============================================================

CREATE DATABASE IF NOT EXISTS six_characteristics
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE six_characteristics;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(64)  NOT NULL COMMENT '姓名',
  `account`     VARCHAR(64)  NOT NULL UNIQUE COMMENT '账号',
  `password`    VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
  `role`        VARCHAR(32)  NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN/USER/ANALYST',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 装备表
CREATE TABLE IF NOT EXISTS `equipment` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(128)  NOT NULL COMMENT '装备名称',
  `model`       VARCHAR(128)  NOT NULL COMMENT '型号',
  `structure`   TEXT          COMMENT '结构描述',
  `parts`       TEXT          COMMENT '组成部件(JSON)',
  `remark`      TEXT          COMMENT '备注',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装备信息表';

-- 可靠性分析表 (GJB 450A)
CREATE TABLE IF NOT EXISTS `reliability` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT,
  `equipment_id`      BIGINT        NOT NULL COMMENT '装备ID',
  `mtbf`              DOUBLE        COMMENT '平均故障间隔时间(h)',
  `rate`              DOUBLE        COMMENT '可靠度',
  `reliability_curve` TEXT          COMMENT '可靠性曲线数据(JSON)',
  `params`            TEXT          COMMENT '分布参数(JSON): {type,alpha,beta}',
  `result`            TEXT          COMMENT '分析结果(JSON)',
  `create_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='可靠性分析表';

-- 维修性分析表 (GJB 368B)
CREATE TABLE IF NOT EXISTS `maintainability` (
  `id`           BIGINT   NOT NULL AUTO_INCREMENT,
  `equipment_id` BIGINT   NOT NULL COMMENT '装备ID',
  `mttr`         DOUBLE   COMMENT '平均修复时间(h)',
  `repair_rate`  DOUBLE   COMMENT '修复率',
  `process`      TEXT     COMMENT '维修流程(JSON)',
  `result`       TEXT     COMMENT '分析结果(JSON)',
  `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修性分析表';

-- 测试性分析表
CREATE TABLE IF NOT EXISTS `testability` (
  `id`           BIGINT   NOT NULL AUTO_INCREMENT,
  `equipment_id` BIGINT   NOT NULL COMMENT '装备ID',
  `fdr`          DOUBLE   COMMENT '故障检测率(%)',
  `fir`          DOUBLE   COMMENT '故障隔离率(%)',
  `test_point`   TEXT     COMMENT '测试点配置(JSON)',
  `result`       TEXT     COMMENT '分析结果(JSON)',
  `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试性分析表';

-- 保障性分析表
CREATE TABLE IF NOT EXISTS `supportability` (
  `id`           BIGINT   NOT NULL AUTO_INCREMENT,
  `equipment_id` BIGINT   NOT NULL COMMENT '装备ID',
  `resource`     TEXT     COMMENT '保障资源(JSON)',
  `cost`         DOUBLE   COMMENT '保障费用(万元)',
  `support_rate` DOUBLE   COMMENT '保障率(%)',
  `result`       TEXT     COMMENT '分析结果(JSON)',
  `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保障性分析表';

-- 安全性分析表 (GJB 900)
CREATE TABLE IF NOT EXISTS `safety` (
  `id`           BIGINT   NOT NULL AUTO_INCREMENT,
  `equipment_id` BIGINT   NOT NULL COMMENT '装备ID',
  `fta_tree`     LONGTEXT COMMENT '故障树结构(JSON)',
  `min_cut_set`  TEXT     COMMENT '最小割集(JSON)',
  `failure_prob` DOUBLE   COMMENT '顶事件失效概率',
  `result`       TEXT     COMMENT '分析结果(JSON)',
  `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全性分析表';

-- 环境适应性分析表
CREATE TABLE IF NOT EXISTS `environment` (
  `id`           BIGINT   NOT NULL AUTO_INCREMENT,
  `equipment_id` BIGINT   NOT NULL COMMENT '装备ID',
  `temp`         TEXT     COMMENT '温度参数(JSON): {min,max,cycle}',
  `vibration`    TEXT     COMMENT '振动参数(JSON): {freq,amplitude}',
  `humidity`     TEXT     COMMENT '湿度参数(JSON): {min,max}',
  `adapt_score`  DOUBLE   COMMENT '环境适应性综合评分(0-100)',
  `result`       TEXT     COMMENT '分析结果(JSON)',
  `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='环境适应性分析表';

-- 报告表
CREATE TABLE IF NOT EXISTS `report` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `equipment_id` BIGINT       NOT NULL COMMENT '装备ID',
  `report_type`  VARCHAR(32)  NOT NULL COMMENT '报告类型: RELIABILITY/MAINTAINABILITY/COMPREHENSIVE',
  `file_path`    VARCHAR(512) COMMENT '文件路径',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告表';

-- 初始管理员账号 (密码: Admin@123)
-- BCrypt 哈希由 Python 生成: bcrypt.hashpw(b'Admin@123', bcrypt.gensalt())
INSERT INTO `user` (`name`, `account`, `password`, `role`) VALUES
('系统管理员', 'admin', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.qJ5I8qQvQ5Q5WG', 'ADMIN'),
('分析工程师', 'analyst', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.qJ5I8qQvQ5Q5WG', 'ANALYST');

-- 示例装备数据
INSERT INTO `equipment` (`name`, `model`, `structure`, `parts`, `remark`) VALUES
('某型雷达系统', 'LD-2000A', '由天线阵列、发射机、接收机、信号处理机、显控台组成', '["天线阵列","发射机","接收机","信号处理机","显控台"]', '地面防空雷达'),
('某型通信设备', 'TX-500B', '由射频单元、基带处理单元、电源模块、控制单元组成', '["射频单元","基带处理单元","电源模块","控制单元"]', '战术通信设备'),
('某型火控系统', 'FC-300C', '由传感器组、计算机、执行机构、显示器组成', '["传感器组","计算机","执行机构","显示器"]', '车载火控系统');
