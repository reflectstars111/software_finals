export function formatTime(value?: string): string {
  return value ? new Date(value).toLocaleString() : '暂无记录'
}
