import { injectable } from 'tsyringe';
import axios from 'axios';

export interface DashboardSummary {
  totalEmployeesCount: number;
  activeProjectsCount: number;
  newEmployeesCount: number;
  onLeaveEmployeesCount: number;
}

@injectable()
export class DashboardRepository {
  private baseUrl = import.meta.env.VITE_API_BASE_URL || '';

  async fetchSummary(): Promise<DashboardSummary> {
    // 백엔드 API 호출 (/api/dashboards/summary)
    const { data } = await axios.get<DashboardSummary>(`${this.baseUrl}/api/dashboards/summary`);
    return data;
  }
}
