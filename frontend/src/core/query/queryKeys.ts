import {
  normalizeDepartmentEmployeesParams,
  normalizeEmployeeSearchParams,
  normalizeEmployeeProjectsParams,
  normalizeLimit,
  normalizePartyProjectsParams,
  normalizePartySearchParams,
  normalizeProjectAssignmentParams,
  normalizeProjectSearchParams,
} from '@/core/query/normalizeParams';

type QueryKeyParams = object | null | undefined;

function toQueryRecord(params: QueryKeyParams): Record<string, unknown> {
  return (params ?? {}) as Record<string, unknown>;
}

const AUTH_KEY = ['auth'] as const;
const EMPLOYEE_KEY = ['employee'] as const;
const DEPARTMENT_KEY = ['department'] as const;
const PARTY_KEY = ['party'] as const;
const PROJECT_KEY = ['project'] as const;
const DASHBOARD_KEY = ['dashboard'] as const;
const NOTIFICATION_KEY = ['notification'] as const;
const CHAT_KEY = ['chat'] as const;
const ADMIN_KEY = ['admin'] as const;

export const authKeys = {
  all: AUTH_KEY,
  me: () => [...AUTH_KEY, 'me'] as const,
};

export const employeeKeys = {
  all: EMPLOYEE_KEY,
  listRoot: () => [...EMPLOYEE_KEY, 'list'] as const,
  list: (params: QueryKeyParams = {}) =>
    [...EMPLOYEE_KEY, 'list', normalizeEmployeeSearchParams(toQueryRecord(params))] as const,
  summary: (params: QueryKeyParams = {}) =>
    [...EMPLOYEE_KEY, 'summary', normalizeEmployeeSearchParams(toQueryRecord(params))] as const,
  detail: (employeeId: number | null | undefined) =>
    [...EMPLOYEE_KEY, 'detail', employeeId ?? 0] as const,
  currentProfile: (email: string | null | undefined = '') =>
    [...EMPLOYEE_KEY, 'current-profile', email ?? ''] as const,
  statuses: () => [...EMPLOYEE_KEY, 'statuses'] as const,
  types: () => [...EMPLOYEE_KEY, 'types'] as const,
  grades: () => [...EMPLOYEE_KEY, 'grades'] as const,
  positions: () => [...EMPLOYEE_KEY, 'positions'] as const,
  avatars: () => [...EMPLOYEE_KEY, 'avatars'] as const,
  positionHistory: (employeeId: number | null | undefined) =>
    [...EMPLOYEE_KEY, 'position-history', employeeId ?? 0] as const,
  payrollCurrent: (employeeId: number | null | undefined) =>
    [...EMPLOYEE_KEY, 'payroll', 'current', employeeId ?? 0] as const,
  payrollHistory: (employeeId: number | null | undefined) =>
    [...EMPLOYEE_KEY, 'payroll', 'history', employeeId ?? 0] as const,
  projects: (employeeId: number | null | undefined, params: QueryKeyParams = {}) =>
    [...EMPLOYEE_KEY, 'projects', employeeId ?? 0, normalizeEmployeeProjectsParams(toQueryRecord(params))] as const,
};

export const departmentKeys = {
  all: DEPARTMENT_KEY,
  organizationChart: () => [...DEPARTMENT_KEY, 'organization-chart'] as const,
  detail: (departmentId: number | null | undefined) =>
    [...DEPARTMENT_KEY, 'detail', departmentId ?? 0] as const,
  employeesRoot: (departmentId: number | null | undefined) =>
    [...DEPARTMENT_KEY, 'employees', departmentId ?? 0] as const,
  employees: (departmentId: number | null | undefined, params: QueryKeyParams = {}) =>
    [...DEPARTMENT_KEY, 'employees', departmentId ?? 0, normalizeDepartmentEmployeesParams(toQueryRecord(params))] as const,
};

export const partyKeys = {
  all: PARTY_KEY,
  listRoot: () => [...PARTY_KEY, 'list'] as const,
  list: (params: QueryKeyParams = {}) =>
    [...PARTY_KEY, 'list', normalizePartySearchParams(toQueryRecord(params))] as const,
  summary: (params: QueryKeyParams = {}) =>
    [...PARTY_KEY, 'summary', normalizePartySearchParams(toQueryRecord(params))] as const,
  detail: (partyId: number | null | undefined) => [...PARTY_KEY, 'detail', partyId ?? 0] as const,
  projectsRoot: (partyId: number | null | undefined) => [...PARTY_KEY, 'projects', partyId ?? 0] as const,
  projects: (partyId: number | null | undefined, params: QueryKeyParams = {}) =>
    [...PARTY_KEY, 'projects', partyId ?? 0, normalizePartyProjectsParams(toQueryRecord(params))] as const,
};

export const projectKeys = {
  all: PROJECT_KEY,
  listRoot: () => [...PROJECT_KEY, 'list'] as const,
  list: (params: QueryKeyParams = {}) =>
    [...PROJECT_KEY, 'list', normalizeProjectSearchParams(toQueryRecord(params))] as const,
  summary: (params: QueryKeyParams = {}) =>
    [...PROJECT_KEY, 'summary', normalizeProjectSearchParams(toQueryRecord(params))] as const,
  detail: (projectId: number | null | undefined) => [...PROJECT_KEY, 'detail', projectId ?? 0] as const,
  statuses: () => [...PROJECT_KEY, 'statuses'] as const,
  revenuePlans: (projectId: number | null | undefined) =>
    [...PROJECT_KEY, 'revenue-plans', projectId ?? 0] as const,
  assignmentsRoot: (projectId: number | null | undefined) =>
    [...PROJECT_KEY, 'assignments', projectId ?? 0] as const,
  assignments: (projectId: number | null | undefined, params: QueryKeyParams = {}) =>
    [...PROJECT_KEY, 'assignments', projectId ?? 0, normalizeProjectAssignmentParams(toQueryRecord(params))] as const,
};

export const dashboardKeys = {
  all: DASHBOARD_KEY,
  summary: () => [...DASHBOARD_KEY, 'summary'] as const,
  monthlyRevenueSummary: () => [...DASHBOARD_KEY, 'monthly-revenue-summary'] as const,
};

export const notificationKeys = {
  all: NOTIFICATION_KEY,
  list: () => [...NOTIFICATION_KEY, 'list'] as const,
};

export const chatKeys = {
  all: CHAT_KEY,
  sessions: () => [...CHAT_KEY, 'sessions'] as const,
  recent: (limit: number | null | undefined = 20) =>
    [...CHAT_KEY, 'sessions', 'recent', { limit: normalizeLimit(limit) }] as const,
  favorites: () => [...CHAT_KEY, 'sessions', 'favorites'] as const,
  sessionDetail: (sessionId: string | null | undefined) =>
    [...CHAT_KEY, 'session-detail', sessionId ?? ''] as const,
};

export const adminKeys = {
  all: ADMIN_KEY,
  permissionGroups: {
    all: [...ADMIN_KEY, 'permission-groups'] as const,
    list: (params: QueryKeyParams = {}) =>
      [...ADMIN_KEY, 'permission-groups', 'list', toQueryRecord(params)] as const,
    detail: (permissionGroupId: number | null | undefined) =>
      [...ADMIN_KEY, 'permission-groups', 'detail', permissionGroupId ?? 0] as const,
    catalog: () => [...ADMIN_KEY, 'permission-groups', 'catalog'] as const,
  },
  accounts: {
    all: [...ADMIN_KEY, 'accounts'] as const,
    assignable: (permissionGroupId: number | null | undefined, params: QueryKeyParams = {}) =>
      [...ADMIN_KEY, 'accounts', 'assignable', permissionGroupId ?? 0, toQueryRecord(params)] as const,
  },
};
