import { mockRecommendations } from '../data/mockRecommendations'
import type { FeedbackValue, PersonalizedRecommendation, RecommendationFeedback, RecommendationItem, RecommendationType, UserPortrait } from '../productTypes'

export const RECOMMENDATION_WEIGHTS = {
  personality: 0.4,
  preference: 0.4,
  feedback: 0.2
}

const feedbackScores: Record<FeedbackValue, number> = {
  like: 100,
  neutral: 60,
  dislike: 20
}

function average(values: number[]) {
  return values.length ? values.reduce((sum, value) => sum + value, 0) / values.length : 50
}

function feedbackScore(item: RecommendationItem, feedbacks: RecommendationFeedback[]) {
  const related = feedbacks.filter((feedback) => feedback.recommendationId === item.id)
  if (!related.length) return 60
  return average(related.map((feedback) => feedbackScores[feedback.feedback]))
}

function reasonFor(item: RecommendationItem, portrait: UserPortrait) {
  const topDimension = item.personalityDimensions
    .map((dimension) => [dimension, portrait[dimension]] as const)
    .sort((a, b) => b[1] - a[1])[0]
  const labels: Record<string, string> = {
    openness: '开放性较高，愿意尝试新鲜体验',
    conscientiousness: '尽责性较强，适合计划明确的选择',
    extraversion: '外向性较强，更容易从互动中获得能量',
    agreeableness: '宜人性较高，重视同行者的感受',
    emotionalStability: '更需要稳定节奏和恢复空间',
    foodAdventure: '饮食探索倾向更明显',
    foodSocial: '偏好分享型用餐场景',
    travelAdventure: '旅行探索倾向更明显',
    travelPlanning: '更重视旅行计划和确定性',
    socialEnergy: '社交能量较充足'
  }
  return `因为你的${labels[topDimension?.[0] || 'openness']}，系统优先推荐「${item.title}」。`
}

function fitFor(item: RecommendationItem, portrait: UserPortrait) {
  const preference = item.preferenceDimensions
    .map((dimension) => portrait[dimension])
    .sort((a, b) => b - a)[0] || 50
  if (preference >= 70) return '它和你当前的生活偏好高度一致，可以直接作为近期选择。'
  if (preference >= 50) return '它保留了熟悉感和探索空间，适合作为低风险尝试。'
  return '它能补足你较少尝试的场景，建议从轻量版本开始。'
}

export function buildRecommendations(
  type: RecommendationType,
  portrait: UserPortrait,
  feedbacks: RecommendationFeedback[]
): PersonalizedRecommendation[] {
  return mockRecommendations
    .filter((item) => item.type === type)
    .map((item) => {
      const personality = average(item.personalityDimensions.map((dimension) => portrait[dimension]))
      const preference = average(item.preferenceDimensions.map((dimension) => portrait[dimension]))
      const feedback = feedbackScore(item, feedbacks)
      const matchScore = Math.round(
        personality * RECOMMENDATION_WEIGHTS.personality +
          preference * RECOMMENDATION_WEIGHTS.preference +
          feedback * RECOMMENDATION_WEIGHTS.feedback
      )
      return {
        ...item,
        matchScore: Math.max(1, Math.min(99, Math.round((matchScore + item.baseScore) / 2))),
        reason: reasonFor(item, portrait),
        fitReason: fitFor(item, portrait)
      }
    })
    .sort((a, b) => b.matchScore - a.matchScore)
}
