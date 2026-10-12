@echo off
chcp 936 >nul
REM ============================================================
REM  斩神后端 - 重启脚本 (GBK)
REM  用法: restart-backend.bat          直接重启
REM        restart-backend.bat rebuild  删除 jar 重新打包后再重启
REM ============================================================
setlocal

call "%~dp0stop-backend.bat"

if /i "%~1"=="rebuild" (
    echo [信息] 删除旧 jar, 强制重新构建...
    if exist "%~dp0target\springBootTest-0.0.1-SNAPSHOT.jar" (
        del /f "%~dp0target\springBootTest-0.0.1-SNAPSHOT.jar" >nul 2>nul
    )
)

timeout /t 2 /nobreak >nul
call "%~dp0start-backend.bat"

endlocal
