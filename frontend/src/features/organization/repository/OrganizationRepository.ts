import { inject, singleton } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';
import {
  normalizeOrganizationChartResponse,
  mapOrganizationDepartmentDetail,
  type OrganizationChartNode,
  type OrganizationDepartmentDetail,
} from '@/features/organization/models/organization';
import { PageResponse } from '@/core/api';
import type { EmployeeListItem } from '@/features/employee/models/employeeListItem';
import { mapEmployeeListItem } from '@/features/employee/models/employeeListItem';

@singleton()
export default class OrganizationRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  async fetchOrganizationChart(): Promise<OrganizationChartNode[]> {
    const response = await this.httpRepository.get({ path: '/api/departments/organization-chart' });
    return normalizeOrganizationChartResponse(response);
  }

  async fetchDepartmentDetail(departmentId: string): Promise<OrganizationDepartmentDetail> {
    // TODO: API 경로 및 응답 스키마 확정 후 수정이 필요합니다.
    const response = await this.httpRepository.get({
      path: `/api/departments/${departmentId}`,
    });
    return mapOrganizationDepartmentDetail(response);
  }

  async fetchDepartmentEmployees(
    departmentId: string,
    params: { page: number; size: number; name?: string; sort?: string },
  ): Promise<PageResponse<EmployeeListItem>> {
    const queryParams: Record<string, string> = {
      page: Math.max(params.page - 1, 0).toString(),
      size: params.size.toString(),
    };

    if (params.name) {
      queryParams.name = params.name;
    }

    if (params.sort) {
      queryParams.sort = params.sort;
    }

    const response = await this.httpRepository.get({
      path: `/api/departments/${departmentId}/employees`,
      params: queryParams,
    });
    return PageResponse.fromPage(response, mapEmployeeListItem);
  }

  async assignTeamLeader(departmentId: string, employeeId: string): Promise<void> {
    await this.httpRepository.post({
      path: `/api/departments/${departmentId}/assign-team-leader`,
      data: { leaderEmployeeId: employeeId },
    });
  }
}
