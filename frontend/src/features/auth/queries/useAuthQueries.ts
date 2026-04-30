import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useMutation, useQuery } from '@tanstack/vue-query';
import { appContainer } from '@/core/di/container';
import AuthRepository from '@/features/auth/repository/AuthRepository';
import { authKeys, queryClient } from '@/core/query';

export function useAuthMeQuery(enabledRef: MaybeRefOrGetter<boolean> = true) {
  const repository = appContainer.resolve(AuthRepository);

  return useQuery({
    queryKey: authKeys.me(),
    queryFn: () => repository.fetchMe(),
    enabled: computed(() => Boolean(toValue(enabledRef))),
  });
}

export function useLoginMutation() {
  const repository = appContainer.resolve(AuthRepository);

  return useMutation({
    mutationFn: (payload: { username: string; password: string }) => repository.login(payload),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: authKeys.me() });
    },
  });
}

export function useLogoutMutation() {
  const repository = appContainer.resolve(AuthRepository);

  return useMutation({
    mutationFn: () => repository.logout(),
    onSuccess: async () => {
      await queryClient.removeQueries({ queryKey: authKeys.all });
    },
  });
}

export function useChangePasswordMutation() {
  const repository = appContainer.resolve(AuthRepository);

  return useMutation({
    mutationFn: (payload: { currentPassword: string; newPassword: string }) =>
      repository.changePassword(payload),
  });
}

export function useRequestRegistrationMutation() {
  const repository = appContainer.resolve(AuthRepository);

  return useMutation({
    mutationFn: (payload: { email: string }) => repository.requestRegistration(payload),
  });
}

export function useConfirmRegistrationMutation() {
  const repository = appContainer.resolve(AuthRepository);

  return useMutation({
    mutationFn: (payload: { token: string; password: string }) =>
      repository.confirmRegistration(payload),
  });
}

export function useRequestPasswordResetMutation() {
  const repository = appContainer.resolve(AuthRepository);

  return useMutation({
    mutationFn: (payload: { email: string }) => repository.requestPasswordReset(payload),
  });
}

export function useConfirmPasswordResetMutation() {
  const repository = appContainer.resolve(AuthRepository);

  return useMutation({
    mutationFn: (payload: { token: string; password: string }) =>
      repository.confirmPasswordReset(payload),
  });
}
