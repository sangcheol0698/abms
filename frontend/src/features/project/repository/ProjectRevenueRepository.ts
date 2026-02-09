import { injectable } from 'tsyringe';
import axios from 'axios';

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
  private baseUrl = import.meta.env.VITE_API_BASE_URL || '';

  async create(request: ProjectRevenuePlanCreateRequest): Promise<void> {
    await axios.post(`${this.baseUrl}/api/projectRevenuePlans`, request);
  }

  async findByProjectId(projectId: number): Promise<ProjectRevenuePlanResponse[]> {
    const { data } = await axios.get<ProjectRevenuePlanResponse[]>(
      `${this.baseUrl}/api/projectRevenuePlans/${projectId}`,
    );
    return data;
  }
}
