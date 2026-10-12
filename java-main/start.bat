@echo off
REM ============================================================
REM  ZhanShen frontend - legacy entry, forwards to start-frontend.bat
REM  (Old version hardcoded a stale project path; fixed to be relative.)
REM ============================================================
call "%~dp0start-frontend.bat"
