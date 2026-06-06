<template>
  <div class="space-y-10">
    <!-- 页面标题 -->
    <header class="space-y-3">
      <h1 class="font-serif text-4xl font-medium text-ink">
        {{ localeStore.t.tags }}
      </h1>
      <p class="text-ink-light">
        {{ localeStore.t.tagsSubtitle }}
      </p>
    </header>

    <!-- 管理面板 -->
    <Card v-if="userStore.isLoggedIn" class="border-ink/10 bg-paper-card shadow-soft">
      <CardContent class="p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-sm font-semibold text-ink">
            {{ editingId ? localeStore.t.editTag : localeStore.t.createTag }}
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
            v-model="form.slug"
            type="text"
            :placeholder="localeStore.t.slug"
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

    <!-- 标签云 -->
    <Card v-else-if="tags.length > 0" class="border-ink/10 bg-paper-card shadow-soft">
      <CardContent class="p-8">
        <div class="flex flex-wrap gap-3">
          <span
            v-for="tag in tags"
            :key="tag.id"
            class="inline-flex items-center gap-2 rounded-full border border-transparent bg-paper-dark/70 px-4 py-2 text-sm cursor-pointer transition-all duration-300 hover:border-clay/40 hover:bg-paper-dark hover:-translate-y-0.5"
            :style="{
              fontSize: getTagSize(getArticleCount(tag)) + 'px',
              color: 'var(--ink)',
            }"
            :title="
              userStore.isAdmin
                ? `id: ${tag.id} | ${localeStore.t.slug}: ${tag.slug} | ${localeStore.t.creatorId}: ${tag.creatorId ?? '-'} `
                : ''
            "
            @click="goToTagArticles(tag.id)"
          >
            <span class="mr-2 text-clay">#</span>{{ tag.name }}
            <span
              class="ml-2 rounded-full bg-paper-card px-2 py-0.5 text-xs text-ink-light"
              >{{ getArticleCount(tag) }}</span
            >
            <button
              v-if="canManage(tag)"
              class="ml-2 text-[11px] text-destructive hover:underline"
              @click.stop="handleDelete(tag)"
            >
              {{ localeStore.t.delete }}
            </button>
            <button
              v-if="canManage(tag)"
              class="ml-1 text-[11px] text-clay hover:underline"
              @click.stop="startEdit(tag)"
            >
              {{ localeStore.t.editTag }}
            </button>
          </span>
        </div>
      </CardContent>
    </Card>

    <!-- 空状态 -->
    <Card v-else class="border-dashed border-ink/20 bg-paper-card">
      <CardContent class="py-12 text-center">
        <div class="text-4xl mb-4">🏷️</div>
        <p class="text-ink-light text-lg">{{ localeStore.t.noTags }}</p>
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
        @click="fetchTags"
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
            {{ localeStore.t.confirmDeleteTag }}
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
            <Button variant="destructive" @click="confirmDeleteTag">
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
import { tagApi } from "@/api/tag";
import type { Tag } from "@/types/api";
import { Card, CardContent } from "@/components/ui/card";
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
const tags = ref<Tag[]>([]);
const loading = ref(false);
const error = ref("");
const editingId = ref<number | null>(null);
const form = ref({
  name: "",
  slug: "",
});
const deleteDialog = ref<{ open: boolean; target: Tag | null }>({
  open: false,
  target: null,
});

/**
 * 获取标签列表
 */
async function fetchTags() {
  loading.value = true;
  error.value = "";
  try {
    tags.value = await tagApi.getAll();
  } catch (err: any) {
    error.value = err.message || "Failed to fetch tags";
    console.error("Failed to fetch tags:", err);
  } finally {
    loading.value = false;
  }
}

/**
 * 检查是否可管理标签（管理员或创建者）。
 */
function canManage(tag: Tag): boolean {
  if (!userStore.isLoggedIn) return false;
  if (userStore.isAdmin) return true;
  return !!tag.creatorId && tag.creatorId === userStore.userInfo?.id;
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
 * 开始编辑标签。
 */
function startEdit(tag: Tag) {
  editingId.value = tag.id;
  form.value = {
    name: tag.name,
    slug: tag.slug,
  };
}

/**
 * 重置表单。
 */
function resetForm() {
  editingId.value = null;
  form.value = { name: "", slug: "" };
}

/**
 * 提交新增/更新。
 */
async function handleSubmit() {
  if (!form.value.name.trim()) return;
  const payload = {
    name: form.value.name.trim(),
    slug: form.value.slug.trim() || buildSlug(form.value.name),
  };
  try {
    if (editingId.value) {
      await tagApi.update(editingId.value, payload);
    } else {
      await tagApi.create(payload);
    }
    resetForm();
    await fetchTags();
  } catch (err) {
    console.error("保存标签失败:", err);
  }
}

/**
 * 删除标签。
 */
async function handleDelete(tag: Tag) {
  openDeleteDialog(tag);
}

/**
 * 打开删除确认对话框。
 */
function openDeleteDialog(tag: Tag) {
  deleteDialog.value = {
    open: true,
    target: tag,
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
 * 确认删除标签。
 */
async function confirmDeleteTag() {
  const target = deleteDialog.value.target;
  if (!target) return;
  try {
    await tagApi.delete(target.id);
    await fetchTags();
  } catch (err) {
    console.error("删除标签失败:", err);
  } finally {
    closeDeleteDialog();
  }
}

/**
 * 根据文章数量计算标签大小
 */
function getTagSize(articleCount: number): number {
  const minSize = 14;
  const maxSize = 24;
  const maxCount = Math.max(...tags.value.map(getArticleCount), 1);
  return minSize + (articleCount / maxCount) * (maxSize - minSize);
}

function getArticleCount(tag: Tag): number {
  return tag.articleCount ?? 0;
}

/**
 * 跳转到标签文章列表页
 */
function goToTagArticles(tagId: number) {
  router.push({ name: "TagArticles", params: { id: tagId } });
}

onMounted(() => {
  fetchTags();
});
</script>
