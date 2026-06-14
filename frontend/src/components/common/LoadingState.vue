<script setup lang="ts">
defineProps<{
  message?: string
}>()
</script>

<template>
  <section class="loading-state" aria-busy="true">
    <div class="loading-spinner"></div>
    <p>{{ message || '正在加载...' }}</p>
  </section>
</template>
