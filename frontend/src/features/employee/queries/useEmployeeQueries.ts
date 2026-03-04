import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useMutation, useQuery } from '@tanstack/vue-query';
import { appContainer } from '@/core/di/container';
import { employeeKeys, departmentKeys, dashboardKeys, queryClient } from '@/core/query';
import { EmployeeRepository } from '@/features/employee/repository/EmployeeRepository';
import type { EmployeeCreatePayload } from '@/features/employee/models/employee';
import type { EmployeeSearchParams } from '@/features/employee/models/employeeListItem';

async function invalidateEmployeeSideEffects(employeeId?: number) {
  const tasks: Promise<unknown>[] = [
    queryClient.invalidateQueries({ queryKey: employeeKeys.all }),
    queryClient.invalidateQueries({ queryKey: departmentKeys.all }),
    queryClient.invalidateQueries({ queryKey: dashboardKeys.summary() }),
  ];

  if (employeeId && employeeId > 0) {
    tasks.push(queryClient.invalidateQueries({ queryKey: employeeKeys.detail(employeeId) }));
  }

  await Promise.all(tasks);
}

export function useEmployeesQuery(paramsRef: MaybeRefOrGetter<EmployeeSearchParams>) {
  const repository = appContainer.resolve(EmployeeRepository);
  const params = computed(() => toValue(paramsRef));

  return useQuery({
    queryKey: computed(() =>
      employeeKeys.list((params.value ?? {}) as unknown as Record<string, unknown>),
    ),
    queryFn: () => repository.search(params.value),
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
