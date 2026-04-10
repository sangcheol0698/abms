import { inject, injectable } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';
import type {
  DashboardDepartmentFinancialItem,
  DashboardEmployeeOverview,
  DashboardProjectOverview,
  DashboardSummary,
  DashboardUpcomingDeadlineItem,
} from '@/features/dashboard/models/dashboard';
import { getCurrentYear } from '@/features/dashboard/utils/format';

@injectable()
export class DashboardRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  async fetchSummary(year = getCurrentYear()): Promise<DashboardSummary> {
    const response = await this.httpRepository.get<DashboardSummary>({
      path: '/api/dashboards/summary',
      params: { year: String(year) },
    });

    return {
      totalEmployeesCount: Number(response?.totalEmployeesCount ?? 0),
      activeProjectsCount: Number(response?.activeProjectsCount ?? 0),
      completedProjectsCount: Number(response?.completedProjectsCount ?? 0),
      newEmployeesCount: Number(response?.newEmployeesCount ?? 0),
      yearRevenue: Number(response?.yearRevenue ?? 0),
      yearProfit: Number(response?.yearProfit ?? 0),
    };
  }

  async fetchUpcomingDeadlines(limit = 5): Promise<DashboardUpcomingDeadlineItem[]> {
    const response = await this.httpRepository.get<DashboardUpcomingDeadlineItem[]>({
      path: '/api/dashboards/upcoming-deadlines',
      params: { limit: String(limit) },
    });

    return Array.isArray(response)
      ? response.map((item) => ({
          projectId: Number(item?.projectId ?? 0),
          name: String(item?.name ?? ''),
          partyName: String(item?.partyName ?? ''),
          status: String(item?.status ?? ''),
          statusDescription: String(item?.statusDescription ?? ''),
          endDate: String(item?.endDate ?? ''),
          daysLeft: Number(item?.daysLeft ?? 0),
        }))
      : [];
  }

  async fetchDepartmentFinancials(
    year = getCurrentYear(),
    limit = 5,
  ): Promise<DashboardDepartmentFinancialItem[]> {
    const response = await this.httpRepository.get<DashboardDepartmentFinancialItem[]>({
      path: '/api/dashboards/department-financials',
      params: {
        year: String(year),
        limit: String(limit),
      },
    });

    return Array.isArray(response)
      ? response.map((item) => ({
          departmentId: Number(item?.departmentId ?? 0),
          departmentName: String(item?.departmentName ?? ''),
          revenue: Number(item?.revenue ?? 0),
          profit: Number(item?.profit ?? 0),
          profitMargin: Number(item?.profitMargin ?? 0),
        }))
      : [];
  }

  async fetchProjectOverview(year = getCurrentYear()): Promise<DashboardProjectOverview> {
    const response = await this.httpRepository.get<DashboardProjectOverview>({
      path: '/api/dashboards/project-overview',
      params: { year: String(year) },
    });

    return {
      totalCount: Number(response?.totalCount ?? 0),
      scheduledCount: Number(response?.scheduledCount ?? 0),
      inProgressCount: Number(response?.inProgressCount ?? 0),
      completedCount: Number(response?.completedCount ?? 0),
      onHoldCount: Number(response?.onHoldCount ?? 0),
      cancelledCount: Number(response?.cancelledCount ?? 0),
    };
  }

  async fetchEmployeeOverview(year = getCurrentYear()): Promise<DashboardEmployeeOverview> {
    const response = await this.httpRepository.get<DashboardEmployeeOverview>({
      path: '/api/dashboards/employee-overview',
      params: { year: String(year) },
    });

    return {
      totalCount: Number(response?.totalCount ?? 0),
      fullTimeCount: Number(response?.fullTimeCount ?? 0),
      freelancerCount: Number(response?.freelancerCount ?? 0),
      outsourcingCount: Number(response?.outsourcingCount ?? 0),
      partTimeCount: Number(response?.partTimeCount ?? 0),
    };
  }
}
