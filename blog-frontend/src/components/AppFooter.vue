<script setup lang="ts">
import { Github } from "lucide-vue-next";
import { useLocaleStore } from "@/stores/locale";

const localeStore = useLocaleStore();
</script>

<template>
  <footer class="border-t border-ink/10 bg-paper-dark/30 pt-20">
    <div class="mx-auto w-full max-w-6xl px-6 pb-12">
      <div class="flex flex-col gap-12 md:flex-row md:items-start md:justify-between">
        <div class="max-w-sm">
          <span class="mb-6 block font-serif text-2xl font-semibold leading-[1.2] text-ink pb-1">
            mulehang-blog
          </span>
          <p class="text-[15px] leading-relaxed text-ink-light">
            {{ localeStore.t.footerDesc }}
          </p>
        </div>

        <div class="flex gap-16">
          <div>
            <h4 class="mb-6 text-sm font-semibold text-ink">
              {{ localeStore.t.quickLinks }}
            </h4>
            <ul class="space-y-3 text-[15px] text-ink-light">
              <li>
                <router-link to="/" class="footer-link">
                  {{ localeStore.t.navHome }}
                </router-link>
              </li>
              <li>
                <router-link to="/categories" class="footer-link">
                  {{ localeStore.t.navCategories }}
                </router-link>
              </li>
              <li>
                <router-link to="/tags" class="footer-link">
                  {{ localeStore.t.navTags }}
                </router-link>
              </li>
              <li>
                <router-link to="/columns" class="footer-link">
                  {{ localeStore.t.navColumns }}
                </router-link>
              </li>
              <li>
                <router-link to="/ai" class="footer-link">
                  {{ localeStore.t.navAi }}
                </router-link>
              </li>
              <li>
                <router-link to="/about" class="footer-link">
                  {{ localeStore.t.about }}
                </router-link>
              </li>
            </ul>
          </div>

          <div>
            <h4 class="mb-6 text-sm font-semibold text-ink">Social</h4>
            <div class="flex gap-4">
              <a
                href="https://github.com/mulehang6/mulehang-blog"
                class="text-ink-light transition-colors hover:text-clay"
                aria-label="GitHub"
              >
                <Github :size="20" :stroke-width="1.5" />
              </a>
            </div>
          </div>
        </div>
      </div>

      <div
        class="mt-16 flex flex-col items-center justify-between gap-3 border-t border-ink/10 pt-8 text-xs text-ink-lighter md:flex-row"
      >
        <p>&copy; 2026 Mulehang. Crafted with Vue & Tailwind.</p>
        <p class="font-mono">git commit -m "update aesthetic"</p>
      </div>
    </div>
  </footer>
</template>
