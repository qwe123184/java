import axios from 'axios'
import { getAccessToken, getRefreshToken, clearSession, applyTokens } from '../store/auth'

// 统一与后端 springBootTest 通信的 axios 实例
const api = axios.create({ baseURL: '/api', timeout: 15000 })

// 请求拦截：自动附加访问令牌
api.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 静默刷新令牌：用 refreshToken 换发新的令牌对（独立实例，避免递归拦截）
let refreshing = null
function refreshTokens() {
  if (!refreshing) {
    const rt = getRefreshToken()
    if (!rt) return null
    refreshing = axios
      .create({ baseURL: '/api', timeout: 15000 })
      .post('/user/refresh', { refreshToken: rt })
      .then((res) => {
        const d = res.data?.data
        if (res.data?.code === 0 && d?.accessToken) {
          applyTokens({ accessToken: d.accessToken, refreshToken: d.refreshToken })
          return d.accessToken
        }
        throw new Error('refresh failed')
      })
      .finally(() => { refreshing = null })
  }
  return refreshing
}

function redirectToAuth() {
  clearSession()
  const onAdmin = location.hash.startsWith('#/admin')
  location.hash = onAdmin ? '#/admin/login' : '#/'
}

// 响应拦截：401（访问令牌过期）时先用 refreshToken 静默续期并重放原请求；
// 续期失败才清理会话并回登录页。避免答题/对局中途被登出导致进度丢失。
api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const config = error.config
    if (
      error.response?.status === 401 &&
      config &&
      !config.__retried &&
      !String(config.url || '').includes('/user/refresh') &&
      getRefreshToken()
    ) {
      try {
        const newToken = await refreshTokens()
        if (newToken) {
          config.__retried = true
          config.headers.Authorization = `Bearer ${newToken}`
          return api.request(config)
        }
      } catch (e) {
        // 续期失败，走下方统一登出
      }
    }
    if (error.response?.status === 401) redirectToAuth()
    return Promise.reject(error)
  }
)

// ===== 业务接口 =====
// 觉醒禁墟（注册）
export function register(payload) {
  return api.post('/user/register', payload)
}

// 进入神域（登录）
export function login(payload) {
  return api.post('/user/login', payload)
}

// 执政官进入后台（管理员登录，仅 ADMIN 角色）
export function adminLogin(payload) {
  return api.post('/admin/login', payload)
}

// 获取当前执政官信息
export function adminProfile() {
  return api.get('/admin/me')
}

// 以刷新令牌换发新令牌对（后端已放行 /user/refresh，无需携带 accessToken）
export function refreshToken(refreshToken) {
  return api.post('/user/refresh', { refreshToken })
}

// 获取当前守夜人信息
export function me() {
  return api.get('/user/me')
}

// 修改个人资料（昵称/头像/手机/简介/邮箱/境界）
export function updateProfile(payload) {
  return api.put('/user/profile', payload)
}

// 修改密码
export function changePassword(payload) {
  return api.post('/user/password', payload)
}

// ===== 后台用户管理（仅 ADMIN）=====
// 用户列表（关键词 / 权限 / 状态筛选）
export function adminUserList({ keyword, permission, status, page = 1, size = 20 } = {}) {
  return api.get('/admin/users', {
    params: {
      keyword: keyword || undefined,
      permission: permission == null ? undefined : permission,
      status: status == null ? undefined : status,
      page,
      size
    }
  })
}

// 封禁 / 解封账号（status: 0 正常 / 1 封禁）
export function adminUserSetStatus(id, status) {
  return api.put(`/admin/users/${id}/status`, null, { params: { status } })
}

// 调整权限（permission: 0 管理员 / 1 默认）
export function adminUserSetPermission(id, permission) {
  return api.put(`/admin/users/${id}/permission`, null, { params: { permission } })
}

// 删除账号
export function adminUserDelete(id) {
  return api.delete(`/admin/users/${id}`)
}

export default api
