<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { adminApi } from '../api'
import type { AdminDashboard, AdminUser, Question, Recommendation, RecommendationRule } from '../types'

type FeedbackRow = {
  id: number
  userPhone: string
  itemTitle: string
  scene: string
  rating: string
  comment?: string
  createdAt: string
}

type LogRow = {
  id: number
  adminPhone: string
  action: string
  detail: string
  createdAt: string
}

const dimensions = ['OPENNESS', 'CONSCIENTIOUSNESS', 'EXTRAVERSION', 'AGREEABLENESS', 'NEUROTICISM']
const testTypes = [
  { value: 'personality', label: '基础性格' },
  { value: 'food', label: '饮食偏好' },
  { value: 'travel', label: '旅游偏好' }
]
const scenes = [
  { value: 'food', label: '餐饮' },
  { value: 'travel', label: '旅行' },
  { value: 'outfit', label: '穿搭' },
  { value: 'career', label: '生涯' }
]

const dashboard = ref<AdminDashboard | null>(null)
const questions = ref<Question[]>([])
const recs = ref<Recommendation[]>([])
const feedback = ref<FeedbackRow[]>([])
const logs = ref<LogRow[]>([])
const users = ref<AdminUser[]>([])
const rules = ref<RecommendationRule[]>([])
const error = ref('')
const notice = ref('')
const loading = ref(false)

const questionForm = ref({
  id: 0,
  type: 'personality',
  content: '',
  active: true,
  options: [
    { label: 'A', content: '', weights: defaultWeights('OPENNESS') },
    { label: 'B', content: '', weights: defaultWeights('CONSCIENTIOUSNESS') }
  ]
})

const recForm = ref({
  id: 0,
  scene: 'food',
  title: '',
  description: '',
  tags: 'explore',
  baseScore: 60,
  active: true
})

const ruleForm = ref({
  id: 0,
  tag: '',
  label: '',
  weight: 0,
  active: true
})

const stats = computed(() => dashboard.value?.stats)

function defaultWeights(primary: string) {
  return Object.fromEntries(dimensions.map((dimension) => [dimension, dimension === primary ? 5 : 3])) as Record<string, number>
}

function formatTime(value?: string) {
  return value ? new Date(value).toLocaleString() : '暂无'
}

async function load() {
  error.value = ''
  loading.value = true
  try {
    const [dashboardData, questionData, recData, feedbackData, logData, userData, ruleData] = await Promise.all([
      adminApi.dashboard(),
      adminApi.questions(),
      adminApi.recommendationItems(),
      adminApi.feedback(),
      adminApi.logs(),
      adminApi.users(),
      adminApi.recommendationRules()
    ])
    dashboard.value = dashboardData
    questions.value = questionData
    recs.value = recData
    feedback.value = feedbackData as FeedbackRow[]
    logs.value = logData as LogRow[]
    users.value = userData
    rules.value = ruleData
  } catch (err) {
    error.value = (err as Error).message
  } finally {
    loading.value = false
  }
}

function resetQuestionForm() {
  questionForm.value = {
    id: 0,
    type: 'personality',
    content: '',
    active: true,
    options: [
      { label: 'A', content: '', weights: defaultWeights('OPENNESS') },
      { label: 'B', content: '', weights: defaultWeights('CONSCIENTIOUSNESS') }
    ]
  }
}

function editQuestion(question: Question) {
  questionForm.value = {
    id: question.id,
    type: question.type,
    content: question.content,
    active: question.active,
    options: question.options.map((option) => ({
      label: option.label,
      content: option.content,
      weights: { ...option.weights }
    }))
  }
}

function addOption() {
  questionForm.value.options.push({
    label: String.fromCharCode(65 + questionForm.value.options.length),
    content: '',
    weights: defaultWeights('OPENNESS')
  })
}

async function saveQuestion() {
  error.value = ''
  notice.value = ''
  try {
    const payload = { ...questionForm.value }
    if (questionForm.value.id) {
      await adminApi.updateQuestion(questionForm.value.id, payload)
      notice.value = '题目已更新'
    } else {
      await adminApi.createQuestion(payload)
      notice.value = '题目已新增'
    }
    resetQuestionForm()
    await load()
  } catch (err) {
    error.value = (err as Error).message
  }
}

function editRec(item: Recommendation) {
  recForm.value = {
    id: item.id,
    scene: item.scene,
    title: item.title,
    description: item.description,
    tags: item.tags.join(', '),
    baseScore: item.baseScore ?? item.score,
    active: item.active ?? true
  }
}

function resetRecForm() {
  recForm.value = { id: 0, scene: 'food', title: '', description: '', tags: 'explore', baseScore: 60, active: true }
}

async function saveRec() {
  error.value = ''
  notice.value = ''
  const payload = {
    scene: recForm.value.scene,
    title: recForm.value.title,
    description: recForm.value.description,
    tags: recForm.value.tags.split(',').map((tag) => tag.trim()).filter(Boolean),
    baseScore: recForm.value.baseScore,
    active: recForm.value.active
  }
  try {
    if (recForm.value.id) {
      await adminApi.updateRecommendation(recForm.value.id, payload)
      notice.value = '推荐项已更新'
    } else {
      await adminApi.createRecommendation(payload)
      notice.value = '推荐项已新增'
    }
    resetRecForm()
    await load()
  } catch (err) {
    error.value = (err as Error).message
  }
}

function editRule(rule: RecommendationRule) {
  ruleForm.value = { ...rule }
}

function resetRuleForm() {
  ruleForm.value = { id: 0, tag: '', label: '', weight: 0, active: true }
}

async function saveRule() {
  error.value = ''
  notice.value = ''
  const payload = {
    tag: ruleForm.value.tag,
    label: ruleForm.value.label,
    weight: ruleForm.value.weight,
    active: ruleForm.value.active
  }
  try {
    if (ruleForm.value.id) {
      await adminApi.updateRecommendationRule(ruleForm.value.id, payload)
      notice.value = '推荐规则已更新'
    } else {
      await adminApi.createRecommendationRule(payload)
      notice.value = '推荐规则已新增'
    }
    resetRuleForm()
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

async function deleteRule(id: number) {
  await adminApi.deleteRecommendationRule(id)
  await load()
}

async function toggleUser(user: AdminUser) {
  await adminApi.updateUser(user.id, { active: !user.active })
  await load()
}

onMounted(load)
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">后台管理</p>
        <h1>产品运营控制台</h1>
        <p class="muted">维护题库、推荐项、推荐规则、用户状态、反馈和操作日志。</p>
      </div>
      <button class="secondary" :disabled="loading" @click="load">{{ loading ? '刷新中' : '刷新' }}</button>
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

    <div v-if="dashboard" class="grid four" style="margin-top: 16px">
      <div class="card"><h3>测试分布</h3><p class="muted">{{ dashboard.testsByType }}</p></div>
      <div class="card"><h3>反馈分布</h3><p class="muted">{{ dashboard.feedbackByRating }}</p></div>
      <div class="card"><h3>场景推荐</h3><p class="muted">{{ dashboard.recommendationsByScene }}</p></div>
      <div class="card"><h3>有效分享</h3><p class="muted">{{ dashboard.activeShares }}</p></div>
    </div>

    <div class="grid two" style="margin-top: 16px">
      <section class="panel">
        <h2>{{ questionForm.id ? '编辑题目' : '新增题目' }}</h2>
        <div class="field">
          <label>测试类型</label>
          <select v-model="questionForm.type">
            <option v-for="item in testTypes" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
        </div>
        <div class="field"><label>题目内容</label><input v-model="questionForm.content" /></div>
        <label class="check-line"><input v-model="questionForm.active" type="checkbox" /> 启用题目</label>
        <div v-for="option in questionForm.options" :key="option.label" class="option-editor">
          <div class="grid two">
            <div class="field"><label>选项标识</label><input v-model="option.label" /></div>
            <div class="field"><label>选项内容</label><input v-model="option.content" /></div>
          </div>
          <div class="weights-grid">
            <div v-for="dimension in dimensions" :key="dimension" class="field">
              <label>{{ dimension }}</label>
              <input v-model.number="option.weights[dimension]" type="number" min="0" max="10" />
            </div>
          </div>
        </div>
        <div class="toolbar">
          <button class="secondary" type="button" @click="addOption">增加选项</button>
          <button class="primary" type="button" @click="saveQuestion">保存题目</button>
          <button v-if="questionForm.id" class="ghost" type="button" @click="resetQuestionForm">取消编辑</button>
        </div>
      </section>

      <section class="panel">
        <h2>{{ recForm.id ? '编辑推荐项' : '新增推荐项' }}</h2>
        <div class="field">
          <label>场景</label>
          <select v-model="recForm.scene">
            <option v-for="item in scenes" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
        </div>
        <div class="field"><label>标题</label><input v-model="recForm.title" /></div>
        <div class="field"><label>描述</label><textarea v-model="recForm.description"></textarea></div>
        <div class="field"><label>标签，逗号分隔</label><input v-model="recForm.tags" /></div>
        <div class="field"><label>基础分</label><input v-model.number="recForm.baseScore" type="number" min="0" max="100" /></div>
        <label class="check-line"><input v-model="recForm.active" type="checkbox" /> 启用推荐项</label>
        <div class="toolbar">
          <button class="primary" type="button" @click="saveRec">保存推荐项</button>
          <button v-if="recForm.id" class="ghost" type="button" @click="resetRecForm">取消编辑</button>
        </div>
      </section>
    </div>

    <section class="panel" style="margin-top: 16px">
      <h2>推荐规则</h2>
      <div class="grid four">
        <div class="field"><label>标签</label><input v-model="ruleForm.tag" /></div>
        <div class="field"><label>名称</label><input v-model="ruleForm.label" /></div>
        <div class="field"><label>权重</label><input v-model.number="ruleForm.weight" type="number" min="-30" max="30" /></div>
        <div class="field"><label>状态</label><select v-model="ruleForm.active"><option :value="true">启用</option><option :value="false">停用</option></select></div>
      </div>
      <div class="toolbar" style="margin-bottom: 12px">
        <button class="primary" type="button" @click="saveRule">保存规则</button>
        <button v-if="ruleForm.id" class="ghost" type="button" @click="resetRuleForm">取消编辑</button>
      </div>
      <div class="table-wrap">
        <table>
          <thead><tr><th>标签</th><th>名称</th><th>权重</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="rule in rules" :key="rule.id">
              <td>{{ rule.tag }}</td>
              <td>{{ rule.label }}</td>
              <td>{{ rule.weight }}</td>
              <td>{{ rule.active ? '启用' : '停用' }}</td>
              <td class="row-actions"><button class="secondary" @click="editRule(rule)">编辑</button><button class="danger" @click="deleteRule(rule.id)">删除</button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="panel" style="margin-top: 16px">
      <h2>用户管理</h2>
      <div class="table-wrap">
        <table>
          <thead><tr><th>手机号</th><th>昵称</th><th>角色</th><th>状态</th><th>最近登录</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.phone }}</td>
              <td>{{ user.displayName }}</td>
              <td>{{ user.role }}</td>
              <td>{{ user.active ? '正常' : '停用' }}</td>
              <td>{{ formatTime(user.lastLoginAt) }}</td>
              <td><button class="secondary" @click="toggleUser(user)">{{ user.active ? '停用' : '恢复' }}</button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <div class="grid two" style="margin-top: 16px">
      <section class="panel">
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
                <td class="row-actions"><button class="secondary" @click="editQuestion(q)">编辑</button><button class="danger" @click="disableQuestion(q.id)">停用</button></td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel">
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
                <td class="row-actions"><button class="secondary" @click="editRec(r)">编辑</button><button class="danger" @click="disableRec(r.id)">停用</button></td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <div class="grid two" style="margin-top: 16px">
      <section class="panel">
        <h2>最近反馈</h2>
        <div class="table-wrap">
          <table>
            <thead><tr><th>用户</th><th>推荐项</th><th>评分</th><th>评论</th><th>时间</th></tr></thead>
            <tbody>
              <tr v-for="item in feedback" :key="item.id">
                <td>{{ item.userPhone }}</td>
                <td>{{ item.itemTitle }}</td>
                <td>{{ item.rating }}</td>
                <td>{{ item.comment || '无' }}</td>
                <td>{{ formatTime(item.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
      <section class="panel">
        <h2>操作日志</h2>
        <div class="table-wrap">
          <table>
            <thead><tr><th>管理员</th><th>动作</th><th>详情</th><th>时间</th></tr></thead>
            <tbody>
              <tr v-for="item in logs" :key="item.id">
                <td>{{ item.adminPhone }}</td>
                <td>{{ item.action }}</td>
                <td>{{ item.detail }}</td>
                <td>{{ formatTime(item.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </section>
</template>
