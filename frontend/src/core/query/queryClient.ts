import { QueryClient, type DefaultOptions } from '@tanstack/vue-query';
import HttpError from '@/core/http/HttpError';

function shouldRetry(failureCount: number, error: unknown): boolean {
  if (!(error instanceof HttpError)) {
    return false;
  }

  const status = error.status;
  if (typeof status !== 'number') {
    return false;
  }

  if (status >= 500 && status < 600) {
    return failureCount < 1;
  }

  return false;
}

const defaultOptions: DefaultOptions = {
  queries: {
    staleTime: 30_000,
    gcTime: 300_000,
    refetchOnWindowFocus: false,
    refetchOnReconnect: true,
    retry: shouldRetry,
  },
  mutations: {
    retry: false,
  },
};

export const queryClient = new QueryClient({
  defaultOptions,
});
