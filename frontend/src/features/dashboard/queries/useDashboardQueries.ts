import { useQuery } from '@tanstack/vue-query';
import { appContainer } from '@/core/di/container';
import { dashboardKeys } from '@/core/query';
import {
  DashboardRepository,
} from '@/features/dashboard/repository/DashboardRepository';
import DashboardRevenueRepository from '@/features/dashboard/repository/DashboardRevenueRepository';

export function useDashboardSummaryQuery() {
  const repository = appContainer.resolve(DashboardRepository);

  return useQuery({
    queryKey: dashboardKeys.summary(),
    queryFn: () => repository.fetchSummary(),
  });
}

export function useDashboardMonthlyRevenueSummaryQuery() {
  const repository = appContainer.resolve(DashboardRevenueRepository);

  return useQuery({
    queryKey: dashboardKeys.monthlyRevenueSummary(),
    queryFn: () => repository.fetchMonthlyRevenueSummary(),
  });
}
