@echo off
chcp 65001 >nul
REM ========================================
REM 启动后端服务
REM ========================================

echo [信息] 正在启动后端服务...
echo [信息] 后端将运行在: http://localhost:8080
echo [信息] 按 Ctrl+C 停止服务
echo.

if exist mvnw.cmd (
    call mvnw.cmd spring-boot:run
) else if exist mvnw (
    call mvnw spring-boot:run
) else (
    echo [错误] 未找到 Maven Wrapper，请使用 Maven 命令:
    echo   mvn spring-boot:run
    pause
    exit /b 1
)
