<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { recommendationApi } from '../api'
import type { Recommendation } from '../types'

const scenes = [
  { value: 'food', label: '餐饮' },
  { value: 'travel', label: '旅行' },
  { value: 'outfit', label: '穿搭' },
  { value: 'career', label: '生涯' }
]
const scene = ref('food')
const items = ref<Recommendation[]>([])
const comments = ref<Record<number, string>>({})
const error = ref('')
const notice = ref('')

async function load() {
  error.value = ''
  try {
    items.value = await recommendationApi.list(scene.value)
  } catch (err) {
    error.value = (err as Error).message
  }
}

async function feedback(item: Recommendation, rating: string) {
  error.value = ''
  notice.value = ''
  try {
    await recommendationApi.feedback(item.id, { rating, comment: comments.value[item.id] || '' })
    notice.value = '反馈已记录，后续推荐会调整权重'
    await load()
  } catch (err) {
    error.value = (err as Error).message
  }
}

watch(scene, load)
onMounted(load)
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">场景推荐</p>
        <h1>把性格画像变成生活建议</h1>
      </div>
      <div class="segmented">
        <button v-for="item in scenes" :key="item.value" :class="{ active: scene === item.value }" @click="scene = item.value">
          {{ item.label }}
        </button>
      </div>
    </header>
    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="notice" class="notice">{{ notice }}</div>
    <div class="grid two">
      <article v-for="item in items" :key="item.id" class="card">
        <div class="page-header" style="margin-bottom: 8px">
          <h2>{{ item.title }}</h2>
          <span class="score-pill">{{ item.score }}</span>
        </div>
        <p>{{ item.description }}</p>
        <p class="muted">标签：{{ item.tags.join(' / ') }}</p>
        <div class="field">
          <label>可选反馈</label>
          <input v-model="comments[item.id]" maxlength="100" placeholder="0-100 字" />
        </div>
        <div class="toolbar">
          <button class="primary" @click="feedback(item, 'like')">喜欢</button>
          <button class="secondary" @click="feedback(item, 'neutral')">一般</button>
          <button class="ghost" @click="feedback(item, 'dislike')">不喜欢</button>
        </div>
      </article>
    </div>
  </section>
</template>

