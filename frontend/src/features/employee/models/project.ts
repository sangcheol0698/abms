export type EmployeeProjectAssignmentStatus = 'CURRENT' | 'SCHEDULED' | 'ENDED';

export interface EmployeeProjectItem {
  projectId: number;
  projectCode: string;
  projectName: string;
  partyId: number;
  role: string | null;
  assignmentStartDate: string;
  assignmentEndDate: string | null;
  assignmentStatus: EmployeeProjectAssignmentStatus;
  projectStatus: string;
  projectStatusLabel: string;
  leadDepartmentId: number | null;
  leadDepartmentName: string | null;
  partyName: string;
}

export interface EmployeeProjectsSearchParams {
  page: number;
  size: number;
  name?: string;
  assignmentStatuses?: string[];
  projectStatuses?: string[];
}

export function mapEmployeeProjectItem(input: any): EmployeeProjectItem {
  const rawAssignmentStatus = String(input?.assignmentStatus ?? 'ENDED').toUpperCase();
  const assignmentStatus: EmployeeProjectAssignmentStatus =
    rawAssignmentStatus === 'CURRENT'
      ? 'CURRENT'
      : rawAssignmentStatus === 'SCHEDULED'
        ? 'SCHEDULED'
        : 'ENDED';

  return {
    projectId: Number(input?.projectId ?? 0),
    projectCode: String(input?.projectCode ?? ''),
    projectName: String(input?.projectName ?? ''),
    partyId: Number(input?.partyId ?? 0),
    role: input?.role == null ? null : String(input.role),
    assignmentStartDate: String(input?.assignmentStartDate ?? ''),
    assignmentEndDate: input?.assignmentEndDate ? String(input.assignmentEndDate) : null,
    assignmentStatus,
    projectStatus: String(input?.projectStatus ?? ''),
    projectStatusLabel: String(input?.projectStatusDescription ?? ''),
    leadDepartmentId: input?.leadDepartmentId ? Number(input.leadDepartmentId) : null,
    leadDepartmentName: input?.leadDepartmentName ? String(input.leadDepartmentName) : null,
    partyName: String(input?.partyName ?? ''),
  };
}
