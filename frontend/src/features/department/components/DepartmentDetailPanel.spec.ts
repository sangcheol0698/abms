import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import type { Component } from 'vue';
import type { RouteRecordRaw } from 'vue-router';
import { renderWithProviders } from '@/test-utils';
import type { DepartmentSummary } from '@/features/department/models/department';

const canViewEmployeeDetailMock = vi.fn();
const canManageEmployeeMock = vi.fn();
const routerPushMock = vi.fn();

vi.mock('@/core/composables/useQuerySync', () => ({
  useQuerySync: vi.fn(),
}));

vi.mock('@/features/employee/permissions', () => ({
  canViewEmployeeDetail: (...args: unknown[]) => canViewEmployeeDetailMock(...args),
  canManageEmployee: (...args: unknown[]) => canManageEmployeeMock(...args),
}));

vi.mock('vue-router', async () => {
  const actual = await vi.importActual<typeof import('vue-router')>('vue-router');
  return {
    ...actual,
    useRouter: () => ({
      push: routerPushMock,
    }),
  };
});

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'home', component: { template: '<div>home</div>' } },
  { path: '/employees/:employeeId', name: 'employee-detail', component: { template: '<div>employee</div>' } },
];

const department: DepartmentSummary = {
  departmentId: 10,
  departmentName: '개발팀',
  departmentCode: 'DEV',
  departmentType: 'TEAM',
  departmentLeader: {
    employeeId: 3,
    employeeName: '리더',
    position: '팀장',
    avatarCode: 'SKY_GLOW',
    avatarLabel: 'Sky Glow',
    avatarImageUrl: '',
  },
  employees: [],
  employeeCount: 5,
  childDepartmentCount: 1,
};

let DepartmentDetailPanelComponent: Component;

async function mountPanel() {
  return renderWithProviders(DepartmentDetailPanelComponent, {
    route: '/',
    routes,
    props: {
      department,
      departmentChart: [
        {
          departmentId: 10,
          departmentName: '개발팀',
          departmentCode: 'DEV',
          departmentType: 'TEAM',
          departmentLeader: null,
          employeeCount: 5,
          children: [],
        },
      ],
    },
    global: {
      stubs: {
        DepartmentEmployeeList: { template: '<div />' },
        DepartmentLeaderAssignDialog: { template: '<div data-test="leader-assign-dialog" />' },
        Tabs: { template: '<div><slot /></div>' },
        TabsList: { template: '<div><slot /></div>' },
        TabsTrigger: { template: '<button type="button"><slot /></button>' },
        TabsContent: { template: '<div><slot /></div>' },
        Badge: { template: '<span><slot /></span>' },
        Button: { template: '<button type="button"><slot /></button>' },
        Separator: { template: '<hr />' },
        GitBranch: { template: '<span />' },
        UserRound: { template: '<span />' },
        Users: { template: '<span />' },
        Pencil: { template: '<span />' },
      },
    },
  });
}

describe('DepartmentDetailPanel', () => {
  beforeAll(async () => {
    DepartmentDetailPanelComponent = (await import('@/features/department/components/DepartmentDetailPanel.vue')).default as Component;
  }, 30000);

  beforeEach(() => {
    vi.clearAllMocks();
    canManageEmployeeMock.mockReturnValue(false);
  });

  it('직원 상세 조회 권한이 있으면 부서 리더를 링크로 표시하고 상세로 이동한다', async () => {
    canViewEmployeeDetailMock.mockReturnValue(true);

    const { wrapper } = await mountPanel();

    const link = wrapper.find('[data-test="leader-employee-link"]');
    expect(link.exists()).toBe(true);

    await link.trigger('click');

    expect(routerPushMock).toHaveBeenCalledWith({
      name: 'employee-detail',
      params: { employeeId: '3' },
    });
  });

  it('직원 상세 조회 권한이 없으면 부서 리더를 텍스트로 표시하고 링크를 숨긴다', async () => {
    canViewEmployeeDetailMock.mockReturnValue(false);

    const { wrapper } = await mountPanel();

    expect(wrapper.find('[data-test="leader-employee-link"]').exists()).toBe(false);
    expect(wrapper.find('[data-test="leader-employee-text"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('리더');
  });

  it('리더 변경 권한이 있으면 변경 버튼과 임명 다이얼로그를 노출한다', async () => {
    canViewEmployeeDetailMock.mockReturnValue(true);
    canManageEmployeeMock.mockReturnValue(true);

    const { wrapper } = await mountPanel();

    expect(wrapper.findAll('button').some((button) => button.text().includes('변경'))).toBe(true);
    expect(wrapper.find('[data-test="leader-assign-dialog"]').exists()).toBe(true);
  });

  it('리더 변경 권한이 없으면 변경 버튼과 임명 다이얼로그를 숨긴다', async () => {
    canViewEmployeeDetailMock.mockReturnValue(true);
    canManageEmployeeMock.mockReturnValue(false);

    const { wrapper } = await mountPanel();

    expect(wrapper.findAll('button').some((button) => button.text().includes('변경'))).toBe(false);
    expect(wrapper.find('[data-test="leader-assign-dialog"]').exists()).toBe(false);
  });
});
