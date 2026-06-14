<script setup lang="ts">
import * as echarts from 'echarts'
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import EmptyState from '../components/common/EmptyState.vue'
import LoadingState from '../components/common/LoadingState.vue'
import PageContainer from '../components/common/PageContainer.vue'
import MatchResultCard from '../components/match/MatchResultCard.vue'
import { demoInviteCodes, demoPartnerPortrait } from '../data/mockUsers'
import type { MatchInvite, MatchResult } from '../productTypes'
import { generateMyInvite, matchWithInvite } from '../services/matchService'
import { useAuthStore } from '../stores/auth'
import { personalityDimensionLabels } from '../utils/scoring'
import { loadProductState } from '../utils/storage'

const auth = useAuthStore()
const code = ref('STAR2026')
const myInvite = ref<MatchInvite | null>(null)
const result = ref<MatchResult | null>(null)
const hasPortrait = ref(false)
const error = ref('')
const notice = ref('')
const loading = ref(false)
const chartEl = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null

function load() {
  const state = loadProductState()
  hasPortrait.value = Boolean(state.portrait)
  myInvite.value = state.invites.find((invite) => invite.status === 'active') || null
  result.value = state.matchResults[0] || null
}

async function createInvite() {
  error.value = ''
  try {
    myInvite.value = await generateMyInvite(String(auth.user?.id || 'local-user'))
    notice.value = `你的匹配邀请码已生成：${myInvite.value.code}`
  } catch (err) {
    error.value = (err as Error).message
  }
}

async function createMatch() {
  error.value = ''
  notice.value = ''
  loading.value = true
  try {
    result.value = await matchWithInvite(code.value, auth.user?.displayName || '我')
    notice.value = '匹配成功，已生成双人适配报告。'
    await nextTick()
    draw()
  } catch (err) {
    error.value = (err as Error).message
  } finally {
    loading.value = false
  }
}

function draw() {
  const state = loadProductState()
  if (!chartEl.value || !state.portrait) return
  const keys = Object.keys(personalityDimensionLabels) as Array<keyof typeof personalityDimensionLabels>
  chart?.dispose()
  chart = echarts.init(chartEl.value)
  chart.setOption({
    legend: { bottom: 0 },
    tooltip: {},
    radar: {
      indicator: keys.map((key) => ({ name: personalityDimensionLabels[key], max: 100 })),
      radius: '62%'
    },
    series: [{
      type: 'radar',
      data: [
        { value: keys.map((key) => state.portrait![key]), name: '我' },
        { value: keys.map((key) => demoPartnerPortrait[key]), name: '示例好友' }
      ]
    }]
  })
}

function resize() {
  chart?.resize()
}

onMounted(async () => {
  load()
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
    eyebrow="双人适配"
    title="通过邀请码授权生成关系分析报告"
    description="适配报告只展示维度差异、契合度和相处建议，不展示对方完整答题内容。"
  >
    <div v-if="notice" class="notice">{{ notice }}</div>
    <div v-if="error" class="error">{{ error }}</div>
    <LoadingState v-if="loading" message="正在计算双人适配..." />

    <EmptyState
      v-if="!hasPortrait"
      title="未完成测试，暂时无法生成匹配。"
      description="请先完成自己的画像测试，再生成邀请码或输入对方邀请码。"
      action-label="去完成测试"
      action-to="/tests/personality"
    />

    <template v-else>
      <section class="grid two">
        <article class="panel">
          <h2>匹配入口区</h2>
          <p class="muted">你可以先生成自己的邀请码，也可以输入对方给你的邀请码。</p>
          <div class="toolbar">
            <button class="primary" type="button" @click="createInvite">生成我的匹配邀请码</button>
            <span v-if="myInvite" class="code-pill">{{ myInvite.code }}</span>
          </div>
        </article>
        <article class="panel">
          <h2>输入对方邀请码</h2>
          <div class="field">
            <label>示例可用邀请码：{{ demoInviteCodes.join(' / ') }}</label>
            <input v-model="code" placeholder="请输入对方邀请码" />
          </div>
          <button class="primary full" type="button" :disabled="loading" @click="createMatch">
            {{ loading ? '匹配计算中...' : '双方授权后生成适配报告' }}
          </button>
        </article>
      </section>

      <section v-if="result" class="panel match-summary">
        <div class="split">
          <div>
            <p class="eyebrow">匹配成功</p>
            <h2>{{ result.userAName }} × {{ result.userBName }}</h2>
            <p>总体契合度来自五大维度差异的加权模型：单维契合度 = 100 - 维度差异。</p>
          </div>
          <span class="big-score">{{ result.compatibility }}%</span>
        </div>
      </section>

      <section class="grid two">
        <div class="panel">
          <h2>双人雷达图对比</h2>
          <div ref="chartEl" class="chart-box"></div>
        </div>
        <div v-if="result" class="grid">
          <MatchResultCard title="高契合维度" :items="result.strengths" />
          <MatchResultCard title="差异较大维度" :items="result.differences" />
        </div>
      </section>

      <section v-if="result" class="grid two">
        <MatchResultCard title="潜在冲突提醒" :items="result.warnings" />
        <MatchResultCard title="相处建议" :items="result.suggestions" />
      </section>

      <section class="privacy-note">
        该适配报告基于双方已授权的历史测试数据生成，不会展示对方完整答题内容，仅展示维度差异和相处建议。
      </section>
    </template>
  </PageContainer>
</template>
