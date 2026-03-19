import { computed } from 'vue';
import type { Component } from 'vue';
import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test-utils';

const routerPushMock = vi.fn();
let storage: Record<string, string> = {};
const projectsData = {
  value: {
    content: [
      {
        projectId: 1,
        projectCode: 'PRJ-1',
        projectName: 'ABMS 구축',
        partyId: 3,
        role: '개발자',
        assignmentStartDate: '2026-01-01',
        assignmentEndDate: null,
        assignmentStatus: 'CURRENT' as const,
        projectStatus: 'IN_PROGRESS',
        projectStatusLabel: '진행 중',
        leadDepartmentId: 10,
        leadDepartmentName: '개발팀',
        partyName: '아바쿠스',
      },
    ],
    page: 1,
    size: 10,
    totalPages: 1,
    totalElements: 1,
  },
};
let EmployeeProjectsPanelComponent: Component;

vi.mock('vue-router', async () => {
  const actual = await vi.importActual<typeof import('vue-router')>('vue-router');
  return {
    ...actual,
    useRouter: () => ({
      push: routerPushMock,
    }),
  };
});

vi.mock('@/features/employee/queries/useEmployeeQueries', () => ({
  useEmployeeProjectsQuery: () => ({
    data: computed(() => projectsData.value),
    isLoading: { value: false },
    error: { value: null },
  }),
}));

vi.mock('@/features/party/permissions', () => ({
  canReadParties: () => true,
}));

describe('EmployeeProjectsPanel', () => {
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
    EmployeeProjectsPanelComponent = (await import('@/features/employee/components/EmployeeProjectsPanel.vue')).default;
  }, 20000);

  beforeEach(() => {
    vi.clearAllMocks();
    storage = {
      user: JSON.stringify({
        permissions: [{ code: 'party.read', scopes: ['ALL'] }],
      }),
    };
    projectsData.value = {
      content: [
        {
          projectId: 1,
          projectCode: 'PRJ-1',
          projectName: 'ABMS 구축',
          partyId: 3,
          role: '개발자',
          assignmentStartDate: '2026-01-01',
          assignmentEndDate: null,
          assignmentStatus: 'CURRENT',
          projectStatus: 'IN_PROGRESS',
          projectStatusLabel: '진행 중',
          leadDepartmentId: 10,
          leadDepartmentName: '개발팀',
          partyName: '아바쿠스',
        },
      ],
      page: 1,
      size: 10,
      totalPages: 1,
      totalElements: 1,
    };
  });

  it('프로젝트 목록을 테이블로 렌더링하고 상세로 이동할 수 있다', async () => {
    const { wrapper } = await renderWithProviders(EmployeeProjectsPanelComponent, {
      props: { employeeId: 1 },
      route: '/employees/1?tab=projects',
      routes: [
        { path: '/employees/:employeeId', name: 'employee-detail', component: { template: '<div />' } },
        { path: '/projects/:projectId', name: 'project-detail', component: { template: '<div />' } },
        { path: '/departments/:departmentId', name: 'department', component: { template: '<div />' } },
        { path: '/parties/:partyId', name: 'party-detail', component: { template: '<div />' } },
      ],
    });

    expect(wrapper.text()).toContain('ABMS 구축');
    expect(wrapper.text()).toContain('현재 참여');

    const projectLink = wrapper.findAll('button').find((button) => button.text().includes('ABMS 구축'));
    await projectLink?.trigger('click');

    expect(routerPushMock).toHaveBeenCalledWith('/projects/1');

    const departmentLink = wrapper.findAll('button').find((button) => button.text().includes('개발팀'));
    const partyLink = wrapper.findAll('button').find((button) => button.text().includes('아바쿠스'));

    await departmentLink?.trigger('click');
    await partyLink?.trigger('click');

    expect(routerPushMock).toHaveBeenCalledWith({ path: '/departments/10' });
    expect(routerPushMock).toHaveBeenCalledWith({ path: '/parties/3' });
  }, 10000);

  it('데이터가 없으면 빈 상태를 표시한다', async () => {
    projectsData.value = {
      content: [],
      page: 1,
      size: 10,
      totalPages: 0,
      totalElements: 0,
    };
    const { wrapper } = await renderWithProviders(EmployeeProjectsPanelComponent, {
      props: { employeeId: 1 },
      route: '/employees/1?tab=projects',
      routes: [
        { path: '/employees/:employeeId', name: 'employee-detail', component: { template: '<div />' } },
      ],
    });

    expect(wrapper.text()).toContain('참여한 프로젝트가 없습니다.');
  }, 10000);
});
