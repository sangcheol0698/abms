<template>
  <Card>
    <CardHeader>
      <CardTitle>부서별 재무 현황</CardTitle>
      <CardDescription>{{ selectedYear }}년 누적 부서별 실적입니다.</CardDescription>
    </CardHeader>
    <CardContent>
      <div
        v-if="isLoading && departments.length === 0"
        class="flex h-[240px] items-center justify-center text-sm text-muted-foreground"
      >
        데이터를 불러오는 중입니다...
      </div>
      <div
        v-else-if="departments.length === 0"
        class="flex h-[240px] items-center justify-center text-sm text-muted-foreground"
      >
        표시할 부서별 재무 데이터가 없습니다.
      </div>
      <div v-else class="space-y-6">
        <div
          v-for="(dept, index) in departments"
          :key="index"
          class="flex items-center justify-between"
        >
          <div class="space-y-1">
            <p class="text-sm font-medium leading-none">{{ dept.name }}</p>
            <p class="text-xs text-muted-foreground">
              이익률 {{ dept.profitMargin.toFixed(1) }}%
            </p>
          </div>
          <div class="text-right">
            <div class="font-medium text-sm">{{ formatAmount(dept.revenue) }}</div>
            <div class="text-xs text-muted-foreground">이익 {{ formatAmount(dept.profit) }}</div>
          </div>
        </div>
      </div>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useDashboardDepartmentFinancialQuery } from '@/features/dashboard/queries/useDashboardQueries';
import { formatCurrencyAmount } from '@/features/dashboard/utils/format';

const props = withDefaults(
  defineProps<{
    selectedYear?: number;
  }>(),
  {
    selectedYear: new Date().getFullYear(),
  },
);

const departmentFinancialQuery = useDashboardDepartmentFinancialQuery(computed(() => props.selectedYear));
const isLoading = computed(() => departmentFinancialQuery.isLoading.value);
const departments = computed(() =>
  (departmentFinancialQuery.data.value ?? []).map((item) => ({
    name: item.departmentName,
    revenue: item.revenue,
    profit: item.profit,
    profitMargin: item.profitMargin,
  })),
);

function formatAmount(value: number) {
  return formatCurrencyAmount(value);
}
</script>
