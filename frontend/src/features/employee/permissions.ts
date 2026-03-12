import type { DepartmentChartNode } from '@/features/department/models/department';
import { getStoredPermissions, getStoredUser, hasStoredPermission } from '@/features/auth/session';

const EMPLOYEE_READ = 'employee.read';
const EMPLOYEE_WRITE = 'employee.write';
const EMPLOYEE_EXCEL_DOWNLOAD = 'employee.excel.download';
const EMPLOYEE_EXCEL_UPLOAD = 'employee.excel.upload';
const PERMISSION_GROUP_MANAGE = 'permission.group.manage';
const SELF_SCOPE = 'SELF';
const ALL_SCOPE = 'ALL';
const OWN_DEPARTMENT_SCOPE = 'OWN_DEPARTMENT';
const OWN_DEPARTMENT_TREE_SCOPE = 'OWN_DEPARTMENT_TREE';

export interface EmployeePermissionTarget {
  employeeId?: number | null;
  email?: string | null;
  departmentId?: number | null;
}

export interface EmployeePermissionContext {
  departmentChart?: DepartmentChartNode[] | null;
  currentEmployeeId?: number | null;
  currentDepartmentId?: number | null;
}

function normalizeEmail(email: string | null | undefined): string {
  return typeof email === 'string' ? email.trim().toLowerCase() : '';
}

function resolveContext(context?: EmployeePermissionContext): Required<EmployeePermissionContext> {
  const storedUser = getStoredUser();
  return {
    departmentChart: context?.departmentChart ?? null,
    currentEmployeeId: context?.currentEmployeeId ?? storedUser?.employeeId ?? null,
    currentDepartmentId: context?.currentDepartmentId ?? storedUser?.departmentId ?? null,
  };
}

function getPermissionScopes(code: string): string[] {
  return getStoredPermissions()
    .filter((permission) => permission.code === code)
    .flatMap((permission) => permission.scopes);
}

function hasScope(code: string, scope: string): boolean {
  return getPermissionScopes(code).includes(scope);
}

function hasAnyScope(code: string, scopes: string[]): boolean {
  return getPermissionScopes(code).some((scope) => scopes.includes(scope));
}

function findDepartmentNode(
  nodes: DepartmentChartNode[],
  departmentId: number,
): DepartmentChartNode | null {
  for (const node of nodes) {
    if (node.departmentId === departmentId) {
      return node;
    }

    const found = findDepartmentNode(node.children ?? [], departmentId);
    if (found) {
      return found;
    }
  }

  return null;
}

function collectDepartmentTreeIds(
  nodes: DepartmentChartNode[] | null | undefined,
  rootDepartmentId: number | null,
): Set<number> {
  if (!Array.isArray(nodes) || nodes.length === 0 || rootDepartmentId == null) {
    return new Set<number>();
  }

  const root = findDepartmentNode(nodes, rootDepartmentId);
  if (!root) {
    return new Set<number>([rootDepartmentId]);
  }

  const collected = new Set<number>();
  const stack: DepartmentChartNode[] = [root];
  while (stack.length > 0) {
    const current = stack.pop();
    if (!current) {
      continue;
    }
    collected.add(current.departmentId);
    stack.push(...(current.children ?? []));
  }

  return collected;
}

function resolveDepartmentScopedIds(
  code: string,
  context?: EmployeePermissionContext,
): Set<number> {
  const resolvedContext = resolveContext(context);
  const currentDepartmentId = resolvedContext.currentDepartmentId;
  if (currentDepartmentId == null) {
    return new Set<number>();
  }

  const scoped = new Set<number>();
  if (hasScope(code, OWN_DEPARTMENT_SCOPE)) {
    scoped.add(currentDepartmentId);
  }
  if (hasScope(code, OWN_DEPARTMENT_TREE_SCOPE)) {
    collectDepartmentTreeIds(resolvedContext.departmentChart, currentDepartmentId).forEach((departmentId) => {
      scoped.add(departmentId);
    });
  }

  return scoped;
}

function matchesCurrentUser(target: EmployeePermissionTarget, context?: EmployeePermissionContext): boolean {
  const resolvedContext = resolveContext(context);
  if (
    resolvedContext.currentEmployeeId != null &&
    target.employeeId != null &&
    resolvedContext.currentEmployeeId === target.employeeId
  ) {
    return true;
  }

  return isCurrentUserEmail(target.email);
}

function isCurrentUserEmail(email: string | null | undefined): boolean {
  const currentEmail = normalizeEmail(getStoredUser()?.email);
  return currentEmail.length > 0 && currentEmail === normalizeEmail(email);
}

export function canReadEmployeeDetail(): boolean {
  return hasStoredPermission(EMPLOYEE_READ);
}

export function canDownloadEmployeeExcel(): boolean {
  return hasStoredPermission(EMPLOYEE_EXCEL_DOWNLOAD)
    && hasAnyScope(EMPLOYEE_EXCEL_DOWNLOAD, [
      ALL_SCOPE,
      OWN_DEPARTMENT_SCOPE,
      OWN_DEPARTMENT_TREE_SCOPE,
      SELF_SCOPE,
    ]);
}

export function canViewEmployeeDetail(
  target: EmployeePermissionTarget,
  context?: EmployeePermissionContext,
): boolean {
  if (!canReadEmployeeDetail()) {
    return false;
  }

  if (hasScope(EMPLOYEE_READ, ALL_SCOPE)) {
    return true;
  }

  if (hasScope(EMPLOYEE_READ, SELF_SCOPE) && matchesCurrentUser(target, context)) {
    return true;
  }

  if (target.departmentId != null) {
    return resolveDepartmentScopedIds(EMPLOYEE_READ, context).has(target.departmentId);
  }

  return false;
}

export function canManageEmployees(): boolean {
  return hasStoredPermission(EMPLOYEE_WRITE)
    && hasAnyScope(EMPLOYEE_WRITE, [ALL_SCOPE, OWN_DEPARTMENT_SCOPE, OWN_DEPARTMENT_TREE_SCOPE]);
}

export function canManageEmployee(
  target: EmployeePermissionTarget,
  context?: EmployeePermissionContext,
): boolean {
  if (!hasStoredPermission(EMPLOYEE_WRITE)) {
    return false;
  }

  if (hasScope(EMPLOYEE_WRITE, ALL_SCOPE)) {
    return true;
  }

  if (target.departmentId != null) {
    return resolveDepartmentScopedIds(EMPLOYEE_WRITE, context).has(target.departmentId);
  }

  return false;
}

export function canEditOwnProfile(target?: string | EmployeePermissionTarget | null): boolean {
  if (!hasStoredPermission(EMPLOYEE_WRITE)) {
    return false;
  }

  if (!getPermissionScopes(EMPLOYEE_WRITE).includes(SELF_SCOPE)) {
    return false;
  }

  if (target == null) {
    return true;
  }

  if (typeof target === 'string') {
    return isCurrentUserEmail(target);
  }

  return matchesCurrentUser(target);
}

export function canAccessOwnProfileEditor(): boolean {
  return hasStoredPermission(EMPLOYEE_WRITE);
}

export function canUploadEmployeeExcel(): boolean {
  return hasStoredPermission(EMPLOYEE_EXCEL_UPLOAD)
    && hasAnyScope(EMPLOYEE_EXCEL_UPLOAD, [ALL_SCOPE, OWN_DEPARTMENT_SCOPE, OWN_DEPARTMENT_TREE_SCOPE]);
}

export function canAccessDepartmentEmployees(
  departmentId: number | null | undefined,
  context?: EmployeePermissionContext,
): boolean {
  if (!canReadEmployeeDetail() || departmentId == null) {
    return false;
  }

  if (hasScope(EMPLOYEE_READ, ALL_SCOPE)) {
    return true;
  }

  return resolveDepartmentScopedIds(EMPLOYEE_READ, context).has(departmentId);
}

export function canManagePermissionGroups(): boolean {
  return hasStoredPermission(PERMISSION_GROUP_MANAGE);
}
