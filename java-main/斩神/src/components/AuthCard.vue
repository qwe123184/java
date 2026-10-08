<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../store/auth'

const router = useRouter()
const auth = useAuth()

const mode = ref('login') // 'login' | 'register'
const username = ref('')
const email = ref('')
const password = ref('')
const showPw = ref(false)
const realm = ref('池境')
const remember = ref(true)
const toast = ref({ show: false, msg: '', type: '' })
let toastTimer

const realms = ['盏境', '池境', '川境', '海境']

const isLogin = computed(() => mode.value === 'login')

const formTitle = computed(() => (isLogin.value ? '登 录 神 域' : '觉 醒 禁 墟'))
const formHint = computed(() =>
  isLogin.value
    ? '以守夜人之名，进入凡尘神域。'
    : '于迷雾中点亮你的第一道禁墟，踏上斩神之路。'
)
const submitText = computed(() => (isLogin.value ? '进 入 神 域' : '点 燃 禁 墟'))

function setMode(m) {
  mode.value = m
}

function showToast(msg, type = '') {
  toast.value = { show: true, msg, type }
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value.show = false), 2600)
}

function onSubmit() {
  const u = username.value.trim()
  const p = password.value.trim()
  if (!u) return showToast('请先留下你的守夜人代号。', 'err')
  if (p.length < 4) return showToast('禁墟密钥至少需 4 位，横刀之誓不可轻许。', 'err')

  if (isLogin.value) {
    auth.login({ username: u })
    showToast(`代号「${u}」已认证，凡尘神域为你敞开。`, 'ok')
    setTimeout(() => router.push('/home'), 700)
  } else {
    const em = email.value.trim()
    if (em && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(em)) {
      return showToast('灵魂烙印（邮箱）格式有误。', 'err')
    }
    auth.register({ username: u, realm: realm.value, email: em })
    showToast(`守夜人「${u}」于【${realm.value}】觉醒，禁墟已铭刻。`, 'ok')
    setTimeout(() => router.push('/home'), 700)
  }
}
</script>

<template>
  <section class="card-wrap">
    <form class="card" @submit.prevent="onSubmit">
      <h2><span class="dot"></span><span>{{ formTitle }}</span></h2>
      <p class="hint">{{ formHint }}</p>

      <div class="tabs">
        <button type="button" :class="{ active: isLogin }" @click="setMode('login')">登 录</button>
        <button type="button" :class="{ active: !isLogin }" @click="setMode('register')">觉 醒</button>
      </div>

      <div class="field">
        <svg class="ic" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="8" r="4" /><path d="M4 21c0-4 4-6 8-6s8 2 8 6" />
        </svg>
        <input v-model="username" type="text" placeholder="守夜人代号 / 代号即姓名" required />
      </div>

      <div class="field" v-show="!isLogin">
        <svg class="ic" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="5" width="18" height="14" rx="2" /><path d="M3 7l9 6 9-6" />
        </svg>
        <input v-model="email" type="email" placeholder="灵魂烙印 · 邮箱" />
      </div>

      <div class="field">
        <svg class="ic" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="5" y="11" width="14" height="9" rx="2" /><path d="M8 11V7a4 4 0 018 0v4" />
        </svg>
        <input v-model="password" :type="showPw ? 'text' : 'password'"
               placeholder="禁墟密钥 / 横刀之誓" required />
        <svg class="toggle-pw" viewBox="0 0 24 24" fill="none" stroke="currentColor"
             stroke-width="2" @click="showPw = !showPw">
          <path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7-10-7-10-7z" />
          <circle cx="12" cy="12" r="3" />
        </svg>
      </div>

      <!-- 觉醒专属：禁墟境界 -->
      <div class="realm" v-show="!isLogin">
        <button type="button" v-for="r in realms" :key="r"
                :class="{ on: realm === r }" @click="realm = r">{{ r }}</button>
      </div>

      <div class="row" v-show="isLogin">
        <label><input type="checkbox" v-model="remember" /> 铭记此身（记住我）</label>
        <a href="#" @click.prevent="showToast('密钥已封存于诸神精神病院，请联系总司令重置。')">遗忘密钥？</a>
      </div>

      <button class="submit" type="submit">{{ submitText }}</button>

      <p class="alt">
        <template v-if="isLogin">尚未觉醒禁墟？ <a href="#" @click.prevent="setMode('register')">在此觉醒 →</a></template>
        <template v-else>已是守夜人？ <a href="#" @click.prevent="setMode('login')">返回登录 →</a></template>
      </p>
    </form>

    <transition name="toast">
      <div class="toast" v-if="toast.show" :class="toast.type">{{ toast.msg }}</div>
    </transition>
  </section>
</template>

<style scoped>
.card-wrap { display: flex; justify-content: center; position: relative; }

.card {
  width: min(420px, 100%);
  background: var(--glass);
  border: 1px solid var(--glass-bd);
  border-radius: 22px;
  padding: 38px 34px 34px;
  backdrop-filter: blur(22px) saturate(140%);
  box-shadow: 0 24px 80px rgba(0, 0, 0, .55), inset 0 1px 0 rgba(255, 255, 255, .06);
  position: relative; overflow: hidden;
}
.card::before {
  content: ""; position: absolute; inset: -1px; border-radius: 22px; padding: 1px;
  background: linear-gradient(130deg, rgba(46,230,255,.7), rgba(177,77,255,.5), rgba(255,77,141,.6));
  -webkit-mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
  -webkit-mask-composite: xor; mask-composite: exclude;
  opacity: .8; pointer-events: none;
}
.card::after {
  content: ""; position: absolute; top: -60%; left: -30%; width: 60%; height: 220%;
  background: linear-gradient(90deg, transparent, rgba(46,230,255,.10), transparent);
  transform: rotate(20deg); animation: sweep 7s ease-in-out infinite;
}
@keyframes sweep { 0%,100%{ left:-40%; } 50%{ left:120%; } }

.card h2 {
  font-size: 23px; font-weight: 700; letter-spacing: .06em; margin-bottom: 4px;
  display: flex; align-items: center; gap: 10px;
}
.card h2 .dot {
  width: 9px; height: 9px; border-radius: 50%; background: var(--cyan);
  box-shadow: 0 0 12px var(--cyan); animation: pulse 1.8s infinite;
}
@keyframes pulse { 0%,100%{ opacity:1; transform:scale(1);} 50%{ opacity:.4; transform:scale(.7);} }
.card .hint { font-size: 13px; color: var(--ink-dim); margin-bottom: 26px; }

.tabs {
  display: flex; gap: 8px; background: rgba(0,0,0,.25); padding: 5px;
  border-radius: 12px; margin-bottom: 24px;
}
.tabs button {
  flex: 1; border: 0; background: transparent; color: var(--ink-dim);
  padding: 10px 0; border-radius: 8px; font-size: 14px; font-weight: 600;
  cursor: pointer; transition: .25s; font-family: inherit;
}
.tabs button.active {
  color: #fff; background: linear-gradient(100deg, rgba(46,230,255,.22), rgba(177,77,255,.22));
  box-shadow: inset 0 0 0 1px rgba(120,150,255,.3);
}

.field { position: relative; margin-bottom: 16px; }
.field .ic {
  position: absolute; left: 14px; top: 50%; transform: translateY(-50%);
  color: var(--ink-dim); width: 18px; height: 18px; pointer-events: none;
}
.field input {
  width: 100%; padding: 14px 14px 14px 44px; border-radius: 12px;
  background: rgba(8,10,24,.6); border: 1px solid rgba(120,150,255,.16);
  color: #fff; font-size: 14.5px; font-family: inherit; letter-spacing: .02em;
  transition: .25s; outline: none;
}
.field input::placeholder { color: #5a6390; }
.field input:focus {
  border-color: var(--cyan);
  box-shadow: 0 0 0 3px rgba(46,230,255,.12), 0 0 22px rgba(46,230,255,.25);
  background: rgba(10,14,32,.8);
}
.field .toggle-pw {
  position: absolute; right: 12px; top: 50%; transform: translateY(-50%);
  color: var(--ink-dim); cursor: pointer; width: 20px; height: 20px;
}
.field .toggle-pw:hover { color: var(--cyan); }

.row {
  display: flex; justify-content: space-between; align-items: center;
  margin: 4px 2px 22px; font-size: 13px;
}
.row label { display: flex; align-items: center; gap: 7px; color: var(--ink-dim); cursor: pointer; }
.row input[type=checkbox] { accent-color: var(--cyan); width: 15px; height: 15px; }
.row a { color: var(--cyan); text-decoration: none; }
.row a:hover { text-decoration: underline; }

.submit {
  width: 100%; padding: 15px; border: 0; border-radius: 13px; cursor: pointer;
  font-size: 15.5px; font-weight: 700; letter-spacing: .14em; color: #04050d;
  font-family: inherit;
  background: linear-gradient(100deg, var(--cyan), var(--violet) 55%, var(--pink));
  box-shadow: var(--neon); transition: .25s; position: relative; overflow: hidden;
}
.submit:hover {
  filter: brightness(1.08); transform: translateY(-1px);
  box-shadow: 0 0 26px rgba(46,230,255,.7), 0 0 60px rgba(177,77,255,.45);
}
.submit:active { transform: translateY(0); }

.alt { text-align: center; margin-top: 20px; font-size: 13px; color: var(--ink-dim); }
.alt a { color: var(--gold); text-decoration: none; font-weight: 600; }
.alt a:hover { text-decoration: underline; }

.realm { display: flex; gap: 8px; margin: 0 0 20px; }
.realm button {
  flex: 1; border: 1px solid rgba(120,150,255,.16); background: rgba(8,10,24,.4);
  color: var(--ink-dim); border-radius: 10px; padding: 9px 0; font-size: 12.5px;
  cursor: pointer; font-family: inherit; transition: .2s; letter-spacing: .06em;
}
.realm button.on {
  color: #fff; border-color: var(--violet);
  background: linear-gradient(100deg, rgba(46,230,255,.16), rgba(177,77,255,.16));
  box-shadow: 0 0 16px rgba(177,77,255,.25);
}

.toast {
  position: fixed; left: 50%; bottom: 34px; transform: translateX(-50%);
  background: rgba(10,14,32,.92); border: 1px solid var(--glass-bd); color: #fff;
  padding: 12px 22px; border-radius: 12px; font-size: 14px; z-index: 20;
  box-shadow: 0 10px 40px rgba(0,0,0,.5); backdrop-filter: blur(10px);
  white-space: nowrap;
}
.toast.err { border-color: rgba(255,77,141,.6); }
.toast.ok  { border-color: rgba(46,230,255,.6); }
.toast-enter-active, .toast-leave-active { transition: .35s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(-50%) translateY(40px); }
</style>
