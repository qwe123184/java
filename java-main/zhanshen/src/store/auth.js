import { reactive, computed } from 'vue'

const STORAGE_KEY = 'zs_session'

function load() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

// 单例响应式会话状态：登录后持久化到 localStorage，刷新页面仍保持登录
const session = reactive(load() || { user: null, accessToken: null, refreshToken: null })

function persist() {
  if (session.accessToken) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
  } else {
    localStorage.removeItem(STORAGE_KEY)
  }
}

// 供 axios 拦截器使用的底层访问器
export function getAccessToken() {
  return session.accessToken
}
export function getRefreshToken() {
  return session.refreshToken
}
export function applyTokens({ accessToken, refreshToken }) {
  session.accessToken = accessToken
  if (refreshToken) session.refreshToken = refreshToken
  persist()
}
export function clearSession() {
  session.user = null
  session.accessToken = null
  session.refreshToken = null
  persist()
}

export function useAuth() {
  const isAuthed = computed(() => !!session.accessToken)
  const user = computed(() => session.user)

  // 登录/注册成功后写入会话
  function setSession(user, accessToken, refreshToken) {
    session.user = user
    session.accessToken = accessToken
    session.refreshToken = refreshToken
    persist()
  }

  function logout() {
    clearSession()
  }

  return { isAuthed, user, setSession, logout }
}
