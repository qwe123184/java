<script setup>
import { onMounted, onBeforeUnmount, ref, computed, watch } from 'vue'
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
  fetchFeatured()
})

onBeforeUnmount(stopCarousel)

// ===== 视图状态：home（展示+搜索）/ rank（境界排行）=====
const view = ref('home')

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
  query.value = ''
  activeNav.value = n.key
  if (n.key === 'home') { view.value = 'home'; activeCat.value = '全部'; return }
  if (n.key === 'forbidden') { view.value = 'home'; activeCat.value = '禁墟'; return }
  if (n.key === 'quest') { view.value = 'home'; activeCat.value = '任务'; return }
  if (n.key === 'archive') { view.value = 'home'; activeCat.value = '全部'; return }
  if (n.key === 'rank') { view.value = 'rank'; activeCat.value = '全部'; return }
}

// ===== 搜索栏（调用后端 /api/archive/list，保留登录/me 接口不变）=====
const query = ref('')
const cats = ['全部', '禁墟', '神秘', '守夜人', '任务']
const activeCat = ref('全部')

const list = ref([])   // 当前展示的档案
const total = ref(0)   // 命中总数
const loading = ref(false)
const revision = ref(0) // 每次拉取成功自增，驱动卡片/排行入场动画重放
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
    revision.value++
  } catch {
    // 检索失败时静默回退为空列表（令牌失效时拦截器会清理会话并跳登录）
    list.value = []
    total.value = 0
    revision.value++
  } finally {
    loading.value = false
  }
}

// 关键词 / 分类变化时防抖检索（300ms），避免每次按键都打接口
watch([query, activeCat], () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(fetchArchives, 300)
})

// 境界排行：以 sort 归一战力条
const maxSort = computed(() => {
  const m = list.value.reduce((mx, it) => Math.max(mx, it.sort || 0), 0)
  return m || 1
})

const typeGlyph = { 禁墟: '✦', 神秘: '☽', 守夜人: '⚔', 任务: '◈' }

// ===== 首页精选轮播（取 sort 最高的 3 条，自动轮播）=====
const featured = ref([])
const featIdx = ref(0)
let featTimer = null

function startCarousel() {
  stopCarousel()
  if (featured.value.length > 1) {
    featTimer = setInterval(() => {
      featIdx.value = (featIdx.value + 1) % featured.value.length
    }, 4800)
  }
}
function stopCarousel() {
  if (featTimer) { clearInterval(featTimer); featTimer = null }
}
function goFeat(i) {
  featIdx.value = i
  startCarousel()
}
async function fetchFeatured() {
  try {
    const { data } = await archiveList({ size: 3 })
    if (data?.code === 0 && data?.data) {
      featured.value = (data.data.records || []).slice(0, 3)
      if (featIdx.value >= featured.value.length) featIdx.value = 0
      startCarousel()
    }
  } catch {
    /* 精选轮播为增强项，失败不阻塞首页 */
  }
}

// ===== 档案详情弹窗（查阅按钮 → 调后端 /api/archive/{id}）=====
const detailItem = ref(null)   // 当前打开的档案
const detailLoading = ref(false)
let detailTimer

async function openDetail(it) {
  if (!it) return
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
          :class="{ on: activeNav === n.key }"
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
    <section class="hero" :class="{ rank: view === 'rank' }">
      <div class="hero-glow" aria-hidden="true"></div>
      <h1 class="hero-title" v-if="view === 'home'">欢迎回到 <span>凡尘神域</span></h1>
      <h1 class="hero-title" v-else>境界排行 <span>守夜人战力榜</span></h1>
      <p class="hero-sub" v-if="view === 'home' && user?.realm">你已于【{{ user.realm }}】觉醒禁墟，迷雾之外，万家灯火由你守护。</p>
      <p class="hero-sub" v-else-if="view === 'home'">黯夜终临之时，吾必立于万万人前，横刀向渊。</p>
      <p class="hero-sub" v-else>以禁墟境界与战力，排列神域诸强之座次。</p>

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

      <div class="chips" v-if="view === 'home'">
        <button
          v-for="c in cats"
          :key="c"
          :class="{ on: activeCat === c }"
          @click="activeCat = c"
        >{{ c }}</button>
      </div>
    </section>

    <!-- 首页精选轮播 -->
    <section class="featured" v-if="view === 'home' && featured.length" @mouseenter="stopCarousel" @mouseleave="startCarousel">
      <transition name="cfade" mode="out-in">
        <div class="feat-slide" :key="featIdx" :data-type="featured[featIdx].type" @click="openDetail(featured[featIdx])">
          <span class="feat-glyph">{{ typeGlyph[featured[featIdx].type] || '◈' }}</span>
          <div class="feat-body">
            <span class="feat-tag" :data-type="featured[featIdx].type">{{ featured[featIdx].type }} · 精选</span>
            <h2>{{ featured[featIdx].name }}</h2>
            <p>{{ featured[featIdx].desc }}</p>
            <span class="feat-more">查阅禁墟 <span class="arrow">→</span></span>
          </div>
        </div>
      </transition>
      <div class="feat-dots">
        <button
          v-for="(f, i) in featured"
          :key="i"
          class="dot"
          :class="{ on: i === featIdx }"
          :data-type="f.type"
          @click="goFeat(i)"
        ><span class="bar" v-if="i === featIdx"></span></button>
      </div>
    </section>

    <!-- 结果 -->
    <section class="results">
      <div class="res-head">
        <span>{{ view === 'rank' ? '境界排行' : '检索结果' }}</span>
        <span class="count">
          <template v-if="loading">检索中…</template>
          <template v-else-if="view === 'rank'">{{ total }} 位 · 按战力排序</template>
          <template v-else>{{ total }} 条 · 当前「{{ activeCat }}」</template>
        </span>
      </div>

      <!-- 骨架屏：首次/空结果加载时显示 -->
      <div v-if="loading && list.length === 0" class="grid">
        <div class="card skel" v-for="n in 6" :key="'sk' + n">
          <div class="skel-row"><span class="skel-glyph"></span><div class="skel-lines"><i></i><i></i></div></div>
          <div class="skel-line"></div>
          <div class="skel-line short"></div>
        </div>
      </div>

      <!-- 首页：卡片网格 -->
      <div v-else-if="view === 'home' && list.length" class="grid" :key="revision">
        <article class="card" v-for="(it, i) in list" :key="it.id ?? it.name" :style="{ '--i': i }">
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
            <button class="more" @click="openDetail(it)">查阅 <span class="arrow">→</span></button>
          </footer>
        </article>
      </div>

      <!-- 排行：战力榜 -->
      <div v-else-if="view === 'rank' && list.length" class="ranklist" :key="revision">
        <article
          class="row"
          v-for="(it, i) in list"
          :key="it.id ?? it.name"
          :style="{ '--i': i }"
          :data-type="it.type"
          @click="openDetail(it)"
        >
          <span class="rk" :class="i < 3 ? 'top' + (i + 1) : ''">{{ i + 1 }}</span>
          <span class="glyph" :data-type="it.type">{{ typeGlyph[it.type] || '◈' }}</span>
          <div class="r-body">
            <div class="r-top">
              <h3>{{ it.name }}</h3>
              <span class="tag" :data-type="it.type">{{ it.type }}</span>
            </div>
            <div class="r-bar">
              <i :style="{ width: Math.max(8, ((it.sort || 0) / maxSort) * 100) + '%' }" :data-type="it.type"></i>
            </div>
          </div>
          <span class="r-realm">境界 · {{ it.realm }}</span>
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
  animation: sigilPulse 3.4s ease-in-out infinite;
}
@keyframes sigilPulse {
  0%,100% { box-shadow: 0 0 12px rgba(46,230,255,.45), 0 0 26px rgba(177,77,255,.3); }
  50% { box-shadow: 0 0 22px rgba(46,230,255,.8), 0 0 48px rgba(177,77,255,.55); }
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
  position: relative;
}
.avatar::after {
  content: ""; position: absolute; inset: -3px; border-radius: 50%;
  border: 1px solid rgba(245,210,122,.5); opacity: .7;
  animation: ringSpin 6s linear infinite;
}
@keyframes ringSpin { to { transform: rotate(360deg); } }
.user b { color: var(--gold); }
.logout {
  margin-left: 4px; border: 1px solid var(--glass-bd); background: rgba(255,255,255,.03);
  color: var(--ink); padding: 8px 14px; border-radius: 9px; cursor: pointer;
  font-family: inherit; font-size: 13px; transition: .2s;
}
.logout:hover { border-color: var(--pink); color: #fff; box-shadow: 0 0 16px rgba(255,77,141,.3); }

/* ===== 搜索区 ===== */
.hero { max-width: 880px; margin: 40px auto 22px; text-align: center; position: relative; }
.hero-glow {
  position: absolute; left: 50%; top: -10px; width: 420px; height: 220px;
  transform: translateX(-50%);
  background: radial-gradient(closest-side, rgba(46,230,255,.16), rgba(177,77,255,.10) 55%, transparent 75%);
  filter: blur(14px); pointer-events: none; z-index: -1;
  animation: glowBreathe 6s ease-in-out infinite;
}
@keyframes glowBreathe {
  0%,100% { opacity: .6; transform: translateX(-50%) scale(1); }
  50% { opacity: 1; transform: translateX(-50%) scale(1.08); }
}
.hero-title {
  font-size: clamp(28px, 4vw, 46px); font-weight: 800; letter-spacing: .02em;
  animation: riseIn .7s cubic-bezier(.22,.61,.36,1) both;
}
.hero-title span {
  background: linear-gradient(100deg, var(--cyan), var(--violet), var(--pink), var(--cyan));
  background-size: 220% auto;
  -webkit-background-clip: text; background-clip: text; color: transparent;
  animation: shimmer 5s linear infinite;
}
@keyframes shimmer { to { background-position: 220% center; } }
.hero-sub { margin-top: 12px; color: var(--ink-dim); font-size: 15px; line-height: 1.8; animation: riseIn .7s .08s cubic-bezier(.22,.61,.36,1) both; }
@keyframes riseIn { from { opacity: 0; transform: translateY(18px); } to { opacity: 1; transform: none; } }

.searchbar {
  display: flex; align-items: center; gap: 10px;
  margin: 30px auto 0; max-width: 620px;
  padding: 6px 6px 6px 16px; border-radius: 16px;
  background: var(--glass); border: 1px solid var(--glass-bd);
  backdrop-filter: blur(22px) saturate(140%);
  box-shadow: 0 18px 60px rgba(0,0,0,.5), inset 0 1px 0 rgba(255,255,255,.06);
  transition: .25s;
  animation: riseIn .7s .16s cubic-bezier(.22,.61,.36,1) both;
  position: relative; overflow: hidden;
}
.searchbar::before {
  content: ""; position: absolute; inset: 0; border-radius: 16px; padding: 1px;
  background: linear-gradient(120deg, transparent 30%, rgba(46,230,255,.6), rgba(177,77,255,.6), transparent 70%);
  background-size: 220% 100%;
  -webkit-mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
  -webkit-mask-composite: xor; mask-composite: exclude;
  opacity: 0; transition: opacity .3s;
}
.searchbar:focus-within::before { opacity: 1; animation: borderFlow 3s linear infinite; }
@keyframes borderFlow { to { background-position: 220% 0; } }
.searchbar:focus-within {
  border-color: transparent;
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
  position: relative; overflow: hidden;
}
.s-btn::after {
  content: ""; position: absolute; top: 0; left: -120%; width: 60%; height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,.5), transparent);
  transform: skewX(-20deg); animation: btnSweep 3.2s ease-in-out infinite;
}
@keyframes btnSweep { 0%,60% { left: -120%; } 100% { left: 140%; } }
.s-btn:hover { filter: brightness(1.08); transform: translateY(-1px); }

.chips { display: flex; gap: 9px; justify-content: center; margin-top: 18px; flex-wrap: wrap;
  animation: riseIn .7s .24s cubic-bezier(.22,.61,.36,1) both; }
.chips button {
  border: 1px solid rgba(120,150,255,.16); background: rgba(8,10,24,.4);
  color: var(--ink-dim); border-radius: 999px; padding: 8px 18px;
  font-size: 13px; cursor: pointer; font-family: inherit; transition: .2s; letter-spacing: .06em;
  position: relative; overflow: hidden;
}
.chips button:hover { color: #fff; border-color: var(--cyan); }
.chips button.on {
  color: #fff; border-color: var(--violet);
  background: linear-gradient(100deg, rgba(46,230,255,.16), rgba(177,77,255,.16));
  box-shadow: 0 0 16px rgba(177,77,255,.25);
}

/* ===== 首页精选轮播 ===== */
.featured {
  max-width: 980px; margin: 0 auto 26px; position: relative;
  border-radius: 18px; overflow: hidden;
  border: 1px solid var(--glass-bd); background: var(--glass);
  backdrop-filter: blur(16px);
  box-shadow: 0 18px 50px rgba(0,0,0,.45);
  animation: riseIn .7s .3s cubic-bezier(.22,.61,.36,1) both;
}
.feat-slide {
  display: flex; align-items: center; gap: 22px; padding: 26px 28px; cursor: pointer;
  position: relative; overflow: hidden;
}
.feat-slide[data-type="禁墟"] { background: radial-gradient(120% 140% at 0% 0%, rgba(177,77,255,.18), transparent 60%); }
.feat-slide[data-type="神秘"] { background: radial-gradient(120% 140% at 0% 0%, rgba(46,230,255,.16), transparent 60%); }
.feat-slide[data-type="守夜人"] { background: radial-gradient(120% 140% at 0% 0%, rgba(245,210,122,.16), transparent 60%); }
.feat-slide[data-type="任务"] { background: radial-gradient(120% 140% at 0% 0%, rgba(255,77,141,.16), transparent 60%); }
.feat-glyph {
  width: 64px; height: 64px; flex-shrink: 0; display: grid; place-items: center;
  border-radius: 16px; font-size: 30px;
  background: linear-gradient(135deg, rgba(46,230,255,.22), rgba(177,77,255,.22));
  color: var(--cyan); box-shadow: 0 0 24px rgba(46,230,255,.25);
  animation: glyphFloat 4s ease-in-out infinite;
}
@keyframes glyphFloat { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-6px); } }
.feat-body { text-align: left; flex: 1; }
.feat-tag {
  font-size: 11.5px; letter-spacing: .1em; color: var(--violet);
  border: 1px solid rgba(177,77,255,.3); background: rgba(177,77,255,.08);
  padding: 2px 10px; border-radius: 999px;
}
.feat-tag[data-type="神秘"] { color: var(--cyan); border-color: rgba(46,230,255,.3); background: rgba(46,230,255,.08); }
.feat-tag[data-type="守夜人"] { color: var(--gold); border-color: rgba(245,210,122,.3); background: rgba(245,210,122,.08); }
.feat-tag[data-type="任务"] { color: var(--pink); border-color: rgba(255,77,141,.3); background: rgba(255,77,141,.08); }
.feat-body h2 { font-size: 24px; color: #fff; margin: 10px 0 8px; letter-spacing: .03em;
  background: linear-gradient(100deg,#fff,var(--cyan) 60%,var(--violet));
  -webkit-background-clip: text; background-clip: text; color: transparent; }
.feat-body p { color: var(--ink-dim); font-size: 13.5px; line-height: 1.7; max-width: 640px; }
.feat-more { display: inline-flex; align-items: center; gap: 6px; margin-top: 12px; color: var(--cyan); font-size: 13px; letter-spacing: .04em; }
.feat-more .arrow { transition: transform .25s; }
.feat-slide:hover .feat-more .arrow { transform: translateX(5px); }
.cfade-enter-active, .cfade-leave-active { transition: .55s cubic-bezier(.22,.61,.36,1); }
.cfade-enter-from { opacity: 0; transform: translateX(26px); }
.cfade-leave-to { opacity: 0; transform: translateX(-26px); }
.feat-dots { display: flex; gap: 8px; justify-content: center; padding: 0 0 14px; }
.feat-dots .dot {
  width: 26px; height: 5px; border-radius: 999px; border: 0; cursor: pointer; padding: 0;
  background: rgba(120,150,255,.2); overflow: hidden; position: relative;
}
.feat-dots .dot.on { background: rgba(120,150,255,.25); }
.feat-dots .dot .bar {
  position: absolute; left: 0; top: 0; height: 100%; width: 100%;
  background: linear-gradient(90deg, var(--cyan), var(--violet));
  transform-origin: left; animation: featBar 4.8s linear forwards;
}
@keyframes featBar { from { transform: scaleX(0); } to { transform: scaleX(1); } }

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
  transition: transform .3s cubic-bezier(.22,.61,.36,1), box-shadow .3s, border-color .3s;
  display: flex; flex-direction: column; gap: 12px;
  position: relative;
  opacity: 0; animation: cardIn .55s cubic-bezier(.22,.61,.36,1) forwards;
  animation-delay: calc(var(--i, 0) * 55ms);
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(22px) scale(.96); filter: blur(2px); }
  to { opacity: 1; transform: none; filter: none; }
}
.card::after {
  content: ""; position: absolute; inset: 0; border-radius: 16px; padding: 1px;
  background: linear-gradient(130deg, rgba(46,230,255,0), rgba(177,77,255,.5), rgba(255,77,141,0));
  -webkit-mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
  -webkit-mask-composite: xor; mask-composite: exclude;
  opacity: 0; transition: opacity .3s; pointer-events: none;
}
.card:hover {
  transform: translateY(-6px) scale(1.015);
  border-color: rgba(120,150,255,.4);
  box-shadow: 0 18px 46px rgba(0,0,0,.5), 0 0 30px rgba(46,230,255,.22);
}
.card:hover::after { opacity: 1; }
.card header { display: flex; align-items: center; gap: 12px; }
.glyph {
  width: 40px; height: 40px; flex-shrink: 0; display: grid; place-items: center;
  border-radius: 11px; font-size: 19px;
  background: linear-gradient(135deg, rgba(46,230,255,.2), rgba(177,77,255,.2));
  color: var(--cyan);
  transition: transform .35s cubic-bezier(.22,.61,.36,1), box-shadow .35s;
}
.card:hover .glyph { transform: rotate(-8deg) scale(1.08); box-shadow: 0 0 18px rgba(46,230,255,.4); }
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
  display: inline-flex; align-items: center; gap: 4px;
}
.more .arrow { transition: transform .25s; }
.more:hover { color: #fff; text-shadow: 0 0 12px var(--cyan); }
.more:hover .arrow { transform: translateX(4px); }

/* 骨架屏 */
.card.skel { opacity: 1; animation: none; gap: 14px; }
.skel-row { display: flex; align-items: center; gap: 12px; }
.skel-glyph { width: 40px; height: 40px; border-radius: 11px; flex-shrink: 0; }
.skel-lines { flex: 1; display: flex; flex-direction: column; gap: 7px; }
.skel-lines i { height: 11px; border-radius: 6px; }
.skel-lines i:last-child { width: 55%; }
.skel-line { height: 11px; border-radius: 6px; }
.skel-line.short { width: 70%; }
.skel-glyph, .skel-lines i, .skel-line {
  background: linear-gradient(100deg, rgba(120,150,255,.08) 30%, rgba(120,150,255,.22) 50%, rgba(120,150,255,.08) 70%);
  background-size: 220% 100%;
  animation: shimmer 1.4s linear infinite;
}

/* ===== 境界排行 ===== */
.ranklist { display: flex; flex-direction: column; gap: 10px; }
.row {
  display: flex; align-items: center; gap: 16px;
  padding: 14px 18px; border-radius: 14px; cursor: pointer;
  background: var(--glass); border: 1px solid var(--glass-bd); backdrop-filter: blur(12px);
  transition: transform .25s, box-shadow .25s, border-color .25s;
  opacity: 0; animation: rowIn .5s cubic-bezier(.22,.61,.36,1) forwards;
  animation-delay: calc(var(--i, 0) * 45ms);
}
@keyframes rowIn { from { opacity: 0; transform: translateX(-18px); } to { opacity: 1; transform: none; } }
.row:hover {
  transform: translateX(4px);
  border-color: rgba(120,150,255,.4);
  box-shadow: 0 12px 34px rgba(0,0,0,.4), 0 0 24px rgba(46,230,255,.16);
}
.rk {
  width: 34px; height: 34px; flex-shrink: 0; display: grid; place-items: center;
  border-radius: 10px; font-weight: 800; font-size: 15px; color: var(--ink-dim);
  background: rgba(120,150,255,.08); border: 1px solid var(--glass-bd);
}
.rk.top1 { color: #04050d; background: linear-gradient(135deg,#ffe9a8,#f5d27a); box-shadow: 0 0 18px rgba(245,210,122,.6); border-color: transparent; }
.rk.top2 { color: #04050d; background: linear-gradient(135deg,#e6ecff,#aeb8d8); box-shadow: 0 0 16px rgba(174,184,216,.5); border-color: transparent; }
.rk.top3 { color: #04050d; background: linear-gradient(135deg,#ffcd9e,#d99157); box-shadow: 0 0 16px rgba(217,145,87,.5); border-color: transparent; }
.r-body { flex: 1; min-width: 0; }
.r-top { display: flex; align-items: center; gap: 10px; }
.r-top h3 { font-size: 15.5px; color: #fff; letter-spacing: .03em; }
.r-bar { height: 6px; margin-top: 9px; border-radius: 999px; background: rgba(120,150,255,.12); overflow: hidden; }
.r-bar i {
  display: block; height: 100%; border-radius: 999px;
  background: linear-gradient(90deg, var(--cyan), var(--violet));
  animation: barGrow .7s cubic-bezier(.22,.61,.36,1) both;
  animation-delay: calc(var(--i, 0) * 45ms + .15s);
}
@keyframes barGrow { from { width: 0 !important; } }
.r-bar i[data-type="禁墟"] { background: linear-gradient(90deg, var(--violet), var(--pink)); }
.r-bar i[data-type="神秘"] { background: linear-gradient(90deg, var(--cyan), #7fd9ff); }
.r-bar i[data-type="守夜人"] { background: linear-gradient(90deg, var(--gold), #ffe9a8); }
.r-bar i[data-type="任务"] { background: linear-gradient(90deg, var(--pink), #ff9ec4); }
.r-realm { font-size: 12.5px; color: var(--ink-dim); letter-spacing: .06em; white-space: nowrap; }

.empty { text-align: center; padding: 60px 0; color: var(--ink-dim); }
.e-glyph { font-size: 46px; color: var(--violet); opacity: .6; margin-bottom: 14px; animation: floaty 3.4s ease-in-out infinite; }
@keyframes floaty { 0%,100% { transform: translateY(0); opacity: .55; } 50% { transform: translateY(-10px); opacity: .9; } }
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
  overflow: hidden;
  animation: pop .32s cubic-bezier(.22,.61,.36,1) both;
}
@keyframes pop { from { opacity: 0; transform: translateY(18px) scale(.94); } to { opacity: 1; transform: none; } }
.modal::before {
  content: ""; position: absolute; inset: -1px; border-radius: 20px; padding: 1px;
  background: linear-gradient(130deg, rgba(46,230,255,.7), rgba(177,77,255,.5), rgba(255,77,141,.6));
  -webkit-mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
  -webkit-mask-composite: xor; mask-composite: exclude;
  opacity: .7; pointer-events: none;
}
.modal::after {
  content: ""; position: absolute; top: -50%; left: -30%; width: 50%; height: 200%;
  background: linear-gradient(90deg, transparent, rgba(46,230,255,.12), transparent);
  transform: rotate(18deg); animation: modalSweep 6s ease-in-out infinite;
}
@keyframes modalSweep { 0%,100% { left: -40%; } 50% { left: 120%; } }
.modal-close {
  position: absolute; top: 14px; right: 14px; width: 30px; height: 30px;
  border: 1px solid var(--glass-bd); background: rgba(255,255,255,.03); color: var(--ink-dim);
  border-radius: 9px; cursor: pointer; font-size: 14px; transition: .2s; line-height: 1; z-index: 1;
}
.modal-close:hover { color: #fff; border-color: var(--pink); box-shadow: 0 0 14px rgba(255,77,141,.3); }
.modal-head { display: flex; align-items: center; gap: 14px; animation: riseIn .4s .05s both; }
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
.modal-realm { margin: 18px 0 10px; font-size: 13.5px; color: var(--ink-dim); letter-spacing: .06em; animation: riseIn .4s .12s both; }
.modal-realm b { color: var(--gold); }
.modal-desc { color: var(--ink); font-size: 14.5px; line-height: 1.85; animation: riseIn .4s .18s both; }
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
  .feat-slide { flex-direction: column; text-align: center; }
  .feat-body { text-align: center; }
  .r-realm { display: none; }
}

/* 尊重系统“减少动态效果”偏好 */
@media (prefers-reduced-motion: reduce) {
  * { animation-duration: .001ms !important; animation-iteration-count: 1 !important; transition-duration: .001ms !important; }
  .card, .row { opacity: 1; }
}
</style>
