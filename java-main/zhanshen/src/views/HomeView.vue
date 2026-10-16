<script setup>
import { onMounted, ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../store/auth'
import { me } from '../api/auth'
import { archiveList, archiveDetail } from '../api/archive'

const router = useRouter()
const { user, setSession, logout } = useAuth()

function onLogout() {
  logout()
  router.push('/')
}

// 进入神域时以访问令牌拉取最新守夜人信息，确保会话有效（保留已有后端接口调用）
onMounted(async () => {
  if (!user.value) {
    try {
      const { data } = await me()
      if (data?.data) setSession(data.data, null, null)
    } catch {
      /* 令牌失效时拦截器已清理会话 */
    }
  }
  // 进入神域即拉取档案（首页展示），由后端 /api/archive/list 提供数据
  fetchArchives()
})

// ===== 导航栏 =====
const navs = [
  { key: 'home', label: '首页', active: true },
  { key: 'forbidden', label: '禁墟' },
  { key: 'quest', label: '任务' },
  { key: 'archive', label: '档案' },
  { key: 'rank', label: '排行' }
]
const activeNav = ref('home')
const toast = ref({ show: false, msg: '' })
let toastTimer
function showToast(msg) {
  toast.value = { show: true, msg }
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value.show = false), 2400)
}
function onNav(n) {
  if (n.key === 'home') {
    activeNav.value = 'home'
    return
  }
  // 其它页面尚未构建，先给前端反馈（不触发后端）
  showToast(`「${n.label}」神域仍在构筑中，敬请期待。`)
}

// ===== 搜索栏（调用后端 /api/archive/list，保留登录/me 接口不变）=====
const query = ref('')
const cats = ['全部', '禁墟', '神秘', '守夜人', '任务']
const activeCat = ref('全部')

const list = ref([])   // 当前展示的档案
const total = ref(0)   // 命中总数
const loading = ref(false)
let searchTimer

// 拉取神域档案（首页展示 + 搜索），按关键词与类型筛选
async function fetchArchives() {
  loading.value = true
  try {
    const type = activeCat.value === '全部' ? '' : activeCat.value
    const { data } = await archiveList({ keyword: query.value.trim(), type })
    if (data?.code === 0 && data?.data) {
      list.value = data.data.records || []
      total.value = data.data.total || 0
    } else {
      list.value = []
      total.value = 0
    }
  } catch {
    // 检索失败时静默回退为空列表（令牌失效时拦截器会清理会话并跳登录）
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 关键词 / 分类变化时防抖检索（300ms），避免每次按键都打接口
watch([query, activeCat], () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(fetchArchives, 300)
})

const typeGlyph = { 禁墟: '✦', 神秘: '☽', 守夜人: '⚔', 任务: '◈' }

// ===== 档案详情弹窗（查阅按钮 → 调后端 /api/archive/{id}）=====
const detailItem = ref(null)   // 当前打开的档案
const detailLoading = ref(false)
let detailTimer

async function openDetail(it) {
  detailItem.value = null
  detailLoading.value = true
  try {
    const { data } = await archiveDetail(it.id)
    if (data?.code === 0 && data?.data) {
      detailItem.value = data.data
    } else {
      showToast('该档案已消散于迷雾之中。')
    }
  } catch {
    showToast('神域动荡，禁墟详情暂时无法查阅。')
  } finally {
    detailLoading.value = false
  }
}

function closeDetail() {
  detailItem.value = null
  detailLoading.value = false
  clearTimeout(detailTimer)
}
</script>

<template>
  <main class="home">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="brand">
        <span class="sigil">神</span>
        <span class="brand-txt">斩神之凡尘神域</span>
      </div>

      <ul class="menu">
        <li
          v-for="n in navs"
          :key="n.key"
          :class="{ on: (n.key === 'home' ? activeNav === 'home' : false) }"
          @click="onNav(n)"
        >
          {{ n.label }}
        </li>
      </ul>

      <div class="user">
        <span class="avatar">{{ (user?.username || '无')[0] }}</span>
        <span class="uname">守夜人「<b>{{ user?.username || '无名' }}</b>」</span>
        <button class="logout" @click="onLogout">退出神域</button>
      </div>
    </nav>

    <!-- 搜索区 -->
    <section class="hero">
      <h1>欢迎回到 <span>凡尘神域</span></h1>
      <p v-if="user?.realm">你已于【{{ user.realm }}】觉醒禁墟，迷雾之外，万家灯火由你守护。</p>
      <p v-else>黯夜终临之时，吾必立于万万人前，横刀向渊。</p>

      <div class="searchbar">
        <svg class="s-ic" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="7" /><path d="M21 21l-4.3-4.3" />
        </svg>
        <input
          v-model="query"
          type="text"
          placeholder="检索禁墟 · 神秘 · 守夜人 · 任务指令 …"
        />
        <button class="s-btn">检 索</button>
      </div>

      <div class="chips">
        <button
          v-for="c in cats"
          :key="c"
          :class="{ on: activeCat === c }"
          @click="activeCat = c"
        >{{ c }}</button>
      </div>
    </section>

    <!-- 结果 -->
    <section class="results">
      <div class="res-head">
        <span>检索结果</span>
        <span class="count">
          <template v-if="loading">检索中…</template>
          <template v-else>{{ total }} 条 · 当前「{{ activeCat }}」</template>
        </span>
      </div>

      <div v-if="list.length" class="grid">
        <article class="card" v-for="it in list" :key="it.id ?? it.name">
          <header>
            <span class="glyph" :data-type="it.type">{{ typeGlyph[it.type] || '◈' }}</span>
            <div class="titles">
              <h3>{{ it.name }}</h3>
              <span class="tag" :data-type="it.type">{{ it.type }}</span>
            </div>
          </header>
          <p class="desc">{{ it.desc }}</p>
          <footer>
            <span class="realm">境界 · {{ it.realm }}</span>
            <button class="more" @click="openDetail(it)">查阅 →</button>
          </footer>
        </article>
      </div>

      <div v-else class="empty">
        <div class="e-glyph">☾</div>
        <p>迷雾中未见「{{ query }}」的踪迹，换个禁墟之名再试。</p>
      </div>
    </section>

    <transition name="toast">
      <div class="toast" v-if="toast.show">{{ toast.msg }}</div>
    </transition>

    <!-- 档案详情弹窗 -->
    <transition name="modal">
      <div v-if="detailItem || detailLoading" class="modal-mask" @click.self="closeDetail">
        <div class="modal">
          <button class="modal-close" @click="closeDetail" aria-label="关闭">✕</button>

          <div v-if="detailLoading" class="modal-loading">
            <span class="spinner"></span> 正在唤醒禁墟记忆…
          </div>

          <template v-else-if="detailItem">
            <header class="modal-head">
              <span class="glyph" :data-type="detailItem.type">{{ typeGlyph[detailItem.type] || '◈' }}</span>
              <div>
                <h2>{{ detailItem.name }}</h2>
                <span class="tag" :data-type="detailItem.type">{{ detailItem.type }}</span>
              </div>
            </header>
            <div class="modal-realm">境界 · <b>{{ detailItem.realm }}</b></div>
            <p class="modal-desc">{{ detailItem.desc }}</p>
          </template>
        </div>
      </div>
    </transition>
  </main>
</template>

<style scoped>
.home {
  position: relative; z-index: 2;
  height: 100vh; overflow-y: auto;
  padding: 0 clamp(20px, 5vw, 56px) 60px;
}
.home::-webkit-scrollbar { width: 8px; }
.home::-webkit-scrollbar-thumb { background: rgba(120,150,255,.25); border-radius: 8px; }
.home::-webkit-scrollbar-track { background: transparent; }

/* ===== 导航栏 ===== */
.navbar {
  position: sticky; top: 0; z-index: 5;
  display: flex; align-items: center; gap: 20px;
  padding: 14px 0; margin-bottom: 8px;
  background: linear-gradient(180deg, rgba(4,5,13,.85), rgba(4,5,13,.45) 70%, transparent);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--glass-bd);
}
.brand { display: flex; align-items: center; gap: 11px; font-weight: 700; letter-spacing: .12em; white-space: nowrap; }
.sigil {
  width: 34px; height: 34px; display: grid; place-items: center;
  border-radius: 9px; background: linear-gradient(135deg, var(--cyan), var(--violet));
  color: #04050d; font-weight: 800; font-family: serif; box-shadow: var(--neon);
}
.brand-txt { background: linear-gradient(100deg, var(--cyan), var(--violet), var(--pink));
  -webkit-background-clip: text; background-clip: text; color: transparent; }

.menu {
  list-style: none; display: flex; gap: 6px; margin: 0 auto 0 8px;
  flex-wrap: wrap;
}
.menu li {
  padding: 8px 16px; border-radius: 10px; cursor: pointer;
  color: var(--ink-dim); font-size: 14px; letter-spacing: .08em;
  border: 1px solid transparent; transition: .2s; user-select: none;
}
.menu li:hover { color: #fff; background: rgba(120,150,255,.1); }
.menu li.on {
  color: #fff;
  background: linear-gradient(100deg, rgba(46,230,255,.18), rgba(177,77,255,.18));
  border-color: rgba(120,150,255,.35);
  box-shadow: 0 0 16px rgba(46,230,255,.2);
}

.user { display: flex; align-items: center; gap: 10px; white-space: nowrap; font-size: 13.5px; color: var(--ink-dim); }
.avatar {
  width: 32px; height: 32px; border-radius: 50%; display: grid; place-items: center;
  background: linear-gradient(135deg, var(--violet), var(--pink)); color: #04050d;
  font-weight: 800; font-family: serif;
}
.user b { color: var(--gold); }
.logout {
  margin-left: 4px; border: 1px solid var(--glass-bd); background: rgba(255,255,255,.03);
  color: var(--ink); padding: 8px 14px; border-radius: 9px; cursor: pointer;
  font-family: inherit; font-size: 13px; transition: .2s;
}
.logout:hover { border-color: var(--pink); color: #fff; box-shadow: 0 0 16px rgba(255,77,141,.3); }

/* ===== 搜索区 ===== */
.hero { max-width: 880px; margin: 40px auto 34px; text-align: center; }
.hero h1 { font-size: clamp(28px, 4vw, 46px); font-weight: 800; letter-spacing: .02em; }
.hero h1 span {
  background: linear-gradient(100deg, var(--cyan), var(--violet), var(--pink));
  -webkit-background-clip: text; background-clip: text; color: transparent;
}
.hero p { margin-top: 12px; color: var(--ink-dim); font-size: 15px; line-height: 1.8; }

.searchbar {
  display: flex; align-items: center; gap: 10px;
  margin: 30px auto 0; max-width: 620px;
  padding: 6px 6px 6px 16px; border-radius: 16px;
  background: var(--glass); border: 1px solid var(--glass-bd);
  backdrop-filter: blur(22px) saturate(140%);
  box-shadow: 0 18px 60px rgba(0,0,0,.5), inset 0 1px 0 rgba(255,255,255,.06);
  transition: .25s;
}
.searchbar:focus-within {
  border-color: var(--cyan);
  box-shadow: 0 0 0 3px rgba(46,230,255,.12), 0 0 28px rgba(46,230,255,.28);
}
.s-ic { width: 22px; height: 22px; color: var(--cyan); flex-shrink: 0; }
.searchbar input {
  flex: 1; border: 0; background: transparent; outline: none;
  color: #fff; font-size: 15px; font-family: inherit; letter-spacing: .02em; padding: 12px 4px;
}
.searchbar input::placeholder { color: #5a6390; }
.s-btn {
  border: 0; border-radius: 11px; padding: 12px 22px; cursor: pointer;
  font-size: 14.5px; font-weight: 700; letter-spacing: .14em; color: #04050d;
  font-family: inherit;
  background: linear-gradient(100deg, var(--cyan), var(--violet) 55%, var(--pink));
  box-shadow: var(--neon); transition: .25s;
}
.s-btn:hover { filter: brightness(1.08); transform: translateY(-1px); }

.chips { display: flex; gap: 9px; justify-content: center; margin-top: 18px; flex-wrap: wrap; }
.chips button {
  border: 1px solid rgba(120,150,255,.16); background: rgba(8,10,24,.4);
  color: var(--ink-dim); border-radius: 999px; padding: 8px 18px;
  font-size: 13px; cursor: pointer; font-family: inherit; transition: .2s; letter-spacing: .06em;
}
.chips button:hover { color: #fff; border-color: var(--cyan); }
.chips button.on {
  color: #fff; border-color: var(--violet);
  background: linear-gradient(100deg, rgba(46,230,255,.16), rgba(177,77,255,.16));
  box-shadow: 0 0 16px rgba(177,77,255,.25);
}

/* ===== 结果 ===== */
.results { max-width: 980px; margin: 0 auto; }
.res-head {
  display: flex; justify-content: space-between; align-items: baseline;
  margin-bottom: 16px; color: var(--ink); font-size: 15px; letter-spacing: .08em;
}
.res-head .count { color: var(--ink-dim); font-size: 13px; }

.grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.card {
  padding: 18px; border-radius: 16px; background: var(--glass);
  border: 1px solid var(--glass-bd); backdrop-filter: blur(14px);
  transition: .25s; display: flex; flex-direction: column; gap: 12px;
}
.card:hover {
  transform: translateY(-3px);
  border-color: rgba(120,150,255,.4);
  box-shadow: 0 14px 40px rgba(0,0,0,.45), 0 0 24px rgba(46,230,255,.18);
}
.card header { display: flex; align-items: center; gap: 12px; }
.glyph {
  width: 40px; height: 40px; flex-shrink: 0; display: grid; place-items: center;
  border-radius: 11px; font-size: 19px;
  background: linear-gradient(135deg, rgba(46,230,255,.2), rgba(177,77,255,.2));
  color: var(--cyan);
}
.glyph[data-type="禁墟"] { color: var(--violet); }
.glyph[data-type="神秘"] { color: var(--cyan); }
.glyph[data-type="守夜人"] { color: var(--gold); }
.glyph[data-type="任务"] { color: var(--pink); }
.titles h3 { font-size: 16px; color: #fff; letter-spacing: .04em; }
.tag {
  display: inline-block; margin-top: 5px; font-size: 11.5px; letter-spacing: .08em;
  padding: 2px 10px; border-radius: 999px; color: var(--violet);
  border: 1px solid rgba(177,77,255,.3); background: rgba(177,77,255,.08);
}
.tag[data-type="神秘"] { color: var(--cyan); border-color: rgba(46,230,255,.3); background: rgba(46,230,255,.08); }
.tag[data-type="守夜人"] { color: var(--gold); border-color: rgba(245,210,122,.3); background: rgba(245,210,122,.08); }
.tag[data-type="任务"] { color: var(--pink); border-color: rgba(255,77,141,.3); background: rgba(255,77,141,.08); }
.desc { color: var(--ink-dim); font-size: 13.5px; line-height: 1.7; flex: 1; }
.card footer {
  display: flex; justify-content: space-between; align-items: center;
  border-top: 1px solid rgba(120,150,255,.1); padding-top: 12px;
}
.realm { font-size: 12.5px; color: var(--ink-dim); letter-spacing: .06em; }
.more {
  border: 0; background: transparent; color: var(--cyan); cursor: pointer;
  font-size: 13px; font-family: inherit; letter-spacing: .04em;
}
.more:hover { color: #fff; text-shadow: 0 0 12px var(--cyan); }

.empty { text-align: center; padding: 60px 0; color: var(--ink-dim); }
.e-glyph { font-size: 46px; color: var(--violet); opacity: .6; margin-bottom: 14px; }
.empty p { font-size: 14.5px; }

/* ===== Toast ===== */
.toast {
  position: fixed; left: 50%; bottom: 34px; transform: translateX(-50%);
  background: rgba(10,14,32,.92); border: 1px solid var(--glass-bd); color: #fff;
  padding: 12px 22px; border-radius: 12px; font-size: 14px; z-index: 20;
  box-shadow: 0 10px 40px rgba(0,0,0,.5); backdrop-filter: blur(10px);
  white-space: nowrap; max-width: 90vw; overflow: hidden; text-overflow: ellipsis;
}
.toast-enter-active, .toast-leave-active { transition: .35s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(-50%) translateY(40px); }

/* ===== 档案详情弹窗 ===== */
.modal-mask {
  position: fixed; inset: 0; z-index: 30;
  display: grid; place-items: center; padding: 20px;
  background: rgba(2, 3, 9, .66); backdrop-filter: blur(6px);
}
.modal {
  position: relative; width: min(460px, 100%);
  padding: 30px 30px 28px; border-radius: 20px;
  background: var(--glass); border: 1px solid var(--glass-bd);
  backdrop-filter: blur(24px) saturate(140%);
  box-shadow: 0 30px 90px rgba(0,0,0,.6), inset 0 1px 0 rgba(255,255,255,.06);
  animation: pop .28s ease;
}
@keyframes pop { from { opacity: 0; transform: translateY(18px) scale(.96); } to { opacity: 1; transform: none; } }
.modal::before {
  content: ""; position: absolute; inset: -1px; border-radius: 20px; padding: 1px;
  background: linear-gradient(130deg, rgba(46,230,255,.7), rgba(177,77,255,.5), rgba(255,77,141,.6));
  -webkit-mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
  -webkit-mask-composite: xor; mask-composite: exclude;
  opacity: .7; pointer-events: none;
}
.modal-close {
  position: absolute; top: 14px; right: 14px; width: 30px; height: 30px;
  border: 1px solid var(--glass-bd); background: rgba(255,255,255,.03); color: var(--ink-dim);
  border-radius: 9px; cursor: pointer; font-size: 14px; transition: .2s; line-height: 1;
}
.modal-close:hover { color: #fff; border-color: var(--pink); box-shadow: 0 0 14px rgba(255,77,141,.3); }
.modal-head { display: flex; align-items: center; gap: 14px; }
.modal-head .glyph {
  width: 46px; height: 46px; flex-shrink: 0; display: grid; place-items: center;
  border-radius: 12px; font-size: 22px;
  background: linear-gradient(135deg, rgba(46,230,255,.2), rgba(177,77,255,.2)); color: var(--cyan);
}
.modal-head .glyph[data-type="禁墟"] { color: var(--violet); }
.modal-head .glyph[data-type="神秘"] { color: var(--cyan); }
.modal-head .glyph[data-type="守夜人"] { color: var(--gold); }
.modal-head .glyph[data-type="任务"] { color: var(--pink); }
.modal-head h2 { font-size: 21px; color: #fff; letter-spacing: .04em; }
.modal-realm { margin: 18px 0 10px; font-size: 13.5px; color: var(--ink-dim); letter-spacing: .06em; }
.modal-realm b { color: var(--gold); }
.modal-desc { color: var(--ink); font-size: 14.5px; line-height: 1.85; }
.modal-loading { display: flex; align-items: center; gap: 12px; color: var(--ink-dim); font-size: 14px; padding: 18px 4px; }
.modal-loading .spinner {
  width: 16px; height: 16px; border-radius: 50%;
  border: 2px solid rgba(46,230,255,.25); border-top-color: var(--cyan);
  animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.modal-enter-active, .modal-leave-active { transition: .25s; }
.modal-enter-from, .modal-leave-to { opacity: 0; }

@media (max-width: 920px) { .grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 680px) {
  .menu { order: 3; width: 100%; margin: 4px 0 0; }
  .navbar { flex-wrap: wrap; }
  .grid { grid-template-columns: 1fr; }
}
</style>
