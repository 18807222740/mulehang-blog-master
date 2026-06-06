import { request } from './request'
import type { Category } from '@/types/api'

/**
 * 分类相关 API
 */
export const categoryApi = {
  /**
   * 获取所有分类
   */
  getAll(): Promise<Category[]> {
    return request.get('/api/v1/categories')
  },

  /**
   * 获取分类详情
   */
  getById(id: number): Promise<Category> {
    return request.get(`/api/v1/categories/${id}`)
  },

  /**
   * 创建分类（管理员权限）
   */
  create(data: {
    name: string
    slug: string
    description?: string
  }): Promise<number> {
    return request.post('/api/v1/categories', data)
  },

  /**
   * 更新分类（管理员权限）
   */
  update(id: number, data: {
    name: string
    slug: string
    description?: string
  }): Promise<void> {
    return request.put(`/api/v1/categories/${id}`, data)
  },

  /**
   * 删除分类（管理员权限）
   */
  delete(id: number): Promise<void> {
    return request.delete(`/api/v1/categories/${id}`)
  }
}
