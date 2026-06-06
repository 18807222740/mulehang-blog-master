<template>
  <BeamBackground />
  <MainLayout v-if="!isStandalonePage">
    <router-view />
  </MainLayout>
  <div v-else class="min-h-screen bg-transparent text-foreground">
    <div class="min-h-screen flex flex-col">
      <router-view />
    </div>
  </div>
  <Toaster />
</template>

<script setup lang="ts">
import { Toaster } from "@/components/ui/sonner";
import MainLayout from "@/layouts/MainLayout.vue";
import BeamBackground from "@/components/BeamBackground.vue";
import { useRoute } from "vue-router";
import { computed, onBeforeUnmount, watch } from "vue";
import { useUserStore } from "@/stores/user";
import { useNotificationStore } from "@/stores/notifications";

const route = useRoute();
const userStore = useUserStore();
const notificationStore = useNotificationStore();

const isStandalonePage = computed(() => {
  const name = route.name as string;
  return ["Login", "Register", "GitHubCallback"].includes(name);
});

/**
 * 根据登录状态自动连接/断开 WebSocket 通知。
 */
watch(
  () => ({ loggedIn: userStore.isLoggedIn }),
  ({ loggedIn }) => {
    if (loggedIn) {
      notificationStore.connect();
      return;
    }
    notificationStore.disconnect();
  },
  { immediate: true },
);

onBeforeUnmount(() => {
  notificationStore.disconnect();
});
</script>
