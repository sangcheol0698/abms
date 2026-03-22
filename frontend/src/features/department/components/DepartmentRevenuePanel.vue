<template>
  <div class="flex flex-col gap-4">
    <div v-if="isLoading && data.length === 0" class="rounded-xl border bg-card p-6 text-sm text-muted-foreground">
      매출 데이터를 불러오는 중입니다...
    </div>

    <div v-else class="flex flex-col gap-4">
      <div class="grid gap-3 md:grid-cols-3">
        <article class="rounded-xl border bg-card p-4 shadow-sm">
          <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
            기준 월 매출
          </p>
          <p class="mt-2 text-2xl font-semibold text-foreground" data-test="latest-revenue">
            {{ formatAmount(latestSummary?.revenue ?? 0) }}
          </p>
          <p class="mt-1 text-xs text-muted-foreground">
            {{ latestMonthLabel }} 기준
          </p>
        </article>
        <article class="rounded-xl border bg-card p-4 shadow-sm">
          <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
            기준 월 비용
          </p>
          <p class="mt-2 text-2xl font-semibold text-foreground" data-test="latest-cost">
            {{ formatAmount(latestSummary?.cost ?? 0) }}
          </p>
          <p class="mt-1 text-xs text-muted-foreground">
            {{ latestMonthLabel }} 기준
          </p>
        </article>
        <article class="rounded-xl border bg-card p-4 shadow-sm">
          <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
            기준 월 이익
          </p>
          <p class="mt-2 text-2xl font-semibold text-foreground" data-test="latest-profit">
            {{ formatAmount(latestSummary?.profit ?? 0) }}
          </p>
          <p class="mt-1 text-xs text-muted-foreground">
            {{ latestMonthLabel }} 기준
          </p>
        </article>
      </div>

      <div class="rounded-xl border bg-card p-4 shadow-sm">
        <div class="flex items-center justify-between gap-3">
          <div>
            <p class="text-sm font-semibold text-foreground">최근 6개월 매출 추이</p>
            <p class="text-xs text-muted-foreground">
              부서 주관 프로젝트의 최신 월별 스냅샷을 기준으로 집계합니다.
            </p>
          </div>
        </div>

        <div
          v-if="errorMessage"
          class="mt-4 rounded-lg border border-dashed border-destructive/40 bg-destructive/5 p-4 text-sm text-destructive"
        >
          {{ errorMessage }}
        </div>

        <div v-else class="mt-4 flex h-[240px] items-end gap-2 sm:gap-4">
          <div
            v-for="item in data"
            :key="item.monthKey"
            class="group relative flex h-full flex-1 flex-col justify-end gap-1"
          >
            <div
              class="absolute bottom-full left-1/2 z-10 mb-2 hidden w-max -translate-x-1/2 rounded border border-border bg-popover px-3 py-2 text-xs shadow-md group-hover:block"
            >
              <div class="mb-1 font-semibold">{{ item.monthLabel }}</div>
              <div class="grid grid-cols-2 gap-x-2 gap-y-1">
                <span class="text-muted-foreground">매출:</span>
                <span class="text-right font-medium">{{ formatAmount(item.revenue) }}</span>
                <span class="text-muted-foreground">비용:</span>
                <span class="text-right font-medium">{{ formatAmount(item.cost) }}</span>
                <span class="text-muted-foreground">이익:</span>
                <span class="text-right font-medium text-primary">{{ formatAmount(item.profit) }}</span>
              </div>
            </div>

            <div class="flex h-full w-full items-end justify-center gap-0.5 sm:gap-1">
              <div
                class="w-full rounded-t-sm transition-all hover:opacity-90"
                :style="{ height: `${(item.revenue / maxValue) * 100}%`, backgroundColor: revenueColor }"
              />
              <div
                class="w-full rounded-t-sm transition-all hover:opacity-90"
                :style="{ height: `${(item.cost / maxValue) * 100}%`, backgroundColor: costColor }"
              />
              <div
                class="w-full rounded-t-sm transition-all hover:opacity-90"
                :style="{ height: `${(Math.max(0, item.profit) / maxValue) * 100}%`, backgroundColor: profitColor }"
              />
            </div>

            <div class="mt-2 text-center text-xs font-medium text-muted-foreground">
              {{ item.monthLabel }}
            </div>
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
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { getChartColor } from '@/core/theme/theme';
import { useDepartmentRevenueTrendQuery } from '@/features/department/queries/useDepartmentQueries';

const props = defineProps<{
  departmentId: number;
}>();

const revenueColor = getChartColor(0);
const costColor = getChartColor(3);
const profitColor = getChartColor(2);

const currentYearMonth = computed(() => {
  const now = new Date();
  return `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`;
});

const revenueTrendQuery = useDepartmentRevenueTrendQuery(
  computed(() => props.departmentId),
  currentYearMonth,
);

const isLoading = computed(() => revenueTrendQuery.isLoading.value || revenueTrendQuery.isFetching.value);
const errorMessage = computed(() => {
  const error = revenueTrendQuery.error.value;
  if (!error) {
    return '';
  }

  return error instanceof Error ? error.message : '매출 데이터를 불러오지 못했습니다.';
});

const data = computed(() =>
  (revenueTrendQuery.data.value ?? []).map((item) => {
    const date = new Date(item.targetMonth);
    return {
      monthKey: item.targetMonth,
      monthLabel: `${date.getMonth() + 1}월`,
      revenue: item.revenue,
      cost: item.cost,
      profit: item.profit,
    };
  }),
);

const latestSummary = computed(() => {
  if (data.value.length === 0) {
    return null;
  }

  return data.value[data.value.length - 1] ?? null;
});

const latestMonthLabel = computed(() => latestSummary.value?.monthLabel ?? '이번 달');

const maxValue = computed(() => {
  const maximum = Math.max(
    0,
    ...data.value.map((item) => Math.max(item.revenue, item.cost, Math.max(0, item.profit))),
  );

  return maximum > 0 ? maximum * 1.1 : 1;
});

function formatAmount(value: number) {
  if (value >= 100000000) {
    return `${(value / 100000000).toFixed(1)}억`;
  }
  if (value <= -100000000) {
    return `${(value / 100000000).toFixed(1)}억`;
  }
  if (value >= 1000000 || value <= -1000000) {
    return `${(value / 1000000).toFixed(0)}백만`;
  }
  return `${value.toLocaleString()}원`;
}
</script>
