<script setup lang="ts">
import { onMounted, ref } from 'vue'
import EmptyState from '../components/common/EmptyState.vue'
import PageContainer from '../components/common/PageContainer.vue'
import type { ProductState } from '../productTypes'
import { clearMyPortraitData, getProfileProductData, revokeAuthorization } from '../services/profileService'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const state = ref<ProductState | null>(null)
const notice = ref('')
const error = ref('')

function formatTime(value?: string) {
  return value ? new Date(value).toLocaleString() : '暂无记录'
}

async function load() {
  state.value = await getProfileProductData()
}

async function revoke(code: string) {
  await revokeAuthorization(code)
  notice.value = '匹配授权已撤销。'
  await load()
}

async function clearData() {
  error.value = ''
  const ok = window.confirm('确认清除后，你的测试结果、推荐记录和匹配授权将被删除。该操作不可恢复。')
  if (!ok) return
  await clearMyPortraitData()
  notice.value = '你的画像数据、推荐反馈和匹配授权已清除。'
  await load()
}

function logout() {
  auth.logout()
  location.href = '/'
}

onMounted(load)
</script>

<template>
  <PageContainer
    eyebrow="个人中心"
    title="管理你的画像、反馈、匹配授权和隐私数据"
    description="你的测试答案仅用于生成个人报告、推荐结果和授权后的双人适配分析。"
  >
    <div v-if="notice" class="notice">{{ notice }}</div>
    <div v-if="error" class="error">{{ error }}</div>

    <section class="grid two">
      <article class="panel profile-card">
        <div class="avatar">{{ auth.user?.displayName?.slice(0, 1) || '我' }}</div>
        <div>
          <h2>{{ auth.user?.displayName || '本地用户' }}</h2>
          <p class="muted">{{ auth.user?.phone || '未绑定手机号' }}</p>
          <p>最近一次测试时间：{{ formatTime(state?.lastTestAt) }}</p>
        </div>
        <div class="toolbar">
          <RouterLink class="button" to="/tests/personality">重新测试</RouterLink>
          <button class="danger" type="button" @click="clearData">清除我的画像数据</button>
          <button class="ghost" type="button" @click="logout">退出登录</button>
        </div>
      </article>

      <article class="panel">
        <h2>隐私说明</h2>
        <p>
          测试数据仅本人可见。未经授权，其他用户无法查看你的完整测试结果；
          双人适配需要邀请码授权，且只展示维度差异和相处建议。
        </p>
      </article>
    </section>

    <section class="grid two">
      <article class="panel">
        <h2>历史测试记录</h2>
        <EmptyState
          v-if="!state?.testHistory.length"
          title="暂无历史测试"
          description="完成测试后，这里会记录测试时间、类型和综合画像。"
          action-label="去测试"
          action-to="/tests/personality"
        />
        <div v-else class="table-wrap">
          <table>
            <thead><tr><th>测试时间</th><th>测试类型</th><th>综合画像</th><th>报告</th></tr></thead>
            <tbody>
              <tr v-for="item in state.testHistory" :key="item.id">
                <td>{{ formatTime(item.createdAt) }}</td>
                <td>{{ item.type }}</td>
                <td>{{ item.portraitTitle }}</td>
                <td><RouterLink v-if="item.canViewReport" to="/report">查看报告</RouterLink></td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>

      <article class="panel">
        <h2>我的推荐反馈记录</h2>
        <EmptyState
          v-if="!state?.feedbacks.length"
          title="暂无推荐反馈"
          description="在推荐页点击喜欢、一般或不喜欢后，这里会保留记录。"
          action-label="查看推荐"
          action-to="/recommendations"
        />
        <div v-else class="table-wrap">
          <table>
            <thead><tr><th>推荐名称</th><th>类型</th><th>反馈</th><th>时间</th></tr></thead>
            <tbody>
              <tr v-for="item in state.feedbacks" :key="`${item.recommendationId}-${item.createdAt}`">
                <td>{{ item.recommendationTitle }}</td>
                <td>{{ item.recommendationType }}</td>
                <td>{{ item.feedback }}</td>
                <td>{{ formatTime(item.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>
    </section>

    <section class="grid two">
      <article class="panel">
        <h2>隐私授权管理</h2>
        <EmptyState
          v-if="!state?.invites.length"
          title="暂无匹配邀请码"
          description="生成邀请码后，你可以在这里查看和撤销授权。"
          action-label="管理匹配授权"
          action-to="/match"
        />
        <div v-else class="table-wrap">
          <table>
            <thead><tr><th>邀请码</th><th>状态</th><th>生成时间</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="invite in state.invites" :key="invite.code">
                <td>{{ invite.code }}</td>
                <td>{{ invite.status }}</td>
                <td>{{ formatTime(invite.createdAt) }}</td>
                <td><button v-if="invite.status === 'active'" class="danger" type="button" @click="revoke(invite.code)">撤销授权</button></td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>

      <article class="panel">
        <h2>我的双人适配记录</h2>
        <EmptyState
          v-if="!state?.matchResults.length"
          title="暂无双人适配记录"
          description="使用邀请码完成匹配后，这里会记录契合度和建议。"
          action-label="去匹配"
          action-to="/match"
        />
        <div v-else class="table-wrap">
          <table>
            <thead><tr><th>对象</th><th>契合度</th><th>时间</th></tr></thead>
            <tbody>
              <tr v-for="item in state.matchResults" :key="item.id">
                <td>{{ item.userBName }}</td>
                <td>{{ item.compatibility }}%</td>
                <td>{{ formatTime(item.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>
    </section>
  </PageContainer>
</template>
