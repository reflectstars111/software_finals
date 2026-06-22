<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import EmptyState from '../components/common/EmptyState.vue'
import LoadingState from '../components/common/LoadingState.vue'
import PageContainer from '../components/common/PageContainer.vue'
import type { AiRecommendationItem, AiRecommendationResponse, Recommendation } from '../types'
import {
  listAiRecommendations,
  listRecommendations,
  submitAiFeedback,
  submitFeedback
} from '../services/recommendationService'

const tabs: Array<{ value: string; label: string }> = [
  { value: 'food', label: '餐饮推荐' },
  { value: 'travel', label: '旅游推荐' },
  { value: 'social', label: '社交推荐' },
  { value: 'outfit', label: '穿搭推荐' }
]

const active = ref('food')
const items = ref<Recommendation[]>([])
const foodResponse = ref<AiRecommendationResponse | null>(null)
const travelResponse = ref<AiRecommendationResponse | null>(null)
const foodLocation = ref<{ lat: number; lng: number } | null>(null)
const city = ref(localStorage.getItem('radar_city') || '武汉')
const submitted = ref<Record<number, string>>({})
const submittedAi = ref<Record<string, string>>({})
const loading = ref(true)
const locating = ref(false)
const error = ref('')
const notice = ref('')

const foodItems = computed(() => foodResponse.value?.items || [])
const travelItems = computed(() => travelResponse.value?.items || [])
const foodSourceLabel = computed(() => sourceLabel(foodResponse.value, '实时餐饮'))
const travelSourceLabel = computed(() => sourceLabel(travelResponse.value, '实时景点'))
const targetCity = computed(() => city.value.trim() || travelResponse.value?.city || '目标城市')

function sourceLabel(response: AiRecommendationResponse | null, empty: string) {
  if (!response) return empty
  return response.degraded ? '规则兜底' : response.source === 'AI' ? 'AI 推荐' : '规则推荐'
}

function sceneLabel(scene: string) {
  const labels: Record<string, string> = {
    FOOD: '餐饮推荐', food: '餐饮推荐',
    TRAVEL: '旅游推荐', travel: '旅游推荐',
    SOCIAL: '社交推荐', social: '社交推荐',
    OUTFIT: '穿搭推荐', outfit: '穿搭推荐',
    CAREER: '职业推荐', career: '职业推荐'
  }
  return labels[scene] || '生活推荐'
}

function formatDistance(value?: number) {
  if (typeof value !== 'number') return ''
  return value >= 1000 ? `${(value / 1000).toFixed(1)} km` : `${Math.round(value)} m`
}

function rememberCity() {
  const trimmedCity = city.value.trim()
  if (trimmedCity) localStorage.setItem('radar_city', trimmedCity)
  return trimmedCity
}

function paramsForFood() {
  const trimmedCity = rememberCity()
  return {
    scene: 'food',
    lat: foodLocation.value?.lat,
    lng: foodLocation.value?.lng,
    city: trimmedCity || undefined,
    limit: 6
  }
}

function paramsForTravel() {
  const trimmedCity = rememberCity()
  return {
    scene: 'travel',
    city: trimmedCity || undefined,
    limit: 6
  }
}

async function loadFood() {
  foodResponse.value = await listAiRecommendations(paramsForFood())
  travelResponse.value = null
  items.value = []
}

async function loadTravel() {
  travelResponse.value = await listAiRecommendations(paramsForTravel())
  foodResponse.value = null
  items.value = []
}

async function loadRuleRecommendations() {
  items.value = await listRecommendations(active.value)
  foodResponse.value = null
  travelResponse.value = null
}

async function load() {
  loading.value = true
  error.value = ''
  notice.value = ''
  try {
    if (active.value === 'food') {
      await loadFood()
    } else if (active.value === 'travel') {
      await loadTravel()
    } else {
      await loadRuleRecommendations()
    }
  } catch (err) {
    error.value = (err as Error).message || '推荐加载失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

async function locateAndRefresh() {
  error.value = ''
  notice.value = ''
  if (!navigator.geolocation) {
    error.value = '当前浏览器不支持定位。你可以输入城市后刷新推荐。'
    return
  }
  locating.value = true
  try {
    const position = await new Promise<GeolocationPosition>((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject, {
        enableHighAccuracy: true,
        timeout: 8000,
        maximumAge: 120000
      })
    })
    foodLocation.value = {
      lat: position.coords.latitude,
      lng: position.coords.longitude
    }
    notice.value = '已使用当前位置刷新附近餐饮。'
    await loadFood()
  } catch {
    error.value = '定位未完成。允许浏览器定位后可获取附近餐厅，也可以先按城市刷新。'
  } finally {
    locating.value = false
  }
}

function clearLocation() {
  foodLocation.value = null
  notice.value = '已切回城市推荐。'
  loadFood()
}

async function feedback(item: Recommendation, rating: string) {
  try {
    await submitFeedback(item.id, rating)
    submitted.value[item.id] = rating
    notice.value = rating === 'DISLIKE'
      ? '已收到反馈，后续将减少相似类型推荐。'
      : '已收到反馈，后续将优先推荐相似内容。'
    await loadRuleRecommendations()
  } catch (err) {
    error.value = (err as Error).message || '反馈提交失败'
  }
}

async function feedbackAi(item: AiRecommendationItem, rating: string) {
  const response = active.value === 'travel' ? travelResponse.value : foodResponse.value
  if (!response?.recordId) return
  try {
    await submitAiFeedback(response.recordId, { rating, itemName: item.name })
    submittedAi.value[item.name] = rating
    notice.value = rating === 'DISLIKE'
      ? '已记录反馈，下次会避开相似选择。'
      : '已记录反馈，下次会优先考虑相似选择。'
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
    title="把画像变成今天就能执行的选择"
    description="餐饮和旅游会结合位置、城市、画像与真实地图数据生成建议；其他分类继续使用规则排序。"
  >
    <template #actions>
      <div class="segmented">
        <button v-for="tab in tabs" :key="tab.value" :class="{ active: active === tab.value }" @click="active = tab.value">
          {{ tab.label }}
        </button>
      </div>
    </template>

    <section v-if="active === 'food'" class="food-command panel">
      <div class="food-command-copy">
        <p class="eyebrow">餐饮雷达</p>
        <h2>先定位置，再给出可去的餐厅</h2>
        <p class="muted">允许定位后按附近餐厅生成推荐；没有定位时，会按城市和你的饮食画像展示可参考的真实餐厅。</p>
        <div class="food-status-row">
          <span>{{ foodSourceLabel }}</span>
          <span v-if="foodResponse?.degraded">AI 暂不可用，已展示规则兜底</span>
          <span v-if="foodLocation">已启用附近定位</span>
        </div>
      </div>
      <div class="food-controls">
        <label class="field compact-field">
          <span>城市</span>
          <input v-model="city" placeholder="输入城市，例如武汉" @keyup.enter="load" />
        </label>
        <div class="toolbar food-control-actions">
          <button class="secondary" type="button" :disabled="loading" @click="load">按城市刷新</button>
          <button class="primary" type="button" :disabled="locating || loading" @click="locateAndRefresh">
            {{ locating ? '定位中...' : '定位附近' }}
          </button>
          <button v-if="foodLocation" class="ghost" type="button" @click="clearLocation">不用定位</button>
        </div>
      </div>
    </section>

    <section v-else-if="active === 'travel'" class="food-command travel-command panel">
      <div class="food-command-copy">
        <p class="eyebrow">旅行雷达</p>
        <h2>{{ targetCity }}适合你的景点</h2>
        <p class="muted">输入目标城市后，只展示该城市内真实存在的景点，并按你的旅行探索、计划感和性格画像排序。</p>
        <div class="food-status-row">
          <span>{{ travelSourceLabel }}</span>
          <span v-if="travelResponse?.degraded">AI 暂不可用，已展示规则兜底</span>
          <span v-else-if="travelResponse">同城景点排序</span>
        </div>
      </div>
      <div class="food-controls">
        <label class="field compact-field">
          <span>目标城市</span>
          <input v-model="city" placeholder="输入城市，例如成都" @keyup.enter="loadTravel" />
        </label>
        <div class="toolbar food-control-actions">
          <button class="primary" type="button" :disabled="loading" @click="loadTravel">生成景点推荐</button>
        </div>
      </div>
    </section>

    <div v-if="notice" class="notice">{{ notice }}</div>
    <div v-if="error" class="error">{{ error }}</div>
    <LoadingState v-if="loading" :message="active === 'food' ? '正在计算附近餐饮...' : active === 'travel' ? '正在筛选同城景点...' : '正在计算推荐排序...'" />

    <template v-else>
      <template v-if="active === 'food'">
        <EmptyState
          v-if="error.includes('请先完成')"
          title="请先完成测试，系统将根据你的画像生成餐饮推荐。"
          description="完成测评后，系统会用饮食探索、饮食社交和性格维度解释为什么推荐这些餐厅。"
          action-label="去完成测试"
          action-to="/tests/personality"
        />
        <EmptyState
          v-else-if="!foodItems.length"
          title="暂时没有餐饮推荐"
          description="尝试允许定位，或输入城市后重新刷新。"
        />
        <section v-else class="food-results">
          <article v-for="(item, index) in foodItems" :key="`${item.name}-${index}`" class="card food-card">
            <div class="food-card-head">
              <div class="food-rank">{{ index + 1 }}</div>
              <div class="food-title-block">
                <p class="eyebrow">{{ item.category || '附近餐厅' }}</p>
                <h2>{{ item.name }}</h2>
                <p v-if="item.address" class="muted food-address">{{ item.address }}</p>
              </div>
              <span class="score-pill">{{ item.matchScore }}%</span>
            </div>
            <p class="food-reason">{{ item.reason || '这家店与你当前的饮食画像匹配度较高。' }}</p>
            <div class="food-facts">
              <span v-if="formatDistance(item.distanceMeters)">{{ formatDistance(item.distanceMeters) }}</span>
              <span v-if="item.rating">评分 {{ item.rating }}</span>
              <span v-if="item.priceLevel">{{ item.priceLevel }}</span>
            </div>
            <div v-if="item.tags?.length" class="tag-row">
              <span v-for="tag in item.tags" :key="tag">{{ tag }}</span>
            </div>
            <p v-if="submittedAi[item.name]" class="notice compact">已记录反馈：{{ submittedAi[item.name] }}</p>
            <div class="toolbar food-card-actions">
              <a v-if="item.mapUrl" class="button secondary small" :href="item.mapUrl" target="_blank" rel="noreferrer">打开地图</a>
              <button class="primary small" type="button" @click="feedbackAi(item, 'LIKE')">想去</button>
              <button class="secondary small" type="button" @click="feedbackAi(item, 'NEUTRAL')">一般</button>
              <button class="ghost small" type="button" @click="feedbackAi(item, 'DISLIKE')">避开</button>
            </div>
          </article>
        </section>
      </template>

      <template v-else-if="active === 'travel'">
        <EmptyState
          v-if="!travelItems.length"
          title="暂时没有旅行推荐"
          description="换一个城市，或稍后重新生成。"
        />
        <section v-else class="travel-shelf" :aria-label="`${targetCity}景点推荐`">
          <article v-for="(item, index) in travelItems" :key="`${item.city}-${item.name}-${index}`" class="card travel-spot-card">
            <div class="travel-spot-topline">
              <span>{{ item.city || targetCity }}</span>
              <strong>{{ item.matchScore }}%</strong>
            </div>
            <h2>{{ item.name }}</h2>
            <p v-if="item.category" class="eyebrow">{{ item.category }}</p>
            <p v-if="item.address" class="muted food-address">{{ item.address }}</p>
            <p class="food-reason">{{ item.reason || '这个景点与你当前的旅行画像匹配度较高。' }}</p>
            <div v-if="item.highlights?.length" class="travel-section compact-travel-section">
              <p class="travel-label">适合你的看点</p>
              <div class="tag-row">
                <span v-for="point in item.highlights" :key="point">{{ point }}</span>
              </div>
            </div>
            <div v-if="item.itinerary?.length" class="travel-section compact-travel-section">
              <p class="travel-label">游览建议</p>
              <ol class="travel-list">
                <li v-for="step in item.itinerary" :key="step">{{ step }}</li>
              </ol>
            </div>
            <div v-if="item.tips?.length" class="travel-section compact-travel-section">
              <p class="travel-label">提醒</p>
              <ul class="travel-list">
                <li v-for="tip in item.tips" :key="tip">{{ tip }}</li>
              </ul>
            </div>
            <p v-if="submittedAi[item.name]" class="notice compact">已记录反馈：{{ submittedAi[item.name] }}</p>
            <div class="toolbar food-card-actions">
              <a v-if="item.mapUrl" class="button secondary small" :href="item.mapUrl" target="_blank" rel="noreferrer">打开地图</a>
              <button class="primary small" type="button" @click="feedbackAi(item, 'LIKE')">想去</button>
              <button class="secondary small" type="button" @click="feedbackAi(item, 'NEUTRAL')">一般</button>
              <button class="ghost small" type="button" @click="feedbackAi(item, 'DISLIKE')">避开</button>
            </div>
          </article>
        </section>
      </template>

      <template v-else>
        <EmptyState
          v-if="error.includes('请先完成')"
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
                <p class="eyebrow">{{ sceneLabel(item.scene) }}</p>
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
      </template>
    </template>
  </PageContainer>
</template>