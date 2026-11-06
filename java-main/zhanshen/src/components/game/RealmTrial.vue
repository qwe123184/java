<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTheme } from '../../game-theme'
import { stageProgress, stageStart, stageSubmit, stageLeaderboard } from '../../api/game'

const props = defineProps({ theme: { type: String, default: 'shenyu' } })
const T = computed(() => getTheme(props.theme))

// 与后端 RealmStageConfig 对齐的境界链（仅用于地图展示）
const REALMS = [
  { key: 'fanchen', name: '凡尘' },
  { key: 'chi', name: '池境' },
  { key: 'zhan', name: '盏境' },
  { key: 'chuan', name: '川境' },
  { key: 'hai', name: '海境' },
  { key: 'wuliang', name: '无量' },
  { key: 'kelain', name: '克莱因' },
  { key: 'shenming', name: '神明' }
]

const phase = ref('map')        // map | quiz | result
const progress = ref(null)
const items = ref([])
const currentQ = ref(0)
const picked = ref({})
const result = ref(null)
const board = ref([])
const loading = ref(false)
const showBreakthrough = ref(false)
const toast = ref({ show: false, msg: '' })
let toastTimer

const q = computed(() => items.value[currentQ.value] || null)
const answeredCount = computed(() => Object.keys(picked.value).length)
const allAnswered = computed(() => answeredCount.value === items.value.length)

function showToast(msg) {
  toast.value = { show: true, msg }
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value.show = false), 2400)
}

function realmStatus(index) {
  if (!progress.value) return 'locked'
  const curIdx = progress.value.currentRealmIndex
  if (index < curIdx) return 'cleared'
  if (index === curIdx) return 'current'
  return 'locked'
}

async function loadProgress() {
  try {
    const { data } = await stageProgress()
    if (data?.code === 0) progress.value = data.data
  } catch (e) {
    showToast(e?.response?.data?.message || '加载进度失败')
  }
}

async function loadBoard() {
  try {
    const { data } = await stageLeaderboard()
    if (data?.code === 0) board.value = data.data || []
  } catch (e) {}
}

async function start() {
  loading.value = true
  try {
    const { data } = await stageStart()
    if (data?.code === 0) {
      items.value = data.data.items || []
      currentQ.value = 0
      picked.value = {}
      result.value = null
      phase.value = 'quiz'
    } else {
      showToast(data?.msg || '无法开启试炼')
    }
  } catch (e) {
    showToast(e?.response?.data?.message || '开启试炼失败')
  } finally {
    loading.value = false
  }
}

function choose(choice) {
  if (!q.value) return
  picked.value = { ...picked.value, [q.value.id]: choice }
}

async function submit() {
  loading.value = true
  try {
    const answers = Object.entries(picked.value).map(([questionId, choice]) => ({
      questionId: Number(questionId),
      choice
    }))
    const { data } = await stageSubmit(progress.value.currentRealmKey, answers)
    if (data?.code === 0) {
      result.value = data.data
      phase.value = 'result'
      await loadProgress()
      if (data.data.passed) triggerBreakthrough()
    } else {
      showToast(data?.msg || '提交失败')
    }
  } catch (e) {
    showToast(e?.response?.data?.message || '提交失败')
  } finally {
    loading.value = false
  }
}

function triggerBreakthrough() {
  showBreakthrough.value = true
  setTimeout(() => (showBreakthrough.value = false), 1900)
}

function again() {
  start()
}
function backToMap() {
  phase.value = 'map'
  loadProgress()
}

onMounted(() => {
  loadProgress()
  loadBoard()
})
</script>

<template>
  <div class="rt">
    <div class="intro">
      <h3>禁墟试炼 · 境界突破</h3>
      <p>以守夜人之躯，逐境突破。答对试炼之题，引灵入体，证得更高之境。</p>
    </div>

    <div class="stats" v-if="progress">
      <div class="stat"><span class="k">已突破</span><span class="v">{{ progress.clears }}</span></div>
      <div class="stat"><span class="k">经验</span><span class="v">{{ progress.exp }}</span></div>
      <div class="stat"><span class="k">最佳连对</span><span class="v">{{ progress.bestStreak }}</span></div>
      <div class="stat"><span class="k">进度</span><span class="v">{{ progress.progressPercent }}%</span></div>
    </div>

    <section class="panel map">
      <h3>境界之路</h3>
      <div class="realms">
        <div v-for="(r, i) in REALMS" :key="r.key" class="realm" :class="realmStatus(i)">
          <span class="dot"></span>
          <span class="nm">{{ r.name }}</span>
          <span class="tag">{{ realmStatus(i) === 'cleared' ? '已破' : realmStatus(i) === 'current' ? '试炼中' : '未启' }}</span>
        </div>
      </div>
      <div class="bar"><div class="fill" :style="{ width: (progress ? progress.progressPercent : 0) + '%' }"></div></div>
      <p class="desc" v-if="progress">{{ progress.realmDesc }}</p>
    </section>

    <div v-if="phase === 'map'" class="action">
      <button class="primary" :disabled="loading" @click="start">
        {{ progress && progress.allCleared ? '重历神明试炼' : '开启「' + (progress ? progress.currentRealmName : '') + '」试炼' }}
      </button>
    </div>

    <section v-if="phase === 'quiz'" class="panel quiz">
      <div class="qhead">
        <span class="qidx">第 {{ currentQ + 1 }} / {{ items.length }} 题</span>
        <span class="qneed">需答对 {{ progress.passThreshold }} 题方可突破</span>
      </div>
      <div class="qcard">
        <h4>{{ q?.question }}</h4>
        <div class="opts">
          <button v-for="opt in ['A', 'B', 'C', 'D']" :key="opt" class="opt" :class="{ on: picked[q?.id] === opt }" @click="choose(opt)">
            <span class="lab">{{ opt }}</span><span class="txt">{{ q && q['option' + opt] }}</span>
          </button>
        </div>
      </div>
      <button class="primary next" :disabled="!allAnswered || loading" @click="submit">
        {{ answeredCount === items.length ? '提交试炼' : '请答完全部题目（' + answeredCount + '/' + items.length + '）' }}
      </button>
    </section>

    <section v-if="phase === 'result' && result" class="panel result">
      <div class="verdict" :class="{ pass: result.passed }">
        <h3>{{ result.passed ? '突破成功' : '突破未竟' }}</h3>
        <p>{{ result.message }}</p>
      </div>
      <div class="rstats">
        <span>答对 {{ result.correctCount }}/{{ result.total }}</span>
        <span>突破值 +{{ result.breakthroughValue }}</span>
        <span v-if="result.passed && result.nextRealmName">下一境：{{ result.nextRealmName }}</span>
      </div>
      <div class="raction">
        <button class="primary" @click="again">{{ result.passed ? (result.allCleared ? '再战神明' : '继续下一境') : '重新试炼' }}</button>
        <button class="ghost" @click="backToMap">返回境界之路</button>
      </div>
    </section>

    <section class="panel board">
      <h3>神域境界榜（按已突破境界排序）</h3>
      <ol v-if="board.length" class="lb">
        <li v-for="u in board" :key="u.userId">
          <span class="rk">{{ u.rank }}</span>
          <span class="nm">{{ u.nickname || u.username }}</span>
          <span class="sc">{{ REALMS[u.score - 1] ? REALMS[u.score - 1].name : '凡尘' }}</span>
          <span class="sub">{{ u.sub }}</span>
        </li>
      </ol>
      <div v-else class="empty">还没有人踏上境界之路，来做第一个守夜人！</div>
    </section>

    <transition name="pop">
      <div v-if="showBreakthrough" class="breakthrough">
        <div class="ring"></div>
        <div class="word">突破！</div>
      </div>
    </transition>

    <transition name="toast">
      <div v-if="toast.show" class="toast">{{ toast.msg }}</div>
    </transition>
  </div>
</template>

<style scoped>
.rt { max-width: 760px; margin: 0 auto; display: flex; flex-direction: column; gap: 18px; }
.intro { text-align: center; }
.intro h3 { margin: 0 0 8px; font-size: 20px; color: #fff; letter-spacing: .1em;
  background: linear-gradient(100deg, var(--g-accent), var(--g-accent2), var(--g-accent3));
  -webkit-background-clip: text; background-clip: text; color: transparent; }
.intro p { margin: 0; color: var(--ink-dim); font-size: 13.5px; }

.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.stat { padding: 14px; border-radius: 14px; background: var(--glass); border: 1px solid var(--glass-bd); text-align: center; }
.stat .k { display: block; font-size: 12px; color: var(--ink-dim); letter-spacing: .08em; margin-bottom: 6px; }
.stat .v { font-size: 22px; font-weight: 800; color: #fff; }

.panel { padding: 20px; border-radius: 16px; background: var(--glass); border: 1px solid var(--glass-bd); backdrop-filter: blur(16px); box-shadow: 0 18px 50px rgba(0,0,0,.4); }
.panel h3 { margin: 0 0 14px; font-size: 16px; color: #fff; letter-spacing: .08em; }

.realms { display: flex; flex-wrap: wrap; gap: 10px; justify-content: space-between; }
.realm { position: relative; flex: 1 1 80px; min-width: 76px; padding: 12px 8px; border-radius: 12px; text-align: center;
  border: 1px solid var(--glass-bd); background: rgba(8,10,24,.35); transition: .25s; }
.realm .dot { display: block; width: 12px; height: 12px; margin: 0 auto 8px; border-radius: 50%; background: rgba(120,150,255,.25); }
.realm .nm { display: block; font-size: 14px; color: var(--ink-dim); font-weight: 700; }
.realm .tag { display: block; font-size: 11px; color: var(--ink-dim); margin-top: 3px; opacity: .7; }
.realm.cleared { border-color: var(--g-accent); box-shadow: 0 0 14px var(--g-glow); }
.realm.cleared .dot { background: linear-gradient(135deg, var(--g-accent), var(--g-accent3)); }
.realm.cleared .nm { color: #fff; }
.realm.current { border-color: var(--g-accent3); background: rgba(177,77,255,.12); animation: pulse 1.6s ease-in-out infinite; }
.realm.current .dot { background: var(--g-accent3); }
.realm.current .nm { color: #fff; }
.realm.current .tag { color: var(--g-accent3); opacity: 1; }
@keyframes pulse { 0%,100% { box-shadow: 0 0 10px rgba(177,77,255,.3); } 50% { box-shadow: 0 0 22px rgba(177,77,255,.6); } }

.bar { height: 8px; border-radius: 999px; background: rgba(8,10,24,.6); overflow: hidden; margin-top: 14px; }
.bar .fill { height: 100%; border-radius: 999px; background: linear-gradient(100deg, var(--g-accent), var(--g-accent2), var(--g-accent3)); transition: width .6s ease; box-shadow: 0 0 14px var(--g-glow); }
.desc { margin: 14px 0 0; color: var(--ink-dim); font-size: 13.5px; line-height: 1.7; text-align: center; }

.action { display: flex; justify-content: center; }
.primary { border: 0; cursor: pointer; font-family: inherit; font-size: 15px; font-weight: 800; letter-spacing: .06em;
  color: #04050d; padding: 14px 30px; border-radius: 13px; background: linear-gradient(100deg, var(--g-accent), var(--g-accent3)); box-shadow: 0 10px 30px var(--g-glow); transition: .2s; }
.primary:hover { transform: translateY(-2px); }
.primary:disabled { opacity: .5; cursor: not-allowed; transform: none; }
.ghost { border: 1px solid var(--glass-bd); background: transparent; color: var(--ink); padding: 14px 24px; border-radius: 13px; cursor: pointer; font-family: inherit; font-size: 14px; transition: .2s; }
.ghost:hover { border-color: var(--g-accent); color: #fff; }

.qhead { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; color: var(--ink-dim); font-size: 13px; }
.qhead .qneed { color: var(--g-accent3); }
.qcard h4 { margin: 0 0 16px; font-size: 17px; color: #fff; line-height: 1.6; font-weight: 700; }
.opts { display: flex; flex-direction: column; gap: 10px; }
.opt { display: flex; align-items: center; gap: 12px; text-align: left; cursor: pointer; font-family: inherit; font-size: 14.5px;
  color: var(--ink); padding: 14px 16px; border-radius: 12px; border: 1px solid var(--glass-bd); background: rgba(8,10,24,.4); transition: .18s; }
.opt:hover { border-color: var(--g-accent); }
.opt.on { border-color: var(--g-accent); background: rgba(46,230,255,.12); color: #fff; box-shadow: 0 0 14px var(--g-glow); }
.opt .lab { width: 26px; height: 26px; flex: 0 0 26px; display: grid; place-items: center; border-radius: 8px; font-weight: 800; background: rgba(120,150,255,.1); color: var(--g-accent); }
.opt.on .lab { background: linear-gradient(135deg, var(--g-accent), var(--g-accent3)); color: #04050d; }
.next { width: 100%; margin-top: 16px; }

.result .verdict { text-align: center; padding: 8px 0 16px; }
.result .verdict h3 { margin: 0 0 8px; font-size: 24px; color: var(--pink); letter-spacing: .1em; }
.result .verdict.pass h3 { color: #5cffb0; }
.result .verdict p { margin: 0; color: var(--ink-dim); font-size: 13.5px; line-height: 1.7; }
.rstats { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; margin: 14px 0 18px; }
.rstats span { padding: 7px 14px; border-radius: 10px; background: rgba(8,10,24,.5); color: var(--g-accent); font-size: 13px; font-weight: 600; }
.raction { display: flex; gap: 12px; justify-content: center; flex-wrap: wrap; }

.board .lb { list-style: none; display: flex; flex-direction: column; gap: 7px; }
.board .lb li { display: grid; grid-template-columns: 34px 1fr auto auto; align-items: center; gap: 12px; padding: 8px 12px; border-radius: 10px; background: rgba(8,10,24,.35); }
.board .lb .rk { width: 26px; height: 26px; display: grid; place-items: center; border-radius: 8px; font-weight: 800; color: var(--ink-dim); background: rgba(120,150,255,.08); }
.board .lb li:nth-child(1) .rk { background: linear-gradient(135deg,#ffe9a8,#f5d27a); color: #04050d; }
.board .lb li:nth-child(2) .rk { background: linear-gradient(135deg,#e6ecff,#aeb8d8); color: #04050d; }
.board .lb li:nth-child(3) .rk { background: linear-gradient(135deg,#ffcd9e,#d99157); color: #04050d; }
.board .nm { color: #fff; font-size: 13.5px; }
.board .sc { color: var(--g-accent); font-weight: 700; font-size: 13px; }
.board .sub { color: var(--ink-dim); font-size: 12px; }
.empty { color: var(--ink-dim); font-size: 13.5px; padding: 14px 0; }

.breakthrough { position: fixed; inset: 0; z-index: 40; display: grid; place-items: center; pointer-events: none; }
.breakthrough .ring { position: absolute; width: 320px; height: 320px; border-radius: 50%; border: 3px solid var(--g-accent3); animation: ring .9s ease-out forwards; }
.breakthrough .word { font-size: 56px; font-weight: 900; letter-spacing: .1em; color: #fff;
  background: linear-gradient(100deg, var(--g-accent), var(--g-accent2), var(--g-accent3)); -webkit-background-clip: text; background-clip: text; color: transparent; animation: wordp .9s ease-out forwards; }
@keyframes ring { from { transform: scale(.2); opacity: .9; } to { transform: scale(1.4); opacity: 0; } }
@keyframes wordp { from { transform: scale(.5); opacity: 0; } to { transform: scale(1); opacity: 1; } }

.toast { position: fixed; left: 50%; bottom: 34px; transform: translateX(-50%); background: rgba(10,14,32,.92); border: 1px solid var(--pink); color: #ffd9e6; padding: 12px 22px; border-radius: 12px; font-size: 14px; z-index: 50; box-shadow: 0 10px 40px rgba(0,0,0,.5); }
.toast-enter-active, .toast-leave-active { transition: .35s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(-50%) translateY(40px); }

@media (prefers-reduced-motion: reduce) {
  * { animation-duration: .001ms !important; animation-iteration-count: 1 !important; transition-duration: .001ms !important; }
}
</style>
