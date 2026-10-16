@echo off
chcp 936 >nul
REM ============================================================
REM  斩神前端 - 一键启动脚本 (GBK + CRLF, 切勿用 UTF-8 保存)
REM  Vue 3 + Vite 开发服务器, 端口 5173, /api 代理到 8081
REM  本脚本放在[项目根目录]或[前端子目录]都能用:
REM      自动识别含 package.json 的目录作为前端目录.
REM
REM  端口占用采用三态判定, 重复运行不会误报失败:
REM      (a) 端口空闲          -> 正常启动
REM      (b) 端口被[本项目前端] 占用 -> 视为成功(幂等), 只提示已在运行
REM      (c) 端口被[其它程序]   占用 -> 报错并给出占用进程
REM
REM  流程: 定位前端目录 -> Node 自检 -> 后端端口提示 -> 端口三态判定
REM        -> node_modules 缺失时自动 npm install -> 后台启动 -> 就绪检查
REM  注意: 电脑每次重启会重置, node_modules 可能已被删除, 缺失时自动安装.
REM ============================================================
setlocal EnableDelayedExpansion

REM ---------- 配置 ----------
set "PORT=5173"
set "BACKEND_PORT=8081"
set "OPEN_BROWSER=1"
set "SCRIPT_DIR=%~dp0"

REM ---------- 0) 定位前端目录(含 package.json 的目录) ----------
set "APP_HOME="
if exist "%SCRIPT_DIR%package.json" set "APP_HOME=%SCRIPT_DIR%"
if not defined APP_HOME if exist "%SCRIPT_DIR%zhanshen\package.json" set "APP_HOME=%SCRIPT_DIR%zhanshen"
if not defined APP_HOME if exist "%SCRIPT_DIR%frontend\package.json" set "APP_HOME=%SCRIPT_DIR%frontend"
if not defined APP_HOME if exist "%SCRIPT_DIR%web\package.json" set "APP_HOME=%SCRIPT_DIR%web"
if not defined APP_HOME (
    echo [错误] 未找到前端目录 ^(需要包含 package.json^).
    echo        已排查下面这些位置:
    echo          %SCRIPT_DIR%
    echo          %SCRIPT_DIR%zhanshen\
    echo          %SCRIPT_DIR%frontend\
    echo          %SCRIPT_DIR%web\
    goto FAIL
)
echo [OK] 前端目录: %APP_HOME%

set "LOG_DIR=%APP_HOME%\log"
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
if not errorlevel 1 goto NODE_OK

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
goto NODE_DONE

:NODE_OK
echo [OK] Node 已就绪

:NODE_DONE

REM ---------- 2) 检查后端 ----------
netstat -ano | findstr /c:":%BACKEND_PORT% " | findstr /i "LISTENING" >nul
if errorlevel 1 (
    echo [警告] 后端 ^(端口 %BACKEND_PORT%^) 未在运行, /api 请求将失败.
    echo        请先运行 springBootTest\start-backend.bat
) else (
    echo [OK] 后端 %BACKEND_PORT% 正在监听
)

REM ---------- 3) 端口占用三态判定 ----------
netstat -ano | findstr /c:":%PORT% " | findstr /i "LISTENING" >nul
if errorlevel 1 goto PORT_FREE

echo [信息] 端口 %PORT% 已被占用, 正在判断是否为本项目前端...
set "PROBE=%TEMP%\zs_fe_probe_%RANDOM%%RANDOM%.txt"
if exist "%PROBE%" del /f /q "%PROBE%" >nul 2>nul
curl -s -m 5 -o "%PROBE%" "http://localhost:%PORT%/@vite/client" 2>nul
if not exist "%PROBE%" goto PORT_FOREIGN
findstr /i /c:"vite" "%PROBE%" >nul
if errorlevel 1 goto PORT_FOREIGN
del /f /q "%PROBE%" >nul 2>nul
echo [OK] 端口 %PORT% 上运行的正是本项目前端 ^(Vite 已就绪^), 无需重复启动.
goto ALREADY_RUNNING

:PORT_FOREIGN
if exist "%PROBE%" del /f /q "%PROBE%" >nul 2>nul
echo [错误] 端口 %PORT% 被[其它程序]占用, 它不是本项目前端.
echo        占用情况:
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /c:":%PORT% " ^| findstr /i "LISTENING"') do (
    echo          PID=%%P
    tasklist /fi "PID eq %%P" /nh 2>nul
)
echo        处理办法:
echo          1) 运行 stop-frontend.bat 尝试释放 ^(仅结束 node.exe, 不会误杀^);
echo          2) 若是别的程序占用, 请手动关闭它, 或改 vite.config.js 的端口.
goto FAIL

:PORT_FREE
echo [OK] 端口 %PORT% 空闲

REM ---------- 4) node_modules 缺失时自动安装 ----------
if exist "%APP_HOME%\node_modules" goto DEPS_OK
echo [信息] node_modules 不存在 ^(可能已被重置删除^), 开始安装依赖...
echo [信息] 需要联网, 首次安装可能需要几分钟, 请耐心等待.
pushd "%APP_HOME%"
call npm install
set "NPM_RC=%errorlevel%"
popd
if not "!NPM_RC!"=="0" (
    echo [错误] npm install 失败 ^(退出码 !NPM_RC!^), 请检查网络或 npm 镜像设置.
    echo        可手动排查: cd /d "%APP_HOME%"
    echo                    npm install
    goto FAIL
)
echo [OK] 依赖安装完成
goto DEPS_DONE

:DEPS_OK
echo [OK] 依赖已就绪

:DEPS_DONE

REM ---------- 5) 后台启动开发服务器 ----------
echo [信息] 正在启动 Vite 开发服务器...
pushd "%APP_HOME%"
start "ZhanShen-Frontend-%PORT%" /min cmd /c "chcp 65001 >nul & npm run dev > "%LOG%" 2>&1"
popd

REM ---------- 6) 等待就绪 ----------
echo [信息] 等待服务就绪 ^(最多 60 秒^)...
set /a "TRIES=0"
:WAIT
timeout /t 2 /nobreak >nul
curl -s -o nul -m 3 http://localhost:%PORT%/
if not errorlevel 1 goto READY
set /a TRIES+=1
if !TRIES! GEQ 30 (
    echo [警告] 等待超时. 日志: %LOG%
    echo ---------- 日志尾部 ----------
    powershell -NoProfile -Command "Get-Content -Encoding UTF8 '%LOG%' -Tail 25"
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
goto OPEN_AND_END

:ALREADY_RUNNING
echo.
echo ============================================================
echo   [成功] 前端已在运行: http://localhost:%PORT%/
echo    - 无需重复启动; 重复运行本脚本不会多开进程.
echo    - 页面异常 ^(白屏/404/代码改动不生效^) 时:
echo        请先 stop-frontend.bat 再启动, 不要重复运行本脚本.
echo    - 日志目录: %LOG_DIR%
echo ============================================================
goto OPEN_AND_END

:OPEN_AND_END
if "%OPEN_BROWSER%"=="1" (
    echo [信息] 正在打开浏览器...
    start "" "http://localhost:%PORT%/"
)
if /i not "%~1"=="/nopause" pause
endlocal
exit /b 0

:FAIL
echo.
echo [失败] 前端未启动, 请按上面的提示处理.
if /i not "%~1"=="/nopause" pause
endlocal
exit /b 1
