<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { adminApi } from '../api'
import PageContainer from '../components/common/PageContainer.vue'
import type { AdminDashboard, AdminUser, Question, Recommendation, RecommendationRule } from '../types'

const dashboard = ref<AdminDashboard | null>(null)
const users = ref<AdminUser[]>([])
const questions = ref<Question[]>([])
const recommendations = ref<Recommendation[]>([])
const rules = ref<RecommendationRule[]>([])
const error = ref('')
const notice = ref('')
const loading = ref(false)

const stats = computed(() => dashboard.value?.stats)

function formatTime(value?: string) {
  return value ? new Date(value).toLocaleString() : '暂无'
}

async function load() {
  error.value = ''
  loading.value = true
  try {
    const [dashboardData, userData, questionData, recData, ruleData] = await Promise.all([
      adminApi.dashboard(),
      adminApi.users(),
      adminApi.questions(),
      adminApi.recommendationItems(),
      adminApi.recommendationRules()
    ])
    dashboard.value = dashboardData
    users.value = userData
    questions.value = questionData
    recommendations.value = recData
    rules.value = ruleData
  } catch (err) {
    error.value = (err as Error).message
  } finally {
    loading.value = false
  }
}

async function toggleUser(user: AdminUser) {
  const updated = await adminApi.updateUser(user.id, { active: !user.active })
  users.value = users.value.map((item) => item.id === user.id ? updated : item)
  notice.value = `${updated.displayName} 已${updated.active ? '恢复' : '停用'}`
}

async function toggleRule(rule: RecommendationRule) {
  const updated = await adminApi.updateRecommendationRule(rule.id, { ...rule, active: !rule.active })
  rules.value = rules.value.map((item) => item.id === rule.id ? updated : item)
  notice.value = `推荐规则「${updated.label}」已${updated.active ? '启用' : '停用'}`
}

onMounted(load)
</script>

<template>
  <PageContainer
    eyebrow="后台管理"
    title="内容、用户和推荐规则管理"
    description="用于课程展示阶段快速查看系统数据规模，并管理用户状态和推荐规则。"
  >
    <template #actions>
      <button class="secondary" type="button" @click="load">刷新</button>
    </template>

    <div v-if="loading" class="notice">正在加载后台数据...</div>
    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="notice" class="notice">{{ notice }}</div>

    <section class="grid five admin-metrics">
      <div class="card metric"><strong>{{ stats?.users ?? 0 }}</strong><span>用户</span></div>
      <div class="card metric"><strong>{{ stats?.questions ?? 0 }}</strong><span>题目</span></div>
      <div class="card metric"><strong>{{ stats?.recommendations ?? 0 }}</strong><span>推荐项</span></div>
      <div class="card metric"><strong>{{ stats?.feedbacks ?? 0 }}</strong><span>反馈</span></div>
      <div class="card metric"><strong>{{ stats?.matches ?? 0 }}</strong><span>匹配</span></div>
    </section>

    <section class="grid two section-gap">
      <article class="panel">
        <h2>用户管理</h2>
        <div class="table-wrap">
          <table>
            <thead><tr><th>手机号</th><th>昵称</th><th>角色</th><th>状态</th><th>最近登录</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="user in users" :key="user.id">
                <td>{{ user.phone }}</td>
                <td>{{ user.displayName }}</td>
                <td>{{ user.role }}</td>
                <td>{{ user.active ? '启用' : '停用' }}</td>
                <td>{{ formatTime(user.lastLoginAt) }}</td>
                <td><button class="secondary" type="button" @click="toggleUser(user)">{{ user.active ? '停用' : '恢复' }}</button></td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>

      <article class="panel">
        <h2>推荐规则</h2>
        <div class="table-wrap">
          <table>
            <thead><tr><th>标签</th><th>名称</th><th>权重</th><th>状态</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="rule in rules" :key="rule.id">
                <td>{{ rule.tag }}</td>
                <td>{{ rule.label }}</td>
                <td>{{ rule.weight }}</td>
                <td>{{ rule.active ? '启用' : '停用' }}</td>
                <td><button class="secondary" type="button" @click="toggleRule(rule)">{{ rule.active ? '停用' : '启用' }}</button></td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>
    </section>

    <section class="grid two section-gap">
      <article class="panel">
        <h2>题库概览</h2>
        <div class="table-wrap">
          <table>
            <thead><tr><th>类型</th><th>题目</th><th>选项数</th><th>状态</th></tr></thead>
            <tbody>
              <tr v-for="question in questions" :key="question.id">
                <td>{{ question.type }}</td>
                <td>{{ question.content }}</td>
                <td>{{ question.options.length }}</td>
                <td>{{ question.active ? '启用' : '停用' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>

      <article class="panel">
        <h2>推荐库概览</h2>
        <div class="table-wrap">
          <table>
            <thead><tr><th>场景</th><th>标题</th><th>标签</th><th>基础分</th></tr></thead>
            <tbody>
              <tr v-for="item in recommendations" :key="item.id">
                <td>{{ item.scene }}</td>
                <td>{{ item.title }}</td>
                <td>{{ item.tags.join(' / ') }}</td>
                <td>{{ item.baseScore ?? item.score }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>
    </section>
  </PageContainer>
</template>
