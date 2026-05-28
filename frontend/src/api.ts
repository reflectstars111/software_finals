import axios from 'axios'
import type {
  AdminStats,
  ApiResponse,
  AuthResponse,
  MatchReport,
  Question,
  Recommendation,
  Report,
  TestResult,
  UserProfile
} from './types'

export const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('radar_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>
    if (typeof body?.code === 'number' && body.code !== 0) {
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return response
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络异常，请稍后再试'
    return Promise.reject(new Error(message))
  }
)

const dataOf = <T>(promise: Promise<{ data: ApiResponse<T> }>) => promise.then((res) => res.data.data)

export const authApi = {
  register: (payload: { phone: string; password: string; displayName: string }) =>
    dataOf<AuthResponse>(api.post('/auth/register', payload)),
  login: (payload: { phone: string; password: string }) => dataOf<AuthResponse>(api.post('/auth/login', payload)),
  me: () => dataOf<UserProfile>(api.get('/users/me')),
  updateMe: (payload: { displayName?: string; avatarUrl?: string }) => dataOf<UserProfile>(api.put('/users/me', payload))
}

export const testApi = {
  questions: (type: string) => dataOf<Question[]>(api.get('/questions', { params: { type } })),
  submit: (payload: { type: string; answers: { questionId: number; optionIds: number[] }[] }) =>
    dataOf<TestResult>(api.post('/tests/submit', payload)),
  history: () => dataOf<TestResult[]>(api.get('/tests/history'))
}

export const reportApi = {
  me: () => dataOf<Report>(api.get('/reports/me')),
  share: () => dataOf<{ token: string; url: string; report: Report }>(api.post('/shares/report')),
  byToken: (token: string) => dataOf<Report>(api.get(`/shares/${token}`))
}

export const recommendationApi = {
  list: (scene: string) => dataOf<Recommendation[]>(api.get('/recommendations', { params: { scene } })),
  feedback: (id: number, payload: { rating: string; comment?: string }) =>
    dataOf<void>(api.post(`/recommendations/${id}/feedback`, payload))
}

export const matchApi = {
  create: (friendPhone: string) => dataOf<MatchReport>(api.post('/matches', { friendPhone })),
  get: (id: number) => dataOf<MatchReport>(api.get(`/matches/${id}`))
}

export const adminApi = {
  stats: () => dataOf<AdminStats>(api.get('/admin/stats')),
  questions: () => dataOf<Question[]>(api.get('/admin/questions')),
  feedback: () => dataOf<unknown[]>(api.get('/admin/feedback')),
  logs: () => dataOf<unknown[]>(api.get('/admin/logs')),
  recommendationItems: () => dataOf<Recommendation[]>(api.get('/admin/recommendation-items')),
  createQuestion: (payload: unknown) => dataOf<Question>(api.post('/admin/questions', payload)),
  updateQuestion: (id: number, payload: unknown) => dataOf<Question>(api.put(`/admin/questions/${id}`, payload)),
  deleteQuestion: (id: number) => dataOf<void>(api.delete(`/admin/questions/${id}`)),
  createRecommendation: (payload: unknown) => dataOf<Recommendation>(api.post('/admin/recommendation-items', payload)),
  updateRecommendation: (id: number, payload: unknown) => dataOf<Recommendation>(api.put(`/admin/recommendation-items/${id}`, payload)),
  deleteRecommendation: (id: number) => dataOf<void>(api.delete(`/admin/recommendation-items/${id}`))
}

