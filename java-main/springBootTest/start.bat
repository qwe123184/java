@echo off
REM ============================================================
REM  ZhanShen backend - one-click launcher (Windows)
REM  Prereqs: JDK 17+ on PATH, MySQL 8 running, root pw 123456
REM  Build first with: mvn package -DskipTests
REM  Port is fixed to 8081; --server.port overrides any env var.
REM ============================================================
setlocal
set DB_PASSWORD=123456
set JAR=target\springBootTest-0.0.1-SNAPSHOT.jar
if not exist "%JAR%" goto NOJAR
echo Starting backend on http://localhost:8081 ...
java -jar "%JAR%" --server.port=8081 --spring.profiles.active=dev
goto END
:NOJAR
echo [ERROR] %JAR% not found. Run: mvn package -DskipTests
pause
:END
endlocal
