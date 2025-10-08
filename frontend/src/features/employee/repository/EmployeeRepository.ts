import { inject, singleton } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';
import type { EmployeeCreatePayload, EmployeeSummary } from '@/features/employee/models/employee';
import type { EmployeeFilterOption } from '@/features/employee/models/employeeFilters';
import { mapEmployeeSummary } from '@/features/employee/models/employee';
import PageResponse from '@/core/common/PageResponse';
import {
  type EmployeeListItem,
  type EmployeeSearchParams,
  mapEmployeeListItem,
} from '@/features/employee/models/employeeListItem';

interface EmployeeStatusResponse {
  name: string;
  description?: string;
}

interface EmployeeTypeResponse {
  name: string;
  description?: string;
}

interface EmployeeGradeResponse {
  name: string;
  description?: string;
  level?: number;
}

interface EmployeePositionResponse {
  name: string;
  description?: string;
  rank?: number;
}

function toFilterOption(response: { name: string; description?: string }): EmployeeFilterOption {
  return {
    value: response.name,
    label: response.description ?? response.name,
  };
}

@singleton()
export class EmployeeRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  async findById(employeeId: string): Promise<EmployeeSummary> {
    const response = await this.httpRepository.get({ path: `/api/employees/${employeeId}` });
    return mapEmployeeSummary(response);
  }

  async create(payload: EmployeeCreatePayload): Promise<EmployeeSummary> {
    const response = await this.httpRepository.post({ path: '/api/employees', data: payload });
    return mapEmployeeSummary(response);
  }

  async update(employeeId: string, payload: EmployeeCreatePayload): Promise<EmployeeSummary> {
    const response = await this.httpRepository.put({
      path: `/api/employees/${employeeId}`,
      data: payload,
    });
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

  async fetchStatuses(): Promise<EmployeeFilterOption[]> {
    const response = await this.httpRepository.get<EmployeeStatusResponse[]>({
      path: '/api/employees/statuses',
    });
    return Array.isArray(response)
      ? response.map((item) => toFilterOption(item))
      : [];
  }

  async fetchTypes(): Promise<EmployeeFilterOption[]> {
    const response = await this.httpRepository.get<EmployeeTypeResponse[]>({
      path: '/api/employees/types',
    });
    return Array.isArray(response)
      ? response.map((item) => toFilterOption(item))
      : [];
  }

  async fetchGrades(): Promise<EmployeeFilterOption[]> {
    const response = await this.httpRepository.get<EmployeeGradeResponse[]>({
      path: '/api/employees/grades',
    });
    if (!Array.isArray(response)) {
      return [];
    }
    return response
      .slice()
      .sort((a, b) => (a.level ?? Number.MAX_SAFE_INTEGER) - (b.level ?? Number.MAX_SAFE_INTEGER))
      .map((item) => ({
        label: item.description ?? item.name,
        value: item.name,
      }));
  }

  async fetchPositions(): Promise<EmployeeFilterOption[]> {
    const response = await this.httpRepository.get<EmployeePositionResponse[]>({
      path: '/api/employees/positions',
    });
    if (!Array.isArray(response)) {
      return [];
    }
    return response
      .slice()
      .sort((a, b) => (a.rank ?? Number.MAX_SAFE_INTEGER) - (b.rank ?? Number.MAX_SAFE_INTEGER))
      .map((item) => ({
        label: item.description ?? item.name,
        value: item.name,
      }));
  }
}
