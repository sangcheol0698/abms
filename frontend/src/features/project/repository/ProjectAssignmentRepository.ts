import { inject, injectable } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';

export interface ProjectAssignmentResponse {
  id: number;
  projectId: number;
  employeeId: number;
  employeeName: string;
  departmentName?: string;
  role: string;
  startDate: string;
  endDate: string;
}

export interface ProjectAssignmentCreateRequest {
  projectId: number;
  employeeId: number;
  role: string;
  startDate: string;
  endDate: string;
}

@injectable()
export default class ProjectAssignmentRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  async findByProjectId(projectId: number): Promise<ProjectAssignmentResponse[]> {
    return this.httpRepository.get<ProjectAssignmentResponse[]>({
      path: '/api/project-assignments',
      params: { projectId },
    });
  }

  async create(request: ProjectAssignmentCreateRequest): Promise<{ id: number }> {
    return this.httpRepository.post<{ id: number }>({
      path: '/api/project-assignments',
      data: request,
    });
  }
}
