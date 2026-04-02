# 环境变量配置清单

## Render 后端服务配置（必须项）

### 数据库连接
```
SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-d775kmvkijhs73ab1m80-a:5432/six_xing?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
```

```
SPRING_DATASOURCE_USERNAME=six_xing_user
```

```
SPRING_DATASOURCE_PASSWORD=niDzP4jahuloGhwMy5XNG7ULNpSiRW7w
```

### JWT 认证
```
JWT_SECRET=SixCharacteristicsSecretKeyForJwtTokenGeneration2026
```

```
JWT_EXPIRATION=86400000
```

### 服务配置
```
SERVER_PORT=8080
```

---

## 注意事项

1. **Region 必须选 Oregon**（与数据库相同）
2. **Environment 选 Docker**
3. **Dockerfile Path: ./backend/Dockerfile**
4. **等待 5-10 分钟部署完成**

---

时间戳: 2026-04-02 20:39 Asia/Shanghai
