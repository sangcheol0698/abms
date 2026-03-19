import { computed, ref } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { employeeKeys, queryClient } from '@/core/query';
import {
  useChangeEmployeeSalaryMutation,
  useEmployeeCurrentPayrollQuery,
  useEmployeePayrollHistoryQuery,
} from '@/features/employee/queries/useEmployeeQueries';

const vueQueryMocks = vi.hoisted(() => ({
  useQueryMock: vi.fn((options) => options),
  useMutationMock: vi.fn((options) => options),
}));

const employeeRepositoryMock = {
  fetchCurrentPayroll: vi.fn(),
  fetchPayrollHistory: vi.fn(),
  changeSalary: vi.fn(),
};

vi.mock('@tanstack/vue-query', async () => {
  const actual = await vi.importActual<typeof import('@tanstack/vue-query')>('@tanstack/vue-query');
  return {
    ...actual,
    keepPreviousData: Symbol('keepPreviousData'),
    useQuery: vueQueryMocks.useQueryMock,
    useMutation: vueQueryMocks.useMutationMock,
  };
});

vi.mock('@/core/di/container', () => ({
  appContainer: {
    resolve: () => employeeRepositoryMock,
  },
}));

describe('useEmployeeQueries payroll', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(queryClient, 'invalidateQueries').mockResolvedValue(undefined as never);
  });

  it('현재 연봉/이력 query를 저장소와 query key에 연결한다', async () => {
    employeeRepositoryMock.fetchCurrentPayroll.mockResolvedValueOnce(null);
    employeeRepositoryMock.fetchPayrollHistory.mockResolvedValueOnce([]);

    const currentQuery = useEmployeeCurrentPayrollQuery(ref(3));
    const historyQuery = useEmployeePayrollHistoryQuery(computed(() => 3));

    expect(currentQuery.queryKey.value).toEqual(employeeKeys.payrollCurrent(3));
    expect(historyQuery.queryKey.value).toEqual(employeeKeys.payrollHistory(3));
    expect(currentQuery.enabled.value).toBe(true);
    expect(historyQuery.enabled.value).toBe(true);

    await currentQuery.queryFn();
    await historyQuery.queryFn();

    expect(employeeRepositoryMock.fetchCurrentPayroll).toHaveBeenCalledWith(3);
    expect(employeeRepositoryMock.fetchPayrollHistory).toHaveBeenCalledWith(3);
  });

  it('연봉 변경 mutation 성공 시 현재 연봉과 이력을 invalidate한다', async () => {
    const mutation = useChangeEmployeeSalaryMutation();

    await mutation.mutationFn({
      employeeId: 7,
      payload: { annualSalary: 90000000, startDate: '2025-05-01' },
    });
    await mutation.onSuccess?.(undefined, {
      employeeId: 7,
      payload: { annualSalary: 90000000, startDate: '2025-05-01' },
    });

    expect(employeeRepositoryMock.changeSalary).toHaveBeenCalledWith(7, {
      annualSalary: 90000000,
      startDate: '2025-05-01',
    });
    expect(queryClient.invalidateQueries).toHaveBeenCalledWith({
      queryKey: employeeKeys.payrollCurrent(7),
    });
    expect(queryClient.invalidateQueries).toHaveBeenCalledWith({
      queryKey: employeeKeys.payrollHistory(7),
    });
  });
});
