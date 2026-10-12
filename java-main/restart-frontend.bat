@echo off
chcp 936 >nul
REM ============================================================
REM  斩神前端 - 重启脚本 (GBK)
REM  用法: restart-frontend.bat  停止后重新启动开发服务器
REM ============================================================
setlocal

call "%~dp0stop-frontend.bat"
timeout /t 2 /nobreak >nul
call "%~dp0start-frontend.bat"

endlocal
