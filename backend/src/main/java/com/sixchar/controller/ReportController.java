package com.sixchar.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sixchar.dto.R;
import com.sixchar.entity.Report;
import com.sixchar.mapper.ReportMapper;
import com.sixchar.mapper.EquipmentMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportMapper reportMapper;
    private final EquipmentMapper equipmentMapper;

    public ReportController(ReportMapper reportMapper, EquipmentMapper equipmentMapper) {
        this.reportMapper = reportMapper;
        this.equipmentMapper = equipmentMapper;
    }

    @GetMapping
    public R<?> list(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer size
    ) {
        Page<Report> result = reportMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Report>().orderByDesc(Report::getCreateTime)
        );

        List<Map<String, Object>> records = new ArrayList<>();
        for (Report r : result.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("equipmentId", r.getEquipmentId());
            item.put("equipmentName",
                equipmentMapper.selectById(r.getEquipmentId()) != null
                    ? equipmentMapper.selectById(r.getEquipmentId()).getName()
                    : "未知装备");
            item.put("reportType", r.getReportType());
            item.put("filePath", r.getFilePath());
            item.put("createTime", r.getCreateTime());
            item.put("status", r.getFilePath() != null ? "已完成" : "生成中");
            records.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", records);
        data.put("total", result.getTotal());
        return R.ok(data);
    }

    @PostMapping("/generate")
    public R<?> generate(@RequestBody Map<String, Object> params) {
        Report report = new Report();
        report.setEquipmentId(Long.parseLong(params.get("equipmentId").toString()));
        report.setReportType(params.get("reportType").toString());
        report.setFilePath("/reports/report_" + System.currentTimeMillis() + ".pdf");
        reportMapper.insert(report);

        Map<String, Object> result = new HashMap<>();
        result.put("reportId", report.getId());
        result.put("status", "生成成功");
        return R.ok(result);
    }

    @GetMapping("/download/{id}")
    public R<?> download(@PathVariable Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null) return R.error("报告不存在");
        Map<String, Object> result = new HashMap<>();
        result.put("filePath", report.getFilePath());
        result.put("reportType", report.getReportType());
        return R.ok(result);
    }
}
