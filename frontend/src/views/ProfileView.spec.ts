import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import ProfileView from './ProfileView.vue'

vi.mock('../stores/auth', () => ({
  useAuthStore: () => ({ user: { id: 1, displayName: '晨间探索者', phone: '13900000001' }, logout: vi.fn() })
}))

vi.mock('../api', () => ({
  testApi: {
    history: vi.fn().mockResolvedValue([
      { id: 1, type: 'PERSONALITY', scores: {}, createdAt: '2026-06-01T00:00:00Z' },
      { id: 2, type: 'FOOD', scores: {}, createdAt: '2026-05-28T00:00:00Z' }
    ])
  },
  reportApi: {
    history: vi.fn().mockResolvedValue([{ id: 1, summary: 'test', createdAt: '2026-06-01T00:00:00Z' }]),
    shares: vi.fn().mockResolvedValue([{ id: 1, token: 'abc12345def', active: true, createdAt: '2026-06-01T00:00:00Z' }])
  },
  matchApi: {
    list: vi.fn().mockResolvedValue([{ id: 1, owner: { displayName: '我' }, target: { displayName: '好友' }, score: 85, summary: 'test', advantages: [], warnings: [], advice: [], createdAt: '2026-06-01T00:00:00Z' }]),
    listInvites: vi.fn().mockResolvedValue([{ code: 'MEABC123', status: 'ACTIVE', createdAt: '2026-06-01T00:00:00Z', expiresAt: '2026-09-01T00:00:00Z' }])
  },
  recommendationApi: {
    myFeedback: vi.fn().mockResolvedValue([{ id: 1, itemTitle: '创意融合小馆', scene: 'FOOD', rating: 'LIKE', createdAt: '2026-06-01T00:00:00Z' }])
  }
}))

vi.mock('../services/reportService', () => ({
  revokeShare: vi.fn().mockResolvedValue(undefined)
}))

describe('ProfileView', () => {
  it('renders test history, feedback, invites, shares, and matches from real API data', async () => {
    const wrapper = mount(ProfileView, {
      global: {
        stubs: { RouterLink: { template: '<a><slot /></a>' } }
      }
    })
    await flushPromises()

    expect(wrapper.text()).toContain('晨间探索者')
    expect(wrapper.text()).toContain('13900000001')
    expect(wrapper.text()).toContain('历史测试记录')
    expect(wrapper.text()).toContain('性格测试')
    expect(wrapper.text()).toContain('推荐反馈记录')
    expect(wrapper.text()).toContain('创意融合小馆')
    expect(wrapper.text()).toContain('MEABC123')
    expect(wrapper.text()).toContain('双人适配记录')
    expect(wrapper.text()).toContain('隐私说明')
  })
})
