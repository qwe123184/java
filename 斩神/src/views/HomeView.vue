<script setup>
import { useRouter } from 'vue-router'
import { useAuth } from '../store/auth'

const router = useRouter()
const { user, logout } = useAuth()

function onLogout() {
  logout()
  router.push('/')
}

const stats = [
  { label: '禁墟', value: '凡尘神域', icon: '✦' },
  { label: '神位', value: '炽天使', icon: '☉' },
  { label: '阵营', value: '夜幕小队', icon: '⚔' },
  { label: '状态', value: '守夜中', icon: '◉' }
]

const quests = [
  { name: '肃清沧南市神秘', diff: '克莱因', done: false },
  { name: '镇守沉龙关', diff: '海境', done: false },
  { name: '封印诸神精神病院', diff: '无量', done: true }
]
</script>

<template>
  <main class="home">
    <header class="topbar">
      <div class="brand">
        <span class="sigil">神</span>
        <span>斩神之凡尘神域</span>
      </div>
      <div class="user">
        守夜人「<b>{{ user?.username || '无名' }}</b>」
        <button class="logout" @click="onLogout">退出神域</button>
      </div>
    </header>

    <section class="hero">
      <h1>欢迎回到 <span>凡尘神域</span></h1>
      <p v-if="user?.realm">你已于【{{ user.realm }}】觉醒禁墟，迷雾之外，万家灯火由你守护。</p>
      <p v-else>黯夜终临之时，吾必立于万万人前，横刀向渊。</p>
    </section>

    <section class="grid">
      <div class="stat" v-for="s in stats" :key="s.label">
        <div class="stat-ic">{{ s.icon }}</div>
        <div>
          <div class="stat-label">{{ s.label }}</div>
          <div class="stat-value">{{ s.value }}</div>
        </div>
      </div>
    </section>

    <section class="panel">
      <h2>当前任务 · 守夜人指令</h2>
      <ul class="quests">
        <li v-for="q in quests" :key="q.name" :class="{ done: q.done }">
          <span class="q-mark">{{ q.done ? '✓' : '◌' }}</span>
          <span class="q-name">{{ q.name }}</span>
          <span class="q-diff">{{ q.diff }}</span>
        </li>
      </ul>
    </section>
  </main>
</template>

<style scoped>
.home {
  position: relative; z-index: 2;
  max-width: 980px; margin: 0 auto;
  padding: 32px clamp(20px, 5vw, 48px) 60px;
}

.topbar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 6px 0 22px; border-bottom: 1px solid var(--glass-bd);
}
.brand { display: flex; align-items: center; gap: 12px; font-weight: 700; letter-spacing: .12em; }
.sigil {
  width: 34px; height: 34px; display: grid; place-items: center;
  border-radius: 9px; background: linear-gradient(135deg, var(--cyan), var(--violet));
  color: #04050d; font-weight: 800; font-family: serif; box-shadow: var(--neon);
}
.user { font-size: 14px; color: var(--ink-dim); }
.user b { color: var(--gold); }
.logout {
  margin-left: 14px; border: 1px solid var(--glass-bd); background: rgba(255,255,255,.03);
  color: var(--ink); padding: 8px 14px; border-radius: 9px; cursor: pointer;
  font-family: inherit; font-size: 13px; transition: .2s;
}
.logout:hover { border-color: var(--pink); color: #fff; box-shadow: 0 0 16px rgba(255,77,141,.3); }

.hero { margin: 40px 0 30px; }
.hero h1 { font-size: clamp(28px, 4vw, 46px); font-weight: 800; letter-spacing: .02em; }
.hero h1 span {
  background: linear-gradient(100deg, var(--cyan), var(--violet), var(--pink));
  -webkit-background-clip: text; background-clip: text; color: transparent;
}
.hero p { margin-top: 12px; color: var(--ink-dim); font-size: 15px; line-height: 1.8; }

.grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 30px; }
.stat {
  display: flex; align-items: center; gap: 12px; padding: 16px; border-radius: 14px;
  background: var(--glass); border: 1px solid var(--glass-bd); backdrop-filter: blur(14px);
}
.stat-ic {
  width: 38px; height: 38px; display: grid; place-items: center; font-size: 18px;
  border-radius: 10px; background: linear-gradient(135deg, rgba(46,230,255,.2), rgba(177,77,255,.2));
  color: var(--cyan);
}
.stat-label { font-size: 12px; color: var(--ink-dim); letter-spacing: .1em; }
.stat-value { font-size: 16px; font-weight: 700; color: #fff; }

.panel {
  padding: 22px; border-radius: 16px; background: var(--glass);
  border: 1px solid var(--glass-bd); backdrop-filter: blur(14px);
}
.panel h2 { font-size: 16px; letter-spacing: .08em; margin-bottom: 16px; color: var(--ink); }
.quests { list-style: none; display: flex; flex-direction: column; gap: 10px; }
.quests li {
  display: flex; align-items: center; gap: 12px; padding: 12px 14px; border-radius: 11px;
  background: rgba(8,10,24,.5); border: 1px solid rgba(120,150,255,.1); font-size: 14px;
}
.quests li.done { opacity: .6; }
.q-mark { color: var(--cyan); width: 18px; }
.quests li.done .q-mark { color: var(--gold); }
.q-name { flex: 1; color: var(--ink); }
.q-diff {
  font-size: 12px; color: var(--violet); border: 1px solid rgba(177,77,255,.3);
  padding: 3px 10px; border-radius: 999px;
}

@media (max-width: 680px) { .grid { grid-template-columns: repeat(2, 1fr); } }
</style>
