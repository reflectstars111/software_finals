import { recommendationApi } from '../api'
import type { LocationRecommendation, RegionInfo, UserFeedback } from '../types'

export async function listRecommendations(scene: string, region?: RegionInfo) {
  return recommendationApi.list(scene, region) as Promise<LocationRecommendation[]>
}

export async function submitFeedback(id: number, rating: string, comment?: string) {
  return recommendationApi.feedback(id, { rating, comment })
}

export async function getMyFeedback() {
  return recommendationApi.myFeedback() as Promise<UserFeedback[]>
}
