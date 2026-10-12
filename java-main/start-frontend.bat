@echo off
chcp 936 >nul
REM ============================================================
REM  斩神前端 - 一键启动脚本 (GBK)
REM  Vue 3 + Vite 开发服务器, 端口 5173, /api 代理到 8081
REM  流程: Node 自检 -> node_modules 缺失时自动 npm install
REM        -> 端口检测 -> 后台启动 -> 就绪检查
REM  注意: 电脑每次重启会重置, node_modules 可能已被删除,
REM        本脚本不依赖 node_modules 预存在, 缺失时自动安装.
REM ============================================================
setlocal EnableDelayedExpansion

REM ---------- 配置 ----------
set "PORT=5173"
set "BACKEND_PORT=8081"
set "APP_HOME=%~dp0"
set "LOG_DIR=%APP_HOME%log"

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
for /f "tokens=1-4 delims=/ " %%a in ("%date%") do (set "D=%%a%%b%%c")
for /f "tokens=1-3 delims=:." %%a in ("%time%") do (set "T=%%a%%b%%c")
set "T=%T: =0%"
set "LOG=%LOG_DIR%\frontend-%D%-%T%.log"

echo ============================================================
echo   斩神前端启动器 (端口 %PORT%)
echo ============================================================

REM ---------- 1) 定位 node/npm ----------
where node >nul 2>nul
if errorlevel 1 (
    set "FOUND="
    for /d %%D in ("%USERPROFILE%\.workbuddy\binaries\node\versions\*") do (
        if exist "%%D\node.exe" (
            set "NODE_BIN=%%D"
            set "FOUND=1"
        )
    )
    if not defined FOUND (
        echo [错误] 未找到 Node.js, 请安装: https://nodejs.org
        goto FAIL
    )
    set "PATH=!NODE_BIN!;!PATH!"
    echo [OK] 使用托管 Node: !NODE_BIN!
) else (
    echo [OK] Node 已就绪
)

REM ---------- 2) 检查后端 ----------
netstat -ano | findstr /c:":%BACKEND_PORT% " | findstr /i "LISTENING" >nul
if errorlevel 1 (
    echo [警告] 后端 ^(端口 %BACKEND_PORT%^) 未在运行, /api 请求将失败.
    echo        请先运行 springBootTest\start-backend.bat
) else (
    echo [OK] 后端 %BACKEND_PORT% 正在监听
)

REM ---------- 3) 检查端口占用 ----------
netstat -ano | findstr /c:":%PORT% " | findstr /i "LISTENING" >nul
if not errorlevel 1 (
    echo [错误] 端口 %PORT% 已被占用, 前端可能已在运行.
    echo        如需重启请先运行 stop-frontend.bat
    goto FAIL
)
echo [OK] 端口 %PORT% 空闲

REM ---------- 4) node_modules 缺失时自动安装 ----------
if not exist "%APP_HOME%node_modules" (
    echo [信息] node_modules 不存在 ^(可能已被重置删除^), 开始安装依赖...
    echo [信息] 需要联网, 首次安装可能需要几分钟, 请耐心等待.
    pushd "%APP_HOME%"
    call npm install
    if errorlevel 1 (
        popd
        echo [错误] npm install 失败, 请检查网络连接.
        goto FAIL
    )
    popd
)
echo [OK] 依赖已就绪

REM ---------- 5) 后台启动开发服务器 ----------
echo [信息] 正在启动 Vite 开发服务器...
start "ZhanShen-Frontend-%PORT%" /min cmd /c "cd /d "%APP_HOME%" && npm run dev > "%LOG%" 2>&1"

REM ---------- 6) 等待就绪 ----------
echo [信息] 等待服务就绪 ^(最多 60 秒^)...
set /a "TRIES=0"
:WAIT
timeout /t 2 /nobreak >nul
curl -s -o nul -m 3 http://localhost:%PORT%/
if not errorlevel 1 goto READY
set /a TRIES+=1
if %TRIES% GEQ 30 (
    echo [警告] 等待超时, 日志: %LOG%
    echo ---------- 日志尾部 ----------
    powershell -NoProfile -Command "Get-Content '%LOG%' -Tail 25"
    goto FAIL
)
goto WAIT

:READY
echo.
echo ============================================================
echo   [成功] 前端已启动: http://localhost:%PORT%/
echo    - 接口代理: /api -^> http://localhost:%BACKEND_PORT%
echo    - 日志    : %LOG%
echo    - 停止服务: 运行 stop-frontend.bat
echo ============================================================
goto END

:FAIL
exit /b 1

:END
endlocal
