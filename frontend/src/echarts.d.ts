declare module 'echarts' {
  export interface ECharts {
    setOption(option: unknown): void
    dispose(): void
    resize(): void
  }

  export function init(container: HTMLElement): ECharts
}
