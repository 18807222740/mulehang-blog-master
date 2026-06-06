<template>
  <div class="comment-list">
    <Card class="border-ink/10 bg-paper-card shadow-soft">
      <CardHeader>
        <CardTitle class="flex items-center justify-between">
          <span class="font-serif text-2xl text-ink">评论 ({{ total }})</span>
          <Button
            variant="ghost"
            size="sm"
            class="text-ink-light hover:text-clay"
            @click="fetchComments"
          >
            刷新
          </Button>
        </CardTitle>
      </CardHeader>
      <CardContent>
        <!-- 发表评论 -->
        <div v-if="userStore.isLoggedIn" class="mb-6">
          <CommentForm
            :article-id="articleId"
            @success="handleCommentSuccess"
          />
        </div>
        <div v-else class="mb-6 rounded-xl border border-ink/10 bg-paper-dark p-4 text-center">
          <p class="text-ink-light mb-2">登录后可发表评论</p>
          <Button
            variant="outline"
            class="rounded-xl border-ink/20 text-ink hover:bg-paper-card"
            @click="router.push('/login')"
          >
            立即登录
          </Button>
        </div>

        <!-- 评论列表 -->
        <div v-if="loading" class="flex justify-center py-8">
          <div
            class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"
          ></div>
        </div>

        <div v-else-if="comments.length > 0" class="space-y-6">
          <CommentItem
            v-for="comment in comments"
            :key="comment.id"
            :comment="comment"
            @reply="handleReply"
            @refresh="fetchComments"
          />
        </div>

        <div v-else class="text-center py-8 text-ink-light">
          暂无评论，快来发表第一条评论吧！
        </div>

        <!-- 分页 -->
        <div
          v-if="totalPages > 1"
          class="mt-6 flex items-center justify-center gap-2"
        >
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
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { commentApi } from "@/api/comment";
import type { CommentVO } from "@/types/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import CommentForm from "./CommentForm.vue";
import CommentItem from "./CommentItem.vue";

/**
 * 组件 Props
 */
const props = defineProps<{
  articleId: number;
}>();

const router = useRouter();
const userStore = useUserStore();

const comments = ref<CommentVO[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);
const totalPages = ref(0);

/**
 * 获取评论列表
 */
async function fetchComments() {
  loading.value = true;
  try {
    const result = await commentApi.getListByArticle(
      props.articleId,
      currentPage.value,
      pageSize.value,
    );

    // 构建评论树
    comments.value = buildCommentTree(result.list);
    total.value = result.total;
    totalPages.value = Math.ceil(result.total / pageSize.value);
  } catch (err) {
    console.error("获取评论列表失败:", err);
  } finally {
    loading.value = false;
  }
}

/**
 * 将扁平评论列表转换为树形结构
 */
function buildCommentTree(flatComments: CommentVO[]): CommentVO[] {
  const map = new Map<number, CommentVO>();
  const roots: CommentVO[] = [];

  // 第一遍：建立映射
  flatComments.forEach((comment) => {
    map.set(comment.id, { ...comment, children: [] });
  });

  // 第二遍：构建树
  flatComments.forEach((comment) => {
    const node = map.get(comment.id)!;
    // 处理 parentId 为 null 或 0 的情况（都表示根评论）
    if (comment.parentId === null || comment.parentId === 0) {
      roots.push(node);
    } else {
      const parent = map.get(comment.parentId);
      if (parent) {
        parent.children = parent.children || [];
        parent.children.push(node);
      }
    }
  });

  return roots;
}

/**
 * 处理评论成功
 */
function handleCommentSuccess() {
  currentPage.value = 1;
  fetchComments();
}

/**
 * 处理回复
 */
function handleReply(comment: CommentVO) {
  console.log("回复评论:", comment);
}

/**
 * 切换页码
 */
function changePage(page: number) {
  currentPage.value = page;
  fetchComments();
}

onMounted(() => {
  fetchComments();
});
</script>
