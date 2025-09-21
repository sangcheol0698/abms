<template>
  <div class="flex min-h-screen bg-background text-foreground">
    <aside class="hidden w-64 shrink-0 border-r border-border bg-sidebar p-6 text-sm md:flex md:flex-col">
      <div class="mb-8 flex items-center gap-2">
        <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-primary text-primary-foreground">AB</div>
        <div>
          <p class="text-base font-semibold">ABMS</p>
          <p class="text-xs text-muted-foreground">Abacus Inc.</p>
        </div>
      </div>
      <nav class="flex flex-1 flex-col gap-1">
        <RouterLink
          v-for="item in mainLinks"
          :key="item.to"
          :to="item.to"
          class="flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
          :class="isActive(item.to) ? 'bg-sidebar-primary text-sidebar-primary-foreground shadow' : 'text-muted-foreground'"
        >
          <component :is="item.icon" class="h-4 w-4" aria-hidden="true" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
      <footer class="mt-6 border-t border-border pt-4 text-xs text-muted-foreground">
        <p class="font-medium text-foreground">도움이 필요하신가요?</p>
        <p>docs/도메인모델.md를 참고해 비즈니스 규칙을 확인하세요.</p>
      </footer>
    </aside>

    <div class="flex flex-1 flex-col">
      <header class="sticky top-0 z-10 flex h-16 items-center justify-between border-b border-border bg-background/95 px-4 backdrop-blur">
        <div>
          <p class="text-sm text-muted-foreground">ABACUS Business Management Suite</p>
          <h1 class="text-lg font-semibold tracking-tight">{{ activePageTitle }}</h1>
        </div>
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-md border border-border bg-card px-3 py-2 text-xs font-medium text-muted-foreground shadow-sm transition hover:bg-card/80"
        >
          빠른 명령 (⌘K)
        </button>
      </header>

      <main class="flex-1 overflow-y-auto bg-background px-4 py-6">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, RouterLink, RouterView } from 'vue-router';
import { Home, Users } from 'lucide-vue-next';

const route = useRoute();

const mainLinks = [
  { label: '조직도', to: '/', icon: Home },
  { label: '구성원', to: '/employees', icon: Users },
];

const isActive = (to: string) => route.path === to;

const activePageTitle = computed(() => route.meta?.title ?? '');
</script>
