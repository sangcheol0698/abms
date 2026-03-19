import { nextTick, watch, type Ref } from 'vue';
import { useRoute, useRouter, type LocationQuery } from 'vue-router';
import type { ColumnFiltersState } from '@tanstack/vue-table';
import {
  deserializePagination,
  deserializeArrayFilter,
  deserializeSingleFilter,
  serializePagination,
  serializeArrayFilter,
} from '@/core/utils/queryParams';

const QUERY_KEYS = {
  page: 'projectAssignmentPage',
  size: 'projectAssignmentSize',
  name: 'projectAssignmentName',
  statuses: 'projectAssignmentStatuses',
  roles: 'projectAssignmentRoles',
} as const;

export interface UseProjectAssignmentQuerySyncOptions {
  page: Ref<number>;
  pageSize: Ref<number>;
  columnFilters: Ref<ColumnFiltersState>;
}

export function useProjectAssignmentQuerySync(options: UseProjectAssignmentQuerySyncOptions) {
  const { page, pageSize, columnFilters } = options;
  const route = useRoute();
  const router = useRouter();

  let isApplyingRoute = false;
  let isUpdatingRoute = false;
  let pendingRouteQuery: LocationQuery | null = null;
  let hasPendingStateSync = false;

  function buildQueryFromState(): Record<string, string> {
    const filterMap = new Map<string, unknown>(columnFilters.value.map((filter) => [filter.id, filter.value]));
    const query: Record<string, string> = {
      [QUERY_KEYS.page]: serializePagination(page.value, pageSize.value).page,
      [QUERY_KEYS.size]: serializePagination(page.value, pageSize.value).size,
    };

    const name = filterMap.get('employeeName');
    if (typeof name === 'string' && name.trim().length > 0) {
      query[QUERY_KEYS.name] = name.trim();
    }

    const statuses = toArray(filterMap.get('assignmentStatus'));
    if (statuses.length > 0) {
      query[QUERY_KEYS.statuses] = serializeArrayFilter(statuses);
    }

    const roles = toArray(filterMap.get('role'));
    if (roles.length > 0) {
      query[QUERY_KEYS.roles] = serializeArrayFilter(roles);
    }

    return query;
  }

  function applyRouteQuery(rawQuery: LocationQuery) {
    isApplyingRoute = true;

    const pagination = deserializePagination({
      page: rawQuery[QUERY_KEYS.page] ?? rawQuery.page,
      size: rawQuery[QUERY_KEYS.size] ?? rawQuery.size,
    });
    page.value = pagination.page;
    pageSize.value = pagination.size;

    const filters: ColumnFiltersState = [];
    const name = deserializeSingleFilter(rawQuery, QUERY_KEYS.name);
    if (name) {
      filters.push({ id: 'employeeName', value: name });
    }

    const statuses = deserializeArrayFilter(rawQuery[QUERY_KEYS.statuses] as string | string[] | undefined);
    if (statuses.length) {
      filters.push({ id: 'assignmentStatus', value: statuses });
    }

    const roles = deserializeArrayFilter(rawQuery[QUERY_KEYS.roles] as string | string[] | undefined);
    if (roles.length) {
      filters.push({ id: 'role', value: roles });
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
    [page, pageSize, columnFilters],
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
