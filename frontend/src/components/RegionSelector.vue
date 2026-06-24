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

async function loadProvinces() {
  try {
    provinces.value = await fetchProvinces()
  } catch {
    error.value = '加载省份数据失败'
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
    try { cities.value = await fetchCities(p.id) } catch { error.value = '加载城市数据失败' }
  }
})

watch(selectedCity, async (c) => {
  selectedDistrict.value = null
  districts.value = []
  if (c) {
    try { districts.value = await fetchDistricts(c.id) } catch { error.value = '加载区县数据失败' }
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
      <div class="region-fields">
        <select v-model="selectedProvince" class="field select">
          <option :value="null" disabled>选择省份</option>
          <option v-for="p in provinces" :key="p.id" :value="p">{{ p.name }}</option>
        </select>
        <select v-model="selectedCity" class="field select" :disabled="!selectedProvince">
          <option :value="null" disabled>选择城市</option>
          <option v-for="c in cities" :key="c.id" :value="c">{{ c.name }}</option>
        </select>
        <select v-model="selectedDistrict" class="field select" :disabled="!selectedCity">
          <option :value="null">不限区县</option>
          <option v-for="d in districts" :key="d.id" :value="d">{{ d.name }}</option>
        </select>
        <button class="ghost" type="button" :disabled="locating" @click="autoLocate">{{ locating ? "定位中..." : "使用当前位置" }}</button>
        <button class="primary" type="button" :disabled="!selectedProvince || !selectedCity || saving" @click="save">
          {{ saving ? '保存中...' : '确认' }}
        </button>
      </div>
      <div v-if="region" class="region-current">
        当前地域：{{ region.province }} {{ region.city }}{{ region.district ? ' ' + region.district : '' }}
      </div>
      <div v-else class="region-hint">填写所在地，获取 AI 精准推荐</div>
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
.region-fields {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  flex-wrap: wrap;
}
.region-fields select {
  min-width: 120px;
}
.region-current {
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: var(--muted);
}
.region-hint {
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: var(--muted);
  font-style: italic;
}
.region-error {
  margin-top: 0.5rem;
  color: var(--error);
  font-size: 0.875rem;
}
.region-loading {
  font-size: 0.875rem;
  color: var(--muted);
}
.consent-actions { margin-top: 1.25rem; justify-content: flex-end; }
</style>
