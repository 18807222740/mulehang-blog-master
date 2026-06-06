<template>
  <div class="space-y-10">
    <div class="mx-auto w-full max-w-5xl space-y-8">
      <header class="text-center space-y-3">
        <h1 class="font-serif text-4xl font-medium text-ink">
          {{ localeStore.t.aboutTitle }}
        </h1>
        <p class="text-ink-light">
          {{ localeStore.t.aboutSubtitle }}
        </p>
      </header>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card class="border-ink/10 bg-paper-card shadow-soft">
          <CardHeader>
            <CardTitle class="font-serif text-2xl text-ink">
              {{ localeStore.t.aboutIntroTitle }}
            </CardTitle>
          </CardHeader>
          <CardContent class="space-y-3">
            <p class="text-sm text-ink-light">
              {{ localeStore.t.aboutIntroLine1 }}
            </p>
            <p class="text-sm text-ink-light">
              {{ localeStore.t.aboutIntroLine2 }}
            </p>
          </CardContent>
        </Card>

        <Card class="border-ink/10 bg-paper-card shadow-soft">
          <CardHeader>
            <CardTitle class="font-serif text-2xl text-ink">
              {{ localeStore.t.aboutStackTitle }}
            </CardTitle>
          </CardHeader>
          <CardContent class="space-y-4 text-sm">
            <div class="space-y-2">
              <p
                class="text-xs font-semibold uppercase tracking-[0.2em] text-ink-light"
              >
                {{ localeStore.t.aboutFrontend }}
              </p>
              <div class="grid grid-cols-2 gap-3">
                <div
                  class="rounded-full border border-ink/10 bg-paper-dark px-3 py-2 text-ink"
                >
                  Vue 3 + Vite
                </div>
                <div
                  class="rounded-full border border-ink/10 bg-paper-dark px-3 py-2 text-ink"
                >
                  Pinia + Router
                </div>
                <div
                  class="rounded-full border border-ink/10 bg-paper-dark px-3 py-2 text-ink"
                >
                  Tailwind CSS
                </div>
                <div
                  class="rounded-full border border-ink/10 bg-paper-dark px-3 py-2 text-ink"
                >
                  shadcn-vue
                </div>
              </div>
            </div>
            <div class="space-y-2">
              <p
                class="text-xs font-semibold uppercase tracking-[0.2em] text-ink-light"
              >
                {{ localeStore.t.aboutBackend }}
              </p>
              <div class="grid grid-cols-2 gap-3">
                <div
                  class="rounded-full border border-ink/10 bg-paper-dark px-3 py-2 text-ink"
                >
                  Spring Boot 3
                </div>
                <div
                  class="rounded-full border border-ink/10 bg-paper-dark px-3 py-2 text-ink"
                >
                  MyBatis-Plus
                </div>
                <div
                  class="rounded-full border border-ink/10 bg-paper-dark px-3 py-2 text-ink"
                >
                  Redis + Caffeine
                </div>
                <div
                  class="rounded-full border border-ink/10 bg-paper-dark px-3 py-2 text-ink"
                >
                  ElasticSearch
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">
            {{ localeStore.t.aboutHighlightsTitle }}
          </CardTitle>
        </CardHeader>
        <CardContent class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
          <div
            class="rounded-2xl border border-ink/10 bg-paper-dark px-4 py-3 text-ink"
          >
            {{ localeStore.t.aboutHighlight1 }}
          </div>
          <div
            class="rounded-2xl border border-ink/10 bg-paper-dark px-4 py-3 text-ink"
          >
            {{ localeStore.t.aboutHighlight2 }}
          </div>
          <div
            class="rounded-2xl border border-ink/10 bg-paper-dark px-4 py-3 text-ink"
          >
            {{ localeStore.t.aboutHighlight3 }}
          </div>
          <div
            class="rounded-2xl border border-ink/10 bg-paper-dark px-4 py-3 text-ink"
          >
            {{ localeStore.t.aboutHighlight4 }}
          </div>
        </CardContent>
      </Card>

      <Card class="border-ink/10 bg-paper-card shadow-soft">
        <CardHeader>
          <CardTitle class="font-serif text-2xl text-ink">
            {{ localeStore.t.aboutContactTitle }}
          </CardTitle>
        </CardHeader>
        <CardContent class="space-y-2 text-sm text-ink-light">
          <p>{{ localeStore.t.aboutContactLine1 }}</p>
          <p>{{ localeStore.t.aboutContactLine2 }}</p>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
<script setup lang="ts">
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useLocaleStore } from "@/stores/locale";

const localeStore = useLocaleStore();
</script>
