<script setup lang="ts">
defineProps<{
  message?: string
  inline?: boolean
}>()
</script>

<template>
  <section class="loading-state" :class="{ 'loading-inline': inline }" aria-busy="true">
    <div class="loading-spinner"></div>
    <p>{{ message || '正在加载...' }}</p>
  </section>
</template>

<style scoped>
.loading-spinner {
  border-color: rgba(108, 204, 160, 0.12);
  border-top-color: var(--blip);
  position: relative;
}
.loading-spinner::before {
  content: '';
  position: absolute;
  inset: -6px;
  border-radius: 50%;
  box-shadow: var(--glow-blip);
}
.loading-inline {
  padding: 12px 16px;
  border: 0;
}
</style>
