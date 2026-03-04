import {
  normalizeDepartmentEmployeesParams,
  normalizeEmployeeSearchParams,
  normalizeLimit,
  normalizePartySearchParams,
  normalizeProjectSearchParams,
} from '@/core/query/normalizeParams';

const AUTH_KEY = ['auth'] as const;
const EMPLOYEE_KEY = ['employee'] as const;
const DEPARTMENT_KEY = ['department'] as const;
const PARTY_KEY = ['party'] as const;
const PROJECT_KEY = ['project'] as const;
const DASHBOARD_KEY = ['dashboard'] as const;
const NOTIFICATION_KEY = ['notification'] as const;
const CHAT_KEY = ['chat'] as const;

export const authKeys = {
  all: AUTH_KEY,
  me: () => [...AUTH_KEY, 'me'] as const,
};

export const employeeKeys = {
  all: EMPLOYEE_KEY,
  listRoot: () => [...EMPLOYEE_KEY, 'list'] as const,
  list: (params: Record<string, unknown> = {}) =>
    [...EMPLOYEE_KEY, 'list', normalizeEmployeeSearchParams(params)] as const,
  detail: (employeeId: number | null | undefined) =>
    [...EMPLOYEE_KEY, 'detail', employeeId ?? 0] as const,
  statuses: () => [...EMPLOYEE_KEY, 'statuses'] as const,
  types: () => [...EMPLOYEE_KEY, 'types'] as const,
  grades: () => [...EMPLOYEE_KEY, 'grades'] as const,
  positions: () => [...EMPLOYEE_KEY, 'positions'] as const,
  avatars: () => [...EMPLOYEE_KEY, 'avatars'] as const,
  positionHistory: (employeeId: number | null | undefined) =>
    [...EMPLOYEE_KEY, 'position-history', employeeId ?? 0] as const,
};

export const departmentKeys = {
  all: DEPARTMENT_KEY,
  organizationChart: () => [...DEPARTMENT_KEY, 'organization-chart'] as const,
  detail: (departmentId: number | null | undefined) =>
    [...DEPARTMENT_KEY, 'detail', departmentId ?? 0] as const,
  employeesRoot: (departmentId: number | null | undefined) =>
    [...DEPARTMENT_KEY, 'employees', departmentId ?? 0] as const,
  employees: (departmentId: number | null | undefined, params: Record<string, unknown> = {}) =>
    [...DEPARTMENT_KEY, 'employees', departmentId ?? 0, normalizeDepartmentEmployeesParams(params)] as const,
};

export const partyKeys = {
  all: PARTY_KEY,
  listRoot: () => [...PARTY_KEY, 'list'] as const,
  list: (params: Record<string, unknown> = {}) =>
    [...PARTY_KEY, 'list', normalizePartySearchParams(params)] as const,
  detail: (partyId: number | null | undefined) => [...PARTY_KEY, 'detail', partyId ?? 0] as const,
  projects: (partyId: number | null | undefined) => [...PARTY_KEY, 'projects', partyId ?? 0] as const,
  options: () => [...PARTY_KEY, 'options'] as const,
};

export const projectKeys = {
  all: PROJECT_KEY,
  listRoot: () => [...PROJECT_KEY, 'list'] as const,
  list: (params: Record<string, unknown> = {}) =>
    [...PROJECT_KEY, 'list', normalizeProjectSearchParams(params)] as const,
  detail: (projectId: number | null | undefined) => [...PROJECT_KEY, 'detail', projectId ?? 0] as const,
  statuses: () => [...PROJECT_KEY, 'statuses'] as const,
  revenuePlans: (projectId: number | null | undefined) =>
    [...PROJECT_KEY, 'revenue-plans', projectId ?? 0] as const,
  assignments: (projectId: number | null | undefined) =>
    [...PROJECT_KEY, 'assignments', projectId ?? 0] as const,
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
