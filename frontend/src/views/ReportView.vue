<script setup lang="ts">
import * as echarts from 'echarts'
import html2canvas from 'html2canvas'
import { nextTick, onMounted, ref } from 'vue'
import { reportApi } from '../api'
import type { Report } from '../types'

const report = ref<Report | null>(null)
const error = ref('')
const notice = ref('')
const chartEl = ref<HTMLDivElement | null>(null)
const reportEl = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

async function load() {
  error.value = ''
  try {
    report.value = await reportApi.me()
    await nextTick()
    draw()
  } catch (err) {
    error.value = (err as Error).message
  }
}

function draw() {
  if (!chartEl.value || !report.value) return
  chart?.dispose()
  chart = echarts.init(chartEl.value)
  chart.setOption({
    tooltip: {},
    radar: {
      indicator: report.value.indicators,
      radius: '68%',
      axisName: { color: '#243036' },
      splitArea: { areaStyle: { color: ['#f4f8f6', '#ffffff'] } }
    },
    series: [
      {
        type: 'radar',
        data: [{ value: report.value.radarValues, name: '性格得分' }],
        areaStyle: { color: 'rgba(36, 123, 117, 0.24)' },
        lineStyle: { color: '#247b75', width: 3 },
        itemStyle: { color: '#247b75' }
      }
    ]
  })
}

async function createShare() {
  error.value = ''
  try {
    const result = await reportApi.share()
    notice.value = `分享链接：${result.url}`
    await navigator.clipboard?.writeText(result.url)
  } catch (err) {
    error.value = (err as Error).message
  }
}

async function exportPng() {
  if (!reportEl.value) return
  const canvas = await html2canvas(reportEl.value, { backgroundColor: '#ffffff' })
  const link = document.createElement('a')
  link.download = '性格雷达报告.png'
  link.href = canvas.toDataURL('image/png')
  link.click()
}

onMounted(load)
window.addEventListener('resize', () => chart?.resize())
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">综合画像</p>
        <h1>我的性格雷达报告</h1>
        <p class="muted">报告基于最新基础性格测试生成，可导出 PNG 或生成分享链接。</p>
      </div>
      <div class="toolbar">
        <button class="secondary" type="button" @click="exportPng">导出 PNG</button>
        <button class="primary" type="button" @click="createShare">生成分享</button>
      </div>
    </header>
    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="notice" class="notice">{{ notice }}</div>
    <section v-if="report" ref="reportEl" class="panel">
      <div class="grid two">
        <div>
          <h2>{{ report.user.displayName }} 的多维性格画像</h2>
          <p class="muted">生成时间：{{ new Date(report.generatedAt).toLocaleString() }}</p>
          <div class="grid">
            <div v-for="line in report.interpretations" :key="line" class="card">{{ line }}</div>
          </div>
        </div>
        <div ref="chartEl" class="chart-box"></div>
      </div>
      <h2 style="margin-top: 18px">生活建议</h2>
      <div class="grid two">
        <div v-for="suggestion in report.suggestions" :key="suggestion" class="card">{{ suggestion }}</div>
      </div>
    </section>
  </section>
</template>

