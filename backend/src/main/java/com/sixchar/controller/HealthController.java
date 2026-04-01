package com.sixchar.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 健康检查与系统信息
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", System.currentTimeMillis());
        result.put("service", "装备六性分析系统");
        result.put("version", "1.0.0");
        return result;
    }

    @GetMapping("/standards")
    public Map<String, String> standards() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("可靠性", "GJB 450A-2020");
        map.put("维修性", "GJB 368B-2009");
        map.put("测试性", "GJB 2547A-2012");
        map.put("保障性", "GJB 3872A-2018");
        map.put("安全性", "GJB 900-2018");
        map.put("环境适应性", "GJB 150A-2009");
        return map;
    }
}
