import { testApi } from '../api'
import type { Question, TestResult } from '../types'

export async function loadQuestions(type?: string) {
  const allTypes = ['personality', 'food', 'travel', 'social']
  if (type) {
    const questions = await testApi.questions(type)
    return { [type]: questions } as Record<string, Question[]>
  }
  const results = await Promise.all(allTypes.map((t) => testApi.questions(t)))
  return Object.fromEntries(allTypes.map((t, i) => [t, results[i]]))
}

export async function submitTest(type: string, answers: { questionId: number; optionIds: number[] }[]) {
  return testApi.submit({ type, answers })
}

export async function getTestHistory() {
  return testApi.history()
}
