import { request } from './request'
import type { LoginResponse, UserInfo } from '@/types/api'

/**
 * 认证相关 API
 */
export const authApi = {
  /**
   * 用户登录
   */
  login(data: { username: string; password: string }): Promise<LoginResponse> {
    return request.post('/api/v1/auth/login', data)
  },

  /**
   * 用户注册
   */
  register(data: { username: string; password: string; email: string; nickname: string }): Promise<LoginResponse> {
    return request.post('/api/v1/auth/register', data)
  },

  /**
   * 退出登录
   */
  logout(): Promise<void> {
    return request.post('/api/v1/auth/logout')
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser(): Promise<UserInfo> {
    return request.get('/api/v1/users/current')
  },

  /**
   * 获取 GitHub OAuth 授权 URL
   */
  getGitHubAuthorizeUrl(state?: string): Promise<string> {
    return request.get('/api/v1/auth/oauth/github/authorize', { params: { state } })
  },

  /**
   * GitHub OAuth 回调登录
   */
  githubOAuthLogin(code: string, state: string): Promise<LoginResponse> {
    return request.get('/api/v1/auth/oauth/github/callback', { params: { code, state } })
  }
}
