@echo off
chcp 65001 >nul
echo ============================================
echo   装备六性分析系统 - 前端启动脚本
echo ============================================
echo.

echo [1/3] 检查 Node.js...
node -v >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Node.js，请先安装 Node.js 18+
    pause
    exit /b 1
)

echo [2/3] 安装依赖...
cd /d "%~dp0frontend"
call npm install

echo [3/3] 启动前端服务...
echo.
echo 启动后访问: http://localhost:5173
echo 后端API:    http://localhost:8080
echo.
call npm run dev

pause
