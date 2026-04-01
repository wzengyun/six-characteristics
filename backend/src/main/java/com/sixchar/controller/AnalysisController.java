package com.sixchar.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sixchar.dto.R;
import com.sixchar.entity.*;
import com.sixchar.mapper.*;
import com.sixchar.service.SixAnalysisService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 六性综合分析控制器
 * 对标 GJB 450A / GJB 368B / GJB 900 标准
 */
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final SixAnalysisService analysisService;
    private final ReliabilityMapper reliabilityMapper;
    private final MaintainabilityMapper maintainabilityMapper;
    private final TestabilityMapper testabilityMapper;
    private final SupportabilityMapper supportabilityMapper;
    private final SafetyMapper safetyMapper;
    private final EnvironmentMapper environmentMapper;
    private final EquipmentMapper equipmentMapper;

    public AnalysisController(
            SixAnalysisService analysisService,
            ReliabilityMapper reliabilityMapper,
            MaintainabilityMapper maintainabilityMapper,
            TestabilityMapper testabilityMapper,
            SupportabilityMapper supportabilityMapper,
            SafetyMapper safetyMapper,
            EnvironmentMapper environmentMapper,
            EquipmentMapper equipmentMapper
    ) {
        this.analysisService = analysisService;
        this.reliabilityMapper = reliabilityMapper;
        this.maintainabilityMapper = maintainabilityMapper;
        this.testabilityMapper = testabilityMapper;
        this.supportabilityMapper = supportabilityMapper;
        this.safetyMapper = safetyMapper;
        this.environmentMapper = environmentMapper;
        this.equipmentMapper = equipmentMapper;
    }

    /**
     * 综合分析 - 获取某装备的六性评分
     */
    @GetMapping("/{equipmentId}")
    public R<?> getComprehensiveAnalysis(@PathVariable Long equipmentId) {
        Equipment eq = equipmentMapper.selectById(equipmentId);
        if (eq == null) return R.error("装备不存在");

        List<Map<String, Object>> detail = new ArrayList<>();

        // 可靠性
        Reliability r = reliabilityMapper.selectOne(
            new LambdaQueryWrapper<Reliability>().eq(Reliability::getEquipmentId, equipmentId)
        );
        if (r != null) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", "可靠性");
            m.put("color", "#00d4ff");
            m.put("score", analysisService.calcReliabilityScore(r));
            m.put("indicator", String.format("MTBF = %.0fh, R(t=1000h) = %.4f", r.getMtbf()!=null?r.getMtbf():2000.0, analysisService.reliabilityAt(r.getMtbf()!=null?r.getMtbf():2000.0, 1000.0)));
            m.put("status", analysisService.calcReliabilityScore(r) >= 85 ? "达标" : "待改进");
            m.put("suggestion", analysisService.calcReliabilityScore(r) >= 85 ? "保持现有维护策略" : "建议提升MTBF水平");
            detail.add(m);
        }

        // 维修性
        Maintainability mt = maintainabilityMapper.selectOne(
            new LambdaQueryWrapper<Maintainability>().eq(Maintainability::getEquipmentId, equipmentId)
        );
        if (mt != null) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", "维修性");
            m.put("color", "#4ade80");
            m.put("score", analysisService.calcMaintainabilityScore(mt));
            m.put("indicator", String.format("MTTR = %.1fh, 修复率 = %.4f", mt.getMttr()!=null?mt.getMttr():2.5, mt.getRepairRate()!=null?mt.getRepairRate():0.4));
            m.put("status", analysisService.calcMaintainabilityScore(mt) >= 80 ? "达标" : "待改进");
            m.put("suggestion", "优化维修流程");
            detail.add(m);
        }

        // 测试性
        Testability ts = testabilityMapper.selectOne(
            new LambdaQueryWrapper<Testability>().eq(Testability::getEquipmentId, equipmentId)
        );
        if (ts != null) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", "测试性");
            m.put("color", "#f59e0b");
            m.put("score", analysisService.calcTestabilityScore(ts));
            m.put("indicator", String.format("FDR = %.1f%%, FIR = %.1f%%", ts.getFdr()!=null?ts.getFdr():92, ts.getFir()!=null?ts.getFir():88));
            m.put("status", analysisService.calcTestabilityScore(ts) >= 80 ? "达标" : "待改进");
            m.put("suggestion", "增加测试点数量");
            detail.add(m);
        }

        // 保障性
        Supportability sp = supportabilityMapper.selectOne(
            new LambdaQueryWrapper<Supportability>().eq(Supportability::getEquipmentId, equipmentId)
        );
        if (sp != null) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", "保障性");
            m.put("color", "#a78bfa");
            m.put("score", analysisService.calcSupportabilityScore(sp));
            m.put("indicator", String.format("保障率 = %.1f%%", sp.getSupportRate()!=null?sp.getSupportRate():88));
            m.put("status", analysisService.calcSupportabilityScore(sp) >= 80 ? "达标" : "待改进");
            m.put("suggestion", "完善备件管理");
            detail.add(m);
        }

        // 安全性
        Safety sf = safetyMapper.selectOne(
            new LambdaQueryWrapper<Safety>().eq(Safety::getEquipmentId, equipmentId)
        );
        if (sf != null) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", "安全性");
            m.put("color", "#f87171");
            m.put("score", analysisService.calcSafetyScore(sf));
            m.put("indicator", String.format("失效概率 = %.6f", sf.getFailureProb()!=null?sf.getFailureProb():0.0001));
            m.put("status", analysisService.calcSafetyScore(sf) >= 75 ? "达标" : "待改进");
            m.put("suggestion", "加强故障树分析");
            detail.add(m);
        }

        // 环境适应性
        Environment env = environmentMapper.selectOne(
            new LambdaQueryWrapper<Environment>().eq(Environment::getEquipmentId, equipmentId)
        );
        if (env != null) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", "环境适应性");
            m.put("color", "#34d399");
            m.put("score", analysisService.calcEnvironmentScore(env));
            m.put("indicator", String.format("环境适应性评分 = %.1f", env.getAdaptScore()!=null?env.getAdaptScore():86));
            m.put("status", analysisService.calcEnvironmentScore(env) >= 80 ? "达标" : "待改进");
            m.put("suggestion", "加强高温防护");
            detail.add(m);
        }

        // 如果没有数据，填充默认数据
        if (detail.isEmpty()) {
            String[][] defaults = {
                {"可靠性",    "#00d4ff", "92", "MTBF = 2000h, R(t=1000h) = 0.95", "达标", "保持现有维护策略"},
                {"维修性",    "#4ade80", "85", "MTTR = 2.5h, M(t=4h) = 0.82",    "达标", "优化维修流程"},
                {"测试性",    "#f59e0b", "88", "FDR = 92%, FIR = 88%",             "达标", "增加测试点"},
                {"保障性",    "#a78bfa", "90", "保障率 = 90%",                       "达标", "完善备件管理"},
                {"安全性",    "#f87171", "78", "P(f) = 0.0001",                     "待改进", "加强故障树分析"},
                {"环境适应性","#34d399", "86", "综合评分 = 86",                       "达标", "加强高温防护"},
            };
            for (String[] d : defaults) {
                Map<String, Object> m = new HashMap<>();
                m.put("name", d[0]); m.put("color", d[1]); m.put("score", Integer.parseInt(d[2]));
                m.put("indicator", d[3]); m.put("status", d[4]); m.put("suggestion", d[5]);
                detail.add(m);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("equipmentId", equipmentId);
        result.put("equipmentName", eq.getName());
        result.put("detail", detail);
        return R.ok(result);
    }

    // ==================== 各单项保存接口 ====================

    @PostMapping("/reliability")
    public R<?> saveReliability(@RequestBody Reliability r) {
        if (r.getMtbf() != null && r.getMtbf() > 0) r.setRate(1.0 / r.getMtbf());
        if (r.getId() == null) reliabilityMapper.insert(r); else reliabilityMapper.updateById(r);
        return R.ok();
    }

    @PostMapping("/maintainability")
    public R<?> saveMaintainability(@RequestBody Maintainability m) {
        if (m.getMttr() != null && m.getMttr() > 0) m.setRepairRate(1.0 / m.getMttr());
        if (m.getId() == null) maintainabilityMapper.insert(m); else maintainabilityMapper.updateById(m);
        return R.ok();
    }

    @PostMapping("/testability")
    public R<?> saveTestability(@RequestBody Testability t) {
        if (t.getId() == null) testabilityMapper.insert(t); else testabilityMapper.updateById(t);
        return R.ok();
    }

    @PostMapping("/supportability")
    public R<?> saveSupportability(@RequestBody Supportability s) {
        if (s.getId() == null) supportabilityMapper.insert(s); else supportabilityMapper.updateById(s);
        return R.ok();
    }

    @PostMapping("/safety")
    public R<?> saveSafety(@RequestBody Safety sf) {
        if (sf.getId() == null) safetyMapper.insert(sf); else safetyMapper.updateById(sf);
        return R.ok();
    }

    @PostMapping("/environment")
    public R<?> saveEnvironment(@RequestBody Environment env) {
        if (env.getId() == null) environmentMapper.insert(env); else environmentMapper.updateById(env);
        return R.ok();
    }
}
