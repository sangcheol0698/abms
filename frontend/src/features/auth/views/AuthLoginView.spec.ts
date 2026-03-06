import { beforeEach, describe, expect, it, vi } from 'vitest';
import { type RouteRecordRaw } from 'vue-router';
import { flushPromises } from '@vue/test-utils';
import HttpError from '@/core/http/HttpError';
import AuthLoginView from '@/features/auth/views/AuthLoginView.vue';
import { renderWithProviders } from '@/test-utils';
import { toast } from 'vue-sonner';
import { clearStoredUser, setStoredUser } from '@/features/auth/session';

const mutateAsyncMock = vi.fn();
const refetchMock = vi.fn();

vi.mock('@/features/auth/queries/useAuthQueries', () => ({
  useLoginMutation: () => ({
    mutateAsync: mutateAsyncMock,
  }),
  useAuthMeQuery: () => ({
    refetch: refetchMock,
  }),
}));

vi.mock('@/features/auth/session', () => ({
  setStoredUser: vi.fn(),
  clearStoredUser: vi.fn(),
}));

vi.mock('vue-sonner', () => ({
  toast: {
    error: vi.fn(),
  },
}));

const routes: RouteRecordRaw[] = [
  { path: '/auths/login', name: 'auth-login', component: AuthLoginView },
  { path: '/', name: 'home', component: { template: '<div>home</div>' } },
  { path: '/employees', name: 'employees', component: { template: '<div>employees</div>' } },
  {
    path: '/auths/register',
    name: 'auth-register',
    component: { template: '<div>register</div>' },
  },
];

async function mountLoginView(path = '/auths/login') {
  return renderWithProviders(AuthLoginView, {
    routes,
    route: path,
  });
}

describe('AuthLoginView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mutateAsyncMock.mockResolvedValue(undefined);
    refetchMock.mockResolvedValue({
      data: {
        email: 'user@abms.co.kr',
        name: '사용자',
      },
    });
  });

  it('이메일이 비어 있으면 검증 메시지를 보여준다', async () => {
    const { wrapper } = await mountLoginView();

    await wrapper.find('#password').setValue('password123!');
    await wrapper.find('form').trigger('submit.prevent');

    expect(wrapper.text()).toContain('이메일을 입력해 주세요.');
    expect(mutateAsyncMock).not.toHaveBeenCalled();
  });

  it('비밀번호가 비어 있으면 검증 메시지를 보여준다', async () => {
    const { wrapper } = await mountLoginView();

    await wrapper.find('#username').setValue('user@abms.co.kr');
    await wrapper.find('form').trigger('submit.prevent');

    expect(wrapper.text()).toContain('비밀번호를 입력해 주세요.');
    expect(mutateAsyncMock).not.toHaveBeenCalled();
  });

  it('이메일을 trim/lowercase 처리해서 로그인 요청을 보낸다', async () => {
    const { wrapper } = await mountLoginView();

    await wrapper.find('#username').setValue('  User.Name@ABMS.CO.KR  ');
    await wrapper.find('#password').setValue('password123!');
    await wrapper.find('form').trigger('submit.prevent');

    expect(mutateAsyncMock).toHaveBeenCalledWith({
      username: 'user.name@abms.co.kr',
      password: 'password123!',
    });
  });

  it('로그인 성공 시 /api/auth/me 결과를 저장하고 redirect 경로로 이동한다', async () => {
    const { wrapper, router } = await mountLoginView('/auths/login?redirect=/employees');

    await wrapper.find('#username').setValue('user@abms.co.kr');
    await wrapper.find('#password').setValue('password123!');
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();

    expect(setStoredUser).toHaveBeenCalledWith({
      email: 'user@abms.co.kr',
      name: '사용자',
    });
    expect(router.currentRoute.value.fullPath).toBe('/employees');
  });

  it('me 조회 실패 시 입력값 기반 사용자 정보로 저장한다', async () => {
    refetchMock.mockRejectedValueOnce(new Error('failed'));

    const { wrapper } = await mountLoginView();

    await wrapper.find('#username').setValue('john.doe@abms.co.kr');
    await wrapper.find('#password').setValue('password123!');
    await wrapper.find('form').trigger('submit.prevent');

    expect(setStoredUser).toHaveBeenCalledWith({
      email: 'john.doe@abms.co.kr',
      name: 'John Doe',
    });
  });

  it('redirect가 절대 경로가 아니면 홈으로 이동한다', async () => {
    const { wrapper, router } = await mountLoginView('/auths/login?redirect=https://evil.example');

    await wrapper.find('#username').setValue('user@abms.co.kr');
    await wrapper.find('#password').setValue('password123!');
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();

    expect(router.currentRoute.value.fullPath).toBe('/');
  });

  it('redirect가 auth 경로면 홈으로 이동한다', async () => {
    const { wrapper, router } = await mountLoginView('/auths/login?redirect=/auths/register');

    await wrapper.find('#username').setValue('user@abms.co.kr');
    await wrapper.find('#password').setValue('password123!');
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();

    expect(router.currentRoute.value.fullPath).toBe('/');
  });

  it('로그인 실패 시 사용자 정보를 비우고 에러를 표시한다', async () => {
    mutateAsyncMock.mockRejectedValueOnce(new HttpError({ message: '로그인 실패' }));

    const { wrapper } = await mountLoginView();

    await wrapper.find('#username').setValue('user@abms.co.kr');
    await wrapper.find('#password').setValue('wrong-password');
    await wrapper.find('form').trigger('submit.prevent');

    expect(clearStoredUser).toHaveBeenCalled();
    expect(wrapper.text()).toContain('로그인 실패');
    expect(toast.error).toHaveBeenCalledWith('로그인에 실패했습니다.', {
      description: '로그인 실패',
    });
  });

  it('로그인 요청 중에는 버튼 텍스트가 "로그인 중..."으로 바뀐다', async () => {
    let resolveLogin: () => void = () => {};
    mutateAsyncMock.mockImplementationOnce(
      () =>
        new Promise<void>((resolve) => {
          resolveLogin = resolve;
        }),
    );

    const { wrapper } = await mountLoginView();

    await wrapper.find('#username').setValue('user@abms.co.kr');
    await wrapper.find('#password').setValue('password123!');
    await wrapper.find('form').trigger('submit.prevent');

    expect(wrapper.text()).toContain('로그인 중...');

    resolveLogin();
    await Promise.resolve();
    await Promise.resolve();

    expect(wrapper.text()).toContain('로그인');
  });

  it('로딩 상태일 때 제출 버튼이 비활성화된다', async () => {
    mutateAsyncMock.mockImplementationOnce(() => new Promise(() => undefined));

    const { wrapper } = await mountLoginView();

    await wrapper.find('#username').setValue('user@abms.co.kr');
    await wrapper.find('#password').setValue('password123!');
    await wrapper.find('form').trigger('submit.prevent');

    const submitButton = wrapper.find('button[type="submit"]');
    expect(submitButton.attributes('disabled')).toBeDefined();
  });
});
