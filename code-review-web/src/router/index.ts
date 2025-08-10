import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      name: 'Layout',
      component: () => import('@/layout/index.vue'),
      redirect: '/dashboard',
      meta: { requiresAuth: true },
      children: [
        {
          path: '/dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { title: '仪表板', icon: 'Dashboard' }
        },
        {
          path: '/reviews',
          name: 'Reviews',
          component: () => import('@/views/reviews/index.vue'),
          meta: { title: '评审管理', icon: 'Document' }
        },
        {
          path: '/issues',
          name: 'Issues',
          component: () => import('@/views/issues/index.vue'),
          meta: { title: '问题管理', icon: 'Warning' }
        },
        {
          path: '/statistics',
          name: 'Statistics',
          component: () => import('@/views/statistics/index.vue'),
          meta: { title: '统计分析', icon: 'DataAnalysis' }
        },
        {
          path: '/assignments',
          name: 'Assignments',
          component: () => import('@/views/assignments/index.vue'),
          meta: { title: '任务分配', icon: 'User', roles: ['TEAM_LEADER', 'ARCHITECT'] }
        },
        {
          path: '/summaries',
          name: 'Summaries',
          component: () => import('@/views/summaries/index.vue'),
          meta: { title: '智能汇总', icon: 'Document', roles: ['TEAM_LEADER', 'ARCHITECT'] }
        },
        {
          path: '/notifications',
          name: 'Notifications',
          component: () => import('@/views/notifications/index.vue'),
          meta: { title: '通知中心', icon: 'Bell' }
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth !== false) {
    if (!userStore.token) {
      next('/login')
      return
    }
    
    // 检查角色权限
    if (to.meta.roles && Array.isArray(to.meta.roles)) {
      if (!to.meta.roles.includes(userStore.user?.role)) {
        next('/dashboard')
        return
      }
    }
  }
  
  next()
})

export default router