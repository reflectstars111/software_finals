import type { TestModule } from '../productTypes'

export const scaleOptions = [
  { value: 1, label: '非常不同意' },
  { value: 2, label: '不太同意' },
  { value: 3, label: '一般' },
  { value: 4, label: '比较同意' },
  { value: 5, label: '非常同意' }
]

export const testModules: TestModule[] = [
  {
    category: 'personality',
    title: '基础性格测试',
    shortTitle: '性格',
    description: '了解你的五大性格维度，作为报告、推荐和匹配的基础。共 25 题，请按真实感受作答。',
    questions: [
      // ---- 开放性 Openness ----
      { id: 'p-open-1', category: 'personality', dimension: 'openness', text: '我愿意尝试新的餐厅、路线或活动。' },
      { id: 'p-open-2', category: 'personality', dimension: 'openness', text: '面对长假目的地，我会被异国、边疆或文化差异大的地方吸引。' },
      { id: 'p-open-3', category: 'personality', dimension: 'openness', text: '我愿意凭感觉加料尝试，创造属于自己的版本，而不是严格照搬教程。' },
      { id: 'p-open-4', category: 'personality', dimension: 'openness', text: '看到 AI 和机器人越来越聪明时，我更容易想象积极的新可能。' },
      { id: 'p-open-5', category: 'personality', dimension: 'openness', text: '看到别人放弃稳定生活去追求自由，我会心生羡慕并觉得那才是鲜活的人生。' },
      { id: 'p-open-6', category: 'personality', dimension: 'openness', text: '我更愿意选择有风险但充满可能性的职业方向，而非稳定但重复的工作。' },

      // ---- 尽责性 Conscientiousness ----
      { id: 'p-cons-1', category: 'personality', dimension: 'conscientiousness', text: '我做决定前会先确认计划、预算和风险。' },
      { id: 'p-cons-2', category: 'personality', dimension: 'conscientiousness', text: '我经常临时决定重要安排。', reverse: true },
      { id: 'p-cons-3', category: 'personality', dimension: 'conscientiousness', text: '拿到大作业或项目后，我会尽早拆解进度并提前完成。' },
      { id: 'p-cons-4', category: 'personality', dimension: 'conscientiousness', text: '我的电脑桌面和浏览器标签通常会保持分类清楚、数量可控。' },
      { id: 'p-cons-5', category: 'personality', dimension: 'conscientiousness', text: '出门远行前，我会列好清单、分门别类收纳行李。' },
      { id: 'p-cons-6', category: 'personality', dimension: 'conscientiousness', text: '我更偏好口碑稳定、价格清晰的餐厅，而非信息不透明的选择。' },

      // ---- 外向性 Extraversion ----
      { id: 'p-extra-1', category: 'personality', dimension: 'extraversion', text: '和朋友交流或参加活动会让我更有能量。' },
      { id: 'p-extra-2', category: 'personality', dimension: 'extraversion', text: '看到微信群突然出现 99+ 未读时，我会想立刻点开加入聊天。' },
      { id: 'p-extra-3', category: 'personality', dimension: 'extraversion', text: '面对突然通知的破冰团建，我通常会期待认识新朋友。' },
      { id: 'p-extra-4', category: 'personality', dimension: 'extraversion', text: '线上聊天和线下社交我都喜欢，但现实中与人面对面让我更自在。' },
      { id: 'p-extra-5', category: 'personality', dimension: 'extraversion', text: '发生严重分歧时，我倾向于立即当面沟通解决，不愿拖延。' },

      // ---- 宜人性 Agreeableness ----
      { id: 'p-agree-1', category: 'personality', dimension: 'agreeableness', text: '安排活动时，我会主动照顾同行者的感受。' },
      { id: 'p-agree-2', category: 'personality', dimension: 'agreeableness', text: '朋友拿着不太理想的报告找我提意见时，我会先照顾对方感受。' },
      { id: 'p-agree-3', category: 'personality', dimension: 'agreeableness', text: '朋友之间发生争执时，我会先安抚双方情绪，而非立刻分出对错。' },
      { id: 'p-agree-4', category: 'personality', dimension: 'agreeableness', text: '团队中遇到能力不足但态度好的队友，我会更看重对方的态度。' },
      { id: 'p-agree-5', category: 'personality', dimension: 'agreeableness', text: '遇到需要帮助的弱者时，我会优先于个人安排去帮助。' },

      // ---- 情绪稳定性 Emotional Stability ----
      { id: 'p-stable-1', category: 'personality', dimension: 'emotionalStability', text: '行程突然变化时，我通常能保持稳定。' },
      { id: 'p-stable-2', category: 'personality', dimension: 'emotionalStability', text: '发生严重分歧时，我可以先冷静下来，再找合适时机沟通。' },
      { id: 'p-stable-3', category: 'personality', dimension: 'emotionalStability', text: '面对陌生人的突然热情，我会先保持警觉并观察对方意图。', reverse: true },
      { id: 'p-stable-4', category: 'personality', dimension: 'emotionalStability', text: '遇到挫折时，我更相信调整方法能改变结果，而非归结为运气。' }
    ]
  },
  {
    category: 'food',
    title: '饮食偏好测试',
    shortTitle: '饮食',
    description: '判断你更适合探索型、稳定型、社交型还是舒缓型饮食场景。共 20 题。',
    questions: [
      // ---- 饮食探索 Food Adventure ----
      { id: 'f-adv-1', category: 'food', dimension: 'foodAdventure', text: '选择餐厅时，我会优先考虑没尝试过的新口味。' },
      { id: 'f-adv-2', category: 'food', dimension: 'foodAdventure', text: '朋友带我去"超级正宗但环境很破"的苍蝇馆子时，我会更期待味道。' },
      { id: 'f-adv-3', category: 'food', dimension: 'foodAdventure', text: '遇到折耳根、香菜、螺蛳粉这类争议食物时，我愿意尝试一下。' },
      { id: 'f-adv-4', category: 'food', dimension: 'foodAdventure', text: '看到奇特跨界融合菜（如折耳根美式、螺蛳粉汉堡）的网红店，我愿意尝鲜。' },
      { id: 'f-adv-5', category: 'food', dimension: 'foodAdventure', text: '我更偏好重口味、刺激性强的食物，越辣越过瘾。' },

      // ---- 饮食社交 Food Social ----
      { id: 'f-social-1', category: 'food', dimension: 'foodSocial', text: '我喜欢适合多人分享和聊天的用餐方式。' },
      { id: 'f-social-2', category: 'food', dimension: 'foodSocial', text: '遇到精美的大餐时，我会想先拍照记录或分享给朋友。' },
      { id: 'f-social-3', category: 'food', dimension: 'foodSocial', text: '聚餐时，我很在意公共餐桌礼仪和彼此尊重。' },
      { id: 'f-social-4', category: 'food', dimension: 'foodSocial', text: '点菜时说"随便"但上菜后又挑剔的人，我认为是聚餐中最不可接受的。' },

      // ---- 饮食舒缓情绪相关（关联 emotionalStability） ----
      { id: 'f-stable-1', category: 'food', dimension: 'emotionalStability', text: '一个人吃火锅或烤肉时，我也能自在享受，不太在意旁人眼光。' },
      { id: 'f-stable-2', category: 'food', dimension: 'emotionalStability', text: '我喜欢安静、轻负担、不赶时间的用餐体验。' },

      // ---- 稳定偏好 Conscientiousness (via food) ----
      { id: 'f-cons-1', category: 'food', dimension: 'conscientiousness', text: '我更偏好口碑稳定、价格清晰的餐厅。' },
      { id: 'f-cons-2', category: 'food', dimension: 'conscientiousness', text: '选择住宿或用餐时，我更看重卫生、交通和舒适稳定。' },

      // ---- 更多饮食冒险维度 ----
      { id: 'f-adv-6', category: 'food', dimension: 'foodAdventure', text: '累了一天后，重口味、高能量的食物更容易让我恢复状态。' },
      { id: 'f-adv-7', category: 'food', dimension: 'foodAdventure', text: '深夜饿了时，我更愿意点高热量的烧烤或炸鸡外卖来犒劳自己。' },
      { id: 'f-adv-8', category: 'food', dimension: 'foodAdventure', text: '吃重口味食物时，冰镇碳酸饮料或冰啤酒是我的标配搭配。' },

      // ---- 更多社交维度 ----
      { id: 'f-social-5', category: 'food', dimension: 'foodSocial', text: '吃饭全程只顾自己玩手机、毫无交流的人让我觉得像拼桌陌生人。' },

      // ---- 情绪相关 ----
      { id: 'f-stable-3', category: 'food', dimension: 'emotionalStability', text: '累的时候，满眼是肉的烤肉、牛排或炸鸡最能让我恢复状态。' },
      { id: 'f-stable-4', category: 'food', dimension: 'emotionalStability', text: '比起高热量大餐，清爽的轻食或素菜汤更能让我感到舒适。', reverse: true },

      // ---- 宜人性关联 ----
      { id: 'f-agree-1', category: 'food', dimension: 'agreeableness', text: '在别人盘子里乱翻或用沾了口水的筷子夹公共菜，是我最不能接受的餐桌行为。' }
    ]
  },
  {
    category: 'travel',
    title: '旅游偏好测试',
    shortTitle: '旅游',
    description: '识别你的旅行探索度、计划需求和恢复节奏。共 20 题。',
    questions: [
      // ---- 旅行探索 Travel Adventure ----
      { id: 't-adv-1', category: 'travel', dimension: 'travelAdventure', text: '旅行时，我期待探索未知路线和小众地点。' },
      { id: 't-adv-2', category: 'travel', dimension: 'travelAdventure', text: '面对长假目的地，我会被异国、边疆或文化差异大的地方吸引。' },
      { id: 't-adv-3', category: 'travel', dimension: 'travelAdventure', text: '如果目的地很远或机会难得，我可以接受"特种兵式旅游"的短时间高密度打卡。' },
      { id: 't-adv-4', category: 'travel', dimension: 'travelAdventure', text: '假期选择目的地时，远离喧嚣的自然风光最让我心动。' },
      { id: 't-adv-5', category: 'travel', dimension: 'travelAdventure', text: '比起经典地标打卡，我更愿意钻进当地人扎堆的老社区和菜市场。' },

      // ---- 旅行计划 Travel Planning ----
      { id: 't-plan-1', category: 'travel', dimension: 'travelPlanning', text: '出发前，我会准备清单、交通方案和备用计划。' },
      { id: 't-plan-2', category: 'travel', dimension: 'travelPlanning', text: '去陌生城市前，我会先用地图或攻略熟悉地形和路线。' },
      { id: 't-plan-3', category: 'travel', dimension: 'travelPlanning', text: '选择住宿时，我更看重卫生、交通和稳定舒适。' },
      { id: 't-plan-4', category: 'travel', dimension: 'travelPlanning', text: '出发前我喜欢用地图 APP 的 3D 实景或卫星图先"云漫游"一遍才踏实。' },
      { id: 't-plan-5', category: 'travel', dimension: 'travelPlanning', text: '旅行临近结束时，我已经开始整理回程后的待办事项了。', reverse: true },

      // ---- 外向性 Extraversion (via travel) ----
      { id: 't-extra-1', category: 'travel', dimension: 'extraversion', text: '我愿意在旅行中参加市集、夜游或当地活动。' },
      { id: 't-extra-2', category: 'travel', dimension: 'extraversion', text: '旅行遇到音乐节、赛事或大型活动时，我愿意去现场感受氛围。' },
      { id: 't-extra-3', category: 'travel', dimension: 'extraversion', text: '外地比赛或项目结束后，我愿意拉着队友去当地热门景点逛一圈。' },

      // ---- 情绪稳定性 Emotional Stability (via travel) ----
      { id: 't-stable-1', category: 'travel', dimension: 'emotionalStability', text: '我不喜欢高强度打卡，更重视安全和休息。' },
      { id: 't-stable-2', category: 'travel', dimension: 'emotionalStability', text: '旅行途中遇到倾盆大雨、室外行程被打乱时，我能把它当成随机体验并调整安排。' },
      { id: 't-stable-3', category: 'travel', dimension: 'emotionalStability', text: '旅游时遇到极端天气打乱计划，我会觉得整趟旅行全毁了。', reverse: true },

      // ---- 开放性 Openness (via travel) ----
      { id: 't-open-1', category: 'travel', dimension: 'openness', text: '面对语言不通、文化差异巨大的异国他乡，我会感到兴奋而非焦虑。' },
      { id: 't-open-2', category: 'travel', dimension: 'openness', text: '抵达旅游地第一站，我会直奔当地最出名的美食街品尝地道风味。' },
      { id: 't-open-3', category: 'travel', dimension: 'openness', text: '旅行住宿我更看重特色和氛围感，比如有设计感的民宿或能认识新朋友的青旅。' },

      // ---- 宜人性 Agreeableness (via travel) ----
      { id: 't-agree-1', category: 'travel', dimension: 'agreeableness', text: '旅行结束前，我会精心给亲戚朋友挑选伴手礼。' }
    ]
  },
  {
    category: 'social',
    title: '社交倾向测试',
    shortTitle: '社交',
    description: '帮助系统推荐更适合你的社交方式和双人适配建议。共 20 题。',
    questions: [
      // ---- 社交能量 Social Energy ----
      { id: 's-energy-1', category: 'social', dimension: 'socialEnergy', text: '我愿意主动发起聚会、活动或共同计划。' },
      { id: 's-energy-2', category: 'social', dimension: 'socialEnergy', text: '连续社交后，我需要较长时间独处恢复。', reverse: true },
      { id: 's-energy-3', category: 'social', dimension: 'socialEnergy', text: '在线下活动遇到很对脾气的人时，我愿意主动加联系方式。' },
      { id: 's-energy-4', category: 'social', dimension: 'socialEnergy', text: '组队比赛或项目中，我愿意担任主动推进和带节奏的人。' },
      { id: 's-energy-5', category: 'social', dimension: 'socialEnergy', text: '打游戏时，我更喜欢拉朋友组队，而不是一直单排。' },

      // ---- 宜人性 Agreeableness (via social) ----
      { id: 's-agree-1', category: 'social', dimension: 'agreeableness', text: '出现分歧时，我倾向先理解对方的需求。' },
      { id: 's-agree-2', category: 'social', dimension: 'agreeableness', text: '朋友发生争执时，我会努力安抚双方情绪，降低冲突。' },
      { id: 's-agree-3', category: 'social', dimension: 'agreeableness', text: '团队活动有人迟到时，我更愿意安抚其他成员并等待，而非直接开始。' },
      { id: 's-agree-4', category: 'social', dimension: 'agreeableness', text: '聊天时对方发来我不理解的抽象表情包，我也愿意顺着气氛参与互动。' },

      // ---- 开放性 Openness (via social) ----
      { id: 's-open-1', category: 'social', dimension: 'openness', text: '我愿意认识不同背景的新朋友。' },
      { id: 's-open-2', category: 'social', dimension: 'openness', text: '聊天里出现抽象或发疯表情包时，我能接住并参与这种幽默。' },
      { id: 's-open-3', category: 'social', dimension: 'openness', text: '比起线下见面，我在网上更容易表达真实想法。', reverse: true },

      // ---- 外向性 Extraversion (via social) ----
      { id: 's-extra-1', category: 'social', dimension: 'extraversion', text: '参加需要组队的活动时，我更享受担任主攻手带领团队的感受。' },
      { id: 's-extra-2', category: 'social', dimension: 'extraversion', text: '在路上听到喜欢的音乐时，我会不自觉地跟着节奏律动，脑海里开始构想画面。' },

      // ---- 情绪稳定性 Emotional Stability (via social) ----
      { id: 's-stable-1', category: 'social', dimension: 'emotionalStability', text: '面对突然的过度热情，我会保持警觉并先观察对方意图。', reverse: true },
      { id: 's-stable-2', category: 'social', dimension: 'emotionalStability', text: '我愿意为了获得公众影响力而接受网络审视和评判。', reverse: true },

      // ---- 尽责性 Conscientiousness (via social) ----
      { id: 's-cons-1', category: 'social', dimension: 'conscientiousness', text: '多人组队时，我更倾向按约定流程推进，不喜欢临时改规则。' },

      // ---- Extra social energy ----
      { id: 's-energy-6', category: 'social', dimension: 'socialEnergy', text: '参加挑战性比赛时，我更看重过程和体验而非最终名次。' }
    ]
  }
]
