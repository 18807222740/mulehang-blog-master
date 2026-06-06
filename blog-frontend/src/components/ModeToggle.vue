<script setup lang="ts">
import { Moon, Sun } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { useAppStore } from '@/stores/app'
import { computed } from 'vue'

const appStore = useAppStore()
const isDark = computed(() => appStore.isDark)

const toggleTheme = () => {
  appStore.toggleDark()
}
</script>

<template>
  <Button variant="ghost" size="icon" @click="toggleTheme">
    <Sun v-if="!isDark" class="h-[1.2rem] w-[1.2rem] rotate-0 scale-100 transition-all dark:-rotate-90 dark:scale-0" />
    <Moon v-else class="absolute h-[1.2rem] w-[1.2rem] rotate-90 scale-0 transition-all dark:rotate-0 dark:scale-100" />
    <span class="sr-only">Toggle theme</span>
  </Button>
</template>
