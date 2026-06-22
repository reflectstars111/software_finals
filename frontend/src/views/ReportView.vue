<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import EmptyState from '../components/common/EmptyState.vue'
import LoadingState from '../components/common/LoadingState.vue'
import PageContainer from '../components/common/PageContainer.vue'
import type { Report } from '../types'
import { getMyReport, shareReport } from '../services/reportService'
import { DISPLAY_LABELS, dimensionExplanation, portraitTitle, portraitSummary, displayScore } from '../utils/dimensions'
import type { DimensionKey } from '../utils/dimensions'
import { useRadarChart } from '../composables/useRadarChart'

const route = useRoute()
const chartEl = ref<HTMLDivElement | null>(null)
const { draw, resize } = useRadarChart(chartEl)
const report = ref<Report | null>(null)
const loading = ref(true)
const error = ref('')
const notice = ref('')
const shareUrl = ref('')

const isDemo = computed(() => route.query.demo === 'true')
const title = computed(() => report.value?.scores ? portraitTitle(report.value.scores) : '')
const summary = computed(() => report.value?.scores ? portraitSummary(report.value.scores) : '')

const personalityKeys: DimensionKey[] = ['OPENNESS', 'CONSCIENTIOUSNESS', 'EXTRAVERSION', 'AGREEABLENESS', 'NEUROTICISM']
const lifestyleKeys: DimensionKey[] = ['FOOD_ADVENTURE', 'FOOD_SOCIAL', 'TRAVEL_ADVENTURE', 'TRAVEL_PLANNING', 'SOCIAL_ENERGY']

const personalityCards = computed(() => {
  if (!report.value?.scores) return []
  return personalityKeys.map((key) => {
    const rawScore = report.value!.scores[key] ?? 50
    const displayVal = displayScore(key, rawScore)
    return {
      key,
      name: DISPLAY_LABELS[key],
      score: displayVal,
      rawScore,
      ...dimensionExplanation(key, rawScore)
    }
  })
})

const lifestyleCards = computed(() => {
  if (!report.value?.scores) return []
  return lifestyleKeys.map((key) => ({
    key,
    name: DISPLAY_LABELS[key],
    score: report.value!.scores[key] ?? 50,
    ...dimensionExplanation(key, report.value!.scores[key] ?? 50)
  }))
})

function drawChart() {
  if (!report.value) return
  draw({
    tooltip: {},
    radar: {
      indicator: personalityCards.value.map((c) => ({ name: c.name, max: 100 })),
      radius: '65%',
      axisName: { color: '#c8d0e0' },
      splitArea: { areaStyle: { color: ['rgba(108,204,160,0.04)', 'rgba(108,204,160,0.02)'] } },
      axisLine: { lineStyle: { color: 'rgba(180,185,210,0.2)' } },
      splitLine: { lineStyle: { color: 'rgba(180,185,210,0.15)' } }
    },
    series: [{
      type: 'radar',
      data: [{ value: personalityCards.value.map((c) => c.score), name: '画像分数' }],
      areaStyle: { color: 'rgba(108, 204, 160, 0.12)' },
      lineStyle: { color: '#6ccca0', width: 3 },
      itemStyle: { color: '#6ccca0' }
    }]
  })
}

async function doShare() {
  try {
    const result = await shareReport()
    const url = `${location.origin}/share/${result.token}`
    shareUrl.value = url
    await navigator.clipboard?.writeText(url)
    notice.value = '分享链接已生成并复制到剪贴板！'
  } catch (err) {
    error.value = (err as Error).message || '生成分享链接失败'
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    report.value = await getMyReport()
  } catch (err) {
    error.value = (err as Error).message || '报告加载失败'
  } finally {
    loading.value = false
  }
}

watch(report, async () => { await nextTick(); drawChart() })
onMounted(async () => {
  await load()
  await nextTick()
  drawChart()
  window.addEventListener('resize', resize)
})
</script>

<template>
  <PageContainer
    eyebrow="我的报告"
    title="可解释的多维生活画像"
    description="报告由后端基于你的测试答案自动生成，包含10个维度的分析和个性化建议。"
  >
    <template #actions>
      <div class="toolbar">
        <RouterLink class="button secondary" to="/tests/personality">重新测试</RouterLink>
        <RouterLink class="button" to="/recommendations">进入推荐</RouterLink>
        <button v-if="report" class="ghost" type="button" @click="doShare">分享报告</button>
      </div>
    </template>

    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="notice" class="notice">{{ notice }}</div>
    <LoadingState v-if="loading" message="正在生成报告..." />

    <EmptyState
      v-else-if="!report"
      title="你还没有完成测试，请先完成测试后查看报告。"
      description="完成四类测评后，这里会展示雷达图、10维维度解释、生活偏好标签和个性化建议。"
      action-label="去完成测试"
      action-to="/tests/personality"
    />

    <template v-else>
      <section class="panel report-summary">
        <div>
          <p class="eyebrow">{{ isDemo ? '示例数据' : '最近一次测试报告' }}</p>
          <h2>你的综合画像：{{ title }}</h2>
          <p>{{ summary }}</p>
        </div>
      </section>

      <section class="grid two report-grid">
        <div class="radar-shell">
          <h2>性格雷达图</h2>
          <div ref="chartEl" class="chart-box"></div>
        </div>
        <div class="panel">
          <h2>推荐依据说明</h2>
          <ul class="recommendation-basis-list">
            <li>开放性会提高探索型餐饮、城市漫游和新社交活动的推荐权重。</li>
            <li>尽责性会提高计划明确、价格可控和流程稳定的推荐权重。</li>
            <li>外向性和社交能量会影响聚餐、活动和双人适配中的互动建议。</li>
            <li>饮食探索和旅行计划等维度直接影响对应场景的推荐排序。</li>
            <li>情绪稳定性会影响系统对舒缓、低压力和恢复型场景的推荐。</li>
          </ul>
        </div>
      </section>

      <section class="grid two section-gap">
        <article v-for="card in personalityCards" :key="card.key" class="card dimension-card" :class="card.key">
          <div class="split">
            <h3>{{ card.name }}</h3>
            <strong>{{ card.score }} / 100</strong>
          </div>
          <div class="mini-meter"><span class="dimension-meter-fill" :style="{ '--meter-width': `${card.score}%` }"></span></div>
          <p>{{ card.explanation }}</p>
          <p class="muted">{{ card.impact }}</p>
        </article>
      </section>

      <section class="grid two section-gap">
        <article v-for="card in lifestyleCards" :key="card.key" class="card dimension-card lifestyle" :class="card.key">
          <div class="split">
            <h3>{{ card.name }}</h3>
            <strong>{{ card.score }} / 100</strong>
          </div>
          <div class="mini-meter"><span class="dimension-meter-fill" :style="{ '--meter-width': `${card.score}%` }"></span></div>
          <p>{{ card.explanation }}</p>
          <p class="muted">{{ card.impact }}</p>
        </article>
      </section>

      <section v-if="report.suggestions.length" class="panel section-gap">
        <h2>个性化建议</h2>
        <div v-for="(s, i) in report.suggestions" :key="i" class="suggestion-row">
          <span class="suggestion-num">{{ i + 1 }}</span>
          <p>{{ s }}</p>
        </div>
      </section>
    </template>
  </PageContainer>
</template>

<style scoped>
.dimension-card.OPENNESS,
.dimension-card.CONSCIENTIOUSNESS,
.dimension-card.EXTRAVERSION,
.dimension-card.AGREEABLENESS,
.dimension-card.NEUROTICISM {
  border-left: 3px solid var(--blip);
  padding-left: 15px;
}
.dimension-card.lifestyle {
  border-left: 3px solid var(--signal);
  padding-left: 15px;
}

.recommendation-basis-list {
  padding-left: 18px;
  line-height: 1.8;
  color: var(--muted);
}

.dimension-meter-fill {
  width: var(--meter-width);
}
</style>
