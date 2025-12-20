import { getEmployeeAvatarOption } from '@/features/employee/constants/avatars';

export interface OrganizationLeader {
  employeeId: string;
  employeeName: string;
  position: string;
  avatarCode: string;
  avatarLabel: string;
  avatarImageUrl: string;
}

export interface OrganizationEmployee {
  employeeId: string;
  employeeName: string;
  position: string;
}

export interface OrganizationChartNode {
  departmentId: string;
  departmentName: string;
  departmentCode: string;
  departmentType: string;
  departmentLeader: OrganizationLeader | null;
  employeeCount: number;
  children: OrganizationChartNode[];
}

function mapLeader(input: any): OrganizationLeader | null {
  if (!input) {
    return null;
  }

  const rawAvatarCode = typeof input?.avatar === 'string' ? input.avatar : null;
  const avatarOption = getEmployeeAvatarOption(rawAvatarCode);

  // position이 EnumResponse 객체면 description을, 문자열이면 그대로 사용
  const position = input.position?.description || input.position || '';

  return {
    employeeId: String(input.employeeId),
    employeeName: input.employeeName,
    position,
    avatarCode: avatarOption.code,
    avatarLabel: avatarOption.label,
    avatarImageUrl: avatarOption.imageUrl,
  };
}

function mapEmployees(input: any[] | undefined): OrganizationEmployee[] {
  if (!Array.isArray(input)) {
    return [];
  }

  return input.map((employee) => ({
    employeeId: String(employee.employeeId),
    employeeName: employee.employeeName,
    position: employee.position,
  }));
}

export function mapOrganizationChartNode(input: any): OrganizationChartNode {
  const employees = mapEmployees(input.employees);
  const computedEmployeeCount =
    typeof input?.employeeCount === 'number' ? Number(input.employeeCount) : employees.length;

  return {
    departmentId: String(input.departmentId),
    departmentName: input.departmentName,
    departmentCode: input.departmentCode,
    departmentType: input.departmentType,
    departmentLeader: mapLeader(input.departmentLeader),
    employeeCount: Number.isFinite(computedEmployeeCount)
      ? computedEmployeeCount
      : employees.length,
    children: Array.isArray(input.children) ? input.children.map(mapOrganizationChartNode) : [],
  };
}

export function normalizeOrganizationChartResponse(response: unknown): OrganizationChartNode[] {
  if (!response) {
    return [];
  }

  if (Array.isArray(response)) {
    return response.map(mapOrganizationChartNode);
  }

  return [mapOrganizationChartNode(response)];
}

export interface OrganizationDepartmentDetail {
  departmentId: string;
  departmentName: string;
  departmentCode: string;
  departmentType: string;
  departmentLeader: OrganizationLeader | null;
  employees: OrganizationEmployee[];
  employeeCount: number;
}

export interface OrganizationDepartmentSummary {
  departmentId: string;
  departmentName: string;
  departmentCode: string;
  departmentType: string;
  departmentLeader: OrganizationLeader | null;
  employees: OrganizationEmployee[];
  employeeCount: number;
  childDepartmentCount: number;
}

export function mapOrganizationDepartmentDetail(input: any): OrganizationDepartmentDetail {
  const employees = mapEmployees(input?.employees);

  return {
    departmentId: String(input?.departmentId ?? ''),
    departmentName: String(input?.departmentName ?? ''),
    departmentCode: String(input?.departmentCode ?? ''),
    departmentType: String(input?.departmentType ?? ''),
    departmentLeader: mapLeader(input?.departmentLeader),
    employees,
    employeeCount:
      typeof input?.employeeCount === 'number' ? Number(input.employeeCount) : employees.length,
  };
}
