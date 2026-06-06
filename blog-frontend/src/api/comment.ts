import { request } from './request'
import type { CommentVO, PageResult } from '@/types/api'

/**
 * 评论相关 API
 */
export const commentApi = {
  /**
   * 发表评论/回复
   */
  create(data: {
    articleId: number
    content: string
    parentId?: number
    replyToUser?: number
  }): Promise<number> {
    return request.post('/api/v1/comments', data)
  },

  /**
   * 获取文章评论列表
   */
  getListByArticle(
    articleId: number,
    pageNo: number = 1,
    pageSize: number = 20
  ): Promise<PageResult<CommentVO>> {
    return request.get(`/api/v1/articles/${articleId}/comments`, {
      params: { pageNo, pageSize }
    })
  },

  /**
   * 获取当前用户评论列表
   */
  getCurrentUserComments(
    pageNo: number = 1,
    pageSize: number = 10
  ): Promise<PageResult<CommentVO>> {
    return request.get('/api/v1/users/current/comments', {
      params: { pageNo, pageSize }
    })
  },

  /**
   * 点赞评论
   */
  like(commentId: number): Promise<boolean> {
    return request.post(`/api/v1/comments/${commentId}/like`)
  },

  /**
   * 取消点赞评论
   */
  unlike(commentId: number): Promise<boolean> {
    return request.delete(`/api/v1/comments/${commentId}/like`)
  },

  /**
   * 编辑评论
   */
  update(commentId: number, data: { content: string }): Promise<boolean> {
    return request.put(`/api/v1/comments/${commentId}`, data)
  },

  /**
   * 删除评论
   */
  delete(commentId: number): Promise<boolean> {
    return request.delete(`/api/v1/comments/${commentId}`)
  },

  /**
   * 查询用户是否已点赞评论
   */
  getLikeStatus(commentId: number): Promise<boolean> {
    return request.get(`/api/v1/comments/${commentId}/like/status`)
  }
}
