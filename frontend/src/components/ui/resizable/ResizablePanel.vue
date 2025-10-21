<script setup lang="ts">
import type { SplitterPanelEmits, SplitterPanelProps } from "reka-ui"
import { SplitterPanel, useForwardPropsEmits } from "reka-ui"

const props = defineProps<SplitterPanelProps>()
const emits = defineEmits<SplitterPanelEmits>()

defineSlots<{
  default?: (props: {
    isCollapsed: boolean
    isExpanded: boolean
    collapse: () => void
    expand: () => void
    resize: (size: number) => void
  }) => any
}>()

const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
  <SplitterPanel
    data-slot="resizable-panel"
    v-bind="forwarded"
    v-slot="slotProps"
  >
    <slot v-bind="slotProps" />
  </SplitterPanel>
</template>
