<script setup lang="ts">
import type { HTMLAttributes } from "vue"
import { NumberFieldInput } from "reka-ui"
import { cn } from "@/lib/utils"

const props = defineProps<{
  class?: HTMLAttributes["class"]
}>()
</script>

<template>
  <NumberFieldInput
    data-slot="input"
    :class="cn('flex h-9 w-full rounded-md border border-input bg-input-surface py-1 text-sm text-center shadow-sm transition-[color,box-shadow,background-color] placeholder:text-muted-foreground hover:bg-input-surface-hover focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50', props.class)"
  />
</template>
