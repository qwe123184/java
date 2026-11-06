<script setup>
import { ref, computed, onBeforeUnmount, onMounted } from 'vue'
import { getTheme } from '../../game-theme'
import { reactionSubmit, reactionLeaderboard } from '../../api/game'

const props = defineProps({ theme: { type: String, default: 'shenyu' } })
const T = computed(() => getTheme(props.theme))

const phase = ref('idle') // idle | waiting | go | done | early
const reactionMs = ref(0)
const bestMs = ref(0)
const plays = ref(0)
const rank = ref(0)
const leaderboard = ref([])
let timer = null
let startedAt = 0

const toast = ref({ show: false, msg: '' })
let toastTimer
function showToast(msg) {
  toast.value = { show: true, msg }
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value.show = false), 2200)
}

function start() {
  clearTimeout(timer)
  phase.value = 'waiting'
  const delay = 1500 + Math.random() * 2800
  timer = setTimeout(() => {
    phase.value = 'go'
    startedAt = performance.now()
  }, delay)
}

function hit() {
  if (phase.value === 'idle' || phase.value === 'done') { start(); return }
  if (phase.value === 'waiting') {
    clearTimeout(timer)
    phase.value = 'early'
    showToast('太心急了！等绿色信号出现再点。')
    return
  }
  if (phase.value === 'go') {
    const ms = Math.round(performance.now() - startedAt)
    reactionMs.value = ms
    phase.value = 'done'
    submit(ms)
  }
}

async function submit(ms) {
  try {
    const { data } = await reactionSubmit(ms)
    if (data?.code === 0) {
      bestMs.value = data.data.bestMs
      plays.value = data.data.plays
      rank.value = data.data.rank
    }
  } catch (e) {
    showToast(e?.response?.data?.message || '提交失败。')
  }
}

async function refreshBoard() {
  try { const { data } = await reactionLeaderboard(); if (data?.code === 0) leaderboard.value = data.data || [] } catch {}
}

function verdict(ms) {
  if (ms <= 200) return '神之手！电光石火'
  if (ms <= 320) return '守夜人级反应'
  if (ms <= 500) return '稳健'
  return '再练练手速'
}

onMounted(refreshBoard)
onBeforeUnmount(() => clearTimeout(timer))
</script>

<template>
  <div class="rt">
    <div class="intro">
      <h3>{{ T.reactionTitle }}</h3>
      <p>{{ T.reactionDesc }}</p>
    </div>

    <div class="pad" :class="phase" @click="hit">
      <div v-if="phase === 'idle'" class="big">点击开始试炼</div>
      <div v-else-if="phase === 'waiting'" class="big warn">等待信号…</div>
      <div v-else-if="phase === 'go'" class="big go">点击！</div>
      <div v-else-if="phase === 'early'" class="big warn">太早了！</div>
      <div v-else class="big done">
        <span class="ms">{{ reactionMs }}</span><span class="unit">ms</span>
        <small>{{ verdict(reactionMs) }}</small>
      </div>
    </div>

    <div class="stats">
      <div class="stat"><span class="k">最佳</span><span class="v">{{ bestMs || '—' }}<i>ms</i></span></div>
      <div class="stat"><span class="k">试炼次数</span><span class="v">{{ plays }}</span></div>
      <div class="stat"><span class="k">当前排名</span><span class="v">{{ rank || '—' }}</span></div>
    </div>

    <section class="panel board">
      <h3>反应试炼排行榜（越快越靠前）</h3>
      <ol v-if="leaderboard.length" class="lb">
        <li v-for="u in leaderboard" :key="u.userId">
          <span class="rk">{{ u.rank }}</span>
          <span class="nm">{{ u.nickname || u.username }}</span>
          <span class="sc">{{ u.score }} ms</span>
          <span class="sub">{{ u.sub }}</span>
        </li>
      </ol>
      <div v-else class="empty">还没有人挑战反应试炼，来做第一个！</div>
    </section>

    <transition name="toast">
      <div v-if="toast.show" class="toast">{{ toast.msg }}</div>
    </transition>
  </div>
</template>

<style scoped>
.rt { max-width: 720px; margin: 0 auto; display: flex; flex-direction: column; gap: 18px; }
.intro { text-align: center; }
.intro h3 { margin: 0 0 8px; font-size: 20px; color: #fff; letter-spacing: .1em;
  background: linear-gradient(100deg, var(--g-accent), var(--g-accent2), var(--g-accent3));
  -webkit-background-clip: text; background-clip: text; color: transparent; }
.intro p { margin: 0; color: var(--ink-dim); font-size: 13.5px; }

.pad { height: 240px; border-radius: 20px; display: grid; place-items: center; cursor: pointer;
  border: 1px solid var(--glass-bd); background: var(--glass); backdrop-filter: blur(16px);
  transition: background .15s, box-shadow .2s, border-color .2s; user-select: none; }
.pad.idle { box-shadow: 0 18px 50px rgba(0,0,0,.4); }
.pad.waiting { background: linear-gradient(160deg, rgba(255,77,141,.22), rgba(255,77,141,.08)); border-color: rgba(255,77,141,.5); }
.pad.go { background: linear-gradient(160deg, rgba(92,255,176,.32), rgba(46,230,255,.14)); border-color: #5cffb0; box-shadow: 0 0 40px rgba(92,255,176,.4); }
.pad.early { background: linear-gradient(160deg, rgba(255,77,141,.3), rgba(255,77,141,.1)); border-color: var(--pink); }
.pad.done { background: var(--glass); }
.big { text-align: center; font-weight: 800; letter-spacing: .04em; color: #fff; }
.big.idle { font-size: 22px; color: var(--ink); }
.big.warn { font-size: 26px; color: var(--pink); }
.big.go { font-size: 34px; color: #5cffb0; animation: pop .25s ease; }
.big.done .ms { font-size: 46px; background: linear-gradient(100deg, var(--g-accent), var(--g-accent3)); -webkit-background-clip: text; background-clip: text; color: transparent; }
.big.done .unit { font-size: 18px; color: var(--ink-dim); margin-left: 6px; }
.big.done small { display: block; margin-top: 8px; font-size: 13px; color: var(--g-accent); font-weight: 600; }
@keyframes pop { from { transform: scale(.8); opacity: .4; } to { transform: scale(1); opacity: 1; } }

.stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.stat { padding: 14px; border-radius: 14px; background: var(--glass); border: 1px solid var(--glass-bd); text-align: center; }
.stat .k { display: block; font-size: 12px; color: var(--ink-dim); letter-spacing: .08em; margin-bottom: 6px; }
.stat .v { font-size: 22px; font-weight: 800; color: #fff; }
.stat .v i { font-size: 12px; color: var(--ink-dim); font-style: normal; margin-left: 3px; }

.panel { padding: 20px; border-radius: 16px; background: var(--glass); border: 1px solid var(--glass-bd); backdrop-filter: blur(16px); box-shadow: 0 18px 50px rgba(0,0,0,.4); }
.panel h3 { margin: 0 0 14px; font-size: 16px; color: #fff; letter-spacing: .08em; }
.empty { color: var(--ink-dim); font-size: 13.5px; padding: 14px 0; }
.lb { list-style: none; display: flex; flex-direction: column; gap: 7px; }
.lb li { display: grid; grid-template-columns: 34px 1fr auto auto; align-items: center; gap: 12px; padding: 8px 12px; border-radius: 10px; background: rgba(8,10,24,.35); }
.lb .rk { width: 26px; height: 26px; display: grid; place-items: center; border-radius: 8px; font-weight: 800; color: var(--ink-dim); background: rgba(120,150,255,.08); }
.lb li:nth-child(1) .rk { background: linear-gradient(135deg,#ffe9a8,#f5d27a); color: #04050d; }
.lb li:nth-child(2) .rk { background: linear-gradient(135deg,#e6ecff,#aeb8d8); color: #04050d; }
.lb li:nth-child(3) .rk { background: linear-gradient(135deg,#ffcd9e,#d99157); color: #04050d; }
.lb .nm { color: #fff; font-size: 13.5px; }
.lb .sc { color: var(--g-accent); font-weight: 700; font-size: 13px; }
.lb .sub { color: var(--ink-dim); font-size: 12px; }

.toast { position: fixed; left: 50%; bottom: 34px; transform: translateX(-50%); background: rgba(10,14,32,.92); border: 1px solid var(--pink); color: #ffd9e6; padding: 12px 22px; border-radius: 12px; font-size: 14px; z-index: 20; box-shadow: 0 10px 40px rgba(0,0,0,.5); }
.toast-enter-active, .toast-leave-active { transition: .35s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(-50%) translateY(40px); }
</style>
