<template>
  <div class="space-y-10">
    <!-- 顶部标题区域 -->
    <section class="relative overflow-hidden pb-10">
      <div class="max-w-3xl">
        <div class="mb-6 inline-flex items-center gap-2">
          <span
            class="rounded-full bg-clay/10 px-3 py-1 text-xs font-mono font-medium text-clay"
          >
            v1.0
          </span>
        </div>
        <h1
          class="font-serif text-4xl font-medium tracking-tight leading-[1.3] text-ink selection:bg-clay/30 selection:text-ink md:text-5xl"
        >
          {{ localeStore.t.latestArticles }}
        </h1>
        <p class="mt-3 text-base text-ink-light md:text-lg">
          {{ localeStore.t.latestSubtitle }}
        </p>
        <div class="mt-6 flex flex-wrap gap-4">
          <Button
            size="lg"
            class="h-11 rounded-xl bg-ink px-7 text-sm font-medium text-white shadow-sketch transition-all hover:bg-clay hover:shadow-none hover:translate-y-[2px] dark:bg-clay dark:text-paper-bg dark:hover:bg-clay/90"
            @click="scrollToList"
          >
            开始阅读
          </Button>
          <Button
            size="lg"
            @click="loadArticles"
            :disabled="loading"
            class="h-11 gap-2 rounded-xl bg-ink px-7 text-sm font-medium text-white shadow-sketch transition-all hover:bg-clay hover:shadow-none hover:translate-y-[2px] disabled:opacity-70 dark:bg-clay dark:text-paper-bg dark:hover:bg-clay/90"
          >
            <span :class="{ 'animate-spin': loading }">↻</span>
            {{ localeStore.t.refresh }}
          </Button>
        </div>
      </div>
    </section>

    <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 lg:gap-8">
      <!-- 文章列表 (左侧) -->
      <div id="article-list" class="lg:col-span-8 space-y-6">
        <!-- 加载中状态 -->
        <div v-if="loading && articles.length === 0" class="space-y-6">
          <Card
            v-for="i in 3"
            :key="i"
            class="animate-pulse border-ink/10 bg-paper-card shadow-soft"
          >
            <CardHeader>
              <div class="h-6 bg-paper-dark rounded w-3/4"></div>
              <div class="h-4 bg-paper-dark rounded w-1/2 mt-2"></div>
            </CardHeader>
            <CardContent>
              <div class="space-y-2">
                <div class="h-4 bg-paper-dark rounded"></div>
                <div class="h-4 bg-paper-dark rounded w-5/6"></div>
              </div>
            </CardContent>
          </Card>
        </div>

        <!-- 文章列表 -->
        <div v-else-if="articles.length > 0" class="space-y-4">
          <Card
            v-for="article in articles"
            :key="article.id"
            class="group cursor-pointer overflow-hidden border-ink/10 bg-paper-card shadow-soft sketch-hover"
            @click="router.push(`/articles/${article.slug}`)"
          >
            <CardHeader class="pb-3">
              <div class="flex items-start justify-between gap-4">
                <div class="space-y-1">
                  <CardTitle
                    class="font-serif text-2xl font-medium text-ink transition-colors group-hover:text-clay"
                  >
                    {{ article.title }}
                  </CardTitle>
                  <div class="flex items-center gap-2 text-xs text-ink-light">
                    <Badge
                      v-if="article.category"
                      variant="secondary"
                      class="rounded-full bg-paper-dark text-ink font-normal"
                    >
                      {{ article.category.name }}
                    </Badge>
                    <span v-if="article.author" class="flex items-center gap-1">
                      <span class="w-1 h-1 rounded-full bg-ink-lighter"></span>
                      {{ article.author.username }}
                    </span>
                    <span class="flex items-center gap-1">
                      <span class="w-1 h-1 rounded-full bg-ink-lighter"></span>
                      {{ formatDate(article.createTime) }}
                    </span>
                  </div>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <p class="text-sm text-ink-light line-clamp-2 leading-relaxed">
                {{ article.summary }}
              </p>

              <div
                class="flex items-center justify-between mt-4 pt-4 border-t border-ink/10"
              >
                <div class="flex items-center gap-2">
                  <Badge
                    v-for="tag in article.tags?.slice(0, 3)"
                    :key="tag.id"
                    variant="outline"
                    class="text-xs rounded-full border-ink/20 text-ink-light"
                  >
                    #{{ tag.name }}
                  </Badge>
                </div>
                <div class="flex items-center gap-4 text-xs text-ink-light">
                  <span class="flex items-center gap-1"
                    >👁️ {{ article.readCount || 0 }}</span
                  >
                  <span class="flex items-center gap-1"
                    >❤️ {{ article.likeCount || 0 }}</span
                  >
                  <span class="flex items-center gap-1"
                    >💬 {{ article.commentCount || 0 }}</span
                  >
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <!-- 空状态 -->
        <Card v-else class="border-dashed border-ink/20 bg-paper-card">
          <CardContent class="py-12 text-center">
            <div class="text-4xl mb-4">📭</div>
            <p class="text-ink-light text-lg">
              {{ localeStore.t.emptyArticles }}
            </p>
          </CardContent>
        </Card>

        <!-- 分页 -->
        <div
          v-if="totalPages > 1"
          class="flex items-center justify-center gap-2 pt-4"
        >
          <Button
            variant="outline"
            size="sm"
            :disabled="currentPage === 1"
            class="rounded-full border-ink/20 text-ink hover:bg-paper-dark"
            @click="changePage(currentPage - 1)"
          >
            {{ localeStore.t.previous }}
          </Button>
          <span class="text-sm text-ink-light px-2">
            Page {{ currentPage }} of {{ totalPages }}
          </span>
          <Button
            variant="outline"
            size="sm"
            :disabled="currentPage === totalPages"
            class="rounded-full border-ink/20 text-ink hover:bg-paper-dark"
            @click="changePage(currentPage + 1)"
          >
            {{ localeStore.t.next }}
          </Button>
        </div>
      </div>

      <!-- 侧边栏 (右侧) -->
      <div class="lg:col-span-4 space-y-6">
        <!-- 热门文章 -->
        <Card class="border-ink/10 bg-paper-card shadow-soft">
          <CardHeader>
            <CardTitle
              class="font-serif text-xl text-ink flex items-center gap-2"
            >
              🔥 {{ localeStore.t.trending }}
            </CardTitle>
          </CardHeader>
          <CardContent class="grid gap-4">
            <div v-if="hotArticles.length > 0" class="space-y-3">
              <div
                v-for="(article, index) in hotArticles"
                :key="article.id"
                class="sketch-item flex items-start gap-3 group cursor-pointer rounded-2xl px-3 py-2.5"
                @click="router.push(`/articles/${article.slug}`)"
              >
                <span
                  class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-paper-card text-xs font-medium text-ink-light transition-colors group-hover:bg-clay group-hover:text-white"
                >
                  {{ index + 1 }}
                </span>
                <div class="space-y-1">
                  <p
                    class="text-sm font-medium leading-snug text-ink group-hover:text-clay transition-colors line-clamp-2"
                  >
                    {{ article.title }}
                  </p>
                  <div class="flex items-center gap-2 text-xs text-ink-light">
                    <span
                      >{{ article.readCount || 0 }}
                      {{ localeStore.t.reads }}</span
                    >
                    <span
                      >{{ article.likeCount || 0 }}
                      {{ localeStore.t.likes }}</span
                    >
                  </div>
                </div>
              </div>
            </div>
            <p v-else class="text-sm text-ink-light text-center py-4">
              {{ localeStore.t.noTrending }}
            </p>
          </CardContent>
        </Card>

        <!-- 文章分类 -->
        <CategoryWidget />

        <!-- 专栏 -->
        <ColumnWidget />

        <!-- 热门标签 -->
        <TagCloud />

        <!-- 统计信息 -->
        <Card class="border-ink/10 bg-paper-card shadow-soft sketch-hover">
          <CardHeader>
            <CardTitle
              class="font-serif text-xl text-ink flex items-center gap-2"
            >
              📊 {{ localeStore.t.statistics }}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div class="grid grid-cols-2 gap-4">
              <div
                class="stats-tile space-y-1 rounded-xl border border-ink/10 bg-paper-dark/30 p-3"
              >
                <p class="text-xs text-ink-light">
                  {{ localeStore.t.statArticles }}
                </p>
                <p class="text-2xl font-bold text-clay">
                  {{ siteStats.totalArticles || 0 }}
                </p>
              </div>
              <div
                class="stats-tile space-y-1 rounded-xl border border-ink/10 bg-paper-dark/30 p-3"
              >
                <p class="text-xs text-ink-light">
                  {{ localeStore.t.statReads }}
                </p>
                <p class="text-2xl font-bold text-clay">
                  {{ siteStats.totalReads || 0 }}
                </p>
              </div>
              <div
                class="stats-tile space-y-1 rounded-xl border border-ink/10 bg-paper-dark/30 p-3"
              >
                <p class="text-xs text-ink-light">
                  {{ localeStore.t.statLikes }}
                </p>
                <p class="text-2xl font-bold text-clay">
                  {{ siteStats.totalLikes || 0 }}
                </p>
              </div>
              <div
                class="stats-tile space-y-1 rounded-xl border border-ink/10 bg-paper-dark/30 p-3"
              >
                <p class="text-xs text-ink-light">
                  {{ localeStore.t.statTodayVisits }}
                </p>
                <p class="text-2xl font-bold text-clay">
                  {{ siteStats.todayPV || 0 }}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { articleApi } from "@/api/article";
import { recordPageView, getSiteStats } from "@/api/stats";
import { useLocaleStore } from "@/stores/locale";
import type { ArticleListItem, SiteStats } from "@/types/api";
import CategoryWidget from "@/components/Sidebar/CategoryWidget.vue";
import ColumnWidget from "@/components/Sidebar/ColumnWidget.vue";
import TagCloud from "@/components/Sidebar/TagCloud.vue";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { parseServerDate } from "@/utils/date";

const router = useRouter();
const localeStore = useLocaleStore();

// 文章列表状态
const articles = ref<ArticleListItem[]>([]);
const hotArticles = ref<ArticleListItem[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const totalPages = ref(0);

// 网站统计数据
const siteStats = ref<SiteStats>({
  totalArticles: 0,
  totalReads: 0,
  totalLikes: 0,
  totalComments: 0,
  todayPV: 0,
  todayUV: 0,
  totalPV: 0,
  totalUV: 0,
});

/**
 * 滚动到文章列表区域。
 */
function scrollToList() {
  const target = document.getElementById("article-list");
  if (!target) return;
  const navbar = document.getElementById("app-navbar");
  const offset = navbar ? navbar.getBoundingClientRect().height : 0;
  const top = target.getBoundingClientRect().top + window.scrollY - offset - 12;
  window.scrollTo({ top: Math.max(0, top), behavior: "smooth" });
}

/**
 * 加载文章列表
 */
async function loadArticles() {
  loading.value = true;
  try {
    const response = await articleApi.getList({
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      status: 1,
    });
    articles.value = response.list;
    total.value = response.total;
    totalPages.value = Math.ceil(response.total / pageSize.value);
  } catch (error) {
    console.error("加载文章列表失败:", error);
  } finally {
    loading.value = false;
  }
}

/**
 * 加载热门文章
 */
async function loadHotArticles() {
  try {
    hotArticles.value = await articleApi.getHotArticles(5);
  } catch (error) {
    console.error("加载热门文章失败:", error);
  }
}

/**
 * 加载网站统计数据
 */
async function loadSiteStats() {
  try {
    const stats = await getSiteStats();

    // 逐个赋值，确保响应式更新
    siteStats.value.todayPV = stats.todayPV || 0;
    siteStats.value.todayUV = stats.todayUV || 0;
    siteStats.value.totalPV = stats.totalPV || 0;
    siteStats.value.totalUV = stats.totalUV || 0;
    siteStats.value.totalArticles = stats.totalArticles || 0;
    siteStats.value.totalReads = stats.totalReads || 0;
    siteStats.value.totalLikes = stats.totalLikes || 0;
    siteStats.value.totalComments = stats.totalComments || 0;
  } catch (error) {
    console.error("加载统计数据失败:", error);
  }
}

/**
 * 切换页码
 */
function changePage(page: number) {
  currentPage.value = page;
  loadArticles();
  window.scrollTo({ top: 0, behavior: "smooth" });
}

/**
 * 格式化日期
 */
function formatDate(dateString: string): string {
  const date = parseServerDate(dateString);
  if (!date) return "";
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60));
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60));
      return `${minutes} mins ago`;
    }
    return `${hours} hours ago`;
  }
  if (days === 1) return "Yesterday";
  if (days < 7) return `${days} days ago`;
  return date.toLocaleDateString("zh-CN");
}

// 页面加载时获取数据
onMounted(() => {
  // 记录页面访问
  recordPageView().catch((err) => console.error("记录 PV 失败:", err));
  // 加载数据
  loadArticles();
  loadHotArticles();
  loadSiteStats();
});
</script>
