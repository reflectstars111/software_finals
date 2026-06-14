<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import EmptyState from '../components/common/EmptyState.vue'
import LoadingState from '../components/common/LoadingState.vue'
import PageContainer from '../components/common/PageContainer.vue'
import RecommendationCard from '../components/recommendation/RecommendationCard.vue'
import type { FeedbackValue, PersonalizedRecommendation, RecommendationType } from '../productTypes'
import { listPersonalizedRecommendations, saveRecommendationFeedback } from '../services/recommendationService'
import { RECOMMENDATION_WEIGHTS } from '../utils/recommendation'
import { loadProductState } from '../utils/storage'

const tabs: Array<{ value: RecommendationType; label: string }> = [
  { value: 'food', label: '餐饮推荐' },
  { value: 'travel', label: '旅游推荐' },
  { value: 'social', label: '社交推荐' }
]

const active = ref<RecommendationType>('food')
const items = ref<PersonalizedRecommendation[]>([])
const submitted = ref<Record<string, FeedbackValue>>({})
const loading = ref(true)
const error = ref('')
const notice = ref('')
const hasPortrait = ref(false)

async function load() {
  loading.value = true
  error.value = ''
  hasPortrait.value = Boolean(loadProductState().portrait)
  try {
    items.value = await listPersonalizedRecommendations(active.value)
  } catch {
    error.value = '推荐加载失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

async function feedback(item: PersonalizedRecommendation, value: FeedbackValue) {
  await saveRecommendationFeedback(item.id, item.title, item.type, value)
  submitted.value[item.id] = value
  notice.value = value === 'dislike'
    ? '已收到反馈，后续将减少相似类型推荐。'
    : '已收到反馈，后续将优先推荐相似内容。'
  await load()
}

watch(active, load)
onMounted(load)
</script>

<template>
  <PageContainer
    eyebrow="生活推荐"
    title="把画像变成可以行动的生活建议"
    description="推荐得分 = 性格匹配分 × 40% + 场景偏好分 × 40% + 历史反馈分 × 20%。"
  >
    <template #actions>
      <div class="segmented">
        <button v-for="tab in tabs" :key="tab.value" :class="{ active: active === tab.value }" @click="active = tab.value">
          {{ tab.label }}
        </button>
      </div>
    </template>

    <div class="algorithm-line">
      <span>性格 {{ RECOMMENDATION_WEIGHTS.personality * 100 }}%</span>
      <span>偏好 {{ RECOMMENDATION_WEIGHTS.preference * 100 }}%</span>
      <span>反馈 {{ RECOMMENDATION_WEIGHTS.feedback * 100 }}%</span>
    </div>

    <div v-if="notice" class="notice">{{ notice }}</div>
    <div v-if="error" class="error">{{ error }}</div>
    <LoadingState v-if="loading" message="正在计算推荐排序..." />

    <EmptyState
      v-else-if="!hasPortrait"
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
      <RecommendationCard
        v-for="item in items"
        :key="item.id"
        :item="item"
        :submitted="submitted[item.id]"
        @feedback="feedback"
      />
    </section>
  </PageContainer>
</template>
