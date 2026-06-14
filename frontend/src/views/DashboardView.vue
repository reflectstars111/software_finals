<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageContainer from '../components/common/PageContainer.vue'
import { useAuthStore } from '../stores/auth'
import { loadProductState } from '../utils/storage'
import type { ProductState } from '../productTypes'

const auth = useAuthStore()
const state = ref<ProductState | null>(null)

function load() {
  state.value = loadProductState()
}

function formatTime(value?: string) {
  return value ? new Date(value).toLocaleString() : '暂无记录'
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

    <section class="grid four">
      <div class="card metric"><strong>{{ state?.testHistory.length || 0 }}</strong><span>测试记录</span></div>
      <div class="card metric"><strong>{{ state?.feedbacks.length || 0 }}</strong><span>推荐反馈</span></div>
      <div class="card metric"><strong>{{ state?.matchResults.length || 0 }}</strong><span>适配报告</span></div>
      <div class="card metric"><strong>{{ state?.invites.filter((item) => item.status === 'active').length || 0 }}</strong><span>有效授权</span></div>
    </section>

    <section class="panel section-gap">
      <div class="split">
        <div>
          <p class="eyebrow">最近状态</p>
          <h2>{{ state?.portrait ? '画像已经生成' : '还没有完成画像测试' }}</h2>
          <p class="muted">最近一次测试：{{ formatTime(state?.lastTestAt) }}</p>
        </div>
        <RouterLink class="button" :to="state?.portrait ? '/report' : '/tests/personality'">
          {{ state?.portrait ? '查看报告' : '开始测试' }}
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
  </PageContainer>
</template>
