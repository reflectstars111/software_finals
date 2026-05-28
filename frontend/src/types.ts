export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface UserProfile {
  id: number
  phone: string
  displayName: string
  avatarUrl?: string
  role: 'USER' | 'ADMIN'
}

export interface AuthResponse {
  token: string
  user: UserProfile
}

export interface QuestionOption {
  id: number
  label: string
  content: string
  weights: Record<string, number>
}

export interface Question {
  id: number
  type: string
  content: string
  active: boolean
  options: QuestionOption[]
}

export interface TestResult {
  id: number
  type: string
  scores: Record<string, number>
  createdAt: string
}

export interface RadarIndicator {
  name: string
  max: number
}

export interface Report {
  user: UserProfile
  scores: Record<string, number>
  indicators: RadarIndicator[]
  radarValues: number[]
  interpretations: string[]
  suggestions: string[]
  generatedAt: string
}

export interface Recommendation {
  id: number
  scene: string
  title: string
  description: string
  tags: string[]
  score: number
}

export interface MatchReport {
  id: number
  owner: UserProfile
  target: UserProfile
  score: number
  summary: string
  advantages: string[]
  warnings: string[]
  advice: string[]
  createdAt: string
}

export interface AdminStats {
  users: number
  questions: number
  recommendations: number
  feedbacks: number
  matches: number
}

