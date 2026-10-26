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

REM ---- use absolute tool paths so a shadowed PATH cannot break the script ----
set "PING=%SystemRoot%\System32\ping.exe"
if not exist "%PING%" set "PING=ping"
set "CURL=%SystemRoot%\System32\curl.exe"
if not exist "%CURL%" set "CURL=curl"

REM ---- NOTE: keep everything inline ----
REM  Do NOT re-invoke this script with output redirection: a self-call
REM  recurses and deadlocks on the same log file. All output stays in this
REM  window, and the script always ends with "pause" so nothing flashes away.

echo ============================================================
echo   ZhanShen one-click launcher
echo   (MySQL -^> backend 8081 -^> frontend 5173)
echo   Logs: %BASE%\start-all.log  ^| springBootTest\log\  ^| zhanshen\log\
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
where mvn.cmd >nul 2>&1 && set "MVN_CMD=mvn.cmd"
if not defined MVN_CMD where mvn >nul 2>&1 && set "MVN_CMD=mvn"
if not defined MVN_CMD call :FIND_MVN "%BASE%\tools\maven"
if not defined MVN_CMD call :FIND_MVN "%USERPROFILE%\.m2\wrapper\dists"
if not defined MVN_CMD call :FIND_MVN "%USERPROFILE%\.m2\maven"
if not defined MVN_CMD call :FIND_MVN "%USERPROFILE%\scoop\apps\maven"
if not defined MVN_CMD (
  echo [INFO] Maven not found. Downloading Apache Maven 3.9.9 ...
  set "DL=%BASE%\tools\maven"
  if not exist "!DL!" mkdir "!DL!"
  powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://maven.aliyun.com/repository/central/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip' -OutFile '!DL!\apache-maven-3.9.9-bin.zip'"
  if not exist "!DL!\apache-maven-3.9.9-bin.zip" (
    echo [ERROR] Maven download failed ^(no network?^). See start-guide.md.
    goto :NEEDHELP
  )
  powershell -NoProfile -Command "Expand-Archive -Path '!DL!\apache-maven-3.9.9-bin.zip' -DestinationPath '!DL!' -Force"
  call :FIND_MVN "!DL!"
)
if not defined MVN_CMD (
  echo [ERROR] Maven setup failed. See start-guide.md.
  goto :NEEDHELP
)
echo [OK] Maven = %MVN_CMD%

REM ---- 2. Repair corrupt .m2 jars ----
echo [INFO] Repairing corrupt .m2 jars (deleted ones re-download on build) ...
if exist "%BASE%\repair-m2.ps1" (
  powershell -NoProfile -ExecutionPolicy Bypass -File "%BASE%\repair-m2.ps1"
) else (
  echo [INFO] repair-m2.ps1 not found - skipping .m2 repair ^(non-fatal^).
)

REM ---- 3. MySQL ----
set "DBPW=123456"
set "MYSQLBIN="
for /d %%d in ("C:\Program Files\MySQL\MySQL Server*") do if exist "%%d\bin\mysqladmin.exe" set "MYSQLBIN=%%d\bin"
if not defined MYSQLBIN if exist "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysqladmin.exe" set "MYSQLBIN=C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin"
if not defined MYSQLBIN (
  for /f "delims=" %%p in ('where mysqladmin 2^>nul') do set "MYSQLBIN=%%~dpp"
)
if defined MYSQLBIN if "!MYSQLBIN:~-1!"=="\" set "MYSQLBIN=!MYSQLBIN:~0,-1!"
echo [INFO] Checking MySQL at 127.0.0.1:3306 ...
netstat -ano | findstr /c:":3306 " | findstr /i "LISTENING" >nul
if errorlevel 1 (
  echo [INFO] MySQL not listening, trying to start service ^(needs Administrator^) ...
  net start MySQL80 >nul 2>&1
  net start MySQL >nul 2>&1
  net start MySQL57 >nul 2>&1
  %PING% -n 7 127.0.0.1 >nul
)
if defined MYSQLBIN (
  "%MYSQLBIN%\mysqladmin" -h 127.0.0.1 -uroot -p%DBPW% ping >nul 2>&1
  if errorlevel 1 (
    echo [WARN] MySQL still not reachable on 127.0.0.1:3306 ^(root/%DBPW%^).
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
if exist "%JAR%" (echo [INFO] Rebuilding backend ^(always fresh build^) ...) else (echo [INFO] Building backend ^(first build downloads deps, may take minutes^) ...)
pushd "%BACKEND_HOME%"
call "%MVN_CMD%" package -DskipTests
set "MVN_RC=%errorlevel%"
popd
if not "%MVN_RC%"=="0" (
  echo [ERROR] Maven build failed ^(exit %MVN_RC%^). See log above.
  goto BACKEND_FAIL
)
echo [OK] Build complete.

:JAR_OK
echo [OK] jar ready: %JAR%
echo [INFO] Launching backend in a minimized window ...
start "ZhanShen-Backend-%PORT%" /min "%BASE%\start-backend.bat"

echo [INFO] Waiting for backend (up to 90s) ...
set /a "TRIES=0"
:BE_WAIT
%PING% -n 4 127.0.0.1 >nul
%CURL% -s -o NUL -m 3 http://localhost:%PORT%/actuator/health
if not errorlevel 1 goto BE_READY
set /a TRIES+=1
if !TRIES! GEQ 30 (
  echo [WARN] Backend did not answer in time.
  echo        Restore the minimized window "ZhanShen-Backend-%PORT%" to read the error.
  goto BACKEND_FAIL
)
goto BE_WAIT

:BE_READY
echo.
echo ============================================================
echo   [OK] Backend up: http://localhost:%PORT%/
echo    - Swagger : http://localhost:%PORT%/swagger-ui.html
echo    - Health  : http://localhost:%PORT%/actuator/health
echo    - Console : minimized window "ZhanShen-Backend-%PORT%"
echo ============================================================
goto BACKEND_DONE

:BACKEND_FAIL
echo.
echo [FAIL] Backend not started. See the minimized backend window.
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
  echo [ERROR] Frontend dir ^(zhanshen^) not found.
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
  echo [WARN] Backend ^(port %BACKEND_PORT%^) not running - /api calls will fail.
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
  echo [ERROR] npm install failed ^(exit %NPM_RC%^). Check network/npm registry.
  goto FRONTEND_DONE
)
echo [OK] Dependencies installed.

:FE_DEPS_OK
echo [OK] Dependencies ready.
echo [INFO] Launching Vite dev server in a minimized window ...
start "ZhanShen-Frontend-%PORT%" /min "%BASE%\start-frontend.bat"

echo [INFO] Waiting for frontend (up to 60s) ...
set /a "TRIES=0"
:FE_WAIT
%PING% -n 3 127.0.0.1 >nul
%CURL% -s -o NUL -m 3 http://localhost:%PORT%/
if not errorlevel 1 goto FE_READY
set /a TRIES+=1
if !TRIES! GEQ 30 (
  echo [WARN] Frontend did not answer in time.
  echo        Restore the minimized window "ZhanShen-Frontend-%PORT%" to read the error.
  goto FRONTEND_DONE
)
goto FE_WAIT

:FE_READY
echo.
echo ============================================================
echo   [OK] Frontend up: http://localhost:%PORT%/
echo    - API proxy : /api -^> http://localhost:%BACKEND_PORT%
echo    - Console   : minimized window "ZhanShen-Frontend-%PORT%"
echo ============================================================

:FE_OPEN
set "CHROME_EXE=C:\Program Files\Google\Chrome\Application\chrome.exe"
if not exist "%CHROME_EXE%" set "CHROME_EXE=C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"
if exist "%CHROME_EXE%" (start "" "%CHROME_EXE%" "http://localhost:%PORT%/") else (start "" "http://localhost:%PORT%/")
goto FRONTEND_DONE

:FRONTEND_DONE
goto :EOF

REM ==================== helpers ====================
REM Find mvn.cmd under a directory tree (robust replacement for nested FOR /D loops)
:FIND_MVN
if not exist "%~1" goto :EOF
for /f "delims=" %%p in ('dir /b /s "%~1\mvn.cmd" 2^>nul') do if not defined MVN_CMD set "MVN_CMD=%%p"
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
if errorlevel 1 (echo   listening: NO  -^> backend will fail to connect) else (echo   listening: YES)
echo.
goto :EOF

:NEEDHELP
echo.
echo Setup failed. Read start-guide.md for step-by-step instructions.
if /i not "%~1"=="/nopause" pause
exit /b 1
