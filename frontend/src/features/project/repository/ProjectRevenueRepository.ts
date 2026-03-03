import { inject, injectable } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';

export interface ProjectRevenuePlanCreateRequest {
  projectId: number;
  sequence: number;
  revenueDate: string;
  type: string;
  amount: number;
  memo?: string;
}

export interface ProjectRevenuePlanResponse {
  projectId: number;
  sequence: number;
  revenueDate: string;
  type: string;
  amount: number;
  status?: 'PLANNED' | 'INVOICED';
  memo?: string;
}

@injectable()
export default class ProjectRevenueRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  async create(request: ProjectRevenuePlanCreateRequest): Promise<void> {
    await this.httpRepository.post({
      path: '/api/projectRevenuePlans',
      data: request,
    });
  }

  async findByProjectId(projectId: number): Promise<ProjectRevenuePlanResponse[]> {
    return this.httpRepository.get<ProjectRevenuePlanResponse[]>({
      path: `/api/projectRevenuePlans/${projectId}`,
    });
  }
}
