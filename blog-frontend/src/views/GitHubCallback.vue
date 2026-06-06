<template>
  <div class="min-h-screen flex items-center justify-center bg-transparent py-16">
    <div class="w-full max-w-md">
      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardContent class="py-12">
          <div class="text-center space-y-4">
            <!-- Loading 动画 -->
            <div v-if="loading" class="flex flex-col items-center gap-4">
              <svg class="animate-spin h-12 w-12 text-clay" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <p class="text-lg font-medium text-ink">正在处理 GitHub 登录...</p>
              <p class="text-sm text-ink-light">请稍候</p>
            </div>

            <!-- 错误提示 -->
            <div v-else-if="error" class="flex flex-col items-center gap-4">
              <svg class="h-12 w-12 text-destructive" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p class="text-lg font-medium text-destructive">登录失败</p>
              <p class="text-sm text-ink-light">{{ error }}</p>
              <Button
                @click="router.push('/login')"
                class="mt-4 rounded-xl bg-ink text-white hover:bg-clay"
              >
                返回登录
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(true)
const error = ref('')

/**
 * 处理 GitHub OAuth 回调
 */
async function handleCallback() {
  try {
    // 获取 URL 参数
    const code = route.query.code as string
    const state = route.query.state as string

    if (!code) {
      throw new Error('未获取到授权码')
    }

    // 验证 state（防止 CSRF 攻击）
    const savedState = sessionStorage.getItem('github_oauth_state')
    if (state !== savedState) {
      throw new Error('状态验证失败，请重新登录')
    }

    // 调用后端接口完成登录
    const response = await authApi.githubOAuthLogin(code, state)

    // 保存用户信息和 token
    userStore.setToken(response.token)
    userStore.setUserInfo(response.userInfo)

    // 清除 state
    sessionStorage.removeItem('github_oauth_state')

    // 登录成功，跳转到首页
    setTimeout(() => {
      router.push('/')
    }, 1000)
  } catch (err: any) {
    error.value = err.message || 'GitHub 登录失败'
    loading.value = false
  }
}

// 页面加载时处理回调
onMounted(() => {
  handleCallback()
})
</script>
