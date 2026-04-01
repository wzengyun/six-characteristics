# 装备通用质量特性六性分析系统

<div align="center">

![Vue](https://img.shields.io/badge/Vue-3.4-4FC08D?logo=vue.js)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=spring)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)
![License](https://img.shields.io/badge/License-MIT-green)

**对标 Relex / Isograph 的国产化六性分析平台**

</div>

---

## 📋 项目简介

本系统是一个专业的装备通用质量特性六性分析平台，严格遵循 **GJB 450A**（可靠性）、**GJB 368B**（维修性）、**GJB 900**（安全性）等军工标准，提供完整的六性分析功能。

### 🎯 六大特性

| 特性 | 描述 | 核心指标 |
|------|------|----------|
| 🔵 **可靠性** | 产品在规定条件下完成规定功能的能力 | MTBF、可靠度、故障率 |
| 🟢 **维修性** | 产品在规定条件下维修的能力 | MTTR、修复率、维修度 |
| 🟡 **测试性** | 产品能及时确定状态并隔离故障的能力 | FDR、FIR、虚警率 |
| 🟣 **保障性** | 装备的设计特性和保障资源满足战备要求的能力 | 保障率、资源完备度 |
| 🔴 **安全性** | 产品不发生事故的能力 | 故障树分析、失效概率 |
| 🟢 **环境适应性** | 产品适应环境变化的能力 | 温度、振动、湿度适应性 |

---

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────────────────┐
│                      前端 (Vue 3 + Vite)                 │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐        │
│  │ 装备管理 │ │ 六性分析 │ │ 综合评估 │ │ 报告中心 │        │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘        │
│                    ECharts + Element Plus               │
└──────────────────────────┬──────────────────────────────┘
                           │ REST API
┌──────────────────────────┴──────────────────────────────┐
│                后端 (Spring Boot 3.2 + MyBatis Plus)      │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐        │
│  │ 用户认证 │ │ 装备服务 │ │ 分析引擎 │ │ 报告生成 │        │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘        │
│                    JWT + Spring Security                 │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────┴──────────────────────────────┐
│                     数据库 (MySQL 8.0)                   │
│   user | equipment | reliability | maintainability |    │
│   testability | supportability | safety | environment   │
└─────────────────────────────────────────────────────────┘
```

---

## 🚀 快速开始

### 方式一：本地开发环境

#### 1. 准备数据库

```bash
# 创建数据库并导入初始数据
mysql -u root -p < sql/init.sql
```

#### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端服务地址：http://localhost:8080

#### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端访问地址：http://localhost:5173

### 方式二：Docker 部署

```bash
# 一键启动所有服务
docker-compose up -d

# 访问系统
# 前端: http://localhost
# 后端: http://localhost:8080
# API文档: http://localhost:8080/doc.html
```

---

## 👤 演示账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 系统管理员 | admin | Admin@123 |
| 分析工程师 | analyst | Analyst@123 |

---

## 📦 项目结构

```
six-characteristics/
├── frontend/                # Vue 3 前端项目
│   ├── src/
│   │   ├── views/         # 页面组件
│   │   ├── components/    # 通用组件
│   │   ├── api/           # API 接口
│   │   ├── store/         # Pinia 状态管理
│   │   ├── router/        # 路由配置
│   │   └── assets/        # 静态资源
│   └── package.json
│
├── backend/                 # Spring Boot 后端项目
│   ├── src/main/java/com/sixchar/
│   │   ├── controller/    # 控制器
│   │   ├── service/       # 业务逻辑
│   │   ├── mapper/        # 数据访问
│   │   ├── entity/        # 实体类
│   │   └── config/        # 配置类
│   └── pom.xml
│
├── sql/                     # 数据库脚本
│   └── init.sql
│
└── docker-compose.yml       # Docker 编排
```

---

## ✨ 核心功能

### 1️⃣ 可靠性分析 (GJB 450A)

- **MTBF 计算**：支持指数分布、威布尔分布、正态分布
- **可靠度曲线**：自动生成 R(t) 曲线图
- **RBD 建模**：可靠性框图可视化

### 2️⃣ 维修性分析 (GJB 368B)

- **MTTR 计算**：平均修复时间分析
- **维修度曲线**：M(t) 函数可视化
- **维修流程优化**

### 3️⃣ 测试性分析

- **FDR/FIR 计算**：故障检测率、隔离率
- **测试点优化**：智能推荐测试点配置

### 4️⃣ 保障性分析

- **保障资源评估**：备件、人员、资料完备度
- **保障率计算**

### 5️⃣ 安全性分析 (GJB 900)

- **FTA 故障树**：可视化建模
- **最小割集计算**
- **失效概率评估**

### 6️⃣ 环境适应性

- **温度/振动/湿度**：多维度适应性评估
- **环境应力分析**

### 7️⃣ 综合分析

- **六性雷达图**：综合评估可视化
- **报告生成**：支持 PDF/Word 导出

---

## 📊 界面预览

| 首页概览 | 可靠性分析 |
|:--------:|:----------:|
| ![Dashboard](docs/dashboard.png) | ![Reliability](docs/reliability.png) |

| 故障树分析 | 综合评估 |
|:----------:|:--------:|
| ![FTA](docs/fta.png) | ![Analysis](docs/analysis.png) |

---

## 🛠️ 开发指南

### 环境要求

- Node.js >= 18
- JDK >= 17
- MySQL >= 8.0
- Maven >= 3.8

### API 文档

启动后端后访问：http://localhost:8080/doc.html

---

## 📄 许可证

MIT License

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

<div align="center">

**Made with ❤️ for Equipment Quality Analysis**

</div>
