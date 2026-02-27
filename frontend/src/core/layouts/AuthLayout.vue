<template>
  <div class="min-h-svh bg-muted/20 px-4 py-10">
    <div class="mx-auto flex min-h-[calc(100svh-5rem)] w-full max-w-md flex-col justify-center gap-6">
      <div class="flex justify-center">
        <RouterLink to="/" class="flex items-center gap-3 rounded-lg px-3 py-2">
          <div
            class="flex aspect-square size-10 items-center justify-center rounded-lg bg-primary text-sidebar-primary-foreground"
          >
            <img src="@/assets/main_logo.png" alt="ABACUS Logo" class="size-10" />
          </div>
          <div class="flex flex-col leading-none">
            <span class="font-semibold tracking-tight">ABACUS</span>
            <span class="text-xs text-muted-foreground">Abacus Inc</span>
          </div>
        </RouterLink>
      </div>

      <slot />
    </div>
  </div>
</template>
