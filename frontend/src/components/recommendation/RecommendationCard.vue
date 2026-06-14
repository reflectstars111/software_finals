<script setup lang="ts">
import type { FeedbackValue, PersonalizedRecommendation } from '../../productTypes'

defineProps<{
  item: PersonalizedRecommendation
  submitted?: FeedbackValue
}>()

const emit = defineEmits<{
  feedback: [item: PersonalizedRecommendation, feedback: FeedbackValue]
}>()
</script>

<template>
  <article class="card recommendation-card">
    <div class="split">
      <div>
        <p class="eyebrow">{{ item.type === 'food' ? '餐饮推荐' : item.type === 'travel' ? '旅游推荐' : '社交推荐' }}</p>
        <h2>{{ item.title }}</h2>
      </div>
      <span class="score-pill">{{ item.matchScore }}%</span>
    </div>
    <p>{{ item.description }}</p>
    <div class="tag-row">
      <span v-for="tag in item.tags" :key="tag">{{ tag }}</span>
    </div>
    <div class="reason-box">
      <strong>推荐理由</strong>
      <p>{{ item.reason }}</p>
      <strong>适合你的原因</strong>
      <p>{{ item.fitReason }}</p>
    </div>
    <p v-if="submitted" class="notice compact">已记录反馈：{{ submitted }}</p>
    <div class="toolbar">
      <button class="primary" type="button" @click="emit('feedback', item, 'like')">喜欢</button>
      <button class="secondary" type="button" @click="emit('feedback', item, 'neutral')">一般</button>
      <button class="ghost" type="button" @click="emit('feedback', item, 'dislike')">不喜欢</button>
    </div>
  </article>
</template>
