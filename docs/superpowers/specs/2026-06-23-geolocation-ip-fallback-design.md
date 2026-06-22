# 地理位置优先 + IP 兜底 — 设计说明

**日期**: 2026-06-23
**状态**: 已确认
**范围**: 前端 `RegionSelector.vue` 单文件改造

## 背景

当前"使用当前位置"直接走 IP 定位，缺少用户授权且精度有限。改造为：优先使用浏览器 Geolocation API（高精度 + 原生权限弹窗），用户拒绝后以 IP 定位兜底（需自定义确认弹窗）。

## 流程

```
点击 [使用当前位置]
  → navigator.geolocation.getCurrentPosition()
    → 允许：lat/lng → 后端 reverseGeocode → 匹配下拉框 → auto-save → emit → 推荐刷新
    → 拒绝/超时/出错：弹出 IP 授权确认弹窗
      → 允许：后端 ipLocate → 匹配下拉框 → auto-save → emit → 推荐刷新
      → 拒绝：什么都不做
```

## 改动范围

仅改 `frontend/src/components/RegionSelector.vue`，后端不改。

## 实现要点

1. 重写 `autoLocate()`：
   - `navigator.geolocation` 是否存在检测（非 HTTPS / 不支持时直接走 IP 兜底）
   - `getCurrentPosition()` 参数：`enableHighAccuracy: false`, `timeout: 10000`, `maximumAge: 300000`
   - 成功回调：调 `regionApi.reverseGeocode(lat, lng)`，匹配省市，`save()`
   - 失败回调：`showConsent = true`（展示 IP 兜底弹窗）
2. 新增 `locateByIp()` 函数：从 `autoLocate` 中提取纯 IP 定位逻辑
3. 弹窗文案调整为 GPS 被拒语境："无法获取精确定位，是否使用 IP 地址获取大致位置？"
4. 按钮在 GPS 等待、反查、IP 定位期间均显示 "定位中..." 并 disabled

## 边界处理

- 浏览器不支持 Geolocation API → 直接走 IP 兜底弹窗
- 非 HTTPS 环境（本地开发）→ `navigator.geolocation` 可能不存在
- GPS 超时 → 视为拒绝，走 IP 兜底
- 反查返回 null → 走 IP 兜底
- IP 定位也失败 → 显示 "无法获取位置，请手动选择"
