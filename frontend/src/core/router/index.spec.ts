import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { Router } from 'vue-router';

const { authState, csrfRequestMock } = vi.hoisted(() => ({
  authState: {
    hasStoredUser: false,
    hasStoredPermission: false,
    ensureServerSessionValid: false,
  },
  csrfRequestMock: vi.fn(),
}));

vi.mock('@/features/auth/session', () => ({
  hasStoredUser: () => authState.hasStoredUser,
  hasStoredPermission: () => authState.hasStoredPermission,
  ensureServerSessionValid: vi.fn(() => Promise.resolve(authState.ensureServerSessionValid)),
}));

vi.mock('@/core/http/AxiosHttpClient', () => ({
  default: class AxiosHttpClient {
    request = csrfRequestMock;
  },
}));

vi.mock('@/core/layouts/AuthLayout.vue', () => ({
  default: { template: '<div><slot /></div>' },
}));

vi.mock('@/core/layouts/SidebarLayout.vue', () => ({
  default: { template: '<div><slot /></div>' },
}));

vi.mock('@/features/auth/views/AuthLoginView.vue', () => ({
  default: { template: '<div>login</div>' },
}));

vi.mock('@/features/auth/views/AuthForbiddenView.vue', () => ({
  default: { template: '<div>forbidden</div>' },
}));

vi.mock('@/features/admin/views/PermissionGroupManagementView.vue', () => ({
  default: { template: '<div>permission groups</div>' },
}));

async function loadRouter(): Promise<Router> {
  const module = await import('@/core/router');
  return module.default;
}

describe('router permission guard', () => {
  const storage = new Map<string, string>();

  beforeEach(() => {
    vi.resetModules();
    vi.clearAllMocks();
    storage.clear();
    authState.hasStoredUser = false;
    authState.hasStoredPermission = false;
    authState.ensureServerSessionValid = false;
    csrfRequestMock.mockResolvedValue(undefined);
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
    window.history.replaceState({}, '', '/');
  });

  it(
    '권한 없는 redirect 이동은 로그인 페이지에 머무르지 않고 금지 페이지로 보낸다',
    async () => {
      const router = await loadRouter();

      await router.push('/system/permission-groups');
      await router.isReady();

      expect(router.currentRoute.value.name).toBe('auth-login');
      expect(router.currentRoute.value.query.redirect).toBe('/system/permission-groups');

      authState.hasStoredUser = true;
      authState.ensureServerSessionValid = true;

      await router.push(String(router.currentRoute.value.query.redirect));

      expect(router.currentRoute.value.name).toBe('auth-forbidden');
      expect(router.currentRoute.value.path).toBe('/auths/forbidden');
    },
    15000,
  );
});
