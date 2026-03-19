import { computed, defineComponent, h } from 'vue';
import type { Component } from 'vue';
import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test-utils';

let ProjectRevenuePlanPanelComponent: Component;
const issueMutateAsyncMock = vi.fn();
const cancelMutateAsyncMock = vi.fn();
const toastSuccessMock = vi.fn();
const toastErrorMock = vi.fn();

const revenuePlansData = {
  value: [
    {
      projectId: 7,
      sequence: 1,
      revenueDate: '2026-03-01',
      type: 'DOWN_PAYMENT',
      amount: 10000000,
      status: 'PLANNED',
      memo: '기존 메모',
    },
  ],
};

vi.mock('@/features/project/queries/useProjectQueries', () => ({
  useProjectRevenuePlansQuery: () => ({
    data: computed(() => revenuePlansData.value),
    isLoading: { value: false },
    error: { value: null },
  }),
  useProjectDetailQuery: () => ({
    data: computed(() => ({
      projectId: 7,
      contractAmount: 100000000,
    })),
    isLoading: { value: false },
    error: { value: null },
  }),
  useIssueProjectRevenuePlanMutation: () => ({
    mutateAsync: issueMutateAsyncMock,
    isPending: { value: false },
  }),
  useCancelProjectRevenuePlanMutation: () => ({
    mutateAsync: cancelMutateAsyncMock,
    isPending: { value: false },
  }),
}));

vi.mock('@/features/project/permissions', () => ({
  canManageProjects: () => true,
}));

vi.mock('vue-sonner', () => ({
  toast: {
    success: toastSuccessMock,
    error: toastErrorMock,
  },
}));

const PassThrough = defineComponent({
  setup(_, { slots }) {
    return () => h('div', slots.default?.());
  },
});

describe('ProjectRevenuePlanPanel', () => {
  beforeAll(async () => {
    ProjectRevenuePlanPanelComponent = (await import('@/features/project/components/ProjectRevenuePlanPanel.vue')).default;
  }, 60000);

  beforeEach(() => {
    vi.clearAllMocks();
    revenuePlansData.value = [
      {
        projectId: 7,
        sequence: 1,
        revenueDate: '2026-03-01',
        type: 'DOWN_PAYMENT',
        amount: 10000000,
        status: 'PLANNED',
        memo: '기존 메모',
      },
    ];
  });

  it('행별 수정 액션으로 다이얼로그를 연다', async () => {
    const { wrapper } = await renderWithProviders(ProjectRevenuePlanPanelComponent, {
      props: {
        projectId: 7,
      },
      route: '/projects/7?tab=revenue',
      global: {
        stubs: {
          Alert: PassThrough,
          AlertTitle: PassThrough,
          AlertDescription: PassThrough,
          AlertDialog: PassThrough,
          AlertDialogContent: PassThrough,
          AlertDialogHeader: PassThrough,
          AlertDialogTitle: PassThrough,
          AlertDialogDescription: PassThrough,
          AlertDialogFooter: PassThrough,
          AlertDialogCancel: {
            props: ['disabled'],
            emits: ['click'],
            template: '<button type="button" :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
          },
          AlertDialogAction: {
            props: ['disabled'],
            emits: ['click'],
            template: '<button type="button" :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
          },
          ProjectRevenuePlanRowActions: {
            emits: ['edit', 'toggle-status'],
            template: '<div><button data-test="edit-revenue" @click="$emit(\'edit\')">수정</button><button data-test="issue-revenue" @click="$emit(\'toggle-status\')">발행</button></div>',
          },
          ProjectRevenuePlanDialog: {
            props: ['open', 'plan'],
            template: '<div data-test="revenue-dialog">{{ String(open) }}|{{ plan?.sequence ?? "none" }}</div>',
          },
        },
      },
    });

    expect(wrapper.get('[data-test="revenue-dialog"]').text()).toBe('false|none');

    await wrapper.get('[data-test="edit-revenue"]').trigger('click');

    expect(wrapper.get('[data-test="revenue-dialog"]').text()).toBe('true|1');
  });

  it('행별 발행 액션으로 발행 mutation을 호출한다', async () => {
    const { wrapper } = await renderWithProviders(ProjectRevenuePlanPanelComponent, {
      props: {
        projectId: 7,
      },
      route: '/projects/7?tab=revenue',
      global: {
        stubs: {
          Alert: PassThrough,
          AlertTitle: PassThrough,
          AlertDescription: PassThrough,
          AlertDialog: PassThrough,
          AlertDialogContent: PassThrough,
          AlertDialogHeader: PassThrough,
          AlertDialogTitle: PassThrough,
          AlertDialogDescription: PassThrough,
          AlertDialogFooter: PassThrough,
          AlertDialogCancel: {
            props: ['disabled'],
            emits: ['click'],
            template: '<button type="button" :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
          },
          AlertDialogAction: {
            props: ['disabled'],
            emits: ['click'],
            template: '<button type="button" :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
          },
          ProjectRevenuePlanRowActions: {
            emits: ['edit', 'toggle-status'],
            template: '<button data-test="issue-revenue" @click="$emit(\'toggle-status\')">발행</button>',
          },
          ProjectRevenuePlanDialog: {
            props: ['open', 'plan'],
            template: '<div data-test="revenue-dialog">{{ String(open) }}|{{ plan?.sequence ?? "none" }}</div>',
          },
        },
      },
    });

    await wrapper.get('[data-test="issue-revenue"]').trigger('click');
    expect(wrapper.text()).toContain('매출 일정을 발행하시겠습니까?');

    const confirmButton = wrapper.findAll('button').find((button) => button.text() === '발행하기');
    await confirmButton?.trigger('click');

    expect(issueMutateAsyncMock).toHaveBeenCalledWith({
      projectId: 7,
      sequence: 1,
    });
    expect(toastSuccessMock).toHaveBeenCalledWith('매출 일정을 발행했습니다.');
  });
});
