# 性格雷达·生活指南

这是一个前后端分离的课程演示初版，覆盖注册登录、测试答题、画像报告、雷达图、推荐反馈、双人适配、分享链接和后台管理。

## 目录

- `backend/`：Spring Boot 3 后端。
- `frontend/`：Vue 3 + Vite 前端。
- `infra/`：Docker Compose，包含 MySQL、Redis 和后端服务。
- `docs/`：部署、接口和测试说明。

## 快速开始

```powershell
cd infra
docker compose up --build
```

```powershell
cd frontend
npm install
npm run dev
```

默认访问地址：`http://127.0.0.1:5173`。

更多说明见：

- [部署说明](docs/deployment.md)
- [接口说明](docs/api.md)
- [测试说明](docs/testing.md)
