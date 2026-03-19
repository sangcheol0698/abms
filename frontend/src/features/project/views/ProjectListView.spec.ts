import { defineComponent, h, ref } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders, createMockQueryState } from '@/test-utils';
import ProjectListView from '@/features/project/views/ProjectListView.vue';
import { toast } from 'vue-sonner';

let storage: Record<string, string> = {};

const repositoryMock = {
  downloadExcel: vi.fn(),
  downloadExcelSample: vi.fn(),
  uploadExcel: vi.fn(),
};

const deletionState = {
  isDialogOpen: ref(false),
  isProcessing: ref(false),
  description: ref('삭제 확인'),
  open: vi.fn(() => {
    deletionState.isDialogOpen.value = true;
  }),
  cancel: vi.fn(),
  confirm: vi.fn(),
};

vi.mock('@/core/di/container', () => ({
  appContainer: {
    resolve: () => repositoryMock,
  },
}));

vi.mock('@/features/project/composables/useProjectQuerySync', () => ({
  useProjectQuerySync: vi.fn(),
}));

vi.mock('@/features/project/composables/useProjectDeletion', () => ({
  useProjectDeletion: () => deletionState,
}));

vi.mock('@/features/project/queries/useProjectQueries', () => ({
  useProjectListQuery: () =>
    createMockQueryState({
      data: {
        content: [
          {
            projectId: 1,
            name: 'ABMS 리뉴얼',
            code: 'P-001',
            partyId: 100,
            partyName: '협력사A',
            leadDepartmentId: 20,
            leadDepartmentName: '플랫폼팀',
            statusLabel: '진행 중',
            contractAmount: 1000000,
            startDate: '2024-01-01',
            endDate: '2024-12-31',
          },
        ],
        totalPages: 1,
        totalElements: 1,
      },
    }),
  useProjectOverviewSummaryQuery: () =>
    createMockQueryState({
      data: {
        totalCount: 1,
        scheduledCount: 0,
        inProgressCount: 1,
        completedCount: 0,
        onHoldCount: 0,
        cancelledCount: 0,
        totalContractAmount: 1000000,
      },
    }),
  useProjectStatusesQuery: () =>
    createMockQueryState({
      data: [{ value: 'IN_PROGRESS', label: '진행 중' }],
    }),
  useProjectDetailQuery: () => ({
    ...createMockQueryState(),
    refetch: vi.fn(),
  }),
}));

vi.mock('@/features/party/queries/usePartyQueries', () => ({
  usePartyDetailQuery: () =>
    createMockQueryState({
      data: { partyId: 100, name: '협력사A' },
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

const ProjectCreateDialogStub = defineComponent({
  props: {
    open: {
      type: Boolean,
      required: true,
    },
  },
  setup(props) {
    return () => h('div', { 'data-test': 'project-create-open' }, String(props.open));
  },
});

const PartySelectDialogStub = defineComponent({
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
        h('span', { 'data-test': 'party-open' }, String(props.open)),
        h(
          'button',
          {
            'data-test': 'party-select',
            onClick: () => emit('select', { partyId: 100, partyName: '협력사A' }),
          },
          'select-party',
        ),
      ]);
  },
});

async function mountProjectListView() {
  return renderWithProviders(ProjectListView, {
    route: '/projects',
    routes: [
      { path: '/projects', name: 'projects', component: ProjectListView },
      { path: '/projects/:projectId', name: 'project-detail', component: { template: '<div />' } },
      { path: '/departments/:departmentId', name: 'department', component: { template: '<div />' } },
      { path: '/parties/:partyId', name: 'party-detail', component: { template: '<div />' } },
    ],
    global: {
      stubs: {
        ProjectSummaryCards: true,
        DataTableToolbar: {
          template: '<div><slot name="filters" /><slot name="actions" /></div>',
        },
        DataTableFacetedFilter: true,
        DataTableColumnHeader: true,
        DataTable: true,
        DataTablePagination: DataTablePaginationStub,
        DateRangeFilter: true,
        ExcelUploadDialog: true,
        ProjectCreateDialog: ProjectCreateDialogStub,
        ProjectUpdateDialog: true,
        PartySelectDialog: PartySelectDialogStub,
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
          template: '<div data-test="project-alert-open">{{ String(open) }}<slot /></div>',
        },
        AlertDialogContent: PassThrough,
        AlertDialogHeader: PassThrough,
        AlertDialogTitle: PassThrough,
        AlertDialogDescription: PassThrough,
        AlertDialogFooter: PassThrough,
        AlertDialogCancel: ButtonStub,
        AlertDialogAction: ButtonStub,
        Checkbox: true,
        Badge: true,
        ProjectRowActions: true,
        Building: true,
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

describe('ProjectListView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    storage = {};
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
    storage.user = JSON.stringify({
      name: '홍길동',
      email: 'hong@abms.co.kr',
      employeeId: 1,
      departmentId: 10,
      permissions: [
        { code: 'project.read', scopes: ['ALL'] },
        { code: 'project.write', scopes: ['ALL'] },
        { code: 'project.excel.download', scopes: ['ALL'] },
        { code: 'project.excel.upload', scopes: ['ALL'] },
        { code: 'party.read', scopes: ['ALL'] },
      ],
    });
    localStorage.setItem('user', storage.user);
    deletionState.isDialogOpen.value = false;
    repositoryMock.downloadExcel.mockResolvedValue(undefined);
    repositoryMock.downloadExcelSample.mockResolvedValue(undefined);
  });

  it('프로젝트 추가 버튼 클릭 시 생성 다이얼로그를 연다', async () => {
    const { wrapper } = await mountProjectListView();

    expect(wrapper.get('[data-test="project-create-open"]').text()).toBe('false');

    const createButton = wrapper
      .findAll('button')
      .find((item) => item.text().includes('프로젝트 추가'));
    expect(createButton).toBeDefined();
    await createButton?.trigger('click');

    expect(wrapper.get('[data-test="project-create-open"]').text()).toBe('true');
  });

  it('협력사를 선택하면 필터 라벨이 변경된다', async () => {
    const { wrapper } = await mountProjectListView();

    expect(wrapper.text()).toContain('협력사 선택');

    await wrapper.get('[data-test="party-select"]').trigger('click');

    expect(wrapper.text()).toContain('협력사A');
  });

  it('현재 조건 다운로드를 실행하면 저장소 메서드를 호출한다', async () => {
    const { wrapper } = await mountProjectListView();

    const downloadButton = wrapper
      .findAll('button')
      .find((item) => item.text().includes('현재 조건 다운로드'));
    expect(downloadButton).toBeDefined();

    await downloadButton?.trigger('click');

    expect(repositoryMock.downloadExcel).toHaveBeenCalledWith({ page: 1, size: 10 });
    expect(toast.success).toHaveBeenCalledWith('엑셀 다운로드가 완료되었습니다.');
  });

  it('페이지 크기 변경 후 다운로드하면 변경된 사이즈를 사용한다', async () => {
    const { wrapper } = await mountProjectListView();

    await wrapper.get('[data-test="page-size-change"]').trigger('click');

    const downloadButton = wrapper
      .findAll('button')
      .find((item) => item.text().includes('현재 조건 다운로드'));
    await downloadButton?.trigger('click');

    expect(repositoryMock.downloadExcel).toHaveBeenCalledWith({ page: 1, size: 25 });
  });

  it('샘플 다운로드를 실행하면 샘플 메서드를 호출한다', async () => {
    const { wrapper } = await mountProjectListView();

    const sampleButton = wrapper.findAll('button').find((item) => item.text().includes('샘플 다운로드'));
    expect(sampleButton).toBeDefined();

    await sampleButton?.trigger('click');

    expect(repositoryMock.downloadExcelSample).toHaveBeenCalled();
    expect(toast.success).toHaveBeenCalledWith('샘플 파일 다운로드가 완료되었습니다.');
  });

  it('삭제 상태가 열리면 삭제 다이얼로그 open 상태가 true가 된다', async () => {
    const { wrapper } = await mountProjectListView();

    deletionState.isDialogOpen.value = true;
    await Promise.resolve();

    expect(wrapper.get('[data-test="project-alert-open"]').text()).toContain('true');
  });

  it('project.write 권한이 없으면 프로젝트 추가 버튼이 보이지 않는다', async () => {
    storage.user = JSON.stringify({
      name: '홍길동',
      email: 'hong@abms.co.kr',
      employeeId: 1,
      departmentId: 10,
      permissions: [
        { code: 'project.read', scopes: ['ALL'] },
        { code: 'project.excel.download', scopes: ['ALL'] },
        { code: 'project.excel.upload', scopes: ['ALL'] },
        { code: 'party.read', scopes: ['ALL'] },
      ],
    });
    localStorage.setItem('user', storage.user);

    const { wrapper } = await mountProjectListView();
    const createButton = wrapper.findAll('button').find((item) => item.text().includes('프로젝트 추가'));

    expect(createButton).toBeUndefined();
  });

  it('project.excel.download 권한이 없으면 현재 조건 다운로드 메뉴가 보이지 않는다', async () => {
    storage.user = JSON.stringify({
      name: '홍길동',
      email: 'hong@abms.co.kr',
      employeeId: 1,
      departmentId: 10,
      permissions: [
        { code: 'project.read', scopes: ['ALL'] },
        { code: 'project.write', scopes: ['ALL'] },
        { code: 'project.excel.upload', scopes: ['ALL'] },
        { code: 'party.read', scopes: ['ALL'] },
      ],
    });
    localStorage.setItem('user', storage.user);

    const { wrapper } = await mountProjectListView();
    const downloadButton = wrapper.findAll('button').find((item) => item.text().includes('현재 조건 다운로드'));

    expect(downloadButton).toBeUndefined();
  });

  it('project.excel.upload 권한이 없으면 엑셀 업로드 메뉴가 보이지 않는다', async () => {
    storage.user = JSON.stringify({
      name: '홍길동',
      email: 'hong@abms.co.kr',
      employeeId: 1,
      departmentId: 10,
      permissions: [
        { code: 'project.read', scopes: ['ALL'] },
        { code: 'project.write', scopes: ['ALL'] },
        { code: 'project.excel.download', scopes: ['ALL'] },
        { code: 'party.read', scopes: ['ALL'] },
      ],
    });
    localStorage.setItem('user', storage.user);

    const { wrapper } = await mountProjectListView();
    const uploadButton = wrapper.findAll('button').find((item) => item.text().includes('엑셀 업로드'));

    expect(uploadButton).toBeUndefined();
  });
});
