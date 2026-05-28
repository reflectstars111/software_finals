<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const isShare = computed(() => route.path.startsWith('/share'))

const navItems = computed(() => [
  { path: '/', label: '工作台' },
  { path: '/tests/personality', label: '测试' },
  { path: '/report', label: '报告' },
  { path: '/recommendations', label: '推荐' },
  { path: '/match', label: '适配' },
  { path: '/profile', label: '我的' },
  ...(auth.isAdmin ? [{ path: '/admin', label: '后台' }] : [])
])

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <main class="app-shell" :class="{ 'share-shell': isShare }">
    <aside v-if="auth.isAuthed && !isShare" class="sidebar">
      <div class="brand">
        <div class="brand-mark">R</div>
        <div>
          <strong>性格雷达</strong>
          <span>生活指南</span>
        </div>
      </div>
      <nav class="nav-list">
        <RouterLink v-for="item in navItems" :key="item.path" :to="item.path">{{ item.label }}</RouterLink>
      </nav>
      <button class="ghost full" type="button" @click="logout">退出登录</button>
    </aside>
    <section class="content-area">
      <RouterView />
    </section>
  </main>
</template>

