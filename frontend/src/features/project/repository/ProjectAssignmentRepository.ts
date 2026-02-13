import { injectable } from 'tsyringe';
import axios from 'axios';

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
  private baseUrl = import.meta.env.VITE_API_BASE_URL || '';

  async findByProjectId(projectId: number): Promise<ProjectAssignmentResponse[]> {
    const { data } = await axios.get<ProjectAssignmentResponse[]>(
      `${this.baseUrl}/api/project-assignments`,
      { params: { projectId } },
    );
    return data;
  }

  async create(request: ProjectAssignmentCreateRequest): Promise<{ id: number }> {
    const { data } = await axios.post<{ id: number }>(
      `${this.baseUrl}/api/project-assignments`,
      request,
    );
    return data;
  }
}
