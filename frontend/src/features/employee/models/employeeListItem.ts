import {
  toGradeCode,
  toPositionCode,
  toStatusCode,
  toTypeCode,
} from '@/features/employee/models/employeeFilters';

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

  if (
    Array.isArray(value) &&
    value.length >= 3 &&
    typeof value[0] === 'number' &&
    typeof value[1] === 'number' &&
    typeof value[2] === 'number'
  ) {
    const [year, month, day] = value as [number, number, number];
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
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

export interface EmployeeListItem {
  employeeId: string;
  departmentId: string;
  departmentName: string;
  name: string;
  email: string;
  positionCode: string;
  positionLabel: string;
  statusCode: string;
  statusLabel: string;
  gradeCode: string;
  gradeLabel: string;
  typeCode: string;
  typeLabel: string;
  memo: string;
  joinDate?: string;
  birthDate?: string;
}

export function mapEmployeeListItem(input: any): EmployeeListItem {
  const statusLabel = String(input?.status ?? '');
  const gradeLabel = String(input?.grade ?? '');
  const typeLabel = String(input?.type ?? '');
  const positionLabel = String(input?.position ?? '');
  const joinDate = toIsoDateString(input?.joinDate);
  const birthDate = toIsoDateString(input?.birthDate);

  return {
    employeeId: String(input?.employeeId ?? ''),
    departmentId: String(input?.departmentId ?? ''),
    departmentName: String(input?.departmentName ?? ''),
    name: String(input?.name ?? ''),
    email: String(input?.email ?? ''),
    positionCode: toPositionCode(positionLabel),
    positionLabel,
    statusCode: toStatusCode(statusLabel),
    statusLabel,
    gradeCode: toGradeCode(gradeLabel),
    gradeLabel,
    typeCode: toTypeCode(typeLabel),
    typeLabel,
    memo: String(input?.memo ?? ''),
    joinDate,
    birthDate,
  };
}

export interface EmployeeSearchParams {
  page: number;
  size: number;
  name?: string;
  statuses?: string[];
  types?: string[];
  grades?: string[];
  positions?: string[];
  departmentIds?: string[];
  sort?: string;
}
