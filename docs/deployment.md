# 性格雷达部署说明

## 环境要求

- 后端运行环境：JDK 17 或更高版本。当前机器可临时使用 `C:\Program Files\Android\openjdk\jdk-21.0.8`。
- Maven：`D:\allprogram\apache-maven-3.9.16\bin\mvn.cmd`。
- 前端：Node.js 20 或更高版本，npm 或 pnpm。
- 基础设施：Docker Desktop，用于启动 MySQL 8.0、Redis 7 和后端容器。

## 默认账号

| 角色 | 手机号 | 密码 |
| --- | --- | --- |
| 管理员 | `13800000000` | `Admin@123456` |
| 普通用户 | `13900000001` | `User123456` |
| 好友演示用户 | `13900000002` | `User123456` |

## 启动基础设施和后端

在 `infra/` 目录执行：

```powershell
docker compose up --build
```

服务地址：

- 后端 API：`http://localhost:8080/api`
- 健康检查：`http://localhost:8080/actuator/health`
- MySQL：`localhost:3306`
- Redis：`localhost:6379`

后端默认读取环境变量：

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `JWT_SECRET`
- `CORS_ALLOWED_ORIGINS`
- `PUBLIC_BASE_URL`

## 本机临时服务器一键启动

本机临时服务器推荐链路：

```text
Cloudflare Tunnel -> Caddy :8090 -> frontend/dist + /api -> backend :8080
```

在项目根目录执行：

```powershell
.\deploy\start-temp-server.ps1
```

脚本会依次完成：

- 构建 `frontend/dist`
- `docker compose up -d --build` 启动 MySQL、Redis、后端
- 校验并后台启动 Caddy
- 后台启动 `cloudflared tunnel run personality-radar`

访问地址：

- 本机：`http://127.0.0.1:8090`
- 公网：`https://app.reflectstars.dev`

停止临时服务器：

```powershell
.\deploy\stop-temp-server.ps1
```

如果只想停 Caddy 和 Tunnel，保留 Docker 后端：

```powershell
.\deploy\stop-temp-server.ps1 -KeepDocker
```

常用参数：

```powershell
.\deploy\start-temp-server.ps1 -SkipNpmInstall
.\deploy\start-temp-server.ps1 -SkipFrontendBuild
.\deploy\start-temp-server.ps1 -SkipTunnel
```

运行日志位于 `deploy/runtime/logs/`。

正式对外发布前建议显式设置 `JWT_SECRET`，不要依赖本地开发默认值：

```powershell
$env:JWT_SECRET='替换为至少 32 位的随机字符串'
```

如果通过 Cloudflare Tunnel + Caddy 发布，保持：

- `PUBLIC_BASE_URL=https://app.reflectstars.dev`
- `CORS_ALLOWED_ORIGINS=https://app.reflectstars.dev,https://reflectstars.dev`

## 数据备份

`infra/backup-mysql.ps1` 会调用 `docker exec radar-mysql mysqldump` 导出当前数据库：

```powershell
cd infra
.\backup-mysql.ps1
```

备份文件默认写入 `infra/backups/`。

## 启动前端

在 `frontend/` 目录执行：

```powershell
npm install
npm run dev
```

前端开发地址：`http://127.0.0.1:5173`。

Vite 已配置 `/api` 代理到 `http://127.0.0.1:8080`。

## 前端验收命令

在普通本机 PowerShell 中执行：

```powershell
cd D:\software\finals\frontend
npm test
npm run build
```

预期结果：

- `npm test` 应显示所有 Vitest 用例通过，包括首页、登录、工作台、后台和核心产品逻辑测试。
- `npm run build` 应完成 `vue-tsc -b` 和 `vite build`，并生成 `frontend/dist/`。

如果在受限执行环境中看到 `spawn EPERM`，通常表示该环境禁止 Vite/Vitest 启动 esbuild 子进程；请回到本机 PowerShell 或管理员 PowerShell 复验。

## 本机 Maven 验证命令

如果系统默认 Java 是 1.8，先在当前 PowerShell 会话临时指定 JDK：

```powershell
$env:JAVA_HOME='C:\Program Files\Android\openjdk\jdk-21.0.8'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
& 'D:\allprogram\apache-maven-3.9.16\bin\mvn.cmd' test
```

这不会修改系统环境变量。
