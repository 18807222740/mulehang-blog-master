import { request } from './request'
import type { SiteStats } from '@/types/api'

/**
 * 记录页面访问（PV）
 */
export function recordPageView() {
  return request.post<void>('/api/v1/stats/pv')
}

/**
 * 获取网站统计数据
 */
export function getSiteStats() {
  return request.get<SiteStats>('/api/v1/stats')
}
