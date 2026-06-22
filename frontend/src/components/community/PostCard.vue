<script setup lang="ts">
defineProps<{
  id: number
  authorName: string
  content: string
  domainTag: string
  styleTags: string[]
  likeCount: number
  commentCount: number
  favoriteCount: number
  compatibility: number
  showCompatibility: boolean
  createdAt: string
}>()

function formatTime(value: string) {
  return new Date(value).toLocaleDateString()
}
</script>

<template>
  <RouterLink :to="`/community/post/${id}`" class="card" style="display:block;cursor:pointer;color:inherit">
    <div class="split">
      <div>
        <p class="eyebrow">{{ authorName }} · {{ formatTime(createdAt) }}</p>
        <p style="margin:6px 0">{{ content.slice(0, 120) }}{{ content.length > 120 ? '...' : '' }}</p>
      </div>
      <span v-if="showCompatibility" class="score-pill" :title="`你的契合度：${compatibility}%`">{{ compatibility }}%</span>
    </div>
    <div class="tag-row">
      <span>{{ domainTag }}</span>
      <span v-for="tag in styleTags" :key="tag">{{ tag }}</span>
    </div>
    <div class="toolbar" style="color:var(--muted);font-size:13px">
      <span>❤ {{ likeCount }}</span>
      <span>💬 {{ commentCount }}</span>
      <span>⭐ {{ favoriteCount }}</span>
    </div>
  </RouterLink>
</template>
