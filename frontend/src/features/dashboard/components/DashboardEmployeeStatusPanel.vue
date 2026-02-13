<template>
  <Card class="shadow-none">
    <CardHeader>
      <CardTitle>근무 상태 현황</CardTitle>
      <CardDescription>전체 직원의 근무 및 휴가 현황입니다.</CardDescription>
    </CardHeader>
    <CardContent>
      <div class="flex flex-col items-center gap-4">
        <!-- 도넛 차트 -->
        <div class="relative flex h-48 w-48 items-center justify-center">
          <svg viewBox="0 0 40 40" class="h-full w-full -rotate-90 transform">
            <circle
              v-for="item in distribution"
              :key="item.label"
              cx="20"
              cy="20"
              :r="radius"
              fill="transparent"
              stroke-width="5"
              :stroke="item.colorCode"
              :stroke-dasharray="`${item.percentage} ${100 - item.percentage}`"
              :stroke-dashoffset="item.offset"
              class="cursor-pointer transition-all duration-200 hover:opacity-80"
              @mouseenter="hoveredItem = item"
              @mouseleave="hoveredItem = null"
            />
          </svg>
          <div class="absolute inset-0 flex items-center justify-center pointer-events-none">
            <div class="text-center">
              <div class="text-2xl font-bold">
                {{ hoveredItem ? hoveredItem.count : totalCount }}명
              </div>
              <div class="text-xs text-muted-foreground">
                {{ hoveredItem ? hoveredItem.label : 'Total' }}
              </div>
            </div>
          </div>
        </div>

        <!-- 하단 범례 -->
        <div class="flex flex-wrap justify-center gap-x-4 gap-y-2">
          <div
            v-for="item in distribution"
            :key="item.label"
            class="flex items-center gap-1.5 cursor-pointer transition-opacity hover:opacity-80"
            @mouseenter="hoveredItem = item"
            @mouseleave="hoveredItem = null"
          >
            <div class="h-2.5 w-2.5 rounded-full" :class="item.colorClass"></div>
            <span class="text-xs text-muted-foreground">{{ item.label }}</span>
          </div>
        </div>
      </div>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

const props = defineProps<{
  totalCount?: number;
  onLeaveCount?: number;
}>();

const radius = 15.9155;
const hoveredItem = ref<any>(null);

const distribution = computed(() => {
  const total = props.totalCount || 0;
  const onLeave = props.onLeaveCount || 0;
  const working = Math.max(0, total - onLeave);

  const workingPercentage = total > 0 ? Math.round((working / total) * 100) : 0;
  const onLeavePercentage = total > 0 ? 100 - workingPercentage : 0;

  const items = [
    {
      label: '근무 중',
      count: working,
      percentage: workingPercentage,
      colorClass: 'bg-orange-500',
      colorCode: '#f97316',
      offset: 0,
    },
    {
      label: '휴가/휴직',
      count: onLeave,
      percentage: onLeavePercentage,
      colorClass: 'bg-slate-300',
      colorCode: '#cbd5e1',
      offset: 0,
    },
  ];

  let currentOffset = 0;
  return items.map((item) => {
    const offset = -currentOffset;
    currentOffset += item.percentage;
    return { ...item, offset };
  });
});
</script>
