package com.sixchar.config;

import com.sixchar.entity.User;
import com.sixchar.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 系统数据初始化器
 * 确保初始管理员账号存在
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataInitializer(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void run(String... args) {
        try {
            long adminCount = userMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                    .eq(User::getAccount, "admin")
            );

            if (adminCount == 0) {
                User admin = new User();
                admin.setName("系统管理员");
                admin.setAccount("admin");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setRole("ADMIN");
                userMapper.insert(admin);

                User analyst = new User();
                analyst.setName("分析工程师");
                analyst.setAccount("analyst");
                analyst.setPassword(passwordEncoder.encode("Analyst@123"));
                analyst.setRole("ANALYST");
                userMapper.insert(analyst);

                System.out.println("===========================================");
                System.out.println("  默认账号已初始化:");
                System.out.println("  管理员: admin / Admin@123");
                System.out.println("  工程师: analyst / Analyst@123");
                System.out.println("===========================================");
            }
        } catch (Exception e) {
            System.out.println("DataInitializer 跳过（数据库可能未就绪）: " + e.getMessage());
        }
    }
}
