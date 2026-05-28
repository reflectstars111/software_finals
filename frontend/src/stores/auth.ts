import { defineStore } from 'pinia'
import { authApi } from '../api'
import type { UserProfile } from '../types'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('radar_token') || '',
    user: JSON.parse(localStorage.getItem('radar_user') || 'null') as UserProfile | null
  }),
  getters: {
    isAuthed: (state) => Boolean(state.token && state.user),
    isAdmin: (state) => state.user?.role === 'ADMIN'
  },
  actions: {
    persist(token: string, user: UserProfile) {
      this.token = token
      this.user = user
      localStorage.setItem('radar_token', token)
      localStorage.setItem('radar_user', JSON.stringify(user))
    },
    async login(phone: string, password: string) {
      const result = await authApi.login({ phone, password })
      this.persist(result.token, result.user)
    },
    async register(phone: string, password: string, displayName: string) {
      const result = await authApi.register({ phone, password, displayName })
      this.persist(result.token, result.user)
    },
    async refresh() {
      if (!this.token) return
      this.user = await authApi.me()
      localStorage.setItem('radar_user', JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('radar_token')
      localStorage.removeItem('radar_user')
    }
  }
})

