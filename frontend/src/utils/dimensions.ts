// Display utilities for the 10-dimension personality + lifestyle model
// Backend is the authoritative source for scores; this file only provides display helpers.

export type DimensionKey =
  | 'OPENNESS' | 'CONSCIENTIOUSNESS' | 'EXTRAVERSION' | 'AGREEABLENESS' | 'NEUROTICISM'
  | 'FOOD_ADVENTURE' | 'FOOD_SOCIAL' | 'TRAVEL_ADVENTURE' | 'TRAVEL_PLANNING' | 'SOCIAL_ENERGY'

export const DIMENSION_LABELS: Record<DimensionKey, string> = {
  OPENNESS: '开放性',
  CONSCIENTIOUSNESS: '尽责性',
  EXTRAVERSION: '外向性',
  AGREEABLENESS: '宜人性',
  NEUROTICISM: '情绪敏感度',
  FOOD_ADVENTURE: '饮食探索',
  FOOD_SOCIAL: '饮食社交',
  TRAVEL_ADVENTURE: '旅行探索',
  TRAVEL_PLANNING: '旅行计划',
  SOCIAL_ENERGY: '社交能量'
}

export const DISPLAY_LABELS: Record<DimensionKey, string> = {
  ...DIMENSION_LABELS,
  NEUROTICISM: '情绪稳定性'
}

export function displayScore(dimension: DimensionKey, rawScore: number): number {
  return dimension === 'NEUROTICISM' ? 100 - rawScore : rawScore
}

export function dimensionExplanation(dimension: DimensionKey, score: number): { explanation: string; impact: string } {
  const high = score >= 70
  const low = score < 45
  const base = { OPENNESS, CONSCIENTIOUSNESS, EXTRAVERSION, AGREEABLENESS, NEUROTICISM,
    FOOD_ADVENTURE, FOOD_SOCIAL, TRAVEL_ADVENTURE, TRAVEL_PLANNING, SOCIAL_ENERGY } as const

  function OPENNESS() {
    return high ? '你更容易接受新鲜体验，适合探索类餐厅、小众展览和非标准化路线。'
      : low ? '你更偏好熟悉、稳定和可预测的选择。' : '你愿意尝试新东西，但需要明确收益或安全边界。'
  }
  function CONSCIENTIOUSNESS() {
    return high ? '你重视计划、秩序和可靠交付，适合目标明确的安排。'
      : low ? '你更依赖即时状态，适合轻量任务和短周期反馈。' : '你能在计划和弹性之间切换。'
  }
  function EXTRAVERSION() {
    return high ? '你容易从交流、活动和群体互动中获得能量。'
      : low ? '你更适合低噪音、深度交流和安静场景。' : '你能社交，也需要独处恢复。'
  }
  function AGREEABLENESS() {
    return high ? '你重视关系感受，擅长协调和照顾他人。'
      : low ? '你更直接表达边界和判断，适合规则清晰的协作。' : '你能平衡自己和他人的需求。'
  }
  function NEUROTICISM() {
    return high ? '你对压力和环境变化更敏锐，需要稳定节奏和恢复空间。'
      : low ? '你面对变化更稳定，适合承担不确定性较高的安排。' : '你能感知压力，并通常可以自我调节。'
  }
  function FOOD_ADVENTURE() {
    return high ? '你愿意尝试新奇口味和创意餐饮，探索型餐厅更适合你。'
      : low ? '你更偏好熟悉稳定的口味，固定的老店更让你安心。' : '你对新口味持开放态度，但也看重熟悉的安全感。'
  }
  function FOOD_SOCIAL() {
    return high ? '你享受与朋友分享美食的社交体验，适合聚餐和分享型场景。'
      : low ? '你更喜欢安静独立的用餐方式，不需要太多社交互动。' : '你偶尔享受聚餐，但也珍惜独自用餐的安静。'
  }
  function TRAVEL_ADVENTURE() {
    return high ? '你向往未知目的地和小众路线，探索型旅行能给你带来满足感。'
      : low ? '你更偏爱熟悉的目的地和路线，重复带来的安全感很重要。' : '你在探索和稳妥之间找平衡，视情况决定冒险程度。'
  }
  function TRAVEL_PLANNING() {
    return high ? '你重视出行的确定性和可控性，计划明确的旅程让你更安心。'
      : low ? '你喜欢随性的出行方式，享受即兴和意外的惊喜。' : '你会做基本规划，但保留一定的弹性空间。'
  }
  function SOCIAL_ENERGY() {
    return high ? '你有充足的社交能量，愿意主动发起聚会并参与群体活动。'
      : low ? '连续社交会让你疲惫，你需要充足的独处时间来恢复能量。' : '你能适应不同的社交场合，也会给自己留出独处时间。'
  }
  return {
    explanation: base[dimension](),
    impact: '该维度会影响系统对' + DIMENSION_LABELS[dimension] + '相关餐饮、旅行和社交场景的推荐权重。'
  }
}

export function portraitTitle(scores: Record<string, number>): string {
  if (!scores || Object.keys(scores).length === 0) return '未知型'
  const sorted = Object.entries(scores).sort(([, a], [, b]) => b - a)
  const top = sorted[0][0]
  const titles: Record<string, string> = {
    OPENNESS: '探索型生活家',
    CONSCIENTIOUSNESS: '计划型生活家',
    EXTRAVERSION: '社交型体验者',
    AGREEABLENESS: '温和型协调者',
    NEUROTICISM: '稳定型守护者',
    FOOD_ADVENTURE: '饮食探索家',
    FOOD_SOCIAL: '聚餐分享者',
    TRAVEL_ADVENTURE: '旅行探索者',
    TRAVEL_PLANNING: '出行规划师',
    SOCIAL_ENERGY: '社交能量者'
  }
  return titles[top] || '平衡型生活家'
}

export function portraitSummary(scores: Record<string, number>): string {
  const title = portraitTitle(scores)
  const summaries: Record<string, string> = {
    '探索型生活家': '你具有较高的开放性，适合新鲜餐饮、小众路线和开放式社交活动。',
    '计划型生活家': '你重视秩序和确定性，适合清晰计划、稳定品质和可控节奏。',
    '社交型体验者': '你容易从互动中获得能量，适合分享型餐饮、活动型旅行和轻松社交。',
    '温和型协调者': '你擅长照顾关系感受，适合低冲突、可协商、重体验的生活决策。',
    '稳定型守护者': '你对环境和节奏更敏锐，适合舒缓恢复、低压力和留有余地的安排。',
    '饮食探索家': '你对食物充满好奇心，愿意尝试异域风味和新奇餐饮体验。',
    '聚餐分享者': '你享受通过美食与朋友连接的时光，适合热闹的聚餐和分享型场景。',
    '旅行探索者': '你向往远方和未知，适合自由探索、深度体验的旅行方式。',
    '出行规划师': '你喜欢提前规划行程，确保旅途井然有序、减少不确定性。',
    '社交能量者': '你拥有充沛的社交能量，适合组织和参与各类群体活动。'
  }
  return summaries[title] || '你的生活画像基于10个维度的综合评估。'
}
