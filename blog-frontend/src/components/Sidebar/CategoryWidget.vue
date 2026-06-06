<template>
  <Card class="border-ink/10 bg-paper-card shadow-soft">
    <CardHeader>
      <CardTitle class="font-serif text-xl font-medium text-ink">
        {{ localeStore.t.categories }}
      </CardTitle>
    </CardHeader>
    <CardContent>
      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center py-4">
        <div
          class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"
        ></div>
      </div>

      <!-- 分类列表 -->
      <ul v-else-if="categories.length > 0" class="space-y-1">
        <li
          v-for="category in categories"
          :key="category.id"
          class="sketch-item flex items-center justify-between rounded-2xl px-3 py-2.5 group cursor-pointer"
          @click="goToCategory(category.id)"
        >
          <span
            class="text-sm font-medium text-ink group-hover:text-clay transition-colors"
            >{{ category.name }}</span
          >
          <span
            class="rounded-full border border-transparent bg-paper-bg/80 px-2.5 py-0.5 text-xs text-ink-light transition-colors group-hover:border-clay/40 group-hover:text-clay"
            >{{ category.articleCount }}</span
          >
        </li>
      </ul>

      <!-- 查看全部 -->
      <div
        v-if="categories.length > 0"
        class="mt-4 text-center border-t border-ink/10 pt-4"
      >
        <router-link
          to="/categories"
          class="text-xs font-medium text-clay hover:underline"
        >
          {{ localeStore.t.viewAllCategories }}
        </router-link>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading" class="text-center py-4 text-sm text-ink-light">
        {{ localeStore.t.noCategories }}
      </div>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { categoryApi } from "@/api/category";
import type { Category } from "@/types/api";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { useLocaleStore } from "@/stores/locale";

const router = useRouter();
const localeStore = useLocaleStore();
const categories = ref<Category[]>([]);
const loading = ref(false);

/**
 * 获取分类列表
 */
async function fetchCategories() {
  loading.value = true;
  try {
    const allCategories = await categoryApi.getAll();
    // 只显示前 8 个分类
    categories.value = allCategories.slice(0, 8);
  } catch (err) {
    console.error("获取分类列表失败:", err);
  } finally {
    loading.value = false;
  }
}

/**
 * 跳转到分类文章列表页
 */
function goToCategory(categoryId: number) {
  router.push({ name: "CategoryArticles", params: { id: categoryId } });
}

onMounted(() => {
  fetchCategories();
});
</script>
