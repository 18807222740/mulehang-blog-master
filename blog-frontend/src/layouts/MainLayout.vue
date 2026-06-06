<script setup lang="ts">
import AppNavbar from "@/components/AppNavbar.vue";
import AppFooter from "@/components/AppFooter.vue";
</script>

<template>
  <div class="relative min-h-svh text-foreground">
    <AppNavbar />
    <main class="px-6 pb-16 pt-28">
      <div class="mx-auto w-full max-w-6xl">
        <slot />
      </div>
    </main>
    <AppFooter />
  </div>
</template>
