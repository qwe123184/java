@echo off
setlocal EnableDelayedExpansion
REM ============================================================
REM  ZhanShen one-click launcher (self-contained)
REM  Order: MySQL -> backend (8081) -> frontend (5173)
REM  Requirements: JDK 17+, Node, MySQL8 (127.0.0.1:3306 root/123456)
REM  This script auto-installs Maven and repairs corrupt .m2 jars.
REM  Run as Administrator so "net start MySQL" works.
REM ============================================================

REM ---- avoid localhost going through HTTP proxy ----
set "no_proxy=localhost,127.0.0.1"
set "NO_PROXY=localhost,127.0.0.1"

set "BASE=%~dp0"
if "%BASE:~-1%"=="\" set "BASE=%BASE:~0,-1%"

echo ============================================================
echo   ZhanShen one-click launcher
echo   (MySQL -> backend 8081 -> frontend 5173)
echo ============================================================

REM ---- 0. JDK ----
if not defined JAVA_HOME (
  if exist "C:\Program Files\Zulu\zulu-21" set "JAVA_HOME=C:\Program Files\Zulu\zulu-21"
)
if not defined JAVA_HOME (
  for /d %%d in ("C:\Program Files\Java\jdk*") do set "JAVA_HOME=%%d"
)
if not defined JAVA_HOME (
  for /d %%d in ("C:\Program Files\Java\jre*") do set "JAVA_HOME=%%d"
)
if not defined JAVA_HOME (
  echo [ERROR] JDK 17+ not found. Install JDK and set JAVA_HOME.
  goto :NEEDHELP
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo [OK] JAVA_HOME = %JAVA_HOME%

REM ---- 1. Maven (search, else download 3.9.9) ----
set "MVN_CMD="
where mvn >nul 2>&1 && set "MVN_CMD=mvn"
if not defined MVN_CMD (
  for /d %%d in ("%BASE%\tools\maven\apache-maven-*") do if exist "%%d\bin\mvn.cmd" set "MVN_CMD=%%d\bin\mvn.cmd"
)
if not defined MVN_CMD (
  for /d %%d in ("%USERPROFILE%\.m2\wrapper\dists\apache-maven-*") do for /d %%e in ("%%d\*") do for /d %%f in ("%%e\*") do if exist "%%f\bin\mvn.cmd" if not defined MVN_CMD set "MVN_CMD=%%f\bin\mvn.cmd"
)
if not defined MVN_CMD (
  for /d %%d in ("%USERPROFILE%\.m2\maven\apache-maven-*") do if exist "%%d\bin\mvn.cmd" set "MVN_CMD=%%d\bin\mvn.cmd"
)
if not defined MVN_CMD (
  echo [INFO] Maven not found. Downloading Apache Maven 3.9.9 ...
  set "DL=%BASE%\tools\maven"
  if not exist "!DL!" mkdir "!DL!"
  powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://maven.aliyun.com/repository/central/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip' -OutFile '!DL!\apache-maven-3.9.9-bin.zip'"
  if not exist "!DL!\apache-maven-3.9.9-bin.zip" (
    echo [ERROR] Maven download failed (no network?). See start-guide.md.
    goto :NEEDHELP
  )
  powershell -NoProfile -Command "Expand-Archive -Path '!DL!\apache-maven-3.9.9-bin.zip' -DestinationPath '!DL!' -Force"
  for /d %%d in ("!DL!\apache-maven-*") do if exist "%%d\bin\mvn.cmd" set "MVN_CMD=%%d\bin\mvn.cmd"
)
if not defined MVN_CMD (
  echo [ERROR] Maven setup failed. See start-guide.md.
  goto :NEEDHELP
)
echo [OK] Maven = %MVN_CMD%

REM ---- 2. Repair corrupt .m2 jars ----
echo [INFO] Repairing corrupt .m2 jars (deleted ones re-download on build) ...
powershell -NoProfile -ExecutionPolicy Bypass -File "%BASE%\repair-m2.ps1"

REM ---- 3. MySQL ----
set "DBPW=123456"
set "MYSQLBIN="
for /d %%d in ("C:\Program Files\MySQL\MySQL Server*\bin") do set "MYSQLBIN=%%d"
echo [INFO] Checking MySQL at 127.0.0.1:3306 ...
netstat -ano | findstr /c:":3306 " | findstr /i "LISTENING" >nul
if errorlevel 1 (
  echo [INFO] MySQL not listening, trying to start service (needs Administrator) ...
  net start MySQL80 >nul 2>&1
  net start MySQL >nul 2>&1
  net start MySQL57 >nul 2>&1
  timeout /t 6 >nul
)
if defined MYSQLBIN (
  "%MYSQLBIN%\mysqladmin" -h 127.0.0.1 -uroot -p%DBPW% ping >nul 2>&1
  if errorlevel 1 (
    echo [WARN] MySQL still not reachable on 127.0.0.1:3306 (root/%DBPW%).
    echo        Start MySQL manually, then rerun this script. See start-guide.md.
  ) else (
    echo [OK] MySQL is up.
  )
) else (
  echo [WARN] MySQL client not found; cannot verify DB. Backend may fail to connect.
)

REM ---- 4. Backend ----
echo.
echo [1/2] Starting backend ...
echo.
call :START_BACKEND

REM ---- 5. Frontend ----
echo.
echo [2/2] Starting frontend ...
echo.
call :START_FRONTEND

echo.
echo ============================================================
echo   Launch finished. Current status:
echo ============================================================
echo.
call :STATUS
if /i not "%~1"=="/nopause" pause
endlocal
exit /b 0

REM ==================== backend ====================
:START_BACKEND
set "PORT=8081"
set "BACKEND_HOME=%BASE%\springBootTest"
set "JAR=%BACKEND_HOME%\target\springBootTest-0.0.1-SNAPSHOT.jar"
set "LOG_DIR=%BACKEND_HOME%\log"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
for /f "tokens=1-4 delims=/ " %%a in ("%date%") do (set "D=%%a%%b%%c")
for /f "tokens=1-3 delims=:." %%a in ("%time%") do (set "T=%%a%%b%%c")
set "T=%T: =0%"
set "LOG=%LOG_DIR%\backend-%D%-%T%.log"

echo ============================================================
echo   ZhanShen backend launcher (port %PORT%)
echo ============================================================

netstat -ano | findstr /c:":%PORT% " | findstr /i "LISTENING" >nul
if not errorlevel 1 (
  echo [OK] Port %PORT% already in use - assuming backend already running.
  goto BACKEND_DONE
)
echo [OK] Port %PORT% is free.

if not defined MVN_CMD (
  echo [ERROR] Need Maven to build, but mvn not found.
  goto BACKEND_FAIL
)
if exist "%JAR%" (echo [INFO] Rebuilding backend (always fresh build) ...) else (echo [INFO] Building backend (first build downloads deps, may take minutes) ...)
pushd "%BACKEND_HOME%"
call "%MVN_CMD%" package -DskipTests
set "MVN_RC=%errorlevel%"
popd
if not "%MVN_RC%"=="0" (
  echo [ERROR] Maven build failed (exit %MVN_RC%). See log above.
  goto BACKEND_FAIL
)
echo [OK] Build complete.

:JAR_OK
echo [OK] jar ready: %JAR%
echo [INFO] Launching backend ...
set "BE_CMD=chcp 65001 >nul & java -jar %JAR% --server.port=%PORT% --spring.profiles.active=dev > %LOG% 2>&1"
pushd "%BACKEND_HOME%"
start "ZhanShen-Backend-%PORT%" /min cmd /k "%BE_CMD%"
popd

echo [INFO] Waiting for backend (up to 90s) ...
set /a "TRIES=0"
:BE_WAIT
timeout /t 3 /nobreak >nul
curl -s -o nul -m 3 http://localhost:%PORT%/actuator/health
if not errorlevel 1 goto BE_READY
set /a TRIES+=1
if !TRIES! GEQ 30 (
  echo [WARN] Backend timeout. Log: %LOG%
  echo ---------- log tail ----------
  powershell -NoProfile -Command "Get-Content -Encoding UTF8 '%LOG%' -Tail 30"
  goto BACKEND_FAIL
)
goto BE_WAIT

:BE_READY
echo.
echo ============================================================
echo   [OK] Backend up: http://localhost:%PORT%/
echo    - Swagger : http://localhost:%PORT%/swagger-ui.html
echo    - Health  : http://localhost:%PORT%/actuator/health
echo    - Log     : %LOG%
echo ============================================================
goto BACKEND_DONE

:BACKEND_FAIL
echo.
echo [FAIL] Backend not started. Follow the hints above.
goto BACKEND_DONE

:BACKEND_DONE
goto :EOF

REM ==================== frontend ====================
:START_FRONTEND
set "PORT=5173"
set "BACKEND_PORT=8081"
set "APP_HOME="
if exist "%BASE%\zhanshen\package.json" set "APP_HOME=%BASE%\zhanshen"
if not defined APP_HOME (
  echo [ERROR] Frontend dir (zhanshen) not found.
  goto FRONTEND_DONE
)
echo [OK] Frontend dir: %APP_HOME%

set "LOG_DIR=%APP_HOME%\log"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
for /f "tokens=1-4 delims=/ " %%a in ("%date%") do (set "D=%%a%%b%%c")
for /f "tokens=1-3 delims=:." %%a in ("%time%") do (set "T=%%a%%b%%c")
set "T=%T: =0%"
set "LOG=%LOG_DIR%\frontend-%D%-%T%.log"

echo ============================================================
echo   ZhanShen frontend launcher (port %PORT%)
echo ============================================================

where node >nul 2>nul
if errorlevel 1 (
  set "FOUND="
  for /d %%d in ("%USERPROFILE%\.workbuddy\binaries\node\versions\*") do if exist "%%d\node.exe" set "FOUND=%%d"
  if not defined FOUND (
    echo [ERROR] Node.js not found. Install from https://nodejs.org
    goto FRONTEND_DONE
  )
  set "PATH=!FOUND!;!PATH!"
  echo [OK] Using managed Node: !FOUND!
) else (
  echo [OK] Node ready
)

netstat -ano | findstr /c:":%BACKEND_PORT% " | findstr /i "LISTENING" >nul
if errorlevel 1 (
  echo [WARN] Backend (port %BACKEND_PORT%) not running - /api calls will fail.
) else (
  echo [OK] Backend %BACKEND_PORT% is listening
)

netstat -ano | findstr /c:":%PORT% " | findstr /i "LISTENING" >nul
if not errorlevel 1 (
  echo [OK] Port %PORT% already in use - assuming frontend already running.
  goto FE_OPEN
)

if exist "%APP_HOME%\node_modules" goto FE_DEPS_OK
echo [INFO] node_modules missing, installing (first run may take minutes) ...
pushd "%APP_HOME%"
call npm install
set "NPM_RC=%errorlevel%"
popd
if not "%NPM_RC%"=="0" (
  echo [ERROR] npm install failed (exit %NPM_RC%). Check network/npm registry.
  goto FRONTEND_DONE
)
echo [OK] Dependencies installed.

:FE_DEPS_OK
echo [OK] Dependencies ready.
echo [INFO] Launching Vite dev server ...
set "FE_CMD=chcp 65001 >nul & npm run dev > %LOG% 2>&1"
pushd "%APP_HOME%"
start "ZhanShen-Frontend-%PORT%" /min cmd /k "%FE_CMD%"
popd

echo [INFO] Waiting for frontend (up to 60s) ...
set /a "TRIES=0"
:FE_WAIT
timeout /t 2 /nobreak >nul
curl -s -o nul -m 3 http://localhost:%PORT%/
if not errorlevel 1 goto FE_READY
set /a TRIES+=1
if !TRIES! GEQ 30 (
  echo [WARN] Frontend timeout. Log: %LOG%
  echo ---------- log tail ----------
  powershell -NoProfile -Command "Get-Content -Encoding UTF8 '%LOG%' -Tail 25"
  goto FRONTEND_DONE
)
goto FE_WAIT

:FE_READY
echo.
echo ============================================================
echo   [OK] Frontend up: http://localhost:%PORT%/
echo    - API proxy: /api -> http://localhost:%BACKEND_PORT%
echo    - Log     : %LOG%
echo ============================================================

:FE_OPEN
set "CHROME_EXE=C:\Program Files\Google\Chrome\Application\chrome.exe"
if not exist "%CHROME_EXE%" set "CHROME_EXE=C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"
if exist "%CHROME_EXE%" (start "" "%CHROME_EXE%" "http://localhost:%PORT%/") else (start "" "http://localhost:%PORT%/")
goto FRONTEND_DONE

:FRONTEND_DONE
goto :EOF

REM ==================== status ====================
:STATUS
set "FE_PORT=5173"
set "BE_PORT=8081"
set "DB_PORT=3306"
echo ---[ Frontend  port %FE_PORT% ]---
netstat -ano | findstr /c:":%FE_PORT% " | findstr /i "LISTENING" >nul
if errorlevel 1 (echo   listening: NO) else (echo   listening: YES  http://localhost:%FE_PORT%/)
echo ---[ Backend   port %BE_PORT% ]---
netstat -ano | findstr /c:":%BE_PORT% " | findstr /i "LISTENING" >nul
if errorlevel 1 (echo   listening: NO) else (echo   listening: YES  http://localhost:%BE_PORT%/  Swagger /swagger-ui.html)
echo ---[ MySQL    port %DB_PORT% ]---
netstat -ano | findstr /c:":%DB_PORT% " | findstr /i "LISTENING" >nul
if errorlevel 1 (echo   listening: NO  -> backend will fail to connect) else (echo   listening: YES)
echo.
goto :EOF

:NEEDHELP
echo.
echo Setup failed. Read start-guide.md for step-by-step instructions.
pause
