<template>
  <Card>
    <CardHeader>
      <CardTitle>월별 재무 추이</CardTitle>
      <CardDescription>{{ selectedYear }}년 월별 매출, 비용, 이익 현황입니다.</CardDescription>
    </CardHeader>
    <CardContent>
      <div
        v-if="isLoading && chartData.length === 0"
        class="mt-4 flex h-[240px] items-center justify-center text-sm text-muted-foreground"
      >
        데이터를 불러오는 중입니다...
      </div>
      <div
        v-else-if="chartData.length === 0"
        class="mt-4 flex h-[240px] items-center justify-center text-sm text-muted-foreground"
      >
        표시할 재무 데이터가 없습니다.
      </div>
      <div v-else class="mt-4">
        <BarChart
          :data="chartData"
          :categories="['revenue', 'cost', 'profit']"
          index="month"
          :margin="{ top: 12, right: 0, bottom: 0, left: 0 }"
          :colors="[revenueColor, costColor, profitColor]"
          :rounded-corners="4"
          :show-x-axis="false"
          :show-legend="false"
          :show-grid-line="true"
          :x-tick-values="xTickValues"
          :x-num-ticks="chartData.length"
          :x-tick-text-hide-overlapping="false"
          :y-num-ticks="5"
          :x-formatter="formatMonthTick"
          :y-formatter="formatAxisTick"
          :custom-tooltip="DashboardMonthlyFinancialTooltip"
          class="dashboard-monthly-chart h-[280px]"
        />
        <div class="mt-2 grid text-center text-xs font-medium text-muted-foreground" :style="monthGridStyle">
          <span v-for="item in chartData" :key="item.month">
            {{ item.month }}
          </span>
        </div>
      </div>

      <div class="mt-4 flex items-center justify-center gap-4 text-xs text-muted-foreground">
        <div class="flex items-center gap-1.5">
          <div class="h-2.5 w-2.5 rounded-sm" :style="{ backgroundColor: revenueColor }"></div>
          <span>매출</span>
        </div>
        <div class="flex items-center gap-1.5">
          <div class="h-2.5 w-2.5 rounded-sm" :style="{ backgroundColor: costColor }"></div>
          <span>비용</span>
        </div>
        <div class="flex items-center gap-1.5">
          <div class="h-2.5 w-2.5 rounded-sm" :style="{ backgroundColor: profitColor }"></div>
          <span>이익</span>
        </div>
      </div>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { BarChart } from '@/components/ui/chart-bar';
import { useDashboardMonthlyRevenueSummaryQuery } from '@/features/dashboard/queries/useDashboardQueries';
import DashboardMonthlyFinancialTooltip from '@/features/dashboard/components/DashboardMonthlyFinancialTooltip.vue';
import { getChartColor } from '@/core/theme/theme';
import { formatAxisAmount } from '@/features/dashboard/utils/format';

const props = withDefaults(
  defineProps<{
    selectedYear?: number;
  }>(),
  {
    selectedYear: new Date().getFullYear(),
  },
);

const monthlyRevenueQuery = useDashboardMonthlyRevenueSummaryQuery(computed(() => props.selectedYear));
const isLoading = computed(() => monthlyRevenueQuery.isLoading.value);
const revenueColor = getChartColor(0);
const costColor = getChartColor(3);
const profitColor = getChartColor(2);

interface MonthlyChartDatum {
  month: string;
  revenue: number;
  cost: number;
  profit: number;
}

const chartData = computed<MonthlyChartDatum[]>(() => {
  return (monthlyRevenueQuery.data.value ?? []).map((item) => {
    const date = new Date(item.targetMonth);
    return {
      month: `${date.getMonth() + 1}월`,
      revenue: item.revenue,
      cost: item.cost,
      profit: item.profit,
    };
  });
});

const xTickValues = computed(() => chartData.value.map((_, index) => index));
const monthGridStyle = computed(() => ({
  gridTemplateColumns: `repeat(${Math.max(chartData.value.length, 1)}, minmax(0, 1fr))`,
}));

function formatMonthTick(value: number) {
  return chartData.value[value]?.month ?? '';
}

function formatAxisTick(value: number) {
  return formatAxisAmount(value);
}
</script>

<style scoped>
:deep(.dashboard-monthly-chart) {
  --vis-tooltip-padding: 0;
  --vis-tooltip-border-color: transparent;
  --vis-tooltip-background-color: transparent;
  --vis-tooltip-box-shadow: none;
  --vis-tooltip-border-radius: 0;
}

:deep(.dashboard-monthly-chart .vis-axis-grid-line) {
  stroke: color-mix(in oklch, var(--border), transparent 18%);
  stroke-dasharray: 3 4;
}

:deep(.dashboard-monthly-chart .vis-axis-tick-label) {
  fill: var(--muted-foreground);
  font-size: 12px;
}

:deep(.dashboard-monthly-chart .vis-axis-line) {
  stroke: color-mix(in oklch, var(--border), transparent 12%);
}
</style>
