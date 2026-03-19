import { nextTick, watch, type Ref } from 'vue';
import { useRoute, useRouter, type LocationQuery } from 'vue-router';
import type { ColumnFiltersState } from '@tanstack/vue-table';
import type { EmployeeProjectsSearchParams } from '@/features/employee/models/project';
import {
  deserializeArrayFilter,
  deserializePagination,
  deserializeSingleFilter,
  serializeArrayFilter,
  serializePagination,
} from '@/core/utils/queryParams';

const QUERY_KEYS = {
  page: 'employeeProjectPage',
  size: 'employeeProjectSize',
  name: 'employeeProjectName',
  assignmentStatuses: 'employeeProjectAssignmentStatuses',
  projectStatuses: 'employeeProjectStatuses',
} as const;

export interface UseEmployeeProjectsQuerySyncOptions {
  page: Ref<number>;
  pageSize: Ref<number>;
  columnFilters: Ref<ColumnFiltersState>;
}

export function useEmployeeProjectsQuerySync(options: UseEmployeeProjectsQuerySyncOptions) {
  const { page, pageSize, columnFilters } = options;

  const route = useRoute();
  const router = useRouter();

  let isApplyingRoute = false;
  let isUpdatingRoute = false;
  let pendingRouteQuery: LocationQuery | null = null;
  let hasPendingStateSync = false;

  function buildSearchParams(): EmployeeProjectsSearchParams {
    const params: EmployeeProjectsSearchParams = {
      page: page.value,
      size: pageSize.value,
    };

    const filterMap = new Map<string, unknown>(
      columnFilters.value.map((filter) => [filter.id, filter.value]),
    );

    const nameFilter = filterMap.get('projectName');
    if (typeof nameFilter === 'string' && nameFilter.trim().length > 0) {
      params.name = nameFilter.trim();
    }

    const assignmentStatuses = toArray(filterMap.get('assignmentStatus'));
    if (assignmentStatuses.length > 0) {
      params.assignmentStatuses = assignmentStatuses;
    }

    const projectStatuses = toArray(filterMap.get('projectStatus'));
    if (projectStatuses.length > 0) {
      params.projectStatuses = projectStatuses;
    }

    return params;
  }

  function buildQueryFromState(): Record<string, string> {
    const params = buildSearchParams();
    const pagination = serializePagination(params.page, params.size);
    const query: Record<string, string> = {
      [QUERY_KEYS.page]: pagination.page,
      [QUERY_KEYS.size]: pagination.size,
    };

    if (params.name) {
      query[QUERY_KEYS.name] = params.name;
    }
    if (params.assignmentStatuses?.length) {
      query[QUERY_KEYS.assignmentStatuses] = serializeArrayFilter(params.assignmentStatuses);
    }
    if (params.projectStatuses?.length) {
      query[QUERY_KEYS.projectStatuses] = serializeArrayFilter(params.projectStatuses);
    }

    return query;
  }

  function applyRouteQuery(rawQuery: LocationQuery) {
    isApplyingRoute = true;

    const pagination = deserializePagination({
      page: rawQuery[QUERY_KEYS.page],
      size: rawQuery[QUERY_KEYS.size],
    });
    page.value = pagination.page;
    pageSize.value = pagination.size;

    const filters: ColumnFiltersState = [];
    const name = deserializeSingleFilter({ [QUERY_KEYS.name]: rawQuery[QUERY_KEYS.name] }, QUERY_KEYS.name);
    if (name) {
      filters.push({ id: 'projectName', value: name });
    }

    const assignmentStatuses = deserializeArrayFilter(
      rawQuery[QUERY_KEYS.assignmentStatuses] as string | string[] | undefined,
    );
    if (assignmentStatuses.length) {
      filters.push({ id: 'assignmentStatus', value: assignmentStatuses });
    }

    const projectStatuses = deserializeArrayFilter(
      rawQuery[QUERY_KEYS.projectStatuses] as string | string[] | undefined,
    );
    if (projectStatuses.length) {
      filters.push({ id: 'projectStatus', value: projectStatuses });
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

  function scheduleRouteSyncFromState() {
    if (isApplyingRoute || isUpdatingRoute) {
      hasPendingStateSync = true;
      return;
    }
    updateRouteFromState();
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

  if (Object.keys(route.query).some((key) => Object.values(QUERY_KEYS).includes(key as never))) {
    applyRouteQuery({ ...route.query });
  }

  watch(
    [page, pageSize, columnFilters],
    () => {
      scheduleRouteSyncFromState();
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

  return {
    buildSearchParams,
  };
}
