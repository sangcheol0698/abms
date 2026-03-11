import { defineComponent, h, ref } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders, createMockQueryState } from '@/test-utils';
import { queryClient } from '@/core/query';
import PartyListView from '@/features/party/views/PartyListView.vue';
import { toast } from 'vue-sonner';

const listQueryState = createMockQueryState({
  data: {
    content: [
      {
        partyId: 1,
        name: '협력사A',
        ceo: '대표자',
        manager: '담당자',
        contact: '010-1111-1111',
        email: 'party@abacus.co.kr',
      },
    ],
    totalPages: 2,
    totalElements: 11,
  },
});

const summaryQueryState = createMockQueryState({
  data: {
    totalCount: 11,
    withProjectsCount: 6,
    withInProgressProjectsCount: 3,
    withoutProjectsCount: 5,
    totalContractAmount: 1000000,
  },
});

const detailRefetchMock = vi.fn();
const deleteMutateAsyncMock = vi.fn();

vi.mock('@/features/party/composables/usePartyQuerySync', () => ({
  usePartyQuerySync: () => ({
    buildSearchParams: () => ({ page: 1, size: 10 }),
  }),
}));

vi.mock('@/features/party/queries/usePartyQueries', () => ({
  usePartyListQuery: () => listQueryState,
  usePartyOverviewSummaryQuery: () => summaryQueryState,
  usePartyDetailQuery: () => ({
    ...createMockQueryState(),
    refetch: detailRefetchMock,
  }),
  useDeletePartyMutation: () => ({
    isPending: ref(false),
    mutateAsync: deleteMutateAsyncMock,
  }),
}));

vi.mock('vue-sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

const PassThrough = defineComponent({
  setup(_, { slots }) {
    return () => h('div', slots.default?.());
  },
});

const ButtonStub = defineComponent({
  props: {
    disabled: Boolean,
  },
  emits: ['click'],
  setup(props, { emit, slots }) {
    return () =>
      h(
        'button',
        {
          disabled: props.disabled,
          onClick: (event: MouseEvent) => emit('click', event),
        },
        slots.default?.(),
      );
  },
});

const DataTablePaginationStub = defineComponent({
  emits: ['pageChange', 'pageSizeChange'],
  setup(_, { emit }) {
    return () =>
      h('div', [
        h('button', { 'data-test': 'page-change', onClick: () => emit('pageChange', 2) }, 'page-2'),
        h(
          'button',
          { 'data-test': 'page-size-change', onClick: () => emit('pageSizeChange', 25) },
          'size-25',
        ),
      ]);
  },
});

const PartyFormDialogStub = defineComponent({
  props: {
    open: {
      type: Boolean,
      required: true,
    },
    mode: {
      type: String,
      required: true,
    },
  },
  emits: ['update:open', 'created', 'updated'],
  setup(props, { emit }) {
    return () =>
      h('div', [
        h('span', { 'data-test': 'party-form-open' }, String(props.open)),
        h('span', { 'data-test': 'party-form-mode' }, props.mode),
        h('button', { 'data-test': 'emit-created', onClick: () => emit('created') }, 'created'),
        h('button', { 'data-test': 'emit-updated', onClick: () => emit('updated') }, 'updated'),
      ]);
  },
});

async function mountPartyListView() {
  return renderWithProviders(PartyListView, {
    route: '/parties',
    routes: [
      { path: '/parties', name: 'parties', component: PartyListView },
      { path: '/parties/:partyId', name: 'party-detail', component: { template: '<div />' } },
    ],
    global: {
      stubs: {
        PartySummaryCards: {
          props: ['cards'],
          template: '<div data-test="summary-count">{{ cards.length }}</div>',
        },
        DataTableToolbar: {
          template: '<div><slot name="actions" /></div>',
        },
        DataTableColumnHeader: true,
        DataTable: {
          props: ['data'],
          template: '<div data-test="party-row-count">{{ data.length }}</div>',
        },
        DataTablePagination: DataTablePaginationStub,
        PartyFormDialog: PartyFormDialogStub,
        Button: ButtonStub,
        DropdownMenu: PassThrough,
        DropdownMenuContent: PassThrough,
        DropdownMenuTrigger: PassThrough,
        DropdownMenuItem: ButtonStub,
        DropdownMenuSeparator: true,
        AlertDialog: { props: ['open'], template: '<div data-test="alert-open">{{ String(open) }}<slot /></div>' },
        AlertDialogContent: PassThrough,
        AlertDialogHeader: PassThrough,
        AlertDialogTitle: PassThrough,
        AlertDialogDescription: PassThrough,
        AlertDialogFooter: PassThrough,
        AlertDialogCancel: ButtonStub,
        AlertDialogAction: ButtonStub,
        Checkbox: true,
        MoreHorizontal: true,
        Plus: true,
      },
    },
  });
}

describe('PartyListView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(queryClient, 'invalidateQueries').mockResolvedValue(undefined as never);
    detailRefetchMock.mockResolvedValue({ data: { partyId: 1, name: '협력사A' } });
    deleteMutateAsyncMock.mockResolvedValue(undefined);
  });

  it('협력사 추가 버튼 클릭 시 생성 다이얼로그를 연다', async () => {
    const { wrapper } = await mountPartyListView();

    expect(wrapper.get('[data-test="party-form-open"]').text()).toBe('false');

    const button = wrapper.findAll('button').find((item) => item.text().includes('협력사 추가'));
    await button?.trigger('click');

    expect(wrapper.get('[data-test="party-form-open"]').text()).toBe('true');
    expect(wrapper.get('[data-test="party-form-mode"]').text()).toBe('create');
  });

  it('created 이벤트 시 목록을 invalidate하고 성공 토스트를 표시한다', async () => {
    const { wrapper } = await mountPartyListView();

    await wrapper.get('[data-test="emit-created"]').trigger('click');

    expect(queryClient.invalidateQueries).toHaveBeenCalled();
    expect(toast.success).toHaveBeenCalledWith('협력사가 등록되었습니다.');
    expect(wrapper.get('[data-test="party-form-open"]').text()).toBe('false');
  });

  it('updated 이벤트 시 목록을 invalidate하고 성공 토스트를 표시한다', async () => {
    const { wrapper } = await mountPartyListView();

    await wrapper.get('[data-test="emit-updated"]').trigger('click');

    expect(queryClient.invalidateQueries).toHaveBeenCalled();
    expect(toast.success).toHaveBeenCalledWith('협력사 정보가 수정되었습니다.');
  });

  it('요약 카드와 목록 데이터를 렌더링한다', async () => {
    const { wrapper } = await mountPartyListView();

    expect(wrapper.get('[data-test="summary-count"]').text()).toBe('4');
    expect(wrapper.get('[data-test="party-row-count"]').text()).toBe('1');
  });
});
