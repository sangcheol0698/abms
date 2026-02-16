import { injectable } from 'tsyringe';
import axios from 'axios';

export interface MonthlyRevenueSummaryResponse {
  targetMonth: string;
  revenue: number;
  cost: number;
  profit: number;
}

@injectable()
export default class DashboardRevenueRepository {
  private baseUrl = import.meta.env.VITE_API_BASE_URL || '';

  async fetchMonthlyRevenueSummary(): Promise<MonthlyRevenueSummaryResponse[]> {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const yearMonth = `${year}${month}`;

    const { data } = await axios.get<MonthlyRevenueSummaryResponse[]>(
      `${this.baseUrl}/api/monthlyRevenueSummary/sixMonthTrend`,
      { params: { yearMonth } },
    );
    return data;
  }
}
