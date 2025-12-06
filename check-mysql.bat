@echo off
chcp 65001 >nul
REM ========================================
REM MySQL 服务检查脚本
REM ========================================

echo [检查] MySQL 服务状态...
echo.

REM 查找所有 MySQL 相关服务
sc query state= all | findstr /I "mysql" >nul 2>&1
if errorlevel 1 (
    echo ❌ 未找到 MySQL 服务
    echo 请先安装 MySQL 或启动 MySQL 服务
) else (
    echo ✅ 找到 MySQL 服务:
    sc query state= all | findstr /I "mysql"
)
echo.

REM 检查端口
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul 2>&1
if errorlevel 1 (
    echo ❌ 端口 3306 未被占用 - MySQL 可能未运行
) else (
    echo ✅ 端口 3306 正在监听
)
echo.

pause
