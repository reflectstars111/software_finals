<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { testApi } from '../api'
import type { Question } from '../types'

const route = useRoute()
const router = useRouter()
const type = computed(() => (route.params.type as string) || 'personality')
const labels: Record<string, string> = { personality: '基础性格', food: '饮食偏好', travel: '旅游偏好' }
const questions = ref<Question[]>([])
const answers = ref<Record<number, number>>({})
const error = ref('')
const notice = ref('')
const loading = ref(false)

async function load() {
  error.value = ''
  questions.value = await testApi.questions(type.value)
}

async function submit() {
  error.value = ''
  notice.value = ''
  if (questions.value.some((q) => !answers.value[q.id])) {
    error.value = '请完成所有题目后再提交'
    return
  }
  loading.value = true
  try {
    await testApi.submit({
      type: type.value,
      answers: questions.value.map((q) => ({ questionId: q.id, optionIds: [answers.value[q.id]] }))
    })
    notice.value = '测试已保存，正在前往报告页'
    setTimeout(() => router.push('/report'), 500)
  } catch (err) {
    error.value = (err as Error).message
  } finally {
    loading.value = false
  }
}

watch(type, load)
onMounted(load)
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">测试答题</p>
        <h1>{{ labels[type] }}</h1>
        <p class="muted">单选即可完成，提交后系统会重新计算对应画像。</p>
      </div>
      <div class="segmented">
        <RouterLink class="button" to="/tests/personality">性格</RouterLink>
        <RouterLink class="button" to="/tests/food">饮食</RouterLink>
        <RouterLink class="button" to="/tests/travel">旅游</RouterLink>
      </div>
    </header>
    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="notice" class="notice">{{ notice }}</div>
    <form class="grid" @submit.prevent="submit">
      <section v-for="(question, index) in questions" :key="question.id" class="panel">
        <h2>{{ index + 1 }}. {{ question.content }}</h2>
        <div class="grid two">
          <label v-for="option in question.options" :key="option.id" class="card">
            <input v-model="answers[question.id]" type="radio" :name="String(question.id)" :value="option.id" />
            <strong>{{ option.label }}. {{ option.content }}</strong>
          </label>
        </div>
      </section>
      <button class="primary" type="submit" :disabled="loading">{{ loading ? '提交中' : '提交测试' }}</button>
    </form>
  </section>
</template>

