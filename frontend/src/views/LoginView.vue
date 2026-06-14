<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const mode = ref<'login' | 'register'>('login')
const phone = ref('13900000001')
const password = ref('User123456')
const displayName = ref('新用户')
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  loading.value = true
  try {
    if (mode.value === 'login') {
      await auth.login(phone.value, password.value)
    } else {
      await auth.register(phone.value, password.value, displayName.value)
    }
    router.push('/')
  } catch (err) {
    error.value = (err as Error).message
  } finally {
    loading.value = false
  }
}

function useAdmin() {
  mode.value = 'login'
  phone.value = '13800000000'
  password.value = 'Admin@123456'
}
</script>

<template>
  <section class="auth-page">
    <div class="auth-box panel">
      <div class="auth-title">
        <p class="eyebrow">性格雷达·生活指南</p>
        <h1>{{ mode === 'login' ? '登录' : '注册' }}</h1>
        <p class="muted">默认演示用户：13900000001 / User123456</p>
      </div>
      <div class="segmented full auth-mode-switch">
        <button type="button" :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
        <button type="button" :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
      </div>
      <div v-if="error" class="error">{{ error }}</div>
      <form @submit.prevent="submit">
        <div class="field">
          <label>手机号</label>
          <input v-model="phone" maxlength="11" placeholder="11 位手机号" />
        </div>
        <div class="field">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="6-16 位密码" />
        </div>
        <div v-if="mode === 'register'" class="field">
          <label>昵称</label>
          <input v-model="displayName" placeholder="你的展示昵称" />
        </div>
        <button class="primary full" :disabled="loading" type="submit">{{ loading ? '处理中...' : '进入系统' }}</button>
      </form>
      <button class="ghost full login-admin" type="button" @click="useAdmin">使用管理员账号</button>
      <RouterLink class="ghost full login-back" to="/">返回首页</RouterLink>
    </div>
  </section>
</template>
