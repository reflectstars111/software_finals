<script setup lang="ts">
import * as echarts from 'echarts'
import { nextTick, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { reportApi } from '../api'
import type { Report } from '../types'

const route = useRoute()
const report = ref<Report | null>(null)
const error = ref('')
const chartEl = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null

async function load() {
  try {
    report.value = await reportApi.byToken(route.params.token as string)
    await nextTick()
    if (!chartEl.value || !report.value) return
    chart = echarts.init(chartEl.value)
    chart.setOption({
      radar: { indicator: report.value.indicators, radius: '68%' },
      series: [{ type: 'radar', data: [{ value: report.value.radarValues }] }]
    })
  } catch (err) {
    error.value = (err as Error).message
  }
}

onMounted(load)
window.addEventListener('resize', () => chart?.resize())
</script>

<template>
  <section class="page" style="padding: 28px">
    <header class="page-header">
      <div>
        <p class="eyebrow">分享报告</p>
        <h1>{{ report?.user.displayName || '性格雷达' }} 的公开画像</h1>
      </div>
      <RouterLink class="button" to="/login">进入系统</RouterLink>
    </header>
    <div v-if="error" class="error">{{ error }}</div>
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

