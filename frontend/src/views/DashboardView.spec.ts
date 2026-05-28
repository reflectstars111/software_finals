import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import DashboardView from './DashboardView.vue'

vi.mock('../stores/auth', () => ({
  useAuthStore: () => ({ user: { displayName: '晨间探索者' } })
}))

vi.mock('../api', () => ({
  testApi: {
    history: vi.fn().mockResolvedValue([{ id: 1, type: 'personality', scores: { OPENNESS: 82 }, createdAt: '2026-05-28T00:00:00Z' }])
  },
  reportApi: {
    history: vi.fn().mockResolvedValue([{ id: 1, summary: '开放性：82 分', createdAt: '2026-05-28T00:00:00Z' }]),
    shares: vi.fn().mockResolvedValue([{ id: 1, token: 'abc', active: true, createdAt: '2026-05-28T00:00:00Z' }])
  },
  matchApi: {
    list: vi.fn().mockResolvedValue([{ id: 1, score: 88, target: { displayName: '安静计划家' }, createdAt: '2026-05-28T00:00:00Z' }])
  },
  recommendationApi: {
    list: vi.fn().mockResolvedValue([{ id: 1, title: '城市漫游路线', score: 91, tags: ['explore'], scene: 'travel' }])
  }
}))

describe('DashboardView', () => {
  it('renders live overview from reports, matches, shares and recommendations', async () => {
    const wrapper = mount(DashboardView, {
      global: {
        stubs: { RouterLink: { template: '<a><slot /></a>' } }
      }
    })
    await flushPromises()

    expect(wrapper.text()).toContain('近期动态')
    expect(wrapper.text()).toContain('开放性：82 分')
    expect(wrapper.text()).toContain('安静计划家')
    expect(wrapper.text()).toContain('城市漫游路线')
  })
})
