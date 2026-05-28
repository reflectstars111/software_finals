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

## 启动前端

在 `frontend/` 目录执行：

```powershell
npm install
npm run dev
```

前端开发地址：`http://127.0.0.1:5173`。

Vite 已配置 `/api` 代理到 `http://127.0.0.1:8080`。

## 本机 Maven 验证命令

如果系统默认 Java 是 1.8，先在当前 PowerShell 会话临时指定 JDK：

```powershell
$env:JAVA_HOME='C:\Program Files\Android\openjdk\jdk-21.0.8'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
& 'D:\allprogram\apache-maven-3.9.16\bin\mvn.cmd' test
```

这不会修改系统环境变量。
