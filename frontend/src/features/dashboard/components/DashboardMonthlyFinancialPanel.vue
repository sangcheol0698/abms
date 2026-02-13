<template>
  <Card class="shadow-none">
    <CardHeader>
      <CardTitle>월별 재무 추이</CardTitle>
      <CardDescription>최근 6개월간의 매출, 비용, 이익 현황입니다.</CardDescription>
    </CardHeader>
    <CardContent>
      <div class="mt-4 flex h-[240px] items-end gap-2 sm:gap-4">
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
import { computed } from 'vue';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

const data = [
  { month: '1월', revenue: 120000000, cost: 90000000, profit: 30000000 },
  { month: '2월', revenue: 150000000, cost: 110000000, profit: 40000000 },
  { month: '3월', revenue: 180000000, cost: 130000000, profit: 50000000 },
  { month: '4월', revenue: 140000000, cost: 100000000, profit: 40000000 },
  { month: '5월', revenue: 210000000, cost: 150000000, profit: 60000000 },
  { month: '6월', revenue: 190000000, cost: 140000000, profit: 50000000 },
];

const maxValue = computed(
  () => Math.max(...data.map((d) => Math.max(d.revenue, d.cost, d.profit))) * 1.1,
);

function formatAmount(value: number) {
  if (value >= 100000000) return `${(value / 100000000).toFixed(1)}억`;
  if (value >= 1000000) return `${(value / 1000000).toFixed(0)}백만`;
  return value.toLocaleString();
}
</script>
