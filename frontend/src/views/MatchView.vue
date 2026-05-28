<script setup lang="ts">
import { ref } from 'vue'
import { matchApi } from '../api'
import type { MatchReport } from '../types'

const friendPhone = ref('13900000002')
const report = ref<MatchReport | null>(null)
const error = ref('')
const loading = ref(false)

async function create() {
  error.value = ''
  loading.value = true
  try {
    report.value = await matchApi.create(friendPhone.value)
  } catch (err) {
    error.value = (err as Error).message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">双人适配</p>
        <h1>复用双方历史测试数据</h1>
        <p class="muted">输入好友手机号即可生成契合度、分歧预警和相处建议。</p>
      </div>
    </header>
    <div v-if="error" class="error">{{ error }}</div>
    <section class="panel">
      <div class="field">
        <label>好友手机号</label>
        <input v-model="friendPhone" maxlength="11" />
      </div>
      <button class="primary" :disabled="loading" @click="create">{{ loading ? '计算中' : '生成适配报告' }}</button>
    </section>
    <section v-if="report" class="panel" style="margin-top: 16px">
      <div class="page-header">
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
  </section>
</template>

