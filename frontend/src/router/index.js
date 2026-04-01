import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard',      name: 'Dashboard',      component: () => import('@/views/Dashboard.vue'),      meta: { title: '首页概览' } },
      { path: 'equipment',      name: 'Equipment',      component: () => import('@/views/Equipment.vue'),      meta: { title: '装备管理' } },
      { path: 'reliability',    name: 'Reliability',    component: () => import('@/views/Reliability.vue'),    meta: { title: '可靠性分析' } },
      { path: 'maintainability',name: 'Maintainability',component: () => import('@/views/Maintainability.vue'),meta: { title: '维修性分析' } },
      { path: 'testability',    name: 'Testability',    component: () => import('@/views/Testability.vue'),    meta: { title: '测试性分析' } },
      { path: 'supportability', name: 'Supportability', component: () => import('@/views/Supportability.vue'), meta: { title: '保障性分析' } },
      { path: 'safety',         name: 'Safety',         component: () => import('@/views/Safety.vue'),         meta: { title: '安全性分析' } },
      { path: 'environment',    name: 'Environment',    component: () => import('@/views/Environment.vue'),    meta: { title: '环境适应性' } },
      { path: 'analysis',       name: 'Analysis',       component: () => import('@/views/Analysis.vue'),       meta: { title: '综合分析' } },
      { path: 'report',         name: 'Report',         component: () => import('@/views/Report.vue'),         meta: { title: '报告中心' } },
      { path: 'user',           name: 'User',           component: () => import('@/views/User.vue'),           meta: { title: '个人中心' } },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
