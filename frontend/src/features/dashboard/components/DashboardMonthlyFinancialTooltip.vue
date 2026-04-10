<script setup lang="ts">
import { computed } from 'vue'
import { formatCurrencyAmount } from '@/features/dashboard/utils/format'

const props = defineProps<{
  title?: string
  data: {
    name: string
    color: string
    value: unknown
  }[]
}>()

function formatLabel(name: string) {
  switch (name) {
    case 'revenue':
      return '매출'
    case 'cost':
      return '비용'
    case 'profit':
      return '이익'
    default:
      return name
  }
}

const items = computed(() =>
  props.data.map((item) => ({
    ...item,
    name: formatLabel(item.name),
    value: typeof item.value === 'number' ? formatCurrencyAmount(item.value) : String(item.value ?? ''),
  })),
)
</script>

<template>
  <div class="min-w-[180px] rounded-lg bg-background/95 p-3 text-sm shadow-lg backdrop-blur-sm">
    <div v-if="title" class="mb-2 text-sm font-semibold text-foreground">
      {{ title }}
    </div>
    <div class="flex flex-col gap-1.5">
      <div v-for="(item, key) in items" :key="key" class="flex justify-between gap-6">
        <div class="flex items-center">
          <span class="mr-2 h-2.5 w-2.5 rounded-full" :style="{ backgroundColor: item.color }" />
          <span>{{ item.name }}</span>
        </div>
        <span class="font-semibold">{{ item.value }}</span>
      </div>
    </div>
  </div>
</template>
