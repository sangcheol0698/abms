import type { Component } from 'vue';
import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises } from '@vue/test-utils';
import { type RouteRecordRaw } from 'vue-router';
import HttpError from '@/core/http/HttpError';
import { renderWithProviders } from '@/test-utils';
import { toast } from 'vue-sonner';

const mutateAsyncMock = vi.fn();
const logoutMutateAsyncMock = vi.fn();
const updateEmployeeMutateAsyncMock = vi.fn();
let storage: Record<string, string> = {};
let ProfileDialogComponent: Component;

vi.mock('@/features/auth/queries/useAuthQueries', () => ({
  useChangePasswordMutation: () => ({
    mutateAsync: mutateAsyncMock,
  }),
  useLogoutMutation: () => ({
    mutateAsync: logoutMutateAsyncMock,
  }),
}));

vi.mock('@/features/employee/queries/useEmployeeQueries', () => ({
  useCurrentEmployeeProfileQuery: () => ({
    data: {
      value: {
        employeeId: 1,
        departmentId: 10,
        departmentName: '개발팀',
        employeeName: '테스터',
        name: '테스터',
        email: 'tester@iabacus.co.kr',
        position: '사원',
        positionCode: 'ASSOCIATE',
        status: '재직',
        statusCode: 'ACTIVE',
        grade: '초급',
        gradeCode: 'JUNIOR',
        type: '정규직',
        typeCode: 'FULL_TIME',
        avatarCode: 'SKY_GLOW',
        avatarLabel: 'Sky Glow',
        avatarImageUrl: '',
        memo: '',
        joinDate: '2024-01-01',
        birthDate: '1990-01-01',
      },
    },
    isLoading: { value: false },
    isFetching: { value: false },
  }),
  useEmployeeAvatarsQuery: () => ({
    data: {
      value: [
        {
          code: 'SKY_GLOW',
          label: 'Sky Glow',
          imageUrl: '',
        },
      ],
    },
  }),
  useUpdateEmployeeMutation: () => ({
    mutateAsync: updateEmployeeMutateAsyncMock,
    isPending: { value: false },
  }),
}));

vi.mock('vue-sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'home', component: { template: '<div>home</div>' } },
  { path: '/auths/login', name: 'auth-login', component: { template: '<div>login</div>' } },
];

async function mountProfileDialog() {
  return renderWithProviders(ProfileDialogComponent, {
    routes,
    route: '/',
    props: {
      open: true,
      user: {
        name: '테스터',
        email: 'tester@iabacus.co.kr',
        avatar: '',
      },
    },
    global: {
      stubs: {
        Dialog: { template: '<div><slot /></div>' },
        DialogContent: { template: '<div><slot /></div>' },
        DialogDescription: { template: '<p><slot /></p>' },
        DialogFooter: { template: '<div><slot /></div>' },
        DialogHeader: { template: '<div><slot /></div>' },
        DialogTitle: { template: '<h2><slot /></h2>' },
        SidebarProvider: { template: '<div><slot /></div>' },
        Sidebar: { template: '<aside><slot /></aside>' },
        SidebarContent: { template: '<div><slot /></div>' },
        SidebarGroup: { template: '<div><slot /></div>' },
        SidebarGroupContent: { template: '<div><slot /></div>' },
        SidebarMenu: { template: '<div><slot /></div>' },
        SidebarMenuItem: { template: '<div><slot /></div>' },
        SidebarMenuButton: { template: '<button type="button"><slot /></button>' },
        Breadcrumb: { template: '<nav><slot /></nav>' },
        BreadcrumbList: { template: '<div><slot /></div>' },
        BreadcrumbItem: { template: '<span><slot /></span>' },
        BreadcrumbPage: { template: '<span><slot /></span>' },
        BreadcrumbSeparator: { template: '<span>/</span>' },
        ToggleGroup: { template: '<div><slot /></div>' },
        ToggleGroupItem: { template: '<button type="button"><slot /></button>' },
        AlertDialog: { template: '<div><slot /></div>' },
        AlertDialogContent: { template: '<div><slot /></div>' },
        AlertDialogDescription: { template: '<div><slot /></div>' },
        AlertDialogFooter: { template: '<div><slot /></div>' },
        AlertDialogHeader: { template: '<div><slot /></div>' },
        AlertDialogTitle: { template: '<div><slot /></div>' },
        AlertDialogAction: { template: '<button type="button"><slot /></button>' },
        AlertDialogCancel: { template: '<button type="button"><slot /></button>' },
        Avatar: { template: '<div><slot /></div>' },
        AvatarFallback: { template: '<div><slot /></div>' },
        AvatarImage: { template: '<img />' },
        DatePicker: { template: '<div data-test="date-picker" />' },
        EmployeeAvatarSelectDialog: { template: '<div data-test="avatar-select-dialog" />' },
      },
    },
  });
}

async function switchToSecuritySection(wrapper: Awaited<ReturnType<typeof mountProfileDialog>>['wrapper']) {
  const securityButton = wrapper
    .findAll('button')
    .find((candidate) => candidate.text().includes('보안'));

  expect(securityButton).toBeDefined();
  await securityButton!.trigger('click');
}

async function switchSection(
  wrapper: Awaited<ReturnType<typeof mountProfileDialog>>['wrapper'],
  label: string,
) {
  const button = wrapper.findAll('button').find((candidate) => candidate.text().includes(label));

  expect(button).toBeDefined();
  await button!.trigger('click');
}

describe('ProfileDialog', () => {
  beforeAll(async () => {
    vi.stubGlobal('localStorage', {
      getItem: vi.fn((key: string) => storage[key] ?? null),
      setItem: vi.fn((key: string, value: string) => {
        storage[key] = String(value);
      }),
      removeItem: vi.fn((key: string) => {
        delete storage[key];
      }),
      clear: vi.fn(() => {
        storage = {};
      }),
    });
    ProfileDialogComponent = (await import('@/components/business/ProfileDialog.vue')).default;
  });

  beforeEach(() => {
    vi.clearAllMocks();
    mutateAsyncMock.mockResolvedValue(undefined);
    logoutMutateAsyncMock.mockResolvedValue(undefined);
    updateEmployeeMutateAsyncMock.mockResolvedValue(undefined);
    storage = {};
  });

  it('현재 비밀번호가 비어 있으면 검증 메시지를 보여준다', async () => {
    const { wrapper } = await mountProfileDialog();
    await switchToSecuritySection(wrapper);

    await wrapper.find('#newPassword').setValue('NewPassword123!');
    await wrapper.find('#newPasswordConfirm').setValue('NewPassword123!');
    await wrapper.find('form').trigger('submit.prevent');

    expect(wrapper.text()).toContain('현재 비밀번호를 입력해 주세요.');
    expect(mutateAsyncMock).not.toHaveBeenCalled();
  });

  it('기본 렌더 시 계정 정보 섹션이 활성화된다', async () => {
    const { wrapper } = await mountProfileDialog();

    expect(wrapper.find('[data-test="profile-section"]').exists()).toBe(true);
    expect(wrapper.find('#currentPassword').exists()).toBe(false);
  });

  it('계정 정보 메뉴를 누르면 읽기 전용 계정 정보가 표시된다', async () => {
    const { wrapper } = await mountProfileDialog();
    await switchSection(wrapper, '계정 정보');

    expect(wrapper.find('[data-test="profile-section"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('tester@iabacus.co.kr');
  });

  it('employee.write 권한이 있으면 내 정보 수정 섹션이 표시된다', async () => {
    storage.user = JSON.stringify({
      name: '테스터',
      email: 'tester@iabacus.co.kr',
      permissions: [{ code: 'employee.write', scopes: ['ALL'] }],
    });
    localStorage.setItem('user', storage.user);

    const { wrapper } = await mountProfileDialog();

    expect(wrapper.find('[data-test="self-profile-card"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('내 정보 수정');
  });

  it('openSelfProfileEditor가 true면 계정 정보 탭에서 내 정보 수정 폼이 바로 보인다', async () => {
    storage.user = JSON.stringify({
      name: '테스터',
      email: 'tester@iabacus.co.kr',
      permissions: [{ code: 'employee.write', scopes: ['ALL'] }],
    });
    localStorage.setItem('user', storage.user);

    const { wrapper } = await renderWithProviders(ProfileDialogComponent, {
      routes,
      route: '/',
      props: {
        open: true,
        openSelfProfileEditor: true,
        user: {
          name: '테스터',
          email: 'tester@iabacus.co.kr',
          avatar: '',
        },
      },
      global: {
        stubs: {
          Dialog: { template: '<div><slot /></div>' },
          DialogContent: { template: '<div><slot /></div>' },
          DialogDescription: { template: '<p><slot /></p>' },
          DialogFooter: { template: '<div><slot /></div>' },
          DialogHeader: { template: '<div><slot /></div>' },
          DialogTitle: { template: '<h2><slot /></h2>' },
          SidebarProvider: { template: '<div><slot /></div>' },
          Sidebar: { template: '<aside><slot /></aside>' },
          SidebarContent: { template: '<div><slot /></div>' },
          SidebarGroup: { template: '<div><slot /></div>' },
          SidebarGroupContent: { template: '<div><slot /></div>' },
          SidebarMenu: { template: '<div><slot /></div>' },
          SidebarMenuItem: { template: '<div><slot /></div>' },
          SidebarMenuButton: { template: '<button type="button"><slot /></button>' },
          Breadcrumb: { template: '<nav><slot /></nav>' },
          BreadcrumbList: { template: '<div><slot /></div>' },
          BreadcrumbItem: { template: '<span><slot /></span>' },
          BreadcrumbPage: { template: '<span><slot /></span>' },
          BreadcrumbSeparator: { template: '<span>/</span>' },
          ToggleGroup: { template: '<div><slot /></div>' },
          ToggleGroupItem: { template: '<button type="button"><slot /></button>' },
          AlertDialog: { template: '<div><slot /></div>' },
          AlertDialogContent: { template: '<div><slot /></div>' },
          AlertDialogDescription: { template: '<div><slot /></div>' },
          AlertDialogFooter: { template: '<div><slot /></div>' },
          AlertDialogHeader: { template: '<div><slot /></div>' },
          AlertDialogTitle: { template: '<div><slot /></div>' },
          AlertDialogAction: { template: '<button type="button"><slot /></button>' },
          AlertDialogCancel: { template: '<button type="button"><slot /></button>' },
          Avatar: { template: '<div><slot /></div>' },
          AvatarFallback: { template: '<div><slot /></div>' },
          AvatarImage: { template: '<img />' },
          DatePicker: { template: '<div data-test="date-picker" />' },
          EmployeeAvatarSelectDialog: { template: '<div data-test="avatar-select-dialog" />' },
        },
      },
    });

    expect(wrapper.find('[data-test="profile-section"]').exists()).toBe(true);
    expect(wrapper.find('[data-test="self-profile-inline-form"]').exists()).toBe(true);
  });

  it('알림 설정 메뉴를 누르면 정보 섹션이 표시된다', async () => {
    const { wrapper } = await mountProfileDialog();
    await switchSection(wrapper, '알림 설정');

    expect(wrapper.find('[data-test="notifications-section"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('현재 알림 채널');
    expect(wrapper.text()).toContain('메일/푸시 알림');
  });

  it('환경 설정 메뉴를 누르면 정보 섹션이 표시된다', async () => {
    const { wrapper } = await mountProfileDialog();
    await switchSection(wrapper, '환경 설정');

    expect(wrapper.find('[data-test="preferences-section"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('테마');
    expect(wrapper.text()).toContain('표시 환경');
  });

  it('새 비밀번호 확인이 다르면 검증 메시지를 보여준다', async () => {
    const { wrapper } = await mountProfileDialog();
    await switchToSecuritySection(wrapper);

    await wrapper.find('#currentPassword').setValue('CurrentPassword123!');
    await wrapper.find('#newPassword').setValue('NewPassword123!');
    await wrapper.find('#newPasswordConfirm').setValue('AnotherPassword123!');
    await wrapper.find('form').trigger('submit.prevent');

    expect(wrapper.text()).toContain('새 비밀번호가 일치하지 않습니다.');
    expect(mutateAsyncMock).not.toHaveBeenCalled();
  });

  it('비밀번호 변경 성공 시 토스트를 표시하고 입력값을 초기화한다', async () => {
    const { wrapper } = await mountProfileDialog();
    await switchToSecuritySection(wrapper);

    await wrapper.find('#currentPassword').setValue('CurrentPassword123!');
    await wrapper.find('#newPassword').setValue('NewPassword123!');
    await wrapper.find('#newPasswordConfirm').setValue('NewPassword123!');
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();

    expect(mutateAsyncMock).toHaveBeenCalledWith({
      currentPassword: 'CurrentPassword123!',
      newPassword: 'NewPassword123!',
    });
    expect(toast.success).toHaveBeenCalledWith('비밀번호를 변경했습니다.');
    expect((wrapper.find('#currentPassword').element as HTMLInputElement).value).toBe('');
    expect((wrapper.find('#newPassword').element as HTMLInputElement).value).toBe('');
    expect((wrapper.find('#newPasswordConfirm').element as HTMLInputElement).value).toBe('');
  });

  it('비밀번호 변경 실패 시 서버 메시지를 노출한다', async () => {
    mutateAsyncMock.mockRejectedValueOnce(new HttpError({ message: '현재 비밀번호가 일치하지 않습니다.' }));
    const { wrapper } = await mountProfileDialog();
    await switchToSecuritySection(wrapper);

    await wrapper.find('#currentPassword').setValue('WrongPassword123!');
    await wrapper.find('#newPassword').setValue('NewPassword123!');
    await wrapper.find('#newPasswordConfirm').setValue('NewPassword123!');
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();

    expect(wrapper.text()).toContain('현재 비밀번호가 일치하지 않습니다.');
    expect(toast.error).toHaveBeenCalledWith('비밀번호 변경에 실패했습니다.', {
      description: '현재 비밀번호가 일치하지 않습니다.',
    });
  });

  it('다시 열면 마지막 섹션은 유지되고 비밀번호 입력값은 초기화된다', async () => {
    const { wrapper } = await mountProfileDialog();
    await switchToSecuritySection(wrapper);

    await wrapper.find('#currentPassword').setValue('CurrentPassword123!');
    await wrapper.find('#newPassword').setValue('NewPassword123!');
    await wrapper.find('#newPasswordConfirm').setValue('NewPassword123!');

    await wrapper.setProps({ open: false });
    await wrapper.setProps({ open: true });
    await flushPromises();

    expect(wrapper.find('[data-test="security-section"]').exists()).toBe(true);
    expect((wrapper.find('#currentPassword').element as HTMLInputElement).value).toBe('');
    expect((wrapper.find('#newPassword').element as HTMLInputElement).value).toBe('');
    expect((wrapper.find('#newPasswordConfirm').element as HTMLInputElement).value).toBe('');
  });
});
