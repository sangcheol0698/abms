export interface OrganizationLeader {
  employeeId: string;
  employeeName: string;
  position: string;
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

  return {
    employeeId: input.employeeId,
    employeeName: input.employeeName,
    position: input.position,
  };
}

function mapEmployees(input: any[] | undefined): OrganizationEmployee[] {
  if (!Array.isArray(input)) {
    return [];
  }

  return input.map((employee) => ({
    employeeId: employee.employeeId,
    employeeName: employee.employeeName,
    position: employee.position,
  }));
}

export function mapOrganizationChartNode(input: any): OrganizationChartNode {
  const employees = mapEmployees(input.employees);
  const computedEmployeeCount =
    typeof input?.employeeCount === 'number' ? Number(input.employeeCount) : employees.length;

  const base: OrganizationChartNode = {
    departmentId: input.departmentId,
    departmentName: input.departmentName,
    departmentCode: input.departmentCode,
    departmentType: input.departmentType,
    departmentLeader: mapLeader(input.departmentLeader),
    employeeCount: Number.isFinite(computedEmployeeCount)
      ? computedEmployeeCount
      : employees.length,
    children: Array.isArray(input.children) ? input.children.map(mapOrganizationChartNode) : [],
  };

  return base;
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
