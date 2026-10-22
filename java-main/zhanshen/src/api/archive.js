import api from './auth'

// 神域档案：首页展示 + 搜索
// 复用 auth.js 中的 axios 实例（baseURL=/api，自动附加访问令牌）
export function archiveList({ keyword, type, page = 1, size = 50 } = {}) {
  return api.get('/archive/list', {
    params: { keyword, type, page, size }
  })
}

// 档案详情
export function archiveDetail(id) {
  return api.get(`/archive/${id}`)
}
