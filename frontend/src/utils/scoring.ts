import { testModules } from '../data/mockQuestions'
import type { DimensionKey, SurveyQuestion, TestAnswer, UserPortrait } from '../productTypes'

const dimensionDefaults: UserPortrait = {
  openness: 50,
  conscientiousness: 50,
  extraversion: 50,
  agreeableness: 50,
  emotionalStability: 50,
  foodAdventure: 50,
  foodSocial: 50,
  travelAdventure: 50,
  travelPlanning: 50,
  socialEnergy: 50
}

export const personalityDimensionLabels: Record<keyof Pick<UserPortrait, 'openness' | 'conscientiousness' | 'extraversion' | 'agreeableness' | 'emotionalStability'>, string> = {
  openness: '开放性',
  conscientiousness: '尽责性',
  extraversion: '外向性',
  agreeableness: '宜人性',
  emotionalStability: '情绪稳定性'
}

export function allQuestions(): SurveyQuestion[] {
  return testModules.flatMap((module) => module.questions)
}

export function scoreUserPortrait(answers: TestAnswer[], questions = allQuestions()): UserPortrait {
  const grouped = new Map<DimensionKey, number[]>()
  const answerMap = new Map(answers.map((answer) => [answer.questionId, answer.value]))

  for (const question of questions) {
    const raw = answerMap.get(question.id)
    if (!raw) continue
    const value = question.reverse ? 6 - raw : raw
    const normalized = Math.round(((value - 1) / 4) * 100)
    grouped.set(question.dimension, [...(grouped.get(question.dimension) || []), normalized])
  }

  const portrait = { ...dimensionDefaults }
  for (const [dimension, values] of grouped.entries()) {
    portrait[dimension] = Math.round(values.reduce((sum, value) => sum + value, 0) / values.length)
  }
  return portrait
}

export function portraitTitle(portrait: UserPortrait): string {
  const choices: Array<[keyof UserPortrait, string]> = [
    ['openness', '探索型生活家'],
    ['conscientiousness', '计划型生活家'],
    ['extraversion', '社交型体验者'],
    ['agreeableness', '温和型协调者'],
    ['emotionalStability', '安静型观察者']
  ]
  return choices.sort((a, b) => portrait[b[0]] - portrait[a[0]])[0][1]
}

export function portraitSummary(portrait: UserPortrait): string {
  const title = portraitTitle(portrait)
  if (title === '探索型生活家') return '你具有较高的开放性，适合新鲜餐饮、小众路线和开放式社交活动。'
  if (title === '计划型生活家') return '你重视秩序和确定性，适合清晰计划、稳定品质和可控节奏。'
  if (title === '社交型体验者') return '你容易从互动中获得能量，适合分享型餐饮、活动型旅行和轻松社交。'
  if (title === '温和型协调者') return '你擅长照顾关系感受，适合低冲突、可协商、重体验的生活决策。'
  return '你对环境和节奏更敏锐，适合舒缓恢复、低压力和留有余地的安排。'
}

export function dimensionExplanation(key: keyof typeof personalityDimensionLabels, score: number) {
  const high = score >= 70
  const low = score < 45
  const copy = {
    openness: high
      ? '你更容易接受新鲜体验，适合探索类餐厅、小众展览和非标准化路线。'
      : low
        ? '你更偏好熟悉、稳定和可预测的选择。'
        : '你愿意尝试新东西，但需要明确收益或安全边界。',
    conscientiousness: high
      ? '你重视计划、秩序和可靠交付，适合目标明确的安排。'
      : low
        ? '你更依赖即时状态，适合轻量任务和短周期反馈。'
        : '你能在计划和弹性之间切换。',
    extraversion: high
      ? '你容易从交流、活动和群体互动中获得能量。'
      : low
        ? '你更适合低噪音、深度交流和安静场景。'
        : '你能社交，也需要独处恢复。',
    agreeableness: high
      ? '你重视关系感受，擅长协调和照顾他人。'
      : low
        ? '你更直接表达边界和判断，适合规则清晰的协作。'
        : '你能平衡自己和他人的需求。',
    emotionalStability: high
      ? '你面对变化更稳定，适合承担不确定性较高的安排。'
      : low
        ? '你对压力和环境变化更敏锐，需要稳定节奏和恢复空间。'
        : '你能感知压力，并通常可以自我调节。'
  }
  return {
    explanation: copy[key],
    impact: `该维度会影响系统对${personalityDimensionLabels[key]}相关餐饮、旅行和社交场景的推荐权重。`
  }
}
