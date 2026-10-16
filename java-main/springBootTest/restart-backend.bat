@echo off
chcp 936 >nul
REM ============================================================
REM  斩神后端 - 重启脚本 (GBK + CRLF)
REM  用法: restart-backend.bat          直接重启
REM        restart-backend.bat rebuild  删除 jar 重新打包后再重启
REM  被其它脚本调用时会传入 /nopause, 此时不暂停。
REM ============================================================
setlocal

echo ============================================================
echo   斩神后端重启 (先停 8081, 再启动)
echo ============================================================
echo.
call "%~dp0stop-backend.bat" /nopause

if /i "%~1"=="rebuild" (
    echo.
    echo [信息] 删除旧 jar, 强制重新构建...
    if exist "%~dp0target\springBootTest-0.0.1-SNAPSHOT.jar" (
        del /f "%~dp0target\springBootTest-0.0.1-SNAPSHOT.jar" >nul 2>nul
    )
)

timeout /t 2 /nobreak >nul
echo.
call "%~dp0start-backend.bat" /nopause
if errorlevel 1 (
    echo.
    echo [失败] 后端重启未成功, 详见上方提示.
)
echo.
if /i not "%~1"=="/nopause" if /i not "%~2"=="/nopause" pause
endlocal
exit /b 0
