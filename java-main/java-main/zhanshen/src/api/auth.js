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

// 响应拦截：401（令牌失效）时清理会话，交由路由守卫回登录页
api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401) {
      clearSession()
      if (location.hash !== '#/' && location.pathname !== '/') {
        location.hash = '#/'
      }
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

export default api
