export type ProjectAssignmentStatus = 'CURRENT' | 'SCHEDULED' | 'ENDED';
export type ProjectAssignmentRole = 'DEV' | 'PM' | 'PL' | 'ETC';

export interface ProjectAssignmentItem {
  id: number;
  projectId: number;
  employeeId: number;
  employeeName: string;
  departmentId: number | null;
  departmentName: string | null;
  role: ProjectAssignmentRole | null;
  roleLabel: string;
  startDate: string;
  endDate: string | null;
  assignmentStatus: ProjectAssignmentStatus;
  assignmentStatusLabel: string;
}

export interface ProjectAssignmentCreatePayload {
  projectId: number;
  employeeId: number;
  role: ProjectAssignmentRole;
  startDate: string;
  endDate: string | null;
}

export interface ProjectAssignmentUpdatePayload {
  employeeId: number;
  role: ProjectAssignmentRole;
  startDate: string;
  endDate: string | null;
}

export interface ProjectAssignmentEndPayload {
  endDate: string;
}

export interface ProjectAssignmentSearchParams {
  page: number;
  size: number;
  name?: string;
  assignmentStatuses?: string[];
  roles?: string[];
}

export interface AssignmentEmployeeOption {
  employeeId: number;
  employeeName: string;
  departmentId: number | null;
  departmentName: string | null;
  positionLabel: string;
}

export const projectAssignmentRoleOptions: { value: ProjectAssignmentRole; label: string }[] = [
  { value: 'DEV', label: '개발자' },
  { value: 'PM', label: 'PM' },
  { value: 'PL', label: 'PL' },
  { value: 'ETC', label: '기타' },
];

export function getProjectAssignmentRoleLabel(role: string | null | undefined): string {
  const option = projectAssignmentRoleOptions.find((candidate) => candidate.value === role);
  return option?.label ?? '-';
}

export function getProjectAssignmentStatusLabel(status: string | null | undefined): string {
  if (status === 'CURRENT') {
    return '현재 투입';
  }
  if (status === 'SCHEDULED') {
    return '예정';
  }
  return '종료';
}

export function mapProjectAssignmentItem(input: any): ProjectAssignmentItem {
  const rawStatus = String(input?.assignmentStatus ?? 'ENDED').toUpperCase();
  const assignmentStatus: ProjectAssignmentStatus =
    rawStatus === 'CURRENT' ? 'CURRENT' : rawStatus === 'SCHEDULED' ? 'SCHEDULED' : 'ENDED';

  const rawRole = input?.role == null ? null : String(input.role).toUpperCase();
  const role = rawRole === 'DEV' || rawRole === 'PM' || rawRole === 'PL' || rawRole === 'ETC'
    ? rawRole
    : null;

  return {
    id: Number(input?.id ?? 0),
    projectId: Number(input?.projectId ?? 0),
    employeeId: Number(input?.employeeId ?? 0),
    employeeName: String(input?.employeeName ?? ''),
    departmentId: input?.departmentId == null ? null : Number(input.departmentId),
    departmentName: input?.departmentName == null ? null : String(input.departmentName),
    role,
    roleLabel: getProjectAssignmentRoleLabel(role),
    startDate: String(input?.startDate ?? ''),
    endDate: input?.endDate == null ? null : String(input.endDate),
    assignmentStatus,
    assignmentStatusLabel: getProjectAssignmentStatusLabel(assignmentStatus),
  };
}
