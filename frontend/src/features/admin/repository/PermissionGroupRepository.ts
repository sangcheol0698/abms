import { inject, singleton } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';
import {
  type AssignableAccountSummary,
  type PermissionGroupCatalog,
  type PermissionGroupDetailItem,
  type PermissionGroupFilters,
  type PermissionGroupListItem,
  type PermissionGroupUpsertPayload,
  mapAssignableAccountSummary,
  mapPermissionGroupCatalog,
  mapPermissionGroupDetailItem,
  mapPermissionGroupListItem,
} from '@/features/admin/models/permissionGroup';

function buildGroupSearchParams(filters: PermissionGroupFilters): Record<string, string> {
  const params: Record<string, string> = {};

  if (filters.name?.trim()) {
    params.name = filters.name.trim();
  }

  if (filters.groupType && filters.groupType !== 'ALL') {
    params.groupType = filters.groupType;
  }

  return params;
}

@singleton()
export default class PermissionGroupRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  async search(filters: PermissionGroupFilters): Promise<PermissionGroupListItem[]> {
    const response = await this.httpRepository.get({
      path: '/api/admin/permission-groups',
      params: buildGroupSearchParams(filters),
    });

    return Array.isArray(response) ? response.map(mapPermissionGroupListItem) : [];
  }

  async findById(id: number): Promise<PermissionGroupDetailItem> {
    const response = await this.httpRepository.get({
      path: `/api/admin/permission-groups/${id}`,
    });

    return mapPermissionGroupDetailItem(response);
  }

  async fetchCatalog(): Promise<PermissionGroupCatalog> {
    const response = await this.httpRepository.get({
      path: '/api/admin/permission-groups/catalog',
    });

    return mapPermissionGroupCatalog(response);
  }

  async searchAssignableAccounts(
    permissionGroupId: number,
    keyword?: string,
  ): Promise<AssignableAccountSummary[]> {
    const response = await this.httpRepository.get({
      path: '/api/admin/accounts',
      params: {
        permissionGroupId,
        ...(keyword?.trim() ? { keyword: keyword.trim() } : {}),
      },
    });

    return Array.isArray(response) ? response.map(mapAssignableAccountSummary) : [];
  }

  async create(payload: PermissionGroupUpsertPayload): Promise<number> {
    const response = await this.httpRepository.post<number>({
      path: '/api/admin/permission-groups',
      data: payload,
    });

    return Number(response ?? 0);
  }

  async update(id: number, payload: PermissionGroupUpsertPayload): Promise<void> {
    await this.httpRepository.put({
      path: `/api/admin/permission-groups/${id}`,
      data: payload,
    });
  }

  async delete(id: number): Promise<void> {
    await this.httpRepository.delete({
      path: `/api/admin/permission-groups/${id}`,
    });
  }

  async assignAccount(permissionGroupId: number, accountId: number): Promise<void> {
    await this.httpRepository.post({
      path: `/api/admin/permission-groups/${permissionGroupId}/accounts`,
      data: { accountId },
    });
  }

  async unassignAccount(permissionGroupId: number, accountId: number): Promise<void> {
    await this.httpRepository.delete({
      path: `/api/admin/permission-groups/${permissionGroupId}/accounts/${accountId}`,
    });
  }
}
