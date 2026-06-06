<template>
  <Card class="border-ink/10 bg-paper-card shadow-soft">
    <CardHeader>
      <CardTitle class="font-serif text-xl font-medium text-ink">
        {{ localeStore.t.columns }}
      </CardTitle>
    </CardHeader>
    <CardContent>
      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center py-4">
        <div
          class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"
        ></div>
      </div>

      <!-- 专栏列表 -->
      <ul v-else-if="columns.length > 0" class="space-y-1">
        <li
          v-for="column in columns"
          :key="column.id"
          class="sketch-item flex items-center justify-between rounded-2xl px-3 py-2.5 group cursor-pointer"
          @click="goToColumn(column.id)"
        >
          <span class="text-sm font-medium text-ink group-hover:text-clay transition-colors">
            {{ column.name }}
          </span>
          <span class="text-xs text-ink-light group-hover:text-clay transition-colors">
            →
          </span>
        </li>
      </ul>

      <!-- 查看全部 -->
      <div
        v-if="columns.length > 0"
        class="mt-4 text-center border-t border-ink/10 pt-4"
      >
        <router-link to="/columns" class="text-xs font-medium text-clay hover:underline">
          {{ localeStore.t.viewAllColumns }}
        </router-link>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading" class="text-center py-4 text-sm text-ink-light">
        {{ localeStore.t.noColumns }}
      </div>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { columnApi } from "@/api/column";
import type { Column } from "@/types/api";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { useLocaleStore } from "@/stores/locale";

const router = useRouter();
const localeStore = useLocaleStore();
const columns = ref<Column[]>([]);
const loading = ref(false);

/**
 * 获取专栏列表
 */
async function fetchColumns() {
  loading.value = true;
  try {
    const allColumns = await columnApi.getAll();
    columns.value = allColumns.filter((column) => column.status !== 0).slice(0, 6);
  } catch (err) {
    console.error("获取专栏列表失败:", err);
  } finally {
    loading.value = false;
  }
}

/**
 * 跳转到专栏文章列表页
 */
function goToColumn(columnId: number) {
  router.push({ name: "ColumnArticles", params: { id: columnId } });
}

onMounted(() => {
  fetchColumns();
});
</script>
