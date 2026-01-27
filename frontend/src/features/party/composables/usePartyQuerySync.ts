/**
 * 협력사 목록 URL 쿼리 동기화 Composable
 *
 * 협력사 목록의 페이징, 정렬, 검색 상태를 URL 쿼리와 동기화합니다.
 */

import { nextTick, watch, type Ref } from 'vue';
import { useRoute, useRouter, type LocationQuery } from 'vue-router';
import { useDebounceFn } from '@vueuse/core';
import type { ColumnFiltersState, SortingState } from '@tanstack/vue-table';
import type { PartySearchParams } from '@/features/party/models/partyListItem';
import {
  deserializePagination,
  deserializeSingleFilter,
  deserializeSorting,
  serializePagination,
} from '@/core/utils/queryParams';

export interface UsePartyQuerySyncOptions {
  /** 현재 페이지 번호 (1-based) */
  page: Ref<number>;
  /** 페이지 크기 */
  pageSize: Ref<number>;
  /** 정렬 상태 */
  sorting: Ref<SortingState>;
  /** 컬럼 필터 상태 */
  columnFilters: Ref<ColumnFiltersState>;
  /** 협력사 목록 로딩 함수 */
  onLoadParties: () => Promise<void>;
}

export function usePartyQuerySync(options: UsePartyQuerySyncOptions) {
  const { page, pageSize, sorting, columnFilters, onLoadParties } = options;

  const route = useRoute();
  const router = useRouter();

  let isApplyingRoute = false;
  let isUpdatingRoute = false;

  function buildSearchParams(): PartySearchParams {
    const params: PartySearchParams = {
      page: page.value,
      size: pageSize.value,
    };

    const filterMap = new Map<string, unknown>(
      columnFilters.value.map((filter) => [filter.id, filter.value]),
    );

    const nameFilter = filterMap.get('name');
    if (typeof nameFilter === 'string' && nameFilter.trim().length > 0) {
      params.name = nameFilter.trim();
    }

    const sortState = sorting.value[0];
    if (sortState && sortState.id) {
      params.sort = `${sortState.id},${sortState.desc ? 'desc' : 'asc'}`;
    }

    return params;
  }

  function buildQueryFromState(): Record<string, string> {
    const params = buildSearchParams();
    const query: Record<string, string> = serializePagination(params.page, params.size);

    if (params.name) {
      query.name = params.name;
    }
    if (params.sort) {
      query.sort = params.sort;
    }

    return query;
  }

  function applyRouteQuery(rawQuery: LocationQuery) {
    isApplyingRoute = true;

    const pagination = deserializePagination(rawQuery);
    page.value = pagination.page;
    pageSize.value = pagination.size;

    sorting.value = deserializeSorting(rawQuery);

    const filters: ColumnFiltersState = [];
    const name = deserializeSingleFilter(rawQuery, 'name');
    if (name) {
      filters.push({ id: 'name', value: name });
    }
    columnFilters.value = filters;

    nextTick(() => {
      isApplyingRoute = false;
      updateRouteFromState();
      onLoadParties();
    });
  }

  function updateRouteFromState() {
    if (isApplyingRoute) {
      return;
    }

    const nextQuery = buildQueryFromState();
    const currentQuery = getRouteQueryRecord();

    if (queriesEqual(nextQuery, currentQuery)) {
      return;
    }

    isUpdatingRoute = true;
    router.replace({ query: nextQuery }).finally(() => {
      isUpdatingRoute = false;
    });
  }

  function getRouteQueryRecord(): Record<string, string> {
    const record: Record<string, string> = {};
    Object.entries(route.query).forEach(([key, value]) => {
      if (Array.isArray(value)) {
        const joined = value.join(',');
        if (joined) {
          record[key] = joined;
        }
      } else if (value !== null && value !== undefined) {
        const stringified = String(value);
        if (stringified.length > 0) {
          record[key] = stringified;
        }
      }
    });
    return record;
  }

  function queriesEqual(a: Record<string, string>, b: Record<string, string>): boolean {
    const keys = new Set([...Object.keys(a), ...Object.keys(b)]);
    for (const key of keys) {
      if ((a[key] ?? '') !== (b[key] ?? '')) {
        return false;
      }
    }
    return true;
  }

  const triggerFilterFetch = useDebounceFn(() => {
    onLoadParties();
  }, 300);

  watch(
    columnFilters,
    () => {
      if (isUpdatingRoute || isApplyingRoute) {
        return;
      }
      page.value = 1;
      updateRouteFromState();
      triggerFilterFetch();
    },
    { deep: true },
  );

  watch(
    sorting,
    () => {
      if (isUpdatingRoute || isApplyingRoute) {
        return;
      }
      page.value = 1;
      updateRouteFromState();
      onLoadParties();
    },
    { deep: true },
  );

  watch(page, () => {
    if (isUpdatingRoute || isApplyingRoute) {
      return;
    }
    updateRouteFromState();
    onLoadParties();
  });

  watch(pageSize, () => {
    if (isUpdatingRoute || isApplyingRoute) {
      return;
    }
    page.value = 1;
    updateRouteFromState();
    onLoadParties();
  });

  watch(
    () => route.query,
    (newQuery, oldQuery) => {
      if (isUpdatingRoute || isApplyingRoute) {
        return;
      }
      if (JSON.stringify(newQuery) !== JSON.stringify(oldQuery)) {
        applyRouteQuery(newQuery);
      }
    },
    { deep: true },
  );

  return {
    buildSearchParams,
    applyRouteQuery,
  };
}
