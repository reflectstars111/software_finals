<script setup lang="ts">
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const quick = [
  { title: '完成测试', desc: '基础性格、饮食偏好、旅游偏好', path: '/tests/personality' },
  { title: '查看报告', desc: '雷达图和文字画像', path: '/report' },
  { title: '获取推荐', desc: '餐饮、旅行、穿搭、生涯', path: '/recommendations' },
  { title: '双人适配', desc: '用好友手机号生成适配报告', path: '/match' }
]
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">工作台</p>
        <h1>{{ auth.user?.displayName }}，开始今天的生活指南</h1>
        <p class="muted">从测试到报告、推荐、反馈和适配，初版闭环已经串起来。</p>
      </div>
    </header>
    <div class="grid two">
      <RouterLink v-for="item in quick" :key="item.path" class="card" :to="item.path">
        <h2>{{ item.title }}</h2>
        <p class="muted">{{ item.desc }}</p>
        <span class="button">进入</span>
      </RouterLink>
    </div>
    <section class="panel" style="margin-top: 16px">
      <h2>演示账号</h2>
      <div class="grid three">
        <div class="metric">
          <strong>13900000001</strong>
          <span class="muted">普通用户 / User123456</span>
        </div>
        <div class="metric">
          <strong>13900000002</strong>
          <span class="muted">好友适配 / User123456</span>
        </div>
        <div class="metric">
          <strong>13800000000</strong>
          <span class="muted">管理员 / Admin@123456</span>
        </div>
      </div>
    </section>
  </section>
</template>
