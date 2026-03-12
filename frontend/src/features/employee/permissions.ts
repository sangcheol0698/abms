import { getStoredPermissions, getStoredUser, hasStoredPermission } from '@/features/auth/session';

const EMPLOYEE_READ = 'employee.read';
const EMPLOYEE_WRITE = 'employee.write';
const EMPLOYEE_EXCEL_DOWNLOAD = 'employee.excel.download';
const EMPLOYEE_EXCEL_UPLOAD = 'employee.excel.upload';
const PERMISSION_GROUP_MANAGE = 'permission.group.manage';
const SELF_SCOPE = 'SELF';

function normalizeEmail(email: string | null | undefined): string {
  return typeof email === 'string' ? email.trim().toLowerCase() : '';
}

function getPermissionScopes(code: string): string[] {
  return getStoredPermissions()
    .filter((permission) => permission.code === code)
    .flatMap((permission) => permission.scopes);
}

function hasOnlySelfScope(code: string): boolean {
  const scopes = getPermissionScopes(code);
  return scopes.length > 0 && scopes.every((scope) => scope === SELF_SCOPE);
}

function hasNonSelfScope(code: string): boolean {
  return getPermissionScopes(code).some((scope) => scope !== SELF_SCOPE);
}

function isCurrentUserEmail(email: string | null | undefined): boolean {
  const currentEmail = normalizeEmail(getStoredUser()?.email);
  return currentEmail.length > 0 && currentEmail === normalizeEmail(email);
}

export function canReadEmployeeDetail(): boolean {
  return hasStoredPermission(EMPLOYEE_READ);
}

export function canDownloadEmployeeExcel(): boolean {
  return hasStoredPermission(EMPLOYEE_EXCEL_DOWNLOAD);
}

export function canViewEmployeeDetail(employeeEmail: string | null | undefined): boolean {
  if (!canReadEmployeeDetail()) {
    return false;
  }

  if (hasNonSelfScope(EMPLOYEE_READ)) {
    return true;
  }

  if (hasOnlySelfScope(EMPLOYEE_READ)) {
    return isCurrentUserEmail(employeeEmail);
  }

  return false;
}

export function canManageEmployees(): boolean {
  return hasStoredPermission(EMPLOYEE_WRITE) && hasNonSelfScope(EMPLOYEE_WRITE);
}

export function canManageEmployee(employeeEmail: string | null | undefined): boolean {
  if (canManageEmployees()) {
    return true;
  }

  return canEditOwnProfile(employeeEmail);
}

export function canEditOwnProfile(employeeEmail?: string | null): boolean {
  if (!hasStoredPermission(EMPLOYEE_WRITE)) {
    return false;
  }

  if (!getPermissionScopes(EMPLOYEE_WRITE).includes(SELF_SCOPE)) {
    return false;
  }

  if (employeeEmail == null) {
    return true;
  }

  return isCurrentUserEmail(employeeEmail);
}

export function canAccessOwnProfileEditor(): boolean {
  return hasStoredPermission(EMPLOYEE_WRITE);
}

export function canUploadEmployeeExcel(): boolean {
  return hasStoredPermission(EMPLOYEE_EXCEL_UPLOAD) && hasNonSelfScope(EMPLOYEE_EXCEL_UPLOAD);
}

export function canManagePermissionGroups(): boolean {
  return hasStoredPermission(PERMISSION_GROUP_MANAGE);
}
