import { request } from './request'
import type { UserInfo, UserStats } from '@/types/api'

/**
 * 用户相关 API
 */
export const userApi = {
  /**
   * 获取当前用户信息
   */
  getCurrent(): Promise<UserInfo> {
    return request.get('/api/v1/users/current')
  },

  /**
   * 更新当前用户资料
   */
  updateProfile(data: {
    nickname: string
    email?: string
    avatar?: string
    profile?: string
  }): Promise<UserInfo> {
    return request.put('/api/v1/users/current', data)
  },

  /**
   * 修改当前用户密码
   */
  changePassword(data: { currentPassword: string; newPassword: string }): Promise<void> {
    return request.put('/api/v1/users/password', data)
  },

  /**
   * 删除当前账号
   */
  deleteCurrent(): Promise<void> {
    return request.delete('/api/v1/users/current')
  },

  /**
   * 获取当前用户统计信息
   */
  getStats(): Promise<UserStats> {
    return request.get('/api/v1/users/current/stats')
  },

  /**
   * 获取用户列表（管理员）
   */
  listAll(): Promise<UserInfo[]> {
    return request.get('/api/v1/admin/users')
  }
}
