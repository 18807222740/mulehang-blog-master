import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * 路由配置
 */
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/articles/:slug',
    name: 'ArticleDetail',
    component: () => import('@/views/ArticleDetail.vue'),
    meta: { title: '文章详情' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/privacy',
    name: 'PrivacyByd',
    component: () => import('@/views/PrankByd.vue'),
    meta: { title: '隐私政策' }
  },
  {
    path: '/oauth/github/callback',
    name: 'GitHubCallback',
    component: () => import('@/views/GitHubCallback.vue'),
    meta: { title: 'GitHub 登录中' }
  },
  {
    path: '/categories',
    name: 'CategoryList',
    component: () => import('@/views/CategoryList.vue'),
    meta: { title: '分类列表' }
  },
  {
    path: '/columns',
    name: 'ColumnList',
    component: () => import('@/views/ColumnList.vue'),
    meta: { title: '专栏列表' }
  },
  {
    path: '/columns/:id',
    name: 'ColumnArticles',
    component: () => import('@/views/ColumnArticles.vue'),
    meta: { title: '专栏文章' }
  },
  {
    path: '/categories/:id',
    name: 'CategoryArticles',
    component: () => import('@/views/CategoryArticles.vue'),
    meta: { title: '分类文章' }
  },
  {
    path: '/tags',
    name: 'TagList',
    component: () => import('@/views/TagList.vue'),
    meta: { title: '标签列表' }
  },
  {
    path: '/tags/:id',
    name: 'TagArticles',
    component: () => import('@/views/TagArticles.vue'),
    meta: { title: '标签文章' }
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/Search.vue'),
    meta: { title: '搜索结果' }
  },
  {
    path: '/ai',
    name: 'AiStudio',
    component: () => import('@/views/AiStudio.vue'),
    meta: { title: 'mulehang-blog' }
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/About.vue'),
    meta: { title: '关于' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '个人主页', requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/Settings.vue'),
    meta: { title: '设置', requiresAuth: true }
  },
  {
    path: '/write',
    name: 'ArticleWrite',
    component: () => import('@/views/ArticleWrite.vue'),
    meta: { title: '写文章', requiresAuth: true }
  },
  {
    path: '/write/:id',
    name: 'ArticleEdit',
    component: () => import('@/views/ArticleWrite.vue'),
    meta: { title: '编辑文章', requiresAuth: true }
  },
  {
    path: '/articles/manage',
    name: 'ArticleManage',
    component: () => import('@/views/ArticleManage.vue'),
    meta: { title: '文章管理', requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'AdminConsole',
    component: () => import('@/views/AdminConsole.vue'),
    meta: { title: '管理控制台', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

/**
 * 全局前置守卫：权限控制
 */
router.beforeEach(async (to) => {
  const userStore = useUserStore()

  // 设置页面标题
  document.title = (to.meta.title as string) || 'MuleHang'

  // 整页刷新时 Pinia 会丢失内存态；若此前已登录，先尝试基于 Cookie 恢复会话。
  if (to.meta.requiresAuth && !userStore.isLoggedIn && sessionStorage.getItem('auth_logged_in') === '1') {
    await userStore.restoreSession()
  }

  // 需要登录的页面
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }

  // 需要管理员权限的页面
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    console.error('需要管理员权限')
    return { name: 'Home' }
  }
})

export default router
