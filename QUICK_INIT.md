# 快速数据库初始化指南

## 方法 1：使用在线 SQL 编辑器（推荐，2分钟完成）

### 步骤：

1. **访问在线 PostgreSQL 编辑器：**
   ```
   https://sqlite.online/postgres
   ```

2. **输入连接信息：**
   - Host: `dpg-d775kmvkijhs73ab1m80-a.oregon-postgres.render.com`
   - Port: `5432`
   - Database: `six_xing`
   - User: `six_xing_user`
   - Password: `niDzP4jahuloGhwMy5XNG7ULNpSiRW7w`

3. **复制 SQL 脚本：**
   打开文件：`C:\Users\wangy\.qclaw\workspace\six-characteristics\sql\init_postgresql.sql`
   复制全部内容

4. **粘贴并执行：**
   - 在 SQL 编辑器中粘贴
   - 点击"Run"或"Execute"按钮

5. **完成！刷新前端页面即可登录**

---

## 方法 2：使用 Windows 本地 psql（如果已安装）

```powershell
$env:PGPASSWORD="niDzP4jahuloGhwMy5XNG7ULNpSiRW7w"
psql -h dpg-d775kmvkijhs73ab1m80-a.oregon-postgres.render.com -U six_xing_user -d six_xing -f "C:\Users\wangy\.qclaw\workspace\six-characteristics\sql\init_postgresql.sql"
```

---

## 方法 3：使用数据库客户端软件

**推荐工具：DBeaver（免费）**
下载：https://dbeaver.io/download/

**连接信息：**
```
Host: dpg-d775kmvkijhs73ab1m80-a.oregon-postgres.render.com
Port: 5432
Database: six_xing
Username: six_xing_user
Password: niDzP4jahuloGhwMy5XNG7ULNpSiRW7w
```

---

## 完成后测试

访问：https://frontend-alpha-three-59.vercel.app
登录：
- 用户名：admin
- 密码：Admin@123

应该能看到装备列表（3 个示例装备）。
