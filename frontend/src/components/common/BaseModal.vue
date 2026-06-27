<script setup lang="ts">
import { computed, useAttrs } from 'vue'

withDefaults(defineProps<{
  open: boolean
  title?: string
  confirmText?: string
  cancelText?: string
  variant?: 'default' | 'danger'
}>(), {
  confirmText: '确认',
  cancelText: '取消',
  variant: 'default',
})

defineEmits<{
  close: []
  confirm: []
}>()

const attrs = useAttrs()
const showFooter = computed(() => typeof attrs.onConfirm === 'function')
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="open" class="modal-overlay" @click.self="$emit('close')">
        <div class="modal-panel">
          <div v-if="title" class="split modal-header-row">
            <h2 class="modal-title modal-header-title">{{ title }}</h2>
            <button class="ghost small" type="button" @click="$emit('close')" aria-label="关闭">&times;</button>
          </div>
          <slot />
          <div v-if="showFooter" class="toolbar modal-footer-bar">
            <button class="ghost" type="button" @click="$emit('close')">{{ cancelText }}</button>
            <button
              :class="variant === 'danger' ? 'danger' : 'primary'"
              type="button"
              @click="$emit('confirm')"
            >{{ confirmText }}</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-enter-active { transition: opacity 0.25s cubic-bezier(0.16, 1, 0.3, 1); }
.modal-leave-active { transition: opacity 0.2s cubic-bezier(0.16, 1, 0.3, 1); }
.modal-enter-from, .modal-leave-to { opacity: 0; }

.modal-header-row {
  margin-bottom: 16px;
}

.modal-header-title {
  margin-bottom: 0;
}

.modal-footer-bar {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
