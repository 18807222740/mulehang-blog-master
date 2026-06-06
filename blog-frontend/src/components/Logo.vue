<template>
  <router-link
    to="/"
    class="group relative flex items-center gap-3 no-underline"
  >
    <div class="relative flex h-9 w-9 items-center justify-center">
      <svg viewBox="0 0 40 40" class="logo-badge h-9 w-9 text-clay drop-shadow-sm">
        <path
          class="logo-fill"
          d="M5 5 Q 20 2 35 5 Q 38 20 35 35 Q 20 38 5 35 Q 2 20 5 5 Z"
          fill="currentColor"
        />
        <path
          d="M5 5 Q 20 2 35 5 Q 38 20 35 35 Q 20 38 5 35 Q 2 20 5 5 Z"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          class="logo-mark text-ink"
          d="M12 28 L12 12 L20 22 L28 12 L28 28"
          fill="none"
          stroke="currentColor"
          stroke-width="2.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
    </div>

    <div class="relative flex flex-col">
      <span
        class="font-serif text-2xl font-semibold tracking-tight text-ink transition-colors group-hover:text-ink/80"
      >
        mulehang
      </span>
      <svg
        class="logo-underline absolute -bottom-1 left-0 h-2 w-full opacity-0 transition-opacity duration-300 group-hover:opacity-100"
        viewBox="0 0 100 10"
        preserveAspectRatio="none"
      >
        <path
          d="M0 5 Q 50 12 100 5"
          fill="none"
          stroke="#DA7756"
          stroke-width="3"
          stroke-linecap="round"
        />
      </svg>
    </div>
  </router-link>
</template>

<style scoped>
.logo-badge {
  transition: transform 0.3s ease;
}

.logo-fill {
  opacity: 0.2;
  transition: opacity 0.3s ease;
}

.logo-mark {
  transition: stroke 0.3s ease;
}

.logo-underline path {
  stroke-dasharray: 120;
  stroke-dashoffset: 120;
  transition: stroke-dashoffset 0.6s ease-in-out;
}

.group:hover .logo-badge {
  animation: logo-wiggle 0.5s ease-in-out;
}

.group:hover .logo-fill {
  opacity: 1;
}

.group:hover .logo-mark {
  stroke: var(--paper-bg);
}

.group:hover .logo-underline path {
  stroke-dashoffset: 0;
}

@keyframes logo-wiggle {
  0%,
  100% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(-5deg);
  }
  75% {
    transform: rotate(5deg);
  }
}
</style>
