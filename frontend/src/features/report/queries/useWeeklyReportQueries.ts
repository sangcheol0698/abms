import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query';
import { computed, type MaybeRefOrGetter, toValue } from 'vue';
import { appContainer } from '@/core/di/container';
import { reportKeys } from '@/core/query';
import WeeklyReportRepository from '@/features/report/repository/WeeklyReportRepository';
import type { WeeklyReportDraftSummary, WeeklyReportGenerateRequest } from '@/features/report/models/weeklyReport';
import { isWeeklyReportTerminalStatus } from '@/features/report/models/weeklyReport';

export function useWeeklyReportDraftListQuery() {
  const repository = appContainer.resolve(WeeklyReportRepository);

  return useQuery({
    queryKey: reportKeys.weeklyDrafts(),
    queryFn: () => repository.listDrafts(),
    refetchInterval: (query) => {
      const data = query.state.data as WeeklyReportDraftSummary[] | undefined;
      return data?.some((draft) => !isWeeklyReportTerminalStatus(draft.status)) ? 2000 : false;
    },
  });
}

export function useWeeklyReportDraftDetailQuery(draftIdRef: MaybeRefOrGetter<number | null | undefined>) {
  const repository = appContainer.resolve(WeeklyReportRepository);
  const draftId = computed(() => Number(toValue(draftIdRef) ?? 0));

  return useQuery({
    queryKey: computed(() => reportKeys.weeklyDraftDetail(draftId.value)),
    queryFn: () => repository.getDraftDetail(draftId.value),
    enabled: computed(() => draftId.value > 0),
    refetchInterval: (query) => {
      const data = query.state.data;
      if (!data || isWeeklyReportTerminalStatus((data as { status: string }).status)) {
        return false;
      }
      return 1500;
    },
  });
}

export function useCreateWeeklyReportDraftMutation() {
  const queryClient = useQueryClient();
  const repository = appContainer.resolve(WeeklyReportRepository);

  return useMutation({
    mutationFn: (payload: WeeklyReportGenerateRequest) => repository.createDraft(payload),
    onSuccess: async (detail) => {
      await queryClient.invalidateQueries({ queryKey: reportKeys.weeklyDrafts() });
      queryClient.setQueryData(reportKeys.weeklyDraftDetail(detail.id), detail);
    },
  });
}

export function useRegenerateWeeklyReportDraftMutation() {
  const queryClient = useQueryClient();
  const repository = appContainer.resolve(WeeklyReportRepository);

  return useMutation({
    mutationFn: (draftId: number) => repository.regenerateDraft(draftId),
    onSuccess: async (detail) => {
      await queryClient.invalidateQueries({ queryKey: reportKeys.weeklyDrafts() });
      queryClient.setQueryData(reportKeys.weeklyDraftDetail(detail.id), detail);
    },
  });
}

export function useCancelWeeklyReportDraftMutation() {
  const queryClient = useQueryClient();
  const repository = appContainer.resolve(WeeklyReportRepository);

  return useMutation({
    mutationFn: (draftId: number) => repository.cancelDraft(draftId),
    onSuccess: async (detail) => {
      await queryClient.invalidateQueries({ queryKey: reportKeys.weeklyDrafts() });
      queryClient.setQueryData(reportKeys.weeklyDraftDetail(detail.id), detail);
    },
  });
}

export function useUpdateWeeklyReportDraftMutation() {
  const queryClient = useQueryClient();
  const repository = appContainer.resolve(WeeklyReportRepository);

  return useMutation({
    mutationFn: (variables: { draftId: number; title: string; reportMarkdown: string }) =>
      repository.updateDraft(variables.draftId, {
        title: variables.title,
        reportMarkdown: variables.reportMarkdown,
      }),
    onSuccess: async (detail) => {
      await queryClient.invalidateQueries({ queryKey: reportKeys.weeklyDrafts() });
      queryClient.setQueryData(reportKeys.weeklyDraftDetail(detail.id), detail);
    },
  });
}

export function useDeleteWeeklyReportDraftMutation() {
  const queryClient = useQueryClient();
  const repository = appContainer.resolve(WeeklyReportRepository);

  return useMutation({
    mutationFn: (draftId: number) => repository.deleteDraft(draftId),
    onSuccess: async (_data, draftId) => {
      await queryClient.invalidateQueries({ queryKey: reportKeys.weeklyDrafts() });
      queryClient.removeQueries({ queryKey: reportKeys.weeklyDraftDetail(draftId) });
    },
  });
}
