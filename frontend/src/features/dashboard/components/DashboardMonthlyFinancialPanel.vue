<template>
  <Card class="shadow-none">
    <CardHeader>
      <CardTitle>월별 재무 추이</CardTitle>
      <CardDescription>최근 6개월간의 매출, 비용, 이익 현황입니다.</CardDescription>
    </CardHeader>
    <CardContent>
      <div
        v-if="isLoading && data.length === 0"
        class="mt-4 flex h-[240px] items-center justify-center text-sm text-muted-foreground"
      >
        데이터를 불러오는 중입니다...
      </div>
      <div v-else class="mt-4 flex h-[240px] items-end gap-2 sm:gap-4">
        <div
          v-for="(item, i) in data"
          :key="i"
          class="group relative flex h-full flex-1 flex-col justify-end gap-1"
        >
          <!-- Tooltip -->
          <div
            class="absolute bottom-full left-1/2 mb-2 hidden -translate-x-1/2 rounded bg-popover px-3 py-2 text-xs shadow-md border border-border group-hover:block z-10 w-max"
          >
            <div class="font-semibold mb-1">{{ item.month }}</div>
            <div class="grid grid-cols-2 gap-x-2 gap-y-1">
              <span class="text-muted-foreground">매출:</span>
              <span class="text-right font-medium">{{ formatAmount(item.revenue) }}</span>
              <span class="text-muted-foreground">비용:</span>
              <span class="text-right font-medium">{{ formatAmount(item.cost) }}</span>
              <span class="text-muted-foreground">이익:</span>
              <span class="text-right font-medium text-primary">{{
                formatAmount(item.profit)
              }}</span>
            </div>
          </div>

          <div class="flex h-full w-full items-end justify-center gap-0.5 sm:gap-1">
            <!-- Revenue Bar -->
            <div
              class="w-full rounded-t-sm bg-orange-500 transition-all hover:opacity-90"
              :style="{ height: `${(item.revenue / maxValue) * 100}%` }"
            ></div>
            <!-- Cost Bar -->
            <div
              class="w-full rounded-t-sm bg-slate-300 transition-all hover:opacity-90"
              :style="{ height: `${(item.cost / maxValue) * 100}%` }"
            ></div>
            <!-- Profit Bar -->
            <div
              class="w-full rounded-t-sm bg-lime-500 transition-all hover:opacity-90"
              :style="{ height: `${(Math.max(0, item.profit) / maxValue) * 100}%` }"
            ></div>
          </div>

          <div class="mt-2 text-center text-xs font-medium text-muted-foreground">
            {{ item.month }}
          </div>
        </div>
      </div>

      <div class="mt-4 flex items-center justify-center gap-4 text-xs text-muted-foreground">
        <div class="flex items-center gap-1.5">
          <div class="h-2.5 w-2.5 rounded-sm bg-orange-500"></div>
          <span>매출</span>
        </div>
        <div class="flex items-center gap-1.5">
          <div class="h-2.5 w-2.5 rounded-sm bg-slate-300"></div>
          <span>비용</span>
        </div>
        <div class="flex items-center gap-1.5">
          <div class="h-2.5 w-2.5 rounded-sm bg-lime-500"></div>
          <span>이익</span>
        </div>
      </div>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { appContainer } from '@/core/di/container';
import DashboardRevenueRepository, {
  type MonthlyRevenueSummaryResponse,
} from '@/features/dashboard/repository/DashboardRevenueRepository';

const repository = appContainer.resolve(DashboardRevenueRepository);
const items = ref<MonthlyRevenueSummaryResponse[]>([]);
const isLoading = ref(false);

const data = computed(() => {
  return items.value.map((item) => {
    const date = new Date(item.targetMonth);
    return {
      month: `${date.getMonth() + 1}월`,
      revenue: item.revenue,
      cost: item.cost,
      profit: item.profit,
    };
  });
});

const maxValue = computed(
  () =>
    (Math.max(...data.value.map((d) => Math.max(d.revenue, d.cost, d.profit))) || 100000000) * 1.1,
);

function formatAmount(value: number) {
  if (value >= 100000000) return `${(value / 100000000).toFixed(1)}억`;
  if (value >= 1000000) return `${(value / 1000000).toFixed(0)}백만`;
  return value.toLocaleString();
}

onMounted(async () => {
  isLoading.value = true;
  try {
    items.value = await repository.fetchMonthlyRevenueSummary();
  } catch (error) {
    console.error('Failed to fetch monthly revenue summary:', error);
  } finally {
    isLoading.value = false;
  }
});
</script>
