/**
 * 협력사 목록 URL 쿼리 동기화 Composable
 *
 * 협력사 목록의 페이징, 정렬, 검색 상태를 URL 쿼리와 동기화합니다.
 */

import { nextTick, watch, type Ref } from 'vue';
import { useRoute, useRouter, type LocationQuery } from 'vue-router';
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
}

export function usePartyQuerySync(options: UsePartyQuerySyncOptions) {
  const { page, pageSize, sorting, columnFilters } = options;

  const route = useRoute();
  const router = useRouter();

  let isApplyingRoute = false;
  let isUpdatingRoute = false;
  let pendingRouteQuery: LocationQuery | null = null;
  let hasPendingStateSync = false;

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
      if (pendingRouteQuery) {
        const queuedQuery = pendingRouteQuery;
        pendingRouteQuery = null;
        applyRouteQuery(queuedQuery);
        return;
      }
      if (hasPendingStateSync) {
        hasPendingStateSync = false;
      }
      updateRouteFromState();
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
      if (pendingRouteQuery) {
        const queuedQuery = pendingRouteQuery;
        pendingRouteQuery = null;
        applyRouteQuery(queuedQuery);
        return;
      }
      if (hasPendingStateSync) {
        hasPendingStateSync = false;
        updateRouteFromState();
      }
    });
  }

  function scheduleRouteSyncFromState() {
    if (isApplyingRoute || isUpdatingRoute) {
      hasPendingStateSync = true;
      return;
    }
    updateRouteFromState();
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

  if (Object.keys(route.query).length > 0) {
    applyRouteQuery({ ...route.query });
  }

  watch(
    columnFilters,
    () => {
      if (isApplyingRoute || isUpdatingRoute) {
        return;
      }
      page.value = 1;
      scheduleRouteSyncFromState();
    },
    { deep: true },
  );

  watch(
    sorting,
    () => {
      if (isApplyingRoute || isUpdatingRoute) {
        return;
      }
      page.value = 1;
      scheduleRouteSyncFromState();
    },
    { deep: true },
  );

  watch(page, () => {
    if (isApplyingRoute) {
      return;
    }
    scheduleRouteSyncFromState();
  });

  watch(pageSize, () => {
    if (isApplyingRoute || isUpdatingRoute) {
      return;
    }
    page.value = 1;
    scheduleRouteSyncFromState();
  });

  watch(
    () => route.query,
    (newQuery) => {
      if (isApplyingRoute || isUpdatingRoute) {
        pendingRouteQuery = { ...newQuery };
        return;
      }
      applyRouteQuery(newQuery);
    },
    { deep: true },
  );

  return {
    buildSearchParams,
    applyRouteQuery,
  };
}
