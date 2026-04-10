<template>
  <section class="space-y-6">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-semibold tracking-tight text-foreground">대시보드</h1>
        <p class="text-sm text-muted-foreground">경영 지표와 운영 현황을 한 화면에서 확인합니다.</p>
      </div>
      <div class="flex items-center gap-2">
        <span class="text-sm font-medium text-muted-foreground">기준 연도</span>
        <Select v-model="selectedYearValue">
          <SelectTrigger class="w-[110px]">
            <SelectValue placeholder="연도 선택" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem v-for="year in yearOptions" :key="year" :value="String(year)">
              {{ year }}년
            </SelectItem>
          </SelectContent>
        </Select>
      </div>
    </div>

    <DashboardSummaryCards
      :summary="summary"
      :is-loading="isSummaryLoading"
      :selected-year="selectedYear"
    />

    <DashboardMonthlyFinancialPanel :selected-year="selectedYear" />

    <div class="grid gap-6 md:grid-cols-2">
      <DashboardDepartmentFinancialPanel :selected-year="selectedYear" />
      <DashboardProjectStatusPanel :selected-year="selectedYear" />
    </div>

    <div class="grid gap-6 md:grid-cols-2">
      <DashboardEmployeeDistributionPanel :selected-year="selectedYear" />
      <DashboardUpcomingDeadlinesPanel />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import DashboardMonthlyFinancialPanel from '@/features/dashboard/components/DashboardMonthlyFinancialPanel.vue';
import DashboardDepartmentFinancialPanel from '@/features/dashboard/components/DashboardDepartmentFinancialPanel.vue';
import DashboardProjectStatusPanel from '@/features/dashboard/components/DashboardProjectStatusPanel.vue';
import DashboardUpcomingDeadlinesPanel from '@/features/dashboard/components/DashboardUpcomingDeadlinesPanel.vue';
import DashboardEmployeeDistributionPanel from '@/features/dashboard/components/DashboardEmployeeDistributionPanel.vue';
import DashboardSummaryCards from '@/features/dashboard/components/DashboardSummaryCards.vue';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { useDashboardSummaryQuery } from '@/features/dashboard/queries/useDashboardQueries';
import { getCurrentYear } from '@/features/dashboard/utils/format';

const currentYear = getCurrentYear();
const selectedYearValue = ref(String(currentYear));
const selectedYear = computed(() => Number(selectedYearValue.value));
const yearOptions = computed(() =>
  Array.from({ length: 5 }, (_, index) => currentYear - index),
);
const summaryQuery = useDashboardSummaryQuery(computed(() => selectedYear.value));

const summary = computed(() => summaryQuery.data.value ?? null);
const isSummaryLoading = computed(() => summaryQuery.isLoading.value);
</script>
