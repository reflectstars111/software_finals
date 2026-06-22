<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import EmptyState from '../components/common/EmptyState.vue'
import LoadingState from '../components/common/LoadingState.vue'
import PageContainer from '../components/common/PageContainer.vue'
import type { Recommendation } from '../types'
import { listRecommendations, submitFeedback } from '../services/recommendationService'

const tabs: Array<{ value: string; label: string }> = [
  { value: 'food', label: '餐饮' },
  { value: 'travel', label: '旅行' },
  { value: 'social', label: '社交' },
  { value: 'outfit', label: '穿搭' },
  { value: 'career', label: '职业' },
  { value: 'community', label: '社区' }
]

const sceneLabels: Record<string, string> = {
  FOOD: '餐饮推荐',
  TRAVEL: '旅行推荐',
  SOCIAL: '社交推荐',
  OUTFIT: '穿搭推荐',
  CAREER: '职业推荐'
}

const active = ref('food')
const items = ref<Recommendation[]>([])
const submitted = ref<Record<number, string>>({})
const loading = ref(true)
const error = ref('')
const notice = ref('')

function switchTab(tab: string) {
  if (tab === 'community') {
    location.href = '/community'
    return
  }
  active.value = tab
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    items.value = await listRecommendations(active.value)
  } catch (err) {
    error.value = (err as Error).message || '推荐加载失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

async function feedback(item: Recommendation, rating: string) {
  try {
    await submitFeedback(item.id, rating)
    submitted.value[item.id] = rating
    notice.value = rating === 'DISLIKE'
      ? '已收到反馈，后续会减少相似类型推荐。'
      : '已收到反馈，后续会优先推荐相似内容。'
    await load()
  } catch (err) {
    error.value = (err as Error).message || '反馈提交失败。'
  }
}

function sceneLabel(scene: string) {
  return sceneLabels[scene] || '生活推荐'
}

function feedbackLabel(rating: string) {
  return rating === 'LIKE' ? '喜欢' : rating === 'NEUTRAL' ? '一般' : '不喜欢'
}

watch(active, load)
onMounted(load)
</script>

<template>
  <PageContainer
    eyebrow="生活推荐"
    title="把画像变成可以行动的生活建议"
    description="百分数表示当前推荐与你最近测评画像、历史反馈和后台规则的综合适配度。"
  >
    <template #actions>
      <div class="segmented">
        <button v-for="tab in tabs" :key="tab.value" :class="{ active: active === tab.value }" @click="switchTab(tab.value)">
          {{ tab.label }}
        </button>
      </div>
    </template>

    <div v-if="notice" class="notice">{{ notice }}</div>
    <div v-if="error" class="error">{{ error }}</div>
    <LoadingState v-if="loading" message="正在计算推荐排序..." />

    <EmptyState
      v-else-if="error.includes('请先完成')"
      title="请先完成基础性格测试"
      description="完成测评后，系统会根据你的画像、场景偏好和历史反馈给出推荐。"
      action-label="去完成测试"
      action-to="/tests/personality"
    />

    <EmptyState
      v-else-if="!items.length"
      title="暂无推荐数据"
      description="当前场景没有可用推荐，请切换分类或稍后重试。"
    />

    <section v-else class="grid two">
      <article v-for="item in items" :key="item.id" class="card recommendation-card">
        <div class="split">
          <div>
            <p class="eyebrow">{{ sceneLabel(item.scene) }}</p>
            <h2>{{ item.title }}</h2>
          </div>
          <span class="score-pill" title="综合适配度">{{ item.score }}%</span>
        </div>
        <p>{{ item.description }}</p>
        <div class="tag-row">
          <span v-for="tag in item.tags" :key="tag">{{ tag }}</span>
        </div>
        <p v-if="submitted[item.id]" class="notice compact">已记录反馈：{{ feedbackLabel(submitted[item.id]) }}</p>
        <div class="toolbar">
          <button class="primary" type="button" @click="feedback(item, 'LIKE')">喜欢</button>
          <button class="secondary" type="button" @click="feedback(item, 'NEUTRAL')">一般</button>
          <button class="ghost" type="button" @click="feedback(item, 'DISLIKE')">不喜欢</button>
        </div>
      </article>
    </section>
  </PageContainer>
</template>
