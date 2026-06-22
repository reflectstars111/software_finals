<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import EmptyState from '../../components/common/EmptyState.vue'
import LoadingState from '../../components/common/LoadingState.vue'
import PageContainer from '../../components/common/PageContainer.vue'
import { postApi } from '../../api'
import type { CommentResponse, PostResponse } from '../../types'

const route = useRoute()
const post = ref<PostResponse | null>(null)
const comments = ref<CommentResponse[]>([])
const commentText = ref('')
const loading = ref(true)
const error = ref('')
const notice = ref('')

function formatTime(value: string) { return new Date(value).toLocaleString() }

async function load() {
  loading.value = true; error.value = ''
  try {
    const id = Number(route.params.id)
    post.value = await postApi.get(id)
    comments.value = await postApi.listComments(id, 0)
  } catch (err) { error.value = (err as Error).message || '加载失败' }
  finally { loading.value = false }
}

async function doLike() { await postApi.like(post.value!.id); load() }
async function doUnlike() { await postApi.unlike(post.value!.id); load() }
async function doFavorite() { await postApi.favorite(post.value!.id); load() }
async function doUnfavorite() { await postApi.unfavorite(post.value!.id); load() }
async function doComment() {
  if (!commentText.value.trim()) return
  try {
    await postApi.createComment(post.value!.id, { content: commentText.value })
    commentText.value = ''
    comments.value = await postApi.listComments(post.value!.id, 0)
  } catch (err) { error.value = (err as Error).message }
}
async function deleteComment(commentId: number) {
  await postApi.deleteComment(post.value!.id, commentId)
  comments.value = comments.value.filter(c => c.id !== commentId)
}

onMounted(load)
</script>

<template>
  <PageContainer :eyebrow="post?.domainTag || '帖子'" :title="post?.author.displayName + ' 的动态'">
    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="notice" class="notice">{{ notice }}</div>
    <LoadingState v-if="loading" message="正在加载帖子..." />

    <template v-if="post">
      <article class="panel">
        <p style="white-space:pre-wrap">{{ post.content }}</p>
        <div class="tag-row">
          <span>{{ post.domainTag }}</span>
          <span v-for="tag in post.styleTags" :key="tag">{{ tag }}</span>
        </div>
        <p v-if="post.showCompatibility" class="notice compact">你的契合度：{{ post.compatibility }}%</p>
        <div class="toolbar" style="margin-top:12px">
          <button class="primary small" @click="doLike">❤ {{ post.likeCount }}</button>
          <button class="secondary small" @click="doFavorite">⭐ {{ post.favoriteCount }}</button>
          <span class="muted">👁 {{ post.viewCount }} · 💬 {{ post.commentCount }}</span>
        </div>
      </article>

      <section class="panel section-gap">
        <h2>评论 ({{ comments.length }})</h2>
        <div class="toolbar" style="margin-bottom:12px">
          <input v-model="commentText" placeholder="写评论..." maxlength="500" style="flex:1;min-height:36px;padding:0 8px;border:1px solid var(--line);border-radius:6px" />
          <button class="primary small" @click="doComment">发表</button>
        </div>
        <EmptyState v-if="!comments.length" title="暂无评论" description="成为第一个评论的人" />
        <div v-else v-for="c in comments" :key="c.id" class="card" style="margin-bottom:8px">
          <div class="split">
            <strong>{{ c.user.displayName }}</strong>
            <small class="muted">{{ formatTime(c.createdAt) }}</small>
          </div>
          <p>{{ c.content }}</p>
          <button class="ghost small" @click="deleteComment(c.id)">删除</button>
        </div>
      </section>
    </template>
  </PageContainer>
</template>
