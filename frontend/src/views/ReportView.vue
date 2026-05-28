<script setup lang="ts">
import * as echarts from 'echarts'
import html2canvas from 'html2canvas'
import { nextTick, onMounted, ref } from 'vue'
import { reportApi } from '../api'
import type { Report, ReportSnapshot, ShareLinkSummary } from '../types'

const report = ref<Report | null>(null)
const history = ref<ReportSnapshot[]>([])
const shares = ref<ShareLinkSummary[]>([])
const error = ref('')
const notice = ref('')
const chartEl = ref<HTMLDivElement | null>(null)
const reportEl = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

function formatTime(value?: string) {
  return value ? new Date(value).toLocaleString() : '暂无'
}

async function load() {
  error.value = ''
  try {
    report.value = await reportApi.me()
    history.value = await reportApi.history()
    shares.value = await reportApi.shares()
    await nextTick()
    draw()
  } catch (err) {
    error.value = (err as Error).message
  }
}

async function openSnapshot(item: ReportSnapshot) {
  report.value = item.report
  await nextTick()
  draw()
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
  notice.value = ''
  try {
    const result = await reportApi.share()
    notice.value = `分享链接已复制：${result.url}`
    await navigator.clipboard?.writeText(result.url)
    shares.value = await reportApi.shares()
  } catch (err) {
    error.value = (err as Error).message
  }
}

async function copyShare(url: string) {
  await navigator.clipboard?.writeText(url)
  notice.value = `分享链接已复制：${url}`
}

async function revokeShare(id: number) {
  await reportApi.revokeShare(id)
  shares.value = await reportApi.shares()
  notice.value = '分享链接已撤销'
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
        <p class="muted">报告基于最新基础性格测试生成，可查看历史快照、导出 PNG 或管理公开分享。</p>
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
          <p class="muted">生成时间：{{ formatTime(report.generatedAt) }}</p>
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

    <div class="grid two" style="margin-top: 16px">
      <section class="panel">
        <h2>报告历史</h2>
        <div v-if="!history.length" class="notice">暂无历史报告，完成测试后会自动生成。</div>
        <div v-else class="table-wrap">
          <table>
            <thead><tr><th>摘要</th><th>时间</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="item in history" :key="item.id">
                <td>{{ item.summary }}</td>
                <td>{{ formatTime(item.createdAt) }}</td>
                <td><button class="secondary" type="button" @click="openSnapshot(item)">查看</button></td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel">
        <h2>分享链接</h2>
        <div v-if="!shares.length" class="notice">还没有分享链接。</div>
        <div v-else class="table-wrap">
          <table>
            <thead><tr><th>Token</th><th>状态</th><th>过期时间</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="item in shares" :key="item.id">
                <td>{{ item.token.slice(0, 10) }}...</td>
                <td>{{ item.active ? '有效' : '已撤销' }}</td>
                <td>{{ formatTime(item.expiresAt) }}</td>
                <td class="row-actions">
                  <button class="secondary" type="button" @click="copyShare(item.url)">复制</button>
                  <button v-if="item.active" class="danger" type="button" @click="revokeShare(item.id)">撤销</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </section>
</template>
