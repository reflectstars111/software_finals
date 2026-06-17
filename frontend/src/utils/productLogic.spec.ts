import { describe, expect, it } from 'vitest'
import { DIMENSION_LABELS, DISPLAY_LABELS, displayScore, dimensionExplanation, portraitTitle, portraitSummary, type DimensionKey } from './dimensions'

describe('dimension utilities', () => {
  it('has all 10 dimension labels', () => {
    const keys: DimensionKey[] = [
      'OPENNESS', 'CONSCIENTIOUSNESS', 'EXTRAVERSION', 'AGREEABLENESS', 'NEUROTICISM',
      'FOOD_ADVENTURE', 'FOOD_SOCIAL', 'TRAVEL_ADVENTURE', 'TRAVEL_PLANNING', 'SOCIAL_ENERGY'
    ]
    expect(Object.keys(DIMENSION_LABELS)).toHaveLength(10)
    keys.forEach((k) => expect(DIMENSION_LABELS[k]).toBeTruthy())
  })

  it('inverts NEUROTICISM for display', () => {
    expect(displayScore('NEUROTICISM', 80)).toBe(20)
    expect(displayScore('NEUROTICISM', 20)).toBe(80)
    expect(displayScore('OPENNESS', 80)).toBe(80)
  })

  it('maps NEUROTICISM to stability for display', () => {
    expect(DISPLAY_LABELS['NEUROTICISM']).toBe('情绪稳定性')
    expect(DISPLAY_LABELS['OPENNESS']).toBe('开放性')
  })

  it('returns explanation text for all 10 dimensions', () => {
    const keys: DimensionKey[] = [
      'OPENNESS', 'CONSCIENTIOUSNESS', 'EXTRAVERSION', 'AGREEABLENESS', 'NEUROTICISM',
      'FOOD_ADVENTURE', 'FOOD_SOCIAL', 'TRAVEL_ADVENTURE', 'TRAVEL_PLANNING', 'SOCIAL_ENERGY'
    ]
    for (const key of keys) {
      const high = dimensionExplanation(key, 80)
      const mid = dimensionExplanation(key, 55)
      const low = dimensionExplanation(key, 30)
      expect(high.explanation).toBeTruthy()
      expect(mid.explanation).toBeTruthy()
      expect(low.explanation).toBeTruthy()
      expect(high.impact).toContain(DIMENSION_LABELS[key])
    }
  })

  it('returns portrait title for strongest dimension', () => {
    const scores: Record<string, number> = {
      OPENNESS: 85, CONSCIENTIOUSNESS: 60, EXTRAVERSION: 50, AGREEABLENESS: 55, NEUROTICISM: 40,
      FOOD_ADVENTURE: 50, FOOD_SOCIAL: 50, TRAVEL_ADVENTURE: 50, TRAVEL_PLANNING: 50, SOCIAL_ENERGY: 50
    }
    expect(portraitTitle(scores)).toBe('探索型生活家')
  })

  it('returns summary text for portrait', () => {
    const scores: Record<string, number> = {
      OPENNESS: 85, CONSCIENTIOUSNESS: 60, EXTRAVERSION: 50, AGREEABLENESS: 55, NEUROTICISM: 40,
      FOOD_ADVENTURE: 50, FOOD_SOCIAL: 50, TRAVEL_ADVENTURE: 50, TRAVEL_PLANNING: 50, SOCIAL_ENERGY: 50
    }
    const summary = portraitSummary(scores)
    expect(summary).toBeTruthy()
    expect(summary.length).toBeGreaterThan(10)
  })
})
