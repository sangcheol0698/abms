import { toGradeCode, toPositionCode, toStatusCode, toTypeCode } from '@/features/employee/models/employeeFilters';

export interface EmployeeSummary {
  departmentId: string;
  departmentName: string;
  employeeId: string;
  name: string;
  email: string;
  position: string;
  positionCode: string;
  status: string;
  statusCode: string;
  grade: string;
  gradeCode: string;
  type: string;
  typeCode: string;
  memo: string;
  joinDate?: string;
  birthDate?: string;
}

export function mapEmployeeSummary(input: any): EmployeeSummary {
  const positionLabel = String(input?.position ?? '');
  const statusLabel = String(input?.status ?? '');
  const gradeLabel = String(input?.grade ?? '');
  const typeLabel = String(input?.type ?? '');

  return {
    departmentId: String(input?.departmentId ?? ''),
    departmentName: String(input?.departmentName ?? ''),
    employeeId: String(input?.employeeId ?? ''),
    name: String(input?.name ?? ''),
    email: String(input?.email ?? ''),
    position: positionLabel,
    positionCode: toPositionCode(positionLabel),
    status: statusLabel,
    statusCode: toStatusCode(statusLabel),
    grade: gradeLabel,
    gradeCode: toGradeCode(gradeLabel),
    type: typeLabel,
    typeCode: toTypeCode(typeLabel),
    memo: String(input?.memo ?? ''),
    joinDate: input?.joinDate ? String(input.joinDate) : undefined,
    birthDate: input?.birthDate ? String(input.birthDate) : undefined,
  };
}

export interface EmployeeCreatePayload {
  departmentId: string;
  name: string;
  email: string;
  joinDate: string;
  birthDate: string;
  position: string;
  grade: string;
  type: string;
  memo?: string;
}
