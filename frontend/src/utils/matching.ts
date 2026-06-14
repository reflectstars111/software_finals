import { demoPartnerPortrait } from '../data/mockUsers'
import type { MatchResult, PersonalityScore, UserPortrait } from '../productTypes'
import { personalityDimensionLabels } from './scoring'

export const MATCH_WEIGHTS: Record<keyof PersonalityScore, number> = {
  openness: 0.2,
  conscientiousness: 0.2,
  extraversion: 0.2,
  agreeableness: 0.2,
  emotionalStability: 0.2
}

const dimensionKeys = Object.keys(MATCH_WEIGHTS) as Array<keyof PersonalityScore>

export function createMatchResult(
  userPortrait: UserPortrait,
  partnerPortrait: UserPortrait = demoPartnerPortrait,
  userAName = '我',
  userBName = '对方'
): MatchResult {
  const dimensionScores = {} as Record<keyof PersonalityScore, number>
  for (const key of dimensionKeys) {
    dimensionScores[key] = Math.max(0, Math.round(100 - Math.abs(userPortrait[key] - partnerPortrait[key])))
  }

  const compatibility = Math.round(
    dimensionKeys.reduce((sum, key) => sum + dimensionScores[key] * MATCH_WEIGHTS[key], 0)
  )
  const sorted = [...dimensionKeys].sort((a, b) => dimensionScores[b] - dimensionScores[a])
  const strengths = sorted.slice(0, 2).map((key) => `你们在${personalityDimensionLabels[key]}上较接近，适合围绕共同节奏做决定。`)
  const differences = sorted.slice(-2).reverse().map((key) => `你们在${personalityDimensionLabels[key]}上差异更明显，需要提前沟通边界和期待。`)

  return {
    id: `match-${Date.now()}`,
    createdAt: new Date().toISOString(),
    userAName,
    userBName,
    compatibility,
    dimensionScores,
    strengths,
    differences,
    warnings: ['适配结果不会展示对方完整答题内容，只展示维度差异。'],
    suggestions: [
      '旅行或聚餐前，建议提前确认预算、时间和路线，同时保留一部分自由调整空间。',
      '当偏好不一致时，先约定不可接受项，再讨论可以尝试的新选择。'
    ]
  }
}
