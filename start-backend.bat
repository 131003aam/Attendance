@echo off
chcp 65001 >nul
REM ========================================
REM 启动后端服务
REM ========================================

echo [信息] 正在启动后端服务...
echo [信息] 后端将运行在: http://localhost:8080
echo [信息] 按 Ctrl+C 停止服务
echo.

REM 检查 MySQL 是否运行
echo [检查] 正在检查 MySQL 连接...
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul 2>&1
if errorlevel 1 (
    echo [警告] 未检测到 MySQL 在端口 3306 上运行
    echo [提示] 请确保 MySQL 服务已启动，否则后端可能无法连接数据库
    echo.
)

REM 检查 Maven Wrapper
if not exist mvnw.cmd (
    if not exist mvnw (
        echo [错误] 未找到 Maven Wrapper，请使用 Maven 命令:
        echo   mvn spring-boot:run
        echo.
        echo [错误] 启动失败，窗口将在 10 秒后关闭...
        timeout /t 10 >nul
        exit /b 1
    )
)

REM 启动 Spring Boot 应用
if exist mvnw.cmd (
    call mvnw.cmd spring-boot:run
) else (
    call mvnw spring-boot:run
)

REM 检查启动是否失败
if errorlevel 1 (
    echo.
    echo [错误] 后端启动失败！
    echo [提示] 请检查以下问题:
    echo   1. MySQL 服务是否已启动
    echo   2. 数据库连接配置是否正确
    echo   3. 端口 8080 是否被占用
    echo   4. 查看上方的错误信息
    echo.
    pause
    exit /b 1
)
