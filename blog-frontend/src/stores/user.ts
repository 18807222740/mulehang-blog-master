import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, LoginResponse } from '@/types/api'
import { authApi } from '@/api/auth'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(sessionStorage.getItem('auth_token') || '')
  const userInfo = ref<UserInfo | null>(null)
  let restorePromise: Promise<void> | null = null

  // 计算属性
  const isLoggedIn = computed(() => !!userInfo.value)
  const isAdmin = computed(() => userInfo.value?.roles?.includes('ADMIN') || false)

  /**
   * 登录
   */
  async function login(username: string, password: string) {
    try {
      const res: LoginResponse = await authApi.login({ username, password })
      setToken(res.token)
      setUserInfo(res.userInfo)
      return res
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }

  /**
   * 注册
   */
  async function register(username: string, password: string, email: string, nickname: string) {
    try {
      const res: LoginResponse = await authApi.register({ username, password, email, nickname })
      setToken(res.token)
      setUserInfo(res.userInfo)
      return res
    } catch (error) {
      console.error('注册失败:', error)
      throw error
    }
  }

  /**
   * 退出登录
   */
  async function logout() {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('退出登录失败:', error)
    } finally {
      clearUserData()
    }
  }

  /**
   * 设置 Token
   */
  function setToken(newToken: string) {
    token.value = newToken
    if (newToken) {
      sessionStorage.setItem('auth_token', newToken)
    } else {
      sessionStorage.removeItem('auth_token')
    }
  }

  /**
   * 设置用户信息
   */
  function setUserInfo(user: UserInfo) {
    userInfo.value = user
    sessionStorage.setItem('auth_logged_in', '1')
  }

  /**
   * 清除用户数据
   */
  function clearUserData() {
    token.value = ''
    userInfo.value = null
    sessionStorage.removeItem('auth_token')
    sessionStorage.removeItem('auth_logged_in')
  }

  /**
   * 初始化时尝试恢复会话（基于 Cookie）。
   */
  async function restoreSession() {
    if (restorePromise) {
      return restorePromise
    }

    restorePromise = (async () => {
      try {
        const currentUser = await authApi.getCurrentUser()
        if (currentUser) {
          setUserInfo(currentUser)
        }
      } catch (error) {
        clearUserData()
      } finally {
        restorePromise = null
      }
    })()

    try {
      await restorePromise
    } catch {
      clearUserData()
    }
  }

  void restoreSession()

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    login,
    register,
    logout,
    setToken,
    setUserInfo,
    clearUserData,
    restoreSession
  }
})
