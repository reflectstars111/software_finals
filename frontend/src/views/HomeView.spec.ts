import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import HomeView from './HomeView.vue'

describe('HomeView', () => {
  it('renders product entry and primary actions', () => {
    const wrapper = mount(HomeView, {
      global: {
        stubs: { RouterLink: { template: '<a><slot /></a>' } }
      }
    })

    expect(wrapper.text()).toContain('性格雷达·生活指南')
    expect(wrapper.text()).toContain('开始测试')
    expect(wrapper.text()).toContain('查看示例报告')
    expect(wrapper.text()).toContain('测试')
    expect(wrapper.text()).toContain('报告')
    expect(wrapper.text()).toContain('推荐')
    expect(wrapper.text()).toContain('匹配')
  })
})
