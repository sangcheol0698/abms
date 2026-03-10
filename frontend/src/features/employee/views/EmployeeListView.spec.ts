import { defineComponent, h, ref } from 'vue';
import type { Component } from 'vue';
import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises } from '@vue/test-utils';
import { renderWithProviders, createMockQueryState } from '@/test-utils';
import { toast } from 'vue-sonner';

let storage: Record<string, string> = {};
let EmployeeListViewComponent: Component;

const repositoryMock = {
  downloadExcel: vi.fn(),
  downloadSampleExcel: vi.fn(),
  uploadExcel: vi.fn(),
};

let tableActionHandlers: Record<string, any> = {};

const employeesData = ref({
  content: [
    {
      employeeId: 1,
      departmentId: 10,
      departmentName: '개발팀',
      name: '홍길동',
      email: 'hong@abms.co.kr',
      position: '사원',
      status: '재직',
      grade: '초급',
      type: '정규직',
      joinDate: '2024-01-01',
    },
  ],
  totalPages: 1,
  totalElements: 1,
});

const employeeDetailRefetchMock = vi.fn();

const deletionState = {
  isDialogOpen: ref(false),
  isProcessing: ref(false),
  description: ref('삭제 확인'),
  open: vi.fn(() => {
    deletionState.isDialogOpen.value = true;
  }),
  cancel: vi.fn(() => {
    deletionState.isDialogOpen.value = false;
  }),
  confirm: vi.fn(),
};

vi.mock('@/core/di/container', () => ({
  appContainer: {
    resolve: () => repositoryMock,
  },
}));

vi.mock('@/features/employee/configs/tableColumns', () => ({
  createEmployeeTableColumns: (handlers: Record<string, any>) => {
    tableActionHandlers = handlers;
    return [];
  },
}));

vi.mock('@/features/employee/composables/useEmployeeQuerySync.ts', () => ({
  useEmployeeQuerySync: ({ page, pageSize }: { page: { value: number }; pageSize: { value: number } }) => ({
    buildSearchParams: () => ({
      page: page.value,
      size: pageSize.value,
    }),
  }),
}));

vi.mock('@/features/employee/composables/useEmployeeDeletion', () => ({
  useEmployeeDeletion: () => deletionState,
}));

vi.mock('@/features/employee/composables', () => ({
  useEmployeeSummary: () => ({
    cards: [],
  }),
}));

vi.mock('@/features/department/queries/useDepartmentQueries', () => ({
  useDepartmentOrganizationChartQuery: () =>
    createMockQueryState({
      data: [
        {
          departmentId: 10,
          departmentName: '개발팀',
          departmentCode: 'DEV',
          departmentType: 'TEAM',
          departmentLeader: null,
          employeeCount: 1,
          children: [],
        },
      ],
    }),
}));

vi.mock('@/features/employee/queries/useEmployeeQueries', () => ({
  useEmployeesQuery: () => ({
    ...createMockQueryState({ data: employeesData.value }),
    data: employeesData,
  }),
  useEmployeeStatusesQuery: () => createMockQueryState({ data: [{ value: 'ACTIVE', label: '재직' }] }),
  useEmployeeTypesQuery: () => createMockQueryState({ data: [{ value: 'FULL_TIME', label: '정직원' }] }),
  useEmployeeGradesQuery: () => createMockQueryState({ data: [{ value: 'JUNIOR', label: '초급' }] }),
  useEmployeePositionsQuery: () => createMockQueryState({ data: [{ value: 'ASSOCIATE', label: '사원' }] }),
  useEmployeeDetailQuery: () => ({
    ...createMockQueryState(),
    refetch: employeeDetailRefetchMock,
  }),
}));

vi.mock('vue-sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
    loading: vi.fn(() => 'loading-id'),
    dismiss: vi.fn(),
  },
}));

const PassThrough = defineComponent({
  setup(_, { slots }) {
    return () => h('div', slots.default?.());
  },
});

const ButtonStub = defineComponent({
  props: {
    disabled: {
      type: Boolean,
      required: false,
    },
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
        h(
          'button',
          {
            'data-test': 'page-change',
            onClick: () => emit('pageChange', 2),
          },
          'page-2',
        ),
        h(
          'button',
          {
            'data-test': 'page-size-change',
            onClick: () => emit('pageSizeChange', 25),
          },
          'size-25',
        ),
      ]);
  },
});

const DepartmentSelectDialogStub = defineComponent({
  props: {
    open: {
      type: Boolean,
      required: true,
    },
  },
  emits: ['select', 'update:open'],
  setup(props, { emit }) {
    return () =>
      h('div', [
        h('span', { 'data-test': 'department-open' }, String(props.open)),
        h(
          'button',
          {
            'data-test': 'department-select',
            onClick: () => emit('select', { departmentId: 10, departmentName: '개발팀' }),
          },
          'select-department',
        ),
      ]);
  },
});

const EmployeeCreateDialogStub = defineComponent({
  props: {
    open: {
      type: Boolean,
      required: true,
    },
  },
  setup(props) {
    return () => h('div', { 'data-test': 'create-open' }, String(props.open));
  },
});

const EmployeeUpdateDialogStub = defineComponent({
  props: {
    open: {
      type: Boolean,
      required: true,
    },
  },
  setup(props) {
    return () => h('div', { 'data-test': 'update-open' }, String(props.open));
  },
});

async function mountEmployeeListView() {
  return renderWithProviders(EmployeeListViewComponent, {
    route: '/employees',
    routes: [
      { path: '/employees', name: 'employees', component: EmployeeListViewComponent },
      { path: '/employees/:employeeId', name: 'employee-detail', component: { template: '<div />' } },
      { path: '/departments/:departmentId', name: 'department', component: { template: '<div />' } },
    ],
    global: {
      stubs: {
        EmployeeSummaryCards: true,
        DataTableToolbar: {
          template: '<div><slot name="filters" /><slot name="actions" /></div>',
        },
        DataTableFacetedFilter: true,
        DataTable: true,
        DataTablePagination: DataTablePaginationStub,
        ExcelUploadDialog: true,
        EmployeeCreateDialog: EmployeeCreateDialogStub,
        EmployeeUpdateDialog: EmployeeUpdateDialogStub,
        DepartmentSelectDialog: DepartmentSelectDialogStub,
        Button: ButtonStub,
        DropdownMenu: PassThrough,
        DropdownMenuTrigger: PassThrough,
        DropdownMenuContent: PassThrough,
        DropdownMenuItem: {
          props: ['disabled'],
          emits: ['click'],
          template:
            '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
        },
        DropdownMenuSeparator: true,
        AlertDialog: {
          props: ['open'],
          template: '<div data-test="alert-open">{{ String(open) }}<slot /></div>',
        },
        AlertDialogContent: PassThrough,
        AlertDialogHeader: PassThrough,
        AlertDialogTitle: PassThrough,
        AlertDialogDescription: PassThrough,
        AlertDialogFooter: PassThrough,
        AlertDialogCancel: ButtonStub,
        AlertDialogAction: ButtonStub,
        Building2: true,
        ChevronDown: true,
        Download: true,
        FileSpreadsheet: true,
        Plus: true,
        Upload: true,
        X: true,
      },
    },
  });
}

describe('EmployeeListView', () => {
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
    EmployeeListViewComponent = (await import('@/features/employee/views/EmployeeListView.vue')).default;
  });

  beforeEach(() => {
    vi.clearAllMocks();
    storage = {};
    tableActionHandlers = {};
    deletionState.isDialogOpen.value = false;
    repositoryMock.downloadExcel.mockResolvedValue(undefined);
    employeeDetailRefetchMock.mockResolvedValue({
      data: {
        employeeId: 1,
        name: '홍길동',
      },
    });
    storage.user = JSON.stringify({
      name: '홍길동',
      email: 'hong@abms.co.kr',
      permissions: [
        { code: 'employee.read', scopes: ['ALL'] },
        { code: 'employee.write', scopes: ['ALL'] },
      ],
    });
    localStorage.setItem(
      'user',
      storage.user,
    );
  });

  it('직원 추가 버튼 클릭 시 생성 다이얼로그를 연다', async () => {
    const { wrapper } = await mountEmployeeListView();

    expect(wrapper.get('[data-test="create-open"]').text()).toBe('false');

    const createButton = wrapper
      .findAll('button')
      .find((item) => item.text().includes('직원 추가'));
    expect(createButton).toBeDefined();
    await createButton?.trigger('click');
    await flushPromises();

    expect(wrapper.get('[data-test="create-open"]').text()).toBe('true');
  });

  it('부서를 선택하면 필터 라벨이 변경된다', async () => {
    const { wrapper } = await mountEmployeeListView();

    expect(wrapper.text()).toContain('부서 선택');

    await wrapper.get('[data-test="department-select"]').trigger('click');

    expect(wrapper.text()).toContain('개발팀');
  });

  it('현재 조건 다운로드 클릭 시 저장소 다운로드 메서드를 호출한다', async () => {
    const { wrapper } = await mountEmployeeListView();

    const target = wrapper
      .findAll('button')
      .find((item) => item.text().includes('현재 조건 다운로드'));

    expect(target).toBeDefined();
    await target?.trigger('click');

    expect(repositoryMock.downloadExcel).toHaveBeenCalledWith({ page: 1, size: 10 });
    expect(toast.success).toHaveBeenCalledWith('엑셀 파일을 다운로드했습니다.');
  });

  it('수정 액션 실행 시 상세 조회 후 수정 다이얼로그를 연다', async () => {
    const { wrapper } = await mountEmployeeListView();

    await tableActionHandlers.onEditEmployee({
      employeeId: 1,
      name: '홍길동',
      email: 'hong@abms.co.kr',
    });

    await flushPromises();

    expect(employeeDetailRefetchMock).toHaveBeenCalled();
    expect(wrapper.get('[data-test="update-open"]').text()).toBe('true');
  });

  it('수정 상세 조회가 실패하면 오류 토스트를 표시한다', async () => {
    employeeDetailRefetchMock.mockRejectedValueOnce(new Error('failed'));

    await mountEmployeeListView();

    await tableActionHandlers.onEditEmployee({
      employeeId: 1,
      name: '홍길동',
      email: 'hong@abms.co.kr',
    });

    expect(toast.error).toHaveBeenCalledWith('직원 정보를 불러오지 못했습니다.', {
      description: '직원 정보를 불러오지 못했습니다.',
    });
  });

  it('삭제 액션 실행 시 삭제 확인 다이얼로그를 연다', async () => {
    const { wrapper } = await mountEmployeeListView();

    tableActionHandlers.onDeleteEmployee({ employeeId: 1, name: '홍길동' });
    await flushPromises();

    expect(deletionState.open).toHaveBeenCalledWith(1, '홍길동');
    expect(wrapper.get('[data-test="alert-open"]').text()).toContain('true');
  });

  it('employee.write 권한이 없으면 직원 추가 버튼이 보이지 않는다', async () => {
    storage.user = JSON.stringify({
      name: '홍길동',
      email: 'hong@abms.co.kr',
      permissions: [{ code: 'employee.read', scopes: ['ALL'] }],
    });
    localStorage.setItem('user', storage.user);

    const { wrapper } = await mountEmployeeListView();
    const createButton = wrapper.findAll('button').find((item) => item.text().includes('직원 추가'));

    expect(createButton).toBeUndefined();
  });
});
