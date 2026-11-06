// 神域 / 猫鼠 双主题配置：游戏中心的所有文案与配色均由 theme 驱动。
export const GAME_THEMES = {
  shenyu: {
    key: 'shenyu',
    label: '神域',
    title: '神域对决中心',
    subtitle: '守夜人之间的禁墟博弈，横刀向渊，谁主沉浮。',
    duelist: '守夜人',
    moves: { ATTACK: '攻 ⚔', DEFEND: '防 🛡', SKILL: '术 🔮' },
    moveHint: { ATTACK: '攻伐制胜', DEFEND: '以守代攻', SKILL: '诡术破防' },
    rps: '攻克术 · 术克防 · 防克攻',
    quizTitle: '禁墟答题',
    reactionTitle: '反应试炼',
    reactionDesc: '迷雾乍现的刹那，以最快之手按下封印。',
    accent: '#2ee6ff',
    accent2: '#b14dff',
    accent3: '#ff4d8d',
    glow: 'rgba(46,230,255,.55), rgba(177,77,255,.35)'
  },
  tomjerry: {
    key: 'tomjerry',
    label: '猫鼠',
    title: '猫鼠对决中心',
    subtitle: 'Tom 与 Jerry 的永恒追逐，谁更胜一筹？',
    duelist: '角色',
    moves: { ATTACK: '追 🐱', DEFEND: '躲 🐭', SKILL: '诱 🧀' },
    moveHint: { ATTACK: 'Tom 穷追不舍', DEFEND: 'Jerry 灵活闪避', SKILL: '奶酪诱饵破局' },
    rps: '追克诱 · 诱克躲 · 躲克追',
    quizTitle: '猫鼠知识赛',
    reactionTitle: '反应试炼',
    reactionDesc: '奶酪落地的瞬间，以闪电之手抢先一步。',
    accent: '#4aa3df',
    accent2: '#e0a458',
    accent3: '#7ec8e3',
    glow: 'rgba(74,163,223,.55), rgba(224,165,88,.35)'
  }
}

export function getTheme(key) {
  return GAME_THEMES[key] || GAME_THEMES.shenyu
}
