import { inject, singleton } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';
import { PageResponse } from '@/core/api';
import type {
  ProjectListItem,
  ProjectSearchParams,
} from '@/features/project/models/projectListItem';
import { mapProjectListItem } from '@/features/project/models/projectListItem';
import type {
  ProjectDetail,
  ProjectCreateData,
  ProjectUpdateData,
} from '@/features/project/models/projectDetail';
import {
  downloadBlob,
  extractFilenameFromResponse,
  generateExcelFilename,
} from '@/core/utils/excel';
import { mapProjectDetail } from '@/features/project/models/projectDetail';

export interface ProjectOverviewSummary {
  totalCount: number;
  scheduledCount: number;
  inProgressCount: number;
  completedCount: number;
  onHoldCount: number;
  cancelledCount: number;
  totalContractAmount: number;
}

export interface ProjectOverviewSummaryParams {
  name?: string;
  statuses?: string[];
  partyIds?: number[];
  periodStart?: string;
  periodEnd?: string;
}

export interface ProjectWriteResult {
  projectId: number;
}

@singleton()
export default class ProjectRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  /**
   * 프로젝트 검색 (페이징)
   */
  async search(params: ProjectSearchParams): Promise<PageResponse<ProjectListItem>> {
    const queryParams = buildRequestParams(params);
    const response = await this.httpRepository.get({
      path: '/api/projects',
      params: queryParams,
    });

    return PageResponse.fromPage(response, mapProjectListItem);
  }

  /**
   * 프로젝트 목록 조회 (호환용)
   */
  async list(params: ProjectSearchParams): Promise<PageResponse<ProjectListItem>> {
    return this.search(params);
  }

  async fetchOverviewSummary(params: ProjectOverviewSummaryParams): Promise<ProjectOverviewSummary> {
    const response = await this.httpRepository.get<ProjectOverviewSummary>({
      path: '/api/projects/summary',
      params: buildOverviewRequestParams(params),
    });

    return {
      totalCount: Number(response?.totalCount ?? 0),
      scheduledCount: Number(response?.scheduledCount ?? 0),
      inProgressCount: Number(response?.inProgressCount ?? 0),
      completedCount: Number(response?.completedCount ?? 0),
      onHoldCount: Number(response?.onHoldCount ?? 0),
      cancelledCount: Number(response?.cancelledCount ?? 0),
      totalContractAmount: Number(response?.totalContractAmount ?? 0),
    };
  }

  /**
   * 프로젝트 상세 조회
   */
  async find(projectId: number): Promise<ProjectDetail> {
    const response = await this.httpRepository.get({
      path: `/api/projects/${projectId}`,
    });

    return mapProjectDetail(response);
  }

  /**
   * 프로젝트 생성
   */
  async create(data: ProjectCreateData): Promise<ProjectWriteResult> {
    const response = await this.httpRepository.post<ProjectWriteResult>({
      path: '/api/projects',
      data: {
        partyId: data.partyId,
        leadDepartmentId: data.leadDepartmentId,
        code: data.code,
        name: data.name,
        description: data.description || null,
        status: data.status,
        contractAmount: data.contractAmount,
        startDate: data.startDate,
        endDate: data.endDate,
      },
    });

    return {
      projectId: Number(response?.projectId ?? 0),
    };
  }

  /**
   * 프로젝트 수정
   */
  async update(projectId: number, data: ProjectUpdateData): Promise<ProjectWriteResult> {
    const response = await this.httpRepository.put<ProjectWriteResult>({
      path: `/api/projects/${projectId}`,
      data: {
        partyId: data.partyId,
        leadDepartmentId: data.leadDepartmentId,
        name: data.name,
        description: data.description || null,
        status: data.status,
        contractAmount: data.contractAmount,
        startDate: data.startDate,
        endDate: data.endDate,
      },
    });

    return {
      projectId: Number(response?.projectId ?? 0),
    };
  }

  /**
   * 프로젝트 삭제
   */
  async delete(projectId: number): Promise<void> {
    await this.httpRepository.delete({
      path: `/api/projects/${projectId}`,
    });
  }

  /**
   * 프로젝트 완료 처리
   */
  async complete(projectId: number): Promise<void> {
    await this.httpRepository.patch({
      path: `/api/projects/${projectId}/complete`,
    });
  }

  /**
   * 프로젝트 취소 처리
   */
  async cancel(projectId: number): Promise<void> {
    await this.httpRepository.patch({
      path: `/api/projects/${projectId}/cancel`,
    });
  }

  /**
   * 프로젝트 상태 목록 조회
   */
  async fetchStatuses(): Promise<{ value: string; label: string }[]> {
    const response: any[] = await this.httpRepository.get({
      path: '/api/projects/statuses',
    });

    return response.map((item: any) => ({
      value: item.name,
      label: item.description,
    }));
  }

  /**
   * 엑셀 다운로드
   */
  async downloadExcel(params: ProjectSearchParams): Promise<void> {
    const queryParams = buildRequestParams(params);
    const response = await this.httpRepository.download({
      path: '/api/projects/excel/download',
      params: queryParams,
    });

    if (!response.ok) {
      throw new Error(`엑셀 다운로드에 실패했습니다. (${response.status})`);
    }

    const filename = extractFilenameFromResponse(response, generateExcelFilename('projects'));
    const blob = await response.blob();
    downloadBlob(blob, filename);
  }

  /**
   * 엑셀 샘플 다운로드
   */
  async downloadExcelSample(): Promise<void> {
    const response = await this.httpRepository.download({
      path: '/api/projects/excel/sample',
    });

    if (!response.ok) {
      throw new Error(`샘플 파일 다운로드에 실패했습니다. (${response.status})`);
    }

    const filename = extractFilenameFromResponse(
      response,
      generateExcelFilename('projects_sample'),
    );
    const blob = await response.blob();
    downloadBlob(blob, filename);
  }

  /**
   * 엑셀 업로드
   */
  async uploadExcel(file: File, onProgress?: (progress: number) => void): Promise<void> {
    const formData = new FormData();
    formData.append('file', file);

    await this.httpRepository.upload({
      path: '/api/projects/excel/upload',
      data: formData,
      onProgress,
    });
  }
}

function buildRequestParams(params: ProjectSearchParams): Record<string, string> {
  const query: Record<string, string> = {
    page: Math.max(params.page - 1, 0).toString(),
    size: params.size.toString(),
  };

  if (params.name) {
    query.name = params.name;
  }

  if (params.periodStart) {
    query.periodStart = params.periodStart;
  }

  if (params.periodEnd) {
    query.periodEnd = params.periodEnd;
  }

  if (params.sort) {
    query.sort = params.sort;
  }

  const arrayFields: (keyof ProjectSearchParams)[] = ['statuses', 'partyIds'];
  arrayFields.forEach((field) => {
    const value = params[field];
    if (Array.isArray(value) && value.length > 0) {
      query[field] = value.join(',');
    }
  });

  return query;
}

function buildOverviewRequestParams(params: ProjectOverviewSummaryParams): Record<string, string> {
  const query: Record<string, string> = {};

  if (params.name) {
    query.name = params.name;
  }

  if (params.periodStart) {
    query.periodStart = params.periodStart;
  }

  if (params.periodEnd) {
    query.periodEnd = params.periodEnd;
  }

  const arrayFields: (keyof ProjectOverviewSummaryParams)[] = ['statuses', 'partyIds'];
  arrayFields.forEach((field) => {
    const value = params[field];
    if (Array.isArray(value) && value.length > 0) {
      query[field] = value.join(',');
    }
  });

  return query;
}
