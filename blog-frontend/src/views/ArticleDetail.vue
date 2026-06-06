<template>
  <div class="space-y-10">
    <!-- 加载状态 -->
    <div v-if="loading" class="flex justify-center items-center py-20">
      <div
        class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"
      ></div>
    </div>

    <!-- 文章内容 -->
    <div v-else-if="article" class="mx-auto w-full max-w-4xl space-y-10">
      <!-- 面包屑导航 -->
      <nav
        v-if="article.category"
        class="flex items-center text-sm text-ink-light"
      >
        <router-link to="/" class="hover:text-ink">首页</router-link>
        <span class="mx-2">/</span>
        <router-link
          :to="`/categories/${article.category.id}`"
          class="hover:text-ink"
        >
          {{ article.category.name }}
        </router-link>
        <span class="mx-2">/</span>
        <span class="text-ink">{{ article.title }}</span>
      </nav>
      <nav v-else class="flex items-center text-sm text-ink-light">
        <router-link to="/" class="hover:text-ink">首页</router-link>
        <span class="mx-2">/</span>
        <span class="text-ink">{{ article.title }}</span>
      </nav>

      <!-- 文章头部 -->
      <header class="space-y-4">
        <div class="flex flex-wrap items-center gap-3 text-sm text-ink-light">
          <Badge
            v-if="article.column"
            variant="secondary"
            class="rounded-full bg-paper-dark text-ink"
          >
            {{ article.column.name }}
          </Badge>
          <Badge
            v-if="article.category"
            variant="secondary"
            class="rounded-full bg-paper-dark text-ink"
          >
            {{ article.category.name }}
          </Badge>
          <div v-if="article.author" class="flex items-center gap-2">
            <Avatar class="h-8 w-8">
              <AvatarImage
                :src="article.author.avatar || ''"
                :alt="article.author.username || ''"
              />
              <AvatarFallback>{{
                article.author.username.charAt(0)
              }}</AvatarFallback>
            </Avatar>
            <span>{{ article.author.username }}</span>
          </div>
          <span>
            发布于
            {{ formatDate(article.publishTime || article.createTime) }}
          </span>
          <span v-if="article.updateTime !== article.createTime">
            更新于 {{ formatDate(article.updateTime) }}
          </span>
        </div>
        <h1 class="font-serif text-4xl font-medium text-ink md:text-5xl">
          {{ article.title }}
        </h1>
        <div class="flex flex-wrap items-center gap-4 text-sm text-ink-light">
          <span class="flex items-center gap-1">
            👁️ {{ article.readCount || 0 }} 阅读
          </span>
          <span class="flex items-center gap-1">
            ❤️ {{ article.likeCount || 0 }} 点赞
          </span>
          <span class="flex items-center gap-1">
            💬 {{ article.commentCount || 0 }} 评论
          </span>
        </div>
      </header>

      <!-- 封面图 -->
      <div
        v-if="article.coverUrl"
        class="overflow-hidden rounded-2xl border border-ink/10 shadow-soft"
      >
        <img
          :src="article.coverUrl"
          :alt="article.title"
          loading="lazy"
          decoding="async"
          class="w-full object-cover"
        />
      </div>

      <!-- 文章正文 -->
      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardContent class="pt-6">
          <MarkdownRenderer
            v-if="article.contentMd"
            :content="article.contentMd"
          />
          <div
            v-else
            class="markdown-body prose prose-lg max-w-none"
            v-html="article.contentHtml"
          ></div>
        </CardContent>
      </Card>

      <!-- 文章元信息 -->
      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardContent class="pt-6">
          <div class="flex flex-wrap gap-4 justify-center">
          <div v-if="article.category">
            <span class="mr-2 text-sm text-ink-light">分类：</span>
            <Badge
              variant="secondary"
              class="cursor-pointer rounded-full bg-paper-dark text-ink"
              @click="router.push(`/categories/${article.category.id}`)"
            >
              {{ article.category.name }}
            </Badge>
          </div>
          <div v-if="article.column">
            <span class="mr-2 text-sm text-ink-light">专栏：</span>
            <Badge
              variant="secondary"
              class="cursor-pointer rounded-full bg-paper-dark text-ink"
              @click="router.push(`/columns/${article.column.id}`)"
            >
              {{ article.column.name }}
            </Badge>
          </div>
          <div v-else>
            <span class="mr-2 text-sm text-ink-light">分类：</span>
            <Badge variant="secondary" class="rounded-full bg-paper-dark text-ink">
              未分类
            </Badge>
            </div>
            <div v-if="article.tags && article.tags.length > 0">
              <span class="mr-2 text-sm text-ink-light">标签：</span>
              <Badge
                v-for="tag in article.tags"
                :key="tag.id"
                variant="outline"
                class="mr-2 cursor-pointer rounded-full border-ink/20 text-ink"
                @click="router.push(`/tags/${tag.id}`)"
              >
                # {{ tag.name }}
              </Badge>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- 操作按钮 -->
      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardContent class="pt-6">
          <div class="flex flex-wrap items-center justify-center gap-4">
            <Button
              :variant="isLiked ? 'default' : 'outline'"
              size="lg"
              class="rounded-xl border-ink/20 px-6"
              @click="handleLike"
              :disabled="!userStore.isLoggedIn || liking"
            >
              <span class="mr-2">❤️</span>
              {{ liking ? "点赞中..." : isLiked ? "已点赞" : "点赞" }} ({{
                article.likeCount
              }})
            </Button>
            <Button
              variant="outline"
              size="lg"
              class="rounded-xl border-ink/20 px-6"
              @click="handleShare"
            >
              <span class="mr-2">🔗</span>
              分享
            </Button>
          </div>
          <p v-if="!userStore.isLoggedIn" class="mt-4 text-center text-sm text-ink-light">
            <router-link to="/login" class="text-clay hover:underline">
              登录
            </router-link>
            后可点赞文章
          </p>
        </CardContent>
      </Card>

      <!-- 相关文章推荐（可选） -->
      <Card v-if="relatedArticles.length > 0" class="border-ink/10 bg-paper-card shadow-soft">
        <CardHeader>
          <CardTitle class="font-serif text-2xl font-medium text-ink">
            相关推荐
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div class="space-y-4">
            <div
              v-for="relatedArticle in relatedArticles"
              :key="relatedArticle.id"
              class="flex items-start gap-3 cursor-pointer rounded-xl border border-transparent p-3 transition-colors hover:border-ink/10 hover:bg-paper-dark/60"
              @click="router.push(`/articles/${relatedArticle.slug}`)"
            >
              <div class="flex-1">
                <h4 class="font-medium text-ink hover:text-clay transition-colors">
                  {{ relatedArticle.title }}
                </h4>
                <p class="text-sm text-ink-light mt-1 line-clamp-2">
                  {{ relatedArticle.summary }}
                </p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- 评论区域 -->
      <CommentList v-if="article" :article-id="article.id" />
    </div>

    <!-- 错误提示 -->
    <div v-else class="mx-auto w-full max-w-4xl">
      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardContent class="py-12 text-center">
          <p class="text-ink-light text-lg mb-4">
            {{ error || "文章不存在" }}
          </p>
          <Button @click="router.push('/')">返回首页</Button>
        </CardContent>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "vue-sonner";
import { articleApi } from "@/api/article";
import { useUserStore } from "@/stores/user";
import type { ArticleDetail, ArticleListItem } from "@/types/api";
import MarkdownRenderer from "@/components/Markdown/MarkdownRenderer.vue";
import CommentList from "@/components/Comment/CommentList.vue";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import { parseServerDate } from "@/utils/date";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const article = ref<ArticleDetail | null>(null);
const relatedArticles = ref<ArticleListItem[]>([]);
const loading = ref(false);
const error = ref("");
const isLiked = ref(false);
const liking = ref(false); // 点赞操作中

/**
 * 获取文章详情
 */
async function fetchArticle() {
  loading.value = true;
  error.value = "";
  const slug = route.params.slug as string;

  try {
    article.value = await articleApi.getBySlug(slug);
    // 获取相关文章（同分类）
    if (article.value.category?.id) {
      const result = await articleApi.getList({
        categoryId: article.value.category.id,
        pageSize: 5,
        status: 1,
      });
      relatedArticles.value = result.list
        .filter((a) => a.id !== article.value!.id)
        .slice(0, 3);
    }
    // 查询用户是否已点赞
    if (userStore.isLoggedIn && userStore.userInfo) {
      isLiked.value = await articleApi.getLikeStatus(article.value.id);
    }
  } catch (err: any) {
    error.value = err.message || "获取文章失败";
    console.error("获取文章详情失败:", err);
  } finally {
    loading.value = false;
  }
}

/**
 * 处理点赞/取消点赞
 */
async function handleLike() {
  if (!userStore.isLoggedIn) {
    router.push("/login");
    return;
  }

  if (!article.value || !userStore.userInfo || liking.value) return;

  liking.value = true;
  try {
    if (isLiked.value) {
      // 已点赞，执行取消点赞
      const success = await articleApi.unlike(article.value.id);
      if (success) {
        article.value.likeCount--;
        isLiked.value = false;
        toast.success("已取消点赞");
      } else {
        toast.error("取消点赞失败");
      }
    } else {
      // 未点赞，执行点赞
      const success = await articleApi.like(article.value.id);
      if (success) {
        article.value.likeCount++;
        isLiked.value = true;
        toast.success("点赞成功");
      } else {
        toast.info("您已经点赞过了");
        isLiked.value = true;
      }
    }
  } catch (err: any) {
    console.error("点赞操作失败:", err);
    toast.error("操作失败", {
      description: err.message || "请稍后重试",
    });
  } finally {
    liking.value = false;
  }
}

/**
 * 分享文章
 */
function handleShare() {
  const url = window.location.href;
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard
      .writeText(url)
      .then(() => {
        toast.success("链接已复制到剪贴板");
      })
      .catch((err: any) => {
        toast.error("复制失败", {
          description: err?.message || "请稍后重试",
        });
      });
    return;
  }
  try {
    const textarea = document.createElement("textarea");
    textarea.value = url;
    textarea.style.position = "fixed";
    textarea.style.left = "-9999px";
    document.body.appendChild(textarea);
    textarea.focus();
    textarea.select();
    const success = document.execCommand("copy");
    document.body.removeChild(textarea);
    if (success) {
      toast.success("链接已复制到剪贴板");
    } else {
      toast.error("复制失败", {
        description: "请手动复制地址栏链接",
      });
    }
  } catch (err: any) {
    toast.error("复制失败", {
      description: err?.message || "请手动复制地址栏链接",
    });
  }
}

/**
 * 格式化日期
 */
function formatDate(dateString: string): string {
  const date = parseServerDate(dateString);
  if (!date) return "";
  return date.toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

onMounted(() => {
  fetchArticle();
});

// 监听路由参数变化，解决同组件复用时点击相关推荐不刷新的问题
watch(
  () => route.params.slug,
  (newSlug, oldSlug) => {
    if (newSlug && newSlug !== oldSlug) {
      window.scrollTo(0, 0);
      fetchArticle();
    }
  },
);
</script>
