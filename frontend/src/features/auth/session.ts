import { appContainer } from '@/core/di/container';
import type { AuthMeResponse } from '@/features/auth/repository/AuthRepository';
import AuthRepository from '@/features/auth/repository/AuthRepository';
import { authKeys, queryClient } from '@/core/query';

const USER_STORAGE_KEY = 'user';
const EMPLOYEE_STORAGE_KEY = 'employee';

interface StoredUser {
  name: string;
  email: string;
  avatar?: string;
  permissions: StoredPermission[];
}

export interface StoredPermission {
  code: string;
  scopes: string[];
}

let isSessionValidated = false;
let validatingPromise: Promise<boolean> | null = null;

function normalizePermissions(rawPermissions: unknown): StoredPermission[] {
  if (!Array.isArray(rawPermissions)) {
    return [];
  }

  return rawPermissions
    .map((permission): StoredPermission | null => {
      if (!permission || typeof permission !== 'object') {
        return null;
      }

      const code =
        'code' in permission && typeof permission.code === 'string' ? permission.code.trim() : '';
      if (!code) {
        return null;
      }

      const scopes =
        'scopes' in permission && Array.isArray(permission.scopes)
          ? permission.scopes.filter((scope): scope is string => typeof scope === 'string')
          : [];

      return {
        code,
        scopes,
      };
    })
    .filter((permission): permission is StoredPermission => permission !== null);
}

function parseStoredUser(raw: string | null): StoredUser | null {
  if (!raw) {
    return null;
  }
  try {
    const parsed = JSON.parse(raw) as Partial<StoredUser> | null;
    if (!parsed || typeof parsed !== 'object') {
      return null;
    }
    if (typeof parsed.email !== 'string' || parsed.email.trim().length === 0) {
      return null;
    }
    return {
      email: parsed.email.trim(),
      name: typeof parsed.name === 'string' && parsed.name.trim().length > 0 ? parsed.name : 'User',
      avatar: typeof parsed.avatar === 'string' ? parsed.avatar : '',
      permissions: normalizePermissions(parsed.permissions),
    };
  } catch {
    return null;
  }
}

export function getStoredUser(): StoredUser | null {
  return parseStoredUser(localStorage.getItem(USER_STORAGE_KEY));
}

export function hasStoredUser(): boolean {
  return getStoredUser() !== null;
}

export function setStoredUser(user: AuthMeResponse | StoredUser): void {
  const normalized: StoredUser = {
    name: user.name?.trim() || 'User',
    email: user.email.trim(),
    avatar: 'avatar' in user && typeof user.avatar === 'string' ? user.avatar : '',
    permissions: 'permissions' in user ? normalizePermissions(user.permissions) : [],
  };
  localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(normalized));
  isSessionValidated = true;
}

export function getStoredPermissions(): StoredPermission[] {
  return getStoredUser()?.permissions ?? [];
}

export function hasStoredPermission(code: string): boolean {
  return getStoredPermissions().some((permission) => permission.code === code);
}

export function hasStoredPermissionScope(code: string, scope: string): boolean {
  return getStoredPermissions().some(
    (permission) => permission.code === code && permission.scopes.includes(scope),
  );
}

export function clearStoredUser(): void {
  localStorage.removeItem(USER_STORAGE_KEY);
  localStorage.removeItem(EMPLOYEE_STORAGE_KEY);
  isSessionValidated = false;
  validatingPromise = null;
}

export function markSessionNeedsValidation(): void {
  isSessionValidated = false;
}

export async function ensureServerSessionValid(forceRefresh = false): Promise<boolean> {
  if (!hasStoredUser()) {
    return false;
  }

  if (isSessionValidated && !forceRefresh) {
    return true;
  }

  if (!validatingPromise) {
    const authRepository = appContainer.resolve(AuthRepository);
    validatingPromise = queryClient
      .fetchQuery({
        queryKey: authKeys.me(),
        queryFn: () => authRepository.fetchMe(),
      })
      .then((me) => {
        setStoredUser(me);
        return true;
      })
      .catch(() => {
        clearStoredUser();
        return false;
      })
      .finally(() => {
        validatingPromise = null;
      });
  }

  return validatingPromise;
}
