import { computed } from 'vue';
import type { Component } from 'vue';
import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test-utils';

const routerPushMock = vi.fn();
let PartyProjectsPanelComponent: Component;

const projectsData = {
  value: {
    content: [
      {
        projectId: 10,
        partyId: 1,
        partyName: '협력사A',
        leadDepartmentId: 100,
        leadDepartmentName: '플랫폼팀',
        code: 'P-010',
        name: '진행 프로젝트',
        description: null,
        status: 'IN_PROGRESS',
        statusLabel: '진행 중',
        contractAmount: 2000000,
        startDate: '2024-01-01',
        endDate: '2024-06-30',
      },
    ],
    totalPages: 1,
    totalElements: 1,
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

vi.mock('@/features/party/queries/usePartyQueries', () => ({
  usePartyProjectsQuery: () => ({
    data: computed(() => projectsData.value),
    isLoading: { value: false },
    error: { value: null },
  }),
}));

vi.mock('@/features/project/queries/useProjectQueries', () => ({
  useProjectStatusesQuery: () => ({
    data: { value: [{ value: 'IN_PROGRESS', label: '진행 중' }] },
  }),
}));

describe('PartyProjectsPanel', () => {
  beforeAll(async () => {
    PartyProjectsPanelComponent = (await import('@/features/party/components/PartyProjectsPanel.vue')).default;
  }, 30000);

  beforeEach(() => {
    vi.clearAllMocks();
    projectsData.value = {
      content: [
        {
          projectId: 10,
          partyId: 1,
          partyName: '협력사A',
          leadDepartmentId: 100,
          leadDepartmentName: '플랫폼팀',
          code: 'P-010',
          name: '진행 프로젝트',
          description: null,
          status: 'IN_PROGRESS',
          statusLabel: '진행 중',
          contractAmount: 2000000,
          startDate: '2024-01-01',
          endDate: '2024-06-30',
        },
      ],
      totalPages: 1,
      totalElements: 1,
    };
  });

  it('프로젝트 목록을 테이블로 렌더링하고 프로젝트/부서 링크를 이동시킨다', async () => {
    const { wrapper } = await renderWithProviders(PartyProjectsPanelComponent, {
      props: { partyId: 1 },
      route: '/parties/1?tab=projects',
      routes: [
        { path: '/parties/:partyId', name: 'party-detail', component: { template: '<div />' } },
        { path: '/projects/:projectId', name: 'project-detail', component: { template: '<div />' } },
        { path: '/departments/:departmentId', name: 'department', component: { template: '<div />' } },
      ],
    });

    expect(wrapper.text()).toContain('진행 프로젝트');
    expect(wrapper.text()).toContain('플랫폼팀');

    const projectButton = wrapper.findAll('button').find((button) => button.text() === '진행 프로젝트');
    const departmentButton = wrapper.findAll('button').find((button) => button.text() === '플랫폼팀');
    expect(projectButton).toBeDefined();
    expect(departmentButton).toBeDefined();

    await projectButton?.trigger('click');
    await departmentButton?.trigger('click');

    expect(routerPushMock).toHaveBeenCalledWith({ name: 'project-detail', params: { projectId: 10 } });
    expect(routerPushMock).toHaveBeenCalledWith({ name: 'department', params: { departmentId: 100 } });
  });
});
