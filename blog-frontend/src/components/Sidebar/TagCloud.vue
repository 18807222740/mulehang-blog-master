<template>
  <Card class="border-ink/10 bg-paper-card shadow-soft">
    <CardHeader>
      <CardTitle class="font-serif text-xl font-medium text-ink">
        {{ localeStore.t.tags }}
      </CardTitle>
    </CardHeader>
    <CardContent>
      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center py-4">
        <div
          class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"
        ></div>
      </div>

      <!-- 标签云 -->
      <div v-else-if="tags.length > 0" class="flex flex-wrap gap-2">
        <span
          v-for="tag in tags"
          :key="tag.id"
          class="ink-chip inline-flex items-center rounded-full px-3 py-1.5 text-xs font-medium cursor-pointer text-ink"
          @click="goToTag(tag.id)"
        >
          <span class="text-clay mr-1">#</span>{{ tag.name }}
          <span
            class="ml-1.5 rounded-full border border-transparent bg-paper-bg/80 px-2 text-[10px] text-ink-light"
            >{{ getArticleCount(tag) }}</span
          >
        </span>
      </div>

      <!-- 查看全部 -->
      <div
        v-if="tags.length > 0"
        class="mt-4 text-center border-t border-ink/10 pt-4"
      >
        <router-link
          to="/tags"
          class="text-xs font-medium text-clay hover:underline"
        >
          {{ localeStore.t.viewAllTags }}
        </router-link>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading" class="text-center py-4 text-sm text-ink-light">
        {{ localeStore.t.noTags }}
      </div>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { tagApi } from "@/api/tag";
import type { Tag } from "@/types/api";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { useLocaleStore } from "@/stores/locale";

const router = useRouter();
const localeStore = useLocaleStore();
const tags = ref<Tag[]>([]);
const loading = ref(false);

/**
 * 获取标签列表
 */
async function fetchTags() {
  loading.value = true;
  try {
    const allTags = await tagApi.getAll();
    // 按文章数量排序，只显示前 15 个
    tags.value = allTags
      .sort((a, b) => getArticleCount(b) - getArticleCount(a))
      .slice(0, 15);
  } catch (err) {
    console.error("获取标签列表失败:", err);
  } finally {
    loading.value = false;
  }
}

function getArticleCount(tag: Tag): number {
  return tag.articleCount ?? 0;
}

/**
 * 跳转到标签文章列表页
 */
function goToTag(tagId: number) {
  router.push({ name: "TagArticles", params: { id: tagId } });
}

onMounted(() => {
  fetchTags();
});
</script>
