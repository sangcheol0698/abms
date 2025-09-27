<script setup lang="ts">
import type { HTMLAttributes } from "vue"
import { useVModel } from "@vueuse/core"
import { cn } from "@/lib/utils"

const props = defineProps<{
  defaultValue?: string | number
  modelValue?: string | number
  class?: HTMLAttributes["class"]
}>()

const emits = defineEmits<{
  (e: "update:modelValue", payload: string | number): void
  (e: "compositionupdate", payload: string): void
}>()

const modelValue = useVModel(props, "modelValue", emits, {
  passive: true,
  defaultValue: props.defaultValue,
})

function handleCompositionUpdate(event: CompositionEvent) {
  const target = event.target as HTMLInputElement | null
  if (!target) {
    return
  }
  emits("compositionupdate", target.value)
}
</script>

<template>
  <input
    v-model="modelValue"
    data-slot="input"
    @compositionupdate="handleCompositionUpdate"
    :class="cn(
      'file:text-foreground placeholder:text-muted-foreground/70 selection:bg-primary selection:text-primary-foreground border-input flex h-9 w-full min-w-0 rounded-md border border-border/70 bg-card/80 px-3 py-1 text-base shadow-xs transition-[color,box-shadow] outline-none file:inline-flex file:h-7 file:border-0 file:bg-transparent file:text-sm file:font-medium disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50 md:text-sm dark:bg-slate-900/60',
      'focus-visible:border-ring focus-visible:ring-primary/40 focus-visible:ring-2 focus-visible:ring-offset-1 focus-visible:ring-offset-background',
      'aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive',
      props.class,
    )"
  >
</template>
