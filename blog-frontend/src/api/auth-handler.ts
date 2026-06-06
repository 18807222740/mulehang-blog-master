import type { Router } from 'vue-router'
import { toast } from 'vue-sonner'

let routerInstance: Router | null = null

/**
 * 注入 Vue Router 实例（在 main.ts 中调用，避免 request 模块循环依赖）。
 *
 * @param router Vue Router
 */
export function setAuthRouter(router: Router) {
  routerInstance = router
}

/**
 * 处理未授权：清理会话并使用 SPA 路由跳转登录页。
 *
 * @param redirect 登录后回跳路径
 */
export function handleUnauthorized(redirect?: string) {
  sessionStorage.removeItem('auth_logged_in')
  sessionStorage.removeItem('auth_token')

  const target = redirect || window.location.pathname + window.location.search
  if (routerInstance) {
    routerInstance.push({ name: 'Login', query: target !== '/' ? { redirect: target } : undefined })
  } else {
    window.location.href = '/login'
  }
  toast.error('登录已过期，请重新登录')
}

/**
 * 显示业务错误 Toast。
 *
 * @param message 错误信息
 */
export function showApiError(message: string) {
  if (message) {
    toast.error(message)
  }
}

/**
 * 显示业务成功 Toast。
 *
 * @param message 提示信息
 */
export function showApiSuccess(message: string) {
  if (message) {
    toast.success(message)
  }
}
