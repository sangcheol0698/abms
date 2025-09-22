import { inject, singleton } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';
import type { EmployeeCreatePayload, EmployeeSummary } from '@/features/employee/models/employee';
import { mapEmployeeSummary } from '@/features/employee/models/employee';
import PageResponse from '@/core/common/PageResponse';
import {
    type EmployeeListItem,
    type EmployeeSearchParams,
    mapEmployeeListItem,
} from '@/features/employee/models/employeeListItem';

@singleton()
export default class EmployeeRepository {
    constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  async findById(employeeId: string): Promise<EmployeeSummary> {
    const response = await this.httpRepository.get({ path: `/api/employees/${employeeId}` });
    return mapEmployeeSummary(response);
  }

    async create(payload: EmployeeCreatePayload): Promise<EmployeeSummary> {
        const response = await this.httpRepository.post({ path: '/api/employees', data: payload });
        return mapEmployeeSummary(response);
    }

    async search(params: EmployeeSearchParams): Promise<PageResponse<EmployeeListItem>> {
        const query: Record<string, unknown> = {
            page: Math.max(params.page - 1, 0),
            size: params.size,
        };

        if (params.name) {
            query.name = params.name;
        }

        if (params.statuses?.length) {
            query.statuses = params.statuses.join(',');
        }

        if (params.types?.length) {
            query.types = params.types.join(',');
        }

        if (params.grades?.length) {
            query.grades = params.grades.join(',');
        }

        if (params.positions?.length) {
            query.positions = params.positions.join(',');
        }

        if (params.departmentIds?.length) {
            query.departmentIds = params.departmentIds.join(',');
        }

        if (params.sort) {
            query.sort = params.sort;
        }

        const response = await this.httpRepository.get({ path: '/api/employees', params: query });
        return PageResponse.fromPage(response, mapEmployeeListItem);
    }
}
