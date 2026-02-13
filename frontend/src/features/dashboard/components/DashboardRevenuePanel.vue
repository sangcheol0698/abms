<template>
  <Card class="shadow-none">
    <CardHeader>
      <CardTitle>월별 매출 추이</CardTitle>
      <CardDescription>최근 6개월간의 매출 현황입니다.</CardDescription>
    </CardHeader>
    <CardContent>
      <div class="mt-4 flex h-[200px] items-end gap-4">
        <div
          v-for="(item, i) in data"
          :key="i"
          class="group relative flex h-full flex-1 flex-col justify-end"
        >
          <div
            class="w-full rounded-t-md bg-primary/90 transition-all hover:bg-primary"
            :style="{ height: `${(item.amount / maxAmount) * 100}%` }"
          >
            <div
              class="absolute bottom-full left-1/2 mb-2 hidden -translate-x-1/2 rounded bg-popover px-2 py-1 text-xs font-medium text-popover-foreground shadow-md group-hover:block whitespace-nowrap"
            >
              {{ formatAmount(item.amount) }}
            </div>
          </div>
          <div class="mt-2 text-center text-xs font-medium text-muted-foreground">
            {{ item.month }}
          </div>
        </div>
      </div>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

const data = [
  { month: '1월', amount: 120000000 },
  { month: '2월', amount: 150000000 },
  { month: '3월', amount: 180000000 },
  { month: '4월', amount: 140000000 },
  { month: '5월', amount: 210000000 },
  { month: '6월', amount: 190000000 },
];

const maxAmount = computed(() => Math.max(...data.map((d) => d.amount)) * 1.2);

function formatAmount(value: number) {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW',
    maximumFractionDigits: 0,
  }).format(value);
}
</script>
