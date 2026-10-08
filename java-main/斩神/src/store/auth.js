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

// 单例响应式会话状态（模拟后端，刷新后仍可保持登录）
const session = reactive(load() || { user: null, token: null })

function persist() {
  if (session.token) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
  } else {
    localStorage.removeItem(STORAGE_KEY)
  }
}

export function useAuth() {
  const isAuthed = computed(() => !!session.token)
  const user = computed(() => session.user)

  function login(user) {
    session.user = user
    session.token = 'zs-' + Math.random().toString(36).slice(2)
    persist()
  }

  function register(user) {
    login(user) // 演示用：觉醒即视为建立会话
  }

  function logout() {
    session.user = null
    session.token = null
    persist()
  }

  return { isAuthed, user, login, register, logout }
}
