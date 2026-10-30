<script setup>
import { onMounted, reactive, ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth, clearSession } from '../store/auth'
import { me, updateProfile, changePassword } from '../api/auth'

const router = useRouter()
const { user, setSession } = useAuth()

// 拉取最新守夜人信息，确保表单带出服务端最新资料
const loading = ref(false)
const toast = ref({ show: false, msg: '', ok: true })
let toastTimer
function showToast(msg, ok = true) {
  toast.value = { show: true, msg, ok }
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value.show = false), 2600)
}

// 资料表单（境界选项与后端 UserServiceImpl.REALMS 严格一致，避免保存被静默忽略）
const REALMS = ['盏境', '池境', '川境', '海境']
const profile = reactive({
  nickname: '',
  avatar: '',
  phone: '',
  bio: '',
  email: '',
  realm: ''
})

// 修改密码表单
const pwd = reactive({
  oldPassword: '',
  newPassword: '',
  confirm: ''
})

const savingProfile = ref(false)
const savingPwd = ref(false)

// 头像加载失败时回退为首字母（避免破图）
const avatarErr = ref(false)
watch(() => profile.avatar, () => (avatarErr.value = false))

// 密码可见性切换
const showPwd = reactive({ old: false, new: false, confirm: false })
const pwdMismatch = computed(() => pwd.confirm.length > 0 && pwd.newPassword !== pwd.confirm)

// 格式校验
const EMAIL_RE = /^[\w.+-]+@[\w-]+(\.[\w-]+)+$/
const PHONE_RE = /^1\d{10}$/
function validateProfile() {
  const email = profile.email.trim()
  if (email && !EMAIL_RE.test(email)) return '邮箱格式有误，请检查后再铭刻。'
  const phone = profile.phone.trim()
  if (phone && !PHONE_RE.test(phone)) return '手机号需为 1 开头的 11 位数字。'
  const avatar = profile.avatar.trim()
  if (avatar && !/^(https?:\/\/|data:image\/)/i.test(avatar)) return '头像地址需以 http://、https:// 或 data:image 开头。'
  return ''
}

const avatarPreview = computed(() =>
  profile.avatar?.trim() ||
  (user.value?.avatar?.trim()) ||
  null
)
const initialLetter = computed(() =>
  (profile.nickname || user.value?.nickname || user.value?.username || '无')[0]
)

async function loadProfile() {
  loading.value = true
  try {
    const { data } = await me()
    const u = data?.data || user.value
    if (u) {
      profile.nickname = u.nickname || ''
      profile.avatar = u.avatar || ''
      profile.phone = u.phone || ''
      profile.bio = u.bio || ''
      profile.email = u.email || ''
      profile.realm = u.realm || ''
    }
  } catch {
    showToast('神域动荡，资料暂时无法载入。', false)
  } finally {
    loading.value = false
  }
}

onMounted(loadProfile)

// 保存资料
async function saveProfile() {
  if (savingProfile.value) return
  const bad = validateProfile()
  if (bad) return showToast(bad, false)
  savingProfile.value = true
  try {
    const { data } = await updateProfile({
      nickname: profile.nickname.trim() || null,
      avatar: profile.avatar.trim() || null,
      phone: profile.phone.trim() || null,
      bio: profile.bio.trim() || null,
      email: profile.email.trim() || null,
      realm: profile.realm || null
    })
    if (data?.code === 0) {
      // 用返回的最新资料刷新本地会话（保持首页/导航同步）
      if (data.data) setSession(data.data, null, null)
      else await loadProfile()
      showToast('资料已铭刻于神域石碑。')
    } else {
      showToast(data?.message || '资料更新失败。', false)
    }
  } catch (e) {
    showToast(e?.response?.data?.message || '资料更新失败，请稍后再试。', false)
  } finally {
    savingProfile.value = false
  }
}

// 修改密码
async function savePassword() {
  if (savingPwd.value) return
  if (!pwd.oldPassword) return showToast('请先输入原禁墟密钥。', false)
  if (!pwd.newPassword || pwd.newPassword.length < 4) return showToast('新密钥至少需 4 位。', false)
  if (pwd.newPassword !== pwd.confirm) return showToast('两次输入的密钥不一致。', false)
  savingPwd.value = true
  try {
    const { data } = await changePassword({
      oldPassword: pwd.oldPassword,
      newPassword: pwd.newPassword
    })
    if (data?.code === 0) {
      showToast('禁墟密钥已重塑，即将返回神域重新认证…')
      pwd.oldPassword = ''
      pwd.newPassword = ''
      pwd.confirm = ''
      // 旧令牌不会随改密失效，为确保安全强制退出重新登录
      setTimeout(() => {
        clearSession()
        location.hash = '#/'
      }, 1600)
    } else {
      showToast(data?.message || '密钥修改失败。', false)
    }
  } catch (e) {
    showToast(e?.response?.data?.message || '密钥修改失败，请稍后再试。', false)
  } finally {
    savingPwd.value = false
  }
}

function goHome() {
  router.push('/home')
}
</script>

<template>
  <main class="profile">
    <div class="topbar">
      <button class="back" @click="goHome">← 返回神域</button>
      <h1>个人中心</h1>
      <span class="sub">守夜人「{{ user?.username || '无名' }}」</span>
    </div>

    <div v-if="loading" class="loading">
      <span class="spinner"></span> 正在唤醒守夜人记忆…
    </div>

    <div v-else class="grid">
      <!-- 资料卡片：头像 + 身份 -->
      <aside class="idcard">
        <div class="avatar-wrap">
          <img v-if="avatarPreview && !avatarErr" :src="avatarPreview" alt="avatar" class="avatar-img" @error="avatarErr = true" />
          <div v-else class="avatar-letter">{{ initialLetter }}</div>
          <div class="avatar-ring"></div>
        </div>
        <div class="idname">{{ profile.nickname || user?.username || '无名守夜人' }}</div>
        <div class="idrealm">境界 · {{ profile.realm || user?.realm || '—' }}</div>
        <div class="idperm" :class="user?.permission === 0 ? 'admin' : 'normal'">
          {{ user?.permission === 0 ? '执政官（管理员）' : '普通守夜人' }}
        </div>
      </aside>

      <!-- 编辑区 -->
      <section class="panels">
        <div class="panel">
          <h2>资料编辑</h2>
          <div class="form">
            <label class="f">
              <span>昵称</span>
              <input v-model="profile.nickname" type="text" maxlength="50" placeholder="展示用昵称（可空）" />
            </label>
            <label class="f">
              <span>头像 URL</span>
              <input v-model="profile.avatar" type="text" maxlength="255" placeholder="https://…（可空，留空显示首字母）" />
            </label>
            <label class="f">
              <span>邮箱</span>
              <input v-model="profile.email" type="text" maxlength="120" placeholder="联系邮箱（可空）" />
            </label>
            <label class="f">
              <span>手机号</span>
              <input v-model="profile.phone" type="text" maxlength="20" placeholder="手机号（可空）" />
            </label>
            <label class="f">
              <span>禁墟境界</span>
              <select v-model="profile.realm">
                <option value="">未定境</option>
                <option v-for="r in REALMS" :key="r" :value="r">{{ r }}</option>
              </select>
            </label>
            <label class="f f-full">
              <span>个人简介</span>
              <textarea v-model="profile.bio" maxlength="500" rows="3" placeholder="一句话介绍自己（可空）"></textarea>
            </label>
          </div>
          <button class="save" :disabled="savingProfile" @click="saveProfile">
            {{ savingProfile ? '保存中…' : '铭刻资料' }}
          </button>
        </div>

        <div class="panel">
          <h2>重塑禁墟密钥</h2>
          <div class="form">
            <label class="f">
              <span>原密钥</span>
              <span class="pwd-box">
                <input v-model="pwd.oldPassword" :type="showPwd.old ? 'text' : 'password'" placeholder="请输入当前密码" @keyup.enter="savePassword" />
                <button type="button" class="eye" @click="showPwd.old = !showPwd.old" :title="showPwd.old ? '隐藏' : '显示'">{{ showPwd.old ? '🙈' : '👁' }}</button>
              </span>
            </label>
            <label class="f">
              <span>新密钥</span>
              <span class="pwd-box">
                <input v-model="pwd.newPassword" :type="showPwd.new ? 'text' : 'password'" placeholder="至少 4 位" @keyup.enter="savePassword" />
                <button type="button" class="eye" @click="showPwd.new = !showPwd.new" :title="showPwd.new ? '隐藏' : '显示'">{{ showPwd.new ? '🙈' : '👁' }}</button>
              </span>
            </label>
            <label class="f">
              <span>确认新密钥</span>
              <span class="pwd-box">
                <input v-model="pwd.confirm" :type="showPwd.confirm ? 'text' : 'password'" placeholder="再输一次新密码" @keyup.enter="savePassword" />
                <button type="button" class="eye" @click="showPwd.confirm = !showPwd.confirm" :title="showPwd.confirm ? '隐藏' : '显示'">{{ showPwd.confirm ? '🙈' : '👁' }}</button>
              </span>
              <span v-if="pwdMismatch" class="hint-bad">两次输入的密钥不一致</span>
            </label>
          </div>
          <button class="save pwd" :disabled="savingPwd || pwdMismatch" @click="savePassword">
            {{ savingPwd ? '保存中…' : '更新密钥' }}
          </button>
        </div>
      </section>
    </div>

    <transition name="toast">
      <div class="toast" :class="{ ok: toast.ok, bad: !toast.ok }" v-if="toast.show">{{ toast.msg }}</div>
    </transition>
  </main>
</template>

<style scoped>
.profile {
  position: relative; z-index: 2;
  min-height: 100vh; overflow-y: auto;
  padding: 0 clamp(20px, 5vw, 56px) 60px;
  background:
    radial-gradient(900px 420px at 12% -8%, rgba(46,230,255,.10), transparent 60%),
    radial-gradient(820px 420px at 92% 0%, rgba(177,77,255,.12), transparent 60%);
}
.profile::-webkit-scrollbar { width: 8px; }
.profile::-webkit-scrollbar-thumb { background: rgba(120,150,255,.25); border-radius: 8px; }
.profile::-webkit-scrollbar-track { background: transparent; }

.topbar {
  position: sticky; top: 0; z-index: 5;
  display: flex; align-items: center; gap: 18px;
  padding: 18px 0; margin-bottom: 18px;
  background: linear-gradient(180deg, rgba(4,5,13,.85), rgba(4,5,13,.4) 70%, transparent);
  backdrop-filter: blur(10px);
}
.topbar h1 {
  font-size: 22px; font-weight: 800; letter-spacing: .14em; margin: 0;
  background: linear-gradient(100deg, var(--cyan), var(--violet), var(--pink));
  -webkit-background-clip: text; background-clip: text; color: transparent;
}
.topbar .sub { color: var(--ink-dim); font-size: 13.5px; letter-spacing: .06em; }
.back {
  border: 1px solid var(--glass-bd); background: rgba(255,255,255,.03);
  color: var(--ink); padding: 9px 16px; border-radius: 10px; cursor: pointer;
  font-family: inherit; font-size: 13px; transition: .2s;
}
.back:hover { border-color: var(--cyan); color: #fff; box-shadow: 0 0 16px rgba(46,230,255,.3); }

.loading { text-align: center; padding: 70px 0; color: var(--ink-dim); display: flex; gap: 12px; align-items: center; justify-content: center; }
.spinner {
  width: 16px; height: 16px; border-radius: 50%;
  border: 2px solid rgba(46,230,255,.25); border-top-color: var(--cyan);
  animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.grid {
  max-width: 980px; margin: 0 auto;
  display: grid; grid-template-columns: 280px 1fr; gap: 22px;
  animation: riseIn .6s cubic-bezier(.22,.61,.36,1) both;
}
@keyframes riseIn { from { opacity: 0; transform: translateY(18px); } to { opacity: 1; transform: none; } }

/* 身份卡 */
.idcard {
  align-self: start;
  position: sticky; top: 92px;
  padding: 26px 22px; border-radius: 18px; text-align: center;
  background: var(--glass); border: 1px solid var(--glass-bd); backdrop-filter: blur(16px);
  box-shadow: 0 18px 50px rgba(0,0,0,.45);
}
.avatar-wrap { position: relative; width: 96px; height: 96px; margin: 0 auto 16px; }
.avatar-img, .avatar-letter {
  width: 96px; height: 96px; border-radius: 50%; object-fit: cover;
  display: grid; place-items: center;
  background: linear-gradient(135deg, var(--violet), var(--pink)); color: #04050d;
  font-size: 38px; font-weight: 800; font-family: serif;
}
.avatar-ring {
  position: absolute; inset: -6px; border-radius: 50%;
  border: 1px solid rgba(245,210,122,.5); opacity: .7; animation: ringSpin 6s linear infinite;
}
@keyframes ringSpin { to { transform: rotate(360deg); } }
.idname { font-size: 18px; color: #fff; font-weight: 700; letter-spacing: .04em; }
.idrealm { margin-top: 6px; font-size: 13px; color: var(--ink-dim); letter-spacing: .06em; }
.idperm {
  display: inline-block; margin-top: 12px; font-size: 12px; letter-spacing: .08em;
  padding: 3px 12px; border-radius: 999px;
}
.idperm.admin { color: var(--gold); border: 1px solid rgba(245,210,122,.35); background: rgba(245,210,122,.08); }
.idperm.normal { color: var(--cyan); border: 1px solid rgba(46,230,255,.3); background: rgba(46,230,255,.07); }

/* 面板 */
.panels { display: flex; flex-direction: column; gap: 22px; }
.panel {
  padding: 24px; border-radius: 18px;
  background: var(--glass); border: 1px solid var(--glass-bd); backdrop-filter: blur(16px);
  box-shadow: 0 18px 50px rgba(0,0,0,.4);
}
.panel h2 {
  font-size: 16px; color: #fff; margin: 0 0 18px; letter-spacing: .1em;
  position: relative; padding-left: 14px;
}
.panel h2::before {
  content: ""; position: absolute; left: 0; top: 2px; bottom: 2px; width: 4px; border-radius: 4px;
  background: linear-gradient(180deg, var(--cyan), var(--pink));
}
.form { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.f { display: flex; flex-direction: column; gap: 7px; }
.f-full { grid-column: 1 / -1; }
.f span { font-size: 12.5px; color: var(--ink-dim); letter-spacing: .08em; }
.f input, .f select, .f textarea {
  border: 1px solid var(--glass-bd); background: rgba(8,10,24,.5); color: #fff;
  border-radius: 11px; padding: 11px 13px; font-family: inherit; font-size: 14px;
  outline: none; transition: .2s; resize: vertical;
}
.f input::placeholder, .f textarea::placeholder { color: #5a6390; }
.f input:focus, .f select:focus, .f textarea:focus {
  border-color: transparent;
  box-shadow: 0 0 0 3px rgba(46,230,255,.12), 0 0 22px rgba(46,230,255,.22);
}
.f select { appearance: none; cursor: pointer; }

/* 密码输入框：可见性切换 + 不一致提示 */
.pwd-box { position: relative; display: flex; }
.pwd-box input { width: 100%; padding-right: 44px; }
.pwd-box .eye {
  position: absolute; right: 8px; top: 50%; transform: translateY(-50%);
  background: none; border: 0; cursor: pointer; font-size: 15px; opacity: .55;
  transition: .2s; padding: 4px; line-height: 1;
}
.pwd-box .eye:hover { opacity: 1; }
.hint-bad { font-size: 12px; color: var(--pink); letter-spacing: .05em; }

.save {
  margin-top: 20px; border: 0; border-radius: 12px; padding: 12px 26px; cursor: pointer;
  font-size: 14.5px; font-weight: 700; letter-spacing: .12em; color: #04050d; font-family: inherit;
  background: linear-gradient(100deg, var(--cyan), var(--violet) 55%, var(--pink));
  box-shadow: var(--neon); transition: .22s; position: relative; overflow: hidden;
}
.save::after {
  content: ""; position: absolute; top: 0; left: -120%; width: 60%; height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,.5), transparent);
  transform: skewX(-20deg); animation: sweep 3.4s ease-in-out infinite;
}
@keyframes sweep { 0%,60% { left: -120%; } 100% { left: 140%; } }
.save:hover { filter: brightness(1.08); transform: translateY(-1px); }
.save:disabled { opacity: .6; cursor: not-allowed; }
.save.pwd { background: linear-gradient(100deg, var(--gold), var(--pink)); }

.toast {
  position: fixed; left: 50%; bottom: 34px; transform: translateX(-50%);
  background: rgba(10,14,32,.92); border: 1px solid var(--glass-bd); color: #fff;
  padding: 12px 22px; border-radius: 12px; font-size: 14px; z-index: 20;
  box-shadow: 0 10px 40px rgba(0,0,0,.5); backdrop-filter: blur(10px); white-space: nowrap;
}
.toast.ok { border-color: rgba(46,230,255,.4); }
.toast.bad { border-color: rgba(255,77,141,.5); color: #ffd9e6; }
.toast-enter-active, .toast-leave-active { transition: .35s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(-50%) translateY(40px); }

@media (max-width: 820px) {
  .grid { grid-template-columns: 1fr; }
  .idcard { position: relative; top: 0; }
  .form { grid-template-columns: 1fr; }
}
@media (prefers-reduced-motion: reduce) {
  * { animation-duration: .001ms !important; animation-iteration-count: 1 !important; transition-duration: .001ms !important; }
}
</style>
