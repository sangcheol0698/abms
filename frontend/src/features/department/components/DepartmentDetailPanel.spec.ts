import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { Component } from 'vue';
import { flushPromises } from '@vue/test-utils';
import type { RouteRecordRaw } from 'vue-router';
import { renderWithProviders } from '@/test-utils';
import type { DepartmentSummary } from '@/features/department/models/department';

const canViewEmployeeDetailMock = vi.fn();

vi.mock('@/core/composables/useQuerySync', () => ({
  useQuerySync: vi.fn(),
}));

vi.mock('@/features/employee/permissions', () => ({
  canViewEmployeeDetail: (...args: unknown[]) => canViewEmployeeDetailMock(...args),
}));

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

async function mountPanel() {
  const component = (await import('@/features/department/components/DepartmentDetailPanel.vue')).default as Component;
  return renderWithProviders(component, {
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
        DepartmentLeaderAssignDialog: { template: '<div />' },
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
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('직원 상세 조회 권한이 있으면 부서 리더를 링크로 표시하고 상세로 이동한다', async () => {
    canViewEmployeeDetailMock.mockReturnValue(true);

    const { wrapper, router } = await mountPanel();

    const link = wrapper.find('[data-test="leader-employee-link"]');
    expect(link.exists()).toBe(true);

    await link.trigger('click');
    await flushPromises();

    expect(router.currentRoute.value.name).toBe('employee-detail');
    expect(router.currentRoute.value.params.employeeId).toBe('3');
  });

  it('직원 상세 조회 권한이 없으면 부서 리더를 텍스트로 표시하고 링크를 숨긴다', async () => {
    canViewEmployeeDetailMock.mockReturnValue(false);

    const { wrapper } = await mountPanel();

    expect(wrapper.find('[data-test="leader-employee-link"]').exists()).toBe(false);
    expect(wrapper.find('[data-test="leader-employee-text"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('리더');
  });
});

