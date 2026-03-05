import { computed, defineComponent, h, nextTick, ref } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises } from '@vue/test-utils';
import DepartmentView from '@/features/department/views/DepartmentView.vue';
import type { DepartmentChartNode } from '@/features/department/models/department';
import { renderWithProviders, createMockQueryState } from '@/test-utils';
import { toast } from 'vue-sonner';
import { queryClient } from '@/core/query';

let chartQueryMock = createMockQueryState<DepartmentChartNode[]>();
let detailQueryMock = {
  ...createMockQueryState<any>(),
  refetch: vi.fn(),
};

vi.mock('@/features/department/queries/useDepartmentQueries', () => ({
  useDepartmentOrganizationChartQuery: () => chartQueryMock,
  useDepartmentDetailQuery: () => detailQueryMock,
}));

vi.mock('vue-sonner', () => ({
  toast: {
    error: vi.fn(),
  },
}));

const closeSidebarMock = vi.fn();

const FeatureSplitLayoutStub = defineComponent({
  setup(_, { slots }) {
    const pane = {
      toggleSidebar: vi.fn(),
      isSidebarCollapsed: ref(false),
      isLargeScreen: ref(true),
      closeSidebar: closeSidebarMock,
    };

    return () =>
      h('div', { 'data-test': 'feature-split-layout' }, [
        slots.sidebar?.({ pane }),
        slots.default?.({ pane }),
      ]);
  },
});

const DepartmentTreeStub = defineComponent({
  props: {
    selectedNodeId: {
      type: Number,
      required: false,
    },
  },
  emits: ['update:selectedNodeId'],
  setup(_props, { emit }) {
    return () =>
      h(
        'button',
        {
          'data-test': 'tree-select',
          onClick: () => emit('update:selectedNodeId', 2),
        },
        'select-2',
      );
  },
});

const DepartmentDetailPanelStub = defineComponent({
  props: {
    department: {
      type: Object,
      required: false,
    },
  },
  emits: ['refresh'],
  setup(props, { emit }) {
    return () =>
      h('div', { 'data-test': 'department-detail-panel' }, [
        h('span', { 'data-test': 'department-name' }, props.department?.departmentName ?? '-'),
        h(
          'button',
          {
            'data-test': 'refresh',
            onClick: () => emit('refresh'),
          },
          'refresh',
        ),
      ]);
  },
});

const chartData: DepartmentChartNode[] = [
  {
    departmentId: 1,
    departmentName: '본부',
    departmentCode: 'HQ',
    departmentType: 'ROOT',
    departmentLeader: null,
    employeeCount: 3,
    children: [
      {
        departmentId: 2,
        departmentName: '개발팀',
        departmentCode: 'DEV',
        departmentType: 'TEAM',
        departmentLeader: null,
        employeeCount: 2,
        children: [],
      },
    ],
  },
];

async function mountDepartmentView(path: string) {
  return renderWithProviders(DepartmentView, {
    route: path,
    routes: [
      { path: '/departments', name: 'departments', component: DepartmentView },
      { path: '/departments/:departmentId', name: 'department', component: DepartmentView },
    ],
    global: {
      stubs: {
        FeatureSplitLayout: FeatureSplitLayoutStub,
        DepartmentTree: DepartmentTreeStub,
        DepartmentDetailPanel: DepartmentDetailPanelStub,
        Breadcrumb: { template: '<div><slot /></div>' },
        BreadcrumbList: { template: '<div><slot /></div>' },
        BreadcrumbItem: { template: '<span><slot /></span>' },
        BreadcrumbLink: { template: '<span><slot /></span>' },
        BreadcrumbPage: { template: '<span><slot /></span>' },
        BreadcrumbSeparator: { template: '<span><slot /></span>' },
        Separator: { template: '<hr />' },
        Menu: { template: '<span />' },
        Slash: { template: '<span>/</span>' },
      },
    },
  });
}

describe('DepartmentView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    closeSidebarMock.mockReset();
    chartQueryMock = createMockQueryState<DepartmentChartNode[]>({
      data: chartData,
      isLoading: false,
    });
    detailQueryMock = {
      ...createMockQueryState({
        data: {
          departmentId: 2,
          departmentName: '개발팀',
          departmentCode: 'DEV',
          departmentType: 'TEAM',
          employeeCount: 2,
          employees: [],
          departmentLeader: null,
        },
      }),
      refetch: vi.fn(async () => ({ data: null })),
    };
  });

  it('로딩 중이면 로딩 문구를 표시한다', async () => {
    chartQueryMock = createMockQueryState<DepartmentChartNode[]>({
      data: [],
      isLoading: true,
    });

    const { wrapper } = await mountDepartmentView('/departments');

    expect(wrapper.text()).toContain('부서 정보를 불러오는 중입니다...');
  });

  it('선택된 부서가 없으면 첫 번째 부서로 라우팅한다', async () => {
    chartQueryMock = createMockQueryState<DepartmentChartNode[]>({
      data: [],
      isLoading: false,
    });

    const { router } = await mountDepartmentView('/departments');

    chartQueryMock.data.value = chartData;
    await nextTick();
    await flushPromises();

    expect(router.currentRoute.value.name).toBe('department');
    expect(router.currentRoute.value.params.departmentId).toBe('1');
  });

  it('존재하지 않는 부서 id면 첫 번째 부서로 보정한다', async () => {
    chartQueryMock = createMockQueryState<DepartmentChartNode[]>({
      data: [],
      isLoading: false,
    });

    const { router } = await mountDepartmentView('/departments/999');

    chartQueryMock.data.value = chartData;
    await nextTick();
    await flushPromises();

    expect(router.currentRoute.value.name).toBe('department');
    expect(router.currentRoute.value.params.departmentId).toBe('1');
  });

  it('브레드크럼 클릭 시 해당 부서로 이동한다', async () => {
    const { wrapper, router } = await mountDepartmentView('/departments/2');

    await flushPromises();

    const rootBreadcrumb = wrapper.findAll('button').find((item) => item.text() === '본부');
    expect(rootBreadcrumb).toBeDefined();

    await rootBreadcrumb!.trigger('click');
    await flushPromises();

    expect(router.currentRoute.value.params.departmentId).toBe('1');
  });

  it('트리 선택 이벤트가 발생하면 상세 라우트로 이동한다', async () => {
    const { wrapper, router } = await mountDepartmentView('/departments/1');

    await wrapper.get('[data-test="tree-select"]').trigger('click');

    await flushPromises();

    expect(router.currentRoute.value.params.departmentId).toBe('2');
  });

  it('조직도 조회 에러가 있으면 토스트 에러를 표시한다', async () => {
    await mountDepartmentView('/departments');
    chartQueryMock.error.value = new Error('조회 실패');
    await nextTick();

    expect(toast.error).toHaveBeenCalledWith('부서 정보를 불러오지 못했습니다.', {
      description: '조회 실패',
    });
  });

  it('상세 패널 refresh 이벤트 시 관련 쿼리를 무효화한다', async () => {
    const invalidateSpy = vi
      .spyOn(queryClient, 'invalidateQueries')
      .mockResolvedValue(undefined as any);

    const { wrapper } = await mountDepartmentView('/departments/2');

    await wrapper.get('[data-test="refresh"]').trigger('click');
    await flushPromises();

    expect(invalidateSpy).toHaveBeenCalledTimes(3);

    invalidateSpy.mockRestore();
  });

  it('선택된 부서가 유효하면 헤더에 부서명을 표시한다', async () => {
    const { wrapper } = await mountDepartmentView('/departments/2');

    await nextTick();

    expect(wrapper.text()).toContain('개발팀');
  });

  it('조직도가 비어 있으면 선택된 부서를 초기화한다', async () => {
    const { wrapper } = await mountDepartmentView('/departments/2');

    chartQueryMock.data.value = [];
    await nextTick();
    await flushPromises();

    expect(wrapper.find('[data-test="department-name"]').text()).toBe('-');
  });

  it('부서 아이디 파라미터가 숫자가 아니면 선택을 해제한다', async () => {
    const { wrapper } = await mountDepartmentView('/departments/not-a-number');

    await flushPromises();

    expect(wrapper.find('[data-test="department-name"]').text()).toBe('-');
  });
});
