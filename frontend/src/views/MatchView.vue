<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import EmptyState from '../components/common/EmptyState.vue'
import LoadingState from '../components/common/LoadingState.vue'
import PageContainer from '../components/common/PageContainer.vue'
import type { MatchInvite, MatchReport } from '../types'
import { generateInvite, createMatchByInvite } from '../services/matchService'
import { DISPLAY_LABELS } from '../utils/dimensions'
import type { DimensionKey } from '../utils/dimensions'
import { useRadarChart } from '../composables/useRadarChart'

const personalityKeys: DimensionKey[] = ['OPENNESS', 'CONSCIENTIOUSNESS', 'EXTRAVERSION', 'AGREEABLENESS', 'NEUROTICISM']
const chartEl = ref<HTMLDivElement | null>(null)
const { draw: drawChartFn, resize } = useRadarChart(chartEl)

const inviteCode = ref('')
const myInvite = ref<MatchInvite | null>(null)
const result = ref<MatchReport | null>(null)
const error = ref('')
const notice = ref('')
const loading = ref(false)
const pageLoading = ref(true)
const scoreColor = computed(() => {
  const s = result.value?.score ?? 0
  if (s >= 80) return 'var(--blip)'
  if (s >= 60) return 'var(--signal)'
  return 'var(--trace)'
})

async function createInvite() {
  error.value = ''
  try {
    myInvite.value = await generateInvite()
    notice.value = `你的匹配邀请码已生成：${myInvite.value.code}`
  } catch (err) {
    error.value = (err as Error).message
  }
}

async function copyInvite() {
  if (!myInvite.value) return
  await navigator.clipboard.writeText(myInvite.value.code)
  notice.value = '邀请码已复制到剪贴板'
}

async function doMatch() {
  error.value = ''
  notice.value = ''
  loading.value = true
  try {
    result.value = await createMatchByInvite(inviteCode.value.trim())
    notice.value = '匹配成功，已生成双人适配报告。'
    await nextTick()
    drawMatchChart()
  } catch (err) {
    error.value = (err as Error).message
  } finally {
    loading.value = false
  }
}

function drawMatchChart() {
  if (!result.value) return
  const keys = personalityKeys
  drawChartFn({
    legend: { bottom: 0 },
    tooltip: {},
    radar: {
      indicator: keys.map((k) => ({ name: DISPLAY_LABELS[k], max: 100 })),
      radius: '58%'
    },
    series: [{
      type: 'radar',
      data: [
        { value: keys.map((k) => result.value!.ownerScores[k] ?? 50), name: '我',
          areaStyle: { color: 'rgba(108, 204, 160, 0.10)' },
          lineStyle: { color: '#6ccca0', width: 2.5 },
          itemStyle: { color: '#6ccca0' } },
        { value: keys.map((k) => result.value!.targetScores[k] ?? 50), name: '对方',
          areaStyle: { color: 'rgba(232, 180, 79, 0.10)' },
          lineStyle: { color: '#e8b44f', width: 2.5 },
          itemStyle: { color: '#e8b44f' } }
      ]
    }]
  })
}

onMounted(async () => {
  pageLoading.value = false
  await nextTick()
  drawMatchChart()
  window.addEventListener('resize', resize)
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
    <LoadingState v-if="pageLoading" message="正在加载..." />

    <section class="grid two">
      <article class="panel">
        <h2>匹配入口区</h2>
        <p class="muted">你可以先生成自己的邀请码分享给好友，也可以输入好友给你的邀请码。</p>
        <div class="toolbar">
          <button class="primary" type="button" @click="createInvite">生成我的匹配邀请码</button>
        </div>
        <div v-if="myInvite" class="invite-box">
          <span class="invite-code">{{ myInvite.code }}</span>
          <button class="ghost small" type="button" @click="copyInvite">复制</button>
        </div>
      </article>
      <article class="panel">
        <h2>输入对方邀请码</h2>
        <div class="field">
          <label>好友的匹配邀请码</label>
          <input v-model="inviteCode" placeholder="请输入好友分享的邀请码" />
        </div>
        <button class="primary full" type="button" :disabled="loading" @click="doMatch">
          {{ loading ? '匹配计算中...' : '生成适配报告' }}
        </button>
      </article>
    </section>

    <section v-if="result" class="panel match-summary section-gap">
      <div class="split">
        <div>
          <p class="eyebrow">匹配成功</p>
          <h2>{{ result.owner.displayName }} x {{ result.target.displayName }}</h2>
          <p>总体契合度来自10个维度的差异平均模型。</p>
        </div>
        <span class="big-score match-score-value" :style="{ '--score-bg': scoreColor }">{{ result.score }}%</span>
      </div>
    </section>

    <section v-if="result" class="grid two section-gap">
      <div class="radar-shell">
        <h2>双人雷达图对比</h2>
        <div ref="chartEl" class="chart-box"></div>
      </div>
      <div class="grid">
        <article class="card">
          <h3>{{ result.summary }}</h3>
        </article>
        <article class="card advantage-card">
          <h3>高契合维度</h3>
          <p v-for="a in result.advantages" :key="a">{{ a }}</p>
        </article>
        <article class="card warning-card">
          <h3>差异较大维度</h3>
          <p v-for="w in result.warnings" :key="w">{{ w }}</p>
        </article>
        <article class="card">
          <h3>相处建议</h3>
          <p v-for="adv in result.advice" :key="adv">{{ adv }}</p>
        </article>
      </div>
    </section>

    <section class="privacy-note">
      该适配报告基于双方已授权的历史测试数据生成，不会展示对方完整答题内容，仅展示维度差异和相处建议。
    </section>
  </PageContainer>
</template>

<style scoped>
.match-score-value {
  background: var(--score-bg);
}

.advantage-card {
  border-left: 3px solid var(--blip);
  padding-left: 15px;
}

.warning-card {
  border-left: 3px solid var(--signal);
  padding-left: 15px;
}
</style>
