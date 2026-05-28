<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { matchApi, recommendationApi, reportApi, testApi } from '../api'
import { useAuthStore } from '../stores/auth'
import type { MatchReport, Recommendation, ReportSnapshot, ShareLinkSummary, TestResult } from '../types'

const auth = useAuthStore()
const tests = ref<TestResult[]>([])
const reports = ref<ReportSnapshot[]>([])
const matches = ref<MatchReport[]>([])
const shares = ref<ShareLinkSummary[]>([])
const recommendations = ref<Recommendation[]>([])
const error = ref('')
const loading = ref(true)

const quick = [
  { title: '完成测试', desc: '基础性格、饮食偏好、旅游偏好', path: '/tests/personality' },
  { title: '查看报告', desc: '雷达图、文字画像和历史快照', path: '/report' },
  { title: '获取推荐', desc: '餐饮、旅行、穿搭、生涯建议', path: '/recommendations' },
  { title: '双人适配', desc: '用好友手机号生成适配报告', path: '/match' }
]

function formatTime(value?: string) {
  return value ? new Date(value).toLocaleString() : '暂无记录'
}

async function load() {
  error.value = ''
  loading.value = true
  try {
    const [testData, reportData, matchData, shareData, recData] = await Promise.all([
      testApi.history(),
      reportApi.history(),
      matchApi.list(),
      reportApi.shares(),
      recommendationApi.list('travel')
    ])
    tests.value = testData
    reports.value = reportData
    matches.value = matchData
    shares.value = shareData
    recommendations.value = recData.slice(0, 3)
  } catch (err) {
    error.value = (err as Error).message
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">工作台</p>
        <h1>{{ auth.user?.displayName }}，开始今天的生活指南</h1>
        <p class="muted">这里汇总最新测试、报告、推荐、分享和双人适配状态。</p>
      </div>
      <button class="secondary" type="button" @click="load">刷新</button>
    </header>

    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="loading" class="notice">正在加载你的最新数据...</div>

    <div class="grid four">
      <div class="card metric"><strong>{{ tests.length }}</strong><span>测试记录</span></div>
      <div class="card metric"><strong>{{ reports.length }}</strong><span>报告快照</span></div>
      <div class="card metric"><strong>{{ matches.length }}</strong><span>适配报告</span></div>
      <div class="card metric"><strong>{{ shares.filter((item) => item.active).length }}</strong><span>有效分享</span></div>
    </div>

    <section class="panel" style="margin-top: 16px">
      <div class="page-header compact">
        <div>
          <p class="eyebrow">近期动态</p>
          <h2>最新画像和生活建议</h2>
        </div>
        <RouterLink class="button" to="/report">打开报告</RouterLink>
      </div>
      <div class="grid three">
        <article class="card">
          <h3>最新报告</h3>
          <p v-if="reports[0]">{{ reports[0].summary }}</p>
          <p class="muted">{{ formatTime(reports[0]?.createdAt) }}</p>
        </article>
        <article class="card">
          <h3>最新适配</h3>
          <p v-if="matches[0]">{{ matches[0].target.displayName }} · {{ matches[0].score }} 分</p>
          <p v-else>还没有生成双人适配。</p>
          <p class="muted">{{ formatTime(matches[0]?.createdAt) }}</p>
        </article>
        <article class="card">
          <h3>旅行推荐</h3>
          <p v-if="recommendations[0]">{{ recommendations[0].title }} · {{ recommendations[0].score }} 分</p>
          <p v-else>完成测试后会生成推荐。</p>
          <p class="muted">{{ recommendations[0]?.tags?.join(' / ') || '暂无标签' }}</p>
        </article>
      </div>
    </section>

    <div class="grid two" style="margin-top: 16px">
      <RouterLink v-for="item in quick" :key="item.path" class="card action-card" :to="item.path">
        <h2>{{ item.title }}</h2>
        <p class="muted">{{ item.desc }}</p>
        <span class="button">进入</span>
      </RouterLink>
    </div>
  </section>
</template>
