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
        <p class="text-ink-light">创建您的账户，开始写作之旅</p>
      </div>

      <!-- 注册表单卡片 -->
      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">注册</CardTitle>
          <CardDescription class="text-ink-light">
            填写以下信息创建您的账户
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form @submit.prevent="handleRegister" class="space-y-4">
            <!-- 用户名 -->
            <div class="space-y-2">
              <label for="username" class="text-sm font-medium text-ink">
                用户名
              </label>
              <Input
                id="username"
                v-model="formData.username"
                type="text"
                placeholder="请输入用户名（4-20位）"
                required
                :disabled="loading"
              />
            </div>

            <!-- 昵称 -->
            <div class="space-y-2">
              <label for="nickname" class="text-sm font-medium text-ink">
                昵称
              </label>
              <Input
                id="nickname"
                v-model="formData.nickname"
                type="text"
                placeholder="请输入昵称"
                required
                :disabled="loading"
              />
            </div>

            <!-- 邮箱 -->
            <div class="space-y-2">
              <label for="email" class="text-sm font-medium text-ink">
                邮箱
              </label>
              <Input
                id="email"
                v-model="formData.email"
                type="email"
                placeholder="请输入邮箱地址"
                required
                :disabled="loading"
              />
            </div>

            <!-- 密码 -->
            <div class="space-y-2">
              <label for="password" class="text-sm font-medium text-ink">
                密码
              </label>
              <Input
                id="password"
                v-model="formData.password"
                type="password"
                placeholder="请输入密码（至少6位）"
                required
                :disabled="loading"
              />
            </div>

            <!-- 确认密码 -->
            <div class="space-y-2">
              <label for="confirmPassword" class="text-sm font-medium text-ink">
                确认密码
              </label>
              <Input
                id="confirmPassword"
                v-model="formData.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                required
                :disabled="loading"
              />
            </div>

            <!-- 同意协议 -->
            <div class="flex items-center space-x-2">
              <input
                id="agree"
                type="checkbox"
                v-model="formData.agree"
                class="h-4 w-4 rounded border-ink/20 accent-clay"
              />
              <label for="agree" class="text-sm font-medium text-ink">
                我已阅读并同意
                <a
                  href="https://www.bilibili.com/video/BV1UT42167xb/?spm_id_from=333.337.search-card.all.click&vd_source=879771ccf44eae7c71d3d0f3cc895903"
                  class="text-clay hover:underline"
                  >用户协议</a
                >
                和
                <router-link to="/privacy" class="text-clay hover:underline">
                  隐私政策
                </router-link>
              </label>
            </div>

            <!-- 错误提示 -->
            <div
              v-if="errorMessage"
              class="bg-destructive/10 border border-destructive/20 text-destructive text-sm rounded-md p-3"
            >
              {{ errorMessage }}
            </div>

            <!-- 成功提示 -->
            <div
              v-if="successMessage"
              class="bg-green-500/10 border border-green-500/20 text-green-600 text-sm rounded-md p-3"
            >
              {{ successMessage }}
            </div>

            <!-- 注册按钮 -->
            <Button
              type="submit"
              :disabled="loading || !formData.agree"
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
                注册中...
              </span>
              <span v-else>注册</span>
            </Button>
          </form>
        </CardContent>
      </Card>

      <!-- 登录链接 -->
      <div class="text-center mt-6">
        <p class="text-sm text-ink-light">
          已有账户？
          <router-link
            to="/login"
            class="text-clay font-medium hover:underline"
          >
            立即登录
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
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
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

// 表单数据
const formData = reactive({
  username: "",
  nickname: "",
  email: "",
  password: "",
  confirmPassword: "",
  agree: false,
});

const loading = ref(false);
const errorMessage = ref("");
const successMessage = ref("");

/**
 * 处理注册
 */
async function handleRegister() {
  // 清空消息
  errorMessage.value = "";
  successMessage.value = "";

  // 表单验证
  if (!formData.username.trim()) {
    errorMessage.value = "请输入用户名";
    return;
  }

  if (formData.username.length < 4 || formData.username.length > 20) {
    errorMessage.value = "用户名长度应在 4-20 位之间";
    return;
  }

  if (!formData.nickname.trim()) {
    errorMessage.value = "请输入昵称";
    return;
  }

  if (!formData.email.trim()) {
    errorMessage.value = "请输入邮箱地址";
    return;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(formData.email)) {
    errorMessage.value = "请输入有效的邮箱地址";
    return;
  }

  if (!formData.password.trim()) {
    errorMessage.value = "请输入密码";
    return;
  }

  if (formData.password.length < 6) {
    errorMessage.value = "密码长度不能少于 6 位";
    return;
  }

  if (formData.password !== formData.confirmPassword) {
    errorMessage.value = "两次输入的密码不一致";
    return;
  }

  if (!formData.agree) {
    errorMessage.value = "请阅读并同意用户协议和隐私政策";
    return;
  }

  loading.value = true;

  try {
    // 调用注册 API
    await userStore.register(
      formData.username,
      formData.password,
      formData.email,
      formData.nickname,
    );

    successMessage.value = "注册成功！正在跳转到首页...";

    // 注册成功后自动登录并跳转到首页
    setTimeout(() => {
      router.push("/");
    }, 1500);
  } catch (error: any) {
    errorMessage.value = error.message || "注册失败，请稍后重试";
  } finally {
    loading.value = false;
  }
}
</script>
