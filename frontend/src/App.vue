<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppNav from './components/common/AppNav.vue'

const route = useRoute()
const hideChrome = computed(() => route.path === '/login' || route.path.startsWith('/share'))
</script>

<template>
  <div class="app-shell" :class="{ 'plain-shell': hideChrome }">
    <AppNav v-if="!hideChrome" />
    <main class="content-area">
      <RouterView />
    </main>
  </div>
</template>
