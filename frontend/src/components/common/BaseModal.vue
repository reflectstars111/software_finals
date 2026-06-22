<script setup lang="ts">
defineProps<{
  open: boolean
  title?: string
}>()

defineEmits<{
  close: []
}>()
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-panel">
        <div v-if="title" class="split modal-header-row">
          <h2 class="modal-title modal-header-title">{{ title }}</h2>
          <button class="ghost small" type="button" @click="$emit('close')" aria-label="关闭">&times;</button>
        </div>
        <slot />
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.modal-header-row {
  margin-bottom: 16px;
}

.modal-header-title {
  margin-bottom: 0;
}
</style>
