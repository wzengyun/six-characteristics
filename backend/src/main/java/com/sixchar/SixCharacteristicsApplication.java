package com.sixchar;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sixchar.mapper")
public class SixCharacteristicsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SixCharacteristicsApplication.class, args);
        System.out.println("===========================================");
        System.out.println("  装备六性分析系统启动成功！");
        System.out.println("  API文档: http://localhost:8080/doc.html");
        System.out.println("===========================================");
    }
}
