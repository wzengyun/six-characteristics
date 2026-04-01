@echo off
chcp 65001 >nul
echo ============================================
echo   装备六性分析系统 - 后端启动脚本
echo ============================================
echo.

echo [1/3] 检查 Java...
java -version 2>&1 | findstr "version" >nul
if errorlevel 1 (
    echo [错误] 未检测到 Java，请先安装 JDK 17+
    pause
    exit /b 1
)

echo [2/3] 检查 Maven...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Maven，请先安装 Maven
    pause
    exit /b 1
)

echo [3/3] 启动后端服务...
echo.
echo 提示：请确保 MySQL 已启动，数据库配置正确
echo   地址: localhost:3306
echo   用户: root
echo   密码: root
echo.
cd /d "%~dp0backend"
mvn spring-boot:run
