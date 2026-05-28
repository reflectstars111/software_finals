import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import AdminView from './AdminView.vue'

vi.mock('../api', () => ({
  adminApi: {
    stats: vi.fn().mockResolvedValue({ users: 3, questions: 8, recommendations: 11, feedbacks: 0, matches: 0 }),
    dashboard: vi.fn().mockResolvedValue({
      stats: { users: 3, questions: 8, recommendations: 11, feedbacks: 0, matches: 0 },
      testsByType: { personality: 2, food: 2, travel: 2 },
      feedbackByRating: { like: 1, neutral: 0, dislike: 0 },
      recommendationsByScene: { food: 3, travel: 3, outfit: 2, career: 3 },
      activeShares: 1
    }),
    questions: vi.fn().mockResolvedValue([]),
    recommendationItems: vi.fn().mockResolvedValue([]),
    feedback: vi.fn().mockResolvedValue([]),
    logs: vi.fn().mockResolvedValue([]),
    users: vi.fn().mockResolvedValue([
      { id: 2, phone: '13900000001', displayName: '晨间探索者', role: 'USER', active: true, createdAt: '2026-05-28T00:00:00Z' }
    ]),
    recommendationRules: vi.fn().mockResolvedValue([
      { id: 1, tag: 'explore', label: '探索尝鲜', weight: 4, active: true }
    ]),
    createQuestion: vi.fn(),
    updateQuestion: vi.fn(),
    deleteQuestion: vi.fn(),
    createRecommendation: vi.fn(),
    updateRecommendation: vi.fn(),
    deleteRecommendation: vi.fn(),
    updateUser: vi.fn(),
    createRecommendationRule: vi.fn(),
    updateRecommendationRule: vi.fn(),
    deleteRecommendationRule: vi.fn()
  }
}))

describe('AdminView', () => {
  it('renders productized admin sections for users and recommendation rules', async () => {
    const wrapper = mount(AdminView)
    await flushPromises()

    expect(wrapper.text()).toContain('用户管理')
    expect(wrapper.text()).toContain('推荐规则')
    expect(wrapper.text()).toContain('晨间探索者')
    expect(wrapper.text()).toContain('探索尝鲜')
  })
})
