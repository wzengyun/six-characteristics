package com.sixchar.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sixchar.dto.R;
import com.sixchar.entity.*;
import com.sixchar.mapper.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentMapper equipmentMapper;
    private final ReliabilityMapper reliabilityMapper;
    private final MaintainabilityMapper maintainabilityMapper;
    private final TestabilityMapper testabilityMapper;
    private final SupportabilityMapper supportabilityMapper;
    private final SafetyMapper safetyMapper;
    private final EnvironmentMapper environmentMapper;

    public EquipmentController(
            EquipmentMapper equipmentMapper,
            ReliabilityMapper reliabilityMapper,
            MaintainabilityMapper maintainabilityMapper,
            TestabilityMapper testabilityMapper,
            SupportabilityMapper supportabilityMapper,
            SafetyMapper safetyMapper,
            EnvironmentMapper environmentMapper
    ) {
        this.equipmentMapper = equipmentMapper;
        this.reliabilityMapper = reliabilityMapper;
        this.maintainabilityMapper = maintainabilityMapper;
        this.testabilityMapper = testabilityMapper;
        this.supportabilityMapper = supportabilityMapper;
        this.safetyMapper = safetyMapper;
        this.environmentMapper = environmentMapper;
    }

    @GetMapping
    public R<?> list(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer size,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String model
    ) {
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) wrapper.like(Equipment::getName, name);
        if (model != null && !model.isEmpty()) wrapper.like(Equipment::getModel, model);
        wrapper.orderByDesc(Equipment::getCreateTime);

        Page<Equipment> result = equipmentMapper.selectPage(new Page<>(page, size), wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return R.ok(data);
    }

    @GetMapping("/{id}")
    public R<Equipment> get(@PathVariable Long id) {
        return R.ok(equipmentMapper.selectById(id));
    }

    @PostMapping
    public R<?> create(@RequestBody Equipment equipment) {
        equipmentMapper.insert(equipment);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<?> update(@PathVariable Long id, @RequestBody Equipment equipment) {
        equipment.setId(id);
        equipmentMapper.updateById(equipment);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        equipmentMapper.deleteById(id);
        return R.ok();
    }

    // 可靠性
    @GetMapping("/reliability/{equipmentId}")
    public R<Reliability> getReliability(@PathVariable Long equipmentId) {
        Reliability r = reliabilityMapper.selectOne(
            new LambdaQueryWrapper<Reliability>().eq(Reliability::getEquipmentId, equipmentId)
        );
        return R.ok(r);
    }

    @PostMapping("/reliability")
    public R<?> saveReliability(@RequestBody Reliability r) {
        if (r.getMtbf() != null && r.getMtbf() > 0) r.setRate(1.0 / r.getMtbf());
        if (r.getId() == null) reliabilityMapper.insert(r);
        else reliabilityMapper.updateById(r);
        return R.ok();
    }

    // 维修性
    @GetMapping("/maintainability/{equipmentId}")
    public R<Maintainability> getMaintainability(@PathVariable Long equipmentId) {
        Maintainability m = maintainabilityMapper.selectOne(
            new LambdaQueryWrapper<Maintainability>().eq(Maintainability::getEquipmentId, equipmentId)
        );
        return R.ok(m);
    }

    @PostMapping("/maintainability")
    public R<?> saveMaintainability(@RequestBody Maintainability m) {
        if (m.getMttr() != null && m.getMttr() > 0) m.setRepairRate(1.0 / m.getMttr());
        if (m.getId() == null) maintainabilityMapper.insert(m);
        else maintainabilityMapper.updateById(m);
        return R.ok();
    }

    // 测试性
    @GetMapping("/testability/{equipmentId}")
    public R<Testability> getTestability(@PathVariable Long equipmentId) {
        Testability t = testabilityMapper.selectOne(
            new LambdaQueryWrapper<Testability>().eq(Testability::getEquipmentId, equipmentId)
        );
        return R.ok(t);
    }

    @PostMapping("/testability")
    public R<?> saveTestability(@RequestBody Testability t) {
        if (t.getId() == null) testabilityMapper.insert(t);
        else testabilityMapper.updateById(t);
        return R.ok();
    }

    // 保障性
    @GetMapping("/supportability/{equipmentId}")
    public R<Supportability> getSupportability(@PathVariable Long equipmentId) {
        Supportability s = supportabilityMapper.selectOne(
            new LambdaQueryWrapper<Supportability>().eq(Supportability::getEquipmentId, equipmentId)
        );
        return R.ok(s);
    }

    @PostMapping("/supportability")
    public R<?> saveSupportability(@RequestBody Supportability s) {
        if (s.getId() == null) supportabilityMapper.insert(s);
        else supportabilityMapper.updateById(s);
        return R.ok();
    }

    // 安全性
    @GetMapping("/safety/{equipmentId}")
    public R<Safety> getSafety(@PathVariable Long equipmentId) {
        Safety sf = safetyMapper.selectOne(
            new LambdaQueryWrapper<Safety>().eq(Safety::getEquipmentId, equipmentId)
        );
        return R.ok(sf);
    }

    @PostMapping("/safety")
    public R<?> saveSafety(@RequestBody Safety sf) {
        if (sf.getId() == null) safetyMapper.insert(sf);
        else safetyMapper.updateById(sf);
        return R.ok();
    }

    // 环境适应性
    @GetMapping("/environment/{equipmentId}")
    public R<Environment> getEnvironment(@PathVariable Long equipmentId) {
        Environment env = environmentMapper.selectOne(
            new LambdaQueryWrapper<Environment>().eq(Environment::getEquipmentId, equipmentId)
        );
        return R.ok(env);
    }

    @PostMapping("/environment")
    public R<?> saveEnvironment(@RequestBody Environment env) {
        if (env.getId() == null) environmentMapper.insert(env);
        else environmentMapper.updateById(env);
        return R.ok();
    }

    // 综合分析
    @GetMapping("/analysis/{equipmentId}")
    public R<?> getAnalysis(@PathVariable Long equipmentId) {
        Equipment eq = equipmentMapper.selectById(equipmentId);
        if (eq == null) return R.error("装备不存在");

        String[][] defaults = {
            {"可靠性", "#00d4ff", "92", "MTBF = 2000h, R(1000h) = 0.95", "达标", "保持现有维护策略"},
            {"维修性", "#4ade80", "85", "MTTR = 2.5h, 维修度 = 0.82", "达标", "优化维修流程"},
            {"测试性", "#f59e0b", "88", "FDR = 92%, FIR = 88%", "达标", "增加测试点"},
            {"保障性", "#a78bfa", "90", "保障率 = 90%", "达标", "完善备件管理"},
            {"安全性", "#f87171", "78", "P(f) = 0.0001", "待改进", "加强故障树分析"},
            {"环境适应性", "#34d399", "86", "综合评分 = 86", "达标", "加强高温防护"},
        };

        List<Map<String, Object>> detail = new ArrayList<>();
        for (String[] d : defaults) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", d[0]); m.put("color", d[1]); m.put("score", Integer.parseInt(d[2]));
            m.put("indicator", d[3]); m.put("status", d[4]); m.put("suggestion", d[5]);
            detail.add(m);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("equipmentId", equipmentId);
        result.put("equipmentName", eq.getName());
        result.put("detail", detail);
        return R.ok(result);
    }
}
