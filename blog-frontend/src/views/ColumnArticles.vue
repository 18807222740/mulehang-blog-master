<template>
  <div class="space-y-10">
    <!-- 面包屑导航 -->
    <nav class="flex items-center text-sm text-ink-light">
      <router-link to="/" class="hover:text-ink">首页</router-link>
      <span class="mx-2">/</span>
      <router-link to="/columns" class="hover:text-ink">专栏</router-link>
      <span class="mx-2">/</span>
      <span class="text-ink">{{ column?.name || "加载中..." }}</span>
    </nav>

    <!-- 专栏信息 -->
    <section
      v-if="column"
      class="overflow-hidden rounded-2xl border border-ink/10 bg-paper-card shadow-soft"
    >
      <div v-if="column.coverUrl" class="h-56 w-full overflow-hidden">
        <img
          :src="column.coverUrl"
          :alt="column.name"
          loading="lazy"
          decoding="async"
          class="h-full w-full object-cover"
        />
      </div>
      <div class="p-6">
        <h1 class="font-serif text-3xl font-medium text-ink mb-2">
          {{ column.name }}
        </h1>
        <p class="text-ink-light">
          {{ column.description || "暂无描述" }}
        </p>
        <p class="mt-2 text-sm text-ink-light">
          共 {{ pagination.total }} 篇文章
        </p>
      </div>
    </section>

    <!-- 文章列表 -->
    <div v-if="loading" class="flex justify-center items-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
    </div>

    <div v-else-if="articles.length > 0" class="space-y-5">
      <Card
        v-for="article in articles"
        :key="article.id"
        class="group cursor-pointer border-ink/10 bg-paper-card shadow-soft transition-all duration-300 hover:-translate-y-1 hover:shadow-none"
        @click="goToArticle(article.slug)"
      >
        <CardHeader class="pb-3">
          <CardTitle
            class="font-serif text-2xl font-medium text-ink transition-colors group-hover:text-clay"
          >
            {{ article.title }}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p class="text-sm text-ink-light mb-4 line-clamp-2">
            {{ article.summary }}
          </p>
          <div
            class="flex flex-wrap items-center justify-between gap-3 text-sm text-ink-light"
          >
            <div class="flex items-center gap-4">
              <span>{{
                article.author?.nickname || article.author?.username || "匿名"
              }}</span>
              <span>{{
                formatDate(article.publishTime || article.createTime)
              }}</span>
            </div>
            <div class="flex items-center gap-4">
              <span>👁️ {{ article.readCount }}</span>
              <span>❤️ {{ article.likeCount }}</span>
              <span>💬 {{ article.commentCount }}</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <div v-else class="text-center py-12">
      <p class="text-ink-light">该专栏下暂无文章</p>
    </div>

    <!-- 分页 -->
    <div v-if="pagination.totalPages > 1" class="flex justify-center">
      <div class="flex flex-wrap gap-2">
        <button
          v-for="page in pagination.totalPages"
          :key="page"
          @click="goToPage(page)"
          class="rounded-full border border-ink/10 px-4 py-2 text-sm transition-colors"
          :class="
            page === pagination.pageNo
              ? 'bg-ink text-white'
              : 'bg-paper-card text-ink hover:bg-paper-dark'
          "
        >
          {{ page }}
        </button>
      </div>
    </div>

    <!-- 错误提示 -->
    <div
      v-if="error"
      class="rounded-xl border border-destructive/30 bg-destructive/10 p-4"
    >
      <p class="text-destructive">{{ error }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { columnApi } from "@/api/column";
import { articleApi } from "@/api/article";
import type { Column, ArticleListItem } from "@/types/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { parseServerDate } from "@/utils/date";

const route = useRoute();
const router = useRouter();

const column = ref<Column | null>(null);
const articles = ref<ArticleListItem[]>([]);
const loading = ref(false);
const error = ref("");
const pagination = ref({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  totalPages: 0,
});

/**
 * 获取专栏信息
 */
async function fetchColumn() {
  const columnId = Number(route.params.id);
  try {
    column.value = await columnApi.getById(columnId);
  } catch (err: any) {
    error.value = err.message || "获取专栏信息失败";
    console.error("获取专栏信息失败:", err);
  }
}

/**
 * 获取文章列表
 */
async function fetchArticles() {
  loading.value = true;
  error.value = "";
  const columnId = Number(route.params.id);

  try {
    const result = await articleApi.getList({
      columnId,
      pageNo: pagination.value.pageNo,
      pageSize: pagination.value.pageSize,
      status: 1,
    });

    articles.value = result.list;
    pagination.value.total = result.total;
    pagination.value.totalPages = Math.ceil(
      result.total / pagination.value.pageSize,
    );
  } catch (err: any) {
    error.value = err.message || "获取文章列表失败";
    console.error("获取文章列表失败:", err);
  } finally {
    loading.value = false;
  }
}

/**
 * 跳转到文章详情页
 */
function goToArticle(slug: string) {
  router.push({ name: "ArticleDetail", params: { slug } });
}

/**
 * 跳转到指定页码
 */
function goToPage(page: number) {
  pagination.value.pageNo = page;
  fetchArticles();
  window.scrollTo({ top: 0, behavior: "smooth" });
}

/**
 * 格式化日期
 */
function formatDate(dateStr: string): string {
  const date = parseServerDate(dateStr);
  if (!date) return "";
  return date.toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
}

watch(
  () => route.params.id,
  () => {
    if (route.name === "ColumnArticles") {
      pagination.value.pageNo = 1;
      fetchColumn();
      fetchArticles();
    }
  },
);

onMounted(() => {
  fetchColumn();
  fetchArticles();
});
</script>
