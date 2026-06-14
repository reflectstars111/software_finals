export type TestCategory = 'personality' | 'food' | 'travel' | 'social'

export type RecommendationType = 'food' | 'travel' | 'social'

export type DimensionKey =
  | 'openness'
  | 'conscientiousness'
  | 'extraversion'
  | 'agreeableness'
  | 'emotionalStability'
  | 'foodAdventure'
  | 'foodSocial'
  | 'travelAdventure'
  | 'travelPlanning'
  | 'socialEnergy'

export type FeedbackValue = 'like' | 'neutral' | 'dislike'

export interface UserProfile {
  id: string
  name: string
  avatar?: string
  lastTestAt?: string
}

export interface SurveyQuestion {
  id: string
  category: TestCategory
  dimension: DimensionKey
  text: string
  reverse?: boolean
}

export interface TestModule {
  category: TestCategory
  title: string
  shortTitle: string
  description: string
  questions: SurveyQuestion[]
}

export interface TestAnswer {
  questionId: string
  value: number
}

export interface PersonalityScore {
  openness: number
  conscientiousness: number
  extraversion: number
  agreeableness: number
  emotionalStability: number
}

export interface LifestylePreference {
  foodAdventure: number
  foodSocial: number
  travelAdventure: number
  travelPlanning: number
  socialEnergy: number
}

export type UserPortrait = PersonalityScore & LifestylePreference

export interface TestHistoryRecord {
  id: string
  createdAt: string
  type: string
  portraitTitle: string
  portrait: UserPortrait
  canViewReport: boolean
}

export interface RecommendationItem {
  id: string
  type: RecommendationType
  title: string
  tags: string[]
  baseScore: number
  description: string
  personalityDimensions: DimensionKey[]
  preferenceDimensions: DimensionKey[]
}

export interface PersonalizedRecommendation extends RecommendationItem {
  matchScore: number
  reason: string
  fitReason: string
}

export interface RecommendationFeedback {
  recommendationId: string
  recommendationTitle: string
  recommendationType: RecommendationType
  feedback: FeedbackValue
  createdAt: string
}

export interface MatchInvite {
  code: string
  ownerUserId: string
  createdAt: string
  status: 'active' | 'used' | 'revoked'
}

export interface MatchResult {
  id: string
  createdAt: string
  userAName: string
  userBName: string
  compatibility: number
  dimensionScores: Record<keyof PersonalityScore, number>
  strengths: string[]
  differences: string[]
  suggestions: string[]
  warnings: string[]
}

export interface ProductState {
  answers: Record<string, number>
  portrait: UserPortrait | null
  lastTestAt?: string
  testHistory: TestHistoryRecord[]
  feedbacks: RecommendationFeedback[]
  invites: MatchInvite[]
  matchResults: MatchResult[]
}
