<template>
  <div class="comment-item">
    <div class="flex gap-3">
      <!-- 用户头像 -->
      <Avatar class="h-10 w-10 shrink-0">
        <AvatarImage
          :src="comment.avatar || ''"
          :alt="comment.nickname || ''"
        />
        <AvatarFallback>{{ comment.nickname.charAt(0) }}</AvatarFallback>
      </Avatar>

      <div class="flex-1 min-w-0">
        <!-- 评论头部 -->
        <div class="flex items-center gap-2 mb-2">
          <span class="font-medium text-ink">{{ comment.nickname }}</span>
          <span v-if="comment.replyToUser" class="text-sm text-ink-light">
            回复 @用户{{ comment.replyToUser }}
          </span>
          <span class="text-sm text-ink-light">
            {{ formatDate(comment.createTime) }}
          </span>
        </div>

        <!-- 评论内容 -->
        <div class="mb-2 text-ink whitespace-pre-wrap wrap-break-word">
          <div v-if="isEditing" class="space-y-3">
            <Textarea
              v-model="editContent"
              rows="4"
              class="min-h-20"
              :maxlength="500"
            />
            <div class="flex items-center gap-2">
              <Button
                class="rounded-xl bg-ink text-paper-bg hover:bg-clay dark:bg-clay dark:text-paper-bg"
                :disabled="!editContent.trim() || saving"
                @click="handleUpdate"
              >
                {{ saving ? "保存中..." : "保存" }}
              </Button>
              <Button
                variant="outline"
                class="rounded-xl border-ink/20 text-ink hover:bg-paper-dark"
                :disabled="saving"
                @click="cancelEdit"
              >
                取消
              </Button>
            </div>
          </div>
          <MarkdownRenderer v-else :content="displayContent" />
        </div>

        <!-- 操作按钮 -->
        <div
          v-if="!isEditing"
          class="flex flex-wrap items-center gap-4 text-sm"
        >
          <button
            v-if="!isDeleted"
            class="text-ink-light hover:text-clay transition-colors"
            @click="showReplyForm = !showReplyForm"
          >
            💬 回复
          </button>
          <button
            v-if="!isDeleted"
            class="transition-colors flex items-center gap-1"
            :class="isLiked ? 'text-clay' : 'text-ink-light hover:text-clay'"
            @click="handleLike"
          >
            ❤️ {{ likeCount }}
          </button>
          <button
            v-if="isOwnComment && !isDeleted"
            class="text-ink-light hover:text-clay transition-colors"
            @click="startEdit"
          >
            ✏️ 编辑
          </button>
          <button
            v-if="isOwnComment && !isDeleted"
            class="text-ink-light hover:text-destructive transition-colors"
            @click="handleDelete"
          >
            🗑 删除
          </button>
        </div>

        <!-- 回复表单 -->
        <div v-if="showReplyForm && !isDeleted" class="mt-4">
          <CommentForm
            :article-id="comment.articleId"
            :parent-comment="comment"
            @success="handleReplySuccess"
            @cancel="showReplyForm = false"
          />
        </div>

        <!-- 子评论 -->
        <div
          v-if="comment.children && comment.children.length > 0"
          class="mt-4 space-y-4"
        >
          <CommentItem
            v-for="child in comment.children"
            :key="child.id"
            :comment="child"
            @reply="$emit('reply', $event)"
            @refresh="$emit('refresh')"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { toast } from "vue-sonner";
import type { CommentVO } from "@/types/api";
import { commentApi } from "@/api/comment";
import { useUserStore } from "@/stores/user";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import MarkdownRenderer from "@/components/Markdown/MarkdownRenderer.vue";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import CommentForm from "./CommentForm.vue";
import { parseServerDate } from "@/utils/date";

/**
 * 组件 Props
 */
const props = defineProps<{
  comment: CommentVO;
}>();

/**
 * 组件 Emits
 */
const emit = defineEmits<{
  (e: "reply", comment: CommentVO): void;
  (e: "refresh"): void;
}>();

const router = useRouter();
const userStore = useUserStore();

const showReplyForm = ref(false);
const likeCount = ref(props.comment.likeCount);
const isLiked = ref(!!props.comment.liked);
const liking = ref(false);
const isEditing = ref(false);
const editContent = ref(props.comment.content || "");
const saving = ref(false);

const isOwnComment = computed(
  () => userStore.userInfo?.id === props.comment.userId,
);

const isDeleted = computed(() => props.comment.status === 3);

const displayContent = computed(() => {
  if (isDeleted.value) return "该评论已删除";
  return props.comment.content || "";
});

watch(
  () => props.comment.likeCount,
  (value) => {
    likeCount.value = value;
  },
);

watch(
  () => props.comment.liked,
  (value) => {
    isLiked.value = !!value;
  },
);

watch(
  () => props.comment.content,
  (value) => {
    if (!isEditing.value) {
      editContent.value = value || "";
    }
  },
);

/**
 * 处理点赞
 */
async function handleLike() {
  if (!userStore.isLoggedIn) {
    toast.error("请先登录");
    router.push("/login");
    return;
  }
  if (isDeleted.value) return;
  if (liking.value) return;
  liking.value = true;
  try {
    if (isLiked.value) {
      const success = await commentApi.unlike(props.comment.id);
      if (success) {
        likeCount.value = Math.max(0, likeCount.value - 1);
        isLiked.value = false;
        toast.success("已取消点赞");
      } else {
        toast.error("取消点赞失败");
      }
    } else {
      const success = await commentApi.like(props.comment.id);
      if (success) {
        likeCount.value += 1;
        isLiked.value = true;
        toast.success("点赞成功");
      } else {
        isLiked.value = true;
        toast.info("您已经点赞过了");
      }
    }
  } catch (err: any) {
    console.error("评论点赞失败:", err);
    toast.error("操作失败", {
      description: err.message || "请稍后重试",
    });
  } finally {
    liking.value = false;
  }
}

/**
 * 进入编辑模式
 */
function startEdit() {
  editContent.value = props.comment.content || "";
  isEditing.value = true;
}

/**
 * 取消编辑
 */
function cancelEdit() {
  editContent.value = props.comment.content || "";
  isEditing.value = false;
}

/**
 * 提交编辑
 */
async function handleUpdate() {
  if (!editContent.value.trim() || saving.value) return;
  saving.value = true;
  try {
    await commentApi.update(props.comment.id, {
      content: editContent.value.trim(),
    });
    toast.success("评论已更新");
    isEditing.value = false;
    emit("refresh");
  } catch (err: any) {
    console.error("更新评论失败:", err);
    toast.error("更新失败", {
      description: err.message || "请稍后重试",
    });
  } finally {
    saving.value = false;
  }
}

/**
 * 删除评论
 */
async function handleDelete() {
  if (saving.value) return;
  const confirmed = window.confirm("确定要删除这条评论吗？");
  if (!confirmed) return;
  saving.value = true;
  try {
    await commentApi.delete(props.comment.id);
    toast.success("评论已删除");
    emit("refresh");
  } catch (err: any) {
    console.error("删除评论失败:", err);
    toast.error("删除失败", {
      description: err.message || "请稍后重试",
    });
  } finally {
    saving.value = false;
  }
}

/**
 * 处理回复成功
 */
function handleReplySuccess() {
  showReplyForm.value = false;
  emit("refresh");
}

/**
 * 格式化日期
 */
function formatDate(dateStr: string): string {
  if (!dateStr) return "刚刚";

  const date = parseServerDate(dateStr);
  // 检查日期是否有效
  if (!date) {
    return "日期解析错误";
  }

  const now = new Date();
  const diff = now.getTime() - date.getTime();

  // 如果时间差为负数，说明是未来时间
  if (diff < 0) {
    return date.toLocaleString("zh-CN");
  }

  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60));
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60));
      if (minutes === 0) {
        return "刚刚";
      }
      return `${minutes}分钟前`;
    }
    return `${hours}小时前`;
  }
  if (days === 1) return "昨天";
  if (days < 7) return `${days}天前`;
  return date.toLocaleDateString("zh-CN");
}
</script>
