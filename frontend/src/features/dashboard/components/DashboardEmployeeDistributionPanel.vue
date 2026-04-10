<template>
  <Card>
    <CardHeader>
      <CardTitle>직원 구성 현황</CardTitle>
      <CardDescription>{{ selectedYear }}년 말 기준 고용 형태별 분포입니다.</CardDescription>
    </CardHeader>
    <CardContent>
      <div
        v-if="isLoading && totalCount === 0"
        class="flex h-[240px] items-center justify-center text-sm text-muted-foreground"
      >
        데이터를 불러오는 중입니다...
      </div>
      <div
        v-else-if="totalCount === 0"
        class="flex h-[240px] items-center justify-center text-sm text-muted-foreground"
      >
        표시할 직원 구성 데이터가 없습니다.
      </div>
      <div v-else class="flex flex-col items-center gap-4">
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
              :stroke="item.color"
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
            <div class="h-2.5 w-2.5 rounded-full" :style="{ backgroundColor: item.color }"></div>
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
import { getChartColor } from '@/core/theme/theme';
import { useDashboardEmployeeOverviewQuery } from '@/features/dashboard/queries/useDashboardQueries';

const props = withDefaults(
  defineProps<{
    selectedYear?: number;
  }>(),
  {
    selectedYear: new Date().getFullYear(),
  },
);

// SVG 원의 반지름 (둘레가 100이 되도록 설정: 2 * PI * r = 100)
const radius = 15.9155;

interface DistributionItem {
  label: string;
  count: number;
  percentage: number;
  color: string;
  offset: number;
}

const hoveredItem = ref<DistributionItem | null>(null);
const employeeOverviewQuery = useDashboardEmployeeOverviewQuery(computed(() => props.selectedYear));
const isLoading = computed(() => employeeOverviewQuery.isLoading.value);
const summary = computed(() => employeeOverviewQuery.data.value ?? null);
const totalCount = computed(
  () =>
    (summary.value?.fullTimeCount ?? 0) +
    (summary.value?.freelancerCount ?? 0) +
    (summary.value?.outsourcingCount ?? 0) +
    (summary.value?.partTimeCount ?? 0),
);

const distribution = computed(() => {
  const total = totalCount.value;

  const items = [
    {
      label: '정직원',
      count: summary.value?.fullTimeCount ?? 0,
      percentage: total > 0 ? ((summary.value?.fullTimeCount ?? 0) / total) * 100 : 0,
      color: getChartColor(0),
      offset: 0,
    },
    {
      label: '프리랜서',
      count: summary.value?.freelancerCount ?? 0,
      percentage: total > 0 ? ((summary.value?.freelancerCount ?? 0) / total) * 100 : 0,
      color: getChartColor(1),
      offset: 0,
    },
    {
      label: '외주',
      count: summary.value?.outsourcingCount ?? 0,
      percentage: total > 0 ? ((summary.value?.outsourcingCount ?? 0) / total) * 100 : 0,
      color: getChartColor(2),
      offset: 0,
    },
    {
      label: '파트타임',
      count: summary.value?.partTimeCount ?? 0,
      percentage: total > 0 ? ((summary.value?.partTimeCount ?? 0) / total) * 100 : 0,
      color: getChartColor(3),
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
