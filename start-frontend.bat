@echo off
chcp 65001 >nul
REM ========================================
REM 启动前端服务
REM ========================================

if not exist "frontend\package.json" (
    echo [错误] 请在项目根目录运行此脚本
    pause
    exit /b 1
)

cd frontend

if not exist "node_modules" (
    echo [信息] 首次运行，正在安装依赖...
    call npm install
    if errorlevel 1 (
        echo [错误] 依赖安装失败
        pause
        exit /b 1
    )
)

echo [信息] 正在启动前端服务...
echo [信息] 前端将运行在: http://localhost:5173
echo [信息] 按 Ctrl+C 停止服务
echo.

call npm run dev
