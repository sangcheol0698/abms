import { nextTick, watch, type Ref } from 'vue';
import { useRoute, useRouter, type LocationQuery } from 'vue-router';
import type { ColumnFiltersState } from '@tanstack/vue-table';
import {
  deserializeArrayFilter,
  deserializeSingleFilter,
  serializeArrayFilter,
} from '@/core/utils/queryParams';

const QUERY_KEYS = {
  memo: 'projectRevenueMemo',
  statuses: 'projectRevenueStatuses',
  types: 'projectRevenueTypes',
} as const;

export interface UseProjectRevenuePlanQuerySyncOptions {
  columnFilters: Ref<ColumnFiltersState>;
}

export function useProjectRevenuePlanQuerySync(options: UseProjectRevenuePlanQuerySyncOptions) {
  const { columnFilters } = options;
  const route = useRoute();
  const router = useRouter();

  let isApplyingRoute = false;
  let isUpdatingRoute = false;
  let pendingRouteQuery: LocationQuery | null = null;
  let hasPendingStateSync = false;

  function buildQueryFromState(): Record<string, string> {
    const filterMap = new Map<string, unknown>(columnFilters.value.map((filter) => [filter.id, filter.value]));
    const query: Record<string, string> = {};

    const memo = filterMap.get('memo');
    if (typeof memo === 'string' && memo.trim().length > 0) {
      query[QUERY_KEYS.memo] = memo.trim();
    }

    const statuses = toArray(filterMap.get('status'));
    if (statuses.length > 0) {
      query[QUERY_KEYS.statuses] = serializeArrayFilter(statuses);
    }

    const types = toArray(filterMap.get('type'));
    if (types.length > 0) {
      query[QUERY_KEYS.types] = serializeArrayFilter(types);
    }

    return query;
  }

  function applyRouteQuery(rawQuery: LocationQuery) {
    isApplyingRoute = true;

    const filters: ColumnFiltersState = [];
    const memo = deserializeSingleFilter(rawQuery, QUERY_KEYS.memo);
    if (memo) {
      filters.push({ id: 'memo', value: memo });
    }

    const statuses = deserializeArrayFilter(rawQuery[QUERY_KEYS.statuses] as string | string[] | undefined);
    if (statuses.length) {
      filters.push({ id: 'status', value: statuses });
    }

    const types = deserializeArrayFilter(rawQuery[QUERY_KEYS.types] as string | string[] | undefined);
    if (types.length) {
      filters.push({ id: 'type', value: types });
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
    const currentQuery = getTrackedRouteQueryRecord();
    if (queriesEqual(nextQuery, currentQuery)) {
      return;
    }

    const mergedQuery = { ...route.query } as Record<string, string>;
    Object.values(QUERY_KEYS).forEach((key) => {
      delete mergedQuery[key];
    });
    Object.assign(mergedQuery, nextQuery);

    isUpdatingRoute = true;
    router.replace({ query: mergedQuery }).finally(() => {
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

  function getTrackedRouteQueryRecord(): Record<string, string> {
    const record: Record<string, string> = {};
    Object.values(QUERY_KEYS).forEach((key) => {
      const value = route.query[key];
      if (Array.isArray(value)) {
        const joined = value.join(',');
        if (joined) {
          record[key] = joined;
        }
      } else if (value !== undefined && value !== null) {
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

  if (Object.keys(route.query).some((key) => Object.values(QUERY_KEYS).includes(key as never))) {
    applyRouteQuery({ ...route.query });
  }

  watch(
    columnFilters,
    () => {
      if (isApplyingRoute || isUpdatingRoute) {
        hasPendingStateSync = true;
        return;
      }
      updateRouteFromState();
    },
    { deep: true },
  );

  watch(
    () => route.query,
    (query) => {
      if (isUpdatingRoute) {
        return;
      }
      if (isApplyingRoute) {
        pendingRouteQuery = { ...query };
        return;
      }
      applyRouteQuery({ ...query });
    },
  );
}
