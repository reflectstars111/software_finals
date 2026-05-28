<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { authApi, testApi } from '../api'
import { useAuthStore } from '../stores/auth'
import type { TestResult } from '../types'

const auth = useAuthStore()
const displayName = ref(auth.user?.displayName || '')
const avatarUrl = ref(auth.user?.avatarUrl || '')
const history = ref<TestResult[]>([])
const error = ref('')
const notice = ref('')

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
  history.value = await testApi.history()
})
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">个人中心</p>
        <h1>账号资料与历史测试</h1>
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
        <h2>历史记录</h2>
        <div class="table-wrap">
          <table>
            <thead>
              <tr><th>类型</th><th>时间</th><th>摘要</th></tr>
            </thead>
            <tbody>
              <tr v-for="item in history" :key="item.id">
                <td>{{ item.type }}</td>
                <td>{{ new Date(item.createdAt).toLocaleString() }}</td>
                <td>{{ Object.values(item.scores).join(' / ') }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </section>
</template>

