@echo off
chcp 936 >nul
REM ============================================================
REM  斩神前端 - 停止脚本 (GBK + CRLF)
REM  仅结束[占用 5173 端口]且进程名为 node.exe 的进程, 避免误杀.
REM  结束后会复查端口是否真的释放, 不会静默失败.
REM ============================================================
setlocal EnableDelayedExpansion
set "PORT=5173"

echo ============================================================
echo   斩神前端停止器 (端口 %PORT%)
echo ============================================================

REM ---------- 1) 收集占用端口的 PID(去重) ----------
set "PIDS="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /c:":%PORT% " ^| findstr /i "LISTENING"') do (
    if "!PIDS: [%%P]=!"=="!PIDS!" set "PIDS=!PIDS! [%%P]"
)
if not defined PIDS (
    echo [信息] 端口 %PORT% 未在监听, 无需停止.
    goto END
)

REM ---------- 2) 逐个校验进程名并结束 ----------
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
        echo        如确认要结束它, 请手动执行: taskkill /f /pid !P!
    )
)

REM ---------- 3) 复查端口 ----------
echo.
if !KILLED! GTR 0 (
    echo [信息] 等待端口释放...
    timeout /t 2 /nobreak >nul
    netstat -ano | findstr /c:":%PORT% " | findstr /i "LISTENING" >nul
    if errorlevel 1 (
        echo [OK] 端口 %PORT% 已释放.
    ) else (
        echo [警告] 端口 %PORT% 仍在监听, 可能有残留进程.
        echo        请再运行一次本脚本, 或手动: netstat -ano ^| findstr :%PORT%
    )
) else (
    echo [信息] 没有可结束的 node 进程. 若端口确实被占用, 见上方警告.
)

:END
echo.
if /i not "%~1"=="/nopause" pause
endlocal
exit /b 0
