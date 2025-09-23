import {
  toGradeCode,
  toPositionCode,
  toStatusCode,
  toTypeCode,
} from '@/features/employee/models/employeeFilters';

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
}

export function mapEmployeeListItem(input: any): EmployeeListItem {
  const statusLabel = String(input?.status ?? '');
  const gradeLabel = String(input?.grade ?? '');
  const typeLabel = String(input?.type ?? '');
  const positionLabel = String(input?.position ?? '');

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
