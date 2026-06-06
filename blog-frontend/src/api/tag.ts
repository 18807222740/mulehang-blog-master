import { request } from './request'
import type { Tag } from '@/types/api'

/**
 * 标签相关 API
 */
export const tagApi = {
  /**
   * 获取所有标签
   */
  getAll(): Promise<Tag[]> {
    return request.get('/api/v1/tags')
  },

  /**
   * 获取标签详情
   */
  getById(id: number): Promise<Tag> {
    return request.get(`/api/v1/tags/${id}`)
  },

  /**
   * 创建标签（管理员权限）
   */
  create(data: {
    name: string
    slug: string
  }): Promise<number> {
    return request.post('/api/v1/tags', data)
  },

  /**
   * 更新标签（管理员权限）
   */
  update(id: number, data: {
    name: string
    slug: string
  }): Promise<void> {
    return request.put(`/api/v1/tags/${id}`, data)
  },

  /**
   * 删除标签（管理员权限）
   */
  delete(id: number): Promise<void> {
    return request.delete(`/api/v1/tags/${id}`)
  }
}
