<template>
  <div class="min-h-screen flex items-center justify-center bg-transparent">
    <div class="text-center space-y-4">
      <h1 class="font-serif text-6xl font-medium text-ink-lighter">404</h1>
      <p class="text-xl text-ink-light">页面未找到</p>
      <router-link
        to="/"
        class="inline-flex items-center rounded-xl bg-ink px-6 py-2 text-white transition-colors hover:bg-clay"
      >
        返回首页
      </router-link>
    </div>
  </div>
</template>
