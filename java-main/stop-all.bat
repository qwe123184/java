@echo off
chcp 936 >nul
REM ============================================================
REM  斩神 - 一键停止前后端 (GBK + CRLF)
REM  顺序: 先前端(5173), 再后端(8081)
REM  端口未在监听时会自动跳过, 最后调用 status.bat 复查。
REM ============================================================
setlocal

echo ============================================================
echo   斩神 一键停止 (先 5173, 再 8081)
echo ============================================================
echo.
call "%~dp0stop-frontend.bat" /nopause
echo.
call "%~dp0springBootTest\stop-backend.bat" /nopause
echo.
echo ============================================================
echo   停止流程结束, 当前实际状态:
echo ============================================================
echo.
call "%~dp0status.bat" /nopause
if /i not "%~1"=="/nopause" pause
endlocal
exit /b 0
