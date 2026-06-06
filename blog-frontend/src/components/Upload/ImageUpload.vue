<template>
  <div class="space-y-2">
    <label v-if="label" class="text-sm font-medium text-ink">{{ label }}</label>
    <div
      class="relative rounded-xl border border-dashed px-4 py-6 transition-colors"
      :class="wrapperClass"
      @dragover.prevent="handleDragOver"
      @dragleave.prevent="handleDragLeave"
      @drop.prevent="handleDrop"
    >
      <input
        ref="fileInput"
        type="file"
        :accept="accept"
        class="hidden"
        :disabled="disabled || uploading"
        @change="handleFileChange"
      />

      <div v-if="!previewUrl" class="flex flex-col items-center gap-2 text-center">
        <p class="text-sm text-ink-light">{{ dropText }}</p>
        <Button
          type="button"
          size="sm"
          :disabled="disabled || uploading"
          class="rounded-full bg-ink text-white hover:bg-clay"
          @click="triggerFileInput"
        >
          {{ uploadText }}
        </Button>
        <p v-if="helper" class="text-xs text-ink-light">{{ helper }}</p>
      </div>

      <div v-else class="space-y-3">
        <img
          :src="previewUrl"
          :alt="previewAlt"
          loading="lazy"
          decoding="async"
          class="h-48 w-full rounded-lg object-cover"
        />
        <div class="flex items-center justify-between text-xs text-ink-light">
          <span class="truncate">{{ fileName || previewUrl }}</span>
          <button
            v-if="canRemove"
            type="button"
            class="text-clay transition-colors hover:text-ink"
            @click="handleRemove"
          >
            {{ removeText }}
          </button>
        </div>
      </div>

      <div v-if="uploading" class="absolute inset-x-4 bottom-3">
        <div class="h-1 w-full rounded-full bg-ink/10">
          <div
            class="h-1 rounded-full bg-clay transition-all"
            :style="{ width: `${progress}%` }"
          ></div>
        </div>
        <p class="mt-1 text-xs text-ink-light">Uploading {{ progress }}%</p>
      </div>
    </div>
    <p v-if="errorMessage" class="text-xs text-destructive">{{ errorMessage }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from "vue";
import { Button } from "@/components/ui/button";
import { fileApi } from "@/api/file";

interface Props {
  modelValue?: string;
  label?: string;
  helper?: string;
  accept?: string;
  maxSizeMb?: number;
  disabled?: boolean;
  uploadText?: string;
  dropText?: string;
  removeText?: string;
  previewAlt?: string;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: "",
  label: "",
  helper: "",
  accept: "image/*",
  maxSizeMb: 5,
  disabled: false,
  uploadText: "Select Image",
  dropText: "Click or drag to upload",
  removeText: "Remove",
  previewAlt: "Preview image",
});

const emit = defineEmits<{
  (event: "update:modelValue", value: string): void;
}>();

const fileInput = ref<HTMLInputElement | null>(null);
const localPreview = ref("");
const fileName = ref("");
const uploading = ref(false);
const progress = ref(0);
const dragActive = ref(false);
const errorMessage = ref("");

const previewUrl = computed(() => localPreview.value || props.modelValue || "");
const canRemove = computed(() => !!previewUrl.value && !uploading.value && !props.disabled);
const wrapperClass = computed(() => {
  if (props.disabled) return "border-ink/10 bg-paper-card opacity-60";
  if (dragActive.value) return "border-clay bg-paper-dark/60";
  return "border-ink/20 bg-paper-card";
});

/**
 * 触发文件选择对话框。
 */
function triggerFileInput() {
  if (props.disabled || uploading.value) return;
  fileInput.value?.click();
}

/**
 * 处理文件选择事件。
 */
function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;
  uploadFile(file);
  target.value = "";
}

/**
 * 处理拖拽进入。
 */
function handleDragOver() {
  if (props.disabled || uploading.value) return;
  dragActive.value = true;
}

/**
 * 处理拖拽离开。
 */
function handleDragLeave() {
  dragActive.value = false;
}

/**
 * 处理拖拽放置。
 */
function handleDrop(event: DragEvent) {
  if (props.disabled || uploading.value) return;
  dragActive.value = false;
  const file = event.dataTransfer?.files?.[0];
  if (!file) return;
  uploadFile(file);
}

/**
 * 上传文件并生成预览。
 */
async function uploadFile(file: File) {
  errorMessage.value = "";
  if (!file.type.startsWith("image/")) {
    errorMessage.value = "Only image files are supported.";
    return;
  }
  if (props.maxSizeMb && file.size > props.maxSizeMb * 1024 * 1024) {
    errorMessage.value = `File size exceeds ${props.maxSizeMb} MB.`;
    return;
  }

  setLocalPreview(file);
  uploading.value = true;
  progress.value = 0;

  try {
    const uploaded = await fileApi.upload(file, (value) => {
      progress.value = value;
    });
    emit("update:modelValue", uploaded.url);
    fileName.value = uploaded.originalFilename || file.name;
  } catch (error) {
    errorMessage.value = "Upload failed. Please try again.";
  } finally {
    uploading.value = false;
  }
}

/**
 * 设置本地预览地址。
 */
function setLocalPreview(file: File) {
  clearLocalPreview();
  localPreview.value = URL.createObjectURL(file);
  fileName.value = file.name;
}

/**
 * 清理本地预览地址。
 */
function clearLocalPreview() {
  if (localPreview.value) {
    URL.revokeObjectURL(localPreview.value);
    localPreview.value = "";
  }
}

/**
 * 移除已选图片。
 */
function handleRemove() {
  clearLocalPreview();
  fileName.value = "";
  emit("update:modelValue", "");
}

watch(
  () => props.modelValue,
  (value) => {
    if (value && localPreview.value) {
      clearLocalPreview();
    }
    if (!value) {
      fileName.value = "";
    }
  },
);

onBeforeUnmount(() => {
  clearLocalPreview();
});
</script>
