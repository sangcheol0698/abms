import { computed, defineComponent, h } from 'vue';
import type { Component } from 'vue';
import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test-utils';

let storage: Record<string, string> = {};
let ProjectDetailViewComponent: Component;

vi.mock('@/features/project/queries/useProjectQueries', () => ({
  useProjectDetailQuery: () => ({
    data: computed(() => ({
      projectId: 1,
      partyId: 1,
      partyName: '협력사A',
      code: 'P-001',
      name: 'ABMS 리뉴얼',
      status: 'IN_PROGRESS',
      statusLabel: '진행 중',
      contractAmount: 1000000,
      leadDepartmentId: 10,
    })),
    isLoading: { value: false },
    error: { value: null },
  }),
  useDeleteProjectMutation: () => ({ mutateAsync: vi.fn(), isPending: { value: false } }),
}));

const PassThrough = defineComponent({
  setup(_, { slots }) {
    return () => h('div', slots.default?.());
  },
});

const ButtonStub = defineComponent({
  emits: ['click'],
  setup(_, { emit, slots }) {
    return () => h('button', { onClick: () => emit('click') }, slots.default?.());
  },
});

const ProjectDetailHeaderStub = defineComponent({
  setup(_, { slots }) {
    return () => h('div', slots.actions?.());
  },
});

async function mountProjectDetailView() {
  return renderWithProviders(ProjectDetailViewComponent, {
    route: '/projects/1',
    routes: [
      { path: '/projects/:projectId', name: 'project-detail', component: ProjectDetailViewComponent },
      { path: '/projects', name: 'projects', component: { template: '<div />' } },
      { path: '/parties/:partyId', name: 'party-detail', component: { template: '<div />' } },
      { path: '/departments/:departmentId', name: 'department', component: { template: '<div />' } },
    ],
    global: {
      stubs: {
        Alert: PassThrough,
        AlertDescription: PassThrough,
        AlertTitle: PassThrough,
        Button: ButtonStub,
        Tabs: PassThrough,
        TabsContent: PassThrough,
        TabsList: PassThrough,
        TabsTrigger: ButtonStub,
        AlertDialog: PassThrough,
        AlertDialogAction: ButtonStub,
        AlertDialogCancel: ButtonStub,
        AlertDialogContent: PassThrough,
        AlertDialogDescription: PassThrough,
        AlertDialogFooter: PassThrough,
        AlertDialogHeader: PassThrough,
        AlertDialogTitle: PassThrough,
        AlertDialogTrigger: PassThrough,
        ProjectDetailHeader: ProjectDetailHeaderStub,
        ProjectOverviewPanel: true,
        ProjectAssignmentPanel: true,
        ProjectRevenuePlanPanel: true,
        ProjectUpdateDialog: true,
        Pencil: true,
        Trash2: true,
      },
    },
  });
}

describe('ProjectDetailView', () => {
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
    ProjectDetailViewComponent = (await import('@/features/project/views/ProjectDetailView.vue')).default;
  });

  beforeEach(() => {
    storage = {};
  });

  it('project.write 권한이 있으면 편집/삭제 액션이 보인다', async () => {
    storage.user = JSON.stringify({
      name: '홍길동',
      email: 'hong@abms.co.kr',
      permissions: [
        { code: 'project.read', scopes: ['ALL'] },
        { code: 'project.write', scopes: ['ALL'] },
        { code: 'party.read', scopes: ['ALL'] },
      ],
    });
    localStorage.setItem('user', storage.user);

    const { wrapper } = await mountProjectDetailView();

    expect(wrapper.text()).toContain('삭제');
    expect(wrapper.text()).toContain('프로젝트 편집');
  });

  it('project.write 권한이 없으면 편집/삭제 액션이 보이지 않는다', async () => {
    storage.user = JSON.stringify({
      name: '홍길동',
      email: 'hong@abms.co.kr',
      permissions: [
        { code: 'project.read', scopes: ['ALL'] },
        { code: 'party.read', scopes: ['ALL'] },
      ],
    });
    localStorage.setItem('user', storage.user);

    const { wrapper } = await mountProjectDetailView();

    expect(wrapper.text()).not.toContain('삭제');
    expect(wrapper.text()).not.toContain('프로젝트 편집');
  });
});
