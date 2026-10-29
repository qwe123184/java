import axios from 'axios'
import { getAccessToken, clearSession, applyTokens } from '../store/auth'

// 统一与后端 springBootTest 通信的 axios 实例
const api = axios.create({ baseURL: '/api', timeout: 15000 })

// 请求拦截：自动附加访问令牌
api.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截：401（令牌失效）时清理会话，并按当前所在模块回对应登录页
api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401) {
      clearSession()
      // 在后台模块则回后台登录页，否则回首页登录页
      const onAdmin = location.hash.startsWith('#/admin')
      location.hash = onAdmin ? '#/admin/login' : '#/'
    }
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

// 以刷新令牌换发新令牌对
// 注意：当前后端 AuthInterceptor 要求 /api/** 除 login/register 外都需有效 accessToken，
// 因此 refresh 端点本身也需要 accessToken。待后端放开后可直接调用。
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
