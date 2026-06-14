import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import DashboardView from './DashboardView.vue'

vi.mock('../stores/auth', () => ({
  useAuthStore: () => ({ user: { displayName: '晨间探索者' } })
}))

vi.mock('../utils/storage', () => ({
  loadProductState: () => ({
    answers: {},
    portrait: {
      openness: 80,
      conscientiousness: 60,
      extraversion: 70,
      agreeableness: 65,
      emotionalStability: 55,
      foodAdventure: 75,
      foodSocial: 68,
      travelAdventure: 72,
      travelPlanning: 58,
      socialEnergy: 74
    },
    lastTestAt: '2026-06-01T00:00:00Z',
    testHistory: [{ id: '1' }],
    feedbacks: [{ recommendationId: 'f' }],
    invites: [{ code: 'ME123', status: 'active' }],
    matchResults: [{ id: 'm' }]
  })
}))

describe('DashboardView', () => {
  it('renders local product overview', () => {
    const wrapper = mount(DashboardView, {
      global: {
        stubs: { RouterLink: { template: '<a><slot /></a>' } }
      }
    })

    expect(wrapper.text()).toContain('晨间探索者')
    expect(wrapper.text()).toContain('测试记录')
    // Dashboard shows empty state when no portrait exists
    expect(wrapper.text()).toContain('还没有完成画像测试')
  })
})
