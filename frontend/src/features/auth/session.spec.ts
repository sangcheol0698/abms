import { beforeEach, describe, expect, it, vi } from 'vitest';
import { queryClient } from '@/core/query';
import type { AuthMeResponse } from '@/features/auth/repository/AuthRepository';
import {
  clearStoredUser,
  ensureServerSessionValid,
  getStoredPermissions,
  getStoredUser,
  hasStoredPermission,
  hasStoredPermissionScope,
  setStoredUser,
} from '@/features/auth/session';

const { fetchMeMock, resolveMock } = vi.hoisted(() => ({
  fetchMeMock: vi.fn<() => Promise<AuthMeResponse>>(),
  resolveMock: vi.fn(),
}));

vi.mock('@/core/di/container', () => ({
  appContainer: {
    resolve: resolveMock,
  },
}));

describe('auth session', () => {
  const storage = new Map<string, string>();

  beforeEach(() => {
    queryClient.clear();
    storage.clear();
    vi.clearAllMocks();
    vi.stubGlobal('localStorage', {
      getItem: (key: string) => storage.get(key) ?? null,
      setItem: (key: string, value: string) => {
        storage.set(key, value);
      },
      removeItem: (key: string) => {
        storage.delete(key);
      },
      clear: () => {
        storage.clear();
      },
    });
    resolveMock.mockReturnValue({
      fetchMe: fetchMeMock,
    });
    clearStoredUser();
  });

  it('권한 정보를 세션에 저장하고 다시 읽을 수 있다', () => {
    setStoredUser({
      email: 'user@abms.co.kr',
      name: '사용자',
      permissions: [
        {
          code: 'employee.read',
          scopes: ['SELF'],
        },
        {
          code: 'employee.write',
          scopes: ['OWN_DEPARTMENT'],
        },
      ],
    });

    expect(getStoredUser()).toEqual({
      email: 'user@abms.co.kr',
      name: '사용자',
      avatar: '',
      permissions: [
        {
          code: 'employee.read',
          scopes: ['SELF'],
        },
        {
          code: 'employee.write',
          scopes: ['OWN_DEPARTMENT'],
        },
      ],
    });
    expect(getStoredPermissions()).toHaveLength(2);
    expect(hasStoredPermission('employee.read')).toBe(true);
    expect(hasStoredPermissionScope('employee.read', 'SELF')).toBe(true);
    expect(hasStoredPermissionScope('employee.write', 'SELF')).toBe(false);
  });

  it('기존 저장 데이터에 permissions가 없어도 안전하게 읽는다', () => {
    localStorage.setItem(
      'user',
      JSON.stringify({
        email: 'legacy@abms.co.kr',
        name: 'Legacy',
      }),
    );

    expect(getStoredUser()).toEqual({
      email: 'legacy@abms.co.kr',
      name: 'Legacy',
      avatar: '',
      permissions: [],
    });
    expect(getStoredPermissions()).toEqual([]);
  });

  it('미검증 사용자 정보는 다음 검증에서 /api/auth/me를 다시 조회한다', async () => {
    fetchMeMock.mockResolvedValue({
      email: 'user@abms.co.kr',
      name: '사용자',
      permissions: [
        {
          code: 'employee.read',
          scopes: ['SELF'],
        },
      ],
    });

    setStoredUser(
      {
        email: 'user@abms.co.kr',
        name: '사용자',
        permissions: [],
      },
      { validated: false },
    );

    await expect(ensureServerSessionValid()).resolves.toBe(true);
    expect(fetchMeMock).toHaveBeenCalledTimes(1);
    expect(getStoredPermissions()).toEqual([
      {
        code: 'employee.read',
        scopes: ['SELF'],
      },
    ]);

    await expect(ensureServerSessionValid()).resolves.toBe(true);
    expect(fetchMeMock).toHaveBeenCalledTimes(1);
  });
});
