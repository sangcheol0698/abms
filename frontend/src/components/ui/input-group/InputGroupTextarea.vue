<script setup lang="ts">
import type { HTMLAttributes } from "vue"
import { cn } from "@/lib/utils"
import { Textarea } from '@/components/ui/textarea'

defineOptions({ inheritAttrs: false })

const props = defineProps<{
  class?: HTMLAttributes["class"]
}>()

const modelValue = defineModel<string | number | undefined>()
</script>

<template>
  <Textarea
    v-model="modelValue"
    v-bind="$attrs"
    data-slot="input-group-control"
    :class="cn(
      'flex-1 resize-none rounded-none border-0 bg-transparent py-3 shadow-none focus-visible:ring-0 dark:bg-transparent',
      props.class,
    )"
  />
</template>
