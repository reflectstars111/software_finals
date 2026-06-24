# ReflectStars 前端界面优化 Skill 与成熟方案参考

适用项目：`reflectstars111/software_finals`  
适用工具：Claude Code、Codex  
适用阶段：前端界面优化、UI/UX 审查、组件化重构、答辩展示前 polish

---

## 1. 目标

本文件用于整理 ReflectStars「性格雷达·生活指南」项目在优化前端界面时可以参考和使用的：

1. Claude Code / Codex 可用的 Agent Skills；
2. 前端 UI/UX 优化工作流；
3. Vue 3 成熟后台模板参考；
4. ReflectStars 项目专属 UI Skill；
5. 推荐的开发任务拆分与验收标准。

本项目当前技术栈为：

```text
Vue 3
Vite
TypeScript
Pinia
Vue Router
Axios
ECharts
Spring Boot
MySQL
Redis
```

项目核心体验是：

```text
注册登录
→ 完成性格与生活偏好测试
→ 生成 10 维雷达画像
→ 获取餐饮、旅行、社交、穿搭、生涯等生活推荐
→ 进行推荐反馈
→ 生成双人适配报告
→ 分享报告
→ 社区互动
→ 后台管理
```

因此前端优化不应该简单套后台模板，而应该采用：

```text
用户侧：产品化、星图感、雷达感、生活推荐感
后台侧：管理系统化、清晰、高效、表格友好
```

---

## 2. 已去除内容

根据当前开发工具选择，本文已经去掉 Cursor 专用方案，包括：

```text
cursor-designer
awesome-cursorrules
.cursor/rules/*.mdc
```

保留 Claude Code、Codex、通用 Agent Skills、Vue 成熟模板和 ReflectStars 自定义 skill。

---

## 3. 最推荐的整体路线

### 3.1 用户侧页面

适合页面：

```text
HomeView.vue
LoginView.vue
DashboardView.vue
TestView.vue
ReportView.vue
RecommendationsView.vue
MatchView.vue
ShareView.vue
CommunityView.vue
PostDetailView.vue
CreatePostView.vue
ProfileView.vue
```

推荐方向：

```text
ReflectStars Aurora UI
深蓝紫星空背景
雷达扫描感
玻璃态卡片
薄荷青主色
琥珀金推荐高亮
柔和光晕
圆角卡片
清晰的进度与反馈状态
移动端友好
```

### 3.2 后台页面

适合页面：

```text
AdminView.vue
```

推荐方向：

```text
参考成熟 Vue Admin 模板
左侧导航 + 顶部工具栏
统计卡片
表格管理
弹窗表单
筛选与搜索
操作日志
```

后台可以参考 Vben Admin、SoybeanAdmin、vue-pure-admin、Art Design Pro，但不建议整站迁移。

---

## 4. 推荐保留的 Agent Skills

### 4.1 Anthropic 官方 `frontend-design`

仓库/文档：

```text
https://github.com/anthropics/skills/blob/main/skills/frontend-design/SKILL.md
https://github.com/anthropics/claude-code/blob/main/plugins/frontend-design/skills/frontend-design/SKILL.md
https://claude.com/plugins/frontend-design
```

适用工具：

```text
Claude Code
```

作用：

```text
用于构建新 UI 或重塑现有 UI。
强调独特视觉方向、排版、配色、布局和动效。
避免 AI 生成的普通模板感。
```

适合 ReflectStars 的用途：

```text
首页产品化
登录页品牌化
测试页沉浸式问卷化
报告页星图雷达化
推荐页卡片流优化
双人适配页关系报告化
社区页 PersonaFeed 化
```

使用建议：

```text
在 Claude Code 中让它优先参考 frontend-design 的原则，
再结合 ReflectStars 自定义 UI Skill 执行具体 Vue 代码修改。
```

示例任务：

```text
使用 frontend-design 的思路，重塑 ReflectStars 的 ReportView.vue。
要求保留现有接口逻辑和 ECharts 雷达图，只优化页面结构、视觉层级、卡片布局和移动端体验。
```

---

### 4.2 Anthropic Skills 示例仓库

仓库：

```text
https://github.com/anthropics/skills
```

作用：

```text
学习官方 Agent Skills 的目录结构、SKILL.md 写法、参考文件组织方式。
```

适合 ReflectStars 的用途：

```text
学习如何写项目专属 skill；
学习如何拆分 instructions、references、scripts、assets；
参考文档类、设计类、开发类 skill 的结构。
```

Claude Code 中 project skill 推荐目录：

```text
.claude/skills/reflectstars-ui/SKILL.md
```

个人全局 skill 推荐目录：

```text
~/.claude/skills/reflectstars-ui/SKILL.md
```

---

### 4.3 Claude Code 内置 `/run`、`/verify`、`/run-skill-generator`

官方说明：

```text
https://code.claude.com/docs/en/skills
```

作用：

```text
/run
  启动并驱动应用，看修改是否真的工作。

/verify
  构建并运行应用，验证代码修改是否达到目标，不只依赖 typecheck 或单元测试。

/run-skill-generator
  让 Claude Code 学会你们项目的启动流程，并生成项目专属 run skill。
```

适合 ReflectStars 的用途：

```text
前端页面改完后，让 Claude Code 真实启动项目并查看页面；
验证登录、测试、报告、推荐、分享、后台等流程；
避免只通过 npm run build 但实际页面坏掉。
```

推荐执行：

```text
第一次在项目中运行：

/run-skill-generator

让 Claude Code 记录你们项目的启动方式，例如：

cd frontend
npm install
npm run dev

以及后端 / Docker / Caddy 是否需要启动。
```

后续修改页面后运行：

```text
/verify
```

---

### 4.4 Codex `AGENTS.md`

官方文档：

```text
https://developers.openai.com/codex/guides/agents-md
```

作用：

```text
给 Codex 提供仓库级项目说明、开发约束、测试命令、风格要求。
Codex 会在开始工作前读取 AGENTS.md。
```

建议放置位置：

```text
AGENTS.md
```

适合 ReflectStars 的用途：

```text
约束 Codex 不要乱改 API 字段；
不破坏现有 Vue Router、Pinia、Axios 结构；
前端 UI 优化优先保留业务逻辑；
改完后运行 npm run build 或 npm test；
用户侧保持星图雷达风格，后台侧参考 Admin 模板。
```

示例见本文第 8 节。

---

### 4.5 Codex Agent Skills

官方文档：

```text
https://developers.openai.com/codex/skills
```

作用：

```text
Codex 可通过 Agent Skills 获取任务专用能力。
Skill 是一个目录，包含 SKILL.md，也可以包含 scripts、references、assets 等。
```

Codex 仓库级 skill 推荐目录：

```text
.agents/skills/reflectstars-ui/SKILL.md
```

Codex user 级 skill 推荐目录：

```text
~/.agents/skills/reflectstars-ui/SKILL.md
```

适合 ReflectStars 的用途：

```text
把 ReflectStars UI 设计规则写成一个 Codex 可识别的 skill；
让 Codex 在优化前端页面时自动遵守项目视觉风格、技术栈和验收标准。
```

注意：

```text
Codex 的 SKILL.md frontmatter 需要包含 name 和 description。
为了让 Claude Code 和 Codex 都能使用，建议 SKILL.md 使用通用写法：

---
name: reflectstars-ui
description: ...
---
```

---

### 4.6 `ui-ux-pro-max-skill`

仓库：

```text
https://github.com/nextlevelbuilder/ui-ux-pro-max-skill
```

作用：

```text
提供较完整的 UI/UX 设计智能参考，包括风格、配色、字体、产品类型、UX 指南、图表类型等。
```

适合 ReflectStars 的用途：

```text
生成或完善设计系统；
确定颜色、字体、按钮、卡片、表单、标签、图表规范；
为报告页、推荐页、社区页建立统一 UI 规则。
```

建议用法：

```text
不要整套照搬；
把它当作设计系统参考；
最终仍以 ReflectStars Aurora UI 为准。
```

---

### 4.7 `Ilm-Alan/frontend-design`

仓库：

```text
https://github.com/Ilm-Alan/frontend-design
```

作用：

```text
一个支持 Claude Code、Codex、Gemini CLI 的前端设计 skill。
特点是提供 aesthetic anchors，即把配色、字体、纹理锁定成明确 token，避免风格漂移。
```

适合 ReflectStars 的用途：

```text
帮助你们固定一个视觉锚点：

ReflectStars Aurora UI
  deep navy
  violet
  mint cyan
  amber
  glassmorphism
  radar scanning
  star map texture
```

建议用法：

```text
可以参考它的 SKILL.md 结构；
也可以直接安装后，让 Claude Code / Codex 在设计阶段参考；
但最终仍建议写一份项目专属 reflectstars-ui skill。
```

---

### 4.8 Vercel `web-design-guidelines`

仓库：

```text
https://github.com/vercel-labs/agent-skills/blob/main/skills/web-design-guidelines/SKILL.md
```

作用：

```text
审查 UI 代码是否符合 Web Interface Guidelines。
覆盖 accessibility、performance、UX 等规则。
```

适合 ReflectStars 的用途：

```text
改完页面后做 UI 审计；
检查按钮、表单、焦点、可访问性、移动端布局；
避免只追求视觉效果而忽略可用性。
```

推荐使用阶段：

```text
第一轮设计完成后；
每个重点页面改完后；
答辩展示前。
```

适合审查文件：

```text
frontend/src/views/HomeView.vue
frontend/src/views/TestView.vue
frontend/src/views/ReportView.vue
frontend/src/views/RecommendationsView.vue
frontend/src/styles.css
```

---

## 5. Vue 成熟方案参考

### 5.1 Vue Vben Admin

仓库：

```text
https://github.com/vbenjs/vue-vben-admin
```

适合参考：

```text
/admin 后台布局
权限菜单
表格页
弹窗表单
统计卡片
后台页面组织
```

不建议：

```text
不要把用户侧首页、报告页、推荐页套成 Vben 后台风格。
```

---

### 5.2 SoybeanAdmin

仓库：

```text
https://github.com/soybeanjs/soybean-admin
https://docs.soybeanjs.cn/
```

适合参考：

```text
现代后台视觉
主题配置
暗色/亮色风格
仪表盘卡片
页面过渡
菜单结构
```

适合你们：

```text
如果想让 AdminView.vue 更现代、更好看，优先看 SoybeanAdmin。
```

---

### 5.3 vue-pure-admin

仓库：

```text
https://github.com/pure-admin/vue-pure-admin
```

适合参考：

```text
Element Plus 表格
后台表单
权限管理
中后台页面结构
```

适合你们：

```text
如果后台功能多、表格多，可以参考它的结构和交互。
```

---

### 5.4 Art Design Pro

仓库：

```text
https://github.com/Daymychen/art-design-pro
https://www.artd.pro/docs/
```

适合参考：

```text
视觉更强的 Vue 3 后台
Vite + TypeScript + Element Plus
用户体验与视觉设计
仪表盘和管理页面质感
```

适合你们：

```text
如果希望后台不是普通模板，而是更适合展示给老师/用户，可以重点参考 Art Design Pro。
```

---

### 5.5 V3 Admin Vite

仓库：

```text
https://github.com/un-pany/v3-admin-vite
```

适合参考：

```text
Vue3 + Vite + TypeScript + Element Plus 后台基础骨架
```

适合你们：

```text
作为简单、稳定的后台结构参考。
```

---

## 6. 推荐项目目录改造

建议在仓库中新增这些文件：

```text
software_finals/
  AGENTS.md

  .claude/
    skills/
      reflectstars-ui/
        SKILL.md

  .agents/
    skills/
      reflectstars-ui/
        SKILL.md

  docs/
    frontend-ui-optimization-plan.md
```

说明：

```text
AGENTS.md
  给 Codex 使用的仓库级工作约束。

.claude/skills/reflectstars-ui/SKILL.md
  给 Claude Code 使用的项目专属 UI Skill。

.agents/skills/reflectstars-ui/SKILL.md
  给 Codex 使用的项目专属 UI Skill。

docs/frontend-ui-optimization-plan.md
  记录前端优化计划、页面优先级、验收标准。
```

---

## 7. ReflectStars UI Skill 推荐内容

下面这份内容可以同时放入：

```text
.claude/skills/reflectstars-ui/SKILL.md
.agents/skills/reflectstars-ui/SKILL.md
```

```markdown
---
name: reflectstars-ui
description: Use when improving the ReflectStars Vue 3 frontend UI, including landing pages, personality test flow, radar report, recommendations, matching, sharing, community, dashboard, and admin pages. Preserve existing business logic and API contracts while upgrading visual design, UX, responsiveness, and component structure.
---

# ReflectStars Frontend UI Skill

## Project context

ReflectStars is a “Personality Radar · Lifestyle Guide” web application.

The user journey is:

1. Register or log in.
2. Complete personality and lifestyle preference tests.
3. Generate a 10-dimensional radar profile.
4. Receive personalized recommendations for food, travel, social activities, outfits, and career.
5. Give feedback on recommendations.
6. Generate two-person compatibility reports.
7. Share reports.
8. Interact in a community feed.
9. Admins manage users, questions, recommendations, rules, feedback, and logs.

## Tech stack

Frontend:

- Vue 3
- Vite
- TypeScript
- Pinia
- Vue Router
- Axios
- ECharts

Backend:

- Spring Boot
- MySQL
- Redis
- JWT auth

## Core design direction

Use “ReflectStars Aurora UI”.

Visual identity:

- Deep navy and violet background.
- Star map atmosphere.
- Radar scanning motif.
- Glassmorphism cards.
- Mint cyan as primary action color.
- Amber gold as recommendation highlight color.
- Soft glow, subtle gradients, rounded cards.
- Clear visual hierarchy.
- Smooth but restrained motion.

Avoid:

- Generic admin template style on user-facing pages.
- Overuse of random gradients.
- Excessive animation.
- Changing API field names.
- Breaking existing router, Pinia, Axios, or ECharts logic.
- Replacing the entire stack unless explicitly asked.

## Page rules

### Home page

The home page should feel like a product landing page, not a feature list.

Must include:

- Strong hero section.
- Clear product value.
- CTA to start testing.
- Three-step flow: test → radar profile → recommendation.
- 10-dimensional profile explanation.
- Recommendation scenes.
- Two-person matching entry.

### Login page

The login page should be brand-oriented.

Prefer:

- Two-column layout on desktop.
- Radar/star visual on the left.
- Login/register form on the right.
- Clear test account/admin entry hint if available.
- Mobile-friendly stacked layout.

### Dashboard page

The dashboard should guide the user to the next action.

Must show:

- Greeting.
- Profile/test completion status.
- Recent report status.
- Recommendation entry.
- Matching entry.
- Community entry.
- Clear next-step cards.

### Test page

The test page should feel like an immersive questionnaire.

Must include:

- Module navigation.
- Current question card.
- Large selectable options.
- Clear selected state.
- Progress bar.
- Previous/next/submit actions.
- Mobile-friendly layout.

### Report page

The report page is the most important display page.

Must emphasize:

- Personality title.
- One-sentence summary.
- Large ECharts radar chart.
- 10 dimension cards.
- Lifestyle interpretation.
- Recommendation CTA.
- Share CTA.
- Good mobile layout.

### Recommendations page

The recommendation page should feel like an AI personalized recommendation feed.

Recommendation cards should show:

- Scene/category label.
- Match score.
- Title.
- Description.
- AI reason if available.
- Source badge if AI-generated.
- Tags.
- Feedback buttons: like / neutral / dislike.
- Clear submitted feedback state.

### Match page

The match page should feel like a relationship compatibility report.

Prefer:

- Two profile cards.
- Compatibility score in the center.
- Similarity dimensions.
- Difference dimensions.
- Relationship suggestions.
- Shared activity suggestions.

### Community page

The community page should feel like PersonaFeed.

Prefer:

- Feed cards.
- Author info.
- Post title and summary.
- Tags.
- Like/comment/favorite indicators.
- Empty state with clear action.

### Admin page

The admin page can follow mature Vue admin patterns.

Prefer:

- Sidebar navigation.
- Top operation area.
- Metric cards.
- Tables.
- Filter/search.
- Modal or drawer forms.
- Clear loading/empty/error states.

## Component rules

When UI repeats, extract components.

Recommended components:

- AppButton.vue
- AppCard.vue
- AppBadge.vue
- SectionHeader.vue
- ScorePill.vue
- RadarPanel.vue
- QuestionCard.vue
- TestProgress.vue
- DimensionCard.vue
- RecommendationCard.vue
- FeedbackButtons.vue
- AdminMetricCard.vue
- AdminTableShell.vue

## CSS rules

Prefer using the existing CSS design system.

If editing `frontend/src/styles.css`:

- Keep variables centralized.
- Avoid one-off colors in page files.
- Keep mobile breakpoints.
- Respect reduced motion where possible.
- Ensure focus states are visible.
- Avoid class name conflicts.

## Implementation constraints

Before editing:

1. Read the target Vue file.
2. Identify existing API calls and state.
3. Preserve business logic.
4. Preserve router paths.
5. Preserve emitted events and props.
6. Preserve existing TypeScript types.
7. Only refactor UI and component structure unless explicitly asked.

After editing:

1. Run frontend build.
2. Check TypeScript errors.
3. Check page visually if browser tools are available.
4. Check mobile layout.
5. Summarize what changed and what files were touched.
```

---

## 8. Codex AGENTS.md 推荐内容

建议放入仓库根目录：

```text
AGENTS.md
```

内容如下：

```markdown
# AGENTS.md

## Repository context

This repository is ReflectStars, a “Personality Radar · Lifestyle Guide” web application.

The main user flow is:

1. Register or log in.
2. Complete personality and lifestyle tests.
3. Generate a 10-dimensional radar profile.
4. Receive personalized lifestyle recommendations.
5. Give recommendation feedback.
6. Generate compatibility reports.
7. Share reports.
8. Use community features.
9. Admins manage users, questions, recommendations, rules, feedback, and logs.

## Tech stack

Frontend:

- Vue 3
- Vite
- TypeScript
- Pinia
- Vue Router
- Axios
- ECharts

Backend:

- Spring Boot
- MySQL
- Redis
- JWT

## Frontend work rules

When modifying frontend UI:

1. Preserve existing API contracts.
2. Do not rename backend response fields unless explicitly asked.
3. Do not break Vue Router paths.
4. Do not remove existing auth guards.
5. Do not replace Pinia state logic unless necessary.
6. Do not rewrite Axios API modules unless necessary.
7. Prefer component extraction for repeated UI.
8. Preserve mobile responsiveness.
9. Keep visible focus states and accessible labels.
10. Run build/test commands when possible.

## Visual direction

Use ReflectStars Aurora UI for user-facing pages:

- Deep navy/violet background.
- Star map atmosphere.
- Radar scanning motif.
- Glassmorphism cards.
- Mint cyan primary actions.
- Amber gold recommendation highlights.
- Rounded cards.
- Soft glow.
- Restrained motion.

User-facing pages should not look like generic admin templates.

Admin pages can reference mature Vue admin templates:

- Vue Vben Admin
- SoybeanAdmin
- vue-pure-admin
- Art Design Pro
- V3 Admin Vite

## Recommended priority

Frontend optimization priority:

1. `frontend/src/styles.css`
2. `frontend/src/components/common/AppNav.vue`
3. `frontend/src/views/HomeView.vue`
4. `frontend/src/views/LoginView.vue`
5. `frontend/src/views/TestView.vue`
6. `frontend/src/views/ReportView.vue`
7. `frontend/src/views/RecommendationsView.vue`
8. `frontend/src/views/MatchView.vue`
9. `frontend/src/views/CommunityView.vue`
10. `frontend/src/views/AdminView.vue`

## Validation commands

When editing frontend files, prefer:

```bash
cd frontend
npm install
npm run build
npm test
```

If test setup is incomplete, at least run:

```bash
cd frontend
npm run build
```

## Output expectation

After each task, summarize:

1. Files changed.
2. UI/UX improvements.
3. Logic preserved.
4. Build/test result.
5. Any remaining risks.
```

---

## 9. 前端优化任务拆分

### 任务 1：建立 UI 基础规范

目标文件：

```text
frontend/src/styles.css
```

目标：

```text
统一颜色变量
统一背景
统一卡片
统一按钮
统一标签
统一表单
统一空状态
统一加载状态
统一移动端断点
```

验收：

```text
页面没有明显风格割裂；
按钮、卡片、标签看起来属于同一系统；
移动端不横向溢出。
```

---

### 任务 2：优化导航

目标文件：

```text
frontend/src/components/common/AppNav.vue
```

目标：

```text
玻璃态顶部导航
品牌 Logo/文字更突出
当前路由状态清晰
登录/退出/后台入口清晰
移动端可用
```

验收：

```text
导航不遮挡内容；
移动端不挤压；
当前页面高亮明显。
```

---

### 任务 3：首页产品化

目标文件：

```text
frontend/src/views/HomeView.vue
```

目标：

```text
从普通功能介绍改成产品落地页。
```

结构：

```text
Hero
三步流程
10 维画像
推荐场景
双人适配
社区入口
CTA
```

验收：

```text
用户 5 秒内能看懂项目价值；
首页能体现“性格雷达·生活指南”。
```

---

### 任务 4：登录页品牌化

目标文件：

```text
frontend/src/views/LoginView.vue
```

目标：

```text
桌面端左右分栏；
左侧品牌视觉；
右侧登录/注册表单；
移动端上下布局。
```

验收：

```text
登录注册逻辑不变；
表单校验仍可用；
视觉不再像普通表单页。
```

---

### 任务 5：测试页沉浸式问卷

目标文件：

```text
frontend/src/views/TestView.vue
```

目标：

```text
一题一卡片；
模块导航；
进度条；
大选项按钮；
清晰选中状态。
```

可新增组件：

```text
frontend/src/components/test/QuestionCard.vue
frontend/src/components/test/TestProgress.vue
frontend/src/components/test/ModuleStepper.vue
```

验收：

```text
答题逻辑不变；
提交逻辑不变；
用户清楚知道当前进度；
移动端可答题。
```

---

### 任务 6：报告页星图雷达化

目标文件：

```text
frontend/src/views/ReportView.vue
```

目标：

```text
强化人格标题；
强化 ECharts 雷达图；
增加 10 维度卡片；
突出分享和推荐 CTA。
```

可新增组件：

```text
frontend/src/components/report/ReportHero.vue
frontend/src/components/report/RadarSummaryCard.vue
frontend/src/components/report/DimensionCard.vue
```

验收：

```text
雷达图正常渲染；
维度解释清晰；
分享按钮可用；
推荐入口明确。
```

---

### 任务 7：推荐页卡片流优化

目标文件：

```text
frontend/src/views/RecommendationsView.vue
```

目标：

```text
推荐项变成卡片流；
AI 推荐标识明显；
适配度突出；
反馈按钮清晰；
地区提醒清晰。
```

可新增组件：

```text
frontend/src/components/recommendation/RecommendationCard.vue
frontend/src/components/recommendation/FeedbackButtons.vue
frontend/src/components/recommendation/SceneTabs.vue
```

验收：

```text
推荐列表加载正常；
场景切换正常；
反馈提交正常；
反馈后状态明显。
```

---

### 任务 8：双人适配页关系报告化

目标文件：

```text
frontend/src/views/MatchView.vue
```

目标：

```text
输入手机号/邀请码流程清晰；
生成报告后显示两人画像；
总契合度明显；
相似点和差异点清晰。
```

验收：

```text
匹配逻辑不变；
结果结构更清晰；
适合答辩展示。
```

---

### 任务 9：社区 PersonaFeed 化

目标文件：

```text
frontend/src/views/CommunityView.vue
frontend/src/views/PostDetailView.vue
frontend/src/views/CreatePostView.vue
```

目标：

```text
社区首页像内容流；
帖子卡片清晰；
详情页层级清晰；
发帖页表单体验更好。
```

验收：

```text
帖子列表、点赞、收藏、评论、发帖逻辑不变；
空状态和加载状态清晰。
```

---

### 任务 10：后台 AdminView 模板化优化

目标文件：

```text
frontend/src/views/AdminView.vue
```

参考：

```text
Vue Vben Admin
SoybeanAdmin
vue-pure-admin
Art Design Pro
V3 Admin Vite
```

目标：

```text
左侧菜单；
顶部区域；
统计卡片；
表格管理；
筛选搜索；
表单弹窗。
```

验收：

```text
后台功能不减少；
用户、题库、推荐项、规则、反馈、日志模块清晰；
操作按钮位置统一。
```

---

## 10. Claude Code 推荐提示词

### 10.1 启动项目前

```text
请先阅读本仓库的 AGENTS.md 和 .claude/skills/reflectstars-ui/SKILL.md。
本次任务只优化前端 UI，不改变后端接口、不改数据库、不重命名 API 字段。
```

### 10.2 优化首页

```text
使用 reflectstars-ui skill，优化 frontend/src/views/HomeView.vue。

要求：
1. 保留现有路由和按钮跳转逻辑；
2. 改成产品落地页结构；
3. 强化“性格雷达·生活指南”的品牌感；
4. 使用深蓝紫星空、雷达扫描、玻璃态卡片风格；
5. 移动端可用；
6. 如需要公共样式，优先修改 frontend/src/styles.css；
7. 完成后运行 cd frontend && npm run build。
```

### 10.3 优化测试页

```text
使用 reflectstars-ui skill，优化 frontend/src/views/TestView.vue。

目标：
1. 把测试页改成沉浸式问卷；
2. 保留现有题目加载、选项选择、进度计算和提交逻辑；
3. 增加模块导航、进度条、一题一卡片、大选项按钮；
4. 清晰区分选中状态；
5. 移动端可答题；
6. 必要时抽出 QuestionCard、TestProgress、ModuleStepper 组件；
7. 完成后运行前端 build。
```

### 10.4 优化报告页

```text
使用 reflectstars-ui skill，优化 frontend/src/views/ReportView.vue。

目标：
1. 保留现有 ECharts 雷达图和报告数据逻辑；
2. 强化人格标题、一句话总结、10 维度卡片；
3. 增加星图雷达视觉；
4. 突出“获取推荐”和“分享报告”按钮；
5. 移动端适配；
6. 完成后运行前端 build。
```

### 10.5 优化推荐页

```text
使用 reflectstars-ui skill，优化 frontend/src/views/RecommendationsView.vue。

目标：
1. 保留推荐加载、场景切换、地区选择、反馈提交逻辑；
2. 把推荐项改成高质量推荐卡片；
3. 显示场景标签、适配度、AI 推荐理由、标签、反馈按钮；
4. AI 推荐项增加视觉标识；
5. 反馈后按钮状态要明显；
6. 移动端适配；
7. 完成后运行前端 build。
```

---

## 11. Codex 推荐提示词

### 11.1 通用

```text
Read AGENTS.md first. Use the reflectstars-ui skill if available.
Only improve the frontend UI. Preserve existing API contracts, router paths, Pinia stores, Axios services, and TypeScript types.
After changes, run cd frontend && npm run build and summarize changed files, UI improvements, preserved logic, and any risks.
```

### 11.2 单页优化

```text
Use the reflectstars-ui skill to improve frontend/src/views/ReportView.vue.

Keep the existing report loading logic, ECharts radar chart logic, share link logic, and recommendation navigation.
Improve layout, visual hierarchy, cards, CTA placement, responsiveness, and ReflectStars Aurora UI styling.
Do not rename API fields or route paths.
Run cd frontend && npm run build after editing.
```

### 11.3 审查任务

```text
Audit the frontend UI implementation against the ReflectStars Aurora UI direction and web design best practices.

Focus on:
1. visual consistency
2. accessibility
3. responsive layout
4. empty/loading/error states
5. button and form clarity
6. repeated UI that should become components
7. risk of broken API logic

Output file-specific findings and recommended fixes.
```

---

## 12. UI 验收清单

每个页面改完后，用下面清单检查。

### 12.1 通用

```text
页面是否有明确标题？
主操作按钮是否明显？
卡片间距是否统一？
按钮样式是否统一？
标签样式是否统一？
加载状态是否清楚？
空状态是否告诉用户下一步？
错误状态是否有可执行建议？
移动端是否可用？
是否没有横向滚动？
键盘焦点是否可见？
```

### 12.2 测试页

```text
是否知道当前模块？
是否知道当前题号？
是否知道完成进度？
选项是否容易点击？
选中状态是否明显？
上一题/下一题/提交是否清楚？
```

### 12.3 报告页

```text
雷达图是否第一眼可见？
人格标题是否突出？
10 维度解释是否清楚？
CTA 是否明确？
分享功能入口是否明显？
```

### 12.4 推荐页

```text
适配度是否明显？
推荐理由是否清楚？
AI 推荐是否有标识？
反馈按钮是否清楚？
反馈后状态是否明显？
地区提醒是否清楚？
```

### 12.5 后台页

```text
模块入口是否清晰？
统计卡片是否易读？
表格操作是否统一？
搜索筛选是否明确？
新增/编辑/删除入口是否清楚？
危险操作是否有确认？
```

---

## 13. 不建议做的事情

```text
不要全站迁移到 Element Plus。
不要把用户侧页面做成后台模板。
不要一次性引入 Tailwind / UnoCSS 并大规模重写。
不要重写 API 层。
不要重命名后端字段。
不要为了 UI 改坏登录鉴权。
不要为了动效牺牲移动端性能。
不要每个页面单独写一套颜色和按钮。
```

---

## 14. 推荐依赖

短期只建议新增：

```bash
cd frontend
npm install lucide-vue-next
```

用途：

```text
统一图标风格；
替代零散 emoji 或不一致图标；
用于导航、功能卡片、推荐标签、后台菜单。
```

如果后台需要更快开发表格和弹窗，可以考虑：

```bash
cd frontend
npm install element-plus
```

但建议只用于 AdminView 或后台组件，不要污染用户侧整体风格。

---

## 15. 推荐 PR 顺序

```text
PR 1：添加 AGENTS.md 与 reflectstars-ui skill
PR 2：统一 styles.css 与基础组件
PR 3：首页 + 登录页优化
PR 4：测试页优化
PR 5：报告页优化
PR 6：推荐页优化
PR 7：匹配页 + 分享页优化
PR 8：社区页优化
PR 9：后台页优化
PR 10：UI 审计、移动端修复、答辩展示 polish
```

---

## 16. 参考链接

### Claude Code / Agent Skills

```text
Claude Code Skills 文档
https://code.claude.com/docs/en/skills

Anthropic Skills 示例仓库
https://github.com/anthropics/skills

Anthropic frontend-design skill
https://github.com/anthropics/skills/blob/main/skills/frontend-design/SKILL.md

Claude Code frontend-design plugin
https://github.com/anthropics/claude-code/blob/main/plugins/frontend-design/skills/frontend-design/SKILL.md

Claude frontend-design plugin 页面
https://claude.com/plugins/frontend-design
```

### Codex

```text
Codex AGENTS.md 文档
https://developers.openai.com/codex/guides/agents-md

Codex Agent Skills 文档
https://developers.openai.com/codex/skills
```

### UI/UX Skills

```text
UI UX Pro Max Skill
https://github.com/nextlevelbuilder/ui-ux-pro-max-skill

Ilm-Alan frontend-design
https://github.com/Ilm-Alan/frontend-design

Vercel web-design-guidelines
https://github.com/vercel-labs/agent-skills/blob/main/skills/web-design-guidelines/SKILL.md
```

### Vue Admin 参考

```text
Vue Vben Admin
https://github.com/vbenjs/vue-vben-admin

SoybeanAdmin
https://github.com/soybeanjs/soybean-admin
https://docs.soybeanjs.cn/

vue-pure-admin
https://github.com/pure-admin/vue-pure-admin

Art Design Pro
https://github.com/Daymychen/art-design-pro
https://www.artd.pro/docs/

V3 Admin Vite
https://github.com/un-pany/v3-admin-vite
```

---

## 17. 最终建议

你们项目最合适的路线是：

```text
Claude Code:
  使用 frontend-design + reflectstars-ui skill
  使用 /run /verify 验证真实页面

Codex:
  使用 AGENTS.md 约束仓库规范
  使用 .agents/skills/reflectstars-ui 作为项目专属 UI Skill

设计参考:
  UI UX Pro Max
  Ilm-Alan/frontend-design
  Vercel web-design-guidelines

后台参考:
  Vben Admin
  SoybeanAdmin
  vue-pure-admin
  Art Design Pro
  V3 Admin Vite
```

核心原则：

```text
用户侧做成“星图雷达产品”。
后台侧做成“成熟管理系统”。
不要推倒重写。
不要全站套模板。
先优化最能展示项目价值的页面：Home、Test、Report、Recommendations。
```
