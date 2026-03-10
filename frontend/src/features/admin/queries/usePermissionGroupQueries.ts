import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useMutation, useQuery } from '@tanstack/vue-query';
import { appContainer } from '@/core/di/container';
import { adminKeys, queryClient } from '@/core/query';
import PermissionGroupRepository from '@/features/admin/repository/PermissionGroupRepository';
import type {
  PermissionGroupFilters,
  PermissionGroupUpsertPayload,
} from '@/features/admin/models/permissionGroup';

async function invalidatePermissionGroupSideEffects(permissionGroupId?: number) {
  const tasks: Promise<unknown>[] = [
    queryClient.invalidateQueries({ queryKey: adminKeys.permissionGroups.all }),
    queryClient.invalidateQueries({ queryKey: adminKeys.accounts.all }),
  ];

  if (permissionGroupId && permissionGroupId > 0) {
    tasks.push(
      queryClient.invalidateQueries({
        queryKey: adminKeys.permissionGroups.detail(permissionGroupId),
      }),
    );
    tasks.push(
      queryClient.invalidateQueries({
        queryKey: adminKeys.accounts.assignable(permissionGroupId, {}),
      }),
    );
  }

  await Promise.all(tasks);
}

export function usePermissionGroupListQuery(filtersRef: MaybeRefOrGetter<PermissionGroupFilters>) {
  const repository = appContainer.resolve(PermissionGroupRepository);
  const filters = computed(() => toValue(filtersRef));

  return useQuery({
    queryKey: computed(() => adminKeys.permissionGroups.list(filters.value)),
    queryFn: () => repository.search(filters.value),
  });
}

export function usePermissionGroupDetailQuery(groupIdRef: MaybeRefOrGetter<number | null | undefined>) {
  const repository = appContainer.resolve(PermissionGroupRepository);
  const groupId = computed(() => Number(toValue(groupIdRef) ?? 0));

  return useQuery({
    queryKey: computed(() => adminKeys.permissionGroups.detail(groupId.value)),
    queryFn: () => repository.findById(groupId.value),
    enabled: computed(() => groupId.value > 0),
  });
}

export function usePermissionGroupCatalogQuery() {
  const repository = appContainer.resolve(PermissionGroupRepository);

  return useQuery({
    queryKey: adminKeys.permissionGroups.catalog(),
    queryFn: () => repository.fetchCatalog(),
  });
}

export function useAssignableAccountsQuery(
  groupIdRef: MaybeRefOrGetter<number | null | undefined>,
  keywordRef: MaybeRefOrGetter<string | undefined>,
) {
  const repository = appContainer.resolve(PermissionGroupRepository);
  const groupId = computed(() => Number(toValue(groupIdRef) ?? 0));
  const keyword = computed(() => toValue(keywordRef) ?? '');

  return useQuery({
    queryKey: computed(() =>
      adminKeys.accounts.assignable(groupId.value, { keyword: keyword.value }),
    ),
    queryFn: () => repository.searchAssignableAccounts(groupId.value, keyword.value),
    enabled: computed(() => groupId.value > 0),
  });
}

export function useCreatePermissionGroupMutation() {
  const repository = appContainer.resolve(PermissionGroupRepository);

  return useMutation({
    mutationFn: (payload: PermissionGroupUpsertPayload) => repository.create(payload),
    onSuccess: async (createdId) => {
      await invalidatePermissionGroupSideEffects(createdId);
    },
  });
}

export function useUpdatePermissionGroupMutation() {
  const repository = appContainer.resolve(PermissionGroupRepository);

  return useMutation({
    mutationFn: (variables: { id: number; payload: PermissionGroupUpsertPayload }) =>
      repository.update(variables.id, variables.payload),
    onSuccess: async (_data, variables) => {
      await invalidatePermissionGroupSideEffects(variables.id);
    },
  });
}

export function useDeletePermissionGroupMutation() {
  const repository = appContainer.resolve(PermissionGroupRepository);

  return useMutation({
    mutationFn: (id: number) => repository.delete(id),
    onSuccess: async (_data, id) => {
      await invalidatePermissionGroupSideEffects(id);
    },
  });
}

export function useAssignPermissionGroupAccountMutation() {
  const repository = appContainer.resolve(PermissionGroupRepository);

  return useMutation({
    mutationFn: (variables: { permissionGroupId: number; accountId: number }) =>
      repository.assignAccount(variables.permissionGroupId, variables.accountId),
    onSuccess: async (_data, variables) => {
      await invalidatePermissionGroupSideEffects(variables.permissionGroupId);
    },
  });
}

export function useUnassignPermissionGroupAccountMutation() {
  const repository = appContainer.resolve(PermissionGroupRepository);

  return useMutation({
    mutationFn: (variables: { permissionGroupId: number; accountId: number }) =>
      repository.unassignAccount(variables.permissionGroupId, variables.accountId),
    onSuccess: async (_data, variables) => {
      await invalidatePermissionGroupSideEffects(variables.permissionGroupId);
    },
  });
}
