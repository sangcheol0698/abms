<template>
  <Card class="shadow-none">
    <CardHeader>
      <CardTitle>직급별 인원 분포</CardTitle>
      <CardDescription>직급에 따른 직원 수 현황입니다.</CardDescription>
    </CardHeader>
    <CardContent>
      <div class="flex flex-col items-center gap-4">
        <!-- 도넛 차트 -->
        <div class="relative flex h-48 w-48 items-center justify-center">
          <svg viewBox="0 0 40 40" class="h-full w-full -rotate-90 transform">
            <circle
              v-for="item in chartData"
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
            v-for="item in chartData"
            :key="item.label"
            class="flex items-center gap-1.5 cursor-pointer transition-opacity hover:opacity-80"
            @mouseenter="hoveredItem = item"
            @mouseleave="hoveredItem = null"
          >
            <div
              class="h-2.5 w-2.5 rounded-full"
              :style="{ backgroundColor: item.colorCode }"
            ></div>
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

const levels = [
  { label: '사원', count: 15 },
  { label: '선임', count: 12 },
  { label: '책임', count: 8 },
  { label: '팀장', count: 5 },
  { label: '임원', count: 3 },
];

const COLORS = [
  '#f97316', // Orange
  '#eab308', // Yellow
  '#84cc16', // Lime
  '#cbd5e1', // Slate 300
  '#0ea5e9', // Sky 500
];

const radius = 15.9155;
const hoveredItem = ref<any>(null);

const totalCount = computed(() => levels.reduce((acc, curr) => acc + curr.count, 0));

const chartData = computed(() => {
  const total = totalCount.value;
  let currentOffset = 0;

  return levels.map((item, index) => {
    const percentage = total > 0 ? (item.count / total) * 100 : 0;
    const offset = -currentOffset;
    currentOffset += percentage;

    return {
      ...item,
      percentage,
      offset,
      colorCode: COLORS[index % COLORS.length],
    };
  });
});
</script>
