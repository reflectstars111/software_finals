<script setup lang="ts">
defineProps<{
  title: string
  description: string
  actionLabel?: string
  actionTo?: string
}>()
</script>

<template>
  <section class="empty-state">
    <h2>{{ title }}</h2>
    <p>{{ description }}</p>
    <RouterLink v-if="actionLabel && actionTo" class="button" :to="actionTo">{{ actionLabel }}</RouterLink>
  </section>
</template>
