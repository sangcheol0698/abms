import 'reflect-metadata';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PageResponse from '@/core/api/page';
import type HttpRepository from '@/core/http/HttpRepository';
import { EmployeeRepository } from '../EmployeeRepository';

describe('EmployeeRepository projects', () => {
  let httpGet: ReturnType<typeof vi.fn>;
  let repository: EmployeeRepository;

  beforeEach(() => {
    httpGet = vi.fn();
    repository = new EmployeeRepository({
      get: httpGet,
    } as unknown as HttpRepository);
  });

  it('직원 프로젝트 응답을 모델로 변환한다', async () => {
    httpGet.mockResolvedValueOnce({
      content: [
        {
          projectId: 3,
          projectCode: 'PRJ-3',
          projectName: '프로젝트 3',
          partyId: 8,
          role: '개발자',
          assignmentStartDate: '2026-01-01',
          assignmentEndDate: null,
          assignmentStatus: 'CURRENT',
          projectStatus: 'IN_PROGRESS',
          projectStatusDescription: '진행 중',
          leadDepartmentId: 10,
          leadDepartmentName: '개발팀',
          partyName: '아바쿠스',
        },
      ],
      pageNumber: 0,
      pageSize: 10,
      totalPages: 1,
      totalElements: 1,
    });

    const result = await repository.fetchProjects(7, { page: 1, size: 10 });

    expect(httpGet).toHaveBeenCalledWith({
      path: '/api/employees/7/projects',
      params: {
        page: '0',
        size: '10',
      },
    });
    expect(result).toBeInstanceOf(PageResponse);
    expect(result.page).toBe(1);
    expect(result.totalElements).toBe(1);
    expect(result.content[0]).toEqual({
      projectId: 3,
      projectCode: 'PRJ-3',
      projectName: '프로젝트 3',
      partyId: 8,
      role: '개발자',
      assignmentStartDate: '2026-01-01',
      assignmentEndDate: null,
      assignmentStatus: 'CURRENT',
      projectStatus: 'IN_PROGRESS',
      projectStatusLabel: '진행 중',
      leadDepartmentId: 10,
      leadDepartmentName: '개발팀',
      partyName: '아바쿠스',
    });
  });
});
