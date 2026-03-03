import { inject, injectable } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';

export interface DashboardSummary {
  totalEmployeesCount: number;
  activeProjectsCount: number;
  newEmployeesCount: number;
  onLeaveEmployeesCount: number;
}

@injectable()
export class DashboardRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  async fetchSummary(): Promise<DashboardSummary> {
    return this.httpRepository.get<DashboardSummary>({
      path: '/api/dashboards/summary',
    });
  }
}
