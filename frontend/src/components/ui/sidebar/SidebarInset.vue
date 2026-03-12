<script setup lang="ts">
import type { HTMLAttributes } from "vue"
import { cn } from "@/lib/utils"

const props = defineProps<{
  class?: HTMLAttributes["class"]
}>()
</script>

<template>
  <main
    data-slot="sidebar-inset"
    :class="cn(
      'bg-background relative flex w-full flex-1 flex-col',
      'md:peer-data-[variant=inset]:m-2 md:peer-data-[variant=inset]:ml-0 md:peer-data-[variant=inset]:rounded-xl md:peer-data-[variant=inset]:border md:peer-data-[variant=inset]:border-border/60 md:peer-data-[variant=inset]:bg-background/98 md:peer-data-[variant=inset]:shadow-[0_16px_36px_-28px_color-mix(in_oklch,var(--foreground),transparent_42%)] dark:md:peer-data-[variant=inset]:shadow-none md:peer-data-[variant=inset]:backdrop-blur-sm md:peer-data-[variant=inset]:peer-data-[state=collapsed]:ml-2',
      props.class,
    )"
  >
    <slot />
  </main>
</template>
