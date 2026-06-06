<template>
  <div class="space-y-10">
    <!-- 搜索头部 -->
    <section class="space-y-4">
      <h1 class="font-serif text-4xl font-medium text-ink">搜索结果</h1>
      <div class="flex items-center gap-4">
        <div class="relative flex-1">
          <Input
            v-model="keyword"
            placeholder="搜索文章..."
            class="rounded-xl border-ink/10 bg-paper-card pr-10 text-ink placeholder:text-ink-lighter"
            @keyup.enter="handleSearch"
          />
          <Button
            class="absolute right-0 top-0 h-11 rounded-xl bg-ink px-5 text-sm font-medium text-white hover:bg-clay dark:bg-clay dark:text-paper-bg whitespace-nowrap leading-none"
            @click="handleSearch"
          >
            搜索
          </Button>
        </div>
      </div>

      <!-- 搜索历史 -->
      <div
        v-if="searchHistory.length > 0"
        class="rounded-2xl border border-ink/10 bg-paper-card p-4"
      >
        <div class="mb-3 flex items-center justify-between">
          <span class="text-sm text-ink-light">搜索历史</span>
          <Button
            variant="ghost"
            size="sm"
            class="text-ink-light hover:text-clay"
            @click="clearHistory"
          >
            清除
          </Button>
        </div>
        <div class="flex flex-wrap gap-2">
          <Badge
            v-for="(item, index) in searchHistory"
            :key="index"
            variant="secondary"
            class="cursor-pointer rounded-full bg-paper-dark text-ink"
            @click="handleHistoryClick(item)"
          >
            {{ item }}
          </Badge>
        </div>
      </div>
    </section>

    <!-- 搜索结果信息 -->
    <div v-if="currentKeyword" class="text-ink-light">
      搜索 "<span class="font-medium text-ink">{{ currentKeyword }}</span
      >" 找到 <span class="font-medium text-ink">{{ total }}</span> 篇文章
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="flex justify-center items-center py-20">
      <div
        class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"
      ></div>
    </div>

    <!-- 搜索结果列表 -->
    <div v-else-if="articles.length > 0" class="space-y-5">
      <Card
        v-for="article in articles"
        :key="article.id"
        class="group cursor-pointer border-ink/10 bg-paper-card shadow-soft transition-all duration-300 hover:-translate-y-1 hover:shadow-none"
        @click="router.push(`/articles/${article.slug}`)"
      >
        <CardHeader>
          <CardTitle
            class="font-serif text-2xl font-medium text-ink transition-colors group-hover:text-clay"
          >
            <span
              v-if="article.highlightTitle"
              v-html="article.highlightTitle"
            ></span>
            <span v-else>{{ article.title }}</span>
          </CardTitle>
          <CardDescription class="mt-2 line-clamp-2 text-base text-ink-light">
            <span
              v-if="article.highlightSummary"
              v-html="article.highlightSummary"
            ></span>
            <span v-else>{{ article.summary }}</span>
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div
            class="flex flex-wrap items-center justify-between gap-3 text-sm text-ink-light"
          >
            <div class="flex items-center gap-4">
              <Badge
                variant="secondary"
                class="rounded-full bg-paper-dark text-ink"
              >
                {{ article.categoryName || "未分类" }}
              </Badge>
              <span>{{ article.authorName || "匿名" }}</span>
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
          <div
            v-if="article.tags && article.tags.length > 0"
            class="mt-3 flex flex-wrap gap-2"
          >
            <Badge
              v-for="tag in article.tags"
              :key="tag"
              variant="outline"
              class="rounded-full border-ink/20 text-xs text-ink-light"
            >
              # {{ tag }}
            </Badge>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- 空状态 -->
    <Card
      v-else-if="currentKeyword"
      class="border-ink/10 bg-paper-card shadow-soft"
    >
      <CardContent class="py-12 text-center">
        <p class="text-ink-light text-lg mb-4">没有找到相关文章</p>
        <p class="text-sm text-ink-light mb-6">
          试试其他关键词或浏览
          <router-link to="/" class="text-clay hover:underline">
            所有文章
          </router-link>
        </p>
      </CardContent>
    </Card>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="flex items-center justify-center gap-2">
      <Button
        variant="outline"
        size="sm"
        :disabled="currentPage === 1"
        class="rounded-full border-ink/20 text-ink hover:bg-paper-dark"
        @click="changePage(currentPage - 1)"
      >
        上一页
      </Button>
      <span class="text-sm text-ink-light">
        第 {{ currentPage }} / {{ totalPages }} 页
      </span>
      <Button
        variant="outline"
        size="sm"
        :disabled="currentPage === totalPages"
        class="rounded-full border-ink/20 text-ink hover:bg-paper-dark"
        @click="changePage(currentPage + 1)"
      >
        下一页
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { articleApi } from "@/api/article";
import type { ArticleSearchItem } from "@/types/api";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { parseServerDate } from "@/utils/date";

const route = useRoute();
const router = useRouter();

const keyword = ref("");
const currentKeyword = ref("");
const articles = ref<ArticleSearchItem[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const totalPages = ref(0);

/**
 * 从 localStorage 读取搜索历史
 */
const searchHistory = computed(() => {
  const history = localStorage.getItem("search_history");
  return history ? JSON.parse(history) : [];
});

/**
 * 执行搜索
 */
async function performSearch(searchKeyword: string) {
  if (!searchKeyword.trim()) return;

  loading.value = true;
  currentKeyword.value = searchKeyword;

  try {
    const result = await articleApi.search({
      keyword: searchKeyword,
      pageNo: currentPage.value,
      pageSize: pageSize.value,
    });

    articles.value = result.list;
    total.value = result.total;
    totalPages.value = Math.ceil(result.total / pageSize.value);
  } catch (err) {
    console.error("搜索失败:", err);
  } finally {
    loading.value = false;
  }
}

/**
 * 处理搜索
 */
function handleSearch() {
  if (!keyword.value.trim()) return;

  // 保存搜索历史
  saveSearchHistory(keyword.value.trim());

  // 更新 URL
  router.push({
    name: "Search",
    query: { keyword: keyword.value.trim() },
  });

  // 执行搜索
  currentPage.value = 1;
  performSearch(keyword.value.trim());
}

/**
 * 保存搜索历史
 */
function saveSearchHistory(keyword: string) {
  const history = searchHistory.value;
  const newHistory = [
    keyword,
    ...history.filter((item: string) => item !== keyword),
  ].slice(0, 10);
  localStorage.setItem("search_history", JSON.stringify(newHistory));
}

/**
 * 清除搜索历史
 */
function clearHistory() {
  localStorage.removeItem("search_history");
}

/**
 * 点击搜索历史
 */
function handleHistoryClick(item: string) {
  keyword.value = item;
  handleSearch();
}

/**
 * 切换页码
 */
function changePage(page: number) {
  currentPage.value = page;
  performSearch(currentKeyword.value);
  window.scrollTo({ top: 0, behavior: "smooth" });
}

/**
 * 格式化日期
 */
function formatDate(dateString: string): string {
  const date = parseServerDate(dateString);
  if (!date) return "";
  return date.toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
}

onMounted(() => {
  const queryKeyword = route.query.keyword as string;
  if (queryKeyword) {
    keyword.value = queryKeyword;
    performSearch(queryKeyword);
  }
});
</script>
