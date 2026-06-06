import { describe, it, expect, vi, beforeEach } from 'vitest'
import type { Router } from 'vue-router'
import { setAuthRouter, handleUnauthorized, showApiError, showApiSuccess } from '../auth-handler'

vi.mock('vue-sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}))

import { toast } from 'vue-sonner'

describe('auth-handler', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    sessionStorage.clear()
    setAuthRouter(null as unknown as Router)
  })

  it('handleUnauthorized 应清理会话并跳转登录页', () => {
    sessionStorage.setItem('auth_logged_in', '1')
    sessionStorage.setItem('auth_token', 'token')

    const push = vi.fn()
    setAuthRouter({ push } as unknown as Router)

    handleUnauthorized('/articles/1')

    expect(sessionStorage.getItem('auth_logged_in')).toBeNull()
    expect(sessionStorage.getItem('auth_token')).toBeNull()
    expect(push).toHaveBeenCalledWith({
      name: 'Login',
      query: { redirect: '/articles/1' },
    })
    expect(toast.error).toHaveBeenCalledWith('登录已过期，请重新登录')
  })

  it('无 Router 时应降级为 window.location', () => {
    const originalHref = window.location.href
    Object.defineProperty(window, 'location', {
      value: { ...window.location, href: originalHref, pathname: '/articles/1', search: '' },
      writable: true,
    })

    handleUnauthorized()

    expect(window.location.href).toBe('/login')
  })

  it('showApiError 与 showApiSuccess 应调用 toast', () => {
    showApiError('错误信息')
    showApiSuccess('成功信息')
    expect(toast.error).toHaveBeenCalledWith('错误信息')
    expect(toast.success).toHaveBeenCalledWith('成功信息')
  })
})
