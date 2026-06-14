<script setup lang="ts">
defineProps<{
  title: string
  items: string[]
}>()
</script>

<template>
  <article class="card">
    <h3>{{ title }}</h3>
    <p v-for="item in items" :key="item">{{ item }}</p>
  </article>
</template>
