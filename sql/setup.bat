@echo off
chcp 65001 >nul 2>&1
echo ==========================================
echo   装备六性分析系统 - 数据库初始化
echo ==========================================
echo.
echo 请确保 MySQL 服务已启动
echo 默认配置: root / root
echo.

set /p MYSQL_USER="MySQL 用户名 [默认 root]: " || set MYSQL_USER=root
set /p MYSQL_PWD="MySQL 密码 [默认 root]: " || set MYSQL_PWD=root

echo.
echo 正在创建数据库并导入数据...
echo.

mysql -u%MYSQL_USER% -p%MYSQL_PWD% < "%~dp0init.sql"

if %errorlevel% equ 0 (
    echo.
    echo ==========================================
    echo   数据库初始化成功！
    echo   数据库名: six_characteristics
    echo ==========================================
) else (
    echo.
    echo [错误] 数据库初始化失败，请检查 MySQL 连接配置
    echo.
    echo 提示: 
    echo   1. 确认 MySQL 服务已启动
    echo   2. 确认用户名密码正确
    echo   3. 可手动执行: mysql -u%MYSQL_USER% -p%MYSQL_PWD% ^< init.sql
)
pause
