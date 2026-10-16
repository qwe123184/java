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

## 2026-10-16 修复：前端启动器 ENOENT（npm 找不到 package.json）

**现象**：双击根目录 `start-frontend.bat` 后报错，脚本却继续走到 Vite 启动并空等 60 秒：

```
npm ERR! path C:\Users\Administrator\Desktop\java-main\package.json
npm ERR! enoent Could not read package.json: ENOENT: no such file or directory
```

**根因**：该脚本用 `set "APP_HOME=%~dp0"`（脚本所在目录）当项目目录，而它当时被放在**根目录**；但前端工程（`package.json` / `vite.config.js` / `src` / `index.html`）实际在 `zhanshen/` 子目录下。于是 npm 在根目录找不到 `package.json`。另有一处控制流缺陷：`call npm install` 失败后没被拦住（嵌套 if 块的 errorlevel 判断未生效），脚本继续执行 `npm run dev`，把 npm 报错刷屏并白等 60 秒超时。

**修复内容**：

- `start-frontend.bat` 新增**前端目录自动识别**：依次探测 `%~dp0`、`zhanshen\`、`frontend\`、`web\`，取第一个含 `package.json` 的目录；全都不是就直接报错退出。因此该脚本放在根目录或 `zhanshen\` 内都能用（两处各放一份）。
- 依赖安装段改为 `goto` 跳转结构（彻底避开嵌套 if 块陷阱）；`npm install` 退出码非 0 时立即报错退出，不再空跑 Vite。
- 日志目录改为 `<前端目录>\log`（与历史日志位置一致）。
- 失败时 `pause`，双击运行时窗口不会一闪而过；被 `restart-*.bat` 调用时传 `/nopause` 跳过。
- 补齐缺失脚本：`stop-frontend.bat`（只杀 5173 上进程名为 `node.exe` 的进程，防误杀）、`springBootTest\start-backend.bat` / `stop-backend.bat`（自动定位 Maven → jar 缺失自动 `mvn package -DskipTests` → 端口检查 → 后台启动 → `/actuator/health` 就绪检查），并修正 `restart-frontend.bat` / `restart-backend.bat` 的调用参数。
- 旧脚本备份在 `_bak_20261016\`；另有误生成的空 `package-lock.json` 一并移入该目录。

**已验证（2026-10-16）**：

- 前端：`zhanshen/` 内 `npm install` 成功（60 个包 / 41s）；`npm run dev` 起 Vite 5.4.21，`http://localhost:5173/` 返回 200。
- 后端：`mvn package -DskipTests` → BUILD SUCCESS，`target/springBootTest-0.0.1-SNAPSHOT.jar` 已生成；`java -jar` 启动后 Tomcat 监听 8081，`/actuator/health` 返回 `{"status":"UP"}` 且 db 组件 UP（MySQL 已连）。
- 接口前缀是 **`/api/user`**（`AuthController` 的 `@RequestMapping("/api/user")`），免鉴权白名单为 `/api/user/login`、`/api/user/register`、`/api/user/refresh`；其余 `/api/**` 都要求 accessToken，未携带会返回 `401 未携带有效访问令牌`（探错前缀如 `/api/auth/...` 就是 401，别误判成 bug）。
- Git Bash 下直接跑 `mvn` 会报 `ClassNotFoundException: org.codehaus.plexus.classworlds.launcher.Launcher`（MSYS 把 classpath 路径改写坏了）。绕过方式：用 `mvn.cmd`，或直接调 classworlds：
  `java -classpath "<maven>/boot/plexus-classworlds-2.11.0.jar" -Dclassworlds.conf="<maven>/bin/m2.conf" -Dmaven.home="<maven>" -Dmaven.multiModuleProjectDirectory="<项目目录>" org.codehaus.plexus.classworlds.launcher.Launcher package -DskipTests`

**注意**：

- AI 工具会话无法直接执行 `.bat`（cmd.exe 被安全策略拦截），只能验证等价命令，脚本本身请双击运行确认。
- `斩神\` 目录是前端的一份**陈旧重复副本**（其 `start.bat` 仍硬编码已失效的 `C:\Users\Administrator\WorkBuddy\2026-09-18-14-06-25\zhan-shen-vue`），建议删除或忽略，勿与 `zhanshen\` 混用。
- 根目录的 `start-all.bat` / `stop-all.bat` / `restart-all.bat` 曾在压缩包层层解压过程中丢失，**已于 2026-10-16 重建**：`start-all.bat` 先后端后前端依次拉起，`stop-all.bat` 先停前端再停后端，`restart-all.bat`（可加 `rebuild` 强制重打包后端）串起整条链路；三者均通过 `/nopause` 调用子脚本、只在最外层 pause。

## 2026-10-16 修复（二）：端口被占被误判为「失败」

**现象**：前端明明已经启动成功，再双击一次 `start-frontend.bat` 却输出：

```
[OK] 前端目录: ...\Desktop\java-main\zhanshen
[OK] Node 已就绪
[警告] 后端 (端口 8081) 未在运行, /api 请求将失败.
[错误] 端口 5173 已被占用, 前端可能已在运行.
       如需重启请先运行 stop-frontend.bat
[失败] 前端未启动, 请按上面的提示处理.
```

用户容易误以为启动失败。实测当时 5173 上的进程就是本项目 Vite（PID 为 node.exe，页面标题「斩神之凡尘神域 · 守夜人认证」，`/@vite/client` 返回 200），`zhanshen\log\frontend-20261016-143333.log` 里也写着 `VITE v5.4.21 ready in 359 ms` —— **服务完全正常**。

**根因**：启动脚本把「端口已被占用」一律当成致命错误，缺少三态判定。同时 `zhanshen\vite.config.js` 没有 `strictPort`，5173 被占时 Vite 会静默改到 5174/5175，导致脚本提示的地址与实际端口不一致。

**修复内容**：所有启停脚本改为**端口三态判定**（幂等，重复双击安全）：

| 分支 | 判定方式 | 行为 |
|---|---|---|
| (a) 端口空闲 | `netstat` + `LISTENING` | 正常启动 |
| (b) 本项目已在运行 | `GET /@vite/client` 命中 `vite`（后端为 `/actuator/health` 命中 `UP`） | 报 **[成功] 已在运行**，退出码 0，不重复启动 |
| (c) 被其它程序占用 | 探测失败 | 报错并列出占用 PID + 镜像名，提示用 stop 脚本处理 |

- `vite.config.js` 加 `strictPort: true`：端口被占时直接报错退出，不再偷偷换端口。
- `stop-frontend.bat` / `stop-backend.bat` 重写：PID 去重（IPv4/IPv6 同 PID 只杀一次）、结束前后都校验镜像名、结束后**复查端口是否真的释放**，失败给明确提示而不是静默返回。
- 新增 `status.bat`：只读体检，分别显示前端/后端/MySQL 是否监听、PID、是否为本项目、访问地址；`start-all` / `stop-all` / `restart-all` 结束时都会自动调用它。
- 启动成功后自动打开浏览器（脚本顶部 `set "OPEN_BROWSER=1"` 可关闭）。
- 修正 `restart-frontend.bat` / `restart-backend.bat` 缺少 `/nopause` 判断的问题 —— 原先被 `restart-all.bat` 调用时会卡在「按任意键继续」。
- 子进程日志改为 `chcp 65001` 输出（UTF-8），Vite 的 `➜` 等符号不再显示成乱码；脚本读日志尾部时用 `Get-Content -Encoding UTF8`。
- 旧版本备份在 `_bak_20261016\*.bak2`。

**日常用法**：不确定有没有在跑就先双击 `status.bat`；启动用 `start-all.bat`；改代码不生效用 `restart-all.bat`。重复双击 `start-*.bat` 是安全的，只会提示「已在运行」。

## 首页后端功能（同日 14:51 用户需求：完成首页后端，含搜索/展示，自己建表）

**新增数据表 `zhan_shen.t_archive`（神域档案，首页展示/检索数据源）**
- 字段：`id`(PK自增) `name` `type`(禁墟/神秘/守夜人/任务) `realm`(境界) `description` `status`(0下架/1展示) `sort`(排序) `create_time` `update_time`。
- 唯一键 `uk_name`，`schema.sql` 用 `CREATE TABLE IF NOT EXISTS` 随启动自动建表；`data.sql` 用 `INSERT IGNORE` 幂等灌入 12 条示例档案（重启不重复）。
- 建表/灌数据走 `spring.sql.init`（dev profile 已开启 `mode=always`）。

**新增后端代码**（包 `com.fenglin.springboottest`）
- `entity/Archive.java`、`mapper/ArchiveMapper.java`、`common/ArchiveVO.java`（`desc` 映射 description）。
- `service/ArchiveService.java` + `impl/ArchiveServiceImpl.java`：`search(keyword,type,page,size)`（status=1 + 类型筛选 + 关键词 like name/description/realm/type，按 sort/id 倒序分页）、`detail(id)`。
- `controller/ArchiveController.java`：`GET /api/archive/list`、`GET /api/archive/{id}`。位于 `/api/**` 之下，**需携带有效访问令牌**（AuthInterceptor 已覆盖）。

**前端接入**（`zhanshen/`）
- 新增 `src/api/archive.js`（`archiveList` / `archiveDetail`，复用 auth.js 的 axios 实例，自动带 Bearer）。
- `HomeView.vue` 改为进入神域即 `archiveList` 拉取档案（替代原先静态 `items`）；搜索框/分类 chips 变化走 300ms 防抖后调后端；保留登录与 `me` 接口不变。

**已实测（同进程内启动后端 + curl）**：注册→登录拿 token→
- `GET /api/archive/list` 无筛选返回 12 条；
- `keyword=林七夜` 命中 2 条（凡尘神域/守夜人·林七夜）；
- `type=任务` 命中 3 条；
- `GET /api/archive/1` 返回 凡尘神域 详情；
- 无 token 访问返回 **401**（鉴权生效）。
- `mvn package -DskipTests` → BUILD SUCCESS（26 源文件）；`vite build` → 95 模块编译通过。

## 仍存在的缺失项（非阻塞）

- 后端缺 `.mvn/wrapper/`（mvnw 损坏），缺真正的 README（`HELP.md` 是 Spring Initializr 模板）。
- 前端无 README。
- 数据库密码硬编码 123456（`application-dev.properties` 与脚本中），生产前应改环境变量注入。
- 根目录的 `AI_README.md` 与 `start-all/stop-all/restart-all.bat` 不在任何 git 仓库内（两个子项目是独立仓库，根目录未纳入版本管理）。
