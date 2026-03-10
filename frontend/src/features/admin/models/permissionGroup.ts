import type { EnumResponse } from '@/core/api';

export type PermissionGroupType = 'SYSTEM' | 'CUSTOM';

export interface PermissionGroupListItem {
  id: number;
  name: string;
  description: string;
  groupType: PermissionGroupType;
  assignedAccountCount: number;
  grantCount: number;
}

export interface PermissionGrantItem {
  permissionCode: string;
  permissionName: string;
  permissionDescription: string;
  scopes: string[];
}

export interface PermissionGroupAccountItem {
  accountId: number;
  employeeId: number;
  employeeName: string;
  email: string;
  departmentName: string;
  position: EnumResponse;
}

export interface PermissionGroupDetailItem {
  id: number;
  name: string;
  description: string;
  groupType: PermissionGroupType;
  grants: PermissionGrantItem[];
  accounts: PermissionGroupAccountItem[];
}

export interface PermissionCatalogItem {
  code: string;
  name: string;
  description: string;
}

export interface PermissionGroupCatalog {
  permissions: PermissionCatalogItem[];
  scopes: EnumResponse[];
}

export interface PermissionGroupGrantPayload {
  permissionCode: string;
  scopes: string[];
}

export interface PermissionGroupUpsertPayload {
  name: string;
  description: string;
  grants: PermissionGroupGrantPayload[];
}

export interface PermissionGroupFilters {
  name?: string;
  groupType?: PermissionGroupType | 'ALL';
}

export interface AssignableAccountSummary {
  accountId: number;
  employeeId: number;
  employeeName: string;
  email: string;
  departmentName: string;
  position: EnumResponse;
}

export function isPermissionGroupType(value: unknown): value is PermissionGroupType {
  return value === 'SYSTEM' || value === 'CUSTOM';
}

export function mapPermissionGroupListItem(response: any): PermissionGroupListItem {
  return {
    id: Number(response.id ?? 0),
    name: String(response.name ?? ''),
    description: String(response.description ?? ''),
    groupType: isPermissionGroupType(response.groupType) ? response.groupType : 'CUSTOM',
    assignedAccountCount: Number(response.assignedAccountCount ?? 0),
    grantCount: Number(response.grantCount ?? 0),
  };
}

export function mapPermissionGrantItem(response: any): PermissionGrantItem {
  return {
    permissionCode: String(response.permissionCode ?? ''),
    permissionName: String(response.permissionName ?? ''),
    permissionDescription: String(response.permissionDescription ?? ''),
    scopes: Array.isArray(response.scopes)
      ? response.scopes.filter((scope: unknown): scope is string => typeof scope === 'string')
      : [],
  };
}

export function mapPermissionGroupAccountItem(response: any): PermissionGroupAccountItem {
  return {
    accountId: Number(response.accountId ?? 0),
    employeeId: Number(response.employeeId ?? 0),
    employeeName: String(response.employeeName ?? ''),
    email: String(response.email ?? ''),
    departmentName: String(response.departmentName ?? ''),
    position: {
      code: String(response.position?.code ?? ''),
      description: String(response.position?.description ?? ''),
      level: Number(response.position?.level ?? 0),
    },
  };
}

export function mapPermissionGroupDetailItem(response: any): PermissionGroupDetailItem {
  return {
    id: Number(response.id ?? 0),
    name: String(response.name ?? ''),
    description: String(response.description ?? ''),
    groupType: isPermissionGroupType(response.groupType) ? response.groupType : 'CUSTOM',
    grants: Array.isArray(response.grants) ? response.grants.map(mapPermissionGrantItem) : [],
    accounts: Array.isArray(response.accounts)
      ? response.accounts.map(mapPermissionGroupAccountItem)
      : [],
  };
}

export function mapPermissionGroupCatalog(response: any): PermissionGroupCatalog {
  return {
    permissions: Array.isArray(response.permissions)
      ? response.permissions.map((item) => ({
          code: String(item.code ?? ''),
          name: String(item.name ?? ''),
          description: String(item.description ?? ''),
        }))
      : [],
    scopes: Array.isArray(response.scopes)
      ? response.scopes.map((scope) => ({
          code: String(scope.code ?? ''),
          description: String(scope.description ?? ''),
          level: Number(scope.level ?? 0),
        }))
      : [],
  };
}

export function mapAssignableAccountSummary(response: any): AssignableAccountSummary {
  return mapPermissionGroupAccountItem(response);
}
