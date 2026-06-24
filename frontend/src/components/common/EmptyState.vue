<script setup lang="ts">
import { CircleAlert } from 'lucide-vue-next'

defineProps<{
  title: string
  description: string
  actionLabel?: string
  actionTo?: string
  icon?: string
}>()
</script>

<template>
  <section class="empty-state">
    <slot name="icon">
      <CircleAlert v-if="!icon" :size="32" class="empty-icon" />
    </slot>
    <h2>{{ title }}</h2>
    <p>{{ description }}</p>
    <RouterLink v-if="actionLabel && actionTo" class="button" :to="actionTo">{{ actionLabel }}</RouterLink>
  </section>
</template>

<style scoped>
.empty-icon {
  color: var(--muted);
  opacity: 0.5;
}
.empty-state {
  border-color: var(--line);
}
</style>
