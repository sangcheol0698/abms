import { inject, injectable } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';
import type { DashboardMonthlyFinancialItem } from '@/features/dashboard/models/dashboard';
import { getCurrentYear } from '@/features/dashboard/utils/format';

@injectable()
export default class DashboardRevenueRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  async fetchMonthlyRevenueSummary(year = getCurrentYear()): Promise<DashboardMonthlyFinancialItem[]> {
    return this.httpRepository.get<DashboardMonthlyFinancialItem[]>({
      path: '/api/dashboards/monthly-financials',
      params: { year: String(year) },
    });
  }
}
