import { computed, defineComponent, h, ref } from 'vue';
import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test-utils';

const listData = ref<any[]>([]);
const detailData = ref<any | null>(null);
const catalogData = ref({
  permissions: [
    {
      code: 'employee.read',
      name: '직원 조회',
      description: '직원 조회',
    },
  ],
  scopes: [
    {
      code: 'ALL',
      description: '전체',
      level: 1,
    },
    {
      code: 'SELF',
      description: '본인',
      level: 5,
    },
  ],
});
const assignableAccountsData = ref<any[]>([]);

const createMutateAsync = vi.fn();
const updateMutateAsync = vi.fn();
const deleteMutateAsync = vi.fn();
const assignMutateAsync = vi.fn();
const unassignMutateAsync = vi.fn();
let PermissionGroupManagementViewComponent: any;

vi.mock('@/features/admin/queries/usePermissionGroupQueries', () => ({
  usePermissionGroupListQuery: () => ({
    data: computed(() => listData.value),
    isLoading: { value: false },
    isFetching: { value: false },
    error: { value: null },
  }),
  usePermissionGroupDetailQuery: () => ({
    data: computed(() => detailData.value),
    isLoading: { value: false },
    isFetching: { value: false },
    error: { value: null },
  }),
  usePermissionGroupCatalogQuery: () => ({
    data: computed(() => catalogData.value),
    isLoading: { value: false },
    isFetching: { value: false },
    error: { value: null },
  }),
  useAssignableAccountsQuery: () => ({
    data: computed(() => assignableAccountsData.value),
    isLoading: { value: false },
    isFetching: { value: false },
  }),
  useCreatePermissionGroupMutation: () => ({
    mutateAsync: createMutateAsync,
    isPending: { value: false },
  }),
  useUpdatePermissionGroupMutation: () => ({
    mutateAsync: updateMutateAsync,
    isPending: { value: false },
  }),
  useDeletePermissionGroupMutation: () => ({
    mutateAsync: deleteMutateAsync,
    isPending: { value: false },
  }),
  useAssignPermissionGroupAccountMutation: () => ({
    mutateAsync: assignMutateAsync,
    isPending: { value: false },
  }),
  useUnassignPermissionGroupAccountMutation: () => ({
    mutateAsync: unassignMutateAsync,
    isPending: { value: false },
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

const FeatureSplitLayoutStub = defineComponent({
  setup(_, { slots }) {
    const pane = {
      isLargeScreen: { value: true },
      isSidebarCollapsed: { value: false },
      toggleSidebar: vi.fn(),
      closeSidebar: vi.fn(),
    };

    return () =>
      h('div', [
        h('div', { 'data-slot': 'sidebar' }, slots.sidebar?.({ pane })),
        h('div', { 'data-slot': 'content' }, slots.default?.({ pane })),
      ]);
  },
});

const DataTableStub = defineComponent({
  props: {
    data: {
      type: Array,
      default: () => [],
    },
  },
  setup(props) {
    return () =>
      h(
        'div',
        { 'data-testid': 'data-table' },
        Array.isArray(props.data) ? JSON.stringify(props.data) : '',
      );
  },
});

const ButtonStub = defineComponent({
  props: {
    disabled: Boolean,
    type: {
      type: String,
      default: 'button',
    },
  },
  emits: ['click'],
  setup(props, { emit, slots }) {
    return () =>
      h(
        'button',
        {
          type: props.type,
          disabled: props.disabled,
          onClick: (event: MouseEvent) => emit('click', event),
        },
        slots.default?.(),
      );
  },
});

const InputStub = defineComponent({
  props: {
    modelValue: {
      type: String,
      default: '',
    },
    placeholder: String,
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    return () =>
      h('input', {
        value: props.modelValue,
        placeholder: props.placeholder,
        onInput: (event: Event) =>
          emit('update:modelValue', (event.target as HTMLInputElement).value),
      });
  },
});

async function mountView() {
  return renderWithProviders(PermissionGroupManagementViewComponent, {
    global: {
      stubs: {
        Badge: PassThrough,
        Button: ButtonStub,
        DataTable: DataTableStub,
        DataTableColumnHeader: PassThrough,
        FeatureSplitLayout: FeatureSplitLayoutStub,
        Input: InputStub,
        ScrollArea: PassThrough,
        Select: PassThrough,
        SelectContent: PassThrough,
        SelectItem: PassThrough,
        SelectTrigger: PassThrough,
        SelectValue: PassThrough,
        Skeleton: PassThrough,
        Table: PassThrough,
        TableBody: PassThrough,
        TableCell: PassThrough,
        TableHead: PassThrough,
        TableHeader: PassThrough,
        TableRow: PassThrough,
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
        PermissionGroupEditorDialog: true,
        PermissionGroupAccountAssignDialog: true,
      },
    },
  });
}

describe('PermissionGroupManagementView', () => {
  beforeAll(async () => {
    vi.stubGlobal('localStorage', {
      getItem: vi.fn(() => null),
      setItem: vi.fn(),
      removeItem: vi.fn(),
      clear: vi.fn(),
      key: vi.fn(() => null),
      length: 0,
    } as Storage);
    PermissionGroupManagementViewComponent = (
      await import('@/features/admin/views/PermissionGroupManagementView.vue')
    ).default;
  });

  beforeEach(() => {
    vi.clearAllMocks();
    listData.value = [];
    detailData.value = null;
    assignableAccountsData.value = [];
  });

  it('시스템 그룹 상세에서는 수정과 삭제 버튼이 비활성화된다', async () => {
    listData.value = [
      {
        id: 1,
        name: '일반 그룹',
        description: '설명',
        groupType: 'SYSTEM',
        assignedAccountCount: 1,
        grantCount: 2,
      },
    ];
    detailData.value = {
      id: 1,
      name: '일반 그룹',
      description: '설명',
      groupType: 'SYSTEM',
      grants: [],
      accounts: [],
    };

    const { wrapper } = await mountView();
    const buttons = wrapper.findAll('button');
    const editButton = buttons.find((button) => button.text() === '수정');
    const deleteButton = buttons.find((button) => button.text() === '삭제');

    expect(wrapper.text()).toContain('시스템 그룹은 권한 구성과 기본 정보를 수정할 수 없고');
    expect(editButton?.attributes('disabled')).toBeDefined();
    expect(deleteButton?.attributes('disabled')).toBeDefined();
  });

  it('커스텀 그룹 상세에서는 권한과 소속 계정 정보를 보여준다', async () => {
    listData.value = [
      {
        id: 2,
        name: '운영 그룹',
        description: '설명',
        groupType: 'CUSTOM',
        assignedAccountCount: 1,
        grantCount: 1,
      },
    ];
    detailData.value = {
      id: 2,
      name: '운영 그룹',
      description: '운영 설명',
      groupType: 'CUSTOM',
      grants: [
        {
          permissionCode: 'employee.read',
          permissionName: '직원 조회',
          permissionDescription: '직원 상세 조회',
          scopes: ['SELF'],
        },
      ],
      accounts: [
        {
          accountId: 10,
          employeeId: 10,
          employeeName: '홍길동',
          email: 'hong@abacus.co.kr',
          departmentName: '개발팀',
          position: {
            code: 'ASSOCIATE',
            description: 'Associate',
            level: 1,
          },
        },
      ],
    };

    const { wrapper } = await mountView();

    expect(wrapper.text()).toContain('운영 그룹');
    expect(wrapper.text()).toContain('employee.read');
    expect(wrapper.text()).toContain('홍길동');
    expect(wrapper.findAll('button').find((button) => button.text() === '수정')?.attributes('disabled')).toBeUndefined();
  });
});
