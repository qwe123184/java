# ZhanShen 一键启动指南 (start-all.bat)

`start-all.bat` 会按顺序启动：MySQL → 后端 (8081) → 前端 (5173)，最后打开浏览器。

## 前置要求
- **JDK 17+**：脚本会自动探测 `C:\Program Files\Zulu\zulu-21`、`C:\Program Files\Java\jdk*`。
  没有就装一个并设好 `JAVA_HOME`。
- **Node.js**：前端 `npm` 需要它。建议 Node 18+。
- **MySQL 8**，监听 `127.0.0.1:3306`，账号 `root` / 密码 `123456`。
  启动脚本会尝试 `net start MySQL80` / `MySQL` / `MySQL57`，**这一步需要以管理员身份运行本脚本**，
  否则请自己先手动启动 MySQL。
- **Maven**：脚本会依次查找系统 `mvn`、`.\tools\maven\`、`%USERPROFILE%\.m2\wrapper\dists\`、
  `%USERPROFILE%\.m2\maven\`；都找不到就自动从阿里云下载 3.9.9 到 `.\tools\maven\`。

## 运行方式
直接双击 `start-all.bat` 即可。想让窗口不暂停（例如被别的脚本调用），加参数：
```
start-all.bat /nopause
```

## 端口与地址
- 前端：http://localhost:5173  （`/api` 已代理到后端 8081）
- 后端：http://localhost:8081  （Swagger：`/swagger-ui.html`，健康检查：`/actuator/health`）
- 数据库：127.0.0.1:3306

## 后台管理
- 执政官(管理员)登录页：http://localhost:5173/#/admin/login
- 内置管理员账号：`admin` / `admin123`（上线前请改密码）
- 新增更多管理员：先正常注册，再执行
  ```sql
  UPDATE zhan_shen.t_user SET role='ADMIN' WHERE username='你要提权的用户名';
  ```

## 停服
运行同目录的 `stop-all.bat` 关闭前后端进程。

## 排错
- 后端一直起不来：先看 `springBootTest\log\backend-*.log` 尾部。最常见是 MySQL 没开 / 账号密码不对。
- 前端 401：未登录或令牌失效，`/api/**` 需要登录令牌，属正常。
- `.m2` 里 jar 损坏导致构建失败：脚本第 2 步会调用 `repair-m2.ps1` 清理损坏标记后重新下载。
