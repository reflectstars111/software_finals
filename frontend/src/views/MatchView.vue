<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { matchApi } from '../api'
import type { MatchReport } from '../types'

const friendPhone = ref('13900000002')
const report = ref<MatchReport | null>(null)
const history = ref<MatchReport[]>([])
const error = ref('')
const notice = ref('')
const loading = ref(false)

function formatTime(value?: string) {
  return value ? new Date(value).toLocaleString() : '暂无'
}

async function loadHistory() {
  history.value = await matchApi.list()
  if (!report.value && history.value.length) {
    report.value = history.value[0]
  }
}

async function create() {
  error.value = ''
  notice.value = ''
  loading.value = true
  try {
    report.value = await matchApi.create(friendPhone.value)
    notice.value = '适配报告已生成'
    await loadHistory()
  } catch (err) {
    error.value = (err as Error).message
  } finally {
    loading.value = false
  }
}

onMounted(loadHistory)
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">双人适配</p>
        <h1>复用双方历史测试数据</h1>
        <p class="muted">输入好友手机号即可生成契合度、分歧预警和相处建议，历史报告会自动保留。</p>
      </div>
    </header>

    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="notice" class="notice">{{ notice }}</div>

    <section class="panel">
      <div class="grid two">
        <div class="field">
          <label>好友手机号</label>
          <input v-model="friendPhone" maxlength="11" />
        </div>
        <div class="field">
          <label>操作</label>
          <button class="primary" :disabled="loading" @click="create">{{ loading ? '计算中' : '生成适配报告' }}</button>
        </div>
      </div>
    </section>

    <section v-if="report" class="panel" style="margin-top: 16px">
      <div class="page-header compact">
        <div>
          <h2>{{ report.owner.displayName }} × {{ report.target.displayName }}</h2>
          <p class="muted">{{ report.summary }}</p>
        </div>
        <span class="score-pill">{{ report.score }}</span>
      </div>
      <div class="grid three">
        <div class="card">
          <h3>互补优势</h3>
          <p v-for="item in report.advantages" :key="item">{{ item }}</p>
        </div>
        <div class="card">
          <h3>分歧预警</h3>
          <p v-for="item in report.warnings" :key="item">{{ item }}</p>
        </div>
        <div class="card">
          <h3>相处建议</h3>
          <p v-for="item in report.advice" :key="item">{{ item }}</p>
        </div>
      </div>
    </section>

    <section class="panel" style="margin-top: 16px">
      <h2>历史适配</h2>
      <div v-if="!history.length" class="notice">暂无历史适配报告。</div>
      <div v-else class="table-wrap">
        <table>
          <thead><tr><th>对象</th><th>分数</th><th>时间</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="item in history" :key="item.id">
              <td>{{ item.target.displayName }}</td>
              <td>{{ item.score }}</td>
              <td>{{ formatTime(item.createdAt) }}</td>
              <td><button class="secondary" @click="report = item">查看</button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>
