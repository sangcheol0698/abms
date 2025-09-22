import { inject, singleton } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';
import {
  normalizeOrganizationChartResponse,
  castToOrganizationWithEmployees,
  type OrganizationChartNode,
  type OrganizationChartWithEmployeesNode,
} from '@/features/organization/models/organization';

@singleton()
export default class OrganizationRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  async fetchOrganizationChart(): Promise<OrganizationChartNode[]> {
    const response = await this.httpRepository.get({ path: '/api/departments/organization-chart' });
    return normalizeOrganizationChartResponse(response);
  }

  async fetchOrganizationChartWithEmployees(): Promise<OrganizationChartWithEmployeesNode[]> {
    const response = await this.httpRepository.get({
      path: '/api/departments/organization-chart/employees',
    });
    const normalized = normalizeOrganizationChartResponse(response);
    return castToOrganizationWithEmployees(normalized);
  }
}
