import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useMutation, useQuery } from '@tanstack/vue-query';
import { chatKeys, queryClient } from '@/core/query';
import { useChatRepository } from '@/features/chat/repository/useChatRepository';

export async function invalidateChatSessions(sessionId?: string | null) {
  const tasks: Promise<unknown>[] = [
    queryClient.invalidateQueries({ queryKey: chatKeys.sessions() }),
  ];

  if (sessionId && sessionId.trim().length > 0) {
    tasks.push(queryClient.invalidateQueries({ queryKey: chatKeys.sessionDetail(sessionId) }));
  }

  await Promise.all(tasks);
}

export function useChatRecentSessionsQuery(limitRef: MaybeRefOrGetter<number> = 20) {
  const repository = useChatRepository();
  const limit = computed(() => Number(toValue(limitRef) || 20));

  return useQuery({
    queryKey: computed(() => chatKeys.recent(limit.value)),
    queryFn: () => repository.getRecentSessions(limit.value),
  });
}

export function useChatFavoriteSessionsQuery() {
  const repository = useChatRepository();

  return useQuery({
    queryKey: chatKeys.favorites(),
    queryFn: () => repository.getFavoriteSessions(),
  });
}

export function useChatSessionDetailQuery(sessionIdRef: MaybeRefOrGetter<string | null | undefined>) {
  const repository = useChatRepository();
  const sessionId = computed(() => String(toValue(sessionIdRef) ?? ''));

  return useQuery({
    queryKey: computed(() => chatKeys.sessionDetail(sessionId.value)),
    queryFn: () => repository.getSessionDetail(sessionId.value),
    enabled: computed(() => sessionId.value.length > 0),
  });
}

export function useRenameChatSessionMutation() {
  const repository = useChatRepository();

  return useMutation({
    mutationFn: (variables: { sessionId: string; title: string }) =>
      repository.updateSessionTitle(variables.sessionId, variables.title),
    onSuccess: async (_data, variables) => {
      await invalidateChatSessions(variables.sessionId);
    },
  });
}

export function useDeleteChatSessionMutation() {
  const repository = useChatRepository();

  return useMutation({
    mutationFn: (sessionId: string) => repository.deleteSession(sessionId),
    onSuccess: async (_data, sessionId) => {
      await invalidateChatSessions(sessionId);
    },
  });
}

export function useToggleChatFavoriteMutation() {
  const repository = useChatRepository();

  return useMutation({
    mutationFn: (sessionId: string) => repository.toggleFavorite(sessionId),
    onSuccess: async (_data, sessionId) => {
      await invalidateChatSessions(sessionId);
    },
  });
}
