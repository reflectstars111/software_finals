# 性格雷达接口说明

统一响应结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

除注册、登录、分享读取和健康检查外，其余接口需要请求头：

```http
Authorization: Bearer <JWT>
```

## 认证与用户

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/auth/register` | 手机号注册 |
| `POST` | `/api/auth/login` | 登录并返回 JWT |
| `GET` | `/api/users/me` | 当前用户资料 |
| `PUT` | `/api/users/me` | 修改昵称和头像 |

## 测试与报告

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/questions?type=personality` | 获取基础性格题目 |
| `GET` | `/api/questions?type=food` | 获取饮食偏好题目 |
| `GET` | `/api/questions?type=travel` | 获取旅游偏好题目 |
| `POST` | `/api/tests/submit` | 提交答案并生成测试结果 |
| `GET` | `/api/tests/history` | 查看历史测试记录 |
| `GET` | `/api/reports/me` | 获取最新综合画像报告 |

测试提交示例：

```json
{
  "type": "personality",
  "answers": [
    { "questionId": 1, "optionIds": [1] }
  ]
}
```

## 推荐、反馈、适配和分享

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/recommendations?scene=food` | 获取餐饮推荐 |
| `GET` | `/api/recommendations?scene=travel` | 获取旅行推荐 |
| `GET` | `/api/recommendations?scene=outfit` | 获取穿搭推荐 |
| `GET` | `/api/recommendations?scene=career` | 获取生涯推荐 |
| `POST` | `/api/recommendations/{id}/feedback` | 提交推荐反馈 |
| `POST` | `/api/matches` | 根据好友手机号生成双人适配 |
| `GET` | `/api/matches/{id}` | 查看适配报告 |
| `POST` | `/api/shares/report` | 生成报告分享链接 |
| `GET` | `/api/shares/{token}` | 查看只读分享报告 |

反馈提交示例：

```json
{
  "rating": "like",
  "comment": "这个推荐很适合我"
}
```

适配提交示例：

```json
{
  "friendPhone": "13900000002"
}
```

## 管理后台

后台接口需要 `ADMIN` 角色。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/admin/stats` | 基础统计 |
| `GET` | `/api/admin/questions` | 题库列表 |
| `POST` | `/api/admin/questions` | 新增题目 |
| `PUT` | `/api/admin/questions/{id}` | 修改题目 |
| `DELETE` | `/api/admin/questions/{id}` | 停用题目 |
| `GET` | `/api/admin/recommendation-items` | 推荐项列表 |
| `POST` | `/api/admin/recommendation-items` | 新增推荐项 |
| `PUT` | `/api/admin/recommendation-items/{id}` | 修改推荐项 |
| `DELETE` | `/api/admin/recommendation-items/{id}` | 停用推荐项 |
| `GET` | `/api/admin/feedback` | 最近反馈 |
| `GET` | `/api/admin/logs` | 后台操作日志 |
