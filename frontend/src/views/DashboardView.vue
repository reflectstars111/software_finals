<script setup lang="ts">
import { onMounted, ref } from 'vue'
import EmptyState from '../components/common/EmptyState.vue'
import LoadingState from '../components/common/LoadingState.vue'
import PageContainer from '../components/common/PageContainer.vue'
import { useAuthStore } from '../stores/auth'
import { getProfileData } from '../services/profileService'
import { formatTime } from '../utils/format'
import { getErrorMessage } from '../utils/errors'

const auth = useAuthStore()
const testCount = ref(0)
const feedbackCount = ref(0)
const matchCount = ref(0)
const shareCount = ref(0)
const hasReport = ref(false)
const lastTestAt = ref('')
const loading = ref(true)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await getProfileData()
    testCount.value = data.testHistory.length
    feedbackCount.value = data.feedbacks.length
    matchCount.value = data.matches.length
    shareCount.value = data.shares.filter((s) => s.active).length
    hasReport.value = data.reportSnapshots.length > 0
    lastTestAt.value = data.testHistory[0]?.createdAt || ''
  } catch (err) {
    error.value = getErrorMessage(err, '数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <PageContainer
    eyebrow="工作台"
    :title="`${auth.user?.displayName || '你好'}，这里是你的生活指南概览`"
    description="快速查看最近测试、报告、推荐反馈和双人适配状态。"
  >
    <template #actions>
      <button class="secondary" type="button" @click="load">刷新</button>
    </template>

    <div v-if="error" class="error">{{ error }}</div>
    <LoadingState v-if="loading" message="正在加载数据..." />

    <template v-else>
      <section class="grid four">
        <div class="card metric"><strong>{{ testCount }}</strong><span>测试记录</span></div>
        <div class="card metric"><strong>{{ feedbackCount }}</strong><span>推荐反馈</span></div>
        <div class="card metric"><strong>{{ matchCount }}</strong><span>适配报告</span></div>
        <div class="card metric"><strong>{{ shareCount }}</strong><span>有效分享</span></div>
      </section>

      <section class="panel section-gap">
        <div class="split">
          <div>
            <p class="eyebrow">最近状态</p>
            <h2>{{ hasReport ? '画像已经生成' : '还没有完成画像测试' }}</h2>
            <p class="muted">最近一次测试：{{ formatTime(lastTestAt) }}</p>
          </div>
          <RouterLink class="button" :to="hasReport ? '/report' : '/tests/personality'">
            {{ hasReport ? '查看报告' : '开始测试' }}
          </RouterLink>
        </div>
      </section>

      <section class="grid three section-gap">
        <RouterLink class="card action-card" to="/tests/personality">
          <h2>完成测评</h2>
          <p class="muted">性格、饮食、旅游和社交倾向一次完成。</p>
        </RouterLink>
        <RouterLink class="card action-card" to="/recommendations">
          <h2>获取推荐</h2>
          <p class="muted">查看餐饮、旅游和社交推荐，并提交反馈。</p>
        </RouterLink>
        <RouterLink class="card action-card" to="/match">
          <h2>双人适配</h2>
          <p class="muted">生成邀请码，查看授权后的关系分析。</p>
        </RouterLink>
      </section>
    </template>
  </PageContainer>
</template>
