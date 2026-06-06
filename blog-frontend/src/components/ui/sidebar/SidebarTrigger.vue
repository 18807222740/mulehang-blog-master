<script setup lang="ts">
import type { HTMLAttributes } from "vue";
import { PanelLeft } from "lucide-vue-next";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { useSidebar } from "./utils";

const props = defineProps<{
  class?: HTMLAttributes["class"];
}>();

const { toggleSidebar } = useSidebar();
</script>

<template>
  <Button
    data-sidebar="trigger"
    data-slot="sidebar-trigger"
    variant="ghost"
    size="icon"
    :class="
      cn(
        'h-7 w-7 transition-all duration-300 hover:scale-110 hover:bg-accent/80 active:scale-95 group',
        props.class,
      )
    "
    @click="toggleSidebar"
  >
    <PanelLeft
      class="transition-transform duration-300 group-hover:rotate-12 group-active:rotate-0"
    />
    <span class="sr-only">Toggle Sidebar</span>
  </Button>
</template>
