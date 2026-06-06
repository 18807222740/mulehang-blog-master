<template>
  <div
    class="min-h-screen flex items-center justify-center bg-transparent py-16"
  >
    <div class="w-full max-w-md">
      <!-- Logo 区域 -->
      <div class="text-center mb-8">
        <h1 class="font-serif text-4xl font-medium text-ink mb-2">
          Mulehang Blog
        </h1>
        <p class="text-ink-light">欢迎回来，请登录您的账户</p>
      </div>

      <!-- 登录表单卡片 -->
      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">登录</CardTitle>
          <CardDescription class="text-ink-light">
            输入您的用户名和密码以登录系统
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form @submit.prevent="handleLogin" class="space-y-4">
            <!-- 用户名输入 -->
            <div class="space-y-2">
              <label for="username" class="text-sm font-medium text-ink">
                用户名
              </label>
              <Input
                id="username"
                v-model="username"
                type="text"
                placeholder="请输入用户名"
                required
                :disabled="loading"
              />
            </div>

            <!-- 密码输入 -->
            <div class="space-y-2">
              <div class="flex items-center justify-between">
                <label for="password" class="text-sm font-medium text-ink">
                  密码
                </label>
                <a href="#" class="text-xs text-clay hover:underline">
                  忘记密码？
                </a>
              </div>
              <div class="relative">
                <Input
                  id="password"
                  v-model="password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="请输入密码"
                  required
                  :disabled="loading"
                  class="pr-10"
                />
                <button
                  type="button"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-ink-lighter transition hover:text-ink disabled:cursor-not-allowed disabled:opacity-40"
                  :disabled="loading"
                  :aria-label="showPassword ? '隐藏密码' : '显示密码'"
                  @click="showPassword = !showPassword"
                >
                  <Eye v-if="!showPassword" class="h-4 w-4" />
                  <EyeOff v-else class="h-4 w-4" />
                </button>
              </div>
            </div>

            <!-- 记住我 -->
            <div class="flex items-center space-x-2">
              <input
                id="remember"
                type="checkbox"
                v-model="remember"
                class="h-4 w-4 rounded border-ink/20 accent-clay"
              />
              <label for="remember" class="text-sm font-medium text-ink">
                记住我
              </label>
            </div>

            <!-- 错误提示 -->
            <div
              v-if="errorMessage"
              class="bg-destructive/10 border border-destructive/20 text-destructive text-sm rounded-md p-3"
            >
              {{ errorMessage }}
            </div>

            <!-- 登录按钮 -->
            <Button
              type="submit"
              :disabled="loading"
              class="w-full rounded-xl bg-ink text-white hover:bg-clay dark:bg-clay dark:text-paper-bg"
              size="lg"
            >
              <span v-if="loading" class="flex items-center gap-2">
                <svg
                  class="animate-spin h-4 w-4"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    class="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    stroke-width="4"
                  ></circle>
                  <path
                    class="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  ></path>
                </svg>
                登录中...
              </span>
              <span v-else>登录</span>
            </Button>
          </form>

          <!-- 分割线 -->
          <div class="relative my-6">
            <div class="absolute inset-0 flex items-center">
              <span class="w-full border-t border-ink/10"></span>
            </div>
            <div class="relative flex justify-center text-xs uppercase">
              <span class="bg-paper-card px-2 text-ink-light">或</span>
            </div>
          </div>

          <!-- GitHub 登录 -->
          <Button
            variant="outline"
            type="button"
            :disabled="loading"
            @click="handleGitHubLogin"
            class="w-full rounded-xl border-ink/20 text-ink hover:bg-paper-dark"
          >
            <svg class="mr-2 h-4 w-4" viewBox="0 0 24 24">
              <path
                fill="currentColor"
                d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.17 6.839 9.49.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.603-3.369-1.34-3.369-1.34-.454-1.156-1.11-1.464-1.11-1.464-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.029-2.683-.103-.253-.446-1.27.098-2.647 0 0 .84-.269 2.75 1.025A9.578 9.578 0 0112 6.836c.85.004 1.705.114 2.504.336 1.909-1.294 2.747-1.025 2.747-1.025.546 1.377.203 2.394.1 2.647.64.699 1.028 1.592 1.028 2.683 0 3.842-2.339 4.687-4.566 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.578.688.48C19.138 20.167 22 16.418 22 12c0-5.523-4.477-10-10-10z"
              />
            </svg>
            使用 GitHub 登录
          </Button>
        </CardContent>
      </Card>

      <!-- 注册链接 -->
      <div class="text-center mt-6">
        <p class="text-sm text-ink-light">
          还没有账户？
          <router-link
            to="/register"
            class="text-clay font-medium hover:underline"
          >
            立即注册
          </router-link>
        </p>
      </div>

      <!-- 返回首页 -->
      <div class="text-center mt-4">
        <Button
          variant="ghost"
          size="sm"
          class="text-ink-light hover:text-clay"
          @click="router.push('/')"
        >
          ← 返回首页
        </Button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { Eye, EyeOff } from "lucide-vue-next";
import { useUserStore } from "@/stores/user";
import { authApi } from "@/api/auth";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

const router = useRouter();
const userStore = useUserStore();

// 表单状态
const username = ref("");
const password = ref("");
const showPassword = ref(false);
const remember = ref(false);
const loading = ref(false);
const errorMessage = ref("");

/**
 * 处理登录
 */
async function handleLogin() {
  // 清空错误信息
  errorMessage.value = "";

  // 表单验证
  if (!username.value.trim()) {
    errorMessage.value = "请输入用户名";
    return;
  }

  if (!password.value.trim()) {
    errorMessage.value = "请输入密码";
    return;
  }

  if (password.value.length < 6) {
    errorMessage.value = "密码长度不能少于 6 位";
    return;
  }

  loading.value = true;

  try {
    await userStore.login(username.value, password.value);

    // 登录成功，跳转到首页
    router.push("/");
  } catch (error: any) {
    // 显示错误信息
    errorMessage.value = error.message || "登录失败，请检查用户名和密码";
  } finally {
    loading.value = false;
  }
}

/**
 * 处理 GitHub 登录
 */
async function handleGitHubLogin() {
  loading.value = true;
  errorMessage.value = "";

  try {
    // 生成随机 state 防止 CSRF
    const state = Math.random().toString(36).substring(2);

    // 获取 GitHub 授权 URL
    const authorizeUrl = await authApi.getGitHubAuthorizeUrl(state);

    // 保存 state 到 sessionStorage
    sessionStorage.setItem("github_oauth_state", state);

    // 跳转到 GitHub 授权页面
    window.location.href = authorizeUrl;
  } catch (error: any) {
    errorMessage.value = error.message || "获取 GitHub 授权失败";
    loading.value = false;
  }
}
</script>
