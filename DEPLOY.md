# 🚀 后端部署指南

## 方案 1：Render（推荐 - 完全免费）

### 步骤 1：创建 Render 账户
1. 访问 https://render.com
2. 用 GitHub 账户登录（已授权）

### 步骤 2：创建 MySQL 数据库
1. 在 Render Dashboard 点击 "New +"
2. 选择 "MySQL"
3. 配置：
   - Name: `six-characteristics-db`
   - Database: `six_characteristics`
   - Username: `root`
   - Password: `root`（自动生成，记下来）
   - Region: `Oregon` (或离你最近的)
   - Plan: `Free`
4. 点击 "Create Database"
5. 等待 5-10 分钟，复制连接字符串

### 步骤 3：创建 Web Service
1. 在 Render Dashboard 点击 "New +"
2. 选择 "Web Service"
3. 连接 GitHub 仓库：
   - 选择 `wzengyun/six-characteristics`
   - Branch: `master`
4. 配置：
   - Name: `six-characteristics-api`
   - Environment: `Docker`
   - Region: `Oregon`
   - Plan: `Free`
5. 环境变量（从 MySQL 连接字符串提取）：
   ```
   SPRING_DATASOURCE_URL=jdbc:mysql://[HOST]:[PORT]/six_characteristics?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
   SPRING_DATASOURCE_USERNAME=root
   SPRING_DATASOURCE_PASSWORD=[PASSWORD]
   JWT_SECRET=SixCharacteristicsSecretKeyForJwtTokenGeneration2026
   JWT_EXPIRATION=86400000
   SERVER_PORT=8080
   ```
6. 点击 "Create Web Service"
7. 等待 10-15 分钟构建和部署

### 步骤 4：获取后端 URL
部署完成后，Render 会显示：
```
https://six-characteristics-api.onrender.com
```

---

## 方案 2：Railway（备选 - 也免费）

### 步骤 1：创建 Railway 账户
1. 访问 https://railway.app
2. 用 GitHub 账户登录

### 步骤 2：创建项目
1. 点击 "New Project"
2. 选择 "Deploy from GitHub repo"
3. 选择 `wzengyun/six-characteristics`

### 步骤 3：添加 MySQL 服务
1. 点击 "Add Service"
2. 选择 "MySQL"
3. 配置数据库名称和密码

### 步骤 4：部署后端
1. 点击 "Add Service" → "GitHub Repo"
2. 选择同一仓库
3. 配置环境变量（同上）
4. 部署

---

## 前端配置

部署完成后，修改前端 API 地址：

**文件：** `frontend/src/utils/http.js`

```javascript
// 改为你的后端 URL
const API_BASE_URL = 'https://six-characteristics-api.onrender.com'
```

然后重新部署前端到 Vercel：
```bash
cd frontend
npm run build
npx vercel --prod --yes
```

---

## 测试

部署完成后，访问：
- **前端：** https://frontend-alpha-three-59.vercel.app
- **后端 API：** https://six-characteristics-api.onrender.com/api/health
- **登录：** admin / Admin@123

---

## 常见问题

### Q: 部署失败？
A: 检查 Dockerfile 和 pom.xml 是否正确，查看 Render/Railway 的构建日志

### Q: 数据库连接失败？
A: 确保环境变量中的 MySQL 连接字符串正确，包括主机名、端口、用户名、密码

### Q: 前端无法连接后端？
A: 检查 `http.js` 中的 API_BASE_URL 是否正确，确保后端已部署并运行

---

## 部署完成后

1. ✅ 前端已部署到 Vercel
2. ⏳ 后端待部署到 Render/Railway
3. ⏳ 修改前端 API 地址
4. ⏳ 重新部署前端

**预计总时间：** 30-45 分钟
