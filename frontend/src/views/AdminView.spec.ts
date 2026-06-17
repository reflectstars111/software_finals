import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import AdminView from './AdminView.vue'

vi.mock('../api', () => ({
  adminApi: {
    dashboard: vi.fn().mockResolvedValue({
      stats: { users: 3, questions: 44, recommendations: 20, feedbacks: 5, matches: 2 },
      testsByType: {},
      feedbackByRating: {},
      recommendationsByScene: {},
      activeShares: 1
    }),
    users: vi.fn().mockResolvedValue([
      { id: 2, phone: '13900000001', displayName: '晨间探索者', role: 'USER', active: true }
    ]),
    questions: vi.fn().mockResolvedValue([
      { id: 1, type: 'personality', content: '看到微信群突然出现 99+ 未读时...', active: true, options: [{ id: 1 }, { id: 2 }, { id: 3 }, { id: 4 }, { id: 5 }] }
    ]),
    recommendationItems: vi.fn().mockResolvedValue([
      { id: 1, scene: 'food', title: '创意融合小馆', tags: ['explore'], score: 80, baseScore: 68 }
    ]),
    recommendationRules: vi.fn().mockResolvedValue([
      { id: 1, tag: 'explore', label: '探索尝鲜', weight: 4, active: true }
    ]),
    feedback: vi.fn().mockResolvedValue([
      { id: 1, userPhone: '13900000001', itemTitle: '创意融合小馆', rating: 'LIKE', createdAt: '2026-06-01T00:00:00Z' }
    ]),
    logs: vi.fn().mockResolvedValue([
      { id: 1, adminPhone: '13800000000', action: 'UPDATE_USER', detail: '启用用户', createdAt: '2026-06-01T00:00:00Z' }
    ]),
    updateUser: vi.fn(),
    updateRecommendationRule: vi.fn()
  }
}))

describe('AdminView', () => {
  it('renders admin dashboard with users, rules, questions, recommendations, feedback, and logs', async () => {
    const wrapper = mount(AdminView, {
      global: {
        stubs: { LoadingState: { template: '<div>loading...</div>' }, EmptyState: { template: '<div>empty</div>' } }
      }
    })
    await flushPromises()

    expect(wrapper.text()).toContain('用户管理')
    expect(wrapper.text()).toContain('推荐规则')
    expect(wrapper.text()).toContain('晨间探索者')
    expect(wrapper.text()).toContain('探索尝鲜')
    expect(wrapper.text()).toContain('题库管理')
    expect(wrapper.text()).toContain('推荐库管理')
    expect(wrapper.text()).toContain('最近反馈')
    expect(wrapper.text()).toContain('操作日志')
  })
})
