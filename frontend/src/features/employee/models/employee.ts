import { getEmployeeAvatarOption } from '@/features/employee/constants/avatars';
import {
  toGradeCode,
  toPositionCode,
  toStatusCode,
  toTypeCode,
} from '@/features/employee/models/employeeFilters';

function toIsoDateString(value: unknown): string {
  if (!value) {
    return '';
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
      return `${maybeDate.year}-${String(maybeDate.month).padStart(2, '0')}-${String(maybeDate.day).padStart(2, '0')}`;
    }
  }

  return '';
}

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
  avatarCode: string;
  avatarLabel: string;
  avatarImageUrl: string;
  memo: string;
  joinDate: string;
  birthDate: string;
}

export function mapEmployeeSummary(input: any): EmployeeSummary {
  const positionLabel = String(input?.position ?? '');
  const statusLabel = String(input?.status ?? '');
  const gradeLabel = String(input?.grade ?? '');
  const typeLabel = String(input?.type ?? '');
  const joinDate = toIsoDateString(input?.joinDate);
  const birthDate = toIsoDateString(input?.birthDate);
  const rawAvatarCode = typeof input?.avatarCode === 'string' ? input.avatarCode : null;
  const avatarLabelFromApi = typeof input?.avatarLabel === 'string' ? input.avatarLabel : '';
  const avatarOption = getEmployeeAvatarOption(rawAvatarCode);
  const avatarLabel = avatarLabelFromApi.length > 0 ? avatarLabelFromApi : avatarOption.label;

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
    avatarCode: avatarOption.code,
    avatarLabel,
    avatarImageUrl: avatarOption.imageUrl,
    memo: String(input?.memo ?? ''),
    joinDate: joinDate,
    birthDate: birthDate,
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
  avatar: string;
  memo?: string;
}
