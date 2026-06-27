<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import BaseModal from './common/BaseModal.vue'
import { fetchProvinces, fetchCities, fetchDistricts, getMyRegion, saveMyRegion } from '../services/regionService'
import { regionApi } from '../api'
import type { RegionInfo, SimpleRegion } from '../types'

const emit = defineEmits<{
  (e: 'update:hasRegion', value: boolean): void
  (e: 'regionChanged'): void
}>()

const provinces = ref<SimpleRegion[]>([])
const cities = ref<SimpleRegion[]>([])
const districts = ref<SimpleRegion[]>([])

const selectedProvince = ref<SimpleRegion | null>(null)
const selectedCity = ref<SimpleRegion | null>(null)
const selectedDistrict = ref<SimpleRegion | null>(null)

const region = ref<RegionInfo | null>(null)
const saving = ref(false)
const locating = ref(false)
const loaded = ref(false)
const error = ref('')
const showConsent = ref(false)

function regionErrorMessage(err: unknown, fallback: string) {
  const message = (err as Error)?.message || ''
  if (message.includes('Network Error') || message.includes('timeout') || message.includes('ECONNREFUSED')) {
    return fallback + '：后端服务未连接，请确认 8080 端口服务已启动。'
  }
  return message ? fallback + '：' + message : fallback
}

async function loadProvinces() {
  try {
    provinces.value = await fetchProvinces()
  } catch (err) {
    error.value = regionErrorMessage(err, '加载省份数据失败')
  }
}

async function loadRegion() {
  try {
    const r = await getMyRegion()
    region.value = r
    emit('update:hasRegion', !!r)
  } catch {
    // No region yet
  }
}

onMounted(async () => {
  await loadProvinces()
  await loadRegion()
  loaded.value = true
})

onUnmounted(() => {
  if (geoTimeout) { clearTimeout(geoTimeout); geoTimeout = null }
})

watch(selectedProvince, async (p) => {
  selectedCity.value = null
  selectedDistrict.value = null
  cities.value = []
  districts.value = []
  if (p) {
    try { cities.value = await fetchCities(p.id) } catch (err) { error.value = regionErrorMessage(err, '加载城市数据失败') }
  }
})

watch(selectedCity, async (c) => {
  selectedDistrict.value = null
  districts.value = []
  if (c) {
    try { districts.value = await fetchDistricts(c.id) } catch (err) { error.value = regionErrorMessage(err, '加载区县数据失败') }
  }
})

let geoTimeout: ReturnType<typeof setTimeout> | null = null

async function autoLocate() {
  console.log('[autoLocate] clicked')
  locating.value = true; error.value = ''

  // Check if browser supports Geolocation API
  if (!navigator.geolocation) {
    console.log('[autoLocate] navigator.geolocation not available → IP fallback')
    locating.value = false
    showConsent.value = true
    return
  }

  // Safety timer: if geolocation doesn't respond within 5s, auto-fallback to IP
  // (Google Location Services may be unreachable in some regions)
  geoTimeout = setTimeout(() => {
    console.warn('[autoLocate] geolocation timed out (5s safety) → IP fallback')
    geoTimeout = null
    // getCurrentPosition callbacks will be ignored after this
    locating.value = false
    showConsent.value = true
  }, 5000)

  console.log('[autoLocate] calling getCurrentPosition...')
  try {
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        console.log('[autoLocate] GPS success:', pos.coords.latitude, pos.coords.longitude)
        if (!geoTimeout) return // safety timer already fired, ignore
        clearTimeout(geoTimeout)
        geoTimeout = null
        handleGeoSuccess(pos)
      },
      (err) => {
        console.warn('[autoLocate] GPS error/denied:', err?.code, err?.message)
        if (!geoTimeout) return // already handled
        clearTimeout(geoTimeout)
        geoTimeout = null
        locating.value = false
        showConsent.value = true
      },
      {
        enableHighAccuracy: false,
        timeout: 8000,
        maximumAge: 300000
      }
    )
  } catch (e) {
    console.error('[autoLocate] getCurrentPosition threw synchronously:', e)
    if (geoTimeout) { clearTimeout(geoTimeout); geoTimeout = null }
    locating.value = false
    showConsent.value = true
  }
  console.log('[autoLocate] getCurrentPosition returned (waiting for callback)')
}

async function handleGeoSuccess(pos: GeolocationPosition) {
  try {
    const result = await regionApi.reverseGeocode(pos.coords.latitude, pos.coords.longitude)
    console.log('[autoLocate] reverseGeocode result:', result)
    if (result && result.province && result.city) {
      await applyRegionResult(result)
    } else {
      console.log('[autoLocate] reverseGeocode empty → IP fallback')
      locating.value = false
      showConsent.value = true
    }
  } catch (e) {
    console.error('[autoLocate] reverseGeocode error:', e)
    locating.value = false
    showConsent.value = true
  }
}

async function locateByIp() {
  console.log('[locateByIp] starting IP locate')
  locating.value = true; error.value = ''
  try {
    const result = await regionApi.ipLocate()
    console.log('[locateByIp] result:', result)
    if (result && result.province && result.city) {
      await applyRegionResult(result)
    } else {
      console.log('[locateByIp] empty result')
      error.value = '无法获取位置，请手动选择'
    }
  } catch (e) {
    console.error('[locateByIp] error:', e)
    error.value = '位置获取失败'
  }
  finally { locating.value = false }
}

async function applyRegionResult(result: { province: string; city: string }) {
  const matchedProv = provinces.value.find(p => p.name.includes(result.province))
  if (matchedProv) {
    selectedProvince.value = matchedProv
    cities.value = await fetchCities(matchedProv.id)
    const matchedCity = cities.value.find(c => c.name.includes(result.city))
    if (matchedCity) {
      selectedCity.value = matchedCity
      districts.value = await fetchDistricts(matchedCity.id)
    }
  }
  await save()
  locating.value = false
}

async function save() {
  if (!selectedProvince.value || !selectedCity.value) return
  saving.value = true
  error.value = ''
  try {
    const payload: RegionInfo = {
      province: selectedProvince.value.name,
      city: selectedCity.value.name,
      district: selectedDistrict.value?.name
    }
    const saved = await saveMyRegion(payload)
    region.value = saved
    emit('update:hasRegion', true)
    emit('regionChanged')
  } catch (e) {
    error.value = (e as Error).message || '保存地域失败'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="region-selector">
    <div v-if="!loaded" class="region-loading">加载地域数据...</div>
    <template v-else>
      <div class="region-shell">
        <div class="region-copy">
          <span class="region-kicker">推荐位置</span>
          <h2>选择餐饮、旅游推荐城市</h2>
          <p v-if="region">
            当前使用 <strong>{{ region.province }} {{ region.city }}{{ region.district ? ' ' + region.district : '' }}</strong> 生成本地化结果。
          </p>
          <p v-else>补充所在地后，系统会优先匹配附近的餐厅、景点和城市体验。</p>
        </div>

        <div class="region-controls">
          <label class="region-field">
            <span>省份</span>
            <select v-model="selectedProvince">
              <option :value="null" disabled>选择省份</option>
              <option v-for="p in provinces" :key="p.id" :value="p">{{ p.name }}</option>
            </select>
          </label>
          <label class="region-field">
            <span>城市</span>
            <select v-model="selectedCity" :disabled="!selectedProvince">
              <option :value="null" disabled>选择城市</option>
              <option v-for="c in cities" :key="c.id" :value="c">{{ c.name }}</option>
            </select>
          </label>
          <label class="region-field">
            <span>区县</span>
            <select v-model="selectedDistrict" :disabled="!selectedCity">
              <option :value="null">不限区县</option>
              <option v-for="d in districts" :key="d.id" :value="d">{{ d.name }}</option>
            </select>
          </label>
        </div>

        <div class="region-actions">
          <button class="region-locate" type="button" :disabled="locating" @click="autoLocate">
            {{ locating ? "定位中..." : "使用当前位置" }}
          </button>
          <button class="primary" type="button" :disabled="!selectedProvince || !selectedCity || saving" @click="save">
            {{ saving ? '保存中...' : '确认地域' }}
          </button>
        </div>
      </div>
      <div v-if="error" class="region-error">{{ error }}</div>

      <BaseModal :open="showConsent" title="位置授权" @close="showConsent = false">
        <p>无法获取精确定位，是否使用 IP 地址获取大致位置？</p>
        <div class="toolbar consent-actions">
          <button class="ghost" type="button" @click="showConsent = false">拒绝</button>
          <button class="primary" type="button" @click="showConsent = false; locateByIp()">允许</button>
        </div>
      </BaseModal>
    </template>
  </div>
</template>

<style scoped>
.region-selector {
  margin-bottom: 1.5rem;
}

.region-shell {
  display: grid;
  grid-template-columns: minmax(210px, 0.85fr) minmax(360px, 1.25fr) auto;
  align-items: end;
  gap: 18px;
  padding: 18px;
  border: 1px solid rgba(216, 212, 226, 0.95);
  border-radius: var(--radius-card);
  background:
    linear-gradient(135deg, rgba(108, 204, 160, 0.08), rgba(232, 180, 79, 0.08)),
    var(--surface);
  box-shadow: var(--shadow);
}

.region-copy {
  min-width: 0;
}

.region-kicker {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 9px;
  border-radius: 999px;
  background: var(--field);
  color: var(--blip);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.06em;
}

.region-copy h2 {
  margin: 10px 0 6px;
  color: var(--field);
}

.region-copy p {
  margin: 0;
  color: var(--muted);
  font-size: 0.9rem;
  line-height: 1.55;
}

.region-copy strong {
  color: var(--field);
}

.region-controls {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.region-field {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.region-field span {
  color: var(--muted);
  font-size: 12px;
  font-weight: 800;
}

.region-field select {
  width: 100%;
  min-height: 44px;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  padding: 0 36px 0 12px;
  color: var(--ink);
  background:
    linear-gradient(45deg, transparent 50%, var(--muted) 50%) calc(100% - 18px) 19px / 6px 6px no-repeat,
    linear-gradient(135deg, var(--muted) 50%, transparent 50%) calc(100% - 14px) 19px / 6px 6px no-repeat,
    var(--surface);
  appearance: none;
  outline: none;
  transition: border-color 0.16s ease, box-shadow 0.16s ease, background-color 0.16s ease;
}

.region-field select:focus {
  border-color: var(--blip);
  box-shadow: var(--ring-blip);
}

.region-field select:disabled {
  background-color: var(--soft);
  color: var(--muted);
}

.region-actions {
  display: flex;
  align-items: end;
  gap: 10px;
  justify-content: flex-end;
}

.region-locate {
  min-height: 44px;
  padding: 0 14px;
  border: 1px solid rgba(232, 180, 79, 0.45);
  border-radius: var(--radius-sm);
  background: #fff8e8;
  color: var(--field);
  font-weight: 800;
  white-space: nowrap;
  transition: background 0.16s ease, transform 0.10s ease, border-color 0.16s ease;
}

.region-locate:hover:not(:disabled) {
  border-color: var(--signal);
  background: #fff2d2;
}

.region-locate:active:not(:disabled) {
  transform: scale(0.97);
}

.region-error {
  margin-top: 0.75rem;
  color: var(--error);
  font-size: 0.875rem;
}
.region-loading {
  padding: 18px;
  border: 1px solid var(--line);
  border-radius: var(--radius-card);
  background: var(--surface);
  color: var(--muted);
  font-size: 0.9rem;
}
.consent-actions { margin-top: 1.25rem; justify-content: flex-end; }

@media (max-width: 920px) {
  .region-shell {
    grid-template-columns: 1fr;
    align-items: stretch;
    gap: 14px;
    padding: 16px;
  }

  .region-controls {
    grid-template-columns: 1fr;
  }

  .region-actions {
    justify-content: stretch;
  }

  .region-actions button {
    flex: 1;
  }
}

@media (max-width: 520px) {
  .region-actions {
    flex-direction: column;
  }

  .region-actions button {
    width: 100%;
  }
}
</style>
