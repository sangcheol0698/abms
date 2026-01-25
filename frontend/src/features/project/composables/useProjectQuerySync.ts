/**
 * 프로젝트 목록 URL 쿼리 동기화 Composable
 *
 * 프로젝트 목록의 페이징, 정렬, 필터 상태를 URL 쿼리와 동기화합니다.
 */

import { nextTick, watch, type Ref } from 'vue';
import { useRoute, useRouter, type LocationQuery } from 'vue-router';
import { useDebounceFn } from '@vueuse/core';
import type { ColumnFiltersState, SortingState } from '@tanstack/vue-table';
import type { ProjectSearchParams } from '@/features/project/models/projectListItem';
import {
  serializePagination,
  deserializePagination,
  deserializeSorting,
  serializeArrayFilter,
  deserializeArrayFilter,
  deserializeSingleFilter,
} from '@/core/utils/queryParams';

interface DateRange {
  start?: Date | string;
  end?: Date | string;
}

export interface UseProjectQuerySyncOptions {
  /** 현재 페이지 번호 (1-based) */
  page: Ref<number>;
  /** 페이지 크기 */
  pageSize: Ref<number>;
  /** 정렬 상태 */
  sorting: Ref<SortingState>;
  /** 컬럼 필터 상태 */
  columnFilters: Ref<ColumnFiltersState>;
  /** 날짜 필터 */
  dateRange: Ref<DateRange | null>;
  /** 프로젝트 목록 로딩 함수 */
  onLoadProjects: () => Promise<void>;
}

export function useProjectQuerySync(options: UseProjectQuerySyncOptions) {
  const { page, pageSize, sorting, columnFilters, dateRange, onLoadProjects } = options;

  const route = useRoute();
  const router = useRouter();

  let isApplyingRoute = false;
  let isUpdatingRoute = false;

  function buildSearchParams(): ProjectSearchParams {
    const params: ProjectSearchParams = {
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

    const statusFilter = toArray(filterMap.get('status'));
    if (statusFilter.length > 0) {
      params.statuses = statusFilter;
    }

    const partyFilter = toArray(filterMap.get('partyId'));
    if (partyFilter.length > 0) {
      params.partyIds = partyFilter.map(Number);
    }

    const start = toIsoDateString(dateRange.value?.start);
    const end = toIsoDateString(dateRange.value?.end);
    if (start) {
      params.startDate = start;
    }
    if (end) {
      params.endDate = end;
    }

    const sortState = sorting.value[0];
    if (sortState && sortState.id) {
      const sortId = mapSortId(sortState.id);
      if (sortId) {
        params.sort = `${sortId},${sortState.desc ? 'desc' : 'asc'}`;
      }
    }

    return params;
  }

  function buildQueryFromState(): Record<string, string> {
    const params = buildSearchParams();
    const query: Record<string, string> = serializePagination(params.page, params.size);

    if (params.name) {
      query.name = params.name;
    }
    if (params.statuses?.length) {
      query.statuses = serializeArrayFilter(params.statuses);
    }
    if (params.partyIds?.length) {
      query.partyIds = serializeArrayFilter(params.partyIds.map(String));
    }
    if (params.startDate) {
      query.startDate = params.startDate;
    }
    if (params.endDate) {
      query.endDate = params.endDate;
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

    sorting.value = mapSortingFromQuery(deserializeSorting(rawQuery));

    const filters: ColumnFiltersState = [];
    const name = deserializeSingleFilter(rawQuery, 'name');
    if (name) {
      filters.push({ id: 'name', value: name });
    }

    const statuses = deserializeArrayFilter(rawQuery.statuses as string | string[] | undefined);
    if (statuses.length) {
      filters.push({ id: 'status', value: statuses });
    }

    const partyIds = deserializeArrayFilter(rawQuery.partyIds as string | string[] | undefined);
    if (partyIds.length) {
      filters.push({ id: 'partyId', value: partyIds });
    }

    const startDate = deserializeSingleFilter(rawQuery, 'startDate');
    const endDate = deserializeSingleFilter(rawQuery, 'endDate');
    if (startDate && endDate) {
      dateRange.value = { start: startDate, end: endDate };
    } else {
      dateRange.value = null;
    }

    columnFilters.value = filters;

    nextTick(() => {
      isApplyingRoute = false;
      updateRouteFromState();
      onLoadProjects();
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

  function toArray(value: unknown): string[] {
    if (Array.isArray(value)) {
      return value.filter((item): item is string => typeof item === 'string');
    }
    if (typeof value === 'string') {
      return [value];
    }
    return [];
  }

  function toIsoDateString(value: unknown): string | undefined {
    if (!value) {
      return undefined;
    }
    if (typeof value === 'string') {
      return value.length >= 10 ? value.slice(0, 10) : value;
    }
    if (value instanceof Date && !Number.isNaN(value.getTime())) {
      return value.toISOString().slice(0, 10);
    }
    if (typeof value === 'object' && value !== null) {
      const maybeDate = value as { year?: number; month?: number; day?: number };
      if (
        typeof maybeDate.year === 'number' &&
        typeof maybeDate.month === 'number' &&
        typeof maybeDate.day === 'number'
      ) {
        const year = maybeDate.year;
        const month = String(maybeDate.month).padStart(2, '0');
        const day = String(maybeDate.day).padStart(2, '0');
        return `${year}-${month}-${day}`;
      }
    }
    return undefined;
  }

  function mapSortId(id: string): string | null {
    if (id === 'period') {
      return 'startDate';
    }
    return id;
  }

  function mapSortingFromQuery(querySorting: SortingState): SortingState {
    if (querySorting.length === 0) {
      return [];
    }
    const [first] = querySorting;
    if (!first?.id) {
      return [];
    }
    const mappedId = first.id === 'startDate' || first.id === 'endDate' ? 'period' : first.id;
    return [{ id: mappedId, desc: first.desc }];
  }

  const triggerFilterFetch = useDebounceFn(() => {
    onLoadProjects();
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
    dateRange,
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
      onLoadProjects();
    },
    { deep: true },
  );

  watch(page, () => {
    if (isUpdatingRoute || isApplyingRoute) {
      return;
    }
    updateRouteFromState();
    onLoadProjects();
  });

  watch(pageSize, () => {
    if (isUpdatingRoute || isApplyingRoute) {
      return;
    }
    page.value = 1;
    updateRouteFromState();
    onLoadProjects();
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
