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
import type { EmployeeAvatarOption } from '@/features/employee/constants/avatars';
import {
  getEmployeeAvatarOption,
  getEmployeeAvatarOptions,
  isEmployeeAvatarCode,
} from '@/features/employee/constants/avatars';
import {
  createExcelFormData,
  downloadBlob,
  extractFilenameFromResponse,
  generateExcelFilename,
} from '@/core/utils/excel';

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

interface EmployeeAvatarResponse {
  code: string;
  displayName?: string;
}

function toFilterOption(response: { name: string; description?: string }): EmployeeFilterOption {
  return {
    value: response.name,
    label: response.description ?? response.name,
  };
}

function buildRequestParams(params: EmployeeSearchParams): Record<string, unknown> {
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

  return query;
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

  async delete(employeeId: string): Promise<void> {
    await this.httpRepository.delete({
      path: `/api/employees/${employeeId}`,
    });
  }

  async restore(employeeId: string): Promise<void> {
    await this.httpRepository.patch({
      path: `/api/employees/${employeeId}/restore`,
    });
  }

  async search(params: EmployeeSearchParams): Promise<PageResponse<EmployeeListItem>> {
    const response = await this.httpRepository.get({
      path: '/api/employees',
      params: buildRequestParams(params),
    });
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

  async fetchAvatars(): Promise<EmployeeAvatarOption[]> {
    const response = await this.httpRepository.get<EmployeeAvatarResponse[]>({
      path: '/api/employees/avatars',
    });

    if (!Array.isArray(response)) {
      return getEmployeeAvatarOptions();
    }

    const merged = response
      .map((item) => {
        if (!isEmployeeAvatarCode(item.code)) {
          return null;
        }
        const option = getEmployeeAvatarOption(item.code);
        const label = item.displayName && item.displayName.trim().length > 0 ? item.displayName : option.label;
        return { ...option, label };
      })
      .filter((option): option is EmployeeAvatarOption => option !== null);

    const deduped = new Map<string, EmployeeAvatarOption>();
    for (const option of merged) {
      deduped.set(option.code, option);
    }

    for (const fallback of getEmployeeAvatarOptions()) {
      if (!deduped.has(fallback.code)) {
        deduped.set(fallback.code, fallback);
      }
    }

    return Array.from(deduped.values());
  }

  async downloadExcel(params: EmployeeSearchParams): Promise<void> {
    const response = await this.httpRepository.download({
      path: '/api/employees/excel/download',
      params: buildRequestParams(params),
    });

    if (!response.ok) {
      throw new Error(`엑셀 다운로드에 실패했습니다. (${response.status})`);
    }

    const filename = extractFilenameFromResponse(response, generateExcelFilename('employees'));
    const blob = await response.blob();
    downloadBlob(blob, filename);
  }

  async downloadSampleExcel(): Promise<void> {
    const response = await this.httpRepository.download({
      path: '/api/employees/excel/sample',
    });

    if (!response.ok) {
      throw new Error(`샘플 파일 다운로드에 실패했습니다. (${response.status})`);
    }

    const filename = extractFilenameFromResponse(
      response,
      generateExcelFilename('employees_sample'),
    );
    const blob = await response.blob();
    downloadBlob(blob, filename);
  }

  async uploadExcel(file: File, onProgress?: (progress: number) => void): Promise<void> {
    const formData = createExcelFormData(file);
    await this.httpRepository.upload({
      path: '/api/employees/excel/upload',
      data: formData,
      onProgress,
    });
  }
}
