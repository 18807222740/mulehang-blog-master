<template>
  <div class="space-y-10">
    <!-- 页面标题 -->
    <header class="space-y-3">
      <h1 class="font-serif text-4xl font-medium text-ink">
        {{ localeStore.t.columns }}
      </h1>
      <p class="text-ink-light">
        {{ localeStore.t.columnsSubtitle }}
      </p>
    </header>

    <!-- 管理面板 -->
    <Card
      v-if="userStore.isLoggedIn"
      class="border-ink/10 bg-paper-card shadow-soft"
    >
      <CardContent class="p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-sm font-semibold text-ink">
            {{
              editingId
                ? localeStore.t.editColumn
                : localeStore.t.createColumn
            }}
          </h3>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <input
            v-model="form.name"
            type="text"
            :placeholder="localeStore.t.name"
            class="flex h-10 w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink placeholder:text-ink-lighter focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
          />
          <input
            v-model="form.coverUrl"
            type="url"
            placeholder="封面图片 URL（可选）"
            class="flex h-10 w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink placeholder:text-ink-lighter focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
          />
          <input
            v-model="form.description"
            type="text"
            :placeholder="localeStore.t.description"
            class="flex h-10 w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink placeholder:text-ink-lighter focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
          />
          <div class="grid grid-cols-2 gap-3">
            <input
              v-model.number="form.sort"
              type="number"
              min="0"
              placeholder="排序值"
              class="flex h-10 w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink placeholder:text-ink-lighter focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
            />
            <select
              v-model.number="form.status"
              class="flex h-10 w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
            >
              <option :value="1">公开</option>
              <option :value="0">隐藏</option>
            </select>
          </div>
        </div>
        <div class="mt-4 flex items-center gap-2">
          <Button size="sm" @click="handleSubmit">
            {{ localeStore.t.save }}
          </Button>
          <Button v-if="editingId" size="sm" variant="ghost" @click="resetForm">
            {{ localeStore.t.cancel }}
          </Button>
        </div>
      </CardContent>
    </Card>

    <!-- 加载状态 -->
    <div v-if="loading" class="flex justify-center items-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
    </div>

    <!-- 专栏列表 -->
    <div
      v-else-if="columns.length > 0"
      class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
    >
      <Card
        v-for="column in columns"
        :key="column.id"
        class="group cursor-pointer overflow-hidden border-ink/10 bg-paper-card shadow-soft sketch-hover"
        @click="goToColumnArticles(column.id)"
      >
        <div v-if="column.coverUrl" class="h-40 overflow-hidden">
          <img
            :src="column.coverUrl"
            :alt="column.name"
            loading="lazy"
            decoding="async"
            class="h-full w-full object-cover transition-transform duration-300 group-hover:scale-[1.02]"
          />
        </div>
        <CardHeader>
          <div class="flex items-start justify-between gap-2">
            <CardTitle
              class="font-serif text-2xl font-medium text-ink transition-colors group-hover:text-clay"
            >
              {{ column.name }}
            </CardTitle>
            <div class="flex items-center gap-2 text-xs">
              <button
                v-if="userStore.isLoggedIn"
                class="text-clay hover:underline"
                @click.stop="startEdit(column)"
              >
                {{ localeStore.t.editColumn }}
              </button>
              <button
                v-if="userStore.isLoggedIn"
                class="text-destructive hover:underline"
                @click.stop="handleDelete(column)"
              >
                {{ localeStore.t.delete }}
              </button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <p class="text-sm text-ink-light mb-4 line-clamp-2 h-10">
            {{ column.description || localeStore.t.noDescription }}
          </p>
          <div class="flex items-center justify-between text-sm">
            <Badge
              variant="secondary"
              class="rounded-full bg-paper-dark text-ink"
            >
              {{ column.status === 0 ? "隐藏" : "公开" }}
            </Badge>
            <span class="text-clay group-hover:underline text-xs font-medium">
              {{ localeStore.t.viewArticles }}
            </span>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- 空状态 -->
    <Card v-else class="border-dashed border-ink/20 bg-paper-card">
      <CardContent class="py-12 text-center">
        <div class="text-4xl mb-4">📭</div>
        <p class="text-ink-light text-lg">{{ localeStore.t.noColumns }}</p>
      </CardContent>
    </Card>

    <!-- 错误提示 -->
    <div
      v-if="error"
      class="p-4 bg-destructive/10 border border-destructive/20 rounded-md text-center"
    >
      <p class="text-destructive">{{ error }}</p>
      <Button
        variant="link"
        @click="fetchColumns"
        class="mt-2 text-destructive hover:text-destructive/80"
      >
        Try Again
      </Button>
    </div>

    <AlertDialog
      :open="deleteDialog.open"
      @update:open="handleDeleteDialogOpen"
    >
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>确认删除</AlertDialogTitle>
          <AlertDialogDescription>
            {{ localeStore.t.confirmDeleteColumn }}
            <span v-if="deleteDialog.target" class="font-medium text-foreground">
              「{{ deleteDialog.target.name }}」
            </span>
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel @click="closeDeleteDialog">取消</AlertDialogCancel>
          <AlertDialogAction @click="confirmDeleteColumn">
            确认删除
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { columnApi } from "@/api/column";
import type { Column } from "@/types/api";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { useLocaleStore } from "@/stores/locale";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const localeStore = useLocaleStore();
const userStore = useUserStore();
const columns = ref<Column[]>([]);
const loading = ref(false);
const error = ref("");
const editingId = ref<number | null>(null);
const form = ref({
  name: "",
  description: "",
  coverUrl: "",
  sort: 100,
  status: 1,
});
const deleteDialog = ref<{ open: boolean; target: Column | null }>({
  open: false,
  target: null,
});

/**
 * 获取专栏列表
 */
async function fetchColumns() {
  loading.value = true;
  error.value = "";
  try {
    columns.value = await columnApi.getAll();
  } catch (err: any) {
    error.value = err.message || "Failed to fetch columns";
    console.error("Failed to fetch columns:", err);
  } finally {
    loading.value = false;
  }
}

/**
 * 生成 slug。
 */
function buildSlug(name: string): string {
  return name
    .trim()
    .toLowerCase()
    .replace(/\s+/g, "-")
    .replace(/[^a-z0-9-]/g, "");
}

/**
 * 开始编辑专栏。
 */
function startEdit(column: Column) {
  editingId.value = column.id;
  form.value = {
    name: column.name,
    description: column.description ?? "",
    coverUrl: column.coverUrl ?? "",
    sort: column.sort ?? 100,
    status: column.status ?? 1,
  };
}

/**
 * 重置表单。
 */
function resetForm() {
  editingId.value = null;
  form.value = { name: "", description: "", coverUrl: "", sort: 100, status: 1 };
}

/**
 * 提交新增/更新。
 */
async function handleSubmit() {
  if (!form.value.name.trim()) return;
  const payload = {
    name: form.value.name.trim(),
    slug: buildSlug(form.value.name),
    description: form.value.description.trim() || undefined,
    coverUrl: form.value.coverUrl.trim() || undefined,
    sort: form.value.sort,
    status: form.value.status,
  };
  try {
    if (editingId.value) {
      await columnApi.update(editingId.value, payload);
    } else {
      await columnApi.create(payload);
    }
    resetForm();
    await fetchColumns();
  } catch (err) {
    console.error("保存专栏失败:", err);
  }
}

/**
 * 删除专栏。
 */
async function handleDelete(column: Column) {
  openDeleteDialog(column);
}

/**
 * 打开删除确认对话框。
 */
function openDeleteDialog(column: Column) {
  deleteDialog.value = {
    open: true,
    target: column,
  };
}

/**
 * 处理删除对话框开关。
 */
function handleDeleteDialogOpen(open: boolean) {
  if (!open) {
    closeDeleteDialog();
    return;
  }
  deleteDialog.value.open = true;
}

/**
 * 关闭删除确认对话框。
 */
function closeDeleteDialog() {
  deleteDialog.value.open = false;
  deleteDialog.value.target = null;
}

/**
 * 确认删除专栏。
 */
async function confirmDeleteColumn() {
  const target = deleteDialog.value.target;
  if (!target) return;
  try {
    await columnApi.delete(target.id);
    await fetchColumns();
  } catch (err) {
    console.error("删除专栏失败:", err);
  } finally {
    closeDeleteDialog();
  }
}

/**
 * 跳转到专栏文章列表页
 */
function goToColumnArticles(columnId: number) {
  router.push({ name: "ColumnArticles", params: { id: columnId } });
}

onMounted(() => {
  fetchColumns();
});
</script>
