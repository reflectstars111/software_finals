<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/common/PageContainer.vue'
import { postApi } from '../../api'

const router = useRouter()
const content = ref('')
const domainTag = ref('FOOD')
const customTag = ref('')
const styleTags = ref<string[]>([])
const submitting = ref(false)
const error = ref('')

const domainStyleTags: Record<string, string[]> = {
  FOOD: ['轻食·健康', '咸辣重口', '甜品烘焙', '探店打卡', '家常料理', '深夜食堂'],
  TRAVEL: ['自然风光', '城市漫游', '文化古迹', '冒险极限', '躺平度假'],
  OUTFIT: ['简约通勤', '复古文艺', '潮流街头', '运动休闲', '日系森系'],
  CAREER: ['技术成长', '创业日记', '斜槓日常', '职场吐槽'],
  SOCIAL: ['聚会活动', '深度交流', '独处时光'],
  OTHER: []
}

function switchDomain() {
  styleTags.value = []
}

function toggleTag(tag: string) {
  if (styleTags.value.includes(tag)) {
    styleTags.value = styleTags.value.filter(t => t !== tag)
  } else if (styleTags.value.length < 3) {
    styleTags.value.push(tag)
  }
}

function addCustomTag() {
  const t = customTag.value.trim()
  if (t && !styleTags.value.includes(t) && t.length <= 15 && styleTags.value.length < 3) {
    styleTags.value.push(t)
    customTag.value = ''
  }
}

async function submit() {
  if (!content.value.trim()) { error.value = '请输入正文内容'; return }
  submitting.value = true; error.value = ''
  try {
    const result = await postApi.create({ content: content.value, domainTag: domainTag.value, styleTags: styleTags.value })
    router.push(`/community/post/${result.id}`)
  } catch (err) {
    error.value = (err as Error).message || '发布失败'
  } finally { submitting.value = false }
}
</script>

<template>
  <PageContainer eyebrow="发布动态" title="分享你的生活" description="发布文字动态，系统会自动分析你的人格向量并匹配同频的人。">
    <div v-if="error" class="error">{{ error }}</div>
    <div class="panel">
      <div class="field">
        <label>领域标签（必选）</label>
        <select v-model="domainTag" @change="switchDomain" style="min-height:44px;padding:0 12px;border:1px solid var(--line);border-radius:8px">
          <option value="FOOD">饮食</option><option value="TRAVEL">旅行</option><option value="SOCIAL">社交</option>
          <option value="OUTFIT">穿搭</option><option value="CAREER">生涯</option><option value="OTHER">其他</option>
        </select>
      </div>
      <div class="field">
        <label>风格标签（可选，最多3个）</label>
        <div class="tag-row">
          <span v-for="tag in domainStyleTags[domainTag]" :key="tag" :style="{cursor:'pointer',opacity:styleTags.includes(tag)?1:0.5,background:styleTags.includes(tag)?'var(--blip)':'var(--soft)',color:styleTags.includes(tag)?'#fff':'var(--muted)'}" @click="toggleTag(tag)">{{ tag }}</span>
        </div>
      </div>
      <div class="field" v-if="styleTags.length < 3">
        <label>自定义标签（1-15字）</label>
        <div class="toolbar">
          <input v-model="customTag" maxlength="15" placeholder="输入自定义标签" style="flex:1;min-height:36px;padding:0 8px;border:1px solid var(--line);border-radius:6px" />
          <button class="primary small" @click="addCustomTag">添加</button>
        </div>
      </div>
      <div class="field">
        <label>正文（1-2000字）</label>
        <textarea v-model="content" maxlength="2000" rows="6" style="width:100%;min-height:140px;padding:10px 12px;border:1px solid var(--line);border-radius:8px;resize:vertical" placeholder="分享你的想法..."></textarea>
        <span class="muted" style="text-align:right">{{ content.length }}/2000</span>
      </div>
      <button class="primary full" :disabled="submitting" @click="submit">{{ submitting ? '发布中...' : '提交发布' }}</button>
    </div>
  </PageContainer>
</template>
