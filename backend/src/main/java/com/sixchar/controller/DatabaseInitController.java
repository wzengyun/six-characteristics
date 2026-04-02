package com.sixchar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据库初始化控制器
 */
@RestController
@RequestMapping("/api/database")
public class DatabaseInitController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/init")
    public String initDatabase() {
        try {
            // 创建用户表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS \"user\" (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "name VARCHAR(64) NOT NULL, " +
                "account VARCHAR(64) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, " +
                "role VARCHAR(32) NOT NULL DEFAULT 'USER', " +
                "create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");

            // 创建装备表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS equipment (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "name VARCHAR(128) NOT NULL, " +
                "model VARCHAR(128) NOT NULL, " +
                "structure TEXT, " +
                "parts TEXT, " +
                "remark TEXT, " +
                "create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");

            // 创建可靠性分析表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS reliability (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "equipment_id BIGINT NOT NULL, " +
                "mtbf DOUBLE PRECISION, " +
                "rate DOUBLE PRECISION, " +
                "reliability_curve TEXT, " +
                "params TEXT, " +
                "result TEXT, " +
                "create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");

            // 创建维修性分析表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS maintainability (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "equipment_id BIGINT NOT NULL, " +
                "mttr DOUBLE PRECISION, " +
                "max_time DOUBLE PRECISION, " +
                "preventive_time DOUBLE PRECISION, " +
                "curve_data TEXT, " +
                "result TEXT, " +
                "create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");

            // 创建测试性分析表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS testability (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "equipment_id BIGINT NOT NULL, " +
                "fdr DOUBLE PRECISION, " +
                "fir DOUBLE PRECISION, " +
                "far DOUBLE PRECISION, " +
                "detection_data TEXT, " +
                "isolation_data TEXT, " +
                "result TEXT, " +
                "create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");

            // 创建安全性分析表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS safety (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "equipment_id BIGINT NOT NULL, " +
                "hazard_level VARCHAR(32), " +
                "risk_score DOUBLE PRECISION, " +
                "fault_tree TEXT, " +
                "risk_data TEXT, " +
                "result TEXT, " +
                "create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");

            // 创建保障性分析表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS supportability (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "equipment_id BIGINT NOT NULL, " +
                "mlrs DOUBLE PRECISION, " +
                "spares_data TEXT, " +
                "support_data TEXT, " +
                "result TEXT, " +
                "create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");

            // 创建环境适应性分析表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS environment (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "equipment_id BIGINT NOT NULL, " +
                "temp_score DOUBLE PRECISION, " +
                "vibration_score DOUBLE PRECISION, " +
                "humidity_score DOUBLE PRECISION, " +
                "overall_score DOUBLE PRECISION, " +
                "detail_data TEXT, " +
                "result TEXT, " +
                "create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");

            // 创建综合分析表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS analysis (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "equipment_id BIGINT NOT NULL, " +
                "reliability_score DOUBLE PRECISION, " +
                "maintainability_score DOUBLE PRECISION, " +
                "testability_score DOUBLE PRECISION, " +
                "supportability_score DOUBLE PRECISION, " +
                "safety_score DOUBLE PRECISION, " +
                "environment_score DOUBLE PRECISION, " +
                "overall_score DOUBLE PRECISION, " +
                "create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");

            // 插入默认用户（密码：Admin@123）
            int userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM \"user\" WHERE account = 'admin'", Integer.class);
            
            if (userCount == 0) {
                jdbcTemplate.execute("INSERT INTO \"user\" (name, account, password, role) VALUES " +
                    "('系统管理员', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN'), " +
                    "('分析工程师', 'analyst', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ANALYST')");

                // 插入示例装备
                jdbcTemplate.execute("INSERT INTO equipment (name, model, structure, parts, remark) VALUES " +
                    "('某型雷达系统', 'RL-2026A', '相控阵雷达，包含天线阵列、信号处理单元、电源系统', '[\"天线阵列\", \"信号处理器\", \"电源模块\", \"控制单元\"]', '某型地面雷达系统'), " +
                    "('某型通信设备', 'COM-2026B', '战术通信电台，支持多频段通信', '[\"发射机\", \"接收机\", \"天线\", \"电源\"]', '战术通信设备'), " +
                    "('某型导航系统', 'NAV-2026C', '惯性导航与GPS组合导航系统', '[\"惯性测量单元\", \"GPS接收机\", \"导航计算机\", \"显示屏\"]', '组合导航系统')");
            }

            return "数据库初始化成功！";
        } catch (Exception e) {
            return "数据库初始化失败: " + e.getMessage();
        }
    }
}
