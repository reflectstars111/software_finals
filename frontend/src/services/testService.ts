import { testModules } from '../data/mockQuestions'
import type { TestAnswer } from '../productTypes'
import { portraitTitle, scoreUserPortrait } from '../utils/scoring'
import { loadProductState, saveProductState } from '../utils/storage'

export async function loadTestModules() {
  return testModules
}

export async function saveAssessment(answers: TestAnswer[]) {
  const state = loadProductState()
  const portrait = scoreUserPortrait(answers)
  const createdAt = new Date().toISOString()
  state.answers = Object.fromEntries(answers.map((answer) => [answer.questionId, answer.value]))
  state.portrait = portrait
  state.lastTestAt = createdAt
  state.testHistory = [
    {
      id: `test-${Date.now()}`,
      createdAt,
      type: '综合测评',
      portraitTitle: portraitTitle(portrait),
      portrait,
      canViewReport: true
    },
    ...state.testHistory
  ].slice(0, 20)
  saveProductState(state)
  return portrait
}
