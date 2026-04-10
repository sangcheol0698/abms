import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery } from '@tanstack/vue-query';
import { appContainer } from '@/core/di/container';
import { dashboardKeys } from '@/core/query';
import {
  DashboardRepository,
} from '@/features/dashboard/repository/DashboardRepository';
import DashboardRevenueRepository from '@/features/dashboard/repository/DashboardRevenueRepository';
import { getCurrentYear } from '@/features/dashboard/utils/format';

export function useDashboardSummaryQuery(yearRef: MaybeRefOrGetter<number> = getCurrentYear()) {
  const repository = appContainer.resolve(DashboardRepository);
  const year = computed(() => Number(toValue(yearRef) ?? getCurrentYear()));

  return useQuery({
    queryKey: computed(() => dashboardKeys.summary(year.value)),
    queryFn: () => repository.fetchSummary(year.value),
  });
}

export function useDashboardMonthlyRevenueSummaryQuery(
  yearRef: MaybeRefOrGetter<number> = getCurrentYear(),
) {
  const repository = appContainer.resolve(DashboardRevenueRepository);
  const year = computed(() => Number(toValue(yearRef) ?? getCurrentYear()));

  return useQuery({
    queryKey: computed(() => dashboardKeys.monthlyRevenueSummary(year.value)),
    queryFn: () => repository.fetchMonthlyRevenueSummary(year.value),
  });
}

export function useDashboardEmployeeOverviewQuery(
  yearRef: MaybeRefOrGetter<number> = getCurrentYear(),
) {
  const repository = appContainer.resolve(DashboardRepository);
  const year = computed(() => Number(toValue(yearRef) ?? getCurrentYear()));

  return useQuery({
    queryKey: computed(() => dashboardKeys.employeeOverview(year.value)),
    queryFn: () => repository.fetchEmployeeOverview(year.value),
  });
}

export function useDashboardProjectOverviewQuery(
  yearRef: MaybeRefOrGetter<number> = getCurrentYear(),
) {
  const repository = appContainer.resolve(DashboardRepository);
  const year = computed(() => Number(toValue(yearRef) ?? getCurrentYear()));

  return useQuery({
    queryKey: computed(() => dashboardKeys.projectOverview(year.value)),
    queryFn: () => repository.fetchProjectOverview(year.value),
  });
}

export function useDashboardUpcomingDeadlinesQuery(limit = 5) {
  const repository = appContainer.resolve(DashboardRepository);

  return useQuery({
    queryKey: dashboardKeys.upcomingDeadlines(limit),
    queryFn: () => repository.fetchUpcomingDeadlines(limit),
  });
}

export function useDashboardDepartmentFinancialQuery(
  yearRef: MaybeRefOrGetter<number> = getCurrentYear(),
  limit = 5,
) {
  const repository = appContainer.resolve(DashboardRepository);
  const year = computed(() => Number(toValue(yearRef) ?? getCurrentYear()));

  return useQuery({
    queryKey: computed(() => dashboardKeys.departmentFinancials(year.value, limit)),
    queryFn: () => repository.fetchDepartmentFinancials(year.value, limit),
  });
}
