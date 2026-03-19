import { computed, defineComponent, h } from 'vue';
import type { Component } from 'vue';
import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test-utils';

let storage: Record<string, string> = {};
let PartyDetailViewComponent: Component;

vi.mock('@/features/party/queries/usePartyQueries', () => ({
  usePartyDetailQuery: () => ({
    data: computed(() => ({ partyId: 1, name: '협력사A' })),
    isLoading: { value: false },
    error: { value: null },
  }),
  useDeletePartyMutation: () => ({ mutateAsync: vi.fn(), isPending: { value: false } }),
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

const TabsStub = defineComponent({
  props: {
    modelValue: {
      type: String,
      default: 'overview',
    },
  },
  emits: ['update:modelValue'],
  setup(props, { slots }) {
    return () => h('div', { 'data-test': 'active-tab' }, [props.modelValue, slots.default?.()]);
  },
});

const PartyDetailHeaderStub = defineComponent({
  props: {
    canManage: {
      type: Boolean,
      default: true,
    },
  },
  setup(props) {
    return () => h('div', props.canManage ? '협력사 편집삭제' : 'readonly');
  },
});

async function mountPartyDetailView() {
  return renderWithProviders(PartyDetailViewComponent, {
    route: '/parties/1',
    routes: [
      { path: '/parties/:partyId', name: 'party-detail', component: PartyDetailViewComponent },
      { path: '/parties', name: 'parties', component: { template: '<div />' } },
    ],
    global: {
      stubs: {
        Alert: PassThrough,
        AlertDescription: PassThrough,
        AlertTitle: PassThrough,
        Tabs: TabsStub,
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
        DropdownMenu: PassThrough,
        DropdownMenuContent: PassThrough,
        DropdownMenuItem: ButtonStub,
        DropdownMenuSeparator: true,
        DropdownMenuTrigger: PassThrough,
        PartyDetailHeader: PartyDetailHeaderStub,
        PartyOverviewPanel: true,
        PartyProjectsPanel: true,
        PartyFormDialog: true,
      },
    },
  }, 60000);
}

describe('PartyDetailView', () => {
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
    PartyDetailViewComponent = (await import('@/features/party/views/PartyDetailView.vue')).default;
  });

  beforeEach(() => {
    storage = {};
  });

  it('party.write 권한이 없으면 수정/삭제 액션이 보이지 않는다', async () => {
    storage.user = JSON.stringify({
      name: '홍길동',
      email: 'hong@abms.co.kr',
      permissions: [{ code: 'party.read', scopes: ['ALL'] }],
    });
    localStorage.setItem('user', storage.user);

    const { wrapper } = await mountPartyDetailView();

    expect(wrapper.text()).not.toContain('협력사 편집삭제');
  });

  it('project.read 권한이 없으면 프로젝트 탭이 보이지 않는다', async () => {
    storage.user = JSON.stringify({
      name: '홍길동',
      email: 'hong@abms.co.kr',
      permissions: [
        { code: 'party.read', scopes: ['ALL'] },
        { code: 'party.write', scopes: ['ALL'] },
      ],
    });
    localStorage.setItem('user', storage.user);

    const { wrapper } = await mountPartyDetailView();

    expect(wrapper.text()).not.toContain('프로젝트');
  });

  it('tab query가 있으면 해당 탭을 활성화한다', async () => {
    storage.user = JSON.stringify({
      name: '홍길동',
      email: 'hong@abms.co.kr',
      permissions: [
        { code: 'party.read', scopes: ['ALL'] },
        { code: 'project.read', scopes: ['ALL'] },
      ],
    });
    localStorage.setItem('user', storage.user);

    const { wrapper } = await renderWithProviders(PartyDetailViewComponent, {
      route: '/parties/1?tab=projects',
      routes: [
        { path: '/parties/:partyId', name: 'party-detail', component: PartyDetailViewComponent },
        { path: '/parties', name: 'parties', component: { template: '<div />' } },
      ],
      global: {
        stubs: {
          Alert: PassThrough,
          AlertDescription: PassThrough,
          AlertTitle: PassThrough,
          Tabs: TabsStub,
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
          DropdownMenu: PassThrough,
          DropdownMenuContent: PassThrough,
          DropdownMenuItem: ButtonStub,
          DropdownMenuSeparator: true,
          DropdownMenuTrigger: PassThrough,
          PartyDetailHeader: PartyDetailHeaderStub,
          PartyOverviewPanel: true,
          PartyProjectsPanel: true,
          PartyFormDialog: true,
        },
      },
    });

    expect(wrapper.get('[data-test="active-tab"]').text()).toContain('projects');
  });
});
