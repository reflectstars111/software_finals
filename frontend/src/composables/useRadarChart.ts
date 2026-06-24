import * as echarts from 'echarts'
import { onUnmounted, type Ref } from 'vue'

export function useRadarChart(chartEl: Ref<HTMLDivElement | null>) {
  let chart: echarts.ECharts | null = null

  function draw(option: Record<string, unknown>) {
    if (!chartEl.value) return
    if (!chart) {
      chart = echarts.init(chartEl.value)
    }
    chart.setOption(option as any)
  }

  function resize() {
    chart?.resize()
  }

  onUnmounted(() => {
    window.removeEventListener('resize', resize)
    chart?.dispose()
  })

  return { draw, resize }
}
