import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { keepPreviousData, useMutation, useQuery } from '@tanstack/vue-query';
import { appContainer } from '@/core/di/container';
import { authKeys, employeeKeys, departmentKeys, dashboardKeys, queryClient } from '@/core/query';
import { EmployeeRepository } from '@/features/employee/repository/EmployeeRepository';
import type { EmployeeCreatePayload } from '@/features/employee/models/employee';
import type { EmployeeProjectsSearchParams } from '@/features/employee/models/project';
import type { ChangeEmployeeSalaryPayload } from '@/features/employee/models/payroll';
import type { EmployeeSearchParams } from '@/features/employee/models/employeeListItem';
import type { EmployeeOverviewSummaryParams } from '@/features/employee/repository/EmployeeRepository';
import AuthRepository from '@/features/auth/repository/AuthRepository';
import { getStoredUser, markSessionNeedsValidation, refreshStoredUserSession } from '@/features/auth/session';

async function invalidateEmployeeSideEffects(employeeId?: number) {
  const tasks: Promise<unknown>[] = [
    queryClient.invalidateQueries({ queryKey: employeeKeys.all }),
    queryClient.invalidateQueries({ queryKey: departmentKeys.all }),
    queryClient.invalidateQueries({ queryKey: dashboardKeys.summary() }),
  ];

  const currentUser = getStoredUser();
  const isCurrentUserMutation = Boolean(
    employeeId && currentUser?.employeeId && currentUser.employeeId === employeeId,
  );

  if (isCurrentUserMutation) {
    tasks.push(queryClient.invalidateQueries({ queryKey: authKeys.me() }));
    tasks.push(queryClient.invalidateQueries({ queryKey: employeeKeys.currentProfile() }));
  }

  if (employeeId && employeeId > 0) {
    tasks.push(queryClient.invalidateQueries({ queryKey: employeeKeys.detail(employeeId) }));
  }

  await Promise.all(tasks);

  if (isCurrentUserMutation) {
    markSessionNeedsValidation();
    await refreshStoredUserSession(true);
  }
}

export function useEmployeesQuery(paramsRef: MaybeRefOrGetter<EmployeeSearchParams>) {
  const repository = appContainer.resolve(EmployeeRepository);
  const params = computed(() => toValue(paramsRef));

  return useQuery({
    queryKey: computed(() =>
      employeeKeys.list((params.value ?? {}) as unknown as Record<string, unknown>),
    ),
    queryFn: () => repository.search(params.value),
    placeholderData: keepPreviousData,
  });
}

export function useEmployeeOverviewSummaryQuery(
  paramsRef: MaybeRefOrGetter<EmployeeOverviewSummaryParams>,
) {
  const repository = appContainer.resolve(EmployeeRepository);
  const params = computed(() => toValue(paramsRef));

  return useQuery({
    queryKey: computed(() => employeeKeys.summary(params.value ?? {})),
    queryFn: () => repository.fetchOverviewSummary(params.value),
    placeholderData: keepPreviousData,
  });
}

export function useEmployeeDetailQuery(
  employeeIdRef: MaybeRefOrGetter<number | null | undefined>,
) {
  const repository = appContainer.resolve(EmployeeRepository);
  const employeeId = computed(() => Number(toValue(employeeIdRef) ?? 0));

  return useQuery({
    queryKey: computed(() => employeeKeys.detail(employeeId.value)),
    queryFn: () => repository.findById(employeeId.value),
    enabled: computed(() => employeeId.value > 0),
  });
}

export function useCurrentEmployeeProfileQuery(enabledRef: MaybeRefOrGetter<boolean> = true) {
  const employeeRepository = appContainer.resolve(EmployeeRepository);
  const authRepository = appContainer.resolve(AuthRepository);
  const enabled = computed(() => Boolean(toValue(enabledRef)));

  return useQuery({
    queryKey: computed(() => employeeKeys.currentProfile()),
    queryFn: async () => {
      const me = await queryClient.fetchQuery({
        queryKey: authKeys.me(),
        queryFn: () => authRepository.fetchMe(),
      });

      if (!me.employeeId || me.employeeId <= 0) {
        throw new Error('현재 로그인한 직원 정보를 찾을 수 없습니다.');
      }

      return employeeRepository.findById(me.employeeId);
    },
    enabled,
  });
}

export function useEmployeeStatusesQuery() {
  const repository = appContainer.resolve(EmployeeRepository);

  return useQuery({
    queryKey: employeeKeys.statuses(),
    queryFn: () => repository.fetchStatuses(),
  });
}

export function useEmployeeTypesQuery() {
  const repository = appContainer.resolve(EmployeeRepository);

  return useQuery({
    queryKey: employeeKeys.types(),
    queryFn: () => repository.fetchTypes(),
  });
}

export function useEmployeeGradesQuery() {
  const repository = appContainer.resolve(EmployeeRepository);

  return useQuery({
    queryKey: employeeKeys.grades(),
    queryFn: () => repository.fetchGrades(),
  });
}

export function useEmployeePositionsQuery() {
  const repository = appContainer.resolve(EmployeeRepository);

  return useQuery({
    queryKey: employeeKeys.positions(),
    queryFn: () => repository.fetchPositions(),
  });
}

export function useEmployeeAvatarsQuery() {
  const repository = appContainer.resolve(EmployeeRepository);

  return useQuery({
    queryKey: employeeKeys.avatars(),
    queryFn: () => repository.fetchAvatars(),
  });
}

export function useEmployeePositionHistoryQuery(
  employeeIdRef: MaybeRefOrGetter<number | null | undefined>,
) {
  const repository = appContainer.resolve(EmployeeRepository);
  const employeeId = computed(() => Number(toValue(employeeIdRef) ?? 0));

  return useQuery({
    queryKey: computed(() => employeeKeys.positionHistory(employeeId.value)),
    queryFn: () => repository.fetchPositionHistory(employeeId.value),
    enabled: computed(() => employeeId.value > 0),
  });
}

export function useEmployeeCurrentPayrollQuery(
  employeeIdRef: MaybeRefOrGetter<number | null | undefined>,
) {
  const repository = appContainer.resolve(EmployeeRepository);
  const employeeId = computed(() => Number(toValue(employeeIdRef) ?? 0));

  return useQuery({
    queryKey: computed(() => employeeKeys.payrollCurrent(employeeId.value)),
    queryFn: () => repository.fetchCurrentPayroll(employeeId.value),
    enabled: computed(() => employeeId.value > 0),
  });
}

export function useEmployeePayrollHistoryQuery(
  employeeIdRef: MaybeRefOrGetter<number | null | undefined>,
) {
  const repository = appContainer.resolve(EmployeeRepository);
  const employeeId = computed(() => Number(toValue(employeeIdRef) ?? 0));

  return useQuery({
    queryKey: computed(() => employeeKeys.payrollHistory(employeeId.value)),
    queryFn: () => repository.fetchPayrollHistory(employeeId.value),
    enabled: computed(() => employeeId.value > 0),
  });
}

export function useEmployeeProjectsQuery(
  employeeIdRef: MaybeRefOrGetter<number | null | undefined>,
  paramsRef: MaybeRefOrGetter<EmployeeProjectsSearchParams>,
) {
  const repository = appContainer.resolve(EmployeeRepository);
  const employeeId = computed(() => Number(toValue(employeeIdRef) ?? 0));
  const params = computed(() => toValue(paramsRef));

  return useQuery({
    queryKey: computed(() =>
      employeeKeys.projects(employeeId.value, (params.value ?? {}) as unknown as Record<string, unknown>),
    ),
    queryFn: () => repository.fetchProjects(employeeId.value, params.value),
    enabled: computed(() => employeeId.value > 0),
    placeholderData: keepPreviousData,
  });
}

export function useCreateEmployeeMutation() {
  const repository = appContainer.resolve(EmployeeRepository);

  return useMutation({
    mutationFn: (payload: EmployeeCreatePayload) => repository.create(payload),
    onSuccess: async (created) => {
      await invalidateEmployeeSideEffects(created.employeeId);
    },
  });
}

export function useUpdateEmployeeMutation() {
  const repository = appContainer.resolve(EmployeeRepository);

  return useMutation({
    mutationFn: (variables: { employeeId: number; payload: EmployeeCreatePayload }) =>
      repository.update(variables.employeeId, variables.payload),
    onSuccess: async (updated) => {
      await invalidateEmployeeSideEffects(updated.employeeId);
    },
  });
}

export function useDeleteEmployeeMutation() {
  const repository = appContainer.resolve(EmployeeRepository);

  return useMutation({
    mutationFn: (employeeId: number) => repository.delete(employeeId),
    onSuccess: async (_data, employeeId) => {
      await invalidateEmployeeSideEffects(employeeId);
    },
  });
}

export function useRestoreEmployeeMutation() {
  const repository = appContainer.resolve(EmployeeRepository);

  return useMutation({
    mutationFn: (employeeId: number) => repository.restore(employeeId),
    onSuccess: async (_data, employeeId) => {
      await invalidateEmployeeSideEffects(employeeId);
    },
  });
}

export function useTakeLeaveEmployeeMutation() {
  const repository = appContainer.resolve(EmployeeRepository);

  return useMutation({
    mutationFn: (employeeId: number) => repository.takeLeave(employeeId),
    onSuccess: async (_data, employeeId) => {
      await invalidateEmployeeSideEffects(employeeId);
    },
  });
}

export function useActivateEmployeeMutation() {
  const repository = appContainer.resolve(EmployeeRepository);

  return useMutation({
    mutationFn: (employeeId: number) => repository.activate(employeeId),
    onSuccess: async (_data, employeeId) => {
      await invalidateEmployeeSideEffects(employeeId);
    },
  });
}

export function useResignEmployeeMutation() {
  const repository = appContainer.resolve(EmployeeRepository);

  return useMutation({
    mutationFn: (variables: { employeeId: number; resignationDate: string }) =>
      repository.resign(variables.employeeId, variables.resignationDate),
    onSuccess: async (_data, variables) => {
      await invalidateEmployeeSideEffects(variables.employeeId);
    },
  });
}

export function usePromoteEmployeeMutation() {
  const repository = appContainer.resolve(EmployeeRepository);

  return useMutation({
    mutationFn: (variables: { employeeId: number; position: string; grade?: string }) =>
      repository.promote(variables.employeeId, variables.position, variables.grade),
    onSuccess: async (_data, variables) => {
      await invalidateEmployeeSideEffects(variables.employeeId);
    },
  });
}

export function useChangeEmployeeSalaryMutation() {
  const repository = appContainer.resolve(EmployeeRepository);

  return useMutation({
    mutationFn: (variables: { employeeId: number; payload: ChangeEmployeeSalaryPayload }) =>
      repository.changeSalary(variables.employeeId, variables.payload),
    onSuccess: async (_data, variables) => {
      await queryClient.invalidateQueries({
        queryKey: employeeKeys.payrollCurrent(variables.employeeId),
      });
      await queryClient.invalidateQueries({
        queryKey: employeeKeys.payrollHistory(variables.employeeId),
      });
    },
  });
}
