import { describe, expect, it } from 'vitest';
import {
  normalizeDepartmentEmployeesParams,
  normalizeEmployeeSearchParams,
  normalizePartySearchParams,
  normalizeProjectSearchParams,
} from '@/core/query/normalizeParams';

describe('normalizeParams', () => {
  it('normalizes employee params to a stable shape', () => {
    const normalized = normalizeEmployeeSearchParams({
      page: 0,
      size: '20',
      name: '  kim  ',
      statuses: ['leave', 'working', 'working'],
      types: ['CONTRACT', 'FULL_TIME'],
      grades: ['B', 'A', 'A'],
      positions: ['P2', 'P1'],
      departmentIds: ['3', 1, 3, 2],
      sort: 'name,asc',
    });

    expect(normalized).toEqual({
      page: 1,
      size: 20,
      name: 'kim',
      statuses: ['leave', 'working'],
      types: ['CONTRACT', 'FULL_TIME'],
      grades: ['A', 'B'],
      positions: ['P1', 'P2'],
      departmentIds: [1, 2, 3],
      sort: 'name,asc',
    });
  });

  it('normalizes project params including date range', () => {
    const normalized = normalizeProjectSearchParams({
      page: '2',
      size: 30,
      name: '',
      statuses: ['IN_PROGRESS', 'SCHEDULED', 'IN_PROGRESS'],
      partyIds: [10, '2', 2],
      periodStart: new Date('2025-01-01T12:00:00.000Z'),
      periodEnd: { year: 2025, month: 12, day: 31 },
      sort: 'periodStart,desc',
    });

    expect(normalized).toEqual({
      page: 2,
      size: 30,
      name: null,
      statuses: ['IN_PROGRESS', 'SCHEDULED'],
      partyIds: [2, 10],
      periodStart: '2025-01-01',
      periodEnd: '2025-12-31',
      sort: 'periodStart,desc',
    });
  });

  it('normalizes department employee params', () => {
    const normalized = normalizeDepartmentEmployeesParams({
      page: -1,
      size: 0,
      name: '  ',
      sort: undefined,
      statuses: ['WORKING', 'WORKING', 'LEAVE'],
    });

    expect(normalized).toEqual({
      page: 1,
      size: 10,
      name: null,
      sort: null,
      statuses: ['LEAVE', 'WORKING'],
      types: [],
      grades: [],
      positions: [],
    });
  });

  it('normalizes party params', () => {
    const normalized = normalizePartySearchParams({
      page: undefined,
      size: null,
      name: '  ABMS  ',
      sort: 'name,asc',
    });

    expect(normalized).toEqual({
      page: 1,
      size: 10,
      name: 'ABMS',
      sort: 'name,asc',
    });
  });
});
