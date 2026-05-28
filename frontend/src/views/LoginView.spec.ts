import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import LoginView from './LoginView.vue'

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() })
}))

describe('LoginView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('renders the login form with demo account guidance', () => {
    const wrapper = mount(LoginView)

    expect(wrapper.text()).toContain('性格雷达·生活指南')
    expect(wrapper.text()).toContain('默认演示用户：13900000001 / User123456')
    expect(wrapper.find('input[placeholder="11 位手机号"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
  })
})
