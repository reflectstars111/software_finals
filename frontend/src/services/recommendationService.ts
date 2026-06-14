import type { FeedbackValue, RecommendationType } from '../productTypes'
import { buildRecommendations } from '../utils/recommendation'
import { loadProductState, saveProductState } from '../utils/storage'

export async function listPersonalizedRecommendations(type: RecommendationType) {
  const state = loadProductState()
  if (!state.portrait) return []
  return buildRecommendations(type, state.portrait, state.feedbacks)
}

export async function saveRecommendationFeedback(id: string, title: string, type: RecommendationType, feedback: FeedbackValue) {
  const state = loadProductState()
  state.feedbacks = [
    {
      recommendationId: id,
      recommendationTitle: title,
      recommendationType: type,
      feedback,
      createdAt: new Date().toISOString()
    },
    ...state.feedbacks
  ].slice(0, 40)
  saveProductState(state)
}
