@echo off
chcp 936 >nul
REM ============================================================
REM  斩神 - 一键启动前后端 (自包含, GBK + CRLF)
REM  顺序: 先后端(8081), 后前端(5173)
REM  依赖: JDK 17+, Maven(自动定位), Node, MySQL8(127.0.0.1:3306 root/123456)
REM  端口三态判定, 重复运行安全(幂等). 末打印状态.
REM ============================================================
setlocal EnableDelayedExpansion

REM ============================================================
REM 关键修复: 本机环境存在 HTTP 代理(HTTP_PROXY/HTTPS_PROXY, 如 127.0.0.1:50881),
REM 会让 curl 对本机 localhost 的请求被代理拦截(返回 502), 导致端口探测与健康检查全部失效,
REM 表现为"打不开/启动失败"。显式声明 localhost/127.0.0.1 不走代理即可解决。
REM ============================================================
set "no_proxy=localhost,127.0.0.1"
set "NO_PROXY=localhost,127.0.0.1"

echo ============================================================
echo   斩神 一键启动 (先后端 8081, 后前端 5173)
echo ============================================================
echo.
echo [1/2] 启动后端...
echo.
call :START_BACKEND
echo.
echo [2/2] 启动前端...
echo.
call :START_FRONTEND
echo.
echo ============================================================
echo   启动流程结束, 当前实际状态:
echo ============================================================
echo.
call :STATUS
if /i not "%~1"=="/nopause" pause
endlocal
exit /b 0

REM ==================== 后端 ====================
:START_BACKEND
set "PORT=8081"
set "BACKEND_HOME=%~dp0springBootTest\"
set "DB_PASSWORD=123456"
set "JAR=%BACKEND_HOME%target\springBootTest-0.0.1-SNAPSHOT.jar"
set "LOG_DIR=%BACKEND_HOME%log"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
for /f "tokens=1-4 delims=/ " %%a in ("%date%") do (set "D=%%a%%b%%c")
for /f "tokens=1-3 delims=:." %%a in ("%time%") do (set "T=%%a%%b%%c")
set "T=%T: =0%"
set "LOG=%LOG_DIR%\backend-%D%-%T%.log"

echo ============================================================
echo   斩神后端启动器 (端口 %PORT%)
echo ============================================================

where java >nul 2>nul
if errorlevel 1 (
    echo [错误] 未找到 java, 请安装 JDK 17+ 并加入 PATH.
    goto BACKEND_FAIL
)
echo [OK] java 已就绪

set "MVN_CMD="
for /f "delims=" %%M in ('where mvn 2^>nul') do if not defined MVN_CMD set "MVN_CMD=%%M"
if not defined MVN_CMD (
    REM Maven 包裹分发目录(可能多一层哈希/版本子目录) + 手动安装的 .m2\maven
    for /d %%D in ("%USERPROFILE%\.m2\wrapper\dists\apache-maven-*") do for /d %%E in ("%%D\*") do for /d %%F in ("%%E\*") do if exist "%%F\bin\mvn.cmd" if not defined MVN_CMD set "MVN_CMD=%%F\bin\mvn.cmd"
    for /d %%D in ("%USERPROFILE%\.m2\maven\apache-maven-*") do if exist "%%D\bin\mvn.cmd" if not defined MVN_CMD set "MVN_CMD=%%D\bin\mvn.cmd"
)
if defined MVN_CMD (echo [OK] Maven: !MVN_CMD!) else (echo [警告] 未找到 Maven, 仅在 jar 已存在时才能启动.)

netstat -ano | findstr /c:":%PORT% " | findstr /i "LISTENING" >nul
if errorlevel 1 goto PORT_FREE

echo [信息] 端口 %PORT% 已被占用, 正在判断是否为本项目后端...
set "PROBE=%TEMP%\zs_be_probe_%RANDOM%%RANDOM%.txt"
if exist "%PROBE%" del /f /q "%PROBE%" >nul 2>nul
curl -s -m 5 -o "%PROBE%" "http://localhost:%PORT%/actuator/health" 2>nul
if not exist "%PROBE%" goto PORT_FOREIGN
findstr /i /c:"UP" "%PROBE%" >nul
if errorlevel 1 goto PORT_FOREIGN
del /f /q "%PROBE%" >nul 2>nul
echo [OK] 端口 %PORT% 上运行的正是本项目后端 (健康检查 UP), 无需重复启动.
goto BACKEND_DONE

:PORT_FOREIGN
if exist "%PROBE%" del /f /q "%PROBE%" >nul 2>nul
echo [错误] 端口 %PORT% 被[其它程序]占用, 它不是本项目后端.
echo        占用情况:
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /c:":%PORT% " ^| findstr /i "LISTENING"') do (
    echo          PID=%%P
    tasklist /fi "PID eq %%P" /nh 2>nul
)
echo        处理办法:
echo          1) 运行 stop-all.bat 尝试释放 (仅结束 java.exe, 不会误杀);
echo          2) 若是别的程序占用, 请手动关闭它.
goto BACKEND_FAIL

:PORT_FREE
echo [OK] 端口 %PORT% 空闲

if exist "%JAR%" goto JAR_OK
if not defined MVN_CMD (
    echo [错误] 需要 Maven 打包, 但未找到 mvn.
    goto BACKEND_FAIL
)
echo [信息] 未找到 jar, 开始打包 (首次需联网下载依赖, 可能几分钟)...
pushd "%BACKEND_HOME%"
call "!MVN_CMD!" package -DskipTests
set "MVN_RC=%errorlevel%"
popd
if not "!MVN_RC!"=="0" (
    echo [错误] Maven 打包失败 (退出码 !MVN_RC!), 请查看上方日志.
    goto BACKEND_FAIL
)
echo [OK] 打包完成

:JAR_OK
echo [OK] jar 已就绪: %JAR%

echo [信息] 正在启动后端服务...
pushd "%BACKEND_HOME%"
start "ZhanShen-Backend-%PORT%" /min cmd /c "chcp 65001 >nul & java -jar "%JAR%" --server.port=%PORT% --spring.profiles.active=dev > "%LOG%" 2>&1"
popd

echo [信息] 等待服务就绪 (最多 90 秒)...
set /a "TRIES=0"
:BE_WAIT
timeout /t 3 /nobreak >nul
curl -s -o nul -m 3 http://localhost:%PORT%/actuator/health
if not errorlevel 1 goto BE_READY
set /a TRIES+=1
if !TRIES! GEQ 30 (
    echo [警告] 等待超时. 日志: %LOG%
    echo ---------- 日志尾部 ----------
    powershell -NoProfile -Command "Get-Content -Encoding UTF8 '%LOG%' -Tail 30"
    goto BACKEND_FAIL
)
goto BE_WAIT

:BE_READY
echo.
echo ============================================================
echo   [成功] 后端已启动: http://localhost:%PORT%/
echo    - Swagger : http://localhost:%PORT%/swagger-ui.html
echo    - 健康检查: http://localhost:%PORT%/actuator/health
echo    - 日志    : %LOG%
echo    - 停止服务: 运行 stop-all.bat
echo ============================================================
goto BACKEND_DONE

:BACKEND_FAIL
echo.
echo [失败] 后端未启动, 请按上面的提示处理.
goto BACKEND_DONE

:BACKEND_DONE
goto :EOF

REM ==================== 前端 ====================
:START_FRONTEND
set "PORT=5173"
set "BACKEND_PORT=8081"
set "OPEN_BROWSER=1"
set "SCRIPT_DIR=%~dp0"

set "APP_HOME="
if exist "%SCRIPT_DIR%package.json" set "APP_HOME=%SCRIPT_DIR%"
if not defined APP_HOME if exist "%SCRIPT_DIR%zhanshen\package.json" set "APP_HOME=%SCRIPT_DIR%zhanshen"
if not defined APP_HOME if exist "%SCRIPT_DIR%frontend\package.json" set "APP_HOME=%SCRIPT_DIR%frontend"
if not defined APP_HOME if exist "%SCRIPT_DIR%web\package.json" set "APP_HOME=%SCRIPT_DIR%web"
if not defined APP_HOME (
    echo [错误] 未找到前端目录 (需要包含 package.json).
    echo        已排查: %SCRIPT_DIR%, %SCRIPT_DIR%zhanshen\, %SCRIPT_DIR%frontend\, %SCRIPT_DIR%web\
    goto FRONTEND_DONE
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
    goto FRONTEND_DONE
)
set "PATH=!NODE_BIN!;!PATH!"
echo [OK] 使用托管 Node: !NODE_BIN!
goto NODE_DONE

:NODE_OK
echo [OK] Node 已就绪

:NODE_DONE

netstat -ano | findstr /c:":%BACKEND_PORT% " | findstr /i "LISTENING" >nul
if errorlevel 1 (
    echo [警告] 后端 (端口 %BACKEND_PORT%) 未在运行, /api 请求将失败.
) else (
    echo [OK] 后端 %BACKEND_PORT% 正在监听
)

netstat -ano | findstr /c:":%PORT% " | findstr /i "LISTENING" >nul
if errorlevel 1 goto FE_PORT_FREE

echo [信息] 端口 %PORT% 已被占用, 正在判断是否为本项目前端...
set "PROBE=%TEMP%\zs_fe_probe_%RANDOM%%RANDOM%.txt"
if exist "%PROBE%" del /f /q "%PROBE%" >nul 2>nul
curl -s -m 5 -o "%PROBE%" "http://localhost:%PORT%/@vite/client" 2>nul
if not exist "%PROBE%" goto FE_PORT_FOREIGN
findstr /i /c:"vite" "%PROBE%" >nul
if errorlevel 1 goto FE_PORT_FOREIGN
del /f /q "%PROBE%" >nul 2>nul
echo [OK] 端口 %PORT% 上运行的正是本项目前端 (Vite 已就绪), 无需重复启动.
goto FE_READY

:FE_PORT_FOREIGN
if exist "%PROBE%" del /f /q "%PROBE%" >nul 2>nul
echo [错误] 端口 %PORT% 被[其它程序]占用, 它不是本项目前端.
echo        占用情况:
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /c:":%PORT% " ^| findstr /i "LISTENING"') do (
    echo          PID=%%P
    tasklist /fi "PID eq %%P" /nh 2>nul
)
echo        处理办法:
echo          1) 运行 stop-all.bat 尝试释放 (仅结束 node.exe, 不会误杀);
echo          2) 若是别的程序占用, 请手动关闭它.
goto FRONTEND_DONE

:FE_PORT_FREE
echo [OK] 端口 %PORT% 空闲

if exist "%APP_HOME%\node_modules" goto FE_DEPS_OK
echo [信息] node_modules 不存在 (可能已被重置删除), 开始安装依赖...
echo [信息] 需要联网, 首次安装可能需要几分钟, 请耐心等待.
pushd "%APP_HOME%"
call npm install
set "NPM_RC=%errorlevel%"
popd
if not "!NPM_RC!"=="0" (
    echo [错误] npm install 失败 (退出码 !NPM_RC!), 请检查网络或 npm 镜像设置.
    echo        可手动排查: cd /d "%APP_HOME%" 然后 npm install
    goto FRONTEND_DONE
)
echo [OK] 依赖安装完成
goto FE_DEPS_DONE

:FE_DEPS_OK
echo [OK] 依赖已就绪

:FE_DEPS_DONE

echo [信息] 正在启动 Vite 开发服务器...
pushd "%APP_HOME%"
start "ZhanShen-Frontend-%PORT%" /min cmd /c "chcp 65001 >nul & npm run dev > "%LOG%" 2>&1"
popd

echo [信息] 等待服务就绪 (最多 60 秒)...
set /a "TRIES=0"
:FE_WAIT
timeout /t 2 /nobreak >nul
curl -s -o nul -m 3 http://localhost:%PORT%/
if not errorlevel 1 goto FE_READY
set /a TRIES+=1
if !TRIES! GEQ 30 (
    echo [警告] 等待超时. 日志: %LOG%
    echo ---------- 日志尾部 ----------
    powershell -NoProfile -Command "Get-Content -Encoding UTF8 '%LOG%' -Tail 25"
    goto FRONTEND_DONE
)
goto FE_WAIT

:FE_READY
echo.
echo ============================================================
echo   [成功] 前端已启动: http://localhost:%PORT%/
echo    - 接口代理: /api -> http://localhost:%BACKEND_PORT%
echo    - 日志    : %LOG%
echo    - 停止服务: 运行 stop-all.bat
echo ============================================================
goto FE_OPEN

:FE_OPEN
if "%OPEN_BROWSER%"=="1" (
    set "CHROME_EXE=C:\Program Files\Google\Chrome\Application\chrome.exe"
    if not exist "%CHROME_EXE%" set "CHROME_EXE=C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"
    if exist "%CHROME_EXE%" (start "" "%CHROME_EXE%" "http://localhost:%PORT%/") else (start "" "http://localhost:%PORT%/")
)
goto FRONTEND_DONE

:FRONTEND_DONE
goto :EOF

REM ==================== 状态 ====================
:STATUS
set "FE_PORT=5173"
set "BE_PORT=8081"
set "DB_PORT=3306"
echo ---[ 前端 端口 %FE_PORT% ]---
netstat -ano | findstr /c:":%FE_PORT% " | findstr /i "LISTENING" >nul
if errorlevel 1 (echo   监听 : 否) else (echo   监听 : 是  地址 http://localhost:%FE_PORT%/)
echo ---[ 后端 端口 %BE_PORT% ]---
netstat -ano | findstr /c:":%BE_PORT% " | findstr /i "LISTENING" >nul
if errorlevel 1 (echo   监听 : 否) else (echo   监听 : 是  地址 http://localhost:%BE_PORT%/  Swagger /swagger-ui.html)
echo ---[ MySQL 端口 %DB_PORT% ]---
netstat -ano | findstr /c:":%DB_PORT% " | findstr /i "LISTENING" >nul
if errorlevel 1 (echo   监听 : 否 --^> 后端会连接失败, 请先启动 MySQL 服务) else (echo   监听 : 是  正常)
echo.
goto :EOF
