import { getEmployeeAvatarOption } from '@/features/employee/constants/avatars';

export interface DepartmentLeader {
  employeeId: number;
  employeeName: string;
  position: string;
  avatarCode: string;
  avatarLabel: string;
  avatarImageUrl: string;
}

export interface DepartmentEmployee {
  employeeId: number;
  employeeName: string;
  position: string;
}

export interface DepartmentChartNode {
  departmentId: number;
  departmentName: string;
  departmentCode: string;
  departmentType: string;
  departmentLeader: DepartmentLeader | null;
  employeeCount: number;
  children: DepartmentChartNode[];
}

function mapLeader(input: any): DepartmentLeader | null {
  if (!input) {
    return null;
  }

  const rawAvatarCode = typeof input?.avatar === 'string' ? input.avatar : null;
  const avatarOption = getEmployeeAvatarOption(rawAvatarCode);

  // position이 EnumResponse 객체면 description을, 문자열이면 그대로 사용
  const position = input.position?.description || input.position || '';

  return {
    employeeId: Number(input.employeeId),
    employeeName: input.employeeName,
    position,
    avatarCode: avatarOption.code,
    avatarLabel: avatarOption.label,
    avatarImageUrl: avatarOption.imageUrl,
  };
}

function mapEmployees(input: any[] | undefined): DepartmentEmployee[] {
  if (!Array.isArray(input)) {
    return [];
  }

  return input.map((employee) => ({
    employeeId: Number(employee.employeeId),
    employeeName: employee.employeeName,
    position: employee.position,
  }));
}

export function mapDepartmentChartNode(input: any): DepartmentChartNode {
  const employees = mapEmployees(input.employees);
  const computedEmployeeCount =
    typeof input?.employeeCount === 'number' ? Number(input.employeeCount) : employees.length;

  return {
    departmentId: Number(input.departmentId),
    departmentName: input.departmentName,
    departmentCode: input.departmentCode,
    departmentType: input.departmentType,
    departmentLeader: mapLeader(input.departmentLeader),
    employeeCount: Number.isFinite(computedEmployeeCount)
      ? computedEmployeeCount
      : employees.length,
    children: Array.isArray(input.children) ? input.children.map(mapDepartmentChartNode) : [],
  };
}

export function normalizeDepartmentChartResponse(response: unknown): DepartmentChartNode[] {
  if (!response) {
    return [];
  }

  if (Array.isArray(response)) {
    return response.map(mapDepartmentChartNode);
  }

  return [mapDepartmentChartNode(response)];
}

export interface DepartmentDetail {
  departmentId: number;
  departmentName: string;
  departmentCode: string;
  departmentType: string;
  departmentLeader: DepartmentLeader | null;
  employees: DepartmentEmployee[];
  employeeCount: number;
}

export interface DepartmentSummary {
  departmentId: number;
  departmentName: string;
  departmentCode: string;
  departmentType: string;
  departmentLeader: DepartmentLeader | null;
  employees: DepartmentEmployee[];
  employeeCount: number;
  childDepartmentCount: number;
}

export function mapDepartmentDetail(input: any): DepartmentDetail {
  const employees = mapEmployees(input?.employees);

  return {
    departmentId: Number(input?.departmentId ?? 0),
    departmentName: String(input?.departmentName ?? ''),
    departmentCode: String(input?.departmentCode ?? ''),
    departmentType: String(input?.departmentType ?? ''),
    departmentLeader: mapLeader(input?.departmentLeader),
    employees,
    employeeCount:
      typeof input?.employeeCount === 'number' ? Number(input.employeeCount) : employees.length,
  };
}
