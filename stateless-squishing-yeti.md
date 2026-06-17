# UI 深度美化：安装顶级前端 Skill + 优化项目

## 已安装
- `garden-skills` / `web-design-engineer` (ConardLi, 6.2K⭐) — 25 种风格配方
- `frontend-design` (Anthropic 官方) — 设计方向锚定

## 本轮安装

| Skill | 安装命令 | 用途 |
|-------|---------|------|
| **ux-skill** | `pip install uxskill` | 152 条反 AI 味 lint 规则，确定性校验引擎 |

> UI/UX Pro Max (83K⭐) 和 Impeccable 需要交互式 CLI plugin marketplace，当前环境不支持。ux-skill 通过 pip 安装最可靠，且规则最全面。

## 实施步骤

### Step 1：安装 ux-skill
```bash
pip install uxskill
```

### Step 2：用 ux-skill 审查 styles.css
对 frontend/src/styles.css 运行 anti-AI-slop lint，检查：
- Inter 字体残留（已替换为 Source Sans 3 + DM Serif Display）
- 紫色渐变、嵌套卡片等 AI 通用模式
- 圆角过大（>12px）、阴影过重
- 对比度不达标

### Step 3：修正问题

### Step 4：验证
- `vue-tsc --noEmit` + `vite build` 通过
- `vitest run` 12 tests 通过
