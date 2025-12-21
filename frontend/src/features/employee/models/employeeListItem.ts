import { getEmployeeAvatarOption } from '@/features/employee/constants/avatars';
import {
  toGradeLabel,
  toPositionLabel,
  toStatusLabel,
  toTypeLabel,
} from '@/features/employee/models/employeeFilters';
import { extractEnumCode } from '@/core/api/enum';

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
  employeeId: number;
  departmentId: number;
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
  avatarCode: string;
  avatarLabel: string;
  avatarImageUrl: string;
  memo: string;
  joinDate?: string;
  birthDate?: string;
}

export function mapEmployeeListItem(input: any): EmployeeListItem {
  // 백엔드 EnumResponse에서 code 추출
  const statusCode = extractEnumCode(input?.status);
  const gradeCode = extractEnumCode(input?.grade);
  const typeCode = extractEnumCode(input?.type);
  const positionCode = extractEnumCode(input?.position);
  const joinDate = toIsoDateString(input?.joinDate);
  const birthDate = toIsoDateString(input?.birthDate);

  // Email handling: can be string or object { address: string }
  let email = '';
  if (typeof input?.email === 'string') {
    email = input.email;
  } else if (input?.email?.address) {
    email = input.email.address;
  }

  // Avatar handling: input.avatar는 EnumResponse 객체 또는 문자열
  const avatarCode = extractEnumCode(input?.avatar);
  const avatarOption = getEmployeeAvatarOption(avatarCode || null);
  const avatarLabel = avatarOption.label;

  return {
    employeeId: Number(input?.employeeId ?? 0),
    departmentId: Number(input?.departmentId ?? 0),
    departmentName: String(input?.departmentName ?? ''),
    name: String(input?.name ?? ''),
    email,
    positionCode,
    positionLabel: toPositionLabel(positionCode),
    statusCode,
    statusLabel: toStatusLabel(statusCode),
    gradeCode,
    gradeLabel: toGradeLabel(gradeCode),
    typeCode,
    typeLabel: toTypeLabel(typeCode),
    avatarCode: avatarOption.code,
    avatarLabel,
    avatarImageUrl: avatarOption.imageUrl,
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
  departmentIds?: number[];
  sort?: string;
}
