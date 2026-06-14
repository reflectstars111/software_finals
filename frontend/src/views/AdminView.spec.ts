import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import AdminView from './AdminView.vue'

vi.mock('../api', () => ({
  adminApi: {
    dashboard: vi.fn().mockResolvedValue({
      stats: { users: 3, questions: 14, recommendations: 16, feedbacks: 1, matches: 1 },
      testsByType: {},
      feedbackByRating: {},
      recommendationsByScene: {},
      activeShares: 1
    }),
    users: vi.fn().mockResolvedValue([
      { id: 2, phone: '13900000001', displayName: '晨间探索者', role: 'USER', active: true }
    ]),
    questions: vi.fn().mockResolvedValue([
      { id: 1, type: 'PERSONALITY', content: '周末突然空出一天，你更想怎么安排？', active: true, options: [] }
    ]),
    recommendationItems: vi.fn().mockResolvedValue([
      { id: 1, scene: 'FOOD', title: '创意融合小馆', tags: ['explore'], score: 80 }
    ]),
    recommendationRules: vi.fn().mockResolvedValue([
      { id: 1, tag: 'explore', label: '探索尝鲜', weight: 4, active: true }
    ]),
    updateUser: vi.fn(),
    updateRecommendationRule: vi.fn()
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
