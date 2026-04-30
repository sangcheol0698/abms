import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises } from '@vue/test-utils';
import { type RouteRecordRaw } from 'vue-router';
import HttpError from '@/core/http/HttpError';
import AuthPasswordResetConfirmView from '@/features/auth/views/AuthPasswordResetConfirmView.vue';
import { renderWithProviders } from '@/test-utils';
import { toast } from 'vue-sonner';

const mutateAsyncMock = vi.fn();

vi.mock('@/features/auth/queries/useAuthQueries', () => ({
  useConfirmPasswordResetMutation: () => ({
    mutateAsync: mutateAsyncMock,
  }),
}));

vi.mock('vue-sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

const routes: RouteRecordRaw[] = [
  {
    path: '/auths/password-reset-confirm',
    name: 'auth-password-reset-confirm',
    component: AuthPasswordResetConfirmView,
  },
  { path: '/auths/password-reset', name: 'auth-password-reset', component: { template: '<div>reset</div>' } },
  { path: '/auths/login', name: 'auth-login', component: { template: '<div>login</div>' } },
];

async function mountView(path = '/auths/password-reset-confirm?token=reset-token') {
  return renderWithProviders(AuthPasswordResetConfirmView, {
    routes,
    route: path,
  });
}

describe('AuthPasswordResetConfirmView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mutateAsyncMock.mockResolvedValue(undefined);
  });

  it('토큰이 없으면 제출할 수 없다는 안내를 보여준다', async () => {
    const { wrapper } = await mountView('/auths/password-reset-confirm');

    expect(wrapper.text()).toContain('비밀번호 재설정 토큰이 없습니다.');
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined();
  });

  it('비밀번호 확인이 다르면 검증 메시지를 보여준다', async () => {
    const { wrapper } = await mountView();

    await wrapper.find('#password').setValue('ResetPassword123!');
    await wrapper.find('#passwordConfirm').setValue('OtherPassword123!');
    await wrapper.find('form').trigger('submit.prevent');

    expect(wrapper.text()).toContain('비밀번호가 일치하지 않습니다.');
    expect(mutateAsyncMock).not.toHaveBeenCalled();
  });

  it('토큰과 새 비밀번호로 재설정을 확정하고 로그인 화면으로 이동한다', async () => {
    const { wrapper, router } = await mountView();

    await wrapper.find('#password').setValue('ResetPassword123!');
    await wrapper.find('#passwordConfirm').setValue('ResetPassword123!');
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();

    expect(mutateAsyncMock).toHaveBeenCalledWith({
      token: 'reset-token',
      password: 'ResetPassword123!',
    });
    expect(toast.success).toHaveBeenCalledWith('비밀번호를 재설정했습니다. 새 비밀번호로 로그인해 주세요.');
    expect(router.currentRoute.value.path).toBe('/auths/login');
  });

  it('재설정 실패 시 서버 메시지를 보여준다', async () => {
    mutateAsyncMock.mockRejectedValueOnce(new HttpError({ message: '만료된 토큰입니다.' }));
    const { wrapper } = await mountView();

    await wrapper.find('#password').setValue('ResetPassword123!');
    await wrapper.find('#passwordConfirm').setValue('ResetPassword123!');
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();

    expect(wrapper.text()).toContain('만료된 토큰입니다.');
    expect(toast.error).toHaveBeenCalledWith('비밀번호 재설정에 실패했습니다.', {
      description: '만료된 토큰입니다.',
    });
  });
});
