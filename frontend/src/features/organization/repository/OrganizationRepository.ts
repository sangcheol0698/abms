import { inject, singleton } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';
import {
  normalizeOrganizationChartResponse,
  mapOrganizationDepartmentDetail,
  type OrganizationChartNode,
  type OrganizationDepartmentDetail,
} from '@/features/organization/models/organization';

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
}
