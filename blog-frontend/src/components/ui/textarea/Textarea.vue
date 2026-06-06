<template>
  <textarea
    :class="
      cn(
        'flex min-h-20 w-full rounded-xl border border-ink/10 bg-paper-card px-3 py-2 text-sm text-ink placeholder:text-ink-lighter focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-clay/40 focus-visible:ring-offset-0 disabled:cursor-not-allowed disabled:opacity-50 scrollbar-soft',
        props.class,
      )
    "
    :value="modelValue"
    @input="
      $emit('update:modelValue', ($event.target as HTMLTextAreaElement).value)
    "
    v-bind="$attrs"
  />
</template>

<script setup lang="ts">
import { cn } from "@/lib/utils";

/**
 * Props 定义
 */
const props = defineProps<{
  class?: string;
  modelValue?: string;
}>();

/**
 * Emits 定义：支持 v-model 双向绑定
 */
defineEmits<{
  (e: "update:modelValue", value: string): void;
}>();
</script>
