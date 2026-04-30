import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises } from '@vue/test-utils';
import { type RouteRecordRaw } from 'vue-router';
import HttpError from '@/core/http/HttpError';
import AuthPasswordResetRequestView from '@/features/auth/views/AuthPasswordResetRequestView.vue';
import { renderWithProviders } from '@/test-utils';
import { toast } from 'vue-sonner';

const mutateAsyncMock = vi.fn();

vi.mock('@/features/auth/queries/useAuthQueries', () => ({
  useRequestPasswordResetMutation: () => ({
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
  { path: '/auths/password-reset', name: 'auth-password-reset', component: AuthPasswordResetRequestView },
  { path: '/auths/login', name: 'auth-login', component: { template: '<div>login</div>' } },
];

async function mountView() {
  return renderWithProviders(AuthPasswordResetRequestView, {
    routes,
    route: '/auths/password-reset',
  });
}

describe('AuthPasswordResetRequestView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mutateAsyncMock.mockResolvedValue(undefined);
  });

  it('회사 이메일이 아니면 검증 메시지를 보여준다', async () => {
    const { wrapper } = await mountView();

    await wrapper.find('#email').setValue('user@example.com');
    await wrapper.find('form').trigger('submit.prevent');

    expect(wrapper.text()).toContain('회사 이메일(@iabacus.co.kr)만 사용할 수 있습니다.');
    expect(mutateAsyncMock).not.toHaveBeenCalled();
  });

  it('이메일을 trim/lowercase 처리해서 재설정 요청을 보낸다', async () => {
    const { wrapper } = await mountView();

    await wrapper.find('#email').setValue('  User@IABACUS.CO.KR  ');
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();

    expect(mutateAsyncMock).toHaveBeenCalledWith({ email: 'user@iabacus.co.kr' });
    expect(wrapper.text()).toContain('입력한 이메일이 가입된 계정이면');
    expect(toast.success).toHaveBeenCalledWith('비밀번호 재설정 안내를 확인해 주세요.');
  });

  it('요청 실패 시 서버 메시지를 보여준다', async () => {
    mutateAsyncMock.mockRejectedValueOnce(new HttpError({ message: '메일 발송 실패' }));
    const { wrapper } = await mountView();

    await wrapper.find('#email').setValue('user@iabacus.co.kr');
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();

    expect(wrapper.text()).toContain('메일 발송 실패');
    expect(toast.error).toHaveBeenCalledWith('비밀번호 재설정 요청에 실패했습니다.', {
      description: '메일 발송 실패',
    });
  });
});
