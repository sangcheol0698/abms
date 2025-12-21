import { inject, singleton } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';
import { PageResponse } from '@/core/api';
import type { ProjectListItem } from '@/features/project/models/projectListItem';
import { mapProjectListItem } from '@/features/project/models/projectListItem';
import type {
  ProjectDetail,
  ProjectCreateData,
  ProjectUpdateData,
} from '@/features/project/models/projectDetail';
import { mapProjectDetail } from '@/features/project/models/projectDetail';

@singleton()
export default class ProjectRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  /**
   * 프로젝트 목록 조회 (페이징)
   */
  async list(
    params: { page: number; size: number; sort?: string } & Record<string, any>,
  ): Promise<PageResponse<ProjectListItem>> {
    const queryParams: Record<string, string> = {};

    // Copy all params to queryParams, converting non-string values to string
    Object.keys(params).forEach((key) => {
      if (params[key] !== undefined && params[key] !== null) {
        queryParams[key] = String(params[key]);
      }
    });

    // Override page and size with calculated values
    queryParams.page = Math.max(params.page - 1, 0).toString();
    queryParams.size = params.size.toString();

    if (params.sort) {
      queryParams.sort = params.sort;
    }

    const response = await this.httpRepository.get({
      path: '/api/projects',
      params: queryParams,
    });

    return PageResponse.fromPage(response, mapProjectListItem);
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
  async create(data: ProjectCreateData): Promise<ProjectDetail> {
    const response = await this.httpRepository.post({
      path: '/api/projects',
      data: {
        partyId: data.partyId,
        code: data.code,
        name: data.name,
        description: data.description || null,
        status: data.status,
        contractAmount: data.contractAmount,
        startDate: data.startDate,
        endDate: data.endDate,
      },
    });

    return mapProjectDetail(response);
  }

  /**
   * 프로젝트 수정
   */
  async update(projectId: number, data: ProjectUpdateData): Promise<ProjectDetail> {
    const response = await this.httpRepository.put({
      path: `/api/projects/${projectId}`,
      data: {
        partyId: data.partyId,
        name: data.name,
        description: data.description || null,
        status: data.status,
        contractAmount: data.contractAmount,
        startDate: data.startDate,
        endDate: data.endDate,
      },
    });

    return mapProjectDetail(response);
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
  async complete(projectId: number): Promise<ProjectDetail> {
    const response = await this.httpRepository.patch({
      path: `/api/projects/${projectId}/complete`,
    });

    return mapProjectDetail(response);
  }

  /**
   * 프로젝트 취소 처리
   */
  async cancel(projectId: number): Promise<ProjectDetail> {
    const response = await this.httpRepository.patch({
      path: `/api/projects/${projectId}/cancel`,
    });

    return mapProjectDetail(response);
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
}
