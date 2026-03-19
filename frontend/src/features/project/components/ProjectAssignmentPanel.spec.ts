import { computed } from 'vue';
import type { Component } from 'vue';
import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test-utils';

const routerPushMock = vi.fn();
let ProjectAssignmentPanelComponent: Component;

const assignmentsData = {
  value: {
    content: [
      {
        id: 1,
        projectId: 7,
        employeeId: 10,
        employeeName: '김개발',
        departmentId: 100,
        departmentName: '백엔드팀',
        role: 'DEV',
        roleLabel: '개발자',
        startDate: '2026-03-20',
        endDate: null,
        assignmentStatus: 'SCHEDULED',
        assignmentStatusLabel: '예정',
      },
      {
        id: 2,
        projectId: 7,
        employeeId: 11,
        employeeName: '박PM',
        departmentId: 101,
        departmentName: '기획팀',
        role: 'PM',
        roleLabel: 'PM',
        startDate: '2026-03-01',
        endDate: '2026-03-31',
        assignmentStatus: 'CURRENT',
        assignmentStatusLabel: '현재 투입',
      },
    ],
    totalPages: 1,
    totalElements: 2,
  },
};

vi.mock('vue-router', async () => {
  const actual = await vi.importActual<typeof import('vue-router')>('vue-router');
  return {
    ...actual,
    useRouter: () => ({
      push: routerPushMock,
    }),
  };
});

vi.mock('@/features/project/queries/useProjectQueries', () => ({
  useProjectAssignmentsQuery: () => ({
    data: computed(() => assignmentsData.value),
    isLoading: { value: false },
    error: { value: null },
  }),
}));

vi.mock('@/features/project/permissions', () => ({
  canManageProjects: () => true,
}));

vi.mock('@/features/employee/permissions', () => ({
  canReadEmployeeDetail: () => true,
}));

describe('ProjectAssignmentPanel', () => {
  beforeAll(async () => {
    ProjectAssignmentPanelComponent = (await import('@/features/project/components/ProjectAssignmentPanel.vue')).default;
  }, 60000);

  beforeEach(() => {
    vi.clearAllMocks();
    assignmentsData.value = {
      content: [
        {
          id: 1,
          projectId: 7,
          employeeId: 10,
          employeeName: '김개발',
          departmentId: 100,
          departmentName: '백엔드팀',
          role: 'DEV',
          roleLabel: '개발자',
          startDate: '2026-03-20',
          endDate: null,
          assignmentStatus: 'SCHEDULED',
          assignmentStatusLabel: '예정',
        },
        {
          id: 2,
          projectId: 7,
          employeeId: 11,
          employeeName: '박PM',
          departmentId: 101,
          departmentName: '기획팀',
          role: 'PM',
          roleLabel: 'PM',
          startDate: '2026-03-01',
          endDate: '2026-03-31',
          assignmentStatus: 'CURRENT',
          assignmentStatusLabel: '현재 투입',
        },
      ],
      totalPages: 1,
      totalElements: 2,
    };
  });

  it('투입 이력을 테이블로 렌더링하고 직원/부서 링크를 이동시킨다', async () => {
    const { wrapper } = await renderWithProviders(ProjectAssignmentPanelComponent, {
      props: {
        project: {
          projectId: 7,
          partyId: 1,
          partyName: '협력사A',
          code: 'P-007',
          name: '테스트 프로젝트',
          description: null,
          status: 'IN_PROGRESS',
          statusLabel: '진행 중',
          contractAmount: 1000,
          startDate: '2026-03-01',
          endDate: '2026-12-31',
          leadDepartmentId: 100,
          leadDepartmentName: '백엔드팀',
        },
      },
      route: '/projects/7',
      routes: [
        { path: '/projects/:projectId', name: 'project-detail', component: { template: '<div />' } },
        { path: '/employees/:employeeId', name: 'employee-detail', component: { template: '<div />' } },
        { path: '/departments/:departmentId', name: 'department', component: { template: '<div />' } },
      ],
      global: {
        stubs: {
          ProjectAssignmentFormDialog: {
            props: ['open'],
            template: '<div data-test="assignment-form-open">{{ String(open) }}</div>',
          },
          ProjectAssignmentEndDialog: {
            props: ['open'],
            template: '<div data-test="assignment-end-open">{{ String(open) }}</div>',
          },
        },
      },
    });

    expect(wrapper.text()).toContain('김개발');
    expect(wrapper.text()).toContain('예정');
    expect(wrapper.text()).toContain('현재 투입');

    const employeeLink = wrapper.findAll('button').find((button) => button.text() === '김개발');
    const departmentLink = wrapper.findAll('button').find((button) => button.text() === '백엔드팀');
    expect(employeeLink).toBeDefined();
    expect(departmentLink).toBeDefined();

    await employeeLink?.trigger('click');
    await departmentLink?.trigger('click');

    expect(routerPushMock).toHaveBeenCalledWith({ name: 'employee-detail', params: { employeeId: 10 } });
    expect(routerPushMock).toHaveBeenCalledWith({ name: 'department', params: { departmentId: 100 } });
  });

  it('추가 버튼을 누르면 폼 다이얼로그를 연다', async () => {
    const { wrapper } = await renderWithProviders(ProjectAssignmentPanelComponent, {
      props: {
        project: {
          projectId: 7,
          partyId: 1,
          partyName: '협력사A',
          code: 'P-007',
          name: '테스트 프로젝트',
          description: null,
          status: 'IN_PROGRESS',
          statusLabel: '진행 중',
          contractAmount: 1000,
          startDate: '2026-03-01',
          endDate: '2026-12-31',
          leadDepartmentId: 100,
          leadDepartmentName: '백엔드팀',
        },
      },
      route: '/projects/7',
      routes: [{ path: '/projects/:projectId', name: 'project-detail', component: { template: '<div />' } }],
      global: {
        stubs: {
          ProjectAssignmentFormDialog: {
            props: ['open'],
            template: '<div data-test="assignment-form-open">{{ String(open) }}</div>',
          },
          ProjectAssignmentEndDialog: {
            props: ['open'],
            template: '<div data-test="assignment-end-open">{{ String(open) }}</div>',
          },
        },
      },
    });

    const addButton = wrapper.findAll('button').find((button) => button.text().includes('투입 인력 추가'));
    expect(addButton).toBeDefined();
    expect(wrapper.get('[data-test="assignment-form-open"]').text()).toBe('false');

    await addButton?.trigger('click');

    expect(wrapper.get('[data-test="assignment-form-open"]').text()).toBe('true');
  });
});
