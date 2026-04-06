import { beforeEach, describe, expect, it, vi } from 'vitest';
import { nextTick, ref } from 'vue';
import { renderWithProviders, createMockQueryState } from '@/test-utils';
import WeeklyReportView from '@/features/report/views/WeeklyReportView.vue';

const listState = createMockQueryState({
  data: [
    {
      id: 1,
      title: '주간 운영 보고서 (2026-03-23 ~ 2026-03-29)',
      weekStart: '2026-03-23',
      weekEnd: '2026-03-29',
      status: 'DRAFT',
      statusDescription: '초안',
      reportMarkdown: '# 보고서',
      snapshotSummary: {
        totalEmployees: 30,
        onLeaveEmployees: 2,
        inProgressProjects: 8,
        startedProjects: 1,
        endedProjects: 1,
        monthlyRevenueAvailable: true,
      },
      failureReason: null,
      createdAt: new Date('2026-03-30T09:00:00'),
      updatedAt: new Date('2026-03-30T09:05:00'),
    },
  ],
});

const detailState = createMockQueryState({
  data: {
    id: 1,
    title: '주간 운영 보고서 (2026-03-23 ~ 2026-03-29)',
    weekStart: '2026-03-23',
    weekEnd: '2026-03-29',
    status: 'DRAFT',
    statusDescription: '초안',
    reportMarkdown: '# 보고서',
    snapshotSummary: {
      totalEmployees: 30,
      onLeaveEmployees: 2,
      inProgressProjects: 8,
      startedProjects: 1,
      endedProjects: 1,
      monthlyRevenueAvailable: true,
    },
    snapshotJson: '{ "employees": { "totalEmployees": 30 } }',
    failureReason: null,
    createdAt: new Date('2026-03-30T09:00:00'),
    updatedAt: new Date('2026-03-30T09:05:00'),
  },
});

const createMutation = {
  isPending: ref(false),
  mutateAsync: vi.fn(async () => ({
    id: 2,
    title: '주간 운영 보고서 (2026-03-30 ~ 2026-04-05)',
    weekStart: '2026-03-30',
    weekEnd: '2026-04-05',
    status: 'PENDING',
    statusDescription: '대기 중',
    reportMarkdown: '',
    snapshotSummary: {
      totalEmployees: 31,
      onLeaveEmployees: 1,
      inProgressProjects: 9,
      startedProjects: 2,
      endedProjects: 0,
      monthlyRevenueAvailable: false,
    },
    snapshotJson: '',
    failureReason: null,
    createdAt: new Date('2026-04-06T09:00:00'),
    updatedAt: new Date('2026-04-06T09:05:00'),
  })),
};

const regenerateMutation = {
  isPending: ref(false),
  mutateAsync: vi.fn(async () => ({
    id: 3,
    title: '주간 운영 보고서 (2026-03-23 ~ 2026-03-29)',
    weekStart: '2026-03-23',
    weekEnd: '2026-03-29',
    status: 'PENDING',
    statusDescription: '대기 중',
    reportMarkdown: '',
    snapshotSummary: {
      totalEmployees: 30,
      onLeaveEmployees: 2,
      inProgressProjects: 8,
      startedProjects: 1,
      endedProjects: 1,
      monthlyRevenueAvailable: true,
    },
    snapshotJson: '',
    failureReason: null,
    createdAt: new Date('2026-04-06T10:00:00'),
    updatedAt: new Date('2026-04-06T10:05:00'),
  })),
};

const cancelMutation = {
  isPending: ref(false),
  mutateAsync: vi.fn(async () => ({
    id: 1,
    title: '주간 운영 보고서 (2026-03-23 ~ 2026-03-29)',
    weekStart: '2026-03-23',
    weekEnd: '2026-03-29',
    status: 'CANCELLED',
    statusDescription: '생성 중지',
    reportMarkdown: '',
    snapshotSummary: {
      totalEmployees: 0,
      onLeaveEmployees: 0,
      inProgressProjects: 0,
      startedProjects: 0,
      endedProjects: 0,
      monthlyRevenueAvailable: false,
    },
    snapshotJson: '',
    failureReason: null,
    createdAt: new Date('2026-04-06T10:10:00'),
    updatedAt: new Date('2026-04-06T10:11:00'),
  })),
};

const updateMutation = {
  isPending: ref(false),
  mutateAsync: vi.fn(async () => ({
    id: 1,
    title: '수정된 주간 운영 보고서',
    weekStart: '2026-03-23',
    weekEnd: '2026-03-29',
    status: 'DRAFT',
    statusDescription: '초안',
    reportMarkdown: '# 수정된 보고서',
    snapshotSummary: {
      totalEmployees: 30,
      onLeaveEmployees: 2,
      inProgressProjects: 8,
      startedProjects: 1,
      endedProjects: 1,
      monthlyRevenueAvailable: true,
    },
    snapshotJson: '{ "employees": { "totalEmployees": 30 } }',
    failureReason: null,
    createdAt: new Date('2026-03-30T09:00:00'),
    updatedAt: new Date('2026-03-30T10:00:00'),
  })),
};

const deleteMutation = {
  isPending: ref(false),
  mutateAsync: vi.fn(async () => undefined),
};

vi.mock('@/features/report/queries/useWeeklyReportQueries', () => ({
  useWeeklyReportDraftListQuery: () => ({
    ...listState,
    refetch: vi.fn(),
  }),
  useWeeklyReportDraftDetailQuery: () => detailState,
  useCreateWeeklyReportDraftMutation: () => createMutation,
  useCancelWeeklyReportDraftMutation: () => cancelMutation,
  useUpdateWeeklyReportDraftMutation: () => updateMutation,
  useDeleteWeeklyReportDraftMutation: () => deleteMutation,
  useRegenerateWeeklyReportDraftMutation: () => regenerateMutation,
}));

vi.mock('vue-sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

describe('WeeklyReportView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('초안 목록과 상세를 렌더링한다', async () => {
    const { wrapper } = await renderWithProviders(WeeklyReportView, {
      route: '/reports/weekly',
      routes: [
        {
          path: '/reports/weekly',
          name: 'weekly-report',
          component: WeeklyReportView,
        },
        {
          path: '/reports/weekly/:draftId',
          name: 'weekly-report-detail',
          component: WeeklyReportView,
        },
      ],
      global: {
        stubs: {
          FeatureSplitLayout: {
            template: '<div><slot name="sidebar" /><slot /></div>',
          },
          MarkdownRenderer: {
            props: ['content'],
            template: '<div data-test="markdown">{{ content }}</div>',
          },
          Button: {
            props: ['disabled'],
            emits: ['click'],
            template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
          },
          DatePicker: {
            props: ['modelValue', 'type', 'id'],
            emits: ['update:modelValue'],
            template: '<div data-test="date-picker" :data-id="id" @click="$emit(\'update:modelValue\', new Date(\'2026-03-31\'))"></div>',
          },
          Label: true,
          Badge: true,
          Card: { template: '<div><slot /></div>' },
          CardHeader: { template: '<div><slot /></div>' },
          CardContent: { template: '<div><slot /></div>' },
          CardTitle: { template: '<div><slot /></div>' },
          CardDescription: { template: '<div><slot /></div>' },
          Input: {
            props: ['modelValue', 'id'],
            emits: ['update:modelValue'],
            template: '<input :id="id" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
          },
          Textarea: {
            props: ['modelValue', 'id'],
            emits: ['update:modelValue'],
            template: '<textarea :id="id" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>',
          },
          Dialog: { template: '<div><slot /></div>' },
          DialogContent: { template: '<div><slot /></div>' },
          DialogHeader: { template: '<div><slot /></div>' },
          DialogTitle: { template: '<div><slot /></div>' },
          DialogDescription: { template: '<div><slot /></div>' },
          DialogFooter: { template: '<div><slot /></div>' },
          AlertDialog: { template: '<div><slot /></div>' },
          AlertDialogContent: { template: '<div><slot /></div>' },
          AlertDialogHeader: { template: '<div><slot /></div>' },
          AlertDialogTitle: { template: '<div><slot /></div>' },
          AlertDialogDescription: { template: '<div><slot /></div>' },
          AlertDialogFooter: { template: '<div><slot /></div>' },
          AlertDialogCancel: { template: '<button><slot /></button>' },
          AlertDialogAction: { template: '<button @click="$emit(\'click\')"><slot /></button>', emits: ['click'] },
          Progress: true,
          Loader2: true,
          RefreshCcw: true,
          Sparkles: true,
        },
      },
    });

    expect(wrapper.text()).toContain('주간 운영 보고서');
    expect(wrapper.text()).toContain('총 직원');
    expect(wrapper.find('[data-test="markdown"]').text()).toContain('# 보고서');
  });

  it('초안 생성 요청을 호출한다', async () => {
    const { wrapper } = await renderWithProviders(WeeklyReportView, {
      route: '/reports/weekly',
      routes: [
        {
          path: '/reports/weekly',
          name: 'weekly-report',
          component: WeeklyReportView,
        },
        {
          path: '/reports/weekly/:draftId',
          name: 'weekly-report-detail',
          component: WeeklyReportView,
        },
      ],
      global: {
        stubs: {
          FeatureSplitLayout: {
            template: '<div><slot name="sidebar" /><slot /></div>',
          },
          MarkdownRenderer: true,
          Button: {
            props: ['disabled'],
            emits: ['click'],
            template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
          },
          DatePicker: {
            props: ['modelValue', 'type', 'id'],
            emits: ['update:modelValue'],
            template: '<div data-test="date-picker" :data-id="id" @click="$emit(\'update:modelValue\', new Date(\'2026-03-31\'))"></div>',
          },
          Label: true,
          Badge: true,
          Card: { template: '<div><slot /></div>' },
          CardHeader: { template: '<div><slot /></div>' },
          CardContent: { template: '<div><slot /></div>' },
          CardTitle: { template: '<div><slot /></div>' },
          CardDescription: { template: '<div><slot /></div>' },
          Input: {
            props: ['modelValue', 'id'],
            emits: ['update:modelValue'],
            template: '<input :id="id" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
          },
          Textarea: {
            props: ['modelValue', 'id'],
            emits: ['update:modelValue'],
            template: '<textarea :id="id" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>',
          },
          Dialog: { template: '<div><slot /></div>' },
          DialogContent: { template: '<div><slot /></div>' },
          DialogHeader: { template: '<div><slot /></div>' },
          DialogTitle: { template: '<div><slot /></div>' },
          DialogDescription: { template: '<div><slot /></div>' },
          DialogFooter: { template: '<div><slot /></div>' },
          AlertDialog: { template: '<div><slot /></div>' },
          AlertDialogContent: { template: '<div><slot /></div>' },
          AlertDialogHeader: { template: '<div><slot /></div>' },
          AlertDialogTitle: { template: '<div><slot /></div>' },
          AlertDialogDescription: { template: '<div><slot /></div>' },
          AlertDialogFooter: { template: '<div><slot /></div>' },
          AlertDialogCancel: { template: '<button><slot /></button>' },
          AlertDialogAction: { template: '<button @click="$emit(\'click\')"><slot /></button>', emits: ['click'] },
          Progress: true,
          Loader2: true,
          RefreshCcw: true,
          Sparkles: true,
        },
      },
    });

    const createButton = wrapper.findAll('button').find((button) => button.text().includes('초안 생성'));
    expect(createButton).toBeDefined();

    await createButton!.trigger('click');

    expect(createMutation.mutateAsync).toHaveBeenCalledTimes(1);
  });

  it('생성 중 상태에서는 중지 버튼을 렌더링한다', async () => {
    detailState.data.value = {
      ...detailState.data.value,
      status: 'GENERATING',
      statusDescription: 'AI 초안 작성 중',
      reportMarkdown: '',
      snapshotJson: '',
    };

    const { wrapper } = await renderWithProviders(WeeklyReportView, {
      route: '/reports/weekly',
      routes: [
        {
          path: '/reports/weekly',
          name: 'weekly-report',
          component: WeeklyReportView,
        },
        {
          path: '/reports/weekly/:draftId',
          name: 'weekly-report-detail',
          component: WeeklyReportView,
        },
      ],
      global: {
        stubs: {
          FeatureSplitLayout: {
            template: '<div><slot name="sidebar" /><slot /></div>',
          },
          MarkdownRenderer: true,
          Button: {
            props: ['disabled'],
            emits: ['click'],
            template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
          },
          DatePicker: {
            props: ['modelValue', 'type', 'id'],
            emits: ['update:modelValue'],
            template: '<div data-test="date-picker" :data-id="id" @click="$emit(\'update:modelValue\', new Date(\'2026-03-31\'))"></div>',
          },
          Label: true,
          Badge: true,
          Card: { template: '<div><slot /></div>' },
          CardHeader: { template: '<div><slot /></div>' },
          CardContent: { template: '<div><slot /></div>' },
          CardTitle: { template: '<div><slot /></div>' },
          CardDescription: { template: '<div><slot /></div>' },
          Input: {
            props: ['modelValue', 'id'],
            emits: ['update:modelValue'],
            template: '<input :id="id" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
          },
          Textarea: {
            props: ['modelValue', 'id'],
            emits: ['update:modelValue'],
            template: '<textarea :id="id" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>',
          },
          Dialog: { template: '<div><slot /></div>' },
          DialogContent: { template: '<div><slot /></div>' },
          DialogHeader: { template: '<div><slot /></div>' },
          DialogTitle: { template: '<div><slot /></div>' },
          DialogDescription: { template: '<div><slot /></div>' },
          DialogFooter: { template: '<div><slot /></div>' },
          AlertDialog: { template: '<div><slot /></div>' },
          AlertDialogContent: { template: '<div><slot /></div>' },
          AlertDialogHeader: { template: '<div><slot /></div>' },
          AlertDialogTitle: { template: '<div><slot /></div>' },
          AlertDialogDescription: { template: '<div><slot /></div>' },
          AlertDialogFooter: { template: '<div><slot /></div>' },
          AlertDialogCancel: { template: '<button><slot /></button>' },
          AlertDialogAction: { template: '<button @click="$emit(\'click\')"><slot /></button>', emits: ['click'] },
          Progress: true,
          Loader2: true,
          RefreshCcw: true,
          Sparkles: true,
        },
      },
    });

    const cancelButton = wrapper.findAll('button').find((button) => button.text().includes('중지'));
    expect(cancelButton).toBeDefined();

    detailState.data.value = {
      ...detailState.data.value,
      status: 'DRAFT',
      statusDescription: '초안',
      reportMarkdown: '# 보고서',
      snapshotJson: '{ "employees": { "totalEmployees": 30 } }',
    };
  });
});
