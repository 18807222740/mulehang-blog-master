<template>
  <div class="space-y-10">
    <!-- 页面标题 -->
    <header class="space-y-3">
      <h1 class="font-serif text-4xl font-medium text-ink">
        {{ localeStore.t.categories }}
      </h1>
      <p class="text-ink-light">
        {{ localeStore.t.categoriesSubtitle }}
      </p>
    </header>

    <!-- 管理面板 -->
    <Card v-if="userStore.isLoggedIn" class="border-ink/10 bg-paper-card shadow-soft">
      <CardContent class="p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-sm font-semibold text-ink">
            {{
              editingId
                ? localeStore.t.editCategory
                : localeStore.t.createCategory
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
            v-model="form.description"
            type="text"
            :placeholder="localeStore.t.description"
            class="flex h-10 w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink placeholder:text-ink-lighter focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40"
          />
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
      <div
        class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"
      ></div>
    </div>

    <!-- 分类列表 -->
    <div
      v-else-if="categories.length > 0"
      class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
    >
      <Card
        v-for="category in categories"
        :key="category.id"
        class="group cursor-pointer border-ink/10 bg-paper-card shadow-soft transition-all duration-300 hover:-translate-y-1 hover:shadow-none"
        :title="
          userStore.isAdmin
            ? `id: ${category.id} | ${localeStore.t.slug}: ${category.slug} | ${localeStore.t.creatorId}: ${category.creatorId ?? '-'}`
            : ''
        "
        @click="goToCategoryArticles(category.id)"
      >
        <CardHeader>
          <div class="flex items-start justify-between gap-2">
            <CardTitle
              class="font-serif text-2xl font-medium text-ink transition-colors group-hover:text-clay"
            >
              {{ category.name }}
            </CardTitle>
            <div class="flex items-center gap-2 text-xs">
              <button
                v-if="canManage(category)"
                class="text-clay hover:underline"
                @click.stop="startEdit(category)"
              >
                {{ localeStore.t.editCategory }}
              </button>
              <button
                v-if="canManage(category)"
                class="text-destructive hover:underline"
                @click.stop="handleDelete(category)"
              >
                {{ localeStore.t.delete }}
              </button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <p class="text-sm text-ink-light mb-4 line-clamp-2 h-10">
            {{ category.description || localeStore.t.noDescription }}
          </p>
          <div class="flex items-center justify-between text-sm">
            <Badge variant="secondary" class="rounded-full bg-paper-dark text-ink">
              {{ category.articleCount }} {{ localeStore.t.statArticles }}
            </Badge>
            <span
              class="text-clay group-hover:underline text-xs font-medium"
            >
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
        <p class="text-ink-light text-lg">{{ localeStore.t.noCategories }}</p>
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
        @click="fetchCategories"
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
            {{ localeStore.t.confirmDeleteCategory }}
            <span
              v-if="deleteDialog.target"
              class="font-medium text-foreground"
            >
              「{{ deleteDialog.target.name }}」
            </span>
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel @click="closeDeleteDialog">取消</AlertDialogCancel>
          <AlertDialogAction as-child>
            <Button variant="destructive" @click="confirmDeleteCategory">
              确认删除
            </Button>
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { categoryApi } from "@/api/category";
import type { Category } from "@/types/api";
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
const categories = ref<Category[]>([]);
const loading = ref(false);
const error = ref("");
const editingId = ref<number | null>(null);
const form = ref({
  name: "",
  description: "",
});
const deleteDialog = ref<{ open: boolean; target: Category | null }>({
  open: false,
  target: null,
});

/**
 * 获取分类列表
 */
async function fetchCategories() {
  loading.value = true;
  error.value = "";
  try {
    categories.value = await categoryApi.getAll();
  } catch (err: any) {
    error.value = err.message || "Failed to fetch categories";
    console.error("Failed to fetch categories:", err);
  } finally {
    loading.value = false;
  }
}

/**
 * 检查是否可管理分类（管理员或创建者）。
 */
function canManage(category: Category): boolean {
  if (!userStore.isLoggedIn) return false;
  if (userStore.isAdmin) return true;
  return !!category.creatorId && category.creatorId === userStore.userInfo?.id;
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
 * 开始编辑分类。
 */
function startEdit(category: Category) {
  editingId.value = category.id;
  form.value = {
    name: category.name,
    description: category.description ?? "",
  };
}

/**
 * 重置表单。
 */
function resetForm() {
  editingId.value = null;
  form.value = { name: "", description: "" };
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
  };
  try {
    if (editingId.value) {
      await categoryApi.update(editingId.value, payload);
    } else {
      await categoryApi.create(payload);
    }
    resetForm();
    await fetchCategories();
  } catch (err) {
    console.error("保存分类失败:", err);
  }
}

/**
 * 删除分类。
 */
async function handleDelete(category: Category) {
  openDeleteDialog(category);
}

/**
 * 打开删除确认对话框。
 */
function openDeleteDialog(category: Category) {
  deleteDialog.value = {
    open: true,
    target: category,
  };
}

/**
 * 处理删除对话框开关。
 */
function handleDeleteDialogOpen(open: boolean) {
  deleteDialog.value.open = open;
}

/**
 * 关闭删除确认对话框。
 */
function closeDeleteDialog() {
  deleteDialog.value.open = false;
  deleteDialog.value.target = null;
}

/**
 * 确认删除分类。
 */
async function confirmDeleteCategory() {
  const target = deleteDialog.value.target;
  if (!target) return;
  try {
    await categoryApi.delete(target.id);
    await fetchCategories();
  } catch (err) {
    console.error("删除分类失败:", err);
  } finally {
    closeDeleteDialog();
  }
}

/**
 * 跳转到分类文章列表页
 */
function goToCategoryArticles(categoryId: number) {
  router.push({ name: "CategoryArticles", params: { id: categoryId } });
}

onMounted(() => {
  fetchCategories();
});
</script>
