<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { adminApi } from '../api'
import type { AdminStats, Question, Recommendation } from '../types'

const stats = ref<AdminStats | null>(null)
const questions = ref<Question[]>([])
const recs = ref<Recommendation[]>([])
const feedback = ref<unknown[]>([])
const logs = ref<unknown[]>([])
const error = ref('')
const notice = ref('')

const questionForm = ref({
  type: 'personality',
  content: '',
  options: [
    { label: 'A', content: '', weights: { OPENNESS: 5, CONSCIENTIOUSNESS: 3, EXTRAVERSION: 3, AGREEABLENESS: 3, NEUROTICISM: 3 } },
    { label: 'B', content: '', weights: { OPENNESS: 3, CONSCIENTIOUSNESS: 5, EXTRAVERSION: 3, AGREEABLENESS: 3, NEUROTICISM: 3 } }
  ]
})

const recForm = ref({
  scene: 'food',
  title: '',
  description: '',
  tags: 'explore',
  baseScore: 60
})

async function load() {
  error.value = ''
  try {
    const [statsData, questionData, recData, feedbackData, logData] = await Promise.all([
      adminApi.stats(),
      adminApi.questions(),
      adminApi.recommendationItems(),
      adminApi.feedback(),
      adminApi.logs()
    ])
    stats.value = statsData
    questions.value = questionData
    recs.value = recData
    feedback.value = feedbackData
    logs.value = logData
  } catch (err) {
    error.value = (err as Error).message
  }
}

async function createQuestion() {
  error.value = ''
  notice.value = ''
  try {
    await adminApi.createQuestion({ ...questionForm.value, active: true })
    questionForm.value.content = ''
    questionForm.value.options.forEach((o) => (o.content = ''))
    notice.value = '题目已新增'
    await load()
  } catch (err) {
    error.value = (err as Error).message
  }
}

async function createRec() {
  error.value = ''
  notice.value = ''
  try {
    await adminApi.createRecommendation({
      scene: recForm.value.scene,
      title: recForm.value.title,
      description: recForm.value.description,
      tags: recForm.value.tags.split(',').map((tag) => tag.trim()).filter(Boolean),
      baseScore: recForm.value.baseScore,
      active: true
    })
    recForm.value.title = ''
    recForm.value.description = ''
    notice.value = '推荐项已新增'
    await load()
  } catch (err) {
    error.value = (err as Error).message
  }
}

async function disableQuestion(id: number) {
  await adminApi.deleteQuestion(id)
  await load()
}

async function disableRec(id: number) {
  await adminApi.deleteRecommendation(id)
  await load()
}

onMounted(load)
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">后台管理</p>
        <h1>题库、推荐项、反馈和日志</h1>
      </div>
      <button class="secondary" @click="load">刷新</button>
    </header>
    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="notice" class="notice">{{ notice }}</div>
    <div v-if="stats" class="grid five">
      <div class="card metric"><strong>{{ stats.users }}</strong><span>用户</span></div>
      <div class="card metric"><strong>{{ stats.questions }}</strong><span>题目</span></div>
      <div class="card metric"><strong>{{ stats.recommendations }}</strong><span>推荐</span></div>
      <div class="card metric"><strong>{{ stats.feedbacks }}</strong><span>反馈</span></div>
      <div class="card metric"><strong>{{ stats.matches }}</strong><span>适配</span></div>
    </div>

    <div class="grid two" style="margin-top: 16px">
      <section class="panel">
        <h2>新增题目</h2>
        <div class="field">
          <label>测试类型</label>
          <select v-model="questionForm.type">
            <option value="personality">基础性格</option>
            <option value="food">饮食偏好</option>
            <option value="travel">旅游偏好</option>
          </select>
        </div>
        <div class="field">
          <label>题目内容</label>
          <input v-model="questionForm.content" />
        </div>
        <div v-for="option in questionForm.options" :key="option.label" class="field">
          <label>选项 {{ option.label }}</label>
          <input v-model="option.content" />
        </div>
        <button class="primary" @click="createQuestion">保存题目</button>
      </section>

      <section class="panel">
        <h2>新增推荐项</h2>
        <div class="field">
          <label>场景</label>
          <select v-model="recForm.scene">
            <option value="food">餐饮</option>
            <option value="travel">旅行</option>
            <option value="outfit">穿搭</option>
            <option value="career">生涯</option>
          </select>
        </div>
        <div class="field"><label>标题</label><input v-model="recForm.title" /></div>
        <div class="field"><label>描述</label><textarea v-model="recForm.description"></textarea></div>
        <div class="field"><label>标签，逗号分隔</label><input v-model="recForm.tags" /></div>
        <div class="field"><label>基础分</label><input v-model.number="recForm.baseScore" type="number" min="0" max="100" /></div>
        <button class="primary" @click="createRec">保存推荐项</button>
      </section>
    </div>

    <section class="panel" style="margin-top: 16px">
      <h2>题库列表</h2>
      <div class="table-wrap">
        <table>
          <thead><tr><th>ID</th><th>类型</th><th>题目</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="q in questions" :key="q.id">
              <td>{{ q.id }}</td>
              <td>{{ q.type }}</td>
              <td>{{ q.content }}</td>
              <td>{{ q.active ? '启用' : '停用' }}</td>
              <td><button class="danger" @click="disableQuestion(q.id)">停用</button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="panel" style="margin-top: 16px">
      <h2>推荐项列表</h2>
      <div class="table-wrap">
        <table>
          <thead><tr><th>ID</th><th>场景</th><th>标题</th><th>标签</th><th>分数</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="r in recs" :key="r.id">
              <td>{{ r.id }}</td>
              <td>{{ r.scene }}</td>
              <td>{{ r.title }}</td>
              <td>{{ r.tags.join(', ') }}</td>
              <td>{{ r.score }}</td>
              <td><button class="danger" @click="disableRec(r.id)">停用</button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <div class="grid two" style="margin-top: 16px">
      <section class="panel">
        <h2>最近反馈</h2>
        <pre>{{ feedback }}</pre>
      </section>
      <section class="panel">
        <h2>操作日志</h2>
        <pre>{{ logs }}</pre>
      </section>
    </div>
  </section>
</template>

