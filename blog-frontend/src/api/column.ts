import { request } from './request'
import type { Column } from '@/types/api'

/**
 * 专栏相关 API
 */
export const columnApi = {
  /**
   * 获取专栏列表
   */
  getAll(): Promise<Column[]> {
    return request.get('/api/v1/columns')
  },

  /**
   * 获取专栏详情
   */
  getById(id: number): Promise<Column> {
    return request.get(`/api/v1/columns/${id}`)
  },

  /**
   * 创建专栏
   */
  create(data: {
    name: string
    slug: string
    coverUrl?: string
    description?: string
    sort?: number
    status?: number
  }): Promise<number> {
    return request.post('/api/v1/columns', data)
  },

  /**
   * 更新专栏
   */
  update(id: number, data: {
    name?: string
    slug?: string
    coverUrl?: string
    description?: string
    sort?: number
    status?: number
  }): Promise<void> {
    return request.put(`/api/v1/columns/${id}`, data)
  },

  /**
   * 删除专栏
   */
  delete(id: number): Promise<void> {
    return request.delete(`/api/v1/columns/${id}`)
  }
}
