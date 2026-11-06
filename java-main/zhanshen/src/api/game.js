import api from './auth'

// 神域对决中心：对决擂台 / 禁墟答题 / 反应试炼
export function duelCreate(payload) {
  return api.post('/game/duel/create', payload)
}
export function duelAccept(roomId) {
  return api.post(`/game/duel/${roomId}/accept`)
}
export function duelMove(roomId, choice) {
  return api.post(`/game/duel/${roomId}/move`, { choice })
}
export function duelRoom(roomId) {
  return api.get(`/game/duel/${roomId}`)
}
export function duelMine() {
  return api.get('/game/duel/mine')
}
export function duelLeaderboard() {
  return api.get('/game/duel/leaderboard')
}

export function quizCreate(payload) {
  return api.post('/game/quiz/create', payload)
}
export function quizAccept(roomId) {
  return api.post(`/game/quiz/${roomId}/accept`)
}
export function quizAnswer(roomId, choice, questionId) {
  return api.post(`/game/quiz/${roomId}/answer`, { choice, questionId })
}
export function quizRoom(roomId) {
  return api.get(`/game/quiz/${roomId}`)
}
export function quizMine() {
  return api.get('/game/quiz/mine')
}
export function quizLeaderboard() {
  return api.get('/game/quiz/leaderboard')
}

export function reactionSubmit(ms) {
  return api.post('/game/reaction/submit', { ms })
}
export function reactionLeaderboard() {
  return api.get('/game/reaction/leaderboard')
}
