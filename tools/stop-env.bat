@echo off
chcp 65001 >nul
setlocal

:: 切换到项目根目录（脚本位于 tools/ 下）
cd /d "%~dp0\.."

echo.
echo ========================================
echo   LY-FactMesh 基础环境停止并清理
echo ========================================
echo.

:: 检查 Docker 是否可用
docker info >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [错误] 无法连接 Docker 引擎。
    echo.
    echo 请先启动 Docker Desktop，等待其完全就绪后再运行此脚本。
    echo.
    echo 若 Docker 已启动仍报错，可尝试：
    echo   1. 右键托盘 Docker 图标 ^> Quit Docker Desktop ^> 重新打开
    echo   2. 执行 wsl --shutdown 后重启 Docker Desktop
    echo   3. 重启电脑
    echo.
    pause
    exit /b 1
)

echo 正在停止并删除容器、网络、卷...
echo.
docker compose -f tools/docker-compose-base.yml down -v

if %ERRORLEVEL% neq 0 (
    echo.
    echo [警告] 部分资源可能未能完全清理
    pause
    exit /b 1
)

echo.
echo [完成] 环境已清理
echo.
pause
