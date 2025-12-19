/**
 * URL 쿼리 파라미터 변환 유틸리티
 *
 * 페이징, 정렬, 필터 등의 상태를 URL 쿼리와 상호 변환하는 공통 함수들을 제공합니다.
 * 모든 List View에서 재사용 가능합니다.
 */

import type { LocationQuery } from 'vue-router';
import type { SortingState } from '@tanstack/vue-table';

/**
 * 페이징 정보를 쿼리 파라미터로 직렬화
 *
 * @param page - 페이지 번호 (1-based)
 * @param size - 페이지 크기
 * @returns 쿼리 파라미터 객체
 *
 * @example
 * serializePagination(2, 20)
 * // { page: '2', size: '20' }
 */
export function serializePagination(page: number, size: number): Record<string, string> {
  return {
    page: page.toString(),
    size: size.toString(),
  };
}

/**
 * 쿼리 파라미터에서 페이징 정보 추출
 *
 * @param query - Vue Router의 LocationQuery
 * @returns 페이징 정보 { page, size }
 *
 * @example
 * deserializePagination({ page: '2', size: '20' })
 * // { page: 2, size: 20 }
 */
export function deserializePagination(query: LocationQuery): { page: number; size: number } {
  const page = Number(query.page) || 1;
  const size = Number(query.size) || 10;
  return { page, size };
}

/**
 * TanStack Table의 정렬 상태를 쿼리 파라미터로 직렬화
 *
 * @param sorting - TanStack Table SortingState
 * @returns 쿼리 파라미터 객체 (정렬이 없으면 빈 객체)
 *
 * @example
 * serializeSorting([{ id: 'name', desc: false }])
 * // { sort: 'name,asc' }
 *
 * serializeSorting([{ id: 'createdAt', desc: true }])
 * // { sort: 'createdAt,desc' }
 */
export function serializeSorting(sorting: SortingState): Record<string, string> {
  if (sorting.length === 0) return {};

  const [firstSort] = sorting || [];
  return {
    sort: `${firstSort?.id},${firstSort?.desc ? 'desc' : 'asc'}`,
  };
}

/**
 * 쿼리 파라미터에서 정렬 상태 추출
 *
 * @param query - Vue Router의 LocationQuery
 * @returns TanStack Table SortingState
 *
 * @example
 * deserializeSorting({ sort: 'name,asc' })
 * // [{ id: 'name', desc: false }]
 */
export function deserializeSorting(query: LocationQuery): SortingState {
  if (!query.sort || typeof query.sort !== 'string') return [];

  const [id, order] = query.sort.split(',');
  if (!id) return [];

  return [{ id, desc: order === 'desc' }];
}

/**
 * 배열 값을 쉼표로 구분된 문자열로 직렬화
 *
 * @param values - 문자열 배열
 * @returns 쉼표로 구분된 문자열
 *
 * @example
 * serializeArrayFilter(['ACTIVE', 'ON_LEAVE'])
 * // 'ACTIVE,ON_LEAVE'
 */
export function serializeArrayFilter(values: string[]): string {
  return values.join(',');
}

/**
 * 쉼표로 구분된 문자열을 배열로 역직렬화
 *
 * @param value - 쉼표로 구분된 문자열 또는 null
 * @returns 문자열 배열
 *
 * @example
 * deserializeArrayFilter('ACTIVE,ON_LEAVE')
 * // ['ACTIVE', 'ON_LEAVE']
 *
 * deserializeArrayFilter(null)
 * // []
 */
export function deserializeArrayFilter(value: string | string[] | null | undefined): string[] {
  if (!value) return [];
  if (Array.isArray(value)) return value;
  return value.split(',').filter((v) => v.length > 0);
}

/**
 * 단일 값 필터 직렬화
 *
 * @param value - 필터 값
 * @returns 쿼리 파라미터 객체
 */
export function serializeSingleFilter(
  key: string,
  value: string | null | undefined,
): Record<string, string> {
  if (!value) return {};
  return { [key]: value };
}

/**
 * 단일 값 필터 역직렬화
 *
 * @param query - Vue Router의 LocationQuery
 * @param key - 쿼리 파라미터 키
 * @returns 필터 값 또는 null
 */
export function deserializeSingleFilter(query: LocationQuery, key: string): string | null {
  const value = query[key];
  if (!value) return null;
  if (Array.isArray(value)) return value[0] ?? null;
  return value;
}
