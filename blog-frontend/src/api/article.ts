import { request } from './request'
import type { ArticleListItem, ArticleDetail, PageResult, ArticleSearchItem } from '@/types/api'

/**
 * 文章相关 API
 */
export const articleApi = {
  /**
   * 获取文章列表（分页）
   */
  getList(params: {
    pageNo?: number
    pageSize?: number
    status?: number
    categoryId?: number
    columnId?: number
    tagId?: number
    authorId?: number
    keyword?: string
  }): Promise<PageResult<ArticleListItem>> {
    return request.get('/api/v1/articles', { params })
  },

  /**
   * 通过 slug 获取文章详情（前台展示）
   */
  getBySlug(slug: string): Promise<ArticleDetail> {
    return request.get(`/api/v1/articles/slug/${slug}`)
  },

  /**
   * 通过 ID 获取文章详情
   */
  getById(id: number): Promise<ArticleDetail> {
    return request.get(`/api/v1/articles/${id}`)
  },

  /**
   * 获取热门文章
   */
  getHotArticles(topN: number = 10): Promise<ArticleListItem[]> {
    return request.get('/api/v1/articles/hot', { params: { topN } })
  },

  /**
   * 搜索文章（ES 全文搜索）
   */
  search(params: {
    keyword: string
    pageNo?: number
    pageSize?: number
    categoryId?: number
    authorId?: number
    tag?: string
  }): Promise<PageResult<ArticleSearchItem>> {
    return request.get('/api/v1/articles/search', { params })
  },

  /**
   * 点赞文章
   */
  like(articleId: number): Promise<boolean> {
    return request.post(`/api/v1/articles/${articleId}/like`)
  },

  /**
   * 取消点赞文章
   */
  unlike(articleId: number): Promise<boolean> {
    return request.delete(`/api/v1/articles/${articleId}/like`)
  },

  /**
   * 查询用户是否已点赞文章
   */
  getLikeStatus(articleId: number): Promise<boolean> {
    return request.get(`/api/v1/articles/${articleId}/like/status`)
  },

  /**
   * 创建文章
   */
  create(data: {
    title: string
    slug?: string
    summary?: string
    coverUrl?: string
    status?: number
    sourceType?: number
    allowComment?: number
    isPinned?: number
    categoryId?: number
    columnId?: number
    tagIds?: number[]
    contentMd: string
  }): Promise<number> {
    return request.post('/api/v1/articles', data)
  },

  /**
   * 更新文章
   */
  update(id: number, data: {
    title?: string
    slug?: string
    summary?: string
    coverUrl?: string
    status?: number
    sourceType?: number
    allowComment?: number
    isPinned?: number
    categoryId?: number
    columnId?: number
    tagIds?: number[]
    contentMd?: string
  }): Promise<void> {
    return request.put(`/api/v1/articles/${id}`, data)
  },

  /**
   * 发布文章
   */
  publish(id: number): Promise<void> {
    return request.post(`/api/v1/articles/${id}/publish`)
  },

  /**
   * 删除文章
   */
  delete(id: number): Promise<void> {
    return request.delete(`/api/v1/articles/${id}`)
  },

  /**
   * 重建搜索索引
   */
  rebuildSearchIndex(): Promise<number> {
    return request.post('/api/v1/articles/rebuild-search-index')
  },

  /**
   * 发送测试邮件（延迟队列）
   */
  sendTestEmail(params: {
    to: string
    subject?: string
    content?: string
    delaySeconds?: number
  }): Promise<void> {
    return request.post('/api/v1/articles/email/test', null, { params })
  }
}
