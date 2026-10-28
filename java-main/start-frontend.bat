@echo off
setlocal EnableDelayedExpansion
REM ============================================================
REM  ZhanShen frontend launcher (Vite dev server, port 5173)
REM  Normally invoked by start-all.bat; you can also run it alone.
REM  Runs vite directly via node (bypasses npm wrapper issues) and
REM  logs everything to zhanshen\log\frontend-console-*.log
REM ============================================================

set "BASE=%~dp0"
if "%BASE:~-1%"=="\" set "BASE=%BASE:~0,-1%"
set "PORT=5173"
set "BACKEND_PORT=8081"
set "APP_HOME=%BASE%\zhanshen"
set "VITE_JS=%APP_HOME%\node_modules\vite\bin\vite.js"
set "LOG_DIR=%APP_HOME%\log"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
for /f "tokens=1-4 delims=/ " %%a in ("%date%") do (set "D=%%a%%b%%c")
for /f "tokens=1-3 delims=:." %%a in ("%time%") do (set "T=%%a%%b%%c")
set "T=%T: =0%"
set "LOG=%LOG_DIR%\frontend-console-%D%-%T%.log"

echo [%date% %time%] frontend launcher start > "%LOG%"

if not exist "%APP_HOME%\package.json" (
  echo [ERROR] Frontend dir not found: %APP_HOME%
  echo [%date% %time%] ERROR package.json not found >> "%LOG%"
  pause
  exit /b 1
)
echo [%date% %time%] package.json OK >> "%LOG%"

REM ---- avoid localhost going through HTTP proxy ----
set "no_proxy=localhost,127.0.0.1"
set "NO_PROXY=localhost,127.0.0.1"

REM ---- Node.js: prefer the managed WorkBuddy node (tested), else system ----
set "NODE_EXE=%USERPROFILE%\.workbuddy\binaries\node\versions\22.22.2-3\node.exe"
if exist "!NODE_EXE!" (
  echo [OK] Using managed Node: !NODE_EXE!
  echo [%date% %time%] node = !NODE_EXE! ^(managed^) >> "%LOG%"
) else (
  set "NODE_EXE="
  for /f "delims=" %%p in ('where node 2^>nul') do if not defined NODE_EXE set "NODE_EXE=%%p"
  if not defined NODE_EXE (
    echo [ERROR] Node.js not found. Install from https://nodejs.org
    echo [%date% %time%] ERROR node not found >> "%LOG%"
    pause
    exit /b 1
  )
  echo [OK] Using system Node: !NODE_EXE!
  echo [%date% %time%] node = !NODE_EXE! ^(system^) >> "%LOG%"
)

if not exist "!NODE_EXE!" (
  echo [ERROR] Node.exe missing: !NODE_EXE!
  echo [%date% %time%] ERROR node.exe missing >> "%LOG%"
  pause
  exit /b 1
)

if not exist "%VITE_JS%" (
  echo [ERROR] vite.js not found: %VITE_JS%
  echo         Run: cd zhanshen ^&^& npm install
  echo [%date% %time%] ERROR vite.js missing >> "%LOG%"
  pause
  exit /b 1
)
echo [%date% %time%] vite.js OK >> "%LOG%"

netstat -ano | findstr /c:":%PORT% " | findstr /i "LISTENING" >nul
if not errorlevel 1 (
  echo [WARN] Port %PORT% is already in use - frontend may already be running.
  echo        Run stop-all.bat first if you want a fresh restart.
)

if not exist "%APP_HOME%\node_modules" (
  echo [INFO] node_modules missing, installing. First run may take minutes.
  echo [%date% %time%] npm install start >> "%LOG%"
  pushd "%APP_HOME%"
  call npm install
  set "NPM_RC=%errorlevel%"
  popd
  echo [%date% %time%] npm install rc=!NPM_RC! >> "%LOG%"
  if not "!NPM_RC!"=="0" (
    echo [ERROR] npm install failed. Check network / npm registry.
    pause
    exit /b 1
  )
)

echo ============================================================
echo   ZhanShen frontend (port %PORT%)
echo   Minimize this window freely - closing it stops the frontend.
echo   Log: %LOG%
echo ============================================================
echo.
echo [%date% %time%] launching vite via node >> "%LOG%"
cd /d "%APP_HOME%"
"%NODE_EXE%" "%VITE_JS%" >> "%LOG%" 2>&1
set "RC=%errorlevel%"
echo [%date% %time%] vite exited rc=%RC% >> "%LOG%"
echo.
echo [EXIT] Frontend stopped, exit code %RC%.
echo [INFO] Full log: %LOG%
type "%LOG%"
pause
endlocal
exit /b %RC%
