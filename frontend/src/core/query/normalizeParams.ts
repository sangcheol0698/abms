function toPositiveInteger(value: unknown, fallback: number): number {
  const parsed = typeof value === 'number' ? value : Number(value);
  if (!Number.isFinite(parsed)) {
    return fallback;
  }
  const normalized = Math.trunc(parsed);
  return normalized > 0 ? normalized : fallback;
}

function toNullableString(value: unknown): string | null {
  if (typeof value !== 'string') {
    return null;
  }
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
}

function toIsoDateString(value: unknown): string | null {
  if (!value) {
    return null;
  }

  if (typeof value === 'string') {
    const trimmed = value.trim();
    if (!trimmed) {
      return null;
    }
    return trimmed.length >= 10 ? trimmed.slice(0, 10) : trimmed;
  }

  if (value instanceof Date && !Number.isNaN(value.getTime())) {
    return value.toISOString().slice(0, 10);
  }

  if (typeof value === 'object') {
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

  return null;
}

function toSortedUniqueStrings(values: unknown): string[] {
  if (!Array.isArray(values)) {
    return [];
  }

  return Array.from(
    new Set(
      values
        .map((value) => (typeof value === 'string' ? value.trim() : ''))
        .filter((value) => value.length > 0),
    ),
  ).sort((a, b) => a.localeCompare(b));
}

function toSortedUniqueNumbers(values: unknown): number[] {
  if (!Array.isArray(values)) {
    return [];
  }

  return Array.from(
    new Set(
      values
        .map((value) => (typeof value === 'number' ? value : Number(value)))
        .filter((value) => Number.isFinite(value))
        .map((value) => Math.trunc(value))
        .filter((value) => value > 0),
    ),
  ).sort((a, b) => a - b);
}

export interface NormalizedEmployeeSearchParams {
  page: number;
  size: number;
  name: string | null;
  statuses: string[];
  types: string[];
  grades: string[];
  positions: string[];
  departmentIds: number[];
  sort: string | null;
}

export function normalizeEmployeeSearchParams(params: Record<string, unknown> = {}): NormalizedEmployeeSearchParams {
  return {
    page: toPositiveInteger(params.page, 1),
    size: toPositiveInteger(params.size, 10),
    name: toNullableString(params.name),
    statuses: toSortedUniqueStrings(params.statuses),
    types: toSortedUniqueStrings(params.types),
    grades: toSortedUniqueStrings(params.grades),
    positions: toSortedUniqueStrings(params.positions),
    departmentIds: toSortedUniqueNumbers(params.departmentIds),
    sort: toNullableString(params.sort),
  };
}

export interface NormalizedEmployeeProjectsParams {
  page: number;
  size: number;
  name: string | null;
  assignmentStatuses: string[];
  projectStatuses: string[];
}

export function normalizeEmployeeProjectsParams(
  params: Record<string, unknown> = {},
): NormalizedEmployeeProjectsParams {
  return {
    page: toPositiveInteger(params.page, 1),
    size: toPositiveInteger(params.size, 10),
    name: toNullableString(params.name),
    assignmentStatuses: toSortedUniqueStrings(params.assignmentStatuses),
    projectStatuses: toSortedUniqueStrings(params.projectStatuses),
  };
}

export interface NormalizedDepartmentEmployeesParams {
  page: number;
  size: number;
  name: string | null;
  sort: string | null;
  statuses: string[];
  types: string[];
  grades: string[];
  positions: string[];
}

export function normalizeDepartmentEmployeesParams(
  params: Record<string, unknown> = {},
): NormalizedDepartmentEmployeesParams {
  return {
    page: toPositiveInteger(params.page, 1),
    size: toPositiveInteger(params.size, 10),
    name: toNullableString(params.name),
    sort: toNullableString(params.sort),
    statuses: toSortedUniqueStrings(params.statuses),
    types: toSortedUniqueStrings(params.types),
    grades: toSortedUniqueStrings(params.grades),
    positions: toSortedUniqueStrings(params.positions),
  };
}

export interface NormalizedProjectSearchParams {
  page: number;
  size: number;
  name: string | null;
  statuses: string[];
  partyIds: number[];
  periodStart: string | null;
  periodEnd: string | null;
  sort: string | null;
}

export function normalizeProjectSearchParams(params: Record<string, unknown> = {}): NormalizedProjectSearchParams {
  return {
    page: toPositiveInteger(params.page, 1),
    size: toPositiveInteger(params.size, 10),
    name: toNullableString(params.name),
    statuses: toSortedUniqueStrings(params.statuses),
    partyIds: toSortedUniqueNumbers(params.partyIds),
    periodStart: toIsoDateString(params.periodStart),
    periodEnd: toIsoDateString(params.periodEnd),
    sort: toNullableString(params.sort),
  };
}

export interface NormalizedPartySearchParams {
  page: number;
  size: number;
  name: string | null;
  sort: string | null;
}

export function normalizePartySearchParams(params: Record<string, unknown> = {}): NormalizedPartySearchParams {
  return {
    page: toPositiveInteger(params.page, 1),
    size: toPositiveInteger(params.size, 10),
    name: toNullableString(params.name),
    sort: toNullableString(params.sort),
  };
}

export function normalizeLimit(limit: unknown, fallback = 20): number {
  return toPositiveInteger(limit, fallback);
}
