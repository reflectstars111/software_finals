<script setup lang="ts">
import { Heart, MessageCircle, Star } from 'lucide-vue-next'

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
  <RouterLink :to="`/community/post/${id}`" class="card">
    <div class="split">
      <div>
        <p class="eyebrow">{{ authorName }} · {{ formatTime(createdAt) }}</p>
        <p class="post-excerpt">{{ content.slice(0, 120) }}{{ content.length > 120 ? '...' : '' }}</p>
      </div>
      <span v-if="showCompatibility" class="score-pill" :title="`你的契合度：${compatibility}%`">{{ compatibility }}%</span>
    </div>
    <div class="tag-row">
      <span>{{ domainTag }}</span>
      <span v-for="tag in styleTags" :key="tag">{{ tag }}</span>
    </div>
    <div class="post-meta">
      <span><Heart :size="14" class="icon-sm" /> {{ likeCount }}</span>
      <span><MessageCircle :size="14" class="icon-sm" /> {{ commentCount }}</span>
      <span><Star :size="14" class="icon-sm" /> {{ favoriteCount }}</span>
    </div>
  </RouterLink>
</template>
