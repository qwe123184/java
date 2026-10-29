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

// 后台档案库列表（含下架），支持关键词 / 类型 / 状态筛选
export function archiveAdminList({ keyword, type, status, page = 1, size = 20 } = {}) {
  return api.get('/archive/admin/list', {
    params: { keyword, type, status, page, size }
  })
}

// 新增档案（后台管理）
export function archiveCreate(payload) {
  return api.post('/archive', payload)
}

// 修改档案（后台管理）
export function archiveUpdate(id, payload) {
  return api.put(`/archive/${id}`, payload)
}

// 删除档案（后台管理）
export function archiveDelete(id) {
  return api.delete(`/archive/${id}`)
}

// 批量上下架档案（后台管理）：ids 列表 + status（0 下架 / 1 展示）
export function archiveBatchStatus(ids, status) {
  return api.put('/archive/status/batch', { ids, status })
}
