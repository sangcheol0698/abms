export interface EmployeeSummary {
  departmentId: string;
  departmentName: string;
  employeeId: string;
  name: string;
  email: string;
  position: string;
  status: string;
  grade: string;
  type: string;
  memo: string;
}

export function mapEmployeeSummary(input: any): EmployeeSummary {
  return {
    departmentId: String(input?.departmentId ?? ''),
    departmentName: String(input?.departmentName ?? ''),
    employeeId: String(input?.employeeId ?? ''),
    name: String(input?.name ?? ''),
    email: String(input?.email ?? ''),
    position: String(input?.position ?? ''),
    status: String(input?.status ?? ''),
    grade: String(input?.grade ?? ''),
    type: String(input?.type ?? ''),
    memo: String(input?.memo ?? ''),
  };
}

export interface EmployeeCreatePayload {
  departmentId: string;
  name: string;
  email: string;
  position: string;
  grade: string;
  type: string;
  memo?: string;
}
