<script setup lang="ts">
import * as echarts from 'echarts'
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import EmptyState from '../components/common/EmptyState.vue'
import PageContainer from '../components/common/PageContainer.vue'
import ReportDimensionCard from '../components/report/ReportDimensionCard.vue'
import { demoPortrait } from '../data/mockUsers'
import type { UserPortrait } from '../productTypes'
import { dimensionExplanation, personalityDimensionLabels, portraitSummary, portraitTitle } from '../utils/scoring'
import { loadProductState } from '../utils/storage'

const route = useRoute()
const chartEl = ref<HTMLDivElement | null>(null)
const notice = ref('')
let chart: echarts.ECharts | null = null

const isDemo = computed(() => route.query.demo === 'true')
const state = computed(() => loadProductState())
const portrait = computed<UserPortrait | null>(() => isDemo.value ? demoPortrait : state.value.portrait)
const title = computed(() => portrait.value ? portraitTitle(portrait.value) : '')
const summary = computed(() => portrait.value ? portraitSummary(portrait.value) : '')

const dimensionCards = computed(() => {
  if (!portrait.value) return []
  return (Object.keys(personalityDimensionLabels) as Array<keyof typeof personalityDimensionLabels>).map((key) => ({
    key,
    name: personalityDimensionLabels[key],
    score: portrait.value![key],
    ...dimensionExplanation(key, portrait.value![key])
  }))
})

const lifestyleTags = computed(() => {
  if (!portrait.value) return []
  const tags = []
  if (portrait.value.foodAdventure >= 65) tags.push('饮食探索')
  if (portrait.value.foodSocial >= 65) tags.push('聚餐分享')
  if (portrait.value.travelAdventure >= 65) tags.push('旅行探索')
  if (portrait.value.travelPlanning >= 65) tags.push('计划稳定')
  if (portrait.value.socialEnergy >= 65) tags.push('社交能量')
  return tags.length ? tags : ['轻量尝试', '稳定节奏']
})

function draw() {
  if (!chartEl.value || !portrait.value) return
  chart?.dispose()
  chart = echarts.init(chartEl.value)
  chart.setOption({
    tooltip: {},
    radar: {
      indicator: dimensionCards.value.map((item) => ({ name: item.name, max: 100 })),
      radius: '68%',
      axisName: { color: '#243036' }
    },
    series: [
      {
        type: 'radar',
        data: [{ value: dimensionCards.value.map((item) => item.score), name: '画像分数' }],
        areaStyle: { color: 'rgba(36, 123, 117, 0.22)' },
        lineStyle: { color: '#247b75', width: 3 },
        itemStyle: { color: '#247b75' }
      }
    ]
  })
}

async function copyDemoLink() {
  const url = `${location.origin}/report?demo=true`
  await navigator.clipboard?.writeText(url)
  notice.value = '示例报告链接已复制。'
}

function resize() {
  chart?.resize()
}

watch(portrait, async () => {
  await nextTick()
  draw()
})

onMounted(async () => {
  await nextTick()
  draw()
  window.addEventListener('resize', resize)
})

onUnmounted(() => {
  window.removeEventListener('resize', resize)
  chart?.dispose()
})
</script>

<template>
  <PageContainer
    eyebrow="我的报告"
    title="可解释的多维生活画像"
    description="报告会把性格维度转化为餐饮、旅行、社交建议的依据。"
  >
    <template #actions>
      <div class="toolbar">
        <RouterLink class="button secondary" to="/tests/personality">重新测试</RouterLink>
        <RouterLink class="button" to="/recommendations">进入推荐</RouterLink>
      </div>
    </template>

    <div v-if="notice" class="notice">{{ notice }}</div>

    <EmptyState
      v-if="!portrait"
      title="你还没有完成测试，请先完成测试后查看报告。"
      description="完成四类测评后，这里会展示雷达图、维度解释、生活偏好标签和推荐依据。"
      action-label="去完成测试"
      action-to="/tests/personality"
    />

    <template v-else>
      <section class="panel report-summary">
        <div>
          <p class="eyebrow">{{ isDemo ? '示例数据' : '最近一次测试' }}</p>
          <h2>你的综合画像：{{ title }}</h2>
          <p>{{ summary }}</p>
          <div class="tag-row">
            <span v-for="tag in lifestyleTags" :key="tag">{{ tag }}</span>
          </div>
        </div>
        <button class="ghost" type="button" @click="copyDemoLink">复制示例链接</button>
      </section>

      <section class="grid two report-grid">
        <div class="panel">
          <h2>性格雷达图</h2>
          <div ref="chartEl" class="chart-box"></div>
        </div>
        <div class="panel">
          <h2>推荐依据说明</h2>
          <p>开放性会提高探索型餐饮、城市漫游和新社交活动的权重。</p>
          <p>尽责性会提高计划明确、价格可控和流程稳定的推荐权重。</p>
          <p>外向性和社交能量会影响聚餐、活动和双人适配中的互动建议。</p>
          <p>情绪稳定性会影响系统对舒缓、低压力和恢复型场景的推荐。</p>
        </div>
      </section>

      <section class="grid two">
        <ReportDimensionCard
          v-for="item in dimensionCards"
          :key="item.key"
          :name="item.name"
          :score="item.score"
          :explanation="item.explanation"
          :impact="item.impact"
        />
      </section>

      <section class="grid three">
        <article class="card">
          <h3>饮食倾向分析</h3>
          <p>你在饮食探索上的分数为 {{ portrait.foodAdventure }}，在聚餐分享上的分数为 {{ portrait.foodSocial }}。</p>
        </article>
        <article class="card">
          <h3>旅游倾向分析</h3>
          <p>你在旅行探索上的分数为 {{ portrait.travelAdventure }}，在计划稳定上的分数为 {{ portrait.travelPlanning }}。</p>
        </article>
        <article class="card">
          <h3>社交倾向分析</h3>
          <p>你的社交能量分数为 {{ portrait.socialEnergy }}，适合据此选择活动强度和互动密度。</p>
        </article>
      </section>
    </template>
  </PageContainer>
</template>
