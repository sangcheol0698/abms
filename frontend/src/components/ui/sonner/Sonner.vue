<script lang="ts" setup>
import type { ToasterProps } from "vue-sonner"
import { reactiveOmit } from "@vueuse/core"
import { CircleCheckIcon, InfoIcon, Loader2Icon, OctagonXIcon, TriangleAlertIcon, XIcon } from "lucide-vue-next"
import { Toaster as Sonner } from "vue-sonner"

const props = defineProps<ToasterProps>()
const delegatedProps = reactiveOmit(props, "toastOptions")
</script>

<template>
  <Sonner
    class="toaster group"
    :toast-options="{
      classes: {
        toast: 'group toast group-[.toaster]:bg-background group-[.toaster]:text-foreground group-[.toaster]:border-border group-[.toaster]:shadow-lg',
        description: 'group-[.toast]:text-muted-foreground',
        actionButton:
          'group-[.toast]:bg-primary group-[.toast]:text-primary-foreground',
        cancelButton:
          'group-[.toast]:bg-muted group-[.toast]:text-muted-foreground',
      },
    }"
    v-bind="delegatedProps"
  >
    <template #success-icon>
      <CircleCheckIcon class="size-4" />
    </template>
    <template #info-icon>
      <InfoIcon class="size-4" />
    </template>
    <template #warning-icon>
      <TriangleAlertIcon class="size-4" />
    </template>
    <template #error-icon>
      <OctagonXIcon class="size-4" />
    </template>
    <template #loading-icon>
      <div>
        <Loader2Icon class="size-4 animate-spin" />
      </div>
    </template>
    <template #close-icon>
      <XIcon class="size-4" />
    </template>
  </Sonner>
</template>

<style>
.toaster {
  --success-bg: color-mix(in oklch, var(--status-success), white 88%);
  --success-border: color-mix(in oklch, var(--status-success), white 72%);
  --success-text: color-mix(in oklch, var(--status-success), black 38%);
  --error-bg: color-mix(in oklch, var(--status-danger), white 90%);
  --error-border: color-mix(in oklch, var(--status-danger), white 76%);
  --error-text: color-mix(in oklch, var(--status-danger), black 28%);
}

.dark .toaster {
  --success-bg: color-mix(in oklch, var(--status-success), black 78%);
  --success-border: color-mix(in oklch, var(--status-success), black 64%);
  --success-text: color-mix(in oklch, var(--status-success), white 14%);
  --error-bg: color-mix(in oklch, var(--status-danger), black 82%);
  --error-border: color-mix(in oklch, var(--status-danger), black 68%);
  --error-text: color-mix(in oklch, var(--status-danger), white 16%);
}
</style>
