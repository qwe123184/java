# AI_README - 斩神项目上下文速查（写给 AI 助手的备忘）

> 本文件供 AI 助手在每次新会话中快速恢复上下文，避免重复探索。
> 电脑每次重启会重置环境（`node_modules`、`target` 会被删除），按下面"快速启动"执行即可。

## 项目布局（相对本文件所在目录）

| 目录 | 说明 | 端口 |
|---|---|---|
| `springBootTest/` | 后端：Spring Boot 3.4.5 + MyBatis-Plus 3.5.7 + MySQL 8 + JWT + springdoc | 8081 (dev) |
| `zhanshen/` | 前端：Vue 3 + Vite，`/api` 代理到 8081（见 `zhanshen/vite.config.js`） | 5173 |

## 快速启动（开机后首选）

直接运行根目录脚本（GBK 编码、全相对路径，缺失依赖自动补齐）：

- `start-all.bat` — 一键启动前后端（先后端后前端）
- `stop-all.bat` — 一键停止
- `restart-all.bat` / `restart-all.bat rebuild` — 重启（可强制重打包后端 jar）

单端脚本在各自子目录：`start-backend.bat` / `stop-backend.bat` / `restart-backend.bat`、`start-frontend.bat` / `stop-frontend.bat` / `restart-frontend.bat`。

脚本行为：jar 缺失 → 自动 `mvn package -DskipTests`；`node_modules` 缺失 → 自动 `npm install`。
**重置后首次启动必须联网**（Maven 依赖与 npm 包都要重新下载，需几分钟）。

## 环境关键事实（2026-10-12 验证）

- JDK：Zulu 21.0.10 在 PATH（项目 target 17，兼容）。
- Maven：**不在 PATH**。优先用 `where mvn`，否则扫描 `%USERPROFILE%\.m2\wrapper\dists\apache-maven-*-bin\*\apache-maven-*\bin\mvn.cmd`（脚本已内置该逻辑）。项目 `./mvnw` 损坏（缺 `.mvn/wrapper/maven-wrapper.properties`），不要用。
- Maven 镜像：`~/.m2/settings.xml` 配了 aliyunmaven；**首次构建禁止 `-o` 离线模式**（本地仓库缺依赖会失败）。
- Node：PATH 里有 WorkBuddy 托管 v22；否则扫描 `%USERPROFILE%\.workbuddy\binaries\node\versions\*`。
- MySQL 8：127.0.0.1:3306，root / 123456，库 `zhan_shen`（启动时 schema.sql 幂等自动建库建表）。客户端：`C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`，查询加 `--default-character-set=utf8mb4`。
- 后端启动参数：`java -jar target/springBootTest-0.0.1-SNAPSHOT.jar --server.port=8081 --spring.profiles.active=dev`，需环境变量 `DB_PASSWORD=123456`。
- 测试账号：`smoke_test_1012` / `smoke1234`（我创建的冒烟账号，如库被重置则不存在）。

## 入口地址

- 前端 http://localhost:5173/ ｜ 后端 http://localhost:8081/
- Swagger http://localhost:8081/swagger-ui.html ｜ 健康检查 http://localhost:8081/actuator/health

## 已知坑（别再踩）

1. **bat 必须 GBK 编码 + CRLF**。cmd 按 GBK 解析批处理，UTF-8 中文会把 REM 注释解析成乱码命令导致脚本崩溃。转换命令：`iconv -f UTF-8 -t GBK src.utf8 | sed 's/$/\r/' > out.bat`。
2. **AI 工具会话里拉起的进程会被回收**。在 AI 的 Bash/PowerShell 工具里启动的服务，会话结束后会被杀掉（日志尾部出现 `^C`）。验证脚本用 `Start-Process` 或让用户双击运行；不要声称"服务会持续运行"。
3. `netstat -ano | findstr LISTENING` 判断端口；stop 脚本仅杀 java/node 进程（防误杀）。
4. PowerShell 工具不回显 stdout，需把输出重定向到文件再读；cmd.exe 不能从 Git Bash 里调用（被安全策略拦截）。
5. 数据库历史账号（testuser01 等）已于 2026-10-12 前丢失，不是启动脚本造成的。

## 已修复项（2026-10-12 第二轮）

- ✅ 两个子项目均已 `git init`（main 分支）并完成首次提交，含 `.gitignore`（后端忽略 target/log，前端忽略 node_modules/dist/log）。git 身份用 qwe123184 + GitHub noreply 邮箱（仅仓库本地配置）。**尚未关联远程仓库**，如要推送：`git remote add origin <url>` 后 `git push -u origin main`。
- ✅ `application-prob.properties` 已改名 `application-prod.properties`（无代码引用旧名，已确认安全）。
- ✅ 前端 `zhanshen/start.bat` 已改为转发到 `start-frontend.bat`。

## 仍存在的缺失项（非阻塞）

- 后端缺 `.mvn/wrapper/`（mvnw 损坏），缺真正的 README（`HELP.md` 是 Spring Initializr 模板）。
- 前端无 README。
- 数据库密码硬编码 123456（`application-dev.properties` 与脚本中），生产前应改环境变量注入。
- 根目录的 `AI_README.md` 与 `start-all/stop-all/restart-all.bat` 不在任何 git 仓库内（两个子项目是独立仓库，根目录未纳入版本管理）。
