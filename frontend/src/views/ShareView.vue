<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import EmptyState from '../components/common/EmptyState.vue'
import LoadingState from '../components/common/LoadingState.vue'
import { reportApi } from '../api'
import type { Report } from '../types'
import { useRadarChart } from '../composables/useRadarChart'

const route = useRoute()
const report = ref<Report | null>(null)
const error = ref('')
const loading = ref(true)
const chartEl = ref<HTMLDivElement | null>(null)
const { draw: drawChartFn, resize } = useRadarChart(chartEl)

async function load() {
  loading.value = true
  try {
    report.value = await reportApi.byToken(route.params.token as string)
    await nextTick()
    drawShareChart()
  } catch (err) {
    error.value = (err as Error).message
  } finally {
    loading.value = false
  }
}

function drawShareChart() {
  if (!report.value) return
  drawChartFn({
    radar: { indicator: report.value.indicators, radius: '68%' },
    series: [{ type: 'radar', data: [{ value: report.value.radarValues, name: '公开画像' }] }]
  })
}

onMounted(() => {
  load()
  window.addEventListener('resize', resize)
})
</script>

<template>
  <section class="page share-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">分享报告</p>
        <h1>{{ report?.user.displayName || '性格雷达' }} 的公开画像</h1>
        <p class="muted">公开报告只展示摘要维度，不展示完整答题内容。</p>
      </div>
      <RouterLink class="button" to="/">进入系统</RouterLink>
    </header>
    <LoadingState v-if="loading" message="正在加载分享报告..." />
    <div v-if="error" class="error">{{ error }}</div>
    <EmptyState
      v-if="!loading && !report"
      title="分享报告不可用"
      description="链接可能已过期或被撤销。"
      action-label="返回首页"
      action-to="/"
    />
    <section v-if="report" class="panel">
      <div class="grid two">
        <div ref="chartEl" class="chart-box"></div>
        <div class="grid">
          <div v-for="line in report.interpretations" :key="line" class="card">{{ line }}</div>
        </div>
      </div>
    </section>
  </section>
</template>
