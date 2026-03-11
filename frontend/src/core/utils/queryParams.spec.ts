import { describe, expect, it } from 'vitest';
import {
  deserializeArrayFilter,
  deserializePagination,
  deserializeSingleFilter,
  deserializeSorting,
  serializeArrayFilter,
  serializePagination,
  serializeSingleFilter,
  serializeSorting,
} from '@/core/utils/queryParams';

describe('queryParams utils', () => {
  it('pagination을 직렬화/역직렬화한다', () => {
    expect(serializePagination(2, 20)).toEqual({ page: '2', size: '20' });
    expect(deserializePagination({ page: '0', size: '0' })).toEqual({ page: 1, size: 10 });
    expect(deserializePagination({ page: '3', size: '15' })).toEqual({ page: 3, size: 15 });
  });

  it('sorting을 직렬화/역직렬화한다', () => {
    expect(serializeSorting([])).toEqual({});
    expect(serializeSorting([{ id: 'name', desc: true }])).toEqual({ sort: 'name,desc' });
    expect(deserializeSorting({ sort: 'name,asc' })).toEqual([{ id: 'name', desc: false }]);
    expect(deserializeSorting({ sort: '' })).toEqual([]);
    expect(deserializeSorting({ sort: 'desc' })).toEqual([{ id: 'desc', desc: false }]);
  });

  it('배열/단일 필터를 직렬화/역직렬화한다', () => {
    expect(serializeArrayFilter(['A', 'B'])).toBe('A,B');
    expect(deserializeArrayFilter('A,B,,C')).toEqual(['A', 'B', 'C']);
    expect(deserializeArrayFilter(['A', 'B'])).toEqual(['A', 'B']);
    expect(deserializeArrayFilter(undefined)).toEqual([]);

    expect(serializeSingleFilter('name', 'ABMS')).toEqual({ name: 'ABMS' });
    expect(serializeSingleFilter('name', '')).toEqual({});
    expect(deserializeSingleFilter({ name: ['ABMS', 'CDP'] }, 'name')).toBe('ABMS');
    expect(deserializeSingleFilter({ name: 'ABMS' }, 'name')).toBe('ABMS');
    expect(deserializeSingleFilter({}, 'name')).toBeNull();
  });
});
