<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTheme } from '../../game-theme'
import {
  duelCreate, duelAccept, duelMove, duelRoom, duelMine, duelLeaderboard
} from '../../api/game'

const props = defineProps({ theme: { type: String, default: 'shenyu' } })
const T = computed(() => getTheme(props.theme))

const opponent = ref('')
const targetWins = ref(3)
const myRooms = ref([])
const activeRoom = ref(null)
const leaderboard = ref([])
const busy = ref(false)
const toast = ref({ show: false, msg: '', ok: true })
let toastTimer
function showToast(msg, ok = true) {
  toast.value = { show: true, msg, ok }
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value.show = false), 2600)
}

async function refreshMine() {
  try {
    const { data } = await duelMine()
    if (data?.code === 0) myRooms.value = data.data || []
  } catch { /* 忽略 */ }
}
async function refreshBoard() {
  try {
    const { data } = await duelLeaderboard()
    if (data?.code === 0) leaderboard.value = data.data || []
  } catch { /* 忽略 */ }
}

async function createDuel() {
  if (busy.value) return
  if (!opponent.value.trim()) return showToast('请填写对手代号。', false)
  busy.value = true
  try {
    const { data } = await duelCreate({
      opponentUsername: opponent.value.trim(),
      theme: props.theme,
      targetWins: targetWins.value
    })
    if (data?.code === 0) {
      showToast('对决邀约已发出，等待对手应战。')
      opponent.value = ''
      await refreshMine()
      openRoom(data.data.id)
    } else {
      showToast(data?.message || '发起失败。', false)
    }
  } catch (e) {
    showToast(e?.response?.data?.message || '发起失败。', false)
  } finally { busy.value = false }
}

async function accept(room) {
  try {
    const { data } = await duelAccept(room.id)
    if (data?.code === 0) { showToast('应战成功，对决开始！'); await refreshMine(); openRoom(room.id) }
    else showToast(data?.message || '应战失败。', false)
  } catch (e) { showToast(e?.response?.data?.message || '应战失败。', false) }
}

async function openRoom(id) {
  try {
    const { data } = await duelRoom(id)
    if (data?.code === 0) activeRoom.value = data.data
  } catch { showToast('房间已消散。', false) }
}
function back() { activeRoom.value = null }

async function playMove(choice) {
  if (!activeRoom.value) return
  try {
    const { data } = await duelMove(activeRoom.value.id, choice)
    if (data?.code === 0) {
      activeRoom.value = data.data
      await refreshMine()
    } else showToast(data?.message || '出手失败。', false)
  } catch (e) { showToast(e?.response?.data?.message || '出手失败。', false) }
}

const isChallenger = computed(() => activeRoom.value?.myRole === 'P1')
const canMove = computed(() => activeRoom.value?.status === 'PLAYING' && !activeRoom.value.myChoice)
const waitingOpp = computed(() => activeRoom.value?.status === 'PLAYING' && activeRoom.value.myChoice)
const iWon = computed(() => activeRoom.value?.status === 'FINISHED' && activeRoom.value.winnerRole === activeRoom.value.myRole)

function roleName(room) {
  return room.myRole === 'P1' ? room.challengerName : room.opponentName
}
function oppName(room) {
  return room.myRole === 'P1' ? room.opponentName : room.challengerName
}

onMounted(() => { refreshMine(); refreshBoard() })
</script>

<template>
  <div class="arena">
    <!-- 列表视图 -->
    <div v-if="!activeRoom" class="wrap">
      <section class="panel create">
        <h3>发起对决</h3>
        <p class="hint">{{ T.rps }}</p>
        <div class="row">
          <input v-model="opponent" placeholder="对手代号（如 linqiye）" @keyup.enter="createDuel" />
          <select v-model.number="targetWins">
            <option :value="2">2 胜</option>
            <option :value="3">3 胜</option>
            <option :value="5">5 胜</option>
          </select>
          <button class="go" :disabled="busy" @click="createDuel">发起挑战</button>
        </div>
      </section>

      <section class="panel list">
        <h3>我的对局</h3>
        <div v-if="!myRooms.length" class="empty">还没有对局，向好友发起一场禁墟博弈吧。</div>
        <ul v-else class="rooms">
          <li v-for="r in myRooms" :key="r.id" class="room">
            <div class="meta">
              <span class="code">#{{ r.roomCode }}</span>
              <span class="vs">{{ roleName(r) }} <b>VS</b> {{ oppName(r) }}</span>
              <span class="score">{{ r.p1Wins }} : {{ r.p2Wins }}（{{ r.targetWins }}胜）</span>
            </div>
            <div class="acts">
              <span class="badge" :class="r.status.toLowerCase()">{{ r.status }}</span>
              <button v-if="r.status === 'WAITING' && r.myRole === 'P2'" class="accept" @click="accept(r)">应战</button>
              <button class="view" @click="openRoom(r.id)">查看</button>
            </div>
          </li>
        </ul>
      </section>

      <section class="panel board">
        <h3>对决排行榜</h3>
        <ol v-if="leaderboard.length" class="lb">
          <li v-for="u in leaderboard" :key="u.userId">
            <span class="rk">{{ u.rank }}</span>
            <span class="nm">{{ u.nickname || u.username }}</span>
            <span class="sc">{{ u.score }} 胜</span>
            <span class="sub">{{ u.sub }}</span>
          </li>
        </ol>
        <div v-else class="empty">暂无战绩。</div>
      </section>
    </div>

    <!-- 房间视图 -->
    <div v-else class="wrap room-view">
      <button class="back" @click="back">← 返回</button>
      <div class="scoreboard">
        <div class="side" :class="{ me: isChallenger }">
          <span class="who">{{ activeRoom.challengerName }}</span>
          <span class="wins">{{ activeRoom.p1Wins }}</span>
        </div>
        <div class="mid">
          <div class="vs">VS</div>
          <div class="target">先达 {{ activeRoom.targetWins }} 胜</div>
        </div>
        <div class="side" :class="{ me: !isChallenger }">
          <span class="who">{{ activeRoom.opponentName }}</span>
          <span class="wins">{{ activeRoom.p2Wins }}</span>
        </div>
      </div>

      <div v-if="activeRoom.status === 'WAITING'" class="state waiting">
        {{ isChallenger ? '已发出邀约，等待对手应战…' : '收到对决邀约，是否应战？' }}
        <button v-if="!isChallenger" class="accept big" @click="accept(activeRoom)">应战</button>
      </div>

      <div v-else-if="activeRoom.status === 'PLAYING'" class="state play">
        <div v-if="canMove" class="moves">
          <button v-for="m in ['ATTACK','DEFEND','SKILL']" :key="m" class="move" :class="m.toLowerCase()" @click="playMove(m)">
            <span class="gly">{{ T.moves[m] }}</span>
            <span class="tip">{{ T.moveHint[m] }}</span>
          </button>
        </div>
        <div v-else-if="waitingOpp" class="waiting-opp">你已出手（{{ T.moves[activeRoom.myChoice] }}），静候对手…</div>
      </div>

      <div v-else class="state over">
        <div class="result" :class="{ win: iWon, lose: !iWon }">
          {{ iWon ? '🏆 你赢得了这场对决！' : '对决落败，横刀再战。' }}
        </div>
      </div>

      <section class="rounds">
        <h4>回合战报</h4>
        <ul>
          <li v-for="(r, i) in activeRoom.rounds" :key="r.id" :class="r.winner?.toLowerCase()">
            <span class="no">R{{ i + 1 }}</span>
            <span class="c p1">{{ r.p1Choice || '—' }}</span>
            <span class="arrow">⚔</span>
            <span class="c p2">{{ r.p2Choice || '—' }}</span>
            <span class="win">{{ r.winner === 'P1' ? 'P1 胜' : r.winner === 'P2' ? 'P2 胜' : r.winner === 'DRAW' ? '平' : '' }}</span>
          </li>
        </ul>
      </section>
    </div>

    <transition name="toast">
      <div v-if="toast.show" class="toast" :class="toast.ok ? 'ok' : 'bad'">{{ toast.msg }}</div>
    </transition>
  </div>
</template>

<style scoped>
.arena { min-height: 100%; }
.wrap { max-width: 920px; margin: 0 auto; display: flex; flex-direction: column; gap: 18px; }
.panel { padding: 20px; border-radius: 16px; background: var(--glass); border: 1px solid var(--glass-bd); backdrop-filter: blur(16px); box-shadow: 0 18px 50px rgba(0,0,0,.4); }
.panel h3 { margin: 0 0 14px; font-size: 16px; color: #fff; letter-spacing: .08em; }
.hint { color: var(--ink-dim); font-size: 12.5px; margin: -8px 0 14px; letter-spacing: .04em; }
.create .row { display: flex; gap: 10px; flex-wrap: wrap; }
.create input { flex: 1; min-width: 180px; border: 1px solid var(--glass-bd); background: rgba(8,10,24,.5); color: #fff; border-radius: 11px; padding: 11px 13px; font-family: inherit; font-size: 14px; outline: none; }
.create input:focus { border-color: transparent; box-shadow: 0 0 0 3px rgba(46,230,255,.12), 0 0 22px rgba(46,230,255,.2); }
.create select { border: 1px solid var(--glass-bd); background: rgba(8,10,24,.5); color: #fff; border-radius: 11px; padding: 11px 12px; font-family: inherit; }
.go { border: 0; border-radius: 11px; padding: 11px 22px; cursor: pointer; font-weight: 700; letter-spacing: .1em; color: #04050d; font-family: inherit; background: linear-gradient(100deg, var(--g-accent), var(--g-accent2) 55%, var(--g-accent3)); box-shadow: 0 0 18px var(--g-glow); transition: .2s; }
.go:hover { filter: brightness(1.08); }
.go:disabled { opacity: .6; cursor: not-allowed; }

.empty { color: var(--ink-dim); font-size: 13.5px; padding: 14px 0; }
.rooms { list-style: none; display: flex; flex-direction: column; gap: 10px; }
.room { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 12px 14px; border-radius: 12px; background: rgba(8,10,24,.4); border: 1px solid var(--glass-bd); flex-wrap: wrap; }
.meta { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.code { font-size: 12px; color: var(--g-accent); letter-spacing: .1em; }
.vs { color: #fff; font-size: 13.5px; }
.vs b { color: var(--g-accent3); margin: 0 4px; }
.score { font-size: 12.5px; color: var(--ink-dim); }
.acts { display: flex; align-items: center; gap: 8px; }
.badge { font-size: 11px; padding: 3px 10px; border-radius: 999px; letter-spacing: .08em; }
.badge.waiting { color: var(--gold); border: 1px solid rgba(245,210,122,.35); background: rgba(245,210,122,.08); }
.badge.playing { color: var(--g-accent); border: 1px solid rgba(46,230,255,.35); background: rgba(46,230,255,.08); }
.badge.finished { color: var(--pink); border: 1px solid rgba(255,77,141,.35); background: rgba(255,77,141,.08); }
.accept, .view { border: 1px solid var(--glass-bd); background: rgba(255,255,255,.03); color: var(--ink); padding: 7px 14px; border-radius: 9px; cursor: pointer; font-family: inherit; font-size: 12.5px; transition: .2s; }
.accept { color: var(--g-accent); border-color: rgba(46,230,255,.35); }
.accept:hover { color: #fff; border-color: var(--g-accent); box-shadow: 0 0 14px var(--g-glow); }
.view:hover { color: #fff; border-color: var(--g-accent); }

.lb { list-style: none; display: flex; flex-direction: column; gap: 7px; }
.lb li { display: grid; grid-template-columns: 34px 1fr auto auto; align-items: center; gap: 12px; padding: 8px 12px; border-radius: 10px; background: rgba(8,10,24,.35); }
.lb .rk { width: 26px; height: 26px; display: grid; place-items: center; border-radius: 8px; font-weight: 800; color: var(--ink-dim); background: rgba(120,150,255,.08); }
.lb li:nth-child(1) .rk { background: linear-gradient(135deg,#ffe9a8,#f5d27a); color: #04050d; }
.lb li:nth-child(2) .rk { background: linear-gradient(135deg,#e6ecff,#aeb8d8); color: #04050d; }
.lb li:nth-child(3) .rk { background: linear-gradient(135deg,#ffcd9e,#d99157); color: #04050d; }
.lb .nm { color: #fff; font-size: 13.5px; }
.lb .sc { color: var(--g-accent); font-weight: 700; font-size: 13px; }
.lb .sub { color: var(--ink-dim); font-size: 12px; }

/* 房间视图 */
.room-view { gap: 16px; }
.back { align-self: flex-start; border: 1px solid var(--glass-bd); background: rgba(255,255,255,.03); color: var(--ink); padding: 8px 16px; border-radius: 10px; cursor: pointer; font-family: inherit; font-size: 13px; }
.back:hover { border-color: var(--g-accent); color: #fff; }
.scoreboard { display: grid; grid-template-columns: 1fr auto 1fr; align-items: center; gap: 16px; padding: 20px; border-radius: 16px; background: var(--glass); border: 1px solid var(--glass-bd); backdrop-filter: blur(16px); }
.side { text-align: center; padding: 10px; border-radius: 12px; border: 1px solid transparent; }
.side.me { border-color: rgba(46,230,255,.4); background: rgba(46,230,255,.06); box-shadow: 0 0 20px rgba(46,230,255,.15); }
.side .who { display: block; color: #fff; font-size: 14px; margin-bottom: 6px; }
.side .wins { font-size: 38px; font-weight: 800; background: linear-gradient(100deg, var(--g-accent), var(--g-accent3)); -webkit-background-clip: text; background-clip: text; color: transparent; }
.mid { text-align: center; }
.mid .vs { font-size: 20px; font-weight: 800; color: var(--g-accent3); letter-spacing: .1em; }
.mid .target { font-size: 12px; color: var(--ink-dim); margin-top: 4px; }

.state { text-align: center; padding: 16px; }
.waiting { color: var(--ink-dim); }
.waiting .accept.big { display: block; margin: 14px auto 0; font-size: 15px; padding: 12px 30px; }
.moves { display: flex; gap: 14px; justify-content: center; flex-wrap: wrap; }
.move { width: 130px; padding: 18px 10px; border-radius: 16px; cursor: pointer; font-family: inherit; color: #fff; background: rgba(8,10,24,.45); border: 1px solid var(--glass-bd); display: flex; flex-direction: column; gap: 8px; align-items: center; transition: .2s; }
.move .gly { font-size: 22px; }
.move .tip { font-size: 11.5px; color: var(--ink-dim); }
.move:hover { transform: translateY(-4px); }
.move.attack { border-color: rgba(255,77,141,.5); box-shadow: 0 0 22px rgba(255,77,141,.25); }
.move.defend { border-color: rgba(46,230,255,.5); box-shadow: 0 0 22px rgba(46,230,255,.25); }
.move.skill { border-color: rgba(177,77,255,.5); box-shadow: 0 0 22px rgba(177,77,255,.25); }
.move.attack:hover { box-shadow: 0 0 30px rgba(255,77,141,.5); }
.move.defend:hover { box-shadow: 0 0 30px rgba(46,230,255,.5); }
.move.skill:hover { box-shadow: 0 0 30px rgba(177,77,255,.5); }
.waiting-opp { color: var(--ink-dim); font-size: 14px; }

.over .result { display: inline-block; padding: 14px 28px; border-radius: 14px; font-size: 18px; font-weight: 800; letter-spacing: .06em; }
.over .result.win { color: #04050d; background: linear-gradient(100deg, var(--g-accent), var(--gold)); box-shadow: 0 0 30px var(--g-glow); }
.over .result.lose { color: #fff; border: 1px solid rgba(255,77,141,.5); background: rgba(255,77,141,.12); }

.rounds { padding: 18px; border-radius: 16px; background: var(--glass); border: 1px solid var(--glass-bd); }
.rounds h4 { margin: 0 0 12px; color: #fff; font-size: 14px; letter-spacing: .08em; }
.rounds ul { list-style: none; display: flex; flex-direction: column; gap: 6px; }
.rounds li { display: grid; grid-template-columns: 44px 1fr 24px 1fr 60px; align-items: center; gap: 10px; padding: 8px 12px; border-radius: 10px; background: rgba(8,10,24,.35); font-size: 13px; }
.rounds .no { color: var(--ink-dim); }
.rounds .c { color: #fff; text-align: center; }
.rounds .arrow { text-align: center; color: var(--g-accent3); }
.rounds .win { text-align: right; font-size: 12px; color: var(--ink-dim); }
.rounds li.p1 .win { color: var(--g-accent); }
.rounds li.p2 .win { color: var(--g-accent3); }

.toast { position: fixed; left: 50%; bottom: 34px; transform: translateX(-50%); background: rgba(10,14,32,.92); border: 1px solid var(--glass-bd); color: #fff; padding: 12px 22px; border-radius: 12px; font-size: 14px; z-index: 20; box-shadow: 0 10px 40px rgba(0,0,0,.5); backdrop-filter: blur(10px); }
.toast.ok { border-color: rgba(46,230,255,.4); }
.toast.bad { border-color: rgba(255,77,141,.5); color: #ffd9e6; }
.toast-enter-active, .toast-leave-active { transition: .35s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(-50%) translateY(40px); }

@media (max-width: 640px) {
  .scoreboard { grid-template-columns: 1fr; }
  .mid { order: -1; }
  .move { width: 100%; }
}
</style>
