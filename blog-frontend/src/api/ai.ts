import { request } from './request'
import type { AiAssistantRequest, AiChatRequest, AiWritingRequest } from '@/types/api'

type StreamHandlers = {
  onMessage: (chunk: string) => void
  onDone?: () => void
  onError?: (message: string) => void
}

const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

function buildHeaders() {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json'
  }
  return headers
}

function parseSsePayload(payload: string, handlers: StreamHandlers) {
  const blocks = payload.split('\n\n')
  for (const block of blocks) {
    if (!block.trim()) continue
    const lines = block.split('\n').filter(Boolean)
    let event = 'message'
    let data = ''
    for (const line of lines) {
      if (line.startsWith('event:')) {
        event = line.slice(6).trim()
      } else if (line.startsWith('data:')) {
        data += line.slice(5).trim()
      }
    }
    if (!data) continue
    if (event === 'done' || data === '[DONE]') {
      handlers.onDone?.()
      continue
    }
    if (
      event === 'error' ||
      event === 'blocked' ||
      data.startsWith('[ERROR]') ||
      data.startsWith('[BLOCKED]')
    ) {
      handlers.onError?.(data.replace(/^\[(ERROR|BLOCKED)\]\s*/, ''))
      continue
    }
    handlers.onMessage(data)
  }
}

const aiTimeoutConfig = {
  timeout: 60000
}

/**
 * AI 相关 API
 */
export const aiApi = {
  /**
   * AI 对话（同步）
   */
  chat(payload: AiChatRequest): Promise<string> {
    return request.post('/api/v1/ai/chat', payload, aiTimeoutConfig)
  },

  /**
   * AI 对话（流式）
   */
  chatStream(payload: AiChatRequest, handlers: StreamHandlers): AbortController {
    return streamSse('/api/v1/ai/chat/stream', payload, handlers)
  },

  /**
   * 生成摘要
   */
  generateSummary(payload: AiAssistantRequest): Promise<string> {
    return request.post('/api/v1/ai/assistant/summary', payload, aiTimeoutConfig)
  },

  /**
   * 推荐标题
   */
  suggestTitles(payload: AiAssistantRequest): Promise<string[]> {
    return request.post('/api/v1/ai/assistant/titles', payload, aiTimeoutConfig)
  },

  /**
   * 推荐标签
   */
  suggestTags(payload: AiAssistantRequest): Promise<string[]> {
    return request.post('/api/v1/ai/assistant/tags', payload, aiTimeoutConfig)
  },

  /**
   * 生成文章大纲
   */
  generateOutline(payload: AiWritingRequest): Promise<string[]> {
    return request.post('/api/v1/ai/writing/outline', payload, aiTimeoutConfig)
  },

  /**
   * 续写文章（同步）
   */
  continueWriting(payload: AiWritingRequest): Promise<string> {
    return request.post('/api/v1/ai/writing/continue', payload, aiTimeoutConfig)
  },

  /**
   * 续写文章（流式）
   */
  continueWritingStream(payload: AiWritingRequest, handlers: StreamHandlers): AbortController {
    return streamSse('/api/v1/ai/writing/continue/stream', payload, handlers)
  },

  /**
   * 润色文章
   */
  polish(payload: AiWritingRequest): Promise<string> {
    return request.post('/api/v1/ai/writing/polish', payload, aiTimeoutConfig)
  },

  /**
   * 翻译文章
   */
  translate(payload: AiWritingRequest): Promise<string> {
    return request.post('/api/v1/ai/writing/translate', payload, aiTimeoutConfig)
  }
}

function streamSse(path: string, payload: unknown, handlers: StreamHandlers): AbortController {
  const controller = new AbortController()
  fetch(`${baseUrl}${path}`, {
    method: 'POST',
    headers: buildHeaders(),
    body: JSON.stringify(payload),
    signal: controller.signal,
    credentials: 'include'
  })
    .then(async (response) => {
      if (!response.ok) {
        const text = await response.text()
        throw new Error(text || response.statusText)
      }
      if (!response.body) {
        throw new Error('SSE 响应体为空')
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''
      while (true) {
        const { done, value } = await reader.read()
        if (done) break
        buffer += decoder.decode(value, { stream: true })
        const parts = buffer.split('\n\n')
        buffer = parts.pop() || ''
        for (const part of parts) {
          parseSsePayload(part, handlers)
        }
      }
      if (buffer.trim()) {
        parseSsePayload(buffer, handlers)
      }
      handlers.onDone?.()
    })
    .catch((error) => {
      if (controller.signal.aborted) {
        handlers.onDone?.()
        return
      }
      handlers.onError?.(error?.message || '流式请求失败')
    })
  return controller
}
