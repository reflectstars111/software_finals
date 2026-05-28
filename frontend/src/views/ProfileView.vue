<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { authApi, reportApi, testApi } from '../api'
import { useAuthStore } from '../stores/auth'
import type { ReportSnapshot, ShareLinkSummary, TestResult } from '../types'

const auth = useAuthStore()
const displayName = ref(auth.user?.displayName || '')
const avatarUrl = ref(auth.user?.avatarUrl || '')
const history = ref<TestResult[]>([])
const reports = ref<ReportSnapshot[]>([])
const shares = ref<ShareLinkSummary[]>([])
const error = ref('')
const notice = ref('')

function formatTime(value?: string) {
  return value ? new Date(value).toLocaleString() : '暂无'
}

async function save() {
  error.value = ''
  notice.value = ''
  try {
    const user = await authApi.updateMe({ displayName: displayName.value, avatarUrl: avatarUrl.value })
    auth.user = user
    localStorage.setItem('radar_user', JSON.stringify(user))
    notice.value = '资料已更新'
  } catch (err) {
    error.value = (err as Error).message
  }
}

onMounted(async () => {
  try {
    const [testData, reportData, shareData] = await Promise.all([
      testApi.history(),
      reportApi.history(),
      reportApi.shares()
    ])
    history.value = testData
    reports.value = reportData
    shares.value = shareData
  } catch (err) {
    error.value = (err as Error).message
  }
})
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">个人中心</p>
        <h1>账号资料与历史数据</h1>
      </div>
    </header>
    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="notice" class="notice">{{ notice }}</div>
    <div class="grid two">
      <section class="panel">
        <div class="field">
          <label>昵称</label>
          <input v-model="displayName" />
        </div>
        <div class="field">
          <label>头像 URL</label>
          <input v-model="avatarUrl" />
        </div>
        <button class="primary" @click="save">保存资料</button>
      </section>
      <section class="panel">
        <h2>历史测试</h2>
        <div v-if="!history.length" class="notice">暂无测试记录。</div>
        <div v-else class="table-wrap">
          <table>
            <thead><tr><th>类型</th><th>时间</th><th>摘要</th></tr></thead>
            <tbody>
              <tr v-for="item in history" :key="item.id">
                <td>{{ item.type }}</td>
                <td>{{ formatTime(item.createdAt) }}</td>
                <td>{{ Object.values(item.scores).join(' / ') }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <div class="grid two" style="margin-top: 16px">
      <section class="panel">
        <h2>报告快照</h2>
        <div v-if="!reports.length" class="notice">暂无报告快照。</div>
        <div v-else class="table-wrap">
          <table>
            <thead><tr><th>摘要</th><th>时间</th></tr></thead>
            <tbody>
              <tr v-for="item in reports" :key="item.id">
                <td>{{ item.summary }}</td>
                <td>{{ formatTime(item.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel">
        <h2>分享状态</h2>
        <div v-if="!shares.length" class="notice">暂无分享链接。</div>
        <div v-else class="table-wrap">
          <table>
            <thead><tr><th>Token</th><th>状态</th><th>过期时间</th></tr></thead>
            <tbody>
              <tr v-for="item in shares" :key="item.id">
                <td>{{ item.token.slice(0, 10) }}...</td>
                <td>{{ item.active ? '有效' : '已撤销' }}</td>
                <td>{{ formatTime(item.expiresAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </section>
</template>
