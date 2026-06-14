<script setup lang="ts">
defineProps<{
  name: string
  score: number
  explanation: string
  impact: string
}>()
</script>

<template>
  <article class="card dimension-card">
    <div class="split">
      <h3>{{ name }}</h3>
      <strong>{{ score }} / 100</strong>
    </div>
    <div class="mini-meter"><span :style="{ width: `${score}%` }"></span></div>
    <p>{{ explanation }}</p>
    <p class="muted">{{ impact }}</p>
  </article>
</template>
