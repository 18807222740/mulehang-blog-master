<template>
  <div class="comment-form">
    <Card class="border-ink/10 bg-paper-card shadow-soft">
      <CardHeader>
        <CardTitle class="font-serif text-xl text-ink">
          {{ parentComment ? `回复 @${parentComment.nickname}` : "发表评论" }}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <Textarea
          v-model="content"
          :placeholder="parentComment ? '写下你的回复...' : '写下你的评论...'"
          rows="4"
          class="mb-4"
          :maxlength="500"
          @paste="handlePasteImage"
        />
        <div class="flex items-center justify-between">
          <span class="text-sm text-ink-light"> {{ content.length }}/500 </span>
          <div class="flex gap-2">
            <Button
              v-if="parentComment"
              variant="outline"
              class="rounded-xl border-ink/20 text-ink hover:bg-paper-dark"
              @click="handleCancel"
            >
              取消
            </Button>
            <Button
              @click="handleSubmit"
              :disabled="!content.trim() || submitting"
              class="rounded-xl bg-ink text-paper-bg hover:bg-clay dark:bg-clay dark:text-paper-bg"
            >
              {{ submitting ? "发送中..." : "发送" }}
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref } from "vue";
import { toast } from "vue-sonner";
import { commentApi } from "@/api/comment";
import { fileApi } from "@/api/file";
import type { CommentVO } from "@/types/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";

/**
 * 组件 Props
 */
const props = defineProps<{
  articleId: number;
  parentComment?: CommentVO;
}>();

/**
 * 组件 Emits
 */
const emit = defineEmits<{
  (e: "success"): void;
  (e: "cancel"): void;
}>();

const content = ref("");
const submitting = ref(false);

/**
 * 粘贴图片到评论内容
 */
async function handlePasteImage(event: ClipboardEvent) {
  const items = event.clipboardData?.items;
  if (!items || !items.length) return;

  const imageItem = Array.from(items).find((item) =>
    item.type.startsWith("image/"),
  );
  if (!imageItem) return;

  const file = imageItem.getAsFile();
  if (!file) return;

  event.preventDefault();

  try {
    const uploaded = await fileApi.upload(file);
    const textarea = event.target as HTMLTextAreaElement | null;
    if (!textarea) return;
    const alt = uploaded.originalFilename || file.name;
    insertMarkdownAtCursor(textarea, `![${alt}](${uploaded.url})`);
    toast.success("图片已上传");
  } catch (err) {
    console.error("图片上传失败:", err);
    toast.error("图片上传失败");
  }
}

/**
 * 在光标处插入 Markdown
 */
function insertMarkdownAtCursor(textarea: HTMLTextAreaElement, value: string) {
  const current = content.value || "";
  const start = textarea.selectionStart ?? current.length;
  const end = textarea.selectionEnd ?? current.length;
  content.value = `${current.slice(0, start)}${value}${current.slice(end)}`;

  nextTick(() => {
    const cursor = start + value.length;
    textarea.setSelectionRange(cursor, cursor);
    textarea.focus();
  });
}

/**
 * 提交评论
 */
async function handleSubmit() {
  if (!content.value.trim()) return;

  submitting.value = true;
  try {
    await commentApi.create({
      articleId: props.articleId,
      content: content.value.trim(),
      parentId: props.parentComment?.id,
      replyToUser: props.parentComment?.userId,
    });

    content.value = "";
    emit("success");
  } catch (err: any) {
    console.error("发表评论失败:", err);
    alert(err.message || "发表评论失败，请稍后重试");
  } finally {
    submitting.value = false;
  }
}

/**
 * 取消回复
 */
function handleCancel() {
  content.value = "";
  emit("cancel");
}
</script>
