@echo off
chcp 65001 >nul 2>&1
title ZhanShen-Launcher

set "PROJECT_DIR=C:\Users\Administrator\WorkBuddy\2026-09-18-14-06-25\zhan-shen-vue"
cd /d "%PROJECT_DIR%"

REM --- locate node/npm ---
where node >nul 2>&1
if errorlevel 1 (
    REM Fallback: WorkBuddy-managed node (no system node installed)
    set "FOUND="
    for /d %%D in ("C:\Users\Administrator\.workbuddy\binaries\node\versions\*") do (
        if exist "%%D\node.exe" (
            set "NODE_BIN=%%D"
            set "FOUND=1"
        )
    )
    if not defined FOUND (
        echo [Error] Node.js not found. Install it from https://nodejs.org
        pause
        exit /b 1
    )
    set "PATH=%NODE_BIN%;%PATH%"
)

REM --- install deps on first run ---
if not exist node_modules (
    echo [Setup] Installing dependencies (one-time, please wait)...
    call npm install
    if errorlevel 1 (
        echo [Error] npm install failed. Check your network connection.
        pause
        exit /b 1
    )
)

echo [Start] Launching dev server... (close this window to stop)
echo [Start] Browser will open at http://localhost:5173/
echo.
call npm run dev
