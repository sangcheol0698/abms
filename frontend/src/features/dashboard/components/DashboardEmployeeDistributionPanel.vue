<template>
  <Card class="shadow-none">
    <CardHeader>
      <CardTitle>직원 구성 현황</CardTitle>
      <CardDescription>전체 직원의 고용 형태별 분포입니다.</CardDescription>
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
}>();

// SVG 원의 반지름 (둘레가 100이 되도록 설정: 2 * PI * r = 100)
const radius = 15.9155;

interface DistributionItem {
  label: string;
  count: number;
  percentage: number;
  colorClass: string;
  colorCode: string;
  offset: number;
}

const hoveredItem = ref<DistributionItem | null>(null);

const distribution = computed(() => {
  const total = props.totalCount || 0;

  // 실제 데이터가 없으므로 총원을 기준으로 임의의 비율로 계산하여 보여줍니다.
  // 추후 API에서 상세 데이터를 받아오도록 수정할 수 있습니다.
  const fullTime = Math.round(total * 0.75); // 75%
  const freelance = Math.round(total * 0.15); // 15%
  const outsource = total - fullTime - freelance; // 나머지

  const items = [
    {
      label: '정직원',
      count: fullTime,
      percentage: 75,
      colorClass: 'bg-orange-500',
      colorCode: '#f97316',
      offset: 0,
    },
    {
      label: '프리랜서',
      count: freelance,
      percentage: 15,
      colorClass: 'bg-yellow-500',
      colorCode: '#eab308',
      offset: 0,
    },
    {
      label: '외주(협력사)',
      count: outsource,
      percentage: 10,
      colorClass: 'bg-lime-500',
      colorCode: '#84cc16',
      offset: 0,
    },
  ];

  // 오프셋 계산 (누적 퍼센티지)
  let currentOffset = 0;
  return items.map((item) => {
    const offset = -currentOffset;
    currentOffset += item.percentage;
    return { ...item, offset };
  });
});
</script>
