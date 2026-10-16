@echo off
chcp 936 >nul
REM ============================================================
REM  斩神 - 环境状态体检 (GBK + CRLF)
REM  只读检查: 不启动、不结束任何进程.
REM  用途: 一眼看清"现在到底跑没跑", 避免重复双击启动脚本。
REM ============================================================
setlocal EnableDelayedExpansion
set "FE_PORT=5173"
set "BE_PORT=8081"
set "DB_PORT=3306"

echo ============================================================
echo   斩神 环境状态体检  (只读, 不会改动任何东西)
echo ============================================================
echo.

REM ================= 前端 =================
echo ---[ 前端 端口 %FE_PORT% ]---
set "PROBE="
netstat -ano | findstr /c:":%FE_PORT% " | findstr /i "LISTENING" >nul
if errorlevel 1 goto FE_DOWN
echo   监听 : 是
set "DUMP="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /c:":%FE_PORT% " ^| findstr /i "LISTENING"') do (
    if "!DUMP: [%%P]=!"=="!DUMP!" set "DUMP=!DUMP! [%%P]"
)
for %%P in (!DUMP!) do (
    set "Q=%%P"
    set "Q=!Q:[=!"
    set "Q=!Q:]=!"
    echo   PID  : !Q!
)
set "PROBE=%TEMP%\zs_st_fe_%RANDOM%%RANDOM%.txt"
curl -s -m 5 -o "%PROBE%" "http://localhost:%FE_PORT%/@vite/client" 2>nul
findstr /i /c:"vite" "%PROBE%" >nul
if errorlevel 1 goto FE_FOREIGN
echo   类型 : 本项目 Vite 开发服务器 ^(正常^)
echo   地址 : http://localhost:%FE_PORT%/
if exist "%PROBE%" del /f /q "%PROBE%" >nul 2>nul
goto FE_DONE

:FE_FOREIGN
echo   类型 : 被其它程序占用
echo   处理 : 运行 stop-frontend.bat
if exist "%PROBE%" del /f /q "%PROBE%" >nul 2>nul
goto FE_DONE

:FE_DOWN
echo   监听 : 否
echo   处理 : 运行 start-frontend.bat
:FE_DONE
echo.

REM ================= 后端 =================
echo ---[ 后端 端口 %BE_PORT% ]---
set "PROBE="
netstat -ano | findstr /c:":%BE_PORT% " | findstr /i "LISTENING" >nul
if errorlevel 1 goto BE_DOWN
echo   监听 : 是
set "DUMP="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /c:":%BE_PORT% " ^| findstr /i "LISTENING"') do (
    if "!DUMP: [%%P]=!"=="!DUMP!" set "DUMP=!DUMP! [%%P]"
)
for %%P in (!DUMP!) do (
    set "Q=%%P"
    set "Q=!Q:[=!"
    set "Q=!Q:]=!"
    echo   PID  : !Q!
)
set "PROBE=%TEMP%\zs_st_be_%RANDOM%%RANDOM%.txt"
curl -s -m 5 -o "%PROBE%" "http://localhost:%BE_PORT%/actuator/health" 2>nul
findstr /i /c:"UP" "%PROBE%" >nul
if errorlevel 1 goto BE_FOREIGN
echo   类型 : 本项目后端 ^(健康检查 UP^)
echo   地址 : http://localhost:%BE_PORT%/
echo   文档 : http://localhost:%BE_PORT%/swagger-ui.html
if exist "%PROBE%" del /f /q "%PROBE%" >nul 2>nul
goto BE_DONE

:BE_FOREIGN
echo   类型 : 被其它程序占用, 或服务未就绪
echo   处理 : 运行 stop-backend.bat 后重启
if exist "%PROBE%" del /f /q "%PROBE%" >nul 2>nul
goto BE_DONE

:BE_DOWN
echo   监听 : 否
echo   处理 : 运行 springBootTest\start-backend.bat
:BE_DONE
echo.

REM ================= 数据库 =================
echo ---[ MySQL 端口 %DB_PORT% ]---
netstat -ano | findstr /c:":%DB_PORT% " | findstr /i "LISTENING" >nul
if errorlevel 1 (
    echo   监听 : 否 --^> 后端会连接失败, 请先启动 MySQL 服务
) else (
    echo   监听 : 是 ^(正常^)
)
echo.

echo ============================================================
echo   提示
echo    - 一键启动: start-all.bat     一键停止: stop-all.bat
echo    - 前端已在运行还去双击启动脚本是安全的, 只会提示"已在运行".
echo    - 代码改动不生效 / 页面白屏时: restart-all.bat
echo ============================================================
echo.
if /i not "%~1"=="/nopause" pause
endlocal
exit /b 0
