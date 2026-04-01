@echo off
REM 装备六性分析系统 - 快速部署脚本（Windows）
REM 用法：deploy.bat

setlocal enabledelayedexpansion
chcp 65001 > nul

echo.
echo 🚀 装备六性分析系统 - 快速部署
echo ================================
echo.

REM 检查依赖
echo 📋 检查依赖...
where git >nul 2>&1 || (echo ❌ 需要 Git & exit /b 1)
where npm >nul 2>&1 || (echo ❌ 需要 Node.js/npm & exit /b 1)
where npx >nul 2>&1 || (echo ❌ 需要 npx & exit /b 1)

echo ✅ 依赖检查完成
echo.

REM 前端部署
echo 📦 前端部署到 Vercel...
cd frontend
call npm install
call npm run build
call npx vercel --prod --yes

echo ✅ 前端已部署
echo.

REM 后端部署提示
echo 🔧 后端部署（手动）
echo ================================
echo.
echo 请选择部署平台：
echo 1. Render（推荐）- https://render.com
echo 2. Railway - https://railway.app
echo.
echo 部署步骤：
echo 1. 访问平台官网
echo 2. 用 GitHub 账户登录
echo 3. 创建 MySQL 数据库
echo 4. 创建 Web Service，连接此仓库
echo 5. 配置环境变量（见 DEPLOY.md）
echo 6. 等待部署完成
echo.

REM 前端配置
echo 🔗 配置前端 API 地址...
set /p BACKEND_URL="请输入后端 URL (例如 https://api.example.com): "

REM 更新 API 地址
powershell -Command "(Get-Content src\utils\http.js) -replace 'const API_BASE_URL = .*', 'const API_BASE_URL = ''%BACKEND_URL%''' | Set-Content src\utils\http.js"

echo ✅ API 地址已更新
echo.

REM 重新部署前端
echo 📤 重新部署前端...
call npm run build
call npx vercel --prod --yes

echo.
echo 🎉 部署完成！
echo ================================
echo 后端：%BACKEND_URL%
echo.
echo 登录凭证：
echo   用户名：admin
echo   密码：Admin@123
echo.
pause
