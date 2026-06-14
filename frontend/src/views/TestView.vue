<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import EmptyState from '../components/common/EmptyState.vue'
import LoadingState from '../components/common/LoadingState.vue'
import PageContainer from '../components/common/PageContainer.vue'
import { scaleOptions } from '../data/mockQuestions'
import type { TestModule, SurveyQuestion } from '../productTypes'
import { loadTestModules, saveAssessment } from '../services/testService'
import { loadProductState } from '../utils/storage'

const route = useRoute()
const router = useRouter()
const modules = ref<TestModule[]>([])
const answers = ref<Record<string, number>>({})
const currentIndex = ref(0)
const loading = ref(true)
const submitting = ref(false)
const error = ref('')
const notice = ref('')

const questions = computed(() => modules.value.flatMap((module) => module.questions))
const currentQuestion = computed<SurveyQuestion | undefined>(() => questions.value[currentIndex.value])
const answeredCount = computed(() => questions.value.filter((question) => answers.value[question.id]).length)
const totalProgress = computed(() => questions.value.length ? Math.round((answeredCount.value / questions.value.length) * 100) : 0)
const currentModule = computed(() => modules.value.find((module) => module.category === currentQuestion.value?.category))

function jumpToCategory(category: string) {
  const index = questions.value.findIndex((question) => question.category === category)
  if (index >= 0) currentIndex.value = index
}

function next() {
  error.value = ''
  if (!currentQuestion.value) return
  if (!answers.value[currentQuestion.value.id]) {
    error.value = '请先回答当前题目，再进入下一题。'
    return
  }
  currentIndex.value = Math.min(currentIndex.value + 1, questions.value.length - 1)
}

function prev() {
  error.value = ''
  currentIndex.value = Math.max(currentIndex.value - 1, 0)
}

async function submit() {
  error.value = ''
  notice.value = ''
  const missing = questions.value.length - answeredCount.value
  if (missing > 0) {
    error.value = `你还有 ${missing} 道题未完成，请完成后再生成报告。`
    return
  }
  if (!window.confirm('确认提交后将生成新的画像报告，并覆盖当前推荐依据。')) return
  submitting.value = true
  try {
    await saveAssessment(questions.value.map((question) => ({ questionId: question.id, value: answers.value[question.id] })))
    notice.value = '测试已提交，正在前往报告页。'
    setTimeout(() => router.push('/report'), 400)
  } catch {
    error.value = '提交失败，请稍后重试。'
  } finally {
    submitting.value = false
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    modules.value = await loadTestModules()
    answers.value = { ...loadProductState().answers }
    jumpToCategory((route.params.type as string) || 'personality')
  } catch {
    error.value = '题目加载失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

watch(() => route.params.type, (value) => jumpToCategory((value as string) || 'personality'))
onMounted(load)
</script>

<template>
  <PageContainer
    eyebrow="测试中心"
    title="完成四类测评，生成你的生活画像"
    description="请按真实感受选择 1-5 级量表。系统会用你的答案计算报告、推荐和双人适配结果。"
  >
    <template #actions>
      <span class="progress-label">总进度 {{ totalProgress }}%</span>
    </template>

    <div class="progress-track"><span :style="{ width: `${totalProgress}%` }"></span></div>
    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="notice" class="notice">{{ notice }}</div>
    <LoadingState v-if="loading" message="正在加载题库..." />

    <EmptyState
      v-else-if="!questions.length"
      title="题目加载失败，请稍后重试。"
      description="当前题库为空，请刷新页面或联系管理员补充题库。"
      action-label="刷新"
      action-to="/tests/personality"
    />

    <section v-else class="test-layout">
      <aside class="module-list">
        <button
          v-for="module in modules"
          :key="module.category"
          type="button"
          :class="{ active: currentModule?.category === module.category }"
          @click="jumpToCategory(module.category)"
        >
          <strong>{{ module.title }}</strong>
          <span>{{ module.description }}</span>
        </button>
      </aside>

      <article v-if="currentQuestion" class="panel question-panel">
        <div class="split">
          <p class="eyebrow">{{ currentModule?.title }}</p>
          <strong>{{ currentIndex + 1 }} / {{ questions.length }}</strong>
        </div>
        <h2>{{ currentQuestion.text }}</h2>
        <div class="scale-grid">
          <label v-for="option in scaleOptions" :key="option.value" :class="{ selected: answers[currentQuestion.id] === option.value }">
            <input v-model="answers[currentQuestion.id]" type="radio" :name="currentQuestion.id" :value="option.value" />
            <span>{{ option.value }}</span>
            <strong>{{ option.label }}</strong>
          </label>
        </div>
        <div class="toolbar question-actions">
          <button class="ghost" type="button" :disabled="currentIndex === 0" @click="prev">上一题</button>
          <button v-if="currentIndex < questions.length - 1" class="primary" type="button" @click="next">下一题</button>
          <button v-else class="primary" type="button" :disabled="submitting" @click="submit">
            {{ submitting ? '提交中...' : '生成报告' }}
          </button>
        </div>
      </article>
    </section>
  </PageContainer>
</template>
