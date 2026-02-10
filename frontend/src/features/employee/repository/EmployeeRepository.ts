import { inject, singleton } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';
import type { EmployeeCreatePayload, EmployeeSummary } from '@/features/employee/models/employee';
import type { EmployeeFilterOption } from '@/features/employee/models/employeeFilters';
import {
  getEmployeeGradeOptions,
  getEmployeePositionOptions,
  getEmployeeStatusOptions,
  getEmployeeTypeOptions,
  setEmployeeGradeOptions,
  setEmployeePositionOptions,
  setEmployeeStatusOptions,
  setEmployeeTypeOptions,
} from '@/features/employee/models/employeeFilters';
import { mapEmployeeSummary } from '@/features/employee/models/employee';
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
import { type EnumResponse, PageResponse, toSelectOption } from '@/core/api';

// 기존 import 문 아래에 추가
import type { PositionHistory } from '@/features/employee/models/positionHistory';

/**
 * 상태(Status) API 응답 타입
 * @see GET /api/employees/statuses
 */
type EmployeeStatusResponse = EnumResponse;

/**
 * 타입(Type) API 응답 타입
 */
type EmployeeTypeResponse = EnumResponse;

/**
 * 등급(Grade) API 응답 타입
 * level: 정렬을 위한 순서 값 (낮을수록 높은 등급)
 */
interface EmployeeGradeResponse extends EnumResponse {
  level?: number;
}

/**
 * 직급(Position) API 응답 타입
 * rank: 정렬을 위한 순서 값 (낮을수록 높은 직급)
 */
interface EmployeePositionResponse extends EnumResponse {
  rank?: number;
}

/**
 * 아바타 API 응답 타입
 * @see GET /api/employees/avatars
 */
type EmployeeAvatarResponse = EnumResponse;

function buildRequestParams(params: EmployeeSearchParams): Record<string, string> {
  const query: Record<string, string> = {
    page: Math.max(params.page - 1, 0).toString(),
    size: params.size.toString(),
  };

  if (params.name) {
    query.name = params.name;
  }

  if (params.sort) {
    query.sort = params.sort;
  }

  const arrayFields: (keyof EmployeeSearchParams)[] = [
    'statuses',
    'types',
    'grades',
    'positions',
    'departmentIds',
  ];

  arrayFields.forEach((field) => {
    const value = params[field];
    if (Array.isArray(value) && value.length > 0) {
      query[field] = value.join(',');
    }
  });

  return query;
}

@singleton()
export class EmployeeRepository {
  private filterOptionsPromise: Promise<void> | null = null;
  private statusOptionsLoaded = false;
  private typeOptionsLoaded = false;
  private gradeOptionsLoaded = false;
  private positionOptionsLoaded = false;

  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) { }

  private hasFilterOptionsLoaded(): boolean {
    return (
      (this.statusOptionsLoaded || getEmployeeStatusOptions().length > 0) &&
      (this.typeOptionsLoaded || getEmployeeTypeOptions().length > 0) &&
      (this.gradeOptionsLoaded || getEmployeeGradeOptions().length > 0) &&
      (this.positionOptionsLoaded || getEmployeePositionOptions().length > 0)
    );
  }

  private async ensureFilterOptionsLoaded(): Promise<void> {
    if (this.hasFilterOptionsLoaded()) {
      return;
    }

    if (!this.filterOptionsPromise) {
      this.filterOptionsPromise = (async () => {
        const [statuses, types, grades, positions] = await Promise.all([
          this.requestStatusOptions(),
          this.requestTypeOptions(),
          this.requestGradeOptions(),
          this.requestPositionOptions(),
        ]);

        setEmployeeStatusOptions(statuses);
        setEmployeeTypeOptions(types);
        setEmployeeGradeOptions(grades);
        setEmployeePositionOptions(positions);

        this.statusOptionsLoaded = true;
        this.typeOptionsLoaded = true;
        this.gradeOptionsLoaded = true;
        this.positionOptionsLoaded = true;
      })().finally(() => {
        this.filterOptionsPromise = null;
      });
    }

    await this.filterOptionsPromise;
  }

  private async requestStatusOptions(): Promise<EmployeeFilterOption[]> {
    const response = await this.httpRepository.get<EmployeeStatusResponse[]>({
      path: '/api/employees/statuses',
    });
    return Array.isArray(response) ? response.map(toSelectOption) : [];
  }

  private async requestTypeOptions(): Promise<EmployeeFilterOption[]> {
    const response = await this.httpRepository.get<EmployeeTypeResponse[]>({
      path: '/api/employees/types',
    });
    return Array.isArray(response) ? response.map(toSelectOption) : [];
  }

  private async requestGradeOptions(): Promise<EmployeeFilterOption[]> {
    const response = await this.httpRepository.get<EmployeeGradeResponse[]>({
      path: '/api/employees/grades',
    });
    if (!Array.isArray(response)) {
      return [];
    }
    // level을 기준으로 정렬 후 SelectOption으로 변환
    return response
      .slice()
      .sort((a, b) => (a.level ?? Number.MAX_SAFE_INTEGER) - (b.level ?? Number.MAX_SAFE_INTEGER))
      .map(toSelectOption);
  }

  private async requestPositionOptions(): Promise<EmployeeFilterOption[]> {
    const response = await this.httpRepository.get<EmployeePositionResponse[]>({
      path: '/api/employees/positions',
    });
    if (!Array.isArray(response)) {
      return [];
    }
    // rank를 기준으로 정렬 후 SelectOption으로 변환
    return response
      .slice()
      .sort((a, b) => (a.rank ?? Number.MAX_SAFE_INTEGER) - (b.rank ?? Number.MAX_SAFE_INTEGER))
      .map(toSelectOption);
  }

  async findById(employeeId: number): Promise<EmployeeSummary> {
    await this.ensureFilterOptionsLoaded();
    const response = await this.httpRepository.get({ path: `/api/employees/${employeeId}` });
    return mapEmployeeSummary(response);
  }

  async create(payload: EmployeeCreatePayload): Promise<EmployeeSummary> {
    await this.ensureFilterOptionsLoaded();
    const response = await this.httpRepository.post({ path: '/api/employees', data: payload });
    return mapEmployeeSummary(response);
  }

  async update(employeeId: number, payload: EmployeeCreatePayload): Promise<EmployeeSummary> {
    await this.ensureFilterOptionsLoaded();
    const response = await this.httpRepository.put({
      path: `/api/employees/${employeeId}`,
      data: payload,
    });
    return mapEmployeeSummary(response);
  }

  async delete(employeeId: number): Promise<void> {
    await this.httpRepository.delete({
      path: `/api/employees/${employeeId}`,
    });
  }

  async restore(employeeId: number): Promise<void> {
    await this.httpRepository.patch({
      path: `/api/employees/${employeeId}/restore`,
    });
  }

  async takeLeave(employeeId: number): Promise<void> {
    await this.httpRepository.patch({
      path: `/api/employees/${employeeId}/take-leave`,
    });
  }

  async activate(employeeId: number): Promise<void> {
    await this.httpRepository.patch({
      path: `/api/employees/${employeeId}/activate`,
    });
  }

  async resign(employeeId: number, resignationDate: string): Promise<void> {
    await this.httpRepository.patch({
      path: `/api/employees/${employeeId}/resign`,
      params: { resignationDate },
    });
  }

  async promote(employeeId: number, position: string, grade?: string): Promise<void> {
    await this.httpRepository.patch({
      path: `/api/employees/${employeeId}/promote`,
      data: { position, grade },
    });
  }

  async search(params: EmployeeSearchParams): Promise<PageResponse<EmployeeListItem>> {
    await this.ensureFilterOptionsLoaded();
    const response = await this.httpRepository.get({
      path: '/api/employees',
      params: buildRequestParams(params),
    });
    return PageResponse.fromPage(response, mapEmployeeListItem);
  }

  async fetchStatuses(): Promise<EmployeeFilterOption[]> {
    const statuses = await this.requestStatusOptions();
    setEmployeeStatusOptions(statuses);
    this.statusOptionsLoaded = true;
    return statuses;
  }

  async fetchTypes(): Promise<EmployeeFilterOption[]> {
    const types = await this.requestTypeOptions();
    setEmployeeTypeOptions(types);
    this.typeOptionsLoaded = true;
    return types;
  }

  async fetchGrades(): Promise<EmployeeFilterOption[]> {
    const grades = await this.requestGradeOptions();
    setEmployeeGradeOptions(grades);
    this.gradeOptionsLoaded = true;
    return grades;
  }

  async fetchPositions(): Promise<EmployeeFilterOption[]> {
    const positions = await this.requestPositionOptions();
    setEmployeePositionOptions(positions);
    this.positionOptionsLoaded = true;
    return positions;
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
        // 백엔드에서 description이 있으면 사용, 없으면 기본 label 사용
        const label =
          item.description && item.description.trim().length > 0 ? item.description : option.label;
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

  /**
   * 직원의 직급 이력을 조회합니다.
   * GET /api/positionHistory/{employeeId}
   */
  async fetchPositionHistory(employeeId: number): Promise<PositionHistory[]> {
    return this.httpRepository.get({
      path: `/api/positionHistory/${employeeId}`,
    });
  }
}
