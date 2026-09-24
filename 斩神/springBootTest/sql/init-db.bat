@echo off
REM ============================================================
REM  一键建库建表脚本（Windows）
REM  作用：使用本地 MySQL 执行 sql/init.sql，自动创建数据库 zhan_shen 与表 t_user
REM  用法：双击运行，按提示输入 MySQL root 密码即可
REM  说明：若 MySQL 安装路径不同，请修改下方的 MYSQL_BIN
REM ============================================================

SET MYSQL_BIN="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

REM 若默认路径不存在，尝试常见备选路径
IF NOT EXIST %MYSQL_BIN% (
  SET MYSQL_BIN="C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe"
)
IF NOT EXIST %MYSQL_BIN% (
  SET MYSQL_BIN="C:\xampp\mysql\bin\mysql.exe"
)

echo 正在连接 MySQL 并执行 sql/init.sql ...
echo 请输入 MySQL 密码（输入时不可见）：
%MYSQL_BIN% -u root -p < "%~dp0init.sql"

IF ERRORLEVEL 1 (
  echo.
  echo [失败] 执行出错，请检查 MySQL 是否启动、密码是否正确、路径是否匹配。
  pause
  exit /b 1
)

echo.
echo [成功] 数据库 zhan_shen 与表 t_user 已就绪。
pause
