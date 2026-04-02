-- ============================================================
-- 装备通用质量特性六性分析系统 - PostgreSQL 数据库初始化脚本
-- 标准: GJB 450A / GJB 368B / GJB 900
-- ============================================================

-- 用户表
CREATE TABLE IF NOT EXISTS "user" (
  id          BIGSERIAL PRIMARY KEY,
  name        VARCHAR(64)  NOT NULL,
  account     VARCHAR(64)  NOT NULL UNIQUE,
  password    VARCHAR(255) NOT NULL,
  role        VARCHAR(32)  NOT NULL DEFAULT 'USER',
  create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 装备表
CREATE TABLE IF NOT EXISTS equipment (
  id          BIGSERIAL PRIMARY KEY,
  name        VARCHAR(128) NOT NULL,
  model       VARCHAR(128) NOT NULL,
  structure   TEXT,
  parts       TEXT,
  remark      TEXT,
  create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 可靠性分析表 (GJB 450A)
CREATE TABLE IF NOT EXISTS reliability (
  id                BIGSERIAL PRIMARY KEY,
  equipment_id      BIGINT       NOT NULL,
  mtbf              DOUBLE PRECISION,
  rate              DOUBLE PRECISION,
  reliability_curve TEXT,
  params            TEXT,
  result            TEXT,
  create_time       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reliability_equipment_id ON reliability(equipment_id);

-- 维修性分析表 (GJB 368B)
CREATE TABLE IF NOT EXISTS maintainability (
  id                BIGSERIAL PRIMARY KEY,
  equipment_id      BIGINT       NOT NULL,
  mttr              DOUBLE PRECISION,
  max_time          DOUBLE PRECISION,
  preventive_time   DOUBLE PRECISION,
  curve_data        TEXT,
  result            TEXT,
  create_time       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_maintainability_equipment_id ON maintainability(equipment_id);

-- 测试性分析表 (GJB 2547A)
CREATE TABLE IF NOT EXISTS testability (
  id                BIGSERIAL PRIMARY KEY,
  equipment_id      BIGINT       NOT NULL,
  fdr               DOUBLE PRECISION,
  fir               DOUBLE PRECISION,
  far               DOUBLE PRECISION,
  detection_data    TEXT,
  isolation_data    TEXT,
  result            TEXT,
  create_time       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_testability_equipment_id ON testability(equipment_id);

-- 安全性分析表 (GJB 900)
CREATE TABLE IF NOT EXISTS safety (
  id                BIGSERIAL PRIMARY KEY,
  equipment_id      BIGINT       NOT NULL,
  hazard_level      VARCHAR(32),
  risk_score        DOUBLE PRECISION,
  fault_tree        TEXT,
  risk_data         TEXT,
  result            TEXT,
  create_time       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_safety_equipment_id ON safety(equipment_id);

-- 保障性分析表 (GJB 3847A)
CREATE TABLE IF NOT EXISTS supportability (
  id                BIGSERIAL PRIMARY KEY,
  equipment_id      BIGINT       NOT NULL,
  mlrs              DOUBLE PRECISION,
  spares_data       TEXT,
  support_data      TEXT,
  result            TEXT,
  create_time       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_supportability_equipment_id ON supportability(equipment_id);

-- 环境适应性分析表 (GJB 150A)
CREATE TABLE IF NOT EXISTS environment (
  id                BIGSERIAL PRIMARY KEY,
  equipment_id      BIGINT       NOT NULL,
  temp_score        DOUBLE PRECISION,
  vibration_score   DOUBLE PRECISION,
  humidity_score    DOUBLE PRECISION,
  overall_score     DOUBLE PRECISION,
  detail_data       TEXT,
  result            TEXT,
  create_time       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_environment_equipment_id ON environment(equipment_id);

-- 综合分析表
CREATE TABLE IF NOT EXISTS analysis (
  id                BIGSERIAL PRIMARY KEY,
  equipment_id      BIGINT       NOT NULL,
  reliability_score DOUBLE PRECISION,
  maintainability_score DOUBLE PRECISION,
  testability_score DOUBLE PRECISION,
  supportability_score DOUBLE PRECISION,
  safety_score      DOUBLE PRECISION,
  environment_score DOUBLE PRECISION,
  overall_score     DOUBLE PRECISION,
  create_time       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_analysis_equipment_id ON analysis(equipment_id);

-- ============================================================
-- 初始化示例数据
-- ============================================================

-- 插入默认用户 (密码: Admin@123, Analyst@123)
INSERT INTO "user" (name, account, password, role) VALUES
('系统管理员', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN'),
('分析师', 'analyst', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ANALYST')
ON CONFLICT (account) DO NOTHING;

-- 插入示例装备
INSERT INTO equipment (name, model, structure, parts, remark) VALUES
('某型雷达系统', 'RL-2026A', '相控阵雷达，包含天线阵列、信号处理单元、电源系统', '["天线阵列", "信号处理器", "电源模块", "控制单元"]', '某型地面雷达系统'),
('某型通信设备', 'COM-2026B', '战术通信电台，支持多频段通信', '["发射机", "接收机", "天线", "电源"]', '战术通信设备'),
('某型导航系统', 'NAV-2026C', '惯性导航与GPS组合导航系统', '["惯性测量单元", "GPS接收机", "导航计算机", "显示屏"]', '组合导航系统')
ON CONFLICT DO NOTHING;
