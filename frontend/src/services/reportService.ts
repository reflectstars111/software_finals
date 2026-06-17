import { reportApi } from '../api'

export async function getMyReport() {
  return reportApi.me()
}

export async function getReportHistory() {
  return reportApi.history()
}

export async function getReportSnapshot(id: number) {
  return reportApi.get(id)
}

export async function shareReport() {
  return reportApi.share()
}

export async function getShares() {
  return reportApi.shares()
}

export async function revokeShare(id: number) {
  return reportApi.revokeShare(id)
}
