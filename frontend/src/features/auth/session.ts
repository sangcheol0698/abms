import { appContainer } from '@/core/di/container';
import type { AuthMeResponse } from '@/features/auth/repository/AuthRepository';
import AuthRepository from '@/features/auth/repository/AuthRepository';

const USER_STORAGE_KEY = 'user';
const EMPLOYEE_STORAGE_KEY = 'employee';

interface StoredUser {
  name: string;
  email: string;
  avatar?: string;
}

let isSessionValidated = false;
let validatingPromise: Promise<boolean> | null = null;

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
  };
  localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(normalized));
  isSessionValidated = true;
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

export async function ensureServerSessionValid(): Promise<boolean> {
  if (!hasStoredUser()) {
    return false;
  }

  if (isSessionValidated) {
    return true;
  }

  if (!validatingPromise) {
    const authRepository = appContainer.resolve(AuthRepository);
    validatingPromise = authRepository
      .fetchMe()
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
