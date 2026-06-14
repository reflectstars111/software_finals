<script setup lang="ts">
import HeroRadarScene from '../components/common/HeroRadarScene.vue'

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
</script>

<template>
  <section class="home-page">
    <div class="hero-band">
      <HeroRadarScene />
      <div class="hero-copy">
        <p class="eyebrow">性格雷达·生活指南</p>
        <h1>用性格画像帮你发现更适合自己的饮食、旅行和社交方式。</h1>
        <p>
          这不是单纯的性格测试。你会完成一组轻量测评，获得可解释报告，再把报告用于生活推荐和双人适配分析。
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
  </section>
</template>
