<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../store/auth'
import { adminLogin } from '../api/auth'

const router = useRouter()
const auth = useAuth()

const username = ref('')
const password = ref('')
const showPw = ref(false)
const loading = ref(false)
const toast = ref({ show: false, msg: '', type: '' })
let toastTimer

const formTitle = '执 政 官 登 录'
const formHint = '后台入口 · 仅神域执政官可凭印信进入。'

const submitText = computed(() => (loading.value ? '核 验 中 …' : '进 入 后 台'))

function showToast(msg, type = '') {
  toast.value = { show: true, msg, type }
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value.show = false), 2800)
}

async function onSubmit() {
  if (loading.value) return
  const u = username.value.trim()
  const p = password.value.trim()
  if (!u) return showToast('请输入执政官印名。', 'err')
  if (!p) return showToast('请输入后台密钥。', 'err')

  loading.value = true
  try {
    const { data } = await adminLogin({ username: u, password: p })
    auth.setSession(data.data.user, data.data.accessToken, data.data.refreshToken)
    showToast(data.msg || '印信核验通过，正在开启后台…', 'ok')
    setTimeout(() => router.push('/admin'), 600)
  } catch (e) {
    showToast(e.response?.data?.msg || '印信核验失败，请确认执政官身份。', 'err')
  } finally {
    loading.value = false
  }
}

function backHome() {
  router.push('/')
}
</script>

<template>
  <main class="admin-login">
    <div class="glow" aria-hidden="true"></div>

    <form class="card" @submit.prevent="onSubmit">
      <div class="crest">⚙</div>
      <h2>{{ formTitle }}</h2>
      <p class="hint">{{ formHint }}</p>

      <div class="field">
        <svg class="ic" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="8" r="4" /><path d="M4 21c0-4 4-6 8-6s8 2 8 6" />
        </svg>
        <input v-model="username" type="text" placeholder="执政官印名" autocomplete="username" required />
      </div>

      <div class="field">
        <svg class="ic" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="5" y="11" width="14" height="9" rx="2" /><path d="M8 11V7a4 4 0 018 0v4" />
        </svg>
        <input v-model="password" :type="showPw ? 'text' : 'password'"
               placeholder="后台密钥" autocomplete="current-password" required />
        <svg class="toggle-pw" viewBox="0 0 24 24" fill="none" stroke="currentColor"
             stroke-width="2" @click="showPw = !showPw">
          <path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7-10-7-10-7z" />
          <circle cx="12" cy="12" r="3" />
        </svg>
      </div>

      <button class="submit" type="submit" :disabled="loading">{{ submitText }}</button>

      <button class="back" type="button" @click="backHome">← 返回神域大门</button>
    </form>

    <transition name="toast">
      <div class="toast" :data-type="toast.type" v-if="toast.show">{{ toast.msg }}</div>
    </transition>
  </main>
</template>

<style scoped>
.admin-login {
  position: relative; z-index: 2;
  min-height: 100vh;
  display: grid; place-items: center;
  padding: 40px 22px;
}
.glow {
  position: absolute; left: 50%; top: 18%;
  width: 460px; height: 260px; transform: translateX(-50%);
  background: radial-gradient(closest-side, rgba(245,210,122,.16), rgba(255,77,141,.10) 55%, transparent 75%);
  filter: blur(16px); pointer-events: none; z-index: -1;
  animation: glowBreathe 6s ease-in-out infinite;
}
@keyframes glowBreathe {
  0%,100% { opacity: .55; transform: translateX(-50%) scale(1); }
  50% { opacity: 1; transform: translateX(-50%) scale(1.07); }
}

.card {
  width: min(400px, 100%);
  padding: 36px 32px 28px; border-radius: 20px;
  background: var(--glass); border: 1px solid var(--glass-bd);
  backdrop-filter: blur(24px) saturate(140%);
  box-shadow: 0 24px 70px rgba(0,0,0,.55), inset 0 1px 0 rgba(255,255,255,.06);
  animation: pop .35s cubic-bezier(.22,.61,.36,1) both;
}
@keyframes pop { from { opacity: 0; transform: translateY(18px) scale(.96); } to { opacity: 1; transform: none; } }

.crest {
  width: 56px; height: 56px; margin: 0 auto 14px;
  display: grid; place-items: center; font-size: 26px; border-radius: 16px;
  color: var(--gold);
  background: linear-gradient(135deg, rgba(245,210,122,.22), rgba(255,77,141,.18));
  border: 1px solid rgba(245,210,122,.4);
  box-shadow: 0 0 24px rgba(245,210,122,.28);
  animation: crestFloat 4s ease-in-out infinite;
}
@keyframes crestFloat { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-5px); } }

h2 {
  text-align: center; font-size: 22px; letter-spacing: .12em; color: #fff;
  background: linear-gradient(100deg, #fff, var(--gold) 65%, var(--pink));
  -webkit-background-clip: text; background-clip: text; color: transparent;
}
.hint { text-align: center; margin: 10px 0 24px; color: var(--ink-dim); font-size: 13px; }

.field {
  display: flex; align-items: center; gap: 10px;
  margin-bottom: 14px; padding: 0 14px; border-radius: 12px;
  background: rgba(8,10,24,.5); border: 1px solid var(--glass-bd);
  transition: .2s;
}
.field:focus-within {
  border-color: rgba(245,210,122,.55);
  box-shadow: 0 0 0 3px rgba(245,210,122,.1), 0 0 22px rgba(245,210,122,.18);
}
.ic { width: 20px; height: 20px; color: var(--gold); flex-shrink: 0; opacity: .8; }
.field input {
  flex: 1; border: 0; background: transparent; outline: none;
  color: #fff; font-size: 14.5px; font-family: inherit; padding: 13px 2px;
}
.field input::placeholder { color: #5a6390; }
.toggle-pw { width: 20px; height: 20px; color: #5a6390; cursor: pointer; flex-shrink: 0; transition: .2s; }
.toggle-pw:hover { color: var(--gold); }

.submit {
  width: 100%; margin-top: 8px; border: 0; border-radius: 12px;
  padding: 13px; cursor: pointer; font-family: inherit;
  font-size: 15px; font-weight: 700; letter-spacing: .16em; color: #04050d;
  background: linear-gradient(100deg, var(--gold), #ffb46b 55%, var(--pink));
  box-shadow: 0 6px 26px rgba(245,210,122,.35);
  transition: .25s;
}
.submit:hover:not(:disabled) { filter: brightness(1.08); transform: translateY(-1px); }
.submit:disabled { opacity: .6; cursor: wait; }

.back {
  display: block; margin: 16px auto 0;
  border: 0; background: transparent; cursor: pointer; font-family: inherit;
  color: var(--ink-dim); font-size: 13px; letter-spacing: .04em; transition: .2s;
}
.back:hover { color: var(--gold); }

.toast {
  position: fixed; left: 50%; bottom: 34px; transform: translateX(-50%);
  background: rgba(10,14,32,.92); border: 1px solid var(--glass-bd); color: #fff;
  padding: 12px 22px; border-radius: 12px; font-size: 14px; z-index: 20;
  box-shadow: 0 10px 40px rgba(0,0,0,.5); backdrop-filter: blur(10px);
  white-space: nowrap; max-width: 90vw; overflow: hidden; text-overflow: ellipsis;
}
.toast[data-type='err'] { border-color: rgba(255,77,141,.5); }
.toast[data-type='ok'] { border-color: rgba(245,210,122,.5); }
.toast-enter-active, .toast-leave-active { transition: .35s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(-50%) translateY(40px); }
</style>
