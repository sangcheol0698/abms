/**
 * URL 쿼리 파라미터 동기화 Composable
 *
 * 컴포넌트의 상태를 URL 쿼리와 양방향으로 동기화합니다.
 * 새로고침해도 상태가 유지됩니다.
 */

import { watch, type Ref } from 'vue';
import { useRoute, useRouter, type LocationQuery, type LocationQueryRaw } from 'vue-router';

export interface UseQuerySyncOptions<TState> {
  /** 동기화할 상태 ref */
  state: Ref<TState>;
  /** URL 쿼리 키 */
  queryKey: string;
  /** 상태를 쿼리 값으로 직렬화 */
  serialize: (state: TState) => string | undefined;
  /** 쿼리 값을 상태로 역직렬화 */
  deserialize: (value: unknown) => TState | undefined;
  /** 기본값 */
  defaultValue?: TState;
}

/**
 * 단일 상태와 URL 쿼리 파라미터를 동기화
 *
 * @example
 * ```typescript
 * const departmentId = ref<string | undefined>('dept-1');
 *
 * useQuerySync({
 *   state: departmentId,
 *   queryKey: 'departmentId',
 *   serialize: (value) => value,
 *   deserialize: (value) => {
 *     if (typeof value === 'string' && value.trim()) {
 *       return value.trim();
 *     }
 *     return undefined;
 *   },
 * });
 * ```
 */
export function useQuerySync<TState>(options: UseQuerySyncOptions<TState>) {
  const { state, queryKey, serialize, deserialize, defaultValue } = options;

  const route = useRoute();
  const router = useRouter();

  let isApplyingRoute = false;
  let isUpdatingRoute = false;

  // 초기값: URL에서 상태 복원
  const initialValue = deserialize(route.query[queryKey]);
  if (initialValue !== undefined) {
    state.value = initialValue;
  } else if (defaultValue !== undefined && state.value === undefined) {
    state.value = defaultValue;
  }

  // 상태 변경 시 URL 업데이트
  watch(
    state,
    (newValue) => {
      if (isApplyingRoute) {
        return;
      }

      updateRouteQuery(newValue);
    },
    { flush: 'post' },
  );

  // URL 변경 시 상태 업데이트
  watch(
    () => route.query[queryKey],
    (value) => {
      if (isUpdatingRoute) {
        return;
      }

      const newValue = deserialize(value);

      if (newValue === state.value) {
        return;
      }

      isApplyingRoute = true;
      if (newValue !== undefined) {
        state.value = newValue;
      } else if (defaultValue !== undefined) {
        state.value = defaultValue;
      }
      isApplyingRoute = false;
    },
  );

  /**
   * URL 쿼리 업데이트
   */
  function updateRouteQuery(value: TState) {
    const currentValue = deserialize(route.query[queryKey]);

    if (value === currentValue) {
      return;
    }

    const nextQuery: LocationQueryRaw = { ...route.query };

    const serialized = serialize(value);
    if (serialized !== undefined && serialized !== '') {
      nextQuery[queryKey] = serialized;
    } else {
      delete nextQuery[queryKey];
    }

    isUpdatingRoute = true;
    router
      .replace({ query: nextQuery })
      .finally(() => {
        isUpdatingRoute = false;
      })
      .catch((error) => {
        console.warn(`URL 쿼리 업데이트 실패 (${queryKey}):`, error);
      });
  }

  return {
    /** URL 쿼리에서 현재 상태 값 추출 */
    extractFromQuery: (query: LocationQuery) => deserialize(query[queryKey]),
    /** 상태를 URL 쿼리로 수동 업데이트 */
    updateQuery: updateRouteQuery,
  };
}
