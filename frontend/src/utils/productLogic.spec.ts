import { describe, expect, it } from 'vitest'
import { testModules } from '../data/mockQuestions'
import type { SurveyQuestion, UserPortrait } from '../productTypes'
import { createMatchResult } from './matching'
import { buildRecommendations, RECOMMENDATION_WEIGHTS } from './recommendation'
import { scoreUserPortrait } from './scoring'

const basePortrait: UserPortrait = {
  openness: 82,
  conscientiousness: 64,
  extraversion: 76,
  agreeableness: 70,
  emotionalStability: 58,
  foodAdventure: 88,
  foodSocial: 80,
  travelAdventure: 72,
  travelPlanning: 54,
  socialEnergy: 78
}

describe('product logic', () => {
  it('keeps the DOCX-expanded question bank split across four assessment modules', () => {
    expect(testModules.map((module) => module.category)).toEqual(['personality', 'food', 'travel', 'social'])
    // Personality has 26, food 20, travel 20, social 18 — expanded from DOCX scenarios
    expect(testModules.map((module) => module.questions.length)).toEqual([26, 20, 20, 18])

    const allQuestionText = testModules.flatMap((module) => module.questions.map((question) => question.text))
    expect(allQuestionText.length).toBeGreaterThanOrEqual(60)
    expect(allQuestionText.some((t) => t.includes('99+'))).toBe(true)
    expect(allQuestionText.some((t) => t.includes('苍蝇馆子'))).toBe(true)
    expect(allQuestionText.some((t) => t.includes('倾盆大雨'))).toBe(true)
    expect(allQuestionText.some((t) => t.includes('抽象'))).toBe(true)
  })

  it('scores normal and reverse survey questions into portrait dimensions', () => {
    const questions: SurveyQuestion[] = [
      { id: 'open', category: 'personality', dimension: 'openness', text: '开放题' },
      { id: 'stable-reverse', category: 'personality', dimension: 'emotionalStability', text: '反向题', reverse: true }
    ]

    const portrait = scoreUserPortrait([
      { questionId: 'open', value: 5 },
      { questionId: 'stable-reverse', value: 5 }
    ], questions)

    expect(portrait.openness).toBe(100)
    expect(portrait.emotionalStability).toBe(0)
    expect(portrait.conscientiousness).toBe(50)
  })

  it('keeps recommendation weights explicit and lowers disliked items', () => {
    expect(RECOMMENDATION_WEIGHTS).toEqual({ personality: 0.4, preference: 0.4, feedback: 0.2 })

    const before = buildRecommendations('food', basePortrait, [])
    const target = before.find((item) => item.id === 'food-fusion')
    expect(target).toBeTruthy()

    const after = buildRecommendations('food', basePortrait, [{
      recommendationId: 'food-fusion',
      recommendationTitle: '创意融合小馆',
      recommendationType: 'food',
      feedback: 'dislike',
      createdAt: '2026-06-01T00:00:00.000Z'
    }])
    const lowered = after.find((item) => item.id === 'food-fusion')

    expect(lowered?.matchScore).toBeLessThan(target!.matchScore)
  })

  it('calculates a perfect match for identical portraits', () => {
    const result = createMatchResult(basePortrait, basePortrait, '我', '好友')

    expect(result.compatibility).toBe(100)
    expect(Object.values(result.dimensionScores)).toEqual([100, 100, 100, 100, 100])
    expect(result.suggestions.length).toBeGreaterThan(0)
  })
})
