import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AUTH_HTTP_ERROR_EVENT } from '@/features/auth/http-auth-error';
import { registerAuthHttpErrorHandler } from '@/features/auth/registerAuthHttpErrorHandler';

const sessionMocks = vi.hoisted(() => ({
  clearStoredUserMock: vi.fn(),
}));

vi.mock('@/features/auth/session', () => ({
  clearStoredUser: sessionMocks.clearStoredUserMock,
}));

describe('registerAuthHttpErrorHandler', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.resetModules();
  });

  it('401 이벤트 수신 시 사용자 정보를 지우고 세션 만료 페이지로 이동한다', async () => {
    const push = vi.fn();
    const router = {
      currentRoute: {
        value: {
          name: 'dashboard',
          path: '/',
          fullPath: '/projects?page=2',
        },
      },
      push,
    };

    registerAuthHttpErrorHandler(router as never);
    window.dispatchEvent(
      new CustomEvent(AUTH_HTTP_ERROR_EVENT, {
        detail: { status: 401 },
      }),
    );

    expect(sessionMocks.clearStoredUserMock).toHaveBeenCalled();
    expect(push).toHaveBeenCalledWith({
      name: 'auth-session-expired',
      query: { redirect: '/projects?page=2' },
    });
  });
});
