import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { keepPreviousData, useMutation, useQuery } from '@tanstack/vue-query';
import { appContainer } from '@/core/di/container';
import { partyKeys, projectKeys, queryClient } from '@/core/query';
import PartyRepository from '@/features/party/repository/PartyRepository';
import type { PartySearchParams } from '@/features/party/models/partyListItem';
import type { PartyCreateData, PartyUpdateData } from '@/features/party/models/partyDetail';
import type { PartyOverviewSummaryParams } from '@/features/party/repository/PartyRepository';

async function invalidatePartySideEffects(partyId?: number) {
  const tasks: Promise<unknown>[] = [
    queryClient.invalidateQueries({ queryKey: partyKeys.all }),
    queryClient.invalidateQueries({ queryKey: projectKeys.all }),
  ];

  if (partyId && partyId > 0) {
    tasks.push(queryClient.invalidateQueries({ queryKey: partyKeys.detail(partyId) }));
    tasks.push(queryClient.invalidateQueries({ queryKey: partyKeys.projects(partyId) }));
  }

  await Promise.all(tasks);
}

export function usePartyListQuery(paramsRef: MaybeRefOrGetter<PartySearchParams>) {
  const repository = appContainer.resolve(PartyRepository);
  const params = computed(() => toValue(paramsRef));

  return useQuery({
    queryKey: computed(() =>
      partyKeys.list((params.value ?? {}) as unknown as Record<string, unknown>),
    ),
    queryFn: () => repository.list(params.value),
    placeholderData: keepPreviousData,
  });
}

export function usePartyOverviewSummaryQuery(
  paramsRef: MaybeRefOrGetter<PartyOverviewSummaryParams>,
) {
  const repository = appContainer.resolve(PartyRepository);
  const params = computed(() => toValue(paramsRef));

  return useQuery({
    queryKey: computed(() => partyKeys.summary(params.value ?? {})),
    queryFn: () => repository.fetchOverviewSummary(params.value),
    placeholderData: keepPreviousData,
  });
}

export function usePartyDetailQuery(partyIdRef: MaybeRefOrGetter<number | null | undefined>) {
  const repository = appContainer.resolve(PartyRepository);
  const partyId = computed(() => Number(toValue(partyIdRef) ?? 0));

  return useQuery({
    queryKey: computed(() => partyKeys.detail(partyId.value)),
    queryFn: () => repository.find(partyId.value),
    enabled: computed(() => partyId.value > 0),
  });
}

export function usePartyProjectsQuery(partyIdRef: MaybeRefOrGetter<number | null | undefined>) {
  const repository = appContainer.resolve(PartyRepository);
  const partyId = computed(() => Number(toValue(partyIdRef) ?? 0));

  return useQuery({
    queryKey: computed(() => partyKeys.projects(partyId.value)),
    queryFn: () => repository.fetchProjects(partyId.value),
    enabled: computed(() => partyId.value > 0),
  });
}

export function usePartyOptionsQuery() {
  const repository = appContainer.resolve(PartyRepository);

  return useQuery({
    queryKey: partyKeys.options(),
    queryFn: () => repository.fetchAll(),
  });
}

export function useCreatePartyMutation() {
  const repository = appContainer.resolve(PartyRepository);

  return useMutation({
    mutationFn: (data: PartyCreateData) => repository.create(data),
    onSuccess: async (created) => {
      await invalidatePartySideEffects(created.partyId);
    },
  });
}

export function useUpdatePartyMutation() {
  const repository = appContainer.resolve(PartyRepository);

  return useMutation({
    mutationFn: (variables: { partyId: number; data: PartyUpdateData }) =>
      repository.update(variables.partyId, variables.data),
    onSuccess: async (updated) => {
      await invalidatePartySideEffects(updated.partyId);
    },
  });
}

export function useDeletePartyMutation() {
  const repository = appContainer.resolve(PartyRepository);

  return useMutation({
    mutationFn: (partyId: number) => repository.delete(partyId),
    onSuccess: async (_data, partyId) => {
      await invalidatePartySideEffects(partyId);
    },
  });
}
