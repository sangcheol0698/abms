import 'reflect-metadata';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import HttpError from '@/core/http/HttpError';
import type HttpRepository from '@/core/http/HttpRepository';
import { EmployeeRepository } from '../EmployeeRepository';

describe('EmployeeRepository payroll', () => {
  let httpGet: ReturnType<typeof vi.fn>;
  let httpPost: ReturnType<typeof vi.fn>;
  let repository: EmployeeRepository;

  beforeEach(() => {
    httpGet = vi.fn();
    httpPost = vi.fn();
    repository = new EmployeeRepository({
      get: httpGet,
      post: httpPost,
    } as unknown as HttpRepository);
  });

  it('현재 연봉 조회 응답을 모델로 변환한다', async () => {
    httpGet.mockResolvedValueOnce({
      employeeId: 5,
      annualSalary: 72000000,
      monthlySalary: 6000000,
      startDate: '2025-01-01',
      status: 'CURRENT',
    });

    const result = await repository.fetchCurrentPayroll(5);

    expect(httpGet).toHaveBeenCalledWith({
      path: '/api/employees/5/payroll',
    });
    expect(result).toEqual({
      employeeId: 5,
      annualSalary: 72000000,
      monthlySalary: 6000000,
      startDate: '2025-01-01',
      status: 'CURRENT',
    });
  });

  it('현재 연봉이 없으면 null을 반환한다', async () => {
    httpGet.mockRejectedValueOnce(new HttpError({
      response: {
        status: 404,
        data: { message: '현재 연봉 정보가 없습니다.' },
      },
    } as any));

    const result = await repository.fetchCurrentPayroll(5);

    expect(result).toBeNull();
  });

  it('연봉 이력 조회와 변경 요청 경로를 올바르게 사용한다', async () => {
    httpGet.mockResolvedValueOnce([
      {
        employeeId: 5,
        annualSalary: 72000000,
        monthlySalary: 6000000,
        startDate: '2025-01-01',
        endDate: null,
        status: 'CURRENT',
      },
    ]);

    const history = await repository.fetchPayrollHistory(5);
    await repository.changeSalary(5, { annualSalary: 84000000, startDate: '2025-04-01' });

    expect(httpGet).toHaveBeenCalledWith({
      path: '/api/employees/5/payroll-history',
    });
    expect(history).toEqual([
      {
        employeeId: 5,
        annualSalary: 72000000,
        monthlySalary: 6000000,
        startDate: '2025-01-01',
        endDate: null,
        status: 'CURRENT',
      },
    ]);
    expect(httpPost).toHaveBeenCalledWith({
      path: '/api/employees/5/payroll-history',
      data: {
        annualSalary: 84000000,
        startDate: '2025-04-01',
      },
    });
  });
});
