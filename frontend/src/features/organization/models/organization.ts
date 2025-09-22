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
  children: OrganizationChartNode[];
}

export interface OrganizationChartWithEmployeesNode extends OrganizationChartNode {
  employees: OrganizationEmployee[];
  children: OrganizationChartWithEmployeesNode[];
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

function hasEmployees(node: any): node is OrganizationChartWithEmployeesNode {
  return Array.isArray(node?.employees);
}

export function mapOrganizationChartNode(input: any): OrganizationChartNode {
  const base: OrganizationChartNode = {
    departmentId: input.departmentId,
    departmentName: input.departmentName,
    departmentCode: input.departmentCode,
    departmentType: input.departmentType,
    departmentLeader: mapLeader(input.departmentLeader),
    children: Array.isArray(input.children) ? input.children.map(mapOrganizationChartNode) : [],
  };

  if (Array.isArray(input.employees)) {
    (base as OrganizationChartWithEmployeesNode).employees = mapEmployees(input.employees);
  }

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

export function castToOrganizationWithEmployees(
  nodes: OrganizationChartNode[],
): OrganizationChartWithEmployeesNode[] {
  return nodes.map((node) => ({
    ...node,
    employees: hasEmployees(node)
      ? ((node as OrganizationChartWithEmployeesNode).employees ?? [])
      : [],
    children: castToOrganizationWithEmployees(node.children ?? []),
  }));
}
