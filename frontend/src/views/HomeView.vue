<script setup lang="ts">
import { UtensilsCrossed, MapPin, Users, HeartHandshake, MessageSquare } from 'lucide-vue-next'
import HeroRadarScene from '../components/common/HeroRadarScene.vue'
import { DIMENSION_LABELS } from '../utils/dimensions'
import type { DimensionKey } from '../utils/dimensions'

const capabilities = [
  {
    title: '多维度性格画像',
    desc: '通过五大性格维度和生活偏好题目，生成能被解释的个人画像。'
  },
  {
    title: '个性化生活推荐',
    desc: '把画像转换为餐饮、旅行和社交建议，并说明为什么适合你。'
  },
  {
    title: '双人关系适配',
    desc: '通过邀请码授权比较双方维度差异，只展示建议，不暴露完整答题。'
  }
]

const dimensionKeys: DimensionKey[] = [
  'OPENNESS', 'CONSCIENTIOUSNESS', 'EXTRAVERSION', 'AGREEABLENESS', 'NEUROTICISM',
  'FOOD_ADVENTURE', 'FOOD_SOCIAL', 'TRAVEL_ADVENTURE', 'TRAVEL_PLANNING', 'SOCIAL_ENERGY'
]

const sceneCards = [
  { title: '饮食推荐', desc: '根据口味偏好与饮食探索精神，推荐适合的餐厅与美食体验。', to: '/recommendations', icon: UtensilsCrossed },
  { title: '旅行推荐', desc: '结合旅行风格与计划偏好，匹配目的地、路线与出行方式。', to: '/recommendations', icon: MapPin },
  { title: '社交推荐', desc: '基于社交能量与性格倾向，推荐活动类型与社交场景。', to: '/recommendations', icon: Users }
]
</script>

<template>
  <section class="home-page">
    <div class="hero-band">
      <HeroRadarScene />
      <div class="hero-copy">
        <p class="eyebrow">性格雷达·生活指南</p>
        <h1>用10维画像，照亮你的生活选择。</h1>
        <p>
          完成轻量测评，获得星图雷达般的多维生活画像，再把画像用于个性化推荐和双人适配分析。
        </p>
        <div class="toolbar">
          <RouterLink class="button" to="/tests/personality">开始测试</RouterLink>
          <RouterLink class="button secondary" to="/report?demo=true">查看示例报告</RouterLink>
        </div>
      </div>
      <div class="hero-flow" aria-label="产品流程">
        <div class="flow-step">测试</div>
        <div class="flow-line"></div>
        <div class="flow-step">报告</div>
        <div class="flow-line"></div>
        <div class="flow-step">推荐</div>
        <div class="flow-line"></div>
        <div class="flow-step">匹配</div>
      </div>
    </div>

    <section class="grid three">
      <article v-for="item in capabilities" :key="item.title" class="card feature-card">
        <h2>{{ item.title }}</h2>
        <p>{{ item.desc }}</p>
      </article>
    </section>

    <section class="panel product-note">
      <h2>完整使用路径</h2>
      <p>
        进入网站后，你可以先了解产品价值，再完成性格、饮食、旅游和社交倾向测评；系统会生成综合画像报告，
        并基于报告给出餐饮、旅游和社交推荐。推荐反馈会影响后续排序，双人适配则需要邀请码授权。
      </p>
    </section>

    <!-- 10-Dimension Explanation -->
    <section class="panel section-gap">
      <h2>10维生活画像</h2>
      <p class="muted">你的画像由五大性格维度与五大生活偏好维度共同构成，每个维度都在推荐算法中扮演不同角色。</p>
      <div class="grid two section-gap">
        <div v-for="key in dimensionKeys" :key="key" class="card">
          <h3>{{ DIMENSION_LABELS[key] }}</h3>
          <div class="mini-meter"><span class="dimension-demo-fill"></span></div>
          <p class="muted">该维度影响对应场景的推荐权重与排序优先级。</p>
        </div>
      </div>
    </section>

    <!-- Scene Entry Cards -->
    <section class="section-gap">
      <h2 class="scene-section-title">探索推荐场景</h2>
      <div class="grid three">
        <RouterLink v-for="scene in sceneCards" :key="scene.title" :to="scene.to" class="card scene-card">
          <component :is="scene.icon" :size="28" class="metric-icon" />
          <h2>{{ scene.title }}</h2>
          <p class="muted">{{ scene.desc }}</p>
        </RouterLink>
      </div>
    </section>

    <!-- Match Entry -->
    <section class="panel section-gap">
      <div class="split">
        <div>
          <h2><HeartHandshake :size="22" class="icon-md match-heading-icon" />双人关系适配</h2>
          <p class="muted">通过邀请码授权，对比两个人10维画像的契合度，发现相似点与互补空间。</p>
        </div>
        <RouterLink class="button" to="/match">开始匹配</RouterLink>
      </div>
    </section>

    <!-- Community Entry -->
    <section class="panel section-gap">
      <div class="split">
        <div>
          <h2><MessageSquare :size="22" class="icon-md community-heading-icon" />个性社区</h2>
          <p class="muted">基于人格向量匹配同频的人，发现共鸣动态，分享你的生活方式。</p>
        </div>
        <RouterLink class="button secondary" to="/community">进入社区</RouterLink>
      </div>
    </section>
  </section>
</template>

<style scoped>
.dimension-demo-fill {
  width: 50%;
}

.scene-section-title {
  margin-bottom: 16px;
}

.match-heading-icon {
  vertical-align: middle;
  margin-right: 6px;
  color: var(--trace);
}

.community-heading-icon {
  vertical-align: middle;
  margin-right: 6px;
  color: var(--blip);
}
</style>
