package com.sixchar.controller;

import com.sixchar.dto.R;
import com.sixchar.entity.User;
import com.sixchar.mapper.UserMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @PostMapping("/login")
    public R<?> login(@RequestBody Map<String, String> params) {
        String account = params.get("account");
        String password = params.get("password");

        User user = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getAccount, account)
        );

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return R.error("账号或密码错误");
        }

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        String token = Jwts.builder()
            .subject(user.getId().toString())
            .claim("name", user.getName())
            .claim("role", user.getRole())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(key)
            .compact();

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        user.setPassword(null);
        data.put("user", user);

        return R.ok(data);
    }

    @GetMapping("/profile")
    public R<User> profile(@RequestAttribute("userId") Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) user.setPassword(null);
        return R.ok(user);
    }

    @PutMapping("/profile")
    public R<?> updateProfile(@RequestAttribute("userId") Long userId, @RequestBody Map<String, String> params) {
        User user = userMapper.selectById(userId);
        if (user == null) return R.error("用户不存在");
        if (params.containsKey("name")) user.setName(params.get("name"));
        if (params.containsKey("password") && params.get("password") != null && !params.get("password").isEmpty()) {
            user.setPassword(passwordEncoder.encode(params.get("password")));
        }
        userMapper.updateById(user);
        user.setPassword(null);
        return R.ok(user);
    }
}
