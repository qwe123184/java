@echo off
chcp 936 >nul
REM ============================================================
REM  斩神后端 - 一键启动脚本 (GBK + CRLF)
REM  Spring Boot 3.4.5 + MyBatis-Plus 3.5.7 + MySQL8 + JWT, 端口 8081
REM
REM  端口占用采用三态判定, 重复运行不会误报失败:
REM      (a) 端口空闲            -> 正常启动
REM      (b) 端口被[本项目后端]占用 -> 视为成功(幂等), 只提示已在运行
REM      (c) 端口被[其它程序]占用   -> 报错并给出占用进程
REM
REM  流程: 定位 Maven -> jar 缺失时自动 mvn package -DskipTests
REM        -> 端口三态判定 -> 后台启动 -> 就绪检查
REM  依赖: JDK 17+, MySQL 8 (127.0.0.1:3306, root/123456), 首次构建需联网.
REM ============================================================
setlocal EnableDelayedExpansion

REM ---------- 配置 ----------
set "PORT=8081"
set "APP_HOME=%~dp0"
set "DB_PASSWORD=123456"
set "JAR=%APP_HOME%target\springBootTest-0.0.1-SNAPSHOT.jar"
set "LOG_DIR=%APP_HOME%log"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
for /f "tokens=1-4 delims=/ " %%a in ("%date%") do (set "D=%%a%%b%%c")
for /f "tokens=1-3 delims=:." %%a in ("%time%") do (set "T=%%a%%b%%c")
set "T=%T: =0%"
set "LOG=%LOG_DIR%\backend-%D%-%T%.log"

echo ============================================================
echo   斩神后端启动器 (端口 %PORT%)
echo ============================================================

REM ---------- 1) 检查 JDK ----------
where java >nul 2>nul
if errorlevel 1 (
    echo [错误] 未找到 java, 请安装 JDK 17+ 并加入 PATH.
    goto FAIL
)
echo [OK] java 已就绪

REM ---------- 2) 定位 Maven (PATH 优先, 否则扫 .m2 wrapper) ----------
set "MVN_CMD="
for /f "delims=" %%M in ('where mvn 2^>nul') do if not defined MVN_CMD set "MVN_CMD=%%M"
if not defined MVN_CMD (
    for /f "delims=" %%M in ('dir /b /s "%USERPROFILE%\.m2\wrapper\dists\apache-maven-*-bin\*\apache-maven-*\bin\mvn.cmd" 2^>nul') do if not defined MVN_CMD set "MVN_CMD=%%M"
)
if defined MVN_CMD (echo [OK] Maven: !MVN_CMD!) else (echo [警告] 未找到 Maven, 仅在 jar 已存在时才能启动.)

REM ---------- 3) 端口占用三态判定 ----------
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
echo [OK] 端口 %PORT% 上运行的正是本项目后端 ^(健康检查 UP^), 无需重复启动.
goto ALREADY_RUNNING

:PORT_FOREIGN
if exist "%PROBE%" del /f /q "%PROBE%" >nul 2>nul
echo [错误] 端口 %PORT% 被[其它程序]占用, 它不是本项目后端.
echo        占用情况:
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /c:":%PORT% " ^| findstr /i "LISTENING"') do (
    echo          PID=%%P
    tasklist /fi "PID eq %%P" /nh 2>nul
)
echo        处理办法:
echo          1) 运行 stop-backend.bat 尝试释放 ^(仅结束 java.exe, 不会误杀^);
echo          2) 若是别的程序占用, 请手动关闭它.
goto FAIL

:PORT_FREE
echo [OK] 端口 %PORT% 空闲

REM ---------- 4) jar 缺失则打包 ----------
if exist "%JAR%" goto JAR_OK
if not defined MVN_CMD (
    echo [错误] 需要 Maven 打包, 但未找到 mvn.
    goto FAIL
)
echo [信息] 未找到 jar, 开始打包 ^(首次需联网下载依赖, 可能几分钟^)...
pushd "%APP_HOME%"
call "!MVN_CMD!" package -DskipTests
set "MVN_RC=%errorlevel%"
popd
if not "!MVN_RC!"=="0" (
    echo [错误] Maven 打包失败 ^(退出码 !MVN_RC!^), 请查看上方日志.
    goto FAIL
)
echo [OK] 打包完成

:JAR_OK
echo [OK] jar 已就绪: %JAR%

REM ---------- 5) 后台启动 ----------
echo [信息] 正在启动后端服务...
pushd "%APP_HOME%"
start "ZhanShen-Backend-%PORT%" /min cmd /c "chcp 65001 >nul & java -jar "%JAR%" --server.port=%PORT% --spring.profiles.active=dev > "%LOG%" 2>&1"
popd

REM ---------- 6) 等待就绪 ----------
echo [信息] 等待服务就绪 ^(最多 90 秒^)...
set /a "TRIES=0"
:WAIT
timeout /t 3 /nobreak >nul
curl -s -o nul -m 3 http://localhost:%PORT%/actuator/health
if not errorlevel 1 goto READY
set /a TRIES+=1
if !TRIES! GEQ 30 (
    echo [警告] 等待超时. 日志: %LOG%
    echo ---------- 日志尾部 ----------
    powershell -NoProfile -Command "Get-Content -Encoding UTF8 '%LOG%' -Tail 30"
    goto FAIL
)
goto WAIT

:READY
echo.
echo ============================================================
echo   [成功] 后端已启动: http://localhost:%PORT%/
echo    - Swagger : http://localhost:%PORT%/swagger-ui.html
echo    - 健康检查: http://localhost:%PORT%/actuator/health
echo    - 日志    : %LOG%
echo    - 停止服务: 运行 stop-backend.bat
echo ============================================================
goto END

:ALREADY_RUNNING
echo.
echo ============================================================
echo   [成功] 后端已在运行: http://localhost:%PORT%/
echo    - 无需重复启动; 重复运行本脚本不会多开进程.
echo    - Swagger : http://localhost:%PORT%/swagger-ui.html
echo    - 健康检查: http://localhost:%PORT%/actuator/health
echo    - 需要重启时: 运行 restart-backend.bat
echo ============================================================
goto END

:FAIL
echo.
echo [失败] 后端未启动, 请按上面的提示处理.
if /i not "%~1"=="/nopause" pause
endlocal
exit /b 1

:END
if /i not "%~1"=="/nopause" pause
endlocal
exit /b 0
