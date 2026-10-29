import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuth } from '../store/auth'
import AuthView from '../views/AuthView.vue'
import HomeView from '../views/HomeView.vue'
import ProfileView from '../views/ProfileView.vue'
import AdminLoginView from '../views/AdminLoginView.vue'
import AdminView from '../views/AdminView.vue'

const routes = [
  { path: '/', name: 'auth', component: AuthView },
  { path: '/home', name: 'home', component: HomeView, meta: { requiresAuth: true } },
  { path: '/profile', name: 'profile', component: ProfileView, meta: { requiresAuth: true } },
  { path: '/admin/login', name: 'admin-login', component: AdminLoginView },
  { path: '/admin', name: 'admin', component: AdminView, meta: { requiresAuth: true, requiresAdmin: true } }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  const { isAuthed, isAdmin } = useAuth()

  // 需要登录但没登录：后台去登录页，普通去首页
  if (to.meta.requiresAuth && !isAuthed.value) {
    return to.meta.requiresAdmin ? '/admin/login' : '/'
  }
  // 需要管理员权限但当前不是管理员：退回后台登录页
  if (to.meta.requiresAdmin && !isAdmin.value) {
    return '/admin/login'
  }
  // 已是管理员却访问后台登录页：直接进后台
  if (to.name === 'admin-login' && isAuthed.value && isAdmin.value) {
    return '/admin'
  }
})

export default router
