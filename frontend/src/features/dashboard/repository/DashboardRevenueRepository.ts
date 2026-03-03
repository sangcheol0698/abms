import { inject, injectable } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';

export interface MonthlyRevenueSummaryResponse {
  targetMonth: string;
  revenue: number;
  cost: number;
  profit: number;
}

@injectable()
export default class DashboardRevenueRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  async fetchMonthlyRevenueSummary(): Promise<MonthlyRevenueSummaryResponse[]> {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const yearMonth = `${year}${month}`;

    return this.httpRepository.get<MonthlyRevenueSummaryResponse[]>({
      path: '/api/monthlyRevenueSummary/sixMonthTrend',
      params: { yearMonth },
    });
  }
}
