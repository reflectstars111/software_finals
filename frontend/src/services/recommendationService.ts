import { recommendationApi } from '../api'
import type { Recommendation, UserFeedback } from '../types'

export async function listRecommendations(scene: string) {
  return recommendationApi.list(scene) as Promise<Recommendation[]>
}

export async function submitFeedback(id: number, rating: string, comment?: string) {
  return recommendationApi.feedback(id, { rating, comment })
}

export async function getMyFeedback() {
  return recommendationApi.myFeedback() as Promise<UserFeedback[]>
}
