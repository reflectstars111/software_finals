import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from './stores/auth'
import HomeView from './views/HomeView.vue'
import DashboardView from './views/DashboardView.vue'
import LoginView from './views/LoginView.vue'
import TestView from './views/TestView.vue'
import ReportView from './views/ReportView.vue'
import RecommendationsView from './views/RecommendationsView.vue'
import MatchView from './views/MatchView.vue'
import ProfileView from './views/ProfileView.vue'
import AdminView from './views/AdminView.vue'
import ShareView from './views/ShareView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomeView },
    { path: '/dashboard', component: DashboardView, meta: { auth: true } },
    { path: '/login', component: LoginView },
    { path: '/tests/:type?', component: TestView, meta: { auth: true } },
    { path: '/report', component: ReportView, meta: { auth: true } },
    { path: '/recommendations', component: RecommendationsView, meta: { auth: true } },
    { path: '/match', component: MatchView, meta: { auth: true } },
    { path: '/profile', component: ProfileView, meta: { auth: true } },
    { path: '/admin', component: AdminView, meta: { auth: true, admin: true } },
    { path: '/share/:token', component: ShareView }
  ]
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path === '/report' && to.query.demo === 'true') return true
  if (to.meta.auth && !auth.isAuthed) return '/login'
  if (to.meta.admin && !auth.isAdmin) return '/'
})

export default router
