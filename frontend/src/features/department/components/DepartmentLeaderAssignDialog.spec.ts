import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { Component } from 'vue';
import { renderWithProviders } from '@/test-utils';
import type { EmployeeListItem } from '@/features/employee/models/employeeListItem';

const canManageEmployeeMock = vi.fn();

const employees: EmployeeListItem[] = [
  {
    employeeId: 1,
    departmentId: 10,
    departmentName: '개발팀',
    name: '허용 직원',
    email: 'allow@abacus.co.kr',
    positionCode: 'ASSOCIATE',
    positionLabel: '사원',
    statusCode: 'ACTIVE',
    statusLabel: '재직',
    gradeCode: 'JUNIOR',
    gradeLabel: '초급',
    typeCode: 'FULL_TIME',
    typeLabel: '정규직',
    avatarCode: 'SKY_GLOW',
    avatarLabel: 'Sky Glow',
    avatarImageUrl: '',
    joinDate: '2024-01-01',
  },
  {
    employeeId: 2,
    departmentId: 20,
    departmentName: '외부팀',
    name: '차단 직원',
    email: 'block@abacus.co.kr',
    positionCode: 'ASSOCIATE',
    positionLabel: '사원',
    statusCode: 'ACTIVE',
    statusLabel: '재직',
    gradeCode: 'JUNIOR',
    gradeLabel: '초급',
    typeCode: 'FULL_TIME',
    typeLabel: '정규직',
    avatarCode: 'SKY_GLOW',
    avatarLabel: 'Sky Glow',
    avatarImageUrl: '',
    joinDate: '2024-01-01',
  },
];

vi.mock('@vueuse/core', () => ({
  useDebounceFn: (fn: (query: string) => void) => fn,
}));

vi.mock('@/features/department/queries/useDepartmentQueries', () => ({
  useAssignDepartmentLeaderMutation: () => ({
    mutateAsync: vi.fn(),
    isPending: { value: false },
  }),
}));

vi.mock('@/features/employee/queries/useEmployeeQueries', () => ({
  useEmployeesQuery: () => ({
    data: { value: { content: employees } },
    isLoading: { value: false },
    isFetching: { value: false },
  }),
}));

vi.mock('@/features/employee/permissions', () => ({
  canManageEmployee: (...args: unknown[]) => canManageEmployeeMock(...args),
}));

vi.mock('vue-sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

async function mountDialog() {
  const component = (await import('@/features/department/components/DepartmentLeaderAssignDialog.vue')).default as Component;
  return renderWithProviders(component, {
    props: {
      open: true,
      departmentId: 10,
      departmentName: '개발팀',
      departmentChart: [
        {
          departmentId: 10,
          departmentName: '개발팀',
          departmentCode: 'DEV',
          departmentType: 'TEAM',
          departmentLeader: null,
          employeeCount: 2,
          children: [],
        },
      ],
    },
    global: {
      stubs: {
        Dialog: { template: '<div><slot /></div>' },
        DialogContent: { template: '<div><slot /></div>' },
        DialogDescription: { template: '<div><slot /></div>' },
        DialogFooter: { template: '<div><slot /></div>' },
        DialogHeader: { template: '<div><slot /></div>' },
        DialogTitle: { template: '<div><slot /></div>' },
        Command: { template: '<div><slot /></div>' },
        CommandEmpty: { template: '<div><slot /></div>' },
        CommandGroup: { template: '<div><slot /></div>' },
        CommandItem: { template: '<div><slot /></div>' },
        CommandList: { template: '<div><slot /></div>' },
        Button: { template: '<button type="button"><slot /></button>' },
        Avatar: { template: '<div><slot /></div>' },
        AvatarFallback: { template: '<div><slot /></div>' },
        AvatarImage: { template: '<img src="" alt="" />' },
        Badge: { template: '<span><slot /></span>' },
        Check: { template: '<span />' },
        Loader2: { template: '<span />' },
        Search: { template: '<span />' },
      },
    },
  });
}

describe('DepartmentLeaderAssignDialog', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('검색 결과를 employee.write scope 범위로 필터링한다', async () => {
    canManageEmployeeMock.mockImplementation((target: { departmentId?: number }) => target.departmentId === 10);

    const { wrapper } = await mountDialog();

    await wrapper.find('input').setValue('직원');

    expect(wrapper.text()).toContain('허용 직원');
    expect(wrapper.text()).not.toContain('차단 직원');
  });

  it('권한 범위에 맞는 직원이 없으면 빈 결과 문구를 표시한다', async () => {
    canManageEmployeeMock.mockReturnValue(false);

    const { wrapper } = await mountDialog();

    await wrapper.find('input').setValue('직원');

    expect(wrapper.text()).toContain('검색 결과가 없습니다.');
  });
});

