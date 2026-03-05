import { ref } from 'vue';

export interface MockQueryState<T> {
  data?: T;
  isLoading?: boolean;
  isFetching?: boolean;
  error?: unknown;
}

export function createMockQueryState<T>(state: MockQueryState<T> = {}) {
  return {
    data: ref(state.data),
    isLoading: ref(Boolean(state.isLoading)),
    isFetching: ref(Boolean(state.isFetching)),
    error: ref(state.error ?? null),
  };
}
