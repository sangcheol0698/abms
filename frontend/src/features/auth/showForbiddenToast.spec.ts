import { beforeEach, describe, expect, it, vi } from 'vitest';
import { showForbiddenToast } from '@/features/auth/showForbiddenToast';

const toastMocks = vi.hoisted(() => ({
  toastErrorMock: vi.fn(),
}));

vi.mock('vue-sonner', () => ({
  toast: {
    error: toastMocks.toastErrorMock,
  },
}));

describe('showForbiddenToast', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-03-12T00:00:00Z'));
  });

  it('쿨다운 안에서는 중복 토스트를 막는다', () => {
    showForbiddenToast();
    showForbiddenToast();

    expect(toastMocks.toastErrorMock).toHaveBeenCalledTimes(1);

    vi.advanceTimersByTime(1500);
    showForbiddenToast();

    expect(toastMocks.toastErrorMock).toHaveBeenCalledTimes(2);
  });
});
