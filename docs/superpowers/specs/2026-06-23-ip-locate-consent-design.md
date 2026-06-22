# IP 定位授权确认 — 设计说明

**日期**: 2026-06-23
**状态**: 已确认
**范围**: 前端 `RegionSelector.vue` 单文件改造

## 背景

当前"使用当前位置"按钮点击后直接发起 IP 定位请求，缺少用户授权环节。需要在发起定位前增加授权确认弹窗。

## 改造目标

点击"使用当前位置" → 弹出授权确认弹窗 → 用户允许后才发起 IP 定位 → 回填地区下拉框，效果与手动选择一致。

## 改动范围

仅改 `frontend/src/components/RegionSelector.vue`，不改后端。

## 新流程

```
用户点击 [使用当前位置]
  → 弹出授权确认 Modal
    → 拒绝：关闭弹窗，什么都不做
    → 允许：关闭弹窗，执行 autoLocate()（现有逻辑不变）
      → 后端从 CF-Connecting-IP / X-Forwarded-For 提取 IP
      → ip-api.com 解析省市
      → 前端匹配下拉列表
      → auto-save
      → emit regionChanged
```

## 实现要点

1. 新增 `showConsent` ref 控制 Modal 显隐
2. 按钮 `@click` 改为 `showConsent = true`
3. Modal "允许"按钮回调执行 `showConsent = false` + `autoLocate()`
4. Modal "拒绝"按钮回调执行 `showConsent = false`
5. Modal 样式：居中 overlay + 半透明遮罩 + 双按钮

## 不涉及

- 后端 IP 提取逻辑不变
- API 接口不变
- 下拉框交互不变
- 自动保存逻辑不变
