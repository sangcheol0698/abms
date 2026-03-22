import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { keepPreviousData, useMutation, useQuery } from '@tanstack/vue-query';
import { appContainer } from '@/core/di/container';
import { departmentKeys, queryClient } from '@/core/query';
import DepartmentRepository from '@/features/department/repository/DepartmentRepository';

export interface DepartmentEmployeesQueryParams {
  departmentId: number;
  page: number;
  size: number;
  name?: string;
  sort?: string;
  statuses?: string[];
  types?: string[];
  grades?: string[];
  positions?: string[];
}

export function useDepartmentOrganizationChartQuery() {
  const repository = appContainer.resolve(DepartmentRepository);

  return useQuery({
    queryKey: departmentKeys.organizationChart(),
    queryFn: () => repository.fetchOrganizationChart(),
  });
}

export function useDepartmentDetailQuery(
  departmentIdRef: MaybeRefOrGetter<number | null | undefined>,
) {
  const repository = appContainer.resolve(DepartmentRepository);
  const departmentId = computed(() => Number(toValue(departmentIdRef) ?? 0));

  return useQuery({
    queryKey: computed(() => departmentKeys.detail(departmentId.value)),
    queryFn: () => repository.fetchDepartmentDetail(departmentId.value),
    enabled: computed(() => departmentId.value > 0),
  });
}

export function useDepartmentRevenueTrendQuery(
  departmentIdRef: MaybeRefOrGetter<number | null | undefined>,
  yearMonthRef: MaybeRefOrGetter<string>,
) {
  const repository = appContainer.resolve(DepartmentRepository);
  const departmentId = computed(() => Number(toValue(departmentIdRef) ?? 0));
  const yearMonth = computed(() => String(toValue(yearMonthRef) ?? ''));

  return useQuery({
    queryKey: computed(() => departmentKeys.revenueTrend(departmentId.value, yearMonth.value)),
    queryFn: () => repository.fetchDepartmentRevenueTrend(departmentId.value, yearMonth.value),
    enabled: computed(() => departmentId.value > 0 && yearMonth.value.length > 0),
    placeholderData: keepPreviousData,
  });
}

export function useDepartmentEmployeesQuery(
  paramsRef: MaybeRefOrGetter<DepartmentEmployeesQueryParams>,
) {
  const repository = appContainer.resolve(DepartmentRepository);
  const params = computed(() => toValue(paramsRef));
  const departmentId = computed(() => Number(params.value.departmentId ?? 0));

  return useQuery({
    queryKey: computed(() =>
      departmentKeys.employees(
        departmentId.value,
        (params.value ?? {}) as unknown as Record<string, unknown>,
      ),
    ),
    queryFn: () =>
      repository.fetchDepartmentEmployees(departmentId.value, {
        page: params.value.page,
        size: params.value.size,
        name: params.value.name,
        sort: params.value.sort,
        statuses: params.value.statuses,
        types: params.value.types,
        grades: params.value.grades,
        positions: params.value.positions,
      }),
    enabled: computed(() => departmentId.value > 0),
    placeholderData: keepPreviousData,
  });
}

export function useAssignDepartmentLeaderMutation() {
  const repository = appContainer.resolve(DepartmentRepository);

  return useMutation({
    mutationFn: (variables: { departmentId: number; employeeId: number }) =>
      repository.assignTeamLeader(variables.departmentId, variables.employeeId),
    onSuccess: async (_data, variables) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: departmentKeys.organizationChart() }),
        queryClient.invalidateQueries({ queryKey: departmentKeys.detail(variables.departmentId) }),
        queryClient.invalidateQueries({
          queryKey: departmentKeys.employeesRoot(variables.departmentId),
        }),
      ]);
    },
  });
}
