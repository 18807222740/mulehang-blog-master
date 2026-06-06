import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/types/api'
import { handleUnauthorized, showApiError } from './auth-handler'

/** 未授权业务码（与后端 ResultCodeEnum.UNAUTHORIZED 一致） */
const CODE_UNAUTHORIZED = 40100

/** 权限不足业务码 */
const CODE_FORBIDDEN = 40300

/**
 * Axios 实例配置
 */
const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 请求拦截器：自动附加 Bearer Token
 */
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = sessionStorage.getItem('auth_token')
    if (token && !config.headers.get('Authorization')) {
      config.headers.set('Authorization', `Bearer ${token}`)
    }
    return config
  },
  (error) => {
    showApiError('请求发送失败')
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器：统一处理业务错误与 HTTP 错误
 */
instance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, msg, message } = response.data
    const errorMsg = msg || message || '请求失败'

    if (code !== 0) {
      if (code === CODE_UNAUTHORIZED) {
        const loggedIn = sessionStorage.getItem('auth_logged_in') === '1'
        if (loggedIn) {
          handleUnauthorized(window.location.pathname + window.location.search)
        }
      } else if (code === CODE_FORBIDDEN) {
        showApiError(errorMsg || '权限不足')
      } else {
        showApiError(errorMsg)
      }
      return Promise.reject(new Error(errorMsg))
    }

    return response
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 401:
          if (sessionStorage.getItem('auth_logged_in') === '1') {
            handleUnauthorized(window.location.pathname + window.location.search)
          }
          break
        case 403:
          showApiError('拒绝访问')
          break
        case 404:
          showApiError('请求资源不存在')
          break
        case 500:
          showApiError('服务器错误')
          break
        default:
          showApiError(error.message || '请求失败')
      }
    } else if (error.request) {
      showApiError('网络错误，请检查网络连接')
    } else {
      showApiError(error.message || '请求配置错误')
    }

    return Promise.reject(error)
  }
)

/**
 * 封装请求方法
 */
class Request {
  /**
   * GET 请求
   */
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return instance.get<ApiResponse<T>>(url, config).then(res => res.data.data as T)
  }

  /**
   * POST 请求
   */
  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return instance.post<ApiResponse<T>>(url, data, config).then(res => res.data.data as T)
  }

  /**
   * PUT 请求
   */
  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return instance.put<ApiResponse<T>>(url, data, config).then(res => res.data.data as T)
  }

  /**
   * DELETE 请求
   */
  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return instance.delete<ApiResponse<T>>(url, config).then(res => res.data.data as T)
  }
}

export const request = new Request()
export default instance
