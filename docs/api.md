# 性格雷达接口说明

统一响应结构：

```json
{ "code": 0, "message": "success", "data": {} }
```

除注册、登录、分享读取和健康检查外，其余接口需要请求头：

```http
Authorization: Bearer <JWT>
```

## 10 维度模型

系统基于 10 个维度生成画像、推荐和匹配结果：

| 维度键 | 中文名 | 分类 |
|--------|--------|------|
| `OPENNESS` | 开放性 | 人格 |
| `CONSCIENTIOUSNESS` | 尽责性 | 人格 |
| `EXTRAVERSION` | 外向性 | 人格 |
| `AGREEABLENESS` | 宜人性 | 人格 |
| `NEUROTICISM` | 情绪敏感度 | 人格（报告中反转为情绪稳定性） |
| `FOOD_ADVENTURE` | 饮食探索 | 生活方式 |
| `FOOD_SOCIAL` | 饮食社交 | 生活方式 |
| `TRAVEL_ADVENTURE` | 旅行探索 | 生活方式 |
| `TRAVEL_PLANNING` | 旅行计划 | 生活方式 |
| `SOCIAL_ENERGY` | 社交能量 | 生活方式 |

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
| `GET` | `/api/questions?type=personality` | 基础性格题目 |
| `GET` | `/api/questions?type=food` | 饮食偏好题目 |
| `GET` | `/api/questions?type=travel` | 旅游偏好题目 |
| `GET` | `/api/questions?type=social` | 社交倾向题目 |
| `POST` | `/api/tests/submit` | 提交答案并生成测试结果 |
| `GET` | `/api/tests/history` | 历史测试记录 |
| `GET` | `/api/reports/me` | 最新综合画像报告（聚合全部4类测试） |
| `GET` | `/api/reports/history` | 报告快照历史 |
| `GET` | `/api/reports/{id}` | 指定报告快照 |

测试提交示例：

```json
{
  "type": "personality",
  "answers": [
    { "questionId": 1, "optionIds": [1] }
  ]
}
```

## 推荐与反馈

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/recommendations?scene=food` | 餐饮推荐 |
| `GET` | `/api/recommendations?scene=travel` | 旅行推荐 |
| `GET` | `/api/recommendations?scene=social` | 社交推荐 |
| `GET` | `/api/recommendations?scene=outfit` | 穿搭推荐 |
| `GET` | `/api/recommendations?scene=career` | 生涯推荐 |
| `POST` | `/api/recommendations/{id}/feedback` | 提交推荐反馈 |
| `GET` | `/api/recommendations/feedback/me` | 当前用户反馈历史 |

反馈提交示例：

```json
{ "rating": "LIKE", "comment": "这个推荐很适合我" }
```

## 双人匹配

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/matches` | 手机号匹配 |
| `POST` | `/api/matches/invite` | 生成匹配邀请码（90天有效） |
| `GET` | `/api/matches/invites` | 当前用户邀请码列表 |
| `POST` | `/api/matches/by-invite` | 通过邀请码创建匹配 |
| `GET` | `/api/matches` | 匹配报告历史 |
| `GET` | `/api/matches/{id}` | 指定匹配报告 |

手机号匹配示例：

```json
{ "friendPhone": "13900000002" }
```

邀请码匹配示例：

```json
{ "inviteCode": "ME7A0AE1" }
```

## 分享

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/shares/report` | 生成报告分享链接 |
| `GET` | `/api/shares` | 当前用户分享链接 |
| `DELETE` | `/api/shares/{id}` | 撤销分享链接 |
| `GET` | `/api/shares/{token}` | 公开只读分享报告 |

## 管理后台

需要 `ADMIN` 角色。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/admin/stats` | 基础统计 |
| `GET` | `/api/admin/dashboard` | 运营统计面板 |
| `GET` | `/api/admin/users` | 用户列表 |
| `PUT` | `/api/admin/users/{id}` | 停用/恢复/调整角色 |
| `GET` | `/api/admin/questions` | 题库列表 |
| `POST` | `/api/admin/questions` | 新增题目 |
| `PUT` | `/api/admin/questions/{id}` | 修改题目 |
| `DELETE` | `/api/admin/questions/{id}` | 软删除题目 |
| `GET` | `/api/admin/recommendation-items` | 推荐项列表 |
| `POST` | `/api/admin/recommendation-items` | 新增推荐项 |
| `PUT` | `/api/admin/recommendation-items/{id}` | 修改推荐项 |
| `DELETE` | `/api/admin/recommendation-items/{id}` | 软删除推荐项 |
| `GET` | `/api/admin/recommendation-rules` | 推荐规则列表 |
| `POST` | `/api/admin/recommendation-rules` | 新增规则 |
| `PUT` | `/api/admin/recommendation-rules/{id}` | 修改规则 |
| `DELETE` | `/api/admin/recommendation-rules/{id}` | 删除规则 |
| `GET` | `/api/admin/feedback` | 最近反馈 |
| `GET` | `/api/admin/logs` | 操作日志 |
