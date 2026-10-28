@echo off
setlocal EnableDelayedExpansion
REM ============================================================
REM  ZhanShen backend launcher (port 8081)
REM  Runs the already-built Spring Boot jar in this window.
REM  Normally invoked by start-all.bat; you can also run it alone
REM  (run start-all.bat first once, so the jar exists).
REM ============================================================

set "BASE=%~dp0"
if "%BASE:~-1%"=="\" set "BASE=%BASE:~0,-1%"
set "PORT=8081"
set "BACKEND_HOME=%BASE%\springBootTest"
set "JAR=%BACKEND_HOME%\target\springBootTest-0.0.1-SNAPSHOT.jar"
set "LOG_DIR=%BACKEND_HOME%\log"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
for /f "tokens=1-4 delims=/ " %%a in ("%date%") do (set "D=%%a%%b%%c")
for /f "tokens=1-3 delims=:." %%a in ("%time%") do (set "T=%%a%%b%%c")
set "T=%T: =0%"
set "LOG=%LOG_DIR%\backend-console-%D%-%T%.log"
set "PS_TEE=%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe"

REM ---- JDK ----
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
  pause
  exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM ---- avoid localhost going through HTTP proxy ----
set "no_proxy=localhost,127.0.0.1"
set "NO_PROXY=localhost,127.0.0.1"

netstat -ano | findstr /c:":%PORT% " | findstr /i "LISTENING" >nul
if not errorlevel 1 (
  echo [WARN] Port %PORT% is already in use - backend may already be running.
  echo        Run stop-all.bat first if you want a fresh restart.
)

if not exist "%JAR%" (
  echo [ERROR] Jar not found: %JAR%
  echo         Run start-all.bat once to build it, then retry.
  pause
  exit /b 1
)

echo ============================================================
echo   ZhanShen backend (port %PORT%)
echo   Minimize this window freely - closing it stops the backend.
echo ============================================================
echo.
"%JAVA_HOME%\bin\java.exe" -Dfile.encoding=UTF-8 -jar "%JAR%" 2>&1 | "%PS_TEE%" -NoProfile -Command "$input | Tee-Object -FilePath '%LOG%' -Append"
set "RC=%errorlevel%"
echo.
echo [EXIT] Backend stopped, exit code %RC%. Full log: %LOG%
pause
endlocal
exit /b %RC%
