<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import FogBackground from '../components/FogBackground.vue'
import DuelArena from '../components/game/DuelArena.vue'
import QuizBattle from '../components/game/QuizBattle.vue'
import ReactionTrial from '../components/game/ReactionTrial.vue'
import RealmTrial from '../components/game/RealmTrial.vue'
import { getTheme } from '../game-theme'

const router = useRouter()
const THEME_KEY = 'zs_game_theme'
const theme = ref(localStorage.getItem(THEME_KEY) || 'shenyu')
const tab = ref('duel')

const T = computed(() => getTheme(theme.value))

// 主题驱动的根级 CSS 变量，三款游戏共用同一套配色
const rootStyle = computed(() => ({
  '--g-accent': T.value.accent,
  '--g-accent2': T.value.accent2,
  '--g-accent3': T.value.accent3,
  '--g-glow': T.value.glow
}))

function setTheme(k) {
  theme.value = k
  try { localStorage.setItem(THEME_KEY, k) } catch {}
}
function goHome() { router.push('/home') }

const tabs = [
  { key: 'duel', label: '对决擂台' },
  { key: 'quiz', label: '禁墟答题' },
  { key: 'reaction', label: '反应试炼' },
  { key: 'stage', label: '禁墟试炼' }
]

onMounted(() => {})
</script>

<template>
  <FogBackground />
  <main class="games" :style="rootStyle">
    <div class="topbar">
      <button class="back" @click="goHome">← 返回神域</button>
      <div class="brand">
        <h1>{{ T.title }}</h1>
        <p>{{ T.subtitle }}</p>
      </div>
      <div class="theme-switch">
        <button :class="{ on: theme === 'shenyu' }" @click="setTheme('shenyu')">神域</button>
        <button :class="{ on: theme === 'tomjerry' }" @click="setTheme('tomjerry')">猫鼠</button>
      </div>
    </div>

    <nav class="tabs">
      <button
        v-for="t in tabs"
        :key="t.key"
        :class="{ on: tab === t.key }"
        @click="tab = t.key"
      >{{ t.label }}</button>
    </nav>

    <section class="content">
      <DuelArena v-if="tab === 'duel'" :theme="theme" />
      <QuizBattle v-else-if="tab === 'quiz'" :theme="theme" />
      <ReactionTrial v-else-if="tab === 'reaction'" :theme="theme" />
      <RealmTrial v-else :theme="theme" />
    </section>
  </main>
</template>

<style scoped>
.games {
  position: relative; z-index: 2;
  min-height: 100vh; overflow-y: auto;
  padding: 0 clamp(16px, 4vw, 48px) 60px;
}
.games::-webkit-scrollbar { width: 8px; }
.games::-webkit-scrollbar-thumb { background: rgba(120,150,255,.25); border-radius: 8px; }
.games::-webkit-scrollbar-track { background: transparent; }

.topbar {
  position: sticky; top: 0; z-index: 5;
  display: flex; align-items: center; gap: 18px;
  padding: 16px 0; margin-bottom: 12px;
  background: linear-gradient(180deg, rgba(4,5,13,.85), rgba(4,5,13,.4) 70%, transparent);
  backdrop-filter: blur(10px);
  flex-wrap: wrap;
}
.back { border: 1px solid var(--glass-bd); background: rgba(255,255,255,.03); color: var(--ink); padding: 9px 16px; border-radius: 10px; cursor: pointer; font-family: inherit; font-size: 13px; transition: .2s; }
.back:hover { border-color: var(--g-accent); color: #fff; }
.brand { flex: 1; min-width: 200px; }
.brand h1 { font-size: 22px; font-weight: 800; letter-spacing: .08em; margin: 0;
  background: linear-gradient(100deg, var(--g-accent), var(--g-accent2), var(--g-accent3));
  -webkit-background-clip: text; background-clip: text; color: transparent; }
.brand p { margin: 4px 0 0; color: var(--ink-dim); font-size: 13px; letter-spacing: .04em; }

.theme-switch { display: inline-flex; border: 1px solid var(--glass-bd); border-radius: 999px; overflow: hidden; background: rgba(8,10,24,.5); }
.theme-switch button { border: 0; background: transparent; color: var(--ink-dim); padding: 8px 18px; cursor: pointer; font-family: inherit; font-size: 13px; letter-spacing: .06em; transition: .2s; }
.theme-switch button.on { color: #04050d; font-weight: 700; background: linear-gradient(100deg, var(--g-accent), var(--g-accent3)); }

.tabs { display: flex; gap: 8px; margin-bottom: 18px; flex-wrap: wrap; }
.tabs button { border: 1px solid var(--glass-bd); background: rgba(8,10,24,.4); color: var(--ink-dim); padding: 9px 20px; border-radius: 11px; cursor: pointer; font-family: inherit; font-size: 14px; letter-spacing: .06em; transition: .2s; }
.tabs button:hover { color: #fff; border-color: var(--g-accent); }
.tabs button.on { color: #fff; border-color: transparent; background: linear-gradient(100deg, rgba(46,230,255,.16), rgba(177,77,255,.16)); box-shadow: 0 0 16px var(--g-glow); }

.content { animation: fade .35s ease; }
@keyframes fade { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: none; } }

@media (prefers-reduced-motion: reduce) {
  * { animation-duration: .001ms !important; animation-iteration-count: 1 !important; transition-duration: .001ms !important; }
}
</style>
