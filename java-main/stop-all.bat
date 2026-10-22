@echo off
chcp 936 >nul
REM ============================================================
REM  斩神 - 一键停止前后端 (自包含, GBK + CRLF)
REM  顺序: 先前端(5173), 再后端(8081)
REM  端口未在监听时会自动跳过, 末打印状态.
REM ============================================================
setlocal EnableDelayedExpansion

echo ============================================================
echo   斩神 一键停止 (先 5173, 再 8081)
echo ============================================================
echo.
call :STOP_FRONTEND
echo.
call :STOP_BACKEND
echo.
echo ============================================================
echo   停止流程结束, 当前实际状态:
echo ============================================================
echo.
call :STATUS
if /i not "%~1"=="/nopause" pause
endlocal
exit /b 0

REM ==================== 前端停止 ====================
:STOP_FRONTEND
set "PORT=5173"
echo ============================================================
echo   斩神前端停止器 (端口 %PORT%)
echo ============================================================
set "PIDS="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /c:":%PORT% " ^| findstr /i "LISTENING"') do (
    if "!PIDS: [%%P]=!"=="!PIDS!" set "PIDS=!PIDS! [%%P]"
)
if not defined PIDS (
    echo [信息] 端口 %PORT% 未在监听, 无需停止.
    goto FE_END
)
set /a "KILLED=0"
for %%P in (!PIDS!) do (
    set "P=%%P"
    set "P=!P:[=!"
    set "P=!P:]=!"
    set "IMG="
    for /f "tokens=1" %%N in ('tasklist /fi "PID eq !P!" /nh 2^>nul') do set "IMG=%%N"
    if /i "!IMG!"=="node.exe" (
        echo [信息] 结束 node 进程 PID=!P! ...
        taskkill /f /pid !P! >nul 2>nul
        set /a KILLED+=1
    ) else (
        echo [警告] PID=!P! 的镜像名是 "!IMG!", 不是 node.exe, 已跳过以免误杀.
    )
)
echo.
if !KILLED! GTR 0 (
    echo [信息] 等待端口释放...
    timeout /t 2 /nobreak >nul
    netstat -ano | findstr /c:":%PORT% " | findstr /i "LISTENING" >nul
    if errorlevel 1 (echo [OK] 端口 %PORT% 已释放.) else (echo [警告] 端口 %PORT% 仍在监听, 可能有残留进程.)
) else (
    echo [信息] 没有可结束的 node 进程.
)
:FE_END
goto :EOF

REM ==================== 后端停止 ====================
:STOP_BACKEND
set "PORT=8081"
echo ============================================================
echo   斩神后端停止器 (端口 %PORT%)
echo ============================================================
set "PIDS="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /c:":%PORT% " ^| findstr /i "LISTENING"') do (
    if "!PIDS: [%%P]=!"=="!PIDS!" set "PIDS=!PIDS! [%%P]"
)
if not defined PIDS (
    echo [信息] 端口 %PORT% 未在监听, 无需停止.
    goto BE_END
)
set /a "KILLED=0"
for %%P in (!PIDS!) do (
    set "P=%%P"
    set "P=!P:[=!"
    set "P=!P:]=!"
    set "IMG="
    for /f "tokens=1" %%N in ('tasklist /fi "PID eq !P!" /nh 2^>nul') do set "IMG=%%N"
    if /i "!IMG!"=="java.exe" (
        echo [信息] 结束 java 进程 PID=!P! ...
        taskkill /f /pid !P! >nul 2>nul
        set /a KILLED+=1
    ) else (
        echo [警告] PID=!P! 的镜像名是 "!IMG!", 不是 java.exe, 已跳过以免误杀.
    )
)
echo.
if !KILLED! GTR 0 (
    echo [信息] 等待端口释放...
    timeout /t 3 /nobreak >nul
    netstat -ano | findstr /c:":%PORT% " | findstr /i "LISTENING" >nul
    if errorlevel 1 (echo [OK] 端口 %PORT% 已释放.) else (echo [警告] 端口 %PORT% 仍在监听, 可能有残留进程.)
) else (
    echo [信息] 没有可结束的 java 进程.
)
:BE_END
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
