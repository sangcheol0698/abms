import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { Router } from 'vue-router';

const { authState, ensureCsrfInitializedMock, nprogressMock } = vi.hoisted(() => ({
  authState: {
    hasStoredUser: false,
    hasStoredPermission: false,
    ensureServerSessionValid: false,
    ensureServerSessionDelayMs: 0,
  },
  ensureCsrfInitializedMock: vi.fn(),
  nprogressMock: {
    configure: vi.fn(),
    start: vi.fn(),
    done: vi.fn(),
    isStarted: vi.fn(() => false),
  },
}));

vi.mock('@/features/auth/session', () => ({
  hasStoredUser: () => authState.hasStoredUser,
  hasStoredPermission: () => authState.hasStoredPermission,
  ensureServerSessionValid: vi.fn(
    () =>
      new Promise<boolean>((resolve) => {
        setTimeout(
          () => resolve(authState.ensureServerSessionValid),
          authState.ensureServerSessionDelayMs,
        );
      }),
  ),
}));

vi.mock('@/core/http/csrf', () => ({
  ensureCsrfInitialized: ensureCsrfInitializedMock,
}));

vi.mock('nprogress', () => ({
  default: nprogressMock,
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

vi.mock('@/features/dashboard/views/DashboardView.vue', () => ({
  default: { template: '<div>dashboard</div>' },
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
    vi.useRealTimers();
    storage.clear();
    authState.hasStoredUser = false;
    authState.hasStoredPermission = false;
    authState.ensureServerSessionValid = false;
    authState.ensureServerSessionDelayMs = 0;
    ensureCsrfInitializedMock.mockResolvedValue(undefined);
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

  it('권한 없는 redirect 이동은 로그인 페이지에 머무르지 않고 금지 페이지로 보낸다', async () => {
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
  }, 15000);

  it('빠른 라우트 이동에서는 nprogress를 표시하지 않는다', async () => {
    authState.hasStoredUser = true;
    authState.ensureServerSessionValid = true;

    const router = await loadRouter();

    await router.push('/');
    await router.isReady();

    expect(nprogressMock.start).not.toHaveBeenCalled();
    expect(nprogressMock.done).toHaveBeenCalled();
  });

  it('느린 라우트 이동에서는 nprogress를 시작하고 완료한다', async () => {
    vi.useFakeTimers();
    authState.hasStoredUser = true;
    authState.ensureServerSessionValid = true;
    authState.ensureServerSessionDelayMs = 250;

    const router = await loadRouter();
    const navigation = router.push('/');

    await vi.advanceTimersByTimeAsync(130);
    expect(nprogressMock.start).toHaveBeenCalled();

    await vi.advanceTimersByTimeAsync(200);
    await navigation;
    await router.isReady();

    expect(nprogressMock.done).toHaveBeenCalled();
  });
});
