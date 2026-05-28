# 性格雷达测试说明

## 后端测试

后端包含核心算法单元测试：

- `ScoringEngineTest`：验证 1-5 权重到 0-100 分数的归一化。
- `RecommendationRankerTest`：验证推荐基础分、标签偏好和画像分数的加权排序。
- `MatchEngineTest`：验证双人适配分计算和一位小数保留。
- `ProductizedApiTest`：验证报告历史、匹配历史、分享撤销、后台用户管理和推荐规则接口。

执行命令：

```powershell
$env:JAVA_HOME='C:\Program Files\Android\openjdk\jdk-21.0.8'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
& 'D:\allprogram\apache-maven-3.9.16\bin\mvn.cmd' test
```

## 前端测试与构建

执行命令：

```powershell
npm install
npm test
npm run build
```

`npm test` 使用 Vitest 和 jsdom。`npm run build` 会先运行 `vue-tsc -b`，再执行 Vite 生产构建。

## 手工验收流程

1. 使用 `13900000001 / User123456` 登录。
2. 完成基础性格测试。
3. 打开综合报告，确认雷达图、维度解释和生活建议展示正常。
4. 打开推荐页，切换餐饮、旅行、穿搭、生涯四个场景。
5. 对任意推荐提交“喜欢 / 一般 / 不喜欢”反馈，确认提示成功。
6. 在双人适配页输入 `13900000002`，生成适配报告。
7. 在报告页生成分享链接，打开 `/share/{token}` 查看只读报告。
8. 使用 `13800000000 / Admin@123456` 登录后台，确认统计、题库、推荐项、推荐规则、用户管理、反馈和日志可见。
9. 在后台停用普通用户后，确认该用户无法重新登录；再恢复用户状态。

## 当前环境注意事项

如果依赖安装失败，请检查当前 PowerShell 会话是否存在这些环境变量：

```powershell
Get-ChildItem Env:*proxy*
Get-ChildItem Env:*offline*
```

本环境曾出现 `HTTP_PROXY=http://127.0.0.1:9` 和 `NPM_CONFIG_OFFLINE=true`，会导致 npm/pnpm 无法正常安装依赖。可在当前会话临时清理：

```powershell
Remove-Item Env:NPM_CONFIG_OFFLINE -ErrorAction SilentlyContinue
Remove-Item Env:HTTP_PROXY -ErrorAction SilentlyContinue
Remove-Item Env:HTTPS_PROXY -ErrorAction SilentlyContinue
Remove-Item Env:ALL_PROXY -ErrorAction SilentlyContinue
```
