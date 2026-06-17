<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import EmptyState from '../components/common/EmptyState.vue'
import LoadingState from '../components/common/LoadingState.vue'
import PageContainer from '../components/common/PageContainer.vue'
import type { Recommendation } from '../types'
import { listRecommendations, submitFeedback } from '../services/recommendationService'

const tabs: Array<{ value: string; label: string }> = [
  { value: 'food', label: '餐饮推荐' },
  { value: 'travel', label: '旅游推荐' },
  { value: 'social', label: '社交推荐' },
  { value: 'outfit', label: '穿搭推荐' }
]

const active = ref('food')
const items = ref<Recommendation[]>([])
const submitted = ref<Record<number, string>>({})
const loading = ref(true)
const error = ref('')
const notice = ref('')

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
      ? '已收到反馈，后续将减少相似类型推荐。'
      : '已收到反馈，后续将优先推荐相似内容。'
    await load()
  } catch (err) {
    error.value = (err as Error).message || '反馈提交失败'
  }
}

watch(active, load)
onMounted(load)
</script>

<template>
  <PageContainer
    eyebrow="生活推荐"
    title="把画像变成可以行动的生活建议"
    description="得分基于基础分 + 标签偏好 + 规则权重 + 性格维度信号调整。"
  >
    <template #actions>
      <div class="segmented">
        <button v-for="tab in tabs" :key="tab.value" :class="{ active: active === tab.value }" @click="active = tab.value">
          {{ tab.label }}
        </button>
      </div>
    </template>

    <div v-if="notice" class="notice">{{ notice }}</div>
    <div v-if="error" class="error">{{ error }}</div>
    <LoadingState v-if="loading" message="正在计算推荐排序..." />

    <EmptyState
      v-else-if="error.includes('请先完成')"
      title="请先完成测试，系统将根据你的画像生成个性化推荐。"
      description="完成测评后，系统会根据性格、场景偏好和历史反馈给出可解释推荐。"
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
            <p class="eyebrow">{{ item.scene === 'FOOD' ? '餐饮推荐' : item.scene === 'TRAVEL' ? '旅游推荐' : '社交推荐' }}</p>
            <h2>{{ item.title }}</h2>
          </div>
          <span class="score-pill">{{ item.score }}%</span>
        </div>
        <p>{{ item.description }}</p>
        <div class="tag-row">
          <span v-for="tag in item.tags" :key="tag">{{ tag }}</span>
        </div>
        <p v-if="submitted[item.id]" class="notice compact">已记录反馈：{{ submitted[item.id] }}</p>
        <div class="toolbar">
          <button class="primary" type="button" @click="feedback(item, 'LIKE')">喜欢</button>
          <button class="secondary" type="button" @click="feedback(item, 'NEUTRAL')">一般</button>
          <button class="ghost" type="button" @click="feedback(item, 'DISLIKE')">不喜欢</button>
        </div>
      </article>
    </section>
  </PageContainer>
</template>
