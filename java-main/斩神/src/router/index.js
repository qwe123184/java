import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuth } from '../store/auth'
import AuthView from '../views/AuthView.vue'
import HomeView from '../views/HomeView.vue'

const routes = [
  { path: '/', name: 'auth', component: AuthView },
  { path: '/home', name: 'home', component: HomeView, meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  const { isAuthed } = useAuth()
  if (to.meta.requiresAuth && !isAuthed.value) return '/'
})

export default router
