/**
 * 직원 목록 URL 쿼리 동기화 Composable
 *
 * 직원 목록의 페이징, 정렬, 필터 상태를 URL 쿼리와 동기화합니다.
 * 새로고침해도 상태가 유지됩니다.
 */

import { nextTick, watch, type Ref } from 'vue';
import { useRoute, useRouter, type LocationQuery } from 'vue-router';
import { useDebounceFn } from '@vueuse/core';
import type { ColumnFiltersState, SortingState } from '@tanstack/vue-table';
import type { EmployeeSearchParams } from '@/features/employee/models/employeeListItem';
import {
  serializePagination,
  deserializePagination,
  deserializeSorting,
  serializeArrayFilter,
  deserializeArrayFilter,
  deserializeSingleFilter,
} from '@/core/utils/queryParams';

export interface UseEmployeeQuerySyncOptions {
  /** 현재 페이지 번호 (1-based) */
  page: Ref<number>;
  /** 페이지 크기 */
  pageSize: Ref<number>;
  /** 정렬 상태 */
  sorting: Ref<SortingState>;
  /** 컬럼 필터 상태 */
  columnFilters: Ref<ColumnFiltersState>;
  /** 직원 목록 로딩 함수 */
  onLoadEmployees: () => Promise<void>;
}

/**
 * 직원 목록 URL 쿼리 동기화 Composable
 *
 * @param options - 동기화 옵션
 * @returns 쿼리 동기화 관련 함수들
 *
 * @example
 * ```typescript
 * const { buildSearchParams, applyRouteQuery } = useEmployeeQuerySync({
 *   page,
 *   pageSize,
 *   sorting,
 *   columnFilters,
 *   onLoadEmployees: loadEmployees,
 * });
 *
 * // 마운트 시 URL에서 상태 복원
 * onMounted(() => {
 *   if (Object.keys(route.query).length > 0) {
 *     applyRouteQuery(route.query);
 *   }
 * });
 * ```
 */
export function useEmployeeQuerySync(options: UseEmployeeQuerySyncOptions) {
  const { page, pageSize, sorting, columnFilters, onLoadEmployees } = options;

  const route = useRoute();
  const router = useRouter();

  let isApplyingRoute = false;
  let isUpdatingRoute = false;

  /**
   * 현재 상태를 API 요청 파라미터로 변환
   */
  function buildSearchParams(): EmployeeSearchParams {
    const params: EmployeeSearchParams = {
      page: page.value,
      size: pageSize.value,
    };

    // 필터 맵 생성
    const filterMap = new Map<string, unknown>(
      columnFilters.value.map((filter) => [filter.id, filter.value]),
    );

    // 이름 검색
    const nameFilter = filterMap.get('name');
    if (typeof nameFilter === 'string' && nameFilter.trim().length > 0) {
      params.name = nameFilter.trim();
    }

    // 상태 필터
    const statusFilter = toArray(filterMap.get('status'));
    if (statusFilter.length > 0) {
      params.statuses = statusFilter;
    }

    // 타입 필터
    const typeFilter = toArray(filterMap.get('type'));
    if (typeFilter.length > 0) {
      params.types = typeFilter;
    }

    // 등급 필터
    const gradeFilter = toArray(filterMap.get('grade'));
    if (gradeFilter.length > 0) {
      params.grades = gradeFilter;
    }

    // 직급 필터
    const positionFilter = toArray(filterMap.get('position'));
    if (positionFilter.length > 0) {
      params.positions = positionFilter;
    }

    // 부서 필터
    const departmentFilter = toArray(filterMap.get('departmentId'));
    if (departmentFilter.length > 0) {
      params.departmentIds = departmentFilter.map(Number);
    }

    // 정렬
    const sortState = sorting.value[0];
    if (sortState && sortState.id) {
      params.sort = `${sortState.id},${sortState.desc ? 'desc' : 'asc'}`;
    }

    return params;
  }

  /**
   * 현재 상태를 URL 쿼리 파라미터로 변환
   */
  function buildQueryFromState(): Record<string, string> {
    const params = buildSearchParams();

    // 페이징
    const query: Record<string, string> = serializePagination(params.page, params.size);

    // 이름 검색
    if (params.name) {
      query.name = params.name;
    }

    // 배열 필터들
    if (params.statuses?.length) {
      query.statuses = serializeArrayFilter(params.statuses);
    }
    if (params.types?.length) {
      query.types = serializeArrayFilter(params.types);
    }
    if (params.grades?.length) {
      query.grades = serializeArrayFilter(params.grades);
    }
    if (params.positions?.length) {
      query.positions = serializeArrayFilter(params.positions);
    }
    if (params.departmentIds?.length) {
      query.departmentIds = serializeArrayFilter(params.departmentIds.map(String));
    }

    // 정렬
    if (params.sort) {
      query.sort = params.sort;
    }

    return query;
  }

  /**
   * URL 쿼리를 현재 상태로 적용
   */
  function applyRouteQuery(rawQuery: LocationQuery) {
    isApplyingRoute = true;

    // 페이징
    const pagination = deserializePagination(rawQuery);
    page.value = pagination.page;
    pageSize.value = pagination.size;

    // 정렬
    sorting.value = deserializeSorting(rawQuery);

    // 필터 구성
    const filters: ColumnFiltersState = [];

    // 이름 검색
    const name = deserializeSingleFilter(rawQuery, 'name');
    if (name) {
      filters.push({ id: 'name', value: name });
    }

    // 상태 필터
    const statuses = deserializeArrayFilter(rawQuery.statuses as string | string[] | undefined);
    if (statuses.length) {
      filters.push({ id: 'status', value: statuses });
    }

    // 타입 필터
    const types = deserializeArrayFilter(rawQuery.types as string | string[] | undefined);
    if (types.length) {
      filters.push({ id: 'type', value: types });
    }

    // 등급 필터
    const grades = deserializeArrayFilter(rawQuery.grades as string | string[] | undefined);
    if (grades.length) {
      filters.push({ id: 'grade', value: grades });
    }

    // 직급 필터
    const positions = deserializeArrayFilter(rawQuery.positions as string | string[] | undefined);
    if (positions.length) {
      filters.push({ id: 'position', value: positions });
    }

    // 부서 필터
    const departments = deserializeArrayFilter(
      rawQuery.departmentIds as string | string[] | undefined,
    );
    if (departments.length) {
      filters.push({ id: 'departmentId', value: departments });
    }

    columnFilters.value = filters;

    // 상태 적용 후 데이터 로딩
    nextTick(() => {
      isApplyingRoute = false;
      updateRouteFromState();
      onLoadEmployees();
    });
  }

  /**
   * 현재 상태를 URL에 반영
   */
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

  /**
   * 현재 라우트 쿼리를 Record로 변환
   */
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

  /**
   * 두 쿼리가 동일한지 비교
   */
  function queriesEqual(a: Record<string, string>, b: Record<string, string>): boolean {
    const keys = new Set([...Object.keys(a), ...Object.keys(b)]);
    for (const key of keys) {
      if ((a[key] ?? '') !== (b[key] ?? '')) {
        return false;
      }
    }
    return true;
  }

  /**
   * unknown 값을 string[] 배열로 변환
   */
  function toArray(value: unknown): string[] {
    if (Array.isArray(value)) {
      return value.filter((item): item is string => typeof item === 'string');
    }
    if (typeof value === 'string') {
      return [value];
    }
    return [];
  }

  // 필터 변경 시 데이터 로딩 (디바운스)
  const triggerFilterFetch = useDebounceFn(() => {
    onLoadEmployees();
  }, 300);

  // 필터 변경 감지
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

  // 정렬 변경 감지
  watch(
    sorting,
    () => {
      if (isUpdatingRoute || isApplyingRoute) {
        return;
      }
      page.value = 1;
      updateRouteFromState();
      onLoadEmployees();
    },
    { deep: true },
  );

  // 페이지 변경 감지
  watch(page, () => {
    if (isUpdatingRoute || isApplyingRoute) {
      return;
    }
    updateRouteFromState();
    onLoadEmployees();
  });

  // 페이지 크기 변경 감지
  watch(pageSize, () => {
    if (isUpdatingRoute || isApplyingRoute) {
      return;
    }
    page.value = 1;
    updateRouteFromState();
    onLoadEmployees();
  });

  // 라우트 쿼리 변경 감지 (뒤로가기/앞으로가기)
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
