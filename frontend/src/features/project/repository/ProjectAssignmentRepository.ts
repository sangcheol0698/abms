import { inject, injectable } from 'tsyringe';
import { PageResponse } from '@/core/api';
import HttpRepository from '@/core/http/HttpRepository';
import type {
  ProjectAssignmentCreatePayload,
  ProjectAssignmentEndPayload,
  ProjectAssignmentItem,
  ProjectAssignmentSearchParams,
  ProjectAssignmentUpdatePayload,
} from '@/features/project/models/projectAssignment';
import { mapProjectAssignmentItem } from '@/features/project/models/projectAssignment';

@injectable()
export default class ProjectAssignmentRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  async findByProjectId(
    projectId: number,
    params: ProjectAssignmentSearchParams,
  ): Promise<PageResponse<ProjectAssignmentItem>> {
    const response = await this.httpRepository.get<any>({
      path: '/api/project-assignments',
      params: buildProjectAssignmentRequestParams(projectId, params),
    });

    return new PageResponse<ProjectAssignmentItem>({
      page: Number(response?.pageNumber ?? response?.number ?? 0) + 1,
      size: Number(response?.pageSize ?? response?.size ?? params.size),
      totalPages: Number(response?.totalPages ?? 0),
      totalElements: Number(response?.totalElements ?? 0),
      content: Array.isArray(response?.content) ? response.content.map(mapProjectAssignmentItem) : [],
    });
  }

  async create(request: ProjectAssignmentCreatePayload): Promise<{ id: number }> {
    return this.httpRepository.post<{ id: number }>({
      path: '/api/project-assignments',
      data: request,
    });
  }

  async update(assignmentId: number, request: ProjectAssignmentUpdatePayload): Promise<{ id: number }> {
    return this.httpRepository.patch<{ id: number }>({
      path: `/api/project-assignments/${assignmentId}`,
      data: request,
    });
  }

  async end(assignmentId: number, request: ProjectAssignmentEndPayload): Promise<{ id: number }> {
    return this.httpRepository.patch<{ id: number }>({
      path: `/api/project-assignments/${assignmentId}/end`,
      data: request,
    });
  }
}

function buildProjectAssignmentRequestParams(
  projectId: number,
  params: ProjectAssignmentSearchParams,
): Record<string, string> {
  const query: Record<string, string> = {
    projectId: String(projectId),
    page: String(Math.max(params.page - 1, 0)),
    size: String(params.size),
  };

  if (params.name) {
    query.name = params.name;
  }
  if (params.assignmentStatuses?.length) {
    query.assignmentStatuses = params.assignmentStatuses.join(',');
  }
  if (params.roles?.length) {
    query.roles = params.roles.join(',');
  }

  return query;
}
