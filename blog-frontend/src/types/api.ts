/**
 * API 统一响应结构
 */
export interface ApiResponse<T = any> {
  code: number
  msg?: string
  message?: string
  data: T
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  list: T[]
  total: number
  pageNo: number
  pageSize: number
  totalPages?: number
}

/**
 * 用户信息
 */
export interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
  avatar?: string
  profile?: string
  roles: string[]
  lastLoginTime?: string
}

/**
 * 登录响应
 */
export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
  userInfo: UserInfo
}

/**
 * 文章列表项
 */
export interface ArticleListItem {
  id: number
  title: string
  slug: string
  summary: string
  coverUrl?: string
  status?: number
  author: {
    id: number
    username: string
    nickname: string
    avatar?: string
  } | null
  category: {
    id: number
    name: string
    slug: string
  } | null
  tags: Array<{
    id: number
    name: string
    slug: string
  }>
  readCount: number
  likeCount: number
  commentCount: number
  createTime: string
  updateTime: string
  publishTime?: string
}

/**
 * 搜索结果项（对应后端 ArticleSearchVO）
 */
export interface ArticleSearchItem {
  id: number
  title: string
  summary: string
  slug: string
  coverUrl?: string
  authorId?: number
  authorName?: string
  categoryId?: number
  categoryName?: string
  tags: string[]
  status?: number
  readCount: number
  likeCount: number
  commentCount: number
  publishTime?: string
  createTime: string
  highlightTitle?: string
  highlightSummary?: string
}

/**
 * 文章详情
 */
export interface ArticleDetail {
  id: number
  title: string
  slug: string
  summary: string
  coverUrl?: string
  contentHtml: string
  contentMd?: string
  author: {
    id: number
    username: string
    nickname: string
    avatar?: string
  } | null
  category: {
    id: number
    name: string
    slug: string
  } | null
  column?: Column
  tags: Array<{
    id: number
    name: string
    slug: string
  }>
  wordCount?: number
  readCount: number
  likeCount: number
  commentCount: number
  status: number
  sourceType: number
  allowComment?: number
  isPinned?: number
  publishTime?: string
  createTime: string
  updateTime: string
}

/**
 * 分类
 */
export interface Category {
  id: number
  name: string
  slug: string
  description?: string
  articleCount: number
  creatorId?: number
}

/**
 * 标签
 */
export interface Tag {
  id: number
  name: string
  slug: string
  articleCount: number
  creatorId?: number
}

/**
 * 专栏
 */
export interface Column {
  id: number
  name: string
  slug: string
  coverUrl?: string
  description?: string
  sort?: number
  status?: number
}

/**
 * 评论
 */
export interface CommentVO {
  id: number
  articleId: number
  articleTitle?: string
  articleSlug?: string
  rootId: number | null
  userId: number
  username: string
  nickname: string
  avatar: string | null
  replyToUser: number | null
  content: string
  parentId: number | null
  likeCount: number
  liked?: boolean
  status: number
  location?: string | null
  isTop?: number
  createTime: string
  children?: CommentVO[]
}

/**
 * WebSocket 通知消息
 */
export interface Notification {
  type: string
  title?: string
  content?: string
  articleId?: number
  articleTitle?: string
  commentId?: number
  senderId?: number
  senderName?: string
  receiverId?: number
  url?: string
  timestamp?: string
  read?: boolean
}

/**
 * 用户统计数据
 */
export interface UserStats {
  articleCount: number
  commentCount: number
  likeCount: number
}

/**
 * 网站统计数据
 */
export interface SiteStats {
  todayPV: number
  todayUV: number
  totalPV: number
  totalUV: number
  totalArticles: number
  totalReads: number
  totalLikes: number
  totalComments: number
}

/**
 * 文件上传响应
 */
export interface UploadFile {
  url: string
  platform?: string
  path?: string
  filename?: string
  originalFilename?: string
  ext?: string
  size?: number
}

/**
 * AI 对话消息
 */
export type AiRole = 'system' | 'user' | 'assistant'

export interface AiMessage {
  role: AiRole
  content: string
}

/**
 * AI 对话请求
 */
export interface AiChatRequest {
  messages: AiMessage[]
  temperature?: number
  maxTokens?: number
  provider?: string
  baseUrl?: string
  model?: string
  apiKey?: string
}

/**
 * AI 助手请求
 */
export interface AiAssistantRequest {
  content: string
  maxLength?: number
  count?: number
  provider?: string
  baseUrl?: string
  model?: string
  apiKey?: string
}

/**
 * AI 写作请求
 */
export interface AiWritingRequest {
  topic?: string
  content?: string
  targetLanguage?: string
  targetLength?: number
  provider?: string
  baseUrl?: string
  model?: string
  apiKey?: string
}
