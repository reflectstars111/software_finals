import type { RecommendationItem } from '../productTypes'

export const mockRecommendations: RecommendationItem[] = [
  {
    id: 'food-fusion',
    type: 'food',
    title: '创意融合小馆',
    tags: ['探索尝鲜', '适合聊天'],
    baseScore: 72,
    description: '菜单变化多，适合把晚餐变成一次轻量冒险。',
    personalityDimensions: ['openness', 'extraversion'],
    preferenceDimensions: ['foodAdventure', 'foodSocial']
  },
  {
    id: 'food-bowl',
    type: 'food',
    title: '轻食能量碗',
    tags: ['计划稳定', '轻负担'],
    baseScore: 66,
    description: '清爽、稳定、可控，适合希望保持节奏的工作日。',
    personalityDimensions: ['conscientiousness', 'emotionalStability'],
    preferenceDimensions: ['foodSocial']
  },
  {
    id: 'food-hotpot',
    type: 'food',
    title: '热闹火锅局',
    tags: ['社交表达', '多人分享'],
    baseScore: 68,
    description: '气氛热烈，适合需要社交能量和分享感的朋友聚餐。',
    personalityDimensions: ['extraversion', 'agreeableness'],
    preferenceDimensions: ['foodSocial']
  },
  {
    id: 'travel-citywalk',
    type: 'travel',
    title: '城市漫游路线',
    tags: ['探索尝鲜', '低门槛'],
    baseScore: 74,
    description: '用展览、街区和咖啡馆串起一个不赶路的周末。',
    personalityDimensions: ['openness'],
    preferenceDimensions: ['travelAdventure']
  },
  {
    id: 'travel-planned',
    type: 'travel',
    title: '两天一夜计划游',
    tags: ['计划稳定', '效率友好'],
    baseScore: 70,
    description: '交通、住宿和景点节奏清晰，降低临场决策压力。',
    personalityDimensions: ['conscientiousness'],
    preferenceDimensions: ['travelPlanning']
  },
  {
    id: 'travel-slow',
    type: 'travel',
    title: '疗愈慢旅行',
    tags: ['舒缓恢复', '自然节奏'],
    baseScore: 67,
    description: '减少打卡压力，把重点放在自然、休息和恢复。',
    personalityDimensions: ['emotionalStability', 'agreeableness'],
    preferenceDimensions: ['travelPlanning']
  },
  {
    id: 'social-salon',
    type: 'social',
    title: '主题沙龙或读书会',
    tags: ['深度交流', '观点表达'],
    baseScore: 71,
    description: '适合在低压力场景里认识同频的人，并保留表达空间。',
    personalityDimensions: ['openness', 'agreeableness'],
    preferenceDimensions: ['socialEnergy']
  },
  {
    id: 'social-market',
    type: 'social',
    title: '市集与轻社交活动',
    tags: ['社交表达', '探索尝鲜'],
    baseScore: 73,
    description: '人群、摊位和活动都足够开放，适合边走边聊。',
    personalityDimensions: ['extraversion', 'openness'],
    preferenceDimensions: ['socialEnergy', 'travelAdventure']
  },
  {
    id: 'social-small',
    type: 'social',
    title: '三人以内的小聚',
    tags: ['舒缓恢复', '关系维护'],
    baseScore: 68,
    description: '低噪音、关系稳定，适合需要恢复又不想完全独处的时刻。',
    personalityDimensions: ['agreeableness', 'emotionalStability'],
    preferenceDimensions: ['socialEnergy']
  }
]
