import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  clearStoredUser,
  getStoredPermissions,
  getStoredUser,
  hasStoredPermission,
  hasStoredPermissionScope,
  setStoredUser,
} from '@/features/auth/session';

describe('auth session', () => {
  const storage = new Map<string, string>();

  beforeEach(() => {
    storage.clear();
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
});
