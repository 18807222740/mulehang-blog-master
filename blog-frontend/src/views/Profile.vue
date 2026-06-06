<template>
  <div class="space-y-10">
    <div class="space-y-8">
      <!-- 用户信息卡片 -->
      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardContent class="pt-6">
          <div
            class="flex flex-col md:flex-row items-center md:items-start gap-6"
          >
            <!-- 头像 -->
            <Avatar class="h-24 w-24">
              <AvatarImage
                :src="userStore.userInfo?.avatar || ''"
                :alt="userStore.userInfo?.nickname || ''"
              />
              <AvatarFallback class="text-2xl">{{
                userStore.userInfo?.nickname?.charAt(0) || "U"
              }}</AvatarFallback>
            </Avatar>

            <!-- 用户详细信息 -->
            <div class="flex-1 text-center md:text-left">
              <h1 class="font-serif text-3xl font-medium text-ink mb-2">
                {{ userStore.userInfo?.nickname }}
              </h1>
              <p class="text-ink-light mb-2">
                @{{ userStore.userInfo?.username }}
              </p>
              <p class="text-ink-light mb-4">
                {{ userStore.userInfo?.email }}
              </p>

              <!-- 个人简介 -->
              <p
                v-if="userStore.userInfo?.profile"
                class="text-ink mb-4"
              >
                {{ userStore.userInfo.profile }}
              </p>

              <!-- 角色标签 -->
              <div class="flex flex-wrap gap-2 justify-center md:justify-start">
                <Badge
                  v-for="role in userStore.userInfo?.roles"
                  :key="role"
                  :variant="role === 'ADMIN' ? 'default' : 'secondary'"
                  class="rounded-full"
                >
                  {{ getRoleLabel(role) }}
                </Badge>
              </div>
            </div>

            <!-- 编辑按钮 -->
            <Button
              @click="router.push('/settings')"
              variant="outline"
              class="rounded-xl border-ink/20 text-ink hover:bg-paper-dark"
            >
              编辑资料
            </Button>
          </div>
        </CardContent>
      </Card>

      <!-- 统计信息 -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card class="border-ink/10 bg-paper-card shadow-soft">
          <CardContent class="pt-6">
            <div class="text-center">
              <p class="text-3xl font-bold text-clay mb-2">
                {{ stats.articleCount }}
              </p>
              <p class="text-sm text-ink-light">发表文章</p>
            </div>
          </CardContent>
        </Card>
        <Card class="border-ink/10 bg-paper-card shadow-soft">
          <CardContent class="pt-6">
            <div class="text-center">
              <p class="text-3xl font-bold text-clay mb-2">
                {{ stats.commentCount }}
              </p>
              <p class="text-sm text-ink-light">发表评论</p>
            </div>
          </CardContent>
        </Card>
        <Card class="border-ink/10 bg-paper-card shadow-soft">
          <CardContent class="pt-6">
            <div class="text-center">
              <p class="text-3xl font-bold text-clay mb-2">
                {{ stats.likeCount }}
              </p>
              <p class="text-sm text-ink-light">获得点赞</p>
            </div>
          </CardContent>
        </Card>
      </div>

      <!-- 最近文章 -->
      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">最近文章</CardTitle>
        </CardHeader>
        <CardContent>
          <div v-if="loading" class="flex justify-center py-8">
            <div
              class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"
            ></div>
          </div>
          <div v-else-if="articles.length > 0" class="space-y-4">
            <div
              v-for="article in articles"
              :key="article.id"
              class="flex items-start gap-4 rounded-xl border border-transparent p-4 transition-colors cursor-pointer hover:border-ink/10 hover:bg-paper-dark/60"
              @click="router.push(`/articles/${article.slug}`)"
            >
              <img
                v-if="article.coverUrl"
                :src="article.coverUrl"
                :alt="article.title"
                loading="lazy"
                decoding="async"
                class="h-20 w-20 rounded-xl object-cover"
              />
              <div class="flex-1 min-w-0">
                <h3
                  class="font-semibold mb-1 text-ink hover:text-clay transition-colors"
                >
                  {{ article.title }}
                </h3>
                <p class="text-sm text-ink-light line-clamp-2 mb-2">
                  {{ article.summary }}
                </p>
                <div
                  class="flex items-center gap-4 text-xs text-ink-light"
                >
                  <span>👁️ {{ article.readCount }}</span>
                  <span>❤️ {{ article.likeCount }}</span>
                  <span>💬 {{ article.commentCount }}</span>
                  <span>{{
                    formatDate(article.publishTime || article.createTime)
                  }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="text-center py-8 text-ink-light">
            暂无文章
          </div>
        </CardContent>
      </Card>

      <!-- 最近评论 -->
      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">最近评论</CardTitle>
        </CardHeader>
        <CardContent>
          <div v-if="commentsLoading" class="flex justify-center py-8">
            <div
              class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"
            ></div>
          </div>
          <div v-else-if="comments.length > 0" class="space-y-4">
            <div
              v-for="comment in comments"
              :key="comment.id"
              class="rounded-xl border border-transparent p-4 transition-colors cursor-pointer hover:border-ink/10 hover:bg-paper-dark/60"
              @click="goToCommentArticle(comment)"
            >
              <p class="text-sm text-ink line-clamp-2">{{ comment.content }}</p>
              <div class="mt-2 flex flex-wrap items-center gap-3 text-xs text-ink-light">
                <span v-if="comment.articleTitle">《{{ comment.articleTitle }}》</span>
                <span v-else-if="comment.articleSlug">{{ comment.articleSlug }}</span>
                <span>{{ formatDate(comment.createTime) }}</span>
              </div>
            </div>
          </div>
          <div v-else class="text-center py-8 text-ink-light">
            暂无评论
          </div>
          <p
            v-if="commentsError"
            class="mt-2 text-center text-xs text-destructive"
          >
            {{ commentsError }}
          </p>
        </CardContent>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { articleApi } from "@/api/article";
import { commentApi } from "@/api/comment";
import { userApi } from "@/api/user";
import type { ArticleListItem, CommentVO, UserStats } from "@/types/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import { parseServerDate } from "@/utils/date";

const router = useRouter();
const userStore = useUserStore();

const articles = ref<ArticleListItem[]>([]);
const loading = ref(false);
const comments = ref<CommentVO[]>([]);
const commentsLoading = ref(false);
const commentsError = ref("");
const stats = ref<UserStats>({
  articleCount: 0,
  commentCount: 0,
  likeCount: 0,
});

/**
 * 获取用户文章列表
 */
async function fetchUserArticles() {
  if (!userStore.userInfo) return;

  loading.value = true;
  try {
    const result = await articleApi.getList({
      authorId: userStore.userInfo.id,
      pageNo: 1,
      pageSize: 10,
      status: 1,
    });
    articles.value = result.list;
  } catch (err) {
    console.error("获取用户文章失败:", err);
  } finally {
    loading.value = false;
  }
}

/**
 * 获取用户统计信息
 */
async function fetchUserStats() {
  if (!userStore.userInfo) return;
  try {
    const result = await userApi.getStats();
    stats.value = {
      articleCount: result.articleCount || 0,
      commentCount: result.commentCount || 0,
      likeCount: result.likeCount || 0,
    };
  } catch (err) {
    console.error("获取用户统计失败:", err);
  }
}

/**
 * 获取用户评论列表
 */
async function fetchUserComments() {
  if (!userStore.userInfo) return;

  commentsLoading.value = true;
  commentsError.value = "";
  try {
    const result = await commentApi.getCurrentUserComments(1, 10);
    comments.value = result.list;
  } catch (err: any) {
    commentsError.value = err.message || "获取评论失败";
  } finally {
    commentsLoading.value = false;
  }
}

/**
 * 获取角色标签
 */
function getRoleLabel(role: string): string {
  const roleMap: Record<string, string> = {
    ADMIN: "管理员",
    USER: "用户",
  };
  return roleMap[role] || role;
}

/**
 * 跳转到评论对应文章
 */
function goToCommentArticle(comment: CommentVO) {
  if (!comment.articleSlug) return;
  router.push(`/articles/${comment.articleSlug}`);
}

/**
 * 格式化日期
 */
function formatDate(dateStr: string): string {
  if (!dateStr) return "";
  const date = parseServerDate(dateStr);
  if (!date) return "";
  return date.toLocaleDateString("zh-CN");
}

onMounted(() => {
  // 如果未登录，跳转到登录页
  if (!userStore.isLoggedIn) {
    router.push("/login");
    return;
  }

  fetchUserStats();
  fetchUserArticles();
  fetchUserComments();
});
</script>
