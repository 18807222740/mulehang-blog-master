import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import MockAdapter from 'axios-mock-adapter'
import instance, { request } from '../request'

vi.mock('../auth-handler', () => ({
  handleUnauthorized: vi.fn(),
  showApiError: vi.fn(),
}))

import { handleUnauthorized, showApiError } from '../auth-handler'

describe('request', () => {
  let mock: MockAdapter

  beforeEach(() => {
    vi.clearAllMocks()
    sessionStorage.clear()
    mock = new MockAdapter(instance)
  })

  afterEach(() => {
    mock.restore()
  })

  it('GET 成功时应返回 data 字段', async () => {
    mock.onGet('/api/v1/test').reply(200, { code: 0, data: { id: 1 }, msg: 'ok' })

    const result = await request.get<{ id: number }>('/api/v1/test')
    expect(result).toEqual({ id: 1 })
  })

  it('业务码非 0 时应 reject 并展示错误', async () => {
    mock.onGet('/api/v1/fail').reply(200, { code: 40000, msg: '参数错误' })

    await expect(request.get('/api/v1/fail')).rejects.toThrow('参数错误')
    expect(showApiError).toHaveBeenCalledWith('参数错误')
  })

  it('40100 且已登录时应触发 handleUnauthorized', async () => {
    sessionStorage.setItem('auth_logged_in', '1')
    mock.onGet('/api/v1/auth').reply(200, { code: 40100, msg: '未登录' })

    await expect(request.get('/api/v1/auth')).rejects.toThrow('未登录')
    expect(handleUnauthorized).toHaveBeenCalled()
  })

  it('HTTP 403 时应展示拒绝访问', async () => {
    mock.onGet('/api/v1/forbidden').reply(403)

    await expect(request.get('/api/v1/forbidden')).rejects.toBeTruthy()
    expect(showApiError).toHaveBeenCalledWith('拒绝访问')
  })
})
