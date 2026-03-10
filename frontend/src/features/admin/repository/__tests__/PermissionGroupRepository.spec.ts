import 'reflect-metadata';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type HttpRepository from '@/core/http/HttpRepository';
import PermissionGroupRepository from '@/features/admin/repository/PermissionGroupRepository';

describe('PermissionGroupRepository', () => {
  let httpGet: ReturnType<typeof vi.fn>;
  let httpPost: ReturnType<typeof vi.fn>;
  let httpPut: ReturnType<typeof vi.fn>;
  let httpDelete: ReturnType<typeof vi.fn>;
  let repository: PermissionGroupRepository;

  beforeEach(() => {
    httpGet = vi.fn().mockResolvedValue(undefined);
    httpPost = vi.fn().mockResolvedValue(undefined);
    httpPut = vi.fn().mockResolvedValue(undefined);
    httpDelete = vi.fn().mockResolvedValue(undefined);

    repository = new PermissionGroupRepository({
      get: httpGet,
      post: httpPost,
      put: httpPut,
      delete: httpDelete,
    } as unknown as HttpRepository);
  });

  it('권한 그룹 목록을 조회한다', async () => {
    httpGet.mockResolvedValueOnce([
      {
        id: 1,
        name: '최고 관리자 그룹',
        description: '설명',
        groupType: 'SYSTEM',
        assignedAccountCount: 2,
        grantCount: 5,
      },
    ]);

    const result = await repository.search({
      name: '관리자',
      groupType: 'SYSTEM',
    });

    expect(httpGet).toHaveBeenCalledWith({
      path: '/api/admin/permission-groups',
      params: {
        name: '관리자',
        groupType: 'SYSTEM',
      },
    });
    expect(result[0]).toMatchObject({
      id: 1,
      groupType: 'SYSTEM',
      assignedAccountCount: 2,
    });
  });

  it('권한 그룹 상세를 조회한다', async () => {
    httpGet.mockResolvedValueOnce({
      id: 3,
      name: '운영 그룹',
      description: '운영 설명',
      groupType: 'CUSTOM',
      grants: [
        {
          permissionCode: 'employee.read',
          permissionName: '직원 조회',
          permissionDescription: '직원 상세 조회',
          scopes: ['SELF'],
        },
      ],
      accounts: [
        {
          accountId: 1,
          employeeId: 1,
          employeeName: '홍길동',
          email: 'hong@abacus.co.kr',
          departmentName: '개발팀',
          position: {
            code: 'ASSOCIATE',
            description: 'Associate',
            level: 1,
          },
        },
      ],
    });

    const result = await repository.findById(3);

    expect(httpGet).toHaveBeenCalledWith({
      path: '/api/admin/permission-groups/3',
    });
    expect(result.grants[0].permissionCode).toBe('employee.read');
    expect(result.accounts[0].employeeName).toBe('홍길동');
  });

  it('권한 카탈로그를 조회한다', async () => {
    httpGet.mockResolvedValueOnce({
      permissions: [
        {
          code: 'permission.group.manage',
          name: '권한 그룹 관리',
          description: '관리 권한',
        },
      ],
      scopes: [
        {
          code: 'ALL',
          description: '전체',
          level: 1,
        },
      ],
    });

    const result = await repository.fetchCatalog();

    expect(httpGet).toHaveBeenCalledWith({
      path: '/api/admin/permission-groups/catalog',
    });
    expect(result.permissions[0].code).toBe('permission.group.manage');
    expect(result.scopes[0].description).toBe('전체');
  });

  it('추가 가능한 계정을 검색한다', async () => {
    httpGet.mockResolvedValueOnce([
      {
        accountId: 2,
        employeeId: 2,
        employeeName: '김개발',
        email: 'kim@abacus.co.kr',
        departmentName: '플랫폼팀',
        position: {
          code: 'PRINCIPAL',
          description: 'Principal',
          level: 3,
        },
      },
    ]);

    const result = await repository.searchAssignableAccounts(10, '김');

    expect(httpGet).toHaveBeenCalledWith({
      path: '/api/admin/accounts',
      params: {
        permissionGroupId: 10,
        keyword: '김',
      },
    });
    expect(result[0].accountId).toBe(2);
  });

  it('권한 그룹을 생성하고 수정하고 삭제한다', async () => {
    httpPost.mockResolvedValueOnce(5);

    const payload = {
      name: '운영 그룹',
      description: '설명',
      grants: [{ permissionCode: 'employee.read', scopes: ['SELF'] }],
    };

    const createdId = await repository.create(payload);
    await repository.update(createdId, payload);
    await repository.delete(createdId);

    expect(createdId).toBe(5);
    expect(httpPost).toHaveBeenCalledWith({
      path: '/api/admin/permission-groups',
      data: payload,
    });
    expect(httpPut).toHaveBeenCalledWith({
      path: '/api/admin/permission-groups/5',
      data: payload,
    });
    expect(httpDelete).toHaveBeenCalledWith({
      path: '/api/admin/permission-groups/5',
    });
  });

  it('계정을 권한 그룹에 할당하고 해제한다', async () => {
    await repository.assignAccount(7, 11);
    await repository.unassignAccount(7, 11);

    expect(httpPost).toHaveBeenCalledWith({
      path: '/api/admin/permission-groups/7/accounts',
      data: { accountId: 11 },
    });
    expect(httpDelete).toHaveBeenCalledWith({
      path: '/api/admin/permission-groups/7/accounts/11',
    });
  });
});
